import {
  Component,
  input,
  output,
  signal,
  computed,
  forwardRef,
  HostListener,
  OnInit,
  OnDestroy,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, ControlValueAccessor, NG_VALUE_ACCESSOR, AbstractControl } from '@angular/forms';
import { NgxMaskDirective, provideNgxMask } from 'ngx-mask';

export type FormFieldType = 'text' | 'email' | 'password' | 'number' | 'tel' | 'date' | 'datetime-local' | 'search' | 'url';
export type FormFieldSize = 'sm' | 'md' | 'lg';

export interface FormFieldOption {
  value: any;
  label: string;
  disabled?: boolean;
}

export interface FormFieldConfig {
  type?: FormFieldType;
  label?: string;
  placeholder?: string;
  hint?: string;
  error?: string;
  required?: boolean;
  disabled?: boolean;
  readonly?: boolean;
  size?: FormFieldSize;
  mask?: string;
  maskOptions?: Record<string, any>;
  prefix?: string;
  suffix?: string;
  options?: FormFieldOption[];
  multiple?: boolean;
  min?: number | string;
  max?: number | string;
  step?: number | string;
  minlength?: number;
  maxlength?: number;
  pattern?: string;
  autocomplete?: string;
  rows?: number; // for textarea
  icon?: string;
  iconPosition?: 'left' | 'right';
  clearable?: boolean;
  debounceTime?: number;
}

@Component({
  selector: 'app-form-field',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, NgxMaskDirective],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => FormFieldComponent),
      multi: true,
    },
    provideNgxMask(),
  ],
  templateUrl: './form-field.component.html',
  styleUrl: './form-field.component.scss',
})
export class FormFieldComponent implements ControlValueAccessor, OnInit, OnDestroy {
  config = input.required<FormFieldConfig>();
  control = input<AbstractControl | null>(null);
  name = input<string>('');
  id = input<string>('');

  blur = output<Event>();
  focus = output<Event>();
  valueChange = output<any>();
  clear = output<void>();
  iconClick = output<Event>();

  private _value = signal<any>(null);
  private _focused = signal(false);
  private _disabled = signal(false);
  private onChange: (value: any) => void = () => {};
  private onTouched: () => void = () => {};

  readonly value = computed(() => this._value());
  readonly focused = computed(() => this._focused());
  readonly disabled = computed(() => this._disabled() || this.config().disabled || this.control()?.disabled);
  readonly required = computed(() => this.config().required);
  readonly hasError = computed(() => {
    const ctrl = this.control();
    return !!(ctrl && ctrl.invalid && (ctrl.dirty || ctrl.touched));
  });
  readonly errorMessage = computed(() => {
    const ctrl = this.control();
    if (ctrl?.errors) {
      const errors = ctrl.errors;
      if (errors['required']) return 'Este campo é obrigatório';
      if (errors['email']) return 'E-mail inválido';
      if (errors['minlength']) return `Mínimo ${errors['minlength'].requiredLength} caracteres`;
      if (errors['maxlength']) return `Máximo ${errors['maxlength'].requiredLength} caracteres`;
      if (errors['pattern']) return 'Formato inválido';
      if (errors['min']) return `Valor mínimo: ${errors['min'].min}`;
      if (errors['max']) return `Valor máximo: ${errors['max'].max}`;
      if (errors['cpf']) return errors['cpf'].message;
      if (errors['cnpj']) return errors['cnpj'].message;
      if (errors['cep']) return errors['cep'].message;
      if (errors['phone']) return errors['phone'].message;
      if (errors['stateRegistration']) return errors['stateRegistration'].message;
      if (errors['cpfCnpj']) return errors['cpfCnpj'].message;
    }
    return this.config().error || '';
  });
  readonly showClear = computed(() => this.config().clearable && this._value() !== null && this._value() !== '' && !this.disabled());

  readonly inputId = computed(() => this.id() || `form-field-${this.name() || Math.random().toString(36).substr(2, 9)}`);
  readonly describedBy = computed(() => {
    const ids = [];
    if (this.hint()) ids.push(`${this.inputId()}-hint`);
    if (this.errorMessage()) ids.push(`${this.inputId()}-error`);
    return ids.length > 0 ? ids.join(' ') : undefined;
  });

  ngOnInit(): void {
    this._disabled.set(this.config().disabled ?? false);
  }

  ngOnDestroy(): void {}

  writeValue(value: any): void {
    this._value.set(value);
  }

  registerOnChange(fn: (value: any) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this._disabled.set(isDisabled);
  }

  onInput(event: Event): void {
    const target = event.target as HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement;
    let value: any = target.value;

    // Type conversion for number inputs
    if (this.config().type === 'number' && value !== '') {
      value = parseFloat(value);
      if (isNaN(value)) value = null;
    }

    this._value.set(value);
    this.onChange(value);
    this.valueChange.emit(value);
  }

  onBlur(event: Event): void {
    this._focused.set(false);
    this.onTouched();
    this.blur.emit(event);
  }

  onFocus(event: Event): void {
    this._focused.set(true);
    this.focus.emit(event);
  }

  onClear(event: Event): void {
    event.stopPropagation();
    this._value.set(null);
    this.onChange(null);
    this.valueChange.emit(null);
    this.clear.emit();
  }

  onIconClick(event: Event): void {
    this.iconClick.emit(event);
  }

  onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Escape' && this.showClear()) {
      this.onClear(event);
    }
  }

  getInputType(): string {
    const type = this.config().type || 'text';
    // Para date, usar type date nativo
    return type;
  }

  getInputMode(): string {
    const type = this.config().type;
    if (type === 'number') return 'numeric';
    if (type === 'tel') return 'tel';
    if (type === 'email') return 'email';
    return 'text';
  }

  isSelect(): boolean {
    return !!this.config().options && this.config().options!.length > 0;
  }

  isTextarea(): boolean {
    return this.config().type === 'textarea' || (this.config().rows && this.config().rows! > 1);
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: Event): void {
    // Fechar dropdowns se necessário
  }
}
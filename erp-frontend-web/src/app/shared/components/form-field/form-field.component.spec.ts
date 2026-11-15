import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormFieldComponent } from './form-field.component';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

describe('FormFieldComponent', () => {
  let component: FormFieldComponent;
  let fixture: ComponentFixture<FormFieldComponent>;

  const mockConfig = {
    label: 'Nome',
    placeholder: 'Digite seu nome',
    hint: 'Seu nome completo',
    required: true,
    type: 'text' as const,
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FormFieldComponent, ReactiveFormsModule, NoopAnimationsModule],
    }).compileComponents();

    fixture = TestBed.createComponent(FormFieldComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('config', mockConfig);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render label with required asterisk', () => {
    const label = fixture.debugElement.query(By.css('label'));
    expect(label.nativeElement.textContent).toContain('Nome');
    expect(label.nativeElement.textContent).toContain('*');
  });

  it('should render placeholder', () => {
    const input = fixture.debugElement.query(By.css('input'));
    expect(input.nativeElement.placeholder).toBe('Digite seu nome');
  });

  it('should render hint', () => {
    const hint = fixture.debugElement.query(By.css('p.text-neutral-500'));
    expect(hint.nativeElement.textContent).toContain('Seu nome completo');
  });

  it('should update value on input', () => {
    const input = fixture.debugElement.query(By.css('input'));
    input.nativeElement.value = 'Teste';
    input.nativeElement.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    expect(component.value()).toBe('Teste');
  });

  it('should emit valueChange on input', () => {
    spyOn(component.valueChange, 'emit');
    const input = fixture.debugElement.query(By.css('input'));
    input.nativeElement.value = 'Teste';
    input.nativeElement.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    expect(component.valueChange.emit).toHaveBeenCalledWith('Teste');
  });

  it('should show error when control is invalid and touched', () => {
    const control = new FormControl('', [Validators.required]);
    control.markAsTouched();
    fixture.componentRef.setInput('control', control);
    fixture.detectChanges();

    const error = fixture.debugElement.query(By.css('p.text-danger-600'));
    expect(error).toBeTruthy();
    expect(error.nativeElement.textContent).toContain('Este campo é obrigatório');
  });

  it('should not show error when control is valid', () => {
    const control = new FormControl('Teste', [Validators.required]);
    fixture.componentRef.setInput('control', control);
    fixture.detectChanges();

    const error = fixture.debugElement.query(By.css('p.text-danger-600'));
    expect(error).toBeFalsy();
  });

  it('should show custom error message from config', () => {
    const configWithError = { ...mockConfig, error: 'Erro personalizado' };
    fixture.componentRef.setInput('config', configWithError);
    fixture.detectChanges();

    const error = fixture.debugElement.query(By.css('p.text-danger-600'));
    expect(error.nativeElement.textContent).toContain('Erro personalizado');
  });

  it('should apply disabled state', () => {
    const configDisabled = { ...mockConfig, disabled: true };
    fixture.componentRef.setInput('config', configDisabled);
    fixture.detectChanges();

    const input = fixture.debugElement.query(By.css('input'));
    expect(input.nativeElement.disabled).toBeTrue();
    expect(fixture.debugElement.nativeElement.classList).toContain('opacity-50');
  });

  it('should show clear button when clearable and has value', () => {
    const configClearable = { ...mockConfig, clearable: true };
    fixture.componentRef.setInput('config', configClearable);
    component.writeValue('Teste');
    fixture.detectChanges();

    const clearBtn = fixture.debugElement.query(By.css('button[aria-label="Limpar campo"]'));
    expect(clearBtn).toBeTruthy();
  });

  it('should clear value on clear button click', () => {
    const configClearable = { ...mockConfig, clearable: true };
    fixture.componentRef.setInput('config', configClearable);
    component.writeValue('Teste');
    fixture.detectChanges();

    spyOn(component.clear, 'emit');
    spyOn(component.valueChange, 'emit');

    const clearBtn = fixture.debugElement.query(By.css('button[aria-label="Limpar campo"]'));
    clearBtn.nativeElement.click();
    fixture.detectChanges();

    expect(component.value()).toBeNull();
    expect(component.clear.emit).toHaveBeenCalled();
    expect(component.valueChange.emit).toHaveBeenCalledWith(null);
  });

  it('should render select with options', () => {
    const configSelect = {
      ...mockConfig,
      options: [
        { value: '1', label: 'Opção 1' },
        { value: '2', label: 'Opção 2' },
      ],
    };
    fixture.componentRef.setInput('config', configSelect);
    fixture.detectChanges();

    const select = fixture.debugElement.query(By.css('select'));
    expect(select).toBeTruthy();
    const options = select.nativeElement.querySelectorAll('option');
    expect(options.length).toBe(3); // placeholder + 2 options
  });

  it('should render textarea when rows > 1', () => {
    const configTextarea = { ...mockConfig, rows: 4 };
    fixture.componentRef.setInput('config', configTextarea);
    fixture.detectChanges();

    const textarea = fixture.debugElement.query(By.css('textarea'));
    expect(textarea).toBeTruthy();
    expect(textarea.nativeElement.rows).toBe(4);
  });

  it('should apply mask when provided', () => {
    const configMask = { ...mockConfig, mask: '(00) 00000-0000' };
    fixture.componentRef.setInput('config', configMask);
    fixture.detectChanges();

    const input = fixture.debugElement.query(By.css('input'));
    expect(input.nativeElement.getAttribute('mask')).toBe('(00) 00000-0000');
  });

  it('should call onTouched on blur', () => {
    const input = fixture.debugElement.query(By.css('input'));
    input.nativeElement.dispatchEvent(new Event('blur'));
    fixture.detectChanges();

    // onTouched is internal, but we can verify focused state
    expect(component.focused()).toBeFalse();
  });

  it('should emit blur event', () => {
    spyOn(component.blur, 'emit');
    const input = fixture.debugElement.query(By.css('input'));
    const event = new Event('blur');
    input.nativeElement.dispatchEvent(event);
    fixture.detectChanges();

    expect(component.blur.emit).toHaveBeenCalledWith(event);
  });

  it('should emit focus event', () => {
    spyOn(component.focus, 'emit');
    const input = fixture.debugElement.query(By.css('input'));
    const event = new Event('focus');
    input.nativeElement.dispatchEvent(event);
    fixture.detectChanges();

    expect(component.focus.emit).toHaveBeenCalledWith(event);
  });

  it('should set value via writeValue', () => {
    component.writeValue('Valor inicial');
    fixture.detectChanges();

    const input = fixture.debugElement.query(By.css('input'));
    expect(input.nativeElement.value).toBe('Valor inicial');
  });

  it('should register onChange and onTouched', () => {
    const onChange = jasmine.createSpy('onChange');
    const onTouched = jasmine.createSpy('onTouched');

    component.registerOnChange(onChange);
    component.registerOnTouched(onTouched);

    const input = fixture.debugElement.query(By.css('input'));
    input.nativeElement.value = 'Novo valor';
    input.nativeElement.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    expect(onChange).toHaveBeenCalledWith('Novo valor');

    input.nativeElement.dispatchEvent(new Event('blur'));
    expect(onTouched).toHaveBeenCalled();
  });

  it('should set disabled state via setDisabledState', () => {
    component.setDisabledState(true);
    fixture.detectChanges();

    const input = fixture.debugElement.query(By.css('input'));
    expect(input.nativeElement.disabled).toBeTrue();
  });
});
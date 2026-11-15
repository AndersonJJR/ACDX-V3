import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { BtnComponent } from '../../../shared/components/ui/btn/btn.component';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, BtnComponent],
  templateUrl: './forgot-password.component.html',
  styleUrl: './forgot-password.component.scss',
})
export class ForgotPasswordComponent {
  private fb = inject(FormBuilder);
  private router = inject(Router);

  isLoading = signal(false);
  successMessage = signal('');
  errorMessage = signal('');

  forgotForm: FormGroup = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
  });

  async onSubmit(): Promise<void> {
    if (this.forgotForm.invalid) {
      this.forgotForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set('');
    this.successMessage.set('');

    try {
      // TODO: Call forgot password API
      await new Promise(resolve => setTimeout(resolve, 1000));
      this.successMessage.set('E-mail de recuperação enviado! Verifique sua caixa de entrada.');
    } catch (error: any) {
      this.errorMessage.set('Erro ao solicitar recuperação');
    } finally {
      this.isLoading.set(false);
    }
  }

  goBackToLogin(): void {
    this.router.navigate(['/login']);
  }
}
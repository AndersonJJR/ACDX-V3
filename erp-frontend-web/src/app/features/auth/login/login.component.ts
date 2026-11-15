import { Component, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { BtnComponent } from '../../../shared/components/ui/btn/btn.component';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, BtnComponent],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  isLoading = signal(false);
  errorMessage = signal('');

  loginForm: FormGroup = this.fb.group({
    username: ['', [Validators.required]],
    password: ['', [Validators.required]],
    rememberMe: [false],
  });

  async onSubmit(): Promise<void> {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set('');

    try {
      const success = await this.authService.login(this.loginForm.value);
      if (success) {
        this.router.navigate(['/dashboard']);
      } else {
        this.errorMessage.set('Credenciais inválidas');
      }
    } catch (error: any) {
      this.errorMessage.set(error?.error?.detail || 'Erro ao fazer login');
    } finally {
      this.isLoading.set(false);
    }
  }

  navigateToForgotPassword(): void {
    this.router.navigate(['/forgot-password']);
  }
}
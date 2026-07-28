import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-register-page',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.page.html',
  styleUrl: './register.page.scss',
})
export class RegisterPage {
  nom = '';
  prenom = '';
  email = '';
  password = '';
  confirmPassword = '';
  showPassword = false;
  showConfirmPassword = false;

  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly success = signal<boolean>(false);

  constructor(
    private readonly auth: AuthService,
    private readonly router: Router,
  ) {}

  get hasMinLength(): boolean {
    return this.password.length >= 12;
  }

  get hasUppercase(): boolean {
    return /[A-Z]/.test(this.password);
  }

  get hasLowercase(): boolean {
    return /[a-z]/.test(this.password);
  }

  get hasNumber(): boolean {
    return /\d/.test(this.password);
  }

  get hasSpecial(): boolean {
    return /[^A-Za-z0-9]/.test(this.password);
  }

  get passwordsMatch(): boolean {
    return this.password === this.confirmPassword && this.password.length > 0;
  }

  get isPasswordValid(): boolean {
    return (
      this.hasMinLength &&
      this.hasUppercase &&
      this.hasLowercase &&
      this.hasNumber &&
      this.hasSpecial
    );
  }

  get isFormValid(): boolean {
    return (
      this.nom.trim().length > 0 &&
      this.prenom.trim().length > 0 &&
      this.email.trim().length > 0 &&
      this.isPasswordValid &&
      this.passwordsMatch
    );
  }

  onSubmit(form: NgForm) {
    if (form.invalid || !this.isFormValid || this.loading()) {
      return;
    }

    this.loading.set(true);
    this.error.set(null);
    this.success.set(false);

    this.auth
      .register({
        nom: this.nom,
        prenom: this.prenom,
        email: this.email,
        password: this.password,
      })
      .subscribe({
        next: () => {
          this.loading.set(false);
          this.success.set(true);
          form.resetForm();
          this.nom = '';
          this.prenom = '';
          this.email = '';
          this.password = '';
          this.confirmPassword = '';
        },
        error: (err) => {
          this.loading.set(false);
          if (err.error && err.error.message) {
            this.error.set(err.error.message);
          } else {
            this.error.set('Une erreur est survenue lors de l\'inscription.');
          }
        },
      });
  }
}

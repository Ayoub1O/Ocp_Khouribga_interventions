import { CommonModule } from '@angular/common';
import { Component, signal } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-forgot-password-page',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './forgot-password.page.html',
  styleUrl: './forgot-password.page.scss',
})
export class ForgotPasswordPage {
  protected email = '';
  protected readonly loading = signal(false);
  protected readonly message = signal<string | null>(null);
  protected readonly error = signal<string | null>(null);

  constructor(private readonly auth: AuthService) {}

  protected submit(form: NgForm): void {
    if (form.invalid || this.loading()) {
      return;
    }

    this.loading.set(true);
    this.error.set(null);
    this.message.set(null);

    this.auth.forgotPassword({ email: this.email.trim() }).subscribe({
      next: (response) => {
        this.loading.set(false);
        this.message.set(response.message);
      },
      error: () => {
        this.loading.set(false);
        this.error.set('Demande impossible pour le moment.');
      },
    });
  }
}

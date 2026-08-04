import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-reset-password-page',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './reset-password.page.html',
  styleUrl: './reset-password.page.scss',
})
export class ResetPasswordPage implements OnInit {
  protected token = '';
  protected newPassword = '';
  protected confirmPassword = '';
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly message = signal<string | null>(null);

  constructor(
    private readonly auth: AuthService,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token') ?? '';
    if (!this.token) {
      this.error.set('Lien de reinitialisation invalide.');
    }
  }

  protected submit(form: NgForm): void {
    if (form.invalid || this.loading() || !this.token) {
      return;
    }
    if (this.newPassword !== this.confirmPassword) {
      this.error.set('Les mots de passe ne correspondent pas.');
      return;
    }

    this.loading.set(true);
    this.error.set(null);
    this.message.set(null);

    this.auth.resetPassword({ token: this.token, newPassword: this.newPassword }).subscribe({
      next: (response) => {
        this.loading.set(false);
        this.message.set(response.message);
        setTimeout(() => void this.router.navigateByUrl('/login'), 1200);
      },
      error: () => {
        this.loading.set(false);
        this.error.set('Lien expire ou mot de passe non conforme.');
      },
    });
  }
}

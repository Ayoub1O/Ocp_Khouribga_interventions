import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { AuthService } from '../../../core/auth/auth.service';
import { CurrentUser } from '../../../core/auth/auth.models';

@Component({
  selector: 'app-profile-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profile.page.html',
  styleUrl: './profile.page.scss',
})
export class ProfilePage implements OnInit {
  protected readonly user = signal<CurrentUser | null>(null);
  protected readonly loading = signal(true);
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly success = signal<string | null>(null);

  protected profileForm = {
    nom: '',
    prenom: '',
    telephone: '',
  };

  protected passwordForm = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  };

  constructor(private readonly auth: AuthService) {}

  ngOnInit(): void {
    this.auth.me().subscribe({
      next: (user) => {
        this.user.set(user);
        this.profileForm = {
          nom: user.nom || '',
          prenom: user.prenom || '',
          telephone: user.telephone || '',
        };
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.error.set('Impossible de charger le profil.');
      },
    });
  }

  protected saveProfile(form: NgForm): void {
    if (form.invalid || this.saving()) {
      return;
    }

    this.saving.set(true);
    this.error.set(null);
    this.success.set(null);

    this.auth.updateProfile({
      nom: this.profileForm.nom.trim(),
      prenom: this.profileForm.prenom.trim(),
      telephone: this.profileForm.telephone.trim() || null,
    }).subscribe({
      next: (user) => {
        this.user.set(user);
        this.saving.set(false);
        this.success.set('Profil mis a jour.');
      },
      error: () => {
        this.saving.set(false);
        this.error.set('Impossible de mettre a jour le profil.');
      },
    });
  }

  protected changePassword(form: NgForm): void {
    if (form.invalid || this.saving()) {
      return;
    }
    if (this.passwordForm.newPassword !== this.passwordForm.confirmPassword) {
      this.error.set('Les mots de passe ne correspondent pas.');
      return;
    }

    this.saving.set(true);
    this.error.set(null);
    this.success.set(null);

    this.auth.changePassword({
      currentPassword: this.passwordForm.currentPassword,
      newPassword: this.passwordForm.newPassword,
    }).subscribe({
      next: () => {
        this.saving.set(false);
        this.success.set('Mot de passe mis a jour. Reconnexion requise.');
        setTimeout(() => this.auth.logout(), 1000);
      },
      error: () => {
        this.saving.set(false);
        this.error.set('Mot de passe actuel incorrect ou nouveau mot de passe non conforme.');
      },
    });
  }
}

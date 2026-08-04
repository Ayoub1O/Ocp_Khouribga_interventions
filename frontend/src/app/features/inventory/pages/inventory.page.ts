import { CommonModule } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { AuthService } from '../../../core/auth/auth.service';
import { Intervention } from '../../../core/interventions/interventions.models';
import { InterventionsService } from '../../../core/interventions/interventions.service';
import { InventoryService } from '../../../core/inventory/inventory.service';
import { SparePart, StockMovementType } from '../../../core/inventory/inventory.models';

@Component({
  selector: 'app-inventory-page',
  imports: [CommonModule, FormsModule, MatButtonModule],
  templateUrl: './inventory.page.html',
  styleUrl: './inventory.page.scss',
})
export class InventoryPage implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly inventoryService = inject(InventoryService);
  private readonly interventionsService = inject(InterventionsService);

  protected readonly parts = signal<SparePart[]>([]);
  protected readonly completedInterventions = signal<Intervention[]>([]);
  protected readonly selectedPart = signal<SparePart | null>(null);
  protected readonly loading = signal(true);
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly success = signal<string | null>(null);

  protected readonly canManageStock = computed(() => {
    const role = this.auth.currentUser()?.role;
    return role === 'TECH_N3' || role === 'ADMIN';
  });
  protected readonly isAdmin = computed(() => this.auth.currentUser()?.role === 'ADMIN');

  protected readonly alertCount = computed(() => this.parts().filter((part) => part.lowStock).length);
  protected readonly activeCount = computed(() => this.parts().filter((part) => part.actif).length);
  protected readonly totalQuantity = computed(() =>
    this.parts().reduce((total, part) => total + part.quantiteDisponible, 0),
  );

  protected partForm = {
    reference: '',
    nom: '',
    description: '',
    quantiteInitiale: 0,
    seuilAlerte: 1,
    actif: true,
  };

  protected movementForm = {
    type: 'SORTIE' as StockMovementType,
    quantite: 1,
    interventionId: '',
    commentaire: '',
  };

  ngOnInit(): void {
    this.load();
  }

  protected load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.loadCompletedInterventions();

    this.inventoryService.listParts().subscribe({
      next: (parts) => {
        this.parts.set(parts);
        this.loading.set(false);
        if (!this.selectedPart() && parts.length > 0) {
          this.selectPart(parts[0]);
        }
      },
      error: () => {
        this.loading.set(false);
        this.error.set('Impossible de charger le stock.');
      },
    });
  }

  protected selectPart(part: SparePart): void {
    this.selectedPart.set(part);
    this.partForm = {
      reference: part.reference,
      nom: part.nom,
      description: part.description ?? '',
      quantiteInitiale: part.quantiteDisponible,
      seuilAlerte: part.seuilAlerte,
      actif: part.actif,
    };
  }

  protected newPart(): void {
    this.selectedPart.set(null);
    this.partForm = {
      reference: '',
      nom: '',
      description: '',
      quantiteInitiale: 0,
      seuilAlerte: 1,
      actif: true,
    };
  }

  protected savePart(): void {
    if (!this.canManageStock() || this.saving()) {
      return;
    }

    this.saving.set(true);
    this.error.set(null);
    this.success.set(null);

    const selected = this.selectedPart();
    const operation = selected
      ? this.inventoryService.updatePart(selected.id, {
          nom: this.partForm.nom.trim(),
          description: this.partForm.description.trim(),
          seuilAlerte: Number(this.partForm.seuilAlerte),
          actif: this.partForm.actif,
        })
      : this.inventoryService.createPart({
          reference: this.partForm.reference.trim(),
          nom: this.partForm.nom.trim(),
          description: this.partForm.description.trim(),
          quantiteInitiale: Number(this.partForm.quantiteInitiale),
          seuilAlerte: Number(this.partForm.seuilAlerte),
        });

    operation.subscribe({
      next: (part) => {
        this.saving.set(false);
        this.success.set(selected ? 'Piece mise a jour.' : 'Piece creee.');
        this.selectedPart.set(part);
        this.load();
      },
      error: () => {
        this.saving.set(false);
        this.error.set('Operation stock impossible. Verifiez les champs et vos droits.');
      },
    });
  }

  protected createMovement(): void {
    const part = this.selectedPart();
    if (!part || !this.canManageStock() || this.saving()) {
      return;
    }

    if (this.movementForm.type === 'SORTIE' && !this.isAdmin() && !this.movementForm.interventionId) {
      this.error.set('Selectionnez une intervention terminee pour une sortie de stock.');
      return;
    }

    this.saving.set(true);
    this.error.set(null);
    this.success.set(null);

    this.inventoryService.createMovement(part.id, {
      type: this.movementForm.type,
      quantite: Number(this.movementForm.quantite),
      interventionId: this.movementForm.interventionId.trim() || null,
      commentaire: this.movementForm.commentaire.trim(),
    }).subscribe({
      next: () => {
        this.saving.set(false);
        this.success.set('Mouvement de stock enregistre.');
        this.movementForm = { type: 'SORTIE', quantite: 1, interventionId: '', commentaire: '' };
        this.load();
      },
      error: () => {
        this.saving.set(false);
        this.error.set('Mouvement impossible. Verifiez la quantite, le commentaire et l intervention si requise.');
      },
    });
  }

  protected interventionLabel(intervention: Intervention): string {
    const ticket = intervention.ticketReference || intervention.ticketTitre || intervention.ticketId.slice(0, 8).toUpperCase();
    const date = this.formatDateTime(intervention.dateFinReelle || intervention.dateFinPrevue);
    return `${ticket} - ${date} - ${intervention.lieu}`;
  }

  private loadCompletedInterventions(): void {
    if (!this.canManageStock()) {
      this.completedInterventions.set([]);
      return;
    }

    this.interventionsService.list().subscribe({
      next: (interventions) => {
        this.completedInterventions.set(
          interventions
            .filter((intervention) => intervention.statut === 'TERMINEE')
            .sort((left, right) =>
              this.timestamp(right.dateFinReelle || right.dateFinPrevue)
              - this.timestamp(left.dateFinReelle || left.dateFinPrevue),
            ),
        );
      },
      error: () => this.completedInterventions.set([]),
    });
  }

  private timestamp(value: string): number {
    return new Date(value).getTime();
  }

  private formatDateTime(value: string): string {
    const date = new Date(value);
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${day}/${month}/${year} ${hours}:${minutes}`;
  }
}

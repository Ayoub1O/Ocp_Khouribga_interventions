import { CommonModule } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { AuthService } from '../../../core/auth/auth.service';
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

  protected readonly parts = signal<SparePart[]>([]);
  protected readonly selectedPart = signal<SparePart | null>(null);
  protected readonly loading = signal(true);
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly success = signal<string | null>(null);

  protected readonly canManageStock = computed(() => {
    const role = this.auth.currentUser()?.role;
    return role === 'TECH_N3' || role === 'ADMIN';
  });

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
        this.error.set('Mouvement impossible. Seuls N3 et admin peuvent modifier le stock.');
      },
    });
  }
}

import { Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { spareParts } from '../../../core/api/mock-data';

@Component({
  selector: 'app-inventory-page',
  imports: [MatButtonModule],
  templateUrl: './inventory.page.html',
  styleUrl: './inventory.page.scss',
})
export class InventoryPage {
  protected readonly spareParts = spareParts;
  protected readonly alertCount = spareParts.filter((part) => part.statut === 'ALERTE').length;
}

import { Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration } from 'chart.js';

@Component({
  selector: 'app-reporting-page',
  imports: [MatButtonModule, BaseChartDirective],
  templateUrl: './reporting.page.html',
  styleUrl: './reporting.page.scss',
})
export class ReportingPage {
  protected readonly resolutionChart: ChartConfiguration<'line'>['data'] = {
    labels: ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam'],
    datasets: [
      {
        label: 'Tickets resolus',
        data: [8, 11, 9, 14, 13, 6],
        borderColor: '#0f766e',
        backgroundColor: 'rgba(15, 118, 110, 0.16)',
        fill: true,
        tension: 0.35,
      },
    ],
  };

  protected readonly categoryChart: ChartConfiguration<'bar'>['data'] = {
    labels: ['Reseau', 'Materiel', 'Email', 'Logiciel', 'Securite'],
    datasets: [
      {
        label: 'Incidents',
        data: [22, 14, 11, 9, 5],
        backgroundColor: '#2563eb',
        borderRadius: 6,
      },
    ],
  };
}

import { Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration } from 'chart.js';
import { tickets } from '../../../core/api/mock-data';

@Component({
  selector: 'app-dashboard-page',
  imports: [MatButtonModule, MatChipsModule, BaseChartDirective],
  templateUrl: './dashboard.page.html',
  styleUrl: './dashboard.page.scss',
})
export class DashboardPage {
  protected readonly tickets = tickets;
  protected readonly kpis = [
    { label: 'Tickets ouverts', value: 18, delta: '+4 depuis hier' },
    { label: 'SLA a risque', value: 5, delta: '2 critiques' },
    { label: 'Interventions du jour', value: 9, delta: '3 en cours' },
    { label: 'Pieces en alerte', value: 3, delta: 'stock bas' },
  ];

  protected readonly statusChart: ChartConfiguration<'doughnut'>['data'] = {
    labels: ['Ouverts', 'En cours', 'Escalades', 'Resolus'],
    datasets: [
      {
        data: [18, 11, 7, 24],
        backgroundColor: ['#2563eb', '#0f766e', '#f59e0b', '#22c55e'],
        borderWidth: 0,
      },
    ],
  };

  protected readonly levelChart: ChartConfiguration<'bar'>['data'] = {
    labels: ['N1', 'N2', 'N3'],
    datasets: [
      {
        label: 'Tickets',
        data: [14, 6, 3],
        backgroundColor: ['#0f766e', '#2563eb', '#f59e0b'],
        borderRadius: 6,
      },
    ],
  };
}

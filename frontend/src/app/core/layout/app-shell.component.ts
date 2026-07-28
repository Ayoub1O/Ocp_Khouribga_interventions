import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatMenuModule } from '@angular/material/menu';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import {
  LucideBell,
  LucideBot,
  LucideBoxes,
  LucideCalendarDays,
  LucideChartNoAxesCombined,
  LucideChevronLeft,
  LucideChevronRight,
  LucideClipboardList,
  LucideLayoutDashboard,
  LucideListChecks,
  LucideLogOut,
  LucidePlus,
  LucideShieldCheck,
  LucideTicket,
  LucideUser,
  LucideUsers,
} from '@lucide/angular';
import { AuthService } from '../auth/auth.service';
import { UserRole } from '../auth/auth.models';

type NavigationItem = {
  label: string;
  route: string;
  icon: string;
  roles: UserRole[];
  badge?: string;
};

@Component({
  selector: 'app-shell',
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatButtonModule,
    MatDividerModule,
    MatMenuModule,
    MatSidenavModule,
    MatToolbarModule,
    LucideBell,
    LucideBot,
    LucideBoxes,
    LucideCalendarDays,
    LucideChartNoAxesCombined,
    LucideChevronLeft,
    LucideChevronRight,
    LucideClipboardList,
    LucideLayoutDashboard,
    LucideListChecks,
    LucideLogOut,
    LucidePlus,
    LucideShieldCheck,
    LucideTicket,
    LucideUser,
    LucideUsers,
  ],
  templateUrl: './app-shell.component.html',
  styleUrl: './app-shell.component.scss',
})
export class AppShellComponent {
  protected readonly auth = inject(AuthService);
  private readonly allRoles: UserRole[] = ['ADMIN', 'TECH_N1', 'TECH_N2', 'TECH_N3', 'DEMANDEUR'];
  protected readonly collapsed = signal(false);
  protected readonly navWidth = computed(() => (this.collapsed() ? '84px' : '280px'));
  protected readonly currentUser = this.auth.currentUser;

  protected readonly navigation: NavigationItem[] = [
    { label: 'Tableau de bord', route: '/dashboard', icon: 'dashboard', roles: this.allRoles },
    { label: 'Mes tickets', route: '/tickets', icon: 'tickets', roles: this.allRoles },
    { label: 'Files support', route: '/queues', icon: 'queues', roles: ['TECH_N1', 'TECH_N2', 'TECH_N3', 'ADMIN'] },
    { label: 'Assistant N0', route: '/n0', icon: 'n0', roles: ['DEMANDEUR', 'ADMIN'] },
    { label: 'Interventions', route: '/interventions', icon: 'interventions', roles: ['TECH_N2', 'TECH_N3', 'ADMIN'] },
    { label: 'Stock', route: '/inventory', icon: 'stock', roles: ['TECH_N1', 'TECH_N2', 'TECH_N3', 'ADMIN'] },
    { label: 'Rapports', route: '/reporting', icon: 'reporting', roles: ['TECH_N1', 'TECH_N2', 'TECH_N3', 'ADMIN'] },
    { label: 'Administration', route: '/admin', icon: 'admin', roles: ['ADMIN'] },
  ];

  protected readonly visibleNavigation = computed(() => {
    const role = this.currentUser()?.role;
    return role ? this.navigation.filter((item) => item.roles.includes(role)) : [];
  });

  protected toggleCollapsed(): void {
    this.collapsed.update((value) => !value);
  }
}

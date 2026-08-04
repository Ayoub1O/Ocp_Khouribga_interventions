import { DatePipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatMenuModule } from '@angular/material/menu';
import { MatSidenavModule } from '@angular/material/sidenav';
import {
  LucideBell,
  LucideBookOpen,
  LucideBoxes,
  LucideCalendarDays,
  LucideChartNoAxesCombined,
  LucideChevronLeft,
  LucideChevronRight,
  LucideClipboardList,
  LucideLayoutDashboard,
  LucideLogOut,
  LucideSettings,
  LucideTicket,
  LucideUser,
  LucideUsers,
} from '@lucide/angular';
import { AuthService } from '../auth/auth.service';
import { UserRole } from '../auth/auth.models';
import { AppNotification } from '../notifications/notifications.models';
import { NotificationsService } from '../notifications/notifications.service';

type NavigationItem = {
  label: string;
  route: string;
  icon: string;
  roles: UserRole[];
};

@Component({
  selector: 'app-shell',
  imports: [
    DatePipe,
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatButtonModule,
    MatDividerModule,
    MatMenuModule,
    MatSidenavModule,
    LucideBell,
    LucideBookOpen,
    LucideBoxes,
    LucideCalendarDays,
    LucideChartNoAxesCombined,
    LucideChevronLeft,
    LucideChevronRight,
    LucideClipboardList,
    LucideLayoutDashboard,
    LucideLogOut,
    LucideSettings,
    LucideTicket,
    LucideUser,
    LucideUsers,
  ],
  templateUrl: './app-shell.component.html',
  styleUrl: './app-shell.component.scss',
})
export class AppShellComponent implements OnInit {
  protected readonly auth = inject(AuthService);
  private readonly notificationsService = inject(NotificationsService);
  private readonly router = inject(Router);
  private readonly allRoles: UserRole[] = ['ADMIN', 'TECH_N1', 'TECH_N2', 'TECH_N3', 'DEMANDEUR'];
  protected readonly collapsed = signal(false);
  protected readonly navWidth = computed(() => (this.collapsed() ? '84px' : '280px'));
  protected readonly currentUser = this.auth.currentUser;
  protected readonly notifications = signal<AppNotification[]>([]);
  protected readonly notificationsLoading = signal(false);
  protected readonly unreadCount = computed(() =>
    this.notifications().filter((notification) => !notification.readAt).length,
  );

  protected readonly navigation: NavigationItem[] = [
    { label: 'Tableau de bord', route: '/dashboard', icon: 'dashboard', roles: this.allRoles },
    { label: 'Tickets', route: '/tickets', icon: 'tickets', roles: this.allRoles },
    { label: 'Files support', route: '/queues', icon: 'queues', roles: ['TECH_N1', 'TECH_N2', 'TECH_N3', 'ADMIN'] },
    { label: 'AssistEX', route: '/n0', icon: 'n0', roles: ['DEMANDEUR', 'ADMIN'] },
    { label: 'Base de connaissances', route: '/knowledge', icon: 'knowledge', roles: ['ADMIN'] },
    { label: 'Equipe', route: '/admin', icon: 'team', roles: ['ADMIN'] },
    { label: 'Interventions', route: '/interventions', icon: 'interventions', roles: ['TECH_N2', 'TECH_N3', 'ADMIN'] },
    { label: 'Stock', route: '/inventory', icon: 'stock', roles: ['TECH_N1', 'TECH_N2', 'TECH_N3', 'ADMIN'] },
    { label: 'Rapports', route: '/reporting', icon: 'reporting', roles: ['TECH_N1', 'TECH_N2', 'TECH_N3', 'ADMIN'] },
    { label: 'Parametres', route: '/admin', icon: 'settings', roles: ['ADMIN'] },
  ];

  protected readonly visibleNavigation = computed(() => {
    const role = this.currentUser()?.role;
    return role ? this.navigation.filter((item) => item.roles.includes(role)) : [];
  });

  ngOnInit(): void {
    this.loadNotifications();
  }

  protected toggleCollapsed(): void {
    this.collapsed.update((value) => !value);
  }

  protected loadNotifications(): void {
    if (!this.currentUser()) {
      return;
    }

    this.notificationsLoading.set(true);
    this.notificationsService.list().subscribe({
      next: (notifications) => {
        this.notifications.set(notifications);
        this.notificationsLoading.set(false);
      },
      error: () => this.notificationsLoading.set(false),
    });
  }

  protected openNotification(notification: AppNotification): void {
    const navigate = () => {
      if (notification.resourceType === 'TICKET') {
        void this.router.navigateByUrl('/tickets');
      } else if (notification.resourceType === 'INTERVENTION') {
        void this.router.navigateByUrl('/interventions');
      } else if (notification.resourceType === 'STOCK') {
        void this.router.navigateByUrl('/inventory');
      }
    };

    if (notification.readAt) {
      navigate();
      return;
    }

    this.notificationsService.markRead(notification.id).subscribe({
      next: (updated) => {
        this.notifications.update((items) =>
          items.map((item) => (item.id === updated.id ? updated : item)),
        );
        navigate();
      },
      error: navigate,
    });
  }
}

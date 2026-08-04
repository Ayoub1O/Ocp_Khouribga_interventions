export type NotificationType =
  | 'TICKET_CREE'
  | 'TICKET_PRIS_EN_CHARGE'
  | 'TICKET_ESCALADE'
  | 'TICKET_RESOLU'
  | 'INTERVENTION_PLANIFIEE'
  | 'INTERVENTION_TERMINEE'
  | 'STOCK_ALERTE';

export interface AppNotification {
  id: string;
  type: NotificationType;
  title: string;
  message: string;
  resourceType: string | null;
  resourceId: string | null;
  createdAt: string;
  readAt: string | null;
}

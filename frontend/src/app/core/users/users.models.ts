import { UserRole } from '../auth/auth.models';

export interface UserAccount {
  id: string;
  nom: string;
  prenom: string;
  email: string;
  telephone: string | null;
  role: UserRole;
  actif: boolean;
  emailVerified: boolean;
  dateCreation: string;
}

export interface InviteUserRequest {
  nom: string;
  prenom: string;
  email: string;
  telephone?: string;
  role: Exclude<UserRole, 'DEMANDEUR'>;
}

export interface PendingInvitation {
  id: string;
  userId: string;
  nom: string;
  prenom: string;
  email: string;
  telephone: string | null;
  invitedRole: Exclude<UserRole, 'DEMANDEUR' | 'ADMIN'>;
  invitedBy: string;
  createdAt: string;
  expiresAt: string;
}

export type UserRole = 'DEMANDEUR' | 'TECH_N1' | 'TECH_N2' | 'TECH_N3' | 'ADMIN';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  accessTokenExpiresAt: string;
  refreshTokenExpiresAt: string;
  userId: string;
  email: string;
  telephone?: string | null;
  role: UserRole;
}

export interface CurrentUser {
  id: string;
  nom?: string;
  prenom?: string;
  email: string;
  telephone?: string | null;
  role: UserRole;
}

export interface RegisterRequest {
  nom: string;
  prenom: string;
  email: string;
  telephone?: string;
  password: string;
}

export interface MessageResponse {
  message: string;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export interface UpdateProfileRequest {
  nom: string;
  prenom: string;
  telephone?: string | null;
}

export interface AcceptInvitationRequest {
  token: string;
  password: string;
}

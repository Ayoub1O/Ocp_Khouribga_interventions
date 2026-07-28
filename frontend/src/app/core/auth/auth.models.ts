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
  role: UserRole;
}

export interface CurrentUser {
  id: string;
  email: string;
  role: UserRole;
}

export interface RegisterRequest {
  nom: string;
  prenom: string;
  email: string;
  password: string;
}

export interface MessageResponse {
  message: string;
}

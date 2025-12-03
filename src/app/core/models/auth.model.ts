export interface User {
  email: string;
  companyName?: string;
  fullName?: string;
  role?: string;
  isAuthenticated: boolean;
}

export interface LoginCredentials {
  email: string;
  password: string;
  rememberMe?: boolean;
}

export interface RegisterData {
  companyName: string;
  email: string;
  password: string;
}
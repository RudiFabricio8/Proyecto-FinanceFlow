// Backend API Response Types
export interface AuthResponse {
  token: string;
  user: UserDTO;
  organization: OrganizationDTO;
}

export interface UserDTO {
  id: string;
  email: string;
  fullName: string;
  role: string;
}

export interface OrganizationDTO {
  id: string;
  name: string;
}

// Frontend Models
export interface User {
  id?: string;
  email: string;
  organizationName?: string;
  organizationId?: string;
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
  organizationName: string;  // Changed from companyName to match backend
  email: string;
  password: string;
  fullName: string;
}
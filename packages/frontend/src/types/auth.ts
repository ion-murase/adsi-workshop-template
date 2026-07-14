import { Role } from './api';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  userId: string;
  name: string;
  email: string;
  role: Role;
  requirePasswordChange: boolean;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export interface UserRequest {
  email: string;
  name: string;
  role: Role;
  primaryDepartmentId: string;
}

export interface UserResponse {
  id: string;
  email: string;
  name: string;
  role: Role;
  primaryDepartmentId: string;
  active: boolean;
  requirePasswordChange: boolean;
}

export interface DepartmentResponse {
  id: string;
  name: string;
  siteId: string;
}

export interface DepartmentRequest {
  name: string;
  siteId: string;
}

export interface SiteResponse {
  id: string;
  name: string;
  timezone: string;
}

export interface SiteRequest {
  name: string;
  timezone: string;
}

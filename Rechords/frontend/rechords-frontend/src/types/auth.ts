export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  firstname: string;
  lastname: string;
  country: string;
}

export interface AuthResponse {
  token: string;
}
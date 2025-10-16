import axios from 'axios';
import type { LoginRequest, RegisterRequest, AuthResponse } from '../types/auth';
import { API_CONFIG } from '../config/api';

// Cliente base de Axios con configuración del backend
const apiClient = axios.create({
  baseURL: API_CONFIG.BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: API_CONFIG.TIMEOUT,
});

// Interceptor para agregar el token a las peticiones
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('auth-storage');
    if (token) {
      try {
        const parsedToken = JSON.parse(token);
        
        // Verificar si el token ha expirado
        if (parsedToken?.state?.expiresAt) {
          const now = Date.now();
          if (now > parsedToken.state.expiresAt) {
            // Token expirado, limpiar y redirigir
            localStorage.removeItem('auth-storage');
            window.location.href = '/login';
            return Promise.reject(new Error('Token expirado'));
          }
        }
        
        if (parsedToken?.state?.token) {
          config.headers.Authorization = `Bearer ${parsedToken.state.token}`;
        }
      } catch (error) {
        console.error('Error parsing token:', error);
        localStorage.removeItem('auth-storage');
        window.location.href = '/login';
        return Promise.reject(error);
      }
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Interceptor para manejar errores de respuesta
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    // Si el servidor responde 401 o 403, redirigir a login
    if (error.response?.status === 401 || error.response?.status === 403) {
      localStorage.removeItem('auth-storage');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export const loginUser = async (credentials: LoginRequest): Promise<AuthResponse> => {
  const response = await apiClient.post<AuthResponse>(API_CONFIG.ENDPOINTS.AUTH.LOGIN, credentials);
  return response.data;
};

export const registerUser = async (userData: RegisterRequest): Promise<AuthResponse> => {
  const response = await apiClient.post<AuthResponse>(API_CONFIG.ENDPOINTS.AUTH.REGISTER, userData);
  return response.data;
};

export const getMySongs = async () => {
  const response = await apiClient.get('/api/songs/my');
  return response.data.content || [];
};

export default apiClient;
// src/store/authStore.ts
import { create } from 'zustand';
import { persist } from 'zustand/middleware';

// Función para decodificar el JWT y extraer el payload
const decodeToken = (token: string): any => {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload);
  } catch (error) {
    console.error('Error decoding token:', error);
    return null;
  }
};

interface AuthState {
  token: string | null;
  expiresAt: number | null;
  userRole: string | null;
  setToken: (token: string) => void;
  logout: () => void;
  isTokenExpired: () => boolean;
  isAdmin: () => boolean;
  getUsername: () => string | null;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      token: null,
      expiresAt: null,
      userRole: null,
      setToken: (token) => {
        // El token expira en 7 días desde ahora
        const expiresAt = Date.now() + (7 * 24 * 60 * 60 * 1000);
        
        // Decodificar token para extraer el rol
        const decoded = decodeToken(token);
        // Buscar el rol en diferentes posibles ubicaciones del payload
        const userRole = decoded?.role || decoded?.authorities?.[0] || null;
        
        set({ token, expiresAt, userRole });
      },
      logout: () => set({ token: null, expiresAt: null, userRole: null }),
      isTokenExpired: () => {
        const { token, expiresAt } = get();
        
        // Si hay token pero no hay expiresAt, es un token antiguo
        // Establecer expiresAt para 7 días desde ahora
        if (token && !expiresAt) {
          const newExpiresAt = Date.now() + (7 * 24 * 60 * 60 * 1000);
          set({ expiresAt: newExpiresAt });
          return false; // No está expirado, acabamos de renovarlo
        }
        
        // Si no hay expiresAt y no hay token, está expirado
        if (!expiresAt) return true;
        
        // Verificar si ya expiró
        return Date.now() > expiresAt;
      },
      isAdmin: () => {
        const { userRole } = get();
        return userRole === 'ADMIN';
      },
      getUsername: () => {
        const { token } = get();
        if (!token) return null;
        
        const decoded = decodeToken(token);
        return decoded?.sub || null; // 'sub' es el claim estándar de JWT para username
      },
    }),
    {
      name: 'auth-storage',
    }
  )
);
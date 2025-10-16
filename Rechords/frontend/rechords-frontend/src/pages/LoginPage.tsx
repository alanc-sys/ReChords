import React, { useState } from 'react';
import { loginUser } from '../api/authApi';
import { useAuthStore } from '../store/authStore';
import type { LoginRequest } from '../types/auth';
import { Link, useNavigate } from 'react-router-dom';

// Componente para los estilos. En un proyecto más grande, esto iría en un archivo .css.
const VintageStyles: React.FC = () => (
  <style>{`
    /* Importa las fuentes de Google Fonts (asegúrate de que también estén en tu index.html) */
    @import url('https://fonts.googleapis.com/css2?family=Playfair+Display:wght@700&family=Lato:wght@400;700&display=swap');
    @import url('https://fonts.googleapis.com/icon?family=Material+Icons');

    :root {
      --primary-color: #6B4F4F;
      --secondary-color: #A8875B;
      --accent-color: #4C573F;
      --bg-color: #F5EFE6;
      --dark-text: #3D3522;
    }
    body {
      font-family: 'Lato', sans-serif;
      background-color: var(--bg-color);
    }
    .playfair-font {
      font-family: 'Playfair Display', serif;
    }
    .grainy-texture {
      position: fixed;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      pointer-events: none;
      background-image: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 800"><filter id="noise"><feTurbulence type="fractalNoise" baseFrequency="0.65" numOctaves="3" stitchTiles="stitch"/></filter><rect width="100%" height="100%" filter="url(%23noise)"/></svg>');
      opacity: 0.1;
      z-index: 100;
    }
    .vintage-title {
      color: var(--dark-text);
      text-shadow: 2px 2px 0px var(--secondary-color), 4px 4px 0px rgba(0, 0, 0, 0.1);
    }
    .form-container {
      border: 4px solid var(--dark-text);
      box-shadow: 8px 8px 0px var(--primary-color);
      transform: rotate(-1deg);
    }
    .form-container-inner {
      transform: rotate(1deg);
    }
    .vinyl-record-vintage {
      position: absolute;
      width: 500px;
      height: 500px;
      border-radius: 50%;
      background-image: radial-gradient(circle at center, #3D3522 2%, var(--secondary-color) 2.2%, var(--secondary-color) 12%, #3D3522 12.2%), repeating-conic-gradient(from 0deg, #222 0deg 0.5deg, #333 0.5deg 1deg);
      box-shadow: 0 0 40px 10px rgba(0, 0, 0, 0.3);
      animation: spin-slow 25s linear infinite;
    }
    @keyframes spin-slow {
      from { transform: rotate(0deg); }
      to { transform: rotate(360deg); }
    }
  `}</style>
);

export const LoginPage: React.FC = () => {
  const [credentials, setCredentials] = useState<LoginRequest>({ username: '', password: '' });
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const { setToken } = useAuthStore();
  const navigate = useNavigate();

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setCredentials({ ...credentials, [e.target.name]: e.target.value });
    // Limpiar error cuando el usuario empieza a escribir
    if (error) setError(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setIsLoading(true);
    
    try {
      const response = await loginUser(credentials);
      setToken(response.token);
      
      // Limpiar formulario
      setCredentials({ username: '', password: '' });
      
      // Redirigir a la página principal
      navigate('/home');
    } catch (err: any) {
      // Manejo específico de errores
      if (err.response?.status === 401 || err.response?.status === 403) {
        setError('❌ Usuario o contraseña incorrectos. Verifica tus credenciales.');
      } else if (err.code === 'ECONNREFUSED' || err.message?.includes('Network Error')) {
        setError('⚠️ No se puede conectar al servidor. Verifica que el backend esté ejecutándose.');
      } else {
        setError('❌ Error al iniciar sesión. Intenta nuevamente.');
      }
      console.error('Login error:', err);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <>
      <VintageStyles />
      <div className="flex items-center justify-center min-h-screen p-4 overflow-hidden">
        <div className="grainy-texture"></div>

        {/* --- Efectos de Fondo --- */}
        <div className="absolute top-[-100px] left-[-150px] opacity-40">
          <div className="vinyl-record-vintage"></div>
        </div>
        <div className="absolute bottom-[-150px] right-[-100px] opacity-30 transform scale-x-[-1] rotate-180">
          <div className="vinyl-record-vintage" style={{ animationDuration: '35s' }}></div>
        </div>

        {/* --- Contenedor del Formulario --- */}
        <div className="relative z-10 w-full max-w-sm md:max-w-md">
          <div className="bg-[var(--bg-color)]/80 backdrop-blur-sm p-8 rounded-sm form-container">
            <div className="form-container-inner">
              
              <div className="flex justify-center">
                <h1 className="playfair-font text-6xl md:text-7xl text-center mb-2 vintage-title">RECHORDS</h1>
              </div>
              <h2 className="text-xl font-semibold text-center text-[var(--accent-color)] mb-8 tracking-widest uppercase">El Sonido del Ayer, Hoy</h2>

              <form onSubmit={handleSubmit} className="space-y-6">
                
                {/* --- Campo de Usuario --- */}
                <div className="relative group">
                  <label className="block text-sm font-bold text-[var(--primary-color)] mb-2 text-left tracking-wider" htmlFor="username">
                    Alias de Artista
                  </label>
                  <div className="relative">
                    <i className="material-icons absolute left-3 top-1/2 -translate-y-1/2 text-[var(--accent-color)]">mic</i>
                    <input
                      className="w-full pl-12 pr-4 py-3 bg-transparent border-2 border-[var(--primary-color)] rounded-none text-[var(--dark-text)] focus:outline-none focus:border-[var(--accent-color)] focus:ring-0 transition-all duration-300"
                      id="username"
                      name="username"
                      value={credentials.username}
                      onChange={handleChange}
                      placeholder="Tu Nombre de Época"
                      type="text"
                      required
                    />
                  </div>
                </div>

                {/* --- Campo de Contraseña --- */}
                <div className="relative group">
                  <label className="block text-sm font-bold text-[var(--primary-color)] mb-2 text-left tracking-wider" htmlFor="password">
                    Clave Maestra
                  </label>
                  <div className="relative">
                    <i className="material-icons absolute left-3 top-1/2 -translate-y-1/2 text-[var(--accent-color)]">vpn_key</i>
                    <input
                      className="w-full pl-12 pr-4 py-3 bg-transparent border-2 border-[var(--primary-color)] rounded-none text-[var(--dark-text)] focus:outline-none focus:border-[var(--accent-color)] focus:ring-0 transition-all duration-300"
                      id="password"
                      name="password"
                      value={credentials.password}
                      onChange={handleChange}
                      placeholder="••••••••••"
                      type="password"
                      required
                    />
                  </div>
                </div>

                {error && <p className="text-sm text-center text-red-700 font-bold">{error}</p>}
                
                <button
                  className="w-full bg-[var(--primary-color)] text-[var(--bg-color)] py-3 rounded-sm playfair-font text-2xl tracking-wider hover:bg-[var(--accent-color)] transition-all duration-300 shadow-md shadow-black/20 transform hover:scale-105 active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none"
                  type="submit"
                  disabled={isLoading}
                >
                  {isLoading ? 'INGRESANDO...' : 'INICIAR SESIÓN'}
                </button>
                
                <p className="text-sm text-center text-[var(--dark-text)] pt-4">
                  ¿Aún no tienes tu pase al club?{' '}
                  <Link to="/register" className="font-bold text-[var(--accent-color)] hover:text-[var(--primary-color)] underline">
                    Regístrate
                  </Link>
                </p>

              </form>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};
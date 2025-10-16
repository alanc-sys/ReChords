import React, { useState } from 'react';
import { registerUser } from '../api/authApi';
import { useAuthStore } from '../store/authStore';
import type { RegisterRequest } from '../types/auth';
import { Link, useNavigate } from 'react-router-dom';

// Reutilizamos los mismos estilos que en el login. No es necesario declararlos de nuevo si están en el mismo CSS.
const VintageStyles: React.FC = () => (
    <style>{`
      /* ... (Copia aquí los mismos estilos CSS del componente LoginPage.tsx) ... */
      /* O mejor aún, mueve estos estilos a tu archivo src/index.css para no repetirlos */
      :root {
        --primary-color: #6B4F4F;
        --secondary-color: #A8875B;
        --accent-color: #4C573F;
        --bg-color: #F5EFE6;
        --dark-text: #3D3522;
      }
      body { font-family: 'Lato', sans-serif; background-color: var(--bg-color); }
      .playfair-font { font-family: 'Playfair Display', serif; }
      .grainy-texture { position: fixed; top: 0; left: 0; width: 100%; height: 100%; pointer-events: none; background-image: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 800"><filter id="noise"><feTurbulence type="fractalNoise" baseFrequency="0.65" numOctaves="3" stitchTiles="stitch"/></filter><rect width="100%" height="100%" filter="url(%23noise)"/></svg>'); opacity: 0.1; z-index: 100; }
      .vintage-title { color: var(--dark-text); text-shadow: 2px 2px 0px var(--secondary-color), 4px 4px 0px rgba(0, 0, 0, 0.1); }
      .form-container { border: 4px solid var(--dark-text); box-shadow: 8px 8px 0px var(--primary-color); transform: rotate(-1deg); }
      .form-container-inner { transform: rotate(1deg); }
      .vinyl-record-vintage { position: absolute; width: 500px; height: 500px; border-radius: 50%; background-image: radial-gradient(circle at center, #3D3522 2%, var(--secondary-color) 2.2%, var(--secondary-color) 12%, #3D3522 12.2%), repeating-conic-gradient(from 0deg, #222 0deg 0.5deg, #333 0.5deg 1deg); box-shadow: 0 0 40px 10px rgba(0, 0, 0, 0.3); animation: spin-slow 25s linear infinite; }
      @keyframes spin-slow { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
    `}</style>
);


export const RegisterPage: React.FC = () => {
  const [userData, setUserData] = useState<RegisterRequest>({
    username: '',
    password: '',
    firstname: '',
    lastname: '',
    country: ''
  });
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const { setToken } = useAuthStore();
  const navigate = useNavigate();

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setUserData({ ...userData, [e.target.name]: e.target.value });
    // Limpiar error cuando el usuario empieza a escribir
    if (error) setError(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    
    // Validar que las contraseñas coincidan
    if (userData.password !== confirmPassword) {
      setError('❌ Las contraseñas no coinciden. Verifica e intenta nuevamente.');
      return;
    }

    // Validar longitud mínima de contraseña
    if (userData.password.length < 6) {
      setError('❌ La contraseña debe tener al menos 6 caracteres.');
      return;
    }
    
    setIsLoading(true);
    
    try {
      const response = await registerUser(userData);
      setToken(response.token);
      
      // Limpiar formulario
      setUserData({
        username: '',
        password: '',
        firstname: '',
        lastname: '',
        country: ''
      });
      setConfirmPassword('');
      
      // Redirigir a la página principal
      navigate('/home');
    } catch (err: any) {
      // Manejo detallado de errores
      if (err.code === 'ECONNREFUSED' || err.message?.includes('Network Error')) {
        setError('⚠️ No se puede conectar al servidor. Verifica que el backend esté ejecutándose.');
      } else if (err.response?.status === 400) {
        setError('❌ ' + (err.response?.data?.message || 'Datos inválidos. Verifica todos los campos.'));
      } else if (err.response?.status === 409) {
        setError('❌ El nombre de usuario ya existe. Elige otro.');
      } else if (err.response?.status === 500) {
        setError('❌ Error en el servidor. Intenta nuevamente más tarde.');
      } else {
        setError('❌ ' + (err.response?.data?.message || 'Error en el registro. Inténtalo de nuevo.'));
      }
      console.error('Register error:', err);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <>
      <VintageStyles />
      <div className="flex items-center justify-center min-h-screen p-4 overflow-hidden">
        <div className="grainy-texture"></div>
        <div className="absolute top-[-100px] left-[-150px] opacity-40">
          <div className="vinyl-record-vintage"></div>
        </div>
        <div className="absolute bottom-[-150px] right-[-100px] opacity-30 transform scale-x-[-1] rotate-180">
          <div className="vinyl-record-vintage" style={{ animationDuration: '35s' }}></div>
        </div>

        <div className="relative z-10 w-full max-w-sm md:max-w-md">
          <div className="bg-[var(--bg-color)]/80 backdrop-blur-sm p-8 rounded-sm form-container">
            <div className="form-container-inner">
              <div className="flex justify-center">
                <h1 className="playfair-font text-5xl md:text-6xl text-center mb-2 vintage-title">ÚNETE AL CLUB</h1>
              </div>
              <h2 className="text-lg font-semibold text-center text-[var(--accent-color)] mb-6 tracking-widest uppercase">Crea tu Identidad Sonora</h2>

              <form onSubmit={handleSubmit} className="space-y-4">
                {/* --- Campos Adicionales para el Registro --- */}
                <div className="flex space-x-4">
                    <InputField icon="person" label="Nombre" name="firstname" value={userData.firstname} onChange={handleChange} placeholder="Tu Nombre"/>
                    <InputField icon="group" label="Apellido" name="lastname" value={userData.lastname} onChange={handleChange} placeholder="Tu Apellido"/>
                </div>
                <InputField icon="public" label="País" name="country" value={userData.country} onChange={handleChange} placeholder="País de Origen"/>
                <InputField icon="mic" label="Alias de Artista" name="username" value={userData.username} onChange={handleChange} placeholder="Tu Nombre de Época"/>
                <InputField icon="vpn_key" label="Clave Maestra" name="password" value={userData.password} onChange={handleChange} placeholder="••••••••••" type="password"/>
                <InputField 
                  icon="lock" 
                  label="Confirmar Clave" 
                  name="confirmPassword" 
                  value={confirmPassword} 
                  onChange={(e) => {
                    setConfirmPassword(e.target.value);
                    if (error) setError(null);
                  }} 
                  placeholder="••••••••••" 
                  type="password"
                />

                {error && <p className="text-sm text-center text-red-700 font-bold">{error}</p>}
                
                <button 
                  type="submit" 
                  className="w-full bg-[var(--primary-color)] text-[var(--bg-color)] py-3 rounded-sm playfair-font text-2xl tracking-wider hover:bg-[var(--accent-color)] transition-all duration-300 shadow-md shadow-black/20 transform hover:scale-105 active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none"
                  disabled={isLoading}
                >
                  {isLoading ? 'CREANDO CUENTA...' : 'CREAR CUENTA'}
                </button>
                
                <p className="text-sm text-center text-[var(--dark-text)] pt-4">
                  ¿Ya tienes tu pase?{' '}
                  <Link to="/login" className="font-bold text-[var(--accent-color)] hover:text-[var(--primary-color)] underline">
                    Inicia Sesión
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

// Componente auxiliar para no repetir el código de los inputs
const InputField: React.FC<{
  icon: string;
  label: string;
  name: string;
  value: string;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  placeholder: string;
  type?: string;
}> = ({ icon, label, name, value, onChange, placeholder, type = 'text' }) => (
    <div className="relative group w-full">
        <label className="block text-sm font-bold text-[var(--primary-color)] mb-2 text-left tracking-wider" htmlFor={name}>
            {label}
        </label>
        <div className="relative">
            <i className="material-icons absolute left-3 top-1/2 -translate-y-1/2 text-[var(--accent-color)]">{icon}</i>
            <input
                className="w-full pl-12 pr-4 py-3 bg-transparent border-2 border-[var(--primary-color)] rounded-none text-[var(--dark-text)] focus:outline-none focus:border-[var(--accent-color)] focus:ring-0 transition-all duration-300"
                id={name}
                name={name}
                value={value}
                onChange={onChange}
                placeholder={placeholder}
                type={type}
                required
            />
        </div>
    </div>
);
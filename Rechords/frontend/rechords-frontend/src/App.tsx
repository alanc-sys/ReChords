import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { HomePage } from './pages/HomePage';
import { CreateSongPage } from './pages/CreateSongPage';
import { EditSongPage } from './pages/EditSongPage';
import { ImportSongPage } from './pages/ImportSongPage';
import { ViewSongPage } from './pages/ViewSongPage';
import { AdminPage } from './pages/AdminPage';
import { MyPlaylistsPage } from './pages/MyPlaylistsPage';
import { ViewPlaylistPage } from './pages/ViewPlaylistPage';
import { PublicSongsPage } from './pages/PublicSongsPage';
import { useAuthStore } from './store/authStore';
import { TunerPage } from './pages/TunerPage';

// Componente para proteger rutas
const ProtectedRoute = ({ children }: { children: React.ReactNode }) => {
  const { token, isTokenExpired, logout } = useAuthStore();
  
  // Si no hay token, redirigir sin mensaje
  if (!token) {
    return <Navigate to="/login" replace />;
  }
  
  // Si el token está expirado, cerrar sesión y redirigir con mensaje
  if (isTokenExpired()) {
    logout();
    alert('Tu sesión ha expirado. Por favor, inicia sesión nuevamente.');
    return <Navigate to="/login" replace />;
  }
  
  return <>{children}</>;
};

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Ruta para la página de Login */}
        <Route path="/login" element={<LoginPage />} />

        {/* Ruta para la página de Registro */}
        <Route path="/register" element={<RegisterPage />} />

        {/* Ruta protegida para la página principal */}
        <Route 
          path="/home" 
          element={
            <ProtectedRoute>
              <HomePage />
            </ProtectedRoute>
          } 
        />

        {/* Ruta para crear canción manualmente */}
        <Route 
          path="/create-song" 
          element={
            <ProtectedRoute>
              <CreateSongPage />
            </ProtectedRoute>
          } 
        />

        {/* Ruta para editar canción */}
        <Route 
          path="/songs/:id/edit" 
          element={
            <ProtectedRoute>
              <EditSongPage />
            </ProtectedRoute>
          } 
        />

        {/* Ruta para importar canción */}
        <Route 
          path="/import-song" 
          element={
            <ProtectedRoute>
              <ImportSongPage />
            </ProtectedRoute>
          } 
        />

        {/* Ruta para ver una canción específica */}
        <Route 
          path="/songs/:id" 
          element={
            <ProtectedRoute>
              <ViewSongPage />
            </ProtectedRoute>
          } 
        />

        {/* Ruta de administración */}
        <Route 
          path="/admin" 
          element={
            <ProtectedRoute>
              <AdminPage />
            </ProtectedRoute>
          } 
        />

        {/* Ruta para ver mis playlists */}
        <Route 
          path="/playlists" 
          element={
            <ProtectedRoute>
              <MyPlaylistsPage />
            </ProtectedRoute>
          } 
        />

        {/* Ruta para ver una playlist específica */}
        <Route 
          path="/playlists/:id" 
          element={
            <ProtectedRoute>
              <ViewPlaylistPage />
            </ProtectedRoute>
          } 
        />

        {/* Ruta para explorar canciones públicas */}
        <Route 
          path="/public-songs" 
          element={
            <ProtectedRoute>
              <PublicSongsPage />
            </ProtectedRoute>
          } 
        />

        {/* Ruta para el afinador vía backend WS */}
        <Route 
          path="/tuner" 
          element={
            <ProtectedRoute>
              <TunerPage />
            </ProtectedRoute>
          }
        />

        {/* Ruta por defecto: si alguien va a la raíz, lo redirigimos a /home */}
        <Route path="/" element={<Navigate to="/home" />} />
        <Route path="*" element={<Navigate to="/login" />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
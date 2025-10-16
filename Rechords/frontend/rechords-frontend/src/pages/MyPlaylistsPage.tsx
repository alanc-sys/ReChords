// src/pages/MyPlaylistsPage.tsx
import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  getMyPlaylists,
  createPlaylist,
  deletePlaylist,
  updatePlaylist,
  type PlaylistSummaryResponse,
  type CreatePlaylistRequest,
  type UpdatePlaylistRequest,
} from '../api/playlistApi';

const VintageStyles: React.FC = () => (
  <style>{`
    @import url('https://fonts.googleapis.com/css2?family=Playfair+Display:wght@700&family=Lato:wght@400;700&display=swap');
    @import url('https://fonts.googleapis.com/icon?family=Material+Icons');
    
    :root {
      --primary-color: #6B4F4F;
      --secondary-color: #A8875B;
      --accent-color: #4C573F;
      --bg-color: #F5EFE6;
      --dark-text: #3D3522;
    }
    body { font-family: 'Lato', sans-serif; background-color: var(--bg-color); }
    .playfair-font { font-family: 'Playfair Display', serif; }
    .grainy-texture { 
      position: fixed; top: 0; left: 0; width: 100%; height: 100%; 
      pointer-events: none;
      background-image: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 800"><filter id="noise"><feTurbulence type="fractalNoise" baseFrequency="0.65" numOctaves="3" stitchTiles="stitch"/></filter><rect width="100%" height="100%" filter="url(%23noise)"/></svg>');
      opacity: 0.1; z-index: -1;
    }
  `}</style>
);

export const MyPlaylistsPage: React.FC = () => {
  const navigate = useNavigate();
  const [playlists, setPlaylists] = useState<PlaylistSummaryResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [editingPlaylist, setEditingPlaylist] = useState<PlaylistSummaryResponse | null>(null);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    isPublic: false,
  });

  useEffect(() => {
    loadPlaylists();
  }, []);

  const loadPlaylists = async () => {
    setLoading(true);
    try {
      const data = await getMyPlaylists();
      setPlaylists(data);
      setError(null);
    } catch (err: any) {
      console.error('Error loading playlists:', err);
      setError('Error al cargar las playlists');
    } finally {
      setLoading(false);
    }
  };

  const handleCreatePlaylist = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const request: CreatePlaylistRequest = {
        name: formData.name,
        description: formData.description || undefined,
        isPublic: formData.isPublic,
      };
      await createPlaylist(request);
      setShowCreateModal(false);
      setFormData({ name: '', description: '', isPublic: false });
      loadPlaylists();
    } catch (err: any) {
      console.error('Error creating playlist:', err);
      alert('Error al crear la playlist');
    }
  };

  const handleUpdatePlaylist = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingPlaylist) return;
    
    try {
      const request: UpdatePlaylistRequest = {
        name: formData.name,
        description: formData.description || undefined,
        isPublic: formData.isPublic,
      };
      await updatePlaylist(editingPlaylist.id, request);
      setEditingPlaylist(null);
      setFormData({ name: '', description: '', isPublic: false });
      loadPlaylists();
    } catch (err: any) {
      console.error('Error updating playlist:', err);
      alert('Error al actualizar la playlist');
    }
  };

  const handleDeletePlaylist = async (id: number, name: string) => {
    if (window.confirm(`¿Estás seguro de eliminar la playlist "${name}"?`)) {
      try {
        await deletePlaylist(id);
        loadPlaylists();
      } catch (err: any) {
        console.error('Error deleting playlist:', err);
        alert('Error al eliminar la playlist');
      }
    }
  };

  const openEditModal = (playlist: PlaylistSummaryResponse) => {
    setEditingPlaylist(playlist);
    setFormData({
      name: playlist.name,
      description: playlist.description || '',
      isPublic: playlist.isPublic,
    });
  };

  const closeModals = () => {
    setShowCreateModal(false);
    setEditingPlaylist(null);
    setFormData({ name: '', description: '', isPublic: false });
  };

  return (
    <>
      <VintageStyles />
      <div className="grainy-texture"></div>

      <div className="min-h-screen" style={{ backgroundColor: 'var(--bg-color)', paddingTop: '2rem', paddingBottom: '4rem' }}>
        <div className="container mx-auto px-4 max-w-7xl">
          {/* Header */}
          <div className="flex justify-between items-center mb-12">
            <h1
              className="playfair-font text-6xl font-bold"
              style={{ color: 'var(--dark-text)' }}
            >
              MIS LISTAS
            </h1>
            <button
              onClick={() => setShowCreateModal(true)}
              className="px-8 py-3 rounded-lg font-bold text-white transition-all hover:opacity-90 flex items-center gap-2"
              style={{ backgroundColor: 'var(--primary-color)' }}
            >
              <i className="material-icons">add</i>
              CREAR LISTA
            </button>
          </div>

          {/* Error */}
          {error && (
            <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-6">
              {error}
            </div>
          )}

          {/* Loading */}
          {loading ? (
            <div className="text-center py-12">
              <i className="material-icons text-6xl animate-spin" style={{ color: 'var(--primary-color)' }}>
                album
              </i>
              <p className="mt-4 text-gray-600">Cargando playlists...</p>
            </div>
          ) : playlists.length === 0 ? (
            <div className="text-center py-12">
              <i className="material-icons text-6xl text-gray-400">library_music</i>
              <p className="mt-4 text-gray-600 text-xl">No tienes playlists todavía</p>
              <button
                onClick={() => setShowCreateModal(true)}
                className="mt-6 px-6 py-3 rounded-lg text-white"
                style={{ backgroundColor: 'var(--primary-color)' }}
              >
                Crear mi primera playlist
              </button>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
              {playlists.map((playlist) => (
                <div
                  key={playlist.id}
                  className="border-4 rounded-lg p-6 bg-white shadow-lg"
                  style={{ borderColor: 'var(--dark-text)' }}
                >
                  {/* Header de la tarjeta */}
                  <div className="flex justify-between items-start mb-4">
                    <h3 className="playfair-font text-2xl font-bold" style={{ color: 'var(--dark-text)' }}>
                      {playlist.name}
                    </h3>
                    <button
                      onClick={() => openEditModal(playlist)}
                      className="text-gray-600 hover:text-gray-800"
                    >
                      <i className="material-icons">edit</i>
                    </button>
                  </div>

                  {/* Contador de canciones */}
                  <p className="text-gray-600 mb-4">
                    {playlist.songCount} {playlist.songCount === 1 ? 'canción' : 'canciones'}
                  </p>

                  {/* Preview de canciones (primeras 3) */}
                  <div className="mb-4 space-y-2 min-h-[80px]">
                    {playlist.songCount > 0 ? (
                      <div className="text-sm text-gray-500 italic">
                        Haz clic para ver las canciones
                      </div>
                    ) : (
                      <div className="text-sm text-gray-400 italic">
                        Lista vacía
                      </div>
                    )}
                  </div>

                  {/* Botones */}
                  <div className="flex gap-2">
                    <button
                      onClick={() => navigate(`/playlists/${playlist.id}`)}
                      className="flex-1 py-3 rounded-lg text-white font-bold transition-all hover:opacity-90"
                      style={{ backgroundColor: 'var(--primary-color)' }}
                    >
                      VER LISTA
                    </button>
                    <button
                      onClick={() => handleDeletePlaylist(playlist.id, playlist.name)}
                      className="px-4 py-3 rounded-lg border-2 hover:bg-red-50 transition-all"
                      style={{ borderColor: 'var(--primary-color)' }}
                    >
                      <i className="material-icons text-red-600">delete</i>
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}

          {/* Botón volver */}
          <div className="mt-12 text-center">
            <Link
              to="/"
              className="inline-flex items-center gap-2 px-6 py-3 rounded-lg border-2 transition-all hover:bg-white"
              style={{ borderColor: 'var(--primary-color)', color: 'var(--primary-color)' }}
            >
              <i className="material-icons">arrow_back</i>
              Volver al inicio
            </Link>
          </div>
        </div>
      </div>

      {/* Modal Crear/Editar Playlist */}
      {(showCreateModal || editingPlaylist) && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg p-8 max-w-md w-full border-4" style={{ borderColor: 'var(--dark-text)' }}>
            <h2 className="playfair-font text-3xl font-bold mb-6" style={{ color: 'var(--dark-text)' }}>
              {editingPlaylist ? 'Editar Playlist' : 'Nueva Playlist'}
            </h2>

            <form onSubmit={editingPlaylist ? handleUpdatePlaylist : handleCreatePlaylist}>
              <div className="mb-4">
                <label className="block text-sm font-bold mb-2" style={{ color: 'var(--dark-text)' }}>
                  Nombre *
                </label>
                <input
                  type="text"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  className="w-full px-4 py-2 border-2 rounded-lg focus:outline-none focus:border-opacity-50"
                  style={{ borderColor: 'var(--primary-color)' }}
                  required
                  maxLength={100}
                />
              </div>

              <div className="mb-4">
                <label className="block text-sm font-bold mb-2" style={{ color: 'var(--dark-text)' }}>
                  Descripción (opcional)
                </label>
                <textarea
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  className="w-full px-4 py-2 border-2 rounded-lg focus:outline-none focus:border-opacity-50 resize-none"
                  style={{ borderColor: 'var(--primary-color)' }}
                  rows={3}
                  maxLength={500}
                />
              </div>

              <div className="mb-6">
                <label className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={formData.isPublic}
                    onChange={(e) => setFormData({ ...formData, isPublic: e.target.checked })}
                    className="w-5 h-5"
                  />
                  <span className="text-sm font-bold" style={{ color: 'var(--dark-text)' }}>
                    Hacer pública esta playlist
                  </span>
                </label>
              </div>

              <div className="flex gap-3">
                <button
                  type="button"
                  onClick={closeModals}
                  className="flex-1 py-3 rounded-lg border-2 font-bold transition-all hover:bg-gray-50"
                  style={{ borderColor: 'var(--primary-color)', color: 'var(--primary-color)' }}
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  className="flex-1 py-3 rounded-lg font-bold text-white transition-all hover:opacity-90"
                  style={{ backgroundColor: 'var(--primary-color)' }}
                >
                  {editingPlaylist ? 'Guardar' : 'Crear'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </>
  );
};


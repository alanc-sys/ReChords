// src/pages/ViewPlaylistPage.tsx
import React, { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import {
  getPlaylistById,
  removeSongFromPlaylist,
  addSongToPlaylist,
  type PlaylistResponse,
} from '../api/playlistApi';
import { getPublicSongs } from '../api/songApi';
import type { SongWithChordsResponse } from '../types/song';

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

export const ViewPlaylistPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [playlist, setPlaylist] = useState<PlaylistResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showAddSongModal, setShowAddSongModal] = useState(false);
  const [availableSongs, setAvailableSongs] = useState<SongWithChordsResponse[]>([]);
  const [searchQuery, setSearchQuery] = useState('');

  useEffect(() => {
    if (id) {
      loadPlaylist();
    }
  }, [id]);

  const loadPlaylist = async () => {
    if (!id) return;
    setLoading(true);
    try {
      const data = await getPlaylistById(Number(id));
      setPlaylist(data);
      setError(null);
    } catch (err: any) {
      console.error('Error loading playlist:', err);
      setError('Error al cargar la playlist');
    } finally {
      setLoading(false);
    }
  };

  const loadAvailableSongs = async () => {
    try {
      const response = await getPublicSongs(0, 100);
      setAvailableSongs(response.content);
    } catch (err: any) {
      console.error('Error loading songs:', err);
    }
  };

  const handleAddSong = async (songId: number) => {
    if (!id) return;
    try {
      await addSongToPlaylist(Number(id), { songId });
      loadPlaylist();
      setShowAddSongModal(false);
    } catch (err: any) {
      console.error('Error adding song:', err);
      alert('Error al añadir la canción');
    }
  };

  const handleRemoveSong = async (songId: number, title: string) => {
    if (!id) return;
    if (window.confirm(`¿Eliminar "${title}" de la playlist?`)) {
      try {
        await removeSongFromPlaylist(Number(id), songId);
        loadPlaylist();
      } catch (err: any) {
        console.error('Error removing song:', err);
        alert('Error al eliminar la canción');
      }
    }
  };

  const openAddSongModal = () => {
    loadAvailableSongs();
    setShowAddSongModal(true);
  };

  const filteredSongs = availableSongs.filter(
    (song) =>
      !playlist?.songs.some((ps) => ps.id === song.id) &&
      (song.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
        song.artist.toLowerCase().includes(searchQuery.toLowerCase()))
  );

  if (loading) {
    return (
      <>
        <VintageStyles />
        <div className="grainy-texture"></div>
        <div className="min-h-screen flex items-center justify-center" style={{ backgroundColor: 'var(--bg-color)' }}>
          <div className="text-center">
            <i className="material-icons text-6xl animate-spin" style={{ color: 'var(--primary-color)' }}>
              album
            </i>
            <p className="mt-4 text-gray-600">Cargando playlist...</p>
          </div>
        </div>
      </>
    );
  }

  if (error || !playlist) {
    return (
      <>
        <VintageStyles />
        <div className="grainy-texture"></div>
        <div className="min-h-screen flex items-center justify-center" style={{ backgroundColor: 'var(--bg-color)' }}>
          <div className="text-center">
            <i className="material-icons text-6xl text-red-500">error</i>
            <p className="mt-4 text-gray-600">{error || 'Playlist no encontrada'}</p>
            <Link
              to="/playlists"
              className="mt-6 inline-block px-6 py-3 rounded-lg text-white"
              style={{ backgroundColor: 'var(--primary-color)' }}
            >
              Volver a Mis Listas
            </Link>
          </div>
        </div>
      </>
    );
  }

  return (
    <>
      <VintageStyles />
      <div className="grainy-texture"></div>

      <div className="min-h-screen" style={{ backgroundColor: 'var(--bg-color)', paddingTop: '2rem', paddingBottom: '4rem' }}>
        <div className="container mx-auto px-4 max-w-5xl">
          {/* Header */}
          <div className="mb-8">
            <Link
              to="/playlists"
              className="inline-flex items-center gap-2 mb-4 text-gray-600 hover:text-gray-800"
            >
              <i className="material-icons">arrow_back</i>
              Volver a Mis Listas
            </Link>

            <div className="flex justify-between items-center">
              <div>
                <h1 className="playfair-font text-5xl font-bold mb-2" style={{ color: 'var(--dark-text)' }}>
                  {playlist.name}
                </h1>
                {playlist.description && (
                  <p className="text-gray-600 text-lg">{playlist.description}</p>
                )}
                <p className="text-gray-500 mt-2">
                  {playlist.songs.length} {playlist.songs.length === 1 ? 'canción' : 'canciones'}
                </p>
              </div>

              <button
                onClick={openAddSongModal}
                className="px-6 py-3 rounded-lg text-white font-bold transition-all hover:opacity-90 flex items-center gap-2"
                style={{ backgroundColor: 'var(--primary-color)' }}
              >
                <i className="material-icons">add</i>
                AÑADIR CANCIÓN
              </button>
            </div>
          </div>

          {/* Lista de canciones */}
          {playlist.songs.length === 0 ? (
            <div className="text-center py-12 bg-white rounded-lg border-4" style={{ borderColor: 'var(--dark-text)' }}>
              <i className="material-icons text-6xl text-gray-400">music_note</i>
              <p className="mt-4 text-gray-600 text-xl">Esta playlist está vacía</p>
              <button
                onClick={openAddSongModal}
                className="mt-6 px-6 py-3 rounded-lg text-white"
                style={{ backgroundColor: 'var(--primary-color)' }}
              >
                Añadir primera canción
              </button>
            </div>
          ) : (
            <div className="space-y-3">
              {playlist.songs.map((song, index) => (
                <div
                  key={song.id}
                  className="bg-white rounded-lg p-4 border-2 shadow-sm flex items-center justify-between hover:shadow-md transition-shadow"
                  style={{ borderColor: 'var(--dark-text)' }}
                >
                  <div className="flex items-center gap-4 flex-1">
                    <div
                      className="w-12 h-12 rounded-full flex items-center justify-center text-white font-bold text-xl"
                      style={{ backgroundColor: 'var(--primary-color)' }}
                    >
                      {index + 1}
                    </div>
                    <div className="flex-1">
                      <h3 className="font-bold text-lg" style={{ color: 'var(--dark-text)' }}>
                        {song.title}
                      </h3>
                      <p className="text-gray-600">{song.artist}</p>
                    </div>
                  </div>

                  <div className="flex gap-2">
                    <Link
                      to={`/songs/${song.id}`}
                      className="px-4 py-2 rounded-lg border-2 hover:bg-gray-50 transition-all"
                      style={{ borderColor: 'var(--primary-color)', color: 'var(--primary-color)' }}
                    >
                      <i className="material-icons">visibility</i>
                    </Link>
                    <button
                      onClick={() => handleRemoveSong(song.id, song.title)}
                      className="px-4 py-2 rounded-lg border-2 hover:bg-red-50 transition-all"
                      style={{ borderColor: 'var(--primary-color)' }}
                    >
                      <i className="material-icons text-red-600">delete</i>
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Modal Añadir Canción */}
      {showAddSongModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg p-8 max-w-2xl w-full max-h-[80vh] overflow-auto border-4" style={{ borderColor: 'var(--dark-text)' }}>
            <h2 className="playfair-font text-3xl font-bold mb-6" style={{ color: 'var(--dark-text)' }}>
              Añadir Canción
            </h2>

            {/* Buscador */}
            <div className="mb-6">
              <input
                type="text"
                placeholder="Buscar canciones..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full px-4 py-2 border-2 rounded-lg focus:outline-none"
                style={{ borderColor: 'var(--primary-color)' }}
              />
            </div>

            {/* Lista de canciones disponibles */}
            <div className="space-y-2 mb-6">
              {filteredSongs.length === 0 ? (
                <p className="text-center text-gray-500 py-8">No hay canciones disponibles</p>
              ) : (
                filteredSongs.map((song) => (
                  <div
                    key={song.id}
                    className="flex items-center justify-between p-3 border rounded-lg hover:bg-gray-50"
                  >
                    <div>
                      <p className="font-bold" style={{ color: 'var(--dark-text)' }}>
                        {song.title}
                      </p>
                      <p className="text-sm text-gray-600">{song.artist}</p>
                    </div>
                    <button
                      onClick={() => handleAddSong(song.id)}
                      className="px-4 py-2 rounded-lg text-white transition-all hover:opacity-90"
                      style={{ backgroundColor: 'var(--primary-color)' }}
                    >
                      Añadir
                    </button>
                  </div>
                ))
              )}
            </div>

            <button
              onClick={() => setShowAddSongModal(false)}
              className="w-full py-3 rounded-lg border-2 font-bold transition-all hover:bg-gray-50"
              style={{ borderColor: 'var(--primary-color)', color: 'var(--primary-color)' }}
            >
              Cerrar
            </button>
          </div>
        </div>
      )}
    </>
  );
};


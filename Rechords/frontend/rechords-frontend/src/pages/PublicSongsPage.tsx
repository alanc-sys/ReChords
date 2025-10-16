// src/pages/PublicSongsPage.tsx
import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { getPublicSongs, searchPublicSongs } from '../api/songApi';
import { getMyPlaylists, addSongToPlaylist } from '../api/playlistApi';
import type { SongWithChordsResponse } from '../types/song';
import type { PlaylistSummaryResponse } from '../api/playlistApi';

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

export const PublicSongsPage: React.FC = () => {
  const navigate = useNavigate();
  const [songs, setSongs] = useState<SongWithChordsResponse[]>([]);
  const [playlists, setPlaylists] = useState<PlaylistSummaryResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [isSearching, setIsSearching] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [showAddToPlaylistModal, setShowAddToPlaylistModal] = useState(false);
  const [selectedSong, setSelectedSong] = useState<SongWithChordsResponse | null>(null);

  useEffect(() => {
    loadSongs(currentPage);
  }, [currentPage]);

  // Debounce de búsqueda
  useEffect(() => {
    const handler = setTimeout(() => {
      if (searchQuery.trim().length > 0) {
        handleSearch(0);
      } else {
        loadSongs(0);
      }
    }, 400);
    return () => clearTimeout(handler);
  }, [searchQuery]);

  const loadSongs = async (page: number) => {
    setLoading(true);
    try {
      const response = await getPublicSongs(page, 12);
      setSongs(response.content);
      setTotalPages(response.totalPages);
      setError(null);
    } catch (err: any) {
      console.error('Error loading public songs:', err);
      setError('Error al cargar las canciones públicas');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async (page: number) => {
    if (!searchQuery.trim()) return;
    setIsSearching(true);
    setLoading(true);
    try {
      const response = await searchPublicSongs(searchQuery.trim(), page, 12);
      setSongs(response.content);
      setTotalPages(response.totalPages);
      setCurrentPage(page);
      setError(null);
    } catch (err: any) {
      console.error('Error searching songs:', err);
      setError('Error al buscar canciones');
    } finally {
      setLoading(false);
      setIsSearching(false);
    }
  };

  const loadPlaylists = async () => {
    try {
      const data = await getMyPlaylists();
      setPlaylists(data);
    } catch (err: any) {
      console.error('Error loading playlists:', err);
    }
  };

  const handleAddToPlaylist = (song: SongWithChordsResponse) => {
    setSelectedSong(song);
    loadPlaylists();
    setShowAddToPlaylistModal(true);
  };

  const handleConfirmAddToPlaylist = async (playlistId: number) => {
    if (!selectedSong) return;

    try {
      await addSongToPlaylist(playlistId, { songId: selectedSong.id });
      setShowAddToPlaylistModal(false);
      setSelectedSong(null);
      
      // Mostrar mensaje de éxito
      const playlistName = playlists.find(p => p.id === playlistId)?.name || 'la playlist';
      alert(`✅ "${selectedSong.title}" añadida a ${playlistName}`);
    } catch (err: any) {
      console.error('Error adding to playlist:', err);
      alert('❌ Error al añadir la canción. Puede que ya esté en la playlist.');
    }
  };

  const filteredSongs = songs; // servidor ya filtra cuando hay query

  return (
    <>
      <VintageStyles />
      <div className="grainy-texture"></div>

      <div className="min-h-screen" style={{ backgroundColor: 'var(--bg-color)', paddingTop: '2rem', paddingBottom: '4rem' }}>
        <div className="container mx-auto px-4 max-w-7xl">
          {/* Header */}
          <div className="mb-8">
            <Link
              to="/"
              className="inline-flex items-center gap-2 mb-4 text-gray-600 hover:text-gray-800"
            >
              <i className="material-icons">arrow_back</i>
              Volver al inicio
            </Link>

            <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
              <div>
                <h1 className="playfair-font text-6xl font-bold mb-2" style={{ color: 'var(--dark-text)' }}>
                  EXPLORAR
                </h1>
                <p className="text-gray-600 text-lg">Descubre canciones públicas aprobadas</p>
              </div>

              {/* Buscador */}
              <div className="relative w-full md:w-96">
                <input
                  type="text"
                  placeholder="Buscar por título o artista..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="w-full px-4 py-3 border-2 rounded-lg focus:outline-none focus:border-opacity-70"
                  style={{ borderColor: 'var(--primary-color)' }}
                />
                <i className="material-icons absolute right-3 top-3 text-gray-400">search</i>
              </div>
            </div>
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
              <p className="mt-4 text-gray-600">Cargando canciones...</p>
            </div>
          ) : filteredSongs.length === 0 ? (
            <div className="text-center py-12">
              <i className="material-icons text-6xl text-gray-400">music_off</i>
              <p className="mt-4 text-gray-600 text-xl">
                {searchQuery ? 'No se encontraron canciones' : 'No hay canciones públicas disponibles'}
              </p>
            </div>
          ) : (
            <>
              {/* Grid de canciones */}
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
                {filteredSongs.map((song) => (
                  <div
                    key={song.id}
                    className="bg-white rounded-lg p-6 border-4 shadow-lg hover:shadow-xl transition-shadow"
                    style={{ borderColor: 'var(--dark-text)' }}
                  >
                    {/* Header con ícono de vinilo */}
                    <div className="flex items-start gap-4 mb-4">
                      <div
                        className="w-16 h-16 rounded-full flex items-center justify-center flex-shrink-0"
                        style={{ backgroundColor: 'var(--primary-color)' }}
                      >
                        <i className="material-icons text-white text-3xl">album</i>
                      </div>
                      <div className="flex-1 min-w-0">
                        <h3
                          className="playfair-font text-xl font-bold mb-1 truncate"
                          style={{ color: 'var(--dark-text)' }}
                          title={song.title}
                        >
                          {song.title}
                        </h3>
                        <p className="text-gray-600 truncate" title={song.artist}>
                          {song.artist}
                        </p>
                      </div>
                    </div>

                    {/* Información adicional */}
                    <div className="space-y-2 mb-4 text-sm text-gray-600">
                      {song.album && (
                        <p className="flex items-center gap-2">
                          <i className="material-icons text-sm">library_music</i>
                          <span className="truncate">{song.album}</span>
                        </p>
                      )}
                      {song.year && (
                        <p className="flex items-center gap-2">
                          <i className="material-icons text-sm">calendar_today</i>
                          <span>{song.year}</span>
                        </p>
                      )}
                      {song.key && (
                        <p className="flex items-center gap-2">
                          <i className="material-icons text-sm">music_note</i>
                          <span>Tonalidad: {song.key}</span>
                        </p>
                      )}
                    </div>

                    {/* Autor */}
                    <p className="text-xs text-gray-500 mb-4">
                      Creada por: {song.createdBy?.username || 'Desconocido'}
                    </p>

                    {/* Botones */}
                    <div className="flex gap-2">
                      <button
                        onClick={() => navigate(`/songs/${song.id}`)}
                        className="flex-1 py-2 rounded-lg border-2 font-bold transition-all hover:bg-gray-50"
                        style={{ borderColor: 'var(--primary-color)', color: 'var(--primary-color)' }}
                      >
                        <i className="material-icons text-sm align-middle">visibility</i> Ver
                      </button>
                      <button
                        onClick={() => handleAddToPlaylist(song)}
                        className="flex-1 py-2 rounded-lg font-bold text-white transition-all hover:opacity-90"
                        style={{ backgroundColor: 'var(--accent-color)' }}
                      >
                        <i className="material-icons text-sm align-middle">add</i> Añadir
                      </button>
                    </div>
                  </div>
                ))}
              </div>

              {/* Paginación */}
              {totalPages > 1 && (
                <div className="flex justify-center items-center gap-4">
                  <button
                    onClick={() => (searchQuery.trim() ? handleSearch(Math.max(0, currentPage - 1)) : setCurrentPage(Math.max(0, currentPage - 1)))}
                    disabled={currentPage === 0}
                    className="px-6 py-3 rounded-lg border-2 font-bold transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                    style={{
                      borderColor: 'var(--primary-color)',
                      color: 'var(--primary-color)',
                      backgroundColor: currentPage === 0 ? 'transparent' : 'white'
                    }}
                  >
                    <i className="material-icons align-middle">chevron_left</i> Anterior
                  </button>
                  
                  <span className="text-lg font-bold" style={{ color: 'var(--dark-text)' }}>
                    Página {currentPage + 1} de {totalPages}
                  </span>
                  
                  <button
                    onClick={() => (searchQuery.trim() ? handleSearch(Math.min(totalPages - 1, currentPage + 1)) : setCurrentPage(Math.min(totalPages - 1, currentPage + 1)))}
                    disabled={currentPage >= totalPages - 1}
                    className="px-6 py-3 rounded-lg border-2 font-bold transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                    style={{
                      borderColor: 'var(--primary-color)',
                      color: 'var(--primary-color)',
                      backgroundColor: currentPage >= totalPages - 1 ? 'transparent' : 'white'
                    }}
                  >
                    Siguiente <i className="material-icons align-middle">chevron_right</i>
                  </button>
                </div>
              )}
            </>
          )}
        </div>
      </div>

      {/* Modal: Añadir a Playlist */}
      {showAddToPlaylistModal && selectedSong && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg p-8 max-w-md w-full border-4" style={{ borderColor: 'var(--dark-text)' }}>
            <h2 className="playfair-font text-3xl font-bold mb-4" style={{ color: 'var(--dark-text)' }}>
              Añadir a Playlist
            </h2>
            
            <p className="text-gray-600 mb-6">
              <strong>{selectedSong.title}</strong> - {selectedSong.artist}
            </p>

            {playlists.length === 0 ? (
              <div className="text-center py-8">
                <i className="material-icons text-6xl text-gray-400">playlist_add</i>
                <p className="mt-4 text-gray-600">No tienes playlists todavía</p>
                <button
                  onClick={() => {
                    setShowAddToPlaylistModal(false);
                    navigate('/playlists');
                  }}
                  className="mt-4 px-6 py-3 rounded-lg text-white font-bold"
                  style={{ backgroundColor: 'var(--primary-color)' }}
                >
                  Crear mi primera playlist
                </button>
              </div>
            ) : (
              <>
                <div className="space-y-2 mb-6 max-h-64 overflow-y-auto">
                  {playlists.map((playlist) => (
                    <button
                      key={playlist.id}
                      onClick={() => handleConfirmAddToPlaylist(playlist.id)}
                      className="w-full p-4 border-2 rounded-lg text-left hover:bg-gray-50 transition-all flex items-center justify-between"
                      style={{ borderColor: 'var(--primary-color)' }}
                    >
                      <div>
                        <p className="font-bold" style={{ color: 'var(--dark-text)' }}>
                          {playlist.name}
                        </p>
                        <p className="text-sm text-gray-600">
                          {playlist.songCount} {playlist.songCount === 1 ? 'canción' : 'canciones'}
                        </p>
                      </div>
                      <i className="material-icons" style={{ color: 'var(--accent-color)' }}>
                        add_circle
                      </i>
                    </button>
                  ))}
                </div>

                <button
                  onClick={() => setShowAddToPlaylistModal(false)}
                  className="w-full py-3 rounded-lg border-2 font-bold transition-all hover:bg-gray-50"
                  style={{ borderColor: 'var(--primary-color)', color: 'var(--primary-color)' }}
                >
                  Cancelar
                </button>
              </>
            )}
          </div>
        </div>
      )}
    </>
  );
};


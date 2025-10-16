// src/pages/AdminPage.tsx
import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  getAdminStats,
  getPendingSongs,
  getAllSongsAdmin,
  approveSong,
  rejectSong,
  unpublishSong,
  deleteSongAdmin,
  processAllAnalytics,
  type AdminStatsResponse,
  type PageResponse,
} from '../api/adminApi';
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

export const AdminPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'pending' | 'all' | 'stats'>('pending');
  const [stats, setStats] = useState<AdminStatsResponse | null>(null);
  const [pendingSongs, setPendingSongs] = useState<PageResponse<SongWithChordsResponse> | null>(null);
  const [allSongs, setAllSongs] = useState<PageResponse<SongWithChordsResponse> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [rejectReason, setRejectReason] = useState<{ [key: number]: string }>({});
  const [showRejectModal, setShowRejectModal] = useState<number | null>(null);

  // Cargar estadísticas
  useEffect(() => {
    loadStats();
  }, []);

  // Cargar canciones según tab activo
  useEffect(() => {
    if (activeTab === 'pending') {
      loadPendingSongs(currentPage);
    } else if (activeTab === 'all') {
      loadAllSongs(currentPage);
    }
  }, [activeTab, currentPage]);

  const loadStats = async () => {
    try {
      const data = await getAdminStats();
      setStats(data);
    } catch (err: any) {
      console.error('Error loading stats:', err);
      setError('Error al cargar estadísticas');
    }
  };

  const handleProcessAllAnalytics = async () => {
    if (!confirm('Esto recalculará la analítica de todas las canciones en segundo plano. ¿Continuar?')) return;
    try {
      const msg = await processAllAnalytics();
      alert(`✅ ${msg}`);
    } catch (err: any) {
      alert('❌ Error al iniciar procesamiento masivo');
    }
  };

  const loadPendingSongs = async (page: number) => {
    setLoading(true);
    try {
      const data = await getPendingSongs(page, 10);
      setPendingSongs(data);
      setError(null);
    } catch (err: any) {
      console.error('Error loading pending songs:', err);
      setError('Error al cargar canciones pendientes');
    } finally {
      setLoading(false);
    }
  };

  const loadAllSongs = async (page: number) => {
    setLoading(true);
    try {
      const data = await getAllSongsAdmin(page, 10);
      setAllSongs(data);
      setError(null);
    } catch (err: any) {
      console.error('Error loading all songs:', err);
      setError('Error al cargar todas las canciones');
    } finally {
      setLoading(false);
    }
  };

  const handleApprove = async (id: number) => {
    if (!confirm('¿Aprobar esta canción?')) return;
    
    try {
      await approveSong(id);
      alert('✅ Canción aprobada exitosamente');
      loadStats();
      if (activeTab === 'pending') loadPendingSongs(currentPage);
      else loadAllSongs(currentPage);
    } catch (err: any) {
      alert('❌ Error al aprobar: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleReject = async (id: number) => {
    const reason = rejectReason[id]?.trim();
    if (!reason) {
      alert('Por favor ingresa un motivo de rechazo');
      return;
    }

    try {
      await rejectSong(id, reason);
      alert('✅ Canción rechazada');
      setShowRejectModal(null);
      setRejectReason(prev => ({ ...prev, [id]: '' }));
      loadStats();
      if (activeTab === 'pending') loadPendingSongs(currentPage);
      else loadAllSongs(currentPage);
    } catch (err: any) {
      alert('❌ Error al rechazar: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleUnpublish = async (id: number) => {
    if (!confirm('¿Despublicar esta canción?')) return;
    
    try {
      await unpublishSong(id);
      alert('✅ Canción despublicada');
      loadStats();
      loadAllSongs(currentPage);
    } catch (err: any) {
      alert('❌ Error al despublicar: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleDelete = async (id: number, title: string) => {
    if (!confirm(`¿Eliminar permanentemente "${title}"? Esta acción no se puede deshacer.`)) return;
    
    try {
      await deleteSongAdmin(id);
      alert('✅ Canción eliminada');
      loadStats();
      if (activeTab === 'pending') loadPendingSongs(currentPage);
      else loadAllSongs(currentPage);
    } catch (err: any) {
      alert('❌ Error al eliminar: ' + (err.response?.data?.message || err.message));
    }
  };

  const renderSongCard = (song: SongWithChordsResponse) => (
    <div key={song.id} className="bg-white border-2 border-[var(--primary-color)] rounded-lg p-6 shadow-md">
      <div className="flex justify-between items-start mb-4">
        <div className="flex-1">
          <h3 className="text-xl font-bold text-[var(--dark-text)] playfair-font">{song.title}</h3>
          <p className="text-gray-600">{song.artist}</p>
          <p className="text-sm text-gray-500 mt-1">
            Por: {song.createdBy.firstname} (@{song.createdBy.username})
          </p>
          <div className="flex gap-2 mt-2">
            <span className={`px-2 py-1 text-xs rounded font-semibold ${
              song.status === 'APPROVED' ? 'bg-green-100 text-green-800' :
              song.status === 'PENDING' ? 'bg-yellow-100 text-yellow-800' :
              song.status === 'REJECTED' ? 'bg-red-100 text-red-800' :
              'bg-gray-100 text-gray-800'
            }`}>
              {song.status}
            </span>
            {song.isPublic && <span className="px-2 py-1 text-xs rounded font-semibold bg-blue-100 text-blue-800">PÚBLICA</span>}
          </div>
          {song.rejectionReason && (
            <p className="text-sm text-red-600 mt-2">
              <strong>Motivo rechazo:</strong> {song.rejectionReason}
            </p>
          )}
        </div>
        <Link 
          to={`/songs/${song.id}`}
          className="text-[var(--accent-color)] hover:text-[var(--primary-color)]"
          target="_blank"
        >
          <i className="material-icons">visibility</i>
        </Link>
      </div>

      <div className="flex gap-2 flex-wrap">
        {song.status === 'PENDING' && (
          <>
            <button
              onClick={() => handleApprove(song.id)}
              className="bg-green-500 text-white px-3 py-1 rounded text-sm hover:bg-green-600 transition flex items-center gap-1"
            >
              <i className="material-icons text-sm">check</i>
              Aprobar
            </button>
            <button
              onClick={() => setShowRejectModal(song.id)}
              className="bg-red-500 text-white px-3 py-1 rounded text-sm hover:bg-red-600 transition flex items-center gap-1"
            >
              <i className="material-icons text-sm">close</i>
              Rechazar
            </button>
          </>
        )}
        
        {song.status === 'APPROVED' && song.isPublic && (
          <button
            onClick={() => handleUnpublish(song.id)}
            className="bg-orange-500 text-white px-3 py-1 rounded text-sm hover:bg-orange-600 transition flex items-center gap-1"
          >
            <i className="material-icons text-sm">visibility_off</i>
            Despublicar
          </button>
        )}

        <button
          onClick={() => handleDelete(song.id, song.title)}
          className="bg-gray-700 text-white px-3 py-1 rounded text-sm hover:bg-gray-800 transition flex items-center gap-1"
        >
          <i className="material-icons text-sm">delete</i>
          Eliminar
        </button>
      </div>

      {/* Modal de rechazo */}
      {showRejectModal === song.id && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white p-6 rounded-lg max-w-md w-full mx-4">
            <h3 className="text-lg font-bold mb-4">Rechazar Canción</h3>
            <textarea
              className="w-full border-2 border-gray-300 rounded p-2 mb-4"
              rows={4}
              placeholder="Motivo del rechazo..."
              value={rejectReason[song.id] || ''}
              onChange={(e) => setRejectReason(prev => ({ ...prev, [song.id]: e.target.value }))}
            />
            <div className="flex gap-2 justify-end">
              <button
                onClick={() => setShowRejectModal(null)}
                className="px-4 py-2 bg-gray-300 rounded hover:bg-gray-400"
              >
                Cancelar
              </button>
              <button
                onClick={() => handleReject(song.id)}
                className="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600"
              >
                Rechazar
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );

  return (
    <>
      <VintageStyles />
      <div className="grainy-texture"></div>
      
      <div className="min-h-screen bg-[var(--bg-color)] p-6">
        <div className="max-w-7xl mx-auto">
          {/* Header */}
          <div className="flex items-center justify-between mb-8">
            <div>
              <h1 className="playfair-font text-4xl text-[var(--dark-text)] mb-2">
                👑 Panel de Administración
              </h1>
              <p className="text-gray-600">Gestión de canciones y moderación</p>
            </div>
            <div className="flex gap-2">
              <button
                onClick={handleProcessAllAnalytics}
                className="bg-[var(--accent-color)] text-white px-4 py-2 rounded hover:bg-[var(--primary-color)] transition flex items-center gap-2"
                title="Procesar analytics de todas las canciones"
              >
                <i className="material-icons">analytics</i>
                Recalcular Analytics
              </button>
              <Link 
                to="/home"
                className="bg-[var(--primary-color)] text-white px-4 py-2 rounded hover:bg-[var(--accent-color)] transition flex items-center gap-2"
              >
                <i className="material-icons">arrow_back</i>
                Volver
              </Link>
            </div>
          </div>

          {/* Estadísticas */}
          {stats && (
            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4 mb-8">
              <div className="bg-white p-4 rounded-lg border-2 border-gray-300 text-center">
                <div className="text-3xl font-bold text-[var(--primary-color)]">{stats.totalSongs}</div>
                <div className="text-sm text-gray-600">Total</div>
              </div>
              <div className="bg-white p-4 rounded-lg border-2 border-gray-300 text-center">
                <div className="text-3xl font-bold text-gray-500">{stats.draftSongs}</div>
                <div className="text-sm text-gray-600">Borradores</div>
              </div>
              <div className="bg-white p-4 rounded-lg border-2 border-yellow-400 text-center">
                <div className="text-3xl font-bold text-yellow-600">{stats.pendingSongs}</div>
                <div className="text-sm text-gray-600">Pendientes</div>
              </div>
              <div className="bg-white p-4 rounded-lg border-2 border-green-400 text-center">
                <div className="text-3xl font-bold text-green-600">{stats.approvedSongs}</div>
                <div className="text-sm text-gray-600">Aprobadas</div>
              </div>
              <div className="bg-white p-4 rounded-lg border-2 border-red-400 text-center">
                <div className="text-3xl font-bold text-red-600">{stats.rejectedSongs}</div>
                <div className="text-sm text-gray-600">Rechazadas</div>
              </div>
              <div className="bg-white p-4 rounded-lg border-2 border-blue-400 text-center">
                <div className="text-3xl font-bold text-blue-600">{stats.totalUsers}</div>
                <div className="text-sm text-gray-600">Usuarios</div>
              </div>
            </div>
          )}

          {/* Tabs */}
          <div className="flex gap-2 mb-6 border-b-2 border-gray-300">
            <button
              onClick={() => { setActiveTab('pending'); setCurrentPage(0); }}
              className={`px-6 py-3 font-bold transition-all ${
                activeTab === 'pending'
                  ? 'bg-[var(--accent-color)] text-white'
                  : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
              }`}
            >
              Pendientes {stats && `(${stats.pendingSongs})`}
            </button>
            <button
              onClick={() => { setActiveTab('all'); setCurrentPage(0); }}
              className={`px-6 py-3 font-bold transition-all ${
                activeTab === 'all'
                  ? 'bg-[var(--accent-color)] text-white'
                  : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
              }`}
            >
              Todas las Canciones
            </button>
          </div>

          {/* Error */}
          {error && (
            <div className="bg-red-50 border-l-4 border-red-500 p-4 mb-6">
              <p className="text-red-800">{error}</p>
            </div>
          )}

          {/* Contenido */}
          {loading ? (
            <div className="text-center py-12">
              <i className="material-icons text-6xl text-[var(--primary-color)] animate-spin">album</i>
              <p className="mt-4 text-gray-600">Cargando...</p>
            </div>
          ) : activeTab === 'pending' ? (
            <>
              {pendingSongs && pendingSongs.content.length > 0 ? (
                <div className="space-y-4">
                  {pendingSongs.content.map(renderSongCard)}
                  
                  {/* Paginación */}
                  {pendingSongs.totalPages > 1 && (
                    <div className="flex justify-center gap-2 mt-6">
                      <button
                        onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
                        disabled={currentPage === 0}
                        className="px-4 py-2 bg-gray-300 rounded disabled:opacity-50"
                      >
                        Anterior
                      </button>
                      <span className="px-4 py-2">
                        Página {currentPage + 1} de {pendingSongs.totalPages}
                      </span>
                      <button
                        onClick={() => setCurrentPage(Math.min(pendingSongs.totalPages - 1, currentPage + 1))}
                        disabled={currentPage >= pendingSongs.totalPages - 1}
                        className="px-4 py-2 bg-gray-300 rounded disabled:opacity-50"
                      >
                        Siguiente
                      </button>
                    </div>
                  )}
                </div>
              ) : (
                <div className="text-center py-12">
                  <i className="material-icons text-6xl text-gray-400">inbox</i>
                  <p className="mt-4 text-gray-600">No hay canciones pendientes</p>
                </div>
              )}
            </>
          ) : (
            <>
              {allSongs && allSongs.content.length > 0 ? (
                <div className="space-y-4">
                  {allSongs.content.map(renderSongCard)}
                  
                  {/* Paginación */}
                  {allSongs.totalPages > 1 && (
                    <div className="flex justify-center gap-2 mt-6">
                      <button
                        onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
                        disabled={currentPage === 0}
                        className="px-4 py-2 bg-gray-300 rounded disabled:opacity-50"
                      >
                        Anterior
                      </button>
                      <span className="px-4 py-2">
                        Página {currentPage + 1} de {allSongs.totalPages}
                      </span>
                      <button
                        onClick={() => setCurrentPage(Math.min(allSongs.totalPages - 1, currentPage + 1))}
                        disabled={currentPage >= allSongs.totalPages - 1}
                        className="px-4 py-2 bg-gray-300 rounded disabled:opacity-50"
                      >
                        Siguiente
                      </button>
                    </div>
                  )}
                </div>
              ) : (
                <div className="text-center py-12">
                  <i className="material-icons text-6xl text-gray-400">inbox</i>
                  <p className="mt-4 text-gray-600">No hay canciones</p>
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </>
  );
};


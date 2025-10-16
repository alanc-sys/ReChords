import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { getMySongs } from '../api/authApi';
import type { SongWithChordsResponse, LineWithChords } from '../types/song';
import { useAuthStore } from '../store/authStore';
import { 
    getMyPlaylists, 
    type PlaylistSummaryResponse,
    getPlaylistById,
    addSongToPlaylist,
    removeSongFromPlaylist,
    type PlaylistResponse
} from '../api/playlistApi';
import { deleteSong } from '../api/songApi';
import { API_CONFIG } from '../config/api';

// --- Estilos Completos (Copiados de tu HTML) ---
const VintageStyles: React.FC = () => (
    <style>{`
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
            color: var(--dark-text);
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
        .nav-bar {
            background: rgba(245, 239, 230, 0.95);
            backdrop-filter: blur(10px);
            border-bottom: 3px solid var(--dark-text);
        }
        
        /* Turntable */
        .turntable-base {
            width: 350px;
            height: 350px;
            background: linear-gradient(145deg, #4a4a4a 0%, #2a2a2a 100%);
            border-radius: 50%;
            border: 8px solid var(--dark-text);
            box-shadow: 0 10px 30px rgba(0,0,0,0.5), inset 0 -5px 20px rgba(0,0,0,0.3);
            position: relative;
        }
        .vinyl-record {
            width: 280px;
            height: 280px;
            border-radius: 50%;
            background-image: radial-gradient(circle at center, #3D3522 15%, var(--secondary-color) 15.2%, var(--secondary-color) 25%, #3D3522 25.2%), 
                            repeating-conic-gradient(from 0deg, #1a1a1a 0deg 1deg, #2a2a2a 1deg 2deg);
            box-shadow: 0 0 20px rgba(0, 0, 0, 0.5), inset 0 2px 10px rgba(0,0,0,0.3);
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            transition: transform 0.5s ease;
        }
        .vinyl-record.spinning {
            animation: spin-record 3s linear infinite;
        }
        @keyframes spin-record {
            from { transform: translate(-50%, -50%) rotate(0deg); }
            to { transform: translate(-50%, -50%) rotate(360deg); }
        }
        .vinyl-center {
            width: 80px;
            height: 80px;
            background: radial-gradient(circle, #1a1a1a 0%, #000 100%);
            border-radius: 50%;
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            border: 3px solid var(--secondary-color);
        }
        .tonearm {
            width: 150px;
            height: 8px;
            background: linear-gradient(90deg, #666 0%, #999 50%, #666 100%);
            position: absolute;
            top: 30%;
            right: -120px;
            transform-origin: right center;
            border-radius: 4px;
            transition: transform 0.8s ease;
            box-shadow: 0 2px 5px rgba(0,0,0,0.3);
        }
        .tonearm::after {
            content: '';
            width: 20px;
            height: 20px;
            background: var(--accent-color);
            border-radius: 50%;
            position: absolute;
            left: -10px;
            top: -6px;
        }
        .tonearm.playing {
            transform: rotate(-25deg);
        }
        
        /* Vinyl Collection */
        .vinyl-collection {
            display: flex;
            justify-content: center;
            align-items: flex-end;
            gap: 1rem;
            perspective: 1000px;
            flex-wrap: wrap; /* Added for responsiveness */
        }
        .vinyl-card {
            width: 120px;
            height: 140px;
            background: linear-gradient(145deg, #2a2a2a 0%, #1a1a1a 100%);
            border: 3px solid var(--dark-text);
            border-radius: 8px;
            box-shadow: 4px 4px 0px var(--primary-color);
            cursor: pointer;
            transition: all 0.3s ease;
            position: relative;
            overflow: hidden;
            flex-shrink: 0; /* Prevent cards from shrinking */
        }
        .vinyl-card:hover {
            transform: translateY(-10px) scale(1.05);
            box-shadow: 6px 6px 0px var(--primary-color);
        }
        .vinyl-card.selected {
            border-color: var(--accent-color);
            box-shadow: 6px 6px 0px var(--accent-color);
            transform: translateY(-15px) scale(1.1);
        }
        .vinyl-card-vinyl {
            width: 90px;
            height: 90px;
            border-radius: 50%;
            background-image: radial-gradient(circle at center, #3D3522 20%, var(--secondary-color) 20.2%, var(--secondary-color) 35%, #3D3522 35.2%), 
                            repeating-conic-gradient(from 0deg, #222 0deg 2deg, #333 2deg 4deg);
            margin: 10px auto;
        }
        .vinyl-card-title {
            text-align: center;
            font-size: 0.7rem;
            font-weight: bold;
            color: var(--bg-color);
            padding: 0 5px;
            word-break: break-word; /* Ensure long titles wrap */
        }
        
        /* Song Display Panel */
        .song-panel {
            background: var(--bg-color);
            border: 4px solid var(--dark-text);
            box-shadow: 8px 8px 0px var(--primary-color);
            border-radius: 4px;
            min-height: 400px;
        }
        .chord-line {
            color: var(--accent-color);
            font-weight: bold;
            font-family: 'Courier New', monospace;
            font-size: 0.9rem;
        }
        
        /* Playlist Section */
        .playlist-card {
            background: var(--bg-color);
            border: 3px solid var(--dark-text);
            box-shadow: 5px 5px 0px var(--accent-color);
            transition: all 0.3s ease;
            cursor: pointer;
        }
        .playlist-card:hover {
            transform: translate(-2px, -2px);
            box-shadow: 7px 7px 0px var(--accent-color);
        }
        .btn-vintage {
            border: 2px solid var(--dark-text);
            box-shadow: 3px 3px 0px var(--primary-color);
            transition: all 0.2s ease;
            background: var(--primary-color);
            color: var(--bg-color);
        }
        .btn-vintage:hover {
            transform: translate(-1px, -1px);
            box-shadow: 4px 4px 0px var(--primary-color);
            background: var(--accent-color);
        }
    `}</style>
);

// --- Componente de Navegación (Funcional) ---
const NavBar: React.FC = () => {
    const navigate = useNavigate();
    const { logout, isAdmin } = useAuthStore();
    const userIsAdmin = isAdmin();

    const handleLogout = () => {
        if (window.confirm('¿Estás seguro que deseas cerrar sesión?')) {
            logout();
            navigate('/login');
        }
    };

    return (
    <nav className="nav-bar sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 py-4">
            <div className="flex items-center justify-between">
                <Link to="/" className="flex items-center space-x-3">
                    <i className="material-icons text-4xl text-[var(--primary-color)]">album</i>
                    <h1 className="playfair-font text-3xl md:text-4xl vintage-title">RECHORDS</h1>
                </Link>
                <div className="hidden md:flex items-center space-x-6 text-sm font-bold tracking-wide">
                    <a href="#turntable" className="text-[var(--accent-color)] hover:text-[var(--primary-color)] transition">TOCADISCOS</a>
                        <a href="#playlists" className="hover:text-[var(--accent-color)] transition">MIS LISTAS</a>
                    <Link to="/public-songs" className="hover:text-[var(--accent-color)] transition">EXPLORAR</Link>
                    <Link to="/tuner" className="text-[var(--accent-color)] hover:text-[var(--primary-color)] transition flex items-center space-x-1">
                        <i className="material-icons text-sm">tune</i>
                        <span>AFINADOR</span>
                    </Link>
        </div>
                    <div className="flex items-center gap-2">
                        <Link to="/create-song" className="btn-vintage px-4 py-2 rounded-sm text-sm font-bold flex items-center space-x-2">
                            <i className="material-icons text-sm">add</i>
                            <span className="hidden md:inline">NUEVA CANCIÓN</span>
                        </Link>
                        {userIsAdmin && (
                    <Link to="/admin" className="btn-vintage px-4 py-2 rounded-sm text-sm font-bold flex items-center space-x-2 bg-[var(--dark-text)] hover:bg-black">
                        <i className="material-icons text-sm">admin_panel_settings</i>
                        <span className="hidden md:inline">ADMIN</span>
                    </Link>
                        )}
                        <button 
                            onClick={handleLogout}
                            className="btn-vintage px-3 py-2 rounded-sm text-sm font-bold flex items-center space-x-2 bg-red-600/80 hover:bg-red-700"
                        >
                            <i className="material-icons text-sm">logout</i>
                            <span className="hidden md:inline">SALIR</span>
                        </button>
                </div>
    </div>
  </div>
    </nav>
);
};

// --- Helper para renderizar Letra y Acordes ---
const renderLyricsWithChords = (lyrics: LineWithChords[] | null | undefined) => {
    if (!lyrics || lyrics.length === 0) {
        return <p className="text-gray-500 italic">No hay letra disponible</p>;
    }

    return lyrics.map((line, lineIndex) => {
        const text = line.text || '';
        const chords = line.chords || [];
        
        return (
            <div key={lineIndex} className="mb-4">
                {/* Línea de acordes */}
                {chords.length > 0 && (
                    <div className="chord-line mb-1 relative h-6">
                        {chords.map((chord, chordIndex) => (
                            <span
                                key={chordIndex}
                                className="absolute"
                                style={{ left: `${chord.start * 0.6}em` }}
                            >
                                {chord.name}
                            </span>
                        ))}
                    </div>
                )}
                {/* Línea de letra */}
                <div className="text-[var(--dark-text)]">{text || <br />}</div>
            </div>
        );
    });
};


// --- Página Principal (HomePage con JSX y Lógica Integrada) ---
// Función helper para ajustar brillo de color
const adjustColorBrightness = (color: string, percent: number): string => {
    const num = parseInt(color.replace('#', ''), 16);
    const amt = Math.round(2.55 * percent);
    const R = (num >> 16) + amt;
    const G = (num >> 8 & 0x00FF) + amt;
    const B = (num & 0x0000FF) + amt;
    return '#' + (
        0x1000000 +
        (R < 255 ? (R < 1 ? 0 : R) : 255) * 0x10000 +
        (G < 255 ? (G < 1 ? 0 : G) : 255) * 0x100 +
        (B < 255 ? (B < 1 ? 0 : B) : 255)
    ).toString(16).slice(1);
};

export const HomePage: React.FC = () => {
    const [songs, setSongs] = useState<SongWithChordsResponse[]>([]);
    const [playlists, setPlaylists] = useState<PlaylistSummaryResponse[]>([]);
    const [selectedSong, setSelectedSong] = useState<SongWithChordsResponse | null>(null);
    const [selectedIndex, setSelectedIndex] = useState<number | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [playlistsLoading, setPlaylistsLoading] = useState(true);
    const [isLyricsExpanded, setIsLyricsExpanded] = useState(false); // Estado para expandir/minimizar
    const [favoritesPlaylist, setFavoritesPlaylist] = useState<PlaylistResponse | null>(null);
    const [favoriteSongIds, setFavoriteSongIds] = useState<Set<number>>(new Set());
    const [isTogglingFavorite, setIsTogglingFavorite] = useState(false);

    // Función para obtener la playlist de Favoritas (creada por defecto en el backend)
    const getFavoritesPlaylist = async (): Promise<PlaylistResponse | null> => {
        try {
            // Buscar la playlist llamada "Favoritas" (creada por defecto)
            const allPlaylists = await getMyPlaylists();
            const existing = allPlaylists.find(p => p.name === "Favoritas");
            
            if (existing) {
                // Cargar los detalles completos de la playlist
                const fullPlaylist = await getPlaylistById(existing.id);
                setFavoritesPlaylist(fullPlaylist);
                // Actualizar el set de IDs de canciones favoritas
                const songIds = new Set(fullPlaylist.songs.map(s => s.id));
                setFavoriteSongIds(songIds);
                return fullPlaylist;
            }
            
            return null;
        } catch (error) {
            console.error("Error obteniendo playlist de Favoritas:", error);
            return null;
        }
    };

    // Función para agregar/quitar de favoritos
    const toggleFavorite = async (songId: number) => {
        if (isTogglingFavorite) return; // Prevenir clics múltiples
        
        setIsTogglingFavorite(true);
        try {
            // Obtener la playlist de Favoritas
            let playlist = favoritesPlaylist;
            if (!playlist) {
                playlist = await getFavoritesPlaylist();
                if (!playlist) {
                    alert("No se pudo encontrar la playlist de Favoritas");
                    return;
                }
            }

            const isFavorite = favoriteSongIds.has(songId);
            
            if (isFavorite) {
                // Quitar de favoritos
                await removeSongFromPlaylist(playlist.id, songId);
                const newIds = new Set(favoriteSongIds);
                newIds.delete(songId);
                setFavoriteSongIds(newIds);
            } else {
                // Agregar a favoritos
                await addSongToPlaylist(playlist.id, { songId });
                const newIds = new Set(favoriteSongIds);
                newIds.add(songId);
                setFavoriteSongIds(newIds);
            }
        } catch (error) {
            console.error("Error al toggle favorito:", error);
            alert("Error al actualizar favoritos");
        } finally {
            setIsTogglingFavorite(false);
        }
    };

    useEffect(() => {
        // Cargar canciones
        getMySongs()
            .then(data => {
                setSongs(data);
                // Seleccionar la primera canción por defecto si existe
                if (data.length > 0) {
                    setSelectedSong(data[0]);
                    setSelectedIndex(0);
                }
            })
            .catch(err => console.error("Error fetching songs:", err))
            .finally(() => setIsLoading(false));

        // Cargar playlists
        getMyPlaylists()
            .then(data => {
                setPlaylists(data);
            })
            .catch(err => console.error("Error fetching playlists:", err))
            .finally(() => setPlaylistsLoading(false));

        // Cargar playlist de Favoritas
        getFavoritesPlaylist().catch(err => 
            console.error("Error al cargar favoritos:", err)
        );
    }, []);

    const navigate = useNavigate();

    const handleSelectSong = (song: SongWithChordsResponse, index: number) => {
        setSelectedSong(song);
        setSelectedIndex(index);
        setIsLyricsExpanded(false); // Resetear a minimizado cuando se selecciona nueva canción

        // Animar el tocadiscos (recreando el efecto del JS original)
        const vinyl = document.getElementById('mainVinyl');
        const tonearm = document.getElementById('tonearm');
        if(vinyl && tonearm) {
            vinyl.classList.remove('spinning');
            tonearm.classList.remove('playing');
            
            setTimeout(() => {
                vinyl.classList.add('spinning');
                tonearm.classList.add('playing');
            }, 100);
        }
    };

    const handleDoubleClickSong = (songId: number) => {
        navigate(`/songs/${songId}`);
    };

    // Función para eliminar una canción
    const handleDeleteSong = async (song: SongWithChordsResponse, event: React.MouseEvent) => {
        event.stopPropagation(); // Prevenir que se seleccione la canción
        
        const confirmMessage = `¿Estás seguro de que quieres eliminar "${song.title}"? Esta acción no se puede deshacer.`;
        if (!confirm(confirmMessage)) return;
        
        try {
            await deleteSong(song.id);
            
            // Actualizar la lista de canciones
            const updatedSongs = songs.filter(s => s.id !== song.id);
            setSongs(updatedSongs);
            
            // Si la canción eliminada era la seleccionada, seleccionar otra
            if (selectedSong?.id === song.id) {
                if (updatedSongs.length > 0) {
                    setSelectedSong(updatedSongs[0]);
                    setSelectedIndex(0);
                } else {
                    setSelectedSong(null);
                    setSelectedIndex(null);
                }
            }
            
            alert('✅ Canción eliminada exitosamente');
        } catch (error: any) {
            console.error("Error al eliminar canción:", error);
            const errorMessage = error.response?.data?.message || error.message || 'Error desconocido';
            alert(`❌ Error al eliminar la canción: ${errorMessage}`);
        }
    };

    return (
        <>
        <VintageStyles />
        <div className="grainy-texture"></div>
            <NavBar />

            <main>
                {/* Main Turntable Section */}
                <section id="turntable" className="relative z-10 py-12">
                    <div className="max-w-7xl mx-auto px-4">
                        <div className="text-center mb-8">
                            <h2 className="playfair-font text-5xl font-bold vintage-title mb-2">TU TOCADISCOS</h2>
                            <p className="text-[var(--accent-color)] font-semibold tracking-widest uppercase">
                                {songs.length > 0 ? 'Selecciona un disco para reproducir' : 'Añade tu primera canción'}
                            </p>
                        </div>
                        
                        <div className="flex justify-center mb-12">
                            <div className="relative">
                                <div className="turntable-base">
                                    <div className="vinyl-record spinning" id="mainVinyl">
                                        <div className="vinyl-center"></div>
                                    </div>
                                    <div className="tonearm playing" id="tonearm"></div>
                                </div>
                            </div>
                        </div>
                        
                        <div className="vinyl-collection mb-12">
                            {isLoading ? (
                                <p>Cargando tus canciones...</p>
                            ) : songs.length > 0 ? (
                                songs.map((song, index) => (
                                    <div 
                                    key={song.id}
                                        className={`vinyl-card ${selectedIndex === index ? 'selected' : ''}`}
                                        onClick={() => handleSelectSong(song, index)}
                                        onDoubleClick={() => handleDoubleClickSong(song.id)}
                                        title="Doble clic para ver detalles"
                                        style={{
                                            background: song.coverColor 
                                                ? `linear-gradient(145deg, ${song.coverColor} 0%, ${adjustColorBrightness(song.coverColor, -20)} 100%)`
                                                : 'linear-gradient(145deg, #2a2a2a 0%, #1a1a1a 100%)'
                                        }}
                                    >
                                        {song.coverImageUrl ? (
                                            <img
                                                src={song.coverImageUrl.startsWith('http') ? song.coverImageUrl : `${API_CONFIG.BASE_URL}${song.coverImageUrl}`}
                                                alt="Portada"
                                                className="w-full h-24 object-cover border-b-2"
                                                style={{ borderColor: 'var(--dark-text)' }}
                                            />
                                        ) : (
                                            <div 
                                                className="vinyl-card-vinyl"
                                                style={{
                                                    backgroundImage: song.coverColor 
                                                        ? `radial-gradient(circle at center, #3D3522 20%, ${song.coverColor} 20.2%, ${song.coverColor} 35%, #3D3522 35.2%), repeating-conic-gradient(from 0deg, #222 0deg 2deg, #333 2deg 4deg)`
                                                        : undefined
                                                }}
                                            ></div>
                                        )}
                                    <div className="vinyl-card-title">{song.title}</div>
                                    </div>
                                ))
                            ) : (
                                <p className="text-center text-[var(--accent-color)] col-span-full">No tienes canciones todavía. ¡Crea una para empezar!</p>
                            )}
                        </div>
                        
                        {/* Song Display */}
                        <div className="max-w-4xl mx-auto">
                       {selectedSong ? (
                               <div className="song-panel p-6 md:p-8">
                                    <div className="flex flex-col md:flex-row items-start justify-between mb-6 gap-4">
                                        <div>
                                            <h3 className="playfair-font text-4xl font-bold text-[var(--dark-text)] mb-2">{selectedSong.title}</h3>
                                            <p className="text-xl text-[var(--primary-color)] font-semibold">{selectedSong.artist}</p>
                                            <div className="flex items-center space-x-3 mt-3">
                                                {selectedSong.key && (
                                                    <span className="inline-block px-3 py-1 bg-[var(--accent-color)] text-[var(--bg-color)] text-xs font-bold border-2 border-[var(--dark-text)] rounded-sm">
                                                        Tonalidad: {selectedSong.key}
                                                    </span>
                                                )}
                                                {selectedSong.tempo && (
                                                    <span className="text-sm text-[var(--accent-color)] font-bold">
                                                        Tempo: {selectedSong.tempo} BPM
                                                    </span>
                                                )}
                                            </div>
                                        </div>
                                        <div className="flex space-x-2">
                                            <Link to={`/songs/${selectedSong.id}`} className="btn-vintage px-4 py-2 rounded-sm text-sm font-bold">
                                                <i className="material-icons text-sm">visibility</i>
                                            </Link>
                                            {/* Botón de eliminar - solo si está en PENDING o DRAFT */}
                                            {(selectedSong.status === 'PENDING' || selectedSong.status === 'DRAFT') && (
                                                <button 
                                                    onClick={(e) => handleDeleteSong(selectedSong, e)}
                                                    className="px-4 py-2 rounded-sm text-sm font-bold border-2 border-[var(--dark-text)] transition-all bg-red-600 text-white hover:bg-red-700 hover:transform hover:translate-x-[-1px] hover:translate-y-[-1px]"
                                                    style={{ boxShadow: '3px 3px 0px #8B0000' }}
                                                    title="Eliminar canción"
                                                >
                                                    <i className="material-icons text-sm">delete</i>
                                                </button>
                                            )}
                                            <button 
                                                onClick={() => toggleFavorite(selectedSong.id)}
                                                disabled={isTogglingFavorite}
                                                className={`btn-vintage px-4 py-2 rounded-sm text-sm font-bold ${isTogglingFavorite ? 'opacity-50 cursor-not-allowed' : ''}`}
                                                title={favoriteSongIds.has(selectedSong.id) ? "Quitar de Favoritas" : "Agregar a Favoritas"}
                                            >
                                                <i className="material-icons text-sm">
                                                    {favoriteSongIds.has(selectedSong.id) ? 'star' : 'star_border'}
                                                </i>
                                            </button>
                                        </div>
                                    </div>
                                    
                                    <div className="border-t-2 border-[var(--dark-text)] pt-6">
                                        <div className="flex items-center justify-between mb-4">
                                            <h4 className="font-bold text-lg text-[var(--accent-color)]">ACORDES Y LETRA:</h4>
                                            
                                        </div>
                                        <div className="space-y-2 font-mono text-sm">
                                            {renderLyricsWithChords(
                                                isLyricsExpanded 
                                                    ? selectedSong.lyrics 
                                                    : selectedSong.lyrics?.slice(0, 8)
                                            )}
                                        </div>
                                        {!isLyricsExpanded && selectedSong.lyrics && selectedSong.lyrics.length > 8 && (
                                            <div className="mt-4 text-center">
                                                <p className="text-sm text-gray-500 italic mb-3">
                                                    ... {selectedSong.lyrics.length - 8} líneas más
                                                </p>
                                                <button
                                                    onClick={() => setIsLyricsExpanded(true)}
                                                    className="btn-vintage px-6 py-2 rounded-sm text-sm font-bold"
                                                >
                                                    VER LETRA COMPLETA
                                                </button>
                                            </div>
                                        )}
                                    </div>
                               </div>
                            ) : !isLoading && (
                                <div className="text-center py-10">
                                    <h3 className="playfair-font text-2xl text-[var(--dark-text)] mb-4">¡Comienza tu colección!</h3>
                                     <p className="text-gray-600 mb-6">Crea tu primera canción para verla aquí</p>
                                        <Link 
                                            to="/create-song" 
                                         className="btn-vintage px-6 py-3 rounded-sm font-bold flex items-center gap-2 mx-auto w-fit"
                                        >
                                            <i className="material-icons">add</i>
                                         Crear Nueva Canción
                                        </Link>
                                </div>
                            )}
                        </div>
                </div>
            </section>

                {/* Playlists Section */}
                <section id="playlists" className="relative z-10 py-12 border-t-4 border-[var(--dark-text)]">
                    <div className="max-w-7xl mx-auto px-4">
                        <div className="flex items-center justify-between mb-8">
                            <h2 className="playfair-font text-5xl font-bold vintage-title">MIS LISTAS</h2>
                            <Link to="/playlists" className="btn-vintage px-6 py-3 rounded-sm playfair-font text-lg font-bold flex items-center space-x-2">
                                <i className="material-icons">library_music</i>
                                <span>VER TODAS</span>
                            </Link>
                        </div>
                        
                        {playlistsLoading ? (
                            <div className="text-center py-8">
                                <p className="text-gray-600">Cargando playlists...</p>
                            </div>
                        ) : playlists.length > 0 ? (
                            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                                {playlists.slice(0, 6).map((playlist) => (
                                    <div key={playlist.id} className="playlist-card p-6 rounded-sm">
                                        <div className="flex items-start justify-between mb-4">
                                            <div>
                                                <h3 className="playfair-font text-2xl font-bold text-[var(--dark-text)] mb-2">
                                                    {playlist.name}
                                                </h3>
                                                <p className="text-sm text-[var(--primary-color)] font-semibold">
                                                    {playlist.songCount} {playlist.songCount === 1 ? 'canción' : 'canciones'}
                                                </p>
                                            </div>
                                            <i className="material-icons text-3xl text-[var(--accent-color)]">queue_music</i>
                                        </div>
                                        {playlist.description && (
                                            <p className="text-sm text-gray-600 mb-4 line-clamp-2">
                                                {playlist.description}
                                            </p>
                                        )}
                                        <div className="flex space-x-2 mt-4">
                                            <Link 
                                                to={`/playlists/${playlist.id}`} 
                                                className="flex-1 btn-vintage py-2 text-sm font-bold rounded-sm text-center"
                                            >
                                                VER LISTA
                                            </Link>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        ) : (
                            <div className="text-center py-12">
                                <i className="material-icons text-6xl text-gray-400 mb-4">library_music</i>
                                <p className="text-gray-600 mb-6">No tienes playlists todavía</p>
                                <Link 
                                    to="/playlists" 
                                    className="btn-vintage px-6 py-3 rounded-sm font-bold inline-flex items-center gap-2"
                                >
                                    <i className="material-icons">add</i>
                                    Crear Primera Playlist
                                </Link>
                            </div>
                        )}
                    </div>
                </section>
        </main>

            {/* Footer */}
            <footer className="relative z-10 mt-16 py-8 border-t-3 border-[var(--dark-text)]">
                <div className="text-center text-[var(--dark-text)]">
                    <p className="playfair-font text-3xl font-bold vintage-title mb-2">RECHORDS</p>
                    <p className="text-sm tracking-wider text-[var(--accent-color)] font-semibold">El Sonido del Ayer, Hoy © 2025</p>
                </div>
            </footer>
        </>
    );
};
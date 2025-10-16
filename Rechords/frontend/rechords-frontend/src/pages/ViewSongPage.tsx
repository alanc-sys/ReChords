// src/pages/ViewSongPage.tsx

import React, { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { getSongById, deleteSong, getSongAnalytics } from '../api/songApi';
import type { SongWithChordsResponse } from '../types/song';
import { useAuthStore } from '../store/authStore';
import { 
    getMyPlaylists, 
    getPlaylistById,
    addSongToPlaylist,
    removeSongFromPlaylist,
    type PlaylistResponse
} from '../api/playlistApi';
import { getAllChords, type ChordCatalog } from '../api/chordApi';
import ChordDiagram from '../components/ChordDiagram';
import { YouTubePlayer, SpotifyPlayer } from '../components/MediaPlayers';
import { API_CONFIG } from '../config/api';

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
      body {
          font-family: 'Lato', sans-serif;
          background-color: var(--bg-color);
          color: var(--dark-text);
      }
      .playfair-font {
          font-family: 'Playfair Display', serif;
      }
      .grainy-texture {
          position: fixed; top: 0; left: 0; width: 100%; height: 100%;
          pointer-events: none;
          background-image: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 800"><filter id="noise"><feTurbulence type="fractalNoise" baseFrequency="0.65" numOctaves="3" stitchTiles="stitch"/></filter><rect width="100%" height="100%" filter="url(%23noise)"/></svg>');
          opacity: 0.1; z-index: 100;
      }
      .vintage-title {
          color: var(--dark-text);
          text-shadow: 2px 2px 0px var(--secondary-color), 4px 4px 0px rgba(0, 0, 0, 0.1);
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
      .chord-line {
        position: relative;
        font-family: 'Courier New', monospace;
        line-height: 1.8;
      }
      .chord-marker {
        position: absolute;
        top: -1.2em;
        font-weight: bold;
        color: var(--accent-color);
        white-space: nowrap;
      }
  `}</style>
);

export const ViewSongPage: React.FC = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { getUsername } = useAuthStore();
    const currentUsername = getUsername();
    
    const [song, setSong] = useState<SongWithChordsResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [analytics, setAnalytics] = useState<{ totalChords: number; uniqueChords: number; difficulty: string; averageChordDensity: number; mostUsedChords: { chordName: string; count: number }[] } | null>(null);
    const [error, setError] = useState<string | null>(null);
    
    // Verificar si el usuario actual es el creador (guardas defensivas)
    const isCreator = !!(song?.createdBy && currentUsername && song.createdBy.username === currentUsername);
    
    // Control de funcionalidades
    const [fontSize, setFontSize] = useState(16); // Tamaño de texto base
    const [spacing, setSpacing] = useState(2); // Espaciado entre líneas
    const [showChords, setShowChords] = useState(true); // Mostrar/ocultar acordes
    const [transpose, setTranspose] = useState(0); // Transposición (-11 a +11)
    const [autoScroll, setAutoScroll] = useState(false); // Auto-scroll
    const [scrollSpeed, setScrollSpeed] = useState(1); // Velocidad de scroll
    
    // Estado de Favoritos
    const [favoritesPlaylist, setFavoritesPlaylist] = useState<PlaylistResponse | null>(null);
    const [isFavorite, setIsFavorite] = useState(false);
    const [isTogglingFavorite, setIsTogglingFavorite] = useState(false);

    // Estado para diagramas de acordes
    const [chordCatalog, setChordCatalog] = useState<Map<string, ChordCatalog>>(new Map());
    const [hoveredChord, setHoveredChord] = useState<{ name: string; x: number; y: number } | null>(null);
    const [isPopupHovered, setIsPopupHovered] = useState(false);
    const hoverTimeoutRef = React.useRef<number | null>(null);

    // Función para obtener la playlist de Favoritas
    const getFavoritesPlaylist = async (): Promise<PlaylistResponse | null> => {
        try {
            const allPlaylists = await getMyPlaylists();
            const existing = allPlaylists.find(p => p.name === "Favoritas");
            
            if (existing) {
                const fullPlaylist = await getPlaylistById(existing.id);
                setFavoritesPlaylist(fullPlaylist);
                return fullPlaylist;
            }
            
            return null;
        } catch (error) {
            console.error("Error obteniendo playlist de Favoritas:", error);
            return null;
        }
    };

    // Función para agregar/quitar de favoritos
    const toggleFavorite = async () => {
        if (isTogglingFavorite || !song) return;
        
        setIsTogglingFavorite(true);
        try {
            let playlist = favoritesPlaylist;
            if (!playlist) {
                playlist = await getFavoritesPlaylist();
                if (!playlist) {
                    alert("No se pudo encontrar la playlist de Favoritas");
                    return;
                }
            }

            if (isFavorite) {
                // Quitar de favoritos
                await removeSongFromPlaylist(playlist.id, song.id);
                setIsFavorite(false);
            } else {
                // Agregar a favoritos
                await addSongToPlaylist(playlist.id, { songId: song.id });
                setIsFavorite(true);
            }
        } catch (error) {
            console.error("Error al toggle favorito:", error);
            alert("Error al actualizar favoritos");
        } finally {
            setIsTogglingFavorite(false);
        }
    };

    // Función para eliminar la canción
    const handleDeleteSong = async () => {
        if (!song) return;
        
        const confirmMessage = `¿Estás seguro de que quieres eliminar "${song.title}"? Esta acción no se puede deshacer.`;
        if (!confirm(confirmMessage)) return;
        
        try {
            await deleteSong(song.id);
            alert('✅ Canción eliminada exitosamente');
            navigate('/home'); // Redirigir a la página de inicio después de eliminar
        } catch (error: any) {
            console.error("Error al eliminar canción:", error);
            const errorMessage = error.response?.data?.message || error.message || 'Error desconocido';
            alert(`❌ Error al eliminar la canción: ${errorMessage}`);
        }
    };

    useEffect(() => {
        if (!id) {
            setError('Song ID not provided');
            setLoading(false);
            return;
        }

        getSongById(Number(id))
            .then(data => {
                setSong(data);
                setError(null);
            })
            .catch(err => {
                console.error('Error fetching song:', err);
                setError('Could not load the song. Please make sure it exists and you have permission to view it.');
            })
            .finally(() => setLoading(false));

        // Cargar analytics
        getSongAnalytics(Number(id)).then(setAnalytics).catch(() => setAnalytics(null));
            
        // Cargar playlist de Favoritas y verificar si esta canción es favorita
        getFavoritesPlaylist().then(playlist => {
            if (playlist && id) {
                const songIsFavorite = playlist.songs.some(s => s.id === Number(id));
                setIsFavorite(songIsFavorite);
            }
        }).catch(err => console.error("Error al cargar favoritos:", err));

        // Cargar catálogo de acordes
        getAllChords().then(chords => {
            const catalogMap = new Map<string, ChordCatalog>();
            chords.forEach(chord => {
                catalogMap.set(chord.name, chord);
            });
            setChordCatalog(catalogMap);
        }).catch(err => console.error("Error al cargar catálogo de acordes:", err));
    }, [id]);

    // Auto-scroll effect
    useEffect(() => {
        if (!autoScroll) return;
        
        const interval = setInterval(() => {
            window.scrollBy({
                top: scrollSpeed,
                behavior: 'smooth'
            });
        }, 50);

        return () => clearInterval(interval);
    }, [autoScroll, scrollSpeed]);

    // Limpiar timeout al desmontar el componente
    useEffect(() => {
        return () => {
            if (hoverTimeoutRef.current) {
                clearTimeout(hoverTimeoutRef.current);
            }
        };
    }, []);

    // Función para transponer acordes
    const transposeChord = (chordName: string, semitones: number): string => {
        const notes = ['C', 'C#', 'D', 'D#', 'E', 'F', 'F#', 'G', 'G#', 'A', 'A#', 'B'];
        const aliases: { [key: string]: string } = {
            'Db': 'C#', 'Eb': 'D#', 'Gb': 'F#', 'Ab': 'G#', 'Bb': 'A#'
        };
        
        // Extraer la nota base del acorde
        let base = chordName.match(/^[A-G][#b]?/)?.[0] || chordName;
        const suffix = chordName.slice(base.length);
        
        // Normalizar alteraciones
        if (aliases[base]) base = aliases[base];
        
        const currentIndex = notes.indexOf(base);
        if (currentIndex === -1) return chordName;
        
        // Calcular nueva posición
        let newIndex = (currentIndex + semitones) % 12;
        if (newIndex < 0) newIndex += 12;
        
        return notes[newIndex] + suffix;
    };

    // Manejar hover sobre acordes
    const handleChordHover = (chordName: string, event: React.MouseEvent<HTMLSpanElement>) => {
        // Limpiar cualquier timeout pendiente
        if (hoverTimeoutRef.current) {
            clearTimeout(hoverTimeoutRef.current);
            hoverTimeoutRef.current = null;
        }
        
        const rect = event.currentTarget.getBoundingClientRect();
        setHoveredChord({
            name: chordName,
            x: rect.left + rect.width / 2,
            y: rect.bottom + 10
        });
    };

    const handleChordLeave = () => {
        // Solo cerrar si el popup no está siendo hovereado
        if (!isPopupHovered) {
            hoverTimeoutRef.current = setTimeout(() => {
                setHoveredChord(null);
            }, 100);
        }
    };

    const handlePopupEnter = () => {
        setIsPopupHovered(true);
        // Limpiar timeout si existe
        if (hoverTimeoutRef.current) {
            clearTimeout(hoverTimeoutRef.current);
            hoverTimeoutRef.current = null;
        }
    };

    const handlePopupLeave = () => {
        setIsPopupHovered(false);
        // Cerrar el popup después de un pequeño delay
        hoverTimeoutRef.current = setTimeout(() => {
            setHoveredChord(null);
        }, 100);
    };

    if (loading) return <div>Loading song...</div>;
    if (error) return <div>Error: {error}</div>;
    if (!song) return <div>Song not found.</div>;

    return (
        <>
            <VintageStyles />
            <div className="grainy-texture"></div>
            
            {/* Controles Flotantes */}
            <div className="fixed right-12 top-20 z-50 flex flex-col gap-2">
                {/* Desplazar (Auto-scroll) */}
                <div className="bg-white border-2 border-gray-300 rounded-lg px-4 py-3 shadow-lg">
                    <button
                        onClick={() => setAutoScroll(!autoScroll)}
                        className={`w-full flex flex-col items-center ${autoScroll ? 'text-[var(--accent-color)]' : ''}`}
                        title="Auto-desplazar"
                    >
                        <i className="material-icons">{autoScroll ? 'pause' : 'play_arrow'}</i>
                        <div className="text-xs mt-1">Desplazar</div>
                    </button>
                    {autoScroll && (
                        <div className="mt-2 pt-2 border-t border-gray-200">
                            <div className="text-xs text-center mb-1">Velocidad</div>
                            <div className="flex items-center gap-1">
                                <button 
                                    onClick={() => setScrollSpeed(Math.max(0.5, scrollSpeed - 0.5))}
                                    className="px-2 py-1 text-xs bg-gray-100 hover:bg-gray-200 rounded"
                                >
                                    −
                                </button>
                                <span className="text-xs flex-1 text-center">{scrollSpeed}x</span>
                                <button 
                                    onClick={() => setScrollSpeed(Math.min(5, scrollSpeed + 0.5))}
                                    className="px-2 py-1 text-xs bg-gray-100 hover:bg-gray-200 rounded"
                                >
                                    +
                                </button>
                            </div>
                        </div>
                    )}
                </div>

                {/* Texto (Tamaño de fuente) */}
                <div className="bg-white border-2 border-gray-300 rounded-lg px-4 py-3 shadow-lg">
                    <div className="flex items-center justify-between gap-2">
                        <button onClick={() => setFontSize(Math.max(12, fontSize - 2))} className="text-lg">A</button>
                        <span className="text-xs">Texto</span>
                        <button onClick={() => setFontSize(Math.min(24, fontSize + 2))} className="text-2xl">A</button>
                    </div>
                </div>

                {/* Espaciado */}
                <div className="bg-white border-2 border-gray-300 rounded-lg px-4 py-3 shadow-lg">
                    <div className="flex items-center justify-between gap-2">
                        <button onClick={() => setSpacing(Math.max(0.5, spacing - 0.25))}>−</button>
                        <span className="text-xs">½</span>
                        <button onClick={() => setSpacing(Math.min(2.5, spacing + 0.25))}>+</button>
                    </div>
                </div>

                {/* Acordes (Mostrar/Ocultar) */}
                <button
                    onClick={() => setShowChords(!showChords)}
                    className={`bg-white border-2 border-gray-300 rounded-lg px-4 py-3 shadow-lg hover:shadow-xl transition-all ${showChords ? 'bg-[var(--secondary-color)] text-white' : ''}`}
                    title="Mostrar/Ocultar acordes"
                >
                    <i className="material-icons">piano</i>
                    <div className="text-xs mt-1">Acordes</div>
                </button>

                {/* Afinación (Transposición) */}
                <div className="bg-white border-2 border-gray-300 rounded-lg px-4 py-3 shadow-lg">
                    <div className="text-xs text-center mb-2">Afinación</div>
                    <div className="flex flex-col gap-1">
                        <button 
                            onClick={() => setTranspose(Math.min(11, transpose + 1))}
                            className="bg-gray-100 hover:bg-gray-200 px-2 py-1 rounded text-xs"
                        >
                            +
                        </button>
                        <div className="text-center font-bold">{transpose > 0 ? '+' : ''}{transpose}</div>
                        <button 
                            onClick={() => setTranspose(Math.max(-11, transpose - 1))}
                            className="bg-gray-100 hover:bg-gray-200 px-2 py-1 rounded text-xs"
                        >
                            −
                        </button>
                    </div>
                </div>
            </div>

            <div className="max-w-4xl mx-auto p-8 pb-20">
                {/* Header */}
                <Link to="/home" className="inline-flex items-center gap-2 text-[var(--accent-color)] hover:text-[var(--primary-color)] mb-4 transition">
                    <i className="material-icons">arrow_back</i>
                    <span>Volver</span>
                </Link>

                <div className="bg-[var(--bg-color)] border-4 border-[var(--dark-text)] shadow-[8px_8px_0px_var(--primary-color)] rounded-lg p-8">
                    <div className="flex items-start justify-between mb-6">
                        <div>
                            <h3 className="playfair-font text-4xl font-bold text-[var(--dark-text)] mb-2">{song.title}</h3>
                            <p className="text-xl text-[var(--primary-color)] font-semibold">{song.artist}</p>
                            {song.key && (
                                <p className="text-sm text-gray-600 mt-2">
                                    Tonalidad: <strong className="text-[var(--accent-color)]">{transposeChord(song.key, transpose)}</strong>
                                    {transpose !== 0 && <span className="text-xs ml-1">(Original: {song.key})</span>}
                                </p>
                            )}
                        </div>
                        <div className="flex space-x-2">
                            {/* Botón de editar - solo visible para el creador */}
                            {isCreator && (
                                <button 
                                    onClick={() => navigate(`/songs/${id}/edit`)}
                                    className="btn-vintage px-4 py-2 rounded-sm text-sm font-bold"
                                    title="Editar canción"
                                >
                                    <i className="material-icons text-sm">edit</i>
                                </button>
                            )}
                            {/* Botón de eliminar - solo visible para el creador y si está en PENDING o DRAFT */}
                            {isCreator && (song.status === 'PENDING' || song.status === 'DRAFT') && (
                                <button 
                                    onClick={handleDeleteSong}
                                    className="px-4 py-2 rounded-sm text-sm font-bold border-2 border-[var(--dark-text)] box-shadow-[3px_3px_0px_#8B0000] transition-all bg-red-600 text-white hover:bg-red-700 hover:transform hover:translate-x-[-1px] hover:translate-y-[-1px] hover:shadow-[4px_4px_0px_#8B0000]"
                                    title="Eliminar canción"
                                >
                                    <i className="material-icons text-sm">delete</i>
                                </button>
                            )}
                            <button 
                                onClick={toggleFavorite}
                                disabled={isTogglingFavorite}
                                className={`btn-vintage px-4 py-2 rounded-sm text-sm font-bold ${isTogglingFavorite ? 'opacity-50 cursor-not-allowed' : ''}`}
                                title={isFavorite ? "Quitar de Favoritas" : "Agregar a Favoritas"}
                            >
                                <i className="material-icons text-sm">
                                    {isFavorite ? 'star' : 'star_border'}
                                </i>
                            </button>
                        </div>
                    </div>
                    
                    {/* Banner de aviso si la canción está PENDING por edición */}
                    {song.status === 'PENDING' && (
                        <div className="mb-6 p-4 bg-yellow-100 border-2 border-yellow-500 rounded-lg">
                            <div className="flex items-center gap-3">
                                <i className="material-icons text-yellow-700 text-3xl">pending</i>
                                <div>
                                    <h4 className="font-bold text-yellow-800 text-lg">
                                        ⏳ Esperando Aprobación
                                    </h4>
                                    <p className="text-sm text-yellow-700">
                                        Esta canción está siendo revisada por un administrador.
                                        {isCreator && ' Podrás ver los cambios una vez que sea aprobada.'}
                                        {!isCreator && ' Los cambios estarán disponibles cuando sea aprobada.'}
                                    </p>
                                </div>
                            </div>
                        </div>
                    )}
                    
                    {/* Players de YouTube y Spotify */}
                    {(song.youtubeVideoId || song.spotifyTrackId) && (
                        <div className="border-t-2 border-[var(--dark-text)] pt-6 pb-6">
                            <h4 className="font-bold text-lg mb-4 text-[var(--accent-color)]">
                                🎵 MULTIMEDIA:
                            </h4>
                            
                            {song.youtubeVideoId && (
                                <div className="mb-4">
                                    <h5 className="text-sm font-semibold text-gray-700 mb-2">
                                        🎬 Video Original
                                    </h5>
                                    <YouTubePlayer videoId={song.youtubeVideoId} />
                                </div>
                            )}
                            
                            {song.spotifyTrackId && (
                                <div>
                                    <h5 className="text-sm font-semibold text-gray-700 mb-2">
                                        🎧 Escuchar en Spotify
                                    </h5>
                                    <SpotifyPlayer trackId={song.spotifyTrackId} />
                                </div>
                            )}
                        </div>
                    )}
                    
                        <div className="border-t-2 border-[var(--dark-text)] pt-6">
                            {/* Portada si existe */}
                            {song.coverImageUrl && (
                                <div className="mb-6">
                                    <img src={song.coverImageUrl.startsWith('http') ? song.coverImageUrl : `${API_CONFIG.BASE_URL}${song.coverImageUrl}`} alt="Portada" className="w-full max-w-md border-2 border-[var(--dark-text)] rounded" />
                                </div>
                            )}
                        {/* Resumen de Analytics */}
                        {analytics && (
                            <div className="mb-6 p-4 bg-white border-2 border-gray-300 rounded-lg">
                                <h4 className="font-bold text-lg mb-3 text-[var(--accent-color)]">Análisis</h4>
                                <div className="grid grid-cols-2 md:grid-cols-4 gap-3 text-sm">
                                    <div><span className="font-semibold">Acordes:</span> {analytics.totalChords ?? 0}</div>
                                    <div><span className="font-semibold">Únicos:</span> {analytics.uniqueChords ?? 0}</div>
                                    <div><span className="font-semibold">Dificultad:</span> {analytics.difficulty ?? 'N/A'}</div>
                                    <div><span className="font-semibold">Densidad:</span> {Number(analytics.averageChordDensity ?? 0).toFixed(2)}</div>
                                </div>
                                {Array.isArray(analytics.mostUsedChords) && analytics.mostUsedChords.length > 0 && (
                                    <div className="mt-3 text-sm">
                                        <span className="font-semibold">Más usados:</span>{' '}
                                        {analytics.mostUsedChords.slice(0, 5).map((c, i) => (
                                            <span key={i} className="inline-block px-2 py-1 border rounded ml-1">{c.chordName} ({c.count})</span>
                                        ))}
                                    </div>
                                )}
                            </div>
                        )}
                        <h4 className="font-bold text-lg mb-4 text-[var(--accent-color)]">LETRA CON ACORDES:</h4>
                        <div 
                            className="space-y-0" 
                            style={{ 
                                fontSize: `${fontSize}px`,
                                fontFamily: 'Courier New, monospace'
                            }}
                        >
                            {song.lyrics && song.lyrics.map((line, index) => (
                                <div 
                                    key={index} 
                                    className="chord-line relative"
                                    style={{ 
                                        marginBottom: `${spacing * 0.5}rem`,
                                        lineHeight: spacing
                                    }}
                                >
                                    {/* Acordes */}
                                    {showChords && line.chords?.map((chord, chordIndex) => (
                                        <span 
                                            key={chordIndex}
                                            className="chord-marker cursor-pointer hover:text-blue-600 hover:underline transition-colors"
                                            style={{ 
                                                left: `${chord.start}ch`,
                                                fontSize: `${fontSize}px`,
                                                top: `-${fontSize * 1.2}px`
                                            }}
                                            onMouseEnter={(e) => handleChordHover(transposeChord(chord.name, transpose), e)}
                                            onMouseLeave={handleChordLeave}
                                        >
                                            {transposeChord(chord.name, transpose)}
                                        </span>
                                    ))}
                                    {/* Texto */}
                                    <div className="text-[var(--dark-text)]">{line.text || '\u00A0'}</div>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            </div>

            {/* Popup de diagrama de acorde */}
            {hoveredChord && (
                <div
                    style={{
                        position: 'fixed',
                        left: `${hoveredChord.x}px`,
                        top: `${hoveredChord.y}px`,
                        transform: 'translateX(-50%)',
                        zIndex: 9999,
                        pointerEvents: 'auto'
                    }}
                    onMouseEnter={handlePopupEnter}
                    onMouseLeave={handlePopupLeave}
                >
                    <ChordDiagram
                        chordName={hoveredChord.name}
                        fingerPositions={chordCatalog.get(hoveredChord.name)?.fingerPositions}
                        fullName={chordCatalog.get(hoveredChord.name)?.fullName}
                    />
                </div>
            )}
        </>
    );
};
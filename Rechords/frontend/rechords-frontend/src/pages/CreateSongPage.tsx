import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { createSong, submitSongForApproval, getCommonChords, getAvailableChords } from '../api/songApi';
import type { CreateSongRequest, ChordInfo, LineWithChords, ChordPositionInfo } from '../types/song';
import { getAllChords } from '../api/chordApi';
import ProposeChordModal, { type ProposedChordData } from '../components/ProposeChordModal';

// Estilos vintage reutilizados
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
    .grainy-texture { position: fixed; top: 0; left: 0; width: 100%; height: 100%; pointer-events: none; background-image: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 800"><filter id="noise"><feTurbulence type="fractalNoise" baseFrequency="0.65" numOctaves="3" stitchTiles="stitch"/></filter><rect width="100%" height="100%" filter="url(%23noise)"/></svg>'); opacity: 0.1; z-index: 100; }
    
    .chord-chip {
      display: inline-block;
      padding: 4px 10px;
      margin: 2px;
      background: var(--accent-color);
      color: var(--bg-color);
      border-radius: 4px;
      cursor: grab;
      font-weight: bold;
      user-select: none;
      transition: all 0.2s;
    }
    .chord-chip:hover {
      background: var(--primary-color);
      transform: scale(1.05);
    }
    .chord-chip:active {
      cursor: grabbing;
    }
    
    .lyrics-line {
      position: relative;
      padding: 8px 0;
      min-height: 40px;
      border-bottom: 1px dashed #ccc;
      font-family: monospace;
      font-size: 16px;
      line-height: 1.8;
    }
    
    .chord-marker {
      position: absolute;
      top: -18px;
      padding: 2px 6px;
      background: var(--secondary-color);
      color: var(--dark-text);
      border-radius: 3px;
      font-size: 12px;
      font-weight: bold;
      cursor: pointer;
      z-index: 10;
    }
    .chord-marker:hover {
      background: var(--primary-color);
      color: var(--bg-color);
    }
  `}</style>
);

// Función helper para generar color aleatorio
const generateRandomColor = (): string => {
  const colors = [
    '#FF6B6B', '#4ECDC4', '#45B7D1', '#FFA07A', '#98D8C8',
    '#F7DC6F', '#BB8FCE', '#85C1E2', '#F8B88B', '#A3E4D7',
    '#6B4F4F', '#A8875B', '#4C573F', '#8B7355', '#D4A574'
  ];
  return colors[Math.floor(Math.random() * colors.length)];
};

export const CreateSongPage: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  
  // Control de pasos
  const [step, setStep] = useState(1); // 1: Info, 2: Agregar acordes
  
  // Datos de la canción
  const [title, setTitle] = useState('');
  const [artist, setArtist] = useState('');
  const [album, setAlbum] = useState('');
  const [year, setYear] = useState<number | undefined>();
  const [key, setKey] = useState('');
  const [tempo, setTempo] = useState<number | undefined>();
  const [lyricsText, setLyricsText] = useState('');
  
  // Enlaces multimedia
  const [youtubeUrl, setYoutubeUrl] = useState('');
  const [spotifyUrl, setSpotifyUrl] = useState('');
  
  // Personalización de portada  
  const [coverColor] = useState(''); // Se genera automáticamente al guardar
  
  // Acordes disponibles
  const [availableChords, setAvailableChords] = useState<ChordInfo[]>([]);
  const [loadingChords, setLoadingChords] = useState(true);
  const [chordsError, setChordsError] = useState<string | null>(null);
  const [showAllChords, setShowAllChords] = useState(false); // Toggle entre comunes y todos
  
  // Letra parseada con acordes
  const [lyricsWithChords, setLyricsWithChords] = useState<LineWithChords[]>([]);
  const [currentLineIndex, setCurrentLineIndex] = useState(0); // Línea actual para agregar acordes
  
  // Acorde siendo arrastrado
  const [draggedChord, setDraggedChord] = useState<ChordInfo | null>(null);
  
  // Acordes propuestos (nuevos)
  const [proposedChords, setProposedChords] = useState<ProposedChordData[]>([]);
  const [unknownChords, setUnknownChords] = useState<string[]>([]);
  const [proposeModalOpen, setProposeModalOpen] = useState(false);
  const [currentUnknownChord, setCurrentUnknownChord] = useState<string | null>(null);
  const [catalogChords, setCatalogChords] = useState<Set<string>>(new Set());

  const loadChords = (loadAll: boolean) => {
    setLoadingChords(true);
    setChordsError(null);
    
    const loadFunction = loadAll ? getAvailableChords : getCommonChords;
    
    loadFunction()
      .then(chords => {
        setAvailableChords(chords);
      })
      .catch(err => {
        console.error('Error cargando acordes:', err);
        setChordsError('No se pudieron cargar los acordes. Verifica que el backend esté corriendo.');
      })
      .finally(() => setLoadingChords(false));
  };

  useEffect(() => {
    // Cargar acordes comunes al iniciar
    loadChords(false);
    
    // Cargar catálogo completo de acordes para detectar desconocidos
    getAllChords()
      .then(chords => {
        const chordNames = new Set(chords.map(c => c.name));
        setCatalogChords(chordNames);
      })
      .catch(err => console.error("Error cargando catálogo de acordes:", err));
  }, []);
  
  // Detectar acordes desconocidos cuando cambian las letras con acordes
  useEffect(() => {
    detectUnknownChords();
  }, [lyricsWithChords, catalogChords]);
  
  /**
   * Detectar acordes que no están en el catálogo
   */
  const detectUnknownChords = () => {
    if (catalogChords.size === 0) return;
    
    const usedChords = new Set<string>();
    lyricsWithChords.forEach(line => {
      line.chords?.forEach(chord => {
        usedChords.add(chord.name);
      });
    });
    
    const unknown = Array.from(usedChords).filter(chordName => {
      // Verificar si ya fue propuesto
      const alreadyProposed = proposedChords.some(pc => pc.name === chordName);
      // Verificar si está en el catálogo
      return !catalogChords.has(chordName) && !alreadyProposed;
    });
    
    setUnknownChords(unknown);
  };
  
  /**
   * Abrir modal para proponer un acorde
   */
  const handleProposeChord = (chordName: string) => {
    setCurrentUnknownChord(chordName);
    setProposeModalOpen(true);
  };
  
  /**
   * Guardar acorde propuesto
   */
  const handleSubmitProposedChord = (chordData: ProposedChordData) => {
    setProposedChords(prev => [...prev, chordData]);
    // Remover de acordes desconocidos
    setUnknownChords(prev => prev.filter(c => c !== chordData.name));
    setProposeModalOpen(false);
    setCurrentUnknownChord(null);
  };

  const toggleChords = () => {
    const newShowAll = !showAllChords;
    setShowAllChords(newShowAll);
    loadChords(newShowAll);
  };

  const handleContinueToChords = () => {
    if (!title.trim() || !artist.trim() || !lyricsText.trim()) {
      setError('❌ Título, artista y letra son obligatorios');
      return;
    }

    // Parsear letra en líneas
    const lines = lyricsText.split('\n');
    const parsed: LineWithChords[] = lines.map((text, index) => ({
      lineNumber: index,
      text: text,
      chords: []
    }));
    setLyricsWithChords(parsed);
    setCurrentLineIndex(0);
    setStep(2);
    setError(null);
  };

  const handleBackToInfo = () => {
    setStep(1);
  };

  const goToNextLine = () => {
    if (currentLineIndex < lyricsWithChords.length - 1) {
      setCurrentLineIndex(currentLineIndex + 1);
    }
  };

  const goToPreviousLine = () => {
    if (currentLineIndex > 0) {
      setCurrentLineIndex(currentLineIndex - 1);
    }
  };

  const skipLine = () => {
    goToNextLine();
  };

  const handleChordDragStart = (chord: ChordInfo) => {
    setDraggedChord(chord);
  };

  const handleChordDragEnd = () => {
    setDraggedChord(null);
  };

  const handleCurrentLineClick = (event: React.MouseEvent<HTMLDivElement>) => {
    if (!draggedChord) return;
    
    const lineElement = event.currentTarget;
    const rect = lineElement.getBoundingClientRect();
    const clickX = event.clientX - rect.left;
    
    // Calcular posición aproximada del carácter
    const textContent = lyricsWithChords[currentLineIndex].text;
    const charWidth = rect.width / (textContent.length || 1);
    const charPosition = Math.floor(clickX / charWidth);
    
    // Agregar el acorde a la línea actual
    const newChord: ChordPositionInfo = {
      start: Math.max(0, Math.min(charPosition, textContent.length)),
      name: draggedChord.name,
      chordId: draggedChord.id
    };
    
    const updatedLines = [...lyricsWithChords];
    updatedLines[currentLineIndex].chords.push(newChord);
    // Ordenar acordes por posición
    updatedLines[currentLineIndex].chords.sort((a, b) => a.start - b.start);
    setLyricsWithChords(updatedLines);
    setDraggedChord(null);
  };

  const removeChordFromCurrentLine = (chordIndex: number) => {
    const updatedLines = [...lyricsWithChords];
    updatedLines[currentLineIndex].chords.splice(chordIndex, 1);
    setLyricsWithChords(updatedLines);
  };

  const handleSaveDraft = async () => {
    if (!title.trim() || !artist.trim()) {
      setError('❌ El título y artista son obligatorios');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      // Filtrar líneas vacías para validación del backend
      const validLyrics = lyricsWithChords.filter(line => line.text && line.text.trim() !== '');
      
      if (validLyrics.length === 0) {
        setError('❌ La letra no puede estar vacía');
        setLoading(false);
        return;
      }

      const songData: CreateSongRequest = {
        title,
        artist,
        album: album || undefined,
        year: year || undefined,
        key: key || undefined,
        tempo: tempo || undefined,
        youtubeUrl: youtubeUrl || undefined,
        spotifyUrl: spotifyUrl || undefined,
        coverColor: coverColor || generateRandomColor(),
        lyrics: validLyrics,
        proposedChords: proposedChords.length > 0 ? proposedChords : undefined
      };

      const response = await createSong(songData);
      alert(`✅ Borrador guardado exitosamente: "${response.title}"`);
      navigate('/home');
    } catch (err: any) {
      console.error('Error al guardar:', err);
      const errorMsg = err.response?.data?.message || err.response?.data || err.message || 'Error desconocido';
      setError('❌ Error al guardar: ' + errorMsg);
    } finally {
      setLoading(false);
    }
  };

  const handleSaveAndSubmit = async () => {
    if (!title.trim() || !artist.trim()) {
      setError('❌ El título y artista son obligatorios');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      // Filtrar líneas vacías para validación del backend
      const validLyrics = lyricsWithChords.filter(line => line.text && line.text.trim() !== '');
      
      if (validLyrics.length === 0) {
        setError('❌ La letra no puede estar vacía');
        setLoading(false);
        return;
      }

      const songData: CreateSongRequest = {
        title,
        artist,
        album: album || undefined,
        year: year || undefined,
        key: key || undefined,
        tempo: tempo || undefined,
        youtubeUrl: youtubeUrl || undefined,
        spotifyUrl: spotifyUrl || undefined,
        coverColor: coverColor || generateRandomColor(),
        lyrics: validLyrics,
        proposedChords: proposedChords.length > 0 ? proposedChords : undefined
      };

      // Primero crear como borrador
      const createdSong = await createSong(songData);
      
      // Luego enviar para aprobación
      await submitSongForApproval(createdSong.id);
      
      alert(`✅ Canción "${createdSong.title}" enviada para revisión`);
      navigate('/home');
    } catch (err: any) {
      console.error('Error al enviar para revisión:', err);
      const errorMsg = err.response?.data?.message || err.response?.data || err.message || 'Error desconocido';
      setError('❌ Error al enviar: ' + errorMsg);
    } finally {
      setLoading(false);
    }
  };

  const currentLine = lyricsWithChords[currentLineIndex];

  return (
    <>
      <VintageStyles />
      <div className="min-h-screen p-6 bg-[var(--bg-color)]">
        <div className="grainy-texture"></div>
        
        <div className="max-w-7xl mx-auto relative z-10">
          {/* Header */}
          <div className="flex items-center justify-between mb-8">
            <h1 className="playfair-font text-4xl md:text-5xl text-[var(--dark-text)] vintage-title">
              {step === 1 ? 'CREAR CANCIÓN - INFORMACIÓN' : 'CREAR CANCIÓN - AGREGAR ACORDES'}
            </h1>
            <div className="flex gap-3">
              <button
                onClick={() => navigate('/import-song')}
                className="bg-[var(--secondary-color)] text-white py-2 px-4 rounded hover:bg-[var(--primary-color)] transition flex items-center gap-2"
              >
                <i className="material-icons">upload_file</i>
                Importar Canción
              </button>
              <button
                onClick={() => navigate('/home')}
                className="bg-gray-400 text-white py-2 px-4 rounded hover:bg-gray-500 transition flex items-center gap-2"
              >
                <i className="material-icons">arrow_back</i>
                Volver
              </button>
            </div>
          </div>

          {/* Indicador de paso */}
          <div className="flex justify-center mb-8">
            <div className="flex items-center gap-4">
              <div className={`flex items-center gap-2 ${step === 1 ? 'text-[var(--accent-color)]' : 'text-gray-400'}`}>
                <div className={`w-8 h-8 rounded-full flex items-center justify-center ${step === 1 ? 'bg-[var(--accent-color)] text-white' : 'bg-gray-300'}`}>1</div>
                <span className="font-bold">Información</span>
              </div>
              <div className="w-12 h-1 bg-gray-300"></div>
              <div className={`flex items-center gap-2 ${step === 2 ? 'text-[var(--accent-color)]' : 'text-gray-400'}`}>
                <div className={`w-8 h-8 rounded-full flex items-center justify-center ${step === 2 ? 'bg-[var(--accent-color)] text-white' : 'bg-gray-300'}`}>2</div>
                <span className="font-bold">Acordes</span>
              </div>
            </div>
          </div>

          {step === 1 ? (
            /* PASO 1: Información de la canción */
            <>
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                {/* Columna Izquierda: Formulario */}
                <div className="lg:col-span-1 bg-white p-6 rounded shadow-lg border-2 border-[var(--primary-color)]">
                  <h2 className="text-xl font-bold text-[var(--primary-color)] mb-4 playfair-font">Información de la Canción</h2>
                  
                  <div className="space-y-4">
                    <InputField label="Título *" value={title} onChange={(e) => setTitle(e.target.value)} placeholder="Nombre de la canción" />
                    <InputField label="Artista *" value={artist} onChange={(e) => setArtist(e.target.value)} placeholder="Nombre del artista" />
                    <InputField label="Álbum" value={album} onChange={(e) => setAlbum(e.target.value)} placeholder="Nombre del álbum (opcional)" />
                    <InputField label="Año" type="number" value={year?.toString() || ''} onChange={(e) => setYear(e.target.value ? parseInt(e.target.value) : undefined)} placeholder="Año de lanzamiento" />
                    <InputField label="Tonalidad" value={key} onChange={(e) => setKey(e.target.value)} placeholder="Ej: C, Am, G" />
                    <InputField label="Tempo (BPM)" type="number" value={tempo?.toString() || ''} onChange={(e) => setTempo(e.target.value ? parseInt(e.target.value) : undefined)} placeholder="Ej: 120" />
                    
                    {/* Enlaces multimedia */}
                    <div className="pt-4 border-t-2 border-dashed border-gray-300">
                      <h3 className="text-sm font-bold text-[var(--dark-text)] mb-3 flex items-center gap-2">
                        <span>🎬</span> Enlaces Multimedia (Opcional)
                      </h3>
                      <div className="space-y-3">
                        <InputField 
                          label="YouTube URL" 
                          value={youtubeUrl} 
                          onChange={(e) => setYoutubeUrl(e.target.value)} 
                          placeholder="https://www.youtube.com/watch?v=..." 
                        />
                        <InputField 
                          label="Spotify URL" 
                          value={spotifyUrl} 
                          onChange={(e) => setSpotifyUrl(e.target.value)} 
                          placeholder="https://open.spotify.com/track/..." 
                        />
                        <p className="text-xs text-gray-600">
                          💡 Agrega enlaces para practicar junto a la canción original
                        </p>
                      </div>
                    </div>
                  </div>

                  <h2 className="text-xl font-bold text-[var(--primary-color)] mt-6 mb-4 playfair-font">Letra</h2>
                  <textarea
                    className="w-full h-64 p-3 border-2 border-[var(--primary-color)] rounded focus:outline-none focus:border-[var(--accent-color)] font-mono"
                    value={lyricsText}
                    onChange={(e) => setLyricsText(e.target.value)}
                    placeholder="Escribe o pega la letra aquí..."
                  />
                </div>

                {/* Columna Derecha: Vista Previa de la Letra */}
                <div className="lg:col-span-1 bg-white p-6 rounded shadow-lg border-2 border-[var(--accent-color)]">
                  <h2 className="text-xl font-bold text-[var(--accent-color)] mb-4 playfair-font">Vista Previa de la Letra</h2>
                  <div className="bg-gray-50 p-4 rounded border border-gray-300 max-h-[500px] overflow-y-auto">
                    {lyricsText.trim() ? (
                      <div className="whitespace-pre-wrap font-mono text-sm">{lyricsText}</div>
                    ) : (
                      <p className="text-gray-400 italic">La letra aparecerá aquí...</p>
                    )}
                  </div>
                </div>
              </div>

              {/* Botón para continuar al paso 2 */}
              <div className="mt-8 flex flex-col items-center gap-4">
                {error && <p className="w-full text-center text-red-700 font-bold">{error}</p>}
                <button
                  onClick={handleContinueToChords}
                  className="bg-[var(--accent-color)] text-[var(--bg-color)] py-3 px-12 rounded-sm playfair-font text-xl tracking-wider hover:bg-[var(--primary-color)] transition-transform transform hover:scale-105 flex items-center gap-2"
                >
                  Continuar a Acordes
                  <i className="material-icons">arrow_forward</i>
                </button>
              </div>
            </>
          ) : (
            /* PASO 2: Agregar acordes línea por línea */
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
              {/* Columna Izquierda: Línea actual */}
              <div className="lg:col-span-2 bg-white p-6 rounded shadow-lg border-2 border-[var(--accent-color)]">
                <div className="flex items-center justify-between mb-4">
                  <h2 className="text-xl font-bold text-[var(--accent-color)] playfair-font">
                    Línea {currentLineIndex + 1} de {lyricsWithChords.length}
                  </h2>
                  <button
                    onClick={handleBackToInfo}
                    className="text-sm text-gray-600 hover:text-[var(--accent-color)] flex items-center gap-1"
                  >
                    <i className="material-icons text-base">edit</i>
                    Editar Info
                  </button>
                </div>

                <p className="text-sm text-gray-600 mb-4">
                  {draggedChord 
                    ? `🎵 Haz clic donde quieras colocar "${draggedChord.name}"` 
                    : '👆 Selecciona un acorde y haz clic en la línea'}
                </p>

                {/* Línea actual con acordes */}
                <div className="bg-gray-50 p-8 rounded border-2 border-[var(--accent-color)] mb-6">
                  <div
                    className="lyrics-line text-2xl"
                    onClick={handleCurrentLineClick}
                    style={{ cursor: draggedChord ? 'crosshair' : 'default', minHeight: '60px' }}
                  >
                    {/* Acordes sobre la línea */}
                    {currentLine && currentLine.chords.map((chord, chordIndex) => (
                      <span
                        key={chordIndex}
                        className="chord-marker"
                        style={{ 
                          left: `${(chord.start / (currentLine.text.length || 1)) * 100}%`,
                          fontSize: '16px'
                        }}
                        onClick={(e) => {
                          e.stopPropagation();
                          removeChordFromCurrentLine(chordIndex);
                        }}
                        title="Click para eliminar"
                      >
                        {chord.name}
                      </span>
                    ))}
                    {/* Texto de la línea */}
                    <span>{currentLine?.text || '\u00A0'}</span>
                  </div>
                </div>

                {/* Controles de navegación */}
                <div className="flex items-center justify-between">
                  <button
                    onClick={goToPreviousLine}
                    disabled={currentLineIndex === 0}
                    className="bg-gray-500 text-white py-2 px-6 rounded hover:bg-gray-600 transition disabled:opacity-30 disabled:cursor-not-allowed flex items-center gap-2"
                  >
                    <i className="material-icons">arrow_back</i>
                    Anterior
                  </button>

                  <button
                    onClick={skipLine}
                    className="bg-gray-400 text-white py-2 px-6 rounded hover:bg-gray-500 transition flex items-center gap-2"
                  >
                    <i className="material-icons">skip_next</i>
                    Saltar
                  </button>

                  <button
                    onClick={goToNextLine}
                    disabled={currentLineIndex >= lyricsWithChords.length - 1}
                    className="bg-[var(--accent-color)] text-white py-2 px-6 rounded hover:bg-[var(--primary-color)] transition disabled:opacity-30 disabled:cursor-not-allowed flex items-center gap-2"
                  >
                    Siguiente
                    <i className="material-icons">arrow_forward</i>
                  </button>
                </div>

                {/* Vista previa completa (colapsable) */}
                <details className="mt-6">
                  <summary className="cursor-pointer text-[var(--accent-color)] font-bold mb-2">
                    Ver canción completa
                  </summary>
                  <div className="bg-gray-50 p-4 rounded border border-gray-300 max-h-64 overflow-y-auto">
                    {lyricsWithChords.map((line, lineIndex) => (
                      <div
                        key={lineIndex}
                        className={`lyrics-line ${lineIndex === currentLineIndex ? 'bg-yellow-100' : ''}`}
                        style={{ fontSize: '14px', padding: '4px' }}
                      >
                        {/* Acordes */}
                        {line.chords.map((chord, chordIndex) => (
                          <span
                            key={chordIndex}
                            className="chord-marker"
                            style={{ 
                              left: `${(chord.start / (line.text.length || 1)) * 100}%`,
                              fontSize: '11px',
                              top: '-16px'
                            }}
                          >
                            {chord.name}
                          </span>
                        ))}
                        {/* Texto */}
                        <span>{line.text || '\u00A0'}</span>
                      </div>
                    ))}
                  </div>
                </details>
              </div>

              {/* Columna Derecha: Acordes disponibles */}
              <div className="lg:col-span-1 bg-white p-6 rounded shadow-lg border-2 border-[var(--secondary-color)]">
                <div className="flex items-center justify-between mb-4">
                  <h2 className="text-xl font-bold text-[var(--secondary-color)] playfair-font">
                    {showAllChords ? 'Todos' : 'Comunes'}
                  </h2>
                  <button
                    onClick={toggleChords}
                    disabled={loadingChords}
                    className="bg-[var(--secondary-color)] text-white px-4 py-1 rounded-sm text-sm hover:bg-[var(--primary-color)] transition-all transform hover:scale-105 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-1"
                  >
                    <i className="material-icons text-base">
                      {showAllChords ? 'star' : 'library_music'}
                    </i>
                    {showAllChords ? 'Comunes' : 'Todos'}
                  </button>
                </div>
                
                {loadingChords ? (
                  <p className="text-gray-500 italic">⏳ Cargando...</p>
                ) : chordsError ? (
                  <div className="text-red-600">
                    <p className="font-bold">❌ {chordsError}</p>
                  </div>
                ) : (
                  <div className="flex flex-wrap gap-2 max-h-[500px] overflow-y-auto">
                    {availableChords.map((chord) => (
                      <span
                        key={chord.id}
                        className="chord-chip"
                        draggable
                        onDragStart={() => handleChordDragStart(chord)}
                        onDragEnd={handleChordDragEnd}
                        onClick={() => setDraggedChord(chord)}
                        title={chord.fullName}
                      >
                        {chord.name}
                      </span>
                    ))}
                  </div>
                )}
              </div>
            </div>
          )}

          {/* Acordes Desconocidos y Propuestos (solo en paso 2) */}
          {step === 2 && (unknownChords.length > 0 || proposedChords.length > 0) && (
            <div className="mt-6 p-4 bg-gradient-to-r from-yellow-50 to-orange-50 border-2 border-yellow-400 rounded-lg">
              <h3 className="text-lg font-bold text-[var(--dark-text)] mb-3 flex items-center gap-2">
                <i className="material-icons text-yellow-600">warning</i>
                Acordes Detectados
              </h3>
              
              {/* Acordes Desconocidos */}
              {unknownChords.length > 0 && (
                <div className="mb-4">
                  <p className="text-sm text-gray-700 mb-2">
                    <strong>🎸 Acordes no encontrados en el catálogo:</strong>
                  </p>
                  <div className="flex flex-wrap gap-2">
                    {unknownChords.map((chordName) => (
                      <button
                        key={chordName}
                        onClick={() => handleProposeChord(chordName)}
                        className="px-3 py-1 bg-yellow-400 text-[var(--dark-text)] rounded font-mono font-bold hover:bg-yellow-500 transition flex items-center gap-1"
                      >
                        <span>{chordName}</span>
                        <i className="material-icons text-sm">add_circle</i>
                      </button>
                    ))}
                  </div>
                  <p className="text-xs text-gray-600 mt-2">
                    ℹ️ Haz clic en un acorde para proponer su inclusión en el catálogo
                  </p>
                </div>
              )}
              
              {/* Acordes Propuestos */}
              {proposedChords.length > 0 && (
                <div>
                  <p className="text-sm text-green-700 mb-2">
                    <strong>✅ Acordes propuestos ({proposedChords.length}):</strong>
                  </p>
                  <div className="flex flex-wrap gap-2">
                    {proposedChords.map((chord, index) => (
                      <div
                        key={index}
                        className="px-3 py-1 bg-green-100 border-2 border-green-500 text-[var(--dark-text)] rounded font-mono font-bold flex items-center gap-2"
                      >
                        <span>{chord.name}</span>
                        <button
                          onClick={() => setProposedChords(prev => prev.filter((_, i) => i !== index))}
                          className="text-red-600 hover:text-red-800"
                          title="Eliminar propuesta"
                        >
                          <i className="material-icons text-sm">close</i>
                        </button>
                      </div>
                    ))}
                  </div>
                  <p className="text-xs text-green-700 mt-2">
                    📝 Estos acordes serán revisados cuando la canción sea aprobada
                  </p>
                </div>
              )}
            </div>
          )}

          {/* Botones de guardado (solo en paso 2) */}
          {step === 2 && (
            <div className="mt-8 flex flex-wrap gap-4 justify-center">
              {error && <p className="w-full text-center text-red-700 font-bold">{error}</p>}
              
              <button
                onClick={handleSaveDraft}
                disabled={loading}
                className="bg-gray-500 text-white py-3 px-8 rounded-sm playfair-font text-xl tracking-wider hover:bg-gray-600 transition-transform transform hover:scale-105 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
              >
                <i className="material-icons">save</i>
                {loading ? 'Guardando...' : 'Guardar Borrador'}
              </button>
              
              <button
                onClick={handleSaveAndSubmit}
                disabled={loading}
                className="bg-[var(--accent-color)] text-[var(--bg-color)] py-3 px-8 rounded-sm playfair-font text-xl tracking-wider hover:bg-[var(--primary-color)] transition-transform transform hover:scale-105 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
              >
                <i className="material-icons">send</i>
                {loading ? 'Enviando...' : 'Guardar y Enviar para Revisión'}
              </button>
            </div>
          )}
        </div>
      </div>
      
      {/* Modal para proponer acordes */}
      <ProposeChordModal
        chordName={currentUnknownChord || ''}
        isOpen={proposeModalOpen}
        onClose={() => {
          setProposeModalOpen(false);
          setCurrentUnknownChord(null);
        }}
        onSubmit={handleSubmitProposedChord}
      />
    </>
  );
};

// Componente auxiliar para inputs
const InputField: React.FC<{
  label: string;
  value: string;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  placeholder: string;
  type?: string;
}> = ({ label, value, onChange, placeholder, type = 'text' }) => (
  <div>
    <label className="block text-sm font-bold text-[var(--primary-color)] mb-1">{label}</label>
    <input
      type={type}
      className="w-full px-3 py-2 border-2 border-[var(--primary-color)] rounded focus:outline-none focus:border-[var(--accent-color)] transition"
      value={value}
      onChange={onChange}
      placeholder={placeholder}
    />
  </div>
);

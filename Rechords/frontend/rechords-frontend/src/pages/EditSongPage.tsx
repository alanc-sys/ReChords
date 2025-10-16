import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { getSongById, updateSong, submitSongForApproval, getCommonChords, getAvailableChords, uploadSongCover, deleteSongCover } from '../api/songApi';
import type { CreateSongRequest, ChordInfo, LineWithChords, ChordPositionInfo, SongWithChordsResponse } from '../types/song';
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

export const EditSongPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [loadingSong, setLoadingSong] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // Canción original
  const [originalSong, setOriginalSong] = useState<SongWithChordsResponse | null>(null);
  
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
  const [coverColor, setCoverColor] = useState(''); // Mantener el existente
  const [coverImageUrl, setCoverImageUrl] = useState<string | undefined>(undefined);
  const [isUploadingCover, setIsUploadingCover] = useState(false);
  
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

  // Cargar canción existente al montar
  useEffect(() => {
    if (!id) {
      setError('ID de canción no proporcionado');
      setLoadingSong(false);
      return;
    }

    getSongById(Number(id))
      .then(song => {
        setOriginalSong(song);
        
        // Llenar formulario con datos existentes
        setTitle(song.title);
        setArtist(song.artist);
        setAlbum(song.album || '');
        setYear(song.year);
        setKey(song.key || '');
        setTempo(song.tempo);
        setYoutubeUrl(song.youtubeUrl || '');
        setSpotifyUrl(song.spotifyUrl || '');
        setCoverColor(song.coverColor || '');
        setCoverImageUrl(song.coverImageUrl || undefined);
        
        // Convertir lyrics a texto
        if (song.lyrics && song.lyrics.length > 0) {
          const text = song.lyrics.map(line => line.text).join('\n');
          setLyricsText(text);
          setLyricsWithChords(song.lyrics);
          
          // Si ya tiene acordes, ir directo al paso 2
          const hasChords = song.lyrics.some(line => line.chords && line.chords.length > 0);
          if (hasChords) {
            setStep(2); // Ir directo al paso de acordes
          }
        }
        
        setLoadingSong(false);
      })
      .catch(err => {
        console.error('Error cargando canción:', err);
        setError('Error al cargar la canción. Es posible que no exista o no tengas permisos para editarla.');
        setLoadingSong(false);
      });
  }, [id]);

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

    // Verificar si el texto cambió comparado con el que ya tenemos parseado
    const currentText = lyricsWithChords.map(line => line.text).join('\n');
    const textChanged = currentText !== lyricsText;
    
    // Si el texto NO cambió, simplemente continuar al paso 2
    if (!textChanged && lyricsWithChords.length > 0) {
      setStep(2);
      setError(null);
      return;
    }

    // Si cambió, advertir al usuario
    if (textChanged && lyricsWithChords.some(l => l.chords && l.chords.length > 0)) {
      const confirmChange = window.confirm(
        '⚠️ Has modificado la letra.\n\n' +
        'Esto puede causar que algunos acordes se desalineen o se pierdan.\n\n' +
        '¿Estás seguro de continuar?'
      );
      
      if (!confirmChange) {
        return;
      }
    }

    // Parsear letra en líneas
    const lines = lyricsText.split('\n');
    
    // Preservar acordes buscando líneas por texto exacto
    const parsed: LineWithChords[] = lines.map((text, index) => {
      // Buscar línea con el mismo texto (sin importar el índice)
      const existingLine = lyricsWithChords.find(l => l.text === text);
      
      return {
        lineNumber: index,
        text: text,
        chords: existingLine?.chords || [] // Preservar acordes si encontramos la línea
      };
    });
    
    setLyricsWithChords(parsed);
    setCurrentLineIndex(0);
    setStep(2);
    setError(null);
  };

  const handleBackToInfo = () => {
    // Actualizar el texto de la letra con las líneas actuales (por si se modificaron acordes)
    const updatedText = lyricsWithChords.map(line => line.text).join('\n');
    setLyricsText(updatedText);
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

  // Mover acorde hacia la izquierda (reducir posición)
  const moveChordLeft = (chordIndex: number) => {
    const updatedLines = [...lyricsWithChords];
    const chord = updatedLines[currentLineIndex].chords[chordIndex];
    if (chord.start > 0) {
      chord.start = Math.max(0, chord.start - 1);
      setLyricsWithChords(updatedLines);
    }
  };

  // Mover acorde hacia la derecha (aumentar posición)
  const moveChordRight = (chordIndex: number) => {
    const updatedLines = [...lyricsWithChords];
    const chord = updatedLines[currentLineIndex].chords[chordIndex];
    const maxPos = currentLine?.text.length || 0;
    if (chord.start < maxPos) {
      chord.start = Math.min(maxPos, chord.start + 1);
      setLyricsWithChords(updatedLines);
    }
  };

  // Limpiar todos los acordes de la línea actual
  const clearCurrentLineChords = () => {
    const confirm = window.confirm('¿Eliminar todos los acordes de esta línea?');
    if (confirm) {
      const updatedLines = [...lyricsWithChords];
      updatedLines[currentLineIndex].chords = [];
      setLyricsWithChords(updatedLines);
    }
  };

  // Actualizar solo información básica sin tocar acordes
  const handleUpdateInfoOnly = async () => {
    if (!id) {
      setError('❌ ID de canción no encontrado');
      return;
    }

    if (!title.trim() || !artist.trim()) {
      setError('❌ El título y artista son obligatorios');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      // Usar los acordes existentes exactamente como están
      const songData: CreateSongRequest = {
        title,
        artist,
        album: album || undefined,
        year: year || undefined,
        key: key || undefined,
        tempo: tempo || undefined,
        youtubeUrl: youtubeUrl || undefined,
        spotifyUrl: spotifyUrl || undefined,
        coverImageUrl: coverImageUrl || originalSong?.coverImageUrl,
        coverColor: coverImageUrl ? undefined : (coverColor || originalSong?.coverColor || generateRandomColor()),
        lyrics: lyricsWithChords, // Mantener acordes sin cambios
        proposedChords: proposedChords.length > 0 ? proposedChords : undefined
      };

      const response = await updateSong(Number(id), songData);
      
      let message = `✅ Información actualizada: "${response.title}"\n(Los acordes se mantuvieron sin cambios)`;
      if (originalSong?.status === 'APPROVED') {
        message += '\n\n⚠️ La canción pasará a revisión nuevamente (estado PENDING).';
      }
      
      alert(message);
      navigate('/home');
    } catch (err: any) {
      console.error('Error al actualizar:', err);
      const errorMsg = err.response?.data?.message || err.response?.data || err.message || 'Error desconocido';
      setError('❌ Error al actualizar: ' + errorMsg);
    } finally {
      setLoading(false);
    }
  };

  const handleSaveDraft = async () => {
    if (!id) {
      setError('❌ ID de canción no encontrado');
      return;
    }

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
        coverImageUrl: coverImageUrl || originalSong?.coverImageUrl,
        coverColor: coverImageUrl ? undefined : (coverColor || originalSong?.coverColor || generateRandomColor()),
        lyrics: validLyrics,
        proposedChords: proposedChords.length > 0 ? proposedChords : undefined
      };

      const response = await updateSong(Number(id), songData);
      
      let message = `✅ Canción actualizada: "${response.title}"`;
      if (originalSong?.status === 'APPROVED') {
        message += '\n⚠️ La canción pasará a revisión nuevamente (estado PENDING).';
      }
      
      alert(message);
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
    if (!id) {
      setError('❌ ID de canción no encontrado');
      return;
    }

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
        coverImageUrl: coverImageUrl || originalSong?.coverImageUrl,
        coverColor: coverImageUrl ? undefined : (coverColor || originalSong?.coverColor || generateRandomColor()),
        lyrics: validLyrics,
        proposedChords: proposedChords.length > 0 ? proposedChords : undefined
      };

      // Actualizar canción
      const updatedSong = await updateSong(Number(id), songData);
      
      // Si no está en PENDING (ya pasó a PENDING al editar APPROVED, o está en DRAFT/REJECTED), enviar para aprobación
      if (updatedSong.status !== 'PENDING') {
        await submitSongForApproval(updatedSong.id);
      }
      
      alert(`✅ Canción "${updatedSong.title}" actualizada y enviada para revisión`);
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

  // Loading inicial
  if (loadingSong) {
    return (
      <>
        <VintageStyles />
        <div className="grainy-texture"></div>
        <div className="flex items-center justify-center min-h-screen">
          <div className="text-center">
            <div className="text-2xl font-bold text-[var(--dark-text)] mb-4">Cargando canción...</div>
            <div className="text-gray-600">Espera un momento</div>
          </div>
        </div>
      </>
    );
  }

  // Si hubo error al cargar
  if (!originalSong && error) {
    return (
      <>
        <VintageStyles />
        <div className="grainy-texture"></div>
        <div className="flex items-center justify-center min-h-screen">
          <div className="text-center">
            <div className="text-2xl font-bold text-red-600 mb-4">❌ Error</div>
            <div className="text-gray-700 mb-6">{error}</div>
            <button
              onClick={() => navigate('/home')}
              className="bg-[var(--primary-color)] text-white px-6 py-3 rounded hover:bg-[var(--accent-color)]"
            >
              Volver al Inicio
            </button>
          </div>
        </div>
      </>
    );
  }

  return (
    <>
      <VintageStyles />
      <div className="min-h-screen p-6 bg-[var(--bg-color)]">
        <div className="grainy-texture"></div>
        
        <div className="max-w-7xl mx-auto relative z-10">
          {/* Advertencia si la canción está APPROVED */}
          {originalSong?.status === 'APPROVED' && (
            <div className="mb-6 p-4 bg-yellow-100 border-2 border-yellow-600 rounded-lg">
              <div className="flex items-center gap-3">
                <i className="material-icons text-yellow-700 text-3xl">warning</i>
                <div>
                  <h4 className="font-bold text-yellow-800 text-lg">
                    ⚠️ Esta canción está APROBADA
                  </h4>
                  <p className="text-sm text-yellow-700">
                    Al guardar los cambios, la canción pasará automáticamente a estado <strong>PENDING</strong> y requerirá nueva aprobación del administrador.
                  </p>
                </div>
              </div>
            </div>
          )}
          
          {/* Header */}
          <div className="flex items-center justify-between mb-8">
            <h1 className="playfair-font text-4xl md:text-5xl text-[var(--dark-text)] vintage-title">
              {step === 1 ? 'EDITAR CANCIÓN - INFORMACIÓN' : 'EDITAR CANCIÓN - AGREGAR ACORDES'}
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

                  {/* Portada */}
                  <div className="pt-4 border-t-2 border-dashed border-gray-300">
                    <h3 className="text-sm font-bold text-[var(--dark-text)] mb-3 flex items-center gap-2">
                      <span>🖼️</span> Portada (Opcional)
                    </h3>
                    <div className="flex items-center gap-4">
                      {coverImageUrl ? (
                        <img src={coverImageUrl} alt="Portada" className="w-24 h-24 object-cover border-2" />
                      ) : (
                        <div className="w-24 h-24 flex items-center justify-center text-xs text-gray-500 border-2">Sin portada</div>
                      )}
                      <div className="flex flex-col gap-2">
                        <label className="text-xs">Subir imagen (max 5MB)</label>
                        <input
                          type="file"
                          accept="image/*"
                          onChange={async (e) => {
                            const file = e.target.files?.[0];
                            if (!file || !id) return;
                            setIsUploadingCover(true);
                            try {
                              const url = await uploadSongCover(Number(id), file);
                              setCoverImageUrl(url);
                              alert('✅ Portada actualizada');
                            } catch (err: any) {
                              alert('❌ Error al subir portada');
                            } finally {
                              setIsUploadingCover(false);
                            }
                          }}
                          disabled={isUploadingCover}
                          className="text-sm"
                        />
                        {coverImageUrl && (
                          <button
                            type="button"
                            onClick={async () => {
                              if (!id) return;
                              if (!confirm('¿Eliminar portada?')) return;
                              try {
                                await deleteSongCover(Number(id));
                                setCoverImageUrl(undefined);
                                alert('✅ Portada eliminada');
                              } catch (err: any) {
                                alert('❌ Error al eliminar portada');
                              }
                            }}
                            className="px-3 py-1 bg-red-600 text-white rounded text-xs hover:bg-red-700 w-fit"
                          >
                            Eliminar portada
                          </button>
                        )}
                      </div>
                    </div>
                    <p className="text-xs text-gray-600 mt-2">Si no subes una imagen, se usará un color de portada.</p>
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

              {/* Botones de acción (Paso 1) */}
              <div className="mt-8 flex flex-col items-center gap-4">
                {error && <p className="w-full text-center text-red-700 font-bold">{error}</p>}
                <button
                  onClick={handleContinueToChords}
                  className="bg-[var(--accent-color)] text-[var(--bg-color)] py-3 px-12 rounded-sm playfair-font text-xl tracking-wider hover:bg-[var(--primary-color)] transition-transform transform hover:scale-105 flex items-center gap-2"
                >
                  Continuar a Acordes
                  <i className="material-icons">arrow_forward</i>
                </button>
                <button
                  onClick={handleUpdateInfoOnly}
                  disabled={loading}
                  className="bg-gray-700 text-white py-3 px-12 rounded-sm playfair-font text-xl tracking-wider hover:bg-gray-800 transition-transform transform hover:scale-105 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
                >
                  Guardar info (sin cambiar acordes)
                  <i className="material-icons">save</i>
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

                <div className="flex items-center justify-between mb-4">
                  <p className="text-sm text-gray-600">
                    {draggedChord 
                      ? `🎵 Haz clic donde quieras colocar "${draggedChord.name}"` 
                      : '👆 Selecciona un acorde y haz clic en la línea'}
                  </p>
                  {currentLine && currentLine.chords && currentLine.chords.length > 0 && (
                    <button
                      onClick={clearCurrentLineChords}
                      className="text-xs text-white bg-red-600 hover:bg-red-700 flex items-center gap-1 px-3 py-2 border-2 border-red-700 rounded shadow-sm transition"
                      title="Eliminar todos los acordes de esta línea"
                    >
                      <i className="material-icons text-sm">delete_sweep</i>
                      Limpiar Acordes
                    </button>
                  )}
                </div>

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

                {/* Panel de ajuste de acordes */}
                {currentLine && currentLine.chords && currentLine.chords.length > 0 && (
                  <div className="mb-6 p-4 bg-[#F5EFE6] border-2 border-[var(--secondary-color)] rounded shadow-sm">
                    <h3 className="text-sm font-bold text-[var(--dark-text)] mb-3 flex items-center gap-2">
                      <i className="material-icons text-base">tune</i>
                      Ajustar Posiciones (si se desalinearon)
                    </h3>
                    <div className="space-y-2">
                      {currentLine.chords.map((chord, chordIndex) => (
                        <div key={chordIndex} className="flex items-center gap-3 p-2 bg-white rounded border-2 border-[var(--primary-color)] shadow-sm">
                          <span className="font-bold text-[var(--accent-color)] min-w-[60px]">{chord.name}</span>
                          <span className="text-xs text-[var(--dark-text)] min-w-[80px]">Pos: {chord.start}</span>
                          <div className="flex gap-1">
                            <button
                              onClick={() => moveChordLeft(chordIndex)}
                              disabled={chord.start === 0}
                              className="px-2 py-1 bg-[var(--accent-color)] text-white rounded text-xs hover:bg-[var(--primary-color)] disabled:bg-gray-300 disabled:cursor-not-allowed flex items-center gap-1 transition"
                              title="Mover a la izquierda"
                            >
                              <i className="material-icons text-sm">chevron_left</i>
                              ←
                            </button>
                            <button
                              onClick={() => moveChordRight(chordIndex)}
                              disabled={chord.start >= (currentLine.text.length || 0)}
                              className="px-2 py-1 bg-[var(--accent-color)] text-white rounded text-xs hover:bg-[var(--primary-color)] disabled:bg-gray-300 disabled:cursor-not-allowed flex items-center gap-1 transition"
                              title="Mover a la derecha"
                            >
                              →
                              <i className="material-icons text-sm">chevron_right</i>
                            </button>
                            <button
                              onClick={() => removeChordFromCurrentLine(chordIndex)}
                              className="px-2 py-1 bg-red-600 text-white rounded text-xs hover:bg-red-700 flex items-center gap-1 ml-2 transition"
                              title="Eliminar acorde"
                            >
                              <i className="material-icons text-sm">delete</i>
                            </button>
                          </div>
                        </div>
                      ))}
                    </div>
                    <p className="text-xs text-[var(--dark-text)] mt-3">
                      💡 Usa las flechas ← → para ajustar la posición de cada acorde
                    </p>
                  </div>
                )}

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
                {loading ? 'Actualizando...' : 'Actualizar Borrador'}
              </button>
              
              <button
                onClick={handleSaveAndSubmit}
                disabled={loading}
                className="bg-[var(--accent-color)] text-[var(--bg-color)] py-3 px-8 rounded-sm playfair-font text-xl tracking-wider hover:bg-[var(--primary-color)] transition-transform transform hover:scale-105 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
              >
                <i className="material-icons">send</i>
                {loading ? 'Enviando...' : 'Actualizar y Enviar para Revisión'}
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

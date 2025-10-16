import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { importSong, createSong, submitSongForApproval } from '../api/songApi';
import type { CreateSongRequest } from '../types/song';

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
  `}</style>
);

export const ImportSongPage: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  
  const [rawText, setRawText] = useState('');
  const [parsedSong, setParsedSong] = useState<CreateSongRequest | null>(null);

  const handleParseText = async () => {
    if (!rawText.trim()) {
      setError('❌ Debes ingresar texto para importar');
      return;
    }

    setLoading(true);
    setError(null);
    setSuccess(null);

    try {
      const parsed = await importSong(rawText);
      setParsedSong(parsed);
      setSuccess('✅ Canción parseada exitosamente. Revisa los datos antes de guardar.');
    } catch (err: any) {
      setError('❌ Error al parsear: ' + (err.response?.data?.message || err.message));
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleSaveDraft = async () => {
    if (!parsedSong) {
      setError('❌ Primero debes parsear el texto');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const response = await createSong(parsedSong);
      alert(`✅ Borrador guardado exitosamente: "${response.title}"`);
      navigate('/home');
    } catch (err: any) {
      setError('❌ Error al guardar borrador: ' + (err.response?.data?.message || err.message));
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleSaveAndSubmit = async () => {
    if (!parsedSong) {
      setError('❌ Primero debes parsear el texto');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const createdSong = await createSong(parsedSong);
      await submitSongForApproval(createdSong.id);
      alert(`✅ Canción "${createdSong.title}" enviada para revisión`);
      navigate('/home');
    } catch (err: any) {
      setError('❌ Error al enviar para revisión: ' + (err.response?.data?.message || err.message));
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <VintageStyles />
      <div className="min-h-screen p-6 bg-[var(--bg-color)]">
        <div className="grainy-texture"></div>
        
        <div className="max-w-6xl mx-auto relative z-10">
          {/* Header */}
          <div className="flex items-center justify-between mb-8">
            <h1 className="playfair-font text-4xl md:text-5xl text-[var(--dark-text)] vintage-title">
              IMPORTAR CANCIÓN
            </h1>
            <button
              onClick={() => navigate('/home')}
              className="bg-gray-400 text-white py-2 px-4 rounded hover:bg-gray-500 transition flex items-center gap-2"
            >
              <i className="material-icons">arrow_back</i>
              Volver
            </button>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Columna Izquierda: Texto a importar */}
            <div className="bg-white p-6 rounded shadow-lg border-2 border-[var(--primary-color)]">
              <h2 className="text-xl font-bold text-[var(--primary-color)] mb-4 playfair-font">Texto de la Canción</h2>
              <p className="text-sm text-gray-600 mb-4">
                Pega aquí el texto de la canción con acordes. El sistema intentará detectar automáticamente el formato.
              </p>
              
              <textarea
                className="w-full h-96 p-3 border-2 border-[var(--primary-color)] rounded focus:outline-none focus:border-[var(--accent-color)] font-mono text-sm"
                value={rawText}
                onChange={(e) => setRawText(e.target.value)}
                placeholder={`Ejemplo:
Título: Mi Canción
Artista: Mi Banda

    C              Am
Esta es la letra de mi canción
    F              G
Con acordes sobre las palabras...`}
              />

              <button
                onClick={handleParseText}
                disabled={loading}
                className="mt-4 w-full bg-[var(--primary-color)] text-white py-3 px-6 rounded-sm playfair-font text-xl tracking-wider hover:bg-[var(--accent-color)] transition-transform transform hover:scale-105 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
              >
                <i className="material-icons">auto_fix_high</i>
                {loading ? 'Procesando...' : 'Parsear Texto'}
              </button>
            </div>

            {/* Columna Derecha: Vista previa parseada */}
            <div className="bg-white p-6 rounded shadow-lg border-2 border-[var(--accent-color)]">
              <h2 className="text-xl font-bold text-[var(--accent-color)] mb-4 playfair-font">Vista Previa</h2>
              
              {!parsedSong ? (
                <p className="text-gray-400 italic">La canción parseada aparecerá aquí...</p>
              ) : (
                <div className="space-y-4">
                  <div className="bg-gray-50 p-4 rounded border border-gray-300">
                    <h3 className="font-bold text-lg text-[var(--dark-text)]">{parsedSong.title || 'Sin título'}</h3>
                    <p className="text-gray-600">{parsedSong.artist || 'Sin artista'}</p>
                    {parsedSong.album && <p className="text-sm text-gray-500">Álbum: {parsedSong.album}</p>}
                    {parsedSong.year && <p className="text-sm text-gray-500">Año: {parsedSong.year}</p>}
                    {parsedSong.key && <p className="text-sm text-gray-500">Tonalidad: {parsedSong.key}</p>}
                    {parsedSong.tempo && <p className="text-sm text-gray-500">Tempo: {parsedSong.tempo} BPM</p>}
                  </div>

                  <div className="bg-gray-50 p-4 rounded border border-gray-300 max-h-96 overflow-y-auto" style={{ fontFamily: 'Courier New, monospace' }}>
                    <h4 className="font-bold text-sm text-gray-700 mb-2">Letra con Acordes:</h4>
                    {parsedSong.lyrics.map((line, index) => (
                      <div key={index} className="mb-3" style={{ position: 'relative', lineHeight: '1.5' }}>
                        {/* Línea de acordes */}
                        {line.chords && line.chords.length > 0 && (
                          <div style={{ 
                            position: 'relative', 
                            height: '20px',
                            color: 'var(--accent-color)', 
                            fontWeight: 'bold',
                            fontSize: '14px',
                            whiteSpace: 'pre'
                          }}>
                            {line.chords.map((chord, cIndex) => (
                              <span
                                key={cIndex}
                                style={{
                                  position: 'absolute',
                                  left: `${chord.start}ch`,
                                  top: 0,
                                }}
                              >
                                {chord.name}
                              </span>
                            ))}
                          </div>
                        )}
                        {/* Línea de texto */}
                        <div style={{ 
                          color: '#333', 
                          fontSize: '14px',
                          whiteSpace: 'pre-wrap',
                          wordBreak: 'keep-all'
                        }}>
                          {line.text || '\u00A0'}
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          </div>

          {/* Mensajes y Botones */}
          <div className="mt-8">
            {error && <p className="text-center text-red-700 font-bold mb-4">{error}</p>}
            {success && <p className="text-center text-green-700 font-bold mb-4">{success}</p>}
            
            {parsedSong && (
              <div className="flex flex-wrap gap-4 justify-center">
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
      </div>
    </>
  );
};

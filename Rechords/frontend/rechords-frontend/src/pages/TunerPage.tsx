import React, { useEffect, useRef, useState } from 'react';
import { API_CONFIG } from '../config/api';

// Definición de afinaciones de guitarra
type GuitarTuning = {
  name: string;
  strings: { note: string; frequency: number }[];
};

const TUNINGS: GuitarTuning[] = [
  {
    name: 'Estándar (E)',
    strings: [
      { note: 'E4', frequency: 329.63 },
      { note: 'B3', frequency: 246.94 },
      { note: 'G3', frequency: 196.00 },
      { note: 'D3', frequency: 146.83 },
      { note: 'A2', frequency: 110.00 },
      { note: 'E2', frequency: 82.41 },
    ],
  },
  {
    name: 'Drop D',
    strings: [
      { note: 'E4', frequency: 329.63 },
      { note: 'B3', frequency: 246.94 },
      { note: 'G3', frequency: 196.00 },
      { note: 'D3', frequency: 146.83 },
      { note: 'A2', frequency: 110.00 },
      { note: 'D2', frequency: 73.42 },
    ],
  },
  {
    name: 'DADGAD',
    strings: [
      { note: 'D4', frequency: 293.66 },
      { note: 'A3', frequency: 220.00 },
      { note: 'G3', frequency: 196.00 },
      { note: 'D3', frequency: 146.83 },
      { note: 'A2', frequency: 110.00 },
      { note: 'D2', frequency: 73.42 },
    ],
  },
  {
    name: 'Open G',
    strings: [
      { note: 'D4', frequency: 293.66 },
      { note: 'B3', frequency: 246.94 },
      { note: 'G3', frequency: 196.00 },
      { note: 'D3', frequency: 146.83 },
      { note: 'G2', frequency: 98.00 },
      { note: 'D2', frequency: 73.42 },
    ],
  },
  {
    name: 'Half Step Down',
    strings: [
      { note: 'D#4', frequency: 311.13 },
      { note: 'A#3', frequency: 233.08 },
      { note: 'F#3', frequency: 185.00 },
      { note: 'C#3', frequency: 138.59 },
      { note: 'G#2', frequency: 103.83 },
      { note: 'D#2', frequency: 77.78 },
    ],
  },
];

export const TunerPage: React.FC = () => {
  const [connected, setConnected] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [freq, setFreq] = useState<number>(0);
  const [cents, setCents] = useState<number>(0);
  const [wsUrl, setWsUrl] = useState<string>('');
  const [selectedTuning, setSelectedTuning] = useState(0); // Índice de la afinación
  const [isListening, setIsListening] = useState(false);

  const wsRef = useRef<WebSocket | null>(null);
  const mediaStreamRef = useRef<MediaStream | null>(null);
  const audioContextRef = useRef<AudioContext | null>(null);
  const processorRef = useRef<ScriptProcessorNode | null>(null);
  
  // Función para encontrar la cuerda más cercana
  const findClosestString = (frequency: number) => {
    if (frequency === 0) return -1;
    
    const strings = TUNINGS[selectedTuning].strings;
    let closest = 0;
    let minDiff = Math.abs(frequency - strings[0].frequency);
    
    for (let i = 1; i < strings.length; i++) {
      const diff = Math.abs(frequency - strings[i].frequency);
      if (diff < minDiff) {
        minDiff = diff;
        closest = i;
      }
    }
    
    return closest;
  };
  
  const closestStringIndex = findClosestString(freq);
  const currentTuning = TUNINGS[selectedTuning];

  const connectWebSocket = () => {
    const url = API_CONFIG.BASE_URL.replace('http://', 'ws://').replace('https://', 'wss://') + '/ws/tuner';
    setWsUrl(url);
    console.log('Conectando WebSocket a:', url);
    
    try {
      const ws = new WebSocket(url);
      wsRef.current = ws;

      ws.onopen = () => {
        console.log('WebSocket conectado');
        setConnected(true);
        setError(null);
      };
      
      ws.onclose = (event) => {
        console.log('WebSocket cerrado:', event.code, event.reason);
        setConnected(false);
        setError(`Conexión cerrada (código ${event.code})`);
      };
      
      ws.onerror = (event) => {
        console.error('Error WebSocket:', event);
        setError('Error de conexión WebSocket - Verifica que el backend esté corriendo');
      };
      
      ws.onmessage = (ev) => {
        try {
          const data = JSON.parse(ev.data);
          console.log('Mensaje recibido:', data);
          if (data.type === 'pitch') {
            setFreq(data.frequency);
            setCents(Math.round(data.cents));
          }
        } catch (e) {
          console.error('Error parseando mensaje:', e);
        }
      };
    } catch (e: any) {
      console.error('Error creando WebSocket:', e);
      setError(`Error creando WebSocket: ${e.message}`);
    }
  };

  useEffect(() => {
    connectWebSocket();
    
    return () => {
      if (wsRef.current) {
        wsRef.current.close();
      }
    };
  }, []);

  const start = async () => {
    try {
      setError(null);
      const stream = await navigator.mediaDevices.getUserMedia({ audio: { channelCount: 1, sampleRate: 44100 }, video: false });
      mediaStreamRef.current = stream;

      const audioContext = new (window.AudioContext || (window as any).webkitAudioContext)({ sampleRate: 44100 });
      audioContextRef.current = audioContext;

      const source = audioContext.createMediaStreamSource(stream);
      const processor = audioContext.createScriptProcessor(4096, 1, 1);
      processorRef.current = processor;

      processor.onaudioprocess = (e) => {
        const input = e.inputBuffer.getChannelData(0);
        // Convertir Float32 [-1,1] a PCM 16-bit LE
        const buffer = new ArrayBuffer(input.length * 2);
        const view = new DataView(buffer);
        for (let i = 0; i < input.length; i++) {
          let s = Math.max(-1, Math.min(1, input[i]));
          view.setInt16(i * 2, s < 0 ? s * 0x8000 : s * 0x7fff, true);
        }
        if (wsRef.current && wsRef.current.readyState === WebSocket.OPEN) {
          wsRef.current.send(buffer);
        }
      };

      source.connect(processor);
      processor.connect(audioContext.destination);
            setIsListening(true);
    } catch (err: any) {
      setError(err?.message || 'No se pudo acceder al micrófono');
    }
  };

  const stop = () => {
    processorRef.current?.disconnect();
    audioContextRef.current?.close();
    mediaStreamRef.current?.getTracks().forEach(t => t.stop());
    setIsListening(false);
    setFreq(0);
    setCents(0);
  };

  return (
    <div className="min-h-screen p-6 bg-[var(--bg-color)]">
      <div className="max-w-4xl mx-auto">
        <h1 className="playfair-font text-4xl font-bold text-center mb-6 vintage-title">
          AFINADOR DE GUITARRA
        </h1>
        
        {/* Selector de afinación */}
        <div className="mb-6 p-4 border-4 border-[var(--dark-text)] rounded bg-[var(--bg-color)]" style={{ boxShadow: '6px 6px 0px var(--primary-color)' }}>
          <label className="block text-sm font-bold mb-2 text-[var(--dark-text)]">AFINACIÓN:</label>
          <select 
            value={selectedTuning}
            onChange={(e) => setSelectedTuning(Number(e.target.value))}
            className="w-full p-2 border-2 border-[var(--dark-text)] rounded font-bold"
            disabled={isListening}
          >
            {TUNINGS.map((tuning, idx) => (
              <option key={idx} value={idx}>{tuning.name}</option>
            ))}
          </select>
        </div>

        {/* Cuerdas de guitarra */}
        <div className="mb-6 space-y-3">
          {currentTuning.strings.map((string, idx) => {
            const isActive = closestStringIndex === idx && freq > 0;
            const isTuned = isActive && Math.abs(cents) < 5;
            const isTooLow = isActive && cents < -5;
            const isTooHigh = isActive && cents > 5;

    return (
              <div 
                key={idx}
                className={`p-4 border-4 border-[var(--dark-text)] rounded transition-all ${
                  isActive ? 'bg-[var(--accent-color)] transform scale-105' : 'bg-[var(--bg-color)]'
                }`}
                style={{ 
                  boxShadow: isActive ? '8px 8px 0px var(--primary-color)' : '4px 4px 0px var(--primary-color)'
                }}
              >
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-4">
                    <div className={`text-3xl font-bold playfair-font ${isActive ? 'text-white' : 'text-[var(--dark-text)]'}`}>
                      {6 - idx}ª
                    </div>
                    <div>
                      <div className={`text-2xl font-bold playfair-font ${isActive ? 'text-white' : 'text-[var(--dark-text)]'}`}>
                        {string.note}
                      </div>
                      <div className={`text-sm ${isActive ? 'text-white/80' : 'text-gray-600'}`}>
                        {string.frequency.toFixed(2)} Hz
                      </div>
                    </div>
                  </div>
                  
                  {/* Indicador visual */}
                  {isActive && (
                    <div className="flex items-center gap-3">
                      {isTooLow && (
                        <div className="flex flex-col items-center">
                          <div className="text-white font-bold">↓ GRAVE</div>
                          <div className="text-white text-sm">{cents.toFixed(0)} cents</div>
                        </div>
                      )}
                      {isTuned && (
                        <div className="flex items-center gap-2">
                          <div className="w-12 h-12 rounded-full bg-green-400 flex items-center justify-center animate-pulse">
                            <span className="text-2xl">✓</span>
                          </div>
                          <div className="text-white font-bold">¡AFINADO!</div>
                        </div>
                      )}
                      {isTooHigh && (
                        <div className="flex flex-col items-center">
                          <div className="text-white font-bold">↑ AGUDO</div>
                          <div className="text-white text-sm">{cents.toFixed(0)} cents</div>
                        </div>
                      )}
                    </div>
                  )}
                </div>

                {/* Barra de afinación */}
                {isActive && (
                  <div className="mt-3">
                    <div className="relative h-2 bg-white/30 rounded-full overflow-hidden">
                      <div className="absolute inset-y-0 left-1/2 w-0.5 bg-white z-10"></div>
                      <div 
                        className={`absolute inset-y-0 h-full transition-all ${
                          isTuned ? 'bg-green-400' : isTooLow ? 'bg-blue-400' : 'bg-red-400'
                        }`}
                        style={{
                          width: '50%',
                          left: `${Math.max(0, Math.min(50, 50 + cents))}%`
                        }}
                      ></div>
                    </div>
                  </div>
                )}
              </div>
            );
          })}
        </div>

        {/* Controles */}
        <div className="flex gap-3 mb-4">
          <button 
            className="btn-vintage flex-1 px-4 py-3 rounded-sm font-bold text-lg"
            onClick={isListening ? stop : start}
            disabled={!connected}
          >
            {isListening ? '⏹ DETENER' : '▶ INICIAR AFINADOR'}
          </button>
        </div>

        {/* Info de conexión colapsable */}
        <details className="mt-4">
          <summary className="cursor-pointer text-sm text-gray-600 hover:text-gray-800">
            Información de conexión
          </summary>
          <div className="mt-2 p-3 border rounded bg-gray-50 text-xs">
            <div className="flex items-center gap-2 mb-2">
              <div className={`w-2 h-2 rounded-full ${connected ? 'bg-green-500' : 'bg-red-500'}`}></div>
              <span>{connected ? 'Conectado' : 'Desconectado'}</span>
            </div>
            <div className="font-mono text-xs break-all text-gray-600">{wsUrl}</div>
            {error && <div className="mt-2 text-red-600">{error}</div>}
          </div>
        </details>

        {/* Instrucciones */}
        {!isListening && (
          <div className="mt-6 p-4 border-2 border-[var(--accent-color)] rounded bg-[var(--bg-color)]/50">
            <h3 className="font-bold mb-2 text-[var(--dark-text)]">💡 CÓMO USAR:</h3>
            <ol className="text-sm space-y-1 text-gray-700 list-decimal list-inside">
              <li>Selecciona tu afinación deseada</li>
              <li>Presiona "INICIAR AFINADOR" y permite el acceso al micrófono</li>
              <li>Toca cada cuerda y ajusta hasta que aparezca "¡AFINADO!"</li>
              <li>Las flechas te indican si debes aflojar (↓) o tensar (↑) la cuerda</li>
            </ol>
          </div>
        )}
      </div>
    </div>
  );
};



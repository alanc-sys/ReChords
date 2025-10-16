// src/components/ChordDiagram.tsx
import React, { useState, useEffect } from 'react';
import { getChordVariations, type ChordCatalog } from '../api/chordApi';

interface ChordDiagramProps {
  chordName: string;
  fingerPositions?: string;
  fullName?: string;
  onVariationChange?: (variation: ChordCatalog) => void;
}

/**
 * Componente que dibuja un diagrama de acorde de guitarra
 * 
 * fingerPositions format: "x32010"
 * - x = cuerda no se toca
 * - 0 = cuerda al aire
 * - 1-24 = traste donde se presiona
 * 
 * Las cuerdas van de izquierda (E grave, 6ta) a derecha (E aguda, 1ra)
 */
export const ChordDiagram: React.FC<ChordDiagramProps> = ({ 
  chordName, 
  fingerPositions,
  fullName,
  onVariationChange
}) => {
  const [variations, setVariations] = useState<ChordCatalog[]>([]);
  const [currentVariationIndex, setCurrentVariationIndex] = useState(0);
  
  const strings = 6;
  const frets = 5;
  const stringSpacing = 20;
  const fretSpacing = 25;
  const width = (strings - 1) * stringSpacing + 40;
  const height = frets * fretSpacing + 40;

  // Cargar variaciones del acorde
  useEffect(() => {
    getChordVariations(chordName)
      .then(vars => {
        if (vars && vars.length > 0) {
          setVariations(vars);
          setCurrentVariationIndex(0);
        }
      })
      .catch(err => console.error('Error loading chord variations:', err));
  }, [chordName]);

  // Usar la variación actual si existe, sino usar props
  const currentVariation = variations.length > 0 ? variations[currentVariationIndex] : null;
  const displayFingerPositions = currentVariation?.fingerPositions || fingerPositions;
  const displayFullName = currentVariation?.fullName || fullName;

  // Parse finger positions
  const positions = displayFingerPositions 
    ? displayFingerPositions.split('').map(pos => {
        if (pos === 'x' || pos === 'X') return 'x';
        if (pos === '0') return 0;
        if (pos === '-') return null; // Para trastes de dos dígitos como "8-10"
        return parseInt(pos, 10);
      }).filter(p => p !== null)
    : Array(6).fill('x');

  // Encontrar el traste mínimo y máximo para ajustar el diagrama
  const numericPositions = positions.filter(p => typeof p === 'number' && p > 0) as number[];
  const minFret = numericPositions.length > 0 ? Math.min(...numericPositions) : 1;
  const startFret = Math.max(1, minFret - 1);
  
  // Funciones para cambiar de variación
  const goToPreviousVariation = (e: React.MouseEvent) => {
    e.stopPropagation();
    e.preventDefault();
    if (currentVariationIndex > 0) {
      const newIndex = currentVariationIndex - 1;
      setCurrentVariationIndex(newIndex);
      if (onVariationChange && variations[newIndex]) {
        onVariationChange(variations[newIndex]);
      }
    }
  };

  const goToNextVariation = (e: React.MouseEvent) => {
    e.stopPropagation();
    e.preventDefault();
    if (currentVariationIndex < variations.length - 1) {
      const newIndex = currentVariationIndex + 1;
      setCurrentVariationIndex(newIndex);
      if (onVariationChange && variations[newIndex]) {
        onVariationChange(variations[newIndex]);
      }
    }
  };

  return (
    <div className="chord-diagram-container bg-white border-2 border-gray-800 rounded-lg p-4 shadow-xl">
      {/* Nombre del acorde con flechas de variación */}
      <div className="text-center mb-2">
        <div className="flex items-center justify-center gap-2">
          {/* Flecha Izquierda */}
          {variations.length > 1 && (
            <button
              onClick={goToPreviousVariation}
              disabled={currentVariationIndex === 0}
              className={`p-1 rounded ${
                currentVariationIndex === 0 
                  ? 'text-gray-300 cursor-not-allowed' 
                  : 'text-gray-700 hover:bg-gray-100 cursor-pointer'
              }`}
              title="Variación anterior"
            >
              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={2} stroke="currentColor" className="w-5 h-5">
                <path strokeLinecap="round" strokeLinejoin="round" d="M15.75 19.5L8.25 12l7.5-7.5" />
              </svg>
            </button>
          )}
          
          <div className="flex-1">
            <div className="font-bold text-lg text-gray-800">{chordName}</div>
            {displayFullName && <div className="text-xs text-gray-600">{displayFullName}</div>}
            {variations.length > 1 && (
              <div className="text-xs text-blue-600 font-semibold mt-1">
                Forma {currentVariationIndex + 1} de {variations.length}
              </div>
            )}
          </div>
          
          {/* Flecha Derecha */}
          {variations.length > 1 && (
            <button
              onClick={goToNextVariation}
              disabled={currentVariationIndex === variations.length - 1}
              className={`p-1 rounded ${
                currentVariationIndex === variations.length - 1
                  ? 'text-gray-300 cursor-not-allowed' 
                  : 'text-gray-700 hover:bg-gray-100 cursor-pointer'
              }`}
              title="Siguiente variación"
            >
              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={2} stroke="currentColor" className="w-5 h-5">
                <path strokeLinecap="round" strokeLinejoin="round" d="M8.25 4.5l7.5 7.5-7.5 7.5" />
              </svg>
            </button>
          )}
        </div>
      </div>

      {/* Diagrama SVG */}
      <svg width={width} height={height} viewBox={`0 0 ${width} ${height}`}>
        {/* Cuerdas (verticales) */}
        {Array.from({ length: strings }).map((_, stringIndex) => (
          <line
            key={`string-${stringIndex}`}
            x1={20 + stringIndex * stringSpacing}
            y1={20}
            x2={20 + stringIndex * stringSpacing}
            y2={20 + frets * fretSpacing}
            stroke={stringIndex === 0 ? "#333" : "#666"}
            strokeWidth={stringIndex === 0 ? 2 : 1}
          />
        ))}

        {/* Trastes (horizontales) */}
        {Array.from({ length: frets + 1 }).map((_, fretIndex) => (
          <line
            key={`fret-${fretIndex}`}
            x1={20}
            y1={20 + fretIndex * fretSpacing}
            x2={20 + (strings - 1) * stringSpacing}
            y2={20 + fretIndex * fretSpacing}
            stroke="#333"
            strokeWidth={fretIndex === 0 ? 3 : 1}
          />
        ))}

        {/* Número del traste inicial (si no empieza en 1) */}
        {startFret > 1 && (
          <text
            x={5}
            y={20 + fretSpacing / 2}
            fontSize="10"
            fill="#666"
            textAnchor="middle"
            dominantBaseline="middle"
          >
            {startFret}
          </text>
        )}

        {/* Posiciones de los dedos */}
        {positions.map((pos, stringIndex) => {
          if (pos === 'x') {
            // Cuerda no se toca (X)
            const x = 20 + stringIndex * stringSpacing;
            return (
              <g key={`pos-${stringIndex}`}>
                <line
                  x1={x - 4}
                  y1={8}
                  x2={x + 4}
                  y2={16}
                  stroke="red"
                  strokeWidth={2}
                />
                <line
                  x1={x + 4}
                  y1={8}
                  x2={x - 4}
                  y2={16}
                  stroke="red"
                  strokeWidth={2}
                />
              </g>
            );
          } else if (pos === 0) {
            // Cuerda al aire (O)
            const x = 20 + stringIndex * stringSpacing;
            return (
              <circle
                key={`pos-${stringIndex}`}
                cx={x}
                cy={12}
                r={5}
                fill="none"
                stroke="green"
                strokeWidth={2}
              />
            );
          } else if (typeof pos === 'number') {
            // Dedo en traste
            const fretPosition = pos - startFret + 1;
            if (fretPosition >= 0 && fretPosition <= frets) {
              const x = 20 + stringIndex * stringSpacing;
              const y = 20 + (fretPosition - 0.5) * fretSpacing;
              return (
                <circle
                  key={`pos-${stringIndex}`}
                  cx={x}
                  cy={y}
                  r={7}
                  fill="#333"
                  stroke="#000"
                  strokeWidth={1}
                />
              );
            }
          }
          return null;
        })}
      </svg>

      {/* Nombres de las cuerdas */}
      <div className="flex justify-between text-xs text-gray-500 mt-1" style={{ paddingLeft: '15px', paddingRight: '15px' }}>
        <span>E</span>
        <span>A</span>
        <span>D</span>
        <span>G</span>
        <span>B</span>
        <span>E</span>
      </div>

      {/* Leyenda */}
      <div className="text-xs text-gray-600 mt-2 text-center">
        <div className="flex justify-center items-center gap-3">
          <span className="flex items-center gap-1">
            <span className="inline-block w-3 h-3 rounded-full bg-black"></span>
            Dedo
          </span>
          <span className="flex items-center gap-1">
            <span className="inline-block w-3 h-3 rounded-full border-2 border-green-600"></span>
            Aire
          </span>
          <span className="flex items-center gap-1">
            <span className="text-red-600 font-bold">✕</span>
            No tocar
          </span>
        </div>
      </div>
    </div>
  );
};

export default ChordDiagram;


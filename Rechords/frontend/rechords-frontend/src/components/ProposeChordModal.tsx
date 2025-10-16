// src/components/ProposeChordModal.tsx
import React, { useState } from 'react';

export interface ProposedChordData {
  name: string;
  fullName: string;
  category: string;
  fingerPositions?: string;
  notes?: string;
}

interface ProposeChordModalProps {
  chordName: string;
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (chordData: ProposedChordData) => void;
}

const CHORD_CATEGORIES = [
  { value: 'MAJOR', label: 'Mayor' },
  { value: 'MINOR', label: 'Menor' },
  { value: 'SEVENTH', label: 'Séptima' },
  { value: 'MAJOR_SEVENTH', label: 'Mayor Séptima' },
  { value: 'MINOR_SEVENTH', label: 'Menor Séptima' },
  { value: 'SUSPENDED', label: 'Suspendido' },
  { value: 'ADD', label: 'Add' },
  { value: 'DIMINISHED', label: 'Disminuido' },
  { value: 'AUGMENTED', label: 'Aumentado' },
  { value: 'OTHER', label: 'Otro' },
];

export const ProposeChordModal: React.FC<ProposeChordModalProps> = ({
  chordName,
  isOpen,
  onClose,
  onSubmit,
}) => {
  const [fullName, setFullName] = useState('');
  const [category, setCategory] = useState('OTHER');
  const [fingerPositions, setFingerPositions] = useState('');
  const [notes, setNotes] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    onSubmit({
      name: chordName,
      fullName: fullName || chordName,
      category,
      fingerPositions: fingerPositions || undefined,
      notes: notes || undefined,
    });
    
    // Reset form
    setFullName('');
    setCategory('OTHER');
    setFingerPositions('');
    setNotes('');
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-[var(--bg-color)] border-4 border-[var(--dark-text)] rounded-lg p-6 max-w-md w-full shadow-xl">
        <div className="flex justify-between items-center mb-4">
          <h2 className="playfair-font text-2xl font-bold text-[var(--dark-text)]">
            Proponer Nuevo Acorde
          </h2>
          <button
            onClick={onClose}
            className="text-gray-500 hover:text-gray-700 text-2xl"
          >
            ×
          </button>
        </div>

        <div className="mb-4 p-3 bg-yellow-50 border-2 border-yellow-400 rounded">
          <p className="text-sm text-yellow-800">
            <strong>ℹ️ Acorde detectado:</strong> <code className="font-mono bg-yellow-100 px-2 py-1 rounded">{chordName}</code>
          </p>
          <p className="text-xs text-yellow-700 mt-1">
            Este acorde no está en nuestro catálogo. Ayúdanos a agregarlo proporcionando la siguiente información:
          </p>
        </div>

        <form onSubmit={handleSubmit}>
          {/* Nombre del Acorde (readonly) */}
          <div className="mb-4">
            <label className="block text-sm font-bold text-[var(--dark-text)] mb-2">
              Nombre del Acorde
            </label>
            <input
              type="text"
              value={chordName}
              disabled
              className="w-full px-3 py-2 border-2 border-gray-300 rounded bg-gray-100 text-gray-600 font-mono"
            />
          </div>

          {/* Nombre Completo */}
          <div className="mb-4">
            <label className="block text-sm font-bold text-[var(--dark-text)] mb-2">
              Nombre Completo
              <span className="text-gray-500 font-normal ml-1">(Opcional)</span>
            </label>
            <input
              type="text"
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              placeholder="Ej: Do mayor novena"
              className="w-full px-3 py-2 border-2 border-[var(--dark-text)] rounded focus:outline-none focus:border-[var(--accent-color)]"
            />
          </div>

          {/* Categoría */}
          <div className="mb-4">
            <label className="block text-sm font-bold text-[var(--dark-text)] mb-2">
              Categoría
              <span className="text-red-500 ml-1">*</span>
            </label>
            <select
              value={category}
              onChange={(e) => setCategory(e.target.value)}
              required
              className="w-full px-3 py-2 border-2 border-[var(--dark-text)] rounded focus:outline-none focus:border-[var(--accent-color)]"
            >
              {CHORD_CATEGORIES.map((cat) => (
                <option key={cat.value} value={cat.value}>
                  {cat.label}
                </option>
              ))}
            </select>
          </div>

          {/* Posiciones de Dedos */}
          <div className="mb-4">
            <label className="block text-sm font-bold text-[var(--dark-text)] mb-2">
              Posiciones de Dedos
              <span className="text-gray-500 font-normal ml-1">(Opcional)</span>
            </label>
            <input
              type="text"
              value={fingerPositions}
              onChange={(e) => setFingerPositions(e.target.value)}
              placeholder="Ej: x32010"
              maxLength={8}
              className="w-full px-3 py-2 border-2 border-[var(--dark-text)] rounded focus:outline-none focus:border-[var(--accent-color)] font-mono"
            />
            <p className="text-xs text-gray-600 mt-1">
              Formato: 6 caracteres (cuerdas 6-1). Usa 'x' para no tocar, '0' al aire, 1-24 para trastes.
            </p>
          </div>

          {/* Notas Adicionales */}
          <div className="mb-6">
            <label className="block text-sm font-bold text-[var(--dark-text)] mb-2">
              Notas Adicionales
              <span className="text-gray-500 font-normal ml-1">(Opcional)</span>
            </label>
            <textarea
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="Ej: Acorde difícil para principiantes, requiere cejilla..."
              rows={3}
              className="w-full px-3 py-2 border-2 border-[var(--dark-text)] rounded focus:outline-none focus:border-[var(--accent-color)] resize-none"
            />
          </div>

          {/* Botones */}
          <div className="flex gap-3">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 px-4 py-2 border-2 border-gray-400 rounded font-bold text-gray-700 hover:bg-gray-100"
            >
              Cancelar
            </button>
            <button
              type="submit"
              className="flex-1 btn-vintage px-4 py-2 rounded font-bold"
            >
              Proponer Acorde
            </button>
          </div>
        </form>

        <div className="mt-4 p-3 bg-blue-50 border-2 border-blue-300 rounded">
          <p className="text-xs text-blue-800">
            <strong>📝 Nota:</strong> Tu propuesta será revisada cuando la canción sea aprobada por un administrador.
            Si es aprobada, el acorde estará disponible para todos los usuarios.
          </p>
        </div>
      </div>
    </div>
  );
};

export default ProposeChordModal;


export interface SongWithChordsResponse {
  id: number;
  title: string;
  artist: string;
  album?: string;
  year?: number;
  key?: string;
  tempo?: number;
  
  // Enlaces multimedia
  youtubeUrl?: string;
  spotifyUrl?: string;
  youtubeVideoId?: string;  // ID extraído para embed
  spotifyTrackId?: string;  // ID extraído para embed
  
  // Personalización de portada
  coverImageUrl?: string;
  coverColor?: string;
  
  status: 'DRAFT' | 'PENDING' | 'APPROVED' | 'REJECTED';
  isPublic: boolean;
  rejectionReason?: string;
  createdAt: string;
  publishedAt?: string;
  createdBy: {
    id: number;
    username: string;
    firstname: string;
  };
  lyrics: Array<{
    lineNumber: number;
    text: string;
    chords: Array<{
      start: number;
      name: string;
      chordId?: number;
    }>;
  }>;
}

export interface ChordPositionInfo {
  start: number;        // Posición inicial en la línea (0-based)
  name: string;         // Nombre del acorde (C, Am, F, etc.)
  chordId?: number;     // ID del acorde en el catálogo (opcional)
}

export interface LineWithChords {
  lineNumber: number;   // Número de línea (0-based)
  text: string;         // Texto de la línea
  chords: ChordPositionInfo[]; // Lista de acordes en esta línea
}

export interface ProposedChordRequest {
  name: string;              // Nombre del acorde (ej: "Cmaj9")
  fullName: string;          // Nombre completo (ej: "Do mayor novena")
  category: string;          // Categoría (MAJOR, MINOR, SEVENTH, etc.)
  fingerPositions?: string;  // Posiciones de dedos (ej: "x32010") - OPCIONAL
  notes?: string;            // Notas adicionales - OPCIONAL
}

export interface CreateSongRequest {
  title: string;
  artist: string;
  album?: string;
  year?: number;
  key?: string;          // Tonalidad de la canción (C, Am, etc.)
  tempo?: number;        // BPM de la canción
  
  // Enlaces multimedia
  youtubeUrl?: string;
  spotifyUrl?: string;
  
  // Personalización de portada
  coverImageUrl?: string;
  coverColor?: string;
  
  lyrics: LineWithChords[]; // Líneas con texto y acordes
  proposedChords?: ProposedChordRequest[]; // Acordes nuevos propuestos - OPCIONAL
}

export interface ChordInfo {
  id: number;
  name: string;
  fullName: string;
  fingerPositions?: string;
  difficulty: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
  isCommon: boolean;
  displayOrder: number;
}


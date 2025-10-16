import apiClient from './authApi';
import type { CreateSongRequest, SongWithChordsResponse, ChordInfo } from '../types/song';
import { API_CONFIG } from '../config/api';

// Tipo para respuestas paginadas
export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

// Crear canción (como borrador por defecto)
export const createSong = async (songData: CreateSongRequest): Promise<SongWithChordsResponse> => {
  const response = await apiClient.post<SongWithChordsResponse>('/api/songs', songData);
  return response.data;
};

// Actualizar canción existente
export const updateSong = async (id: number, songData: CreateSongRequest): Promise<SongWithChordsResponse> => {
  const response = await apiClient.put<SongWithChordsResponse>(`/api/songs/${id}`, songData);
  return response.data;
};

// Enviar canción para revisión (cambia status de DRAFT a PENDING)
export const submitSongForApproval = async (id: number): Promise<SongWithChordsResponse> => {
  const response = await apiClient.put<SongWithChordsResponse>(`/api/songs/${id}/submit`);
  return response.data;
};

// Obtener todos los acordes disponibles (endpoint público)
export const getAvailableChords = async (): Promise<ChordInfo[]> => {
  // Usar fetch directamente para endpoints públicos sin token
  const response = await fetch(`${API_CONFIG.BASE_URL}/api/songs/available-chords`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
    credentials: 'include',
  });
  
  if (!response.ok) {
    throw new Error('Error cargando todos los acordes');
  }
  return response.json();
};

// Obtener acordes comunes (endpoint público)
export const getCommonChords = async (): Promise<ChordInfo[]> => {
  // Usar fetch directamente para endpoints públicos sin token
  const response = await fetch(`${API_CONFIG.BASE_URL}/api/songs/common-chords`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
    credentials: 'include', // Para CORS con credenciales
  });
  
  if (!response.ok) {
    throw new Error('Error cargando acordes comunes');
  }
  return response.json();
};

// Obtener canción por ID
export const getSongById = async (id: number): Promise<SongWithChordsResponse> => {
  const response = await apiClient.get<SongWithChordsResponse>(`/api/songs/${id}`);
  return response.data;
};

// Eliminar canción
export const deleteSong = async (id: number): Promise<void> => {
  await apiClient.delete(`/api/songs/${id}`);
};

// Importar canción desde texto
export const importSong = async (rawText: string): Promise<CreateSongRequest> => {
  const response = await apiClient.post<CreateSongRequest>('/api/songs/import', rawText, {
    headers: {
      'Content-Type': 'text/plain',
    },
  });
  return response.data;
};

// Obtener canciones públicas paginadas
export const getPublicSongs = async (
  page: number = 0,
  size: number = 20,
  sort: string = 'publishedAt,desc'
): Promise<PageResponse<SongWithChordsResponse>> => {
  const response = await apiClient.get<PageResponse<SongWithChordsResponse>>('/api/songs/public', {
    params: { page, size, sort },
  });
  return response.data;
};

// Buscar canciones públicas (servidor)
export const searchPublicSongs = async (
  q: string,
  page: number = 0,
  size: number = 20,
  sort: string = 'title,asc'
): Promise<PageResponse<SongWithChordsResponse>> => {
  const response = await apiClient.get<PageResponse<SongWithChordsResponse>>('/api/songs/search', {
    params: { q, page, size, sort },
  });
  return response.data;
};

// Analytics de una canción
export interface SongAnalyticsResponse {
  songId: number;
  totalChords: number;
  uniqueChords: number;
  difficulty: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | string;
  averageChordDensity: number;
  mostUsedChords: Array<{ chordName: string; count: number }>;
}

export const getSongAnalytics = async (id: number): Promise<SongAnalyticsResponse> => {
  const response = await apiClient.get<SongAnalyticsResponse>(`/api/songs/${id}/analytics`);
  return response.data;
};

// Portadas: subir/eliminar
export const uploadSongCover = async (id: number, file: File): Promise<string> => {
  const formData = new FormData();
  formData.append('file', file);
  const response = await apiClient.post(`/api/songs/${id}/cover`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return response.data;
};

export const deleteSongCover = async (id: number): Promise<void> => {
  await apiClient.delete(`/api/songs/${id}/cover`);
};

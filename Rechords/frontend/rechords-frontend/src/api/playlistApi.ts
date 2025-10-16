// src/api/playlistApi.ts
import apiClient from './authApi';
import type { SongWithChordsResponse } from '../types/song';

const API_URL = '/api';

// ==================== TIPOS ====================
export interface PlaylistSummaryResponse {
  id: number;
  name: string;
  description?: string;
  isPublic: boolean;
  songCount: number;
  createdAt: string;
}

export interface PlaylistResponse {
  id: number;
  name: string;
  description?: string;
  isPublic: boolean;
  createdAt: string;
  songs: SongWithChordsResponse[];  // El backend devuelve objetos SongWithChordsResponse completos
}

export interface CreatePlaylistRequest {
  name: string;
  description?: string;
  isPublic?: boolean;
}

export interface UpdatePlaylistRequest {
  name?: string;
  description?: string;
  isPublic?: boolean;
}

export interface AddSongToPlaylistRequest {
  songId: number;
}

// ==================== OBTENER PLAYLISTS ====================
export const getMyPlaylists = async (): Promise<PlaylistSummaryResponse[]> => {
  const response = await apiClient.get(`${API_URL}/playlists/my`);
  return response.data;
};

export const getPublicPlaylists = async (): Promise<PlaylistSummaryResponse[]> => {
  const response = await apiClient.get(`${API_URL}/playlists/public`);
  return response.data;
};

export const getPlaylistById = async (id: number): Promise<PlaylistResponse> => {
  const response = await apiClient.get(`${API_URL}/playlists/${id}`);
  return response.data;
};

export const searchPublicPlaylists = async (query: string): Promise<PlaylistSummaryResponse[]> => {
  const response = await apiClient.get(`${API_URL}/playlists/search`, {
    params: { q: query },
  });
  return response.data;
};

// ==================== CREAR Y EDITAR ====================
export const createPlaylist = async (request: CreatePlaylistRequest): Promise<PlaylistResponse> => {
  const response = await apiClient.post(`${API_URL}/playlists`, request);
  return response.data;
};

export const updatePlaylist = async (
  id: number,
  request: UpdatePlaylistRequest
): Promise<PlaylistResponse> => {
  const response = await apiClient.put(`${API_URL}/playlists/${id}`, request);
  return response.data;
};

export const deletePlaylist = async (id: number): Promise<void> => {
  await apiClient.delete(`${API_URL}/playlists/${id}`);
};

// ==================== GESTIONAR CANCIONES ====================
export const addSongToPlaylist = async (
  playlistId: number,
  request: AddSongToPlaylistRequest
): Promise<PlaylistResponse> => {
  const response = await apiClient.post(`${API_URL}/playlists/${playlistId}/songs`, request);
  return response.data;
};

export const removeSongFromPlaylist = async (
  playlistId: number,
  songId: number
): Promise<PlaylistResponse> => {
  const response = await apiClient.delete(`${API_URL}/playlists/${playlistId}/songs/${songId}`);
  return response.data;
};


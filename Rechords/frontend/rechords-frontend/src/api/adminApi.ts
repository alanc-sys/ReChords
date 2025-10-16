// src/api/adminApi.ts
import apiClient from './authApi'; // Usar el mismo cliente configurado
import type { SongWithChordsResponse } from '../types/song';

const API_URL = '/api'; // Relativo, ya que apiClient tiene la baseURL

// Tipos de respuesta
export interface AdminStatsResponse {
  totalSongs: number;
  draftSongs: number;
  pendingSongs: number;
  approvedSongs: number;
  rejectedSongs: number;
  totalUsers: number;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface RejectSongRequest {
  reason: string;
}

// ==================== ADMIN STATS ====================
export const getAdminStats = async (): Promise<AdminStatsResponse> => {
  const response = await apiClient.get(`${API_URL}/admin/stats`);
  return response.data;
};

// ==================== OBTENER CANCIONES ====================
export const getPendingSongs = async (
  page: number = 0,
  size: number = 10,
  sort: string = 'createdAt,desc'
): Promise<PageResponse<SongWithChordsResponse>> => {
  const response = await apiClient.get(`${API_URL}/admin/songs/pending`, {
    params: { page, size, sort },
  });
  return response.data;
};

export const getAllSongsAdmin = async (
  page: number = 0,
  size: number = 10,
  sort: string = 'createdAt,desc'
): Promise<PageResponse<SongWithChordsResponse>> => {
  const response = await apiClient.get(`${API_URL}/admin/songs`, {
    params: { page, size, sort },
  });
  return response.data;
};

// ==================== ACCIONES DE MODERACIÓN ====================
export const approveSong = async (id: number): Promise<SongWithChordsResponse> => {
  const response = await apiClient.put(`${API_URL}/admin/songs/${id}/approve`, {});
  return response.data;
};

export const rejectSong = async (
  id: number,
  reason: string
): Promise<SongWithChordsResponse> => {
  const response = await apiClient.put(`${API_URL}/admin/songs/${id}/reject`, { reason });
  return response.data;
};

export const unpublishSong = async (id: number): Promise<SongWithChordsResponse> => {
  const response = await apiClient.put(`${API_URL}/admin/songs/${id}/unpublish`, {});
  return response.data;
};

export const deleteSongAdmin = async (id: number): Promise<void> => {
  await apiClient.delete(`${API_URL}/admin/songs/${id}`);
};

// ==================== ANALYTICS ====================
export const processAllAnalytics = async (): Promise<string> => {
  const response = await apiClient.post(`${API_URL}/admin/analytics/process-all`, {});
  return response.data;
};


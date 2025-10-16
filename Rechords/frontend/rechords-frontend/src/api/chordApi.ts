// src/api/chordApi.ts
import axios from 'axios';

const API_URL = 'http://localhost:8080/api/chords';

export interface ChordCatalog {
  id: number;
  name: string;
  fullName?: string;
  category: string;
  displayOrder?: number;
  isCommon?: boolean;
  difficultyLevel?: string;
  fingerPositions?: string;
  notes?: string;
}

/**
 * Obtener todos los acordes del catálogo
 */
export const getAllChords = async (): Promise<ChordCatalog[]> => {
  const response = await axios.get(API_URL);
  return response.data;
};

/**
 * Obtener un acorde específico por nombre
 */
export const getChordByName = async (name: string): Promise<ChordCatalog | null> => {
  try {
    const response = await axios.get(`${API_URL}/${name}`);
    return response.data;
  } catch (error) {
    console.error(`Error fetching chord ${name}:`, error);
    return null;
  }
};

/**
 * Obtener solo los acordes más comunes
 */
export const getCommonChords = async (): Promise<ChordCatalog[]> => {
  const response = await axios.get(`${API_URL}/common`);
  return response.data;
};

/**
 * Obtener todas las variaciones de un acorde específico
 * Por ejemplo: para "C" devuelve [C principal, C_var1, C_var2]
 */
export const getChordVariations = async (name: string): Promise<ChordCatalog[]> => {
  try {
    const response = await axios.get(`${API_URL}/${name}/variations`);
    return response.data;
  } catch (error) {
    console.error(`Error fetching variations for chord ${name}:`, error);
    return [];
  }
};


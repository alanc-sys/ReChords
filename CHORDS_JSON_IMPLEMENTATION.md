# Implementación de JSON con Acordes y Letra

## Resumen de Cambios

Se ha implementado un sistema completo para manejar canciones con acordes usando un formato JSON que combina letra y posiciones de acordes, optimizando el almacenamiento y permitiendo renderizado directo desde el Frontend.

## Cambios Realizados

### 1. Entidad Song
- **Archivo**: `src/main/java/com/misacordes/application/entities/Song.java`
- **Cambio**: Añadido campo `chordsMap` (TEXT) para almacenar el JSON completo con letra y acordes

### 2. DTOs Nuevos
- **ChordInfo.java**: Representa un acorde individual con posición y nombre
- **LineWithChords.java**: Representa una línea de letra con sus acordes
- **SongWithChordsRequest.java**: Request para crear/actualizar canciones con el nuevo formato
- **SongWithChordsResponse.java**: Response con el formato JSON completo
- **SongAnalyticsResponse.java**: Response con estadísticas analíticas de acordes

### 3. Servicios
- **SongAnalyticsService.java**: Procesa el JSON y extrae información analítica
  - Análisis de frecuencia de acordes
  - Estadísticas por línea
  - Validación de formato JSON
  - Extracción de acordes únicos

- **SongAnalyticsAsyncService.java**: Procesamiento asíncrono de analítica
  - Validación de acordes contra catálogo
  - Procesamiento masivo
  - Logging detallado

### 4. SongService Actualizado
- **Métodos nuevos**:
  - `createSongWithChords()`: Crear canción con formato JSON
  - `getSongWithChordsById()`: Obtener canción con formato JSON
  - `updateSongWithChords()`: Actualizar canción con formato JSON
  - `getSongAnalytics()`: Obtener estadísticas de acordes
- **Procesamiento asíncrono**: Se activa automáticamente al guardar/actualizar

### 5. Controladores
- **songController.java**: Nuevos endpoints
  - `POST /api/songs/with-chords`: Crear canción con JSON
  - `GET /api/songs/{id}/with-chords`: Obtener canción con JSON
  - `PUT /api/songs/{id}/with-chords`: Actualizar canción con JSON
  - `GET /api/songs/{id}/analytics`: Obtener analítica

- **adminController.java**: Endpoint administrativo
  - `POST /api/admin/analytics/process-all`: Procesamiento masivo

### 6. Configuración Asíncrona
- **AsyncConfig.java**: Configuración de ThreadPool para procesamiento asíncrono

## Formato JSON

```json
{
  "title": "Amazing Grace",
  "artist": "John Newton",
  "album": "Hymns Collection",
  "year": 1779,
  "key": "C",
  "tempo": 80,
  "lyrics": [
    {
      "lineNumber": 0,
      "text": "Amazing grace how sweet the sound",
      "chords": [
        { "start": 0, "name": "C", "chordId": 1 },
        { "start": 7, "name": "G", "chordId": 2 }
      ]
    }
  ]
}
```

## Beneficios

### Para el Frontend
- **Renderizado directo**: El JSON contiene toda la información necesaria
- **Sin consultas adicionales**: Una sola llamada API obtiene letra + acordes
- **Estructura consistente**: Formato predecible para el renderizado

### Para el Backend
- **Almacenamiento eficiente**: Un solo campo TEXT vs múltiples filas
- **Analítica rica**: Estadísticas detalladas de acordes
- **Procesamiento asíncrono**: No bloquea operaciones de usuario
- **Validación**: Verificación de formato y consistencia

### Para la Base de Datos
- **Menos registros**: 1 fila por canción vs N filas por acorde
- **Consultas más rápidas**: Sin JOINs complejos para renderizado
- **Escalabilidad**: Mejor rendimiento con muchas canciones

## Endpoints Disponibles

### Usuario
- `POST /api/songs/with-chords` - Crear canción con JSON
- `GET /api/songs/{id}/with-chords` - Obtener canción con JSON
- `PUT /api/songs/{id}/with-chords` - Actualizar canción con JSON
- `GET /api/songs/{id}/analytics` - Obtener estadísticas

### Administrador
- `POST /api/admin/analytics/process-all` - Procesamiento masivo

## Ejemplo de Uso

### Crear Canción
```bash
curl -X POST /api/songs/with-chords \
  -H "Content-Type: application/json" \
  -d @example_song_with_chords.json
```

### Obtener Analítica
```bash
curl -X GET /api/songs/1/analytics
```

## Próximos Pasos

1. **Frontend**: Implementar componentes para renderizar el JSON
2. **Validación**: Expandir validación de acordes contra catálogo
3. **Cache**: Implementar cache para analítica frecuente
4. **Migración**: Script para migrar canciones existentes al nuevo formato
5. **Índices**: Añadir índices GIN para búsquedas en JSON (PostgreSQL)

## Compatibilidad

- Los endpoints existentes siguen funcionando
- El campo `chordsMap` es opcional
- Se mantiene compatibilidad con el sistema anterior
- Migración gradual posible

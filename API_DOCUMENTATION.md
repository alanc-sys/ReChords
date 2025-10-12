# 🎵 ReChords API Documentation

## 📋 Tabla de Contenidos

1. [Introducción](#introducción)
2. [Autenticación](#autenticación)
3. [Endpoints de Usuario](#endpoints-de-usuario)
4. [Endpoints de Canciones](#endpoints-de-canciones)
5. [Endpoints de Acordes](#endpoints-de-acordes)
6. [Endpoints de Playlists](#endpoints-de-playlists)
7. [Endpoints de Administración](#endpoints-de-administración)
8. [Modelos de Datos](#modelos-de-datos)
9. [Códigos de Error](#códigos-de-error)

---

## 🎯 Introducción

ReChords es una API REST para gestionar una biblioteca personal de música con funcionalidades avanzadas de acordes. Permite a los usuarios crear, editar y compartir canciones con posiciones precisas de acordes.

### Características Principales:
- ✅ **Autenticación JWT** con roles (USER/ADMIN)
- ✅ **Gestión de canciones** con workflow de aprobación
- ✅ **Sistema de acordes** con posiciones precisas en JSON
- ✅ **Catálogo de acordes** predefinido (36 acordes)
- ✅ **Sistema de playlists/bibliotecas** personalizadas
- ✅ **Playlists por defecto** (Favoritas, Mis Creaciones)
- ✅ **Búsqueda y filtrado** avanzado
- ✅ **Paginación** en todos los endpoints de listado
- ✅ **Estadísticas de administrador**

### Base URL:
```
http://localhost:8080/api
```

---

## 🔐 Autenticación

Todos los endpoints (excepto login y registro) requieren autenticación JWT.

### Headers Requeridos:
```http
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

### Endpoints de Autenticación:

#### 🔑 Login
```http
POST /auth/login
```

**Request Body:**
```json
{
  "username": "usuario123",
  "password": "password123"
}
```

**Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### 📝 Registro
```http
POST /auth/register
```

**Request Body:**
```json
{
  "username": "nuevousuario",
  "password": "password123",
  "firstname": "Juan",
  "lastname": "Pérez",
  "country": "España"
}
```

**Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

## 👤 Endpoints de Usuario

### 🎵 Obtener Mis Canciones (Paginado)
```http
GET /songs/my?page=0&size=20&sort=createdAt,desc
```

**Parámetros de Query:**
- `page` (opcional): Número de página (default: 0)
- `size` (opcional): Tamaño de página (default: 20, máximo: 20)
- `sort` (opcional): Campo y dirección de ordenamiento (default: "createdAt,desc")

**Response (200):**
```json
{
  "content": [
    {
      "id": 1,
      "title": "Mi Canción",
      "artist": "Mi Artista",
      "album": "Mi Álbum",
      "year": 2024,
      "key": "C",
      "tempo": 120,
      "status": "DRAFT",
      "isPublic": false,
      "rejectionReason": null,
      "createdAt": "2024-01-15T10:30:00",
      "publishedAt": null,
      "createdBy": {
        "id": 1,
        "username": "usuario123",
        "firstname": "Juan"
      },
      "lyrics": [
        {
          "lineNumber": 0,
          "text": "Letra de la canción...",
          "chords": [
            {
              "start": 0,
              "name": "C",
              "chordId": null
            }
          ]
        }
      ]
    }
  ],
  "totalElements": 25,
  "totalPages": 2,
  "size": 20,
  "number": 0,
  "first": true,
  "last": false,
  "numberOfElements": 20
}
```

### 🎵 Obtener Canciones Públicas (Paginado)
```http
GET /songs/public?page=0&size=20&sort=publishedAt,desc
```

**Parámetros de Query:**
- `page` (opcional): Número de página (default: 0)
- `size` (opcional): Tamaño de página (default: 20, máximo: 20)
- `sort` (opcional): Campo y dirección de ordenamiento (default: "publishedAt,desc")

**Response (200):** Página de canciones públicas aprobadas con metadatos de paginación

### 🔍 Buscar Canciones (Paginado)
```http
GET /songs/search?q=busqueda&page=0&size=20&sort=title,asc
```

**Parámetros de Query:**
- `q` (requerido): Término de búsqueda
- `page` (opcional): Número de página (default: 0)
- `size` (opcional): Tamaño de página (default: 20, máximo: 20)
- `sort` (opcional): Campo y dirección de ordenamiento (default: "title,asc")

**Response (200):** Página de canciones que coinciden con la búsqueda

---

## 🎼 Endpoints de Canciones

### ➕ Crear Canción
```http
POST /songs
```

**Request Body:**
```json
{
  "title": "Nueva Canción",
  "artist": "Artista",
  "album": "Álbum",
  "year": 2024,
  "lyricsData": "Letra de la canción\nSegunda línea",
  "chords": [
    {
      "chordName": "C",
      "startPos": 0,
      "endPos": 1,
      "lineNumber": 0
    },
    {
      "chordName": "Am",
      "startPos": 10,
      "endPos": 12,
      "lineNumber": 0
    }
  ]
}
```

**Response (201):**
```json
{
  "id": 1,
  "title": "Nueva Canción",
  "artist": "Artista",
  "album": "Álbum",
  "year": 2024,
  "lyrics": "Letra de la canción\nSegunda línea",
  "status": "DRAFT",
  "isPublic": false,
  "rejectionReason": null,
  "createdAt": "2024-01-15T10:30:00",
  "publishedAt": null,
  "createdBy": {
    "id": 1,
    "username": "usuario123",
    "firstname": "Juan"
  },
  "chordPositions": [
    {
      "chordName": "C",
      "startPos": 0,
      "endPos": 1,
      "lineNumber": 0
    }
  ]
}
```

### 📥 Importar Canción
```http
POST /songs/import
```

**Request Body:** Texto plano con formato de canción
```
Bohemian Rhapsody - Queen

[Verse 1]
C        Am        F         G
Is this the real life? Is this just fantasy?
C        Am        F         G
Caught in a landslide, no escape from reality
```

**Response (200):**
```json
{
  "title": "Bohemian Rhapsody",
  "artist": "Queen",
  "album": null,
  "year": null,
  "lyrics": [
    {
      "lineNumber": 0,
      "text": "[Verse 1]",
      "chords": []
    },
    {
      "lineNumber": 1,
      "text": "Is this the real life? Is this just fantasy?",
      "chords": [
        {
          "position": 0,
          "chordName": "C",
          "chordId": 1
        },
        {
          "position": 8,
          "chordName": "Am",
          "chordId": 2
        },
        {
          "position": 16,
          "chordName": "F",
          "chordId": 3
        },
        {
          "position": 24,
          "chordName": "G",
          "chordId": 4
        }
      ]
    }
  ]
}
```

**Características del Importador:**
- ✅ **Detección automática** de título y artista
- ✅ **Reconocimiento de acordes** con regex avanzado
- ✅ **Emparejamiento** de líneas de acordes con letras
- ✅ **Filtrado** de tablaturas y metadatos
- ✅ **Validación** contra catálogo de acordes

### ✏️ Actualizar Canción
```http
PUT /songs/{id}
```

**Request Body:** Mismo formato que crear canción

**Response (200):** Canción actualizada

### 👁️ Obtener Canción por ID
```http
GET /songs/{id}
```

**Response (200):** Detalles de la canción

### 🗑️ Eliminar Canción
```http
DELETE /songs/{id}
```

**Response (204):** Sin contenido

### 📤 Enviar para Aprobación
```http
PUT /songs/{id}/submit
```

**Response (200):** Canción con status PENDING

---

## 🎸 Endpoints de Acordes

### 🎯 Obtener Acordes Disponibles
```http
GET /songs/available-chords
```

**Response (200):**
```json
[
  {
    "id": 1,
    "name": "C",
    "fullName": "Do mayor",
    "fingerPositions": "x32010",
    "difficulty": "BEGINNER",
    "isCommon": true,
    "displayOrder": 1
  },
  {
    "id": 2,
    "name": "Am",
    "fullName": "La menor",
    "fingerPositions": "x02210",
    "difficulty": "BEGINNER",
    "isCommon": true,
    "displayOrder": 2
  }
]
```

### ⭐ Obtener Acordes Comunes
```http
GET /songs/common-chords
```

**Response (200):** Solo acordes marcados como comunes

### 🎵 Actualizar Posiciones de Acordes
```http
PUT /songs/{id}/chords
```

**Request Body:**
```json
[
  {
    "chordName": "C",
    "startPos": 0,
    "endPos": 1,
    "lineNumber": 0
  },
  {
    "chordName": "F",
    "startPos": 5,
    "endPos": 6,
    "lineNumber": 1
  }
]
```

**Response (200):** Canción con acordes actualizados

---

## 📚 Endpoints de Playlists

El sistema de playlists permite a los usuarios organizar sus canciones en bibliotecas personalizadas. Cada usuario tiene automáticamente dos playlists por defecto: "Favoritas" y "Mis Creaciones".

### 🎵 Crear Playlist
```http
POST /playlists
```

**Request Body:**
```json
{
  "name": "Rock Clásico",
  "description": "Mis canciones de rock favoritas",
  "isPublic": false
}
```

**Response (201):**
```json
{
  "id": 1,
  "name": "Rock Clásico",
  "description": "Mis canciones de rock favoritas",
  "isPublic": false,
  "isDefault": false,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00",
  "songs": []
}
```

### 📋 Obtener Mis Playlists
```http
GET /playlists/my
```

**Response (200):**
```json
[
  {
    "id": 1,
    "name": "Favoritas",
    "description": "Mis canciones favoritas",
    "isPublic": false,
    "isDefault": true,
    "songCount": 5,
    "createdAt": "2024-01-15T10:30:00"
  },
  {
    "id": 2,
    "name": "Rock Clásico",
    "description": "Mis canciones de rock favoritas",
    "isPublic": false,
    "isDefault": false,
    "songCount": 3,
    "createdAt": "2024-01-15T10:30:00"
  }
]
```

### 🎯 Obtener Playlist por ID
```http
GET /playlists/{id}
```

**Response (200):**
```json
{
  "id": 1,
  "name": "Favoritas",
  "description": "Mis canciones favoritas",
  "isPublic": false,
  "isDefault": true,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00",
  "songs": [
    {
      "id": 1,
      "title": "Bohemian Rhapsody",
      "artist": "Queen",
      "genre": "Rock",
      "isPublic": true,
      "addedAt": "2024-01-15T10:35:00"
    }
  ]
}
```

### ✏️ Actualizar Playlist
```http
PUT /playlists/{id}
```

**Request Body:**
```json
{
  "name": "Rock Clásico Actualizado",
  "description": "Descripción actualizada",
  "isPublic": true
}
```

**Response (200):** Playlist actualizada

### 🗑️ Eliminar Playlist
```http
DELETE /playlists/{id}
```

**Response (204):** Playlist eliminada

### ➕ Añadir Canción a Playlist
```http
POST /playlists/{id}/songs
```

**Request Body:**
```json
{
  "songId": 1
}
```

**Response (200):** Playlist con la canción añadida

### ➖ Eliminar Canción de Playlist
```http
DELETE /playlists/{id}/songs/{songId}
```

**Response (200):** Playlist sin la canción eliminada

### 🌐 Obtener Playlists Públicas
```http
GET /playlists/public
```

**Response (200):** Lista de playlists públicas de otros usuarios

### 🔍 Buscar Playlists Públicas
```http
GET /playlists/search?q=rock
```

**Response (200):** Lista de playlists públicas que coinciden con la búsqueda

---

## 👑 Endpoints de Administración

> **Nota:** Requieren rol ADMIN

### 📋 Obtener Canciones Pendientes (Paginado)
```http
GET /admin/songs/pending?page=0&size=20&sort=createdAt,desc
```

**Parámetros de Query:**
- `page` (opcional): Número de página (default: 0)
- `size` (opcional): Tamaño de página (default: 20, máximo: 20)
- `sort` (opcional): Campo y dirección de ordenamiento (default: "createdAt,desc")

**Response (200):** Página de canciones pendientes de aprobación

### 📊 Obtener Todas las Canciones (Paginado)
```http
GET /admin/songs?page=0&size=20&sort=createdAt,desc
```

**Parámetros de Query:**
- `page` (opcional): Número de página (default: 0)
- `size` (opcional): Tamaño de página (default: 20, máximo: 20)
- `sort` (opcional): Campo y dirección de ordenamiento (default: "createdAt,desc")

**Response (200):** Página con todas las canciones del sistema

### ✅ Aprobar Canción
```http
PUT /admin/songs/{id}/approve
```

**Response (200):** Canción aprobada y publicada

### ❌ Rechazar Canción
```http
PUT /admin/songs/{id}/reject
```

**Request Body:**
```json
{
  "reason": "La letra contiene contenido inapropiado"
}
```

**Response (200):** Canción rechazada

### 🔒 Despublicar Canción
```http
PUT /admin/songs/{id}/unpublish
```

**Response (200):** Canción despublicada

### 🗑️ Eliminar Canción (Admin)
```http
DELETE /admin/songs/{id}
```

**Response (204):** Sin contenido

### 📈 Obtener Estadísticas
```http
GET /admin/stats
```

**Response (200):**
```json
{
  "totalSongs": 150,
  "draftSongs": 45,
  "pendingSongs": 12,
  "approvedSongs": 78,
  "rejectedSongs": 15,
  "totalUsers": 89
}
```

---

## 📊 Modelos de Datos

### SongRequest
```json
{
  "title": "string",
  "artist": "string",
  "album": "string",
  "year": "integer",
  "lyricsData": "string",
  "chords": [
    {
      "chordName": "string",
      "startPos": "integer",
      "endPos": "integer",
      "lineNumber": "integer"
    }
  ]
}
```

### SongResponse
```json
{
  "id": "long",
  "title": "string",
  "artist": "string",
  "album": "string",
  "year": "integer",
  "key": "string",
  "tempo": "integer",
  "status": "DRAFT|PENDING|APPROVED|REJECTED",
  "isPublic": "boolean",
  "rejectionReason": "string",
  "createdAt": "datetime",
  "publishedAt": "datetime",
  "createdBy": {
    "id": "long",
    "username": "string",
    "firstname": "string"
  },
  "lyrics": [
    {
      "lineNumber": "integer",
      "text": "string",
      "chords": [
        {
          "start": "integer",
          "name": "string",
          "chordId": "long"
        }
      ]
    }
  ]
}
```

### PageResponse
```json
{
  "content": ["array of items"],
  "totalElements": "long",
  "totalPages": "integer",
  "size": "integer",
  "number": "integer",
  "first": "boolean",
  "last": "boolean",
  "numberOfElements": "integer"
}
```

### ChordInfo
```json
{
  "id": "long",
  "name": "string",
  "fullName": "string",
  "fingerPositions": "string",
  "difficulty": "BEGINNER|INTERMEDIATE|ADVANCED",
  "isCommon": "boolean",
  "displayOrder": "integer"
}
```

### ChordPosition
```json
{
  "chordName": "string",
  "startPos": "integer",
  "endPos": "integer",
  "lineNumber": "integer"
}
```

### CreatePlaylistRequest
```json
{
  "name": "string",
  "description": "string",
  "isPublic": "boolean"
}
```

### UpdatePlaylistRequest
```json
{
  "name": "string",
  "description": "string",
  "isPublic": "boolean"
}
```

### AddSongToPlaylistRequest
```json
{
  "songId": "long"
}
```

### PlaylistResponse
```json
{
  "id": "long",
  "name": "string",
  "description": "string",
  "isPublic": "boolean",
  "isDefault": "boolean",
  "createdAt": "string (ISO 8601)",
  "updatedAt": "string (ISO 8601)",
  "songs": [
    {
      "id": "long",
      "title": "string",
      "artist": "string",
      "genre": "string",
      "isPublic": "boolean",
      "addedAt": "string (ISO 8601)"
    }
  ]
}
```

### PlaylistSummaryResponse
```json
{
  "id": "long",
  "name": "string",
  "description": "string",
  "isPublic": "boolean",
  "isDefault": "boolean",
  "songCount": "integer",
  "createdAt": "string (ISO 8601)"
}
```

---

## ❌ Códigos de Error

### 400 - Bad Request
```json
{
  "error": "BAD_REQUEST",
  "message": "Datos de entrada inválidos"
}
```

### 401 - Unauthorized
```json
{
  "error": "UNAUTHORIZED",
  "message": "Token JWT inválido o expirado"
}
```

### 403 - Forbidden
```json
{
  "error": "FORBIDDEN",
  "message": "No tienes permisos para realizar esta acción"
}
```

### 404 - Not Found
```json
{
  "error": "NOT_FOUND",
  "message": "Recurso no encontrado"
}
```

### 500 - Internal Server Error
```json
{
  "error": "INTERNAL_ERROR",
  "message": "Error interno del servidor"
}
```

---

## 🚀 Ejemplos de Uso

### Flujo Completo: Crear Canción con Acordes

1. **Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"usuario","password":"password"}'
```

2. **Crear Canción:**
```bash
curl -X POST http://localhost:8080/api/songs \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Hotel California",
    "artist": "Eagles",
    "album": "Hotel California",
    "year": 1976,
    "lyricsData": "On a dark desert highway\nCool wind in my hair",
    "chords": [
      {"chordName": "Am", "startPos": 0, "endPos": 2, "lineNumber": 0},
      {"chordName": "E", "startPos": 15, "endPos": 16, "lineNumber": 0},
      {"chordName": "F", "startPos": 0, "endPos": 1, "lineNumber": 1}
    ]
  }'
```

3. **Obtener Acordes Disponibles:**
```bash
curl -X GET http://localhost:8080/api/songs/available-chords \
  -H "Authorization: Bearer <token>"
```

4. **Actualizar Posiciones de Acordes:**
```bash
curl -X PUT http://localhost:8080/api/songs/1/chords \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '[
    {"chordName": "C", "startPos": 0, "endPos": 1, "lineNumber": 0},
    {"chordName": "G", "startPos": 10, "endPos": 11, "lineNumber": 0}
  ]'
```

---

## 🔧 Configuración

### Variables de Entorno
```bash
# Base de datos
DB_URL=jdbc:mysql://localhost:3306/rechords
DB_USERNAME=root
DB_PASSWORD=password

# JWT
JWT_SECRET=mi_clave_secreta_muy_larga_y_segura
JWT_EXPIRATION=86400000

# Servidor
SERVER_PORT=8080
```

### Dependencias Maven
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
```

---

## 📝 Notas Importantes

1. **Posiciones de Acordes:** Las posiciones se basan en índices de caracteres (0-based)
2. **Líneas:** Las líneas también son 0-based
3. **Estados de Canción:** DRAFT → PENDING → APPROVED/REJECTED
4. **Roles:** USER (crear/editar propias) vs ADMIN (gestión completa)
5. **Acordes:** 36 acordes predefinidos con diferentes niveles de dificultad
6. **Paginación:** Todos los endpoints de listado usan paginación con máximo 20 elementos por página
7. **Ordenamiento:** Soporte para ordenamiento por múltiples campos con dirección ASC/DESC
8. **Búsqueda:** La búsqueda es case-insensitive y busca en título y artista

---

## 🤝 Soporte

Para soporte técnico o preguntas sobre la API, contacta al equipo de desarrollo.

**Versión de la API:** 1.0.0  
**Última actualización:** Enero 2024

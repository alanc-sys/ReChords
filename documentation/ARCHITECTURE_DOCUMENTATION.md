# 🏗️ ReChords - Documentación de Arquitectura

## 📋 Tabla de Contenidos

1. [Visión General](#visión-general)
2. [Arquitectura del Sistema](#arquitectura-del-sistema)
3. [Flujo de Trabajo de Acordes](#flujo-de-trabajo-de-acordes)
4. [Sistema de Importación](#sistema-de-importación)
5. [Sistema de Playlists](#sistema-de-playlists)
6. [Base de Datos](#base-de-datos)
7. [Seguridad](#seguridad)
8. [Patrones de Diseño](#patrones-de-diseño)
9. [Flujo de Datos](#flujo-de-datos)
10. [Consideraciones de Performance](#consideraciones-de-performance)
11. [Sistema de Paginación](#sistema-de-paginación)

---

## 🎯 Visión General

ReChords es una aplicación de **biblioteca musical personal** que permite a los usuarios gestionar canciones con **posiciones precisas de acordes**. La aplicación combina funcionalidades de **gestión de contenido** con **herramientas educativas** para músicos.

### Características Principales:
- 🎵 **Gestión de canciones** con letras y acordes en formato JSON
- 🎸 **Sistema de acordes** con posiciones precisas
- 📥 **Importación automática** de canciones desde texto plano
- 📚 **Sistema de playlists/bibliotecas** personalizadas
- 🏷️ **Playlists por defecto** (Favoritas, Mis Creaciones)
- 👥 **Roles de usuario** (USER/ADMIN)
- 🔄 **Workflow de aprobación** para contenido
- 📊 **Estadísticas y administración**
- 🔍 **Búsqueda y filtrado** avanzado
- 📄 **Paginación** en todos los endpoints de listado
- ⚡ **Procesamiento asíncrono** de analytics

---

## 🏗️ Arquitectura del Sistema

### Diagrama de Arquitectura

```
┌─────────────────────────────────────────────────────────────────┐
│                        FRONTEND (React/Vue)                    │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐            │
│  │   Editor    │  │  Visualizer │  │   Search    │            │
│  │   Drag&Drop │  │   Lyrics    │  │   Filters   │            │
│  └─────────────┘  └─────────────┘  └─────────────┘            │
└─────────────────────────────────────────────────────────────────┘
                                │
                                │ HTTP/REST
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    SPRING BOOT BACKEND                         │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐            │
│  │ Controllers │  │  Services   │  │ Repositories│            │
│  │   REST API  │  │ Business    │  │ Data Access │            │
│  └─────────────┘  └─────────────┘  └─────────────┘            │
│           │              │              │                     │
│           └──────────────┼──────────────┘                     │
│                          │                                    │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐            │
│  │   Security  │  │    JWT      │  │ Validation  │            │
│  │   Filters   │  │   Service   │  │   Layer     │            │
│  └─────────────┘  └─────────────┘  └─────────────┘            │
└─────────────────────────────────────────────────────────────────┘
                                │
                                │ JPA/Hibernate
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                      MYSQL DATABASE                            │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐            │
│  │    Users    │  │    Songs    │  │   Chords    │            │
│  │   Roles     │  │   Status    │  │  Catalog    │            │
│  └─────────────┘  └─────────────┘  └─────────────┘            │
│  ┌─────────────┐                                              │
│  │ SongChords  │                                              │
│  │ Positions   │                                              │
│  └─────────────┘                                              │
└─────────────────────────────────────────────────────────────────┘
```

### Capas de la Aplicación

#### 1. **Capa de Presentación (Controllers)**
- **Responsabilidad:** Manejo de requests HTTP
- **Componentes:** 
  - `AuthController` - Autenticación (`/api/auth`)
  - `SongController` - Gestión de canciones y acordes (`/api/songs`)
  - `PlaylistController` - Gestión de playlists (`/api/playlists`)
  - `AdminController` - Funciones de administrador (`/api/admin`)
  - `FileUploadController` - Subida/eliminación de portadas (`/api/songs/{id}/cover`)

#### 2. **Capa de Servicios (Business Logic)**
- **Responsabilidad:** Lógica de negocio y orquestación
- **Componentes:**
  - `AuthService` - Autenticación y autorización
  - `SongService` - Gestión de canciones y acordes
  - `ChordService` - Catálogo de acordes
  - `JwtService` - Manejo de tokens JWT

#### 3. **Capa de Persistencia (Repositories)**
- **Responsabilidad:** Acceso a datos
- **Componentes:**
  - `UserRepository` - Usuarios
  - `SongRepository` - Canciones
  - `SongChordRepository` - Posiciones de acordes
  - `ChordCatalogRepository` - Catálogo de acordes

#### 4. **Capa de Entidades (Domain Models)**
- **Responsabilidad:** Modelado del dominio
- **Componentes:**
  - `User` - Usuario del sistema
  - `Song` - Canción
  - `SongChord` - Posición de acorde en canción
  - `ChordCatalog` - Acorde del catálogo

---

## 🎸 Flujo de Trabajo de Acordes

### Diagrama de Flujo

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │    Backend      │    │   Database      │
│   Editor        │    │   Services      │    │   Storage       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │ 1. Drag Chord         │                       │
         ├──────────────────────►│                       │
         │                       │                       │
         │ 2. Calculate Position │                       │
         │    (line, start, end) │                       │
         ├──────────────────────►│                       │
         │                       │                       │
         │ 3. Save Song          │                       │
         ├──────────────────────►│                       │
         │                       │ 4. Validate Chords   │
         │                       ├──────────────────────►│
         │                       │                       │
         │                       │ 5. Save Song         │
         │                       ├──────────────────────►│
         │                       │                       │
         │                       │ 6. Save Chord        │
         │                       │    Positions         │
         │                       ├──────────────────────►│
         │                       │                       │
         │ 7. Response with      │                       │
         │    Chord Positions    │                       │
         │◄──────────────────────┤                       │
         │                       │                       │
         │ 8. Render Lyrics      │                       │
         │    with Chords        │                       │
         │                       │                       │
```

### Proceso Detallado

#### 1. **Creación de Canción con Acordes**
```java
// Frontend envía:
{
  "title": "Mi Canción",
  "lyricsData": "Letra de la canción\nSegunda línea",
  "chords": [
    {
      "chordName": "C",
      "startPos": 0,      // Posición inicial en la línea
      "endPos": 1,        // Posición final en la línea
      "lineNumber": 0     // Número de línea (0-based)
    }
  ]
}

// Backend procesa:
1. Validar datos de entrada
2. Crear entidad Song
3. Guardar Song en base de datos
4. Para cada ChordPosition:
   - Validar que el acorde existe en ChordCatalog
   - Crear entidad SongChord
   - Guardar SongChord en base de datos
5. Retornar SongResponse con chordPositions
```

#### 2. **Actualización de Posiciones de Acordes**
```java
// Frontend envía:
PUT /songs/{id}/chords
[
  {
    "chordName": "Am",
    "startPos": 5,
    "endPos": 7,
    "lineNumber": 1
  }
]

// Backend procesa:
1. Validar permisos (solo el creador puede editar)
2. Validar estado de canción (solo DRAFT o REJECTED)
3. Eliminar todas las SongChord existentes
4. Crear nuevas SongChord con las posiciones actualizadas
5. Retornar SongResponse actualizado
```

#### 3. **Visualización de Canciones con Acordes**
```java
// Frontend solicita:
GET /songs/{id}

// Backend retorna:
{
  "id": 1,
  "title": "Mi Canción",
  "lyrics": "Letra de la canción\nSegunda línea",
  "chordPositions": [
    {
      "chordName": "C",
      "startPos": 0,
      "endPos": 1,
      "lineNumber": 0
    }
  ]
}

// Frontend renderiza:
Línea 0: C    Letra de la canción
Línea 1:      Segunda línea
```

---

## 📥 Sistema de Importación

El sistema de importación permite a los usuarios convertir texto plano con formato de canción (letras y acordes) en el formato JSON estructurado que utiliza ReChords.

### Arquitectura del Sistema de Importación

```
┌─────────────────────────────────────────────────────────────────┐
│                    SISTEMA DE IMPORTACIÓN                     │
│                                                                 │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐        │
│  │   Raw Text  │    │ SongImport  │    │ SongWith    │        │
│  │   Input     │───►│   Service   │───►│ Chords      │        │
│  │             │    │             │    │ Request     │        │
│  └─────────────┘    └─────────────┘    └─────────────┘        │
│         │                   │                   │              │
│         │                   ▼                   │              │
│         │            ┌─────────────┐            │              │
│         │            │ Chord       │            │              │
│         │            │ Catalog     │            │              │
│         │            │ Repository  │            │              │
│         │            └─────────────┘            │              │
│         │                                       │              │
│         └───────────────────────────────────────┘              │
└─────────────────────────────────────────────────────────────────┘
```

### Características del Importador

#### 🎯 Detección Automática
- **Título y Artista**: Extrae automáticamente del formato "Artista - Título"
- **Secciones**: Reconoce headers como `[Verse 1]`, `[Chorus]`, etc.
- **Líneas de acordes**: Identifica líneas que contienen principalmente acordes

#### 🎸 Reconocimiento de Acordes
- **Regex avanzado**: Patrón que reconoce acordes estándar (C, Am, F#m7, etc.)
- **Validación**: Verifica acordes contra el catálogo de acordes
- **Posicionamiento**: Calcula posiciones exactas de acordes en el texto

#### 📝 Procesamiento de Texto
- **Emparejamiento**: Asocia líneas de acordes con líneas de letra
- **Filtrado**: Ignora tablaturas y metadatos irrelevantes
- **Limpieza**: Normaliza espacios y caracteres especiales

### Algoritmo de Importación

#### 1. Análisis de Líneas
```java
for (String line : lines) {
    if (isChordLine(line)) {
        // Procesar línea de acordes
        processChordLine(line);
    } else if (isSectionHeader(line)) {
        // Procesar header de sección
        processSectionHeader(line);
    } else {
        // Procesar línea de letra
        processLyricLine(line);
    }
}
```

#### 2. Detección de Acordes
```java
Pattern CHORD_PATTERN = Pattern.compile(
    "\\b([A-G][b#]?(m|maj|dim|aug|sus)?[0-9]?[7]?(?![a-zA-Z]))\\b"
);
```

#### 3. Emparejamiento de Líneas
- **Línea de acordes + Línea de letra**: Se combinan en una sola entrada
- **Línea de acordes sola**: Se crea entrada con acordes pero sin letra
- **Línea de letra sola**: Se crea entrada con letra pero sin acordes

### Flujo de Trabajo

#### 1. Recepción de Texto
```
Usuario → SongController.importSong() → SongImportService.parse()
```

#### 2. Procesamiento
```
Raw Text → Análisis de líneas → Detección de acordes → Validación → JSON estructurado
```

#### 3. Validación
```
Acordes detectados → ChordCatalogRepository → Validación contra catálogo → IDs asignados
```

### Formatos Soportados

#### Formato Estándar
```
Título - Artista

[Sección]
C        Am        F         G
Letra de la canción con acordes arriba
```

#### Formato con Múltiples Secciones
```
Bohemian Rhapsody - Queen

[Verse 1]
C        Am        F         G
Is this the real life? Is this just fantasy?

[Chorus]
F        C         Dm        G
Mama, just killed a man
```

### Validaciones y Limitaciones

#### ✅ Validaciones Implementadas
- **Formato de acordes**: Solo acordes válidos del catálogo
- **Estructura**: Detección automática de título/artista
- **Posicionamiento**: Cálculo correcto de posiciones de acordes

#### ⚠️ Limitaciones Actuales
- **Tablaturas**: No se procesan (se ignoran)
- **Acordes complejos**: Algunos acordes muy específicos pueden no detectarse
- **Formato rígido**: Requiere formato relativamente estándar

### Endpoint de Importación

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/songs/import` | Importar canción desde texto plano |

**Request:** Texto plano con formato de canción
**Response:** `SongWithChordsRequest` estructurado

---

## 📚 Sistema de Playlists

El sistema de playlists permite a los usuarios organizar sus canciones en bibliotecas personalizadas, facilitando la gestión y acceso a su música favorita.

### Arquitectura del Sistema de Playlists

```
┌─────────────────────────────────────────────────────────────────┐
│                    SISTEMA DE PLAYLISTS                        │
│                                                                 │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐        │
│  │   Playlist  │    │ PlaylistSong│    │    Song     │        │
│  │   Entity    │◄──►│   Entity    │◄──►│   Entity    │        │
│  │             │    │ (Join Table)│    │             │        │
│  └─────────────┘    └─────────────┘    └─────────────┘        │
│         │                                                      │
│         ▼                                                      │
│  ┌─────────────┐                                              │
│  │    User     │                                              │
│  │   Entity    │                                              │
│  └─────────────┘                                              │
└─────────────────────────────────────────────────────────────────┘
```

### Características del Sistema

#### 🏷️ Playlists por Defecto
Cada usuario recibe automáticamente dos playlists al registrarse:
- **"Favoritas"**: Para marcar canciones como favoritas
- **"Mis Creaciones"**: Para organizar canciones creadas por el usuario

#### 🎵 Gestión de Canciones
- **Añadir canciones**: Los usuarios pueden añadir cualquier canción pública o sus propias creaciones
- **Eliminar canciones**: Remover canciones de playlists personalizadas
- **Orden cronológico**: Las canciones se mantienen en orden de adición

#### 🌐 Visibilidad
- **Playlists privadas**: Solo visibles para el propietario
- **Playlists públicas**: Visibles para otros usuarios para exploración
- **Búsqueda**: Los usuarios pueden buscar playlists públicas por nombre

### Flujo de Trabajo

#### 1. Creación de Playlist
```
Usuario → PlaylistController → PlaylistService → PlaylistRepository → Base de Datos
```

#### 2. Añadir Canción a Playlist
```
Usuario → PlaylistController → PlaylistService → Validaciones → PlaylistSongRepository → Base de Datos
```

#### 3. Gestión de Playlists por Defecto
```
Registro de Usuario → AuthService → PlaylistService.createDefaultPlaylistsForUser() → Base de Datos
```

### Validaciones de Negocio

#### ✅ Reglas de Validación
- **Propiedad**: Solo el propietario puede modificar sus playlists
- **Canciones existentes**: No se pueden añadir canciones duplicadas
- **Playlists por defecto**: No se pueden eliminar las playlists "Favoritas" y "Mis Creaciones"
- **Canciones públicas**: Solo se pueden añadir canciones públicas o propias

#### 🔒 Seguridad
- **Autenticación**: Todos los endpoints requieren JWT válido
- **Autorización**: Los usuarios solo pueden acceder a sus propias playlists (excepto públicas)
- **Validación de datos**: Sanitización de nombres y descripciones

### Endpoints Principales

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/playlists` | Crear nueva playlist |
| `GET` | `/api/playlists/my` | Obtener mis playlists |
| `GET` | `/api/playlists/{id}` | Obtener playlist específica |
| `PUT` | `/api/playlists/{id}` | Actualizar playlist |
| `DELETE` | `/api/playlists/{id}` | Eliminar playlist |
| `POST` | `/api/playlists/{id}/songs` | Añadir canción |
| `DELETE` | `/api/playlists/{id}/songs/{songId}` | Eliminar canción |
| `GET` | `/api/playlists/public` | Obtener playlists públicas |
| `GET` | `/api/playlists/search?q=query` | Buscar playlists públicas |

---

## 🗄️ Base de Datos

### Diagrama ER

```
┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│    User     │         │    Song     │         │  Playlist   │
│─────────────│         │─────────────│         │─────────────│
│ id (PK)     │◄────────┤ created_by  │         │ id (PK)     │
│ username    │         │ id (PK)     │         │ name        │
│ password    │         │ title       │         │ user_id     │◄──┐
│ firstname   │         │ artist      │         │ description │   │
│ lastname    │         │ album       │         │ is_public   │   │
│ country     │         │ year        │         │ is_default  │   │
│ role        │         │ lyrics_data │         │ created_at  │   │
│             │         │ chords_map  │         │ updated_at  │   │
│             │         │ status      │         │             │   │
│             │         │ is_public   │         │             │   │
│             │         │ rejection_  │         │             │   │
│             │         │   reason    │         │             │   │
│             │         │ created_at  │         │             │   │
│             │         │ updated_at  │         │             │   │
│             │         │ published_  │         │             │   │
│             │         │   at        │         │             │   │
└─────────────┘         └─────────────┘         └─────────────┘
                                                         │
                                                         │
                                                ┌─────────────┐
                                                │PlaylistSong │
                                                │─────────────│
                                                │ id (PK)     │
                                                │ playlist_id │◄──┘
                                                │ song_id     │◄────┐
                                                │ added_at    │     │
                                                │             │     │
                                                └─────────────┘     │
                                                         │         │
                                                         │         │
                                                ┌─────────────┐     │
                                                │ChordCatalog │     │
                                                │─────────────│     │
                                                │ id (PK)     │     │
                                                │ name        │     │
                                                │ full_name   │     │
                                                │ category    │     │
                                                │ difficulty_ │     │
                                                │   level     │     │
                                                │ is_common   │     │
                                                │ display_    │     │
                                                │   order     │     │
                                                │ finger_     │     │
                                                │   positions │     │
                                                │ notes       │     │
                                                └─────────────┘     │
                                                         │         │
                                                         └─────────┘
```

### Relaciones

1. **User → Song (1:N)**
   - Un usuario puede crear múltiples canciones
   - `Song.created_by` → `User.id`

2. **Song → SongChord (1:N)**
   - Una canción puede tener múltiples posiciones de acordes
   - `SongChord.song_id` → `Song.id`

3. **ChordCatalog → SongChord (1:N)**
   - Un acorde del catálogo puede aparecer en múltiples canciones
   - `SongChord.chord_id` → `ChordCatalog.id`

### Índices Recomendados

```sql
-- Para búsquedas por usuario
CREATE INDEX idx_song_created_by ON songs(created_by);

-- Para búsquedas por estado
CREATE INDEX idx_song_status ON songs(status);

-- Para búsquedas públicas
CREATE INDEX idx_song_public ON songs(is_public, status);

-- Para posiciones de acordes
CREATE INDEX idx_song_chord_song ON song_chords(song_id);
CREATE INDEX idx_song_chord_position ON song_chords(song_id, line_number, position_start);

-- Para catálogo de acordes
CREATE INDEX idx_chord_name ON chord_catalog(name);
CREATE INDEX idx_chord_common ON chord_catalog(is_common);
```

---

## 🔐 Seguridad

### Autenticación JWT

```java
// Flujo de autenticación:
1. Usuario envía credenciales → /auth/login
2. AuthService valida credenciales
3. JwtService genera token JWT
4. Token se retorna al cliente
5. Cliente incluye token en headers: Authorization: Bearer <token>
6. JwtAuthenticationFilter valida token en cada request
7. SecurityContext se establece con UserDetails
```

### Autorización por Roles

```java
// Roles disponibles:
- USER: Puede crear/editar sus propias canciones
- ADMIN: Puede gestionar todas las canciones y usuarios

// Verificaciones de seguridad:
1. @PreAuthorize("hasAuthority('ADMIN')") - A nivel de método
2. verifyAdmin() - En servicios
3. canUserViewSong() - Control de acceso granular
```

### Filtros de Seguridad

```java
// SecurityConfig configura:
1. CSRF deshabilitado (API REST)
2. JWT filter antes de UsernamePasswordAuthenticationFilter
3. Rutas públicas: /auth/**, /api/auth/**, /api/songs/available-chords, /api/songs/common-chords, /api/uploads/**
4. Rutas admin: /api/admin/** (requiere ADMIN)
5. Resto de rutas: requieren autenticación
```

---

## 🎨 Patrones de Diseño

### 1. **Repository Pattern**
```java
// Abstracción del acceso a datos
public interface SongRepository extends JpaRepository<Song, Long> {
    List<Song> findByCreatedById(Long userId);
    List<Song> findByIsPublicTrueAndStatus(SongStatus status);
}
```

### 2. **Service Layer Pattern**
```java
// Encapsulación de lógica de negocio
@Service
public class SongService {
    // Lógica de negocio compleja
    // Validaciones
    // Orquestación de operaciones
}
```

### 3. **Builder Pattern**
```java
// Construcción de objetos complejos
Song song = Song.builder()
    .title("Mi Canción")
    .artist("Artista")
    .createdBy(user)
    .build();
```

### 4. **DTO Pattern**
```java
// Separación entre entidades de dominio y transferencia de datos
public class SongRequest {
    // Datos de entrada
}

public class SongResponse {
    // Datos de salida
}
```

### 5. **Strategy Pattern**
```java
// Diferentes estrategias de validación según el rol
private boolean canUserViewSong(User user, Song song) {
    if (user.getRole() == Role.ADMIN) return true;
    if (song.getCreatedBy().getId() == user.getId()) return true;
    return song.getIsPublic() && song.getStatus() == SongStatus.APPROVED;
}
```

---

## 🔄 Flujo de Datos

### Flujo de Creación de Canción

```
1. Frontend → SongController.createSong()
2. SongController → SongService.createSong()
3. SongService → SongRepository.save()
4. SongService → ChordCatalogRepository.findByName()
5. SongService → SongChordRepository.save()
6. SongService → mapToResponse()
7. SongService → SongController
8. SongController → Frontend
```

### Flujo de Autenticación

```
1. Frontend → AuthController.login()
2. AuthController → AuthService.login()
3. AuthService → AuthenticationManager.authenticate()
4. AuthService → UserRepository.findByUsername()
5. AuthService → JwtService.getToken()
6. AuthService → AuthController
7. AuthController → Frontend (con token)
```

### Flujo de Autorización

```
1. Frontend → Request con JWT
2. JwtAuthenticationFilter → JwtService.validateToken()
3. JwtService → UserDetailsService.loadUserByUsername()
4. JwtAuthenticationFilter → SecurityContext.setAuthentication()
5. Controller → @PreAuthorize o verifyAdmin()
6. Controller → Service
7. Service → Repository
```

---

## ⚡ Consideraciones de Performance

### 1. **Optimizaciones de Base de Datos**

```java
// Lazy Loading para relaciones
@ManyToOne(fetch = FetchType.LAZY)
private User createdBy;

// Índices para consultas frecuentes
@Table(indexes = {
    @Index(name = "idx_song_status", columnList = "status"),
    @Index(name = "idx_song_public", columnList = "is_public, status")
})
```

### 2. **Caching**

```java
// Cache para acordes comunes (futuro)
@Cacheable("common-chords")
public List<ChordInfo> getCommonChordsForSelection() {
    // Implementación
}
```

### 3. **Paginación**

```java
// Para listados grandes (futuro)
public Page<SongResponse> getSongs(Pageable pageable) {
    // Implementación con paginación
}
```

### 4. **Optimizaciones de Consultas**

```java
// Consultas específicas en lugar de N+1
@Query("SELECT s FROM Song s JOIN FETCH s.createdBy WHERE s.id = :id")
Optional<Song> findByIdWithCreator(@Param("id") Long id);
```

---

## 🚀 Escalabilidad

### 1. **Horizontal Scaling**
- **Load Balancer** para múltiples instancias
- **Stateless** - Sin sesiones en servidor
- **JWT** - Tokens autónomos

### 2. **Base de Datos**
- **Read Replicas** para consultas
- **Sharding** por usuario (futuro)
- **Connection Pooling** configurado

### 3. **Caching**
- **Redis** para cache distribuido
- **CDN** para assets estáticos
- **Application Cache** para datos frecuentes

### 4. **Microservicios** (Futuro)
```
┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│   Auth      │  │   Songs     │  │   Chords    │
│  Service    │  │  Service    │  │  Service    │
└─────────────┘  └─────────────┘  └─────────────┘
```

---

## 📊 Métricas y Monitoreo

### 1. **Health Checks**
```java
// Spring Boot Actuator
/actuator/health
/actuator/metrics
/actuator/info
```

### 2. **Logging**
```java
// Logs estructurados
@Slf4j
@Service
public class SongService {
    public SongResponse createSong(SongRequest request) {
        log.info("Creating song with title: {}", request.getTitle());
        // Implementación
    }
}
```

### 3. **Métricas de Negocio**
- Canciones creadas por día
- Acordes más utilizados
- Usuarios activos
- Tiempo de aprobación promedio

---

## 🔧 Configuración de Desarrollo

### 1. **Profiles**
```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
  jpa:
    hibernate:
      ddl-auto: create-drop
```

### 2. **Testing**
```java
// Tests unitarios con Mockito
@ExtendWith(MockitoExtension.class)
class SongServiceTest {
    // Tests aislados
}

// Tests de integración con @SpringBootTest
@SpringBootTest
class SongControllerIntegrationTest {
    // Tests con contexto completo
}
```

### 3. **Docker** (Futuro)
```dockerfile
FROM openjdk:21-jdk-slim
COPY target/application.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

---

## 📄 Sistema de Paginación

### Visión General

El sistema de paginación de ReChords está implementado para optimizar el rendimiento y la experiencia del usuario al manejar grandes cantidades de datos. Todos los endpoints de listado utilizan paginación para evitar sobrecargar el cliente y el servidor.

### Implementación Técnica

#### 1. **PageResponse DTO**
```java
@Getter
@Setter
@Builder
public class PageResponse<T> {
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    private boolean empty;
    private int numberOfElements;

    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
            .content(page.getContent())
            .pageNumber(page.getNumber())
            .pageSize(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .first(page.isFirst())
            .last(page.isLast())
            .empty(page.isEmpty())
            .numberOfElements(page.getNumberOfElements())
            .build();
    }
}
```

#### 2. **Repository Layer**
```java
@Repository
public interface SongRepository extends JpaRepository<Song, Long> {
    // Métodos paginados
    Page<Song> findByCreatedById(Long userId, Pageable pageable);
    Page<Song> findByIsPublicTrueAndStatus(SongStatus status, Pageable pageable);
    Page<Song> findByStatus(SongStatus status, Pageable pageable);
    Page<Song> findByIsPublicTrueAndStatusAndTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(
        SongStatus status, String title, String artist, Pageable pageable);
    Page<Song> findAll(Pageable pageable);
}
```

#### 3. **Service Layer**
```java
@Service
public class SongService extends BaseService {
    
    public PageResponse<SongWithChordsResponse> getMySongsWithChordsPaginated(Pageable pageable) {
        User currentUser = getCurrentUser();
        Page<Song> songsPage = songRepository.findByCreatedById(currentUser.getId(), pageable);
        Page<SongWithChordsResponse> responsePage = songsPage.map(this::mapToSongWithChordsResponse);
        return PageResponse.from(responsePage);
    }
    
    public PageResponse<SongWithChordsResponse> getPublicSongsWithChordsPaginated(Pageable pageable) {
        Page<Song> songsPage = songRepository.findByIsPublicTrueAndStatus(SongStatus.APPROVED, pageable);
        Page<SongWithChordsResponse> responsePage = songsPage.map(this::mapToSongWithChordsResponse);
        return PageResponse.from(responsePage);
    }
}
```

#### 4. **Controller Layer**
```java
@RestController
@RequestMapping("/api/songs")
public class songController {
    
    @GetMapping("/my")
    public ResponseEntity<PageResponse<SongWithChordsResponse>> getMySongsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {
        
        Pageable pageable = createPageable(page, size, sort);
        PageResponse<SongWithChordsResponse> response = songService.getMySongsWithChordsPaginated(pageable);
        return ResponseEntity.ok(response);
    }
    
    private Pageable createPageable(int page, int size, String[] sort) {
        // Validación de límites
        if (size > 20) size = 20;
        if (size < 1) size = 1;
        
        // Configuración de ordenamiento
        Sort.Direction direction = Sort.Direction.DESC;
        String property = "createdAt";
        
        if (sort.length > 0) {
            property = sort[0];
            if (sort.length > 1) {
                direction = Sort.Direction.fromString(sort[1]);
            }
        }
        
        return PageRequest.of(page, size, Sort.by(direction, property));
    }
}
```

### Parámetros de Paginación

#### Query Parameters
- `page` (opcional): Número de página (0-based, default: 0)
- `size` (opcional): Tamaño de página (default: 20; máximo 20 usuario, 100 admin)
- `sort` (opcional): Campo y dirección de ordenamiento (default: "createdAt,desc")

#### Ejemplos de Uso
```http
# Primera página con 10 elementos
GET /api/songs/my?page=0&size=10

# Segunda página ordenada por título ascendente
GET /api/songs/public?page=1&size=20&sort=title,asc

# Búsqueda paginada
GET /api/songs/search?q=rock&page=0&size=15&sort=publishedAt,desc
```

### Endpoints con Paginación

#### Usuario
- `GET /songs/my` - Mis canciones
- `GET /songs/public` - Canciones públicas
- `GET /songs/search` - Búsqueda de canciones

#### Administración
- `GET /admin/songs/pending` - Canciones pendientes
- `GET /admin/songs` - Todas las canciones

### Beneficios de la Paginación

#### 1. **Rendimiento**
- Reduce la carga en la base de datos
- Minimiza el uso de memoria
- Mejora los tiempos de respuesta

#### 2. **Experiencia de Usuario**
- Carga más rápida de listas
- Navegación intuitiva
- Control del tamaño de página

#### 3. **Escalabilidad**
- Manejo eficiente de grandes volúmenes
- Preparado para crecimiento
- Optimización de recursos

### Consideraciones de Implementación

#### 1. **Límites de Seguridad**
```java
// Validación de límites en el controlador
if (size > 20) size = 20;  // Máximo 20 elementos por página
if (size < 1) size = 1;    // Mínimo 1 elemento por página
```

#### 2. **Ordenamiento por Defecto**
```java
// Ordenamiento por defecto para cada endpoint
private String getDefaultSort(String endpoint) {
    switch (endpoint) {
        case "my": return "createdAt,desc";
        case "public": return "publishedAt,desc";
        case "search": return "title,asc";
        default: return "createdAt,desc";
    }
}
```

#### 3. **Manejo de Errores**
```java
@ExceptionHandler(IllegalArgumentException.class)
public ResponseEntity<ErrorResponse> handlePaginationError(IllegalArgumentException e) {
    return ResponseEntity.badRequest()
        .body(ErrorResponse.builder()
            .error("INVALID_PAGINATION")
            .message("Parámetros de paginación inválidos")
            .build());
}
```

### Testing de Paginación

#### Tests Unitarios
```java
@Test
void getMySongsWithChordsPaginated_ShouldReturnPaginatedResponse() {
    // Arrange
    List<Song> songs = Arrays.asList(testSong);
    Page<Song> songPage = new PageImpl<>(songs);
    Pageable pageable = PageRequest.of(0, 10);
    when(songRepository.findByCreatedById(1L, pageable)).thenReturn(songPage);

    // Act
    PageResponse<SongWithChordsResponse> response = songService.getMySongsWithChordsPaginated(pageable);

    // Assert
    assertNotNull(response);
    assertEquals(1, response.getContent().size());
    assertEquals(1, response.getTotalElements());
    assertEquals(1, response.getTotalPages());
    verify(songRepository).findByCreatedById(1L, pageable);
}
```

#### Tests de Integración
```java
@Test
void getMySongsPaginated_ShouldReturnPaginatedResponse() {
    // Arrange
    createTestSongs(25); // Crear 25 canciones de prueba

    // Act
    ResponseEntity<PageResponse<SongWithChordsResponse>> response = 
        restTemplate.getForEntity("/api/songs/my?page=0&size=10", 
            new ParameterizedTypeReference<PageResponse<SongWithChordsResponse>>() {});

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    PageResponse<SongWithChordsResponse> pageResponse = response.getBody();
    assertEquals(10, pageResponse.getContent().size());
    assertEquals(25, pageResponse.getTotalElements());
    assertEquals(3, pageResponse.getTotalPages());
    assertTrue(pageResponse.isFirst());
    assertFalse(pageResponse.isLast());
}
```

### Métricas y Monitoreo

#### 1. **Métricas de Rendimiento**
- Tiempo de respuesta por página
- Uso de memoria por consulta
- Throughput de requests paginados

#### 2. **Métricas de Uso**
- Tamaño de página más común
- Patrones de navegación
- Frecuencia de uso de ordenamiento

#### 3. **Alertas**
- Tiempo de respuesta > 500ms
- Uso de memoria > 100MB
- Error rate > 1%

---

## 📝 Conclusiones

La arquitectura de ReChords está diseñada para ser:

- ✅ **Escalable** - Preparada para crecimiento
- ✅ **Mantenible** - Código limpio y bien estructurado
- ✅ **Segura** - Autenticación y autorización robustas
- ✅ **Performante** - Optimizada para operaciones frecuentes
- ✅ **Extensible** - Fácil agregar nuevas funcionalidades

El sistema de **posiciones de acordes** es la funcionalidad más innovadora, permitiendo una experiencia de usuario única donde los acordes se pueden arrastrar y posicionar precisamente sobre la letra de las canciones.

---

**Versión:** 1.0.0  
**Última actualización:** Enero 2024  
**Autor:** Equipo de Desarrollo ReChords

# 🏗️ ReChords - Documentación de Arquitectura

## 📋 Tabla de Contenidos

1. [Visión General](#visión-general)
2. [Arquitectura del Sistema](#arquitectura-del-sistema)
3. [Flujo de Trabajo de Acordes](#flujo-de-trabajo-de-acordes)
4. [Base de Datos](#base-de-datos)
5. [Seguridad](#seguridad)
6. [Patrones de Diseño](#patrones-de-diseño)
7. [Flujo de Datos](#flujo-de-datos)
8. [Consideraciones de Performance](#consideraciones-de-performance)

---

## 🎯 Visión General

ReChords es una aplicación de **biblioteca musical personal** que permite a los usuarios gestionar canciones con **posiciones precisas de acordes**. La aplicación combina funcionalidades de **gestión de contenido** con **herramientas educativas** para músicos.

### Características Principales:
- 🎵 **Gestión de canciones** con letras y acordes
- 🎸 **Sistema de acordes** con posiciones precisas
- 👥 **Roles de usuario** (USER/ADMIN)
- 🔄 **Workflow de aprobación** para contenido
- 📊 **Estadísticas y administración**
- 🔍 **Búsqueda y filtrado** avanzado

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
  - `AuthController` - Autenticación
  - `SongController` - Gestión de canciones
  - `AdminController` - Funciones de administrador
  - `ChordController` - Catálogo de acordes

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

## 🗄️ Base de Datos

### Diagrama ER

```
┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│    User     │         │    Song     │         │ SongChord   │
│─────────────│         │─────────────│         │─────────────│
│ id (PK)     │◄────────┤ created_by  │         │ id (PK)     │
│ username    │         │ id (PK)     │◄────────┤ song_id     │
│ password    │         │ title       │         │ chord_id    │
│ firstname   │         │ artist      │         │ position_   │
│ lastname    │         │ album       │         │   start     │
│ country     │         │ year        │         │ position_   │
│ role        │         │ lyrics_data │         │   end       │
│             │         │ status      │         │ line_number │
│             │         │ is_public   │         │ chord_name  │
│             │         │ rejection_  │         │ created_at  │
│             │         │   reason    │         │             │
│             │         │ created_at  │         │             │
│             │         │ updated_at  │         │             │
│             │         │ published_  │         │             │
│             │         │   at        │         │             │
└─────────────┘         └─────────────┘         └─────────────┘
                                                         │
                                                         │
                                                ┌─────────────┐
                                                │ChordCatalog │
                                                │─────────────│
                                                │ id (PK)     │
                                                │ name        │
                                                │ full_name   │
                                                │ category    │
                                                │ difficulty_ │
                                                │   level     │
                                                │ is_common   │
                                                │ display_    │
                                                │   order     │
                                                │ finger_     │
                                                │   positions │
                                                │ notes       │
                                                └─────────────┘
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
3. Rutas públicas: /auth/**
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

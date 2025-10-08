# 🎵 Sistema de Playlists/Bibliotecas - ReChords

## 📋 Resumen de la Implementación

Se ha implementado un sistema completo de playlists/bibliotecas personalizadas donde los usuarios pueden organizar canciones en categorías como "Rock", "Favoritas", "Mis Creaciones", etc.

## 🏗️ Arquitectura del Sistema

### Entidades Principales
- **Playlist**: Lista/biblioteca personalizada del usuario
- **PlaylistSong**: Relación many-to-many entre playlists y canciones
- **User**: Usuario propietario de las playlists

### Características Implementadas
- ✅ **Playlists personalizadas** con nombre y descripción
- ✅ **Playlists por defecto** ("Favoritas", "Mis Creaciones")
- ✅ **Playlists públicas/privadas** para compartir
- ✅ **Orden de canciones** mantenido con índices
- ✅ **Validaciones de permisos** y duplicados
- ✅ **Búsqueda de playlists públicas**

## 🎯 Funcionalidades del Usuario

### Gestión de Playlists
- **Crear playlist personalizada** con nombre y descripción
- **Editar playlist** (nombre, descripción, visibilidad)
- **Eliminar playlist** (excepto las por defecto)
- **Ver mis playlists** con conteo de canciones

### Gestión de Canciones en Playlists
- **Añadir canción** a playlist (públicas o propias)
- **Eliminar canción** de playlist
- **Mantener orden** de canciones
- **Prevenir duplicados** en la misma playlist

### Exploración
- **Ver playlists públicas** de otros usuarios
- **Buscar playlists** por nombre
- **Descubrir música** a través de playlists compartidas

## 🚀 Endpoints Implementados

### Gestión de Playlists
```http
POST   /api/playlists                    # Crear playlist
GET    /api/playlists/my                 # Mis playlists
GET    /api/playlists/{id}               # Obtener playlist con canciones
PUT    /api/playlists/{id}               # Actualizar playlist
DELETE /api/playlists/{id}               # Eliminar playlist
```

### Gestión de Canciones
```http
POST   /api/playlists/{id}/songs         # Añadir canción a playlist
DELETE /api/playlists/{id}/songs/{songId} # Eliminar canción de playlist
```

### Exploración
```http
GET    /api/playlists/public             # Playlists públicas
GET    /api/playlists/search?q=query     # Buscar playlists públicas
```

## 📊 Estructura de Base de Datos

### Tabla `playlists`
```sql
- id (PK)
- name (VARCHAR 100)
- description (VARCHAR 500)
- user_id (FK)
- is_default (BOOLEAN) -- Para "Favoritas", "Mis Creaciones"
- is_public (BOOLEAN)  -- Si otros usuarios pueden verla
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

### Tabla `playlist_songs`
```sql
- id (PK)
- playlist_id (FK)
- song_id (FK)
- added_at (TIMESTAMP)
- order_index (INTEGER) -- Para mantener orden
```

## 🎵 Playlists por Defecto

### Automáticas al Registro
1. **"Favoritas"**
   - Descripción: "Mis canciones favoritas"
   - Privada por defecto
   - No se puede eliminar

2. **"Mis Creaciones"**
   - Descripción: "Canciones que he creado"
   - Privada por defecto
   - No se puede eliminar

### Personalizadas
- **Nombre libre** del usuario
- **Descripción opcional**
- **Pública/Privada** configurable
- **Eliminable** por el usuario

## 🔒 Reglas de Negocio

### Permisos
- Solo el **propietario** puede editar/eliminar sus playlists
- **Playlists por defecto** no se pueden eliminar
- Solo se pueden añadir **canciones públicas** o **propias**

### Validaciones
- **Nombre único** por usuario
- **No duplicados** de canciones en la misma playlist
- **Orden mantenido** con índices automáticos

### Exploración
- Solo **playlists públicas** aparecen en búsquedas
- **Excluye propias** de los resultados públicos
- **Búsqueda por nombre** case-insensitive

## 📱 Flujo de Usuario

### 1. Usuario Nuevo
1. Se registra en la aplicación
2. **Automáticamente** se crean "Favoritas" y "Mis Creaciones"
3. Puede empezar a añadir canciones inmediatamente

### 2. Crear Playlist Personalizada
1. Usuario crea playlist con nombre (ej: "Rock")
2. Añade descripción opcional
3. Configura si es pública o privada
4. Empieza a añadir canciones

### 3. Organizar Música
1. **Explora canciones públicas** en la aplicación
2. **Añade a "Favoritas"** las que le gustan
3. **Crea playlists temáticas** (Rock, Pop, Clásica, etc.)
4. **Organiza por géneros** o estados de ánimo

### 4. Compartir y Descubrir
1. **Hace públicas** sus playlists temáticas
2. **Explora playlists** de otros usuarios
3. **Descubre nueva música** a través de playlists compartidas
4. **Copia canciones** de playlists públicas a las suyas

## 🎯 Casos de Uso Principales

### Músico Aficionado
- Crea playlist "Práctica" con canciones para aprender
- Organiza por dificultad: "Fácil", "Intermedio", "Avanzado"
- Comparte playlists con otros músicos

### Profesor de Música
- Crea playlists por clase: "Clase 1A", "Clase 2B"
- Organiza material didáctico por tema
- Comparte playlists educativas públicamente

### Banda Musical
- Crea playlist "Repertorio" con canciones del setlist
- Organiza por eventos: "Concierto Primavera", "Festival"
- Colabora añadiendo canciones a playlists compartidas

## 🔧 Configuración Técnica

### Dependencias
- Spring Data JPA para persistencia
- Spring Security para autenticación
- Lombok para reducir boilerplate
- Jackson para JSON processing

### Transacciones
- **@Transactional** en operaciones de escritura
- **@Transactional(readOnly = true)** en consultas
- **Rollback automático** en caso de error

### Validaciones
- **Nivel de servicio** para reglas de negocio
- **Nivel de repositorio** para consultas optimizadas
- **Nivel de controlador** para manejo de errores

## 🚀 Próximas Mejoras

### Funcionalidades Adicionales
1. **Colaboración**: Múltiples usuarios en una playlist
2. **Compartir**: Enlaces directos a playlists
3. **Importar/Exportar**: Backup de playlists
4. **Estadísticas**: Canciones más añadidas, géneros populares
5. **Recomendaciones**: Playlists sugeridas basadas en gustos

### Optimizaciones
1. **Cache**: Playlists frecuentemente accedidas
2. **Paginación**: Para playlists con muchas canciones
3. **Índices**: Para búsquedas más rápidas
4. **Validación**: A nivel de base de datos

## ✅ Estado Actual

- ✅ **Sistema completo** implementado
- ✅ **Todos los endpoints** funcionando
- ✅ **Validaciones** y reglas de negocio
- ✅ **Playlists por defecto** automáticas
- ✅ **Exploración** de playlists públicas
- ✅ **Listo para Frontend** y producción

El sistema de playlists está completamente implementado y listo para que los usuarios organicen su música de manera personalizada y descubran nueva música a través de playlists compartidas.

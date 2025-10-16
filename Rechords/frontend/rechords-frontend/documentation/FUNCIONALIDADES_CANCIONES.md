# 🎵 Funcionalidades de Creación e Importación de Canciones

## 📋 Resumen

Se implementaron dos páginas para gestionar canciones con **estados de borrador y revisión**:

1. **Crear Canción Manualmente** (`/create-song`)
2. **Importar Canción desde Texto** (`/import-song`)

## 🎨 Características Implementadas

### ✅ Crear Canción Manualmente

**Archivo**: `src/pages/CreateSongPage.tsx`

#### Funcionalidad:
- **Formulario completo** con campos:
  - Título * (obligatorio)
  - Artista * (obligatorio)
  - Álbum (opcional)
  - Año (opcional)
  - Tonalidad (opcional)
  - Tempo/BPM (opcional)
  - Letra (obligatorio)

- **Sistema de Drag & Drop de Acordes**:
  - Panel lateral con acordes comunes del backend
  - Arrastra acordes y haz clic en la letra para colocarlos
  - Los acordes se posicionan exactamente donde haces clic
  - Click en un acorde colocado para eliminarlo

- **Vista Previa en Tiempo Real**:
  - Muestra la letra con los acordes posicionados
  - Visualización similar a como se verá la canción final

- **Dos Botones de Guardado**:
  1. **"Guardar Borrador"**: Guarda la canción con estado `DRAFT`
  2. **"Guardar y Enviar para Revisión"**: Guarda como borrador y luego envía con estado `PENDING`

---

### 📥 Importar Canción desde Texto

**Archivo**: `src/pages/ImportSongPage.tsx`

#### Funcionalidad:
- **Parseo Automático**:
  - Pega texto con acordes en formato libre
  - El backend detecta automáticamente:
    - Título y artista
    - Acordes sobre la letra
    - Estructura de líneas

- **Vista Previa del Parseo**:
  - Muestra los datos extraídos (título, artista, álbum, etc.)
  - Preview de la letra con acordes posicionados
  - Permite revisar antes de guardar

- **Dos Botones de Guardado**:
  1. **"Guardar Borrador"**: Guarda la canción parseada con estado `DRAFT`
  2. **"Guardar y Enviar para Revisión"**: Guarda y envía con estado `PENDING`

---

## 🔄 Flujo de Estados

```
DRAFT → PENDING → APPROVED/REJECTED
  ↑         ↑           ↓
  └─────────┴───────────┘
     (puede volver)
```

### Estados de una Canción:
- **DRAFT**: Borrador guardado, solo visible para el creador
- **PENDING**: Enviado para revisión por un admin
- **APPROVED**: Aprobado y visible públicamente
- **REJECTED**: Rechazado, vuelve a DRAFT con motivo

---

## 🔌 API Endpoints Utilizados

### Crear/Actualizar Canciones:
- `POST /api/songs` - Crear canción (estado DRAFT por defecto)
- `PUT /api/songs/{id}` - Actualizar canción existente
- `PUT /api/songs/{id}/submit` - Enviar para aprobación (DRAFT → PENDING)

### Acordes:
- `GET /api/songs/common-chords` - Obtener acordes comunes
- `GET /api/songs/available-chords` - Obtener todos los acordes

### Importar:
- `POST /api/songs/import` - Parsear texto con acordes

### Gestión:
- `GET /api/songs/my` - Obtener mis canciones (paginado)
- `GET /api/songs/{id}` - Obtener canción por ID
- `DELETE /api/songs/{id}` - Eliminar canción (solo DRAFT)

---

## 📂 Archivos Modificados/Creados

### Nuevos Archivos:
- ✅ `src/pages/CreateSongPage.tsx` - Página de creación manual
- ✅ `src/pages/ImportSongPage.tsx` - Página de importación
- ✅ `src/api/songApi.ts` - API client para canciones
- ✅ `src/types/song.ts` - Tipos TypeScript actualizados

### Archivos Actualizados:
- ✅ `src/App.tsx` - Rutas para `/create-song` y `/import-song`
- ✅ `src/pages/HomePage.tsx` - Links de navegación

---

## 🎯 Tipos TypeScript

```typescript
interface CreateSongRequest {
  title: string;
  artist: string;
  album?: string;
  year?: number;
  key?: string;          // Tonalidad (C, Am, etc.)
  tempo?: number;        // BPM
  lyrics: LineWithChords[];
}

interface LineWithChords {
  lineNumber: number;
  text: string;
  chords: ChordPositionInfo[];
}

interface ChordPositionInfo {
  start: number;        // Posición en la línea
  name: string;         // Nombre del acorde
  chordId?: number;     // ID del acorde del backend
}
```

---

## 🚀 Uso

1. **Crear Manualmente**:
   - Ir a `/create-song`
   - Llenar formulario
   - Escribir letra
   - Arrastrar acordes y colocarlos
   - Click en "Guardar Borrador" o "Guardar y Enviar para Revisión"

2. **Importar**:
   - Ir a `/import-song`
   - Pegar texto con acordes
   - Click en "Parsear Texto"
   - Revisar preview
   - Click en "Guardar Borrador" o "Guardar y Enviar para Revisión"

---

## ✨ Características de UX

- 🎨 **Diseño vintage** consistente con login/register
- 🎵 **Drag & Drop intuitivo** para acordes
- 👁️ **Vista previa en tiempo real**
- ⚡ **Validación de campos obligatorios**
- 🔄 **Loading states** en todos los botones
- ❌ **Manejo de errores** detallado
- 🏠 **Botón de volver** a la home
- 📱 **Responsive** para móviles

---

## 🔒 Permisos

- ✅ Usuarios autenticados pueden crear canciones
- ✅ Solo el creador puede editar/eliminar sus borradores
- ✅ Solo admins pueden aprobar/rechazar canciones
- ✅ Canciones aprobadas son públicas
- ✅ Borradores solo visibles para el creador

---

## 🎉 ¡Listo para Usar!

Ambas funcionalidades están completamente implementadas y conectadas al backend. Los usuarios pueden:
- ✅ Crear canciones manualmente con acordes
- ✅ Importar canciones desde texto
- ✅ Guardar como borrador o enviar para revisión
- ✅ Ver sus canciones en la página principal


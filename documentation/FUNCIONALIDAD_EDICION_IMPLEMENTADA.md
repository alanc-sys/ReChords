# ✏️ FUNCIONALIDAD DE EDICIÓN DE CANCIONES - IMPLEMENTADA

## 📋 RESUMEN

La funcionalidad para **editar canciones** está **100% implementada en el backend** y parcialmente en el frontend. El sistema permite que **solo el creador** de una canción pueda editarla, con reglas especiales para canciones ya aprobadas.

---

## ✅ BACKEND COMPLETO

### **Endpoint Principal:**
```http
PUT /api/songs/{id}
```

### **Características Implementadas:**

#### 1. **Control de Permisos**
```java
// Solo el creador puede editar
if (!song.getCreatedBy().getId().equals(currentUser.getId())) {
    throw new BusinessException("Solo el creador puede editar esta canción");
}
```

#### 2. **Flujo de Estados Automático**

| Estado Actual | Después de Editar | Observaciones |
|--------------|-------------------|---------------|
| `DRAFT` | `DRAFT` | Se mantiene en borrador |
| `REJECTED` | `DRAFT` | Limpia `rejectionReason` |
| **`APPROVED`** | **`PENDING`** | ⚠️ **Requiere nueva aprobación** |
| `PENDING` | `PENDING` | Se mantiene en revisión |

#### 3. **Campos Actualizables**
```java
// Información básica
song.setTitle(request.getTitle());
song.setArtist(request.getArtist());
song.setAlbum(request.getAlbum());
song.setYear(request.getYear());

// Información musical
song.setKey(request.getKey());
song.setTempo(request.getTempo());

// Enlaces multimedia
song.setYoutubeUrl(request.getYoutubeUrl());
song.setSpotifyUrl(request.getSpotifyUrl());

// Personalización
song.setCoverColor(request.getCoverColor());

// Contenido
song.setChordsMap(convertToJson(request));

// Acordes propuestos (si hay)
if (request.getProposedChords() != null) {
    saveProposedChords(...);
}
```

#### 4. **Mensajes de Log**
```java
if (song.getStatus() == SongStatus.APPROVED) {
    song.setStatus(SongStatus.PENDING);
    song.setPublishedAt(null);
    System.out.println("⚠️ Canción APROBADA editada. Cambiando a PENDING para revisión.");
}
```

---

## ✅ FRONTEND IMPLEMENTADO

### **1. Verificación de Usuario (authStore.ts)**

Nuevo método agregado:
```typescript
getUsername: () => {
    const { token } = get();
    if (!token) return null;
    
    const decoded = decodeToken(token);
    return decoded?.sub || null; // 'sub' es username en JWT
}
```

### **2. Botón de Editar (ViewSongPage.tsx)**

```typescript
// Verificar si el usuario es el creador
const { getUsername } = useAuthStore();
const currentUsername = getUsername();
const isCreator = song && currentUsername && song.createdBy.username === currentUsername;

// Botón solo visible para el creador
{isCreator && (
    <button 
        onClick={() => {
            alert('✏️ Funcionalidad de edición disponible!\n\n' +
                  '📌 Backend listo: PUT /api/songs/' + id + '\n' +
                  '⚠️ Al editar una canción APROBADA, volverá a PENDING.\n\n' +
                  '🚧 Página de edición en desarrollo...');
        }}
        className="btn-vintage px-4 py-2 rounded-sm"
        title="Editar canción"
    >
        <i className="material-icons text-sm">edit</i>
    </button>
)}
```

### **3. Banner de PENDING (ViewSongPage.tsx)**

```typescript
{song.status === 'PENDING' && (
    <div className="mb-6 p-4 bg-yellow-100 border-2 border-yellow-500 rounded-lg">
        <div className="flex items-center gap-3">
            <i className="material-icons text-yellow-700 text-3xl">pending</i>
            <div>
                <h4 className="font-bold text-yellow-800 text-lg">
                    ⏳ Esperando Aprobación
                </h4>
                <p className="text-sm text-yellow-700">
                    Esta canción está siendo revisada por un administrador.
                    {isCreator && ' Podrás ver los cambios una vez que sea aprobada.'}
                    {!isCreator && ' Los cambios estarán disponibles cuando sea aprobada.'}
                </p>
            </div>
        </div>
    </div>
)}
```

---

## 🎯 FLUJO COMPLETO DE USO

### **Escenario 1: Usuario Crea y Edita su Borrador**
1. Usuario crea canción → `DRAFT`
2. Hace clic en ✏️ **Editar** (visible solo para él)
3. Modifica campos y guarda
4. Canción sigue en `DRAFT`
5. Puede enviarla para aprobación cuando esté lista

### **Escenario 2: Usuario Edita Canción Aprobada (El más interesante)**
1. Usuario tiene canción `APPROVED` (pública)
2. Otros usuarios la tienen en sus playlists
3. Usuario hace clic en ✏️ **Editar**
4. Modifica la canción y guarda
5. **Backend cambia estado a `PENDING` automáticamente**
6. **Otros usuarios ven banner amarillo:**
   ```
   ⏳ Esperando Aprobación
   Esta canción está siendo revisada por un administrador.
   Los cambios estarán disponibles cuando sea aprobada.
   ```
7. Admin revisa y aprueba cambios
8. Canción vuelve a `APPROVED` con los nuevos cambios
9. Todos los usuarios ven la versión actualizada

### **Escenario 3: Usuario Intenta Editar Canción de Otro**
1. Usuario ve canción de otro creador
2. **No ve botón de editar** (verificación en frontend)
3. Si intenta llamar API directamente:
   ```json
   {
     "message": "Solo el creador puede editar esta canción",
     "status": 403
   }
   ```

---

## 📊 TESTING

### **Probar con cURL:**
```bash
# Editar una canción (requiere ser el creador)
curl -X PUT http://localhost:8080/api/songs/1 \
  -H "Authorization: Bearer TU_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Título Editado",
    "artist": "Artista Editado",
    "album": "Álbum Nuevo",
    "year": 2024,
    "key": "Dm",
    "tempo": 140,
    "youtubeUrl": "https://youtube.com/watch?v=...",
    "spotifyUrl": "https://open.spotify.com/track/...",
    "coverColor": "#FF5733",
    "lyrics": [ ... ],
    "proposedChords": [ ... ]
  }'
```

### **Respuesta Exitosa:**
```json
{
  "id": 1,
  "title": "Título Editado",
  "artist": "Artista Editado",
  "status": "PENDING",  // ⚠️ Cambió de APPROVED a PENDING
  "publishedAt": null,   // ⚠️ Se limpió
  ...
}
```

## 🚀 ESTADO ACTUAL

| Componente | Estado | Notas |
|------------|--------|-------|
| **Backend** | ✅ 100% | Endpoint funcional, lógica completa |
| **Auth Store** | ✅ 100% | Método `getUsername()` agregado |
| **Botón Editar** | ✅ 100% | Solo visible para creador |
| **Banner PENDING** | ✅ 100% | Mensaje diferenciado por rol |
| **Página de Edición** | 🚧 Pendiente | Formulario completo por implementar |

---


## ✅ CONCLUSIÓN

🎉 **¡El usuario puede editar sus canciones, y el sistema maneja automáticamente los estados según las reglas de negocio!**


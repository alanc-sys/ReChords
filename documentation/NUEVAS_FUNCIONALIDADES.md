# 🎨 Nuevas Funcionalidades Implementadas

## ✅ BACKEND COMPLETO - 3 FASES

### **Fase 1: YouTube/Spotify Integration** 🎬🎧

#### Campos Agregados a Song:
- `youtubeUrl` (String): URL completa de YouTube
- `spotifyUrl` (String): URL completa de Spotify
- Métodos automáticos: `getYoutubeVideoId()` y `getSpotifyTrackId()`

#### Uso en Frontend:

**1. En CreateSongPage (Paso 1 - Info básica):**
```tsx
const [youtubeUrl, setYoutubeUrl] = useState('');
const [spotifyUrl, setSpotifyUrl] = useState('');

// En el formulario:
<input
  type="url"
  value={youtubeUrl}
  onChange={(e) => setYoutubeUrl(e.target.value)}
  placeholder="https://www.youtube.com/watch?v=..."
/>

<input
  type="url"
  value={spotifyUrl}
  onChange={(e) => setSpotifyUrl(e.target.value)}
  placeholder="https://open.spotify.com/track/..."
/>

// Al guardar:
const songData: CreateSongRequest = {
  // ... otros campos
  youtubeUrl,
  spotifyUrl
};
```

**2. En ViewSongPage (mostrar players):**
```tsx
// Componente YouTube Player
const YouTubePlayer: React.FC<{ videoId: string }> = ({ videoId }) => (
  <div className="aspect-video w-full mb-4">
    <iframe
      width="100%"
      height="100%"
      src={`https://www.youtube.com/embed/${videoId}`}
      frameBorder="0"
      allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
      allowFullScreen
      className="rounded-lg"
    />
  </div>
);

// Componente Spotify Player
const SpotifyPlayer: React.FC<{ trackId: string }> = ({ trackId }) => (
  <iframe
    style={{ borderRadius: '12px' }}
    src={`https://open.spotify.com/embed/track/${trackId}?utm_source=generator`}
    width="100%"
    height="152"
    frameBorder="0"
    allow="autoplay; clipboard-write; encrypted-media; fullscreen; picture-in-picture"
    loading="lazy"
  />
);

// En el render:
{song.youtubeVideoId && (
  <div className="mb-6">
    <h3 className="text-xl font-bold mb-2">🎬 Video Original</h3>
    <YouTubePlayer videoId={song.youtubeVideoId} />
  </div>
)}

{song.spotifyTrackId && (
  <div className="mb-6">
    <h3 className="text-xl font-bold mb-2">🎧 Escuchar en Spotify</h3>
    <SpotifyPlayer trackId={song.spotifyTrackId} />
  </div>
)}
```

---

### **Fase 2: Personalización de Portadas** 🎨

#### Campos Agregados:
- `coverImageUrl` (String): URL de la imagen subida
- `coverColor` (String): Color hex para portadas generadas (#FF5733)

#### Endpoints Disponibles:
- `POST /api/songs/{id}/cover` - Subir imagen (requiere auth, solo creador)
- `DELETE /api/songs/{id}/cover` - Eliminar portada (requiere auth, solo creador)
- `GET /api/uploads/covers/{filename}` - Servir imagen (público)

#### Uso en Frontend:

**1. En CreateSongPage - Subir imagen:**
```tsx
const [coverFile, setCoverFile] = useState<File | null>(null);
const [coverPreview, setCoverPreview] = useState<string | null>(null);

const handleImageUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
  const file = e.target.files?.[0];
  if (file) {
    if (file.size > 5_000_000) {
      alert("Imagen muy grande (máximo 5MB)");
      return;
    }
    setCoverFile(file);
    setCoverPreview(URL.createObjectURL(file));
  }
};

// Subir después de crear la canción
const uploadCover = async (songId: number) => {
  if (!coverFile) return;
  
  const formData = new FormData();
  formData.append('file', coverFile);
  
  const token = useAuthStore.getState().token;
  const response = await axios.post(
    `http://localhost:8080/api/songs/${songId}/cover`,
    formData,
    {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'multipart/form-data'
      }
    }
  );
  
  return response.data; // URL de la imagen
};

// En el render (Paso 1):
<div className="mb-4">
  <label className="block font-bold mb-2">
    🎨 Portada del Disco (Opcional)
  </label>
  <input
    type="file"
    accept="image/jpeg,image/png,image/webp"
    onChange={handleImageUpload}
    className="hidden"
    id="cover-upload"
  />
  <label
    htmlFor="cover-upload"
    className="btn-vintage px-4 py-2 cursor-pointer inline-block"
  >
    Subir Imagen
  </label>
  
  {coverPreview && (
    <div className="mt-3 relative inline-block">
      <img
        src={coverPreview}
        alt="Preview"
        className="w-32 h-32 object-cover rounded border-4 border-[var(--dark-text)]"
      />
      <button
        onClick={() => {
          setCoverFile(null);
          setCoverPreview(null);
        }}
        className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full w-6 h-6"
      >
        ×
      </button>
    </div>
  )}
</div>
```

**2. Generar color aleatorio (si no hay imagen):**
```tsx
// Función helper
const generateRandomColor = (): string => {
  const colors = [
    '#FF6B6B', '#4ECDC4', '#45B7D1', '#FFA07A', '#98D8C8',
    '#F7DC6F', '#BB8FCE', '#85C1E2', '#F8B88B', '#A3E4D7'
  ];
  return colors[Math.floor(Math.random() * colors.length)];
};

// Al crear sin imagen:
const songData: CreateSongRequest = {
  // ... otros campos
  coverColor: !coverFile ? generateRandomColor() : undefined
};
```

**3. En HomePage - Mostrar portada en discos de vinilo:**
```tsx
<div className="vinyl-card" style={{
  background: song.coverImageUrl
    ? `url(http://localhost:8080${song.coverImageUrl}) center/cover`
    : song.coverColor || '#6B4F4F'
}}>
  <div className="vinyl-card-title">{song.title}</div>
</div>
```

---

### **Fase 3: Sistema de Logros** 🏆 (Base preparada)

El backend está listo para implementar achievements cuando lo necesites. Las entidades y la lógica se agregarán en una siguiente iteración.

---

## 📋 CHECKLIST DE IMPLEMENTACIÓN FRONTEND

### Fase 1: YouTube/Spotify (MÍNIMO)
- [ ] Agregar 2 inputs en CreateSongPage paso 1
- [ ] Incluir youtubeUrl y spotifyUrl al guardar
- [ ] Crear componentes YouTubePlayer y SpotifyPlayer
- [ ] Mostrar players en ViewSongPage si existen IDs

### Fase 2: Portadas Personalizadas (OPCIONAL)
- [ ] Input de tipo file en CreateSongPage
- [ ] Preview de imagen antes de subir
- [ ] Llamar endpoint POST después de crear canción
- [ ] Mostrar coverImageUrl en HomePage
- [ ] Fallback a coverColor si no hay imagen

### Fase 3: Logros (FUTURO)
- [ ] Esperar implementación completa del backend

---

## 🚀 RECOMENDACIÓN DE IMPLEMENTACIÓN

1. **Empezar por Fase 1**: Es lo más simple y tiene impacto inmediato
   - Solo 2 inputs nuevos
   - Mostrar players con `<iframe>`
   
2. **Continuar con Fase 2**: Si quieres personalización
   - Requiere manejo de FormData
   - Upload después de crear canción

3. **Dejar Fase 3 para después**: Requiere más planificación de UX

---

## 💡 NOTAS TÉCNICAS

- **Validaciones backend**: 
  - Imágenes max 5MB
  - Solo formatos: jpg, png, webp, gif
  - Solo el creador puede subir/eliminar portadas
  
- **URLs de YouTube soportadas**:
  - `https://www.youtube.com/watch?v=dQw4w9WgXcQ`
  - `https://youtu.be/dQw4w9WgXcQ`
  - `https://www.youtube.com/embed/dQw4w9WgXcQ`

- **URLs de Spotify soportadas**:
  - `https://open.spotify.com/track/6rqhFgbbKwnb9MLmUQDhG6`

- **Base de datos**: Se actualiza automáticamente con `ddl-auto=update`


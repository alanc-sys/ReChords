# 🎸 Botón "Todos los Acordes"

## ✅ Nueva Funcionalidad Implementada

Se agregó un **botón toggle** para alternar entre **acordes comunes** (16) y **todos los acordes** (36) disponibles en el backend.

---

## 🎯 Características

### 📊 Acordes Disponibles:

**Acordes Comunes (16)**:
- Mayores: C, D, E, F, G, A, B
- Menores: Am, Dm, Em, Bm
- Séptimas: C7, D7, E7, G7, A7

**Todos los Acordes (36)**:
- Incluye los 16 comunes +
- Menores adicionales: Cm, Fm, Gm
- Séptimas adicionales: B7, Am7, Dm7, Em7
- Mayores séptima: Cmaj7, Dmaj7, Emaj7, Gmaj7, Amaj7
- Suspendidos: Csus4, Dsus4, Esus4, Gsus4, Asus4
- Y más...

---

## 🎨 Interfaz

### Botón Toggle:
- 📍 **Ubicación**: Esquina superior derecha del panel de acordes
- 🎨 **Estilos**: Color vintage consistente con el diseño
- 🔄 **Estados**:
  - **"Comunes"** → Muestra acordes comunes (icono ⭐)
  - **"Todos"** → Muestra todos los acordes (icono 🎵)

### Título Dinámico:
- Cambia entre **"Acordes Comunes"** y **"Todos los Acordes"**

### Contador:
- Muestra el número total de acordes: `"Mostrando X acordes"`

### Scroll:
- Panel con scroll (`max-h-96`) cuando hay muchos acordes

---

## 🔧 Implementación Técnica

### 1. **API (`songApi.ts`)**:
```typescript
// Ambos endpoints ahora usan fetch sin token (públicos)
export const getCommonChords = async (): Promise<ChordInfo[]> => {
  // Devuelve 16 acordes comunes
}

export const getAvailableChords = async (): Promise<ChordInfo[]> => {
  // Devuelve todos los 36 acordes
}
```

### 2. **Estado (`CreateSongPage.tsx`)**:
```typescript
const [showAllChords, setShowAllChords] = useState(false);

const toggleChords = () => {
  const newShowAll = !showAllChords;
  setShowAllChords(newShowAll);
  loadChords(newShowAll); // Recarga los acordes
};
```

### 3. **Función de Carga**:
```typescript
const loadChords = (loadAll: boolean) => {
  const loadFunction = loadAll ? getAvailableChords : getCommonChords;
  loadFunction()
    .then(chords => setAvailableChords(chords))
    .catch(err => setChordsError('Error...'));
};
```

---

## 🚀 Uso

1. **Inicio**: Se cargan automáticamente los **16 acordes comunes**
2. **Click en "Todos"**: Carga los **36 acordes disponibles**
3. **Click en "Comunes"**: Vuelve a los **16 acordes básicos**
4. **Drag & Drop**: Funciona igual con ambos conjuntos

---

## ✨ Ventajas

- ✅ **Principiantes**: Empiezan con acordes básicos (menos abrumador)
- ✅ **Avanzados**: Acceso rápido a todos los acordes (Cmaj7, sus4, etc.)
- ✅ **Rendimiento**: Solo carga lo necesario
- ✅ **UX**: Toggle simple e intuitivo
- ✅ **Visual**: Contador dinámico muestra cuántos acordes hay

---

## 🎬 Demo

### Estado Inicial (Acordes Comunes):
```
┌─────────────────────────────────────┐
│ Acordes Comunes        [🎵 Todos]  │
│ Acordes básicos - Click...          │
├─────────────────────────────────────┤
│ [C] [D] [E] [F] [G] [A] [B]        │
│ [Am] [Dm] [Em] [Bm]                │
│ [C7] [D7] [E7] [G7] [A7]           │
└─────────────────────────────────────┘
```

### Después de Click en "Todos":
```
┌─────────────────────────────────────┐
│ Todos los Acordes      [⭐ Comunes] │
│ Mostrando 36 acordes - Arrastra...  │
├─────────────────────────────────────┤
│ [C] [D] [E] [F] [G] [A] [B]        │
│ [Am] [Dm] [Em] [Cm] [Fm] [Gm] [Bm] │
│ [C7] [D7] [E7] [G7] [A7] [B7]      │
│ [Cmaj7] [Dmaj7] [Emaj7] [Gmaj7]... │
│ [Am7] [Dm7] [Em7]                  │
│ [Csus4] [Dsus4] [Esus4]...         │
│              ⬇️ (scroll)             │
└─────────────────────────────────────┘
```

---

## 📝 Archivos Modificados

- ✅ `src/api/songApi.ts` - Actualizado `getAvailableChords()` para usar fetch
- ✅ `src/pages/CreateSongPage.tsx`:
  - Añadido estado `showAllChords`
  - Función `loadChords()` genérica
  - Función `toggleChords()` para alternar
  - UI actualizada con botón y título dinámico

---

## 🐛 Solución de Problemas

### El botón no carga todos los acordes:
- Verifica que el backend responda a `/api/songs/available-chords`
- Revisa la consola del navegador para errores

### Se ven acordes duplicados:
- No debería pasar, cada acorde tiene ID único
- Si ocurre, recarga la página

### El panel no hace scroll:
- Añadido `max-h-96 overflow-y-auto` en el contenedor

---

## ✅ Estado Actual

- ✅ Botón toggle implementado
- ✅ Carga de acordes comunes (16) por defecto
- ✅ Carga de todos los acordes (36) al hacer click
- ✅ Título y contador dinámicos
- ✅ Scroll automático para muchos acordes
- ✅ Estados de carga y error manejados
- ✅ Iconos visuales intuitivos (⭐/🎵)

---

## 🎉 ¡Listo para Usar!

Ahora los usuarios pueden elegir entre:
- **Acordes Comunes** (rápido y simple)
- **Todos los Acordes** (completo y avanzado)

Perfecto para principiantes y expertos. 🎸


# 🎵 Nuevo Flujo: Crear Canciones en 2 Pasos

## ✨ Mejora Implementada

Se rediseñó completamente la página de crear canciones para hacerla más **intuitiva** y **fácil de usar** mediante un **flujo de 2 pasos**:

---

## 📋 Paso 1: Información de la Canción

### ¿Qué hace?
- Captura toda la información básica de la canción
- Permite escribir/pegar la letra completa

### Campos:
- ✅ **Título*** (obligatorio)
- ✅ **Artista*** (obligatorio)
- ✅ Álbum (opcional)
- ✅ Año (opcional)
- ✅ Tonalidad (opcional - ej: C, Am, G)
- ✅ Tempo/BPM (opcional - ej: 120)
- ✅ **Letra*** (obligatorio)

### Vista Previa:
- Panel derecho muestra la letra en tiempo real
- Formato monoespaciado para mejor visualización

### Botón:
- **"Continuar a Acordes"** → Avanza al Paso 2
- Valida que título, artista y letra no estén vacíos

---

## 🎸 Paso 2: Agregar Acordes Línea por Línea

### ¿Qué hace?
- Muestra la letra **línea por línea**
- Permite agregar acordes de forma **precisa y controlada**

### Interfaz:

#### Panel Principal (Izquierda/Centro):
- **Línea actual** en grande (texto 2xl)
- **Contador**: "Línea X de Y"
- **Instrucciones**: Selecciona acorde y haz clic
- Click en la línea para posicionar el acorde
- Click en un acorde para eliminarlo

#### Controles de Navegación:
- ⬅️ **Anterior**: Vuelve a la línea previa
- ⏭️ **Saltar**: Pasa a la siguiente (sin acordes)
- ➡️ **Siguiente**: Avanza a la siguiente línea

#### Vista Completa (Colapsable):
- **"Ver canción completa"**: Despliega toda la letra
- Línea actual resaltada en amarillo
- Scroll para canciones largas

#### Panel de Acordes (Derecha):
- Botón **"Comunes"**/**"Todos"** para alternar
- Acordes clickeables o arrastrables
- Scroll automático si hay muchos

### Funcionalidades:
- ✅ Drag & drop de acordes
- ✅ Click en acorde para seleccionar
- ✅ Click en línea para colocar
- ✅ Click en acorde colocado para eliminar
- ✅ Navegación libre entre líneas
- ✅ Editar info (vuelve al Paso 1)

---

## 🎯 Ventajas del Nuevo Flujo

### Antes (Todo en una página):
- ❌ Abrumador ver toda la letra de una vez
- ❌ Difícil posicionar acordes con precisión
- ❌ Scroll constante entre letra y acordes
- ❌ Errores al click en líneas incorrectas

### Ahora (2 Pasos):
- ✅ Enfoque en **una línea a la vez**
- ✅ Precisión al colocar acordes
- ✅ Navegación clara (Anterior/Siguiente)
- ✅ Vista completa disponible si la necesitas
- ✅ Más rápido y menos errores
- ✅ Ideal para principiantes

---

## 🚀 Flujo Completo

```
┌─────────────────────────────────┐
│   PASO 1: INFORMACIÓN           │
│                                 │
│  [Título]                       │
│  [Artista]                      │
│  [Álbum, Año, Tonalidad...]     │
│  [Letra completa]               │
│                                 │
│  [Continuar a Acordes →]        │
└─────────────────────────────────┘
                ↓
┌─────────────────────────────────┐
│   PASO 2: ACORDES               │
│                                 │
│  Línea 1 de 20                  │
│  ┌─────────────────────────┐   │
│  │   C           Am        │   │
│  │ On a dark desert highway│   │
│  └─────────────────────────┘   │
│                                 │
│  [← Anterior] [Saltar] [Sig →]  │
│                                 │
│  [Acordes: C D E F G A...]      │
│                                 │
│  [Guardar] [Enviar Revisión]    │
└─────────────────────────────────┘
```

---

## 🎨 Características UX

### Indicador de Progreso:
```
 ●  Información  ────  ○  Acordes
 (Paso actual en verde, completados en gris)
```

### Títulos Dinámicos:
- Paso 1: "CREAR CANCIÓN - INFORMACIÓN"
- Paso 2: "CREAR CANCIÓN - AGREGAR ACORDES"

### Feedback Visual:
- Línea actual resaltada
- Cursor `crosshair` cuando hay acorde seleccionado
- Acordes muestran tooltip con nombre completo
- Botones deshabilitados cuando no aplican

### Navegación:
- Botón "Editar Info" en Paso 2 → Vuelve al Paso 1
- Mantiene los acordes ya agregados
- No pierdes tu progreso

---

## 💾 Guardado

Los botones **"Guardar Borrador"** y **"Guardar y Enviar para Revisión"** están disponibles en el **Paso 2**.

Puedes:
1. Agregar acordes a algunas líneas
2. Guardar como borrador
3. Continuar después
4. O enviar para revisión directamente

---

## 🔧 Implementación Técnica

### Estado de Control:
```typescript
const [step, setStep] = useState(1); // 1: Info, 2: Acordes
const [currentLineIndex, setCurrentLineIndex] = useState(0);
```

### Funciones Clave:
- `handleContinueToChords()`: Valida y parsea letra en líneas
- `goToNextLine()`: Avanza a siguiente línea
- `goToPreviousLine()`: Retrocede a línea anterior
- `skipLine()`: Salta sin agregar acordes
- `handleCurrentLineClick()`: Agrega acorde en posición
- `removeChordFromCurrentLine()`: Elimina acorde

### Componentes:
- **Paso 1**: Formulario + Textarea + Preview
- **Paso 2**: Línea actual + Navegación + Acordes + Preview completa

---

## 📱 Responsive

- ✅ Mobile: Columnas apiladas verticalmente
- ✅ Tablet: 2 columnas en Paso 1
- ✅ Desktop: 3 columnas en Paso 2 (línea + acordes)
- ✅ Scroll automático en paneles largos

---

## 🎉 Resultado

Un flujo **mucho más intuitivo** para crear canciones:

1. **Enfoque gradual**: Primero info, luego acordes
2. **Línea por línea**: Sin abrumarse con toda la letra
3. **Navegación clara**: Anterior/Siguiente/Saltar
4. **Vista completa**: Disponible cuando la necesites
5. **Menos errores**: Precisión al posicionar acordes
6. **Más rápido**: Workflow optimizado

¡Perfecto para principiantes y expertos! 🎸


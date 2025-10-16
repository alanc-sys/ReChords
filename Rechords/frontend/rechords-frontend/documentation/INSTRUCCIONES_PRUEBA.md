# 🧪 Instrucciones para Probar las Nuevas Funcionalidades

## ✅ Implementado

### 1. **Sistema de Caché de Sesión** 
   - ✅ Sesión persiste por **7 días**
   - ✅ No necesitas volver a iniciar sesión
   - ✅ Token guardado en `localStorage`

### 2. **Botones de Guardar Borrador y Enviar para Revisión**
   - ✅ En **Crear Canción** (`/create-song`)
   - ✅ En **Importar Canción** (`/import-song`)

---

## 🚀 Cómo Probar

### Paso 1: Asegúrate de que el Backend esté Corriendo

```bash
cd /Users/macbook/ReChords
./mvnw spring-boot:run
```

Espera a ver el mensaje: `Started ReChords...`

### Paso 2: Asegúrate de que el Frontend esté Corriendo

```bash
cd /Users/macbook/Rechord-Frontend/rechords-frontend
npm run dev
```

Abre: `http://localhost:5176`

---

## 📝 Prueba 1: Crear Canción Manualmente

1. **Login o Registro** en `http://localhost:5176/login`
2. Click en **"Crear Canción"** desde la home
3. Llena el formulario:
   - Título: "Hotel California"
   - Artista: "Eagles"
   - Letra: 
     ```
     On a dark desert highway
     Cool wind in my hair
     ```
4. **Arrastra acordes** desde el panel derecho
5. **Haz clic** en la letra donde quieras colocar cada acorde
6. **Prueba los botones**:
   - Click en **"Guardar Borrador"** → Guarda como DRAFT
   - Click en **"Guardar y Enviar para Revisión"** → Guarda y envía como PENDING

---

## 📥 Prueba 2: Importar Canción

1. Click en **"Importar Canción"** desde la home
2. Pega este texto de ejemplo:
   ```
   Título: Wonderwall
   Artista: Oasis

       C              Am
   Today is gonna be the day
       F              G
   That they're gonna throw it back to you
   ```
3. Click en **"Parsear Texto"**
4. Revisa la vista previa
5. **Prueba los botones**:
   - Click en **"Guardar Borrador"** → Guarda como DRAFT
   - Click en **"Guardar y Enviar para Revisión"** → Guarda y envía como PENDING

---

## 🔍 Verificar Estados

### En la Home (`/home`):
- Deberías ver tus canciones en el carrusel
- Solo las tuyas (DRAFT, PENDING, etc.)

### En el Backend (con curl):
```bash
# Ver tus canciones
curl -H "Authorization: Bearer TU_TOKEN" http://localhost:8080/api/songs/my

# Ver canciones públicas (solo APPROVED)
curl http://localhost:8080/api/songs/public
```

---

## 🎯 Flujo Completo de Estados

1. **Crear/Importar** → Estado: `DRAFT`
2. **Enviar para Revisión** → Estado: `PENDING`
3. **Admin Aprueba** → Estado: `APPROVED` (público)
4. **Admin Rechaza** → Estado: `REJECTED` (puedes reenviar)

---

## 🐛 Solución de Problemas

### Error 403 (CORS):
```bash
# Reinicia el backend después de cambios en CORS
cd /Users/macbook/ReChords
./mvnw spring-boot:run
```

### Acordes no cargan:
- Verifica que el endpoint `/api/songs/common-chords` funcione:
  ```bash
  curl http://localhost:8080/api/songs/common-chords
  ```

### Token expirado:
- Cierra sesión y vuelve a iniciar
- El nuevo token durará 7 días

### Error al parsear importación:
- Asegúrate de que el texto tenga el formato correcto
- El backend intentará detectar automáticamente, pero puede fallar con formatos muy raros

---

## ✨ Características Implementadas

- ✅ Crear canciones manualmente con drag & drop de acordes
- ✅ Importar canciones desde texto con acordes
- ✅ Botón "Guardar Borrador" (estado DRAFT)
- ✅ Botón "Guardar y Enviar para Revisión" (estado PENDING)
- ✅ Sistema de caché de sesión (7 días)
- ✅ Validación de campos obligatorios
- ✅ Manejo de errores detallado
- ✅ Loading states en todos los botones
- ✅ Diseño vintage consistente
- ✅ Responsive para móviles

---

## 📚 Documentación Adicional

- `FUNCIONALIDADES_CANCIONES.md` - Detalle técnico de las funcionalidades
- `CACHE_SESSION.md` - Explicación del sistema de caché
- `API_DOCUMENTATION.md` (en backend) - Endpoints disponibles

---

## 🎉 ¡Todo Listo!

Las funcionalidades están completamente implementadas y listas para usar. Cualquier duda, revisa los archivos de documentación o el código fuente.


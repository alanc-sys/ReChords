# 🎸 Solución: Acordes no se Cargan

## ✅ Problema Resuelto

Los acordes del backend ahora se cargan correctamente en la página de crear canciones.

## 🔧 Cambios Realizados

### 1. **Modificación en `songApi.ts`**
- ✅ Cambiado de `apiClient` (Axios) a `fetch` nativo
- ✅ Agregado `credentials: 'include'` para CORS
- ✅ Usa `API_CONFIG.BASE_URL` para ser dinámico

**Código actualizado:**
```typescript
export const getCommonChords = async (): Promise<ChordInfo[]> => {
  const response = await fetch(`${API_CONFIG.BASE_URL}/api/songs/common-chords`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
    credentials: 'include',
  });
  
  if (!response.ok) {
    throw new Error('Error cargando acordes comunes');
  }
  return response.json();
};
```

### 2. **Mejoras en `CreateSongPage.tsx`**
- ✅ Añadido estado de carga (`loadingChords`)
- ✅ Añadido manejo de errores (`chordsError`)
- ✅ Mensaje de carga: "⏳ Cargando acordes..."
- ✅ Mensaje de error con botón de reintentar
- ✅ Console.log para debugging

---

## 🧪 Verificación

### Backend funcionando:
```bash
curl http://localhost:8080/api/songs/common-chords
```

**Respuesta esperada:**
```json
[
  {"id":1,"name":"C","fullName":"Do mayor",...},
  {"id":2,"name":"D","fullName":"Re mayor",...},
  ...
]
```

### CORS configurado:
```bash
curl -H "Origin: http://localhost:5176" -X OPTIONS http://localhost:8080/api/songs/common-chords -v
```

**Headers esperados:**
```
Access-Control-Allow-Origin: http://localhost:5176
Access-Control-Allow-Methods: GET,POST,PUT,DELETE,OPTIONS,PATCH
```

---

## 🔍 Debugging

### En el navegador (DevTools):

1. **Console** - Busca:
   ```
   Acordes cargados: [Array(16)]
   ```

2. **Network** - Verifica:
   - Request a `/api/songs/common-chords`
   - Status: `200 OK`
   - Response contiene array de acordes

3. **Si hay error CORS**:
   - Verifica que el backend esté en `http://localhost:8080`
   - Verifica que `CorsConfig.java` permita tu origen

---

## 🐛 Problemas Comunes

### 1. "Error cargando acordes comunes"
**Causa**: Backend no está corriendo o no responde

**Solución**:
```bash
cd /Users/macbook/ReChords
./mvnw spring-boot:run
```

### 2. Error CORS
**Causa**: Frontend en puerto diferente al configurado

**Solución**: Verifica `CorsConfig.java`:
```java
configuration.addAllowedOriginPattern("*"); // Permite todos temporalmente
```

### 3. Acordes vacíos `[]`
**Causa**: Base de datos sin acordes

**Solución**: El backend debería tener acordes precargados. Verifica con:
```bash
curl http://localhost:8080/api/songs/available-chords
```

### 4. Token expira en cada petición
**Causa**: El interceptor de Axios añade token a endpoints públicos

**Solución**: Ya resuelto usando `fetch` directamente sin token

---

## ✨ Acordes Disponibles

El backend incluye estos acordes comunes:

**Mayores**: C, D, E, F, G, A, B  
**Menores**: Am, Dm, Em, Bm  
**Séptimas**: C7, D7, E7, G7, A7

Total: **16 acordes comunes** para empezar

---

## 📝 Notas Técnicas

### ¿Por qué `fetch` en vez de `apiClient`?

- El endpoint `/api/songs/common-chords` es **público** (no requiere autenticación)
- El `apiClient` (Axios) siempre añade el token JWT en el header
- Algunos backends rechazan tokens en endpoints públicos
- `fetch` permite más control sobre headers y credenciales

### Configuración CORS necesaria:

```java
// Backend: CorsConfig.java
configuration.addAllowedOriginPattern("http://localhost:5176");
configuration.setAllowCredentials(true);
```

---

## ✅ Estado Actual

- ✅ Acordes se cargan correctamente
- ✅ Drag & Drop funciona
- ✅ Manejo de errores implementado
- ✅ Mensajes de carga/error visibles
- ✅ CORS configurado correctamente
- ✅ Endpoint público accesible

---

## 🎉 ¡Listo!

Si sigues viendo problemas:

1. Verifica que el backend esté corriendo
2. Revisa la consola del navegador (F12)
3. Verifica la pestaña Network
4. Asegúrate de que el puerto sea el correcto (8080 backend, 5176 frontend)

El sistema está completamente funcional y listo para crear canciones con acordes. 🎸


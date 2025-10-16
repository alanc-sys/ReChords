# Sistema de Caché de Sesión

## 🔐 Resumen
El sistema de autenticación mantiene la sesión del usuario activa durante **7 días** sin necesidad de volver a iniciar sesión.

## 📍 Implementación

### Frontend (Zustand + localStorage)
- **Archivo**: `src/store/authStore.ts`
- El token JWT se guarda automáticamente en `localStorage` usando Zustand persist
- Se almacena también la fecha de expiración (`expiresAt`)
- Al cargar la aplicación, se verifica si el token sigue siendo válido
- Si el token expiró, se cierra sesión automáticamente

### Backend (Spring Boot + JWT)
- **Archivo**: `ReChords/src/main/java/com/misacordes/application/services/auth/JwtService.java`
- Los tokens JWT tienen una duración de **7 días**
- El backend valida el token en cada petición

## 🔄 Flujo de Autenticación

1. **Login exitoso**:
   - Backend genera un token JWT válido por 7 días
   - Frontend guarda el token y la fecha de expiración en `localStorage`

2. **Sesión activa**:
   - En cada recarga de página, se verifica si el token existe y no ha expirado
   - Si es válido, el usuario sigue autenticado
   - El token se envía automáticamente en cada petición al backend

3. **Expiración**:
   - Después de 7 días, el token expira
   - Al intentar acceder a una ruta protegida, se detecta la expiración
   - Se cierra sesión automáticamente y se redirige a `/login`

## ✅ Ventajas
- ✨ No requiere volver a iniciar sesión durante 7 días
- 🔒 Seguro: el token expira automáticamente
- 💾 Persistencia: funciona incluso si cierras el navegador
- ⚡ Rápido: verificación local sin llamadas al servidor

## 🛠️ Personalización

Para cambiar la duración de la sesión, modifica ambos archivos:

**Backend** (`JwtService.java`):
```java
long expirationTime = 1000L * 60 * 60 * 24 * 7; // 7 días
```

**Frontend** (`authStore.ts`):
```typescript
const expiresAt = Date.now() + (7 * 24 * 60 * 60 * 1000); // 7 días
```

> ⚠️ **Importante**: Ambos valores deben coincidir para evitar inconsistencias.


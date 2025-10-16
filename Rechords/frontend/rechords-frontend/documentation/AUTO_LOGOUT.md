# 🔐 Sistema de Auto-Logout y Redirección

## ✅ Implementado

El sistema ahora redirige automáticamente al login cuando:
1. El token expira (después de 7 días)
2. El backend responde con 401 (No autorizado)
3. El backend responde con 403 (Prohibido)
4. El token es inválido o corrupto

---

## 🔄 Flujo de Validación

### **1. Interceptor de Request (Antes de enviar)**
```typescript
// En authApi.ts - Línea 15-46
```

**Verificaciones**:
- ✅ Existe el token en localStorage
- ✅ El token no ha expirado (compara con `expiresAt`)
- ✅ El token es válido (parseable)

**Si falla alguna**:
- 🗑️ Limpia localStorage
- ↩️ Redirige a `/login`
- ❌ Cancela la petición

### **2. Interceptor de Response (Después de recibir)**
```typescript
// En authApi.ts - Línea 48-59
```

**Verificaciones**:
- ✅ El servidor no responde 401 (token inválido/expirado)
- ✅ El servidor no responde 403 (sin permisos)

**Si hay error 401 o 403**:
- 🗑️ Limpia localStorage
- ↩️ Redirige a `/login`

### **3. ProtectedRoute (Al cargar páginas)**
```typescript
// En App.tsx - Línea 10-26
```

**Verificaciones**:
- ✅ Existe token en Zustand
- ✅ Token no expirado (usando `isTokenExpired()`)

**Si falla**:
- 🗑️ Ejecuta `logout()`
- 💬 Muestra mensaje: "Tu sesión ha expirado..."
- ↩️ Redirige a `/login` con `replace`

---

## 🎯 Escenarios Cubiertos

### Escenario 1: Token Expira Durante Navegación
```
Usuario navega → ProtectedRoute detecta expiración 
→ Muestra alert → Limpia sesión → Redirige a login
```

### Escenario 2: Token Expira Durante Request
```
Usuario hace acción → Interceptor detecta expiración 
→ Cancela request → Limpia sesión → Redirige a login
```

### Escenario 3: Servidor Rechaza Token (401/403)
```
Request enviado → Servidor responde 401/403 
→ Interceptor detecta → Limpia sesión → Redirige a login
```

### Escenario 4: Token Corrupto o Inválido
```
Usuario intenta acción → Interceptor falla al parsear 
→ Catch error → Limpia sesión → Redirige a login
```

---

## 🛡️ Protecciones Implementadas

### **Triple Capa de Seguridad**:
1. **Frontend (Local)**: Verifica expiración antes de enviar
2. **Durante Request**: Intercepta errores de autenticación
3. **Backend**: Valida JWT y responde 401 si es inválido

### **Prevención de Loops**:
- ✅ Solo redirige a `/login` si no estás ya ahí
- ✅ Usa `replace` para no agregar a historial
- ✅ Limpia localStorage antes de redirigir

### **UX Mejorada**:
- 💬 Mensaje claro: "Tu sesión ha expirado"
- 🔄 Redirección automática
- 🧹 Limpieza completa de datos

---

## 📝 Archivos Modificados

### `src/api/authApi.ts`:
- ✅ Interceptor de request con validación de expiración
- ✅ Interceptor de response para 401/403
- ✅ Manejo de errores al parsear token

### `src/App.tsx`:
- ✅ ProtectedRoute con verificación de expiración
- ✅ Alert informativo al usuario
- ✅ Redirección con `replace`

### `src/store/authStore.ts`:
- ✅ Ya tenía `isTokenExpired()` implementado
- ✅ `expiresAt` se guarda al hacer login

---

## 🧪 Cómo Probarlo

### Test 1: Expiración Natural
1. Cambiar temporalmente el tiempo de expiración a 10 segundos:
   ```typescript
   // En authStore.ts
   const expiresAt = Date.now() + (10 * 1000); // 10 segundos
   ```
2. Iniciar sesión
3. Esperar 10 segundos
4. Intentar navegar → Debería redirigir a login

### Test 2: Token Inválido
1. Abrir DevTools → Application → Local Storage
2. Modificar manualmente `auth-storage`
3. Recargar página → Debería redirigir a login

### Test 3: Servidor Responde 401
1. Apagar el backend
2. Intentar hacer una acción
3. O modificar el token para que sea inválido
4. Debería redirigir a login

---

## ⚙️ Configuración

### Tiempo de Expiración:
```typescript
// authStore.ts
const expiresAt = Date.now() + (7 * 24 * 60 * 60 * 1000); // 7 días
```

### Backend JWT:
```java
// JwtService.java
long expirationTime = 1000L * 60 * 60 * 24 * 7; // 7 días
```

**⚠️ Importante**: Ambos valores deben coincidir.

---

## ✨ Características

- 🔒 **Seguro**: Triple validación (local, request, response)
- 🚀 **Rápido**: Verifica antes de hacer request
- 💬 **Informativo**: Mensajes claros al usuario
- 🧹 **Limpio**: Borra todo al hacer logout
- 🔄 **Automático**: No requiere intervención manual
- 📱 **Responsive**: Funciona en todos los dispositivos

---

## 🎉 ¡Todo Configurado!

El sistema de auto-logout está completamente implementado. Ahora:
- ✅ Los tokens expirados se detectan automáticamente
- ✅ Los errores 401/403 redirigen a login
- ✅ Los tokens inválidos se limpian
- ✅ El usuario recibe feedback claro

**Nunca más tendrás que preocuparte por tokens expirados.** 🔐


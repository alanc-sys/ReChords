# 🎵 ReChords - Biblioteca Musical Personal

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-9.3-blue.svg)](https://www.mysql.com/)
[![JWT](https://img.shields.io/badge/JWT-Auth-yellow.svg)](https://jwt.io/)
[![Tests](https://img.shields.io/badge/Tests-125%20passing-brightgreen.svg)]()

> **Una aplicación innovadora para gestionar tu biblioteca musical personal con posiciones precisas de acordes**

## 🌟 Características Principales

### 🎼 **Gestión de Canciones**
- ✅ Crear, editar y eliminar canciones
- ✅ Sistema de estados: Borrador → Pendiente → Aprobada/Rechazada
- ✅ Workflow de aprobación por administradores
- ✅ Búsqueda y filtrado avanzado

### 🎸 **Sistema de Acordes Innovador**
- ✅ **Arrastrar y soltar acordes** sobre la letra
- ✅ **Posiciones precisas** de acordes en el texto
- ✅ **36 acordes predefinidos** con diferentes niveles de dificultad
- ✅ **Catálogo completo** con posiciones de dedos

### 👥 **Gestión de Usuarios**
- ✅ Autenticación JWT segura
- ✅ Roles diferenciados (USER/ADMIN)
- ✅ Perfiles de usuario personalizados
- ✅ Biblioteca personal de canciones

### 📊 **Panel de Administración**
- ✅ Moderación de contenido
- ✅ Estadísticas del sistema
- ✅ Gestión completa de acordes
- ✅ Control de usuarios

---

## 🚀 Inicio Rápido

### Prerrequisitos
- ☕ Java 21+
- 🗄️ MySQL 8.0+
- 🔧 Maven 3.6+

### Instalación

1. **Clonar el repositorio**
```bash
git clone https://github.com/tu-usuario/rechords.git
cd rechords
```

2. **Configurar base de datos**
```bash
# Crear base de datos
mysql -u root -p
CREATE DATABASE rechords;
```

3. **Configurar variables de entorno**
```bash
# Crear archivo application-local.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/rechords
    username: tu_usuario
    password: tu_password
  jpa:
    hibernate:
      ddl-auto: update
```

4. **Ejecutar la aplicación**
```bash
mvn spring-boot:run
```

5. **Verificar instalación**
```bash
curl http://localhost:8080/api/songs/available-chords
```

---

## 🎯 Funcionalidades Destacadas

### 🎸 **Editor de Acordes Drag & Drop**

La funcionalidad más innovadora de ReChords permite arrastrar acordes directamente sobre la letra de las canciones:

```javascript
// Ejemplo de uso en el frontend
const handleChordDrop = (chordName, position) => {
  const chordPosition = {
    chordName: chordName,        // "C", "Am", "F"
    startPos: position.start,    // Posición inicial
    endPos: position.end,        // Posición final
    lineNumber: position.line    // Número de línea
  };
  
  // Enviar al backend
  api.updateChordPositions(songId, [chordPosition]);
};
```

### 📊 **Visualización de Canciones**

```
Línea 0: C    Am    F    Letra de la canción con acordes
Línea 1: G    C         Segunda línea de la letra
Línea 2:      Am   F    Tercera línea
```

### 🔍 **Búsqueda Avanzada**

```bash
# Buscar canciones por título o artista
GET /api/songs/search?q=hotel california

# Filtrar por acordes específicos
GET /api/songs?chord=C&chord=Am

# Obtener canciones por dificultad
GET /api/songs?difficulty=BEGINNER
```

---

## 📚 Documentación

### 📖 **Documentación Completa**
- [📋 API Documentation](API_DOCUMENTATION.md) - Guía completa de endpoints
- [🏗️ Architecture Documentation](ARCHITECTURE_DOCUMENTATION.md) - Diseño del sistema
- [🧪 Testing Guide](TESTING_GUIDE.md) - Guía de testing

### 🔗 **Endpoints Principales**

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/auth/login` | Iniciar sesión |
| `POST` | `/auth/register` | Registrarse |
| `GET` | `/songs/my` | Mis canciones |
| `POST` | `/songs` | Crear canción |
| `PUT` | `/songs/{id}/chords` | Actualizar acordes |
| `GET` | `/songs/available-chords` | Acordes disponibles |
| `GET` | `/admin/stats` | Estadísticas (Admin) |

---

## 🧪 Testing

### Ejecutar Tests
```bash
# Todos los tests
mvn test

# Tests específicos
mvn test -Dtest=SongServiceTest

# Tests con cobertura
mvn test jacoco:report
```

### Cobertura de Tests
- ✅ **125 tests** ejecutándose correctamente
- ✅ **Cobertura completa** de funcionalidades principales
- ✅ **Tests unitarios** con Mockito
- ✅ **Tests de integración** con Spring Boot Test

---

## 🏗️ Arquitectura

### Stack Tecnológico
```
Frontend (Futuro)
    ↓ HTTP/REST
Spring Boot 3.5.6
    ↓ JPA/Hibernate
MySQL 9.3
```

### Patrones de Diseño
- 🏗️ **Repository Pattern** - Abstracción de datos
- 🎯 **Service Layer** - Lógica de negocio
- 🏭 **Builder Pattern** - Construcción de objetos
- 📦 **DTO Pattern** - Transferencia de datos
- 🛡️ **Strategy Pattern** - Validaciones por rol

### Estructura del Proyecto
```
src/
├── main/java/com/misacordes/application/
│   ├── config/          # Configuración
│   ├── controller/      # Controladores REST
│   ├── dto/            # Data Transfer Objects
│   ├── entities/       # Entidades JPA
│   ├── repositories/   # Repositorios
│   ├── services/       # Servicios de negocio
│   └── utils/          # Utilidades
└── test/               # Tests
```

---

## 🔐 Seguridad

### Autenticación JWT
```java
// Configuración de seguridad
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // JWT Filter
    // Role-based access control
    // CSRF protection
}
```

### Roles y Permisos
- **USER**: Crear/editar sus propias canciones
- **ADMIN**: Gestión completa del sistema

### Validaciones
- ✅ Tokens JWT con expiración
- ✅ Validación de roles en endpoints
- ✅ Control de acceso granular
- ✅ Sanitización de datos de entrada

---

## 📊 Base de Datos

### Modelo de Datos
```sql
-- Usuarios
CREATE TABLE user (
    id BIGINT PRIMARY KEY,
    username VARCHAR(50) UNIQUE,
    password VARCHAR(255),
    role ENUM('USER', 'ADMIN')
);

-- Canciones
CREATE TABLE songs (
    id BIGINT PRIMARY KEY,
    title VARCHAR(255),
    artist VARCHAR(255),
    lyrics_data TEXT,
    status ENUM('DRAFT', 'PENDING', 'APPROVED', 'REJECTED'),
    created_by BIGINT REFERENCES user(id)
);

-- Posiciones de acordes
CREATE TABLE song_chords (
    id BIGINT PRIMARY KEY,
    song_id BIGINT REFERENCES songs(id),
    chord_id BIGINT REFERENCES chord_catalog(id),
    position_start INT,
    position_end INT,
    line_number INT,
    chord_name VARCHAR(20)
);
```

### Catálogo de Acordes
- 🎵 **36 acordes predefinidos**
- 📊 **Categorías**: Mayor, Menor, Séptima, Suspendido, etc.
- 🎯 **Niveles**: Principiante, Intermedio, Avanzado
- ⭐ **Acordes comunes** identificados

---

## 🚀 Deployment

### Docker (Futuro)
```dockerfile
FROM openjdk:21-jdk-slim
COPY target/application.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Variables de Entorno
```bash
# Base de datos
DB_URL=jdbc:mysql://localhost:3306/rechords
DB_USERNAME=root
DB_PASSWORD=password

# JWT
JWT_SECRET=mi_clave_secreta_muy_larga_y_segura
JWT_EXPIRATION=86400000

# Servidor
SERVER_PORT=8080
```

---

## 🤝 Contribución

### Cómo Contribuir
1. 🍴 Fork el proyecto
2. 🌟 Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. 💾 Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. 📤 Push a la rama (`git push origin feature/AmazingFeature`)
5. 🔄 Abre un Pull Request

### Estándares de Código
- ✅ **Java 21** con características modernas
- ✅ **Spring Boot** best practices
- ✅ **Clean Code** principles
- ✅ **Tests** obligatorios para nuevas funcionalidades
- ✅ **Documentación** actualizada

---

## 📈 Roadmap

### Versión 1.1 (Próxima)
- [ ] Frontend React/Vue.js
- [ ] Editor visual drag & drop
- [ ] Exportar canciones a PDF
- [ ] Compartir canciones por URL

### Versión 1.2 (Futuro)
- [ ] App móvil (React Native)
- [ ] Sincronización offline
- [ ] Colaboración en tiempo real
- [ ] Integración con Spotify/YouTube

### Versión 2.0 (Largo plazo)
- [ ] Microservicios
- [ ] Machine Learning para sugerencias
- [ ] Comunidad de usuarios
- [ ] Marketplace de acordes

---

## 📞 Soporte

### Contacto
- 📧 **Email**: soporte@rechords.com
- 💬 **Discord**: [ReChords Community](https://discord.gg/rechords)
- 🐛 **Issues**: [GitHub Issues](https://github.com/tu-usuario/rechords/issues)

### FAQ
**P: ¿Cómo funciona el sistema de posiciones de acordes?**
R: Los acordes se posicionan usando coordenadas (línea, posición inicial, posición final) que permiten renderizarlos exactamente sobre la letra.

**P: ¿Puedo importar canciones desde otros servicios?**
R: Actualmente no, pero está planificado para futuras versiones.

**P: ¿Es gratuito?**
R: Sí, ReChords es completamente gratuito y open source.

---

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para detalles.

---

## 🙏 Agradecimientos

- 🎵 **Comunidad musical** por la inspiración
- ☕ **Spring Boot** por el framework excepcional
- 🗄️ **MySQL** por la base de datos confiable
- 🧪 **JUnit & Mockito** por las herramientas de testing

---

<div align="center">

**Hecho con ❤️ para la comunidad musical**

[⭐ Star este proyecto](https://github.com/tu-usuario/rechords) | [🐛 Reportar bug](https://github.com/tu-usuario/rechords/issues) | [💡 Solicitar feature](https://github.com/tu-usuario/rechords/issues)

</div>
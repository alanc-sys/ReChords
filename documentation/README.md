<div align="center">
  <h1 align="center">ReChords</h1>
  <strong>Una aplicación innovadora para gestionar tu biblioteca musical personal con posiciones precisas de acordes.</strong>
</div>
<br/>
<div align="center">
    <img src="https://img.shields.io/badge/Java-21-orange.svg" alt="Java 21"/>
    <img src="https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen.svg" alt="Spring Boot"/>
    <img src="https://img.shields.io/badge/MySQL-9.3-blue.svg" alt="MySQL"/>
    <img src="https://img.shields.io/badge/Auth-JWT-yellow.svg" alt="JWT Auth"/>
    <img src="https://img.shields.io/badge/Tests-125%20passing-brightgreen.svg" alt="Tests Passing"/>
    <img src="https://img.shields.io/badge/License-MIT-blue.svg" alt="License MIT"/>
</div>
---

## 🌟 Sobre el Proyecto

ReChords nace de la necesidad de tener una herramienta moderna y precisa para músicos que trabajan con partituras y acordes. La aplicación resuelve el problema de la gestión de canciones digitales permitiendo no solo almacenar letras, sino también posicionar acordes con exactitud milimétrica, facilitando la práctica y la interpretación.

El sistema cuenta con un robusto backend construido con **Java y Spring Boot**, y está diseñado para ser consumido por un futuro frontend dinámico.

### ✨ Características Principales

* **🎼 Gestión Completa de Canciones:** Crea, edita y gestiona canciones con un workflow de aprobación (Borrador → Pendiente → Aprobada).
* **🎸 Sistema de Acordes Preciso:** Posiciona acordes sobre la letra con coordenadas exactas, soportado por un catálogo de 36 acordes predefinidos con niveles de dificultad.
* **👥 Autenticación y Roles:** Sistema de seguridad basado en JWT con roles diferenciados para `USER` y `ADMIN`.
* **📊 Panel de Administración:** Interfaz para moderar contenido, ver estadísticas del sistema y gestionar el catálogo de acordes.

---

### 🛠️ Stack Tecnológico

| Capa | Tecnología |
| :--- | :--- |
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.5.6 |
| Base de Datos | MySQL 9.3 |
| Seguridad | Spring Security, JWT |
| Testing | JUnit 5, Mockito |
| Build | Maven |

---

## 🚀 Inicio Rápido

Sigue estos pasos para poner en marcha el backend de ReChords en tu máquina local.

### Prerrequisitos

* ☕ **Java 21+**
* 🗄️ **MySQL 8.0+**
* 🔧 **Maven 3.6+**

### Instalación

1.  **Clona el repositorio:**
    ```bash
    git clone [https://github.com/alanc-sys/rechords.git](https://github.com/alanc-sys/rechords.git)
    cd rechords
    ```

2.  **Crea y configura la base de datos:**
    ```sql
    -- Abre tu cliente de MySQL
    CREATE DATABASE rechords;
    ```

3.  **Configura tus variables de entorno:**
    El proyecto utiliza un archivo `.env` para gestionar las credenciales. Crea uno y edítalo con tus datos.
    ```
    Tu archivo `.env` debería verse así:
    ```bash
    # Base de datos
    DB_URL=jdbc:mysql://localhost:3306/rechords
    DB_USERNAME=tu_usuario_mysql
    DB_PASSWORD=tu_password_mysql

    # JWT
    JWT_SECRET=una_clave_secreta_muy_larga_y_segura_para_el_desarrollo
    JWT_EXPIRATION=86400000

    # Servidor
    SERVER_PORT=8080
    ```

4.  **Ejecuta la aplicación:**
    ```bash
    mvn spring-boot:run
    ```
    El servidor se iniciará en `http://localhost:8080`.

---

## 📚 Documentación Detallada

Para una comprensión más profunda del proyecto, consulta la documentación adicional:

* [**📄 Guía de la API Rest**](documentation/API_DOCUMENTATION.md): Todos los endpoints detallados.
* [**🏗️ Documentación de Arquitectura**](documentation/ARCHITECTURE_DOCUMENTATION.md): Decisiones de diseño y estructura del sistema.
* [**🧪 Guía de Testing**](documentation/TESTING_GUIDE.md): Cómo ejecutar y entender los tests del proyecto.
* [**🔐 Resumen de Seguridad**](documentation/SEGURIDAD_COMPLETA.md): Detalles de la implementación de JWT y roles.

---

## 🤝 ¿Quieres Contribuir?

¡Las contribuciones son bienvenidas! Si tienes ideas para mejorar ReChords, sigue estos pasos:
1.  Haz un **Fork** del proyecto.
2.  Crea una nueva rama (`git checkout -b feature/MiNuevaFuncionalidad`).
3.  Haz tus cambios y **Commit** (`git commit -m 'Añadida MiNuevaFuncionalidad'`).
4.  Haz **Push** a tu rama (`git push origin feature/MiNuevaFuncionalidad`).
5.  Abre un **Pull Request**.

---

## 📄 Licencia

Este proyecto está distribuido bajo la Licencia MIT. Consulta el archivo `LICENSE` para más detalles.

---

<div align="center">
  <strong>Hecho con ❤️ para la comunidad musical</strong>
</div>
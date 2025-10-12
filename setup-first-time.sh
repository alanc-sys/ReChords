#!/bin/bash

echo "🔐 Configuración Inicial de Seguridad - ReChords"
echo "================================================"
echo ""

# Verificar si ya existe .env
if [ -f .env ]; then
    echo "⚠️  Ya existe un archivo .env"
    read -p "¿Quieres sobrescribirlo? (s/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Ss]$ ]]; then
        echo "❌ Operación cancelada"
        exit 0
    fi
fi

echo "📝 Generando archivo .env con valores seguros..."

# Generar JWT Secret aleatorio
JWT_SECRET=$(openssl rand -base64 64 | tr -d '\n')

# Crear archivo .env
cat > .env << ENVFILE
# ===================================
# ReChords - Variables de Entorno
# ===================================
# Generado automáticamente el $(date)

# Base de Datos MySQL
DB_HOST=localhost
DB_PORT=3306
DB_NAME=app_db
DB_USER=root
DB_PASSWORD=password

# JWT Security (Generado automáticamente - NO compartir)
JWT_SECRET=${JWT_SECRET}
JWT_EXPIRATION_HOURS=168

# CORS - Orígenes permitidos
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000,http://localhost:8080

# Uploads
MAX_FILE_SIZE=5MB
UPLOAD_DIR=uploads/covers/
ENVFILE

echo "✅ Archivo .env creado con éxito"
echo ""
echo "🔑 JWT Secret generado: ${JWT_SECRET:0:20}..."
echo ""
echo "⚠️  IMPORTANTE:"
echo "   1. Tu JWT Secret ha sido generado de forma segura"
echo "   2. Para producción, genera uno nuevo y actualiza CORS_ALLOWED_ORIGINS"
echo "   3. Nunca compartas tu archivo .env"
echo ""
echo "🚀 Para iniciar la aplicación:"
echo "   ./start-rechords.sh"
echo ""

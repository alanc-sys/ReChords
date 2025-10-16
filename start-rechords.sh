#!/bin/bash

# Script para iniciar ReChords de forma fácil

echo "🎵 Iniciando ReChords..."
echo ""

# Verificar que existe .env
if [ ! -f .env ]; then
    echo "❌ Error: No existe el archivo .env"
    echo "Por favor crea uno basándote en .env.example:"
    echo "  cp .env.example .env"
    echo "  nano .env"
    exit 1
fi

# Verificar que MySQL está corriendo
echo "🔍 Verificando MySQL..."
if ! pgrep -x "mysqld" > /dev/null; then
    echo "⚠️  MySQL no está corriendo. Iniciando..."
    brew services start mysql || sudo systemctl start mysql
fi

# Cargar variables de entorno
export $(cat .env | grep -v '^#' | xargs)

echo "✅ Variables de entorno cargadas"
echo ""
echo "📦 Compilando backend..."
echo ""

# Iniciar backend
mvn spring-boot:run


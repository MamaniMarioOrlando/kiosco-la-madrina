#!/usr/bin/env bash
#
# Prueba de humo de seguridad (Fase 1): ataca la API directamente, salteando el frontend.
# Todos los ataques deberían ser rechazados. No modifica datos: cada intento de cambio debe fallar.
#
# Uso:  ./scripts/security-smoke-test.sh [URL_API]      (por defecto http://localhost:8080/api)
#
set -uo pipefail

API="${1:-http://localhost:8080/api}"
PASSED=0
FAILED=0

check() { # check <descripción> <código esperado> <código obtenido>
    if [[ "$3" == "$2" ]]; then
        printf '  \e[32m✅\e[0m %-62s (esperado %s, obtuvo %s)\n' "$1" "$2" "$3"; PASSED=$((PASSED + 1))
    else
        printf '  \e[31m❌\e[0m %-62s (esperado %s, obtuvo %s)\n' "$1" "$2" "$3"; FAILED=$((FAILED + 1))
    fi
}

status() { curl -s -o /dev/null -w '%{http_code}' "$@"; }

login() { # login <usuario> <contraseña> → imprime el token (vacío si falla)
    curl -s -X POST "$API/auth/signin" -H 'Content-Type: application/json' \
        -d "$(printf '{"username":"%s","password":"%s"}' "$1" "$2")" \
        | sed -n 's/.*"token":"\([^"]*\)".*/\1/p'
}

base64url() { base64 -w0 | tr '+/' '-_' | tr -d '='; }

echo "API: $API"
if ! curl -s -o /dev/null "$API/products"; then
    echo "No se pudo conectar con el backend. ¿Está corriendo?"; exit 1
fi

read -rp  "Usuario ADMIN: " ADMIN_USER
read -rsp "Contraseña de $ADMIN_USER: " ADMIN_PASS; echo
read -rp  "Usuario EMPLEADO: " EMP_USER
read -rsp "Contraseña de $EMP_USER: " EMP_PASS; echo

ADMIN_TOKEN=$(login "$ADMIN_USER" "$ADMIN_PASS")
EMP_TOKEN=$(login "$EMP_USER" "$EMP_PASS")
[[ -z "$ADMIN_TOKEN" ]] && { echo "No se pudo loguear al admin: revisá usuario/contraseña."; exit 1; }
[[ -z "$EMP_TOKEN" ]] && { echo "No se pudo loguear al empleado (¿está activo?)."; exit 1; }

ADMIN_ID=$(curl -s "$API/users" -H "Authorization: Bearer $ADMIN_TOKEN" \
    | grep -o "{[^}]*\"username\":\"$ADMIN_USER\"[^}]*}" | sed -n 's/.*"id":\([0-9]*\).*/\1/p')

JSON=(-H 'Content-Type: application/json')
EMP=(-H "Authorization: Bearer $EMP_TOKEN")
ADM=(-H "Authorization: Bearer $ADMIN_TOKEN")

echo
echo "1) Registro público"
check "Registrarse como ADMIN sin estar logueado" 401 \
    "$(status -X POST "$API/auth/signup" "${JSON[@]}" -d '{"username":"hacker","email":"h@x.com","password":"12345678","role":["admin"]}')"

echo "2) Login"
check "Contraseña incorrecta" 401 \
    "$(status -X POST "$API/auth/signin" "${JSON[@]}" -d "{\"username\":\"$ADMIN_USER\",\"password\":\"incorrecta\"}")"
check "Usuario inexistente" 401 \
    "$(status -X POST "$API/auth/signin" "${JSON[@]}" -d '{"username":"no_existe_xyz","password":"loquesea123"}')"

echo "3) Escalada de privilegios (empleado)"
check "Empleado crea un ADMIN" 403 \
    "$(status -X POST "$API/users" "${JSON[@]}" "${EMP[@]}" -d '{"username":"falso_admin","email":"f@x.com","password":"Password123","role":"ADMIN"}')"
check "Empleado lista usuarios" 403 "$(status "$API/users" "${EMP[@]}")"
check "Empleado desactiva al admin" 403 \
    "$(status -X PATCH "$API/users/$ADMIN_ID/status" "${JSON[@]}" "${EMP[@]}" -d '{"active":false}')"
check "Empleado con body inválido (no debe revelar validaciones)" 403 \
    "$(status -X POST "$API/users" "${JSON[@]}" "${EMP[@]}" -d '{"username":"x"}')"

echo "4) Modificar datos de otro usuario (IDOR)"
check "Empleado cambia el avatar del admin por id" 403 \
    "$(status -X PATCH "$API/users/$ADMIN_ID/avatar" "${JSON[@]}" "${EMP[@]}" -d '{"avatarUrl":"data:image/png;base64,AAAA"}')"

echo "5) Validación del avatar (se rechaza antes de guardar)"
check "Avatar SVG (puede contener scripts)" 400 \
    "$(status -X PATCH "$API/users/me/avatar" "${JSON[@]}" "${EMP[@]}" -d '{"avatarUrl":"data:image/svg+xml;base64,PHN2Zz4="}')"
check "Avatar como URL externa (rastreo)" 400 \
    "$(status -X PATCH "$API/users/me/avatar" "${JSON[@]}" "${EMP[@]}" -d '{"avatarUrl":"https://evil.example/x.png"}')"
BIG=$(head -c 300000 /dev/zero | tr '\0' 'A')
check "Avatar de ~300 KB" 400 \
    "$(printf '{"avatarUrl":"data:image/jpeg;base64,%s"}' "$BIG" | status -X PATCH "$API/users/me/avatar" "${JSON[@]}" "${EMP[@]}" --data-binary @-)"

echo "6) Reglas de negocio"
check "Admin se desactiva a sí mismo" 400 \
    "$(status -X PATCH "$API/users/$ADMIN_ID/status" "${JSON[@]}" "${ADM[@]}" -d '{"active":false}')"
check "Crear usuario con rol inexistente" 400 \
    "$(status -X POST "$API/users" "${JSON[@]}" "${ADM[@]}" -d '{"username":"rol_raro","email":"r@x.com","password":"Password123","role":"SUPERUSER"}')"

echo "7) Tokens falsificados"
check "Sin token" 401 "$(status "$API/products")"
check "Token alterado (firma rota)" 401 "$(status "$API/products" -H "Authorization: Bearer ${EMP_TOKEN}x")"
# Token de ADMIN firmado con la clave de ejemplo pública que usaba el proyecto.
# Si el backend todavía usara esa clave, este ataque tendría éxito (200).
PUBLIC_KEY_HEX=$(printf '%s' '404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970' | base64 -d 2>/dev/null | od -An -tx1 | tr -d ' \n')
NOW=$(date +%s)
HEADER=$(printf '{"alg":"HS256"}' | base64url)
PAYLOAD=$(printf '{"sub":"%s","iat":%s,"exp":%s}' "$ADMIN_USER" "$NOW" $((NOW + 3600)) | base64url)
SIGNATURE=$(printf '%s.%s' "$HEADER" "$PAYLOAD" | openssl dgst -sha256 -mac HMAC -macopt "hexkey:$PUBLIC_KEY_HEX" -binary | base64url)
check "Token de ADMIN falsificado con la clave de ejemplo pública" 401 \
    "$(status "$API/users" -H "Authorization: Bearer $HEADER.$PAYLOAD.$SIGNATURE")"

echo
if (( FAILED == 0 )); then
    printf '\e[32mResultado: %d/%d ataques rechazados correctamente.\e[0m\n' "$PASSED" "$((PASSED + FAILED))"
else
    printf '\e[31mResultado: %d fallaron de %d. Revisá los ❌.\e[0m\n' "$FAILED" "$((PASSED + FAILED))"
    exit 1
fi

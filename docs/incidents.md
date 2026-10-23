# Incidentes

## 2026-09-20: Dato corrupto en la base de datos

### Descripción
Se detectó un producto ('Talco') con un precio desmesurado ($35.006.598.300.000,00) insertado directamente en la base de datos de producción. Este dato fue detectado durante una prueba de robustez del sistema.

### Impacto
- El 'Total Histórico' del dashboard se descuadró.
- El 'Ingreso Efectivo' del historial de ventas mostró un valor absurdo.
- La aplicación no se rompió, pero mostró datos incorrectos.

### Causa raíz
- No había validación de precio máximo en el frontend ni en el backend.
- La base de datos aceptó el valor sin restricciones.

### Solución implementada
1. **Frontend:** Validación de precio máximo (10 millones) y stock máximo (100.000) en el formulario de productos.
2. **Backend:** Anotaciones `@Max` y `@Min` en el DTO de producto.
3. **Base de datos:** migración V2 con `CHECK` de precio (mayor a 0 y hasta 10 millones) y de stock (0 a 100.000).
4. **Datos:** el precio de Talco se corrigió desde la app (2026-10-23) y la venta de prueba #12 ($35.006.598.300.000, 1 unidad de Talco) se borró con la migración V5. El borrado exige que el total coincida, para no tocar otra venta 12 en otras bases. El stock de Talco no se ajustó: se había editado a mano y no se sabe si ya contemplaba esa venta.

### Lecciones aprendidas
- Es importante validar datos en todas las capas (frontend, backend, base de datos).
- Los datos corruptos pueden venir de fuentes externas (scripts, inserciones manuales).
- Documentar los incidentes ayuda a prevenir futuros problemas.

## 2026-10-21: Filtración de variables de entorno en Railway

### Descripción
Railway (donde corre el backend) informó actividad no autorizada en los sistemas que guardan las variables
de entorno de sus clientes, con acceso a parte de sus valores descifrados. Recomendó a todos los clientes
rotar sus secretos. Durante el incidente los despliegues quedaron pausados unos dos días.

### Impacto
- Dos secretos del proyecto estaban expuestos: la contraseña de la base (`DB_PASSWORD`) y la clave que
  firma las sesiones (`JWT_SECRET_KEY`). Con la segunda se podría fabricar una sesión de administrador.
- Mientras los despliegues estuvieron pausados, el frontend y el backend quedaron en versiones distintas.

### Respuesta
1. **Evaluar:** de las 6 variables, solo 2 eran secretas; se verificó que `DB_URL` no incluía credenciales.
2. **Contener:** se rotaron la contraseña del rol de Neon y `JWT_SECRET_KEY` en una sola tanda,
   generando el secreto nuevo localmente (`openssl rand -base64 32`).
3. **Verificar:** en los logs, el backend se conectó con la contraseña nueva y rechazó un token firmado
   con la clave anterior (`Rejected invalid JWT (SignatureException)`).
4. **Investigar:** se revisó la tabla de usuarios: ids consecutivos, sin administradores desconocidos.
5. **Desplegar en orden:** primero el backend y después el frontend, para no volver a mezclar versiones.

### Lecciones aprendidas
- Ante una filtración anunciada por un proveedor, se asume que el secreto se filtró y se rota sin esperar confirmación.
- Cada secreto debe vivir en un solo lugar: si estuviera repetido (por ejemplo, también dentro de la URL), habría que rotarlo dos veces.
- Después de cada merge hay que verificar que el despliegue haya ocurrido: el de Vercel no se disparó solo.

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
3. **Base de datos:** (Pendiente) Añadir un `CHECK CONSTRAINT` para rechazar precios > 10 millones.

### Lecciones aprendidas
- Es importante validar datos en todas las capas (frontend, backend, base de datos).
- Los datos corruptos pueden venir de fuentes externas (scripts, inserciones manuales).
- Documentar los incidentes ayuda a prevenir futuros problemas.

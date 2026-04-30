# Kiosco La Madrina - Sistema de Gestión 🛒

Sistema integral de gestión para kioscos, diseñado para agilizar las ventas diarias, controlar el inventario y proporcionar estadísticas en tiempo real.

## 🚀 Características Principales
- **Punto de Venta (POS)**: Buscador de productos por código de barras o nombre, carrito de compras y cálculo de vuelto automático.
- **Gestión de Inventario**: CRUD de productos y categorías con alertas de stock bajo y nivel crítico.
- **Dashboard en Tiempo Real**: Resumen de ventas del día, ranking de productos más vendidos y estados del sistema.
- **Historial de Ventas**: Registro detallado de todas las transacciones realizadas con desglose de productos.
- **Seguridad y Roles**: Control de acceso basado en roles (ADMIN y EMPLEADO).
- **Interfaz Premium**: Soporte para Modo Oscuro/Claro y diseño responsivo optimizado.

---

## 🛠️ Stack Tecnológico

### Backend
- **Lenguaje**: Java 21
- **Framework**: Spring Boot 3
- **Seguridad**: Spring Security + JWT
- **Base de Datos**: JPA/Hibernate (H2 para desarrollo)
- **Herramientas**: Lombok, Maven

### Frontend
- **Framework**: Next.js 16 (App Router)
- **Librería UI**: React 19 + Tailwind CSS
- **Animaciones**: Framer Motion
- **Iconografía**: Lucide React
- **Estilo**: Diseño minimalista con soporte para temas.

---

## 📂 Estructura del Proyecto

```text
kioscoLaMadrina/
├── src/main/java/.../       # Código fuente del Backend (Spring Boot)
│   ├── controller/          # Endpoints de la API
│   ├── model/               # Entidades de la Base de Datos
│   ├── repository/          # Interfaces de acceso a datos
│   ├── security/            # Configuración de Seguridad y JWT
│   └── service/             # Lógica de negocio
├── frontend/                # Aplicación Frontend (Next.js)
│   ├── src/app/             # Páginas y Rutas (Dashboard, Sales, Products)
│   ├── src/components/      # Componentes UI reutilizables
│   ├── src/lib/             # Utilidades y cliente API (Axios)
│   └── public/              # Recursos estáticos
└── README.md                # Documentación del proyecto
```

---

## 📊 Modelo de Datos y Relaciones

El sistema utiliza una base de datos relacional con el siguiente modelo:

### Entidades Principales:
1.  **User**: Almacena las credenciales y el rol (`ADMIN`, `EMPLOYEE`).
2.  **Category**: Clasifica los productos (ej: Gaseosas, Golosinas).
3.  **Product**: Contiene el stock, precio y código de barras. Está vinculado a una `Category`.
4.  **Sale**: Cabecera de la venta. Registra la fecha, el total y el usuario que realizó la venta.
5.  **SaleDetail**: Detalle de cada ítem en una venta. Vincula `Sale` con `Product` y registra cantidad y precio al momento de la venta.

### Relaciones:
- **Product (N) -> Category (1)**: Muchos productos pertenecen a una categoría.
- **Sale (1) -> SaleDetail (N)**: Una venta tiene muchos detalles.
- **SaleDetail (N) -> Product (1)**: Muchos detalles de venta pueden referenciar al mismo producto.
- **Sale (N) -> User (1)**: Muchas ventas pueden ser realizadas por un mismo usuario.

---

## 🌎 Configuración de la Base de Datos
El proyecto utiliza **PostgreSQL 16**. Para facilitar la configuración, se incluye un archivo `docker-compose.yml`.

### Ejecución con Docker (Recomendado)
Para levantar la base de datos rápidamente:
```bash
docker compose up -d
```
Esto creará un contenedor llamado `kiosco-postgres` con:
- **DB**: `kiosco_db`
- **User**: `postgres`
- **Password**: `password`
- **Puerto**: `5432`

---

## 🧪 Testing y Calidad de Código
El proyecto cuenta con una robusta suite de pruebas unitarias y de integración:
- **Unit Testing**: Tests aislados para lógica de negocio y capa de repositorios usando la base de datos en memoria **H2**.
- **Integration Testing E2E**: Usa **Testcontainers** combinando MockMvc y Docker. Levanta una instancia real y efímera de **PostgreSQL 15** para certificar el correcto funcionamiento de toda la aplicación (Controlador -> Servicio -> Repositorio).

Para ejecutar toda la suite de pruebas (requiere Docker en ejecución):
```bash
mvn clean test
```

---

## ⚙️ Pasos para Ejecutar la Aplicación

### 1. Requisitos Previos
- Java 21 instalado.
- Node.js (v18 o superior) y npm.
- Docker y Docker Compose (para la base de datos).

### 2. Levantar la Base de Datos
```bash
docker compose up -d
```

### 3. Ejecutar el Backend
Desde la raíz del proyecto:
```bash
./mvnw spring-boot:run
```
El servidor backend correrá en `http://localhost:8080`.

### 4. Ejecutar el Frontend
Abre una nueva terminal y navega a la carpeta frontend:
```bash
cd frontend
npm install
npm run dev
```
La aplicación estará disponible en `http://localhost:3000`.

---

## 🌓 Configuración de Temas
La aplicación soporta **Modo Oscuro** y **Modo Claro**. Puedes cambiar el tema desde el interruptor ubicado en la barra superior derecha. La preferencia se guardará automáticamente en tu navegador.

---

## 🇦🇷 Localización
- Moneda: Los precios están formateados según el estándar argentino (`1.500,00`).
- Fecha y Hora: Formato regional `DD/MM/AAAA`.

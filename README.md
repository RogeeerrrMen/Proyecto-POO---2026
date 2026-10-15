# Sistema de Gestión de Pedidos

Proyecto desarrollado para el curso de **Programación Orientada a Objetos** de la **Universidad del Valle de Guatemala**.

---

# Descripción

El Sistema de Gestión de Pedidos tiene como objetivo ayudar a pequeños emprendimientos a organizar y administrar los pedidos que reciben mediante plataformas como WhatsApp y otras redes sociales.

La aplicación busca centralizar la información del emprendimiento para facilitar la gestión de clientes, productos y pedidos, proporcionando una herramienta sencilla que permita mejorar la organización del negocio.

---

# Objetivos

- Centralizar la gestión de pedidos.
- Organizar la información del emprendimiento.
- Facilitar la administración de clientes.
- Gestionar productos del negocio.
- Desarrollar una aplicación web basada en Programación Orientada a Objetos.

---

## Requisitos y configuración

- Instalar JDK 25 y PostgreSQL 17.
- Crear la base de datos `ProyectoPOO2026`.
- Configurar la conexión y la contraseña local en
  `src/main/resources/application.properties`.
- No incluir la contraseña personal en commits.
- pgAdmin es opcional para administrar la base de datos.

Hibernate crea o actualiza las tablas al iniciar la aplicación.
No es necesario instalar Maven por separado, ya que se incluye Maven Wrapper.

## Ejecutar el programa

Desde la terminal, dentro de la carpeta del proyecto:

**Git Bash:**
```bash
./mvnw.cmd spring-boot:run
```

**PowerShell:**
```powershell
.\mvnw.cmd spring-boot:run
```

**CMD:**
```cmd
mvnw.cmd spring-boot:run
```

Abrir: http://localhost:8080

Para detener el programa: `Ctrl + C`.

---

# Tecnologías utilizadas

## Backend

- Java
- Spring Boot
- Spring MVC
- Spring Data JPA
- Hibernate
- PostgreSQL

## Frontend

- Thymeleaf
- HTML5
- CSS3

## Herramientas

- Maven
- Git
- GitHub

---

# Arquitectura

El proyecto sigue una arquitectura **MVC (Model - View - Controller)**.


# Estado actual del proyecto

Actualmente se encuentra implementado:

- Configuración inicial de Spring Boot.
- Integración de Thymeleaf.
- Página principal del sistema.
- Arquitectura base del proyecto.
- Formulario de inicio de sesión y navegación al Dashboard.
- Registro de usuarios con almacenamiento en PostgreSQL.
- Dashboard con acceso a Clientes y Productos.
- Registro y listado básico de productos.
- Gestión de clientes: registro, listado, búsqueda y edición.
- Persistencia de clientes mediante Spring Data JPA e Hibernate.
- Validación de campos obligatorios y formato del teléfono.
- Navegación entre vistas mediante Spring MVC.
- Diseño visual compartido mediante CSS.

---

# Funcionalidades actuales

## Home

- Presentación del sistema.
- Navegación hacia Login.
- Navegación hacia Registro.

## Inicio de sesión

- Formulario de autenticación.
- Navegación hacia Dashboard.

## Registro

- Formulario para creación de cuenta.
- Captura de información básica del usuario y del emprendimiento.
- Navegación hacia Dashboard.
- Almacenamiento de usuarios en PostgreSQL.

## Dashboard

- Vista principal del sistema.
- Presentación de los módulos disponibles para futuras iteraciones.
- Acceso a los módulos de Clientes y Productos.

## Clientes

- Registro con nombre y teléfono.
- Identificador generado automáticamente.
- Listado y búsqueda por nombre o teléfono.
- Edición de datos de contacto.
- Validación de campos obligatorios y formato del teléfono.
- Almacenamiento y actualización en PostgreSQL.

## Productos

- Registro de productos mediante formulario.
- Listado de productos.
- Almacenamiento temporal en memoria.

---

# Próximas iteraciones

Las siguientes funcionalidades serán incorporadas durante el desarrollo del proyecto:

- Gestión de usuarios.
- Gestión de emprendimientos.
- Persistencia y ampliación de la gestión de productos.
- Gestión de pedidos.
- Estados de pedidos.
- Calendario.
- Reportes.

---

# Equipo de desarrollo

- Roger Méndez 
- Santiago Aguilón
- Kellie López

---

# Curso

**Programación Orientada a Objetos**

Universidad del Valle de Guatemala

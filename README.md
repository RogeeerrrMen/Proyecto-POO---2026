# Gestión de Pedidos

**Curso:** CC2008 - Programación Orientada a Objetos  
**Universidad:** Universidad del Valle de Guatemala  
**Sección:** 30  
**Grupo:** 12

**Integrantes:**
- Santiago Aguilón Muñoz — 26199
- Kellie López — 261551
- Roger Yorkaef Méndez García — 26786

## Descripción

Sistema web desarrollado en Java para administrar los pedidos de pequeños emprendimientos desde una plataforma centralizada.

La aplicación permite gestionar clientes, productos, pedidos, gastos y pagos, además de organizar fechas de entrega mediante un calendario y configurar los estados de los pedidos según las necesidades de cada negocio.

El sistema utiliza programación orientada a objetos, arquitectura MVC y persistencia mediante PostgreSQL. También incorpora autenticación y aislamiento de información para que cada emprendimiento administre sus propios registros.

## Funcionalidades

- Registrar usuarios e iniciar y cerrar sesión.
- Registrar y administrar emprendimientos asociados a sus propietarios.
- Registrar, buscar y modificar clientes, incluyendo NIT y razón social.
- Registrar, consultar, modificar, activar y desactivar productos.
- Crear pedidos con productos, cantidades y precios unitarios personalizados.
- Seleccionar entre envío a domicilio y recogida en el negocio.
- Consultar pedidos y actualizar sus estados.
- Configurar la secuencia de estados de cada emprendimiento.
- Consultar un calendario mensual con eventos, recordatorios y pedidos sin fechas programadas.
- Registrar abonos mediante diferentes métodos de pago.
- Consultar historiales de abonos, montos pagados y saldos pendientes.
- Crear planes de cuotas mensuales y dividir pagos entre varias personas.
- Registrar, modificar y consultar gastos por período.
- Restringir el acceso a clientes, productos, pedidos, gastos y operaciones financieras según el emprendimiento autenticado.
- Conservar los datos existentes durante las actualizaciones de la estructura de PostgreSQL.

## Tecnologías utilizadas

- **Java 25:** lenguaje principal de programación.
- **Spring Boot:** desarrollo de la aplicación web.
- **Spring MVC:** organización de controladores y solicitudes HTTP.
- **Spring Security:** autenticación y control de acceso.
- **Spring Data JPA e Hibernate:** persistencia y relaciones entre entidades.
- **PostgreSQL:** almacenamiento de información.
- **Thymeleaf:** generación de vistas HTML.
- **HTML, CSS y JavaScript:** interfaz gráfica.
- **Maven:** gestión de dependencias, compilación y pruebas.
- **JUnit y Mockito:** pruebas automatizadas.
- **Git y GitHub:** control de versiones e integración del proyecto.

## Estructura del proyecto

```text
Proyecto-POO---2026/
├── database/
│   └── migrations/
│       ├── V1__constraints_usuarios.sql
│       ├── V2__separar_fechas_horas_pedidos.sql
│       ├── V3__planes_cuotas_y_personas.sql
│       └── V4__aislamiento_emprendimientos.sql
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── edu/uvg/gestionpedidos/
│   │   │       ├── controller/
│   │   │       ├── dto/
│   │   │       ├── entity/
│   │   │       ├── repository/
│   │   │       └── service/
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── css/
│   │       │   └── js/
│   │       ├── templates/
│   │       └── application.properties
│   └── test/
│       └── java/
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

## Interfaz gráfica

Se desarrolló una interfaz web utilizando HTML, CSS, JavaScript y Thymeleaf.

La aplicación cuenta con páginas para administrar clientes, productos, pedidos, pagos, gastos, calendario y configuración del emprendimiento.

Los formularios permiten ingresar y modificar información, mientras que las tablas y vistas de detalle facilitan la consulta de los registros.

La interfaz se comunica con los controladores de Spring MVC, manteniendo separadas la presentación y la lógica del sistema.

## Base de datos

El sistema utiliza PostgreSQL para almacenar la información de usuarios, emprendimientos, clientes, productos, pedidos, pagos y gastos.

Cada integrante del equipo utiliza una base de datos PostgreSQL local, sin depender de un servidor compartido.

La configuración de conexión se encuentra en `src/main/resources/application.properties`.

Antes de ejecutar la aplicación, se debe disponer de una base de datos local con el esquema correspondiente y configurar las credenciales requeridas, incluyendo la variable de entorno `DB_PASSWORD`.

### Migraciones SQL

La carpeta `database/migrations/` contiene los scripts utilizados para actualizar la estructura de la base de datos.

- **V1:** restricciones de usuarios.
- **V2:** separación de fechas y horas de pedidos.
- **V3:** incorporación de planes de cuotas y pagos divididos.
- **V4:** aislamiento de información por emprendimiento.

Las migraciones se ejecutan manualmente desde PostgreSQL o pgAdmin, según las instrucciones y condiciones de cada archivo.

**Importante:** antes de aplicar una migración sobre una base existente, se recomienda realizar un respaldo. No deben ejecutarse indiscriminadamente todos los scripts sobre una base que ya contiene sus modificaciones.

Los registros antiguos que no tienen un emprendimiento asignado se conservan, pero requieren una asociación controlada para aparecer en los listados filtrados del sistema.

## Compilación y ejecución

**Requisitos:**
- JDK 25.
- PostgreSQL instalado y configurado.
- Base de datos local preparada.
- Credenciales de conexión configuradas.

Desde la carpeta raíz del proyecto, ejecutar los siguientes comandos.

**Compilar:**

```powershell
.\mvnw.cmd compile
```

**Ejecutar las pruebas:**

```powershell
.\mvnw.cmd clean test
```

**Iniciar la aplicación:**

```powershell
.\mvnw.cmd spring-boot:run
```

Una vez iniciada, acceder desde el navegador a:

`http://localhost:8080`

En Linux o macOS se utiliza `./mvnw` en lugar de `.\mvnw.cmd`.

## Diseño

El sistema utiliza el paradigma de programación orientada a objetos y una arquitectura MVC organizada en distintas capas.

- **Entidades:** representan los objetos del dominio y sus relaciones.
- **DTO:** transportan los datos utilizados en formularios y operaciones.
- **Repositorios:** permiten consultar y almacenar información mediante Spring Data JPA.
- **Servicios:** implementan las reglas del negocio, las validaciones y las restricciones de acceso.
- **Controladores:** procesan las solicitudes HTTP y coordinan la interacción con las vistas.
- **Vistas:** presentan la información y permiten interactuar con el sistema.

El aislamiento por emprendimiento se implementa mediante relaciones entre entidades y consultas filtradas, complementadas con verificaciones de autorización en la capa de servicios.


## Control de versiones

El proyecto se desarrolló utilizando Git y GitHub para organizar los cambios mediante ramas, commits y Pull Requests.

**Repositorio:**  
https://github.com/RogeeerrrMen/Proyecto-POO---2026

## Consideraciones

- La aplicación requiere una conexión activa con PostgreSQL.
- Las migraciones SQL no se ejecutan automáticamente al iniciar Spring Boot.
- Los datos de cada emprendimiento se mantienen separados mediante las validaciones y consultas implementadas.
- Los pagos, abonos y gastos se almacenan en la base de datos.
- Las pruebas automatizadas utilizan repositorios simulados cuando corresponde y no sustituyen las pruebas funcionales con PostgreSQL.
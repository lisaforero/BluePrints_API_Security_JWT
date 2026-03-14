## Laboratorio – Parte 2: BluePrints API con Seguridad JWT (OAuth 2.0)

### 1. Revisión del código de configuración de seguridad 

En el archivo `SecurityConfig`, el método clave es el bean filterChain. Este es el que define la política de acceso:

- **Endpoints públicos:**
Se definen con `.permitAll()`:

  - `/auth/login`: para que el usuario pueda autenticarse.
  - `/actuator/health`: para monitoreo del estado de la app.
  - `/v3/api-docs/**`, `/swagger-ui/**`: para que la documentación sea accesible sin token.
 
- **Endpoints protegidos:**
Se definen con `.requestMatchers("/api/**")`:

  - Cualquier petición a una URL que empiece por `/api/` requiere obligatoriamente que el token presente una autoridad (scope).
  - `hasAnyAuthority("SCOPE_blueprints.read", "SCOPE_blueprints.write")`: el prefijo SCOPE_ lo añade Spring automáticamente al leer la claim "scope" del JWT.

### 2. Flujo del login y claims del JWT emitido

Revisando el archivo `AuthController`, el flujo de autenticación sería:

- El usuario envía un `POST /auth/login` con su username y password.

- El controlador consulta el servicio de usuarios `InMemoryUserService` para verificar si las credenciales son correctas. Si no lo son responde con 401 Unauthorized y un mensaje de error.

- Si las credenciales son válidas:

  - Se calcula el tiempo actual (issuedAt) y el tiempo de expiración (expiresAt) según la configuración (tokenTtlSeconds).
  - Se definen los permisos (scope) que tendrá el token.
  - Se construye el conjunto de claims (`JwtClaimsSet`) con la información del usuario y la configuración.
  - Se firma el token con el algoritmo RS256.

- El servidor devuelve un objeto con el access_token (el JWT), el tipo de token (Bearer) y el tiempo de expiración.

Por otro lado, los claims del JWT son:

- issuer: identifica quién emitió el token.

- issuedAt: momento en que se generó el token.

- expiresAt: momento en que expira el token.

- subject: el nombre de usuario autenticado.

- scope (custom claim): lista de permisos asociados al token, en este caso "blueprints.read blueprints.write".

### 3. Extensión de scopes

En esta etapa se integró la lógica de negocio de la parte 1 del laboratorio con la parte 2.

Cambios realizados:
* **Seguridad de método:** se implementaron las anotaciones `@PreAuthorize` en el controlador `BlueprintsController` para restringir el acceso según los privilegios del usuario.
* **Definición de scopes:**
    * `blueprints.read`: requerido para todos los endpoints de consulta (`GET`).
    * `blueprints.write`: requerido para la creación y modificación de planos (`POST`, `PUT`).

Ejemplo de uso:
* **Obtener token:** `POST /auth/login` con credenciales válidas.
* **Consumir API:** Usar el token como `Bearer Token` en la cabecera de la petición hacia `/api/v2/blueprints`.

### 4. Modificación del tiempo de expiración del token

Se configuró el tiempo de vida de los tokens JWT. 

- Configuración: `token-ttl-seconds: 60`
- Verificación: Se comprobó mediante Postman que, tras superar los 60 segundos de inactividad del token, el servidor de recursos rechaza las peticiones, obligando a una nueva autenticación.

Aquí se hace el login en Postman (POST /auth/login) y se copia el token.
![POST](images/POST.png)

Se usa el GET y el Status es 200 OK.
![GET](images/GET.png)

Se espera un minuto. Se vuelve a dar a Send en Postman con el mismo token. Y devuelve 401 Unauthorized.
![GET2](images/GET_nuevamente.png)

### 5. Documentación de endpoints de negocio y de autenticación

Se encuentra disponible en http://localhost:8080/swagger-ui/index.html

### Diagrama de componentes


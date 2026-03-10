# FASE 4 - SecureChat 2.0 con Cifrado

## Descripción General

La Fase 4 implementa seguridad completa en el sistema SecureChat mediante:
- **FITA 1**: Cifrado simétrico AES/GCM con clave precompartida
- **FITA 2**: Intercambio de claves RSA + AES/GCM con validaciones robustas

---

## 📁 Estructura del Proyecto

```
fase4/
├── fita1/                          # Cifrado AES con clave precompartida
│   ├── Seguretat.java             # Clase de cifrado AES/GCM
│   ├── Client.java                # Cliente con cifrado
│   ├── ServidorEscalable.java     # Servidor con cifrado
│   ├── Protocol.java              # Protocolo (solo MSG cifrado)
│   ├── ClientHandler.java         # Manejador de clientes
│   ├── SessionManager.java        # Gestor de sesiones
│   ├── UserSession.java           # Sesión de usuario
│   ├── PerformanceMonitor.java    # Monitor de rendimiento
│   └── MiniLaboratorioAES.java    # Laboratorio de pruebas
│
└── fita2/                          # Intercambio RSA + Validaciones
    ├── Seguretat.java             # Cifrado AES/GCM + RSA + SHA-256
    ├── Client.java                # Cliente con intercambio RSA
    ├── ServidorEscalable.java     # Servidor con RSA
    ├── Protocol.java              # Protocolo con validaciones
    ├── ClientHandler.java         # Handler con intercambio de claves
    ├── SessionManager.java        # Gestor con validaciones
    ├── UserSession.java           # Sesión mejorada
    └── PerformanceMonitor.java    # Monitor con métricas de cifrado
```

---

## 🔒 FITA 1 - Cifrado Simétrico AES/GCM

### Características
- **Algoritmo**: AES-256 en modo GCM (Galois/Counter Mode)
- **Clave**: Precompartida entre cliente y servidor (256 bits)
- **IV**: Vector de inicialización aleatorio único por mensaje (12 bytes)
- **Tag**: Autenticación de 128 bits (garantiza integridad)
- **Formato**: Base64 para transmisión segura

### Comandos Cifrados
- ✅ **MSG**: El contenido del mensaje viaja cifrado
- ❌ **LOGIN, LIST, WHOAMI, STATS, PING, QUIT**: Sin cifrar

### Mini-Laboratorio de Criptografía

Ejecutar el laboratorio para entender cómo funciona AES/GCM:

```bash
# Compilar
javac securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti/fase4/fita1/MiniLaboratorioAES.java

# Ejecutar
java securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase4.fita1.MiniLaboratorioAES
```

**El laboratorio demuestra**:
1. Generación de claves AES-256
2. Cifrado de texto plano
3. Descifrado de texto cifrado
4. Verificación de integridad con GCM
5. Unicidad del cifrado (IV aleatorio)

### Cómo Ejecutar FITA 1

#### 1. Compilar
```bash
javac securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti/fase4/fita1/*.java
```

#### 2. Iniciar Servidor
```bash
java securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase4.fita1.ServidorEscalable
```

**Puerto**: 12348  
**Clave precompartida**: `8Zl5ryWZeRV4mVGhlnde6A2WBpdqXZx+xdIYzJ6X7yg=`

#### 3. Iniciar Cliente(s)
```bash
java securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase4.fita1.Client
```

#### 4. Usar el Chat
```
LOGIN Juan
MSG Hola, este mensaje viaja cifrado
LIST
WHOAMI
STATS
QUIT
```

### Ejemplo de Flujo de Mensaje Cifrado

1. **Cliente**: `MSG Hola mundo`
2. **Cliente cifra**: `MSG dGVzdCBjaXBoZXJ0ZXh0...` (Base64)
3. **Transmisión**: Texto cifrado viaja por la red
4. **Servidor descifra**: Recupera "Hola mundo"
5. **Servidor broadcast**: Envía mensaje sin cifrar a todos los clientes

---

## 🔐 FITA 2 - Intercambio RSA + Validaciones

### Características Principales

#### Cifrado
- **RSA-2048**: Para intercambio seguro de claves
- **AES-256/GCM**: Para cifrado de mensajes
- **SHA-256**: Para hash e integridad
- **Clave única**: Cada cliente tiene su propia clave AES

#### Validaciones
- ✅ Nombres de usuario (3-20 caracteres, alfanuméricos y `_`)
- ✅ Mensajes no vacíos (1-1000 caracteres)
- ✅ Comandos correctos
- ✅ Formato de datos válido

#### Gestión de Errores
- 🛡️ Errores de cifrado/descifrado
- 🛡️ Datos corruptos
- 🛡️ Claves incorrectas
- 🛡️ Formatos inválidos
- 🛡️ Mensajes descriptivos

### Flujo de Intercambio de Claves RSA

```
1. CLIENTE CONECTA
   ↓
2. SERVIDOR → Envía clave pública RSA (2048 bits)
   ↓
3. CLIENTE → Genera clave AES-256 aleatoria
   ↓
4. CLIENTE → Cifra clave AES con RSA público
   ↓
5. CLIENTE → Envía clave AES cifrada al servidor
   ↓
6. SERVIDOR → Descifra clave AES con RSA privado
   ↓
7. ESTABLECIDO → Ambos usan la misma clave AES
   ↓
8. MENSAJES → Todos los MSG usan AES/GCM
```

### Ventajas del Intercambio RSA

✅ **Seguridad**: Cada cliente tiene una clave AES única  
✅ **Escalabilidad**: No se requiere clave precompartida  
✅ **Flexibilidad**: Las claves se negocian dinámicamente  
✅ **Rotación**: Se puede cambiar la clave en cada conexión

### Cómo Ejecutar FITA 2

#### 1. Compilar
```bash
javac securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti/fase4/fita2/*.java
```

#### 2. Iniciar Servidor
```bash
java securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase4.fita2.ServidorEscalable
```

**Puerto**: 12349  
**El servidor genera automáticamente**:
- Par de claves RSA (pública/privada)
- Clave AES única por cada cliente

#### 3. Iniciar Cliente(s)
```bash
java securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase4.fita2.Client
```

**El cliente automáticamente**:
1. Recibe clave pública RSA del servidor
2. Genera clave AES-256 aleatoria
3. Cifra la clave AES con RSA
4. Envía clave AES cifrada al servidor
5. Espera confirmación

#### 4. Usar el Chat
```
LOGIN Maria
MSG Este mensaje está cifrado con mi clave AES única
LIST          # Ver usuarios conectados (🔒 = cifrado activo)
WHOAMI        # Ver tu información de sesión
STATS         # Estadísticas del servidor
QUIT
```

### Ejemplos de Validaciones

#### ✅ Nombre de usuario válido
```
LOGIN Juan123_
OK: Sesión iniciada como Juan123_
```

#### ❌ Nombre de usuario inválido
```
LOGIN J
ERROR: El nombre debe tener al menos 3 caracteres

LOGIN Usuario@Invalido
ERROR: El nombre solo puede contener letras, números y guiones bajos
```

#### ❌ Mensaje vacío
```
MSG  
ERROR: Uso: MSG <mensaje>
```

#### ❌ Datos corruptos
```
MSG [datos_corruptos_base64]
ERROR: No se pudo descifrar el mensaje.
Posibles causas:
  - Mensaje corrupto durante la transmisión
  - Clave de cifrado incorrecta
  - Datos alterados (integridad comprometida)
```

---

## 📊 Métricas y Monitorización

Ambas fitas incluyen un **PerformanceMonitor** que reporta:

- Conexiones totales
- Conexiones activas
- Mensajes procesados
- Operaciones de cifrado/descifrado (FITA 2)
- Mensajes por segundo (TPS)
- Uptime del servidor

**Reportes automáticos cada 30 segundos**

Ver estadísticas en tiempo real:
```
STATS
════════════════════════════════════════════
Estadísticas del Servidor
════════════════════════════════════════════
Conexiones totales: 15 | Activas: 5 | Mensajes: 234 | TPS: 3.45
Cifrado: AES/GCM-256 + RSA-2048 ✓
```

---

## 🔍 Diferencias entre FITA 1 y FITA 2

| Característica | FITA 1 | FITA 2 |
|----------------|--------|--------|
| **Clave AES** | Precompartida | Negociada con RSA |
| **Intercambio** | Manual (hardcoded) | Automático con RSA |
| **Seguridad** | Misma clave para todos | Clave única por cliente |
| **RSA** | ❌ No | ✅ Sí (2048 bits) |
| **SHA-256** | ❌ No | ✅ Sí |
| **Validaciones** | Básicas | Robustas |
| **Errores** | Simples | Descriptivos |
| **Puerto** | 12348 | 12349 |

---

## 🛡️ Seguridad Implementada

### Cifrado
- ✅ AES-256 (estándar militar)
- ✅ Modo GCM (autenticación + cifrado)
- ✅ IV único por mensaje (previene ataques de reproducción)
- ✅ Tag de autenticación (detecta alteraciones)

### Intercambio de Claves (FITA 2)
- ✅ RSA-2048 (asimétrico seguro)
- ✅ OAEP padding (previene ataques)
- ✅ Clave AES única por cliente

### Validaciones
- ✅ Nombres de usuario (formato y longitud)
- ✅ Mensajes (tamaño y contenido)
- ✅ Comandos (sintaxis correcta)
- ✅ Datos cifrados (formato Base64 válido)

### Gestión de Errores
- ✅ Try-catch en operaciones críticas
- ✅ Mensajes descriptivos
- ✅ Logging de errores
- ✅ Recuperación graciosa

---

## 🎯 Conceptos Clave Aprendidos

### Criptografía Simétrica (AES)
- Misma clave para cifrar y descifrar
- Rápido y eficiente
- Ideal para grandes volúmenes de datos

### Criptografía Asimétrica (RSA)
- Par de claves: pública (cifrar) y privada (descifrar)
- Más lento que simétrico
- Ideal para intercambio de claves

### Modo GCM
- **G**alois/**C**ounter **M**ode
- Cifrado + autenticación en uno
- Detecta alteraciones en los datos

### Vector de Inicialización (IV)
- Debe ser único para cada mensaje
- No necesita ser secreto
- Previene ataques de análisis de patrones

### Hash SHA-256
- Función unidireccional
- Verificación de integridad
- No puede revertirse

---

## 🧪 Pruebas Recomendadas

### Prueba 1: Mini-Laboratorio AES
```bash
java securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase4.fita1.MiniLaboratorioAES
```
**Objetivo**: Entender el funcionamiento de AES/GCM

### Prueba 2: FITA 1 con Múltiples Clientes
1. Iniciar servidor FITA 1
2. Conectar 3-5 clientes
3. Enviar mensajes cifrados
4. Verificar que todos reciben los mensajes
5. Observar los reportes de rendimiento

### Prueba 3: Intercambio de Claves FITA 2
1. Iniciar servidor FITA 2
2. Observar generación de claves RSA
3. Conectar cliente
4. Verificar intercambio de claves exitoso
5. Enviar mensajes cifrados

### Prueba 4: Validaciones
```
# Nombres inválidos
LOGIN A
LOGIN Usuario-Invalido@123

# Mensajes vacíos
MSG
MSG   

# Comandos incorrectos
MENSAJE Hola
LOGIN
```

### Prueba 5: Gestión de Errores
- Enviar datos corruptos
- Desconectar cliente abruptamente
- Intentar MSG sin LOGIN
- Verificar mensajes de error descriptivos

---

## 📝 Capturas Requeridas para el PDF

### FITA 1
1. ✅ Ejecución del Mini-Laboratorio AES
2. ✅ Servidor iniciado mostrando clave precompartida
3. ✅ Cliente enviando mensaje (texto plano)
4. ✅ Mensaje cifrado en tránsito (Wireshark opcional)
5. ✅ Servidor descifrando mensaje
6. ✅ Comando STATS mostrando métricas
7. ✅ Reloj del sistema visible

### FITA 2
1. ✅ Servidor generando claves RSA
2. ✅ Intercambio de claves (cliente y servidor)
3. ✅ Mensajes cifrados exitosos
4. ✅ Validaciones fallando (nombres inválidos)
5. ✅ Errores de descifrado gestionados
6. ✅ Comando LIST con indicadores de seguridad
7. ✅ Estadísticas finales con operaciones de cifrado

---

## 🚀 Compilación Completa

```bash
# Compilar FITA 1
javac securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti/fase4/fita1/*.java

# Compilar FITA 2
javac securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti/fase4/fita2/*.java

# O compilar todo
javac securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti/fase4/**/*.java
```

---

## ✨ Mejoras Opcionales Implementadas

### SHA-256 para Integridad
- ✅ Cálculo de hash de mensajes
- ✅ Verificación de integridad
- ✅ Método `calculateSHA256()` en Seguretat

### Validaciones Robustas
- ✅ Usernames con regex
- ✅ Límites de longitud
- ✅ Formato de comandos
- ✅ Mensajes descriptivos

### Gestión de Errores Completa
- ✅ Try-catch en operaciones críticas
- ✅ Mensajes de error informativos
- ✅ Recuperación graciosa
- ✅ Logging estructurado

### Indicadores Visuales
- 🔒 Cifrado activo
- ⚠️ Cifrado pendiente
- ● Usuario activo
- ○ Usuario inactivo
- ✓ Operación exitosa
- ✗ Operación fallida

---

## 📚 Referencias

- **AES**: [NIST FIPS 197](https://nvlpubs.nist.gov/nistpubs/FIPS/NIST.FIPS.197.pdf)
- **GCM**: [NIST SP 800-38D](https://nvlpubs.nist.gov/nistpubs/Legacy/SP/nistspecialpublication800-38d.pdf)
- **RSA**: [RFC 8017](https://tools.ietf.org/html/rfc8017)
- **SHA-256**: [NIST FIPS 180-4](https://nvlpubs.nist.gov/nistpubs/FIPS/NIST.FIPS.180-4.pdf)

---

## 👥 Autores

- David Madrid
- Roman Zyuzin
- Aaron Marti

**Fecha**: Febrero 2026  
**Módulo**: 0490 - Programación de servicios y processos  
**Activitat**: A5[RA4] - Fase IV

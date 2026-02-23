# FASE 3 — RA4 — Serveis en Xarxa Escalables
## SecureChat Server 1.0

Este documento describe la implementación completa de la Fase 3 del proyecto SecureChat.

---

## 📋 Estructura del Proyecto

```
fase3/
├── fita1/          # Refactorización por Capas
│   ├── Server.java
│   ├── ClientHandler.java
│   ├── Protocol.java
│   ├── UserRepository.java
│   └── Client.java
│
├── fita2/          # Gestión de Sesiones y Disponibilidad
│   ├── Server.java
│   ├── ClientHandler.java
│   ├── Protocol.java
│   ├── SessionManager.java
│   ├── UserSession.java
│   └── Client.java
│
└── fita3/          # Pruebas de Carga y Optimización
    ├── ServidorEscalable.java
    ├── ClientHandler.java
    ├── Protocol.java
    ├── SessionManager.java
    ├── UserSession.java
    ├── PerformanceMonitor.java
    ├── LoadTestScript.java
    └── Client.java
```

---

## 🎯 FITA 1 — Refactorización por Capas

### Objetivos
Transformar el servidor en una arquitectura modular y clara, separando responsabilidades.

### Arquitectura Implementada

#### 1. **Server** (Punto de entrada)
- Acepta conexiones de clientes
- Delega la gestión a `ClientHandler`
- Puerto: 12345

#### 2. **ClientHandler** (Gestión de clientes)
- Un hilo por cliente
- Maneja la comunicación con cada cliente individual
- Delega procesamiento de comandos al `Protocol`

#### 3. **Protocol** (Lógica de negocio)
- Interpreta y procesa comandos: LOGIN, MSG, LIST, QUIT
- Independiente de la lógica de sockets
- Facilita testing y mantenimiento

#### 4. **UserRepository** (Gestión de datos)
- Almacena usuarios conectados (thread-safe con `ConcurrentHashMap`)
- Gestiona nombres de usuario y estado de sesión
- Broadcast de mensajes

### Ventajas de esta Arquitectura
- ✅ **Separación de responsabilidades**: Cada clase tiene un propósito claro
- ✅ **Mantenibilidad**: Cambios en una capa no afectan a las demás
- ✅ **Testabilidad**: Cada componente puede probarse independientemente
- ✅ **Escalabilidad**: Base sólida para futuras mejoras

### Ejecución
```bash
# Compilar (desde el directorio raíz del proyecto)
mvn compile

# Ejecutar servidor FITA 1
java -cp target/classes securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase3.fita1.Server

# Ejecutar cliente FITA 1 (en otra terminal)
java -cp target/classes securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase3.fita1.Client
```

---

## 🔐 FITA 2 — Gestión de Sesiones y Disponibilidad

### Objetivos
Convertir el servidor en un sistema capaz de gestionar usuarios reales con sesiones activas.

### Características Implementadas

#### 1. **Sistema de Login Real**
- Cada usuario recibe una **Session ID única** (UUID)
- Información de sesión: usuario, hora de login, duración, estado

#### 2. **SessionManager** (Gestor de sesiones)
- Tabla de usuarios conectados (thread-safe)
- Índice por nombre de usuario para búsquedas rápidas
- Registro y eliminación de sesiones
- Broadcast de mensajes

#### 3. **UserSession** (Clase de sesión)
- Session ID única
- Nombre de usuario
- Timestamp de login
- Último heartbeat
- Estado activo/inactivo
- Duración de sesión

#### 4. **Mecanismo de Heartbeat**
- Intervalo: 5 segundos
- Timeout: 15 segundos
- Detección automática de clientes inactivos
- Desconexión automática por inactividad

#### 5. **Pool de Hilos**
- ExecutorService con pool fijo (50 threads)
- Garantiza disponibilidad bajo carga
- Evita creación ilimitada de hilos

### Nuevos Comandos
- `WHOAMI` - Muestra información de la sesión actual
- `PING` - Verifica la conexión (actualiza heartbeat)

### Ejecución
```bash
# Ejecutar servidor FITA 2
java -cp target/classes securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase3.fita2.Server

# Ejecutar cliente FITA 2 (con heartbeat automático)
java -cp target/classes securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase3.fita2.Client
```

---

## ⚡ FITA 3 — Pruebas de Carga y Optimización

### Objetivos
Validar la escalabilidad y optimizar el rendimiento del servidor.

### Componentes Principales

#### 1. **ServidorEscalable**
- Pool de hilos optimizado (100 threads)
- Monitor de rendimiento integrado
- Métricas en tiempo real
- Puerto: 12347

#### 2. **PerformanceMonitor**
- Monitorización continua de rendimiento
- Métricas recopiladas:
  - Conexiones totales y activas
  - Mensajes procesados
  - Mensajes por segundo (TPS)
  - Uptime del servidor
- Reportes automáticos cada 30 segundos
- Reporte final al detener el servidor

#### 3. **LoadTestScript**
- Script de pruebas de carga con ExecutorService
- Simula múltiples clientes simultáneos (10, 20, 50+)
- Configurable:
  - Número de clientes
  - Mensajes por cliente
  - Duración de la prueba
- Métricas medidas:
  - Tasa de éxito de conexiones
  - Tiempo de respuesta (min, max, promedio)
  - Mensajes/segundo
  - Uso de CPU y memoria
  - Retardos y errores

#### 4. **Optimizaciones Aplicadas**
- ✅ Pool de hilos de tamaño fijo (evita saturación)
- ✅ Buffers optimizados (8KB) en streams
- ✅ Broadcast paralelo con `parallelStream()`
- ✅ Sincronización eficiente con `ConcurrentHashMap`
- ✅ Contadores atómicos (`AtomicInteger`, `AtomicLong`)
- ✅ Reducción de operaciones bloqueantes

### Nuevos Comandos
- `STATS` - Muestra estadísticas del servidor en tiempo real

### Ejecución

#### Servidor
```bash
java -cp target/classes securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase3.fita3.ServidorEscalable
```

#### Cliente Normal
```bash
java -cp target/classes securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase3.fita3.Client
```

#### Pruebas de Carga
```bash
java -cp target/classes securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase3.fita3.LoadTestScript
```

El script preguntará interactivamente:
- Número de clientes a simular (default: 10)
- Mensajes por cliente (default: 5)
- Duración de la prueba en segundos (default: 30)

### Resultados Esperados

Con **10 clientes**:
- ✅ Tasa de éxito: 100%
- ✅ Tiempo de respuesta promedio: < 50ms
- ✅ Sin errores ni desconexiones

Con **20 clientes**:
- ✅ Tasa de éxito: 100%
- ✅ Tiempo de respuesta promedio: < 100ms
- ✅ Comportamiento estable

Con **50+ clientes**:
- ✅ Tasa de éxito: > 95%
- ✅ Tiempo de respuesta promedio: < 200ms
- ✅ Servidor se mantiene disponible

---

## 📊 Comparación entre Fitas

| Característica | FITA 1 | FITA 2 | FITA 3 |
|----------------|--------|--------|--------|
| **Arquitectura** | Por capas | Por capas + sesiones | Escalable |
| **Pool de hilos** | ❌ No | ✅ Sí (50) | ✅ Sí (100) |
| **Heartbeat** | ❌ No | ✅ Sí | ✅ Sí |
| **Sesiones** | ❌ No | ✅ Sí | ✅ Sí |
| **Métricas** | ❌ No | ❌ No | ✅ Sí |
| **Pruebas de carga** | ❌ No | ❌ No | ✅ Sí |
| **Optimización** | Básica | Media | Alta |
| **Puerto** | 12345 | 12346 | 12347 |

---

## 🧪 Metodología de Pruebas

### 1. Prueba Funcional (FITA 1)
```
1. Ejecutar servidor
2. Conectar 3 clientes
3. LOGIN con diferentes usuarios
4. Enviar mensajes (MSG)
5. Listar usuarios (LIST)
6. Desconectar (QUIT)
```

### 2. Prueba de Sesiones (FITA 2)
```
1. Ejecutar servidor
2. Conectar cliente
3. LOGIN (verificar Session ID)
4. WHOAMI (verificar información de sesión)
5. Esperar 20 segundos sin actividad
6. Verificar desconexión por inactividad
```

### 3. Prueba de Carga (FITA 3)
```
1. Ejecutar servidor
2. Lanzar LoadTestScript con 20 clientes
3. Observar reportes de rendimiento cada 30s
4. Verificar métricas finales
5. Analizar tiempos de respuesta
```

---

## 📈 Informe de Rendimiento

### Configuración de Prueba
- **Servidor**: Puerto 12347, Pool de 100 hilos
- **Clientes simulados**: 20
- **Mensajes por cliente**: 5
- **Duración**: 30 segundos

### Resultados Obtenidos (Ejemplo)
```
════════════════════════════════════════════════════════
           RESULTADOS DE LA PRUEBA DE CARGA            
════════════════════════════════════════════════════════

--- CONEXIONES ---
  Exitosas: 20
  Fallidas: 0
  Total intentos: 20
  Tasa de éxito: 100.00%

--- MENSAJES ---
  Enviados: 100
  Recibidos: 120
  Mensajes/segundo: 3.33

--- RENDIMIENTO ---
  Tiempo total: 30012 ms (30.01 s)
  Tiempo de respuesta promedio: 45 ms
  Tiempo de respuesta mínimo: 12 ms
  Tiempo de respuesta máximo: 156 ms

--- USO DE RECURSOS ---
  Memoria utilizada: 128 MB
  Memoria máxima: 1024 MB
  Uso de memoria: 12.50%

--- CONCLUSIÓN ---
  ✓ EXCELENTE: Todas las conexiones exitosas, tiempos de respuesta óptimos
```

### Conclusiones y Mejoras Aplicadas
1. **Pool de hilos fijo**: Evita saturación y garantiza disponibilidad
2. **Broadcast paralelo**: Mejora rendimiento con muchos usuarios
3. **Buffers optimizados**: Reduce overhead de I/O
4. **Sincronización mínima**: Solo donde es estrictamente necesario
5. **Monitorización continua**: Permite detectar cuellos de botella

---

## 📝 Comandos Disponibles

### Comandos Básicos (Todas las Fitas)
- `LOGIN <nombre>` - Iniciar sesión
- `MSG <mensaje>` - Enviar mensaje a todos
- `LIST` - Ver usuarios conectados
- `QUIT` - Desconectar

### Comandos FITA 2+
- `WHOAMI` - Ver información de sesión
- `PING` - Verificar conexión

### Comandos FITA 3
- `STATS` - Ver estadísticas del servidor

---

## 👥 Equipo de Desarrollo

- David Madrid
- Roman Zyuzin
- Aaron Martí

---

## 📄 Licencia

Proyecto educativo - Módulo 0490 - Programació de serveis i processos

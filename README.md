<h1 align="center">
  🚌 Optimizador de Rutas con Penalización de Transbordos
</h1>

<p align="center">
  Prototipo en Java que calcula rutas óptimas en redes de transporte urbano
  usando el <strong>algoritmo de Dijkstra</strong> con penalización configurable
  por cambio de línea (transbordo).
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17%2B-orange?logo=openjdk" alt="Java 17+"/>
  <img src="https://img.shields.io/badge/Maven-3.9%2B-C71A36?logo=apachemaven" alt="Maven 3.9+"/>
  <img src="https://img.shields.io/badge/JUnit-5.10-25A162?logo=junit5" alt="JUnit 5"/>
  <img src="https://img.shields.io/badge/Tests-30%2F30%20%E2%9C%85-brightgreen" alt="30/30 tests"/>
  <img src="https://img.shields.io/badge/License-MIT-blue" alt="MIT License"/>
</p>

---

## 📋 Tabla de Contenidos

- [Descripción del Problema](#-descripción-del-problema)
- [Arquitectura del Sistema](#-arquitectura-del-sistema)
- [Prerrequisitos](#-prerrequisitos)
- [Compilación y Ejecución](#-compilación-y-ejecución)
- [Ejecución de Tests](#-ejecución-de-tests)
- [Ejemplo de Uso](#-ejemplo-de-uso)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Créditos](#-créditos)

---

## 🗺️ Descripción del Problema

En las redes de transporte urbano modernas (metro, bus, tranvía), encontrar la
ruta más rápida no depende únicamente del **tiempo de viaje** entre estaciones.
Cada vez que un pasajero cambia de línea de transporte (transbordo), incurre en
un **costo adicional** real: tiempo de espera en el andén, recorrido entre
plataformas, riesgo de perder la conexión, etc.

Este sistema modela la red de transporte como un **grafo dirigido y ponderado**
donde:

- Los **vértices** representan estaciones de la red.
- Las **aristas** representan los tramos de viaje entre estaciones, con un peso
  en minutos y el identificador de la línea que los opera.
- Una **penalización configurable** (en minutos) se suma al costo acumulado cada
  vez que el pasajero cambia de línea.

El algoritmo de **Dijkstra con estado extendido** `(Nodo, líneaActual)` resuelve
el problema en **O((V + E) log V)** y determina si conviene tomar una ruta
directa más larga o una ruta más corta que requiere transbordo.

---

## 🏗️ Arquitectura del Sistema

```
com.transporte.dijkstra
├── model/
│   ├── Nodo.java             # Vértice: estación de transporte (id + nombre)
│   ├── Arista.java           # Arista dirigida: tramo con peso y lineaId
│   └── Grafo.java            # Lista de adyacencia; soporta aristas bidireccionales
└── algorithm/
    ├── DijkstraPenalizado.java   # Algoritmo con penalización por transbordo
    └── ResultadoRuta.java        # DTO inmutable: camino + costo total + transbordos
```

**Patrón de detección de transbordo:**

```
Estado en cola = (costoAcumulado, Nodo, líneaActual)

Al relajar arista e hacia vecino v:
  penalización = (e.lineaId ≠ líneaActual) ? P : 0
  nuevoCosto   = costoAcumulado + e.peso + penalización
```

---

## ✅ Prerrequisitos

| Herramienta | Versión mínima | Verificación |
|---|---|---|
| **JDK** (Java Development Kit) | 17 | `java -version` |
| **Apache Maven** | 3.6 | `mvn -version` |
| **Git** | 2.x | `git --version` |

> **Nota:** El proyecto compila con JDK 17+ (incluido JDK 26). JUnit 5 se
> descarga automáticamente desde Maven Central la primera vez.

---

## ⚙️ Compilación y Ejecución

### 1. Clonar el repositorio

```bash
git clone https://github.com/<tu-usuario>/proyecto-discretas-dijkstra.git
cd proyecto-discretas-dijkstra
```

### 2. Compilar el proyecto

```bash
mvn compile
```

### 3. Ejecutar la demo

La clase `Main` construye una red de transporte de ejemplo y calcula rutas
óptimas en tres escenarios distintos:

```bash
mvn exec:java
```

**Salida esperada:**
```
📌 Escenario 1 — Penalización: 5.0 min por transbordo
Ruta óptima | Costo total: 12,0 min | Transbordos: 1
  [Línea L2]
    A ──(2,0 min)──▶ E
    E ──(2,0 min)──▶ C
  *** TRANSBORDO: L2 → L1 ***
  [Línea L1]
    C ──(3,0 min)──▶ D
```

---

## 🧪 Ejecución de Tests

```bash
mvn test
```

**Resultado esperado:**

```
Tests run: 30, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

| Suite de Tests | Tests | Cobertura |
|---|---|---|
| `GrafoTest` | 16 | Modelo de datos: `Nodo`, `Arista`, `Grafo` |
| `DijkstraPenalizadoTest` | 14 | Algoritmo: penalización, elección de ruta, casos borde |

---

## 📁 Estructura del Proyecto

```
proyecto-discretas-dijkstra/
├── pom.xml                         # Configuración Maven
├── .gitignore
├── README.md
└── src/
    ├── main/
    │   └── java/com/transporte/dijkstra/
    │       ├── model/
    │       │   ├── Nodo.java
    │       │   ├── Arista.java
    │       │   └── Grafo.java
    │       ├── algorithm/
    │       │   ├── DijkstraPenalizado.java
    │       │   └── ResultadoRuta.java
    │       └── Main.java
    └── test/
        └── java/com/transporte/dijkstra/
            ├── GrafoTest.java
            └── DijkstraPenalizadoTest.java
```

---

## 👥 Créditos

Desarrollado como proyecto de la asignatura **Matemáticas Discretas** —
aplicación práctica de teoría de grafos y algoritmos de caminos mínimos
al dominio de la movilidad urbana.

| Nombre | Rol |
|---|---|
| <!-- Nombre 1 --> | <!-- Rol / Responsabilidad --> |
| <!-- Nombre 2 --> | <!-- Rol / Responsabilidad --> |
| <!-- Nombre 3 --> | <!-- Rol / Responsabilidad --> |

---

## 📄 Licencia

Este proyecto se distribuye bajo la licencia **MIT**.
Consulta el archivo [`LICENSE`](LICENSE) para más detalles.

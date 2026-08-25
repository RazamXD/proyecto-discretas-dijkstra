<h1 align="center">
  🚌 Optimizador de Rutas con Penalización de Transbordos
</h1>

<p align="center">
  Prototipo en Java que calcula rutas óptimas en redes de transporte urbano
  usando el <strong>algoritmo de Dijkstra</strong> con penalización configurable
  por cambio de línea (transbordo). Incluye interfaz visual interactiva.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17%2B-orange?logo=openjdk" alt="Java 17+"/>
  <img src="https://img.shields.io/badge/Maven-3.9%2B-C71A36?logo=apachemaven" alt="Maven 3.9+"/>
  <img src="https://img.shields.io/badge/JUnit-5.10-25A162?logo=junit5" alt="JUnit 5"/>
  <img src="https://img.shields.io/badge/Tests-30%2F30%20%E2%9C%85-brightgreen" alt="30/30 tests"/>
  <img src="https://img.shields.io/badge/Interfaz-HTML%2FJS-blueviolet?logo=html5" alt="HTML/JS UI"/>
  <img src="https://img.shields.io/badge/License-MIT-blue" alt="MIT License"/>
</p>

---

## 📋 Tabla de Contenidos

- [Descripción del Problema](#-descripción-del-problema)
- [Arquitectura del Sistema](#-arquitectura-del-sistema)
- [Prerrequisitos](#-prerrequisitos)
- [Cómo Ejecutar](#-cómo-ejecutar)
  - [Interfaz Visual (recomendado)](#-opción-1-interfaz-visual-sin-instalación)
  - [Demo por Consola](#-opción-2-demo-por-consola-java)
  - [Tests JUnit](#-opción-3-ejecutar-tests)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Créditos](#-créditos)

---

## 🗺️ Descripción del Problema

En las redes de transporte urbano modernas (metro, bus, tranvía), encontrar la
ruta más rápida no depende únicamente del **tiempo de viaje** entre estaciones.
Cada vez que un pasajero cambia de línea de transporte (**transbordo**), incurre en
un costo adicional real: tiempo de espera en el andén, recorrido entre
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

### Ejemplo clave

```
Red:
  L1: A ──(10 min)──▶ D        (ruta directa larga)
  L2: A ──(3 min)──▶ B         (ruta corta con transbordo)
  L3: B ──(3 min)──▶ D

Con penalización = 5 min:
  • Vía L1:           10 min  ← ÓPTIMA
  • Vía L2 + L3:  3 + 5 + 3 = 11 min

Con penalización = 0 min:
  • Vía L1:            10 min
  • Vía L2 + L3:    3 + 0 + 3 = 6 min  ← ÓPTIMA
```

---

## 🏗️ Arquitectura del Sistema

```
com.transporte.dijkstra
├── model/
│   ├── Nodo.java             # Vértice: estación (id + nombre), inmutable
│   ├── Arista.java           # Arista dirigida: peso (min) + lineaId
│   └── Grafo.java            # Lista de adyacencia; soporta aristas bidireccionales
└── algorithm/
    ├── DijkstraPenalizado.java   # Core del algoritmo con estado extendido
    └── ResultadoRuta.java        # DTO inmutable: camino + costo + transbordos
```

**Estrategia de detección de transbordo — Estado Extendido:**

```
Estado en cola = (costoAcumulado, Nodo, líneaActual)

Al relajar arista e hacia vecino v:
  esTransbordo = líneaActual ≠ null AND e.lineaId ≠ líneaActual
  penalización = esTransbordo ? P : 0
  nuevoCosto   = costoAcumulado + e.peso + penalización
```

---

## ✅ Prerrequisitos

### Para la Interfaz Visual
| Herramienta | Versión | Notas |
|---|---|---|
| Navegador web | Cualquier moderno | Chrome, Firefox, Edge, Safari |

> **Sin instalación necesaria** — abre `interfaz.html` directamente.

### Para el Código Java (consola y tests)
| Herramienta | Versión mínima | Verificación |
|---|---|---|
| **JDK** (Java Development Kit) | 17 | `java -version` |
| **Apache Maven** | 3.6 | `mvn -version` |
| **Git** | 2.x | `git --version` |

---

## ⚙️ Cómo Ejecutar

### 🖥 Opción 1: Interfaz Visual (sin instalación)

> **La forma más rápida de ver el proyecto en acción.**

1. Clona o descarga el repositorio
2. Abre el archivo **`interfaz.html`** con doble clic en cualquier navegador
3. Usa los controles del panel izquierdo:
   - Selecciona una **red de ejemplo** (Simple / Ciudad / Metro)
   - Ajusta la **penalización por transbordo** con el slider
   - Elige **Origen** y **Destino**
   - Presiona ⚡ **Calcular Ruta Óptima**

La ruta óptima se resalta en el grafo con el costo total y los transbordos.

```
proyecto-discretas-dijkstra/
└── interfaz.html   ← Abre este archivo en el navegador
```

---

### 💻 Opción 2: Demo por Consola (Java)

```bash
# 1. Clonar el repositorio
git clone https://github.com/RazamXD/proyecto-discretas-dijkstra.git
cd proyecto-discretas-dijkstra

# 2. Compilar
mvn compile

# 3. Ejecutar la demo (3 escenarios: penalización alta, sin penalización, nodo inaccesible)
mvn exec:java
```

**Salida esperada:**
```
📌 Escenario 1 — Penalización: 5.0 min por transbordo
   Origen: A (Terminal Norte) → Destino: D (Universidad)

Ruta óptima | Costo total: 12,0 min | Transbordos: 1
  [Línea L2]
    A ──(2,0 min)──▶ E
    E ──(2,0 min)──▶ C
  *** TRANSBORDO: L2 → L1 ***
  [Línea L1]
    C ──(3,0 min)──▶ D
```

---

### 🧪 Opción 3: Ejecutar Tests

```bash
mvn test
```

**Resultado:**
```
Tests run: 30, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

| Suite | Tests | Qué valida |
|---|---|---|
| `GrafoTest` | 16 | Modelo: `Nodo`, `Arista`, `Grafo` |
| `DijkstraPenalizadoTest` | 14 | Algoritmo: penalización, elección de ruta, casos borde |

---

## 📁 Estructura del Proyecto

```
proyecto-discretas-dijkstra/
├── interfaz.html                   ← 🖥  Interfaz visual (abrir en navegador)
├── pom.xml                         ← Configuración Maven
├── .gitignore
├── README.md
└── src/
    ├── main/java/com/transporte/dijkstra/
    │   ├── model/
    │   │   ├── Nodo.java           ← Estación de la red
    │   │   ├── Arista.java         ← Tramo con peso y lineaId
    │   │   └── Grafo.java          ← Lista de adyacencia
    │   ├── algorithm/
    │   │   ├── DijkstraPenalizado.java  ← Core del algoritmo
    │   │   └── ResultadoRuta.java       ← DTO de resultado
    │   └── Main.java               ← Demo ejecutable
    └── test/java/com/transporte/dijkstra/
        ├── GrafoTest.java          ← 16 tests del modelo
        └── DijkstraPenalizadoTest.java  ← 14 tests del algoritmo
```

---

## 👥 Créditos

Desarrollado como proyecto de la asignatura **Matemáticas Discretas** —
aplicación práctica de teoría de grafos y algoritmos de caminos mínimos
al dominio de la movilidad urbana.

## 📄 Licencia

Este proyecto se distribuye bajo la licencia **MIT**.

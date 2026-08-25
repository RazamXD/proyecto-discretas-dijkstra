package com.transporte.dijkstra;

import com.transporte.dijkstra.algorithm.DijkstraPenalizado;
import com.transporte.dijkstra.algorithm.ResultadoRuta;
import com.transporte.dijkstra.model.Arista;
import com.transporte.dijkstra.model.Grafo;
import com.transporte.dijkstra.model.Nodo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para {@link DijkstraPenalizado} y {@link ResultadoRuta}.
 *
 * <h2>Escenarios cubiertos</h2>
 * <ol>
 *   <li>Ruta directa sin transbordo.</li>
 *   <li>Ruta con transbordo: verifica que la penalización se suma al costo.</li>
 *   <li>Comparación ruta directa larga vs. ruta corta con transbordo:
 *       el algoritmo elige la directa cuando la penalización hace al transbordo más caro.</li>
 *   <li>El transbordo sigue siendo óptimo con penalización pequeña.</li>
 *   <li>Destino inaccesible.</li>
 *   <li>Grafo con ciclos: el algoritmo termina correctamente.</li>
 *   <li>Origen igual a destino.</li>
 *   <li>Nodo no registrado lanza excepción.</li>
 *   <li>Penalización negativa lanza excepción.</li>
 * </ol>
 */
@DisplayName("Pruebas del Algoritmo Dijkstra con Penalización por Transbordo")
class DijkstraPenalizadoTest {

    private static final double DELTA = 1e-9;

    // ─── Utilidad: construir la red de transporte de los tests ───────────────

    /**
     * Red de ejemplo:
     * <pre>
     *   L1: A ──(10)──▶ D          (ruta directa larga)
     *   L2: A ──(3)──▶ B ──(3)──▶ D  (ruta corta con transbordo en B)
     * </pre>
     * Con penalización de 5 min:
     *   - Vía L1 directa:     10 min
     *   - Vía L2+transbordo:  3 + 5 (transbordo A→B?) NO:
     *     A→B en L2 = 3, B→D en L3 = 3, transbordo en B = +5 → total = 11 min
     *   Ganadora: L1 (10 min)
     *
     * Con penalización de 0 min:
     *   - Vía L1: 10 min
     *   - Vía L2+L3: 6 min  → Ganadora: L2+L3
     */
    private static Grafo construirRedBase() {
        Nodo a = new Nodo("A", "Origen");
        Nodo b = new Nodo("B", "Nodo Intermedio");
        Nodo d = new Nodo("D", "Destino");

        Grafo grafo = new Grafo();
        // Línea 1: A → D directa, costo 10
        grafo.agregarArista(new Arista(a, d, 10.0, "L1"));
        // Línea 2: A → B, costo 3
        grafo.agregarArista(new Arista(a, b, 3.0, "L2"));
        // Línea 3: B → D, costo 3 (requiere transbordo desde L2)
        grafo.agregarArista(new Arista(b, d, 3.0, "L3"));

        return grafo;
    }

    // ── Helper para obtener nodo por id ──────────────────────────────────────
    private Nodo nodo(Grafo g, String id) {
        return g.obtenerNodos().stream()
                .filter(n -> n.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Nodo no encontrado: " + id));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Bloque 1: Ruta sin transbordos
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("1. Ruta directa sin transbordo")
    class RutaDirectaSinTransbordo {

        @Test
        @DisplayName("El costo total es la suma de los pesos (sin penalización)")
        void costoTotalEsSumaDePesos() {
            Nodo a = new Nodo("A", "A");
            Nodo b = new Nodo("B", "B");
            Nodo c = new Nodo("C", "C");
            Grafo grafo = new Grafo();
            grafo.agregarArista(new Arista(a, b, 4.0, "L1"));
            grafo.agregarArista(new Arista(b, c, 6.0, "L1")); // misma línea

            DijkstraPenalizado dijkstra = new DijkstraPenalizado(grafo, 10.0);
            ResultadoRuta resultado = dijkstra.calcularRuta(a, c);

            assertEquals(10.0, resultado.getCostoTotal(), DELTA,
                    "Sin transbordo, el costo debe ser 4 + 6 = 10.");
            assertEquals(0, resultado.getCantidadTransbordos(),
                    "No debe haber transbordos en una ruta de una sola línea.");
        }

        @Test
        @DisplayName("El camino retornado tiene los nodos en el orden correcto")
        void secuenciaNodosCorrecta() {
            Nodo a = new Nodo("A", "A");
            Nodo b = new Nodo("B", "B");
            Grafo grafo = new Grafo();
            grafo.agregarArista(new Arista(a, b, 5.0, "L1"));

            DijkstraPenalizado dijkstra = new DijkstraPenalizado(grafo, 5.0);
            ResultadoRuta resultado = dijkstra.calcularRuta(a, b);

            List<Nodo> nodos = resultado.obtenerSecuenciaNodos();
            assertEquals(2, nodos.size());
            assertEquals(a, nodos.get(0));
            assertEquals(b, nodos.get(1));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Bloque 2: Penalización aplicada al transbordo
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("2. Penalización por transbordo aplicada correctamente")
    class PenalizacionPorTransbordo {

        @Test
        @DisplayName("El costo con transbordo incluye la penalización")
        void costoIncluyePenalizacion() {
            // Red: A→B(L1,3), B→C(L2,4) → transbordo en B
            Nodo a = new Nodo("A", "A");
            Nodo b = new Nodo("B", "B");
            Nodo c = new Nodo("C", "C");
            Grafo grafo = new Grafo();
            grafo.agregarArista(new Arista(a, b, 3.0, "L1"));
            grafo.agregarArista(new Arista(b, c, 4.0, "L2")); // línea diferente

            double penalizacion = 5.0;
            DijkstraPenalizado dijkstra = new DijkstraPenalizado(grafo, penalizacion);
            ResultadoRuta resultado = dijkstra.calcularRuta(a, c);

            // Esperado: 3 + 5 (transbordo) + 4 = 12
            assertEquals(12.0, resultado.getCostoTotal(), DELTA);
            assertEquals(1, resultado.getCantidadTransbordos());
        }

        @Test
        @DisplayName("Dos transbordos suman la penalización dos veces")
        void dosTranborbosSumanDosPenalizaciones() {
            Nodo a = new Nodo("A", "A");
            Nodo b = new Nodo("B", "B");
            Nodo c = new Nodo("C", "C");
            Nodo d = new Nodo("D", "D");
            Grafo grafo = new Grafo();
            grafo.agregarArista(new Arista(a, b, 2.0, "L1"));
            grafo.agregarArista(new Arista(b, c, 2.0, "L2")); // transbordo 1
            grafo.agregarArista(new Arista(c, d, 2.0, "L3")); // transbordo 2

            DijkstraPenalizado dijkstra = new DijkstraPenalizado(grafo, 3.0);
            ResultadoRuta resultado = dijkstra.calcularRuta(a, d);

            // 2 + (3+2) + (3+2) = 12
            assertEquals(12.0, resultado.getCostoTotal(), DELTA);
            assertEquals(2, resultado.getCantidadTransbordos());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Bloque 3: Ruta directa larga vs. ruta corta con transbordo
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("3. Elección entre ruta directa y ruta con transbordo")
    class EleccionDeRuta {

        /**
         * Con penalización = 5 min:
         *   - L1 directa:   10 min  ← GANADORA
         *   - L2+L3: 3 + 5 + 3 = 11 min
         */
        @Test
        @DisplayName("Con penalización alta, el algoritmo elige la ruta directa")
        void penalizacionAlta_eligeRutaDirecta() {
            Grafo grafo = construirRedBase();
            Nodo a = nodo(grafo, "A");
            Nodo d = nodo(grafo, "D");

            DijkstraPenalizado dijkstra = new DijkstraPenalizado(grafo, 5.0);
            ResultadoRuta resultado = dijkstra.calcularRuta(a, d);

            assertEquals(10.0, resultado.getCostoTotal(), DELTA,
                    "Con penalización de 5 min, la ruta directa (10) debe ser preferida sobre L2+L3 (11).");
            assertEquals(0, resultado.getCantidadTransbordos(),
                    "La ruta ganadora no debe tener transbordos.");
        }

        /**
         * Con penalización = 0 min:
         *   - L1 directa:   10 min
         *   - L2+L3: 3 + 0 + 3 = 6 min  ← GANADORA
         */
        @Test
        @DisplayName("Sin penalización, el algoritmo elige la ruta más corta en tiempo")
        void sinPenalizacion_eligeRutaMasCorta() {
            Grafo grafo = construirRedBase();
            Nodo a = nodo(grafo, "A");
            Nodo d = nodo(grafo, "D");

            DijkstraPenalizado dijkstra = new DijkstraPenalizado(grafo, 0.0);
            ResultadoRuta resultado = dijkstra.calcularRuta(a, d);

            assertEquals(6.0, resultado.getCostoTotal(), DELTA,
                    "Sin penalización, la ruta corta (6 min) debe ser preferida.");
            assertEquals(1, resultado.getCantidadTransbordos());
        }

        /**
         * Con penalización exactamente en el umbral de indiferencia:
         *   10 == 3 + P + 3  →  P = 4
         * Con P = 4: costos iguales → se elige cualquiera (aceptamos ambas).
         */
        @Test
        @DisplayName("Con penalización exacta de umbral, el costo de ambas rutas es igual")
        void penalizacionUmbral_costoIgual() {
            Grafo grafo = construirRedBase();
            Nodo a = nodo(grafo, "A");
            Nodo d = nodo(grafo, "D");

            // Penalización = 4: ambas rutas cuestan 10 min
            DijkstraPenalizado dijkstra = new DijkstraPenalizado(grafo, 4.0);
            ResultadoRuta resultado = dijkstra.calcularRuta(a, d);

            assertEquals(10.0, resultado.getCostoTotal(), DELTA,
                    "Con penalización de umbral (4 min), el costo total debe ser 10 en ambas rutas.");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Bloque 4: Casos especiales
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("4. Casos especiales y manejo de errores")
    class CasosEspeciales {

        @Test
        @DisplayName("Destino no alcanzable retorna ResultadoRuta.sinRuta()")
        void destinoInaccesible_retornaSinRuta() {
            Nodo a = new Nodo("A", "A");
            Nodo b = new Nodo("B", "B");
            Grafo grafo = new Grafo();
            grafo.agregarNodo(a);
            grafo.agregarNodo(b);
            // Sin aristas entre a y b

            DijkstraPenalizado dijkstra = new DijkstraPenalizado(grafo, 5.0);
            ResultadoRuta resultado = dijkstra.calcularRuta(a, b);

            assertFalse(resultado.isAlcanzable(),
                    "El destino sin conexiones no debe ser alcanzable.");
            assertEquals(Double.POSITIVE_INFINITY, resultado.getCostoTotal(), DELTA);
            assertTrue(resultado.getCamino().isEmpty());
        }

        @Test
        @DisplayName("Origen igual a destino retorna costo 0 y camino vacío")
        void origenIgualDestino_costoCero() {
            Nodo a = new Nodo("A", "A");
            Grafo grafo = new Grafo();
            grafo.agregarNodo(a);

            DijkstraPenalizado dijkstra = new DijkstraPenalizado(grafo, 5.0);
            ResultadoRuta resultado = dijkstra.calcularRuta(a, a);

            assertEquals(0.0, resultado.getCostoTotal(), DELTA);
            assertTrue(resultado.getCamino().isEmpty());
        }

        @Test
        @DisplayName("Grafo con ciclos: el algoritmo termina correctamente")
        void grafoCiclico_terminaSinBucleInfinito() {
            Nodo a = new Nodo("A", "A");
            Nodo b = new Nodo("B", "B");
            Nodo c = new Nodo("C", "C");
            Grafo grafo = new Grafo();
            // Ciclo: A→B→C→A
            grafo.agregarArista(new Arista(a, b, 1.0, "L1"));
            grafo.agregarArista(new Arista(b, c, 1.0, "L1"));
            grafo.agregarArista(new Arista(c, a, 1.0, "L1")); // ciclo
            grafo.agregarArista(new Arista(b, c, 5.0, "L2")); // alternativa

            DijkstraPenalizado dijkstra = new DijkstraPenalizado(grafo, 3.0);
            // Debe terminar sin lanzar excepción y encontrar A→B→C
            assertDoesNotThrow(() -> {
                ResultadoRuta resultado = dijkstra.calcularRuta(a, c);
                assertTrue(resultado.isAlcanzable());
                // L1: A→B(1) + B→C(1) = 2 min sin transbordo
                assertEquals(2.0, resultado.getCostoTotal(), DELTA);
            });
        }

        @Test
        @DisplayName("Nodo origen no registrado lanza IllegalArgumentException")
        void nodoOrigenNoRegistrado_lanzaExcepcion() {
            Nodo a = new Nodo("A", "A");
            Nodo b = new Nodo("B", "B");
            Grafo grafo = new Grafo();
            grafo.agregarNodo(b); // solo b está registrado

            DijkstraPenalizado dijkstra = new DijkstraPenalizado(grafo, 5.0);
            assertThrows(IllegalArgumentException.class,
                    () -> dijkstra.calcularRuta(a, b));
        }

        @Test
        @DisplayName("Nodo destino no registrado lanza IllegalArgumentException")
        void nodoDestinoNoRegistrado_lanzaExcepcion() {
            Nodo a = new Nodo("A", "A");
            Nodo b = new Nodo("B", "B");
            Grafo grafo = new Grafo();
            grafo.agregarNodo(a);

            DijkstraPenalizado dijkstra = new DijkstraPenalizado(grafo, 5.0);
            assertThrows(IllegalArgumentException.class,
                    () -> dijkstra.calcularRuta(a, b));
        }

        @Test
        @DisplayName("Penalización negativa lanza IllegalArgumentException")
        void penalizacionNegativa_lanzaExcepcion() {
            Grafo grafo = new Grafo();
            assertThrows(IllegalArgumentException.class,
                    () -> new DijkstraPenalizado(grafo, -1.0));
        }

        @Test
        @DisplayName("Ruta óptima en grafo con múltiples caminos y transbordos mixtos")
        void grafoComplejoMixto_encuentraOptimo() {
            // Topología:
            //   L1: A─(2)─▶B─(2)─▶D    (total sin transbordo: 4)
            //   L2: A─(1)─▶C─(1)─▶D    (total: 2, pero transbordo en C desde L2)
            //   Transbordo C→D usa L3:  L3: C─(1)─▶D
            // Con penalización 3:
            //   L1: 4 min (sin transbordo)
            //   L2+L3: 1 + (3 pen) + 1 = 5 min  → L1 gana
            Nodo a = new Nodo("A", "A");
            Nodo b = new Nodo("B", "B");
            Nodo c = new Nodo("C", "C");
            Nodo d = new Nodo("D", "D");
            Grafo grafo = new Grafo();
            grafo.agregarArista(new Arista(a, b, 2.0, "L1"));
            grafo.agregarArista(new Arista(b, d, 2.0, "L1"));
            grafo.agregarArista(new Arista(a, c, 1.0, "L2"));
            grafo.agregarArista(new Arista(c, d, 1.0, "L3")); // transbordo L2→L3

            DijkstraPenalizado dijkstra = new DijkstraPenalizado(grafo, 3.0);
            ResultadoRuta resultado = dijkstra.calcularRuta(a, d);

            assertEquals(4.0, resultado.getCostoTotal(), DELTA,
                    "L1 (4 min) debe ganar sobre L2+L3 (1+3+1=5 min).");
            assertEquals(0, resultado.getCantidadTransbordos());
        }
    }
}

package com.transporte.dijkstra;

import com.transporte.dijkstra.model.Arista;
import com.transporte.dijkstra.model.Grafo;
import com.transporte.dijkstra.model.Nodo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para la capa de modelo: {@link Nodo}, {@link Arista}
 * y {@link Grafo}.
 *
 * <p>Validan el comportamiento de la estructura de datos del grafo de
 * transporte independientemente del algoritmo.</p>
 */
@DisplayName("Pruebas de la estructura del Grafo")
class GrafoTest {

    private Nodo nodoA;
    private Nodo nodoB;
    private Nodo nodoC;
    private Grafo grafo;

    @BeforeEach
    void setUp() {
        nodoA = new Nodo("A", "Estación A");
        nodoB = new Nodo("B", "Estación B");
        nodoC = new Nodo("C", "Estación C");
        grafo = new Grafo();
    }

    // ── Pruebas de Nodo ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Nodo: igualdad basada en id, no en nombre")
    void nodo_igualdadPorId() {
        Nodo nodoA2 = new Nodo("A", "Otro nombre");
        assertEquals(nodoA, nodoA2,
                "Dos nodos con el mismo id deben ser iguales sin importar el nombre.");
    }

    @Test
    @DisplayName("Nodo: hashCode consistente con equals")
    void nodo_hashCodeConsistente() {
        Nodo nodoA2 = new Nodo("A", "Estación A");
        assertEquals(nodoA.hashCode(), nodoA2.hashCode());
    }

    @Test
    @DisplayName("Nodo: id vacío lanza IllegalArgumentException")
    void nodo_idVacioLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new Nodo("", "Test"));
    }

    @Test
    @DisplayName("Nodo: id nulo lanza IllegalArgumentException")
    void nodo_idNuloLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new Nodo(null, "Test"));
    }

    // ── Pruebas de Arista ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Arista: peso cero o negativo lanza IllegalArgumentException")
    void arista_pesoInvalidoLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> new Arista(nodoA, nodoB, 0.0, "L1"),
                "Un peso de 0 debe ser rechazado.");
        assertThrows(IllegalArgumentException.class,
                () -> new Arista(nodoA, nodoB, -5.0, "L1"),
                "Un peso negativo debe ser rechazado.");
    }

    @Test
    @DisplayName("Arista: lineaId vacío lanza IllegalArgumentException")
    void arista_lineaIdVacioLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> new Arista(nodoA, nodoB, 5.0, ""));
    }

    @Test
    @DisplayName("Arista: getters retornan los valores del constructor")
    void arista_gettersCorrectos() {
        Arista arista = new Arista(nodoA, nodoB, 7.5, "L2");
        assertEquals(nodoA,  arista.getOrigen());
        assertEquals(nodoB,  arista.getDestino());
        assertEquals(7.5,    arista.getPeso(),    1e-9);
        assertEquals("L2",   arista.getLineaId());
    }

    // ── Pruebas de Grafo ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Grafo: nodo agregado existe en el grafo")
    void grafo_agregarNodo_existeEnGrafo() {
        grafo.agregarNodo(nodoA);
        assertTrue(grafo.contieneNodo(nodoA));
    }

    @Test
    @DisplayName("Grafo: agregar nodo duplicado es idempotente")
    void grafo_agregarNodoDuplicado_sinEfecto() {
        grafo.agregarNodo(nodoA);
        grafo.agregarNodo(nodoA); // segunda vez
        assertEquals(1, grafo.cantidadNodos());
    }

    @Test
    @DisplayName("Grafo: arista agrega nodos origen y destino automáticamente")
    void grafo_agregarArista_registraNodosAutomaticamente() {
        grafo.agregarArista(new Arista(nodoA, nodoB, 5.0, "L1"));
        assertTrue(grafo.contieneNodo(nodoA));
        assertTrue(grafo.contieneNodo(nodoB));
    }

    @Test
    @DisplayName("Grafo: obtenerVecinos retorna la arista correcta")
    void grafo_obtenerVecinos_retornaAristas() {
        Arista arista = new Arista(nodoA, nodoB, 3.0, "L1");
        grafo.agregarArista(arista);

        List<Arista> vecinos = grafo.obtenerVecinos(nodoA);
        assertEquals(1, vecinos.size());
        assertEquals(arista, vecinos.get(0));
    }

    @Test
    @DisplayName("Grafo: obtenerVecinos de nodo sin salidas retorna lista vacía")
    void grafo_obtenerVecinos_sinAristas_retornaVacio() {
        grafo.agregarNodo(nodoC);
        assertTrue(grafo.obtenerVecinos(nodoC).isEmpty());
    }

    @Test
    @DisplayName("Grafo: nodo no registrado retorna lista vacía en obtenerVecinos")
    void grafo_obtenerVecinos_nodoDesconocido_retornaVacio() {
        // nodoA nunca fue agregado
        assertTrue(grafo.obtenerVecinos(nodoA).isEmpty());
    }

    @Test
    @DisplayName("Grafo: arista bidireccional genera dos aristas dirigidas")
    void grafo_aristaBidireccional_generaDosAristas() {
        grafo.agregarAristaBidireccional(nodoA, nodoB, 4.0, "L1");

        assertEquals(1, grafo.obtenerVecinos(nodoA).size(),
                "A debe tener 1 arista saliente hacia B.");
        assertEquals(1, grafo.obtenerVecinos(nodoB).size(),
                "B debe tener 1 arista saliente hacia A.");
    }

    @Test
    @DisplayName("Grafo: cantidadAristas cuenta todas las aristas dirigidas")
    void grafo_cantidadAristas_correcta() {
        grafo.agregarArista(new Arista(nodoA, nodoB, 2.0, "L1"));
        grafo.agregarArista(new Arista(nodoB, nodoC, 3.0, "L1"));
        grafo.agregarArista(new Arista(nodoA, nodoC, 8.0, "L2"));
        assertEquals(3, grafo.cantidadAristas());
    }

    @Test
    @DisplayName("Grafo: obtenerNodos retorna todos los nodos registrados")
    void grafo_obtenerNodos_retornaTodos() {
        grafo.agregarNodo(nodoA);
        grafo.agregarNodo(nodoB);
        grafo.agregarNodo(nodoC);
        assertEquals(3, grafo.obtenerNodos().size());
        assertTrue(grafo.obtenerNodos().containsAll(List.of(nodoA, nodoB, nodoC)));
    }
}

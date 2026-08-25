package com.transporte.dijkstra.algorithm;

import com.transporte.dijkstra.model.Arista;
import com.transporte.dijkstra.model.Grafo;
import com.transporte.dijkstra.model.Nodo;

import java.util.*;

/**
 * Implementación del algoritmo de Dijkstra con <b>penalización por transbordo</b>.
 *
 * <h2>Motivación</h2>
 * <p>En una red de transporte urbano, cambiar de línea (transbordo) implica un
 * costo adicional real: tiempo de espera en el andén, recorrido hasta otra
 * plataforma, incertidumbre en la conexión, etc. Este algoritmo modela ese costo
 * como una <em>penalización constante</em> que se suma al costo acumulado cada
 * vez que el pasajero cambia de línea durante su recorrido.</p>
 *
 * <h2>Estrategia de Estado Extendido</h2>
 * <p>Para detectar el cambio de línea sin duplicar el grafo (desdoblamiento de
 * vértices), se extiende el estado de la cola de prioridad a una tupla
 * {@code (Nodo, lineaActual)}. De este modo, el algoritmo "recuerda" por qué
 * línea llegó a cada nodo y puede comparar con la línea de la arista candidata.</p>
 *
 * <pre>
 *   Estado = (costoAcumulado, Nodo, lineaActual)
 *   Al expandir vecino v a través de arista e:
 *     nuevoCosto = costoAcumulado + e.peso
 *                + (e.lineaId ≠ lineaActual ? penalizacion : 0)
 * </pre>
 *
 * <h2>Complejidad</h2>
 * <ul>
 *   <li>Tiempo: O((V + E) log V) usando {@link PriorityQueue}.</li>
 *   <li>Espacio: O(V + E) para distancias, predecesores y cola.</li>
 * </ul>
 */
public class DijkstraPenalizado {

    // ─── Estado interno de la cola de prioridad ──────────────────────────────

    /**
     * Elemento de la cola de prioridad que encapsula el estado extendido
     * del algoritmo: nodo actual, línea por la que llegamos a él, costo
     * acumulado y arista que llevó hasta este nodo.
     */
    private static final class EstadoNodo implements Comparable<EstadoNodo> {

        final double costoAcumulado;
        final Nodo   nodo;
        final String lineaActual;   // null = nodo origen (sin línea previa)
        final Arista aristaPrevia;  // null = nodo origen

        EstadoNodo(double costoAcumulado, Nodo nodo,
                   String lineaActual, Arista aristaPrevia) {
            this.costoAcumulado = costoAcumulado;
            this.nodo           = nodo;
            this.lineaActual    = lineaActual;
            this.aristaPrevia   = aristaPrevia;
        }

        @Override
        public int compareTo(EstadoNodo otro) {
            return Double.compare(this.costoAcumulado, otro.costoAcumulado);
        }
    }

    // ─── Atributos ────────────────────────────────────────────────────────────

    private final Grafo grafo;

    /**
     * Penalización en minutos que se aplica al costo acumulado cada vez que
     * el pasajero cambia de línea. Valor recomendado en redes urbanas reales:
     * entre 3 y 10 minutos.
     */
    private final double penalizacionTransbordo;

    // ─── Constructor ──────────────────────────────────────────────────────────

    /**
     * Construye el algoritmo sobre un grafo dado con una penalización
     * de transbordo configurable.
     *
     * @param grafo                  la red de transporte; no puede ser {@code null}
     * @param penalizacionTransbordo minutos adicionales por cambio de línea; ≥ 0
     * @throws IllegalArgumentException si algún argumento viola las restricciones
     */
    public DijkstraPenalizado(Grafo grafo, double penalizacionTransbordo) {
        if (grafo == null) {
            throw new IllegalArgumentException("El grafo no puede ser nulo.");
        }
        if (penalizacionTransbordo < 0) {
            throw new IllegalArgumentException(
                    "La penalización por transbordo no puede ser negativa.");
        }
        this.grafo                  = grafo;
        this.penalizacionTransbordo = penalizacionTransbordo;
    }

    // ─── API pública ──────────────────────────────────────────────────────────

    /**
     * Calcula la ruta óptima entre {@code origen} y {@code destino} en el
     * grafo de transporte, considerando la penalización por cada cambio de línea.
     *
     * @param origen  nodo de partida
     * @param destino nodo de llegada
     * @return {@link ResultadoRuta} con el camino óptimo, el costo total y el
     *         número de transbordos; si el destino no es alcanzable, retorna
     *         {@link ResultadoRuta#sinRuta()}
     * @throws IllegalArgumentException si algún nodo no pertenece al grafo
     */
    public ResultadoRuta calcularRuta(Nodo origen, Nodo destino) {
        validarNodosEnGrafo(origen, destino);

        // Caso trivial: origen y destino son el mismo nodo
        if (origen.equals(destino)) {
            return new ResultadoRuta(Collections.emptyList(), 0.0, 0);
        }

        // ── Inicialización de estructuras ──────────────────────────────────

        // distancias[nodo] = menor costo encontrado hasta el momento
        Map<Nodo, Double>  distancias   = new HashMap<>();
        // predecesor[nodo] = la arista que llevó al nodo con menor costo
        Map<Nodo, Arista>  predecesor   = new HashMap<>();
        // Conjunto de nodos cuya distancia ya fue finalizada
        Set<Nodo>          visitados    = new HashSet<>();

        for (Nodo nodo : grafo.obtenerNodos()) {
            distancias.put(nodo, Double.POSITIVE_INFINITY);
        }
        distancias.put(origen, 0.0);

        // Cola de prioridad ordenada por costo acumulado (min-heap)
        PriorityQueue<EstadoNodo> cola = new PriorityQueue<>();
        cola.offer(new EstadoNodo(0.0, origen, null, null));

        // ── Bucle principal de Dijkstra ────────────────────────────────────

        while (!cola.isEmpty()) {
            EstadoNodo estadoActual = cola.poll();

            // Si ya finalizamos este nodo, descartamos el estado obsoleto
            if (visitados.contains(estadoActual.nodo)) {
                continue;
            }
            visitados.add(estadoActual.nodo);

            // Si llegamos al destino, terminamos la búsqueda
            if (estadoActual.nodo.equals(destino)) {
                break;
            }

            // ── Relajación de aristas vecinas ──────────────────────────────
            for (Arista arista : grafo.obtenerVecinos(estadoActual.nodo)) {
                Nodo vecino = arista.getDestino();

                if (visitados.contains(vecino)) {
                    continue;
                }

                // Detectar transbordo: línea de esta arista ≠ línea anterior
                boolean esTransbordo = estadoActual.lineaActual != null
                        && !arista.getLineaId().equals(estadoActual.lineaActual);

                double penalizacion = esTransbordo ? penalizacionTransbordo : 0.0;
                double nuevoCosto   = estadoActual.costoAcumulado
                                    + arista.getPeso()
                                    + penalizacion;

                if (nuevoCosto < distancias.get(vecino)) {
                    distancias.put(vecino, nuevoCosto);
                    predecesor.put(vecino, arista);
                    cola.offer(new EstadoNodo(nuevoCosto, vecino,
                                              arista.getLineaId(), arista));
                }
            }
        }

        // ── Reconstrucción del camino ──────────────────────────────────────

        if (distancias.get(destino).isInfinite()) {
            return ResultadoRuta.sinRuta();
        }

        return reconstruirCamino(origen, destino, predecesor,
                                 distancias.get(destino));
    }

    // ─── Métodos auxiliares ───────────────────────────────────────────────────

    /**
     * Reconstruye la lista de aristas del camino óptimo recorriendo
     * el mapa de predecesores desde el destino hasta el origen.
     */
    private ResultadoRuta reconstruirCamino(Nodo origen, Nodo destino,
                                             Map<Nodo, Arista> predecesor,
                                             double costoTotal) {
        LinkedList<Arista> camino = new LinkedList<>();
        Nodo cursor = destino;

        while (!cursor.equals(origen)) {
            Arista arista = predecesor.get(cursor);
            camino.addFirst(arista);        // Insertar al frente para obtener orden correcto
            cursor = arista.getOrigen();
        }

        // Contar transbordos
        int transbordos = 0;
        String lineaAnterior = null;
        for (Arista a : camino) {
            if (lineaAnterior != null && !a.getLineaId().equals(lineaAnterior)) {
                transbordos++;
            }
            lineaAnterior = a.getLineaId();
        }

        return new ResultadoRuta(new ArrayList<>(camino), costoTotal, transbordos);
    }

    /**
     * Valida que ambos nodos existan en el grafo antes de ejecutar el algoritmo.
     *
     * @throws IllegalArgumentException si alguno no está registrado
     */
    private void validarNodosEnGrafo(Nodo origen, Nodo destino) {
        if (!grafo.contieneNodo(origen)) {
            throw new IllegalArgumentException(
                    "El nodo origen no existe en el grafo: " + origen.getId());
        }
        if (!grafo.contieneNodo(destino)) {
            throw new IllegalArgumentException(
                    "El nodo destino no existe en el grafo: " + destino.getId());
        }
    }

    /** @return penalización por transbordo configurada (minutos) */
    public double getPenalizacionTransbordo() {
        return penalizacionTransbordo;
    }
}

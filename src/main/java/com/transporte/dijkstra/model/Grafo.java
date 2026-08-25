package com.transporte.dijkstra.model;

import java.util.*;

/**
 * Modela la red de transporte urbano como un grafo dirigido y ponderado.
 *
 * <p>Internamente utiliza una <b>lista de adyacencia</b>: para cada nodo se
 * mantiene la lista de aristas que parten de él. Esto ofrece O(1) para la
 * inserción y O(grado) para la consulta de vecinos, lo cual es apropiado para
 * grafos dispersos como las redes de transporte.</p>
 *
 * <p>La clase no es thread-safe; si se requiere concurrencia, se deben
 * sincronizar externamente los accesos de escritura.</p>
 *
 * <h2>Ejemplo de uso</h2>
 * <pre>{@code
 *   Nodo a = new Nodo("A", "Estación Central");
 *   Nodo b = new Nodo("B", "Plaza Mayor");
 *   Grafo grafo = new Grafo();
 *   grafo.agregarNodo(a);
 *   grafo.agregarNodo(b);
 *   grafo.agregarArista(new Arista(a, b, 5.0, "L1"));
 * }</pre>
 */
public class Grafo {

    /**
     * Lista de adyacencia: cada clave es un nodo y el valor es la lista
     * de aristas que salen de él.
     */
    private final Map<Nodo, List<Arista>> listaAdyacencia;

    /** Construye un grafo vacío. */
    public Grafo() {
        this.listaAdyacencia = new LinkedHashMap<>();
    }

    /**
     * Registra un nodo en el grafo. Si el nodo ya existe, esta operación
     * no tiene efecto (idempotente).
     *
     * @param nodo el nodo a registrar; no puede ser {@code null}
     * @throws IllegalArgumentException si {@code nodo} es {@code null}
     */
    public void agregarNodo(Nodo nodo) {
        if (nodo == null) {
            throw new IllegalArgumentException("No se puede agregar un nodo nulo.");
        }
        listaAdyacencia.putIfAbsent(nodo, new ArrayList<>());
    }

    /**
     * Agrega una arista dirigida al grafo.
     *
     * <p>Si el nodo origen no estaba registrado previamente, se registra
     * automáticamente. El nodo destino también se registra si no existe,
     * garantizando que todos los nodos referenciados sean alcanzables en
     * las consultas de adyacencia.</p>
     *
     * @param arista la arista a agregar; no puede ser {@code null}
     * @throws IllegalArgumentException si {@code arista} es {@code null}
     */
    public void agregarArista(Arista arista) {
        if (arista == null) {
            throw new IllegalArgumentException("No se puede agregar una arista nula.");
        }
        // Auto-registro de nodos referenciados
        agregarNodo(arista.getOrigen());
        agregarNodo(arista.getDestino());

        listaAdyacencia.get(arista.getOrigen()).add(arista);
    }

    /**
     * Agrega una arista <b>no dirigida</b> (bidireccional) entre dos nodos.
     *
     * <p>Internamente crea dos aristas dirigidas con la misma línea y el
     * mismo peso: una en cada sentido.</p>
     *
     * @param origen   nodo de partida
     * @param destino  nodo de llegada
     * @param peso     tiempo de viaje en minutos
     * @param lineaId  identificador de la línea que opera el tramo
     */
    public void agregarAristaBidireccional(Nodo origen, Nodo destino,
                                           double peso, String lineaId) {
        agregarArista(new Arista(origen, destino, peso, lineaId));
        agregarArista(new Arista(destino, origen, peso, lineaId));
    }

    /**
     * Retorna las aristas que salen del nodo indicado.
     *
     * @param nodo el nodo del que se quieren obtener los vecinos
     * @return lista inmutable de aristas de salida; vacía si el nodo no tiene
     *         vecinos o no existe en el grafo
     */
    public List<Arista> obtenerVecinos(Nodo nodo) {
        List<Arista> vecinos = listaAdyacencia.get(nodo);
        if (vecinos == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(vecinos);
    }

    /**
     * Retorna el conjunto de todos los nodos registrados en el grafo.
     *
     * @return conjunto inmutable de nodos
     */
    public Set<Nodo> obtenerNodos() {
        return Collections.unmodifiableSet(listaAdyacencia.keySet());
    }

    /**
     * Indica si un nodo dado está registrado en el grafo.
     *
     * @param nodo el nodo a verificar
     * @return {@code true} si el nodo existe; {@code false} en caso contrario
     */
    public boolean contieneNodo(Nodo nodo) {
        return listaAdyacencia.containsKey(nodo);
    }

    /**
     * Retorna el número de nodos en el grafo.
     *
     * @return cantidad de nodos
     */
    public int cantidadNodos() {
        return listaAdyacencia.size();
    }

    /**
     * Retorna el número total de aristas (dirigidas) en el grafo.
     *
     * @return cantidad de aristas
     */
    public int cantidadAristas() {
        return listaAdyacencia.values().stream()
                .mapToInt(List::size)
                .sum();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Grafo{\n");
        listaAdyacencia.forEach((nodo, aristas) -> {
            sb.append("  ").append(nodo.getId()).append(" -> ");
            aristas.forEach(a -> sb.append(a.getDestino().getId())
                    .append("(").append(a.getPeso()).append("min, ")
                    .append(a.getLineaId()).append(") "));
            sb.append("\n");
        });
        sb.append("}");
        return sb.toString();
    }
}

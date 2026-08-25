package com.transporte.dijkstra.algorithm;

import com.transporte.dijkstra.model.Arista;
import com.transporte.dijkstra.model.Nodo;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Objeto de transferencia inmutable que encapsula el resultado de una
 * ejecución del algoritmo de Dijkstra con penalización por transbordo.
 *
 * <p>Contiene:</p>
 * <ul>
 *   <li>{@link #getCamino()} — secuencia ordenada de aristas que forman la
 *       ruta óptima desde el origen hasta el destino.</li>
 *   <li>{@link #getCostoTotal()} — costo total del camino, incluyendo el
 *       tiempo de viaje acumulado <em>y</em> las penalizaciones por cada
 *       transbordo realizado.</li>
 *   <li>{@link #getCantidadTransbordos()} — número de cambios de línea en
 *       la ruta.</li>
 *   <li>{@link #isAlcanzable()} — {@code false} si el destino no tiene
 *       camino desde el origen.</li>
 * </ul>
 */
public final class ResultadoRuta {

    /** Secuencia de aristas que conforman el camino óptimo. */
    private final List<Arista> camino;

    /**
     * Costo total: suma de pesos de aristas + penalizaciones por transbordo.
     * Valor {@link Double#POSITIVE_INFINITY} cuando el destino no es alcanzable.
     */
    private final double costoTotal;

    /** Número de transbordos (cambios de línea) realizados en la ruta. */
    private final int cantidadTransbordos;

    /**
     * Construye un resultado alcanzable con camino y costo conocidos.
     *
     * @param camino              lista de aristas de la ruta; no puede ser {@code null}
     * @param costoTotal          costo total ≥ 0
     * @param cantidadTransbordos número de transbordos ≥ 0
     */
    public ResultadoRuta(List<Arista> camino, double costoTotal, int cantidadTransbordos) {
        Objects.requireNonNull(camino, "La lista de aristas no puede ser nula.");
        this.camino               = Collections.unmodifiableList(camino);
        this.costoTotal           = costoTotal;
        this.cantidadTransbordos  = cantidadTransbordos;
    }

    /**
     * Retorna una instancia que representa un destino <b>no alcanzable</b>.
     *
     * @return resultado con camino vacío y costo infinito
     */
    public static ResultadoRuta sinRuta() {
        return new ResultadoRuta(Collections.emptyList(), Double.POSITIVE_INFINITY, 0);
    }

    /**
     * @return lista inmutable de aristas en orden origen → destino;
     *         vacía si el destino no es alcanzable
     */
    public List<Arista> getCamino() {
        return camino;
    }

    /**
     * @return costo total del camino (minutos + penalizaciones);
     *         {@link Double#POSITIVE_INFINITY} si no hay ruta
     */
    public double getCostoTotal() {
        return costoTotal;
    }

    /** @return número de cambios de línea en la ruta óptima */
    public int getCantidadTransbordos() {
        return cantidadTransbordos;
    }

    /** @return {@code true} si existe al menos un camino al destino */
    public boolean isAlcanzable() {
        return !camino.isEmpty() || costoTotal == 0.0; // origen == destino
    }

    /**
     * Genera una descripción textual de la ruta para logging o consola.
     *
     * @return cadena con el detalle paso a paso de la ruta
     */
    public String describir() {
        if (!isAlcanzable()) {
            return "Destino no alcanzable desde el nodo origen.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Ruta óptima | Costo total: %.1f min | Transbordos: %d%n",
                costoTotal, cantidadTransbordos));

        String lineaActual = null;
        for (Arista arista : camino) {
            if (!arista.getLineaId().equals(lineaActual)) {
                if (lineaActual != null) {
                    sb.append(String.format("  *** TRANSBORDO: %s → %s ***%n",
                            lineaActual, arista.getLineaId()));
                }
                lineaActual = arista.getLineaId();
                sb.append(String.format("  [Línea %s]%n", lineaActual));
            }
            sb.append(String.format("    %s ──(%.1f min)──▶ %s%n",
                    arista.getOrigen().getId(),
                    arista.getPeso(),
                    arista.getDestino().getId()));
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format("ResultadoRuta{costoTotal=%.1f, transbordos=%d, pasos=%d}",
                costoTotal, cantidadTransbordos, camino.size());
    }

    /**
     * Retorna los nodos visitados en orden (incluyendo origen y destino).
     *
     * @return lista inmutable de nodos en el orden de recorrido; vacía si no
     *         hay ruta
     */
    public List<Nodo> obtenerSecuenciaNodos() {
        if (camino.isEmpty()) return Collections.emptyList();
        List<Nodo> nodos = new java.util.ArrayList<>();
        nodos.add(camino.get(0).getOrigen());
        for (Arista a : camino) {
            nodos.add(a.getDestino());
        }
        return Collections.unmodifiableList(nodos);
    }
}

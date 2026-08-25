package com.transporte.dijkstra.model;

import java.util.Objects;

/**
 * Representa una arista (conexión) entre dos estaciones de la red de transporte.
 *
 * <p>Cada arista almacena:</p>
 * <ul>
 *   <li><b>origen / destino</b>: los nodos que conecta (arista dirigida).</li>
 *   <li><b>peso</b>: el tiempo estimado de viaje en minutos (debe ser &gt; 0).</li>
 *   <li><b>lineaId</b>: identificador de la línea o ruta de transporte a la que
 *       pertenece este tramo (p.ej. "L1", "BUS-A"). Este campo es el que permite
 *       al algoritmo detectar cambios de línea y aplicar la penalización por
 *       transbordo.</li>
 * </ul>
 *
 * <p>Las aristas son inmutables tras su construcción.</p>
 */
public final class Arista {

    /** Nodo de partida de este tramo. */
    private final Nodo origen;

    /** Nodo de llegada de este tramo. */
    private final Nodo destino;

    /**
     * Tiempo de viaje estimado en minutos entre {@code origen} y {@code destino}.
     * Debe ser estrictamente positivo.
     */
    private final double peso;

    /**
     * Identificador de la línea/ruta de transporte que opera este tramo
     * (p.ej. "L1", "L2", "BUS-A"). Usado para detectar transbordos.
     */
    private final String lineaId;

    /**
     * Construye una arista dirigida con todos sus atributos.
     *
     * @param origen   nodo de partida; no puede ser {@code null}
     * @param destino  nodo de llegada; no puede ser {@code null}
     * @param peso     tiempo de viaje en minutos; debe ser &gt; 0
     * @param lineaId  identificador de la línea; no puede ser {@code null} ni vacío
     * @throws IllegalArgumentException si algún argumento viola las restricciones
     */
    public Arista(Nodo origen, Nodo destino, double peso, String lineaId) {
        if (origen == null) {
            throw new IllegalArgumentException("El nodo origen no puede ser nulo.");
        }
        if (destino == null) {
            throw new IllegalArgumentException("El nodo destino no puede ser nulo.");
        }
        if (peso <= 0) {
            throw new IllegalArgumentException(
                    "El peso de la arista debe ser estrictamente positivo; se recibió: " + peso);
        }
        if (lineaId == null || lineaId.isBlank()) {
            throw new IllegalArgumentException("El lineaId no puede ser nulo ni vacío.");
        }
        this.origen  = origen;
        this.destino = destino;
        this.peso    = peso;
        this.lineaId = lineaId;
    }

    /** @return nodo de partida */
    public Nodo getOrigen() { return origen; }

    /** @return nodo de llegada */
    public Nodo getDestino() { return destino; }

    /** @return tiempo de viaje en minutos */
    public double getPeso() { return peso; }

    /** @return identificador de la línea que opera este tramo */
    public String getLineaId() { return lineaId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Arista otra)) return false;
        return Double.compare(otra.peso, peso) == 0
                && origen.equals(otra.origen)
                && destino.equals(otra.destino)
                && lineaId.equals(otra.lineaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(origen, destino, peso, lineaId);
    }

    @Override
    public String toString() {
        return String.format("Arista{%s --(%.1f min, %s)--> %s}",
                origen.getId(), peso, lineaId, destino.getId());
    }
}

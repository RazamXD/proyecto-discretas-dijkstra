package com.transporte.dijkstra.model;

import java.util.Objects;

/**
 * Representa una estación (vértice) dentro de la red de transporte urbano.
 *
 * <p>Un {@code Nodo} es inmutable: una vez construido, su identificador y
 * nombre no cambian. La igualdad y el hash se basan exclusivamente en el
 * {@code id}, lo que permite usar instancias como claves en {@code Map} y
 * elementos en {@code Set} sin ambigüedades.</p>
 *
 * <pre>
 *   Nodo estacion = new Nodo("A", "Estación Central");
 * </pre>
 */
public final class Nodo {

    /** Identificador único de la estación (p.ej. "A", "EST-01"). */
    private final String id;

    /** Nombre legible de la estación (p.ej. "Estación Central"). */
    private final String nombre;

    /**
     * Construye un nodo con los datos de la estación.
     *
     * @param id     identificador único; no puede ser {@code null} ni vacío
     * @param nombre nombre descriptivo de la estación; no puede ser {@code null}
     * @throws IllegalArgumentException si {@code id} o {@code nombre} son inválidos
     */
    public Nodo(String id, String nombre) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El id del nodo no puede ser nulo ni vacío.");
        }
        if (nombre == null) {
            throw new IllegalArgumentException("El nombre del nodo no puede ser nulo.");
        }
        this.id = id;
        this.nombre = nombre;
    }

    /** @return identificador único de la estación */
    public String getId() {
        return id;
    }

    /** @return nombre descriptivo de la estación */
    public String getNombre() {
        return nombre;
    }

    /**
     * Dos nodos son iguales si y solo si tienen el mismo {@code id}.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Nodo otro)) return false;
        return id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Nodo{id='%s', nombre='%s'}", id, nombre);
    }
}

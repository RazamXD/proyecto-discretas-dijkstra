package com.transporte.dijkstra;

import com.transporte.dijkstra.algorithm.DijkstraPenalizado;
import com.transporte.dijkstra.algorithm.ResultadoRuta;
import com.transporte.dijkstra.model.Arista;
import com.transporte.dijkstra.model.Grafo;
import com.transporte.dijkstra.model.Nodo;

/**
 * Clase de demostración que construye una red de transporte urbano de ejemplo
 * y calcula rutas óptimas con y sin penalización por transbordo.
 *
 * <h2>Topología de la red de ejemplo</h2>
 * <pre>
 *   Línea 1 (L1):  A ──(4)──▶ B ──(5)──▶ C ──(3)──▶ D
 *   Línea 2 (L2):  A ──(2)──▶ E ──(2)──▶ C
 *   Línea 3 (L3):  C ──(4)──▶ D
 *
 *   Leyenda: (n) = tiempo de viaje en minutos
 * </pre>
 *
 * <p>Ejecutar con: {@code mvn exec:java}</p>
 */
public class Main {

    public static void main(String[] args) {

        System.out.println("════════════════════════════════════════════════════");
        System.out.println("  Sistema de Rutas de Transporte Urbano — Dijkstra  ");
        System.out.println("════════════════════════════════════════════════════");
        System.out.println();

        // ── Construcción del grafo ───────────────────────────────────────────

        Nodo a = new Nodo("A", "Terminal Norte");
        Nodo b = new Nodo("B", "Plaza Central");
        Nodo c = new Nodo("C", "Mercado Sur");
        Nodo d = new Nodo("D", "Universidad");
        Nodo e = new Nodo("E", "Parque Industrial");

        Grafo red = new Grafo();

        // Línea 1: ruta directa A → B → C → D (total sin transbordo = 12 min)
        red.agregarArista(new Arista(a, b, 4.0, "L1"));
        red.agregarArista(new Arista(b, c, 5.0, "L1"));
        red.agregarArista(new Arista(c, d, 3.0, "L1"));

        // Línea 2: ruta rápida A → E → C (total = 4 min, pero necesita transbordo para D)
        red.agregarArista(new Arista(a, e, 2.0, "L2"));
        red.agregarArista(new Arista(e, c, 2.0, "L2"));

        // Línea 3: conexión C → D en otra línea (= transbordo si venías de L2)
        red.agregarArista(new Arista(c, d, 4.0, "L3"));

        System.out.println(red);
        System.out.println();

        // ── Escenario 1: Penalización de 5 min ──────────────────────────────

        double penalizacion = 5.0;
        DijkstraPenalizado dijkstra = new DijkstraPenalizado(red, penalizacion);

        System.out.println("📌 Escenario 1 — Penalización: " + penalizacion + " min por transbordo");
        System.out.println("   Origen: A (Terminal Norte) → Destino: D (Universidad)");
        System.out.println();

        ResultadoRuta resultado1 = dijkstra.calcularRuta(a, d);
        System.out.println(resultado1.describir());

        // ── Escenario 2: Penalización de 0 min (sin penalización) ───────────

        DijkstraPenalizado sinPenalizacion = new DijkstraPenalizado(red, 0.0);

        System.out.println("📌 Escenario 2 — Sin penalización (transbordo gratuito)");
        System.out.println("   Origen: A (Terminal Norte) → Destino: D (Universidad)");
        System.out.println();

        ResultadoRuta resultado2 = sinPenalizacion.calcularRuta(a, d);
        System.out.println(resultado2.describir());

        // ── Escenario 3: Nodo inaccesible ───────────────────────────────────

        Nodo f = new Nodo("F", "Barrio Aislado");
        red.agregarNodo(f);  // registrado pero sin aristas entrantes

        System.out.println("📌 Escenario 3 — Destino inaccesible");
        System.out.println("   Origen: A → Destino: F (sin conexiones)");
        System.out.println();

        ResultadoRuta resultado3 = dijkstra.calcularRuta(a, f);
        System.out.println("  " + resultado3.describir());

        System.out.println("════════════════════════════════════════════════════");
        System.out.println("  Demo completada exitosamente.");
        System.out.println("════════════════════════════════════════════════════");
    }
}

package com.mycompany.tecnihogar.servicio;

import com.mycompany.tecnihogar.modelo.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SistemaTecniHogar {

    private final Map<String, OrdenServicio> ordenes;
    private final Map<String, Cuadrilla> cuadrillas;
    private final List<Jornada> jornadas;
    private int siguienteNumeroJornada;

    public SistemaTecniHogar() {
        ordenes = new LinkedHashMap<>();
        cuadrillas = new LinkedHashMap<>();
        jornadas = new ArrayList<>();
        siguienteNumeroJornada = 1;
    }

    public String cotizar(OrdenServicio orden) {
        if (orden == null) {
            throw new IllegalArgumentException("Debes indicar un servicio para cotizar.");
        }
        double costo = orden.calcularCosto();
        if (!Double.isFinite(costo) || costo < 0) {
            throw new IllegalArgumentException("El costo calculado no es valido.");
        }
        return String.format(Locale.ROOT, "%s | Costo: Q%.2f | Dias habiles: %d",
                orden.getCategoria(), costo, orden.getDiasAtencion());
    }

    public void registrarOrden(OrdenServicio orden) {
        if (orden == null) {
            throw new IllegalArgumentException("Debes indicar una orden.");
        }
        if (ordenes.containsKey(orden.getCodigo())) {
            throw new IllegalArgumentException("Ya existe una orden con ese codigo.");
        }
        if (orden.getEstado() != EstadoOrden.PENDIENTE || orden.getVisitas() != 0) {
            throw new IllegalArgumentException("Solo puedes registrar una orden nueva y pendiente.");
        }

        // El codigo se comprueba antes del cobro. Un duplicado no va a generar ingresos se valida antes
        orden.cobrarRegistro();
        ordenes.put(orden.getCodigo(), orden);
    }

    public void registrarCuadrilla(Cuadrilla cuadrilla) {
        if (cuadrilla == null) {
            throw new IllegalArgumentException("Debes indicar una cuadrilla.");
        }
        if (cuadrillas.containsKey(cuadrilla.getCodigo())) {
            throw new IllegalArgumentException("Ya existe una cuadrilla con ese codigo.");
        }
        cuadrillas.put(cuadrilla.getCodigo(), cuadrilla);
    }

    public Jornada crearJornada(String codigoCuadrilla) {
        Cuadrilla cuadrilla = buscarCuadrilla(codigoCuadrilla);
        for (Jornada jornada : jornadas) {
            if (jornada.getCuadrilla() == cuadrilla && !jornada.estaCerrada()) {
                throw new IllegalStateException("La cuadrilla ya tiene una jornada sin cerrar.");
            }
        }

        Jornada nueva = new Jornada(siguienteNumeroJornada, cuadrilla);
        jornadas.add(nueva);
        siguienteNumeroJornada++;
        return nueva;
    }

    public void asignarOrden(int numeroJornada, String codigoOrden) {
        Jornada jornada = buscarJornada(numeroJornada);
        OrdenServicio orden = buscarOrden(codigoOrden);
        jornada.asignarOrden(orden);
    }

    public void retirarOrden(int numeroJornada, String codigoOrden) {
        Jornada jornada = buscarJornada(numeroJornada);
        OrdenServicio orden = buscarOrden(codigoOrden);
        jornada.retirarOrden(orden);
    }

    public void iniciarJornada(int numeroJornada) {
        buscarJornada(numeroJornada).iniciar();
    }

    public void registrarResultado(int numeroJornada, String codigoOrden,
                                   ResultadoVisita resultado) {
        Jornada jornada = buscarJornada(numeroJornada);
        OrdenServicio orden = buscarOrden(codigoOrden);
        jornada.registrarResultado(orden, resultado);
    }

    public OrdenServicio buscarOrden(String codigo) {
        OrdenServicio orden = ordenes.get(normalizarCodigo(codigo));
        if (orden == null) {
            throw new IllegalArgumentException("No existe la orden solicitada.");
        }
        return orden;
    }

    public Cuadrilla buscarCuadrilla(String codigo) {
        Cuadrilla cuadrilla = cuadrillas.get(normalizarCodigo(codigo));
        if (cuadrilla == null) {
            throw new IllegalArgumentException("No existe la cuadrilla solicitada.");
        }
        return cuadrilla;
    }

    public Jornada buscarJornada(int numero) {
        for (Jornada jornada : jornadas) {
            if (jornada.getNumero() == numero) {
                return jornada;
            }
        }
        throw new IllegalArgumentException("No existe la jornada solicitada.");
    }

    private String normalizarCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("Debes indicar un codigo.");
        }
        return codigo.trim();
    }

    public List<OrdenServicio> listarOrdenes() {
        return Collections.unmodifiableList(new ArrayList<>(ordenes.values()));
    }

    public List<Cuadrilla> listarCuadrillas() {
        return Collections.unmodifiableList(new ArrayList<>(cuadrillas.values()));
    }

    public Map<String, Map<EstadoOrden, Integer>> contarPorCategoriaYEstado() {
        Map<String, Map<EstadoOrden, Integer>> reporte = new LinkedHashMap<>();

        for (OrdenServicio orden : ordenes.values()) {
            String categoria = orden.getCategoria();
            // se hace un conteo de categoria mandandola a traer con reportes y se hace un conteo del map
            Map<EstadoOrden, Integer> conteos = reporte.get(categoria);
            if (conteos == null) {
                conteos = new EnumMap<>(EstadoOrden.class);
                for (EstadoOrden estado : EstadoOrden.values()) {
                    conteos.put(estado, 0);
                }
                reporte.put(categoria, conteos);
            }
            EstadoOrden estado = orden.getEstado();
            conteos.put(estado, conteos.get(estado) + 1);
        }

        return reporte;
    }

    public double getIngresosPorRegistro() {
        double total = 0.0;
        for (OrdenServicio orden : ordenes.values()) {
            total += orden.getMontoRegistro();
        }
        return total;
    }

    public double getIngresosPorVisitas() {
        double total = 0.0;
        for (OrdenServicio orden : ordenes.values()) {
            total += orden.getCargosVisitasAdicionales();
        }
        return total;
    }

    public double getIngresoTotal() {
        return getIngresosPorRegistro() + getIngresosPorVisitas();
    }

    public Map<String, Double> getIngresosPorCategoria() {
        Map<String, Double> reporte = new LinkedHashMap<>();
        for (OrdenServicio orden : ordenes.values()) {
            String categoria = orden.getCategoria();
            double acumulado = reporte.getOrDefault(categoria, 0.0);
            reporte.put(categoria, acumulado + orden.getIngresoTotal());
        }
        return reporte;
    }

    public List<Jornada> getJornadasNoCerradas() {
        List<Jornada> abiertas = new ArrayList<>();
        for (Jornada jornada : jornadas) {
            if (!jornada.estaCerrada()) {
                abiertas.add(jornada);
            }
        }
        return Collections.unmodifiableList(abiertas);
    }

    // Se llama una vez desde Main, antes de la vista de menu
    public void cargarDatosIniciales() {
        if (!ordenes.isEmpty() || !cuadrillas.isEmpty() || !jornadas.isEmpty()) {
            throw new IllegalStateException("La precarga requiere un sistema vacio.");
        }

        // Antes de incorporarlos se construyen los metodos
        OrdenServicio[] iniciales = {
            new Diagnostico("D01", "Ana", "Zona 1", 1.0, Zona.METROPOLITANA),
            new Diagnostico("D02", "Luis", "Sector periférico A", 1.5, Zona.PERIFERICA),
            new Instalacion("I01", "Carlos", "Zona 10", 2.5, Zona.METROPOLITANA, 5),
            new Instalacion("I02", "Sofía", "Sector periférico B", 2.0, Zona.PERIFERICA, 2),
            new InstalacionElectricaCertificada("E01", "María", "Zona 5",
                    2.5, Zona.METROPOLITANA, 5, 2000.0),
            new InstalacionElectricaCertificada("E02", "Pedro", "Sector periférico C",
                    3.0, Zona.PERIFERICA, 4, 1500.0),
            new MantenimientoAireAcondicionado("A01", "Elena", "Zona 14",
                    2.0, Zona.METROPOLITANA, 12000),
            new MantenimientoAireAcondicionado("A02", "Jorge", "Zona 9",
                    1.5, Zona.METROPOLITANA, 24000)
        };
        Cuadrilla[] equipos = {
            new Cuadrilla("C01", 8.0, true),
            new Cuadrilla("C02", 10.0, false),
            new Cuadrilla("C03", 6.0, true)
        };

        /* Estos ciclos guardan los metodos que estan dentro de los arreglos iniciales y equipos
        con ordenes.put se guardan el codigo y la orden o cuadrilla.
        */
        for (OrdenServicio orden : iniciales) {
            ordenes.put(orden.getCodigo(), orden);
        }
        for (Cuadrilla cuadrilla : equipos) {
            cuadrillas.put(cuadrilla.getCodigo(), cuadrilla);
        }
    }
}
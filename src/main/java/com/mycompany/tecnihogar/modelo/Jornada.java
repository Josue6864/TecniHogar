package com.mycompany.tecnihogar.modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;



public class Jornada {

    private final int numero;
    private final Cuadrilla cuadrilla;
    private final List<OrdenServicio> ordenes;
    private final Map<OrdenServicio, ResultadoVisita> resultados;
    private EstadoJornada estado;

    public Jornada(int numero, Cuadrilla cuadrilla) {
        if (numero <= 0) {
            throw new IllegalArgumentException("El numero de jornada debe ser positivo.");
        }
        if (cuadrilla == null) {
            throw new IllegalArgumentException("Debes indicar una cuadrilla.");
        }
        this.numero = numero;
        this.cuadrilla = cuadrilla;
        this.ordenes = new ArrayList<>();
        /*crea un mapa vacio para que llava deduzca los tipos de lado izquierdo lo que son OrdenServicio, ResultadoVisita
        y lo guarda vacio en el atributo resultados.
        */
        this.resultados = new LinkedHashMap<>();
        this.estado = EstadoJornada.EN_PREPARACION;
    }

    public void asignarOrden(OrdenServicio orden) {
        exigirEnPreparacion();
        if (orden == null) {
            throw new IllegalArgumentException("Debes indicar una orden.");
        }
        if (ordenes.contains(orden)) {
            throw new IllegalStateException("La orden ya pertenece a esta jornada.");
        }
        if (orden.getEstado() != EstadoOrden.PENDIENTE) {
            throw new IllegalStateException("La orden debe estar pendiente.");
        }
        if (!orden.esCompatibleCon(cuadrilla)) {
            throw new IllegalArgumentException("La orden no es compatible con esta cuadrilla.");
        }

        BigDecimal nuevasHoras = sumarHorasAsignadas()
                .add(BigDecimal.valueOf(orden.getHorasEstimadas()));
        if (nuevasHoras.compareTo(BigDecimal.valueOf(cuadrilla.getCapacidadHoras())) > 0) {
            throw new IllegalArgumentException("La orden supera la capacidad disponible.");
        }

        // Todas las validaciones se hacen antes de modificar la jornada.
        orden.asignar();
        ordenes.add(orden);
    }

    public void retirarOrden(OrdenServicio orden) {
        exigirEnPreparacion();
        exigirPertenencia(orden);
        orden.retirar();
        ordenes.remove(orden);
    }

    public void iniciar() {
        exigirEnPreparacion();
        if (ordenes.isEmpty()) {
            throw new IllegalStateException("No puedes iniciar una jornada vacia.");
        }

        /*  Se hace un recorrido de todas las ordenes para verificar que estén en estado ASIGNADA y 
 que no hayan alcanzado el máximo de visitas antes de iniciar la jornada. 
 Esto asegura que todas las ordenes sean validas para iniciar una visita. */
        for (OrdenServicio orden : ordenes) {
            if (orden.getEstado() != EstadoOrden.ASIGNADA
                    || orden.getVisitas() >= orden.getMaximoVisitas()) {
                throw new IllegalStateException(
                        "La orden " + orden.getCodigo() + " no puede iniciar otra visita.");
            }
        }

        for (OrdenServicio orden : ordenes) {
            orden.iniciarVisita();
        }
        estado = EstadoJornada.EN_ATENCION;
    }

    public void registrarResultado(OrdenServicio orden, ResultadoVisita resultado) {
        if (estado != EstadoJornada.EN_ATENCION) {
            throw new IllegalStateException("La jornada debe estar en atención.");
        }
        exigirPertenencia(orden);
        if (resultado == null) {
            throw new IllegalArgumentException("Debes indicar un resultado.");
        }
        if (resultados.containsKey(orden)) {
            throw new IllegalStateException("Esta orden ya tiene resultado en esta jornada.");
        }

        orden.registrarResultado(resultado);
        resultados.put(orden, resultado);

        /*El cierre depende de la jornada actual sin importar los estados de otra jornada */
        if (resultados.size() == ordenes.size()) {
            estado = EstadoJornada.CERRADA;
        }
    }

    private void exigirEnPreparacion() {
        if (estado != EstadoJornada.EN_PREPARACION) {
            throw new IllegalStateException("La jornada debe estar en preparacion.");
        }
    }

    private void exigirPertenencia(OrdenServicio orden) {
        if (orden == null || !ordenes.contains(orden)) {
            throw new IllegalArgumentException("La orden no pertenece a esta jornada.");
        }
    }

    private BigDecimal sumarHorasAsignadas() {
        // Evita rechazar una capacidad exacta por numeros como 0.1 + 0.2.
        BigDecimal total = BigDecimal.ZERO;
        for (OrdenServicio orden : ordenes) {
            total = total.add(BigDecimal.valueOf(orden.getHorasEstimadas()));
        }
        return total;
    }

    public double getHorasAsignadas() {
        return sumarHorasAsignadas().doubleValue();
    }

    public double getCapacidadDisponible() {
        return BigDecimal.valueOf(cuadrilla.getCapacidadHoras())
                .subtract(sumarHorasAsignadas()).doubleValue();
    }

    public boolean estaCerrada() {
        return estado == EstadoJornada.CERRADA;
    }

    public int getNumero() {
        return numero;
    }

    public Cuadrilla getCuadrilla() {
        return cuadrilla;
    }

    public List<OrdenServicio> getOrdenes() {
        return Collections.unmodifiableList(new ArrayList<>(ordenes));
    }

    public EstadoJornada getEstado() {
        return estado;
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT,
                "Jornada %d | Cuadrilla: %s | Estado: %s | "
                + "Horas asignadas: %.2f | Capacidad disponible: %.2f",
                numero, cuadrilla.getCodigo(), estado,
                getHorasAsignadas(), getCapacidadDisponible());
    }
}

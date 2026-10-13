package com.mycompany.tecnihogar.modelo;

import java.util.Locale;

public abstract class OrdenServicio {

    private static final double CARGO_VISITA_ADICIONAL = 40.0;

    private final String codigo;
    private final String cliente;
    private final String direccion;
    private final double horasEstimadas;
    private final Zona zona;

    private EstadoOrden estado;
    private int visitas;
    private double montoRegistro;
    private double cargosVisitasAdicionales;
    private boolean registroCobrado;

    protected OrdenServicio(String codigo, String cliente, String direccion,
                            double horasEstimadas, Zona zona) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El codigo no puede estar vacio.");
        }
        if (cliente == null || cliente.trim().isEmpty()) {
            throw new IllegalArgumentException("El cliente no puede estar vacio.");
        }
        if (direccion == null || direccion.trim().isEmpty()) {
            throw new IllegalArgumentException("La direccion no puede estar vacia.");
        }
        if (!Double.isFinite(horasEstimadas) || horasEstimadas <= 0) {
            throw new IllegalArgumentException(
                    "Las horas estimadas deben ser finitas y mayores que cero.");
        }
        if (zona == null) {
            throw new IllegalArgumentException("Debes indicar una zona.");
        }

        this.codigo = codigo.trim();
        this.cliente = cliente.trim();
        this.direccion = direccion.trim();
        this.horasEstimadas = horasEstimadas;
        this.zona = zona;
        this.estado = EstadoOrden.PENDIENTE;
        this.visitas = 0;
        this.montoRegistro = 0.0;
        this.cargosVisitasAdicionales = 0.0;
        this.registroCobrado = false;
    }

    // Cada categoría debe implementar sus propias reglas.
    public abstract double calcularCosto();
    public abstract int getDiasAtencion();
    public abstract int getMaximoVisitas();
    public abstract String getCategoria();

    public boolean esCompatibleCon(Cuadrilla cuadrilla) {
        if (cuadrilla == null) {
            throw new IllegalArgumentException("Debes indicar una cuadrilla.");
        }
        return true;
    }

    // Despues de validar que el codigo sea unico ya se hace el cobro
    public void cobrarRegistro() {
        if (registroCobrado) {
            throw new IllegalStateException("El registro de esta orden ya se cobro.");
        }

        double costo = calcularCosto();
        if (!Double.isFinite(costo) || costo < 0) {
            throw new IllegalStateException("El costo calculado no es valido.");
        }

        montoRegistro = costo;
        registroCobrado = true;
    }

    public void asignar() {
        if (estado != EstadoOrden.PENDIENTE) {
            throw new IllegalStateException("Solo puedes asignar una orden pendiente.");
        }
        if (visitas >= getMaximoVisitas()) {
            throw new IllegalStateException("La orden agoto sus visitas permitidas.");
        }
        estado = EstadoOrden.ASIGNADA;
    }

    public void retirar() {
        if (estado != EstadoOrden.ASIGNADA) {
            throw new IllegalStateException("Solo puedes retirar una orden asignada.");
        }
        estado = EstadoOrden.PENDIENTE;
    }

    //Esta se llama al iniciar solamente si solo se asigna una orden no se llama ya que no cuenta.
    public void iniciarVisita() {
        if (estado != EstadoOrden.ASIGNADA) {
            throw new IllegalStateException("La orden debe estar asignada para iniciar una visita.");
        }
        if (visitas >= getMaximoVisitas()) {
            throw new IllegalStateException("La orden agoto sus visitas permitidas.");
        }

        visitas++;
        if (visitas > 1) {
            cargosVisitasAdicionales += CARGO_VISITA_ADICIONAL;
        }
        estado = EstadoOrden.EN_ATENCION;
    }

    public void registrarResultado(ResultadoVisita resultado) {
        if (resultado == null) {
            throw new IllegalArgumentException("Debes indicar el resultado de la visita.");
        }
        if (estado != EstadoOrden.EN_ATENCION) {
            throw new IllegalStateException("Solo puedes registrar resultados de ordenes en atencion.");
        }

        if (resultado == ResultadoVisita.COMPLETADA) {
            estado = EstadoOrden.COMPLETADA;
        } else if (visitas >= getMaximoVisitas()) {
            estado = EstadoOrden.CANCELADA;
        } else {
            estado = EstadoOrden.PENDIENTE;
        }
    }

    public String getCodigo() {
        return codigo;
    }

    public String getCliente() {
        return cliente;
    }

    public String getDireccion() {
        return direccion;
    }

    public double getHorasEstimadas() {
        return horasEstimadas;
    }

    public Zona getZona() {
        return zona;
    }

    public EstadoOrden getEstado() {
        return estado;
    }

    public int getVisitas() {
        return visitas;
    }

    public double getMontoRegistro() {
        return montoRegistro;
    }

    public double getCargosVisitasAdicionales() {
        return cargosVisitasAdicionales;
    }

    public double getIngresoTotal() {
        return montoRegistro + cargosVisitasAdicionales;
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT,
                "Orden %s | %s | Cliente: %s | Zona: %s | Horas: %.2f | "
                        + "Estado: %s | Visitas: %d | Registro: Q%.2f | "
                        + "Visitas adicionales: Q%.2f",
                codigo, getCategoria(), cliente, zona, horasEstimadas,
                estado, visitas, montoRegistro, cargosVisitasAdicionales);
    }
}

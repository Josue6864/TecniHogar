package com.mycompany.tecnihogar.modelo;

public class Cuadrilla {

    private final String codigo;
    private final double capacidadHoras;
    private final boolean certificadaRefrigeracion;

    public Cuadrilla(
            String codigo,
            double capacidadHoras,
            boolean certificadaRefrigeracion) {

        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El código de la cuadrilla no puede estar vacío."
            );
        }

        if (!Double.isFinite(capacidadHoras) || capacidadHoras <= 0) {
            throw new IllegalArgumentException(
                    "La capacidad debe ser un número finito mayor que cero."
            );
        }

        this.codigo = codigo.trim();
        this.capacidadHoras = capacidadHoras;
        this.certificadaRefrigeracion = certificadaRefrigeracion;
    }

    public String getCodigo() {
        return codigo;
    }

    public double getCapacidadHoras() {
        return capacidadHoras;
    }

    public boolean isCertificadaRefrigeracion() {
        return certificadaRefrigeracion;
    }

    @Override
    public String toString() {
        return String.format(
                "Cuadrilla %s | Capacidad: %.2f horas | "
                        + "Certificación en refrigeración: %s",
                codigo,
                capacidadHoras,
                certificadaRefrigeracion ? "Sí" : "No"
        );
    }
}
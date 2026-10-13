package com.mycompany.tecnihogar.modelo;

public class Instalacion extends OrdenServicio {

    private static final double TARIFA_POR_HORA = 90.0;
    private static final double HORAS_POR_PUNTO = 0.75;

    private final int puntos;

    public Instalacion(String codigo, String cliente, String direccion,
            double horasEstimadas, Zona zona, int puntos) {
        super(codigo, cliente, direccion, horasEstimadas, zona);

        if (puntos <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad de puntos debe ser mayor que cero.");
        }
        /*Se rechazan horas estimadas mayores que Integer.MAX_VALUE osea en numeros
        son masomenos 2147483647 horas
        en instalaciones, porque las horas facturables se manejan como int*/
        if (horasEstimadas > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "Las horas facturables exceden el rango del tipo int.");
        }

        this.puntos = puntos;
    }

    public int getPuntos() {
        return puntos;
    }

    public double getHorasPorPuntos() {
        return puntos * HORAS_POR_PUNTO;
    }

    public int getHorasFacturables() {
        double horas = Math.max(getHorasEstimadas(), getHorasPorPuntos());
        return (int) Math.ceil(horas);
    }

    @Override
    public double calcularCosto() {
        return Tarifas.baseServicio(getZona())
                + TARIFA_POR_HORA * getHorasFacturables();
    }

    @Override
    public int getDiasAtencion() {
        return getZona() == Zona.METROPOLITANA ? 3 : 5;
    }

    @Override
    public int getMaximoVisitas() {
        return 3;
    }

    @Override
    public String getCategoria() {
        return "Instalacion";
    }
}

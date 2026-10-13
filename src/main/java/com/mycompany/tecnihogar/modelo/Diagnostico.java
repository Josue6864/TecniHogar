package com.mycompany.tecnihogar.modelo;

public class Diagnostico extends OrdenServicio {

    private static final double HORAS_MAXIMAS = 1.5;
    private static final double TARIFA_METROPOLITANA = 150.0;
    private static final double TARIFA_PERIFERICA = 225.0;

    public Diagnostico(String codigo, String cliente, String direccion,
                       double horasEstimadas, Zona zona) {
        super(codigo, cliente, direccion, horasEstimadas, zona);

        if (horasEstimadas > HORAS_MAXIMAS) {
            throw new IllegalArgumentException(
                    "Un diagnóstico no puede superar 1.5 horas estimadas.");
        }
    }

    @Override
    public double calcularCosto() {
        return getZona() == Zona.METROPOLITANA
                ? TARIFA_METROPOLITANA : TARIFA_PERIFERICA;
    }

    @Override
    public int getDiasAtencion() {
        return getZona() == Zona.METROPOLITANA ? 1 : 2;
    }

    @Override
    public int getMaximoVisitas() {
        return 3;
    }

    @Override
    public String getCategoria() {
        return "Diagnóstico";
    }
}


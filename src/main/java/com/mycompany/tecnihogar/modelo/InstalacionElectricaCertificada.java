package com.mycompany.tecnihogar.modelo;

public class InstalacionElectricaCertificada extends Instalacion {

    private static final double CARGO_CERTIFICADO = 120.0;
    private static final double PORCENTAJE_GARANTIA = 0.05;

    private final double valorMateriales;

    public InstalacionElectricaCertificada(String codigo, String cliente,
            String direccion, double horasEstimadas, Zona zona,
            int puntos, double valorMateriales) {
        super(codigo, cliente, direccion, horasEstimadas, zona, puntos);

        if (!Double.isFinite(valorMateriales) || valorMateriales <= 0) {
            throw new IllegalArgumentException(
                    "El valor de los materiales debe ser finito y mayor que cero.");
        }
        this.valorMateriales = valorMateriales;
    }

    public double getValorMateriales() {
        return valorMateriales;
    }

    public double getGarantia() {
        return valorMateriales * PORCENTAJE_GARANTIA;
    }

    @Override
    public double calcularCosto() {
        return super.calcularCosto() + CARGO_CERTIFICADO + getGarantia();
    }

    @Override
    public int getDiasAtencion() {
        return super.getDiasAtencion() + 2;
    }

    @Override
    public int getMaximoVisitas() {
        return 2;
    }

    @Override
    public String getCategoria() {
        return "Instalación eléctrica certificada";
    }
}


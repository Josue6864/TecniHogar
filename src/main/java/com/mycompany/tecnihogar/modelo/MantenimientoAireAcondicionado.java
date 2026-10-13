package com.mycompany.tecnihogar.modelo;

public class MantenimientoAireAcondicionado extends OrdenServicio {

    private static final int BTU_MINIMO = 9000;
    private static final int BTU_MAXIMO = 60000;
    private static final double TARIFA_POR_HORA = 110.0;
    private static final double CARGO_GAS = 80.0;

    private final int capacidadBtu;

    public MantenimientoAireAcondicionado(String codigo, String cliente,
            String direccion, double horasEstimadas, Zona zona, int capacidadBtu) {
        super(codigo, cliente, direccion, horasEstimadas, zona);

        if (zona != Zona.METROPOLITANA) {
            throw new IllegalArgumentException(
                    "El mantenimiento de A/C solo se ofrece en zona metropolitana.");
        }
        if (capacidadBtu < BTU_MINIMO || capacidadBtu > BTU_MAXIMO) {
            throw new IllegalArgumentException(
                    "La capacidad del equipo debe estar entre 9000 y 60000 BTU.");
        }
        this.capacidadBtu = capacidadBtu;
    }

    public int getCapacidadBtu() {
        return capacidadBtu;
    }

    @Override
    public double calcularCosto() {
        return Tarifas.baseServicio(getZona())
                + TARIFA_POR_HORA * getHorasEstimadas() + CARGO_GAS;
    }

    @Override
    public int getDiasAtencion() {
        return 2;
    }

    @Override
    public int getMaximoVisitas() {
        return 1;
    }

    @Override
    public String getCategoria() {
        return "Mantenimiento de aire acondicionado";
    }

    @Override
    public boolean esCompatibleCon(Cuadrilla cuadrilla) {
        super.esCompatibleCon(cuadrilla);
        return cuadrilla.isCertificadaRefrigeracion();
    }
}

package com.mycompany.tecnihogar.modelo;

public final class Tarifas {

    private static final double BASE_METROPOLITANA = 100.0;
    private static final double BASE_PERIFERICA = 175.0;

    private Tarifas() {

    }

    public static double baseServicio(Zona zona) {
        if (zona == null) {
            throw new IllegalArgumentException(
                    "Debes indicar una zona."
            );
        }

        return zona == Zona.METROPOLITANA
                ? BASE_METROPOLITANA : BASE_PERIFERICA;
    }
}
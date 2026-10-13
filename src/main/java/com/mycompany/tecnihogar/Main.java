package com.mycompany.tecnihogar;

import com.mycompany.tecnihogar.modelo.Cuadrilla;

public class Main {

    public static void main(String[] args) {

        Cuadrilla cuadrilla = new Cuadrilla("C01", 8.0, true);

        System.out.println(cuadrilla);
        System.out.println(cuadrilla.getCodigo());                 
        System.out.println(cuadrilla.getCapacidadHoras());         
        System.out.println(cuadrilla.isCertificadaRefrigeracion()); 

        try {
            Cuadrilla invalida = new Cuadrilla("C02", 0, false);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
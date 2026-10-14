package com.mycompany.tecnihogar;

import com.mycompany.tecnihogar.modelo.*;
import com.mycompany.tecnihogar.servicio.SistemaTecniHogar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {

    private Main() {
    }

    public static void main(String[] args) {
        SistemaTecniHogar sistema = new SistemaTecniHogar();
        cargarDatosIniciales(sistema);

        System.out.println("Bienvenido a TecniHogar.");
        System.out.println("Se cargaron 8 ordenes y 3 cuadrillas sin ingresos por registro.");
        System.out.println("Para decimales usa punto, por ejemplo 2.5.");
        System.out.println("Escribe los montos sin separadores de miles, por ejemplo 2000.");

        try (Scanner entrada = new Scanner(System.in)) {
            boolean continuar = true;

            while (continuar) {
                mostrarMenu();

                /* El manejo de errores esta dentro del ciclo cualquier operacion rechazada
                nos devuelve al menu*/ 
                try {
                    int opcion = leerEntero(entrada, "Selecciona una opcion: ");

                    if (opcion == 0) {
                        continuar = false;
                        System.out.println("Gracias por utilizar TecniHogar.");
                    } else {
                        ejecutarOpcion(opcion, sistema, entrada);
                    }
                } catch (IllegalArgumentException | IllegalStateException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        } catch (NoSuchElementException e) {
            // Se finaliza tambien en dado caso se cierre el programa
            System.out.println("\nEntrada finalizada. Programa terminado.");
        }
    }

    private static void mostrarMenu() {
        System.out.println("\nTECNIHOGAR - MENU PRINCIPAL");
        System.out.println("1. Cotizar un servicio");
        System.out.println("2. Registrar una orden");
        System.out.println("3. Registrar una cuadrilla");
        System.out.println("4. Crear una jornada");
        System.out.println("5. Asignar una orden a una jornada");
        System.out.println("6. Retirar una orden de una jornada");
        System.out.println("7. Iniciar una jornada");
        System.out.println("8. Registrar el resultado de una visita");
        System.out.println("9. Consultar ordenes y cuadrillas");
        System.out.println("10. Mostrar los tres reportes de gerencia");
        System.out.println("0. Salir");
    }

    private static void ejecutarOpcion(int opcion, SistemaTecniHogar sistema,
                                       Scanner entrada) {
        switch (opcion) {
            case 1:
            case 2: {
                System.out.println("\nCATEGORIA DEL SERVICIO");
                System.out.println("1. Diagnostico");
                System.out.println("2. Instalacion");
                System.out.println("3. Instalacion electrica certificada");
                System.out.println("4. Mantenimiento de aire acondicionado");

                int categoria;
                do {
                    categoria = leerEntero(entrada, "Categoria (1-4): ");
                    if (categoria < 1 || categoria > 4) {
                        System.out.println("Selecciona una categoria del 1 al 4.");
                    }
                } while (categoria < 1 || categoria > 4);

                String codigo = "COTIZACION";
                if (opcion == 2) {
                    codigo = leerTexto(entrada, "Codigo de la nueva orden: ");
                }

                String cliente = leerTexto(entrada, "Nombre del cliente: ");
                String direccion = leerTexto(entrada, "Direccion: ");
                double horas = leerDecimal(entrada, "Horas estimadas: ");

                System.out.println("1. Metropolitana");
                System.out.println("2. Periferica");
                int opcionZona;
                do {
                    opcionZona = leerEntero(entrada, "Zona (1-2): ");
                    if (opcionZona != 1 && opcionZona != 2) {
                        System.out.println("Selecciona 1 o 2.");
                    }
                } while (opcionZona != 1 && opcionZona != 2);

                Zona zona = opcionZona == 1 ? Zona.METROPOLITANA : Zona.PERIFERICA;
                OrdenServicio orden;

                switch (categoria) {
                    case 1:
                        orden = new Diagnostico(codigo, cliente, direccion, horas, zona);
                        break;
                    case 2: {
                        int puntos = leerEntero(entrada, "Cantidad de puntos: ");
                        orden = new Instalacion(codigo, cliente, direccion, horas, zona, puntos);
                        break;
                    }
                    case 3: {
                        int puntos = leerEntero(entrada, "Cantidad de puntos: ");
                        double materiales = leerDecimal(entrada, "Valor de los materiales (Q): ");
                        orden = new InstalacionElectricaCertificada(
                                codigo, cliente, direccion, horas, zona, puntos, materiales);
                        break;
                    }
                    case 4: {
                        int btu = leerEntero(entrada, "Capacidad del equipo en BTU: (9000 a 60000) ");
                        orden = new MantenimientoAireAcondicionado(
                                codigo, cliente, direccion, horas, zona, btu);
                        break;
                    }
                    default:
                        throw new IllegalArgumentException("Categoria no valida.");
                }

                if (opcion == 1) {
                    System.out.println(sistema.cotizar(orden));
                    System.out.println("Cotizacion realizada sin registrar ni cobrar.");
                } else {
                    sistema.registrarOrden(orden);
                    System.out.printf(Locale.ROOT,
                            "Orden %s registrada. Cobro: Q%.2f%n",
                            orden.getCodigo(), orden.getMontoRegistro());
                }
                break;
            }

            case 3: {
                String codigo = leerTexto(entrada, "Codigo de la cuadrilla: ");
                double capacidad = leerDecimal(entrada, "Capacidad de horas por jornada: ");
                int certificacion;
                do {
                    certificacion = leerEntero(entrada,
                            "Tiene certificacion en refrigeracion? (1 = si, 2 = no): ");
                    if (certificacion != 1 && certificacion != 2) {
                        System.out.println("Selecciona 1 o 2.");
                    }
                } while (certificacion != 1 && certificacion != 2);

                Cuadrilla cuadrilla = new Cuadrilla(codigo, capacidad, certificacion == 1);
                sistema.registrarCuadrilla(cuadrilla);
                System.out.println("Cuadrilla registrada: " + cuadrilla.getCodigo());
                break;
            }

            case 4: {
                String codigo = leerTexto(entrada, "Codigo de la cuadrilla: ");
                Jornada jornada = sistema.crearJornada(codigo);
                System.out.println("Jornada creada con numero: " + jornada.getNumero());
                System.out.println(jornada);
                break;
            }

            case 5: {
                int numero = leerEntero(entrada, "Numero de jornada: ");
                String codigo = leerTexto(entrada, "Codigo de la orden: ");
                sistema.asignarOrden(numero, codigo);
                System.out.println("Orden asignada correctamente.");
                System.out.println(sistema.buscarJornada(numero));
                break;
            }

            case 6: {
                int numero = leerEntero(entrada, "Numero de jornada: ");
                String codigo = leerTexto(entrada, "Codigo de la orden: ");
                sistema.retirarOrden(numero, codigo);
                System.out.println("Orden retirada. Su estado vuelve a PENDIENTE.");
                break;
            }

            case 7: {
                int numero = leerEntero(entrada, "Numero de jornada: ");
                sistema.iniciarJornada(numero);
                System.out.println("Jornada iniciada. Se contabilizaron las visitas y sus cargos aplicables.");
                break;
            }

            case 8: {
                int numero = leerEntero(entrada, "Numero de jornada: ");
                String codigo = leerTexto(entrada, "Codigo de la orden: ");
                int opcionResultado;
                do {
                    opcionResultado = leerEntero(entrada,
                            "Resultado (1 = completada, 2 = fallida): ");
                    if (opcionResultado != 1 && opcionResultado != 2) {
                        System.out.println("Selecciona 1 o 2.");
                    }
                } while (opcionResultado != 1 && opcionResultado != 2);

                ResultadoVisita resultado = opcionResultado == 1
                        ? ResultadoVisita.COMPLETADA : ResultadoVisita.FALLIDA;
                sistema.registrarResultado(numero, codigo, resultado);
                System.out.println("Resultado registrado. Estado de la orden: "
                        + sistema.buscarOrden(codigo).getEstado());

                if (sistema.buscarJornada(numero).estaCerrada()) {
                    System.out.println("Jornada cerrada. La cuadrilla esta disponible.");
                } else {
                    System.out.println("La jornada sigue en atencion; faltan resultados.");
                }
                break;
            }

            case 9: {
                System.out.println("\nORDENES REGISTRADAS");
                List<OrdenServicio> ordenes = sistema.listarOrdenes();
                if (ordenes.isEmpty()) {
                    System.out.println("No hay ordenes registradas.");
                }
                for (OrdenServicio orden : ordenes) {
                    System.out.println(orden);
                    System.out.println("  Direccion: " + orden.getDireccion());
                }

                System.out.println("\nCUADRILLAS REGISTRADAS");
                List<Cuadrilla> cuadrillas = sistema.listarCuadrillas();
                if (cuadrillas.isEmpty()) {
                    System.out.println("No hay cuadrillas registradas.");
                }
                for (Cuadrilla cuadrilla : cuadrillas) {
                    System.out.println(cuadrilla);
                }
                break;
            }

            case 10: {
                System.out.println("\nREPORTE 1: ORDENES POR CATEGORIA Y ESTADO");
                Map<String, Map<EstadoOrden, Integer>> conteos =
                        sistema.contarPorCategoriaYEstado();
                if (conteos.isEmpty()) {
                    System.out.println("No hay ordenes registradas.");
                }
                for (Map.Entry<String, Map<EstadoOrden, Integer>> categoria : conteos.entrySet()) {
                    System.out.println(categoria.getKey() + ":");
                    for (EstadoOrden estado : EstadoOrden.values()) {
                        System.out.println("  " + estado + ": " + categoria.getValue().get(estado));
                    }
                }

                System.out.println("\nREPORTE 2: INGRESOS");
                System.out.printf(Locale.ROOT, "Ingresos por registro: Q%.2f%n",
                        sistema.getIngresosPorRegistro());
                System.out.printf(Locale.ROOT, "Ingresos por visitas adicionales: Q%.2f%n",
                        sistema.getIngresosPorVisitas());
                System.out.printf(Locale.ROOT, "Ingreso total: Q%.2f%n", sistema.getIngresoTotal());
                System.out.println("Ingresos por categoria:");
                for (Map.Entry<String, Double> categoria : sistema.getIngresosPorCategoria().entrySet()) {
                    System.out.printf(Locale.ROOT, "  %s: Q%.2f%n", categoria.getKey(), categoria.getValue());
                }

                System.out.println("\nREPORTE 3: JORNADAS NO CERRADAS");
                List<Jornada> abiertas = sistema.getJornadasNoCerradas();
                if (abiertas.isEmpty()) {
                    System.out.println("No hay jornadas sin cerrar.");
                }
                for (Jornada jornada : abiertas) {
                    System.out.println(jornada);
                }
                break;
            }

            default:
                System.out.println("Opcion no valida. Selecciona un numero del 0 al 10.");
        }
    }

    private static int leerEntero(Scanner entrada, String mensaje) {
        while (true) {
            String texto = leerTexto(entrada, mensaje);
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("Ingresa un numero entero valido, por ejemplo 3.");
            }
        }
    }

    private static double leerDecimal(Scanner entrada, String mensaje) {
        while (true) {
            String texto = leerTexto(entrada, mensaje);
            try {
                double valor = Double.parseDouble(texto);
                if (Double.isFinite(valor)) {
                    return valor;
                }
                System.out.println("El numero debe ser finito; no se acepta NaN ni infinito.");
            } catch (NumberFormatException e) {
                System.out.println("Ingresa un numero valido con punto decimal y sin separadores de miles.");
            }
        }
    }

    private static String leerTexto(Scanner entrada, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            if (!entrada.hasNextLine()) {
                throw new NoSuchElementException("La entrada de consola se cerro.");
            }
            String texto = entrada.nextLine().trim();
            if (!texto.isEmpty()) {
                return texto;
            }
            System.out.println("La entrada no puede estar vacia.");
        }
    }

    private static void cargarDatosIniciales(SistemaTecniHogar sistema) {
        sistema.cargarDatosIniciales();
    }
}
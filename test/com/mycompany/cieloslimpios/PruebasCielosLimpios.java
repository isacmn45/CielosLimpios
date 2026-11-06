/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.cieloslimpios;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

/**
 * Pruebas de comportamiento ejecutables sin bibliotecas externas.
 *
 * @author isacmn45
 */
public final class PruebasCielosLimpios {

    private static int pruebasAprobadas;

    private PruebasCielosLimpios() {
    }

    public static void main(String[] args) throws Exception {
        probar("La energía se mantiene entre 0 y 100", () -> {
            Dron dron = new Dron("ENERGIA-01", -20, true);
            verificar(dron.getNivelEnergia() == 0, "El constructor debe ajustar los negativos.");
            int[] entradas = {0, 100, 120, -10, Integer.MIN_VALUE, Integer.MAX_VALUE};
            int[] esperados = {0, 100, 100, 0, 0, 100};
            for (int i = 0; i < entradas.length; i++) {
                dron.setNivelEnergia(entradas[i]);
                verificar(dron.getNivelEnergia() == esperados[i],
                        "Energía incorrecta para la entrada " + entradas[i]);
            }
        });

        probar("Las series inválidas no reemplazan la serie anterior", () -> {
            esperarIllegalArgument(() -> new Dron(null, 80, false));
            esperarIllegalArgument(() -> new Dron("   ", 80, false));
            Dron dron = new Dron("SERIE-01", 80, false);
            esperarIllegalArgument(() -> dron.setCodigoSerie(null));
            esperarIllegalArgument(() -> dron.setCodigoSerie(""));
            verificar("SERIE-01".equals(dron.getCodigoSerie()),
                    "Un rechazo no debe modificar la serie.");
            dron.setCodigoSerie("SERIE-02");
            verificar("SERIE-02".equals(dron.getCodigoSerie()), "El setter debe actualizar la serie.");
        });

        probar("Los datos y los protocolos internos están encapsulados", () -> {
            for (Field campo : Dron.class.getDeclaredFields()) {
                if (!Modifier.isStatic(campo.getModifiers())) {
                    verificar(Modifier.isPrivate(campo.getModifiers()),
                            "El campo " + campo.getName() + " debe ser privado.");
                }
            }
            verificar(Modifier.isPrivate(Dron.class.getDeclaredMethod(
                    "calibrarGiroscopioInterno").getModifiers()), "La calibración debe ser privada.");
            verificar(Modifier.isPrivate(DronReconocimientoTactico.class.getDeclaredMethod(
                    "emitirAlertaIncendio").getModifiers()), "La sirena debe ser privada.");
            verificar(Modifier.isPublic(Dron.class.getMethod(
                    "transmitirEstadoRed").getModifiers()), "La telemetría debe ser pública.");
            verificar(DronReconocimientoTactico.class.getSuperclass() == Dron.class,
                    "Reconocimiento debe heredar de Dron.");
            verificar(DronCargaAmbiental.class.getSuperclass() == Dron.class,
                    "Carga debe heredar de Dron.");
        });

        probar("La telemetría refleja serie, batería y conexión", () -> {
            Dron dron = new Dron("TELEMETRIA-01", 78, false);
            verificar(!dron.isVinculadoRed(), "La conexión inicial debe respetarse.");
            String desconectado = capturar(dron::transmitirEstadoRed);
            dron.setVinculadoRed(true);
            verificar(dron.isVinculadoRed(), "El setter debe actualizar la conexión.");
            String conectado = capturar(dron::transmitirEstadoRed);
            verificar(conectado.contains("TELEMETRIA-01") && conectado.contains("78"),
                    "El reporte debe incluir serie y batería.");
            verificar(!conectado.equals(desconectado), "El reporte debe cambiar con la conexión.");
        });

        probar("El mapeo escribe siete lecturas y protege el arreglo", () -> {
            int[] secuencia = {0, 50, 35, 36, 7, 21, 49};
            RandomSecuencia sensores = new RandomSecuencia(secuencia);
            DronReconocimientoTactico dron = new DronReconocimientoTactico(
                    "MAPEO-01", 90, true, sensores);
            dron.generarMapeoEstocastico();
            verificar(Arrays.equals(secuencia, dron.getLecturasTermicas()),
                    "El ciclo debe guardar cada lectura en su cuadrante.");
            verificar(sensores.getLlamadas() == 7, "Deben generarse exactamente siete lecturas.");
            int[] copia = dron.getLecturasTermicas();
            copia[0] = 50;
            verificar(dron.getLecturasTermicas()[0] == 0, "El getter debe devolver una copia.");
        });

        probar("Las lecturas aleatorias permanecen en el intervalo 0 a 50", () -> {
            DronReconocimientoTactico dron = new DronReconocimientoTactico(
                    "MAPEO-02", 90, true, new Random(2030));
            for (int iteracion = 0; iteracion < 100; iteracion++) {
                dron.generarMapeoEstocastico();
                int[] lecturas = dron.getLecturasTermicas();
                verificar(lecturas.length == 7, "Cada mapeo debe tener siete cuadrantes.");
                for (int lectura : lecturas) {
                    verificar(lectura >= 0 && lectura <= 50, "Lectura fuera del intervalo: " + lectura);
                }
            }
        });

        probar("El umbral 35 no genera una falsa alarma", () -> {
            DronReconocimientoTactico dron = new DronReconocimientoTactico(
                    "RIESGO-01", 90, true, new RandomSecuencia(0, 35, 12, 35, 4, 20, 34));
            dron.generarMapeoEstocastico();
            verificar(!capturar(dron::analizarAmenazas).contains("¡ALERTA ROJA!"),
                    "Solo las lecturas mayores a 35 deben activar la sirena.");
        });

        probar("Varias amenazas generan una sola alerta", () -> {
            DronReconocimientoTactico dron = new DronReconocimientoTactico(
                    "RIESGO-02", 90, true, new RandomSecuencia(36, 50, 37, 49, 44, 40, 38));
            dron.generarMapeoEstocastico();
            String salida = capturar(dron::analizarAmenazas);
            String mensaje = "¡ALERTA ROJA! Posible incendio en el sector";
            verificar(ocurrencias(salida, mensaje) == 1,
                    "El análisis debe detenerse después de emitir la primera alerta.");
        });

        probar("También se detecta un riesgo en el último cuadrante", () -> {
            DronReconocimientoTactico dron = new DronReconocimientoTactico(
                    "RIESGO-03", 90, true, new RandomSecuencia(0, 10, 20, 30, 35, 1, 36));
            dron.generarMapeoEstocastico();
            verificar(capturar(dron::analizarAmenazas).contains("¡ALERTA ROJA!"),
                    "El análisis debe recorrer los siete cuadrantes.");
        });

        probar("El sensor distingue el límite exacto sin cambiar la carga", () -> {
            DronCargaAmbiental dron = new DronCargaAmbiental("CARGA-01", 80, true, 10.0);
            verificar(dron.getCargaMaxima() == 10.0, "La capacidad debe conservarse.");
            verificar(!dron.evaluarPesoObjeto(10.0), "El límite exacto no es sobrecarga.");
            verificar(dron.evaluarPesoObjeto(10.01), "Superar la capacidad debe dar true.");
            verificar(dron.getCargaActual() == 0.0, "La evaluación no debe enganchar objetos.");
            dron.engancharDesecho(4.0);
            verificar(!dron.evaluarPesoObjeto(6.0), "Debe considerar el espacio restante.");
            verificar(dron.evaluarPesoObjeto(6.01), "Debe sumar el peso y la carga actual.");
            verificar(dron.getCargaActual() == 4.0, "Evaluar debe conservar la carga previa.");
            dron.engancharDesecho(6.0);
            verificar(dron.getCargaActual() == 10.0, "Debe aceptar la capacidad exacta.");
            dron.engancharDesecho(0.1);
            verificar(dron.getCargaActual() == 10.0, "Rechazar no debe alterar la carga.");
        });

        probar("Descargar es seguro incluso con el contenedor vacío", () -> {
            DronCargaAmbiental dron = new DronCargaAmbiental("CARGA-02", 80, true, 10.0);
            dron.engancharDesecho(5.5);
            dron.descargarContenedorBase();
            verificar(dron.getCargaActual() == 0.0, "La descarga debe vaciar el contenedor.");
            dron.descargarContenedorBase();
            verificar(dron.getCargaActual() == 0.0, "Una segunda descarga debe mantener cero.");
            dron.engancharDesecho(10.0);
            verificar(dron.getCargaActual() == 10.0, "Debe poder recolectar después de descargar.");
        });

        probar("Se rechazan pesos y capacidades inválidos sin corromper el estado", () -> {
            double[] capacidadesInvalidas = {0, -1, Double.NaN,
                Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
            for (double capacidad : capacidadesInvalidas) {
                esperarIllegalArgument(() -> new DronCargaAmbiental("INVALIDO", 80, true, capacidad));
            }
            DronCargaAmbiental dron = new DronCargaAmbiental("CARGA-03", 80, true, 10.0);
            dron.engancharDesecho(3.0);
            double[] pesosInvalidos = {-1, Double.NaN,
                Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
            for (double peso : pesosInvalidos) {
                esperarIllegalArgument(() -> dron.evaluarPesoObjeto(peso));
                esperarIllegalArgument(() -> dron.engancharDesecho(peso));
                verificar(dron.getCargaActual() == 3.0, "Un peso inválido no debe alterar la carga.");
            }
            verificar(!dron.evaluarPesoObjeto(0.0), "Un peso cero no debe producir sobrecarga.");
            dron.engancharDesecho(0.0);
            verificar(dron.getCargaActual() == 3.0, "El peso cero debe conservar la carga.");
        });

        probar("La telemetría polimórfica conserva los datos base y añade los de cada modelo", () -> {
            Dron reconocimiento = new DronReconocimientoTactico("POLI-RECON", 64, true);
            DronCargaAmbiental modeloCarga = new DronCargaAmbiental("POLI-CARGA", 71, false, 10.0);
            modeloCarga.engancharDesecho(4.0);
            Dron carga = modeloCarga;

            String salidaReconocimiento = capturar(reconocimiento::transmitirEstadoRed);
            verificarContieneDatos(salidaReconocimiento,
                    "Serie: POLI-RECON", "Batería: 64%", "En línea: sí",
                    "Reconocimiento táctico", "Cuadrantes: 7");

            String salidaCarga = capturar(carga::transmitirEstadoRed);
            verificarContieneDatos(salidaCarga,
                    "Serie: POLI-CARGA", "Batería: 71%", "En línea: no",
                    "Carga ambiental", "4.00", "10.00");
        });

        System.out.println("\nResultado: " + pruebasAprobadas + " pruebas aprobadas.");
    }

    private static void probar(String nombre, Prueba prueba) throws Exception {
        capturar(prueba);
        pruebasAprobadas++;
        System.out.println("[OK] " + nombre);
    }

    private static String capturar(Prueba prueba) throws Exception {
        PrintStream anterior = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (PrintStream temporal = new PrintStream(buffer, true, StandardCharsets.UTF_8)) {
            System.setOut(temporal);
            prueba.ejecutar();
        } finally {
            System.setOut(anterior);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private static void verificar(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new AssertionError(mensaje);
        }
    }

    private static void verificarContieneDatos(String salida, String... datos) {
        verificar(salida != null, "La telemetría debe producir un reporte.");
        for (String dato : datos) {
            verificar(salida.contains(dato), "La telemetría debe incluir el dato: " + dato);
        }
    }

    private static void esperarIllegalArgument(Prueba accion) throws Exception {
        try {
            accion.ejecutar();
        } catch (IllegalArgumentException esperado) {
            return;
        }
        throw new AssertionError("Se esperaba IllegalArgumentException.");
    }

    private static int ocurrencias(String texto, String buscado) {
        int cantidad = 0;
        int indice = 0;
        while ((indice = texto.indexOf(buscado, indice)) >= 0) {
            cantidad++;
            indice += buscado.length();
        }
        return cantidad;
    }

    @FunctionalInterface
    private interface Prueba {

        void ejecutar() throws Exception;
    }

    private static final class RandomSecuencia extends Random {

        private final int[] secuencia;
        private int llamadas;

        RandomSecuencia(int... secuencia) {
            this.secuencia = secuencia.clone();
        }

        @Override
        public int nextInt(int limite) {
            verificar(limite == 51, "Random debe incluir las lecturas 0 y 50.");
            return secuencia[llamadas++ % secuencia.length];
        }

        int getLlamadas() {
            return llamadas;
        }
    }
}

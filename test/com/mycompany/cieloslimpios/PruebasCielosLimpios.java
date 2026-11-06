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

        probar("La jerarquía académica respeta cada nivel de herencia", () -> {
            verificar(Persona.class.getSuperclass() == Object.class,
                    "Persona debe ser la clase base de la jerarquía.");
            verificar(Estudiante.class.getSuperclass() == Persona.class,
                    "Estudiante debe heredar directamente de Persona.");
            verificar(Docente.class.getSuperclass() == Estudiante.class,
                    "Docente debe heredar directamente de Estudiante.");
        });

        probar("Los constructores conservan los datos heredados y propios", () -> {
            Persona persona = new Persona("AlbaPrueba", 28);
            verificar("AlbaPrueba".equals(persona.getNombre()) && persona.getEdad() == 28,
                    "Persona debe conservar nombre y edad.");
            Estudiante estudiante = new Estudiante("BrunoPrueba", 19, "EST-2030", "InformaticaPrueba");
            verificar("BrunoPrueba".equals(estudiante.getNombre()) && estudiante.getEdad() == 19,
                    "El constructor de Estudiante debe inicializar los datos de Persona.");
            verificar("EST-2030".equals(estudiante.getCarnet())
                    && "InformaticaPrueba".equals(estudiante.getCarrera()),
                    "Estudiante debe conservar carnet y carrera.");
            Docente docente = new Docente("ClaraPrueba", 45, "DOC-2030", "RoboticaPrueba", "ArquitecturaPrueba");
            verificar("ClaraPrueba".equals(docente.getNombre()) && docente.getEdad() == 45,
                    "Docente debe conservar los datos heredados de Persona.");
            verificar("DOC-2030".equals(docente.getCarnet())
                    && "RoboticaPrueba".equals(docente.getCarrera()),
                    "Docente debe conservar los datos heredados de Estudiante.");
            verificar("ArquitecturaPrueba".equals(docente.getAsignatura()),
                    "Docente debe conservar su asignatura.");
        });

        probar("Cada presentación incluye los datos del objeto completo", () -> {
            verificarContieneDatos(new Persona("DaliaPrueba", 31).presentar(),
                    "DaliaPrueba", "31");
            verificarContieneDatos(new Estudiante("ElioPrueba", 20, "EST-84", "SistemasPrueba").presentar(),
                    "ElioPrueba", "20", "EST-84", "SistemasPrueba");
            verificarContieneDatos(new Docente("FlorPrueba", 52, "DOC-91", "CienciasPrueba", "RedesPrueba").presentar(),
                    "FlorPrueba", "52", "DOC-91", "CienciasPrueba", "RedesPrueba");
        });

        probar("Un arreglo de Persona ejecuta las versiones de cada subclase", () -> {
            Persona[] personas = {
                new Persona("GabrielPrueba", 33),
                new Estudiante("HelenaPrueba", 21, "EST-POLI", "SoftwarePrueba"),
                new Docente("IvanPrueba", 40, "DOC-POLI", "IngenieriaPrueba", "HerenciaPrueba")
            };
            String[] actividades = new String[personas.length];
            for (int i = 0; i < personas.length; i++) {
                actividades[i] = personas[i].realizarActividad();
                verificar(actividades[i] != null && !actividades[i].isBlank(),
                        "Cada tipo debe describir una actividad.");
            }
            verificar(!actividades[0].equals(actividades[1])
                    && !actividades[0].equals(actividades[2])
                    && !actividades[1].equals(actividades[2]),
                    "El despacho dinámico debe producir una actividad distinta para cada clase.");
            verificarContieneDatos(personas[1].presentar(), "EST-POLI", "SoftwarePrueba");
            verificarContieneDatos(personas[2].presentar(), "DOC-POLI", "IngenieriaPrueba", "HerenciaPrueba");
        });

        probar("Los campos académicos son privados y los métodos se sobrescriben", () -> {
            Class<?>[] clases = {Persona.class, Estudiante.class, Docente.class};
            String[] metodos = {"presentar", "realizarActividad"};
            for (Class<?> clase : clases) {
                for (Field campo : clase.getDeclaredFields()) {
                    if (!Modifier.isStatic(campo.getModifiers())) {
                        verificar(Modifier.isPrivate(campo.getModifiers()),
                                "El campo " + clase.getSimpleName() + "." + campo.getName() + " debe ser privado.");
                    }
                }
                for (String nombre : metodos) {
                    java.lang.reflect.Method metodo = clase.getDeclaredMethod(nombre);
                    verificar(Modifier.isPublic(metodo.getModifiers())
                            && !Modifier.isStatic(metodo.getModifiers())
                            && metodo.getReturnType() == String.class,
                            clase.getSimpleName() + "." + nombre + " debe declarar una implementación pública de instancia.");
                }
            }
        });

        probar("La jerarquía académica valida textos y edades", () -> {
            String[] textosInvalidos = {null, "", "  \t  "};
            for (String texto : textosInvalidos) {
                esperarIllegalArgument(() -> new Persona(texto, 20));
                esperarIllegalArgument(() -> new Estudiante(texto, 20, "EST-01", "Sistemas"));
                esperarIllegalArgument(() -> new Estudiante("Ana", 20, texto, "Sistemas"));
                esperarIllegalArgument(() -> new Estudiante("Ana", 20, "EST-01", texto));
                esperarIllegalArgument(() -> new Docente(texto, 35, "DOC-01", "Sistemas", "Programacion"));
                esperarIllegalArgument(() -> new Docente("Ana", 35, texto, "Sistemas", "Programacion"));
                esperarIllegalArgument(() -> new Docente("Ana", 35, "DOC-01", texto, "Programacion"));
                esperarIllegalArgument(() -> new Docente("Ana", 35, "DOC-01", "Sistemas", texto));
            }
            esperarIllegalArgument(() -> new Persona("Ana", -1));
            esperarIllegalArgument(() -> new Estudiante("Ana", -1, "EST-01", "Sistemas"));
            esperarIllegalArgument(() -> new Docente("Ana", -1, "DOC-01", "Sistemas", "Programacion"));
            verificar(new Persona("Ana", 0).getEdad() == 0, "La edad cero es válida.");
            verificar(new Estudiante("Ana", 0, "EST-01", "Sistemas").getEdad() == 0,
                    "Estudiante debe conservar el límite de edad cero.");
            verificar(new Docente("Ana", 0, "DOC-01", "Sistemas", "Programacion").getEdad() == 0,
                    "Docente debe conservar el límite de edad cero.");
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

    private static void verificarContieneDatos(String presentacion, String... datos) {
        verificar(presentacion != null, "La presentación debe devolver un texto.");
        for (String dato : datos) {
            verificar(presentacion.contains(dato), "La presentación debe incluir el dato: " + dato);
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

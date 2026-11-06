/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.cieloslimpios;

import java.util.Locale;
import java.util.Random;

/**
 * Consola central de la operación: las dos flotas se gestionan por separado.
 *
 * @author isacmn45
 */
public class Main {

    public static void main(String[] args) {
        demostrarJerarquiaPersonas();
        if (args.length > 0 && "--personas".equals(args[0])) {
            return;
        }
        Random random = new Random();
        System.out.println("=== OPERACIÓN CIELOS LIMPIOS | AÑO 2030 ===");

        DronReconocimientoTactico[] escuadronTactico = {
            new DronReconocimientoTactico("TAC-001", 100, true),
            new DronReconocimientoTactico("TAC-002", 80, true),
            new DronReconocimientoTactico("TAC-003", 65, true)
        };

        DronCargaAmbiental[] escuadronPesado = {
            new DronCargaAmbiental("CAR-001", 90, true, 20.0),
            new DronCargaAmbiental("CAR-002", 60, true, 35.0)
        };

        System.out.println("\n--- MISIÓN DEL ESCUADRÓN TÁCTICO ---");
        for (DronReconocimientoTactico dron : escuadronTactico) {
            dron.generarMapeoEstocastico();
            dron.analizarAmenazas();
        }

        System.out.println("\n--- MISIÓN DEL ESCUADRÓN PESADO ---");
        for (DronCargaAmbiental dron : escuadronPesado) {
            double pesoNuevo = 1 + random.nextInt(50); // De 1 a 50 kg.
            System.out.printf(Locale.ROOT, "[%s] Desecho encontrado: %.2f kg.%n",
                    dron.getCodigoSerie(), pesoNuevo);
            dron.engancharDesecho(pesoNuevo);
            dron.descargarContenedorBase();
        }

        System.out.println("\n--- PRUEBA DEL SEGURO DE ENERGÍA SUPERIOR ---");
        escuadronTactico[0].setNivelEnergia(125); // Se limita a 100%.

        System.out.println("\n--- DESGASTE POST-MISIÓN Y TELEMETRÍA FINAL ---");
        // El consumo de la segunda unidad supera su batería para probar el límite 0.
        int[] desgasteTactico = {15, 120, 10};
        for (int i = 0; i < escuadronTactico.length; i++) {
            DronReconocimientoTactico dron = escuadronTactico[i];
            dron.setNivelEnergia(dron.getNivelEnergia() - desgasteTactico[i]);
            dron.setVinculadoRed(dron.getNivelEnergia() > 0);
            dron.transmitirEstadoRed();
        }

        int[] desgastePesado = {20, 100};
        for (int i = 0; i < escuadronPesado.length; i++) {
            DronCargaAmbiental dron = escuadronPesado[i];
            dron.setNivelEnergia(dron.getNivelEnergia() - desgastePesado[i]);
            dron.setVinculadoRed(dron.getNivelEnergia() > 0);
            dron.transmitirEstadoRed();
        }

        System.out.println("\n=== SUPERVISIÓN FINALIZADA ===");
    }

    private static void demostrarJerarquiaPersonas() {
        System.out.println("=== PERSONA -> ESTUDIANTE -> DOCENTE ===");
        System.out.println("Constructores encadenados con super y métodos sobreescritos.");

        Persona[] personas = {
            new Persona("Ana López", 30),
            new Estudiante("Luis Pérez", 20, "EST-001", "Ingeniería en Sistemas"),
            new Docente("María Díaz", 35, "DOC-001", "Maestría en Educación",
                    "Programación Orientada a Objetos")
        };

        for (Persona persona : personas) {
            System.out.println("\nTipo real: " + persona.getClass().getSimpleName());
            System.out.println(persona.presentar());
            System.out.println("Actividad: " + persona.realizarActividad());
        }
        System.out.println("\nSe invocaron las versiones del tipo real desde referencias Persona.");
        System.out.println("=== FIN DE LA DEMOSTRACIÓN DE HERENCIA ===\n");
    }
}

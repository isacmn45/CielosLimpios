/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.cieloslimpios;

import java.util.Objects;
import java.util.Random;

/**
 * Unidad especializada en patrullar los siete cuadrantes forestales.
 *
 * @author isacmn45
 */
public class DronReconocimientoTactico extends Dron {

    private final int[] lecturasTermicas = new int[7];
    private final Random random;

    public DronReconocimientoTactico(String codigoSerie, int nivelEnergia,
            boolean vinculadoRed) {
        this(codigoSerie, nivelEnergia, vinculadoRed, new Random());
    }

    // Permite verificar los umbrales con lecturas reproducibles en las pruebas.
    DronReconocimientoTactico(String codigoSerie, int nivelEnergia,
            boolean vinculadoRed, Random random) {
        super(codigoSerie, nivelEnergia, vinculadoRed);
        this.random = Objects.requireNonNull(random, "El generador es obligatorio.");
    }

    public int[] getLecturasTermicas() {
        return lecturasTermicas.clone();
    }

    @Override
    public void transmitirEstadoRed() {
        super.transmitirEstadoRed();
        System.out.printf("[%s] Modelo: Reconocimiento táctico | Cuadrantes: %d%n",
                getCodigoSerie(), lecturasTermicas.length);
    }

    public void generarMapeoEstocastico() {
        System.out.printf("%n[%s] Escaneando los siete cuadrantes:%n", getCodigoSerie());
        for (int i = 0; i < lecturasTermicas.length; i++) {
            lecturasTermicas[i] = random.nextInt(51); // Incluye 0 y 50.
            System.out.printf("  Sector %d: amenaza térmica %d.%n",
                    i + 1, lecturasTermicas[i]);
        }
    }

    public void analizarAmenazas() {
        for (int i = 0; i < lecturasTermicas.length; i++) {
            if (lecturasTermicas[i] > 35) {
                System.out.printf("[%s] Riesgo detectado en el sector %d: %d.%n",
                        getCodigoSerie(), i + 1, lecturasTermicas[i]);
                emitirAlertaIncendio();
                return; // Se detiene este análisis ante el primer peligro.
            }
        }
        System.out.printf("[%s] Patrulla completada sin amenazas superiores a 35.%n",
                getCodigoSerie());
    }

    private void emitirAlertaIncendio() {
        System.out.printf("[%s] ¡ALERTA ROJA! Posible incendio en el sector%n",
                getCodigoSerie());
    }
}

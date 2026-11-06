/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.cieloslimpios;

import java.util.Locale;

/**
 * Unidad recolectora de desechos; todos los pesos se expresan en kilogramos.
 *
 * @author isacmn45
 */
public class DronCargaAmbiental extends Dron {

    private final double cargaMaxima;
    private double cargaActual;

    public DronCargaAmbiental(String codigoSerie, int nivelEnergia,
            boolean vinculadoRed, double cargaMaxima) {
        super(codigoSerie, nivelEnergia, vinculadoRed);
        if (!Double.isFinite(cargaMaxima) || cargaMaxima <= 0) {
            throw new IllegalArgumentException("La carga máxima debe ser positiva y finita.");
        }
        this.cargaMaxima = cargaMaxima;
        this.cargaActual = 0;
    }

    public double getCargaMaxima() {
        return cargaMaxima;
    }

    public double getCargaActual() {
        return cargaActual;
    }

    /** Retorna true si el objeto provocaría una sobrecarga; no modifica el dron. */
    public boolean evaluarPesoObjeto(double pesoNuevo) {
        if (!Double.isFinite(pesoNuevo) || pesoNuevo < 0) {
            throw new IllegalArgumentException("El peso debe ser finito y no negativo.");
        }
        return cargaActual + pesoNuevo > cargaMaxima;
    }

    public void engancharDesecho(double pesoNuevo) {
        if (evaluarPesoObjeto(pesoNuevo)) {
            System.out.printf(Locale.ROOT,
                    "[%s] Maniobra abortada: objeto de %.2f kg demasiado pesado. "
                    + "Carga: %.2f / %.2f kg.%n",
                    getCodigoSerie(), pesoNuevo, cargaActual, cargaMaxima);
        } else {
            cargaActual += pesoNuevo;
            System.out.printf(Locale.ROOT,
                    "[%s] Recolección exitosa: %.2f kg. Carga: %.2f / %.2f kg.%n",
                    getCodigoSerie(), pesoNuevo, cargaActual, cargaMaxima);
        }
    }

    public void descargarContenedorBase() {
        System.out.printf(Locale.ROOT, "[%s] Descarga en la base: %.2f kg liberados.%n",
                getCodigoSerie(), cargaActual);
        cargaActual = 0;
    }
}

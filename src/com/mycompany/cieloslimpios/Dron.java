/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.cieloslimpios;

/**
 * Chasis común de todas las unidades de la operación Cielos Limpios.
 *
 * @author isacmn45
 */
public class Dron {

    private String codigoSerie;
    private int nivelEnergia;
    private boolean vinculadoRed;

    public Dron(String codigoSerie, int nivelEnergia, boolean vinculadoRed) {
        setCodigoSerie(codigoSerie);
        setNivelEnergia(nivelEnergia);
        this.vinculadoRed = vinculadoRed;
    }

    public String getCodigoSerie() {
        return codigoSerie;
    }

    public void setCodigoSerie(String codigoSerie) {
        if (codigoSerie == null || codigoSerie.trim().isEmpty()) {
            throw new IllegalArgumentException("El código de serie no puede estar vacío.");
        }
        this.codigoSerie = codigoSerie.trim();
    }

    public int getNivelEnergia() {
        return nivelEnergia;
    }

    /** Los valores irregulares se ajustan al porcentaje válido más cercano. */
    public void setNivelEnergia(int nivelEnergia) {
        this.nivelEnergia = Math.max(0, Math.min(100, nivelEnergia));
        if (this.nivelEnergia != nivelEnergia) {
            System.out.printf("[%s] Energía solicitada: %d%%; ajustada a %d%%.%n",
                    codigoSerie, nivelEnergia, this.nivelEnergia);
        }
        calibrarGiroscopioInterno();
    }

    public boolean isVinculadoRed() {
        return vinculadoRed;
    }

    public void setVinculadoRed(boolean vinculadoRed) {
        this.vinculadoRed = vinculadoRed;
    }

    // Private: ni el operador ni las subclases pueden invocar este protocolo.
    private void calibrarGiroscopioInterno() {
        System.out.printf("[%s] Sensores internos reajustados: giroscopio calibrado.%n",
                codigoSerie);
    }

    public void transmitirEstadoRed() {
        System.out.printf("[BALIZAS] Serie: %s | Batería: %d%% | En línea: %s%n",
                codigoSerie, nivelEnergia, vinculadoRed ? "sí" : "no");
    }
}

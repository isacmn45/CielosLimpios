/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.cieloslimpios;

/**
 * Especialización de Persona con los datos académicos de un estudiante.
 *
 * @author isacmn45
 */
public class Estudiante extends Persona {

    private final String carnet;
    private final String carrera;

    public Estudiante(String nombre, int edad, String carnet, String carrera) {
        super(nombre, edad);
        this.carnet = validarTexto(carnet, "El carnet");
        this.carrera = validarTexto(carrera, "La carrera");
    }

    public String getCarnet() {
        return carnet;
    }

    public String getCarrera() {
        return carrera;
    }

    @Override
    public String presentar() {
        return super.presentar() + " Carnet: " + carnet + ". Carrera: " + carrera + ".";
    }

    @Override
    public String realizarActividad() {
        return "Estudio la carrera de " + carrera + ".";
    }
}

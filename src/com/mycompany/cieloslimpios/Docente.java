/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.cieloslimpios;

/**
 * Tercer nivel de la jerarquía solicitada: Persona, Estudiante y Docente.
 *
 * @author isacmn45
 */
public class Docente extends Estudiante {

    private final String asignatura;

    public Docente(String nombre, int edad, String carnet, String carrera,
            String asignatura) {
        super(nombre, edad, carnet, carrera);
        this.asignatura = validarTexto(asignatura, "La asignatura");
    }

    public String getAsignatura() {
        return asignatura;
    }

    @Override
    public String presentar() {
        return super.presentar() + " Soy docente de " + asignatura + ".";
    }

    @Override
    public String realizarActividad() {
        return "Enseño la asignatura de " + asignatura + ".";
    }
}

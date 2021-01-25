package de.kai_morich.simple_bluetooth_le_terminal;

public class Resultados {
    private double fuerza;
    private double spin;
    private double velocidad;

    public Resultados(double fuerza, double spin, double velocidad) {
        this.fuerza = fuerza;
        this.spin = spin;
        this.velocidad = velocidad;
    }

    public double getFuerza() {
        return fuerza;
    }

    public void setFuerza(double fuerza) {
        this.fuerza = fuerza;
    }

    public double getSpin() {
        return spin;
    }

    public void setSpin(double spin) {
        this.spin = spin;
    }

    public double getVelocidad() {
        return velocidad;
    }

    public void setVelocidad(double velocidad) {
        this.velocidad = velocidad;
    }
}

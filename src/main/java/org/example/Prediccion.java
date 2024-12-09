package org.example;

import java.util.List;

public class Prediccion {
    //atributos de la clase
    private String lugar;
    private String fecha;
    private List<String> estadoCielo;
    private double temperaturaMax;
    private double temperaturaMin;
    private double precipitacionTotal;
    private double viento;
    private double coberturaNubosa;
    private double humedad;

    //constructor de la clase
    public Prediccion(String lugar, String fecha, List<String> estadoCielo, double temperaturaMax, double temperaturaMin, double precipitacionTotal,
                      double velocidadViento, double coberturaNubosa, double humedad) {
        this.lugar = lugar;
        this.fecha = fecha;
        this.estadoCielo = estadoCielo;
        this.temperaturaMax = temperaturaMax;
        this.temperaturaMin = temperaturaMin;
        this.precipitacionTotal = precipitacionTotal;
        this.viento = velocidadViento;
        this.coberturaNubosa = coberturaNubosa;
        this.humedad = humedad;
    }

    //getters y setters de la clase
    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getLugar() {
        return lugar;
    }

    public void setLugar(String lugar) {
        this.lugar = lugar;
    }

    public List<String> getEstadoCielo() {
        return estadoCielo;
    }

    public void setEstadoCielo(List<String> estadoCielo) {
        this.estadoCielo = estadoCielo;
    }

    public double getTemperaturaMax() {
        return temperaturaMax;
    }

    public void setTemperaturaMax(double temperaturaMax) {
        this.temperaturaMax = temperaturaMax;
    }

    public double getTemperaturaMin() {
        return temperaturaMin;
    }

    public void setTemperaturaMin(double temperaturaMin) {
        this.temperaturaMin = temperaturaMin;
    }

    public double getPrecipitacionTotal() {
        return precipitacionTotal;
    }

    public void setPrecipitacionTotal(double precipitacionTotal) {
        this.precipitacionTotal = precipitacionTotal;
    }

    public double getViento() {
        return viento;
    }

    public void setViento(double viento) {
        this.viento = viento;
    }

    public double getCoberturaNubosa() {
        return coberturaNubosa;
    }

    public void setCoberturaNubosa(double coberturaNubosa) {
        this.coberturaNubosa = coberturaNubosa;
    }

    public double getHumedad() {
        return humedad;
    }

    public void setHumedad(double humedad) {
        this.humedad = humedad;
    }

    //metodo toString
    @Override
    public String toString() {
        return  lugar + "\n" +
                "Prediccion del dia: " + fecha +
                "\nestado del cielo: " + estadoCielo +
                ", temperatura maxima: " + temperaturaMax + " ºC" +
                ", temperatura minima: " + temperaturaMin + " ºC" +
                ", \nprecipitacion total: " + precipitacionTotal + " l/m2" +
                ", velocidad del viento: " + viento + " km/h" +
                ", cobertura nubosa: " + coberturaNubosa + " %"+
                ", humedad relativa: " + humedad + " % \n -----------------------------------------------------------------------------";
    }
}

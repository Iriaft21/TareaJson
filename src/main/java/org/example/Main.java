package org.example;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Main {

    private static double roundDouble(double value, int numDecimals) {
        return new BigDecimal("" + value).setScale(numDecimals, RoundingMode.HALF_UP).doubleValue();
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int respuesta;
        String json = "./prediccion.json";
        String ApiUrl ="https://servizos.meteogalicia.gal/apiv4/getNumericForecastInfo?locationIds=71953,71940,71954,71956,71933,71938,71934&variables=temperature,wind,sky_state,precipitation_amount,relative_humidity,cloud_area_fraction&API_KEY=4hk91p9mQV1qysT4PE1YJndSRCebJhd5E1uOf07nU1bcqiR0GN1qLy3SfkRp6f4B";
        URL url = null;
        Path direccionArchivo = Paths.get("D:\\Predicciones\\25-11-2023-galicia.csv");
        List<Prediccion> predicciones = new ArrayList<>();

        conexionApi(url, ApiUrl, json);

        do{
            System.out.println("Que accion deseas realizar? \n1.Mostrar datos en pantalla \n2.Generar archivo .csv con datos de las 7 ciudades importantes \n3.Salir");
            respuesta = sc.nextInt();
            switch(respuesta){
                case 1:
                    generarPredicciones(predicciones);
                    if (predicciones.isEmpty()) {
                        System.out.println("No hay predicciones disponibles para mostrar.");
                    } else {
                        for (Prediccion p : predicciones) {
                            System.out.println(p);
                        }
                    }
                    break;
                case 2:
                    generarPredicciones(predicciones);
                    escribirCSV(direccionArchivo, predicciones);
                    break;
            }
        }while(respuesta != 3);
    }

    private static void conexionApi(URL url, String ApiUrl, String json){
        try {
            // Configurar la URL y la conexión HTTP
            url = new URL(ApiUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                StringBuilder stringBuilder = new StringBuilder();
                String linea;
                while ((linea = reader.readLine()) != null) {
                    stringBuilder.append(linea);
                }
                // Escribir JSON en un archivo (sobrescribir el archivo existente)
                Files.write(Paths.get(json), stringBuilder.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                System.out.println("Archivo escrito correctamente");
            }
        } catch (MalformedURLException e) {
            System.out.println("La URL es incorrecta: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error al leer o escribir el archivo: " + e.getMessage());
        }
    }

    private static void generarPredicciones(List<Prediccion> predicciones){
        try {
            Object obj = new JSONParser().parse(new FileReader("prediccion.json"));
            JSONObject objeto = (JSONObject) obj;
            // Obtener la lista de "features"
            JSONArray features = (JSONArray) objeto.get("features");
            // Iterar sobre las features (cada una representa un punto de predicción)
            for (Object featureObj : features) {
                JSONObject feature = (JSONObject) featureObj;
                JSONObject propiedades = (JSONObject) feature.get("properties");
                String lugar = (String) propiedades.get("name");

                JSONArray dias = (JSONArray) propiedades.get("days");
                // Iterar sobre los días
                for (Object dayObj : dias) {
                    JSONObject day = (JSONObject) dayObj;

                    // Extraer periodo de tiempo
                    JSONObject timePeriod = (JSONObject) day.get("timePeriod");
                    String fechaCompleta = (String) ((JSONObject) timePeriod.get("begin")).get("timeInstant");
                    String dia = fechaCompleta.split("T")[0]; // Extraer solo la fecha (antes de la "T")

                    JSONArray variables = (JSONArray) day.get("variables");
                    double temperaturaMaxima = 0, temperaturaMinima = Double.MAX_VALUE;
                    double viento = 0, precipitacion = 0, coberturaNubosa = 0, humedad = 0;
                    List<String> cielo = new ArrayList<>(); // Lista para guardar estados del cielo

                    // Extraer variables
                    for (Object variableObj : variables) {
                        JSONObject variable = (JSONObject) variableObj;
                        String nombreVariable = (String) variable.get("name");

                        if ("temperature".equals(nombreVariable)) {
                            JSONArray valores = (JSONArray) variable.get("values");
                            for (Object valorObj : valores) {
                                JSONObject valor = (JSONObject) valorObj;
                                double temp = ((Number) valor.get("value")).doubleValue();
                                temperaturaMaxima = Math.max(temperaturaMaxima, temp);
                                temperaturaMinima = Math.min(temperaturaMinima, temp);
                            }
                        } else if ("wind".equals(nombreVariable)) {
                            JSONArray valores = (JSONArray) variable.get("values");
                            for (Object valorObj : valores) {
                                JSONObject valor = (JSONObject) valorObj;
                                viento += ((Number) valor.get("moduleValue")).doubleValue();
                            }
                        } else if ("precipitation_amount".equals(nombreVariable)) {
                            JSONArray valores = (JSONArray) variable.get("values");
                            for (Object valorObj : valores) {
                                JSONObject valor = (JSONObject) valorObj;
                                precipitacion += ((Number) valor.get("value")).doubleValue();
                                precipitacion = roundDouble(precipitacion, 2);
                            }
                        } else if ("cloud_area_fraction".equals(nombreVariable)) {
                            JSONArray valores = (JSONArray) variable.get("values");
                            for (Object valorObj : valores) {
                                JSONObject valor = (JSONObject) valorObj;
                                coberturaNubosa += ((Number) valor.get("value")).doubleValue();
                            }
                        } else if ("relative_humidity".equals(nombreVariable)) {
                            JSONArray valores = (JSONArray) variable.get("values");
                            for (Object valorObj : valores) {
                                JSONObject valor = (JSONObject) valorObj;
                                humedad += ((Number) valor.get("value")).doubleValue();
                            }
                        } else if ("sky_state".equals(nombreVariable)) {
                            JSONArray valores = (JSONArray) variable.get("values");
                            for (Object valorObj : valores) {
                                JSONObject valor = (JSONObject) valorObj;
                                String estadoCielo = (String) valor.get("value");

                                switch (estadoCielo) {
                                    case "SUNNY":
                                        estadoCielo = "Soleado";
                                        break;
                                    case "HIGH_CLOUDS":
                                        estadoCielo = "Nubes altas";
                                        break;
                                    case "PARTLY_CLOUDY":
                                        estadoCielo = "Parcialmente nuboso";
                                        break;
                                    case "OVERCAST":
                                        estadoCielo = "Nublado";
                                        break;
                                    case "CLOUDY":
                                        estadoCielo = "Nuboso";
                                        break;
                                    case "FOG":
                                        estadoCielo = "Niebla";
                                        break;
                                    case "SHOWERS":
                                        estadoCielo = "Chubascos";
                                        break;
                                    case "OVERCAST_AND_SHOWERS":
                                        estadoCielo = "Nublado con chubascos";
                                        break;
                                    case "INTERMITENT_SNOW":
                                        estadoCielo = "Nieve intermitente";
                                        break;
                                    case "DRIZZLE":
                                        estadoCielo = "Llovizna";
                                        break;
                                    case "RAIN":
                                        estadoCielo = "Lluvia";
                                        break;
                                    case "SNOW":
                                        estadoCielo = "Nieve";
                                        break;
                                    case "STORMS":
                                        estadoCielo = "Tormentas";
                                        break;
                                    case "MIST":
                                        estadoCielo = "Neblina";
                                        break;
                                    case "FOG_BANK":
                                        estadoCielo = "Banco de niebla";
                                        break;
                                    case "MID_CLOUDS":
                                        estadoCielo = "Nubes medias";
                                        break;
                                    case "WEAK_RAIN":
                                        estadoCielo = "Lluvia débil";
                                        break;
                                    case "WEAK_SHOWERS":
                                        estadoCielo = "Chubascos débiles";
                                        break;
                                    case "STORM_THEN_CLOUDY":
                                        estadoCielo = "Tormenta y luego nuboso";
                                        break;
                                    case "MELTED_SNOW":
                                        estadoCielo = "Nieve derretida";
                                        break;
                                    case "RAIN_HayL":
                                        estadoCielo = "Granizo";
                                        break;
                                    default:
                                        break;// No hacer nada si el valor no coincide con ninguno de los casos
                                }
                                if (!cielo.contains(estadoCielo)) {
                                    cielo.add(estadoCielo); // Añadir solo si es un estado único
                                }
                            }
                        }
                    }
                    // Se hace el promedio de la velocidad del veinto, de la cobertura nubosa y l ahumedad
                    int horas = ((JSONArray) ((JSONObject) variables.get(0)).get("values")).size();
                    viento /= horas;
                    viento = roundDouble(viento, 2);

                    coberturaNubosa /= horas;
                    coberturaNubosa = roundDouble(coberturaNubosa, 2);

                    humedad /= horas;
                    humedad = roundDouble(humedad, 2);

                    // Crear instancia de predicción
                    Prediccion prediccion = new Prediccion(lugar, dia, cielo, temperaturaMaxima, temperaturaMinima, precipitacion,
                            viento, coberturaNubosa,humedad);

                    // Agregar la predicción a la lista
                    predicciones.add(prediccion);
                }
            }
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static void escribirCSV(Path direccionArchivo, List<Prediccion> predicciones){
        System.out.println("Has seleccionado crear un archivo .csv con los resultados de distintas ciudades");
        try {
            // Se obtiene la ruta del directorio que contiene el archivo .csv
            Path pathDirectorio = direccionArchivo.getParent();
            // Si no existe dicho directorio, se crea, así como el archivo csv
            if (pathDirectorio != null && !Files.exists(pathDirectorio)) {
                Files.createDirectories(pathDirectorio);
            }
            if (!Files.exists(direccionArchivo)) {
                Files.createFile(direccionArchivo);
            }
            // Escribir encabezados y datos
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(direccionArchivo.toFile(), false))) { // false para sobrescribir
                // Escribir encabezados
                bw.write("Lugar,Fecha,EstadoCielo,TemperaturaMax,TemperaturaMin,Precipitacion,Viento,CoberturaNubosa,Humedad");
                bw.newLine();
                // Verificar si hay datos
                if (predicciones.isEmpty()) {
                    System.out.println("No hay datos para escribir en el archivo CSV.");
                    return;
                }
                // Escribir datos
                for (Prediccion prediccion : predicciones) {
                    String datos = prediccion.getLugar() + "," + prediccion.getFecha() + "," + prediccion.getEstadoCielo() + ","
                            + prediccion.getTemperaturaMax() + "," + prediccion.getTemperaturaMin() + ","
                            + prediccion.getPrecipitacionTotal() + "," + prediccion.getViento() + ","
                            + prediccion.getCoberturaNubosa() + "," + prediccion.getHumedad();
                    bw.write(datos);
                    bw.newLine();
                }
                System.out.println("Archivo CSV creado y datos escritos exitosamente.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
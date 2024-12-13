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
        //devuelve el valor con dos ddecimales y redondeando para arriba
        return new BigDecimal("" + value).setScale(numDecimals, RoundingMode.HALF_UP).doubleValue();
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int respuesta;
        //ruta del archivo json
        String json = "./prediccion.json";
        //Url de la API que vamos consultar para la prediccion
        String ApiUrl ="https://servizos.meteogalicia.gal/apiv4/getNumericForecastInfo?locationIds=71953,71940,71954,71956,71933,71938,71934&variables=temperature,wind,sky_state,precipitation_amount,relative_humidity,cloud_area_fraction&API_KEY=4hk91p9mQV1qysT4PE1YJndSRCebJhd5E1uOf07nU1bcqiR0GN1qLy3SfkRp6f4B";
        URL url = null;
        //direccion donde se va a crear el .csv
        Path direccionArchivo = Paths.get("D:\\Predicciones\\25-11-2023-galicia.csv");
        //araylist donde se va a guardar las predicciones
        List<Prediccion> predicciones = new ArrayList<>();

        //llamamos al metodo que realiza la conexion con la api
        conexionApi(url, ApiUrl, json);

        do{
            //mostramos las opciones al usuario
            System.out.println("Que accion deseas realizar? \n1.Mostrar datos en pantalla \n2.Generar archivo .csv con datos de las 7 ciudades importantes \n3.Salir");
            //obtenemos su respuesta
            respuesta = sc.nextInt();
            switch(respuesta){
                case 1:
                    //llamamos al metodo que genera las predicciones
                    generarPredicciones(predicciones);
                    if (predicciones.isEmpty()) {
                        //se avisa si no hay ninguna
                        System.out.println("No hay predicciones disponibles para mostrar.");
                    } else {
                        //se imprimen las predicciones
                        for (Prediccion p : predicciones) {
                            System.out.println(p);
                        }
                    }
                    break;
                case 2:
                    //llamamos al metodo que genera las predicciones y al que escribe el csv
                    generarPredicciones(predicciones);
                    escribirCSV(direccionArchivo, predicciones);
                    break;
            }
        }while(respuesta != 3); //repetir hasta que el usuario seleccione el numero 3
    }

    private static void conexionApi(URL url, String ApiUrl, String json){
        try {
            // Configurar la URL y la conexión HTTP
            url = new URL(ApiUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            // Leer la respuesta de la API utilizando un BufferedReader
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                StringBuilder stringBuilder = new StringBuilder();
                String linea;
                // Leer cada línea de la respuesta y agregarla al StringBuilder
                while ((linea = reader.readLine()) != null) {
                    stringBuilder.append(linea);
                }
                // Escribe el contenido del StringBuilder en un archivo JSON. Sobrescribe si el archivo ya existe
                Files.write(Paths.get(json), stringBuilder.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                System.out.println("Archivo escrito correctamente");
            }
        } catch (MalformedURLException e) {
            // Manejar excepciones de URL mal formadas
            System.out.println("La URL es incorrecta: " + e.getMessage());
        } catch (IOException e) {
            // Manejar excepciones de entrada/salida
            System.out.println("Error al leer o escribir el archivo: " + e.getMessage());
        }
    }

    private static void generarPredicciones(List<Prediccion> predicciones){
        try {
            // Parsear el archivo JSON
            Object obj = new JSONParser().parse(new FileReader("prediccion.json"));
            JSONObject objeto = (JSONObject) obj;
            // Obtener la lista de "features"
            JSONArray features = (JSONArray) objeto.get("features");

            // Iterar sobre las features (cada una representa un punto de predicción)
            for (Object featureObj : features) {
                JSONObject feature = (JSONObject) featureObj;
                // Obtener el objeto "properties" de cada feature
                JSONObject propiedades = (JSONObject) feature.get("properties");
                //se obtiene el nombre del lugar de la prediccion
                String lugar = (String) propiedades.get("name");
                // Obtener la lista de días de predicción
                JSONArray dias = (JSONArray) propiedades.get("days");

                // Iterar sobre los días
                for (Object dayObj : dias) {
                    JSONObject day = (JSONObject) dayObj;

                    // Extraer periodo de tiempo
                    JSONObject timePeriod = (JSONObject) day.get("timePeriod");
                    String fechaCompleta = (String) ((JSONObject) timePeriod.get("begin")).get("timeInstant");
                    String dia = fechaCompleta.split("T")[0]; // Extraer solo la fecha (antes de la "T")

                    // Inicializar variables para las predicciones
                    JSONArray variables = (JSONArray) day.get("variables");
                    double temperaturaMaxima = 0, temperaturaMinima = Double.MAX_VALUE;
                    double viento = 0, precipitacion = 0, coberturaNubosa = 0, humedad = 0;
                    List<String> cielo = new ArrayList<>(); // Lista para guardar estados del cielo

                    // Iterar sobre las variables para extraer los valores
                    for (Object variableObj : variables) {
                        JSONObject variable = (JSONObject) variableObj;
                        String nombreVariable = (String) variable.get("name");

                        // Verificar si la variable es "temperature"
                        if ("temperature".equals(nombreVariable)) {
                            // Obtener el array de valores para la variable "temperature"
                            JSONArray valores = (JSONArray) variable.get("values");
                            // Iterar sobre cada valor en el arreglo de valores
                            for (Object valorObj : valores) {
                                JSONObject valor = (JSONObject) valorObj;
                                // Convertir el valor a tipo double
                                double temp = ((Number) valor.get("value")).doubleValue();
                                // Actualizar la temperatura máxima si el valor actual es mayor que la temperatura máxima previa
                                temperaturaMaxima = Math.max(temperaturaMaxima, temp);
                                // Actualizar la temperatura mínima si el valor actual es menor que la temperatura mínima previa
                                temperaturaMinima = Math.min(temperaturaMinima, temp);
                            }
                            // Verificar si la variable es "wind"
                        } else if ("wind".equals(nombreVariable)) {
                            // Obtener el array de valores para la variable "wind"
                            JSONArray valores = (JSONArray) variable.get("values");
                            // Iterar sobre cada valor en el arreglo de valores
                            for (Object valorObj : valores) {
                                JSONObject valor = (JSONObject) valorObj;
                                // Convertir el valor a tipo double
                                viento += ((Number) valor.get("moduleValue")).doubleValue();
                            }
                            // Verificar si la variable es "precipitation_amount"
                        } else if ("precipitation_amount".equals(nombreVariable)) {
                            // Obtener el array de valores para la variable "precipitation_amount"
                            JSONArray valores = (JSONArray) variable.get("values");
                            // Iterar sobre cada valor en el arreglo de valores
                            for (Object valorObj : valores) {
                                JSONObject valor = (JSONObject) valorObj;
                                // Convertir el valor a tipo double
                                precipitacion += ((Number) valor.get("value")).doubleValue();
                                //redondeamos el valor
                                precipitacion = roundDouble(precipitacion, 2);
                            }
                            // Verificar si la variable es "cloud_area_fraction"
                        } else if ("cloud_area_fraction".equals(nombreVariable)) {
                            // Obtener el array de valores para la variable "cloud_area_fraction"
                            JSONArray valores = (JSONArray) variable.get("values");
                            // Iterar sobre cada valor en el arreglo de valores
                            for (Object valorObj : valores) {
                                JSONObject valor = (JSONObject) valorObj;
                                // Convertir el valor a tipo double
                                coberturaNubosa += ((Number) valor.get("value")).doubleValue();
                            }
                            // Verificar si la variable es "relative_humidity"
                        } else if ("relative_humidity".equals(nombreVariable)) {
                            // Obtener el array de valores para la variable "relative_humidity"
                            JSONArray valores = (JSONArray) variable.get("values");
                            // Iterar sobre cada valor en el arreglo de valores
                            for (Object valorObj : valores) {
                                JSONObject valor = (JSONObject) valorObj;
                                // Convertir el valor a tipo double
                                humedad += ((Number) valor.get("value")).doubleValue();
                            }
                            // Verificar si la variable es "sky_state"
                        } else if ("sky_state".equals(nombreVariable)) {
                            // Obtener el array de valores para la variable "sky_state"
                            JSONArray valores = (JSONArray) variable.get("values");
                            // Iterar sobre cada valor en el arreglo de valores
                            for (Object valorObj : valores) {
                                JSONObject valor = (JSONObject) valorObj;
                                //convertir el valor a tipo String
                                String estadoCielo = (String) valor.get("value");
                                //traducimos la string de ingles a castellano
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

                    // Crear instancia de predicción con los datos extraidos
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
        // Mensaje indicando que se seleccionó crear un archivo CSV
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
                // Escribir datos de las predicciones en el archivo .csv
                for (Prediccion prediccion : predicciones) {
                    String datos = prediccion.getLugar() + "," + prediccion.getFecha() + "," + prediccion.getEstadoCielo() + ","
                            + prediccion.getTemperaturaMax() + "," + prediccion.getTemperaturaMin() + ","
                            + prediccion.getPrecipitacionTotal() + "," + prediccion.getViento() + ","
                            + prediccion.getCoberturaNubosa() + "," + prediccion.getHumedad();
                    bw.write(datos);
                    bw.newLine();
                }
                // Mensaje indicando que el archivo CSV fue creado y los datos fueron escritos exitosamente
                System.out.println("Archivo CSV creado y datos escritos exitosamente.");
            }
        } catch (IOException e) {
            // Manejar excepciones de entrada/salida y generar una excepción en tiempo de ejecución
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
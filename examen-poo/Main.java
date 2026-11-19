import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Clase administradora: menú por consola sobre una lista de Empleado
 * y una API REST sencilla que corre en un hilo aparte.
 */
public class Main {

    private static final int PUERTO = 8080;

    /** Lista compartida entre el menú y la API REST. */
    private static final List<Empleado> empleados = new ArrayList<>();

    /** Servidor HTTP, creado en un hilo aparte. */
    private static volatile HttpServer servidor;

    public static void main(String[] args) {
        // Consola en UTF-8 para que se vean tildes y ñ
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        iniciarApiEnHilo();

        int opcion;
        do {
            mostrarMenu();
            opcion = leerEnteroEntre("Elige una opción (1-7): ", 1, 7);
            switch (opcion) {
                case 1 -> crearPorDefecto();
                case 2 -> crearConParametros();
                case 3 -> modificarAtributos();
                case 4 -> visualizarObjetos();
                case 5 -> calcularSalarioDeUno();
                case 6 -> registrarHorasDeUno();
                case 7 -> System.out.println("Saliendo del sistema...");

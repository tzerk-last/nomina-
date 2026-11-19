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
                default -> { }
            }
            System.out.println();
        } while (opcion != 7);

        if (servidor != null) {
            servidor.stop(0);
        }
        Empleado.ENTRADA.close();
        System.exit(0);
    }

    // =====================================================================
    // Opciones del menú
    // =====================================================================

    private static void mostrarMenu() {
        System.out.println("===== ADMINISTRACIÓN DE NÓMINA =====");
        System.out.println("1. Crear objeto por defecto");
        System.out.println("2. Crear objeto con parámetros");
        System.out.println("3. Modificar atributos de un objeto");
        System.out.println("4. Visualizar objetos registrados");
        System.out.println("5. Ejecutar calcularSalario()");
        System.out.println("6. Ejecutar registrarHoras(dias)");
        System.out.println("7. Salir");
    }

    /** Opción 1: crea un Empleado con el constructor por defecto. */
    private static void crearPorDefecto() {
        int pos = agregar(new Empleado());
        System.out.println("Objeto por defecto creado en la posición " + pos + ".");
    }

    /** Opción 2: pide los cuatro datos y crea el objeto con el constructor de parámetros. */
    private static void crearConParametros() {
        String nombre = leerTexto("Nombre: ");
        double salario = leerDecimalNoNegativo("Salario por hora: ");
        int horas = leerEnteroNoNegativo("Horas trabajadas: ");
        double bonificacion = leerDecimalNoNegativo("Bonificación: ");

        int pos = agregar(new Empleado(nombre, salario, horas, bonificacion));
        System.out.println("Objeto creado en la posición " + pos + ".");
    }

    /** Opción 3: modifica un atributo de un objeto existente. */
    private static void modificarAtributos() {
        int pos = seleccionarPosicion("Posición del objeto a modificar: ");
        if (pos < 0) {
            return;                                    // no existe: volver al menú
        }

        Empleado e = obtener(pos);
        System.out.println("Atributo a modificar:");
        System.out.println("  1. Nombre   2. Salario por hora   3. Horas trabajadas   4. Bonificación");
        int campo = leerEnteroEntre("Opción: ", 1, 4);

        try {
            switch (campo) {
                case 1 -> e.setNombre(leerTexto("Nuevo nombre: "));
                case 2 -> e.setSalarioHora(leerDecimalNoNegativo("Nuevo salario por hora: "));
                case 3 -> e.setHorasTrabajadas(leerEnteroNoNegativo("Nuevas horas trabajadas: "));
                case 4 -> e.setBonificacion(leerDecimalNoNegativo("Nueva bonificación: "));
                default -> { }
            }
            System.out.println("Atributo actualizado.");
        } catch (IllegalArgumentException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    /** Opción 4: muestra todos los objetos registrados. */
    private static void visualizarObjetos() {
        List<Empleado> copia;
        synchronized (empleados) {
            copia = new ArrayList<>(empleados);
        }
        if (copia.isEmpty()) {
            System.out.println("No hay objetos registrados.");
            return;
        }

        System.out.println("Objetos registrados:");
        for (int i = 0; i < copia.size(); i++) {
            Empleado e = copia.get(i);
            System.out.println("  [" + i + "] " + e
                    + " | salario calculado $" + String.format(Locale.US, "%,.2f", e.calcularSalario()));
        }
    }

    /** Opción 5: ejecuta calcularSalario() sobre el objeto elegido. */
    private static void calcularSalarioDeUno() {
        int pos = seleccionarPosicion("Posición del objeto: ");
        if (pos < 0) {
            return;
        }
        Empleado e = obtener(pos);

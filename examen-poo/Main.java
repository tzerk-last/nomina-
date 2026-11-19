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
        System.out.println(String.format(Locale.US, "Salario de %s: $%,.2f",
                e.getNombre(), e.calcularSalario()));
    }

    /** Opción 6: ejecuta registrarHoras(dias) sobre el objeto elegido. */
    private static void registrarHorasDeUno() {
        int pos = seleccionarPosicion("Posición del objeto: ");
        if (pos < 0) {
            return;
        }
        int dias = leerEnteroEntre("¿Cuántos días vas a registrar?: ", 1, Integer.MAX_VALUE);
        obtener(pos).registrarHoras(dias);
    }

    // =====================================================================
    // Manejo de la lista compartida
    // =====================================================================

    /** Agrega un objeto a la lista y devuelve su posición. */
    private static int agregar(Empleado e) {
        synchronized (empleados) {
            empleados.add(e);
            return empleados.size() - 1;
        }
    }

    private static int tamanio() {
        synchronized (empleados) {
            return empleados.size();
        }
    }

    /** Devuelve el objeto en la posición dada, o null si no existe. */
    private static Empleado obtener(int pos) {
        synchronized (empleados) {
            if (pos < 0 || pos >= empleados.size()) {
                return null;
            }
            return empleados.get(pos);
        }
    }

    /**
     * Muestra la lista, pide una posición y verifica que exista.
     * Devuelve -1 si la lista está vacía o la posición no existe.
     */
    private static int seleccionarPosicion(String mensaje) {
        if (tamanio() == 0) {
            System.out.println("No hay objetos registrados.");
            return -1;
        }
        visualizarObjetos();
        int pos = leerEntero(mensaje);
        if (pos < 0 || pos >= tamanio()) {
            System.out.println("Error: no existe un objeto en la posición " + pos + ".");
            return -1;
        }
        return pos;
    }

    // =====================================================================
    // Lectura y validación de datos por consola
    // =====================================================================

    /** Pide un texto no vacío. */
    private static String leerTexto(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String texto = Empleado.ENTRADA.nextLine().trim();
            if (!texto.isEmpty()) {
                return texto;
            }
            System.out.println("Error: el valor no puede estar vacío.");
        }
    }

    /** Pide un entero; repite la pregunta si el texto no es un número. */
    private static int leerEntero(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String texto = Empleado.ENTRADA.nextLine().trim();
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("Error: escribe un número entero.");
            }
        }
    }

    /** Pide un entero dentro de un rango [min, max]. */
    private static int leerEnteroEntre(String mensaje, int min, int max) {
        while (true) {
            int valor = leerEntero(mensaje);
            if (valor >= min && valor <= max) {
                return valor;
            }
            System.out.println("Error: el valor debe estar entre " + min + " y " + max + ".");
        }
    }

    /** Pide un entero que no sea negativo. */
    private static int leerEnteroNoNegativo(String mensaje) {
        while (true) {
            int valor = leerEntero(mensaje);
            if (valor >= 0) {
                return valor;
            }
            System.out.println("Error: no se aceptan valores negativos.");
        }
    }

    /** Pide un número decimal que no sea negativo. Acepta coma o punto. */
    private static double leerDecimalNoNegativo(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String texto = Empleado.ENTRADA.nextLine().trim().replace(',', '.');
            try {
                double valor = Double.parseDouble(texto);
                if (Double.isNaN(valor) || Double.isInfinite(valor)) {
                    System.out.println("Error: escribe un número válido.");
                } else if (valor < 0) {
                    System.out.println("Error: no se aceptan valores negativos.");
                } else {
                    return valor;
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: escribe un número válido.");
            }
        }
    }

    // =====================================================================
    // API REST (com.sun.net.httpserver, puerto 8080, hilo aparte)
    // =====================================================================

    /** Crea el servidor HTTP en un hilo aparte para que el menú siga funcionando. */
    private static void iniciarApiEnHilo() {
        Thread hilo = new Thread(() -> {
            try {
                servidor = HttpServer.create(new InetSocketAddress(PUERTO), 0);
                servidor.createContext("/empleados", Main::manejarApi);
                servidor.start();
                System.out.println("API REST escuchando en http://localhost:" + PUERTO);
            } catch (IOException e) {

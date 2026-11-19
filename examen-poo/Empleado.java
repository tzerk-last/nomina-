import java.util.Scanner;

/**
 * Clase Empleado: guarda los datos de un trabajador, calcula su salario
 * y permite registrar sus horas día a día.
 */
public class Empleado {

    /**
     * Único Scanner sobre System.in para toda la aplicación.
     * Se comparte con Main para no perder datos en el buffer de entrada.
     */
    static final Scanner ENTRADA = new Scanner(System.in);

    private static final int HORAS_ORDINARIAS = 40;   // hasta aquí, salario normal
    private static final double FACTOR_EXTRA = 1.5;   // horas adicionales al 150%
    private static final int MAX_HORAS_DIA = 24;      // límite físico por día

    // ----- Atributos privados -----
    private String nombre;
    private double salarioHora;
    private int horasTrabajadas;
    private double bonificacion;

    // ----- Constructores -----

    /** Constructor por defecto. */
    public Empleado() {
        this("Sin asignar", 10000, 40, 0);
    }

    /** Constructor con parámetros. Cada valor se valida antes de asignarse. */
    public Empleado(String nombre, double salarioHora, int horasTrabajadas, double bonificacion) {
        this.nombre = validarNombre(nombre);
        this.salarioHora = validarNoNegativo(salarioHora, "El salario por hora");
        this.horasTrabajadas = validarHoras(horasTrabajadas);
        this.bonificacion = validarNoNegativo(bonificacion, "La bonificación");
    }

    // ----- Métodos de comportamiento -----

    /**
     * Calcula el salario semanal:
     *   - hasta 40 horas se pagan al salario por hora;
     *   - las horas por encima de 40 se pagan al 150%;
     *   - al total se suma la bonificación.
     */
    public double calcularSalario() {
        int horasOrdinarias = Math.min(horasTrabajadas, HORAS_ORDINARIAS);
        int horasExtra = Math.max(horasTrabajadas - HORAS_ORDINARIAS, 0);

        double pagoOrdinario = horasOrdinarias * salarioHora;
        double pagoExtra = horasExtra * salarioHora * FACTOR_EXTRA;

        return pagoOrdinario + pagoExtra + bonificacion;
    }

    /**
     * Pide con Scanner las horas trabajadas de cada uno de los días indicados.
     * Cada valor se valida (no negativo, no más de 24 horas por día), se acumula
     * y el total reemplaza a horasTrabajadas.
     */
    public void registrarHoras(int dias) {
        if (dias <= 0) {
            System.out.println("Error: el número de días debe ser mayor que cero.");
            return;
        }

        int acumulado = 0;                     // suma de las horas de todos los días
        for (int dia = 1; dia <= dias; dia++) {
            acumulado += leerHorasDelDia(dia);
        }

        horasTrabajadas = acumulado;           // actualiza el atributo
        System.out.println("Horas registradas. Total: " + horasTrabajadas + " h.");
    }

    /** Lee las horas de un día y repite la pregunta hasta obtener un valor válido. */
    private int leerHorasDelDia(int dia) {
        while (true) {
            System.out.print("Horas trabajadas el día " + dia + ": ");
            String texto = ENTRADA.nextLine().trim();
            try {
                int horas = Integer.parseInt(texto);
                if (horas < 0) {
                    System.out.println("Error: no se aceptan horas negativas.");
                } else if (horas > MAX_HORAS_DIA) {
                    System.out.println("Error: un día no puede tener más de " + MAX_HORAS_DIA + " horas.");
                } else {
                    return horas;
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: escribe un número entero.");
            }
        }
    }

    // ----- Getters -----

    public String getNombre() {
        return nombre;
    }

    public double getSalarioHora() {
        return salarioHora;
    }

    public int getHorasTrabajadas() {
        return horasTrabajadas;
    }

    public double getBonificacion() {
        return bonificacion;
    }

    // ----- Setters (validan los datos) -----

    public void setNombre(String nombre) {
        this.nombre = validarNombre(nombre);
    }

    public void setSalarioHora(double salarioHora) {
        this.salarioHora = validarNoNegativo(salarioHora, "El salario por hora");
    }

    public void setHorasTrabajadas(int horasTrabajadas) {
        this.horasTrabajadas = validarHoras(horasTrabajadas);
    }

    public void setBonificacion(double bonificacion) {
        this.bonificacion = validarNoNegativo(bonificacion, "La bonificación");
    }

    // ----- Validaciones (static: no se pueden sobrescribir desde el constructor) -----

    private static String validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }
        return nombre.trim();
    }

    private static double validarNoNegativo(double valor, String campo) {
        if (Double.isNaN(valor) || valor < 0) {
            throw new IllegalArgumentException(campo + " no puede ser negativo.");
        }
        return valor;
    }

    private static int validarHoras(int horas) {
        if (horas < 0) {
            throw new IllegalArgumentException("Las horas trabajadas no pueden ser negativas.");
        }
        return horas;
    }

    @Override
    public String toString() {
        return String.format(java.util.Locale.US,
                "%s | $%.2f/h | %d h | bonificación $%.2f",
                nombre, salarioHora, horasTrabajadas, bonificacion);
    }
}

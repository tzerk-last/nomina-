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

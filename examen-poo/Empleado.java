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


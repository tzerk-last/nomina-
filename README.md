# Liquidación de nómina

Aplicación para liquidar la nómina de empleados, con un backend en Java 21 y un frontend en React + Vite.

```
nomina-/
├── examen-poo/          Backend Java 21 (sin frameworks)
│   ├── Empleado.java    Modelo y cálculo de salario
│   └── Main.java        Menú de consola + API REST en el puerto 8080
└── frontend/            Interfaz React + Vite que consume la API
```

## Reglas de cálculo

- Hasta 40 horas se pagan al salario por hora.
- Las horas extra se pagan al 150%.
- A ese total se suma la bonificación.

## Backend

Requisito: JDK 21.

```bash
cd examen-poo
javac -d out Empleado.java Main.java
java -cp out Main
```

El menú de consola y la API corren al mismo tiempo, sobre la misma lista de empleados.

### Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/empleados` | Lista de empleados en JSON |
| GET | `/empleados/{i}/salario` | Salario calculado del empleado en la posición `i` (base 0). 404 si no existe |
| POST | `/empleados` | Crea un empleado con `nombre`, `salarioHora`, `horasTrabajadas` y `bonificacion` |
| PUT | `/empleados/{i}` | Actualiza los campos enviados |
| POST | `/empleados/{i}/horas` | Registra horas por día con `{"horas": [8, 10, 12]}`. El total reemplaza `horasTrabajadas` |

Ejemplos con curl:

```bash
curl -i http://localhost:8080/empleados
curl -i http://localhost:8080/empleados/0/salario
curl -i -X POST -H "Content-Type: application/json" \
  -d '{"nombre":"Ana","salarioHora":20000,"horasTrabajadas":45,"bonificacion":5000}' \
  http://localhost:8080/empleados
```

## Frontend

Requisito: Node.js 18 o superior.

```bash
cd frontend
npm install
npm run dev
```

Abre `http://localhost:5173`. Con el backend corriendo, la página permite crear empleados, editarlos, registrar horas y calcular salarios.

Si el backend usa otra dirección, crea `frontend/.env` con:

```
VITE_API_URL=http://localhost:8080
```

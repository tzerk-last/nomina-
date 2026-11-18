import { useEffect, useState } from "react";

// URL base del backend Java. Se puede cambiar con VITE_API_URL en un archivo .env
const API = import.meta.env.VITE_API_URL || "http://localhost:8080";

const pesos = (valor) =>
  Number(valor).toLocaleString("es-CO", { style: "currency", currency: "COP", maximumFractionDigits: 2 });

const FORMULARIO_VACIO = { nombre: "", salarioHora: "", horasTrabajadas: "40", bonificacion: "0" };

// Envía una petición al backend y devuelve el JSON. Si falla, lanza un Error con el mensaje del servidor.
async function peticion(metodo, ruta, cuerpo) {
  let res;
  try {
    res = await fetch(`${API}${ruta}`, {
      method: metodo,
      headers: cuerpo ? { "Content-Type": "application/json" } : undefined,
      body: cuerpo ? JSON.stringify(cuerpo) : undefined,
    });
  } catch {
    throw new Error(`No se pudo conectar con ${API}. ¿Está corriendo el backend?`);
  }
  const datos = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(datos.error || `Error ${res.status}`);
  return datos;
}

export default function App() {
  const [empleados, setEmpleados] = useState([]);
  const [cargando, setCargando] = useState(false);
  const [error, setError] = useState("");
  const [aviso, setAviso] = useState("");
  const [nuevo, setNuevo] = useState(FORMULARIO_VACIO);
  // Panel abierto bajo una fila: { tipo: "editar", indice, datos } o { tipo: "horas", indice, texto }
  const [panel, setPanel] = useState(null);
  // Resultado del cálculo de salario por fila: { [indice]: { valor } | { error } }
  const [salarios, setSalarios] = useState({});

  // GET /empleados
  async function cargar() {
    setCargando(true);
    setError("");
    try {
      setEmpleados(await peticion("GET", "/empleados"));
      setSalarios({});
    } catch (e) {
      setEmpleados([]);
      setError(e.message);
    } finally {
      setCargando(false);
    }
  }

  useEffect(() => {
    cargar();
  }, []);

  // Ejecuta una acción de escritura, muestra el resultado y recarga la lista
  async function ejecutar(accion, mensajeOk) {
    setError("");
    setAviso("");
    try {
      await accion();
      setAviso(mensajeOk);
      await cargar();
    } catch (e) {
      setError(e.message);
    }
  }

  // POST /empleados
  function crear(evento) {
    evento.preventDefault();
    ejecutar(async () => {
      await peticion("POST", "/empleados", nuevo);
      setNuevo(FORMULARIO_VACIO);
    }, "Empleado creado.");
  }

  // GET /empleados/{i}/salario
  async function calcular(indice) {
    setSalarios((s) => ({ ...s, [indice]: { cargando: true } }));
    try {
      const datos = await peticion("GET", `/empleados/${indice}/salario`);
      setSalarios((s) => ({ ...s, [indice]: { valor: datos.salario } }));
    } catch (e) {
      setSalarios((s) => ({ ...s, [indice]: { error: e.message } }));
    }
  }

  function abrirEditar(e) {
    setPanel({
      tipo: "editar",
      indice: e.indice,
      datos: {
        nombre: e.nombre,
        salarioHora: e.salarioHora,
        horasTrabajadas: e.horasTrabajadas,
        bonificacion: e.bonificacion,
      },
    });
  }

  function abrirHoras(e) {
    setPanel({ tipo: "horas", indice: e.indice, texto: "" });
  }

  // PUT /empleados/{i}
  function guardarEditar() {
    const { indice, datos } = panel;
    ejecutar(async () => {
      await peticion("PUT", `/empleados/${indice}`, datos);
      setPanel(null);
    }, "Empleado actualizado.");
  }

  // POST /empleados/{i}/horas
  function guardarHoras() {
    const { indice, texto } = panel;
    const dias = texto.split(",").map((s) => s.trim()).filter((s) => s !== "");
    if (dias.length === 0) {
      setError("Escribe las horas de cada día separadas por coma, por ejemplo: 8, 10, 12.");
      return;
    }
    ejecutar(async () => {
      await peticion("POST", `/empleados/${indice}/horas`, { horas: dias.map(Number) });
      setPanel(null);
    }, "Horas registradas.");
  }

  return (
    <main className="contenedor">
      <header className="cabecera">
        <h1>Liquidación de nómina</h1>
        <button onClick={cargar} disabled={cargando}>
          {cargando ? "Cargando..." : "Actualizar lista"}
        </button>
      </header>

      {error && <p className="mensaje error">{error}</p>}
      {aviso && !error && <p className="mensaje exito">{aviso}</p>}

      <section className="tarjeta">
        <h2>Nuevo empleado</h2>
        <form className="formulario" onSubmit={crear}>
          <input
            placeholder="Nombre"
            value={nuevo.nombre}
            onChange={(e) => setNuevo({ ...nuevo, nombre: e.target.value })}
          />
          <input
            type="number" min="0" placeholder="Salario por hora"
            value={nuevo.salarioHora}
            onChange={(e) => setNuevo({ ...nuevo, salarioHora: e.target.value })}
          />
          <input
            type="number" min="0" placeholder="Horas"
            value={nuevo.horasTrabajadas}
            onChange={(e) => setNuevo({ ...nuevo, horasTrabajadas: e.target.value })}
          />
          <input
            type="number" min="0" placeholder="Bonificación"
            value={nuevo.bonificacion}
            onChange={(e) => setNuevo({ ...nuevo, bonificacion: e.target.value })}
          />
          <button type="submit">Crear</button>
        </form>
      </section>

      {!error && !cargando && empleados.length === 0 && (
        <p className="mensaje">No hay empleados registrados. Crea el primero arriba.</p>
      )}

      {empleados.length > 0 && (
        <table>
          <thead>
            <tr>
              <th>#</th>
              <th>Nombre</th>
              <th>Salario/hora</th>
              <th>Horas</th>
              <th>Bonificación</th>

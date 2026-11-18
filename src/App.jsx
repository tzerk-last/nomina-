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

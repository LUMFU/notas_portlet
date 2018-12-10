package es.uned;

import java.util.HashMap;
import java.util.Map;

public class Alumno {
    
    private String nombre;
    
    private int numero;
    
    private Asignatura asignatura;
    
    private int nota;
    
    private Map<Integer,Integer> notasPruebas = new HashMap<Integer,Integer>();

    public int getNota() {
        for (Prueba p : asignatura.getPruebas()) {            
            Integer notaPrueba = notasPruebas.get(p.getOrden());
            if (notaPrueba != null && notaPrueba >= p.getNotaMinima()) {
                if (notaPrueba >= p.getNotaMinima()) {
                    nota += notaPrueba * p.getPeso();
                }
            } else if (p.isObligatoria()) {
                nota = 0;
                break;
            }
        }
        nota = nota / 100;
        return nota;
    }

    public Asignatura getAsignatura() {
        return asignatura;
    }

    public void setAsignatura(Asignatura asignatura) {
        this.asignatura = asignatura;
        nota = 0;
    }

    public Map<Integer, Integer> getNotasPruebas() {
        return notasPruebas;
    }

    public void setNotasPruebas(Map<Integer, Integer> notasPruebas) {
        this.notasPruebas = notasPruebas;
    }

    public void setNota(int nota) {
        this.nota = nota;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }
    
    public void anadirNotaPrueba(Prueba p, int nota) {
        if (nota < 0) {
            nota = 0;
        } else if (nota > 100) {
            nota = 100;
        }
        notasPruebas.put(p.getOrden(), nota); 
    }
    
    public int getNotaPrueba(int orden) {
        if (notasPruebas != null && !notasPruebas.isEmpty()) {
            return notasPruebas.get(orden); 
        }
        return 0;
    }
    
}

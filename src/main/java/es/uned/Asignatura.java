package es.uned;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Asignatura {
    
    private String nombre;
    
    private List<Prueba> pruebas = new ArrayList<Prueba>();
    
    private boolean listaCompleta = false;
    
    private List<Alumno> alumnos = new ArrayList<Alumno>();

    public List<Alumno> getAlumnos() {
        return alumnos;
    }

    public void setAlumnos(List<Alumno> alumnos) {
        this.alumnos = alumnos;
    }

    public boolean isListaCompleta() {
        return listaCompleta;
    }

    public void setListaCompleta(boolean listaCompleta) {
        this.listaCompleta = listaCompleta;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<Prueba> getPruebas() {
        return pruebas;
    }

    public void setPruebas(List<Prueba> pruebas) {
        this.pruebas = pruebas;
    }
    
    public void anadirPrueba(Prueba prueba) {
        prueba.setAsignatura(this);
        prueba.setOrden(pruebas.size() + 1);
        pruebas.add(prueba);
    }
    
    public void eliminarPrueba(Prueba prueba) {
        listaCompleta = false;
        eliminarAlumnos();
        prueba.setAsignatura(null);
        prueba.setOrden(0);
        pruebas.remove(prueba);
    }
    
    public boolean anadirAlumno(Alumno alumno) {
        for (Alumno a : alumnos) {
            if (a.getNumero() == alumno.getNumero()) {
                return false;
            }
        }
        alumnos.add(alumno);
        alumno.setAsignatura(this);
        return true;
    }
    
    public void eliminarAlumno(Alumno alumno) {
        alumnos.remove(alumno);
        alumno.setAsignatura(null);
        alumno.setNotasPruebas(new HashMap<>());
    }

    public void eliminarPruebas() {
        eliminarAlumnos();
        listaCompleta = false;
        for (Prueba p : pruebas) {
            p.setAsignatura(null);
            p.setOrden(0);
        }
        pruebas = new ArrayList<>();
    }

    public void eliminarAlumnos() {
        if (alumnos != null) {
            for (Alumno a : alumnos) {
                a.setAsignatura(null);
                a.setNotasPruebas(new HashMap<>());
            }
        }
        alumnos = new ArrayList<>();        
    }
    
    public Alumno obtenerAlumno(int numeroAlumno) {
        for (Alumno a : alumnos) {
            if (a.getNumero() == numeroAlumno) {
                return a;
            }
        }
        return null;
    }
    
    public Prueba obtenerPrueba(String nombrePrueba) {
        for (Prueba p : pruebas) {
            if (p.getNombre().equals(nombrePrueba)) {
                return p;
            }
        }
        return null;
    }
}

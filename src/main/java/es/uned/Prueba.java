package es.uned;

public class Prueba {
    
    private int orden;
    private String nombre;
    private int peso; // en porcentaje
    private int notaMinima; // sobre 100
    private boolean tipoAptoNoApto;
    private boolean obligatoria;
    private Asignatura asignatura;
    
    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public Asignatura getAsignatura() {
        return asignatura;
    }

    public void setAsignatura(Asignatura asignatura) {
        this.asignatura = asignatura;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getPeso() {
        return peso;
    }

    public void setPeso(int peso) {
        this.peso = peso;
    }

    public int getNotaMinima() {
        return notaMinima;
    }

    public void setNotaMinima(int notaMinima) {
        this.notaMinima = notaMinima;
    }

    public boolean isTipoAptoNoApto() {
        return tipoAptoNoApto;
    }

    public void setTipoAptoNoApto(boolean tipoAptoNoApto) {
        this.tipoAptoNoApto = tipoAptoNoApto;
    }

    public boolean isObligatoria() {
        return obligatoria;
    }

    public void setObligatoria(boolean obligatoria) {
        this.obligatoria = obligatoria;
    }    
    
}

package es.uned;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import javax.portlet.GenericPortlet;
import javax.portlet.HeaderRequest;
import javax.portlet.HeaderResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.ProcessAction;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.annotations.ServeResourceMethod;
import javax.servlet.http.Part;

public class NotasPortlet extends GenericPortlet {

	private static final Logger logger = LoggerFactory.getLogger(NotasPortlet.class);

        private Asignatura asignatura = new Asignatura();
        private final String separador = ";"; 
        private String errorPeso = "El peso de la asignatura tiene que ser un número entero entre 0 y 100.";
        private String errorNota = "La nota mínima de la asignatura tiene que ser un número entero entre 0 y 100.";
        
	public void renderHeaders(HeaderRequest headerRequest, HeaderResponse headerResponse)
		throws PortletException, IOException {

		String contextPath = headerRequest.getContextPath();
		String linkMarkup = "<link rel=\"stylesheet\" type=\"text/css\" href=\"" +
			contextPath + "/resources/css/main.css\"></link>";
		headerResponse.addDependency("main.css", "es.uned", null, linkMarkup);
		logger.debug("[HEADER_PHASE] Added resource: " + linkMarkup);
	}

	protected void doView(RenderRequest renderRequest, RenderResponse renderResponse)
		throws PortletException, IOException {  
            String viewPath = "/WEB-INF/views/portletViewMode.jspx";
            PortletRequestDispatcher portletRequestDispatcher =
                    getPortletContext().getRequestDispatcher(viewPath);
            portletRequestDispatcher.include(renderRequest, renderResponse);
            logger.debug("[RENDER_PHASE] Rendering view: " + viewPath);
	}
        
        @ProcessAction(name = "CambiarNombreAction")
	public void CambiarNombreAction(ActionRequest req, ActionResponse resp) 
                throws IOException, PortletException {
            resetearMensajesError(req);
            String nombre = req.getActionParameters().getValue("nombre");
            asignatura.setNombre(nombre);            
            req.getPortletSession().setAttribute("asignatura", asignatura);
        }
        
        @ProcessAction(name = "AnadirPruebaAction")
	public void AnadirPruebaAction(ActionRequest req, ActionResponse resp) 
                throws IOException, PortletException {            
            if (!asignatura.isListaCompleta()) { 
                int i = 1; 
                resetearMensajesError(req);
                String nombre = req.getActionParameters().getValue("nombrePrueba");
                String pesoS = req.getActionParameters().getValue("peso");
                String notaMinimaS = req.getActionParameters().getValue("notaMinima");
                boolean tipoAptoNoApto = (req.getActionParameters().getValue("tipo").equals("true"));
                boolean obligatoria = (req.getActionParameters().getValue("obligatoria").equals("true"));
                int peso = 0;
                if (pesoS != null && !pesoS.isEmpty()) {
                    try {
                        peso = Integer.parseInt(pesoS);
                        if (peso < 0 || peso > 100 ) {
                            req.getPortletSession().setAttribute("errorPeso", errorPeso);
                        }
                        int pesoTotal = 0;
                        for (Prueba p : asignatura.getPruebas()) {
                            pesoTotal += p.getPeso();
                        }
                        if (pesoTotal + peso  > 100) {
                            i = 0;
                            req.getPortletSession().setAttribute("errorPeso", "El peso total de las asignaturas no puede "
                                + "ser superior al 100 %");
                        }
                    } catch (NumberFormatException nfe) {
                        i = 0;
                        req.getPortletSession().setAttribute("errorPeso", errorPeso);
                    }
                }
                int notaMinima = 0;                   
                if (!tipoAptoNoApto && notaMinimaS != null && !notaMinimaS.isEmpty()) {
                    try {
                        notaMinima = Integer.parseInt(notaMinimaS);
                        if (notaMinima < 0 || notaMinima > 100 ) {
                            i = 0;
                            req.getPortletSession().setAttribute("errorNota", errorNota);
                        }
                    } catch (NumberFormatException nfe) {
                        i = 0;
                        req.getPortletSession().setAttribute("errorNota", errorNota);
                    }
                }
                if (i == 1) {
                    Prueba nuevaPrueba = new Prueba();
                    nuevaPrueba.setNombre(nombre);
                    nuevaPrueba.setPeso(peso);
                    nuevaPrueba.setNotaMinima(notaMinima);
                    nuevaPrueba.setObligatoria(obligatoria);
                    nuevaPrueba.setTipoAptoNoApto(tipoAptoNoApto);
                    asignatura.anadirPrueba(nuevaPrueba);
                    req.getPortletSession().setAttribute("asignatura", asignatura);
                }
            }
                        
        }
        
        @ProcessAction(name = "GuardarPruebasAction")
	public void GuardarPruebasAction(ActionRequest req, ActionResponse resp) 
                throws IOException, PortletException {
            resetearMensajesError(req);
            if (!asignatura.isListaCompleta()) {                
                int pesoTotal = 0;
                for (Prueba p : asignatura.getPruebas()) {
                    pesoTotal += p.getPeso();
                }
                if (pesoTotal != 100) {
                    req.getPortletSession().setAttribute("errorGuardar", "El peso de todas las pruebas debe ser 100");
                    return;
                }
                asignatura.setListaCompleta(true);
            }
        }
        
        @ProcessAction(name = "BorrarPruebasAction")
	public void BorrarPruebasAction(ActionRequest req, ActionResponse resp) 
                throws IOException, PortletException {
            resetearMensajesError(req);
            asignatura.eliminarPruebas();
        }
        
        @ProcessAction(name = "ArchivoPruebasAction")
	public void ArchivoPruebasAction(ActionRequest req, ActionResponse resp) 
                throws IOException, PortletException {
            resetearMensajesError(req);
            asignatura.setPruebas(new ArrayList<Prueba>());
            Part fichero = req.getPart("fichero");
            if (fichero != null) {
                InputStream streamFichero = fichero.getInputStream();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(streamFichero));
                String errorSubidaPruebas = "";
                String linea = buffer.readLine();
                asignatura.eliminarPruebas();
                int pesoTotal = 0;
                while (linea != null) {
                    String[] lineas = linea.split(separador);
                    try {
                        String nombrePrueba = lineas[0];
                        int pesoPrueba = 0;
                        int notaMinima = 0;
                        if (!lineas[1].trim().equals("")) {
                            pesoPrueba = Integer.parseInt(lineas[1].trim());
                        }
                        if (!lineas[2].trim().equals("")) { 
                            notaMinima = Integer.parseInt(lineas[2].trim());
                        }
                        boolean tipo = lineas[3].equalsIgnoreCase("apto/no apto");
                        boolean obligatoria = lineas[4].trim().equalsIgnoreCase("obligatoria");
                        Prueba prueba = new Prueba();
                        prueba.setNombre((nombrePrueba));
                        prueba.setNotaMinima(notaMinima);
                        prueba.setPeso(pesoPrueba);
                        prueba.setTipoAptoNoApto(tipo);
                        prueba.setObligatoria(obligatoria);
                        asignatura.anadirPrueba(prueba);
                        pesoTotal += pesoPrueba;
                        linea = buffer.readLine();
                    } catch (NullPointerException npe) {
                        asignatura.setPruebas(new ArrayList<Prueba>());
                        errorSubidaPruebas = "\nLínea: " + linea + "\nFaltan campos";
                    } catch (NumberFormatException nfe) {
                        asignatura.setPruebas(new ArrayList<Prueba>());
                        errorSubidaPruebas = "\nLínea" + linea + 
                                "\nFormato de los números incorrecto:" + lineas[1] + " - " + lineas[2];
                    }
                }
                if (pesoTotal > 100) {
                    req.getPortletSession().setAttribute("errorGuardar", "El porcentaje de las pruebas excede 100");
                } else if (pesoTotal < 100) {
                    req.getPortletSession().setAttribute("errorGuardar", 
                            "El porcentaje de las pruebas no llega a 100, hay que añadir pruebas");
                } else {                        
                    asignatura.setListaCompleta(true);
                }
                if (!errorSubidaPruebas.equals("")) {
                    errorSubidaPruebas = "Error de formato:" + errorSubidaPruebas;
                    req.getPortletSession().setAttribute("errorSubidaPruebas",errorSubidaPruebas);
                }
                req.getPortletSession().setAttribute("asignatura", asignatura);
            }
        }
        
        @ProcessAction(name = "ArchivoAlumnosAction")
	public void ArchivoAlumnosAction(ActionRequest req, ActionResponse resp) 
                throws IOException, PortletException {                
            resetearMensajesError(req);
            Part fichero = req.getPart("fichero");            
            if (fichero != null) {
                InputStream streamFichero = fichero.getInputStream();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(streamFichero));
                String errorSubidaAlumnos = "";
                String linea = buffer.readLine();
                asignatura.eliminarAlumnos();
                while (linea != null) {                    
                    try {
                        String numeroS = linea.substring(0, linea.indexOf(" "));
                        int index = linea.length();
                        if (linea.contains(" - Nota final: ")) {
                            index -= " - Nota final: ".length();
                        }
                        String nombre = linea.substring(linea.indexOf(" ") + 1, index);
                        if (nombre != null && !nombre.trim().isEmpty()) {
                            Alumno alumno = new Alumno();
                            int numero = Integer.parseInt(numeroS.trim());
                            alumno.setNumero(numero);
                            alumno.setNombre(nombre);
                            if (!asignatura.anadirAlumno(alumno)){
                                errorSubidaAlumnos += "\nEl número del alumno ya existe: " + linea;
                            }
                        } else {
                           errorSubidaAlumnos += "\nEl formato de la línea es incorrecto: " + linea;
                        }
                    } catch (NumberFormatException nfe) {
                        asignatura.eliminarAlumnos();
                        errorSubidaAlumnos += "\nFormato del número de alumno incorrecto: " + linea;
                    }
                    linea = buffer.readLine();
                }
                if (!errorSubidaAlumnos.equals("")) {
                    errorSubidaAlumnos = "Error en el fichero:" + errorSubidaAlumnos;
                }
                req.getPortletSession().setAttribute("errorSubidaAlumnos",errorSubidaAlumnos);
                req.getPortletSession().setAttribute("asignatura", asignatura);
            }
        }
        
        @ProcessAction(name = "ArchivoNotasAction")
	public void ArchivoNotasAction(ActionRequest req, ActionResponse resp) 
                throws IOException, PortletException {
            resetearMensajesError(req);
            String errorSubidaNotas = "";
            Part fichero = req.getPart("fichero");
            try {
                if (fichero != null) {
                    InputStream streamFichero = fichero.getInputStream();
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(streamFichero));
                    String linea = buffer.readLine();
                    while (linea != null) { 
                        try {
                            if (!linea.startsWith("Nota final: ")) {                                
                                String numeroAlumnoS = linea.substring(0, linea.indexOf(": "));
                                int numeroAlumno = Integer.parseInt(numeroAlumnoS.trim());
                                String[] pruebas = linea.substring(linea.indexOf(": ") +2).split(separador);
                                Alumno alumno = asignatura.obtenerAlumno(numeroAlumno);
                                if (alumno != null) {
                                    for (String subnota : pruebas) {
                                        String prueba = subnota.substring(0, subnota.indexOf("-"));
                                        String notaS = subnota.substring(subnota.indexOf("-") + 1).trim();
                                        Prueba p = asignatura.obtenerPrueba(prueba);
                                        if (p != null) {
                                            int nota = 0;
                                            if (p.isTipoAptoNoApto()) {
                                                if (notaS.trim().equalsIgnoreCase("Apto")) {
                                                    nota = 100;
                                                }
                                            } else {                                                
                                                nota = Integer.parseInt(notaS);
                                            }
                                            alumno.anadirNotaPrueba(p, nota);
                                        } else {
                                            errorSubidaNotas += "La prueba " + prueba + " no pertenece a la asignatura.\n";
                                        }
                                    }
                                } else {
                                    errorSubidaNotas += "El alumno número " + numeroAlumnoS + " no pertenece a la asignatura.\n";
                                }
                            }
                            linea = buffer.readLine();
                        } catch (Exception e) {
                            errorSubidaNotas += "Error en el formato de la línea: " + linea + "\n";
                            linea = buffer.readLine();
                        }
                    }                        
                } else {
                    errorSubidaNotas = "No se ha subido ningún fichero.";
                }                    
            } catch (Exception e) {
                errorSubidaNotas = "Error en el formato de las notas introducidas.";
            } 
            req.getPortletSession().setAttribute("errorSubidaNotas",errorSubidaNotas);
            req.getPortletSession().setAttribute("asignatura", asignatura);  
        }
        
        @ServeResourceMethod(portletNames = "notas", resourceID = "DescargarArchivoNotas", contentType = "text/plain")
	public void DescargarArchivoNotas(ResourceRequest req, ResourceResponse resp) 
                throws IOException, PortletException {
            resp.setProperty("Content-disposition", "attachment; filename=\"notas.txt\""); 
            OutputStream out = resp.getPortletOutputStream();
            StringBuilder lineasFichero = new StringBuilder();
            for (Alumno a : asignatura.getAlumnos()) {
                lineasFichero.append(a.getNumero() + ": ");
                StringBuilder pruebas =new StringBuilder();
                for (Prueba p : asignatura.getPruebas()) {
                    if (p.isTipoAptoNoApto()) {
                        if (a.getNotaPrueba(p.getOrden()) > 0) {
                            pruebas.append(p.getNombre() + "-Apto" + separador);
                        } else {                            
                            pruebas.append(p.getNombre() + "-Apto" + separador);
                        }
                    } else {
                        pruebas.append(p.getNombre() + "-" + a.getNotaPrueba(p.getOrden()) + separador);
                    }
                }
                pruebas.deleteCharAt(pruebas.length()-1);
                lineasFichero.append(pruebas.toString());
                lineasFichero.append("\nNota final: " + a.getNota() + "\n");
            }            
            out.write(lineasFichero.toString().getBytes());
            out.flush();
            out.close();
        }
        
        @ServeResourceMethod(portletNames = "notas",resourceID = "DescargarArchivoPruebas", contentType = "text/plain")
	public void DescargarArchivoPruebas(ResourceRequest req, ResourceResponse resp) 
                throws IOException, PortletException {
            resp.setProperty("Content-disposition", "attachment; filename=\"pruebas.txt\""); 
            OutputStream out = resp.getPortletOutputStream();
            StringBuilder lineasFichero = new StringBuilder();
            for (Prueba p : asignatura.getPruebas()) {
                String tipoPrueba = p.isTipoAptoNoApto() ? "apto/no apto" : "prueba numérica";
                String obligatoria = p.isObligatoria() ? "obligatoria" : "opcional";
                lineasFichero.append(p.getNombre() + separador + p.getPeso() + separador
                + p.getNotaMinima() + separador + tipoPrueba + separador + obligatoria + "\n");
            }
            out.write(lineasFichero.toString().getBytes());
            out.flush();
            out.close();
        }
        
        @ServeResourceMethod(portletNames = "notas",resourceID = "DescargarArchivoAlumnos", contentType = "text/plain")
	public void DescargarArchivoAlumnos(ResourceRequest req, ResourceResponse resp) 
                throws IOException, PortletException {
            resp.setProperty("Content-disposition", "attachment; filename=\"alumnos.txt\""); 
            OutputStream out = resp.getPortletOutputStream();
            StringBuilder lineasFichero = new StringBuilder();
            for (Alumno a : asignatura.getAlumnos()) {
                lineasFichero.append(a.getNumero() + " " + a.getNombre() + " - Nota final: " 
                    + a.getNota() + "\n");
            }
            out.write(lineasFichero.toString().getBytes());
            out.flush();
            out.close();
        }
            
        @ProcessAction(name = "PuntuarPruebasAction")
	public void PuntuarPruebasAction(ActionRequest req, ActionResponse resp) 
                throws IOException, PortletException {
            resetearMensajesError(req);
            String numeroAlumnoS = req.getActionParameters().getValue("numero");
            try {
                int numeroAlumno = Integer.parseInt(numeroAlumnoS);
                Alumno alumno = null;
                for (Alumno a : asignatura.getAlumnos()) {
                    if (numeroAlumno == a.getNumero()) {
                        alumno = a;
                        break;
                    }
                }
                if (alumno != null) {
                    for (Prueba p : asignatura.getPruebas()) {
                        String notaPruebaS = req.getActionParameters().getValue(String.valueOf(p.getOrden()));
                        int notaPrueba = 0;
                        if (notaPruebaS != null && !notaPruebaS.isEmpty()) {
                            if (p.isTipoAptoNoApto()) {
                                if (notaPruebaS.trim().equalsIgnoreCase("Apto")) {
                                    notaPrueba = 100;
                                } 
                            } else {
                                notaPrueba = Integer.parseInt(notaPruebaS);
                            }                         
                        }
                        alumno.anadirNotaPrueba(p, notaPrueba);
                    }    
                }
            } catch (Exception e) {
                req.getPortletSession().setAttribute("errorNotasAlumnos","Error en el formato de las notas introducidas.");
            }
        }
        
        public Asignatura getAsignatura() {
            return asignatura;
        }
        
        private void resetearMensajesError(ActionRequest req) {              
            req.getPortletSession().setAttribute("errorSubidaAlumnos","");
            req.getPortletSession().setAttribute("errorNota", "");
            req.getPortletSession().setAttribute("errorPeso", "");
            req.getPortletSession().setAttribute("errorSubidaPruebas","");
            req.getPortletSession().setAttribute("errorGuardar", "");
            req.getPortletSession().setAttribute("errorSubidaNotas","");
            req.getPortletSession().setAttribute("errorNotasAlumnos","");
        }
   
}
package cargaplantillas;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CargaPlantillas {

  public static Map<String, String> datosPlantilla = new HashMap<>();

  public static void main(String[] args) {
    // TODO code application logic here
    String archivoQueries = "", entorno = "";
    if (new File("/de").exists()) {
      entorno = "/de";
    } else if (new File("/qa").exists()) {
      entorno = "/qa";
    } else if (new File("/pr").exists()) {
      entorno = "/pr";
    }
    String ruta = entorno + "/ext/Send/Plantillas/";
    System.out.println("ruta " + ruta);
    if (args.length == 1) {
      archivoQueries = args[0];
    } else {
      archivoQueries = ruta + "/tabla_plantillas.txt";
      System.out.println("El nombre del archivo es: " + archivoQueries);
    }
    String[] arrayPlantillas = null;
    try {
      BufferedReader quePlantilla = new BufferedReader(
              new InputStreamReader(
                      new FileInputStream(archivoQueries),
                      "ISO-8859-1"));

      String Sentencialinea = null;
      String plantillaEncontrada = null;
      String queryInsert = null;
      StringBuilder archivoPlantilla = null;
      Date fecha_ini = new Date(new GregorianCalendar().getTimeInMillis());
      MultiFunction Function = new MultiFunction();
      System.out.println("INICIO: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SS").format(new GregorianCalendar().getTimeInMillis()) + "\r\n");
      ClassConexionOracle myConexion = new ClassConexionOracle();
      Connection conexion = myConexion.ConectBD();
      Statement Sttabla = null;
      Sttabla = conexion.createStatement();
      do {
        Sentencialinea = quePlantilla.readLine();
        if (Sentencialinea != null) {
          arrayPlantillas = Sentencialinea.split(",");
          //System.out.println("" + Sentencialinea + "  plantilla " + arrayPlantillas[0]  + " " + arrayPlantillas[5]);
          buscaArchivo(ruta, arrayPlantillas[0], arrayPlantillas[5]);
          //System.out.println("datosPlantilla " + datosPlantilla.get(arrayPlantillas[0]));
          if (!datosPlantilla.get(arrayPlantillas[0]).isEmpty()
                  && datosPlantilla.get(arrayPlantillas[0]).contains(arrayPlantillas[5])) {
            //System.out.println("plantillaEncontrada " + datosPlantilla.get(arrayPlantillas[0]));
            archivoPlantilla = Modihmtl(datosPlantilla.get(arrayPlantillas[0]));
            queryInsert = "UPDATE A003_CATALOGO_PLANTILLAS SET "
                    + "FECHA_ACTIVACION_CPL = TO_DATE('" + arrayPlantillas[2]
                    + "','DD/MM/YY'), FECHA_DESACTIVACION_CPL = TO_DATE('" + arrayPlantillas[3]
                    + "','DD/MM/YY'), CORREO_MUESTRA_CPL = '" + arrayPlantillas[4]
                    + "', CONTENIDO_HTML_CPL = '" + archivoPlantilla.toString()
                    + "' WHERE ID_CPL='" + arrayPlantillas[0] + "' AND TIPO_PRODUCTO_CPL='" + arrayPlantillas[1] + "'";
            System.out.println("queryInsert-> " + queryInsert);
            Sttabla.executeQuery(queryInsert);
            Sttabla.executeQuery("COMMIT");
            
          }
        }
      } while (Sentencialinea != null);
      Sttabla.close();
    } catch (UnsupportedEncodingException | FileNotFoundException ex) {
      Logger.getLogger(CargaPlantillas.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
      Logger.getLogger(CargaPlantillas.class.getName()).log(Level.SEVERE, null, ex);
    } catch (SQLException ex) {
      Logger.getLogger(CargaPlantillas.class.getName()).log(Level.SEVERE, null, ex);
    }

  }

  public static void buscaArchivo(String ruta, String codigo, String valor) {
    String retorno = "NA";
    File contenidoArchivo;
    contenidoArchivo = new File(ruta);
    String[] ArchivosExistentes = contenidoArchivo.list();
    for (String ArchivosExistente : ArchivosExistentes) {
      if (new File(ruta + ArchivosExistente).isDirectory()) {
        buscaArchivo(ruta + ArchivosExistente + "/", codigo, valor);
      } else {
        if (ArchivosExistente.contains(valor)) {
          retorno = ruta + ArchivosExistente;
          datosPlantilla.put(codigo, retorno);
        }
      }
    }
  }

  public static StringBuilder Modihmtl(String Plantilla) {
    File PlantillaHTML = new File(Plantilla);
    StringBuilder salvar = new StringBuilder();
    if (PlantillaHTML.exists()) {
      String linea = "";
      File archivo = null;
      FileReader fr = null;
      BufferedReader br = null;
      try {
        archivo = new File(PlantillaHTML.getAbsolutePath());
        fr = new FileReader(archivo);
        br = new BufferedReader(fr);
        while ((linea = br.readLine()) != null) {
          salvar.append(linea + "\r\n");
        }
      } catch (IOException ex) {
        System.out.println("ErrorIO presente en ModificaHtml " + ex.getMessage());
        ex.printStackTrace();
      } finally {
        if (fr != null) {
          try {
            fr.close();
          } catch (IOException ex) {
            System.out.println("ErrorIO presente en el finally de ModificaHtml " + ex.getMessage());
            ex.printStackTrace();
          }
        }
      }
    } else {
      salvar.append("N");
    }
    return salvar;
  }

}

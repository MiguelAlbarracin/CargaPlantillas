package cargaplantillas;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MultiFunction
{
  public static String SumarDias(String Fecha, String Dias)
  {
    System.out.println("Dias funcion " + Dias);
    
    String[] datesplit = new String[4];
    MultiFunction Function = new MultiFunction();
    datesplit = Fecha.split("-");
    String retorna = "";
    Calendar Caldate = Calendar.getInstance();
    int year = Integer.parseInt(datesplit[0]);
    int month = Integer.parseInt(datesplit[1]);
    int day = Integer.parseInt(datesplit[2]);
    int dias = day + Integer.parseInt(Dias);
    Caldate.set(year, month - 1, dias);
    retorna = getDateString(Caldate, "yyyy-MM-dd");
    
    return retorna;
  }
  
  public String Evalua_Estado_Evento(String Cod_Evento_Funcion, Connection conexion)
  {
    MultiFunction Function = new MultiFunction();
    String[] Arrr_Aux = new String[4];
    
    boolean Ejecutar = true;
    String Procesos_Evento = "";
    Date cfecha = new Date();
    String hoy = Function.getDateString(cfecha, "yyyy-MM-dd");
    
    String error = "";
    String[] message = new String[3];
    String observaciones_Err = "";
    String codusr = "";
    String usuario = "";
    
    String sql_evento = "";
    String Estado_Esperado_Dep = "";
    try
    {
      Statement _stsql_aux_1 = conexion.createStatement();
      Statement _stsql_aux_2 = conexion.createStatement();
      Statement _stsql_aux_3 = conexion.createStatement();
      Statement _stserr = conexion.createStatement();
      
      String Base_Oracle = Function.TraeDirRefOracle("BASE_ORACLE", _stsql_aux_1);
      String Codigo_Evento_Dep = Function.TraeCampoEspecifico(Base_Oracle, "EVENTOS", "COD_EVENTO", Cod_Evento_Funcion, "Varchar", "COD_EVENT_DEPEND", _stsql_aux_1);
      String Codigo_Evento_Final = Function.TraeDirRefOracle("Cod_Evento Final", _stsql_aux_1);
      String Estado_Esperado_Final = Function.TraeCampoEspecifico(Base_Oracle, "EVENTOS", "COD_EVENTO", Codigo_Evento_Final, "Varchar", "ESTADO_ESPERADO", _stsql_aux_1);
      
      String Aux = Function.TraeDirRefOracle("Dias_Seguimiento_Eventos", _stsql_aux_1);
      int Num_Dias = Integer.parseInt(Aux);
      
      String Fecha_Lim = Function.TraeFecha(hoy, "", Num_Dias, "Resta", "Fecha");
      if (Codigo_Evento_Dep.indexOf("&") > 0)
      {
        Arrr_Aux = Codigo_Evento_Dep.split("&");
        Estado_Esperado_Dep = Function.TraeCampoEspecifico(Base_Oracle, "EVENTOS", "COD_EVENTO", Arrr_Aux[0], "Varchar", "ESTADO_ESPERADO", _stsql_aux_1);
        String Estado_Esperado_Dep_1 = Function.TraeCampoEspecifico(Base_Oracle, "EVENTOS", "COD_EVENTO", Arrr_Aux[1], "Varchar", "ESTADO_ESPERADO", _stsql_aux_1);
        sql_evento = "SELECT * FROM " + Base_Oracle + ".SEGUIMIENTO_EVENTOS WHERE COD_EVENTO = '";
        sql_evento = sql_evento + Arrr_Aux[0] + "' AND ESTADO_ACTUAL = '" + Estado_Esperado_Dep + "' ";
        sql_evento = sql_evento + "AND FECHA_MEDICION >= to_date('" + Fecha_Lim + "','yyyy/MM/dd')";
        System.out.println("evento " + sql_evento);
        Ejecutar = true;
        try
        {
          ResultSet Row_evento_1 = _stsql_aux_2.executeQuery(sql_evento);
          while (Row_evento_1.next())
          {
            sql_evento = "SELECT * FROM " + Base_Oracle + ".SEGUIMIENTO_EVENTOS WHERE COD_EVENTO = '" + Arrr_Aux[1] + "' AND ESTADO_ACTUAL = '" + Estado_Esperado_Dep_1 + "'";
            sql_evento = sql_evento + "AND COD_PROCESO = '" + Row_evento_1.getString("COD_PROCESO") + "'";
            try
            {
              ResultSet Row_evento = _stsql_aux_3.executeQuery(sql_evento);
              while (Row_evento.next())
              {
                String Estado_Esperado = Function.TraeCampoEspecifico(Base_Oracle, "EVENTOS", "COD_EVENTO", Cod_Evento_Funcion, "Varchar", "ESTADO_ESPERADO", _stsql_aux_1);
                String Estado_Aceptado = Function.TraeCampoEspecifico(Base_Oracle, "EVENTOS", "COD_EVENTO", Cod_Evento_Funcion, "Varchar", "ESTADO_ACEPTABLE", _stsql_aux_1);
                String Estado_Critico = Function.TraeCampoEspecifico(Base_Oracle, "EVENTOS", "COD_EVENTO", Cod_Evento_Funcion, "Varchar", "ESTADO_CRITICO", _stsql_aux_1);
                String sql_evento_fin = "SELECT * FROM " + Base_Oracle + ".SEGUIMIENTO_EVENTOS WHERE COD_EVENTO = '" + Cod_Evento_Funcion + "' ";
                sql_evento_fin = sql_evento_fin + "AND COD_PROCESO = '" + Row_evento.getString("COD_PROCESO") + "'";
                try
                {
                  Ejecutar = true;
                  ResultSet Row_est_env = _stsql_aux_1.executeQuery(sql_evento_fin);
                  while (Row_est_env.next()) {
                    Ejecutar = false;
                  }
                  if (Ejecutar) {
                    Procesos_Evento = Procesos_Evento + Row_evento.getString("COD_PROCESO") + ";";
                  }
                  Row_est_env.close();
                }
                catch (SQLException e)
                {
                  e.printStackTrace();
                  observaciones_Err = "Revisa que el evento no haya terminado";
                  error = e.getMessage();
                  message = error.split(":");
                  Function.Errores(codusr, usuario, message[0], message[1], sql_evento_fin, "Genera_Distribucion.java", observaciones_Err, "N", "Genera_Distribucion", _stserr);
                  System.exit(1);
                }
              }
              Row_evento.close();
            }
            catch (SQLException e)
            {
              e.printStackTrace();
              observaciones_Err = "Consulta los eventos que cumplan con la condicion del evento";
              error = e.getMessage();
              message = error.split(":");
              Function.Errores(codusr, usuario, message[0], message[1], sql_evento, "Genera_Distribucion.java", observaciones_Err, "N", "Genera_Distribucion", _stserr);
              System.exit(1);
            }
          }
          Row_evento_1.close();
        }
        catch (SQLException e)
        {
          e.printStackTrace();
          observaciones_Err = "Consulta los eventos que cumplan con la condicion del evento";
          error = e.getMessage();
          message = error.split(":");
          Function.Errores(codusr, usuario, message[0], message[1], sql_evento, "Genera_Distribucion.java", observaciones_Err, "N", "Genera_Distribucion", _stserr);
          System.exit(1);
        }
      }
      else
      {
        Ejecutar = true;
        Codigo_Evento_Dep = Function.TraeCampoEspecifico(Base_Oracle, "EVENTOS", "COD_EVENTO", Cod_Evento_Funcion, "Varchar", "COD_EVENT_DEPEND", _stsql_aux_1);
        Estado_Esperado_Dep = Function.TraeCampoEspecifico(Base_Oracle, "EVENTOS", "COD_EVENTO", Codigo_Evento_Dep, "Varchar", "ESTADO_ESPERADO", _stsql_aux_1);
        
        sql_evento = "SELECT * FROM " + Base_Oracle + ".SEGUIMIENTO_EVENTOS WHERE COD_EVENTO = '";
        sql_evento = sql_evento + Codigo_Evento_Dep + "' AND ESTADO_ACTUAL = '" + Estado_Esperado_Dep + "' ";
        sql_evento = sql_evento + "AND FECHA_MEDICION >= to_date('" + Fecha_Lim + "','yyyy/MM/dd')";
        System.out.println("evento " + sql_evento);
        try
        {
          ResultSet Row_evento = _stsql_aux_2.executeQuery(sql_evento);
          while (Row_evento.next())
          {
            String Estado_Esperado = Function.TraeCampoEspecifico(Base_Oracle, "EVENTOS", "COD_EVENTO", Cod_Evento_Funcion, "Varchar", "ESTADO_ESPERADO", _stsql_aux_1);
            String Estado_Aceptado = Function.TraeCampoEspecifico(Base_Oracle, "EVENTOS", "COD_EVENTO", Cod_Evento_Funcion, "Varchar", "ESTADO_ACEPTABLE", _stsql_aux_1);
            String Estado_Critico = Function.TraeCampoEspecifico(Base_Oracle, "EVENTOS", "COD_EVENTO", Cod_Evento_Funcion, "Varchar", "ESTADO_CRITICO", _stsql_aux_1);
            String sql_evento_fin = "SELECT * FROM " + Base_Oracle + ".SEGUIMIENTO_EVENTOS WHERE COD_EVENTO = '" + Cod_Evento_Funcion + "' ";
            sql_evento_fin = sql_evento_fin + "AND COD_PROCESO = '" + Row_evento.getString("COD_PROCESO") + "'";
            System.out.println("pregunta 2" + sql_evento_fin);
            try
            {
              Ejecutar = true;
              ResultSet Row_est_env = _stsql_aux_3.executeQuery(sql_evento_fin);
              while (Row_est_env.next()) {
                Ejecutar = false;
              }
              if (Ejecutar)
              {
                Procesos_Evento = Procesos_Evento + Row_evento.getString("COD_PROCESO") + ";";
                System.out.println("disponibles " + Procesos_Evento);
              }
              Row_est_env.close();
            }
            catch (SQLException e)
            {
              e.printStackTrace();
              observaciones_Err = "Revisa que el evento no haya terminado";
              error = e.getMessage();
              message = error.split(":");
              Function.Errores(codusr, usuario, message[0], message[1], sql_evento_fin, "Genera_Zonificacion.java", observaciones_Err, "N", "Genera_Zonificacion", _stserr);
              System.exit(1);
            }
          }
          Row_evento.close();
        }
        catch (SQLException e)
        {
          e.printStackTrace();
          observaciones_Err = "Consulta los eventos que cumplan con la condicion del evento";
          error = e.getMessage();
          message = error.split(":");
          Function.Errores(codusr, usuario, message[0], message[1], sql_evento, "Genera_Distribucion.java", observaciones_Err, "N", "Genera_Distribucion", _stserr);
          System.exit(1);
        }
      }
      return Procesos_Evento;
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }
    return null;
  }
  
  public void Crea_Log(String Dir_Log, String Nombre_Log, String Mensaje_Log, Connection conexion)
  {
    MultiFunction Function = new MultiFunction();
    String Direccion = "";
    String Nombre="";
    String Mensaje="";
    String[] Arr_Dir = new String[2];
    BufferedWriter fDatosSeg = null;
    Date cfecha = new Date();
    String hoy = Function.getDateString(cfecha, "dd/MM/yyyy HH:mm:ss");
    Statement _stsql = null;
    try{
        _stsql = conexion.createStatement();
        Direccion = Function.TraeDirRefOracle(Dir_Log, _stsql);
        Arr_Dir = Direccion.split(";");
        Nombre = Function.TraeDirRefOracle(Nombre_Log, _stsql);
        Mensaje = Function.TraeDirRefOracle(Mensaje_Log, _stsql);
        if(_stsql != null) _stsql.close();
    }
    catch(SQLException e){
        System.out.println("FALLO: CODIGO: " + e.getErrorCode()+ " MENSAJE: " + e.getMessage());
    }
    String so = System.getProperty("os.name");
    if (so.indexOf("Win") > -1) {
      Direccion = Arr_Dir[0];
    } else {
      Direccion = Arr_Dir[1];
    }
    File archivo = null;
      System.out.println("log "+ Direccion + Nombre);
    archivo = new File(Direccion + Nombre);
    if (!archivo.exists()) {
      try
      {
        FileWriter fichero = new FileWriter(Direccion + Nombre, true);
        System.out.println("ubicacion del archivo : " + Direccion + Nombre);
        fDatosSeg = new BufferedWriter(fichero);
        fDatosSeg.write(Mensaje);
        fDatosSeg.newLine();
        fDatosSeg.write(hoy);
        fDatosSeg.newLine();
        fDatosSeg.close();
        fichero.close();
      }
      catch (IOException e1)
      {
        e1.printStackTrace();
      }
    }
  }
  
  public static String Busca_en_Arreglo(String[] Arr_Aux, String buscar, int posicion)
  {
    String Valor = "N/A";
    String Var_Aux = "";
    String[] Arr_Valor = new String[10];
    int i = 0;
    for (i = 0; i < Arr_Aux.length; i++) {
      if (!Arr_Aux[i].equals(null))
      {
        Var_Aux = Arr_Aux[i];
        Arr_Valor = Var_Aux.split(";");
        if (Arr_Valor[0].equals(buscar))
        {
          Valor = Arr_Valor[posicion];
          break;
        }
      }
    }
    return Valor;
  }
  
  public void Registro(String codusr, String codsession, String usuario, String CodAccion, String Accion, Statement _stsql)
  {
    MultiFunction Function = new MultiFunction();
    Date fechaActual = new Date();
    String Fechahoy = "";String sql = "";
    Fechahoy = Function.getDateString(fechaActual, "yyyy/MM/dd HH:mm:ss");
    String base = Function.TraeDirRefOracle("BASE_ORACLE", _stsql);
    sql = "INSERT INTO " + base + ".LOG_HALCON VALUES('" + codusr + "','" + codsession + "',to_date('" + Fechahoy + "','yyyy/mm/dd hh24:mi:ss'),'" + usuario + "','" + CodAccion + "','" + Accion + "')";
    System.out.println(sql);
    try
    {
      _stsql.executeQuery(sql);
    }
    catch (SQLException e)
    {
      e.printStackTrace();
      String error = "";
      String[] message = new String[3];
      error = e.getMessage();
      Function.Errores(codusr, usuario, error, sql, "Multifuncion.java", "Inserta el registro de log halcon", "N", "LOG HALCON", _stsql);
    }
  }
  
  public void Errores(String codusr, String usuario, String Descripcion, String Sentencia, String NombreArchivo, String Observaciones, String solucion, String modulo, Statement _stsql, PrintWriter out, String name)
  {
    MultiFunction Function = new MultiFunction();
    Date fechaActual = new Date();
    String Fechahoy = "";String sql = "";
    boolean band = false;
    if (codusr.equals("")) {
      codusr = Function.TraeDirRefOracle("Codigo_Usuario_Procesamiento_Halcon", _stsql);
    }
    if (usuario.equals("")) {
      usuario = Function.TraeDirRefOracle("Usuario_Procesamiento_Halcon", _stsql);
    }
    String base = Function.TraeDirRefOracle("BASE_ORACLE", _stsql);
    Fechahoy = Function.getDateString(fechaActual, "yyyy/MM/dd HH:mm:ss");
    if (Sentencia.length() > 3700) {
      Sentencia = Sentencia.substring(0, 3700);
    }
    if (Observaciones.length() > 195) {
      Observaciones = Observaciones.substring(0, 195);
    }
    if (Descripcion.length() > 195) {
      Descripcion = Descripcion.substring(0, 195);
    }
    Sentencia = Sentencia.replace("'", "\"");
    Descripcion = Descripcion.replace("\n", "");
    Descripcion = Descripcion.replace("\013", "");
    Descripcion = Descripcion.trim();
    String[] spltError = new String[4];
    spltError = Descripcion.split(":");
    if (spltError.length > 1) {
      sql = "INSERT INTO " + base + ".LOG_ERRORES_HALCON VALUES('" + codusr + "','" + usuario + "',to_date('" + Fechahoy + "','yyyy/mm/dd hh24:mi:ss'),'" + spltError[0] + "','" + spltError[1] + "','" + Sentencia + "','" + NombreArchivo + "','" + Observaciones + "','" + solucion + "',to_date('0001/01/01 00:00:00','yyyy/mm/dd hh24:mi:ss'),'" + modulo + "')";
    } else {
      sql = "INSERT INTO " + base + ".LOG_ERRORES_HALCON VALUES('" + codusr + "','" + usuario + "',to_date('" + Fechahoy + "','yyyy/mm/dd hh24:mi:ss'),'','" + Descripcion + "','" + Sentencia + "','" + NombreArchivo + "','" + Observaciones + "','" + solucion + "',to_date('0001/01/01 00:00:00','yyyy/mm/dd hh24:mi:ss'),'" + modulo + "')";
    }
    System.out.println(sql);
    try
    {
      if (_stsql.executeUpdate(sql) == 0)
      {
        band = false;
      }
      else
      {
        System.out.println("���ERROR.....GRAVE !!!");
        band = true;
      }
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }
    if (band)
    {
      out.println("<script language=\"javascript\" TYPE=\"text/javascript\">");
      out.println("gotoMain('" + name + "');");
      out.println("</script>");
    }
  }
  
  public void Errores(String codusr, String usuario, String Descripcion, String Sentencia, String NombreArchivo, String Observaciones, String solucion, String modulo, Statement _stsql)
  {
    MultiFunction Function = new MultiFunction();
    Date fechaActual = new Date();
    String Fechahoy = "";String sql = "";
    boolean band = false;
    if (codusr.equals("")) {
      codusr = Function.TraeDirRefOracle("Codigo_Usuario_Procesamiento_Halcon", _stsql);
    }
    if (usuario.equals("")) {
      usuario = Function.TraeDirRefOracle("Usuario_Procesamiento_Halcon", _stsql);
    }
    String base = Function.TraeDirRefOracle("BASE_ORACLE", _stsql);
    Fechahoy = Function.getDateString(fechaActual, "yyyy/MM/dd HH:mm:ss");
    if (Sentencia.length() > 3700) {
      Sentencia = Sentencia.substring(0, 3700);
    }
    if (Observaciones.length() > 195) {
      Observaciones = Observaciones.substring(0, 195);
    }
    if (Descripcion.length() > 195) {
      Descripcion = Descripcion.substring(0, 195);
    }
    Sentencia = Sentencia.replace("'", "\"");
    Descripcion = Descripcion.replace("\n", "");
    Descripcion = Descripcion.replace("\013", "");
    Descripcion = Descripcion.trim();
    String[] spltError = new String[4];
    spltError = Descripcion.split(":");
    if (spltError.length > 1) {
      sql = "INSERT INTO " + base + ".LOG_ERRORES_HALCON VALUES('" + codusr + "','" + usuario + "',to_date('" + Fechahoy + "','yyyy/mm/dd hh24:mi:ss'),'" + spltError[0] + "','" + spltError[1] + "','" + Sentencia + "','" + NombreArchivo + "','" + Observaciones + "','" + solucion + "',to_date('0001/01/01 00:00:00','yyyy/mm/dd hh24:mi:ss'),'" + modulo + "')";
    } else {
      sql = "INSERT INTO " + base + ".LOG_ERRORES_HALCON VALUES('" + codusr + "','" + usuario + "',to_date('" + Fechahoy + "','yyyy/mm/dd hh24:mi:ss'),'','" + Descripcion + "','" + Sentencia + "','" + NombreArchivo + "','" + Observaciones + "','" + solucion + "',to_date('0001/01/01 00:00:00','yyyy/mm/dd hh24:mi:ss'),'" + modulo + "')";
    }
    System.out.println(sql);
    try
    {
      if (_stsql.executeUpdate(sql) != 0) {
        System.exit(1);
      }
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }
  }
  
  public void Errores(String codusr, String usuario, String CodError, String Descripcion, String Sentencia, String NombreArchivo, String Observaciones, String solucion, String modulo, Statement _stsql)
  {
    MultiFunction Function = new MultiFunction();
    Date fechaActual = new Date();
    String Fechahoy = "";String sql = "";
    boolean band = false;
    if (codusr.equals("")) {
      codusr = Function.TraeDirRefOracle("Codigo_Usuario_Procesamiento_Halcon", _stsql);
    }
    if (usuario.equals("")) {
      usuario = Function.TraeDirRefOracle("Usuario_Procesamiento_Halcon", _stsql);
    }
    String base = Function.TraeDirRefOracle("BASE_ORACLE", _stsql);
    Fechahoy = Function.getDateString(fechaActual, "yyyy/MM/dd HH:mm:ss");
    if (Sentencia.length() > 3700) {
      Sentencia = Sentencia.substring(0, 3700);
    }
    if (Observaciones.length() > 195) {
      Observaciones = Observaciones.substring(0, 195);
    }
    if (Descripcion.length() > 195) {
      Descripcion = Descripcion.substring(0, 195);
    }
    Sentencia = Sentencia.replace("'", "\"");
    Descripcion = Descripcion.replace("\n", "");
    Descripcion = Descripcion.replace("\013", "");
    Descripcion = Descripcion.trim();
    String[] spltError = new String[4];
    spltError = Descripcion.split(":");
    sql = "INSERT INTO " + base + ".LOG_ERRORES_HALCON VALUES('" + codusr + "','" + usuario + "',to_date('" + Fechahoy + "','yyyy/mm/dd hh24:mi:ss'),'" + CodError + "','" + Descripcion + "','" + Sentencia + "','" + NombreArchivo + "','" + Observaciones + "','" + solucion + "',to_date('0001/01/01 00:00:00','yyyy/mm/dd hh24:mi:ss'),'" + modulo + "')";
    
    System.out.println(sql);
    try
    {
      if (_stsql.executeUpdate(sql) != 0) {
        System.exit(1);
      }
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }
  }
  
  public String TraeScriptDatEnv(ResultSet Row_evento, int num)
  {
    MultiFunction Function = new MultiFunction();
    String Script_Aux = "";
    String Str_Aux = "";
    String Campo_Sql = "";
    int Cont_Campo = 1;
    while (Cont_Campo <= num)
    {
      Campo_Sql = "SCRIPT_DATOS_" + Cont_Campo;
      try
      {
        Str_Aux = Row_evento.getString(Campo_Sql);
      }
      catch (SQLException e)
      {
        e.printStackTrace();
      }
      if (Str_Aux != null) {
        Script_Aux = Script_Aux + Str_Aux;
      }
      Cont_Campo++;
    }
    Script_Aux = Script_Aux.replace("\"", "'");
    return Script_Aux;
  }
  
  public String TraeCampoEspecifico(String BD, String Tabla, String Campo_Busqueda, String Valor_Campo, String Tipo_Campos, String Campo_Requerido, Statement _stsql)
  {
    String qry_busca = "";
    String Sentencia_Busc = "";
    String Resultado = "";
    String Tipo_Campo_Actl = "";
    String[] Arr_Aux_1 = new String[20];
    String[] Arr_Aux_2 = new String[20];
    String[] Arr_Aux_3 = new String[20];
    MultiFunction Function = new MultiFunction();
    if (Campo_Busqueda.indexOf(";") != -1)
    {
      if ((Tipo_Campos.indexOf(";") > -1) && (Valor_Campo.indexOf(";") > -1))
      {
        Arr_Aux_1 = Campo_Busqueda.split(";");
        Arr_Aux_2 = Valor_Campo.split(";");
        Arr_Aux_3 = Tipo_Campos.split(";");
        if (Arr_Aux_1.length == Arr_Aux_2.length)
        {
          for (int i = 0; i < Arr_Aux_1.length; i++)
          {
            Tipo_Campo_Actl = Arr_Aux_3[i];
            if (Tipo_Campo_Actl.equalsIgnoreCase("Varchar")) {
              Sentencia_Busc = Sentencia_Busc + Arr_Aux_1[i] + "='" + Arr_Aux_2[i] + "' AND ";
            } else if (Tipo_Campo_Actl.equalsIgnoreCase("Int")) {
              Sentencia_Busc = Sentencia_Busc + Arr_Aux_1[i] + "=" + Arr_Aux_2[i] + " AND ";
            } else if (Tipo_Campo_Actl.equalsIgnoreCase("Fec")) {
              Sentencia_Busc = Sentencia_Busc + Arr_Aux_1[i] + "='" + Arr_Aux_2[i] + "' AND ";
            }
          }
          String Sentencia = Sentencia_Busc.substring(0, Sentencia_Busc.length() - 4);
          Sentencia_Busc = Sentencia;
        }
        else
        {
          Resultado = "Error de Consulta";
        }
      }
      else
      {
        Arr_Aux_1 = Campo_Busqueda.split(";");
        for (int i = 0; i < Arr_Aux_1.length; i++) {
          if (Tipo_Campos.equals("Varchar")) {
            Sentencia_Busc = Sentencia_Busc + Campo_Busqueda + "='" + Valor_Campo + "' ";
          } else if (Tipo_Campos == "Int") {
            Sentencia_Busc = Sentencia_Busc + Campo_Busqueda + "=" + Valor_Campo + " ";
          } else if (Tipo_Campos == "Fec") {
            Sentencia_Busc = Sentencia_Busc + Campo_Busqueda + "='" + Valor_Campo + "' ";
          }
        }
      }
    }
    else if (Tipo_Campos.indexOf(";") > -1)
    {
      Arr_Aux_2 = Valor_Campo.split(";");
      Arr_Aux_3 = Tipo_Campos.split(";");
      if ((Arr_Aux_3.length == Arr_Aux_2.length) && (Arr_Aux_2.length > 1))
      {
        for (int i = 0; i < Arr_Aux_2.length; i++)
        {
          Tipo_Campo_Actl = Arr_Aux_3[i];
          if (Tipo_Campo_Actl.equalsIgnoreCase("Varchar")) {
            Sentencia_Busc = Sentencia_Busc + Arr_Aux_1[i] + "='" + Arr_Aux_2[i] + "' AND ";
          } else if (Tipo_Campo_Actl.equalsIgnoreCase("Int")) {
            Sentencia_Busc = Sentencia_Busc + Arr_Aux_1[i] + "=" + Arr_Aux_2[i] + " AND ";
          } else if (Tipo_Campo_Actl.equalsIgnoreCase("Fec")) {
            Sentencia_Busc = Sentencia_Busc + Arr_Aux_1[i] + "='" + Arr_Aux_2[i] + "' AND ";
          }
        }
        String Sentencia = Sentencia_Busc.substring(0, Sentencia_Busc.length() - 4);
        Sentencia_Busc = Sentencia;
      }
      else
      {
        Resultado = "Error de Consulta";
      }
    }
    else if (Tipo_Campos.equals("Varchar"))
    {
      Sentencia_Busc = Sentencia_Busc + Campo_Busqueda + "='" + Valor_Campo + "' ";
    }
    else if (Tipo_Campos == "Int")
    {
      Sentencia_Busc = Sentencia_Busc + Campo_Busqueda + "=" + Valor_Campo + " ";
    }
    else if (Tipo_Campos == "Fec")
    {
      Sentencia_Busc = Sentencia_Busc + Campo_Busqueda + "='" + Valor_Campo + "' ";
    }
    String codusr = "";String codsession = "";String usuario = "";String observaciones_Err = "";
    if (Resultado != "Error de Consulta")
    {
      qry_busca = "SELECT * FROM " + BD + "." + Tabla + " WHERE " + Sentencia_Busc;
      try
      {
        codusr = Function.TraeDirRefOracle("Codigo_Usuario_Procesamiento_Halcon", _stsql);
        usuario = Function.TraeDirRefOracle("Usuario_Procesamiento_Halcon", _stsql);
        ResultSet Row_Busca = _stsql.executeQuery(qry_busca);
        while (Row_Busca.next()) {
          if (Row_Busca.getRow() > 0) {
            if (Campo_Requerido.indexOf(";") != -1)
            {
              Arr_Aux_3 = Campo_Requerido.split(";");
              for (int i = 0; i < Arr_Aux_3.length; i++) {
                Resultado = Resultado + Row_Busca.getString(Arr_Aux_3[i]) + ";";
              }
              Resultado = Resultado.substring(0, Resultado.length() - 1);
            }
            else
            {
              Resultado = Resultado + Row_Busca.getString(Campo_Requerido) + ";";
              Resultado = Resultado.substring(0, Resultado.length() - 1);
            }
          }
        }
      }
      catch (SQLException e)
      {
        e.printStackTrace();
        String error = "";
        observaciones_Err = "Selecciona el campo que se desea buscar y lo retorna";
        error = e.getMessage();
        Function.Errores(codusr, usuario, error, "", "MultiFunction.java", observaciones_Err, "N", "FUNCION - TRAE CAMPO ESPECIFICO", _stsql);
      }
    }
    return Resultado;
  }
  
  public String TraeFecha(String FechaIni, String FechaFin, int NumDay, String Operacion, String ResultForm)
  {
    MultiFunction Function = new MultiFunction();
    String Fecha_Ini = "";
    String Fecha_Fin = "";
    String retorna = "";
    String ano = "";
    String mes = "";
    String dia = "";
    


    int year = 0;int year1 = 0;
    int month = 0;int month1 = 0;
    int day = 0;int day1 = 0;
    double dias = 0.0D;
    Calendar Caldate = Calendar.getInstance();
    String[] datesplit = new String[3];
    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    datesplit = FechaIni.split(" ");
    if (datesplit.length > 1)
    {
      Fecha_Ini = datesplit[0];
      datesplit = Fecha_Ini.split("-");
      ano = datesplit[0];
      mes = datesplit[1];
      dia = datesplit[2];
      Fecha_Ini = ano + "-" + mes + "-" + dia;
    }
    else
    {
      Fecha_Ini = FechaIni;
    }
    if ((FechaFin != "") && (FechaFin != null))
    {
      datesplit = FechaFin.split(" ");
      if (datesplit.length > 1)
      {
        Fecha_Fin = datesplit[0];
        datesplit = Fecha_Fin.split("-");
        ano = datesplit[0];
        mes = datesplit[1];
        dia = datesplit[2];
        Fecha_Fin = ano + "-" + mes + "-" + dia;
      }
      else
      {
        Fecha_Fin = FechaFin;
      }
    }
    if (Operacion.equals("Resta"))
    {
      try
      {
        if ((FechaFin != "") && (FechaFin != null))
        {
          Date dateIni = formatter.parse(Fecha_Ini);
          Date dateFin = formatter.parse(Fecha_Fin);
          long dif = dateFin.getTime() - dateIni.getTime();
          if (ResultForm.equals("Dias"))
          {
            dias = Math.floor(dif / 86400000L);
            retorna = Integer.toString((int)dias);
          }
        }
        else
        {
          datesplit = Fecha_Ini.split("-");
          dia = datesplit[2];
          mes = datesplit[1];
          ano = datesplit[0];
          
          dias = Integer.parseInt(dia) - NumDay;
          if (ResultForm.equals("Fecha"))
          {
            Caldate.set(Integer.parseInt(ano), Integer.parseInt(mes) - 1, (int)dias);
            retorna = getDateString(Caldate, "yyyy-MM-dd");
          }
        }
      }
      catch (ParseException e)
      {
        e.printStackTrace();
      }
    }
    else if (Operacion.equals("SumaFD"))
    {
      datesplit = Fecha_Ini.split("-");
      year = Integer.parseInt(datesplit[0]);
      month = Integer.parseInt(datesplit[1]);
      day = Integer.parseInt(datesplit[2]);
      if ((FechaFin != "") && (FechaFin != null))
      {
        datesplit = Fecha_Fin.split("-");
        year1 = Integer.parseInt(datesplit[0]);
        month1 = Integer.parseInt(datesplit[1]);
        day1 = Integer.parseInt(datesplit[2]);
        if (ResultForm.equals("Dias"))
        {
          dias = day + day1;
          retorna = Integer.toString((int)dias);
        }
      }
      else
      {
        dias = day + NumDay;
        if (ResultForm.equals("Dias"))
        {
          retorna = Integer.toString((int)dias);
        }
        else if (ResultForm.equals("Fecha"))
        {
          try
          {
            Date dateIni = formatter.parse(Fecha_Ini);
            Caldate = getCalendar(dateIni);
            day = Caldate.get(5);
            month = Caldate.get(2) + 1;
            year = Caldate.get(1);
          }
          catch (ParseException e)
          {
            e.printStackTrace();
          }
          dias = day + NumDay;
          Caldate.set(year, month - 1, (int)dias);
          retorna = getDateString(Caldate, "yyyy-MM-dd");
        }
      }
    }
    return retorna;
  }
  
  public String TraeDirRef(String _Referencia, Connection conexion)
  {
    String Direccion = "";
    try
    {
      Statement mySt = conexion.createStatement();
      

      ResultSet RowRef = mySt.executeQuery("SELECT * FROM Halcon.estructura WHERE Referencia = '" + _Referencia + "'");
      while (RowRef.next()) {
        Direccion = RowRef.getString("Direccion");
      }
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }
    return Direccion;
  }
  
  public String TraeDirRefOracle(String _Referencia, Statement _stsql)
  {
    String Direccion = "";
    String _sql = "SELECT * FROM Halcon.estructura WHERE Referencia = '" + _Referencia + "'";
    try
    {
      ResultSet RowRef = _stsql.executeQuery(_sql);
      while (RowRef.next()) {
        Direccion = RowRef.getString("DIRECCION");
      }
      RowRef.close();
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }
    return Direccion;
  }
  
  private static String formatoDefecto = "yyyy-MM-dd";
  
  public static String getDateStringNDaysAgo(int nDaysAgo, String formato)
  {
    SimpleDateFormat sdf = new SimpleDateFormat(formato);
    
    Calendar dateEnd = Calendar.getInstance();
    dateEnd.add(5, nDaysAgo * -1);
    
    return sdf.format(Long.valueOf(dateEnd.getTimeInMillis()));
  }
  
  public static Calendar getCalendarNDaysAgo(int nDaysAgo)
  {
    Calendar dateEnd = Calendar.getInstance();
    dateEnd.add(5, nDaysAgo * -1);
    
    return dateEnd;
  }
  
  public static String getToday()
  {
    Date date = Calendar.getInstance().getTime();
    SimpleDateFormat sdf = new SimpleDateFormat(formatoDefecto);
    
    return sdf.format(date);
  }
  
  public static String getToday(String formato)
  {
    Date date = Calendar.getInstance().getTime();
    SimpleDateFormat sdf = new SimpleDateFormat(formato);
    
    return sdf.format(date);
  }
  
  public static String getDateString(Calendar cFecha)
  {
    SimpleDateFormat sdf = new SimpleDateFormat(formatoDefecto);
    
    return sdf.format(Long.valueOf(cFecha.getTimeInMillis()));
  }
  
  public static String getDateString(Calendar cFecha, String formato)
  {
    SimpleDateFormat sdf = new SimpleDateFormat(formato);
    
    return sdf.format(Long.valueOf(cFecha.getTimeInMillis()));
  }
  
  public String getDateString(Date dFecha, String formato)
  {
    SimpleDateFormat sdf = new SimpleDateFormat(formato);
    
    return sdf.format(dFecha);
  }
  
  public static Calendar getCalendar(Date dFecha)
  {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(dFecha);
    
    return calendar;
  }
  
  public static int getDay(String sFecha)
  {
    String[] splitdate = new String[2];
    splitdate = sFecha.split("-");
    int day = 0;
    day = Integer.parseInt(splitdate[0]);
    return day;
  }
  
  public static int getMonth(String sFecha)
  {
    String[] splitdate = new String[2];
    splitdate = sFecha.split("-");
    int month = 0;
    month = Integer.parseInt(splitdate[1]);
    return month;
  }
  
  public static int getYear(String sFecha)
  {
    String[] splitdate = new String[2];
    splitdate = sFecha.split("-");
    int year = 0;
    year = Integer.parseInt(splitdate[2]);
    return year;
  }
  
  public static String NewDate(int Day, int Mounth, int Year)
  {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Year, Mounth, Day);
    
    return getDateString(calendar);
  }
  
  public static int getYear(Calendar cFecha)
  {
    String Year = cFecha.toString();
    
    String[] splitdate = new String[1000];
    splitdate = Year.split(",");
    Year = splitdate[14];
    splitdate = Year.split("=");
    int year = Integer.parseInt(splitdate[1]);
    return year;
  }
  
  public static int getMonth(Calendar cFecha)
  {
    String Month = cFecha.toString();
    String[] splitdate = new String[1000];
    splitdate = Month.split(",");
    Month = splitdate[14];
    splitdate = Month.split("=");
    int month = Integer.parseInt(splitdate[1]);
    return month;
  }
  
  public static String FormatFecha(String sFecha, String formato)
  {
    String[] divfecha = new String[3];
    String ano = "";
    String mes = "";
    String dia = "";
    divfecha = sFecha.split(" ");
    String Fecha = divfecha[0];
    divfecha = Fecha.split("-");
    ano = divfecha[0];
    mes = divfecha[1];
    dia = divfecha[2];
    if (formato.equals("ddmmyyyy")) {
      Fecha = dia + mes + ano;
    } else if (formato.equals("dd/mm/yyyy")) {
      Fecha = dia + "/" + mes + "/" + ano;
    } else if (formato.equals("dd-mm-yyyy")) {
      Fecha = dia + "-" + mes + "-" + ano;
    } else if (formato.equals("yyyy-mm-dd")) {
      Fecha = ano + "-" + mes + "-" + dia;
    }
    if (formato.equals("ddmmyy"))
    {
      if (ano.equals("2012")) {
        ano = "12";
      } else if (ano.equals("2013")) {
        ano = "13";
      } else if (ano.equals("2014")) {
        ano = "14";
      }
      Fecha = dia + mes + ano;
    }
    return Fecha;
  }
  
  public static String IngresoFecha(String sFecha)
  {
    System.out.println(sFecha);
    String year = "";
    String month = "";
    String day = "";
    

    char[] date = sFecha.toCharArray();
    for (int j = 0; j < sFecha.length(); j++)
    {
      if (j < 2) {
        day = day + date[j];
      }
      if ((1 < j) && (j < 4)) {
        month = month + date[j];
      }
      if (j > 3) {
        year = year + date[j];
      }
    }
    String modfecha = "";
    if (Integer.parseInt(year) > 99) {
      modfecha = "2" + year + "-" + month + "-" + day;
    } else {
      modfecha = "20" + year + "-" + month + "-" + day;
    }
    return modfecha;
  }
  
public void avanceEvento(String Cod_Evento, String Cod_Proceso, String Est_Actual, String Observaciones, Connection conexion)
  {
    MultiFunction Function = new MultiFunction();
      System.out.println("+++avanceEvento+++");
    try
    {
      Statement _stsql = conexion.createStatement();
      Statement _stsql_aux = conexion.createStatement();
      Statement _stsql_aux_1 = conexion.createStatement();
      
      String codusr = "";String codsession = "";String usuario = "";String observaciones_Err = "";
      String _sql_SE = "";
      String _sql = "";
      String _sql_INS = "";
      String Fecha_inicio = "";
      String Fecha_Alarma = "";
      String Estado_Anterior = "";
      Date fechaActual = new Date();
      SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
      String cadenaFecha = formato.format(fechaActual);
      System.out.println("MENSAJE -> Registro Evento: " + Cod_Evento + " en Proceso " + Cod_Proceso + " ->" + cadenaFecha);
      
      boolean Hay_Alarma = false;
      int Cont = 0;
      
      String Base_Oracle = Function.TraeDirRefOracle("BASE_ORACLE", _stsql);
      
      _sql = "SELECT * FROM " + Base_Oracle + ".Eventos WHERE COD_EVENTO = '" + Cod_Evento + "' AND ((ESTADO_ESPERADO = '" + Est_Actual + "')";
      _sql = _sql + " OR (ESTADO_ACEPTABLE = '" + Est_Actual + "') OR (ESTADO_CRITICO = '" + Est_Actual + "'))  ";
      
      Statement stsql_err = conexion.createStatement();
      codusr = Function.TraeDirRefOracle("Codigo_Usuario_Procesamiento_Halcon", stsql_err);
      usuario = Function.TraeDirRefOracle("Usuario_Procesamiento_Halcon", stsql_err);
      try
      {
        ResultSet _Rowsql = _stsql.executeQuery(_sql);
        while (_Rowsql.next())
        {
          Cont = 0;
          Hay_Alarma = false;
          Fecha_Alarma = "";
          Fecha_inicio = "";
          Estado_Anterior = "";
          
          String Nom_Evento = _Rowsql.getString("NOM_EVENTO");
          String Tipo_evento = _Rowsql.getString("TIPO_EVENTO");
          String estado_esperado = _Rowsql.getString("ESTADO_ESPERADO");
          String estado_critico = _Rowsql.getString("ESTADO_CRITICO");
          String estado_aceptable = _Rowsql.getString("ESTADO_ACEPTABLE");
          
          _sql_SE = "SELECT * FROM " + Base_Oracle + ".SEGUIMIENTO_EVENTOS WHERE COD_PROCESO = '" + Cod_Proceso + "' AND COD_EVENTO = '" + Cod_Evento + "' ORDER BY FECHA_MEDICION DESC";
          try
          {
            ResultSet _Row_SE = _stsql_aux.executeQuery(_sql_SE);
            if (_Row_SE.next())
            {
              Cont++;
              if (_Row_SE.getString("ALARMA").equals("S"))
              {
                Hay_Alarma = true;
                Fecha_Alarma = _Row_SE.getString("FECHA_ALARMA");
              }
              Fecha_inicio = formato.format(_Row_SE.getDate("FECHA_INICIO"));
              Estado_Anterior = _Row_SE.getString("ESTADO_ACTUAL");
            }
            _Row_SE.close();
          }
          catch (SQLException e)
          {
            e.printStackTrace();
            String error = "";
            observaciones_Err = "selecciona un evento que tenga activa la ALARMA";
            error = e.getMessage();
            Function.Errores(codusr, usuario, error, "", "MultiFunction.java", observaciones_Err, "N", "FUNCION - AVANCE_EVENTO", stsql_err);
          }
          if ((estado_esperado.equals(Est_Actual)) && (!Estado_Anterior.equals(Est_Actual)))
          {
            if (Hay_Alarma)
            {
              _sql_INS = "INSERT INTO " + Base_Oracle + ".SEGUIMIENTO_EVENTOS (COD_PROCESO, COD_EVENTO, NOM_EVENTO,TIPO_EVENTO,ESTADO_ESPERADO,";
              _sql_INS = _sql_INS + "ESTADO_ACTUAL,FECHA_MEDICION,FECHA_FIN,FECHA_SOLUCION,ALARMA,OBSERVACIONES,FECHA_INICIO) VALUES('" + Cod_Proceso + "','" + Cod_Evento + "'";
              _sql_INS = _sql_INS + ",'" + Nom_Evento + "','" + Tipo_evento + "','" + estado_esperado + "'";
              _sql_INS = _sql_INS + ",'" + Est_Actual + "',to_date('" + cadenaFecha + "','dd/mm/yyyy HH24:MI:SS')";
              _sql_INS = _sql_INS + ",to_date('" + cadenaFecha + "','dd/mm/yyyy HH24:MI:SS'),to_date('" + cadenaFecha + "','dd/mm/yyyy HH24:MI:SS'),'N','" + Observaciones + "'";
              _sql_INS = _sql_INS + ",to_date('" + Fecha_inicio + "','dd/mm/yyyy HH24:MI:SS'))";
            }
            else
            {
              _sql_INS = "INSERT INTO " + Base_Oracle + ".SEGUIMIENTO_EVENTOS (COD_PROCESO, COD_EVENTO,NOM_EVENTO,TIPO_EVENTO,ESTADO_ESPERADO,";
              _sql_INS = _sql_INS + "ESTADO_ACTUAL,FECHA_MEDICION,FECHA_FIN,OBSERVACIONES,ALARMA,FECHA_INICIO) VALUES('" + Cod_Proceso + "','" + Cod_Evento + "'";
              _sql_INS = _sql_INS + ",'" + Nom_Evento + "','" + Tipo_evento + "','" + estado_esperado + "','" + Est_Actual + "'";
              _sql_INS = _sql_INS + ",to_date('" + cadenaFecha + "','dd/mm/yyyy HH24:MI:SS'),to_date('" + cadenaFecha + "','dd/mm/yyyy HH24:MI:SS'),'" + Observaciones + "'";
              _sql_INS = _sql_INS + ",'N',to_date('" + Fecha_inicio + "','dd/mm/yyyy HH24:MI:SS'))";
            }
          }
          else if (estado_critico.equals(Est_Actual))
          {
            if (Cont > 0)
            {
              _sql_INS = "INSERT INTO " + Base_Oracle + ".SEGUIMIENTO_EVENTOS (COD_PROCESO, COD_EVENTO, NOM_EVENTO,TIPO_EVENTO,ESTADO_ESPERADO,";
              _sql_INS = _sql_INS + "ESTADO_ACTUAL,FECHA_MEDICION,FECHA_ALARMA,ALARMA,OBSERVACIONES,FECHA_INICIO) VALUES('" + Cod_Proceso + "','" + Cod_Evento + "'";
              _sql_INS = _sql_INS + ",'" + Nom_Evento + "','" + Tipo_evento + "','" + estado_esperado + "'";
              _sql_INS = _sql_INS + ",'" + Est_Actual + "',to_date('" + cadenaFecha + "','dd/mm/yyyy HH24:MI:SS'),to_date('" + cadenaFecha + "','dd/mm/yyyy HH24:MI:SS')";
              _sql_INS = _sql_INS + ",'S','" + Observaciones + "'";
              _sql_INS = _sql_INS + ",to_date('" + Fecha_inicio + "','dd/mm/yyyy HH24:MI:SS'))";
            }
          }
          else if (estado_aceptable.equals(Est_Actual)) {
            if (Cont > 0)
            {
              if (Hay_Alarma)
              {
                _sql_INS = "INSERT INTO " + Base_Oracle + ".SEGUIMIENTO_EVENTOS (COD_PROCESO, COD_EVENTO, NOM_EVENTO,TIPO_EVENTO,ESTADO_ESPERADO,";
                _sql_INS = _sql_INS + "ESTADO_ACTUAL,FECHA_MEDICION,FECHA_ALARMA,FECHA_SOLUCION,ALARMA,OBSERVACIONES,FECHA_INICIO) VALUES('" + Cod_Proceso + "','" + Cod_Evento + "'";
                _sql_INS = _sql_INS + ",'" + Nom_Evento + "','" + Tipo_evento + "','" + estado_esperado + "'";
                _sql_INS = _sql_INS + ",'" + Est_Actual + "',to_date('" + cadenaFecha + "','dd/mm/yyyy HH24:MI:SS'),to_date('" + Fecha_Alarma + "','yyyy/mm/dd HH24:MI:SS')";
                _sql_INS = _sql_INS + ",to_date('" + cadenaFecha + "','dd/mm/yyyy HH24:MI:SS'),'N','" + Observaciones + "'";
                _sql_INS = _sql_INS + ",to_date('" + Fecha_inicio + "','dd/mm/yyyy HH24:MI:SS'))";
              }
              else
              {
                _sql_INS = "INSERT INTO " + Base_Oracle + ".SEGUIMIENTO_EVENTOS (COD_PROCESO, COD_EVENTO, NOM_EVENTO,TIPO_EVENTO,ESTADO_ESPERADO,";
                _sql_INS = _sql_INS + "ESTADO_ACTUAL,FECHA_MEDICION,ALARMA,OBSERVACIONES,FECHA_INICIO) VALUES('" + Cod_Proceso + "','" + Cod_Evento + "'";
                _sql_INS = _sql_INS + ",'" + Nom_Evento + "','" + Tipo_evento + "','" + estado_esperado + "'";
                _sql_INS = _sql_INS + ",'" + Est_Actual + "',to_date('" + cadenaFecha + "','dd/mm/yyyy HH24:MI:SS'),'N','" + Observaciones + "'";
                _sql_INS = _sql_INS + ",to_date('" + Fecha_inicio + "','yyyy/mm/dd HH24:MI:SS'))";
              }
            }
            else
            {
              _sql_INS = "INSERT INTO " + Base_Oracle + ".SEGUIMIENTO_EVENTOS (COD_PROCESO, COD_EVENTO, NOM_EVENTO,TIPO_EVENTO,ESTADO_ESPERADO,";
              _sql_INS = _sql_INS + "ESTADO_ACTUAL,FECHA_INICIO,FECHA_MEDICION,ALARMA,OBSERVACIONES) VALUES('" + Cod_Proceso + "','" + Cod_Evento + "'";
              _sql_INS = _sql_INS + ",'" + Nom_Evento + "','" + Tipo_evento + "','" + estado_esperado + "'";
              _sql_INS = _sql_INS + ",'" + Est_Actual + "',to_date('" + cadenaFecha + "','dd/mm/yyyy HH24:MI:SS'),to_date('" + cadenaFecha + "','dd/mm/yyyy HH24:MI:SS')";
              _sql_INS = _sql_INS + ",'N','" + Observaciones + "')";
            }
          }
          if (!_sql_INS.equals("")) {
            try
            {
                System.out.println("insert " + _sql_INS);
              _stsql_aux_1.executeUpdate(_sql_INS);
              _stsql_aux_1.executeUpdate("commit");
            }
            catch (SQLException e)
            {
              e.printStackTrace();
              String error = "";
              observaciones_Err = "modifica el seguimiento del evento segun su estado o si tiene alarma";
              error = e.getMessage();
              Function.Errores(codusr, usuario, error, "", "MultiFunction.java", observaciones_Err, "N", "FUNCION - AVANCE_EVENTO", stsql_err);
            }
          }
        }
        _Rowsql.close();
      }
      catch (SQLException e)
      {
        e.printStackTrace();
        String error = "";
        observaciones_Err = "Selecciona el evento segun el estado actual";
        error = e.getMessage();
        Function.Errores(codusr, usuario, error, "", "MultiFunction.java", observaciones_Err, "N", "FUNCION - AVANCE_EVENTO", stsql_err);
      }
      _stsql_aux.close();
      _stsql_aux_1.close();
      _stsql.close();
      stsql_err.close();
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }
  }
}

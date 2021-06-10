package cargaplantillas;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import oracle.jdbc.pool.OracleDataSource;

public class ClassConexionOracle
{
  public Connection conn;
  public ResultSet rs;
  public Statement stmt;
  
  public Connection ConectBD()
  {
    try
    {
      ReadXML xml = new ReadXML();
      xml.LeerXML();
      OracleDataSource ods = new OracleDataSource();
      
      ods.setUser(xml.getVuser().toString());
      
      ods.setPassword(xml.getVpass().toString());
      
      ods.setURL(xml.getVconexion_url().toString());
      
      this.conn = ods.getConnection();
      return this.conn;
    }
    catch (Exception e)
    {
      e.printStackTrace();return null;
    }
    finally
    {
      if (this.conn == null) {
        try
        {
          this.conn.close();
        }
        catch (SQLException e)
        {
          e.printStackTrace();
        }
      }
    }
  }
  
  public Statement StConectBD()
  {
    try
    {
      ReadXML xml = new ReadXML();
      xml.LeerXML();
      
      OracleDataSource ods = new OracleDataSource();
      
      ods.setUser(xml.getVuser().toString());
      
      ods.setPassword(xml.getVpass().toString());
      
      ods.setURL(xml.getVconexion_url().toString());
      

      this.conn = ods.getConnection();
      this.stmt = this.conn.createStatement();
      
      return this.stmt;
    }
    catch (Exception e)
    {
      System.out.println("ERROR Al Conectar con la Base de Datos" + e);
      e.printStackTrace();
      return null;
    }
    finally
    {
      if (this.conn == null) {
        try
        {
          this.conn.close();
        }
        catch (SQLException e)
        {
          e.printStackTrace();
        }
      }
    }
  }
  
  public Boolean CerrarConexion()
    throws SQLException
  {
    this.stmt.close();
    return null;
  }
  
  public Boolean Cerrar()
    throws SQLException
  {
    this.conn.close();
    return null;
  }
  
  public ResultSet getRs()
  {
    return this.rs;
  }
  
  public Statement getStmt()
  {
    return this.stmt;
  }
  
  public Connection getconn()
  {
    if (this.conn == null) {
      this.conn = ConectBD();
    }
    return this.conn;
  }
  
  public void setConn(Connection donn)
  {
    this.conn = donn;
  }
  
  public void setRs(ResultSet rs)
  {
    this.rs = rs;
  }
  
  public void setStmt(Statement stmt)
  {
    this.stmt = stmt;
  }
}

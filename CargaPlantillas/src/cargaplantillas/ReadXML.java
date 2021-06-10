/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cargaplantillas;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ReadXML
{
  private String vversion;
  private String vruta;
  private String vconexion_url;
  private String vuser;
  private String vpass;
  
  public boolean LeerXML()
  {
    Document doc = null;
    try
    {
      DocumentBuilderFactory Configuracion = DocumentBuilderFactory.newInstance();
      DocumentBuilder dConfiguracion = Configuracion.newDocumentBuilder();
      String so = System.getProperty("os.name");
      if (so.indexOf("Win") > -1) {
        doc = dConfiguracion.parse(new File("C:/extconf/configuracion.xml"));
      } else {
        doc = dConfiguracion.parse(new File("/extconf/configuracion_wimeb.xml"));
      }
      doc.getDocumentElement().normalize();
      NodeList listaConfiguracion = doc.getElementsByTagName("conf");
      for (int i = 0; i < listaConfiguracion.getLength(); i++)
      {
        Node Nconfiguracion = listaConfiguracion.item(i);
        if (Nconfiguracion.getNodeType() == 1)
        {
          Element elemento = (Element)Nconfiguracion;
          this.vversion = getTagValue("version", elemento);
          this.vruta = getTagValue("ruta", elemento);
          this.vconexion_url = getTagValue("conexion_url", elemento);
          this.vuser = getTagValue("user", elemento);
          this.vpass = getTagValue("pass", elemento);
        }
      }
      return true;
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return false;
  }
  
  private static String getTagValue(String sTag, Element eElement)
  {
    NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
    Node nValue = nlList.item(0);
    return nValue.getNodeValue();
  }
  
  public String getVversion()
  {
    return this.vversion;
  }
  
  public String getVruta()
  {
    return this.vruta;
  }
  
  public String getVconexion_url()
  {
    return this.vconexion_url;
  }
  
  public String getVuser()
  {
    return this.vuser;
  }
  
  public String getVpass()
  {
    return this.vpass;
  }
}

package org.alicebot.server.net.http.server;

import org.alicebot.server.net.http.HttpServer;
import org.alicebot.server.net.http.util.Code;
import org.alicebot.server.net.http.util.Resource;
import org.alicebot.server.net.http.util.XmlConfiguration;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import org.xml.sax.SAXException;


/* ------------------------------------------------------------ */
/** Config file driven HttpServer.
 *
 * This class initializes HttpServer instances from xml config files
 * that follow the XmlConfiguration dtd.
 *
 * @see HttpServer
 * @see XmlConfiguration
 * @version $Revision: 1.1.1.1 $
 * @author Greg Wilkins (gregw)
 */
public class Server extends HttpServer
{

    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @param configuration 
     */
    public Server(String configuration)
        throws IOException
    {
        this(Resource.newResource(configuration).getURL());
    }
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @param configuration 
     */
    public Server(Resource configuration)
        throws IOException
    {
        this(configuration.getURL());
    }
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @param configuration 
     */
    public Server(URL configuration)
        throws IOException
    {
        try
        {
            XmlConfiguration config=new XmlConfiguration(configuration);
            config.configure(this);
        }
        catch(org.xml.sax.SAXException e)
        {
            Code.warning(e);
            throw new IOException("Jetty configuration problem: "+e);
        }
        catch(NoSuchMethodException e)
        {
            Code.warning(e);
            throw new IOException("Jetty configuration problem: "+e);
        }
        catch(java.lang.reflect.InvocationTargetException e)
        {
            Code.warning(e);
            throw new IOException("Jetty configuration problem: "+e);
        }
        catch(ClassNotFoundException e)
        {
            Code.warning(e);
            throw new IOException("Jetty configuration problem: "+e);
        }
        
    }
    
    
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    public static void main(String[] arg)
    {
        String[] dftConfig={"etc/jetty.xml"};
        
        if (arg.length==0)
        {
            System.err.println("Using default configuration: etc/jetty.xml");
            arg=dftConfig;
        }
        
        for (int i=0;i<arg.length;i++)
        {
            try
            {
                new Server(arg[i]).start();
            }
            catch(Exception e)
            {
                Code.warning(e);
            }
        }
    }
}





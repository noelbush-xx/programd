// ========================================================================
// Copyright (c) 1999 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: Version.java,v 1.1.1.1 2001/06/17 19:01:08 noelbu Exp $
// ========================================================================

package org.alicebot.server.net.http;

/* ------------------------------------------------------------ */
/** Jetty version.
 *
 * This class sets the version data returned in the Server and
 * Servlet-Container headers.   If the
 * java.org.alicebot.server.net.http.Version.paranoid System property is set to
 * true, then this information is suppressed.
 *
 * @version $Revision: 1.1.1.1 $
 * @author Greg Wilkins (gregw)
 */
public class Version
{
    public static boolean __paranoid = 
        Boolean.getBoolean("java.org.alicebot.server.net.http.Version.paranoid");
    
    public static String __Version="Alicebot/3.0.1";
    public static String __VersionDetail="Unknown";
    public static String __ServletEngine="Unknown (Servlet 2.2; JSP 1.1)";

    static
    {
        if (!__paranoid)
        {
            __VersionDetail=__Version+" ("+
                System.getProperty("os.name")+" "+
                System.getProperty("os.version")+" "+
                System.getProperty("os.arch")+")";

            __ServletEngine=__Version+" (JSP 1.1; Servlet 2.2; java "+
                System.getProperty("java.version")+")";
        }
    }
}


// ========================================================================
// Copyright (c) 1997 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: TestConfiguration.java,v 1.1.1.1 2001/06/17 19:02:05 noelbu Exp $
// ========================================================================

package org.alicebot.server.net.http.util;
import java.net.URL;
import java.util.HashMap;

/* ------------------------------------------------------------ */
/** Test XmlConfiguration. 
 *
 * @version $Revision: 1.1.1.1 $
 * @author Greg Wilkins (gregw)
 */
public class TestConfiguration extends HashMap
{
    public TestConfiguration nested;
    public Object testObject;
    public int testInt;
    public URL url;
    
    public void setTest(Object value)
    {
        testObject=value;
    }
    
    public void setTest(int value)
    {
        testInt=value;
    }

    public void call()
    {
        put("Called","Yes");
    }
    
    public TestConfiguration call(Boolean b)
    {
        nested=new TestConfiguration();
        nested.put("Arg",b);
        return nested;
    }
    
    public void call(URL u,boolean b)
    {
        put("URL",b?"1":"0");
        url=u;
    }    
}







// =========================================================================== 
// $Id: UrlEncoded.java,v 1.1.1.1 2001/06/17 19:01:34 noelbu Exp $
package org.alicebot.server.net.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/* ------------------------------------------------------------ */
/** Handles coding of MIME  "x-www-form-urlencoded".
 * This class handles the encoding and decoding for either
 * the query string of a URL or the content of a POST HTTP request.
 *
 * <p><h4>Notes</h4>
 * The hashtable either contains String single values, vectors
 * of String or arrays of Strings.
 *
 * This class is only partially synchronised.  In particular, simple
 * get operations are not protected from concurrent updates.
 *
 * @see java.net.URLEncoder
 * @version $Id: UrlEncoded.java,v 1.1.1.1 2001/06/17 19:01:34 noelbu Exp $
 * @author Greg Wilkins (gregw)
 */
public class UrlEncoded extends MultiMap
{
    /* ----------------------------------------------------------------- */
    public UrlEncoded(UrlEncoded url)
    {
        super(url);
    }
    
    /* ----------------------------------------------------------------- */
    public UrlEncoded()
    {
        super(10);
    }
    
    /* ----------------------------------------------------------------- */
    public UrlEncoded(String s)
    {
        super(10);
        decode(s);
    }
    
    /* ----------------------------------------------------------------- */
    public void decode(String query)
    {
        decodeTo(query,this);
    }
    
    /* -------------------------------------------------------------- */
    /** Encode Hashtable with % encoding.
     */
    public String encode()
    {
        return encode(false);
    }
    
    /* -------------------------------------------------------------- */
    /** Encode Hashtable with % encoding.
     * @param equalsForNullValue if True, then an '=' is always used, even
     * for parameters without a value. e.g. "blah?a=&b=&c=".
     */
    public synchronized String encode(boolean equalsForNullValue)
    {        
        StringBuffer result = new StringBuffer(128);
        synchronized(result)
        {
            Iterator i = entrySet().iterator();
            String separator="";
            while(i.hasNext())
            {
                Map.Entry entry =
                    (Map.Entry)i.next();
                
                String key = entry.getKey().toString();
                Object value = entry.getValue();

                // encode single values and extract multi values
                if (value==null)
                {
                    result.append(separator);
                    separator="&";
                    result.append(URLEncoder.encode(key));
                    if (equalsForNullValue)
                        result.append('=');
                }
                else if (value instanceof List)
                {
                    // encode multi values
                    List values=(List)value;
                    for (int v=0; v<values.size();v++)
                    {
                        result.append(separator);
                        separator="&";
                        result.append(URLEncoder.encode(key));
                        String val=(String)values.get(v);
                        if (val!=null && val.length()>0)
                        {
                            result.append('=');
                            result.append(URLEncoder.encode(val));
                        }
                        else if (equalsForNullValue)
                            result.append('=');
                    }
                }
                else
                {
                    // Encode single item
                    String val=value.toString();
                    result.append(separator);
                    separator="&";
                    result.append(URLEncoder.encode(key));
                    if (equalsForNullValue || val.length()>0)
                    {
                        result.append('=');
                        result.append(URLEncoder.encode(val));
                    }
                }
            }
            return result.toString();
        }
    }

    
    /* -------------------------------------------------------------- */
    /* Decoded parameters to Map.
     * @param content the string containing the encoded parameters
     * @param url The dictionary to add the parameters to
     */
    public static void decodeTo(String content,MultiMap map)
    {
        synchronized(map)
        {
            String token;
            String name;
            String value;

            StringTokenizer tokenizer =
                new StringTokenizer(content, "&", false);

            while ((tokenizer.hasMoreTokens()))
            {
                token = tokenizer.nextToken();
            
                // breaking it at the "=" sign
                int i = token.indexOf('=');
                if (i<0)
                {
                    name=decodeString(token);
                    value="";
                }
                else
                {
                    name=decodeString(token.substring(0,i++));
                    if (i>=token.length())
                        value="";
                    else
                        value = decodeString(token.substring(i));
                }

                // Add value to the map
                if (name.length() > 0)
                    map.add(name,value);
            }
        }
    }
    
    /* -------------------------------------------------------------- */
    /** Decode String with % encoding.
     */
    public static String decodeString(String encoded)
    {
        int len=encoded.length();
        byte[] bytes=new byte[len];
        char[] characters = encoded.toCharArray();
        int n=0;
        boolean noDecode=true;
        
        for (int i=0;i<len;i++)
        {
            char c = characters[i];
            if (c<0||c>0xff)
                throw new IllegalArgumentException("Not decoded");
            
            byte b = (byte)(0xff & c);
            
            if (c=='+')
            {
                noDecode=false;
                b=(byte)' ';
            }
            else if (c=='%' && (i+2)<len)
            {
                noDecode=false;
                b=(byte)(0xff&Integer.parseInt(encoded.substring(i+1,i+3),16));
                i+=2;
            }
            
            bytes[n++]=b;
        }

        if (noDecode)
            return encoded;

        try
        {    
            return new String(bytes,0,n,StringUtil.__ISO_8859_1);
        }
        catch(UnsupportedEncodingException e)
        {
            Code.warning(e);
            return new String(bytes,0,n);
        }
    }
    
    /* ------------------------------------------------------------ */
    /** Perform URL encoding.
     * @param string 
     * @return encoded string.
     */
    public static String encodeString(String string)
    {
        byte[] bytes=null;
        try
        {
            bytes=string.getBytes(StringUtil.__ISO_8859_1);
        }
        catch(UnsupportedEncodingException e)
        {
            Code.warning(e);
            bytes=string.getBytes();
        }
        
        int len=bytes.length;
        byte[] encoded= new byte[bytes.length*3];
        int n=0;
        boolean noEncode=true;
        
        for (int i=0;i<len;i++)
        {
            byte b = bytes[i];
            
            if (b==' ')
            {
                noEncode=false;
                encoded[n++]=(byte)'+';
            }
            else if (b>='a' && b<='z' ||
                     b>='A' && b<='Z' ||
                     b>='0' && b<='9')
            {
                encoded[n++]=b;
            }
            else
            {
                noEncode=false;
                encoded[n++]=(byte)'%';
                byte nibble= (byte) ((b&0xf0)>>4);
                if (nibble>=10)
                    encoded[n++]=(byte)('A'+nibble-10);
                else
                    encoded[n++]=(byte)('0'+nibble);
                nibble= (byte) (b&0xf);
                if (nibble>=10)
                    encoded[n++]=(byte)('A'+nibble-10);
                else
                    encoded[n++]=(byte)('0'+nibble);
            }
        }

        if (noEncode)
            return string;
        
        try
        {    
            return new String(encoded,0,n,StringUtil.__ISO_8859_1);
        }
        catch(UnsupportedEncodingException e)
        {
            Code.warning(e);
            return new String(encoded,0,n);
        }
    }


    /* ------------------------------------------------------------ */
    /** 
     */
    public Object clone()
    {
        return new UrlEncoded(this);
    }

    public static void main(String[] a)
    {
	System.err.println(encodeString(a[0]));
	System.err.println(decodeString(a[0]));

    }
}

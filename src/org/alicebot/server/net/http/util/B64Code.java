// ========================================================================
// Copyright (c) 1999 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: B64Code.java,v 1.1.1.1 2001/06/17 19:01:46 noelbu Exp $
// ========================================================================

package org.alicebot.server.net.http.util;
import java.io.UnsupportedEncodingException;

/* ------------------------------------------------------------ */
/** Fast B64 Encoder/Decoder.
 *
 * @version $Revision: 1.1.1.1 $
 * @author Greg Wilkins (gregw)
 */
public class B64Code
{
    // ------------------------------------------------------------------
    static final char pad = '=';
    static final char[] nibble2code=
    {
        'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P',
        'Q','R','S','T','U','V','W','X','Y','Z','a','b','c','d','e','f',
        'g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v',
        'w','x','y','z','0','1','2','3','4','5','6','7','8','9','+','/'
    };
    
    static byte[] code2nibble = null;
    static
    {
        code2nibble = new byte[256];
        for (int i=0;i<256;i++)
            code2nibble[i]=-1;
        for (byte b=0;b<64;b++)
            code2nibble[(byte)nibble2code[b]]=b;
        code2nibble[(byte)pad]=0;
    }
    

    // ------------------------------------------------------------------
    static public String encode(String s)
        throws UnsupportedEncodingException
    {
        try
        {
            return encode(s,null);
        }
        catch(UnsupportedEncodingException e)
        {
            Code.fail(e);
        }
        return null;
    }


    // ------------------------------------------------------------------
    static public String encode(String s,String charEncoding)
        throws UnsupportedEncodingException
    {
        int nibble=0;
        byte[] bytes;
        if (charEncoding==null)
            bytes= s.getBytes();
        else
            bytes= s.getBytes(charEncoding);
        char[] encode = new char[bytes.length*2+4];

        int e=0;
        int n=0;
        int code=0;
        for (int i=0;i<bytes.length;i++)
        {
            byte b=bytes[i];
            switch(n++)
            {
              case 0:
                  nibble = b>>2;
                  encode[e++]=nibble2code[nibble];
                  nibble = (b&0x3)<<4;
                  break;
                
              case 1:
                  nibble += b>>4;
                  encode[e++]=nibble2code[nibble];
                  nibble = (b&0xf)<<2;
                  break;
                
              case 2:
              default:
                  n=0;
                  nibble += b>>6;
                  encode[e++]=nibble2code[nibble];
                  nibble = (b&0x3f);
                  encode[e++]=nibble2code[nibble];
                  break;
            }
        }

        switch(n++)
        {
          case 0:
              encode[e++]=pad;
              encode[e++]=pad;
              encode[e++]=pad;
              encode[e++]=pad;
              break;
                
          case 1:
              encode[e++]=nibble2code[nibble];
              encode[e++]=pad;
              encode[e++]=pad;
              encode[e++]=pad;
              break;
                
          case 2:
          default:
              encode[e++]=nibble2code[nibble];
              encode[e++]=pad;
        }
        return new String(encode,0,e);
    }


    
    
    // ------------------------------------------------------------------
    static public String decode(String s)
    {
        try
        {
            return decode(s,null);
        }
        catch(UnsupportedEncodingException e)
        {
            Code.fail(e);
        }
        return null;
    }
    
    // ------------------------------------------------------------------
    static public String decode(String s,String charEncoding)
        throws UnsupportedEncodingException
    {
        byte[] nibble = new byte[4];
        byte[] decode = new byte[s.length()];
        int d=0;
        int n=0;
        byte b;
        
        for (int i=0;i<s.length();i++)
        {
            char c = s.charAt(i);
            if (c>=256)
                throw new IllegalArgumentException("String is not B64 encoded");
            nibble[n] = code2nibble[(int)c];
            if (nibble[n]<0)
                throw new IllegalArgumentException("String is not B64 encoded");
            
            if (c==pad)
                break;
            
            
            switch(n++)
            {
              case 0:
                  break;
                
              case 1:
                  b=(byte)((nibble[0]<<2) + (nibble[1]>>4));
                  decode[d++]=b;
                  break;
                  
              case 2:
                  b=(byte)(((nibble[1]&0xf)<<4) + (nibble[2]>>2));
                  decode[d++]=b;
                  break;
                  
              case 3:
              default:
                  b=(byte)(((nibble[2]&0x3)<<6) + nibble[3]);
                  decode[d++]=b;
                  n=0;
                  break;
            }

        }

        
        if (charEncoding==null)
            return new String(decode,0,d);
        return new String(decode,0,d,charEncoding);
    }

    
}

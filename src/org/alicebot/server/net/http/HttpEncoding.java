// ========================================================================
// Copyright (c) 1999 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: HttpEncoding.java,v 1.1.1.1 2001/06/17 19:00:40 noelbu Exp $
// ========================================================================

package org.alicebot.server.net.http;

import org.alicebot.server.net.http.util.Code;
import java.util.Map;

public class HttpEncoding
{
    /* ------------------------------------------------------------ */
    /** Enable a transfer encoding.
     * Enable a transfer encoding on a ChunkableInputStream.
     * @param in 
     * @param coding Coding name 
     * @param parameters Coding parameters or null
     * @exception HttpException 
     */
    public void enableEncoding(ChunkableInputStream in,
                               String coding,
                               Map parameters)
        throws HttpException
    {
        try
        {
            if ("gzip".equals(coding))
            {
                if (parameters!=null && parameters.size()>0)
                    throw new HttpException(HttpResponse.__501_Not_Implemented,
                                            "gzip parameters");
                in.insertFilter(java.util.zip.GZIPInputStream.class
                                .getConstructor(ChunkableInputStream.__filterArg),
                                null);
            }
            else if ("deflate".equals(coding))
            {
                if (parameters!=null && parameters.size()>0)
                    throw new HttpException(HttpResponse.__501_Not_Implemented,
                                            "deflate parameters");
                in.insertFilter(java.util.zip.InflaterInputStream.class
                                .getConstructor(ChunkableInputStream.__filterArg),
                                null);
            }
            else if (!HttpFields.__Identity.equals(coding))
                throw new HttpException(HttpResponse.__501_Not_Implemented);
        }
        catch (HttpException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            Code.warning(e);
            throw new HttpException(HttpResponse.__500_Internal_Server_Error);
        }
    }
    
    /* ------------------------------------------------------------ */
    /** Enable a transfer encoding.
     * Enable a transfer encoding on a ChunkableOutputStream.
     * @param out
     * @param coding Coding name 
     * @param parameters Coding parameters or null
     * @exception HttpException 
     */
    public void enableEncoding(ChunkableOutputStream out,
                               String coding,
                               Map parameters)
        throws HttpException
    {
        try
        {
            if ("gzip".equals(coding))
            {
                if (parameters!=null && parameters.size()>0)
                    throw new HttpException(HttpResponse.__501_Not_Implemented,
                                            "gzip parameters");
                out.insertFilter(java.util.zip.GZIPOutputStream.class
                                 .getConstructor(ChunkableOutputStream.__filterArg),
                                 null);
            }
            else if ("deflate".equals(coding))
            {
                if (parameters!=null && parameters.size()>0)
                    throw new HttpException(HttpResponse.__501_Not_Implemented,
                                            "deflate parameters");
                out.insertFilter(java.util.zip.DeflaterOutputStream.class
                                 .getConstructor(ChunkableOutputStream.__filterArg),
                                null);
            }
            else if (!HttpFields.__Identity.equals(coding))
                throw new HttpException(HttpResponse.__501_Not_Implemented);
        }
        catch (HttpException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            Code.warning(e);
            throw new HttpException(HttpResponse.__500_Internal_Server_Error);
        }
    }
}

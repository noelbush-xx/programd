// ========================================================================
// Copyright (c) 1999 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: Http10TestClient.java,v 1.1.1.1 2001/06/17 19:00:37 noelbu Exp $
// ========================================================================

package org.alicebot.server.net.http;

import org.alicebot.server.net.http.util.Code;
import org.alicebot.server.net.http.util.InetAddrPort;
import org.alicebot.server.net.http.util.LineInput;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;

/* ------------------------------------------------------------ */
/** HTTP 1.0 Test Client.
 *
 * @version $Revision: 1.1.1.1 $
 * @author Greg Wilkins (gregw)
 */
public class Http10TestClient extends Thread
{

    static String[] url=
    {
        "/javadoc/index.html",
        "/javadoc/overview-summary.html",
        "/javadoc/overview-frame.html",
        "/javadoc/allclasses-frame.html",
        "/javadoc/stylesheet.css",
        "/javadoc/stylesheet.css",
        "/javadoc/stylesheet.css",
    };
    
    int _id;
    int _requests;
    int _responses;
    InetAddrPort _addr;
    boolean _pipeline;

    Http10TestClient(int id,InetAddrPort addr, int r, boolean pipeline)
    {
        _id=id;
        _requests=r;
        _responses=r;
        _addr=addr;
        _pipeline=pipeline;
    }
    
    public void run()
    {
        Socket socket=null;
        int byteCount=0;
        try
        {
            //System.err.println("Connect to "+_addr);
            socket=new Socket(_addr.getInetAddress(),
                              _addr.getPort());
            socket.setSoTimeout(0);
            OutputStream out=socket.getOutputStream();
            LineInput in=new LineInput(socket.getInputStream());


            // pipeline request
            if (_pipeline)
            {
                _requests--;
                //System.err.println(_id+": --> "+_requests);
                
                out.write
                    (("GET "+
                     url[_requests%url.length]+
                     " HTTP/1.0\015\012Connection: Keep-Alive\015\012User-Agent: Mozilla/4.61 [en] (X11; I; Linux 2.2.12-20 i686)\015\012Host: localhost:8000\015\012Accept: image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, image/png, */*\015\012Accept-Encoding: gzip\015\012Accept-Language: en\015\012Accept-Charset: iso-8859-1,*,utf-8\015\012\015\012").getBytes());
                out.flush();
                Thread.yield();
            }

            // loop request response
            while(_requests-->0)
            {
                //System.err.println(_id+": --> "+_requests);
                
                out.write
                    (("GET "+
                     url[_requests%url.length]+
                     " HTTP/1.0\015\012Connection: Keep-Alive\015\012User-Agent: Mozilla/4.61 [en] (X11; I; Linux 2.2.12-20 i686)\015\012Host: localhost:8000\015\012Accept: image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, image/png, */*\015\012Accept-Encoding: gzip\015\012Accept-Language: en\015\012Accept-Charset: iso-8859-1,*,utf-8\015\012\015\012").getBytes());
                out.flush();
                Thread.yield();

                String line;
                int len=-1;
                // Read the header and get the length
                while((line=in.readLine())!=null && line.length()>0)
                {
                    if (line.startsWith("Content-Length:"))
                        len=Integer.parseInt(line.substring(16));
                }

                // break if no response
                if (line==null || len<0)
                    break;

                // read response
                byte buffer[] = new byte[4096];
                byteCount=len;
                while (byteCount>0)
                {
                    int b;
                    if (byteCount<4096)
                        b=in.read(buffer,0,byteCount);
                    else
                        b=in.read(buffer,0,4096);                   
                    
                    if (b==-1)
                        break;
                    byteCount-=b;
                }
                if (byteCount>0)
                    break;
                _responses--;
                //System.err.println(_id+": <-- "+(_responses));
            }

            // pipeline response
            if (_pipeline)
            {
                String line;
                int len=-1;
                int headers=0;
                // Read the header and get the length
                while((line=in.readLine())!=null && line.length()>0)
                {
                    if (headers++==0 && !line.startsWith("HTTP"))
                        Code.fail("Bad response:"+line);
                    
                    if (line.startsWith("Content-Length:"))
                        len=Integer.parseInt(line.substring(16));
                }

                // break if no response
                if (line==null || len<0)
                    return;

                // read response
                byte buffer[] = new byte[4096];
                byteCount=len;
                while (byteCount>0)
                {
                    int b;
                    if (byteCount<4096)
                        b=in.read(buffer,0,byteCount);
                    else
                        b=in.read(buffer,0,4096);                   
                    
                    if (b==-1)
                        break;
                    byteCount-=b;
                }
                if (byteCount>0)
                    return;
                _responses--;
                //System.err.println(_id+": <-- "+(_responses));
            }	    
        }
        catch(Exception e)
        {
            Code.warning(e);
        }
        finally
        {
            if (byteCount>0)
                Code.warning("Missed "+byteCount+" bytes");
            if (_requests>0)
                Code.warning("Missed "+_requests+" requests");
            if (_responses>0)
                Code.warning("Missed "+_responses+" responses");
                
            try{
                if (socket!=null)
                    socket.close();
            }
            catch(Exception e)
            {
                Code.warning(e);
            }
        }
    }

    
    public static void main(String[] args)
    {
        try
        {
            if (args.length!=3)
            {
                System.err.println("Usage - java c.m.H.Http10TestClient <url> <threads> <requests>");
                System.exit(1);
            }
            int threads=Integer.parseInt(args[1]);
            int requests=Integer.parseInt(args[2]);
            
            Http10TestClient[] client = new Http10TestClient[threads];
            for (int c=0;c<threads;c++)
                client[c]=
                    new Http10TestClient(c,
                                   new InetAddrPort(args[0]),
                                   requests,true);

            long start=System.currentTimeMillis();
            for (int c=0;c<threads;c++)
                client[c].start();
            for (int c=0;c<threads;c++)
                client[c].join();
            long end=System.currentTimeMillis();

            System.err.println("Requests/Sec="+((requests*threads)/((end-start)/1000)));
            
        }
        catch(Exception e)
        {
            Code.warning(e);
        }
    }
}










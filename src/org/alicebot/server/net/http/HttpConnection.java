// ========================================================================
// Copyright (c) 1999 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: HttpConnection.java,v 1.1.1.1 2001/06/17 19:00:40 noelbu Exp $
// ========================================================================

package org.alicebot.server.net.http;

import org.alicebot.server.net.http.util.Code;
import org.alicebot.server.net.http.util.StringUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


/* ------------------------------------------------------------ */
/** A HTTP Connection.
 * This class provides the generic HTTP handling for
 * a connection to a HTTP server. An instance of HttpConnection
 * is normally created by a HttpListener and then given control
 * in order to run the protocol handling before and after passing
 * a request to the HttpServer of the HttpListener.
 *
 * This class is not synchronized as it should only ever be known
 * to a single thread.
 *
 * @see HttpListener
 * @see HttpServer
 * @version $Id: HttpConnection.java,v 1.1.1.1 2001/06/17 19:00:40 noelbu Exp $
 * @author Greg Wilkins (gregw)
 */
public class HttpConnection
    implements OutputObserver
{
    /* ------------------------------------------------------------ */
    private HttpListener _listener;
    private ChunkableInputStream _inputStream;
    private ChunkableOutputStream _outputStream;
    private boolean _persistent;
    private boolean _close;
    private boolean _keepAlive;
    private String _version;
    private int _dotVersion;
    private boolean _outputSetup;
    private HttpRequest _request;
    private HttpResponse _response;
    private Thread _handlingThread;
    private InetAddress _remoteAddr;
    private HttpServer _httpServer;

    /* ------------------------------------------------------------ */
    /** Constructor.
     * @param listener The listener that created this connection.
     * @param remoteAddr The address of the remote end or null.
     * @param in InputStream to read request(s) from.
     * @param out OutputputStream to write response(s) to.
     */
    protected HttpConnection(HttpListener listener,
                             InetAddress remoteAddr,
                             InputStream in,
                             OutputStream out)
    {
        _listener=listener;
        _remoteAddr=remoteAddr;
        _inputStream=new ChunkableInputStream(in);
        _outputStream=new ChunkableOutputStream(out);
        _outputStream.addObserver(this);
        _outputSetup=false;
        if (_listener!=null)
            _httpServer=_listener.getHttpServer();
    }

    /* ------------------------------------------------------------ */
    /** Get the Remote address.
     * @return the remote address
     */
    public InetAddress getRemoteAddr()
    {
        return _remoteAddr;
    }

    /* ------------------------------------------------------------ */
    /** Get the connections InputStream.
     * @return the connections InputStream
     */
    public ChunkableInputStream getInputStream()
    {
        return _inputStream;
    }
    
    /* ------------------------------------------------------------ */
    /** Get the connections OutputStream.
     * @return the connections OutputStream
     */
    public ChunkableOutputStream getOutputStream()
    {
        return _outputStream;
    }

    /* ------------------------------------------------------------ */
    /** Get the request.
     * @return the request
     */
    public HttpRequest getRequest()
    {
        return _request;
    }
    
    /* ------------------------------------------------------------ */
    /** Get the response.
     * @return the response
     */
    public HttpResponse getResponse()
    {
        return _response;
    }
    
    /* ------------------------------------------------------------ */
    /** Close the connection.
     * This method calls close on the input and output streams and
     * interrupts any thread in the handle method.
     * may be specialized to close sockets etc.
     * @exception IOException 
     */
    public void close()
        throws IOException
    {
        try{
            _outputStream.close();
            _inputStream.close();
        }
        finally
        {
            if (_handlingThread!=null)
                _handlingThread.interrupt();
        }
    }
    
    /* ------------------------------------------------------------ */
    /** Get the connections listener. 
     * @return HttpListener that created this Connection.
     */
    public HttpListener getListener()
    {
        return _listener;
    }

    /* ------------------------------------------------------------ */
    /** Get the listeners HttpServer .
     * Conveniance method equivalent to getListener().getHttpServer().
     * @return HttpServer.
     */
    public HttpServer getHttpServer()
    {
        return _httpServer;
    }

    /* ------------------------------------------------------------ */
    /** Get the listeners Default scheme. 
     * Conveniance method equivalent to getListener().getDefaultProtocol().
     * @return HttpServer.
     */
    public String getDefaultScheme()
    {
        return _listener.getDefaultScheme();
    }
    
    /* ------------------------------------------------------------ */
    /** Get the listeners HttpServer .
     * Conveniance method equivalent to getListener().getHost().
     * @return HttpServer.
     */
    public String getHost()
    {
        return _listener.getHost();
    }
    
    /* ------------------------------------------------------------ */
    /** Get the listeners Port .
     * Conveniance method equivalent to getListener().getPort().
     * @return HttpServer.
     */
    public int getPort()
    {
        return _listener.getPort();
    }
    
    /* ------------------------------------------------------------ */
    /** Handle the connection.
     * Once the connection has been created, this method is called
     * to handle one or more requests that may be received on the
     * connection.  The method only returns once all requests have been
     * handled, an error has been returned to the requestor or the
     * connection has been closed.
     * The service(request,response) method is called by handle to
     * service each request received on the connection.
     */
    public void handle()
    {
        _handlingThread=Thread.currentThread();
        boolean logRequest=false;
        do
        {
            try
            {
                // Create or recycle connection
                if (_request!=null)
                    _request.recycle(this);
                else
                    _request = new HttpRequest(this);

                if (_response!=null)
                    _response.recycle(this);
                else
                    _response = new HttpResponse(this);
                
                
                // Assume the connection is not persistent,
                // unless told otherwise.
                _persistent=false;
                _close=false;
                _keepAlive=false;
                _dotVersion=0;
                logRequest=false;
             
                Code.debug("Wait for request header...");
                
                try
                {
                    _outputSetup=false;
                    _request.readHeader(getInputStream());
                    _listener.customizeRequest(this,_request);
                }
                catch(HttpException e){throw e;}
                catch(IOException e)
                {
                    Code.ignore(e);
                    _persistent=false;
                    _response.destroy();
                    _response=null;
                    return;
                }
                logRequest=true;
                    
                if (_request.getState()!=HttpMessage.__MSG_RECEIVED)
                    throw new HttpException(_response.__400_Bad_Request);
                    
                if (Code.debug())
                {
                    _response.setField("Jetty-Request",
                                       _request.getRequestLine());
                    Code.debug("REQUEST:\n",_request);
                }
                    
                // Pick response version
                _version=_request.getVersion();
                _dotVersion=_request.getDotVersion();
                
                if (_dotVersion>1)
                {
                    Code.debug("Respond to HTTP/1.X with HTTP/1.1");
                    _version=HttpMessage.__HTTP_1_1;
                    _dotVersion=1;
                }
                
                _response.setVersion(_version);
                _response.setCurrentTime(HttpFields.__Date);
                _response.setField(HttpFields.__Server,Version.__VersionDetail);
                _response.setField(HttpFields.__ServletEngine,Version.__ServletEngine);
            
                // Handle Connection header field
                List connectionValues =
                    _request.getFieldValues(HttpFields.__Connection);
                if (connectionValues!=null)
                {
                    Iterator iter = connectionValues.iterator();
                    while (iter.hasNext())
                    {
                        String token=
                            StringUtil.asciiToLowerCase(iter.next().toString());
                        // handle close token
                        if (token.equals(HttpFields.__Close))
                        {
                            _close=true;
                            _response.setField(HttpFields.__Connection,
                                               HttpFields.__Close);
                        }
                        else if (token.equals(HttpFields.__KeepAlive) && _dotVersion==0)
                            _keepAlive=true;
                            
                        // Remove headers for HTTP/1.0 requests
                        if (_dotVersion==0)
                            _request.forceRemoveField(token);
                    }
                }
                    
                // Handle version specifics
                if (_dotVersion==1)
                    verifyHTTP_1_1();
                else if (_dotVersion==0)
                    verifyHTTP_1_0();
                else if (_dotVersion!=-1)
                    throw new HttpException(_response.__505_HTTP_Version_Not_Supported);
                if (Code.verbose(99))
                    Code.debug("IN is "+
                               (_inputStream.isChunking()
                                ?"chunked":"not chunked")+
                               " Content-Length="+
                               _inputStream.getContentLength());
                    
                // service the request
                service(_request,_response);
            } 
            catch (InterruptedIOException e)
            {
                exception(e);
                _persistent=false;
                try
                {
                    _response.commit();
                    _outputStream.flush();
                }
                catch (IOException e2){exception(e2);}
            }
            catch (Exception e)     {exception(e);}
            catch (Error e)         {exception(e);}
            finally
            {
                // Complete the request
                if (_persistent)
                {
                    try{
                        // Read remaining input
                        while(_inputStream.skip(4096)>0 ||
                              _inputStream.read()>=0);
                    }
                    catch(IOException e)
                    {
                        if (_inputStream.getContentLength()>0)
                            _inputStream.setContentLength(0);
                        _persistent=false;
                        exception(new HttpException(_response.__400_Bad_Request,
                                                    "Missing Content"));
                    }
                        
                    // Check for no more content
                    if (_inputStream.getContentLength()>0)
                    {
                        _inputStream.setContentLength(0);
                        _persistent=false;
                        exception (new HttpException(_response.__400_Bad_Request,
                                                     "Missing Content"));
                    }
                        
                    // Commit the response
                    try{
                        _outputStream.flush(_outputStream.isChunking());
                        _outputStream.resetStream();
                        _inputStream.resetStream();
                    }
                    catch(IOException e) {exception(e);}
                }
                else
                {
                    try{
                        if (_response!=null)
                            _response.commit();
                        _outputStream.flush();
                        _outputStream.close();
                    }
                    catch(IOException e) {exception(e);}
                }

                // Log request and response
                if (Code.debug())
                    Code.debug("RESPONSE:\n",_response);
                if (logRequest && _httpServer!=null && _response!=null)
                    _httpServer.log(_request,_response);
            }    
        }while(_persistent);
        
        // Destroy request and response
        if (_request!=null)
            _request.destroy();
        if (_response!=null)
            _response.destroy();
        _request=null;
        _response=null;
        _handlingThread=null;
        
        try{
            close();
        }
        catch (IOException e)
        {
            Code.ignore(e);
        }
        catch (Exception e)
        {
            Code.warning(e);
        }
    }

    /* ------------------------------------------------------------ */
    /* Exception reporting policy method.
     * @param th 
     */
    private void exception(Throwable e)
    {
        try{
            if ( !Code.debug() && e instanceof IOException )
                // Assume it was the browser closing early
                Code.ignore(e);
            else
                Code.warning(_request.toString(),e);
            
            _persistent=false;
            if (!_response.isCommitted())
            {
                _response.reset();
                _response.removeField(HttpFields.__TransferEncoding);
                _response.setField(HttpFields.__Connection,
                                   HttpFields.__Close);
                
                _response.sendError(HttpResponse.__500_Internal_Server_Error,e);
            }
        }
        catch(IOException ex)
        {
            Code.warning(ex);
        }
    }
    
    
    /* ------------------------------------------------------------ */
    /** Service a Request.
     * This implementation passes the request and response to the
     * service method of the HttpServer for this connections listener.
     * If no HttpServer has been associated, the 503 is returned.
     * This method may be specialized to implement other ways of
     * servicing a request.
     * @param request The request
     * @param response The response
     * @exception HttpException 
     * @exception IOException 
     */
    protected void service(HttpRequest request, HttpResponse response)
        throws HttpException, IOException
    {
        if (_httpServer==null)
                throw new HttpException(response.__503_Service_Unavailable);
        _httpServer.service(request,response);
    }
    

    /* ------------------------------------------------------------ */
    /* Verify HTTP/1.0 request
     * @exception HttpException problem with the request. 
     * @exception IOException problem with the connection.
     */
    private void verifyHTTP_1_0()
        throws HttpException, IOException
    {     
        // Set content length
        int content_length=
            _request.getIntField(HttpFields.__ContentLength);
        if (content_length>=0)
            _inputStream.setContentLength(content_length);
        else if (content_length<0)
        {
            // XXX - can't do this check because IE does this after
            // a redirect.
            // Can't have content without a content length
//              String content_type=_request.getField(HttpFields.__ContentType);
//              if (content_type!=null && content_type.length()>0)
//                  throw new HttpException(_response.__411_Length_Required);
            _inputStream.setContentLength(0);
        }

        // dont support persistent connections in HTTP/1.0
        _persistent=_keepAlive;
    }
    
    /* ------------------------------------------------------------ */
    /* Verify HTTP/1.1 request
     * @exception HttpException problem with the request. 
     * @exception IOException problem with the connection.
     */
    private void verifyHTTP_1_1()
        throws HttpException, IOException
    {        
        // Check Host Field exists
        String host=_request.getField(HttpFields.__Host);
        if (host==null || host.length()==0)
            throw new HttpException(_response.__400_Bad_Request);
        
        // check and enable requests transfer encodings.
        boolean _inputEncodings=false;
        List transfer_coding=_request.getFieldValues(HttpFields.__TransferEncoding);
        if (transfer_coding!=null)
        {
            HashMap coding_params = new HashMap(7);
            for (int i=transfer_coding.size(); i-->0;)
            {
                coding_params.clear();
                String coding =
                    HttpFields.valueParameters(transfer_coding.get(i).toString(),
                                               coding_params);
                coding=StringUtil.asciiToLowerCase(coding);

                // Ignore identity coding
                if (HttpFields.__Identity.equals(coding))
                    continue;

                // We have none identity encodings
                _inputEncodings=true;
                
                // Handle Chunking
                if (HttpFields.__Chunked.equals(coding))
                {
                    // chunking must be last and have no parameters
                    if (i+1!=transfer_coding.size() ||
                        coding_params.size()>0)
                        throw new HttpException(_response.__400_Bad_Request);
                    _inputStream.setChunking();
                }
                else
                    getHttpServer().getHttpEncoding()
                        .enableEncoding(_inputStream,coding,coding_params);
            }
        }
        
        // Check input content length can be determined
        int content_length=_request.getIntField(HttpFields.__ContentLength);
        String content_type=_request.getField(HttpFields.__ContentType);
        if (_inputEncodings)
        {
            // Must include chunked
            if (!_inputStream.isChunking())
                throw new HttpException(_response.__400_Bad_Request);
        }
        else
        {
            // If we have a content length, use it
            if (content_length>=0)
                _inputStream.setContentLength(content_length);
            // else if we have no content
            else if (content_type==null || content_type.length()==0)
                _inputStream.setContentLength(0);
            // else we need a content length
            else
            {
                // XXX - can't do this check as IE stuff up on
                // a redirect.
                // throw new HttpException(_response.__411_Length_Required);
                _inputStream.setContentLength(0);
            }
        }

        // Handle Continue Expectations
        String expect=_request.getField(HttpFields.__Expect);
        if (expect!=null && expect.length()>0)
        {
            if (StringUtil.asciiToLowerCase(expect)
                .equals(HttpFields.__ExpectContinue))
            {
                // Send continue if no body available yet.
                if (_inputStream.available()<=0)
                {
                    _outputStream.getRawStream()
                        .write(_response.__Continue);
                    _outputStream.getRawStream()
                        .flush();
                }
            }
            else
                throw new HttpException(_response.__417_Expectation_Failed);
        }
        else if (_inputStream.available()<=0 &&
                 (_request.__PUT.equals(_request.getMethod()) ||
                  _request.__POST.equals(_request.getMethod())))
        {
            // Send continue for RFC 2068 exception
            _outputStream.getRawStream()
                .write(_response.__Continue);
            _outputStream.getRawStream()
                .flush();
        }            
             
        // Persistent unless requested otherwise
        _persistent=!_close;

    }
    

    /* ------------------------------------------------------------ */
    /** Output Notifications.
     * Trigger header and/or filters from output stream observations.
     * Also finalizes method of indicating response content length.
     * Called as a result of the connection subscribing for notifications
     * to the ChunkableOutputStream.
     * @see ChunkableOutputStream
     * @param out The output stream observed.
     * @param action The action.
     */
    public void outputNotify(ChunkableOutputStream out, int action)
        throws IOException
    {
        if (_response==null)
            return;

        switch(action)
        {
          case OutputObserver.__FIRST_WRITE:
              if (!_outputSetup)
                  setupOutputStream();
              break;
              
          case OutputObserver.__RESET_BUFFER:
              _outputSetup=false;
              break;
              
          case OutputObserver.__COMMITING:
              if (_response.getState()==HttpMessage.__MSG_EDITABLE)
                  _response.commitHeader();
              break;
              
          case OutputObserver.__COMMITED:
              break;
              
          case OutputObserver.__CLOSING:
              _response.complete();
              break;
              
          case OutputObserver.__CLOSED:
              break;
        }
    }

    /* ------------------------------------------------------------ */
    /** Setup the reponse output stream.
     * Use the current state of the request and response, to set tranfer
     * parameters such as chunking and content length.
     */
    public void setupOutputStream()
        throws IOException
    {
        if (_outputSetup)
            return;
        _outputSetup=true;
        
        // Determine how to limit content length and
        // enable output transfer encodings 
        List transfer_coding=_response.getFieldValues(HttpFields.__TransferEncoding);
        if (transfer_coding==null || transfer_coding.size()==0)
        {
            switch(_dotVersion)
            {
              case 1:
                  {
                      // if not closed and no length
                      if ((!HttpFields.__Close.equals(_response.getField(HttpFields.__Connection)))&&
                          (_response.getField(HttpFields.__ContentLength)==null))
                      {
                          // Chunk it!
                          _response.removeField(HttpFields.__ContentLength);
                          _response.setField(HttpFields.__TransferEncoding,
                                         HttpFields.__Chunked);
                          _outputStream.setChunking();
                      }
                      break;
                  }
              case 0:
                  {
                      // If we dont have a content length, we can't be
                      // persistent 
                      if (!_keepAlive || !_persistent ||
                          _response.getIntField(HttpFields.__ContentLength)<0)
                      {
                          _persistent=false;
                          if (_keepAlive)
                              _response.setField(HttpFields.__Connection,
                                                 HttpFields.__Close);
                          _keepAlive=false;
                      }
                      else if (_keepAlive)
                          _response.setField(HttpFields.__Connection,
                                             HttpFields.__KeepAlive);
                      break;
                  }
              default:
                  _keepAlive=false;
                  _persistent=false;
            }
        }
        else if (_dotVersion<1)
        {
            // Error for transfer encoding to be set in HTTP/1.0
            _response.removeField(HttpFields.__TransferEncoding);
            throw new HttpException(_response.__501_Not_Implemented,
                                    "Transfer-Encoding not supported in HTTP/1.0");
        }
        else
        {
            // Examine and apply transfer encodings
            HashMap coding_params = new HashMap(7);
            for (int i=transfer_coding.size();i-->0;)
            {
                coding_params.clear();
                String coding =
                    HttpFields.valueParameters(transfer_coding.get(i).toString(),
                                               coding_params);
                coding=StringUtil.asciiToLowerCase(coding);

                // Ignore identity coding
                if (HttpFields.__Identity.equals(coding))
                    continue;
                
                // Handle Chunking
                if (HttpFields.__Chunked.equals(coding))
                {
                    // chunking must be last and have no parameters
                    if (i+1!=transfer_coding.size() ||
                        coding_params.size()>0)
                        throw new HttpException(_response.__400_Bad_Request,
                                                "Missing or incorrect chunked transfer-encoding");
                    _outputStream.setChunking();
                }
                else
                {
                    // Check against any TE field
                    List te = _request.getAcceptableTransferCodings();
                    if (te==null || !te.contains(coding))
                        throw new HttpException(_response.__501_Not_Implemented,
                                                "User agent does not accept "+
                                                coding+
                                                " transfer-encoding");

                    // Set coding
                    getHttpServer().getHttpEncoding()
                        .enableEncoding(_outputStream,coding,coding_params);
                }
            }
        }

        // Nobble the OutputStream for HEAD requests
        if (_request.__HEAD.equals(_request.getMethod()))
            _outputStream.nullOutput();
    }

    
    /* ------------------------------------------------------------ */
    void commitResponse()
        throws IOException
    {            
        _outputSetup=true;
        
        // Handler forced close
        _close=HttpFields.__Close.equals
            (_response.getField(HttpFields.__Connection));
        if (_close)
            _persistent=false;
        
        // if we have no content or encoding,
        // and no content length
        // need to set content length (XXX or may just close connection?)
        if (!_outputStream.isWritten() &&
            !_response.containsField(HttpFields.__TransferEncoding) &&
            !_response.containsField(HttpFields.__ContentLength))
        {
            if(_persistent)
            {
                _response.setIntField(HttpFields.__ContentLength,0);
                if (_dotVersion==0)
                {
                    // Netscape does not like empty responses with
                    // keep-alive
                    if (_response.getStatus()==200)
                    {
                        _close=true;
                        _persistent=false;
                        _response.setField(HttpFields.__Connection,
                                           HttpFields.__Close);
                    }
                    else
                        // Keep it alive
                        _response.setField(HttpFields.__Connection,
                                           HttpFields.__KeepAlive);
                }
            }
            else
            {
                _close=true;
                _response.setField(HttpFields.__Connection,
                                   HttpFields.__Close);
            }
        }
    }
}




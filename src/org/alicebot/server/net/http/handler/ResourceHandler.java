package org.alicebot.server.net.http.handler;

import org.alicebot.server.net.http.ChunkableOutputStream;
import org.alicebot.server.net.http.HandlerContext;
import org.alicebot.server.net.http.HttpException;
import org.alicebot.server.net.http.HttpFields;
import org.alicebot.server.net.http.HttpMessage;
import org.alicebot.server.net.http.HttpRequest;
import org.alicebot.server.net.http.HttpResponse;
import org.alicebot.server.net.http.PathMap;
import org.alicebot.server.net.http.InclusiveByteRange;
import org.alicebot.server.net.http.util.Code;
import org.alicebot.server.net.http.util.Log;
import org.alicebot.server.net.http.util.Resource;
import org.alicebot.server.net.http.util.StringUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;
import java.util.Map;

/* ------------------------------------------------------------ */
/** Handler to serve files and resources.
 * Serves files from a given resource URL base and implements
 * the GET, HEAD, DELETE, OPTIONS, MOVE methods and the
 * IfModifiedSince and IfUnmodifiedSince header fields.
 * A simple memory cache is also provided to reduce file I/O.
 * 
 * @version $Id: ResourceHandler.java,v 1.1.1.1 2001/06/17 19:02:30 noelbu Exp $
 * @author Nuno Preguica 
 * @author Greg Wilkins
 */
public class ResourceHandler extends NullHandler
{
    /* ----------------------------------------------------------------- */
    private String _allowHeader = null;
    private CachedFile _mostRecentlyUsed=null;
    private CachedFile _leastRecentlyUsed=null;
    private Map _cacheMap=null;
    private boolean _dirAllowed=true;
    private boolean _putAllowed=false;
    private boolean _delAllowed=false;
    private int _maxCachedFiles=128;
    private int _maxCachedFileSize =40960;
    private Resource _resourceBase=null;
    private boolean _handleGeneralOptionsQuery=true;
 
    /* ------------------------------------------------------------ */
    List _indexFiles =new ArrayList(2);
    {
        _indexFiles.add("index.html");
        _indexFiles.add("index.htm");
    }
 
    /* ------------------------------------------------------------ */
    public boolean isDirAllowed()
    {
        return _dirAllowed;
    }
    /* ------------------------------------------------------------ */
    public void setDirAllowed(boolean dirAllowed)
    {
        _dirAllowed = dirAllowed;
    }
 
    /* ------------------------------------------------------------ */
    public boolean isPutAllowed()
    {
        return _putAllowed;
    }
    /* ------------------------------------------------------------ */
    public void setPutAllowed(boolean putAllowed)
    {
        _putAllowed = putAllowed;
    }

    /* ------------------------------------------------------------ */
    public boolean isDelAllowed()
    {
        return _delAllowed;
    }
    /* ------------------------------------------------------------ */
    public void setDelAllowed(boolean delAllowed)
    {
        _delAllowed = delAllowed;
    }
 
    /* ------------------------------------------------------------ */
    public List getIndexFiles()
    {
        return _indexFiles;
    }
 
    /* ------------------------------------------------------------ */
    public void setIndexFiles(List indexFiles)
    {
        if (indexFiles==null)
            _indexFiles=new ArrayList(5);
        else
            _indexFiles = indexFiles;
    }
 
    /* ------------------------------------------------------------ */
    public void addIndexFile(String indexFile)
    {
        _indexFiles.add(indexFile);
    }
 
    /* ------------------------------------------------------------ */
    public int getMaxCachedFiles()
    {
        return _maxCachedFiles;
    }

    /* ------------------------------------------------------------ */
    public void setMaxCachedFiles(int maxCachedFiles_)
    {
        _maxCachedFiles = maxCachedFiles_;
    }
 
    /* ------------------------------------------------------------ */
    public int getMaxCachedFileSize()
    {
        return _maxCachedFileSize;
    }
 
    /* ------------------------------------------------------------ */
    public void setMaxCachedFileSize(int maxCachedFileSize)
    {
        _maxCachedFileSize = maxCachedFileSize;
    }

    /* ------------------------------------------------------------ */
    public boolean getHandleGeneralOptionsQuery()
    {
        return _handleGeneralOptionsQuery;
    }

    /* ------------------------------------------------------------ */
    public void setHandleGeneralOptionsQuery(boolean b)
    {
        _handleGeneralOptionsQuery=b;
    }


    /* ----------------------------------------------------------------- */
    /** Construct a ResourceHandler.
     */
    public ResourceHandler()
    {}

 
    /* ----------------------------------------------------------------- */
    public synchronized void start()
    {
        try
        {
            _resourceBase=getHandlerContext().getResourceBase();

            Log.event("ResourceHandler started in "+ _resourceBase);
            _mostRecentlyUsed=null;
            _leastRecentlyUsed=null;
            if (_maxCachedFiles>0 && _maxCachedFileSize>0)
                _cacheMap=new HashMap();
            super.start();
        }
        catch(Exception e)
        {
            Code.warning(e);
            throw new Error(e.toString());
        }
    }
 
    /* ----------------------------------------------------------------- */
    public void stop()
    {
        super.stop();
    }
 
    /* ----------------------------------------------------------------- */
    public void destroy()
    {
        synchronized(_cacheMap)
        {
            if( _cacheMap != null)
                _cacheMap.clear();
            _cacheMap=null;
            _mostRecentlyUsed=null;
            _leastRecentlyUsed=null;
        }
        super.destroy();
    }

    /* ------------------------------------------------------------ */
    /** Translate path to a real file path.
     * @param pathSpec 
     * @param path 
     * @return 
     */
    private Resource makeresource(String pathSpec,String path)
        throws MalformedURLException,IOException
    {
        Resource resourceBase=getHandlerContext().getResourceBase();
        if (resourceBase==null)
            return null;
        String info=PathMap.pathInfo(pathSpec,path);
        if (info==null)
            info=path;
        
        return resourceBase.addPath(info);
    }
 
    /* ------------------------------------------------------------ */
    public void handle(String pathInContext,
                       HttpRequest request,
                       HttpResponse response)
        throws HttpException, IOException
    {
        // Strip any path params
        if (pathInContext!=null && pathInContext.indexOf(";")>0)
            pathInContext=pathInContext.substring(0,pathInContext.indexOf(";"));
        
        // Extract and check filename
        pathInContext=Resource.canonicalPath(pathInContext);
        if (pathInContext==null)
            throw new HttpException(HttpResponse.__403_Forbidden);
        
        Resource resourceBase=getHandlerContext().getResourceBase();
        if (resourceBase==null)
            return;
        
        boolean endsWithSlash= pathInContext.endsWith("/");
        Resource resource = resourceBase.addPath(pathInContext);
        
        try
        {
            Code.debug("\nPATH=",pathInContext,
                       "\nRESOURCE=",resource);
            
            // check filename
            
            String method=request.getMethod();
            if (method.equals(HttpRequest.__GET) ||
                method.equals(HttpRequest.__HEAD))
                handleGet(request, response, pathInContext, resource, endsWithSlash);  
            else if (method.equals(HttpRequest.__PUT))
                handlePut(request, response, pathInContext, resource);
            else if (method.equals(HttpRequest.__DELETE))
                handleDelete(request, response, pathInContext, resource);
            else if (method.equals(HttpRequest.__OPTIONS))
                handleOptions(response, pathInContext);
            else if (method.equals(HttpRequest.__MOVE))
                handleMove(request, response, pathInContext, resource);
            else
            {
                Code.debug("Unknown action:"+method);
                // anything else...
                try{
                    if (resource.exists())
                        response.sendError(response.__501_Not_Implemented);
                }
                catch(Exception e) {Code.ignore(e);}
            }
        }
        finally
        {
            if (resource!=null)
                resource.release();
        }
    }

    /* ------------------------------------------------------------------- */
    public void handleGet(HttpRequest request,
                          HttpResponse response,
                          String path,
                          Resource resource,
                          boolean endsWithSlash)
        throws IOException
    {
        Code.debug("Looking for ",resource);
  
        // Try a cache lookup
        if (_cacheMap!=null && !endsWithSlash)
        {
            CachedFile cachedFile = null;
            synchronized(_cacheMap)
            {
                cachedFile= (CachedFile)_cacheMap.get(resource.toString());

                if (cachedFile!=null && cachedFile.isValid() &&
                    !passConditionalHeaders(request,response,cachedFile.resource))
                    return;
            }

            if (cachedFile != null)
            {
                Code.debug("Cache hit: ",resource);
                sendData(request, response, cachedFile);
                return;
            }
        }  
 
        if (resource!=null && resource.exists())
        {
            // Check modified dates
            if (!passConditionalHeaders(request,response,resource))
                return;
     
            // check if directory
            if (resource.isDirectory())
            {
                if (!endsWithSlash && !path.equals("/"))
                {
                    Code.debug("Redirect to directory/");
                    
                    String q=request.getQuery();
                    StringBuffer buf=request.getRequestURL();
                    buf.append("/");
                    if (q!=null&&q.length()!=0)
                        buf.append("?"+q);
                    response.setField(HttpFields.__Location, buf.toString());
                    response.sendError(302);
                    return;
                }
  
                // See if index file exists
                for (int i=_indexFiles.size();i-->0;)
                {
                    Resource index =
                        resource.addPath((String)_indexFiles.get(i));
      
                    if (index.exists())
                    {
                        // Forward to the index
                        int last=request.setState(HttpMessage.__MSG_EDITABLE);
                        request.setPath(request.getPath()+_indexFiles.get(i));
                        request.setState(last);
                        getHandlerContext().handle(request,response);
                        return;
                    }
                }

                // If we got here, no forward to index took place
                sendDirectory(request,response,resource,
                              path.length()>1);
            }
            // check if it is a file
            else if (resource.exists())
            {
                if (!endsWithSlash)
                    sendFile(request,response,resource);
            }
            else
                // don't know what it is
                Code.warning("Unknown file type");
        }
    }

 
    /* ------------------------------------------------------------ */
    /* Check modification date headers.
     */
    private boolean passConditionalHeaders(HttpRequest request,
                                           HttpResponse response,
                                           Resource resource)
        throws IOException
    {
        if (!request.getMethod().equals(HttpRequest.__HEAD))
        {
            // check any modified headers.
            long date=0;
            
            if ((date=request.getDateField(HttpFields.__IfUnmodifiedSince))>0)
            {
                if (resource.lastModified() > date)
                {
                    response.sendError(response.__412_Precondition_Failed);
                    return false;
                }
            }
            
            if ((date=request.getDateField(HttpFields.__IfModifiedSince))>0)
            {
                if (resource.lastModified() <= date)
                {
                    response.sendError(response.__304_Not_Modified);
                    return false;
                }
            }
   
        }
        return true;
    }
 
 
    /* ------------------------------------------------------------ */
    void handlePut(HttpRequest request,
                   HttpResponse response,
                   String path,
                   Resource resource)
        throws IOException
    {
        Code.debug("PUT ",path," in ",resource);

        if (!_putAllowed)
            return;

        if (resource.exists() &&
            !passConditionalHeaders(request,response,resource))
            return;
        
        try
        {
            int toRead = request.getIntField(HttpFields.__ContentLength);
            InputStream in = request.getInputStream();
     
            OutputStream fos = resource.getOutputStream();
            final int bufSize = 1024;
            byte bytes[] = new byte[bufSize];
            int read;
            Code.debug(HttpFields.__ContentLength+"="+toRead);
            while (toRead > 0 &&
                   (read = in.read(bytes, 0,
                                   (toRead>bufSize?bufSize:toRead))) > 0)
            {
                toRead -= read;
                fos.write(bytes, 0, read);
                if (Code.debug())
                    Code.debug("Read " + read + "bytes: " + bytes);
            }
            in.close();
            fos.close();
            request.setHandled(true);
            response.sendError(response.__204_No_Content);
        }
        catch (SecurityException sex)
        {
            Code.warning(sex);
            response.sendError(response.__403_Forbidden,
                               sex.getMessage());
        }
        catch (Exception ex)
        {
            Code.warning(ex);
        }
    }

    /* ------------------------------------------------------------ */
    void handleDelete(HttpRequest request,
                      HttpResponse response,
                      String path,
                      Resource resource)
        throws IOException
    {
        Code.debug("DELETE ",path," from ",resource);  
 
        if (!resource.exists() ||
            !passConditionalHeaders(request,response,resource))
            return;
 
        if (!_delAllowed)
        {
            setAllowHeader(response);
            response.sendError(response.__405_Method_Not_Allowed);
            return;
        }
 
        try
        {
            // delete the file
            resource.delete();

            // flush the cache
            if (_cacheMap!=null)
            {
                CachedFile cachedFile=(CachedFile)_cacheMap.get(resource.toString());
                if (cachedFile!=null)
                    cachedFile.invalidate();
            }

            // Send response
            request.setHandled(true);
            response.sendError(response.__204_No_Content);
        }
        catch (SecurityException sex)
        {
            Code.warning(sex);
            response.sendError(response.__403_Forbidden, sex.getMessage());
        }
    }

 
    /* ------------------------------------------------------------ */
    void handleMove(HttpRequest request,
                    HttpResponse response,
                    String pathInContext,
                    Resource resource)
        throws IOException
    {
        if (!resource.exists() || !passConditionalHeaders(request,response,resource))
            return;

        if (!_delAllowed || !_putAllowed)
        {
            setAllowHeader(response);
            response.sendError(response.__405_Method_Not_Allowed);
            return;
        }
 
        String newPath = Resource.canonicalPath(request.getField("New-uri"));
        if (newPath==null)
        {
            response.sendError(response.__405_Method_Not_Allowed,
                               "Bad new uri");
            return;
        }

        String contextPath = getHandlerContext().getContextPath();
        if (contextPath!=null && !newPath.startsWith(contextPath))
        {
            response.sendError(response.__405_Method_Not_Allowed,
                               "Not in context");
            return;
        }
        

        // Find path
        try
        {
            // XXX - Check this
            String newInfo=newPath;
            if (contextPath!=null)
                newInfo=newInfo.substring(contextPath.length());
            Resource newFile = _resourceBase.addPath(newInfo);
     
            Code.debug("Moving "+resource+" to "+newFile);
            resource.renameTo(newFile);
    
            request.setHandled(true);
            response.sendError(response.__204_No_Content);
        }
        catch (Exception ex)
        {
            Code.warning(ex);
            setAllowHeader(response);
            response.sendError(response.__405_Method_Not_Allowed,
                               "Error:"+ex);
            return;
        }
    }
 
    /* ------------------------------------------------------------ */
    void handleOptions(HttpResponse response, String path)
        throws IOException
    {
        if (!_handleGeneralOptionsQuery && path.equals("*")) 
            return;

        setAllowHeader(response);
        response.commit();
    }
 
    /* ------------------------------------------------------------ */
    void setAllowHeader(HttpResponse response)
    {
        if (_allowHeader == null)
        {
            StringBuffer sb = new StringBuffer(128);
            sb.append(HttpRequest.__GET);
            sb.append(", ");
            sb.append(HttpRequest.__HEAD);
            if (_putAllowed){
                sb.append(", ");
                sb.append(HttpRequest.__PUT);
            }
            if (_delAllowed){
                sb.append(", ");
                sb.append(HttpRequest.__DELETE);
            }
            if (_putAllowed && _delAllowed)
            {
                sb.append(", ");
                sb.append(HttpRequest.__MOVE);
            }
            sb.append(", ");
            sb.append(HttpRequest.__OPTIONS);
            _allowHeader = sb.toString();
        }
        response.setField(HttpFields.__Allow, _allowHeader);
    }
 


    /* ------------------------------------------------------------ */
    void sendData(HttpRequest request,
                  HttpResponse response,
                  SendableResource data)
        throws IOException
    {
        //
        //  see if there are any range headers
        //
        String reqRanges = request.getField(HttpFields.__Range);
        List validRanges = null;

        if ( reqRanges != null )
        {
            ArrayList ranges=new ArrayList(1);
            ranges.add(reqRanges);
            validRanges = InclusiveByteRange
                .parseRangeHeaders(ranges);
            Code.debug("requested ranges: " + reqRanges + "=" + validRanges);
        }

        // 
        //  if there were no valid ranges, send entire entity
        //
        long resLength = data.getLength();
        if (validRanges == null || validRanges.size() == 0) {
            data.writeHeaders(response, resLength);
            data.writeBytes(response.getOutputStream(), 0, resLength);
            request.setHandled(true);
            return;
        }


        //
        //  run through the ranges and count satisfiable ranges;
        //  also try to collapse overlapping or adjacent ranges 
        //  into a single range
        //
        ListIterator rit = validRanges.listIterator();
        InclusiveByteRange singleSatisfiableRange = null;
        int satisfiableRangeCount = 0;

        while (rit.hasNext())
        {
            InclusiveByteRange ibr = (InclusiveByteRange) rit.next();

            long first0 = ibr.getFirst(resLength);
            if (first0 >= resLength) {
                Code.debug("no satisfiable: ",ibr);
                continue;   // not satisfiable
            }

            if (singleSatisfiableRange == null) {
                singleSatisfiableRange = ibr;
                satisfiableRangeCount = 1;
                Code.debug("first satisfiable range: ",ibr);
                continue;   // found first sat range
            }

            long last1 = singleSatisfiableRange.getLast(resLength);
            if (first0 > (last1 + 1)) {
                satisfiableRangeCount++;
                singleSatisfiableRange = null;
                Code.debug("second (right) satisfiable range: ", ibr);
                break;   // just found second non-overlapping sat range
            }

            long first1 = singleSatisfiableRange.getFirst(resLength);
            long last0 = ibr.getLast(resLength);
            if (last0 < (first1 - 1)) {
                satisfiableRangeCount++;
                singleSatisfiableRange = null;
                Code.debug("second (left) satisfiable range: ", ibr);
                break;   // just found second non-overlapping sat range
            }

            // ranges overlap -> merge the two ranges

            long first = first0;
            long last  = last0;

            if (first1 < first) {
                  first = first1;
            }

            if (last1 > last) {
                  last = last1;
            }

            Code.debug("merged ", ibr," into single satisfiable range: ", singleSatisfiableRange);
            singleSatisfiableRange = new InclusiveByteRange(first, last);
        }


        // 
        //  if there are no satisfiable ranges, send 416 response
        //

        if (satisfiableRangeCount == 0) {
            Code.debug("no satisfiable ranges");
            response.setField(
                       HttpFields.__ContentRange, 
                       InclusiveByteRange.to416HeaderRangeString(resLength)
            );
            response.sendError(response.__416_Requested_Range_Not_Satisfiable);
            request.setHandled(true);
            return;
        }


        // 
        //  if there is only a single valid range (must be satisfiable 
        //  since were here now), send that range with a 216 response
        // 

        if (satisfiableRangeCount == 1) {
            Code.debug("single satisfiable range: " + singleSatisfiableRange);
            long singleLength = singleSatisfiableRange.getSize(resLength);
            data.writeHeaders(response, singleLength);
            response.setField(
                       HttpFields.__ContentRange, 
                       singleSatisfiableRange.toHeaderRangeString(resLength)
            );
            data.writeBytes(response.getOutputStream(), 
                        singleSatisfiableRange.getFirst(resLength), 
                        singleLength);
            response.sendError(response.__206_Partial_Content);
            request.setHandled(true);
            return;
        }


        // 
        //  multiple non-overlapping valid ranges cause a multipart
        //  216 response which does not require an overall 
        //  content-length header
        // 

        /** this is sample code for what could eventually be the
         ** complete implementation including multipart responses

        String encoding = data.getEncoding();
        MultiPartResponse multi = new MultiPartResponse(request, response);
        rit = validRanges.listIterator();
        boolean isFirstMultiPart = true;

        while (rit.hasNext()) {
            InclusiveByteRange ibr = (InclusiveByteRange) rit.next();
            long first = ibr.getFirst(resLength);
            if (first >= resLength) 
                continue;   // not satisfiable

            Code.debug("next satisfiable range: " + ibr);
            if (isFirstMultiPart)
                isFirstMultiPart = false;
            else
                multi.endPart();
            multi.startNextPart(encoding);
            data.writeBytes( multi.out, first, ibr.getSize(resLength));
        }
        multi.endLastPart();
        response.sendError(response.__206_Partial_Content);
        request.setHandled(true);


         ** until this is all implemented, we just pretend we dont
         ** support ranges for such a request and send the whole 
         ** enchilada.
         **/

        data.writeHeaders(response, resLength);
        data.writeBytes(response.getOutputStream(), 0, resLength);
        request.setHandled(true);
        return;
    }


    /* ------------------------------------------------------------ */
    void sendFile(HttpRequest request,
                  HttpResponse response,
                  Resource resource)
        throws IOException
    {
        Code.debug("sendFile: ",resource);

        SendableResource data = null;

        // Can the file be cached?
        if (_cacheMap!=null && resource.length()>0 &&
            resource.length()<_maxCachedFileSize)
            data = new CachedFile(resource);
        else
            data = new UnCachedFile(resource);

        try
        {
            sendData(request, response, data);
        }
        finally
        {
            data.requestDone();
        }
    }


    /* ------------------------------------------------------------------- */
    void sendDirectory(HttpRequest request,
                       HttpResponse response,
                       Resource file,
                       boolean parent)
        throws IOException
    {
        if (_dirAllowed)
        {
            String[] ls = file.list();
            if (ls==null)
            {
                // Just send it as a file and hope that the URL
                // formats the directory
                try{
                    sendFile(request,response,file);
                }
                catch(IOException e)
                {
                    Code.ignore(e);
                    response.sendError(HttpResponse.__403_Forbidden,
                                       "Invalid directory");
                }
                return;
            }

            Code.debug("sendDirectory: "+file);
            String base = request.getPath();
            if (!base.endsWith("/"))
                base+="/";
     
            response.setField(HttpFields.__ContentType,
                              "text/html");
            if (request.getMethod().equals(HttpRequest.__HEAD))
            {
                // Bail out here otherwise we build the page fruitlessly and get
                // hit with a HeadException when we try to write the page...
                response.commit();
                return;
            }
     
            String title = "Directory: "+base;
     
            ChunkableOutputStream out=response.getOutputStream();
     
            out.print("<HTML><HEAD><TITLE>");
            out.print(title);
            out.print("</TITLE></HEAD><BODY>\n<H1>");
            out.print(title);
            out.print("</H1><TABLE BORDER=0>");
     
            if (parent)
            {
                out.print("<TR><TD><A HREF=");
                out.print(padSpaces(base));
                out.print("../>Parent Directory</A></TD><TD></TD><TD></TD></TR>\n");
            }
     
            DateFormat dfmt=DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
                                                           DateFormat.MEDIUM);
            for (int i=0 ; i< ls.length ; i++)
            {
                Resource item = file.addPath(ls[i]);
  
                out.print("<TR><TD><A HREF=\"");
                String path=base+ls[i];
                if (item.isDirectory() && !path.endsWith("/"))
                    path+="/";
                out.print(padSpaces(path));
                out.print("\">");
                out.print(ls[i]);
                out.print("&nbsp;");
                out.print("</TD><TD ALIGN=right>");
                out.print(""+item.length());
                out.print(" bytes&nbsp;</TD><TD>");
                out.print(dfmt.format(new Date(item.lastModified())));
                out.print("</TD></TR>\n");
            }
            out.println("</TABLE>");
            request.setHandled(true);
        }
        else
        {
            // directory request not allowed
            response.sendError(HttpResponse.__403_Forbidden,
                               "Directory access not allowed");
        }
    }


 
    /* ------------------------------------------------------------ */
    /**
     * Replaces spaces by %20
     */
    private String padSpaces(String str)
    {
        return StringUtil.replace(str," ","%20");
    }
 

 
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */

    private interface SendableResource
    {
        long getLength();
        String getEncoding();
        void writeHeaders(HttpResponse response, long count)
                                throws IOException; 
        void writeBytes(ChunkableOutputStream os, long startByte, long count) 
                                throws IOException; 
        void requestDone();
    }


    /* ------------------------------------------------------------ */
    /** Holds an uncached file.  
     */

    private class UnCachedFile implements SendableResource {

        Resource resource;
        InputStream ris = null;
        String encoding;
        long length = 0;
        long pos = 0;


        public String getEncoding()
        {
            return encoding;
        }

        public long getLength()
        {
            return length;
        }

        public UnCachedFile(Resource resource)
        {
            this.resource = resource;
            encoding = getHandlerContext().getMimeByExtension(resource.getName());
            length = resource.length();
        }

        public void writeBytes(ChunkableOutputStream os, long start,long count)
            throws IOException
        {
            if (ris == null || pos > start)
            {
                ris = resource.getInputStream();
                pos = 0;
            }
            
            if (pos < start) {
                ris.skip(start - pos);
                pos = start;
            } 
            
            os.write(ris, (int) count);
        }

        public void writeHeaders(HttpResponse response, long count)
        {
            response.setField(HttpFields.__ContentType,encoding);
            if (length != -1) 
                response.setIntField(HttpFields.__ContentLength, (int) count);
            response.setDateField(HttpFields.__LastModified,resource.lastModified());
            // response.setField(HttpFields.__AcceptRanges,"bytes");
        }

        public void requestDone()
        {
            try
            {
                if (ris != null)
                    ris.close();
            }
            catch (IOException ioe){Code.ignore(ioe);}
        }

    }    

    /* ------------------------------------------------------------ */
    /** Holds a cached file.
     * It is assumed that threads accessing CachedFile have
     * the parents cacheMap locked. 
     */
    private class CachedFile implements SendableResource
    {
        Resource resource;
        long lastModified;
        byte[] bytes;
        String encoding;

        CachedFile prev;
        CachedFile next;

        /* ------------------------------------------------------------ */
        CachedFile(Resource resource)
            throws IOException
        {
            synchronized(_cacheMap)
            {
                load(resource);
                String r=resource.toString();
                Object old=_cacheMap.get(r);                
                if (old!=null)
                    ((CachedFile)old).invalidate();
                _cacheMap.put(r,this);
                
                next=_mostRecentlyUsed;
                _mostRecentlyUsed=this;
                if (next!=null)
                    next.prev=this;
                else
                    _leastRecentlyUsed=this;

                if (_cacheMap.size()>_maxCachedFiles)
                    _leastRecentlyUsed.invalidate();
            }
        }
        
        
        /* ------------------------------------------------------------ */
        public String getEncoding()
        {
            return encoding;
        }


        /* ------------------------------------------------------------ */
        public void writeBytes(ChunkableOutputStream os, long startByte, long count) 
            throws IOException
        {
             os.write(bytes, (int) startByte, (int) count);
        }


        /* ------------------------------------------------------------ */
        boolean isValid()
            throws IOException
        {
            if (resource==null || !resource.exists() ||
                lastModified!=resource.lastModified())
            {
                // The cached file is no longer valid
                invalidate();
                return false;
            }
            else
            {
                // make it the most recently used
                use();
                return true;
            }
        }

        /* ------------------------------------------------------------ */
        public void invalidate()
        {
            synchronized(_cacheMap)
            {
                lastModified--;
                _cacheMap.remove(resource.toString());

                if (prev==null)
                    _mostRecentlyUsed=next;
                else
                    prev.next=next;
                
                if (next==null)
                    _leastRecentlyUsed=prev;
                else
                    next.prev=prev;

                prev=null;
                next=null;
            }
        }
        
        /* ------------------------------------------------------------ */
        public void use()
        {
            synchronized(_cacheMap)
            {
                if (_mostRecentlyUsed!=this)
                {
                    CachedFile tp = prev;
                    CachedFile tn = next;
                    
                    next=_mostRecentlyUsed;
                    _mostRecentlyUsed=this;
                    if (next!=null)
                        next.prev=this;
                    prev=null;
                    
                    // delete it from where it was
                    if (tp!=null)
                        tp.next=tn;
                    if (tn!=null)
                        tn.prev=tp;
                    
                    if (_leastRecentlyUsed==this && tp!=null)
                        _leastRecentlyUsed=tp;
                }
                
            }
        }
  
        /* ------------------------------------------------------------ */
        public void writeHeaders(HttpResponse response, long count)
            throws IOException
        {
            Code.debug("HIT: ",resource);
            response.setField(HttpFields.__ContentType,encoding);
            if (count != -1)
                 response.setIntField(HttpFields.__ContentLength, (int) count);
            response.setDateField(HttpFields.__LastModified,lastModified);
            response.setField(HttpFields.__AcceptRanges,"bytes");
        }

        /* ------------------------------------------------------------ */
        void load(Resource resource)
            throws IOException
        {
            this.resource=resource;
            lastModified=resource.lastModified();
            bytes = new byte[(int)resource.length()];
            Code.debug("LOAD: ",resource);
     
            InputStream in=resource.getInputStream();
            int read=0;
            while (read<bytes.length)
            {
                int len=in.read(bytes,read,bytes.length-read);
                if (len==-1)
                    throw new IOException("Unexpected EOF: "+resource);
                read+=len;
            }
            in.close();
            encoding=getHandlerContext().getMimeByExtension(resource.getName());
        }

        /* ------------------------------------------------------------ */
        public void requestDone()
        {
        }

        /* ------------------------------------------------------------ */
        public long getLength()
        {
            return bytes.length;
        }

        /* ------------------------------------------------------------ */
        public String toString()
        {
            return resource.toString();
        }
    }
}




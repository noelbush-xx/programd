// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: HttpFields.java,v 1.1.1.1 2001/06/17 19:00:43 noelbu Exp $
// ---------------------------------------------------------------------------

package org.alicebot.server.net.http;

import org.alicebot.server.net.http.util.Code;
import org.alicebot.server.net.http.util.DateCache;
import org.alicebot.server.net.http.util.LineInput;
import org.alicebot.server.net.http.util.QuotedStringTokenizer;
import org.alicebot.server.net.http.util.StringUtil;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;


/* ------------------------------------------------------------ */
/** HTTP Fields.
 * A collection of HTTP header and or Trailer fields.
 * This class is not synchronized and needs to be protected from
 * concurrent access.
 *
 * This class is not synchronized as it is expected that modifications
 * will only be performed by a single thread.
 *
 * @version $Id: HttpFields.java,v 1.1.1.1 2001/06/17 19:00:43 noelbu Exp $
 * @author Greg Wilkins (gregw)
 */
public class HttpFields
{
    /* ------------------------------------------------------------ */
    /** General Fields.
     */
    public final static String
        __CacheControl = "Cache-Control",
        __Connection = "Connection",
        __Date = "Date",
        __Pragma = "Pragma",
        __Trailer = "Trailer",
        __TransferEncoding = "Transfer-Encoding",
        __Upgrade = "Upgrade",
        __Via = "Via",
        __Warning = "Warning";
        
    /* ------------------------------------------------------------ */
    /** Entity Fields.
     */
    public final static String
        __Allow = "Allow",
        __ContentEncoding = "Content-Encoding",
        __ContentLanguage = "Content-Language",
        __ContentLength = "Content-Length",
        __ContentLocation = "Content-Location",
        __ContentMD5 = "Content-MD5",
        __ContentRange = "Content-Range",
        __ContentType = "Content-Type",
        __Expires = "Expires",
        __LastModified = "Last-Modified";
    
    /* ------------------------------------------------------------ */
    /** Request Fields.
     */
    public final static String
        __Accept = "Accept",
        __AcceptCharset = "Accept-Charset",
        __AcceptEncoding = "Accept-Encoding",
        __AcceptLanguage = "Accept-Language",
        __Authorization = "Authorization",
        __Expect = "Expect",
        __From = "From",
        __Host = "Host",
        __IfMatch = "If-Match",
        __IfModifiedSince = "If-Modified-Since",
        __IfNoneMatch = "If-None-Match",
        __IfRange = "If-Range",
        __IfUnmodifiedSince = "If-Unmodified-Since",
        __MaxForwards = "Max-Forwards",
        __ProxyAuthentication = "Proxy-Authentication",
        __Range = "Range",
        __Referer = "Referer",
        __TE = "TE",
        __UserAgent = "User-Agent";

    /* ------------------------------------------------------------ */
    /** Response Fields.
     */
    public final static String
        __AcceptRanges = "Accept-Ranges",
        __Age = "Age",
        __ETag = "ETag",
        __Location = "Location",
        __ProxyAuthenticate = "Proxy-Authenticate",
        __RetryAfter = "Retry-After",
        __Server = "Server",
        __ServletEngine = "Servlet-Engine",
        __Vary = "Vary",
        __WwwAuthenticate = "WWW-Authenticate";
     
    /* ------------------------------------------------------------ */
    /** Other Fields.
     */
    public final static String __Cookie = "Cookie";
    public final static String __SetCookie = "Set-Cookie";
    public final static String __SetCookie2 = "Set-Cookie2";
    public final static String __MimeVersion ="MIME-Version";
    public final static String __Identity ="identity";
    
    /* ------------------------------------------------------------ */
    /** Fields Values.
     */    
    public final static String __Chunked = "chunked";
    public final static String __Close = "close";
    public final static String __TextHtml = "text/html";
    public final static String __MessageHttp = "message/http";
    public final static String __KeepAlive = "keep-alive";
    public final static String __WwwFormUrlEncode =
        "application/x-www-form-urlencoded";
    public static final String __ExpectContinue="100-continue";
    
    
    /* ------------------------------------------------------------ */
    /** Single valued Fields.
     */  
    public final static String[] __SingleValued=
    {
        __Age,__Authorization,__ContentLength,__ContentLocation,__ContentMD5,
        __ContentRange,__ContentType,__Date,__ETag,__Expires,__From,__Host,
        __IfModifiedSince,__IfRange,__IfUnmodifiedSince,__LastModified,
        __Location,__MaxForwards,__ProxyAuthentication,__Range,__Referer,
        __RetryAfter,__Server,__UserAgent
    };
    public final static Set __singleValuedSet=new HashSet(37);
    static
    {
        for (int i=0;i<__SingleValued.length;i++)
            __singleValuedSet
                .add(StringUtil.asciiToLowerCase(__SingleValued[i]));
    }
    
    /* ------------------------------------------------------------ */
    /** Inline Fields.
     */  
    public final static String[] __inlineValues=
    {
        __TransferEncoding,__ContentEncoding,
    };
    public final static Set __inlineValuedSet=new HashSet(5);
    static
    {
        for (int i=0;i<__inlineValues.length;i++)
            __inlineValuedSet
                .add(StringUtil.asciiToLowerCase(__inlineValues[i]));
    }
    
    /* ------------------------------------------------------------ */
    public final static String __CRLF = "\015\012";
    public final static String __COLON = ": ";

    /* -------------------------------------------------------------- */
    public final static DateCache __dateCache = 
        new DateCache("EEE, dd MMM yyyy HH:mm:ss 'GMT'",
                      Locale.US);
    public final static SimpleDateFormat __dateSend = 
        new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'",
                             Locale.US);
    private final static String __dateReceiveFmt[] =
    {
        "EEE, dd MMM yyyy HH:mm:ss zzz",
        "EEE, dd MMM yyyy HH:mm:ss",
        "EEE dd MMM yyyy HH:mm:ss zzz",
        "EEE dd MMM yyyy HH:mm:ss",
        "EEE MMM dd yyyy HH:mm:ss zzz",
        "EEE MMM dd yyyy HH:mm:ss",
        "EEE MMM-dd-yyyy HH:mm:ss zzz",
        "EEE MMM-dd-yyyy HH:mm:ss",
        "dd MMM yyyy HH:mm:ss zzz",
        "dd MMM yyyy HH:mm:ss",
        "dd-MMM-yy HH:mm:ss zzz",
        "dd-MMM-yy HH:mm:ss",
        "MMM dd HH:mm:ss yyyy zzz",
        "MMM dd HH:mm:ss yyyy",
        "EEE MMM dd HH:mm:ss yyyy zzz",
        "EEE MMM dd HH:mm:ss yyyy",
        "EEE, MMM dd HH:mm:ss yyyy zzz",
        "EEE, MMM dd HH:mm:ss yyyy",
        "EEE, dd-MMM-yy HH:mm:ss zzz",
        "EEE, dd-MMM-yy HH:mm:ss",
        "EEE dd-MMM-yy HH:mm:ss zzz",
        "EEE dd-MMM-yy HH:mm:ss",
    };
    public static SimpleDateFormat __dateReceive[];
    static
    {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        tz.setID("GMT");
        __dateSend.setTimeZone(tz);
        __dateCache.setTimeZone(tz);
        
        __dateReceive = new SimpleDateFormat[__dateReceiveFmt.length];
        for(int i=0;i<__dateReceive.length;i++)
        {
            __dateReceive[i] =
                new SimpleDateFormat(__dateReceiveFmt[i],Locale.US);
            __dateReceive[i].setTimeZone(tz);
        }
    }
    
    /* -------------------------------------------------------------- */
    private HashMap _map = new HashMap(23);
    private ArrayList _names= new ArrayList(15);
    private List _readOnlyNames=null;

    /* ------------------------------------------------------------ */
    /** Constructor. 
     */
    public HttpFields()
    {}

    /* ------------------------------------------------------------ */
    /** 
     * @return 
     */
    public int size()
    {
        return _map.size();
    }
    
    /* -------------------------------------------------------------- */
    /** Get enumeration of header _names.
     * Returns an enumeration of strings representing the header _names
     * for this request. 
     */
    public List getFieldNames()
    {
        if (_readOnlyNames==null)
            _readOnlyNames=Collections.unmodifiableList(_names);
        return _readOnlyNames;
    }
    
    /* ------------------------------------------------------------ */
    /** 
     * @param name 
     * @return 
     */
    public boolean containsKey(Object name)
    {
        return _map.containsKey(StringUtil.asciiToLowerCase(name.toString()));
    }
    
    /* -------------------------------------------------------------- */
    /**
     * @return the value of a  field, or null if not found.
     * The case of the field name is ignored.
     * @param name the case-insensitive field name
     */
    public String get(String name)
    {
        Object o=_map.get(StringUtil.asciiToLowerCase(name));
        if (o==null)
            return null;
        if (o instanceof List)
            return listToString((List)o);
        return o.toString();
    }
    
    /* -------------------------------------------------------------- */
    /**
     * @return multiple values of a field, or null if not found.
     * Non quoted multiple spaces are replaced with a single space
     * @param name the case-insensitive field name
     */
    public List getValues(String name)
    {
        Object o=_map.get(StringUtil.asciiToLowerCase(name));
        if (o==null)
            return null;
        if (o instanceof List)
            return (List)o;

        List list = new ArrayList();

        QuotedStringTokenizer tok =
            new QuotedStringTokenizer(o.toString(),", \t",true,false);
        String value=null;
        boolean space=false;
        while (tok.hasMoreTokens())
        {
            String token=tok.nextToken();
            if (",".equals(token))
            {
                if (value!=null)
                    list.add(value);
                value=null;
            }
            else if (" ".equals(token) || "\t".equals(token))
            {
                space=(value!=null);
            }
            else if (value==null)
            {
                value=token;
                space=false;
            }
            else if (space)
            {
                value+=" "+token;
                space=false;
            }
            else
                value+=token;
        }
        if(value!=null)
            list.add(value);
            
        return list;
    }
    
        
    /* -------------------------------------------------------------- */
    /** Set a field.
     * @param name the name of the field
     * @param value the value of the field. If null the field is cleared.
     */
    public String put(String name,String value)
    {
        if (value==null)
            return remove(name);
        
        String lname = StringUtil.asciiToLowerCase(name);
        Object old=_map.put(lname,value);
        if (old==null)
        {
            _names.add(name);
            return null;
        }
        return old.toString();
    }

    /* -------------------------------------------------------------- */
    /** Set a multi value field.
     * If the field is allowed to have multiple values.
     * @param name the name of the field
     * @param value the list of values of the field. If null the field is cleared.
     */
    public void put(String name,List value)
    {
        if (value==null)
        {
            remove(name);
            return;
        }
        
        String lname = StringUtil.asciiToLowerCase(name);
        if (!_map.containsKey(lname))
            _names.add(name);
    }
    
    /* -------------------------------------------------------------- */
    /** Add to or set a field.
     * If the field is allowed to have multiple values, add will build
     * a coma separated list for the value.
     * The values are quoted if they contain comas or quote characters.
     * @param name the name of the field
     * @param value the value of the field.
     * @exception IllegalArgumentException If the name is a single
     *            valued field
     */
    public void add(String name,String value)
        throws IllegalArgumentException
    {
        if (value==null)
            return;
        String lname = StringUtil.asciiToLowerCase(name);
        Object existing=_map.get(lname);
        if (existing == null)
        {
            _map.put(lname,value);
            _names.add(name);
        }
        else
        {
            if (__singleValuedSet.contains(lname))
                throw new IllegalArgumentException("Cannot add single valued field: "+name);

            List list;
            if (existing instanceof List)
                list=(List)existing;
            else
            {
                list=new ArrayList(4);
                list.add(QuotedStringTokenizer.unquote(existing.toString()));
                _map.put(lname,list);
            }
            list.add(QuotedStringTokenizer.unquote(value));
        }        
    }
    
    /* ------------------------------------------------------------ */
    /** Remove a field.
     * @param name 
     */
    public String remove(String name)
    {
        String lname = StringUtil.asciiToLowerCase(name);
        _names.remove(name);
        _names.remove(lname);
        Object old=_map.remove(lname);
        if (old==null)
            return null;
        if (old instanceof List)
            return listToString((List)old);
        return old.toString();
    }
   
    /* -------------------------------------------------------------- */
    /** Get a header as an integer value.
     * Returns the value of an integer field or -1 if not found.
     * The case of the field name is ignored.
     * @param name the case-insensitive field name
     * @exception NumberFormatException If bad integer found
     */
    public int getIntField(String name)
        throws NumberFormatException
    {
        String val = valueParameters(get(name),null);
        if (val!=null)
            return Integer.parseInt(val);
        return -1;
    }
    
    /* -------------------------------------------------------------- */
    /** Get a header as a date value.
     * Returns the value of a date field, or -1 if not found.
     * The case of the field name is ignored.
     * @param name the case-insensitive field name
     */
    public long getDateField(String name)
    {
        String val = valueParameters(get(name),null);
        if (val!=null)
        {
            for (int i=0;i<__dateReceive.length;i++)
            {
                try{
                    Date date=(Date)__dateReceive[i].parseObject(val);
                    return date.getTime();
                }
                catch(java.lang.Exception e)
                {Code.ignore(e);}
            }
            if (val.endsWith(" GMT"))
            {
                val=val.substring(0,val.length()-4);
                for (int i=0;i<__dateReceive.length;i++)
                {
                    try{
                        Code.debug("TRY ",val," against ",__dateReceive[i].toPattern());
                        Date date=(Date)__dateReceive[i].parseObject(val);
                        Code.debug("GOT ",date);
                        return date.getTime();
                    }
                    catch(java.lang.Exception e)
                    {
                        Code.ignore(e);
                    }
                }
            }
        }
        return -1;
    }
    
    /* -------------------------------------------------------------- */
    /**
     * Sets the value of an integer field.
     * @param name the field name
     * @param value the field integer value
     */
    public void putIntField(String name, int value)
    {
        put(name, Integer.toString(value));
    }

    /* -------------------------------------------------------------- */
    /**
     * Sets the value of a date field.
     * @param name the field name
     * @param value the field date value
     */
    public void putDateField(String name, Date date)
    {
        put(name, __dateSend.format(date));
    }
    
    /* -------------------------------------------------------------- */
    /**
     * Adds the value of a date field.
     * @param name the field name
     * @param value the field date value
     */
    public void addDateField(String name, Date date)
    {
        add(name, __dateSend.format(date));
    }
    
    /* -------------------------------------------------------------- */
    /**
     * Sets the value of a date field.
     * @param name the field name
     * @param value the field date value
     */
    public void putDateField(String name, long date)
    {
        put(name, __dateSend.format(new Date(date)));
    }
    
    /* -------------------------------------------------------------- */
    /** Set date field to the current time.
     * Sets the value of a date field to the current time.  Uses
     * efficient DateCache mechanism.
     * @param name the field name
     */
    public void putCurrentTime(String name)
    {
        put(name, __dateCache.format(System.currentTimeMillis()));
    }

    /* -------------------------------------------------------------- */
    /** Read HttpHeaders from inputStream.
     */
    public void read(LineInput in)
        throws IOException
    {   
        String last=null;
        char[] buf=null;
        int size=0;
        char[] lbuf=null;
        org.alicebot.server.net.http.util.LineInput.LineBuffer line_buffer;
        synchronized(in)
        {
            while ((line_buffer=in.readLineBuffer())!=null)
            {
                // check space in the lowercase buffer
                buf=line_buffer.buffer;
                size=line_buffer.size;
                if (size==0)
                    break;
                if (lbuf==null || lbuf.length<line_buffer.size)
                    lbuf= new char[buf.length];
                
                // setup loop state machine
                int state=0;
                int i1=-1;
                int i2=-1;
                String name=null;
                String lname=null;
                
                // loop for all chars in buffer
                for (int i=0;i<line_buffer.size;i++)
                {
                    char c=buf[i];
                    
                    switch(state)
                    {
                      case 0: // leading white
                          if (c==' ' || c=='\t')
                          {
                              // continuation line
                              state=2;
                              continue;
                          }
                          state=1;
                          i1=i;
                          i2=i-1;
                      case 1: // reading name
                          if (c==':')
                          {
                              name=new String(buf,i1,i2-i1+1);
                              lname=new String(lbuf,i1,i2-i1+1);  
                              state=2;
                              i1=i;i2=i-1;
                              continue;
                          }
                          if (c>='A'&&c<='Z')
                          {
                              lbuf[i]=(char)(('a'-'A')+c);
                              i2=i;
                          }
                          else
                          {
                              lbuf[i]=c;
                              if (c!=' ' && c!='\t')
                                  i2=i;
                          }
                          continue;
                          
                      case 2: // skip whitespace after :
                          if (c==' ' || c=='\t')
                              continue;
                          state=3;
                          i1=i;
                          i2=i-1;
                          
                      case 3: // looking for last non-white
                          if (c!=' ' && c!='\t')
                              i2=i;
                    }
                    continue;
                }
                
                if (lname==null || lname.length()==0)
                {
                    if (state>=2 && last!=null)
                    {
                        // Continuation line
                        String existing=(String)get(last);
                        StringBuffer sb = new StringBuffer(existing);
                        sb.append(' ');
                        sb.append(new String(buf,i1,i2-i1+1));
                        put(last,sb.toString());
                    }
                    continue;
                }
                
                // Handle repeated headers
                if (_map.containsKey(lname))
                {
                    if (__singleValuedSet.contains(lname))
                    {
                        Code.warning("Ignored duplicate single value header: "+
                                     name);
                    }
                    else
                        add(lname,new String(buf,i1,i2-i1+1));
                }
                else
                {
                    _map.put(lname,new String(buf,i1,i2-i1+1));
                    _names.add(name);
                    last=lname;
                }
            }
        }
    }

    
    /* -------------------------------------------------------------- */
    /* Write Extra HTTP headers.
     */
    public void write(Writer writer)
        throws IOException
    {
        synchronized(writer)
        {
            int size=_names.size();
            for(int k=0;k<size;k++)
            {
                String name = (String)_names.get(k);
                String lname = StringUtil.asciiToLowerCase(name);
                Object o=_map.get(lname);
                if (o==null)
                    continue;
                if (o instanceof List)
                {
                    if ( __inlineValuedSet.contains(lname))
                    {
                        writer.write(name);
                        writer.write(__COLON);
                        writer.write(listToString((List)o));
                        writer.write(__CRLF);
                    }
                    else
                    {
                        List values = (List)o;
                        for (int i=0;i<values.size();i++)
                        {
                            String value = (String)values.get(i);
                            writer.write(name);
                            writer.write(__COLON);
                            writer.write(value);
                            writer.write(__CRLF);
                        }
                    }
                }
                else
                {
                    writer.write(name);
                    writer.write(__COLON);
                    writer.write(o.toString());
                    writer.write(__CRLF);
                }
            }
            writer.write(__CRLF);
        }
    }
    
    /* -------------------------------------------------------------- */
    public String toString()
    {
        try
        {
            StringWriter writer = new StringWriter();
            write(writer);
            return writer.toString();
        }
        catch(Exception e)
        {}
        return null;
    }

    /* ------------------------------------------------------------ */
    /** Destroy the header.
     * Help the garbage collector by null everything that we can.
     */
    public void clear()
    {
        _map.clear();
        _names.clear();
        _readOnlyNames=null;
    }
    
    /* ------------------------------------------------------------ */
    /** Destroy the header.
     * Help the garbage collector by null everything that we can.
     */
    public void destroy()
    {
        _map.clear();
        if (_names!=null)
            _names.clear();
        _names=null;
        _readOnlyNames=null;
    }

    
    /* ------------------------------------------------------------ */
    /** Convert list of strings to coma separated quoted string.
     * @param list List of strings
     * @return 
     */
    public static String listToString(List list)
    {
        StringBuffer buf = new StringBuffer();
        synchronized(buf)
        {
            for (int i=0;i<list.size();i++)
            {
                if (i>0)
                    buf.append(",");
                buf.append(QuotedStringTokenizer.quote((String)list.get(i),
                                                       ", \t"));
            }
            return buf.toString();
        }
    }
    
    /* ------------------------------------------------------------ */
    /** Get field value parameters.
     * Some field values can have parameters.  This method separates
     * the value from the parameters and optionally populates a
     * map with the paramters. For example:<PRE>
     *   FieldName : Value ; param1=val1 ; param2=val2
     * </PRE>
     * @param value The Field value, possibly with parameteres.
     * @param parameters A map to populate with the parameters, or null
     * @return The value.
     */
    public static String valueParameters(String value, Map parameters)
    {
        if (value==null)
            return null;
        
        int i = value.indexOf(";");
        if (i<0)
            return value;
        if (parameters==null)
            return value.substring(0,i).trim();

        StringTokenizer tok1 =
            new QuotedStringTokenizer(value.substring(i),";",false,true);
        while(tok1.hasMoreTokens())
        {
            String token=tok1.nextToken();
            StringTokenizer tok2 =
                new QuotedStringTokenizer(token,"= ");
            if (tok2.hasMoreTokens())
            {
                String paramName=tok2.nextToken();
                String paramVal=null;
                if (tok2.hasMoreTokens())
                    paramVal=tok2.nextToken();
                parameters.put(paramName,paramVal);
            }
        }
        
        return value.substring(0,i).trim();
    }


    /* ------------------------------------------------------------ */
    /** List values in quality order.
     * @param value List of values with quality parameters
     * @return values in quality order.
     */
    public static List qualityList(List values)
    {
        values = new ArrayList(values);
        QualityComparator compare = new QualityComparator();
        Collections.sort(values, compare);
        
        Iterator iter = values.iterator();
        while(iter.hasNext())
        {
            Object o=iter.next();
            Float f=(Float)compare.getQuality(o);
            if (f.floatValue()<0.001)
                iter.remove();
        }
        return values;
    }

    /* ------------------------------------------------------------ */
    /** Compare quality values.
     * This comparitor caches quality values extracted from the
     * valueParameters() method in a HashSet.
     * @see Httpfields.qualityList(List values)
     */
    private static class QualityComparator
        extends HashMap 
        implements Comparator
    {
        private static Float __one = new Float("1.0");
        private HashMap _params = new HashMap(7);
        
        /* ------------------------------------------------------------ */
        QualityComparator()
        {
            super(7);
        }

        /* ------------------------------------------------------------ */
        public synchronized int compare(Object o1, Object o2)
        {
            Float q1 = getQuality(o1);
            Float q2 = getQuality(o2);
            float f = q1.floatValue()-q2.floatValue();
            if (f<=-0.0001)
                return 1;
            if (f>=0.0001)
                return -1;
            return 0;
        }

        /* ------------------------------------------------------------ */
        Float getQuality(Object o)
        {
            Float q = (Float)this.get(o);
            if (q==null)
            {
                _params.clear();
                valueParameters(o.toString(),_params);
                String qs=(String)_params.get("q");
                if (qs==null)
                    q=__one;
                else
                {
                    try{q=new Float(qs);}
                    catch(Exception e){q=__one;}
                }
                QualityComparator.this.put(o,q);
            }
            return q;
        }
        

        /* ------------------------------------------------------------ */
        public boolean equals(Object o)
        {
            return false;
        }
    }
}


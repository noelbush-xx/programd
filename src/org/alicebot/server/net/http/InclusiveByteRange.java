package org.alicebot.server.net.http;

import org.alicebot.server.net.http.util.Code;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;

/* ------------------------------------------------------------ */
/** class for dealing with byte ranges
 * <PRE>
 * 
 *   parses the following types of byte ranges:
 * 
 *       bytes=100-499
 *       bytes=-300
 *       bytes=100-
 *       bytes=1-2,2-3,6-,-2
 *
 *   given an entity length, converts range to string
 * 
 *       bytes 100-499/500
 * 
 * </PRE>
 * 
 * @see RFC2616 3.12, 14.16, 14.35.1, 14.35.2
 * @version $version$
 * @author Helmut Hissen
 */


public class InclusiveByteRange {

     long first = 0;
     long last  = 0;

     public InclusiveByteRange(long first, long last) {
            this.first = first;
            this.last = last;
     }

     long getFirst() {
            return first;
     }

     long getLast() {
            return last;
     }


     public static List parseRangeHeaders(List reqRangeHeaders) {

        ListIterator rit = reqRangeHeaders.listIterator();
        List validRanges = new ArrayList();

        // walk through all Range headers 

        while (rit.hasNext()) {
             StringTokenizer tok = new StringTokenizer((String) rit.next(), " =,-", true);
             if ( !tok.hasMoreTokens() ) continue;
             String t = tok.nextToken();

             List ranges = new ArrayList();

             // walk through multiple ranges for each header

             while ( ranges != null && t != null ) {

                 // find initial required "bytes=".  we ignored all other types of ranges

                 while ( t != null ) {

                    if ( !t.equals("bytes")) {
                       if (tok.hasMoreTokens()) t = tok.nextToken(); else t = null;
                       continue;
                    }

                    if (tok.hasMoreTokens()) t = tok.nextToken(); else t = null;

                    if ( !t.equals("=")) {
                       if (tok.hasMoreTokens()) t = tok.nextToken(); else t = null;
                       continue;
                    }

                    if (tok.hasMoreTokens()) t = tok.nextToken(); else t = null;
                    break;
                 }
                 

                 // read all byte ranges for this header

                 while ( (t != null) && (t.length() > 0) && (Character.isDigit(t.charAt(0)) || t.equals("-"))) {

                     long first = -1;
                     long last  = -1;

                     //
                     // beginning of range is optional
                     //

                     if (!t.equals("-")) {
                        first = Long.parseLong(t);
                        if (tok.hasMoreTokens()) t = tok.nextToken(); else t = null;
                     }


                     //
                     // "-" is required
                     //

                     if (!t.equals("-")) {
                        break;
                     }
                     if (tok.hasMoreTokens()) t = tok.nextToken(); else t = null;


                     //
                     // end of range is optional
                     //

                     if (t != null && Character.isDigit(t.charAt(0)) ) {
                        last = Long.parseLong(t);
                        if (tok.hasMoreTokens()) t = tok.nextToken(); else t = null;
                     }
                    

                     //
                     // only accept valid ranges.  if any of the ranges is syntactically
                     // incorrect, ditch all ranges in that same header
                     //

                     if (first == -1 && last == -1) {
                        ranges = null;
                        break;
                     }

                     if (first != -1 && last != -1 && (first > last)) {
                        ranges = null;
                        break;
                     }

                     if (t != null && !t.equals(",")) {
                        ranges = null;
                        break;
                     }
                     if (tok.hasMoreTokens()) t = tok.nextToken(); else t = null;

                     ranges.add(new InclusiveByteRange(first, last));



                     //
                     // the rfc seems to imply the silly possibility to specify 
                     // something like
                     //     Range: bytes=1,2,bytes=3,4
                     // so, we need to do some extra gymnastics to allow for that
                     //

                     if (t != null && t.equals("bytes")) {
                        if (tok.hasMoreTokens()) t = tok.nextToken(); else t = null;
                        if (t != null && t.equals("=")) {
                           if (tok.hasMoreTokens()) t = tok.nextToken(); else t = null;
                        }
                     }
                 }
             }

             if (ranges != null)
                 validRanges.addAll(ranges);
        }

        return validRanges;
    }


    public long getFirst(long size) {
        if (first != -1)
            return first;
        if ((size - last) < 0)
            return 0;
        return (size - last);
    }


    public long getLast(long size) {
        if (last == -1 || first == -1)
            return size - 1;
        if (last >= size)
            return size - 1;
        return last;
    }


    public long getSize(long size) {
        return (getLast(size) - getFirst(size)) + 1;
    }


    static String formatHeaderRangeString(String rng, String sze) {
        StringBuffer sb = new StringBuffer(40);
        sb.append("bytes ");
        sb.append(rng);
        sb.append("/");
        sb.append(sze);
        return sb.toString();
    }


    public String toHeaderRangeString(long size) {
        return formatHeaderRangeString(
                   Long.toString(getFirst(size)) +"-"+
                   Long.toString(getLast(size)),
                   Long.toString(size)
        );
    }

    public static String to416HeaderRangeString(long size) {
        return formatHeaderRangeString(
                   "*",
                   Long.toString(size)
        );
    }


    /* ------------------------------------------------------------ */

    public String toString() {
        StringBuffer sb = new StringBuffer(60);
        sb.append("[");
        sb.append(Long.toString(first));
        sb.append(";");
        sb.append(Long.toString(last));
        sb.append("]");
        return new String(sb);
    }


    public static void main(String [] args) {

        ArrayList al = new ArrayList(args.length);
        for (int i = 0; i < args.length; i++) {
             al.add(args[i]);
        }
        List parsed = parseRangeHeaders(al);
        System.out.println(parsed);
        ListIterator ali = parsed.listIterator();
        while (ali.hasNext()) {
            InclusiveByteRange ibr = (InclusiveByteRange) ali.next();
            System.out.println(ibr.toHeaderRangeString(1000));
        }
    }

}




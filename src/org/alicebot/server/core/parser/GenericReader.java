/*
    Alicebot Program D
    Copyright (C) 1995-2001, A.L.I.C.E. AI Foundation
    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, 
    USA.
*/


package org.alicebot.server.core.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;

import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.util.DeveloperError;
import org.alicebot.server.core.util.Trace;
import org.alicebot.server.core.util.Toolkit;
import org.alicebot.server.core.util.UserError;


/**
 *  Provides generic reading functionality for such classes
 *  as {@link AIMLReader} and {@link org.alicebot.server.core.util.TargetsReader}.
 *
 *  @author Noel Bush
 */
abstract public class GenericReader
{
    /*
        Constants used in parsing.
    */

    /** The start of a tag marker. */
    protected static final String MARKER_START        = "<";

    /** The end of a tag marker. */
    protected static final String MARKER_END          = ">";

    /** '<code>!--</code>' */
    protected static final String COMMENT_MARK        = "!--";

    /** An empty string. */
    protected static final String EMPTY_STRING        = "";

    /** A slash. */
    protected static final String SLASH               = "/";

    /** A quote mark. */
    protected static final String QUOTE_MARK          = "\"";

    /** An asterisk. */
    protected static final String ASTERISK            = "*";

    /** A colon. */
    protected static final String COLON               = ":";

    /** A space. */
    protected static final String SPACE               = " ";

    /** The system line separator. */
    protected static final String LINE_SEPARATOR      = System.getProperty("line.separator");

    /** An estimate of the maximum buffer length needed (helps tune performance). */
    protected static int bufferStartCapacity          = 100;


    /*
        Instance variables.
    */

    /** An instance of GenericReader should set this field to itself. */
    protected GenericReader          readerInstance;

    /** The <code>Listener</code> that will handle new items. */
    protected GenericReaderListener  listener;

    /** Access to the file. */
    protected BufferedReader         buffReader;

    /** The name of the file. */
    protected String                 fileName;

    /** The flag that indicates whether parsing is done. */
    protected boolean                done            = false;

    /** The flag that indicates whether the read method is searching for a tag marker start. */
    protected boolean                searching       = false;

    /** Parser state. */
    protected int                    state;

    /** The most recent start of a tag. */
    protected int                    tagStart        = 0;

    /** The most recent length of a tag being checked. */
    protected int                    tagLength       = 0;

    /** The location in bufferString to...... */
    protected int                    searchStart     = 0;

    /** The current line number. */
    protected int                    lineNumber      = 0;

    /** The parse buffer. */
    protected StringBuffer           buffer          = new StringBuffer(bufferStartCapacity);

    /** The parse buffer as a String. */
    protected String                 bufferString    = null;

    /** A custom Throwable thrown by the various <code>transition</code> methods if they succeed. */
    protected TransitionMade TRANSITION_MADE;

    /** Indicates whether or not to count bytes. */
    private boolean countBytes;

    /** Used to count bytes read. */
    protected long                   byteCount;

    /** Used to calculate bytes read. */
    protected String                 encoding;


    /**
     *  Constructs a new <code>GenericReader</code>, given a
     *  {@link java.io.BufferedReader BufferedReader} handle to some input stream
     *  (<code>buffReader</code>), a filename to use in printing error messages
     *  (<code>fileName</code>), and a {@link TargetsReaderListener} that will
     *  handle creation of new categories as they are discovered.
     *
     *  @see Targets
     *  
     *  @param fileName         name of the targets data file to be read
     *  @param buffReader       a BufferedReader already open to the file (could be remote)
     *  @param encoding         the encoding with which the file is being read
     *  @param countBytes       whether or not to count bytes read (slows down the process)
     *  @param listener         will handle new items
     */
    public GenericReader(String fileName, BufferedReader buffReader, String encoding,
                         boolean countBytes, GenericReaderListener listener)
    {
        this.fileName        = fileName;
        this.buffReader      = buffReader;
        this.encoding        = encoding;
        this.countBytes      = countBytes;
        this.listener        = listener;
        this.TRANSITION_MADE = new TransitionMade();

        // Do any initialization.
        initialize();
    }


    /**
     *  Constructs a new <code>GenericReader</code>, given a
     *  {@link java.io.BufferedReader BufferedReader} handle to some input stream
     *  (<code>buffReader</code>), a filename to use in printing error messages
     *  (<code>fileName</code>), and a {@link TargetsReaderListener} that will
     *  handle creation of new categories as they are discovered.
     *  In this version, byte counting is disabled.
     *
     *  @see Targets
     *  
     *  @param fileName         name of the targets data file to be read
     *  @param buffReader       a BufferedReader already open to the file (could be remote)
     *  @param listener         will handle new items
     */
    public GenericReader(String fileName, BufferedReader buffReader, GenericReaderListener listener)
    {
        this.fileName        = fileName;
        this.buffReader      = buffReader;
        this.countBytes      = false;
        this.listener        = listener;
        this.TRANSITION_MADE = new TransitionMade();

        // Do any initialization.
        initialize();
    }


    abstract protected void initialize();


    /**
     *  Reads a targets data file and looks for categories. The expected format is:
     */
    public void read()
    {
        StringBuffer line = null;

        // Parse loop.  Anything that sets done to false will cause parsing to stop.
        parsing: while(!done)
        {
            // Searching = true means we are looking for a tag marker.
            searching = true;

            /*
                Searching for tag marker loop.  Setting searching to false
                will trigger a parse attempt.
            */
            searching: while(searching)
            {
                // Convert the buffer to a String for matching purposes.
                bufferString = buffer.toString();

                // Find the next marker start.
                tagStart = bufferString.indexOf(MARKER_START, searchStart);

                // If no tag is found, read another line.
                if(tagStart < 0)
                {
                    // Try to read another line.
                    try
                    {
                        // If buffReader.readLine() is null, an exception will be thrown.
                        line = new StringBuffer(buffReader.readLine());

                        // Update the byteCount.
                        if (countBytes)
                        {
                            try
                            {
                                byteCount += line.toString().getBytes(encoding).length;
                            }
                            catch (UnsupportedEncodingException e)
                            {
                                throw new UserError("Encoding \"" + encoding + "\" is not supported by your platform!");
                            }
                        }

                        // Increment the line number.
                        lineNumber++;

                        // Append line (with line separator), search again.
                        buffer.append(line.toString() + LINE_SEPARATOR);
                    }
                    // An I/O exception means we've got to abort this file.
                    catch(IOException e)
                    {
                        Trace.userinfo(QUOTE_MARK + fileName + "\" could not be read.");
                        return;
                    }
                    // A null pointer exception means the end of the file has been reached.
                    catch(NullPointerException e)
                    {
                        // End of file.
                        searching = false;
                    }
                }
                else
                {
                    // Found a tag start marker, so leave.
                    searching = false;
                }
            }
            // Check if we are at end of file.
            if(tagStart < 0)
            {
                // If so, we are finished. Let the parsing loop take us out.
                done = true;
                continue parsing;
            }

            // Try states, expecting a custom exception to be thrown as soon as a transition is made.
            try
            {
                tryStates();
            }
            // A successful transition will throw this exception, and continue at the parsing loop.
            catch (TransitionMade e)
            {
                continue parsing;
            }

            // Advance searchStart to the character following the start of this unusable tag.
            searchStart = tagStart + 1;
        }
    }


    abstract protected void tryStates() throws TransitionMade;


    /**
     *  <p>
     *  Checks whether {@link #bufferString} contains
     *  <code>tag</code> at {@link #tagStart},
     *  </p>
     *  <p>
     *  If so, sets {@link #tagLength} to the length of
     *  <code>tag</code>, sets {@link #state}
     *  to <code>toState</code> and returns <code>true</code>.
     *  </p>
     *  <p>
     *  If not, returns <code>false</code>.
     *  </p>
     *
     *  @param tag          the tag to look for in {@link #buffer}
     *  @param toState      the parser {@link #state} to assign if successful
     *
     *  @return whether the tag was found
     */
    protected boolean succeed(String tag, int toState)
    {
        tagLength = tag.length();
        if (bufferString.regionMatches(tagStart, tag, 0, tagLength))
        {
            state = toState;
            searchStart = 0;
            buffer.delete(0, tagStart + tagLength);
            return true;
        }
        else
        {
            return false;
        }
    }


    /**
     *  If {@link #bufferString} contains
     *  <code>tag</code> at {@link #tagStart} and
     *  sets {@link #state} to <code>toState</code>.
     *
     *  @param tag      the tag to look for in {@link #buffer}
     *  @param toState  the parser {@link #state} to assign if successful
     *
     *  @throws TransitionMade if the transition is successfully made
     */
    protected void transition(String tag, int toState) throws TransitionMade
    {
        if (succeed(tag, toState))
        {
            throw(TRANSITION_MADE);
        }
    }


    /**
     *  If {@link #bufferString} contains
     *  <code>tag</code> at {@link #tagStart},
     *  sets {@link state} to <code>toState</code>,
     *  captures the substring of {@link #bufferString} from
     *  <code>0</code> to {@link #tagStart},
     *  filtering whitespace and setting <code>component</code>
     *  to the result.
     *
     *  @param tag          the tag to look for in {@link #buffer}
     *  @param toState      the parser {@link #state} to assign if successful
     *  @param component    the component to set to the whitespace-filtered substring of {@link #buffer}
     *                      from <code>0</code> to {@link #tagStart}
     *
     *  @return the substring of {@link #buffer} from <code>0</code> to {@link #tagStart}
     *
     *  @throws TransitionMade if the transition is successfully made
     */
    protected void transition(String tag, int toState, Field component) throws TransitionMade
    {
        if (succeed(tag, toState))
        {
            try
            {
                component.set(readerInstance, Toolkit.filterWhitespace(bufferString.substring(0, tagStart)));
            }
            catch (Exception e)
            {
                throw new DeveloperError(e);
            }
            throw(TRANSITION_MADE);
        }
    }


    /**
     *  <p>
     *  If {@link #bufferString} contains
     *  <code>tag</code> at {@link #tagStart},
     *  sets {@link #state} to <code>toState</code>,
     *  captures the substring of {@link #bufferString} from
     *  <code>0</code> to {@link #tagStart},
     *  filtering whitespace and setting <code>component</code>
     *  to the result, then deleting the tag from the buffer.
     *  </p>
     *
     *  @param tag              the tag to look for in {@link #buffer}
     *  @param toState          the parser {@link #state} to assign if successful
     *  @param component        the component to set to the whitespace-filtered substring of {@link #buffer}
     *                          from <code>0</code> to {@link #tagStart}
     *  @param attributeName    the name of the attribute holding the desired content
     *
     *  @throws TransitionMade if the transition is successfully made
     */
    protected void transition(String tag, int toState, Field component, String attributeName) throws TransitionMade
    {
        if (succeed(tag, toState))
        {
            int markerEnd = bufferString.substring(tagStart).indexOf(MARKER_END);
            if (markerEnd == -1)
            {
                Log.userinfo(tag + " is missing closing \"" + MARKER_END + "\" at " +
                             lineNumber + " in \"" + fileName + "\".", Log.ERROR);
                Log.userinfo("Will not process this element.", Log.ERROR);
            }
            else
            {
                String attributeValue = Toolkit.getAttributeValue(attributeName,
                                                                     bufferString.substring(tagStart, tagStart + markerEnd));
                if (attributeValue.length() > 0)
                {
                    try
                    {
                        component.set(readerInstance, attributeValue);
                    }
                    catch (Exception e)
                    {
                        throw new DeveloperError(e);
                    }
                    throw(TRANSITION_MADE);
                }
            }
        }
    }


    /**
     *  Thrown by the various <code>transition</code>
     *  methods when a transition is successfully made.
     */
    public class TransitionMade extends Throwable
    {
    }
}

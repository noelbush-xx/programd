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

package org.alicebot.server.core.responder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.alicebot.server.core.Bot;
import org.alicebot.server.core.Bots;
import org.alicebot.server.core.Globals;
import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.util.SuffixFilenameFilter;
import org.alicebot.server.core.util.Tag;
import org.alicebot.server.core.util.UserError;


/**
 *  <p>
 *  Contains common methods of template parsing and processing
 *  that are generic for all responders that deal with markup
 *  output, and which use a three-part template,
 *  such as {@link HTMLResponder}, {@link FlashResponder}, etc.
 *  </p>
 *  <p>
 *  Uses a very simple parser that scans a template file and looks
 *  for pseudo-tags with which values can be substituted.
 *  </p>
 *
 *  @author  Kim Sullivan
 *  @author  Noel Bush
 */
abstract public class AbstractMarkupResponder implements Responder
{
    // Instance variables.

    /** The header part of a chat response. */
    protected LinkedList header    = new LinkedList();

    /** The reply part of a chat response. */
    protected LinkedList replyPart = new LinkedList();

    /** The footer part of a chat response. */
    protected LinkedList footer    = new LinkedList();

    /** An entire bot response. */
    protected StringBuffer response   = new StringBuffer();

    /** The host name to use. */
    protected String hostName;

    protected String botid;

    protected Bot bot;


    // Convenience constants.

    /** An empty string. */
    protected static final String EMPTY_STRING        = "";

    /** A period. */
    private static final String PERIOD                = ".";

    /** The string &quot;hostname&quot;. */
    protected static final String HOSTNAME            = "hostname/";

    /** The string &quot;hname&quot; (deprecated). */
    protected static final String HNAME               = "hname";

    /** The string &quot;reply&quot;. */
    protected static final String REPLY_START         = "reply";

    /** The string &quot;/reply&quot;. */
    protected static final String REPLY_END           = "/reply";

    /** The string &quot;userinput/&quot;. */
    protected static final String USERINPUT           = "userinput/";

    /** The string &quot;alice_in&quot; (deprecated). */
    protected static final String ALICE_IN            = "alice_in";

    /** The string &quot;response/&quot;. */
    protected static final String RESPONSE            = "response/";

    /** The string &quot;alice_out&quot; (deprecated). */
    protected static final String ALICE_OUT           = "alice_out";

    /** The string &quot;bot name=&quot;&quot;. */
    protected static final String BOT_NAME_EQUALS     = "bot name=\"";

    /** The string &quot;bot_&quot; (deprecated). */
    protected static final String BOT_                = "bot_";

    /** Tags to process from the template. */
    protected static String[] tags = {EMPTY_STRING, REPLY_START, REPLY_END, HOSTNAME, HNAME, USERINPUT,
                                      ALICE_IN, RESPONSE, ALICE_OUT, BOT_NAME_EQUALS, BOT_};

    /** Whether to log the chat to the database. */
    private static final boolean LOG_CHAT_TO_DATABASE =
        Boolean.valueOf(Globals.getProperty("programd.logging.to-database.chat", "false")).booleanValue();
    
   /** Whether to log the chat to xml text files. */
    private static final boolean LOG_CHAT_TO_XML =
        Boolean.valueOf(Globals.getProperty("programd.logging.to-xml.chat", "true")).booleanValue();
    
    /** The string &quot;_&quot;. */
    protected static final String UNDERSCORE          = "_";

    /** A quote mark. */
    protected static final String QUOTE_MARK          = "\"";

    /** The string &quot;<&quot;. */
    protected static final String MARKER_START        = "<";

    /** The string &quot;>&quot;. */
    protected static final String MARKER_END          = ">";

    /** The string &quot;>&quot;. */
    protected static final String ATOMIC_MARKER_END   = ">";

    /** A space. */
    protected static final String SPACE        = " ";


    /**
     *  Initializes an AbstractMarkupResponder.
     */
    public AbstractMarkupResponder(String botid)
    {
        this.botid = botid;
        this.bot = Bots.getBot(botid);
    }


    /**
     *  Parses a template given a path.
     *
     *  @param path the template path
     */
    protected void parseTemplate(String path)
    {
        FileReader reader = null;
        try
        {
            reader = new FileReader(path);
            parse(reader);
            reader.close();
        }
        catch (IOException e)
        {
            throw new UserError("I/O error trying to read \"" + path + "\".", e);
        }
    }


    public String preprocess(String input, String hostname)
    {
        response.setLength(0);
        this.hostName = hostname;

        int headerSize = header.size();

        // Parse character-by-character through the header template.
        for (int index = 0; index < headerSize; index++)
        {
            Object thisNode = header.get(index);

            // Just append strings.
            if (thisNode instanceof String)
            {
                response.append(thisNode);
            }

            // Append appropriate values for tags.
            else if (thisNode instanceof Tag)
            {
                Tag tag = (Tag)thisNode;
                String tagName = tag.getName();

                // Host name.
                if (tagName.equals(HOSTNAME))
                {
                    response.append(hostname);
                }
                // The user input.
                else if (tagName.equals(USERINPUT))
                {
                    response.append(input);
                }
                // Some bot predicate.
                else if (tagName.startsWith(BOT_NAME_EQUALS))
                {
                    int quote = tagName.lastIndexOf(QUOTE_MARK);
                    // This is clunky but fast.
                    if (quote > 10)
                    {
                        response.append(bot.getPropertyValue(tagName.substring(10, quote)));
                    }
                }
                // (Deprecated forms)
                // Host name.
                else if (tagName.equals(HNAME))
                {
                    response.append(hostname);
                }
                // The user input.
                else if (tagName.equals(ALICE_IN))
                {
                    response.append(input);
                }
                // Some bot predicate.
                else if (tagName.startsWith(BOT_))
                {
                    StringTokenizer tokenizer = new StringTokenizer(tagName, UNDERSCORE);
                    while (tokenizer.hasMoreTokens())
                    {
                        tokenizer.nextToken();
                        response.append(bot.getPropertyValue(tokenizer.nextToken().toLowerCase()));
                    }
                }
            }
        }
        return input;
    }
    

    public String append(String input, String reply, String appendTo)
    {
        int replyPartSize = replyPart.size();


        // Parse character-by-character through the reply part template.
        for (int index = 0; index < replyPartSize; index++)
        {
            Object thisNode = replyPart.get(index);

            // Just append strings.
            if (thisNode instanceof String)
            {
                response.append(thisNode);
            }

            // Append appropriate values for tags.
            else if (thisNode instanceof Tag)
            {
                Tag tag = (Tag)thisNode;
                String tagName = tag.getName();

                // The user input.
                if (tagName.equals(USERINPUT))
                {
                    response.append(input);
                }
                // The whole bot response.
                else if (tagName.equals(RESPONSE))
                {
                    if (response.length() > 0)
                    {
                        response.append(' ');
                    }
                    response.append(reply);
                }
                // Some bot predicate.
                else if (tagName.startsWith(BOT_NAME_EQUALS))
                {
                    int quote = tagName.lastIndexOf(QUOTE_MARK);
                    // This is clunky but fast.
                    if (quote > 10)
                    {
                        response.append(bot.getPropertyValue(tagName.substring(10, quote)));
                    }
                }
                // (Deprecated forms)
                // The user input.
                else if (tagName.equals(ALICE_IN))
                {
                    response.append(input);
                }
                // The bot response.
                else if (tagName.equals(ALICE_OUT))
                {
                    if (response.length() > 0)
                    {
                        response.append(' ');
                    }
                    response.append(reply);
                }
                // Some bot predicate.
                else if (tagName.startsWith(BOT_))
                {
                    StringTokenizer tokenizer = new StringTokenizer(tag.getName(), UNDERSCORE);
                    while (tokenizer.hasMoreTokens())
                    {
                        tokenizer.nextToken();
                        response.append(bot.getPropertyValue(tokenizer.nextToken().toLowerCase()));
                    }
                }
            }
        }
        if (appendTo.length() > 0)
        {
            return appendTo + ' ' + reply;
        }
        return appendTo + reply;
    }


    public void log(String input, String reply, String hostname, String userid, String botid)
    {
        if (LOG_CHAT_TO_DATABASE)
        {
            ResponderDatabaseLogger.log(input, reply, hostname, userid, botid);
        }
        if (LOG_CHAT_TO_XML)
        {
            ResponderXMLLogger.log(input, reply, hostname, userid, botid);
        }
    }
    

    public String postprocess(String reply)
    {
        int footerSize = footer.size();

        // Parse character by character through the footer part template.
        for (int index = 0; index < footerSize; index++)
        {
            Object thisNode = footer.get(index);

            // Just append strings.
            if (thisNode instanceof String)
            {
                response.append(thisNode);
            }

            // Append appropriate values for tags.
            else if (thisNode instanceof Tag)
            {
                Tag tag = (Tag)thisNode;
                String tagName = tag.getName();

                // Bot response.
                if (tagName.equals(RESPONSE))
                {
                    if (response.length() > 0)
                    {
                        response.append(' ');
                    }
                    response.append(reply);
                }
                // Some bot predicate.
                else if (tagName.startsWith(BOT_NAME_EQUALS))
                {
                    int quote = tagName.lastIndexOf(QUOTE_MARK);
                    // This is clunky but fast.
                    if (quote > 10)
                    {
                        response.append(bot.getPropertyValue(tagName.substring(10, quote)));
                    }
                }
                // Host name.
                else if (tagName.equals(HOSTNAME))
                {
                    response.append(hostName);
                }
                // (Deprecated forms)
                // Bot response.
                else if (tagName.equals(ALICE_OUT))
                {
                    if (response.length() > 0)
                    {
                        response.append(' ');
                    }
                    response.append(reply);
                }

                // Some bot predicate.
                else if (tagName.startsWith(BOT_))
                {
                    StringTokenizer tokenizer = new StringTokenizer(tagName, UNDERSCORE);
                    while (tokenizer.hasMoreTokens())
                    {
                        tokenizer.nextToken();
                        response.append(bot.getPropertyValue(tokenizer.nextToken().toLowerCase()));
                    }
                }
                // Host name.
                else if (tagName.equals(HNAME))
                {
                    response.append(hostName);
                }
            }
        }
        return response.toString();
    }
    

    /**
     *  Parses a template into the header, reply part and footer Lists.
     *
     *  @param file a FileReader for the chat template.
     */
    protected void parse(FileReader file) throws IOException
    {
        int index;
        char aChar;
        StringBuffer buffer = new StringBuffer();
        StringBuffer tag = new StringBuffer();
        List currentList = header;

        while ((index = file.read())!= -1)
        {
            aChar = (char)index;
            if (aChar == '<')
            {                         
                tag.setLength(0);
                while ((aChar = (char)file.read()) != '>')
                {
                    tag.append(aChar);
                }
                String tagString = tag.toString();

                int tagsIndex;
                for (tagsIndex = tags.length; --tagsIndex >= 0; )
                {                    
                    if (tagString.startsWith(tags[tagsIndex]))
                    {
                        currentList.add(buffer.toString());
                        buffer.setLength(0);
                        break;
                    }
                }
                switch (tagsIndex)
                {
                    case 0 :
                        buffer.append(MARKER_START + tag + MARKER_END);
                        break;

                    case 1 :
                        currentList = replyPart;
                        break;

                    case 2 :
                        currentList = footer;
                        break;

                    default :
                        currentList.add(new Tag(tagString));
                        break;
                }
            }
            else
            {
                buffer.append(aChar);
            }
        }
        currentList.add(buffer.toString());
    }

    
    /**
     *  Loads a template from a file.
     *
     *  @param path the path to the file
     *
     *  @return the loaded template
     */
    protected static LinkedList loadTemplate(String path)
    {
        String templateLine;
        LinkedList template = new LinkedList();

        FileReader freader = null;

        try
        {
            freader = new FileReader(path);
        }
        catch (FileNotFoundException e)
        {
            return null;
        }
        BufferedReader breader = new BufferedReader(freader);

        template.clear();

        try
        {
            while((templateLine = breader.readLine()) != null)
            {
                template.add(templateLine);
            }
            freader.close();
            breader.close();
        }
        catch (IOException e)
        {
            throw new UserError("I/O error reading \"" + path + "\".", e);
        }

        return template;
    }


    /**
     *  Scans a given directory for templates that match a given
     *  filename filter and returns a map of template names (filenames minus
     *  suffixes) to filenames.
     *
     *  @param directoryName    the name of the directory to scan
     *  @param filter           the filename filter to use
     *
     *  @return a map of template names (filenames minus suffixes) to filenames.
     */
    protected static HashMap registerTemplates(String directoryName, SuffixFilenameFilter filter)
    {
        File directory = new File(directoryName);
        HashMap result = new HashMap();
        if (directory.isDirectory())
        {
            String[] templateFilenames = directory.list(filter);
            int templateCount = templateFilenames.length;
            if (templateCount > 0)
            {
                for (int index = templateCount; --index >= 0; )
                {
                    String templateFilename = templateFilenames[index];
                    result.put(templateFilename.substring(0, templateFilename.lastIndexOf(PERIOD)),
                                  directoryName + File.separator + templateFilename);
                }
            }
        }
        return result;
    }
}

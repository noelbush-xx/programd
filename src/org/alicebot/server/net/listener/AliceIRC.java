/*
    - added initialize method and made implementation of AliceChatListener
    - changed some server property names
*/

/*
    4.1.4 [00] - December 2001, Noel Bush
    - changed "ICQ" to "IRC" as noted by spider@wanfear.com
    - cleaned up some of the gratuitous text decorations,
      and changed all System.out/System.err prints to use Log/Trace
    - added line breaking to response
    - reworked to fit changes to AliceChatListener
    - made to implement ShellCommandable
*/

package org.alicebot.server.net.listener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;

import org.alicebot.server.core.ActiveMultiplexor;
import org.alicebot.server.core.Bot;
import org.alicebot.server.core.Multiplexor;
import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.util.Trace;
import org.alicebot.server.core.responder.Responder;
import org.alicebot.server.core.responder.TextResponder;
import org.alicebot.server.core.util.ShellCommandable;
import org.alicebot.server.core.util.Toolkit;


/**
 *  This code is from the sIRC project and was written
 *  by Chris Knight <a href="http://www.chrisknight.com/sirc/">http://www.chrisknight.com/sirc/</a>
 *  and modified to work with an Alicebot server.
 *
 *  @author  Chris Knight
 *  @author  Jon Baer
 *  @version 4.1.4
 *
 *  @see <a href="http://www.chrisknight.com/sirc/">http://www.chrisknight.com/sirc/</a>
 */

public class AliceIRC extends AliceChatListener implements ShellCommandable
{
    /** Please document. */
    private static final String VERSION = "0.86.0b";

    /** Please document. */
    private static final String VERDATE = "1999.04.07";
    
    /** Please document. */
    private static final boolean DEBUG = false;
    
    /** Please document. */
    private static final int    MAXARGC        = 16;
    
    /** Please document. */
    private static final String SERVERPREFIX   = "[server]";

    /** Please document. */
    private static final String SIRCMESSAGE    = "[irc]";

    /** Please document. */
    private static final String DEBUGPREFIX    = "[debug]";

    /** Please document. */
    private static final String NONE           = "";
    
    /** Please document. */
    private static final byte   NOTCONNECTED   = 0;

    /** Please document. */
    private static final byte   CONNECTING     = 1;

    /** Please document. */
    private static final byte   CONNECTED      = 2;

    /** Please document. */
    private static final byte   DISCONNECTING  = 3;
    
    /** Please document. */
    private byte clientStatus = NOTCONNECTED;
    
    /** Please document. */
    private Socket socket;

    /** Please document. */
    private BufferedReader reader;

    /** Please document. */
    private PrintWriter writer;
    
    /** Please document. */
    private String host;
    
    /** Please document. */
    private String nick;
    
    /** Please document. */
    private String channel;

    /** Please document. */
    private int port;
    
    /** Please document. */
	private static final String MSG = "AliceIRC: ";	

    /** Please document. */
    public static final String label = "AliceIRC";
    
    
    /**
     *  Creates a new AliceIRC chat listener for a given bot.
     *
     *  @param bot	the bot for whom to listen
     */
    public AliceIRC(Bot bot)
    {
        super(bot, "AliceIRC", new String[][] { {"host", ""},
                                                {"port", "6667"},
                                                {"nick", ""},
                                                {"channel", ""} });
    }

    
    public boolean checkParameters()
    {
        // Get parameters.
        host = (String)parameters.get("host");
        try
        {
            port = Integer.parseInt((String)parameters.get("port"));
        }
        catch (NumberFormatException e)
        {
            logMessage("Invalid port specification (try a number!); aborting.");
            return false;
        }
        nick = (String)parameters.get("nick");
        channel = (String)parameters.get("channel");

        // Check parameters.
        if (host.length() == 0)
        {
            logMessage("No host specified; aborting.");
            return false;
        }
        if (port <= 0)
        {
            logMessage("Invalid port; aborting.");
        }
        if (nick.length() == 0)
        {
            logMessage("No nick specified; aborting.");
            return false;
        }
        if (channel.length() == 0)
        {
            logMessage("No channel specified; aborting.");
            return false;
        }
        return true;
    }


    public void shutdown()
    {
        disconnect();
    }


    /**
     *  Connects to the given host and begins listening.
     */
    public void run()
    {
        logMessage("Starting for \"" + botID + "\".");
        processMessageCommandClient("CONNECT", this.host + " " + this.port);
        processMessage("/NICK " + this.nick);
        processMessage("/JOIN " + this.channel);
        listen();
    }


    public String getShellID()
    {
        return "irc";
    }


    public String getShellDescription()
    {
        return "Alice IRC chat listener";
    }


    public String getShellCommands()
    {
        return "Not yet implemented.";
    }


    public void processShellCommand(String command)
    {
        int slash = command.indexOf('/');
        if (slash != 0)
        {
            logMessage("Invalid command.");
            return;
        }
        int space = command.indexOf(' ');
        if (space == -1)
        {
            processMessageCommand(command.substring(1), "");
            return;
        }
        else
        {
            processMessageCommand(command.substring(1, space), command.substring(space + 1));
            return;
        }
    }


    /**
     *  Returns the version string.
     *
     *  @return the version string
     */
    public String getVersion()
    {
        return VERSION + " " + VERDATE;
    }
    
    
    /**
     *  Please document this.
     */
    private void connect()
    {
        if (clientStatus == NOTCONNECTED)
        {
            clientStatus = CONNECTING;
            logMessage("Contacting " + this.host + ":" + this.port);
            
            try
            {
                socket = new Socket(this.host, this.port);
                logMessage("Connected to " + this.host + ":" + this.port);
                
            }
            catch (UnknownHostException e0)
            {
                logMessage("Cannot connect; unknown server.");
                clientStatus = NOTCONNECTED;
            }
            catch (IOException e1)
            {
                logMessage("Cannot connect; the server is down or not responding.");
                clientStatus = NOTCONNECTED;
            }
            // If we didn't have any problems connecting
            if (clientStatus == CONNECTING)
            {
                try
                {
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    writer = new PrintWriter(socket.getOutputStream(), true);
                    clientStatus = CONNECTED;
                }
                catch (IOException e0)
                {
                    logMessage("Cannot connect: I/O error.");
                    clientStatus = DISCONNECTING;
                    
                    try
                    {
                        socket.close();
                    }
                    catch (IOException e1)
                    {
                        // !
                    }
                    finally
                    {
                        socket = null;
                        clientStatus = NOTCONNECTED;
                    }
                }
            }
        }
        else
        {
            switch(clientStatus)
            {
                case CONNECTED :
                    logMessage("Cannot connect again; already connected.");
                    break;
                    
                case CONNECTING :
                    logMessage("Cannot connect again; already in the process of connecting.");
                    break;
                    
                case DISCONNECTING :
                    logMessage("Cannot connect now; still trying to disconnect.");
                    break;
                    
                default :
                    logMessage("Got unknown clientStatusCode: " + clientStatus);
                    break;
            }
        }
    }
    

    /**
     *  Please document this.
     */
    private void disconnect()
    {
        if (clientStatus == CONNECTED)
        {
            clientStatus = DISCONNECTING;
            try
            {
                socket.close();
            }
            catch (IOException e)
            {
                logMessage("IO exception trying to disconnect.");
            }
            finally
            {
                reader = null;
                writer = null;
                logMessage("Connection closed.");
                clientStatus = NOTCONNECTED;
            }
        }
        else
        {
            switch(clientStatus)
            {
                case NOTCONNECTED :
                    logMessage("Cannot close connection; not connected.");
                    break;
                    
                case CONNECTING :
                    logMessage("Cannot close connection; currently trying to connect.");    
                    break;
                    
                case DISCONNECTING :
                    logMessage("Cannot close connection; currently trying to close it.");
                    break;
                    
                default :
                    logMessage("Got unknown clientStatusCode: " + clientStatus);
                    break;
            }
        }
    }
    
    
    /**
     *  Please document this.
     */
    protected void processMessage(String message)
    {
        // If the message isn't blank,
        if (message.length() > 0)
        {
            // and is a command,
            if (message.charAt(0) == '/')
            {
                String command;
                
                try
                {
                    command = message.substring(1, message.indexOf(' '));
                }
                catch (StringIndexOutOfBoundsException e)
                {
                    command = message.substring(1);
                }
                
                if (processMessageCommand(command, message))
                {
                }
                else if (processMessageCommandClient(command, message))
                {
                }
                else if (processMessageCommandDebug(command, message) && DEBUG)
                {
                }
                else
                {
                    sendMessage(SIRCMESSAGE, "Unknown Command: " + command);
                }
            }
            else                        
            {
                if (clientStatus == CONNECTED)
                {
                    if (channel.length() > 0)
                    {
                        sendMessage(NONE, "[" + nick + "] " + message);
                        logMessage("Got a message from [" + nick + "]: " + message);
                        sendServerMessage("/MSG " + " " + channel + " :" + message);
                        
                        // WARNING: Currently uses response from ANY bot!!!!!!
                        String[] botResponse =
                            Toolkit.breakLines(Multiplexor.
                                                    getResponse(message,
                                                                nick + "_IRC",
                                                                botID, new TextResponder()));
                        if (botResponse.length > 0)
                        {
                            for (int line = 0; line < botResponse.length; line++)
                            {
                                processMessage("/PRVMSG " + nick + " " + botResponse[line]);
                            }
                        }
                    }
                }
            }
        }
    }
    
    
    /**
     *  Please document this.
     */
    private boolean processMessageCommand(String command, String message)
    {
        boolean processed = false;
        
        // Extract parameters of message.
        String params = message.substring((message.indexOf(' ') + 1));
        if (params.equals("/" + command))
        {
            params = NONE;
        }
        
        // Process command.
        if (clientStatus == CONNECTED)
        {
            if (command.equalsIgnoreCase("AWAY"))
            {
                sendServerMessage("AWAY :" + params);
                processed = true;
            }
            else if (command.equalsIgnoreCase("INVITE"))
            {
                if (params.length() > 0)
                {
                    sendServerMessage("INVITE " + params + " " + channel);
                }
                processed = true;
            }
            else if (command.equalsIgnoreCase("KICK"))
            {
                if (params.length() > 0)
                {
                    int firstindex = params.indexOf(' ');
                    int secondindex = params.indexOf(' ', (firstindex+1));
                    try
                    {
                        sendServerMessage("KICK " + channel + " " + params.substring(0, secondindex) + " :" + params.substring((secondindex+1)));
                    }
                    catch (StringIndexOutOfBoundsException eMSG)
                    {
                        sendServerMessage("KICK " + channel + " " + params);
                    }
                }
                processed = true;
            }
            else if (command.equalsIgnoreCase("LIST"))
            {
                if (params.length() == 0)
                {
                    sendServerMessage("LIST " + channel);
                }
                else
                {
                    sendServerMessage("LIST " + params);
                }
                processed = true;
            }
            else if (command.equalsIgnoreCase("JOIN"))
            {
                // Check the channel.
                if (params.length() == 0)
                {
                    if (channel.length() == 0)
                    {
                        sendMessage(SIRCMESSAGE, "You're not in a channel.");
                    }
                    else
                    {
                        sendMessage(SIRCMESSAGE, "You're currently in: " + channel + ".");
                    }
                }
                // Join a new channel.
                else if (channel.length() == 0)
                {
                    sendServerMessage("JOIN " + params);
                }
                // Leave the channel.
                else if(params.equals("0"))
                {
                    sendServerMessage("PART " + channel);
                }
                // Change channels.
                else
                {
                    sendServerMessage("PART " + channel);
                    sendServerMessage("JOIN " + params);
                }
                processed = true;
            }
            else if (command.equalsIgnoreCase("MODE"))
            {
                if (params.length() > 0)
                {
                    sendServerMessage("MODE " + channel + " " + params);
                }
                processed = true;
            }
            else if (command.equalsIgnoreCase("MSG"))
            {
                if (params.length() > 0)
                {
                    try
                    {
                        int paramsindex = params.indexOf(' ');
                        sendServerMessage("PRIVMSG " + params.substring(0, paramsindex) + " :" + params.substring((paramsindex+1)));
                    }
                    catch (StringIndexOutOfBoundsException eMSG)
                    {
                        sendServerMessage("PRIVMSG " + params);
                    }
                    sendMessage(NONE, "*" + this.nick + "* " + params);
                }
                processed = true;
            }
            else if (command.equalsIgnoreCase("NAMES"))
            {
                if (params.length() == 0)
                {
                    sendServerMessage("NAMES " + channel);
                }
                else
                {
                    sendServerMessage("NAMES " + params);
                }
                processed = true;
            }
            else if (command.equalsIgnoreCase("NICK"))
            {
                if (params.length() == 0)
                {
                    sendMessage(SIRCMESSAGE, "You're currently known as " + this.nick);
                }
                else
                {
                    sendServerMessage("NICK " + params);
                }
                processed = true;
            }
            else if (command.equalsIgnoreCase("QUIT"))
            {
                if (params.length() == 0)
                {
                    sendServerMessage("QUIT");
                }
                else
                {
                    sendServerMessage("QUIT :" + params);
                }
                
                channel = NONE;
                disconnect();
                processed = true;
            }
            else if (command.equalsIgnoreCase("TOPIC"))
            {
                if (channel.length() == 0)
                {
                    sendMessage(SIRCMESSAGE, "You must be in a channel to set the topic!");
                }
                else
                {
                    if (params.length() == 0)
                    {
                        sendServerMessage("TOPIC " + channel);
                    }
                    else
                    {
                        sendServerMessage("TOPIC " + channel + " :" + params);
                    }
                }
                processed = true;
            }
        }
        else
        {
            // The following commands are processed if we're not connected...
            if (command.equalsIgnoreCase("NICK"))
            {
                if (params.length() == 0)
                {
                    sendMessage(SIRCMESSAGE, "You're currently known as " + this.nick);
                }
                else
                {
                    this.nick = params;
                    sendMessage(SIRCMESSAGE, "You're now known as " + this.nick);
                }
                processed = true;
            }
        }
        return processed;
    }
    
    
    /**
     *  Please document this.
     */
    private boolean processMessageCommandClient(String command, String message)
    {
        boolean processed = false;
        
        int argc = -1;
        int index[] = new int[(MAXARGC + 1)];
        
        do
        {
            argc++;
            if (argc == 0) { index[argc] = message.indexOf(" ");                      }
            else           { index[argc] = message.indexOf(" ", (index[argc-1] + 1)); }
        }
        while (index[argc] != -1);
        
        // note: we have one extra in index[] at this point (a '-1')
        
        String args[] = new String[MAXARGC];
        
        for (int x=0; x < argc; x++)
        {
            if ((x+1) >= argc)
            {
                args[x] = message.substring(index[x]+1);
            }
            else
            {
                args[x] = message.substring(index[x]+1, index[x+1]);
            }
        }
        
        if (command.equalsIgnoreCase("SERVER") || command.equalsIgnoreCase("CONNECT"))
        {
            if (this.nick.length() == 0)
            {
                sendMessage(SIRCMESSAGE, "You cannot connect to a server unless your NICK is set.");
            }
            else if (argc == 0)
            {
                sendMessage(SIRCMESSAGE, "Please specify a server to connect to.");
            }
            else if (argc == 1)
            {
                connect();
                
                if(clientStatus == CONNECTED)
                {
                    sendServerMessage("USER " + this.nick + " " + socket.getInetAddress().getHostName() + " server :" + this.nick);
                    sendServerMessage("NICK " + this.nick);
                }
            }
            else
            {
                try
                {
                    connect();
                    
                    if(clientStatus == CONNECTED)
                    {
                        sendServerMessage("USER " + this.nick + " " + socket.getInetAddress().getHostName() + " server :" + this.nick);
                        sendServerMessage("NICK " + this.nick);
                    }
                }
                catch(NumberFormatException e)
                {
                    sendMessage(SIRCMESSAGE, "The Port you specified is invalid.");
                }
            }
            processed = true;
        }
        else if (command.equalsIgnoreCase("EXIT"))
        {
            if (clientStatus == CONNECTED)
            {
                disconnect();
            } 
            // messagerelay.exit();
            processed = true;
        }
        else if (command.equalsIgnoreCase("COMMANDS") || command.equalsIgnoreCase("HELP"))
        {
            sendMessage(NONE, NONE);
            sendMessage(SIRCMESSAGE, "sIRC Commands:");
            sendMessage(SIRCMESSAGE, "  /away [<message>]");
            sendMessage(SIRCMESSAGE, "  /commands");
            sendMessage(SIRCMESSAGE, "  /connect <server> [<this.port>]");
            sendMessage(SIRCMESSAGE, "  /exit");
            sendMessage(SIRCMESSAGE, "  /invite <this.nickname>");
            sendMessage(SIRCMESSAGE, "  /join [<channel> | \'0\']");
            sendMessage(SIRCMESSAGE, "  /kick <this.nickname> [<message>]");
            sendMessage(SIRCMESSAGE, "  /list [<channel>]");
            sendMessage(SIRCMESSAGE, "  /mode <(+|-)mode> [<this.nickname>]");
            sendMessage(SIRCMESSAGE, "  /msg <this.nickname> <message>");
            sendMessage(SIRCMESSAGE, "  /names [<channel>]");
            sendMessage(SIRCMESSAGE, "  /this.nick [<this.nickname>]");
            sendMessage(SIRCMESSAGE, "  /quit [<message>]");
            sendMessage(SIRCMESSAGE, "  /server <server> [<this.port>]");
            sendMessage(SIRCMESSAGE, "  /topic [<topic>]");
            sendMessage(SIRCMESSAGE, "  /version");
            sendMessage(NONE, NONE);
            processed = true;
        }
        
        return processed;
    }
    
    
    /**
     *  Please document this.
     */
    private boolean processMessageCommandDebug(String command, String message)
    {
        boolean processed = false;
        
        int argc = -1;
        int index[] = new int[(MAXARGC + 1)];
        
        do
        {
            argc++;
            if (argc == 0)
            {
                index[argc] = message.indexOf(" ");
            }
            else
            {
                index[argc] = message.indexOf(" ", (index[argc-1] + 1));
            }
        }
        while (index[argc] != -1);
        
        String args[] = new String[MAXARGC];
        
        for (int x=0; x < argc; x++)
        {
            if ((x+1) >= argc)
            {
                args[x] = message.substring(index[x]+1);
            }
            else
            {
                args[x] = message.substring(index[x]+1, index[x+1]);
            }
        }
        
        if (command.equals("testargs"))  
        {
            StringBuffer teststring = new StringBuffer("Test Arguments; argc=" + argc + " command=" + command);
            
            for (int x = 0; x < argc; x++)
            {
                teststring.append(" [" + (x+1) + ";" + args[x] + "]");
            }
            
            Trace.devinfo(teststring.toString());
            processed = true;
        }
        else if (command.equals("debug"))
        {
            Trace.devinfo("- - - - - - - - - - - - - - - - - - - - ");
            Trace.devinfo("clientStatus=" + clientStatus);
            Trace.devinfo("socket=" + socket);
            Trace.devinfo("reader=" + reader);
            Trace.devinfo("writer=" + writer);
            Trace.devinfo("- - - - - - - - - - - - - - - - - - - - ");
            processed = true;
        }
        else if (command.equals("raw"))    
        {
            String messagex = NONE;
            
            for(int x = 0; x < argc; x++)
            {
                messagex = messagex + args[x] + " ";
            }
            
            sendServerMessage(messagex);        
            processed = true;
        }
        
        return processed;
        
    }
    

    /**
     *  Please document this.
     */
    private void processServerMessage(String message)
    {
        String prefix = NONE;
        String command = NONE;
        String params = NONE;
        String targetnick = NONE;

        if (message == null)
        {
            disconnect();
            return;
        }
        else if (message.length() > 0)
        {
            if (DEBUG)
            {
                Log.devinfo(message, Log.LISTENERS);
            }
        }

        // We have a Prefix
        if(message.charAt(0) == ':')  
        {
            int firstspace = message.indexOf(' ');
            int secondspace = message.indexOf(' ', (firstspace + 1));
            prefix = message.substring(0, firstspace); 
            command = message.substring((firstspace+1), secondspace);
            params = message.substring((secondspace+1));
        }
        // We've got no Prefix
        else                          
        {
            int firstspace = message.indexOf(' ');
            command = message.substring(0, firstspace);
            params = message.substring((firstspace+1));
        }
        
        if(prefix.length() > 0)
        {
            try
            {
                targetnick = prefix.substring(1, prefix.indexOf('!'));
            }
            catch (StringIndexOutOfBoundsException e)
            {
                targetnick = prefix.substring(1);
            }
        }
        
        try
        {
            switch(Integer.parseInt(command))
            {
            case 001 :
                break;
                
            case 321 :
                break;
                
            case 322 :
                {
                    int firstSpaceIndex = params.indexOf(' ');
                    int secondSpaceIndex = params.indexOf(' ', (firstSpaceIndex+1));
                    int colonIndex = params.indexOf(':');
                    sendMessage(SERVERPREFIX,
                        params.substring((firstSpaceIndex+1), secondSpaceIndex) + ": " + params.substring((secondSpaceIndex+1), (colonIndex-1)) + " " + params.substring((colonIndex+1)));
                }
                break;
                
            case 353 :
                {
                    int colonIndex = params.indexOf(':');
                    int equalsIndex = params.indexOf('=');
                    sendMessage(SERVERPREFIX,
                        "Users on " + params.substring((equalsIndex+2), (colonIndex-1)) + ": " + params.substring((colonIndex+1)));
                }
                break;
                
            case 372 :
                sendMessage(SERVERPREFIX, params.substring((params.indexOf(':')+1)));
                break;
                
            default :
                sendMessage(SERVERPREFIX, "(" + command + ") " + params.substring((params.indexOf(':')+1)));
                break;
            }
        }
        catch(NumberFormatException e)
        {
            if (command.equals("INVITE"))
            {
                int firstindex = params.indexOf(' ');
                sendMessage(SERVERPREFIX, targetnick + " has invited you to " + helpExtractIRCString(params.substring((firstindex+1))) + ".");
            }
            else if (command.equals("JOIN"))
            {
                String channelx = helpExtractIRCString(params);
                
                if (targetnick.equals(this.nick))
                {
                    sendMessage(SERVERPREFIX, "You're now on " + channelx + ".");
                    channel = channelx;
                }
                else
                {
                    sendMessage(SERVERPREFIX, targetnick + " has joined the channel.");
                }
            }
            else if(command.equals("KICK"))
            {
                String kickchannel = NONE;
                String kickuser = NONE;
                String kickcomment = NONE;
                
                int firstindex = params.indexOf(' ');
                int secondindex = params.indexOf(' ', (firstindex+1));
                
                try
                {
                    kickchannel = params.substring(0, firstindex);
                    kickuser = params.substring((firstindex+1), secondindex);
                    kickcomment = helpExtractIRCString(params.substring((secondindex+1)));
                    
                    if (kickuser.equals(this.nick))
                    {
                        sendMessage(SERVERPREFIX, "You've just been kicked off " + kickchannel + " by " + targetnick + " (" + kickcomment + ").");
                        channel = NONE;
                    }
                    else
                    {
                        sendMessage(SERVERPREFIX, kickuser + " has been kicked off " + kickchannel + " by " + targetnick + " (" + kickcomment + ").");
                    }
                }
                catch (StringIndexOutOfBoundsException eKICK)
                {
                    kickchannel = params.substring(0, firstindex);
                    kickuser = params.substring((firstindex+1));
                    
                    if (kickuser.equals(this.nick))
                    {
                        sendMessage(SERVERPREFIX, "You've just been kicked off " + kickchannel + " by " + targetnick + ".");
                        channel = NONE;
                    }
                    else
                    {
                        sendMessage(SERVERPREFIX, targetnick + " has been kicked off " + kickchannel + " by " + targetnick + ".");
                    }
                }
            }
            else if(command.equals("NICK"))
            {
                if (targetnick.equals(this.nick))
                {
                    String newnick = helpExtractIRCString(params);
                    sendMessage(SERVERPREFIX, "You're now known as " + newnick + ".");
                    this.nick = newnick;
                }
                else
                {
                    sendMessage(SERVERPREFIX, targetnick + " is now known as " + params.substring(1) + ".");
                }
            }
            else if(command.equals("PART"))
            {
                if (targetnick.equals(this.nick))
                {
                    sendMessage(SERVERPREFIX, "You've just left " + params + ".");
                    channel = NONE;
                }
                else
                {
                    sendMessage(SERVERPREFIX, targetnick + " has left the channel.");
                }
            }
            else if(command.equals("PING"))
            {
                sendServerMessage("PONG " + params);
            }
            else if (command.equals("PRIVMSG"))
            {
                String target = params.substring(0, params.indexOf(' '));
                String gitter = params.substring((params.indexOf(':') + 1));
                
                if (target.equals(this.nick))
                {
                    sendMessage(NONE, "*" + targetnick + "* " + gitter);
                    logMessage("Request: [" + targetnick + "]: " + gitter);

                    String[] botResponse =
                        Toolkit.breakLines(Multiplexor.
                                                getResponse(gitter,
                                                            targetnick + "_IRC",
                                                            botID, new TextResponder()));
                    if (botResponse.length > 0)
                    {
                        for (int line = 0; line < botResponse.length; line++)
                        {
                            processMessage("/MSG " + targetnick + " " + botResponse[line]);
                        }
                    }
                }
                // target equals channel
                else 
                {
                    sendMessage(NONE, "[" + targetnick + "] " + gitter);
                    
                
                }
            }
            else if (command.equals("QUIT"))
            {
                if (params.length() == 0)
                {
                    sendMessage(SERVERPREFIX, targetnick + " has quit.");
                }
                else
                {
                    sendMessage(SERVERPREFIX, targetnick + " has quit (" + helpExtractIRCString(params) + ").");
                }
            }
            else if (command.equals("TOPIC"))
            {
                if(targetnick.equals(this.nick))
                {
                    sendMessage(SERVERPREFIX, "The topic is now: " + params.substring((params.indexOf(':')+1)));
                }
                else
                {
                    sendMessage(SERVERPREFIX, targetnick + " has set the topic to: " + helpExtractIRCString(params.substring((params.indexOf(' ')+1))));
                }
            }
            else
            {
            }
        }
    }
    

    /**
     *  Please document this.
     */
    private String helpExtractIRCString(String string)
    {
        try
        {
            if (string.charAt(0) == ':')
            {
                return string.substring(1);
            }
            else
            {
                return string;
            }
        }
        catch (StringIndexOutOfBoundsException e)
        {
            return NONE;
        }
    }
    
    
    /**
     *  Standard method for logging and notifying of a message.
     *
     *  @param message	the message
     */
    private void logMessage(String message)
    {
        Log.userinfo(MSG + message, Log.LISTENERS);
    }
    
    
    /**
     *  Please document this.
     */
    private void sendMessage(String type, String message)
    {
        Log.userinfo(MSG + type + " " + message, Log.LISTENERS);
    }
    
    
    /**
     *  Please document this.
     */
    private void sendServerMessage(String message)
    {
        if (clientStatus == CONNECTED)
        {
            writer.println(message);
        }
    }
    
    
    /**
     *  Please document this.
     */
    private void listen()
    {
        String line;
        
        while(clientStatus == CONNECTED)
        {
            try
            {
                line = reader.readLine();
                processServerMessage(line);
            }
            catch (IOException e)
            {
                // !
            }
        }
        
    }
}



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

public class AliceIRC implements AliceChatListener, ShellCommandable
{
    // ------------------------------------------------------------------------
    
    private static final String VERSION = "0.86.0b";
    private static final String VERDATE = "1999.04.07";
    
    // ------------------------------------------------------------------------
    
    private static final boolean DEBUG = false;
    
    // ------------------------------------------------------------------------
    
    private static final int    MAXARGC        = 16;
    
    private static final String SERVERPREFIX   = "[server]";
    private static final String SIRCMESSAGE    = "[irc]";
    private static final String DEBUGPREFIX    = "[debug]";
    private static final String NONE           = "";
    
    private static final byte   NOTCONNECTED   = 0;
    private static final byte   CONNECTING     = 1;
    private static final byte   CONNECTED      = 2;
    private static final byte   DISCONNECTING  = 3;
    
    // ------------------------------------------------------------------------
    
    private byte clientStatus = NOTCONNECTED;
    
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    
    private String host, nick, channel;
    private int port;
    
    
    public boolean initialize(Properties properties)
    {
        // Check if enabled.
        if (Boolean.valueOf(properties.getProperty("programd.listeners.irc.enabled", "false")).booleanValue())
        {
            // Get parameters.
            host = properties.getProperty("programd.listeners.irc.host", "");
            try
            {
                port = Integer.parseInt(properties.getProperty("programd.listeners.irc.port", ""));
            }
            catch (NumberFormatException e)
            {
                Log.userinfo("AliceIRC: Invalid port specification (try a number!); aborting.", Log.LISTENERS);
                return false;
            }
            nick = properties.getProperty("programd.listeners.irc.nick", "");
            channel = properties.getProperty("programd.listeners.irc.channel", "");

            // Check parameters.
            if (host.length() == 0)
            {
                Log.userinfo("AliceIRC: no host specified; aborting.", Log.LISTENERS);
                return false;
            }
            if (port <= 0)
            {
                Log.userinfo("AliceIRC: invalid port; aborting.", Log.LISTENERS);
            }
            if (nick.length() == 0)
            {
                Log.userinfo("AliceIRC: no nick specified; aborting.", Log.LISTENERS);
                return false;
            }
            if (channel.length() == 0)
            {
                Log.userinfo("AliceIRC: no channel specified; aborting.", Log.LISTENERS);
                return false;
            }
            return true;
        }
        else
        {
            return false;
        }
    }


    public void shutdown()
    {
        disconnect();
    }


    /**
     *  Creates a new AliceIRC chat listener.
     */
    public AliceIRC()
    {
    }

    
    /**
     *  Connects to the given host and begins listening.
     */
    public void run()
    {
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
            Log.userinfo("AliceIRC: invalid command.", Log.LISTENERS);
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
            Log.userinfo("AliceIRC contacting " + this.host + ":" + this.port, Log.LISTENERS);
            
            try
            {
                socket = new Socket(this.host, this.port);
                Log.userinfo("AliceIRC connected to " + this.host + ":" + this.port, Log.LISTENERS);
                
            }
            catch (UnknownHostException e0)
            {
                Log.userinfo("AliceIRC cannot connect; unknown server.", Log.LISTENERS);
                clientStatus = NOTCONNECTED;
            }
            catch (IOException e1)
            {
                Log.userinfo("AliceIRC cannot connect; the server is down or not responding.", Log.LISTENERS);
                clientStatus = NOTCONNECTED;
            }
            
            if (clientStatus == CONNECTING)    // If we didn'y have any problems connecting
            {
                try
                {
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    writer = new PrintWriter(socket.getOutputStream(), true);
                    clientStatus = CONNECTED;
                }
                catch (IOException e0)
                {
                    Log.userinfo("AliceIRC cannot connect: I/O error.", Log.LISTENERS);
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
                    Log.userinfo("AliceIRC cannot connect; already connected.", Log.LISTENERS);
                    break;
                    
                case CONNECTING :
                    Log.userinfo("AliceIRC cannot connect; already connecting.", Log.LISTENERS);
                    break;
                    
                case DISCONNECTING :
                    Log.userinfo("AliceIRC cannot connect; still trying to disconnect.", Log.LISTENERS);
                    break;
                    
                default :
                    Log.userinfo("AliceIRC got unknown clientStatusCode: " + clientStatus, Log.LISTENERS);
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
                Log.userinfo("AliceIRC: IO exception trying to disconnect.", Log.LISTENERS);
            }
            finally
            {
                reader = null;
                writer = null;
                Log.userinfo("AliceIRC connection closed.", Log.LISTENERS);
                clientStatus = NOTCONNECTED;
            }
        }
        else
        {
            switch(clientStatus)
            {
                case NOTCONNECTED :
                    Log.userinfo("AliceIRC cannot close connection; not connected.", Log.LISTENERS);
                    break;
                    
                case CONNECTING :
                    Log.userinfo("AliceIRC cannot close connection; currently trying to connect.", Log.LISTENERS);    
                    break;
                    
                case DISCONNECTING :
                    Log.userinfo("AliceIRC cannot close connection; currently trying to close it.", Log.LISTENERS);
                    break;
                    
                default :
                    Log.userinfo("AliceIRC got unknown clientStatusCode: " + clientStatus, Log.LISTENERS);
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
                        Log.userinfo("AliceIRC got a message from: " + nick + "> " + message, Log.LISTENERS);
                        sendServerMessage("/MSG " + " " + channel + " :" + message);
                        
                        String[] botResponse =
                            Toolkit.breakLines(
                                ActiveMultiplexor.getInstance().getResponse(message, nick+"_IRC", new TextResponder()));
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
        else if (command.equalsIgnoreCase("VERSION"))  
        {
            /*
            String[] versions = // messagerelay.getClassVersions();
            sendMessage(NONE, NONE);
            sendMessage(SIRCMESSAGE, "sIRC Version: " + versions[0]);
            sendMessage(SIRCMESSAGE, "UserInterface: " + versions[1]);
            sendMessage(SIRCMESSAGE, "AliceIRC: " + versions[2]);
            sendMessage(NONE, NONE);
            processed = true;
            */
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
            else if (command.equals("JOIN"))   // - - - - - - - - - - - - - - - - -
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
                    Log.userinfo("AliceIRC: Request: " +targetnick + "> " + gitter, Log.LISTENERS);
                    String[] botResponse =
                        Toolkit.breakLines(
                            ActiveMultiplexor.getInstance().getResponse(gitter, targetnick+"_IRC", new TextResponder()));
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
     *  Please document this.
     */
    private void sendMessage(String type, String message)
    {
        Log.userinfo("AliceIRC: " + type + " " + message, Log.LISTENERS);
    }
    
    
    /**
     *  Please document this.
     */
    private void sendServerMessage(String message)
    {
        if (clientStatus == CONNECTED)
        {
            writer.println(message);
            if(DEBUG)
            {
                Trace.devinfo("AliceIRC: " + message);
            }
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



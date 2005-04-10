package org.aitools.programd.listener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.logging.Level;

import org.aitools.programd.Core;
import org.aitools.programd.bot.Bot;
import org.aitools.programd.interfaces.ShellCommandable;
import org.aitools.programd.responder.TextResponder;
import org.aitools.programd.util.XMLKit;

/**
 * This code is from the sIRC project and was written by Chris Knight <a
 * href="http://www.chrisknight.com/sirc/">http://www.chrisknight.com/sirc/ </a>
 * and modified to work with a Program D server.
 * 
 * @author Chris Knight
 * @author Jon Baer
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @version 4.5
 * @see <a href="http://www.chrisknight.com/sirc/">sIRC </a>
 */

public class IRCListener extends Listener implements ShellCommandable
{
    /** Please document. */
    private static final String VERSION = "0.86.0b";

    /** Please document. */
    private static final String VERDATE = "1999.04.07";

    /** Please document. */
    private static final boolean DEBUG = false;

    /** Please document. */
    private static final int MAXARGC = 16;

    /** Please document. */
    private static final String SERVERPREFIX = "[server]";

    /** Please document. */
    private static final String SIRCMESSAGE = "[irc]";

    /** Please document. */
    protected static final String DEBUGPREFIX = "[debug]";

    /** Please document. */
    private static final String NONE = "";

    /** Please document. */
    private static final byte NOTCONNECTED = 0;

    /** Please document. */
    private static final byte CONNECTING = 1;

    /** Please document. */
    private static final byte CONNECTED = 2;

    /** Please document. */
    private static final byte DISCONNECTING = 3;

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
    private static final String MSG = "ProgramD-IRC: ";

    /** The label (as required by the registration scheme). */
    public static final String label = "ProgramD-IRC";

    /**
     * Creates a new IRCListener chat listener for a given bot.
     * @param coreToUse
     *            the Core object in use
     * @param botToListenFor the bot for whom to listen
     * @param parametersToUse
     *            the parameters for the listener and their default values
     * @throws InvalidListenerParameterException 
     */
    public IRCListener(Core coreToUse, Bot botToListenFor, HashMap<String, String> parametersToUse) throws InvalidListenerParameterException
    {
        super(coreToUse, botToListenFor, parametersToUse);
                /*new String[][]
            {
                { "host", "" } ,
                { "port", "6667" } ,
                { "nick", "" } ,
                { "channel", "" } } );*/
        this.host = this.parameters.get("host");
        try
        {
            this.port = Integer.parseInt(this.parameters.get("port"));
        } 
        catch (NumberFormatException e)
        {
            throw new InvalidListenerParameterException("Invalid port specification (try a number!)");
        } 
        this.nick = this.parameters.get("nick");
        this.channel = this.parameters.get("channel");
    } 

    /**
     * @see org.aitools.programd.listener.Listener#checkParameters()
     */
    public void checkParameters() throws InvalidListenerParameterException
    {
        if (this.host.length() == 0)
        {
            throw new InvalidListenerParameterException("No host specified.");
        } 
        if (this.port <= 0)
        {
            throw new InvalidListenerParameterException("Invalid port.");
        } 
        if (this.nick.length() == 0)
        {
            throw new InvalidListenerParameterException("No nick specified.");
        } 
        if (this.channel.length() == 0)
        {
            throw new InvalidListenerParameterException("No channel specified.");
        } 
    } 

    /**
     * @see org.aitools.programd.util.ManagedProcess#shutdown()
     */
    public void shutdown()
    {
        disconnect();
    } 

    /**
     * Connects to the given host and begins listening.
     */
    public void run()
    {
        logMessage("Starting for \"" + this.botID + "\".");
        processMessageCommandClient("CONNECT", this.host + " " + this.port);
        processMessage("/NICK " + this.nick);
        processMessage("/JOIN " + this.channel);
        listen();
    } 

    /**
     * @see org.aitools.programd.interfaces.ShellCommandable#getShellID()
     */
    public String getShellID()
    {
        return "irc";
    } 

    /**
     * @see org.aitools.programd.interfaces.ShellCommandable#getShellDescription()
     */
    public String getShellDescription()
    {
        return "ProgramD IRC chat listener";
    } 

    /**
     * @see org.aitools.programd.interfaces.ShellCommandable#getShellCommands()
     */
    public String getShellCommands()
    {
        return "Not yet implemented.";
    } 

    /**
     * @see org.aitools.programd.interfaces.ShellCommandable#processShellCommand(java.lang.String)
     */
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
        // (otherwise...)
        processMessageCommand(command.substring(1, space), command.substring(space + 1));
        return;
    } 

    /**
     * Returns the version string.
     * 
     * @return the version string
     */
    public String getVersion()
    {
        return VERSION + " " + VERDATE;
    } 

    /**
     * Please document this.
     */
    private void connect()
    {
        if (this.clientStatus == NOTCONNECTED)
        {
            this.clientStatus = CONNECTING;
            logMessage("Contacting " + this.host + ":" + this.port);

            try
            {
                this.socket = new Socket(this.host, this.port);
                logMessage("Connected to " + this.host + ":" + this.port);

            } 
            catch (UnknownHostException e0)
            {
                logMessage("Cannot connect; unknown server.");
                this.clientStatus = NOTCONNECTED;
            } 
            catch (IOException e1)
            {
                logMessage("Cannot connect; the server is down or not responding.");
                this.clientStatus = NOTCONNECTED;
            } 
            // If we didn't have any problems connecting
            if (this.clientStatus == CONNECTING)
            {
                try
                {
                    this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
                    this.writer = new PrintWriter(this.socket.getOutputStream(), true);
                    this.clientStatus = CONNECTED;
                } 
                catch (IOException e0)
                {
                    logMessage("Cannot connect: I/O error.");
                    this.clientStatus = DISCONNECTING;

                    try
                    {
                        this.socket.close();
                    } 
                    catch (IOException e1)
                    {
                        // !
                    } 
                    finally
                    {
                        this.socket = null;
                        this.clientStatus = NOTCONNECTED;
                    } 
                } 
            } 
        } 
        else
        {
            switch (this.clientStatus)
            {
                case CONNECTED:
                    logMessage("Cannot connect again; already connected.");
                    break;

                case CONNECTING:
                    logMessage("Cannot connect again; already in the process of connecting.");
                    break;

                case DISCONNECTING:
                    logMessage("Cannot connect now; still trying to disconnect.");
                    break;

                default:
                    logMessage("Got unknown clientStatusCode: " + this.clientStatus);
                    break;
            } 
        } 
    } 

    /**
     * Please document this.
     */
    private void disconnect()
    {
        if (this.clientStatus == CONNECTED)
        {
            this.clientStatus = DISCONNECTING;
            try
            {
                this.socket.close();
            } 
            catch (IOException e)
            {
                logMessage("IO exception trying to disconnect.");
            } 
            finally
            {
                this.reader = null;
                this.writer = null;
                logMessage("Connection closed.");
                this.clientStatus = NOTCONNECTED;
            } 
        } 
        else
        {
            switch (this.clientStatus)
            {
                case NOTCONNECTED:
                    logMessage("Cannot close connection; not connected.");
                    break;

                case CONNECTING:
                    logMessage("Cannot close connection; currently trying to connect.");
                    break;

                case DISCONNECTING:
                    logMessage("Cannot close connection; currently trying to close it.");
                    break;

                default:
                    logMessage("Got unknown clientStatusCode: " + this.clientStatus);
                    break;
            } 
        } 
    } 

    /**
     * Processes an IRC message
     * @param message the message to process
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
                    // Please document this.
                } 
                else if (processMessageCommandClient(command, message))
                {
                    // Please document this.
                } 
                else if (processMessageCommandDebug(command, message) && DEBUG)
                {
                    // Please document this.
                } 
                else
                {
                    sendMessage(SIRCMESSAGE, "Unknown Command: " + command);
                } 
            } 
            else
            {
                if (this.clientStatus == CONNECTED)
                {
                    if (this.channel.length() > 0)
                    {
                        sendMessage(NONE, "[" + this.nick + "] " + message);
                        logMessage("Got a message from [" + this.nick + "]: " + message);
                        sendServerMessage("/MSG " + " " + this.channel + " :" + message);

                        String[] response = XMLKit.breakLinesAtTags(this.core.getResponse(message, this.nick
                                + "_IRC", this.botID, new TextResponder()));
                        if (response.length > 0)
                        {
                            for (int line = 0; line < response.length; line++)
                            {
                                processMessage("/PRVMSG " + this.nick + " " + response[line]);
                            } 
                        } 
                    } 
                } 
            } 
        } 
    } 

    /**
     * Processes an IRC command
     * @param command the command to process
     * @param message the message to process as the argument to the command
     * @return the reuslt of processing the command
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
        if (this.clientStatus == CONNECTED)
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
                    sendServerMessage("INVITE " + params + " " + this.channel);
                } 
                processed = true;
            } 
            else if (command.equalsIgnoreCase("KICK"))
            {
                if (params.length() > 0)
                {
                    int firstindex = params.indexOf(' ');
                    int secondindex = params.indexOf(' ', (firstindex + 1));
                    try
                    {
                        sendServerMessage("KICK " + this.channel + " " + params.substring(0, secondindex) + " :"
                                + params.substring((secondindex + 1)));
                    } 
                    catch (StringIndexOutOfBoundsException eMSG)
                    {
                        sendServerMessage("KICK " + this.channel + " " + params);
                    } 
                } 
                processed = true;
            } 
            else if (command.equalsIgnoreCase("LIST"))
            {
                if (params.length() == 0)
                {
                    sendServerMessage("LIST " + this.channel);
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
                    if (this.channel.length() == 0)
                    {
                        sendMessage(SIRCMESSAGE, "You're not in a channel.");
                    } 
                    else
                    {
                        sendMessage(SIRCMESSAGE, "You're currently in: " + this.channel + ".");
                    } 
                } 
                // Join a new channel.
                else if (this.channel.length() == 0)
                {
                    sendServerMessage("JOIN " + params);
                } 
                // Leave the channel.
                else if (params.equals("0"))
                {
                    sendServerMessage("PART " + this.channel);
                } 
                // Change channels.
                else
                {
                    sendServerMessage("PART " + this.channel);
                    sendServerMessage("JOIN " + params);
                } 
                processed = true;
            } 
            else if (command.equalsIgnoreCase("MODE"))
            {
                if (params.length() > 0)
                {
                    sendServerMessage("MODE " + this.channel + " " + params);
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
                        sendServerMessage("PRIVMSG " + params.substring(0, paramsindex) + " :"
                                + params.substring((paramsindex + 1)));
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
                    sendServerMessage("NAMES " + this.channel);
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

                this.channel = NONE;
                disconnect();
                processed = true;
            } 
            else if (command.equalsIgnoreCase("TOPIC"))
            {
                if (this.channel.length() == 0)
                {
                    sendMessage(SIRCMESSAGE, "You must be in a channel to set the topic!");
                } 
                else
                {
                    if (params.length() == 0)
                    {
                        sendServerMessage("TOPIC " + this.channel);
                    } 
                    else
                    {
                        sendServerMessage("TOPIC " + this.channel + " :" + params);
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
     * Processes a client-side IRC message (?).
     * @param command the command to process
     * @param message the message argument to the command
     * @return whether the processing was successful
     */
    private boolean processMessageCommandClient(String command, String message)
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
                index[argc] = message.indexOf(" ", (index[argc - 1] + 1));
            } 
        } while (index[argc] != -1);

        // note: we have one extra in index[] at this point (a '-1')

        String args[] = new String[MAXARGC];

        for (int x = 0; x < argc; x++)
        {
            if ((x + 1) >= argc)
            {
                args[x] = message.substring(index[x] + 1);
            } 
            else
            {
                args[x] = message.substring(index[x] + 1, index[x + 1]);
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

                if (this.clientStatus == CONNECTED)
                {
                    sendServerMessage("USER " + this.nick + " " + this.socket.getInetAddress().getHostName()
                            + " server :" + this.nick);
                    sendServerMessage("NICK " + this.nick);
                } 
            } 
            else
            {
                try
                {
                    connect();

                    if (this.clientStatus == CONNECTED)
                    {
                        sendServerMessage("USER " + this.nick + " " + this.socket.getInetAddress().getHostName()
                                + " server :" + this.nick);
                        sendServerMessage("NICK " + this.nick);
                    } 
                } 
                catch (NumberFormatException e)
                {
                    sendMessage(SIRCMESSAGE, "The Port you specified is invalid.");
                } 
            } 
            processed = true;
        } 
        else if (command.equalsIgnoreCase("EXIT"))
        {
            if (this.clientStatus == CONNECTED)
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
     * Handle an IRC debugging command (?).
     * @param command the command to process
     * @param message the message argument to the command
     * @return whether or not the command was processed successfully 
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
                index[argc] = message.indexOf(" ", (index[argc - 1] + 1));
            } 
        } while (index[argc] != -1);

        String args[] = new String[MAXARGC];

        for (int x = 0; x < argc; x++)
        {
            if ((x + 1) >= argc)
            {
                args[x] = message.substring(index[x] + 1);
            } 
            else
            {
                args[x] = message.substring(index[x] + 1, index[x + 1]);
            } 
        } 

        if (command.equals("testargs"))
        {
            StringBuffer teststring = new StringBuffer("Test Arguments; argc=" + argc + " command=" + command);

            for (int x = 0; x < argc; x++)
            {
                teststring.append(" [" + (x + 1) + ";" + args[x] + "]");
            } 

            this.logger.log(Level.FINEST, teststring.toString());
            processed = true;
        } 
        else if (command.equals("debug"))
        {
            this.logger.log(Level.FINEST, "- - - - - - - - - - - - - - - - - - - - ");
            this.logger.log(Level.FINEST, "clientStatus=" + this.clientStatus);
            this.logger.log(Level.FINEST, "socket=" + this.socket);
            this.logger.log(Level.FINEST, "reader=" + this.reader);
            this.logger.log(Level.FINEST, "writer=" + this.writer);
            this.logger.log(Level.FINEST, "- - - - - - - - - - - - - - - - - - - - ");
            processed = true;
        } 
        else if (command.equals("raw"))
        {
            String messagex = NONE;

            for (int x = 0; x < argc; x++)
            {
                messagex = messagex + args[x] + " ";
            } 

            sendServerMessage(messagex);
            processed = true;
        } 

        return processed;

    } 

    /**
     * Processes an IRC server message
     * @param message the message to process
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
            this.logger.log(Level.FINEST, message);
        } 

        // We have a Prefix
        if (message.charAt(0) == ':')
        {
            int firstspace = message.indexOf(' ');
            int secondspace = message.indexOf(' ', (firstspace + 1));
            prefix = message.substring(0, firstspace);
            command = message.substring((firstspace + 1), secondspace);
            params = message.substring((secondspace + 1));
        } 
        // We've got no Prefix
        else
        {
            int firstspace = message.indexOf(' ');
            command = message.substring(0, firstspace);
            params = message.substring((firstspace + 1));
        } 

        if (prefix.length() > 0)
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
            switch (Integer.parseInt(command))
            {
                case 001:
                    break;

                case 321:
                    break;

                case 322:
                {
                    int firstSpaceIndex = params.indexOf(' ');
                    int secondSpaceIndex = params.indexOf(' ', (firstSpaceIndex + 1));
                    int colonIndex = params.indexOf(':');
                    sendMessage(SERVERPREFIX, params.substring((firstSpaceIndex + 1), secondSpaceIndex) + ": "
                            + params.substring((secondSpaceIndex + 1), (colonIndex - 1)) + " "
                            + params.substring((colonIndex + 1)));
                } 
                    break;

                case 353:
                {
                    int colonIndex = params.indexOf(':');
                    int equalsIndex = params.indexOf('=');
                    sendMessage(SERVERPREFIX, "Users on " + params.substring((equalsIndex + 2), (colonIndex - 1))
                            + ": " + params.substring((colonIndex + 1)));
                } 
                    break;

                case 372:
                    sendMessage(SERVERPREFIX, params.substring((params.indexOf(':') + 1)));
                    break;

                default:
                    sendMessage(SERVERPREFIX, "(" + command + ") " + params.substring((params.indexOf(':') + 1)));
                    break;
            } 
        } 
        catch (NumberFormatException e)
        {
            if (command.equals("INVITE"))
            {
                int firstindex = params.indexOf(' ');
                sendMessage(SERVERPREFIX, targetnick + " has invited you to "
                        + helpExtractIRCString(params.substring((firstindex + 1))) + ".");
            } 
            else if (command.equals("JOIN"))
            {
                String channelx = helpExtractIRCString(params);

                if (targetnick.equals(this.nick))
                {
                    sendMessage(SERVERPREFIX, "You're now on " + channelx + ".");
                    this.channel = channelx;
                } 
                else
                {
                    sendMessage(SERVERPREFIX, targetnick + " has joined the channel.");
                } 
            } 
            else if (command.equals("KICK"))
            {
                String kickchannel = NONE;
                String kickuser = NONE;
                String kickcomment = NONE;

                int firstindex = params.indexOf(' ');
                int secondindex = params.indexOf(' ', (firstindex + 1));

                try
                {
                    kickchannel = params.substring(0, firstindex);
                    kickuser = params.substring((firstindex + 1), secondindex);
                    kickcomment = helpExtractIRCString(params.substring((secondindex + 1)));

                    if (kickuser.equals(this.nick))
                    {
                        sendMessage(SERVERPREFIX, "You've just been kicked off " + kickchannel + " by " + targetnick
                                + " (" + kickcomment + ").");
                        this.channel = NONE;
                    } 
                    else
                    {
                        sendMessage(SERVERPREFIX, kickuser + " has been kicked off " + kickchannel + " by "
                                + targetnick + " (" + kickcomment + ").");
                    } 
                } 
                catch (StringIndexOutOfBoundsException eKICK)
                {
                    kickchannel = params.substring(0, firstindex);
                    kickuser = params.substring((firstindex + 1));

                    if (kickuser.equals(this.nick))
                    {
                        sendMessage(SERVERPREFIX, "You've just been kicked off " + kickchannel + " by " + targetnick
                                + ".");
                        this.channel = NONE;
                    } 
                    else
                    {
                        sendMessage(SERVERPREFIX, targetnick + " has been kicked off " + kickchannel + " by "
                                + targetnick + ".");
                    } 
                } 
            } 
            else if (command.equals("NICK"))
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
            else if (command.equals("PART"))
            {
                if (targetnick.equals(this.nick))
                {
                    sendMessage(SERVERPREFIX, "You've just left " + params + ".");
                    this.channel = NONE;
                } 
                else
                {
                    sendMessage(SERVERPREFIX, targetnick + " has left the channel.");
                } 
            } 
            else if (command.equals("PING"))
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

                    String[] response = XMLKit.breakLinesAtTags(this.core.getResponse(gitter, targetnick + "_IRC",
                            this.botID, new TextResponder()));
                    if (response.length > 0)
                    {
                        for (int line = 0; line < response.length; line++)
                        {
                            processMessage("/MSG " + targetnick + " " + response[line]);
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
                if (targetnick.equals(this.nick))
                {
                    sendMessage(SERVERPREFIX, "The topic is now: " + params.substring((params.indexOf(':') + 1)));
                } 
                else
                {
                    sendMessage(SERVERPREFIX, targetnick + " has set the topic to: "
                            + helpExtractIRCString(params.substring((params.indexOf(' ') + 1))));
                } 
            } 
            else
            {
                // Please document this.
            } 
        } 
    } 

    /**
     * A helper function for the IRCListener.
     * @param string any input string
     * @return the part of the string following the first <code>:</code> (colon), or the whole string if there is no colon.
     */
    private String helpExtractIRCString(String string)
    {
        try
        {
            if (string.charAt(0) == ':')
            {
                return string.substring(1);
            } 
            // (otherwise...)
            return string;

        } 
        catch (StringIndexOutOfBoundsException e)
        {
            return NONE;
        } 
    } 

    /**
     * Standard method for logging and notifying of a message.
     * 
     * @param message
     *            the message
     */
    private void logMessage(String message)
    {
        this.logger.log(Level.INFO, MSG + message);
    } 

    /**
     * Sends an IRC message
     * @param type the type of message to send
     * @param message the message
     */
    private void sendMessage(String type, String message)
    {
        this.logger.log(Level.INFO, MSG + type + ' ' + message);
    } 

    /**
     * Sends an IRC server message
     * @param message the message to send
     */
    private void sendServerMessage(String message)
    {
        if (this.clientStatus == CONNECTED)
        {
            this.writer.println(message);
        } 
    } 

    /**
     * Please document this.
     */
    private void listen()
    {
        String line;

        while (this.clientStatus == CONNECTED)
        {
            try
            {
                line = this.reader.readLine();
                processServerMessage(line);
            } 
            catch (IOException e)
            {
                // Please document this.
            } 
        } 

    } 
}
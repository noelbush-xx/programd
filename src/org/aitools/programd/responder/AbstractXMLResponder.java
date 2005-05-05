/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.responder;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.w3c.dom.Document;

import org.aitools.programd.bot.Bot;
import org.aitools.programd.processor.ProcessorException;
import org.aitools.programd.responder.xml.XMLTemplateParser;
import org.aitools.programd.util.UserError;

/**
 * Contains common methods of template parsing and processing that are generic
 * for all responders that deal with markup output, such as
 * {@link HTMLResponder} ,{@link FlashResponder} , etc.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
abstract public class AbstractXMLResponder implements Responder
{
    /** The manager responsible for this responder. */
    protected AbstractXMLResponderManager manager;

    /** The template to use. */
    protected Document template;

    /** The original user input. */
    protected String userInput;

    /** The individual user input sentences. */
    protected ArrayList<String> userInputSentences = new ArrayList<String>();

    /** The individual bot replies. */
    protected ArrayList<String> botReplies = new ArrayList<String>();

    /** The complete bot response. */
    protected String botResponse;

    /** The id of the bot that is handling this. */
    protected String botid;

    /** The bot that is handling this. */
    protected Bot bot;

    /** The logger to use. */
    protected static Logger logger = Logger.getLogger("programd");

    private static final String EMPTY_STRING = "";

    /**
     * Initializes an AbstractXMLResponder.
     * 
     * @param respnsibleManager the manager that is responsible for this
     *            responder
     * @param botidToRespondFor the botid to respond for
     * @param templateName the template name to use
     */
    public AbstractXMLResponder(AbstractXMLResponderManager respnsibleManager,
            String botidToRespondFor, String templateName)
    {
        if (templateName == null || templateName.equals(EMPTY_STRING))
        {
            initialize(respnsibleManager, botidToRespondFor, respnsibleManager
                    .getDefaultTemplateName());
        }
        else
        {
            initialize(respnsibleManager, botidToRespondFor, templateName);
        }
    }

    private void initialize(AbstractXMLResponderManager respnsibleManager,
            String botidToRespondFor, String templateName)
    {
        this.botid = botidToRespondFor;
        this.manager = respnsibleManager;
        this.bot = this.manager.getBots().getBot(botidToRespondFor);
        this.template = this.manager.getTemplate(templateName);
    }

    /**
     * Returns the input without any modification.
     * 
     * @see org.aitools.programd.responder.Responder#preprocess(String)
     */
    public String preprocess(String input)
    {
        this.userInput = input;
        return input;
    }

    /**
     * Accumulates the reply in an internal ArrayList, and returns it appended
     * to the <code>appendTo</code> argument.
     * 
     * @see org.aitools.programd.responder.Responder#append(String, String,
     *      String)
     */
    public String append(String input, String reply, String appendTo)
    {
        this.userInputSentences.add(input);
        this.botReplies.add(reply);
        return appendTo + ' ' + reply;
    }

    /**
     * Inserts the user input, bot replies and total response into the chat
     * template, also processing any other special tags in the template, and
     * returns the result.
     * 
     * @see org.aitools.programd.responder.Responder#postprocess(java.lang.String)
     */
    public String postprocess(String finalBotResponse)
    {
        this.botResponse = finalBotResponse;
        return process(this.template);
    }

    /**
     * @param templateToProcess the template to process
     * @return the result of processing the given template
     */
    public String process(Document templateToProcess)
    {
        try
        {
            return new XMLTemplateParser(this.manager.getProcessorRegistry(), this, this.manager
                    .convertHTMLLineBreakers(), this.manager.stripMarkup())
                    .evaluate(templateToProcess);
        }
        catch (ProcessorException e)
        {
            throw new UserError("Processor exception occurred while processing XML template.", e);
        }
    }

    /**
     * @return the original user input.
     */
    public String getUserInput()
    {
        return this.userInput;
    }

    /**
     * @return the individual user input sentences.
     */
    public ArrayList<String> getUserInputSentences()
    {
        return this.userInputSentences;
    }

    /**
     * @return the individual bot replies.
     */
    public ArrayList<String> getBotReplies()
    {
        return this.botReplies;
    }

    /**
     * @return the complete bot response.
     */
    public String getBotResponse()
    {
        return this.botResponse;
    }

    /**
     * @return the id of the bot that is handling this.
     */
    public String getBotID()
    {
        return this.botid;
    }

    /**
     * @return the bot that is handling this.
     */
    public Bot getBot()
    {
        return this.bot;
    }

    /**
     * @return the manager
     */
    public AbstractXMLResponderManager getManager()
    {
        return this.manager;
    }
}
/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.responder;

import javax.servlet.ServletRequest;

import org.aitools.programd.Core;
import org.aitools.programd.CoreSettings;
import org.aitools.programd.server.ServletRequestTransactionEnvelope;
import org.aitools.programd.util.FileManager;
import org.aitools.programd.util.SuffixFilenameFilter;

/**
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @version 4.5
 */
public class FlashResponderManager extends AbstractXMLResponderManager implements ServletRequestResponderManager
{
    /** The settings to use. */
    private FlashResponderSettings settings;

    /** A filename filter for finding html templates. */
    private final static SuffixFilenameFilter FILENAME_FILTER = new SuffixFilenameFilter(new String[] { ".flash", ".data", ".xml" });

    // Convenience constants.

    /** The string "flash". */
    private static String FLASH = "flash";

    /** The string &quot;text/plain&quot. */
    private static final String TEXT_PLAIN = "text/plain";

    /**
     * Creates a new <code>FlashResponderManager</code>, reading in templates
     * and preparing them for use by <code>FlashResponders</code>.
     * 
     * @param coreToUse the Core from which to read some items
     */
    public FlashResponderManager(Core coreToUse)
    {
        super(coreToUse);
        CoreSettings coreSettings = this.core.getSettings();
        this.settings = new FlashResponderSettings(coreSettings.getConfLocationFlashResponder());
        setConvertHTMLLineBreakers(this.settings.convertHtmlLineBreakers());
        setStripMarkup(this.settings.stripMarkup());
        setDefaultTemplateName(this.settings.getChatDefaultTemplateName());
        // Scan and register other templates.
        registerTemplates(FileManager.getExistingFile(this.settings.getTemplateDirectory()).getAbsolutePath(), FILENAME_FILTER);
    }

    /**
     * This will return true if <code>request</code> contains a parameter with
     * the name &quot;flash&quot;.
     * 
     * @see ServletRequestResponderManager#responderHandles(ServletRequest)
     */
    public boolean responderHandles(ServletRequest request)
    {
        return (request.getParameter(FLASH) != null);
    }

    /**
     * @see ServletRequestResponderManager#getResponseFor(ServletRequestTransactionEnvelope)
     */
    public String getResponseFor(ServletRequestTransactionEnvelope envelope)
    {
        envelope.getServiceResponse().setContentType(TEXT_PLAIN);
        return envelope.getCore().getResponse(envelope.getUserRequest(), envelope.getUserID(), envelope.getBotID(),
                new FlashResponder(this, envelope));
    }
}
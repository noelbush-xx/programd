/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.responder;

import java.io.File;
import java.util.HashMap;

import org.aitools.programd.Core;
import org.aitools.programd.CoreSettings;
import org.aitools.programd.util.SuffixFilenameFilter;

/**
 * Implements a {@link Responder} for a Flash client.
 * 
 * @author Chris Fahey
 */
public class FlashResponder extends AbstractMarkupResponder
{
    private CoreSettings coreSettings;
    
    /** The settings to use. */
    private static FlashResponderSettings settings;
    
    /** Location of flash templates. */
    private static String templatesDirectoryName;

    /** Path to the default chat template. */
    private static String chatTemplatePath;

    /** A filename filter for finding html templates. */
    private static final SuffixFilenameFilter flashFilenameFilter = new SuffixFilenameFilter(new String[]
        { ".flash", ".data" } );

    /** Map of template names to filenames. */
    private static HashMap templates;

    /**
     * Creates a new FlashResponder.
     * @param botidToRespondFor the botid to respond for
     * @param templateName the template name to use
     * @param coreToUse the Core to use
     */
    public FlashResponder(String botidToRespondFor, String templateName, Core coreToUse)
    {
        super(botidToRespondFor, coreToUse);
        checkStaticVariables();
        if (templateName.equals(EMPTY_STRING))
        {
            parseTemplate(chatTemplatePath);
        } 
        else
        {
            // Otherwise, try to find the named template.
            String templateFileName = (String) templates.get(templateName);
            if (templateFileName != null)
            {
                parseTemplate(templateFileName);
            } 
            else
            {
                parseTemplate(chatTemplatePath);
            } 
        } 
    }
    
    private void checkStaticVariables()
    {
        if (settings == null)
        {
            settings = new FlashResponderSettings(this.core.getSettings().getConfLocationHtmlResponder());
        }
        if(templatesDirectoryName == null)
        {
            templatesDirectoryName = this.coreSettings.getRootDirectory() + File.separator +
                                      settings.getTemplateDirectory() + File.separator + "html";
        }
        if(chatTemplatePath == null)
        {
            chatTemplatePath = templatesDirectoryName + File.separator +
            settings.getChatDefaultTemplatePath();
        }
        if (templates == null)
        {
            // Scan and register other templates.
            templates = registerTemplates(templatesDirectoryName, flashFilenameFilter);
        }
    }
}
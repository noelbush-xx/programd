/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
 
package org.aitools.programd.responder;

import org.aitools.programd.util.Settings;

/**
 * Automatically generated from properties file, 2005-03-16T12:01:58.697-04:00
 */
public class FlashResponderSettings extends Settings
{
    /**
     * The flash templates directory (relative to programd.home). 
     */
    String templateDirectory;

    /**
     * The default chat template. 
     * Note: Any other *.flash or *.data files in
           programd.responder.flash.template.directory will also be available if
           you specify a template">name (without suffixes) parameter in the user request.
     */
    String chatDefaultTemplatePath;

    /**
     * Creates a <code>FlashResponderSettings</code> using default property values.
     */
    public FlashResponderSettings()
    {
        super();
    }
    
    /**
     * Creates a <code>FlashResponderSettings</code> with the (XML-formatted) properties
     * located at the given path.
     */
    public FlashResponderSettings(String propertiesPath)
    {
        super(propertiesPath);
    }

    /**
    * Initializes the Settings with values from properties, or defaults.
    */
    protected void initialize()
    {
        setTemplateDirectory(this.properties.getProperty("programd.responder.flash.template.directory", "templates/flash"));

        setChatDefaultTemplatePath(this.properties.getProperty("programd.responder.flash.chat-default.template.path", "chat.flash"));

    }

    /**
     * @return the value of templateDirectory
     */
    public String getTemplateDirectory()
    {
        return this.templateDirectory;
    }

    /**
     * @return the value of chatDefaultTemplatePath
     */
    public String getChatDefaultTemplatePath()
    {
        return this.chatDefaultTemplatePath;
    }

    /**
     * @param templateDirectoryToSet   the value to which to set templateDirectory
     */
    public void setTemplateDirectory(String templateDirectoryToSet)
    {
        this.templateDirectory = templateDirectoryToSet;
    }

    /**
     * @param chatDefaultTemplatePathToSet   the value to which to set chatDefaultTemplatePath
     */
    public void setChatDefaultTemplatePath(String chatDefaultTemplatePathToSet)
    {
        this.chatDefaultTemplatePath = chatDefaultTemplatePathToSet;
    }

}
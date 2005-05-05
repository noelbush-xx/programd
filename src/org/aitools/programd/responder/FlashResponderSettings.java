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
 * Automatically generated from properties file, 2005-05-04T22:27:28.475-04:00
 */
public class FlashResponderSettings extends Settings
{
    /**
     *The flash templates directory (relative to programd.home). 
     */
    private String templateDirectory;

    /**
     *The default chat template name. 
    * Note: Any other *.flash or *.data files in
          programd.responder.flash.template.directory will also be available if
          you specify a template-name (without suffixes) parameter in the user request.
     */
    private String chatDefaultTemplateName;

    /**
     *Convert HTML line-breaking elements (br and p) to line breaks. 
     */
    private boolean convertHtmlLineBreakers;

    /**
     *Strip out all markup (such as HTML). 
     */
    private boolean stripMarkup;

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
     *
     * @param propertiesPath the path to the configuration file
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

        setChatDefaultTemplateName(this.properties.getProperty("programd.responder.flash.chat-default.template.name", "chat"));

        setConvertHtmlLineBreakers(Boolean.valueOf(this.properties.getProperty("programd.responder.flash.convert-html-line-breakers", "true")).booleanValue());

        setStripMarkup(Boolean.valueOf(this.properties.getProperty("programd.responder.flash.strip-markup", "true")).booleanValue());

    }

    /**
     * @return the value of templateDirectory
     */
    public String getTemplateDirectory()
    {
        return this.templateDirectory;
    }

    /**
     * @return the value of chatDefaultTemplateName
     */
    public String getChatDefaultTemplateName()
    {
        return this.chatDefaultTemplateName;
    }

    /**
     * @return the value of convertHtmlLineBreakers
     */
    public boolean convertHtmlLineBreakers()
    {
        return this.convertHtmlLineBreakers;
    }

    /**
     * @return the value of stripMarkup
     */
    public boolean stripMarkup()
    {
        return this.stripMarkup;
    }

    /**
     * @param templateDirectoryToSet   the value to which to set templateDirectory
     */
    public void setTemplateDirectory(String templateDirectoryToSet)
    {
        this.templateDirectory = templateDirectoryToSet;
    }

    /**
     * @param chatDefaultTemplateNameToSet   the value to which to set chatDefaultTemplateName
     */
    public void setChatDefaultTemplateName(String chatDefaultTemplateNameToSet)
    {
        this.chatDefaultTemplateName = chatDefaultTemplateNameToSet;
    }

    /**
     * @param convertHtmlLineBreakersToSet   the value to which to set convertHtmlLineBreakers
     */
    public void setConvertHtmlLineBreakers(boolean convertHtmlLineBreakersToSet)
    {
        this.convertHtmlLineBreakers = convertHtmlLineBreakersToSet;
    }

    /**
     * @param stripMarkupToSet   the value to which to set stripMarkup
     */
    public void setStripMarkup(boolean stripMarkupToSet)
    {
        this.stripMarkup = stripMarkupToSet;
    }

}
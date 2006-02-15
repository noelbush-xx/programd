/*
 * This program is copyright 2005 X-31 LLC.  It may not
 * be reproduced, distributed, or used without express
 * permission of the copyright holder.  All rights reserved.
 */

package org.aitools.programd.server.servlet;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.aitools.programd.Core;
import org.aitools.programd.util.URLTools;

/**
 * @author <a href="mailto:noel@x-31.com">Noel Bush</a>
 */
public class ProgramDContextListener implements ServletContextListener
{
    public static final String KEY_CORE = "core";
    
    public static final String PARAM_CORE_CONFIG = "programd-core-config";
    
    private ServletContext context = null;

    /**
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent sce)
    {
        this.context = sce.getServletContext();

        this.context.log("Configuring Program D Core from servlet context listener.");
        
        // Check for the config parameter.
        String config = this.context.getInitParameter(PARAM_CORE_CONFIG);
        if (config == null || config.length() == 0)
        {
            this.context.log("No \"" + PARAM_CORE_CONFIG + "\" init-param specified for Program D.  Cannot continue.");
            return;
        }
        
        // Create the base URL.
        URL baseURL;
        try
        {
            baseURL = this.context.getResource("/");
        }
        catch (MalformedURLException e)
        {
            this.context.log("Error when getting base URL!", e);
            return;
        }

        // Set up the Program D Core.
        Core core = new Core(baseURL, URLTools.contextualize(baseURL, config));
        this.context.setAttribute(KEY_CORE, core);
    }

    /**
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent sce)
    {
        this.context = sce.getServletContext();
        if (this.context != null)
        {
            this.context.removeAttribute(KEY_CORE);
        }
    }
}

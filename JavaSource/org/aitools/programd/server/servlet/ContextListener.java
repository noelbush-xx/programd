/*
 * This program is copyright 2005 X-31 LLC.  It may not
 * be reproduced, distributed, or used without express
 * permission of the copyright holder.  All rights reserved.
 */

package org.aitools.programd.server.servlet;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.aitools.programd.Core;
import org.aitools.programd.util.URITools;

/**
 * @author <a href="mailto:noel@x-31.com">Noel Bush</a>
 */
public class ContextListener implements ServletContextListener
{
    private ServletContext context = null;

    /**
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent sce)
    {
        this.context = sce.getServletContext();

        Logger logger = Logger.getLogger("programd");
        logger.log(Level.INFO, "Configuring Program D Core from servlet context listener.");
        
        // Check for the config parameter.
        String config = this.context.getInitParameter("config");
        if (config == null || config.length() == 0)
        {
            logger.log(Level.SEVERE, "No config init-param specified for Program D.  Cannot continue.");
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
            logger.log(Level.INFO, "Error when getting base URL!", e);
            return;
        }

        // Set up the Program D Core.
        Core core = new Core(baseURL, URITools.contextualize(baseURL, config));
        core.setup();
        core.start();
        this.context.setAttribute("core", core);
    }

    /**
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent sce)
    {
        this.context = sce.getServletContext();
        if (this.context != null)
        {
            this.context.removeAttribute("core");
        }
    }
}

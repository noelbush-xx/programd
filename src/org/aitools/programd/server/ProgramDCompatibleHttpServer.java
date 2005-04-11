/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.server;

/**
 * Specifies the methods that an http server must implement in order to be
 * compatible with Program D.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public interface ProgramDCompatibleHttpServer
{
    /**
     * Configures the http server given the path or URL to a configuration file.
     * 
     * @param configParameters parameters used in configuring the server
     */
    public void configure(Object... configParameters);

    /**
     * Starts the http server.
     */
    public void run();

    /**
     * Shuts down the http server.
     */
    public void shutdown();

    /**
     * @return the port on which the server is listening.
     */
    public int getHttpPort();
}
/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.server;

import javax.servlet.ServletRequest;

import org.aitools.programd.Core;
import org.aitools.programd.responder.NoResponderHandlesThisException;
import org.aitools.programd.responder.ServletRequestResponderManager;
import org.aitools.programd.util.ObjectExemplarRegistry;

/**
 * Registers {@link ServletRequestResponderManager}s.
 * 
 * @since 4.5
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class ServletRequestResponderManagerRegistry extends ObjectExemplarRegistry<ServletRequestResponderManager>
{
    /**
     * The list of ServletRequestResponderManagers (fully-qualified class
     * names).
     */
    private static final String[] MANAGER_LIST = { "org.aitools.programd.responder.FlashResponderManager",
            "org.aitools.programd.responder.HTMLResponderManager" };

    /**
     * Creates a <code>ServletRequestResponderManagerRegistry</code>.
     * 
     * @param core the Core to pass with the constructors of the responder
     *            managers
     * @see ObjectExemplarRegistry
     */
    public ServletRequestResponderManagerRegistry(Core core)
    {
        super(MANAGER_LIST, ServletRequestResponderManager.class, core);
    }

    /**
     * Returns a registered responder manager that handles the given
     * ServletRequest. If multiple registered responder managers indicate that
     * their responder can handle the request, there is no guarantee about which
     * one will be returned!
     * 
     * @param request the request to test
     * @return the registered responder that handles the request
     * @throws NoResponderHandlesThisException if no responder could be found
     *             that handles the request
     */
    public ServletRequestResponderManager getHandlerFor(ServletRequest request) throws NoResponderHandlesThisException
    {
        for (ServletRequestResponderManager manager : this.registry.values())
        {
            if (manager.responderHandles(request))
            {
                return manager;
            }
        }
        throw new NoResponderHandlesThisException(request);
    }
}
/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.graph;

import org.aitools.programd.util.ClassUtils;
import org.aitools.programd.util.UserError;

/**
 * Produces {@link Nodemapper}s based on the classname that
 * is configured in the CoreSettings.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @version 4.6
 */
public class NodemapperFactory
{
    /** The classname of the implementation of Nodemapper that will be produced. */
    private String nodemapperClassname;
    
    /** The actual implementation of Nodemapper to create. */
    private Class<? extends Nodemapper> nodemapperClass;
    
    /** An empty argument set to be passed when constructing a Nodemapper. */
    private static final Object[] args = new Object[]{};
    
    /**
     * Creates a new <code>NodemapperFactory</code>
     * that is configured to create instances of the {@link Nodemapper}
     * subclass <code>classname</code>.
     * 
     * @param classname
     */
    @SuppressWarnings("unchecked")
    public NodemapperFactory(String classname)
    {
        this.nodemapperClassname = classname;
        try
        {
            this.nodemapperClass = (Class<? extends Nodemapper>) Class.forName(classname);
        }
        catch (ClassNotFoundException e)
        {
            throw new UserError(String.format("Could not find Nodemapper implementation \"%s\".", classname), e);
        }
        catch (ClassCastException e)
        {
            throw new UserError(String.format("\"%s\" is not an implementation of Nodemapper.", classname), e);
        }
    }
    
    /**
     * Returns a new Nodemapper.
     * 
     * @return a new Nodemapper
     */
    public Nodemapper getNodemapper()
    {
        return ClassUtils.getNewInstance(this.nodemapperClass, this.nodemapperClassname, args);
    }
}
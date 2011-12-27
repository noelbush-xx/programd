/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.util.xml;

import org.apache.xerces.util.XMLCatalogResolver;


/**
 * Code to handle creating and otherwise dealing with XML entity resolvers.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class Resolvers {

  /**
   * Produce a new XML catalog resolver that reads a catalog from the given path.
   * 
   * @param catalogPath
   * @return the new resolver
   */
  public static XMLCatalogResolver newXMLCatalogResolver(String catalogPath) {
    XMLCatalogResolver resolver = new XMLCatalogResolver();
    resolver.setPreferPublic(false);
    resolver.setCatalogList(new String[]{catalogPath});
    return resolver;
  }
  
}

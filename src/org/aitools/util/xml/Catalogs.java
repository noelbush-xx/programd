/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.util.xml;

import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;


/**
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 *
 */
public class Catalogs {
  
  /**
   * @param catalogPath
   * @return a resolver that uses the catalog at the given path
   */
  public static CatalogResolver getResolver(String catalogPath) {
    CatalogManager catalogManager = new CatalogManager("XMLResolver.properties");
    catalogManager.setCatalogFiles(catalogPath);
    return new CatalogResolver(catalogManager);
  }
}

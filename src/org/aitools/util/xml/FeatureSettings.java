/*
 * aitools utilities
 * Copyright (C) 2006 Noel Bush
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.aitools.util.xml;


/**
 * Contains common settings for XML parser features.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 *
 */
public class FeatureSettings {
  
  /** Value for http://xml.org/sax/features/use-entity-resolver2 */
  private boolean useEntityResolver2 = false;
      
  /** Value for http://xml.org/sax/features/validation */
  private boolean useValidation = false;
      
  /** Value for http://apache.org/xml/features/validation/schema */
  private boolean useSchemaValidation = false;
      
  /** Value for http://apache.org/xml/features/honour-all-schemaLocations */
  private boolean honourAllSchemaLocations = false;
      
  /** Value for http://apache.org/xml/features/xinclude */
  private boolean useXInclude = false;
      
  /** Value for http://apache.org/xml/features/validate-annotations */
  private boolean validateAnnotations = false;
      

  /**
   * @return whether to use an EntityResolver2
   */
  public boolean useEntityResolver2()
  {
      return this.useEntityResolver2;
  }

  /**
   * @return whether to use validation
   */
  public boolean useValidation()
  {
      return this.useValidation;
  }

  /**
   * @return whether to use schema validation
   */
  public boolean schemaValidation()
  {
      return this.useSchemaValidation;
  }

  /**
   * @return whether to honor all schema locations
   */
  public boolean honourAllSchemaLocations()
  {
      return this.honourAllSchemaLocations;
  }

  /**
   * @return whether to use XInclude
   */
  public boolean useXInclude()
  {
      return this.useXInclude;
  }

  /**
   * @return whether to validate annotations
   */
  public boolean validateAnnotations()
  {
      return this.validateAnnotations;
  }

  /**
   * @param value the value for UseEntityResolver2
   */
  public void setUseEntityResolver2(boolean value)
  {
      this.useEntityResolver2 = value;
  }

  /**
   * @param value the value for UseValidation
   */
  public void setUseValidation(boolean value)
  {
      this.useValidation = value;
  }

  /**
   * @param value the value for UseSchemaValidation
   */
  public void setUseSchemaValidation(boolean value)
  {
      this.useSchemaValidation = value;
  }

  /**
   * @param value the value for HonourAllSchemaLocations
   */
  public void setHonourAllSchemaLocations(boolean value)
  {
      this.honourAllSchemaLocations = value;
  }

  /**
   * @param value the value for UseXInclude
   */
  public void setUseXInclude(boolean value)
  {
      this.useXInclude = value;
  }

  /**
   * @param value the value for ValidateAnnotations
   */
  public void setValidateAnnotations(boolean value)
  {
      this.validateAnnotations = value;
  }
}

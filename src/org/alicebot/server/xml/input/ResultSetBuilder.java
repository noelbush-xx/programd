/*-- 

 Copyright 2000 Brett McLaughlin & Jason Hunter. All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:
 
 1. Redistributions of source code must retain the above copyright notice,
    this list of conditions, and the following disclaimer.
 
 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions, the disclaimer that follows these conditions,
    and/or other materials provided with the distribution.
 
 3. The names "JDOM" and "Java Document Object Model" must not be used to
    endorse or promote products derived from this software without prior
    written permission. For written permission, please contact
    license@jdom.org.
 
 4. Products derived from this software may not be called "JDOM", nor may
    "JDOM" appear in their name, without prior written permission from the
    JDOM Project Management (pm@jdom.org).
 
 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 JDOM PROJECT  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT, INDIRECT, 
 INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLUDING, BUT 
 NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Java Document Object Model Project and was originally 
 created by Brett McLaughlin <brett@jdom.org> and 
 Jason Hunter <jhunter@jdom.org>. For more  information on the JDOM 
 Project, please see <http://www.jdom.org/>.
 
 */

package org.alicebot.server.xml.input;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.alicebot.server.xml.*;

/**
 * <p><code>ResultSetBuilder</code> builds a JDOM tree from a 
 * <code>java.sql.ResultSet</code>.  Many good ideas were leveraged from
 * SQLBuilder written from Jon Baer.</p>
 *
 * Notes:
 *   Uses name returned by rsmd.getColumnName(), not getColumnLabel() 
 *   because that is less likely to be a valid XML element title.
 *   Null values are given empty bodies.  Be aware that databases may 
 *   change the case of column names.  setAsXXX() methods are case
 *   insensitive on input column name.  Assign each one a proper output name
 *   if you're worried.  Only build() throws JDOMException.  Any exceptions 
 *   encountered in the set methods are thrown during the build().  
 *   The setAsXXX(String columnName, ...) methods do not verify that a column 
 *   with the given name actually exists.
 *   Still needs method-by-method Javadocs.
 * <p>
 * Issues: 
 *   Do attributes have to be added in a namespace?
 *
 * @author Jason Hunter
 * @author Jon Baer
 * @author David Bartle
 * @version 0.5
 */
public class ResultSetBuilder {
    
    /** The ResultSet that becomes a <code>Document</code> */
    private ResultSet rs;

    /** The meta data from the ResultSet */
    private ResultSetMetaData rsmd;

    /** Allows for throwing an exception whenever needed if caught early on */
    private SQLException exception;

    /** Map of original column names to display names */
    private Map names = new HashMap();

    /**
     * Maps column data to be located either as an <code>Attribute</code> of
     * the row (if in the Map) or a child <code>Element</code> of the row
     * (if not in the Map)
     */
    private Map attribs = new HashMap();

    /** The <code>Namespace</code> to use for each <code>Element</code> */
    private Namespace ns = Namespace.NO_NAMESPACE;

    /** The maximum rows to return from the result set */
    int maxRows = Integer.MAX_VALUE;        // default to all

    /** Name for the root <code>Element</code> of the <code>Document</code> */
    private String rootName = "result";

    /** Name for the each immediate child <code>Element</code> of the root */
    private String rowName = "entry";


    /**
     * <p>
     *   This sets up a <code>java.sql.ResultSet</code> to be built
     *   as a <code>Document</code>.
     * </p>
     *
     * @param rs <code>java.sql.ResultSet</code> to build
     */
    public ResultSetBuilder(ResultSet rs) {
      this.rs = rs;
      try {
        rsmd = rs.getMetaData();
      }
      catch (SQLException e) {
        // Hold the exception until build() is called
        exception = e;
      }
    }

    /**
     * <p>
     *   This builds a <code>Document</code> from the
     *   <code>java.sql.ResultSet</code>.
     * </p>
     *
     * @return <code>Document</code> - resultant Document object.
     * @throws <code>JDOMException</code> when there is a problem
     *                                    with the build.
     *
     */
    public Document build() throws JDOMException {
      if (exception != null) {
        throw new JDOMException("Database problem", exception);
      }

      try {
        int colCount = rsmd.getColumnCount();

        Element root = new Element(rootName, ns);
        Document doc = new Document(root);

        int rowCount = 0;

        // get the column labels for this record set 
        String[] columnName = new String[colCount]; 
        for (int index = 0; index < colCount; index++) { 
          columnName[index] = rsmd.getColumnName(index+1); 
        } 

        // build the org.alicebot.server.xml.Document out of the result set 
        String name; 
        String value; 
        Element entry; 
        Element child; 
        
        while (rs.next() && (rowCount++ < maxRows)) {
          entry = new Element(rowName, ns);
          for (int col = 1; col <= colCount; col++) {
            if (names.isEmpty()) {
              name = columnName[col-1];
            }
            else {
              name = lookupName(columnName[col-1]);
            }

            value = rs.getString(col);
            if (!attribs.isEmpty() && isAttribute(name)) {
              if (!rs.wasNull()) {
                entry.addAttribute(name, value);
              }
            }
            else {
              child = new Element(name, ns);
              if (!rs.wasNull()) {
                child.setText(value);
              }
              entry.addContent(child);
            }
          }
          root.addContent(entry);
        }

        return doc;
      }
      catch (SQLException e) {
        throw new JDOMException("Database problem", e);
      }
    }

    private String lookupName(String origName) {
      String name = (String) names.get(origName.toLowerCase());
      if (name != null) {
        return name;
      }
      else {
        return origName;
      }
    }

    private boolean isAttribute(String origName) {
      Boolean val = (Boolean) attribs.get(origName.toLowerCase());
      if (val == Boolean.TRUE) {
        return true;
      }
      else {
        return false;
      }
    }

    /**
     * <p>
     *   Set the name to use as the root element in
     *   the <code>Document</code>
     * </p>
     *
     * @param rootName <code>String</code> the new name.
     *
     */
    public void setRootName(String rootName) {
      this.rootName = rootName;
    }

    /**
     * <p>
     *   Set the name to use as the row element in
     *   the <code>Document</code>
     * </p>
     *
     * @param rowName <code>String</code> the new name.
     *
     */
    public void setRowName(String rowName) {
      this.rowName = rowName;
    }

    /**
     * <p>
     *   Set the <code>Namespace</code> to use for
     *   each <code>Element</code> in the  <code>Document</code>.
     * </p>
     *
     * @param ns <code>String</code> the namespace to use.
     *
     */
    public void setNamespace(Namespace ns) {
      this.ns = ns;
    }

     /**
     * <p>
     *   Set the maximum number of rows to add to your
     *   <code>Document</code>.
     * </p>
     *
     * @param maxRows <code>int</code>
     *
     */
    public void setMaxRows(int maxRows) {
      this.maxRows = maxRows;
    }

    /**
     * <p>
     *   Set a column as an <code>Attribute</code> of a row using the
     *   original column name. The attribute will appear as the original
     *   column name.
     * </p>
     *
     * @param columnName <code>String</code> the original column name
     *
     */
    public void setAsAttribute(String columnName) {
      attribs.put(columnName.toLowerCase(), Boolean.TRUE);
    }

    /**
     * <p>
     *   Set a column as an <code>Attribute</code> of a row using the
     *   column name.  The attribute will appear as the new name provided.
     * </p>
     *
     * @param columnName <code>String</code> original column name
     * @param attribName <code>String</code> new name to use for the attribute
     *
     */
    public void setAsAttribute(String columnName, String attribName) {
      attribs.put(columnName.toLowerCase(), Boolean.TRUE);
      names.put(columnName.toLowerCase(), attribName);
    }

    /**
     * <p>
     *   Set a column as an <code>Attribute</code> of a row using the
     *   column number. The attribute will appear as the original column
     *   name.
     * </p>
     *
     * @param columnNum <code>int</code>
     *
     */
    public void setAsAttribute(int columnNum) {
      try {
        String name = rsmd.getColumnName(columnNum).toLowerCase();
        attribs.put(name, Boolean.TRUE);
      }
      catch (SQLException e) {
        exception = e;
      }
    }

    /**
     * <p>
     *   Set a column as an <code>Attribute</code> of a row using the
     *   column number. The attribute will appear as new name provided.
     * </p>
     *
     * @param columnNum <code>int</code>
     * @param attribName <code>String</code> new name to use for the attribute
     *
     */
    public void setAsAttribute(int columnNum, String attribName) {
      try {
        String name = rsmd.getColumnName(columnNum).toLowerCase();
        attribs.put(name, Boolean.TRUE);
        names.put(name, attribName);
      }
      catch (SQLException e) {
        exception = e;
      }
    }

    /**
     * <p>
     *   Set a column as an <code>Element</code> of a row using the
     *   column name.  The element name will appear as the new name provided.
     * </p>
     *
     * @param columnName <code>String</code> original column name
     * @param elemName <code>String</code> new name to use for the element
     *
     */
    public void setAsElement(String columnName, String elemName) {
      String name = columnName.toLowerCase();
      attribs.put(name, Boolean.FALSE);
      names.put(name, elemName);
    }

    /**
     * <p>
     *   Set a column as an <code>Element</code> of a row using the
     *   column number. The element name will appear as new name provided.
     * </p>
     *
     * @param columnNum <code>int</code>
     * @param elemName <code>String</code> new name to use for the element
     *
     */
    public void setAsElement(int columnNum, String elemName) {
      try {
        String name = rsmd.getColumnName(columnNum).toLowerCase();
        attribs.put(name, Boolean.FALSE);
        names.put(name, elemName);
      }
      catch (SQLException e) {
        exception = e;
      }
    }

/*
    public void setAsIngore(String columnName) {
    }

    public void setAsIngore(int columnNum) {
    }
*/

}
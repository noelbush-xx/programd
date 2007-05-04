<?xml version="1.0" encoding="UTF-8"?>
<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:d="http://aitools.org/programd/4.7">
  <xsl:output method="text"/>
  <xsl:template match="/">
    <xsl:apply-templates select="xs:schema"/>
  </xsl:template>

  <!-- Match schema element and create three files: base class, programmatically-configured class, and XML-configured class.-->
  <xsl:template match="xs:schema">
    <xsl:apply-templates select="." mode="base-class">
      <xsl:with-param name="classname" select="string(xs:annotation/xs:appinfo/d:settings-classes/d:base-class/d:classname)"/>
      <xsl:with-param name="file" select="string(xs:annotation/xs:appinfo/d:settings-classes/d:base-class/d:filename)"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="." mode="programmatic-class">
      <xsl:with-param name="classname" select="string(xs:annotation/xs:appinfo/d:settings-classes/d:programmatic-class/d:classname)"/>
      <xsl:with-param name="file" select="string(xs:annotation/xs:appinfo/d:settings-classes/d:programmatic-class/d:filename)"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="." mode="xml-class">
      <xsl:with-param name="classname" select="string(xs:annotation/xs:appinfo/d:settings-classes/d:xml-class/d:classname)"/>
      <xsl:with-param name="file" select="string(xs:annotation/xs:appinfo/d:settings-classes/d:xml-class/d:filename)"/>
    </xsl:apply-templates>
  </xsl:template>

  <!--Create the base class.-->
  <xsl:template match="xs:schema" mode="base-class">
    <xsl:param name="classname"/>
    <xsl:param name="file"/>
    <xsl:message>Creating base class in <xsl:value-of select="$file"/></xsl:message>
    <xsl:variable name="package" select="d:package($classname)"/>
    <xsl:result-document href="{$file}">
      <xsl:call-template name="class">
        <xsl:with-param name="classname" select="$classname"/>
        <xsl:with-param name="imports">
          <import>java.net.URI</import>
          <import>java.net.URL</import>
          <import>org.aitools.util.Settings</import>
        </xsl:with-param>
        <xsl:with-param name="superclass">Settings</xsl:with-param>
        <xsl:with-param name="abstract" select="true()"/>
        <xsl:with-param name="content">
          <xsl:apply-templates select="//d:property-name" mode="variable-declarations">
            <xsl:with-param name="package" select="$package"/>
            <xsl:with-param name="classname" select="$classname"/>
          </xsl:apply-templates>
          <xsl:apply-templates select="//d:property-name" mode="getters">
            <xsl:with-param name="package" select="$package"/>
            <xsl:with-param name="classname" select="$classname"/>
          </xsl:apply-templates>
          <xsl:apply-templates select="//d:property-name" mode="setters">
            <xsl:with-param name="package" select="$package"/>
            <xsl:with-param name="classname" select="$classname"/>
          </xsl:apply-templates>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:result-document>
  </xsl:template>

  <!--Create the XML configured class.-->
  <xsl:template match="xs:schema" mode="xml-class">
    <xsl:param name="classname"/>
    <xsl:param name="file"/>
    <xsl:message>Creating XML configured class in <xsl:value-of select="$file"/></xsl:message>
    <xsl:variable name="simple-classname" select="d:simple-classname($classname)"/>
    <xsl:variable name="package" select="d:package($classname)"/>
    <xsl:result-document href="{$file}">
      <xsl:call-template name="class">
        <xsl:with-param name="classname" select="$classname"/>
        <xsl:with-param name="imports">
          <import>java.io.FileNotFoundException</import>
          <import>java.net.URL</import>
          <import>org.apache.log4j.Logger</import>
          <import>org.jdom.Document</import>
          <import>org.jdom.IllegalNameException</import>
          <import>org.jdom.JDOMException</import>
          <import>org.jdom.xpath.XPath</import>
          <import>org.aitools.util.resource.URITools</import>
          <import>org.aitools.util.resource.URLTools</import>
          <import>org.aitools.util.runtime.DeveloperError</import>
          <import>org.aitools.util.runtime.UserError</import>
          <import>org.aitools.util.xml.JDOM</import>
        </xsl:with-param>
        <xsl:with-param name="superclass">CoreSettings</xsl:with-param>
        <xsl:with-param name="content">
          <xsl:text>    /** The path to the settings file. */
    private URL _path;

    /** A logger. */
    private Logger _logger;
    
    /**
     * Creates a &lt;code&gt;</xsl:text>
          <xsl:value-of select="$simple-classname"/>
          <xsl:text>&lt;/code&gt; with the XML-formatted settings file
     * located at the given path.
     *
     * @param path the path to the settings file
     * @param logger
     */
    public </xsl:text>
          <xsl:value-of select="$simple-classname"/>
          <xsl:text>(URL path, Logger logger)
    {
        this._path = path;
        this._logger = logger;
        initialize();
    }
    
    /**
     * Creates a new XPath object with the given path,
     * with the Program D configuration namespace associated with
     * prefix "d".
     *
     * @param path
     * @return XPath object
     */
    protected static XPath getXPath(String path)
    {
        final String CONFIG_NS_URI = "http://aitools.org/programd/4.7/programd-configuration";
        XPath xpath;

        try
        {
            xpath = XPath.newInstance(path);
        }
        catch (JDOMException e)
        {
            throw new UserError("Error in settings.", e);
        }
        try
        {
            xpath.addNamespace("d", CONFIG_NS_URI);
        }
        catch (IllegalNameException e)
        {
            throw new DeveloperError(String.format("Illegal namespace \"%s\".", CONFIG_NS_URI), e);
        }
        return xpath;
    }

    /**
     * Returns the string value of the given XPath expression, evaluated from the given node.
     *
     * @param path
     * @param context
     * @return value
     */
    protected String getXPathStringValue(String path, Object context)
    {
        XPath xpath = getXPath(path);
        try
        {
            return xpath.valueOf(context);
        }
        catch (JDOMException e)
        {
            throw new UserError("Failed to evaluate XPath expression.", e);
        }
    }

    /**
     * Returns the number value of the given XPath expression, evaluated from the given node.
     *
     * @param path
     * @param context
     * @return value
     */
    protected Number getXPathNumberValue(String path, Object context)
    {
        XPath xpath = getXPath(path);
        try
        {
            return xpath.numberValueOf(context);
        }
        catch (JDOMException e)
        {
            throw new UserError("Failed to evaluate XPath expression.", e);
        }
    }

    </xsl:text>
          <xsl:apply-templates select="." mode="xml-initialize-method">
            <xsl:with-param name="package" select="$package"/>
            <xsl:with-param name="classname" select="$classname"/>
          </xsl:apply-templates>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:result-document>
  </xsl:template>

  <!--Create the programmatically configured class.-->
  <xsl:template match="xs:schema" mode="programmatic-class">
    <xsl:param name="classname"/>
    <xsl:param name="file"/>
    <xsl:message>Creating programmatically configured class in <xsl:value-of select="$file"/></xsl:message>
    <xsl:variable name="simple-classname" select="d:simple-classname($classname)"/>
    <xsl:variable name="package" select="d:package($classname)"/>
    <xsl:result-document href="{$file}">
      <xsl:call-template name="class">
        <xsl:with-param name="classname" select="$classname"/>
        <xsl:with-param name="imports">
          <import>java.io.FileNotFoundException</import>
          <import>org.aitools.util.resource.URITools</import>
          <import>org.aitools.util.resource.URLTools</import>
          <import>org.aitools.util.runtime.UserError</import>
        </xsl:with-param>
        <xsl:with-param name="superclass">CoreSettings</xsl:with-param>
        <xsl:with-param name="content">
          <xsl:text>    
    /**
     * Creates a &lt;code&gt;</xsl:text>
          <xsl:value-of select="$simple-classname"/>
          <xsl:text>&lt;/code&gt; with default settings values.
     * These are read from the schema when this Java source file is automatically generated.
     */
    public </xsl:text>
          <xsl:value-of select="$simple-classname"/>
          <xsl:text>()
    {
        initialize();
    }
    
    </xsl:text>
          <xsl:apply-templates select="." mode="default-initialize-method">
            <xsl:with-param name="package" select="$package"/>
            <xsl:with-param name="classname" select="$classname"/>
          </xsl:apply-templates>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:result-document>
  </xsl:template>

  <!--Basic content for any class.-->
  <xsl:template name="class">
    <xsl:param name="classname"/>
    <xsl:param name="imports"/>
    <xsl:param name="superclass"/>
    <xsl:param name="interfaces"/>
    <xsl:param name="abstract"/>
    <xsl:param name="content"/>

    <xsl:variable name="simple-classname" select="d:simple-classname($classname)"/>
    <xsl:variable name="package" select="d:package($classname)"/>

    <!--Copyleft and package header-->
    <xsl:call-template name="copyleft"/>
    <xsl:call-template name="package-declaration">
      <xsl:with-param name="package" select="$package"/>
    </xsl:call-template>

    <!--Imports-->
    <xsl:call-template name="imports">
      <xsl:with-param name="imports" select="$imports"/>
    </xsl:call-template>

    <!--Class block-->
    <xsl:text>
/**
 * Automatically generated at </xsl:text>
    <xsl:value-of select="current-dateTime()"/>
    <xsl:text>.
 */
</xsl:text>
    <xsl:if test="$abstract">
      <xsl:text>abstract </xsl:text>
    </xsl:if>
    <xsl:text>public class </xsl:text>
    <xsl:value-of select="$simple-classname"/>
    <xsl:if test="$superclass">
      <xsl:text> extends </xsl:text>
      <xsl:value-of select="$superclass"/>
    </xsl:if>
    <xsl:if test="$interfaces">
      <xsl:text> implements </xsl:text>
      <xsl:value-of select="$interfaces"/>
    </xsl:if>
    <xsl:text>
{
</xsl:text>
    <xsl:value-of select="$content"/>
    <xsl:text>}
</xsl:text>
  </xsl:template>

  <!--Produce a copyleft notice.-->
  <xsl:template name="copyleft">
    <xsl:text>/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
</xsl:text>
  </xsl:template>

  <!--Produce a package declaration.-->
  <xsl:template name="package-declaration">
    <xsl:param name="package"/>
    <xsl:text>package </xsl:text>
    <xsl:value-of select="$package"/>
    <xsl:text>;

</xsl:text>
  </xsl:template>

  <!--List imports.-->
  <xsl:template name="imports">
    <xsl:param name="imports"/>
    <xsl:for-each select="$imports/import">
      <xsl:text>import </xsl:text>
      <xsl:value-of select="."/>
      <xsl:text>;
</xsl:text>
    </xsl:for-each>
  </xsl:template>

  <!--Create the initialize() method for XML settings.-->
  <xsl:template match="xs:schema" mode="xml-initialize-method">
    <xsl:param name="package"/>
    <xsl:param name="classname"/>
    <xsl:text>/**
     * Initializes the Settings with values from the XML settings file.
     */
    @SuppressWarnings("boxing")
    @Override
    protected void initialize()
    {
        Document document = JDOM.getDocument(this._path, this._logger);
</xsl:text>
    <xsl:apply-templates select="//d:property-name" mode="xml-initialize">
      <xsl:with-param name="package" select="$package"/>
      <xsl:with-param name="classname" select="$classname"/>
    </xsl:apply-templates>
    <xsl:text>    }
</xsl:text>
  </xsl:template>

  <!--Initialize a setting from XML.-->
  <xsl:template match="d:property-name" mode="xml-initialize">
    <xsl:param name="package"/>
    <xsl:param name="classname"/>
    <xsl:text>
        // Initialize </xsl:text>
    <xsl:value-of select="."/>
    <xsl:text>.
</xsl:text>
    <!--Discover type.-->
    <xsl:variable name="type" select="d:get-type(.)"/>
    <xsl:choose>
      <xsl:when test="$type = 'boolean'">
        <xsl:apply-templates select="." mode="xml-initializer">
          <xsl:with-param name="conversion">Boolean.parseBoolean</xsl:with-param>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:when test="$type = 'int'">
        <xsl:apply-templates select="." mode="xml-initializer">
          <xsl:with-param name="get-number" select="true()"/>
          <xsl:with-param name="method-conversion">intValue</xsl:with-param>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:when test="$type = 'String'">
        <xsl:apply-templates select="." mode="xml-initializer"> </xsl:apply-templates>
      </xsl:when>
      <xsl:when test="$type = 'URI'">
        <xsl:apply-templates select="." mode="xml-initializer">
          <xsl:with-param name="conversion">URITools.createValidURI</xsl:with-param>
          <xsl:with-param name="conversion-arguments">, false</xsl:with-param>
          <xsl:with-param name="exception">FileNotFoundException</xsl:with-param>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:when test="$type = 'URL'">
        <xsl:apply-templates select="." mode="xml-initializer">
          <xsl:with-param name="conversion">URLTools.createValidURL</xsl:with-param>
          <xsl:with-param name="conversion-arguments">, this._path, false</xsl:with-param>
          <xsl:with-param name="exception">FileNotFoundException</xsl:with-param>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:when test="starts-with($type, 'enum ')">
        <xsl:variable name="enumType" select="substring-after($type, 'enum ')"/>
        <xsl:variable name="propertyName" select="."/>
        <xsl:text>
        String </xsl:text>
        <xsl:value-of select="$propertyName"/>
        <xsl:text>Value = getXPathStringValue("</xsl:text>
        <xsl:value-of select="d:path-to(., 'd')"/>
        <xsl:text>", document);
</xsl:text>
        <xsl:for-each select="d:get-enum-values(ancestor::xs:schema, $enumType)">
          <xsl:text>        </xsl:text>
          <xsl:if test="position() != 1">
            <xsl:text>else </xsl:text>
      </xsl:if>
          <xsl:text>if (</xsl:text>
          <xsl:value-of select="$propertyName"/>
          <xsl:text>Value.equals("</xsl:text>
          <xsl:value-of select="."/>
          <xsl:text>"))
        {
            set</xsl:text>
          <xsl:value-of select="d:title-case($propertyName)"/>
          <xsl:text>(</xsl:text>
          <xsl:value-of select="$enumType"/>
          <xsl:text>.</xsl:text>
          <xsl:value-of select="d:enum-name(.)"/>
          <xsl:text>);
        }
</xsl:text>
        </xsl:for-each>
      </xsl:when>
    </xsl:choose>
  </xsl:template>
  
  <!--Make an initializer that reads from XML.-->
  <xsl:template match="d:property-name" mode="xml-initializer">
    <xsl:param name="cast"/>
    <xsl:param name="get-number" select="false()"/>
    <xsl:param name="conversion"/>
    <xsl:param name="conversion-arguments"/>
    <xsl:param name="method-conversion"/>
    <xsl:param name="exception"/>
    <xsl:if test="$exception">
      <xsl:text>        try
        {
    </xsl:text>
    </xsl:if>
    <xsl:text>        set</xsl:text>
    <xsl:value-of select="d:title-case(.)"/>
    <xsl:text>(</xsl:text>
    <xsl:if test="$conversion">
      <xsl:value-of select="$conversion"/>
      <xsl:text>(</xsl:text>
    </xsl:if>
    <xsl:if test="$cast">
      <xsl:text>(</xsl:text>
      <xsl:value-of select="$cast"/>
      <xsl:text>)</xsl:text>
    </xsl:if>
    <xsl:text>getXPath</xsl:text>
    <xsl:choose>
      <xsl:when test="$get-number">Number</xsl:when>
      <xsl:otherwise>String</xsl:otherwise>
    </xsl:choose>
    <xsl:text>Value("</xsl:text>
    <xsl:value-of select="d:path-to(., 'd')"/>
    <xsl:text>", document)</xsl:text>
    <xsl:if test="$conversion-arguments">
      <xsl:value-of select="$conversion-arguments"/>
    </xsl:if>
    <xsl:if test="$conversion">
      <xsl:text>)</xsl:text>
    </xsl:if>
    <xsl:if test="$method-conversion">
      <xsl:text>.</xsl:text>
      <xsl:value-of select="$method-conversion"/>
      <xsl:text>()</xsl:text>
    </xsl:if>
    <xsl:text>);
</xsl:text>
    <xsl:if test="$exception">
      <xsl:text>        }
        catch (</xsl:text>
      <xsl:value-of select="$exception"/>
      <xsl:text> e)
        {
            throw new UserError("Error in settings.", e);
        }
</xsl:text>
    </xsl:if>

  </xsl:template>

  <!--Create an initialize() method that initializes to defaults.-->
  <xsl:template match="xs:schema" mode="default-initialize-method">
    <xsl:param name="package"/>
    <xsl:param name="classname"/>
    <xsl:text>/**
     * Initializes the Settings with default values (from the schema).
     */
    @SuppressWarnings("boxing")
    @Override
    protected void initialize()
    {
</xsl:text>
    <xsl:apply-templates select="//d:property-name" mode="default-initialize">
      <xsl:with-param name="package" select="$package"/>
      <xsl:with-param name="classname" select="$classname"/>
    </xsl:apply-templates>
    <xsl:text>    }
</xsl:text>
  </xsl:template>


  <!--Initialize a setting with the default value.-->
  <xsl:template match="d:property-name" mode="default-initialize">
    <xsl:param name="package"/>
    <xsl:param name="classname"/>
    <!--Discover type.-->
    <xsl:variable name="type" select="d:get-type(.)"/>
    <!--Discover default.-->
    <xsl:variable name="default" select="d:get-default(.)"/>
    <xsl:if test="string-length($default) &gt; 0">
      <xsl:choose>
        <xsl:when test="$type = 'boolean'">
          <xsl:apply-templates select="." mode="default-initializer">
            <xsl:with-param name="conversion">Boolean.parseBoolean</xsl:with-param>
            <xsl:with-param name="default" select="$default"/>
          </xsl:apply-templates>
        </xsl:when>
        <xsl:when test="$type = 'int'">
          <xsl:apply-templates select="." mode="default-initializer">
            <xsl:with-param name="conversion">Integer.parseInt</xsl:with-param>
            <xsl:with-param name="default" select="$default"/>
          </xsl:apply-templates>
        </xsl:when>
        <xsl:when test="$type = 'String'">
          <xsl:apply-templates select="." mode="default-initializer">
            <xsl:with-param name="default" select="$default"/>
          </xsl:apply-templates>
        </xsl:when>
        <xsl:when test="$type = 'URI'">
          <xsl:apply-templates select="." mode="default-initializer">
            <xsl:with-param name="conversion">URITools.createValidURI</xsl:with-param>
            <xsl:with-param name="conversion-arguments">, false</xsl:with-param>
            <xsl:with-param name="default" select="$default"/>
            <xsl:with-param name="exception">FileNotFoundException</xsl:with-param>
          </xsl:apply-templates>
        </xsl:when>
        <xsl:when test="$type = 'URL'">
          <xsl:apply-templates select="." mode="default-initializer">
            <xsl:with-param name="conversion">URLTools.createValidURL</xsl:with-param>
            <xsl:with-param name="conversion-arguments">, false</xsl:with-param>
            <xsl:with-param name="default" select="$default"/>
            <xsl:with-param name="exception">FileNotFoundException</xsl:with-param>
          </xsl:apply-templates>
        </xsl:when>
        <xsl:when test="starts-with($type, 'enum ')">
          <xsl:apply-templates select="." mode="default-initializer">
            <xsl:with-param name="default" select="concat(substring-after($type, 'enum '), '.', d:enum-name($default))"/>
            <xsl:with-param name="quote" select="false()"/>
          </xsl:apply-templates>
        </xsl:when>
      </xsl:choose>
    </xsl:if>
  </xsl:template>

  <!--Make an initializer that assigns a default value.-->
  <xsl:template match="d:property-name" mode="default-initializer">
    <xsl:param name="conversion"/>
    <xsl:param name="conversion-arguments"/>
    <xsl:param name="default"/>
    <xsl:param name="exception"/>
    <xsl:param name="quote" select="true()"/>
    <xsl:if test="$exception">
      <xsl:text>        try
        {
    </xsl:text>
    </xsl:if>
    <xsl:text>        set</xsl:text>
    <xsl:value-of select="d:title-case(.)"/>
    <xsl:text>(</xsl:text>
    <xsl:if test="$conversion">
      <xsl:value-of select="$conversion"/>
      <xsl:text>(</xsl:text>
    </xsl:if>
    <xsl:if test="$quote">
      <xsl:text>"</xsl:text>
    </xsl:if>
    <xsl:value-of select="$default"/>
    <xsl:if test="$quote">
      <xsl:text>"</xsl:text>
    </xsl:if>
    <xsl:if test="$conversion-arguments">
      <xsl:value-of select="$conversion-arguments"/>
    </xsl:if>
    <xsl:if test="$conversion">
      <xsl:text>)</xsl:text>
    </xsl:if>
    <xsl:text>);
</xsl:text>
    <xsl:if test="$exception">
      <xsl:text>        }
        catch (</xsl:text>
      <xsl:value-of select="$exception"/>
      <xsl:text> e)
        {
            throw new UserError("Error in settings.", e);
        }
</xsl:text>
    </xsl:if>
  </xsl:template>

  <!--Build the property variable declaration.-->
  <xsl:template match="d:property-name" mode="variable-declarations">
    <xsl:param name="package"/>
    <xsl:param name="classname"/>
    <xsl:text>    /** </xsl:text>
    <xsl:variable name="description" select="string(ancestor::xs:annotation/xs:documentation)"/>
    <xsl:if test="$description">
      <xsl:value-of select="normalize-space(replace(replace($description, '\*.*', '', 's'), '\[.+\]', ''))"/>
    </xsl:if>
    <xsl:text> */
    private </xsl:text>
    <!--Discover type.-->
    <xsl:variable name="type" select="d:get-type(.)"/>
    <xsl:choose>
      <xsl:when test="starts-with($type, 'enum ')">
        <xsl:variable name="enumType" select="substring-after($type, 'enum ')"/>
        <xsl:text/>
        <xsl:value-of select="d:title-case(.)"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="."/>
        <xsl:text>;
    
    /** The possible values for </xsl:text>
        <xsl:value-of select="d:title-case(.)"/>
        <xsl:text>. */
    public static enum </xsl:text>
        <xsl:value-of select="$enumType"/>
        <xsl:text>
    {
        </xsl:text>
        <xsl:for-each select="d:get-enum-values(ancestor::xs:schema, $enumType)">
          <xsl:text>/** </xsl:text>
          <xsl:value-of select="d:get-enum-value-description(ancestor::xs:schema, $enumType, .)"/>
          <xsl:text> */
        </xsl:text>
          <xsl:value-of select="d:enum-name(.)"/>
          <xsl:choose>
            <xsl:when test="position() &lt; last()">
              <xsl:text>,

        </xsl:text>
            </xsl:when>
            <xsl:otherwise>
              <xsl:text>
    }

</xsl:text>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$type"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="."/>
        <xsl:text>;
        
</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--Build the getters.-->
  <xsl:template match="d:property-name" mode="getters">
    <xsl:param name="package"/>
    <xsl:param name="classname"/>
    <xsl:text>    /**
     * @return the value of </xsl:text>
    <xsl:value-of select="."/>
    <xsl:text>
     */
    public </xsl:text>
    <xsl:variable name="description" select="d:get-description(.)"/>
    <!--Discover type.-->
    <xsl:variable name="type" select="d:get-type(.)"/>
    <xsl:choose>
      <xsl:when test="$type = 'boolean'">
        <xsl:text>boolean </xsl:text>
        <xsl:value-of select="."/>
      </xsl:when>
      <xsl:when test="starts-with($type, 'enum ')">
        <xsl:value-of select="substring-after($type, 'enum ')"/>
        <xsl:text> get</xsl:text>
        <xsl:value-of select="d:title-case(.)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$type"/>
        <xsl:text> </xsl:text>
        <xsl:text>get</xsl:text>
        <xsl:value-of select="d:title-case(.)"/>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:text>()
    {
        return this.</xsl:text>
    <xsl:value-of select="."/>
    <xsl:text>;
    }

</xsl:text>
  </xsl:template>

  <!--Build the setters.-->
  <xsl:template match="d:property-name" mode="setters">
    <xsl:param name="package"/>
    <xsl:param name="classname"/>
    <xsl:text>    /**
     * @param value the value for </xsl:text>
    <xsl:value-of select="."/>
    <xsl:text>
     */
    public void set</xsl:text>
    <xsl:value-of select="d:title-case(.)"/>
    <xsl:text>(</xsl:text>
    <xsl:variable name="description" select="d:get-description(.)"/>
    <!--Discover type.-->
    <xsl:variable name="type" select="d:get-type(.)"/>
    <xsl:choose>
      <xsl:when test="starts-with($type, 'enum ')">
        <xsl:value-of select="substring-after($type, 'enum ')"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$type"/>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:text> value)
    {
        this.</xsl:text>
    <xsl:value-of select="."/>
    <xsl:text> = value;
    }

</xsl:text>
  </xsl:template>

  <!--FUNCTIONS-->

  <!--Get the description of a setting.-->
  <xsl:function name="d:get-description">
    <xsl:param name="property-name-node"/>
    <xsl:value-of select="$property-name-node/ancestor::xs:annotation/xs:documentation"/>
  </xsl:function>

  <!--Return the simple name of a class.-->
  <xsl:function name="d:simple-classname">
    <xsl:param name="classname"/>
    <xsl:value-of select="replace($classname, '^.+\.([^\.]+)$', '$1')"/>
  </xsl:function>

  <!--Return the package name of a class.-->
  <xsl:function name="d:package">
    <xsl:param name="classname"/>
    <xsl:value-of select="replace($classname, '^(.+)\.[^\.]+$', '$1')"/>
  </xsl:function>

  <!--Determine the type of a property.-->
  <xsl:function name="d:get-type">
    <xsl:param name="property-name-node"/>
    <xsl:variable name="xs-type">
      <xsl:choose>
        <xsl:when test="$property-name-node/ancestor::xs:attribute">
          <xsl:value-of select="$property-name-node/ancestor::xs:attribute/@type"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$property-name-node/ancestor::xs:element/@type"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$xs-type = 'xs:anyURI'">URI</xsl:when>
      <xsl:when test="$xs-type = 'URL'">URL</xsl:when>
      <xsl:when test="$xs-type = 'xs:string'">String</xsl:when>
      <xsl:when test="$xs-type = 'xs:int'">int</xsl:when>
      <xsl:when test="$xs-type = 'xs:boolean'">boolean</xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="concat('enum ', $xs-type)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <!--Determine the values for an enum.-->
  <xsl:function name="d:get-enum-values">
    <xsl:param name="schemaRoot"/>
    <xsl:param name="enumType"/>
    <xsl:sequence select="$schemaRoot/xs:simpleType[@name = $enumType]/xs:restriction/xs:enumeration/@value"/>
  </xsl:function>

  <!--Get the description for an enum value.-->
  <xsl:function name="d:get-enum-value-description">
    <xsl:param name="schemaRoot"/>
    <xsl:param name="enumType"/>
    <xsl:param name="value"/>
    <xsl:value-of
      select="normalize-space($schemaRoot/xs:simpleType[@name = $enumType]/xs:restriction/xs:enumeration[@value = $value]/xs:annotation/xs:documentation)"
    />
  </xsl:function>

  <!--Determine the XPath required to reach, in an instance document, the node of whose definition this property name is a child in an XML Schema.-->
  <xsl:function name="d:path-to">
    <xsl:param name="property-name-node"/>
    <xsl:param name="prefix"/>
    <xsl:variable name="target" select="$property-name-node/parent::xs:appinfo/parent::xs:annotation/parent::*"/>
    <xsl:choose>
      <xsl:when test="local-name($target) = 'attribute'">
        <xsl:value-of select="d:path-to-attribute($target, $prefix)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="d:path-to-element($target, $prefix)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>
  <xsl:function name="d:path-to-element">
    <xsl:param name="target"/>
    <xsl:param name="prefix"/>
    <xsl:call-template name="ascend">
      <xsl:with-param name="current-location" select="$target/parent::*"/>
      <xsl:with-param name="current-path" select="concat('/', $prefix, ':', $target/@name)"/>
      <xsl:with-param name="prefix" select="$prefix"/>
    </xsl:call-template>
  </xsl:function>
  <xsl:function name="d:path-to-attribute">
    <xsl:param name="target"/>
    <xsl:param name="prefix"/>
    <xsl:call-template name="ascend">
      <xsl:with-param name="current-location" select="$target/parent::*"/>
      <xsl:with-param name="current-path" select="concat('/@', $target/@name)"/>
      <xsl:with-param name="prefix" select="$prefix"/>
    </xsl:call-template>
  </xsl:function>
  <xsl:template name="ascend">
    <xsl:param name="current-location"/>
    <xsl:param name="current-path"/>
    <xsl:param name="prefix"/>
    <xsl:choose>
      <xsl:when test="$current-location/parent::*">
        <xsl:call-template name="ascend">
          <xsl:with-param name="current-location" select="$current-location/parent::*"/>
          <xsl:with-param name="current-path">
            <xsl:choose>
              <xsl:when test="local-name($current-location) = 'element'">
                <xsl:value-of select="concat('/', $prefix, ':', $current-location/@name, $current-path)"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="$current-path"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:with-param>
          <xsl:with-param name="prefix" select="$prefix"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$current-path"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--Determine the default value for a property.-->
  <xsl:function name="d:get-default">
    <xsl:param name="property-name-node"/>
    <xsl:value-of select="$property-name-node/ancestor::xs:element/@default"/>
  </xsl:function>

  <!--A few simple functions used in formatting.-->
  <xsl:function name="d:title-case">
    <xsl:param name="string"/>
    <xsl:sequence select="concat(upper-case(substring($string, 1, 1)), substring($string, 2))"/>
  </xsl:function>
  <xsl:function name="d:enum-name">
    <xsl:param name="string"/>
    <xsl:value-of select="replace(upper-case($string), '\-', '_')"/>
  </xsl:function>
  <xsl:function name="d:variable-case-tokens">
    <xsl:param name="tokens"/>
    <xsl:value-of select="$tokens[1]"/>
    <xsl:for-each select="$tokens[position() &gt; 1]">
      <xsl:value-of select="d:title-case(.)"/>
    </xsl:for-each>
  </xsl:function>
  <xsl:function name="d:escape-metacharacters">
    <xsl:param name="string"/>
    <xsl:sequence select="replace($string, '(\^|\.|\\|\?|\*|\+|\{|\}|\(|\)|\||\^|\$|\[|\])', '\\$1')"/>
  </xsl:function>
  <xsl:function name="d:make-property-name">
    <xsl:param name="string"/>
    <xsl:param name="strip-prefix"/>
    <xsl:sequence
      select="string-join(d:variable-case-tokens(tokenize(replace(replace($string, '    ', ''), $strip-prefix, ''), '[\-.]')), '')"/>
  </xsl:function>
</xsl:transform>

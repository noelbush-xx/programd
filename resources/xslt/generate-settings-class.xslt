<?xml version="1.0" encoding="UTF-8"?>
<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:d="http://aitools.org/programd/4.6">
    <xsl:output method="text"/>
    <xsl:template match="/">
        <xsl:apply-templates select="xs:schema"/>
    </xsl:template>
    <xsl:template match="xs:schema">
        <xsl:variable name="classname" select="string(xs:annotation/xs:appinfo/d:class-name)"/>
        <xsl:variable name="simple-classname" select="replace($classname, '^.+\.([^\.]+)$', '$1')"/>
        <xsl:variable name="package" select="replace($classname, '^(.+)\.[^\.]+$', '$1')"/>
        <!--Copyleft notice.-->
        <xsl:text>/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
 
package </xsl:text>
        <xsl:value-of select="$package"/>
        <xsl:text>;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.aitools.programd.util.URLTools;
import org.aitools.programd.util.UserError;

</xsl:text>
        <xsl:if test="$package != 'org.aitools.programd.util'">
            <xsl:text>import org.aitools.programd.util.Settings;

</xsl:text>
        </xsl:if>
        <xsl:text>/**
 * Automatically generated from properties file, </xsl:text>
        <xsl:value-of select="current-dateTime()"/>
        <xsl:text>
 */
public class </xsl:text>
        <xsl:value-of select="$simple-classname"/>
        <xsl:text> extends Settings
{
</xsl:text>
        <xsl:apply-templates select="//d:property-name" mode="variable-declarations">
            <xsl:with-param name="package" select="$package"/>
            <xsl:with-param name="classname" select="$classname"/>
        </xsl:apply-templates>
        <xsl:text>    /**
     * Creates a &lt;code&gt;</xsl:text>
        <xsl:value-of select="$simple-classname"/>
        <xsl:text>&lt;/code&gt; using default property values.
     */
    public </xsl:text>
        <xsl:value-of select="$simple-classname"/>
        <xsl:text>()
    {
        super();
    }
    
    /**
     * Creates a &lt;code&gt;</xsl:text>
        <xsl:value-of select="$simple-classname"/>
        <xsl:text>&lt;/code&gt; with the (XML-formatted) properties
     * located at the given path.
     *
     * @param propertiesPath the path to the configuration file
     */
    public </xsl:text>
        <xsl:value-of select="$simple-classname"/>
        <xsl:text>(URL propertiesPath)
    {
        super(propertiesPath);
    }

</xsl:text>
        <xsl:apply-templates select="." mode="initialize-method">
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
        <xsl:text>}</xsl:text>
    </xsl:template>
    <!--Create the initialize() method.-->
    <xsl:template match="xs:schema" mode="initialize-method">
        <xsl:param name="package"/>
        <xsl:param name="classname"/>
        <xsl:text>    /**
    * Initializes the Settings with values from properties, or defaults.
    */
    @Override
    protected void initialize()
    {
</xsl:text>
        <xsl:apply-templates select="//d:property-name" mode="initialize">
            <xsl:with-param name="package" select="$package"/>
            <xsl:with-param name="classname" select="$classname"/>
        </xsl:apply-templates>
        <xsl:text>    }

</xsl:text>
    </xsl:template>
    <!--Initialize a property.-->
    <xsl:template match="d:property-name" mode="initialize">
        <xsl:param name="package"/>
        <xsl:param name="classname"/>
        <!--Discover type.-->
        <xsl:variable name="type" select="d:get-type(ancestor::xs:element/@type)"/>
        <!--Discover default.-->
        <xsl:variable name="default" select="d:get-default(.)"/>
        <!--Get the description.-->
        <xsl:variable name="description" select="d:get-description(.)"/>
        <xsl:choose>
            <xsl:when test="$type = 'int'">
                <xsl:text>        try
        {
            set</xsl:text>
                <xsl:value-of select="d:title-case(.)"/>
                <xsl:text>(Integer.parseInt(this.properties.getProperty("</xsl:text>
                <xsl:value-of select="@key"/>
                <xsl:text>", "</xsl:text>
                <xsl:value-of select="$default"/>
                <xsl:text>")));
        }
        catch (NumberFormatException e)
        {
            set</xsl:text>
                <xsl:value-of select="d:title-case(.)"/>
                <xsl:text>(</xsl:text>
                <xsl:value-of select="$default"/>
                <xsl:text>);
        }</xsl:text>
            </xsl:when>
            <xsl:when test="$type = 'boolean'">
                <xsl:text>        set</xsl:text>
                <xsl:value-of select="d:title-case(.)"/>
                <xsl:text>(Boolean.valueOf(this.properties.getProperty("</xsl:text>
                <xsl:value-of select="@key"/>
                <xsl:text>", "</xsl:text>
                <xsl:value-of select="$default"/>
                <xsl:text>")).booleanValue());</xsl:text>
            </xsl:when>
            <xsl:when test="$type = 'enum'">
                <xsl:text>        String </xsl:text>
                <xsl:value-of select="."/>
                <xsl:text>Value = this.properties.getProperty("</xsl:text>
                <xsl:value-of select="@key"/>
                <xsl:text>", "</xsl:text>
                <xsl:value-of select="$default"/>
                <xsl:text>");
         
         </xsl:text>
                <xsl:for-each select="d:get-enum-values($description)">
                    <xsl:if test="position() != 1">
                        <xsl:text>else </xsl:text>
                    </xsl:if>
                    <xsl:text>if (</xsl:text>
                    <xsl:value-of select="."/>
                    <xsl:text>Value.equals("</xsl:text>
                    <xsl:value-of select="."/>
                    <xsl:text>"))
         {
             this.</xsl:text>
                    <xsl:value-of select="."/>
                    <xsl:text> = </xsl:text>
                    <xsl:value-of select="d:title-case(.)"/>
                    <xsl:text>.</xsl:text>
                    <xsl:value-of select="upper-case(.)"/>
                    <xsl:text>;
         }
             </xsl:text>
                </xsl:for-each>
            </xsl:when>
            <xsl:when test="$type = 'URI'">
                <xsl:text>        try
        {
            set</xsl:text>
                <xsl:value-of select="d:title-case(.)"/>
                <xsl:text>(new URI(this.properties.getProperty("</xsl:text>
                <xsl:value-of select="@key"/>
                <xsl:text>", "</xsl:text>
                <xsl:value-of select="$default"/>
                <xsl:text>")));
        }
        catch (URISyntaxException e)
        {
            throw new UserError(e);
        }</xsl:text>
            </xsl:when>
            <xsl:when test="$type = 'URL'">
                <xsl:text>        set</xsl:text>
                <xsl:value-of select="d:title-case(.)"/>
                <xsl:text>(URLTools.contextualize(this.path, this.properties.getProperty("</xsl:text>
                <xsl:value-of select="@key"/>
                <xsl:text>", "</xsl:text>
                <xsl:value-of select="$default"/>
                <xsl:text>")));</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text>        set</xsl:text>
                <xsl:value-of select="d:title-case(.)"/>
                <xsl:text>(this.properties.getProperty("</xsl:text>
                <xsl:value-of select="@key"/>
                <xsl:text>", "</xsl:text>
                <xsl:value-of select="$default"/>
                <xsl:text>"));</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:text>

</xsl:text>
    </xsl:template>
    <!--Build the property variable declaration.-->
    <xsl:template match="d:property-name" mode="variable-declarations">
        <xsl:param name="package"/>
        <xsl:param name="classname"/>
        <xsl:text>    /**
</xsl:text>
        <xsl:variable name="description" select="string(ancestor::xs:annotation/xs:documentation)"/>
        <xsl:if test="$description">
            <xsl:text>     * </xsl:text>
            <xsl:value-of select="normalize-space(replace(replace($description, '\*.*', '', 's'), '\[.+\]', ''))"/>
            <xsl:text>
</xsl:text>
        </xsl:if>
        <xsl:text>     */
    private </xsl:text>
        <!--Discover type.-->
        <xsl:variable name="type" select="d:get-type(.)"/>
        <xsl:choose>
            <xsl:when test="$type = 'enum'">
                <xsl:text></xsl:text>
                <xsl:value-of select="d:title-case(.)"/>
                <xsl:text> </xsl:text>
                <xsl:value-of select="."/>
                <xsl:text>;
    
    /** The possible values for </xsl:text>
                <xsl:value-of select="d:title-case(.)"/>
                <xsl:text>. */
    public static enum </xsl:text>
                <xsl:value-of select="d:title-case(.)"/>
                <xsl:text>
    {
        </xsl:text>
                <xsl:for-each select="d:get-enum-values($description)">
                    <xsl:text>/** </xsl:text>
                    <xsl:value-of select="d:get-enum-value-description(., $description)"/>
                    <xsl:text>. */
        </xsl:text>
                <xsl:value-of select="upper-case(.)"/>
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
                <xsl:text>
        </xsl:text>
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
     * Returns the value of </xsl:text>
        <xsl:value-of select="."/>.
        <xsl:text>
     * 
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
            <xsl:when test="$type = 'enum'">
                <xsl:value-of select="d:title-case(.)"/>
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
     * Sets </xsl:text>
        <xsl:value-of select="."/>.
        <xsl:text>
     * 
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
            <xsl:when test="$type = 'enum'">
                <xsl:value-of select="d:title-case(.)"/>
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
    <xsl:function name="d:get-description">
        <xsl:param name="property-name-node"/>
        <xsl:value-of select="$property-name-node/ancestor::xs:annotation/xs:documentation"/>
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
            <xsl:when test="$xs-type = 'xs:string'">String</xsl:when>
            <xsl:when test="$xs-type = 'xs:int'">int</xsl:when>
            <xsl:when test="$xs-type = 'xs:boolean'">boolean</xsl:when>
            <xsl:otherwise><xsl:value-of select="$xs-type"/></xsl:otherwise>
        </xsl:choose>
    </xsl:function>
    <!--Determine the values for an enum.-->
    <xsl:function name="d:get-enum-values">
        <xsl:param name="description"/>
        <xsl:variable name="valuespec" select="substring-after(substring-before(substring-after($description, '['), '):'), '(')"/>
        <xsl:sequence select="tokenize($valuespec, ', *')"/>
    </xsl:function>
    <!--Determine the default value for a property.-->
    <xsl:function name="d:get-default">
        <xsl:param name="property-name-node"/>
        <xsl:value-of select="$property-name-node/ancestor::xs:element/@default"/>
    </xsl:function>
    <!--Get the description for an enum value.-->
    <xsl:function name="d:get-enum-value-description">
        <xsl:param name="value"/>
        <xsl:param name="description"/>
        <xsl:sequence select="normalize-space(substring-before(substring-after($description, concat('* ', $value, ': ')), '.'))"/>
    </xsl:function>
    <!--A few simple functions used in formatting.-->
    <xsl:function name="d:title-case">
        <xsl:param name="string"/>
        <xsl:sequence select="concat(upper-case(substring($string, 1, 1)), substring($string, 2))"/>
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
        <xsl:sequence
            select="replace($string, '(\^|\.|\\|\?|\*|\+|\{|\}|\(|\)|\||\^|\$|\[|\])', '\\$1')"/>
    </xsl:function>
    <xsl:function name="d:make-property-name">
        <xsl:param name="string"/>
        <xsl:param name="strip-prefix"/>
        <xsl:sequence
            select="string-join(d:variable-case-tokens(tokenize(replace(replace($string, '    ', ''), $strip-prefix, ''), '[\-.]')), '')"
        />
    </xsl:function>
</xsl:transform>

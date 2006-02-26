<?xml version="1.0" encoding="UTF-8"?>
<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
    xmlns="http://aitools.org/programd/4.6/bot-configuration" xmlns:aiml="http://alicebot.org/2001/AIML-1.0.1">
    <xsl:output encoding="UTF-8" method="xml" indent="yes" />
    <xsl:param name="bot-count">100</xsl:param>
    <!--<xsl:param name="aiml-files">../AIML.aiml</xsl:param>-->
    <xsl:param name="aiml-files">../../../../aiml/AAA/*.aiml</xsl:param>
    <xsl:param name="sentence-splitters-path">../../../conf/sentence-splitters.xml</xsl:param>
    <xsl:template match="/">
        <xsl:call-template name="make-bot-config" />
    </xsl:template>
    <xsl:template name="make-bot-config">
        <xsl:element name="bots">
            <xsl:for-each select="1 to $bot-count">
                <xsl:element name="bot">
                    <xsl:attribute name="id">
                        <xsl:value-of select="concat('bot-', .)" />
                    </xsl:attribute>
                    <xsl:attribute name="enabled">true</xsl:attribute>
                    <xsl:element name="sentence-splitters">
                        <xsl:attribute name="href" select="$sentence-splitters-path"/>
                    </xsl:element>
                    <xsl:element name="properties">
                        <xsl:element name="property">
                            <xsl:attribute name="name">name</xsl:attribute>
                            <xsl:attribute name="value" select="concat('Bot ', .)"/>
                        </xsl:element>
                    </xsl:element>
                    <xsl:element name="learn">
                        <xsl:value-of select="$aiml-files"/>
                    </xsl:element>
                </xsl:element>
            </xsl:for-each>
        </xsl:element>
    </xsl:template>
</xsl:transform>

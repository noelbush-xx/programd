/*
 * Copyright (c) 1997 by Arthur Do <arthur@cs.stanford.edu>. All Rights Reserved.
 *
 * Please refer to the file "license.txt" important copyright and licensing 
 * information.
 *
 * THE AUTHOR MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. THE AUTHOR SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */

package org.alicebot.server.net.html;

import java.util.*;

/**
 * HtmlTag is a helper class to store parsed tag information.
 *
 * @version 2.01 09/12/97
 * @author Arthur Do <arthur@cs.stanford.edu>
 * @see     org.alicebot.server.net.html.HtmlStreamTokenizer
 */
public class HtmlTag 
{
	public HtmlTag()
	{
	}

	public HtmlTag(HtmlTag orig)
	{
		m_tag = new String(orig.m_tag);
		m_ttype = orig.m_ttype;
		m_endtag = orig.m_endtag;
		m_params = (Hashtable)orig.m_params.clone();
		m_originalParamNames = (Hashtable)orig.m_originalParamNames.clone();
		Hashtable m_tags = (Hashtable)orig.m_tags.clone();
	}

	/**
	 * Sets the tag name.
	 *
	 * @param	tag  name of tag, e.g. "img"
	 * @exception  HtmlException  if malformed tag.
	 */
	public void setTag(String tag)
		throws HtmlException
	{
		try
		{
			if (tag.charAt(0) == '/')
			{
				m_endtag = true;
				tag = tag.substring(1);
			}

			m_tag = tag;
			Object value = m_tags.get(tag.toUpperCase());
			if (value != null)
				m_ttype = ((Integer)value).intValue();
		}
		catch (StringIndexOutOfBoundsException e)
		{
			throw new HtmlException("invalid tag");
		}
	}

	/**
	 * @return	tag type, e.g. one of the <b>T_</b> constants.
	 */
	public int getTagType()
	{
		return m_ttype;
	}

	/**
	 * @return	tag name, the same name as passed to the constructor.
	 */
	public String getTagString()
	{
		return m_tag;
	}

	/**
	 * @return	this is an end tag or not, i.e. if the tag has a slash before the name.
	 */
	public boolean isEndTag()
	{
		return m_endtag;
	}

	/**
	 * Looks up a tag param name and returns the associated
	 * value, if any. Try to use the predefined <b>P_</b> constants.
	 *
	 * @param	name  name of param, must be lowercase
	 * @return	the value associated with the name, or null.
	 */
	public String getParam(String name)
	{
		return (String)m_params.get(name);
	}

	public boolean hasParam(String name)
	{
		return getParam(name) != null;
	}

	public int getIntParam(String name)
		throws NumberFormatException
	{
		return Integer.parseInt(getParam(name));
	}

	/**
	 * Associates a param name with a value.
	 *
	 * @param	name  name of param, the convention is to use a lowercase name
	 * @param	value  value associated with name
	 */
	public void setParam(String name, String value)
	{
		m_params.put(name, value);
	}

	/**
	 * Associates a param name with a value, preserving the original
	 * name.
	 *
	 * @param	name  name of param, the convention is to use a lowercase name
	 * @param	value  value associated with name
	 * @param	originalName  preserve the orginal case of name
	 */
	public void setParam(String name, String value, String originalName)
	{
		m_originalParamNames.put(name, originalName);
		setParam(name, value);
	}

	/**
	 * Remove association of a param name with a value.
	 *
	 * @param	name  name of param to remove, the convention is to use a lowercase name
	 */
	public void removeParam(String name)
	{
		m_originalParamNames.remove(name);
		m_params.remove(name);
	}

	/**
	 * @return	string representation of tag
	 */
	public String toString()
	{
		StringBuffer tag = new StringBuffer();

		tag.append('<');
		if (m_endtag)
			tag.append('/');
		tag.append(m_tag);

		Enumeration keys = m_params.keys();
		Enumeration elements = m_params.elements();
		while (keys.hasMoreElements())
		{
			String name = (String)keys.nextElement();
			String originalName = (String)m_originalParamNames.get(name);
			if (originalName != null)
				name = originalName;
			tag.append(" " + name);
			String value = (String)elements.nextElement();
			if (value.length() > 0)
				tag.append("=\"" + value + "\"");
		}
		tag.append('>');

		return tag.toString();
	}

	/**
	 * Reset tag to original state, as if it was just constructed.
	 */
	public void reset()
	{
		m_tag = null;
		m_ttype = T_UNKNOWN;
		m_endtag = false;
		m_params = new Hashtable();
		m_originalParamNames = new Hashtable();
		Hashtable m_tags = new Hashtable();
	}

	public static final int T_UNKNOWN = 0;
	public static final int T_A = 1;
	public static final int T_ABBREV = 2;
	public static final int T_ACRONYM = 3;
	public static final int T_ADDRESS = 4;
	public static final int T_APPLET = 5;
	public static final int T_AREA = 6;
	public static final int T_AU = 7;
	public static final int T_B = 8;
	public static final int T_BANNER = 9;
	public static final int T_BASE = 10;
	public static final int T_BASEFONT = 11;
	public static final int T_BGSOUND = 12;
	public static final int T_BIG = 13;
	public static final int T_BLINK = 14;
	public static final int T_BLOCKQUOTE = 15;
	public static final int T_BODY = 16;
	public static final int T_BR = 17;
	public static final int T_CAPTION = 18;
	public static final int T_CENTER = 19;
	public static final int T_CITE = 20;
	public static final int T_CODE = 21;
	public static final int T_COL = 22;
	public static final int T_COLGROUP = 23;
	public static final int T_CREDIT = 24;
	public static final int T_DD = 25;
	public static final int T_DEL = 26;
	public static final int T_DFN = 27;
	public static final int T_DIR = 28;
	public static final int T_DIV = 29;
	public static final int T_DL = 30;
	public static final int T_DT = 31;
	public static final int T_EM = 32;
	public static final int T_EMBED = 33;
	public static final int T_FIG = 34;
	public static final int T_FN = 35;
	public static final int T_FONT = 36;
	public static final int T_FORM = 37;
	public static final int T_FRAME = 38;
	public static final int T_FRAMESET = 39;
	public static final int T_H1 = 40;
	public static final int T_H2 = 41;
	public static final int T_H3 = 42;
	public static final int T_H4 = 43;
	public static final int T_H5 = 44;
	public static final int T_H6 = 45;
	public static final int T_HEAD = 46;
	public static final int T_HTML = 47;
	public static final int T_HR = 48;
	public static final int T_I = 49;
	public static final int T_IMG = 50;
	public static final int T_INPUT = 51;
	public static final int T_INS = 52;
	public static final int T_ISINDEX = 53;
	public static final int T_KBD = 54;
	public static final int T_LANG = 55;
	public static final int T_LH = 56;
	public static final int T_LI = 57;
	public static final int T_LINK = 58;
	public static final int T_MAP = 59;
	public static final int T_MARQUEE = 60;
	public static final int T_MENU = 61;
	public static final int T_META = 62;
	public static final int T_NEXTID = 63;
	public static final int T_NOBR = 64;
	public static final int T_NOEMBED = 65;
	public static final int T_NOFRAME = 66;
	public static final int T_NOFRAMES = 67;
	public static final int T_NOTE = 68;
	public static final int T_OBJECT = 69;
	public static final int T_OL = 70;
	public static final int T_OPTION = 71;
	public static final int T_OVERLAY = 72;
	public static final int T_P = 73;
	public static final int T_PARAM = 74;
	public static final int T_PERSON = 75;
	public static final int T_PRE = 76;
	public static final int T_Q = 77;
	public static final int T_RANGE = 78;
	public static final int T_S = 79;
	public static final int T_SAMP = 80;
	public static final int T_SCRIPT = 81;
	public static final int T_SELECT = 82;
	public static final int T_SMALL = 83;
	public static final int T_SPOT = 84;
	public static final int T_STRONG = 85;
	public static final int T_STYLE = 86;
	public static final int T_SUB = 87;
	public static final int T_SUP = 88;
	public static final int T_TAB = 89;
	public static final int T_TABLE = 90;
	public static final int T_TBODY = 91;
	public static final int T_TD = 92;
	public static final int T_TEXTAREA = 93;
	public static final int T_TFOOT = 94;
	public static final int T_TH = 95;
	public static final int T_THEAD = 96;
	public static final int T_TITLE = 97;
	public static final int T_TR = 98;
	public static final int T_TT = 99;
	public static final int T_U = 100;
	public static final int T_UL = 101;
	public static final int T_VAR = 102;
	public static final int T_WBR = 103;
	/**
	 * <!DOCTYPE ...>
	 */
	public static final int T__DOCTYPE = 104;

	public static final String P_ALIGN = new String("align");
	public static final String P_BACKGROUND = new String("background");
	public static final String P_BORDER = new String("border");
	public static final String P_CHECKED = new String("checked");
	public static final String P_CLEAR = new String("clear");
	public static final String P_CODE = new String("code");
	public static final String P_COLS = new String("cols");
	public static final String P_COLSPAN = new String("colspan");
	public static final String P_FACE = new String("face");
	public static final String P_HEIGHT = new String("height");
	public static final String P_HREF = new String("href");
	public static final String P_LANGUAGE = new String("language");
	public static final String P_LOWSRC = new String("lowsrc");
	public static final String P_MAXLENGTH = new String("maxlength");
	public static final String P_MULTIPLE = new String("multiple");
	public static final String P_NAME = new String("name");
	public static final String P_ROWS = new String("rows");
	public static final String P_ROWSPAN = new String("rowspan");
	public static final String P_SIZE = new String("size");
	public static final String P_SRC = new String("src");
	public static final String P_TARGET = new String("target");
	public static final String P_TYPE = new String("type");
	public static final String P_VALUE = new String("value");
	public static final String P_WIDTH = new String("width");

	private String m_tag = null;
	private int m_ttype = T_UNKNOWN;
	private boolean m_endtag = false;
	private Hashtable m_params = new Hashtable();
	private Hashtable m_originalParamNames = new Hashtable();
	private static Hashtable m_tags = new Hashtable();

	static
	{
		m_tags.put(new String("A"), new Integer(T_A));
		m_tags.put(new String("ABBREV"), new Integer(T_ABBREV));
		m_tags.put(new String("ACRONYM"), new Integer(T_ACRONYM));
		m_tags.put(new String("ADDRESS"), new Integer(T_ADDRESS));
		m_tags.put(new String("APPLET"), new Integer(T_APPLET));
		m_tags.put(new String("AREA"), new Integer(T_AREA));
		m_tags.put(new String("AU"), new Integer(T_AU));
		m_tags.put(new String("B"), new Integer(T_B));
		m_tags.put(new String("BANNER"), new Integer(T_BANNER));
		m_tags.put(new String("BASE"), new Integer(T_BASE));
		m_tags.put(new String("BASEFONT"), new Integer(T_BASEFONT));
		m_tags.put(new String("BGSOUND"), new Integer(T_BGSOUND));
		m_tags.put(new String("BIG"), new Integer(T_BIG));
		m_tags.put(new String("BLINK"), new Integer(T_BLINK));
		m_tags.put(new String("BLOCKQUOTE"), new Integer(T_BLOCKQUOTE));
		m_tags.put(new String("BODY"), new Integer(T_BODY));
		m_tags.put(new String("BR"), new Integer(T_BR));
		m_tags.put(new String("CAPTION"), new Integer(T_CAPTION));
		m_tags.put(new String("CENTER"), new Integer(T_CENTER));
		m_tags.put(new String("CITE"), new Integer(T_CITE));
		m_tags.put(new String("CODE"), new Integer(T_CODE));
		m_tags.put(new String("COL"), new Integer(T_COL));
		m_tags.put(new String("COLGROUP"), new Integer(T_COLGROUP));
		m_tags.put(new String("CREDIT"), new Integer(T_CREDIT));
		m_tags.put(new String("DD"), new Integer(T_DD));
		m_tags.put(new String("DEL"), new Integer(T_DEL));
		m_tags.put(new String("DFN"), new Integer(T_DFN));
		m_tags.put(new String("DIR"), new Integer(T_DIR));
		m_tags.put(new String("DIV"), new Integer(T_DIV));
		m_tags.put(new String("DL"), new Integer(T_DL));
		m_tags.put(new String("!DOCTYPE"), new Integer(T__DOCTYPE));
		m_tags.put(new String("DT"), new Integer(T_DT));
		m_tags.put(new String("EM"), new Integer(T_EM));
		m_tags.put(new String("EMBED"), new Integer(T_EMBED));
		m_tags.put(new String("FIG"), new Integer(T_FIG));
		m_tags.put(new String("FN"), new Integer(T_FN));
		m_tags.put(new String("FONT"), new Integer(T_FONT));
		m_tags.put(new String("FORM"), new Integer(T_FORM));
		m_tags.put(new String("FRAME"), new Integer(T_FRAME));
		m_tags.put(new String("FRAMESET"), new Integer(T_FRAMESET));
		m_tags.put(new String("H1"), new Integer(T_H1));
		m_tags.put(new String("H2"), new Integer(T_H2));
		m_tags.put(new String("H3"), new Integer(T_H3));
		m_tags.put(new String("H4"), new Integer(T_H4));
		m_tags.put(new String("H5"), new Integer(T_H5));
		m_tags.put(new String("H6"), new Integer(T_H6));
		m_tags.put(new String("HEAD"), new Integer(T_HEAD));
		m_tags.put(new String("HTML"), new Integer(T_HTML));
		m_tags.put(new String("HR"), new Integer(T_HR));
		m_tags.put(new String("I"), new Integer(T_I));
		m_tags.put(new String("IMG"), new Integer(T_IMG));
		m_tags.put(new String("INPUT"), new Integer(T_INPUT));
		m_tags.put(new String("INS"), new Integer(T_INS));
		m_tags.put(new String("ISINDEX"), new Integer(T_ISINDEX));
		m_tags.put(new String("KBD"), new Integer(T_KBD));
		m_tags.put(new String("LANG"), new Integer(T_LANG));
		m_tags.put(new String("LH"), new Integer(T_LH));
		m_tags.put(new String("LI"), new Integer(T_LI));
		m_tags.put(new String("LINK"), new Integer(T_LINK));
		m_tags.put(new String("MAP"), new Integer(T_MAP));
		m_tags.put(new String("MARQUEE"), new Integer(T_MARQUEE));
		m_tags.put(new String("MENU"), new Integer(T_MENU));
		m_tags.put(new String("META"), new Integer(T_META));
		m_tags.put(new String("NEXTID"), new Integer(T_NEXTID));
		m_tags.put(new String("NOBR"), new Integer(T_NOBR));
		m_tags.put(new String("NOEMBED"), new Integer(T_NOEMBED));
		m_tags.put(new String("NOFRAME"), new Integer(T_NOFRAME));
		m_tags.put(new String("NOFRAMES"), new Integer(T_NOFRAMES));
		m_tags.put(new String("NOTE"), new Integer(T_NOTE));
		m_tags.put(new String("OBJECT"), new Integer(T_OBJECT));
		m_tags.put(new String("OL"), new Integer(T_OL));
		m_tags.put(new String("OPTION"), new Integer(T_OPTION));
		m_tags.put(new String("OVERLAY"), new Integer(T_OVERLAY));
		m_tags.put(new String("P"), new Integer(T_P));
		m_tags.put(new String("PARAM"), new Integer(T_PARAM));
		m_tags.put(new String("PERSON"), new Integer(T_PERSON));
		m_tags.put(new String("PRE"), new Integer(T_PRE));
		m_tags.put(new String("Q"), new Integer(T_Q));
		m_tags.put(new String("RANGE"), new Integer(T_RANGE));
		m_tags.put(new String("S"), new Integer(T_S));
		m_tags.put(new String("SAMP"), new Integer(T_SAMP));
		m_tags.put(new String("SCRIPT"), new Integer(T_SCRIPT));
		m_tags.put(new String("SELECT"), new Integer(T_SELECT));
		m_tags.put(new String("SMALL"), new Integer(T_SMALL));
		m_tags.put(new String("SPOT"), new Integer(T_SPOT));
		m_tags.put(new String("STRONG"), new Integer(T_STRONG));
		m_tags.put(new String("STYLE"), new Integer(T_STYLE));
		m_tags.put(new String("SUB"), new Integer(T_SUB));
		m_tags.put(new String("SUP"), new Integer(T_SUP));
		m_tags.put(new String("TAB"), new Integer(T_TAB));
		m_tags.put(new String("TABLE"), new Integer(T_TABLE));
		m_tags.put(new String("TBODY"), new Integer(T_TBODY));
		m_tags.put(new String("TD"), new Integer(T_TD));
		m_tags.put(new String("TEXTAREA"), new Integer(T_TEXTAREA));
		m_tags.put(new String("TFOOT"), new Integer(T_TFOOT));
		m_tags.put(new String("TH"), new Integer(T_TH));
		m_tags.put(new String("THEAD"), new Integer(T_THEAD));
		m_tags.put(new String("TITLE"), new Integer(T_TITLE));
		m_tags.put(new String("TR"), new Integer(T_TR));
		m_tags.put(new String("TT"), new Integer(T_TT));
		m_tags.put(new String("U"), new Integer(T_U));
		m_tags.put(new String("UL"), new Integer(T_UL));
		m_tags.put(new String("VAR"), new Integer(T_VAR));
		m_tags.put(new String("WBR"), new Integer(T_WBR));
	}
}


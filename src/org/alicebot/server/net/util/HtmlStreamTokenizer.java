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

import java.io.*;
import java.util.*;

/**
 * <p>HtmlStreamTokenizer is an HTML parser that is similar 
 * to the StreamTokenizer class but is specialized for 
 * HTML streams. This class is useful when you need to 
 * parse the structure of an HTML document.</p>
 *
 * <pre> 
 * import org.alicebot.server.net.html.*;
 * <p>
 * HtmlStreamTokenizer tok = new HtmlStreamTokenizer(inputstream);
 * HtmlTag tag = new HtmlTag();
 *
 * while (tok.nextToken() != HtmlStreamTokenizer.TT_EOF) {
 *	int ttype = tok.getTokenType();
 *
 *	if (ttype == HtmlStreamTokenizer.TT_TAG) {
 *		tok.parseTag(tok.getStringValue(), tag);
 *		System.out.println(&quot;tag: &quot; + tag.toString());
 *	}
 *	else if (ttype == HtmlStreamTokenizer.TT_TEXT) {
 *		System.out.println(&quot;text: &quot; + tok.getStringValue());
 *	}
 *	else if (ttype == HtmlStreamTokenizer.TT_COMMENT) {
 *		System.out.println(&quot;comment: &lt;!--&quot; + tok.getStringValue() + &quot;--&gt;&quot;);
 *	}
 * }
 * </pre>
 *
 * <p>One of the motivations for designing <i>parseTag()</i> to take
 * an HtmlTag argument rather than having <i>parseTag()</i> return
 * a newly created HtmlTag is so you can create your own tag class
 * derived from HtmlTag.
 *
 * <ul>
 * <li> 02/09/98 Thomas Horster-Moller, fixed bug with counting
 * newlines twice on character pushback.
 * </ul>
 *
 * @version 2.01 09/12/97
 * @author Arthur Do <arthur@cs.stanford.edu>
 * @see     org.alicebot.server.net.html.HtmlTag
 * @see     org.alicebot.server.net.html.Table
 */
public class HtmlStreamTokenizer
{
	/**
	 * end of stream.
	 */
    public static final int TT_EOF = -1;
	/**
	 * text token.
	 */
    public static final int TT_TEXT = -2;
	/**
	 * tag token.
	 */
    public static final int TT_TAG = -3;
	/**
	 * comment token.
	 */
	public static final int TT_COMMENT = -4;

	/**
	 * @param	in  input reader
	 */
	public HtmlStreamTokenizer(Reader in)
	{
		m_in = in;
		m_state = STATE_TEXT;
	}

	/**
	 * @param	in  input stream
	 */
	public HtmlStreamTokenizer(InputStream in)
	{
		this(new InputStreamReader(in));
	}

	/**
	 * @return	token type, one of the <b>TT_</b> defines
	 */
	public final int getTokenType()
	{
		return m_ttype;
	}

	/**
	 * @return	string value of the token
	 */
	public final StringBuffer getStringValue()
	{
		return m_buf;
	}

	/**
	 * @return	any white space accumulated since last call to nextToken
	 */
	public final StringBuffer getWhiteSpace()
	{
		return m_whitespace;
	}

	/**
	 * @return	current line number. Every time nextToken() sees a new
	 *			line character ('\n'), it increments the line number.
	 */
	public int getLineNumber()
	{
		return m_lineno;
	}

	/**
	 * @return	the next token
     * @exception  IOException  if error reading input stream.
	 */
	public int nextToken()
		throws IOException
	{
		m_buf.setLength(0);
		m_whitespace.setLength(0);

		while (true)
		{
			int c;
			
			if (m_pushback != 0)
			{
				c = m_pushback;
				if (c == '\n')
					m_lineno--;		// don't count newline twice
				m_pushback = 0;
			}
			else
			{
				c = m_in.read();
			}

			if (c < 0)
			{
				int state = m_state;
				m_state = STATE_EOF;

				if (m_buf.length() > 0 && state == STATE_TEXT)
					return m_ttype = TT_TEXT;
				else
					return m_ttype = TT_EOF;
			}

			if (c == '\n')
				m_lineno++;

			switch (m_state)
			{
			case STATE_TEXT:
				{
					if (c == '<')
					{
						m_state = STATE_TAG;
						if (m_buf.length() > 0)
							return m_ttype = TT_TEXT;
					}
					else if (isSpace(c))
					{
						m_pushback = c;
						m_state = STATE_WS;
						if (m_buf.length() > 0)
							return m_ttype = TT_TEXT;
					}
					else
					{
						m_buf.append((char)c);
					}
				}
				break;

			case STATE_WS:
				{
					if (!isSpace(c))
					{
						m_pushback = c;
						m_state = STATE_TEXT;
					}
					else
					{
						m_whitespace.append((char)c);
					}
				}
				break;

			case STATE_TAG:
				{
					int buflen = m_buf.length();

					if (c == '>')
					{
						m_state = STATE_TEXT;
						return m_ttype = TT_TAG;
					}
					else if (c == '<' && buflen == 0)
					{
						// handle <<, some people use it in <pre>
						m_buf.append("<<");
						m_state = STATE_TEXT;
					}
					else if (c == '-' && buflen == 2 && m_buf.charAt(1) == '-' && m_buf.charAt(0) == '!')
					{
						// handle <!--
						m_buf.setLength(0);
						m_state = STATE_COMMENT;
					}
					else if (c == C_SINGLEQUOTE || c == C_DOUBLEQUOTE)
					{
						// handle quotes inside tag
						m_tagquote = c;
						m_buf.append((char)c);
						m_state = STATE_TAG_QUOTE;
					}
					else
					{
						m_buf.append((char)c);
					}
				}
				break;

			case STATE_TAG_QUOTE:
				{
					// the only way out out of this state is to close the quote
					// special case: some people forget to end quote in a tag
					if (c == '>')
					{
						m_pushback = c;
						m_state = STATE_TAG;
					}
					else
					{
						m_buf.append((char)c);
						if (c == m_tagquote)
						{
							m_state = STATE_TAG;
						}
					}
				}
				break;

			case STATE_COMMENT:
				{
					if (c == '>' && m_comment >= 2)
					{
						m_buf.setLength(m_buf.length() - 2);
						m_comment = 0;
						m_state = STATE_TEXT;
						return m_ttype = TT_COMMENT;
					}
					else if (c == '-')
					{
						m_comment++;
					}
					else
					{
						m_comment = 0;
					}

					m_buf.append((char)c);
				}
				break;
			}
		}
	}

	/**
	 * The reason this function takes an HtmlTag argument rather than returning
	 * a newly created HtmlTag object is so that you can create your own
	 * tag class derived from HtmlTag if desired.
	 *
	 * @param	sbuf  text buffer to parse
	 * @param	tag  parse the text buffer and store the result in this object
     * @exception  HtmlException  if malformed tag.
	 */
	public static void parseTag(StringBuffer sbuf, HtmlTag tag)
		throws HtmlException
	{
		tag.reset();

		String buf = sbuf.toString();
		int len = buf.length();
		int idx = 0;
		int begin = 0;

		// parse tag
		while (idx < len && isSpace(buf.charAt(idx)))
			idx++;

		if (idx == len)
			throw new HtmlException("parse empty tag");

		begin = idx;
		while (idx < len && !isSpace(buf.charAt(idx)))
			idx++;
		String token = buf.substring(begin, idx);

		tag.setTag(token);

		while (idx < len && isSpace(buf.charAt(idx)))
			idx++;

		if (idx == len)
			return;

		parseParams(tag, buf, idx);
	}

	/**
	 * Replaces HTML escape sequences with its character equivalent, e.g.
	 * <b>&amp;copy;</b> becomes <b>&copy;</b>.
	 *
	 * @param	buf  text buffer to unescape
	 * @return	a string with all HTML escape sequences removed
	 */
	public static String unescape(String buf)
	{
		// quick check to see if there are any escape characters
		if (buf.indexOf('&') == -1)
			return buf;

		StringBuffer b = new StringBuffer(buf);
		unescape(b);
		return b.toString();
	}


	/**
	 * Replaces HTML escape sequences with its character equivalent, e.g.
	 * <b>&amp;copy;</b> becomes <b>&copy;</b>.
	 *
	 * @param	buf  will remove all HTML escape sequences from this buffer
	 */
	public static void unescape(StringBuffer buf)
	{
		int len = buf.length();
		int i = 0;
		int r = i;
		boolean retryAmp = false;
		while (i<len)
		{
			char ch = buf.charAt(i);
			if (ch == '&')
			{
				retryAmp = false;
				int saver = r;
				String esc = "";
				int j = i+1;
				for (; j<len; j++)
				{
					buf.setCharAt(r++, ch);
					ch = buf.charAt(j);
					if (ch == ';' || ch == '<' || (isPunct(ch) && ch != '#') || isSpace(ch))
					{
						Character e = parseEscape(esc);
						if (e != null)
						{
							// found escape sequence
							// as opposed to false or unrecognized escape, e.g. AT&T.
							r = saver;
							ch = e.charValue();
						}

						if (ch == '&')
							retryAmp = true;
						i = j;
						break;
					}
					esc += ch;
				}
				if (j == len)
				{
					Character e = parseEscape(esc);
					if (e != null)
					{
						// found escape sequence
						// as opposed to false or unrecognized escape, e.g. AT&T.
						r = saver;
						ch = e.charValue();
					}
					i = j-1;
				}
			}
			if (!retryAmp)
			{
				buf.setCharAt(r++, ch);
				i++;
			}
		}
		buf.setLength(r);
	}

    private int m_ttype;
	private StringBuffer m_buf = new StringBuffer();
	private StringBuffer m_whitespace = new StringBuffer();
	private int m_pushback = 0;
	private int m_lineno = 1;
	private int m_comment = 0;

    private static final int STATE_EOF = -1;
    private static final int STATE_COMMENT = -2;
    private static final int STATE_TEXT = -3;
    private static final int STATE_TAG = -4;
    private static final int STATE_WS = -5;
	private static final int STATE_TAG_QUOTE = -6;

	private int m_state = STATE_TEXT;

	//private InputStream m_in;
	private Reader m_in;

	private static final char C_SINGLEQUOTE = '\'';
	private static final char C_DOUBLEQUOTE = '"';
	private int m_tagquote;

	private static final int CTYPE_LEN = 256;
    private static byte m_ctype[] = new byte[CTYPE_LEN];
    private static final byte CT_WHITESPACE = 1;
    private static final byte CT_DIGIT = 2;
    private static final byte CT_ALPHA = 4;
    private static final byte CT_QUOTE = 8;
    private static final byte CT_COMMENT = 16;

	private static Hashtable m_escapes = new Hashtable();

	static
	{
		int len = m_ctype.length;
		for (int i = 0; i < len; i++)
			m_ctype[i] = 0;

		m_ctype[' '] = CT_WHITESPACE;
		m_ctype['\r'] = CT_WHITESPACE;
		m_ctype['\n'] = CT_WHITESPACE;
		m_ctype['\t'] = CT_WHITESPACE;
		for (int i = 0x0E; i <= 0x1F; i++)
			m_ctype[i] = CT_WHITESPACE;

		m_escapes.put(new String("Aacute"), new Character('\u00c1'));
		m_escapes.put(new String("aacute"), new Character('\u00e1'));
		m_escapes.put(new String("Acirc"), new Character('\u00c2'));
		m_escapes.put(new String("acirc"), new Character('\u00e2'));
		m_escapes.put(new String("AElig"), new Character('\u00c6'));
		m_escapes.put(new String("aelig"), new Character('\u00e6'));
		m_escapes.put(new String("Agrave"), new Character('\u00c0'));
		m_escapes.put(new String("agrave"), new Character('\u00e0'));
		m_escapes.put(new String("amp"), new Character('&'));
		m_escapes.put(new String("aring"), new Character('\u00e5'));
		m_escapes.put(new String("Atilde"), new Character('\u00c3'));
		m_escapes.put(new String("atilde"), new Character('\u00e3'));
		m_escapes.put(new String("Auml"), new Character('\u00c4'));
		m_escapes.put(new String("auml"), new Character('\u00e4'));
		m_escapes.put(new String("brvbar"), new Character('\u00a6'));
		m_escapes.put(new String("Ccedil"), new Character('\u00c7'));
		m_escapes.put(new String("ccedil"), new Character('\u00e7'));
		m_escapes.put(new String("cent"), new Character('\u00a2'));
		m_escapes.put(new String("copy"), new Character('\u00a9'));
		m_escapes.put(new String("deg"), new Character('\u00b0'));
		m_escapes.put(new String("Eacute"), new Character('\u00c9'));
		m_escapes.put(new String("eacute"), new Character('\u00e9'));
		m_escapes.put(new String("Ecirc"), new Character('\u00ca'));
		m_escapes.put(new String("ecirc"), new Character('\u00ea'));
		m_escapes.put(new String("Egrave"), new Character('\u00c8'));
		m_escapes.put(new String("egrave"), new Character('\u00e8'));
		m_escapes.put(new String("ETH"), new Character('\u00d0'));
		m_escapes.put(new String("eth"), new Character('\u00f0'));
		m_escapes.put(new String("Euml"), new Character('\u00cb'));
		m_escapes.put(new String("euml"), new Character('\u00eb'));
		m_escapes.put(new String("frac12"), new Character('\u00bd'));
		m_escapes.put(new String("frac14"), new Character('\u00bc'));
		m_escapes.put(new String("frac34"), new Character('\u00be'));
		m_escapes.put(new String("gt"), new Character('>'));
		m_escapes.put(new String("iacute"), new Character('\u00ed'));
		m_escapes.put(new String("Icirc"), new Character('\u00ce'));
		m_escapes.put(new String("icirc"), new Character('\u00ee'));
		m_escapes.put(new String("iexcl"), new Character('\u00a1'));
		m_escapes.put(new String("Igrave"), new Character('\u00cc'));
		m_escapes.put(new String("igrave"), new Character('\u00ec'));
		m_escapes.put(new String("iquest"), new Character('\u00bf'));
		m_escapes.put(new String("Iuml"), new Character('\u00cf'));
		m_escapes.put(new String("iuml"), new Character('\u00ef'));
		m_escapes.put(new String("laquo"), new Character('\u00ab'));
		m_escapes.put(new String("lt"), new Character('<'));
		m_escapes.put(new String("middot"), new Character('\u00b7'));
		m_escapes.put(new String("nbsp"), new Character('\u0020'));
		m_escapes.put(new String("not"), new Character('\u00ac'));
		m_escapes.put(new String("Ntilde"), new Character('\u00d1'));
		m_escapes.put(new String("ntilde"), new Character('\u00f1'));
		m_escapes.put(new String("Oacute"), new Character('\u00d3'));
		m_escapes.put(new String("oacute"), new Character('\u00f3'));
		m_escapes.put(new String("Ocirc"), new Character('\u00d4'));
		m_escapes.put(new String("ocirc"), new Character('\u00f4'));
		m_escapes.put(new String("Ograve"), new Character('\u00d2'));
		m_escapes.put(new String("ograve"), new Character('\u00f2'));
		m_escapes.put(new String("Oslash"), new Character('\u00d8'));
		m_escapes.put(new String("oslash"), new Character('\u00f8'));
		m_escapes.put(new String("Otilde"), new Character('\u00d5'));
		m_escapes.put(new String("otilde"), new Character('\u00f5'));
		m_escapes.put(new String("Ouml"), new Character('\u00d6'));
		m_escapes.put(new String("ouml"), new Character('\u00f6'));
		m_escapes.put(new String("para"), new Character('\u00b6'));
		m_escapes.put(new String("plusmn"), new Character('\u00b1'));
		m_escapes.put(new String("pound"), new Character('\u00a3'));
		m_escapes.put(new String("quot"), new Character('"'));
		m_escapes.put(new String("reg"), new Character('\u00ae'));
		m_escapes.put(new String("sect"), new Character('\u00a7'));
		m_escapes.put(new String("sup1"), new Character('\u00b9'));
		m_escapes.put(new String("sup2"), new Character('\u00b2'));
		m_escapes.put(new String("sup3"), new Character('\u00b3'));
		m_escapes.put(new String("szlig"), new Character('\u00df'));
		m_escapes.put(new String("THORN"), new Character('\u00de'));
		m_escapes.put(new String("thorn"), new Character('\u00fe'));
		m_escapes.put(new String("Uacute"), new Character('\u00da'));
		m_escapes.put(new String("uacute"), new Character('\u00fa'));
		m_escapes.put(new String("Ucirc"), new Character('\u00db'));
		m_escapes.put(new String("ucirc"), new Character('\u00fb'));
		m_escapes.put(new String("Ugrave"), new Character('\u00d9'));
		m_escapes.put(new String("ugrave"), new Character('\u00f9'));
		m_escapes.put(new String("Uuml"), new Character('\u00dc'));
		m_escapes.put(new String("uuml"), new Character('\u00fc'));
		m_escapes.put(new String("Yacute"), new Character('\u00dd'));
		m_escapes.put(new String("yacute"), new Character('\u00fd'));
		m_escapes.put(new String("yen"), new Character('\u00a5'));
		m_escapes.put(new String("yuml"), new Character('\u00ff'));
	}

	private static boolean isSpace(int c)
	{
		 return c >=0 && c < CTYPE_LEN ? (m_ctype[c] & CT_WHITESPACE) != 0: false;
	}

	private static boolean isPunct(char c)
	{
		return !Character.isLetterOrDigit(c);
	}

	private static Character parseEscape(String s)
	{
		int len = s.length();
		if (len == 0)
			return null;
		Character ch = null;

		if (s.charAt(0) == '#')
		{
			if (len <= 1)
				return null;

			int code = 0;
			for (int i=1; i<len; i++)
			{
				if (!Character.isDigit(s.charAt(i)))
					return null;
				code = (code * 10) + Character.digit(s.charAt(i), 10);
			}
			ch = new Character(Character.forDigit(code, 10));
		}
		else
		{
			ch = (Character)m_escapes.get(s);
		}

		return ch;
	}

	private static void parseParams(HtmlTag tag, String buf, int idx)
		throws HtmlException
	{
		int len = buf.length();
		int begin = 0;

		while (idx < len)
		{
			while (idx < len && isSpace(buf.charAt(idx)))
				idx++;

			if (idx == len)
				continue;

			begin = idx;
			if (buf.charAt(idx) == C_DOUBLEQUOTE)
			{
				idx++;
				while (idx < len && buf.charAt(idx) != C_DOUBLEQUOTE)
					idx++;
				if (idx == len)
					continue;	// bad name
				idx++;
			}
			else if (buf.charAt(idx) == C_SINGLEQUOTE)
			{
				idx++;
				while (idx < len && buf.charAt(idx) != C_SINGLEQUOTE)
					idx++;
				if (idx == len)
					continue;	// bad name
				idx++;
			}
			else
			{
				while (idx < len && !isSpace(buf.charAt(idx)) && buf.charAt(idx) != '=')
					idx++;
			}

			// param names are stored in lower case for fast lookup
			String originalName = buf.substring(begin, idx);
			String name = buf.substring(begin, idx).toLowerCase();

			if (idx < len && isSpace(buf.charAt(idx)))
			{
				while (idx < len && isSpace(buf.charAt(idx)))
					idx++;
			}

			if (idx == len || buf.charAt(idx) != '=')
			{
				// name with empty value
				tag.setParam(name, "", originalName);
				continue;
			}
			idx++;

			if (idx == len)
				continue;

			if (buf.charAt(idx) == ' ')
			{
				// special case: if value is surrounded by quotes
				// then it can have a space after the '='
				while (idx < len && isSpace(buf.charAt(idx)))
					idx++;

				if (idx == len || (buf.charAt(idx) != C_DOUBLEQUOTE && buf.charAt(idx) != C_SINGLEQUOTE))
					continue;
			}

			begin = idx;
			int end = begin;
			if (buf.charAt(idx) == C_DOUBLEQUOTE)
			{
				idx++;
				begin = idx;
				while (idx < len && buf.charAt(idx) != C_DOUBLEQUOTE)
					idx++;
				if (idx == len)
					continue;	// bad value
				end = idx;
				idx++;
			}
			else if (buf.charAt(idx) == C_SINGLEQUOTE)
			{
				idx++;
				begin = idx;
				while (idx < len && buf.charAt(idx) != C_SINGLEQUOTE)
					idx++;
				if (idx == len)
					continue;	// bad value
				end = idx;
				idx++;
			}
			else
			{
				while (idx < len && !isSpace(buf.charAt(idx)))
					idx++;
				end = idx;
			}

			String value = buf.substring(begin, end);

			tag.setParam(name, value, originalName);
		}
	}
}


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

import java.lang.Exception;

public class HtmlException extends Exception
{
	public HtmlException()
	{
	}

	public HtmlException(String s)
	{
		super(s);
	}
}


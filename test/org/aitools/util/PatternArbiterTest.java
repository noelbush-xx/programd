/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.util;

import static org.junit.Assert.*;

import org.aitools.programd.util.NotAnAIMLPatternException;
import org.aitools.programd.util.PatternArbiter;
import org.junit.Test;

/**
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 *
 */
public class PatternArbiterTest {
  
  /**
   * Test checking of valid AIML patterns.
   */
  @SuppressWarnings("static-method")
  @Test
  public void testValidPattern_000() {
    assertFalse(PatternArbiter.isValidAIMLPattern("match"));
  }

  /**
   * Test whether the PatternArbiter correctly interprets AIML-style patterns
   * and can determine whether a given input matches them.
   * @throws NotAnAIMLPatternException 
   */
  @SuppressWarnings("static-method")
  @Test
  public void testMatchPattern_000() throws NotAnAIMLPatternException {
    assertTrue(PatternArbiter.matches("match", "MATCH", true));
  }

  /**
   * Test whether the PatternArbiter correctly interprets AIML-style patterns
   * and can determine whether a given input matches them.
   * @throws NotAnAIMLPatternException 
   */
  @SuppressWarnings("static-method")
  @Test
  public void testMatchPattern_001() throws NotAnAIMLPatternException {
    assertFalse(PatternArbiter.matches("match", "MATCH", false));
  }
  
}

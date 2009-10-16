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

package org.aitools.util;
import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.aitools.util.resource.Filesystem;
import org.aitools.util.runtime.DeveloperError;
import org.exolab.javasource.JAnnotation;
import org.exolab.javasource.JAnnotationType;
import org.exolab.javasource.JClass;
import org.exolab.javasource.JComment;
import org.exolab.javasource.JMethod;
import org.exolab.javasource.JSourceWriter;

/**
 * Provides some useful Unicode functionality that doesn't seem
 * to be available elsewhere.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
@SuppressWarnings("unchecked")
public class Unicode
{
    /** The longest string that will be produced in the test methods. */
    private static final int MAX_STRING_LEN = 65535;
    
    /** A cache of already-determined Unicode block contents. */
    private static Map<String, List<char[]>> BLOCKS = new HashMap<String, List<char[]>>();
    
    /** A cache of already-determined letter contents of Unicode blocks. */
    private static Map<String, List<char[]>> LETTERS = new HashMap<String, List<char[]>>();
    
    /** A cache of already-determined uppercase letter contents of Unicode blocks. */
    private static Map<String, List<char[]>> UPPERCASE_LETTERS = new HashMap<String, List<char[]>>();
    
    /** A cache of already-determined lowercase letter contents of Unicode blocks. */
    private static Map<String, List<char[]>> LOWERCASE_LETTERS = new HashMap<String, List<char[]>>();
    
    /** The set of all available Unicode blocks. */
    private static Set<Character.UnicodeBlock> UNICODE_BLOCKS;
    
    /** {@link Character#isUpperCase(char)}. */
    private static Method CHAR_IS_UPPERCASE_METHOD;
    
    /** {@link Character#isUpperCase(int)}. */
    private static Method INT_IS_UPPERCASE_METHOD;

    /** {@link Character#isLowerCase(char)}. */
    private static Method CHAR_IS_LOWERCASE_METHOD;

    /** {@link Character#isLowerCase(int)}. */
    private static Method INT_IS_LOWERCASE_METHOD;

    /** {@link Character#isLetter(char)}. */
    private static Method CHAR_IS_LETTER_METHOD;

    /** {@link Character#isLetter(int)}. */
    private static Method INT_IS_LETTER_METHOD;

    static
    {
        // Get all Unicode blocks.
        Field mapField = null;
        try
        {
            mapField = Character.UnicodeBlock.class.getDeclaredField("map");
        }
        catch (SecurityException e)
        {
            assert false : "Unable to access Character.UnicodeBlock.map.";
        }
        catch (NoSuchFieldException e)
        {
            assert false : "Could not find Character.UnicodeBlock.map.";
        }
        if (mapField != null)
        {
            mapField.setAccessible(true);
            Map<String, Character.UnicodeBlock> map = null;
            try
            {
                map = (Map<String, Character.UnicodeBlock>)mapField.get(null);
            }
            catch (IllegalArgumentException e)
            {
                assert false : "Character.UnicodeBlock.map does not appear as a static variable.";
            }
            catch (IllegalAccessException e)
            {
                assert false : "Not allowed to access Character.UnicodeBlock.map.";
            }
            if (map != null)
            {
                UNICODE_BLOCKS = new HashSet<Character.UnicodeBlock>(map.values());
        
                // Get the isUpperCase, isLowerCase, and isLetter methods of Character.
                try
                {
                    CHAR_IS_UPPERCASE_METHOD = Character.class.getMethod("isUpperCase", char.class);
                    INT_IS_UPPERCASE_METHOD = Character.class.getMethod("isUpperCase", int.class);
                    CHAR_IS_LOWERCASE_METHOD = Character.class.getMethod("isLowerCase", char.class);
                    INT_IS_LOWERCASE_METHOD = Character.class.getMethod("isLowerCase", int.class);
                    CHAR_IS_LETTER_METHOD = Character.class.getMethod("isLetter", char.class);
                    INT_IS_LETTER_METHOD = Character.class.getMethod("isLetter", int.class);
                }
                catch (SecurityException e)
                {
                    assert false : "Denied access to well-known method of Character.";
                }
                catch (NoSuchMethodException e)
                {
                    assert false : "Well-known method of Character does not exist.";
                }
            }
        }
    }
    
    /**
     * Returns an array containing every character that is a member of
     * the named Unicode block name.
     * 
     * @param blockName
     * @return an array of chars
     */
    public static List<char[]> allCharactersIn(String blockName)
    {
        if (BLOCKS.containsKey(blockName))
        {
            return BLOCKS.get(blockName);
        }

        // Will throw IllegalArgumentException if blockName is not valid.
        Character.UnicodeBlock block = Character.UnicodeBlock.forName(blockName);
        List<char[]> characters = new ArrayList<char[]>();
        
        for (int codePoint = 0; codePoint < 0x10ffff; codePoint++)
        {
            if (Character.isDefined(codePoint))
            {
                if (Character.UnicodeBlock.of(codePoint).equals(block))
                {
                    characters.add(Character.toChars(codePoint));
                }
            }
        }
        BLOCKS.put(blockName, characters);
        return characters;
    }

    /**
     * Returns an array containing every letter that is a member of
     * the named Unicode block name.
     * 
     * @param blockName
     * @return a list of char arrays
     */
    public static List<char[]> allLettersIn(String blockName)
    {
        return allQualifyingCharactersIn(blockName, LETTERS, CHAR_IS_LETTER_METHOD, INT_IS_LETTER_METHOD, false);
    }
    
    /**
     * Returns an array containing every uppercase character that is a member of
     * the named Unicode block name.  <b>However</b>, if the block does not
     * contain <i>any</i> uppercase characters, then it is likely that this
     * is a block for which case folding is not an operative concept; in such cases,
     * this will return all characters which are letters.
     * 
     * @param blockName
     * @return a list of char arrays
     */
    public static List<char[]> allUppercaseCharactersIn(String blockName)
    {
        return allQualifyingCharactersIn(blockName, UPPERCASE_LETTERS, CHAR_IS_UPPERCASE_METHOD, INT_IS_UPPERCASE_METHOD, true);
    }
    
    /**
     * Returns an array containing every lowercase character that is a member of
     * the named Unicode block name.  <b>However</b>, if the block does not
     * contain <i>any</i> lowercase characters, then it is likely that this
     * is a block for which case folding is not an operative concept; in such cases,
     * this will return all characters which are letters.
     * 
     * @param blockName
     * @return a list of char arrays
     */
    public static List<char[]> allLowercaseCharactersIn(String blockName)
    {
        return allQualifyingCharactersIn(blockName, LOWERCASE_LETTERS, CHAR_IS_LOWERCASE_METHOD, INT_IS_LOWERCASE_METHOD, true);
    }
    
    /**
     * Returns an array containing every "qualifying" character that is a member of
     * the named Unicode block name.  "Qualifying" is determined by the <code>qualifies</code>
     * method.  If <code>includeAllIfNoneQualify</code> is true, then if the block does not
     * contain <i>any</i> qualifying characters, then all characters which are letters
     * will be returned instead.
     * 
     * @param blockName the name of the block of characters
     * @param caseMap the map of blockNames to lists of qualifying characters
     * @param charQualifies the method that will return a boolean indicating whether a character qualifies
     * @param intQualifies the method that will return a boolean indicating whether a codepoint (int) qualifies
     * @param includeAllIfNoneQualify whether to include all characters in the result if none qualify
     * @return a list of char arrays
     */
    @SuppressWarnings("boxing")
    public static List<char[]> allQualifyingCharactersIn(String blockName, Map<String, List<char[]>> caseMap, Method charQualifies, Method intQualifies, boolean includeAllIfNoneQualify)
    {
        if (caseMap.containsKey(blockName))
        {
            return caseMap.get(blockName);
        }
        
        List<char[]> wholeSet;
        if (charQualifies == CHAR_IS_LETTER_METHOD && intQualifies == INT_IS_LETTER_METHOD)
        {
            wholeSet = allCharactersIn(blockName);
        }
        else
        {
            wholeSet = allLettersIn(blockName);
        }
        List<char[]> qualifiers = new ArrayList<char[]>();
        
        for (char[] candidate : wholeSet)
        {
            try
            {
                if (candidate.length == 1 && ((Boolean)charQualifies.invoke(Character.class, candidate[0])).booleanValue())
                {
                    qualifiers.add(candidate);
                }
                else if (candidate.length == 2 && ((Boolean)intQualifies.invoke(Character.class, Character.toCodePoint(candidate[0], candidate[1]))).booleanValue())
                {
                    qualifiers.add(candidate);
                }
            }
            catch (IllegalArgumentException e)
            {
                throw new DeveloperError(String.format("Did not provide meaningful arguments to qualifies method \"%s\" or \"%s\".", charQualifies.getName(), intQualifies.getName()), e);
            }
            catch (IllegalAccessException e)
            {
                throw new DeveloperError(String.format("Qualifies method \"%s\" or \"%s\" is not accessible.", charQualifies.getName(), intQualifies.getName()), e);
            }
            catch (InvocationTargetException e)
            {
                throw new DeveloperError(String.format("Qualifies method \"%s\" or \"%s\" threw an exception.", charQualifies.getName(), intQualifies.getName()), e);
            }
        }
        if (qualifiers.size() > 0)
        {
            caseMap.put(blockName, qualifiers);
            return qualifiers;
        }
        if (includeAllIfNoneQualify)
        {
            caseMap.put(blockName, wholeSet);
            return wholeSet;
        }
        return qualifiers;
    }
    
    private static final JAnnotationType TEST_ANNOTATION_TYPE = new JAnnotationType("Test");
    
    /**
     * This is a perhaps ridiculous set of tests.
     * Obviously it is just going to test whether the Unicode blocks on the testing machine's
     * JVM are the same as on the compiling machine's.
     * 
     * @param argv one argument, the directory in which to write the UnicodeTest.java file
     */
    public static void main(String[] argv)
    {
        JClass clazz = new JClass("org.aitools.util.UnicodeTest");

        JComment header = new JComment(JComment.HEADER_STYLE);
        header.setComment(License.TEXT);
        clazz.setHeader(header);
        
        clazz.addImport("static org.junit.Assert.*");
        clazz.addImport("org.junit.Test");

        // Sort all the blocks alphabetically.
        TreeMap<String, Character.UnicodeBlock> sortedBlocks = new TreeMap<String, Character.UnicodeBlock>();
        for (Character.UnicodeBlock block : UNICODE_BLOCKS)
        {
            sortedBlocks.put(block.toString(), block);
        }
        // Create tests for every block.
        for (Character.UnicodeBlock block : sortedBlocks.values())
        {
            String blockName = block.toString();
            List<char[]> letters = allLettersIn(blockName);
            int letterCount = letters.size();
            addTestMethod(clazz, "Letters", blockName, letters, letterCount);
            addTestMethod(clazz, "UppercaseCharacters", blockName, allUppercaseCharactersIn(blockName), letterCount);
            addTestMethod(clazz, "LowercaseCharacters", blockName, allLowercaseCharactersIn(blockName), letterCount);
        }
        
        PrintWriter out = Filesystem.checkOrCreatePrintWriter(argv[0] + File.separator + "UnicodeTest.java", "Unicode test file");
        JSourceWriter writer = new JSourceWriter(out);
        clazz.print(writer);
        writer.close();
        out.close();
    }
    
    private static void addTestMethod(JClass clazz, String characterType, String blockName, List<char[]> characters, int letterCount)
    {
        if (letterCount > 0)
        {
            addTestAllMethod(clazz, characterType, blockName, characters);
        }
        else
        {
            addTestZeroMethod(clazz, characterType, blockName);
        }
    }
    
    private static void addTestAllMethod(JClass clazz, String characterType, String blockName, List<char[]> characters)
    {
        JMethod method = new JMethod(String.format("testAll%sIn%s", characterType, blockName));
        method.addAnnotation(new JAnnotation(TEST_ANNOTATION_TYPE));
        
        // Avoid making quoted strings longer than MAX_STRING_LEN bytes.
        String characterString = Text.merge(characters);
        StringBuilder quotedCharacters = new StringBuilder();
        int stringLength = characterString.getBytes().length;
        if (stringLength > MAX_STRING_LEN)
        {
            CharsetEncoder encoder = Charset.defaultCharset().newEncoder();
            ByteBuffer buffer;
            try
            {
                buffer = encoder.encode(CharBuffer.wrap(characterString));
            }
            catch (CharacterCodingException e)
            {
                throw new DeveloperError("Could not encode string.", e);
            }
            int byteCount = buffer.remaining();
            for (int index = 0; index < byteCount; index += MAX_STRING_LEN)
            {
                if (quotedCharacters.length() > 0)
                {
                    quotedCharacters.append("\" + \n");
                }
                int length = Math.min(MAX_STRING_LEN, buffer.remaining());
                byte[] bytes = new byte[length];
                buffer.get(bytes, 0, length);
                ByteBuffer segment = ByteBuffer.wrap(bytes);
                quotedCharacters.append("\"");
                quotedCharacters.append(segment.asCharBuffer());
            }
            quotedCharacters.append("\"");
        }
        else
        {
            quotedCharacters.append(String.format("\"%s\"", characterString));
        }
        method.setSourceCode(String.format("assertEquals(Text.merge(Unicode.all%sIn(\"%s\")), %s);", characterType, blockName, quotedCharacters.toString()));
        clazz.addMember(method);
        
    }
    
    private static void addTestZeroMethod(JClass clazz, String characterType, String blockName)
    {
        JMethod method = new JMethod(String.format("testZero%sIn%s", characterType, blockName));
        method.addAnnotation(new JAnnotation(TEST_ANNOTATION_TYPE));
        method.setSourceCode(String.format("assertTrue(Unicode.all%sIn(\"%s\").size() == 0);", characterType, blockName));
        clazz.addMember(method);
    }
}

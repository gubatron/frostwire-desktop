/*
 * Copyright (c) 2003-2010 JGoodies Karsten Lentzsch. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  o Neither the name of JGoodies Karsten Lentzsch nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.jgoodies.common.base;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;

/**
 * A test case for class {@link Strings}.
 *
 * @author  Karsten Lentzsch
 * @version $Revision: 1.4 $
 */
public final class StringsTest extends TestCase {

    private static final String EMPTY              = "";
    private static final String BLANK              = "  ";

    private static final String SPACE_TEXT         = " abc";
    private static final String TEXT_SPACE         = "abc ";
    private static final String SPACE_TEXT_SPACE   = " abc ";


    // String Tests ***********************************************************

    @Test
    public void testIsBlank() {
        assertTrue ("Null is blank",                 Strings.isBlank(null));
        assertTrue ("EMPTY is blank",                Strings.isBlank(EMPTY));
        assertTrue ("BLANK is blank",                Strings.isBlank(BLANK));
        assertFalse("SPACE_TEXT is not blank",       Strings.isBlank(SPACE_TEXT));
        assertFalse("TEXT_SPACE is not blank",       Strings.isBlank(TEXT_SPACE));
        assertFalse("SPACE_TEXT_SPACE is not blank", Strings.isBlank(SPACE_TEXT_SPACE));
    }


    @Test
    public void testIsNotBlank() {
        assertFalse("Null is not not-blank",         Strings.isNotBlank(null));
        assertFalse("EMPTY is not not-blank",        Strings.isNotBlank(EMPTY));
        assertFalse("BLANK is not not-blank",        Strings.isNotBlank(BLANK));
        assertTrue ("SPACE_TEXT is not-blank",       Strings.isNotBlank(SPACE_TEXT));
        assertTrue ("TEXT_SPACE is not-blank",       Strings.isNotBlank(TEXT_SPACE));
        assertTrue ("SPACE_TEXT_SPACE is not-blank", Strings.isNotBlank(SPACE_TEXT_SPACE));
    }


    @Test
    public void testIsEmpty() {
        assertTrue ("Null is empty",                 Strings.isEmpty(null));
        assertTrue ("EMPTY is empty",                Strings.isEmpty(EMPTY));
        assertFalse("BLANK is not empty",            Strings.isEmpty(BLANK));
        assertFalse("SPACE_TEXT is not blank",       Strings.isEmpty(SPACE_TEXT));
        assertFalse("TEXT_SPACE is not blank",       Strings.isEmpty(TEXT_SPACE));
        assertFalse("SPACE_TEXT_SPACE is not blank", Strings.isEmpty(SPACE_TEXT_SPACE));
    }


    @Test
    public void testIsNotEmpty() {
        assertFalse("Null is not not-empty",         Strings.isNotEmpty(null));
        assertFalse("EMPTY is not not-empty",        Strings.isNotEmpty(EMPTY));
        assertTrue ("BLANK is not-empty",            Strings.isNotEmpty(BLANK));
        assertTrue ("SPACE_TEXT is not-empty",       Strings.isNotEmpty(SPACE_TEXT));
        assertTrue ("TEXT_SPACE is not-empty",       Strings.isNotEmpty(TEXT_SPACE));
        assertTrue ("SPACE_TEXT_SPACE is not-empty", Strings.isNotEmpty(SPACE_TEXT_SPACE));
    }


    // Operations *********************************************************

    @Test
    public void testAbbreviateCenter() {
        final String empty = "";
        final String blank = " ";
        final String abc = "abc";
        Assert.assertSame(null,  Strings.abbreviateCenter(null, 3));
        Assert.assertSame(empty, Strings.abbreviateCenter(empty, 3));
        Assert.assertSame(blank, Strings.abbreviateCenter(blank, 3));
        Assert.assertSame(abc,   Strings.abbreviateCenter(abc, 3));

        Assert.assertEquals("a\u2026d",   Strings.abbreviateCenter("abcd",    3));
        Assert.assertEquals("a\u2026e",   Strings.abbreviateCenter("abcde",   3));
        Assert.assertEquals("ab\u2026e",  Strings.abbreviateCenter("abcde",   4));
        Assert.assertEquals("ab\u2026f",  Strings.abbreviateCenter("abcdef",  4));
        Assert.assertEquals("ab\u2026fg", Strings.abbreviateCenter("abcdefg", 5));
    }

}

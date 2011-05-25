/*
 * Copyright (c) 2009-2010 JGoodies Karsten Lentzsch. All Rights Reserved.
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

package com.jgoodies.common.format;

import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests classes {@link EmptyDateFormat} and {@link EmptyNumberFormat}.
 *
 * @author  Karsten Lentzsch
 * @version $Revision: 1.1 $
 */
public final class FormatTest {

    @SuppressWarnings("deprecation")
    @Test
    public void emptyDateFormat() {
        Date day1 = new Date(1967, 11, 5);
        Date day2 = new Date(2010, 4,  17);
        DateFormat defaultFormat = DateFormat.getDateInstance();
        DateFormat emptyFormat1 = new EmptyDateFormat(defaultFormat);
        DateFormat emptyFormat2 = new EmptyDateFormat(defaultFormat, day2);
        String formatted = defaultFormat.format(day1);
        testParseDate("Empty",          emptyFormat1,  "",        null);
        testParseDate("Whitespace",     emptyFormat1,  "  ",      null);
        testParseDate("Empty",          emptyFormat2,  "",        day2);
        testParseDate("Whitespace",     emptyFormat2,  "  ",      day2);
        testParseDate("Day1 (raw)",     defaultFormat, formatted, day1);
        testParseDate("Day1 (wrapped)", emptyFormat1,  formatted, day1);
        testParseDate("Day1 (wrapped)", emptyFormat2,  formatted, day1);
    }


    @Test
    public void emptyNumberFormat() {
        Number number1 = Long.valueOf(42);
        Number number2 = Double.valueOf(1.03d);
        NumberFormat defaultFormat = NumberFormat.getInstance();
        NumberFormat emptyFormat1 = new EmptyNumberFormat(defaultFormat);
        NumberFormat emptyFormat2 = new EmptyNumberFormat(defaultFormat, number2);
        String formatted = defaultFormat.format(number1);
        testParseNumber("Empty",             emptyFormat1,  "",        null);
        testParseNumber("Whitespace",        emptyFormat1,  "  ",      null);
        testParseNumber("Empty",             emptyFormat2,  "",        number2);
        testParseNumber("Whitespace",        emptyFormat2,  "  ",      number2);
        testParseNumber("Number1 (raw)",     defaultFormat, formatted, number1);
        testParseNumber("Number1 (wrapped)", emptyFormat1,  formatted, number1);
        testParseNumber("Number1 (wrapped)", emptyFormat2,  formatted, number1);
    }


    // Helper Code ************************************************************

    private void testParseDate(String message, DateFormat format, String source, Date expected) {
        try {
            Object actual = format.parse(source);
            assertEquals(message, expected, actual);
        } catch (ParseException e) {
            Assert.fail(e.getLocalizedMessage());
        }
    }


    private void testParseNumber(String message, NumberFormat format, String source, Number expected) {
        try {
            Object actual = format.parse(source);
            assertEquals(message, expected, actual);
        } catch (ParseException e) {
            Assert.fail(e.getLocalizedMessage());
        }
    }


}

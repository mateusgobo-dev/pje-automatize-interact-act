package br.com.infox.component.agenda;

/*
 * Copyright (c) Ian F. Darwin, http://www.darwinsys.com/, 1996-2002.
 * All rights reserved. Software written by Ian F. Darwin and others.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS''
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * Java, the Duke mascot, and all variants of Sun's Java "steaming coffee
 * cup" logo are trademarks of Sun Microsystems. Sun's, and James Gosling's,
 * pioneering role in inventing and promulgating (and standardizing) the Java 
 * language and environment is gratefully acknowledged.
 * 
 * The pioneering role of Dennis Ritchie and Bjarne Stroustrup, of AT&T, for
 * inventing predecessor languages C and C++ is also gratefully acknowledged.
 */

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Easter - compute the day on which Easter falls.
 * 
 * In the Christian religion, Easter is possibly the most important holiday of
 * the year, so getting its date <I>just so </I> is worthwhile.
 * 
 * @author: Ian F. Darwin, http://www.darwinsys.com/, based on a detailed
 *          algorithm in Knuth, vol 1, pg 155.
 * 
 *          Toronto, 1988. Java version 1996.
 * 
 * @Note: It's not proven correct, although it gets the right answer for years
 *        around the present.
 */
public class Easter {

	/*
	 * Compute the day of the year that Easter falls on. Step names E1 E2 etc.,
	 * are direct references to Knuth, Vol 1, p 155. @exception
	 * IllegalArgumentexception If the year is before 1582 (since the algorithm
	 * only works on the Gregorian calendar).
	 */
	public static final Calendar findHolyDay(int year) {
		if (year <= 1582) {
			throw new IllegalArgumentException("Algorithm invalid before April 1583");
		}
		int golden, century, x, z, d, epact, n;

		golden = (year % 19) + 1; /* E1: metonic cycle */
		century = (year / 100) + 1; /* E2: e.g. 1984 was in 20th C */
		x = (3 * century / 4) - 12; /* E3: leap year correction */
		z = ((8 * century + 5) / 25) - 5; /* E3: sync with moon's orbit */
		d = (5 * year / 4) - x - 10;
		epact = (11 * golden + 20 + z - x) % 30; /* E5: epact */
		if ((epact == 25 && golden > 11) || epact == 24)
			epact++;
		n = 44 - epact;
		n += 30 * (n < 21 ? 1 : 0); /* E6: */
		n += 7 - ((d + n) % 7);
		if (n > 31) /* E7: */
			return new GregorianCalendar(year, 4 - 1, n - 31); /* April */
		else
			return new GregorianCalendar(year, 3 - 1, n); /* March */
	}

	/** Main program, when used as a standalone application */
	public static void main(String[] argv) {

		int year = 0;
		if (argv.length == 0) {
			year = new GregorianCalendar().get(Calendar.YEAR);
		} else
			for (int i = 0; i < argv.length; i++) {
				year = 0;
				try {
					year = Integer.parseInt(argv[i]);
				} catch (IllegalArgumentException e) {
					System.err.println("Year " + argv[i] + " invalid (" + e.getMessage() + ").");
				}
			}
		System.out.println(Easter.findHolyDay(year).getTime());
	}
}
package org.aitools.programd.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * <h3>Mersenne Twister and MersenneTwisterFast</h3>
 * <p>
 * <b>Version 3 </b>, based on version MT199937(99/10/29) of the Mersenne
 * Twister algorithm found at <a
 * href="http://www.math.keio.ac.jp/matumoto/emt.html"> The Mersenne Twister
 * Home Page </a>. By Sean Luke, June 2000.
 * <p>
 * <b>MersenneTwister </b> is a drop-in subclass replacement for
 * java.util.Random. It is properly synchronized and can be used in a
 * multithreaded environment.
 * <p>
 * <b>MersenneTwisterFast </b> is not a subclass of java.util.Random. It has the
 * same public methods as Random does, however, and it is algorithmically
 * identical to MersenneTwister. MersenneTwisterFast has hard-code inlined all
 * of its methods directly, and made all of them final (well, the ones of
 * consequence anyway). Further, these methods are <i>not </i> synchronized, so
 * the same MersenneTwisterFast instance cannot be shared by multiple threads.
 * But all this helps MersenneTwisterFast achieve over twice the speed of
 * MersenneTwister.
 * <h3>About the Mersenne Twister</h3>
 * <p>
 * This is a Java version of the C-program for MT19937: Integer version. The
 * MT19937 algorithm was created by Makoto Matsumoto and Takuji Nishimura, who
 * ask: "When you use this, send an email to: matumoto@math.keio.ac.jp with an
 * appropriate reference to your work". Indicate that this is a translation of
 * their algorithm into Java.
 * <p>
 * <b>Reference. </b> Makato Matsumoto and Takuji Nishimura, "Mersenne Twister:
 * A 623-Dimensionally Equidistributed Uniform Pseudo-Random Number Generator",
 * <i>ACM Transactions on Modeling and Computer Simulation, </i> Vol. 8, No. 1,
 * January 1998, pp 3--30.
 * <h3>About this Version</h3>
 * This version of the code implements the MT19937 Mersenne Twister algorithm,
 * with the 99/10/29 seeding mechanism. The original mechanism did not permit 0
 * as a seed, and odd numbers were not good seed choices. The new version
 * permits any 32-bit signed integer. This algorithm is identical to the MT19937
 * integer algorithm; real values conform to Sun's float and double random
 * number generator standards rather than attempting to implement the half-open
 * or full-open MT19937-1 and MT199937-2 algorithms.
 * <p>
 * This code is based on standard MT19937 C/C++ code by Takuji Nishimura, with
 * suggestions from Topher Cooper and Marc Rieffel, July 1997. The code was
 * originally translated into Java by Michael Lecuyer, January 1999, and is
 * Copyright (c) 1999 by Michael Lecuyer. The included license is as follows:
 * <blockquote><font size="-1"> The basic algorithmic work of this library
 * (appearing in nextInt() and setSeed()) is free software; you can redistribute
 * it and or modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * <p>
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details. You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the Free Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA </font> </blockquote>
 * <h3>Bug Fixes</h3>
 * <p>
 * This implementation implements the bug fixes made in Java 1.2's version of
 * Random, which means it can be used with earlier versions of Java. See <a
 * href="http://www.javasoft.com/products/jdk/1.2/docs/api/java/util/Random.html">
 * the JDK 1.2 java.util.Random documentation </a> for further documentation on
 * the random-number generation contracts made. Additionally, there's an
 * undocumented bug in the JDK java.util.Random.nextBytes() method, which this
 * code fixes.
 * <h3>Important Note on Seeds</h3>
 * <p>
 * Just like java.util.Random, this generator accepts a long seed but doesn't
 * use all of it. java.util.Random uses 48 bits. The Mersenne Twister instead
 * uses 32 bits (int size). So it's best if your seed does not exceed the int
 * range.
 * <h3>Timings On Different Java Versions</h3>
 * <p>
 * MersenneTwister can be used reliably on JDK version 1.1.5 or above. Earlier
 * Java versions have serious bugs in java.util.Random; only MersenneTwisterFast
 * (and not MersenneTwister nor java.util.Random) should be used with them. And
 * why would you use 'em anyway? They're very slow, as you'll see. Here are some
 * timings in milliseconds on a Sun Creator3D/Ultra 60 running SunOS 5.6.
 * <dl>
 * <dt><i>Standard C Version (gcc -O2) </i>
 * <dd>1070
 * <dt><i>Standard C Version (Solaris cc -O) </i>
 * <dd>1210
 * <dt><i>JDK 1.2.2 w/Hotspot Compiler (-O) </i>
 * <dd>MTF: 1785, MT: 3699, java.util.Random: 4849
 * <dt><i>JDK 1.2.1/1.2.2 (-O) </i>
 * <dd>MTF: 1827, MT: 3868, java.util.Random: 4194
 * <dt><i>JDK 1.1.8 (-O) </i>
 * <dd>MTF: 40509, MT: 45853, java.util.Random: 24604 <br>
 * Beats me why it's so slow...
 * <dt><i>JDK 1.1.5 (-O) </i>
 * <dd>MTF: 4056, MT: 20478, java.util.Random: 19692
 * <dt><i>JDK 1.0.2 (-O) </i>
 * <dd>MTF: 71640, MT: 66176, java.util.Random: 67269 <br>
 * <i>Important note: </i> Do not MersenneTwister.java or java.util.Random on a
 * Java version this early! Random number generation in versions less than 1.1.5
 * has serious bugs.
 * </dl>
 * 
 * @version 3
 */

public class MersenneTwister extends java.util.Random implements Serializable
{
    // Period parameters
    private static final int N = 624;

    private static final int M = 397;

    private static final int MATRIX_A = 0x9908b0df;

    // private static final * constant vector a
    private static final int UPPER_MASK = 0x80000000;

    // most significant w-r bits
    private static final int LOWER_MASK = 0x7fffffff;

    // least significant r bits

    // Tempering parameters
    private static final int TEMPERING_MASK_B = 0x9d2c5680;

    private static final int TEMPERING_MASK_C = 0xefc60000;

    // #define TEMPERING_SHIFT_U(y) (y >>> 11)
    // #define TEMPERING_SHIFT_S(y) (y << 7)
    // #define TEMPERING_SHIFT_T(y) (y << 15)
    // #define TEMPERING_SHIFT_L(y) (y >>> 18)

    private int mt[]; // the array for the state vector

    private int mti; // mti==N+1 means mt[N] is not initialized

    private int mag01[];

    // a good initial seed (of int size, though stored in a long)
    private static final long GOOD_SEED = 4357;

    /*
     * implemented here because there's a bug in Random's implementation of the
     * Gaussian code (divide by zero, and log(0), ugh!), yet its gaussian
     * variables are private so we can't access them here. :-(
     */

    private double __nextNextGaussian;

    private boolean __haveNextNextGaussian;

    /**
     * Constructor using the default seed.
     */
    public MersenneTwister()
    {
        super(GOOD_SEED); /* just in case */
        setSeed(GOOD_SEED);
    }

    /**
     * Constructor using a given seed. Though you pass this seed in as a long,
     * it's best to make sure it's actually an integer.
     * 
     * @param seed the seed to use
     */
    public MersenneTwister(final long seed)
    {
        super(seed); /* just in case */
        setSeed(seed);
    }

    /**
     * Initalize the pseudo random number generator. This is the old
     * seed-setting mechanism for the original Mersenne Twister algorithm. You
     * must not use 0 as your seed, and don't pass in a long that's bigger than
     * an int (Mersenne Twister only uses the first 32 bits for its seed). Also
     * it's suggested that for you avoid even-numbered seeds in this older
     * seed-generation procedure.
     * 
     * @param seed the seed to use
     */

    synchronized public void setSeedOld(final long seed)
    {
        // it's always good style to call super
        super.setSeed(seed);

        // Due to a bug in java.util.Random clear up to 1.2, we're
        // doing our own Gaussian variable.
        this.__haveNextNextGaussian = false;

        this.mt = new int[N];

        // setting initial seeds to mt[N] using
        // the generator Line 25 of Table 1 in
        // [KNUTH 1981, The Art of Computer Programming
        // Vol. 2 (2nd Ed.), pp102]

        // the 0xffffffff is commented out because in Java
        // ints are always 32 bits; hence i & 0xffffffff == i

        this.mt[0] = ((int) seed); // & 0xffffffff;

        for (this.mti = 1; this.mti < N; this.mti++)
            this.mt[this.mti] = (69069 * this.mt[this.mti - 1]); // &
        // 0xffffffff;

        // mag01[x] = x * MATRIX_A for x=0,1
        this.mag01 = new int[2];
        this.mag01[0] = 0x0;
        this.mag01[1] = MATRIX_A;
    }

    /**
     * An alternative, more complete, method of seeding the pseudo random number
     * generator. array must be an array of 624 ints, and they can be any value
     * as long as they're not *all* zero.
     * 
     * @param array an array of 624 ints
     */

    synchronized public void setSeed(final int[] array)
    {
        // it's always good style to call super --
        // we'll use MT's canonical random number, but it doesn't
        // really matter.
        super.setSeed(4357);

        // Due to a bug in java.util.Random clear up to 1.2, we're
        // doing our own Gaussian variable.
        this.__haveNextNextGaussian = false;

        this.mt = new int[N];
        System.arraycopy(array, 0, this.mt, 0, N);
        this.mti = N;
        // mag01[x] = x * MATRIX_A for x=0,1
        this.mag01 = new int[2];
        this.mag01[0] = 0x0;
        this.mag01[1] = MATRIX_A;
    }

    /**
     * Initalize the pseudo random number generator. Don't pass in a long that's
     * bigger than an int (Mersenne Twister only uses the first 32 bits for its
     * seed).
     * 
     * @param seed the seed to use
     */

    synchronized public void setSeed(final long seed)
    {
        // it's always good style to call super
        super.setSeed(seed);

        // seed needs to be casted into an int first for this to work
        int _seed = (int) seed;

        // Due to a bug in java.util.Random clear up to 1.2, we're
        // doing our own Gaussian variable.
        this.__haveNextNextGaussian = false;

        this.mt = new int[N];

        for (int i = 0; i < N; i++)
        {
            this.mt[i] = _seed & 0xffff0000;
            _seed = 69069 * _seed + 1;
            this.mt[i] |= (_seed & 0xffff0000) >>> 16;
            _seed = 69069 * _seed + 1;
        }

        this.mti = N;
        // mag01[x] = x * MATRIX_A for x=0,1
        this.mag01 = new int[2];
        this.mag01[0] = 0x0;
        this.mag01[1] = MATRIX_A;
    }

    /**
     * @param bits the number of bits to use
     * @return an integer with <i>bits</i> bits filled with a random number
     */
    synchronized protected int next(final int bits)
    {
        int y;

        if (this.mti >= N) // generate N words at one time
        {
            int kk;

            for (kk = 0; kk < N - M; kk++)
            {
                y = (this.mt[kk] & UPPER_MASK) | (this.mt[kk + 1] & LOWER_MASK);
                this.mt[kk] = this.mt[kk + M] ^ (y >>> 1) ^ this.mag01[y & 0x1];
            }
            for (; kk < N - 1; kk++)
            {
                y = (this.mt[kk] & UPPER_MASK) | (this.mt[kk + 1] & LOWER_MASK);
                this.mt[kk] = this.mt[kk + (M - N)] ^ (y >>> 1) ^ this.mag01[y & 0x1];
            }
            y = (this.mt[N - 1] & UPPER_MASK) | (this.mt[0] & LOWER_MASK);
            this.mt[N - 1] = this.mt[M - 1] ^ (y >>> 1) ^ this.mag01[y & 0x1];

            this.mti = 0;
        }

        y = this.mt[this.mti++];
        y ^= y >>> 11; // TEMPERING_SHIFT_U(y)
        y ^= (y << 7) & TEMPERING_MASK_B; // TEMPERING_SHIFT_S(y)
        y ^= (y << 15) & TEMPERING_MASK_C; // TEMPERING_SHIFT_T(y)
        y ^= (y >>> 18); // TEMPERING_SHIFT_L(y)

        return y >>> (32 - bits); // hope that's right!
    }

    /*
     * If you've got a truly old version of Java, you can omit these two next
     * methods.
     */

    private synchronized void writeObject(final ObjectOutputStream out) throws IOException
    {
        // just so we're synchronized.
        out.defaultWriteObject();
    }

    private synchronized void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        // just so we're synchronized.
        in.defaultReadObject();
    }

    /**
     * This generates a coin flip with a probability <tt>probability</tt> of
     * returning true, else returning false. <tt>probability</tt> must be
     * between 0.0 and 1.0, inclusive. Not as precise a random real event as
     * nextBoolean(double), but twice as fast. To explicitly use this, remember
     * you may need to cast to float first.
     * 
     * @param probability the probability to use (between 0.0 and 1.0)
     * @return the coin flip result
     */

    public boolean nextBoolean(final float probability)
    {
        if (probability < 0.0f || probability > 1.0f)
            throw new IllegalArgumentException("probability must be between 0.0 and 1.0 inclusive.");
        return nextFloat() < probability;
    }

    /**
     * This generates a coin flip with a probability <tt>probability</tt> of
     * returning true, else returning false. <tt>probability</tt> must be
     * between 0.0 and 1.0, inclusive.
     * 
     * @param probability must be between 0.0 and 1.0
     * @return the result of the coin flip
     */

    public boolean nextBoolean(final double probability)
    {
        if (probability < 0.0 || probability > 1.0)
            throw new IllegalArgumentException("probability must be between 0.0 and 1.0 inclusive.");
        return nextDouble() < probability;
    }

    /**
     * A bug fix for all versions of the JDK. The JDK appears to use all four
     * bytes in an integer as independent byte values! Totally wrong. I've
     * submitted a bug report.
     * 
     * @param bytes the bytes for which to get the next bytes (?)
     */

    public void nextBytes(final byte[] bytes)
    {
        for (int x = 0; x < bytes.length; x++)
            bytes[x] = (byte) next(8);
    }

    /**
     * For completeness' sake, though it's not in java.util.Random.
     * 
     * @return the next char
     */
    public char nextChar()
    {
        // chars are 16-bit UniCode values
        return (char) (next(16));
    }

    /**
     * For completeness' sake, though it's not in java.util.Random.
     * 
     * @return the next short
     */

    public short nextShort()
    {
        return (short) (next(16));
    }

    /**
     * For completeness' sake, though it's not in java.util.Random.
     * 
     * @return the next byte
     */

    public byte nextByte()
    {
        return (byte) (next(8));
    }

    /**
     * A bug fix for all JDK code including 1.2. nextGaussian can theoretically
     * ask for the log of 0 and divide it by 0! See Java bug <a
     * href="http://developer.java.sun.com/developer/bugParade/bugs/4254501.html">
     * http://developer.java.sun.com/developer/bugParade/bugs/4254501.html </a>
     * 
     * @return the next Gaussian
     */

    synchronized public double nextGaussian()
    {
        if (this.__haveNextNextGaussian)
        {
            this.__haveNextNextGaussian = false;
            return this.__nextNextGaussian;
        }
        // (otherwise...)
        double v1, v2, s;
        do
        {
            v1 = 2 * nextDouble() - 1; // between -1.0 and 1.0
            v2 = 2 * nextDouble() - 1; // between -1.0 and 1.0
            s = v1 * v1 + v2 * v2;
        }while (s >= 1 || s == 0);
        double multiplier = Math.sqrt(-2 * Math.log(s) / s);
        this.__nextNextGaussian = v2 * multiplier;
        this.__haveNextNextGaussian = true;
        return v1 * multiplier;
    }

    /**
     * Tests the code.
     * 
     * @param args not used
     */
    public static void main(String args[])
    {
        int j;

        MersenneTwister r;

        // UNCOMMENT THIS TO TEST FOR CORRECTNESS
        // WITH ORIGINAL ALGORITHM
        /*
         * r = new MersenneTwister(4357); r.setSeedOld(4357)
         * System.out.println("Output of MersenneTwister, old style"); for
         * (j=0;j <1000;j++) { // first, convert the int from signed to
         * "unsigned" long l = (long)r.nextInt(); if (l < 0 ) l += 4294967296L; //
         * max int value String s = String.valueOf(l); while(s.length() < 10) s = " " +
         * s; // buffer System.out.print(s + " "); if (j%8==7)
         * System.out.println(); }
         */

        // UNCOMMENT THIS TO TEST FOR CORRECTNESS WITH
        // NEW VERSION MT19937 1999/10/28
        // COMPARE WITH
        // http://www.math.keio.ac.jp/~nisimura/random/int/mt19937int.out
        /*
         * r = new MersenneTwister(4357); System.out.println("Output of
         * MersenneTwister with new (1999/10/28) seeding mechanism"); for (j=0;j
         * <1000;j++) { // first, convert the int from signed to "unsigned" long
         * l = (long)r.nextInt(); if (l < 0 ) l += 4294967296L; // max int value
         * String s = String.valueOf(l); while(s.length() < 10) s = " " + s; //
         * buffer System.out.print(s + " "); if (j%5==4) System.out.println(); }
         */

        // UNCOMMENT THIS TO TEST FOR SPEED
        /*
         * r = new MersenneTwister(); System.out.println("\nTime to test
         * grabbing 10000000 ints"); long ms = System.currentTimeMillis(); int
         * xx=0; for (j = 0; j < 10000000; j++) xx += r.nextInt();
         * System.out.println("Mersenne Twister: " +
         * (System.currentTimeMillis()-ms + " Ignore this: " + xx)); Random rr =
         * new Random(1); xx = 0; ms = System.currentTimeMillis(); for (j = 0; j <
         * 10000000; j++) xx += rr.nextInt();
         * System.out.println("java.util.Random: " +
         * (System.currentTimeMillis()-ms + " Ignore this: " + xx));
         */

        // UNCOMMENT THIS TO DO TEST DIFFERENT TYPE OUTPUTS
        // THIS CAN BE USED TO COMPARE THE DIFFERENCE BETWEEN
        // MersenneTwisterFast.java AND MersenneTwister.java
        System.out.println("\nGrab the first 1000 booleans");
        r = new MersenneTwister();
        for (j = 0; j < 1000; j++)
        {
            System.out.print(r.nextBoolean() + " ");
            if (j % 8 == 7)
                System.out.println();
        }
        if (!(j % 8 == 7))
            System.out.println();

        System.out.println("\nGrab 1000 booleans of increasing probability using nextBoolean(double)");
        r = new MersenneTwister();
        for (j = 0; j < 1000; j++)
        {
            System.out.print(r.nextBoolean((j / 999.0)) + " ");
            if (j % 8 == 7)
                System.out.println();
        }
        if (!(j % 8 == 7))
            System.out.println();

        System.out.println("\nGrab 1000 booleans of increasing probability using nextBoolean(float)");
        r = new MersenneTwister();
        for (j = 0; j < 1000; j++)
        {
            System.out.print(r.nextBoolean((j / 999.0f)) + " ");
            if (j % 8 == 7)
                System.out.println();
        }
        if (!(j % 8 == 7))
            System.out.println();

        byte[] bytes = new byte[1000];
        System.out.println("\nGrab the first 1000 bytes using nextBytes");
        r = new MersenneTwister();
        r.nextBytes(bytes);
        for (j = 0; j < 1000; j++)
        {
            System.out.print(bytes[j] + " ");
            if (j % 16 == 15)
                System.out.println();
        }
        if (!(j % 16 == 15))
            System.out.println();

        byte b;
        System.out.println("\nGrab the first 1000 bytes -- must be same as nextBytes");
        r = new MersenneTwister();
        for (j = 0; j < 1000; j++)
        {
            System.out.print((b = r.nextByte()) + " ");
            if (b != bytes[j])
                System.out.print("BAD ");
            if (j % 16 == 15)
                System.out.println();
        }
        if (!(j % 16 == 15))
            System.out.println();

        System.out.println("\nGrab the first 1000 shorts");
        r = new MersenneTwister();
        for (j = 0; j < 1000; j++)
        {
            System.out.print(r.nextShort() + " ");
            if (j % 8 == 7)
                System.out.println();
        }
        if (!(j % 8 == 7))
            System.out.println();

        System.out.println("\nGrab the first 1000 ints");
        r = new MersenneTwister();
        for (j = 0; j < 1000; j++)
        {
            System.out.print(r.nextInt() + " ");
            if (j % 4 == 3)
                System.out.println();
        }
        if (!(j % 4 == 3))
            System.out.println();

        System.out.println("\nGrab the first 1000 ints of different sizes");
        r = new MersenneTwister();
        for (j = 0; j < 1000; j++)
        {
            System.out.print(r.nextInt(j + 1) + " ");
            if (j % 4 == 3)
                System.out.println();
        }
        if (!(j % 4 == 3))
            System.out.println();

        System.out.println("\nGrab the first 1000 longs");
        r = new MersenneTwister();
        for (j = 0; j < 1000; j++)
        {
            System.out.print(r.nextLong() + " ");
            if (j % 3 == 2)
                System.out.println();
        }
        if (!(j % 3 == 2))
            System.out.println();

        System.out.println("\nGrab the first 1000 floats");
        r = new MersenneTwister();
        for (j = 0; j < 1000; j++)
        {
            System.out.print(r.nextFloat() + " ");
            if (j % 4 == 3)
                System.out.println();
        }
        if (!(j % 4 == 3))
            System.out.println();

        System.out.println("\nGrab the first 1000 doubles");
        r = new MersenneTwister();
        for (j = 0; j < 1000; j++)
        {
            System.out.print(r.nextDouble() + " ");
            if (j % 3 == 2)
                System.out.println();
        }
        if (!(j % 3 == 2))
            System.out.println();

        System.out.println("\nGrab the first 1000 gaussian doubles");
        r = new MersenneTwister();
        for (j = 0; j < 1000; j++)
        {
            System.out.print(r.nextGaussian() + " ");
            if (j % 3 == 2)
                System.out.println();
        }
        if (!(j % 3 == 2))
            System.out.println();

    }

}
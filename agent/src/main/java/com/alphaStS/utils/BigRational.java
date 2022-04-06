package com.alphaStS.utils;

import java.math.BigInteger;

/**
 * Immutable class representing the rationals. Backed by BigInteger i.e. absolutely no overflow. Support very basic arithmetic.
 * The rational will always be in lowest reduced form.
 */
public final class BigRational implements Comparable<BigRational> {
    final public static BigRational ZERO = BigRational.valueOf(0);
    final public static BigRational ONE = BigRational.valueOf(1);

    final private BigInteger num;
    final private BigInteger den;

    /**
     * Constructs a rational number with the given numerator and denominator.
     *
     * @param numerator
     * @param denominator
     */
    public BigRational(long numerator, long denominator) {
        this(BigInteger.valueOf(numerator), BigInteger.valueOf(denominator));
    }

	/**
	 * Constructs a rational number with the given numerator and denominator.
	 * @param numerator
	 * @param denominator
	 */
	public BigRational(BigInteger numerator, BigInteger denominator) {
		int c = denominator.compareTo(BigInteger.ZERO);
		if (c == 0) throw new ArithmeticException("BigRational: denominator cannot be 0");
		if (c < 0) {
			denominator = denominator.negate();
			numerator = denominator.negate();
		}
		BigInteger a = numerator.gcd(denominator);
		den = denominator.divide(a);
		num = numerator.divide(a);
    }

    public static BigRational valueOf(BigInteger n) {
        return new BigRational(n, BigInteger.ONE);
    }

    public static BigRational valueOf(long n) {
        return new BigRational(BigInteger.valueOf(n), BigInteger.ONE);
    }

    public BigInteger getNumerator() {
        return num;
    }

    public BigInteger getDenominator() {
        return den;
    }

	public BigRational inverse() {
		return new BigRational(den, num);
	}

	public BigRational negate() {
		return new BigRational(num.negate(), den);
	}

	public BigRational add(BigRational rat) {
		return new BigRational(this.num.multiply(rat.den).add(rat.num.multiply(this.den)), this.den.multiply(rat.den));
    }

    public BigRational subtract(BigRational rat) {
        return new BigRational(this.num.multiply(rat.den).subtract(rat.num.multiply(this.den)), this.den.multiply(rat.den));
    }

    public BigRational multiply(BigRational rat) {
        return new BigRational(rat.num.multiply(this.num), rat.den.multiply(this.den));
    }

    public BigRational divide(BigRational rat) {
        return new BigRational(this.num.multiply(rat.den), this.den.multiply(rat.num));
    }


	@Override
	public int compareTo(BigRational o) {
		int c1 = num.compareTo(o.num);
		int c2 = den.compareTo(o.den);
		if (c1 == 0) {
			if (c2 > 0)       return -1;
			else if (c2 < 0) return 1;
			return 0;
		}
		if (c2 == 0) {
			return c1 > 0 ? 1 : -1;
		}
		return num.multiply(o.den).compareTo(o.num.multiply(den)) > 0 ? 1 : -1;
	}

    public double toDouble() {
        return num.longValueExact() / (double) den.longValueExact();
    }

	/**
	 * Return a copy of the current rational number.
	 * @return a copy of current rational
	 */
	public BigRational copy() {
		return new BigRational(num, den);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		BigRational rational = (BigRational) o;

		if (!den.equals(rational.den)) return false;
		return !num.equals(rational.num);
	}

	@Override
	public int hashCode() {
		int result = num.hashCode();
		result = 31 * result + den.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "BigRational{" +
				"num=" + num +
				", den=" + den +
				'}';
	}
}

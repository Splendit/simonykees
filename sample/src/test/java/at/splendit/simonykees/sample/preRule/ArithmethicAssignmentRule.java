package at.splendit.simonykees.sample.preRule;

@SuppressWarnings("nls")
public class ArithmethicAssignmentRule {

	private int int_a = 3;
	private int int_c = 3;
	private int int_b = 3;
	private int int_q = 3;

	public String a(String i) {
		i = i + "aaa"; 
		return i;
	}

	/*
	 * INTEGER
	 * use this sed to convert to other datatypes:
	 * sed -r 's_int\b_double_g;s_\bi\b_d_g'
	 */

	public int testA(int i) {
		i = (int) i + 1;
		return i;
	}

	public int testB(int i) {
		i = 7 + 4 - 3;
		return i;
	}

	public int testC(int i) {
		i = i + 3;
		return i;
	}

	public int testD(int i) {
		i = i - 3;
		return i;
	}

	public int testE(int i) {
		i = i + 4 - 3;
		return i;
	}

	public int testF(int i) {
		i = i + 4 + 3;
		return i;
	}

	public int testG(int i) {
		i = 3 + i + 4;
		return i;
	}

	public int testH(int i) {
		i = 3 + i - 4;
		return i;
	}

	public int testI(int i) {
		i = int_a + i - 4;
		return i;
	}

	public int testJ(int i) {
		i = int_a + int_b;
		return i;
	}

	public int testK(int i) {
		i = int_a + i;
		return i;
	}

	public int testL(int i) {
		i = i * int_a;
		return i;
	}

	public int testM(int i) {
		i = 4 * i;
		return i;
	}

	public int testN(int i) {
		i = (3 + 4) * i;
		return i;
	}

	public int testO(int i) {
		i = int_a * int_b / int_c * int_q * 3 - i;
		return i;
	}

	public int testP(int i) {
		i = i / 4;
		return i;
	}

	public int testQ(int i) {
		i = 1 + 2 + 3 + 4 + i;
		return i;
	}

	public int testR(int i) {
		i = 1 + 2 + i + 4 + i;
		return i;
	}

	public int testS(int i) {
		i = 1 * 2 + 4 * i;
		return i;
	}

	public int testT(int i) {
		i = i * 4 + i;
		return i;
	}

	public int testU(int i) {
		i = 1 - i + i + 4 + i;
		return i;
	}

	public int testV(int i) {
		i = i * (2 + 4 + i);
		return i;
	}
	
	/*
	 * Corner cases
	 */

	/* FIXME see SIM-94
	public Integer cornerCaseInteger(Integer i) {
		i = i + 1;
		return i;
	}
	
	public Double cornerCaseDouble(Double d) {
		d = d + 1;
		return d;
	}
	
	public Float cornerCaseFloat(Float f) {
		f = f + 1;
		return f;
	}
	
	public Long cornerCaseLong(Long l) {
		l = l + 1;
		return l;
	}
	
	public Short cornerCaseShort(Short s) {
		s = (short) (s + 1);
		return s;
	}
	
	public Byte cornerCaseByte(Byte b) {
		b = (byte) (b + 1);
		return b;
	}
	
	public Character cornerCaseCharacter(Character c) {
		c = (char) (c + 1);
		return c;
	}
	
	public CharSequence cornerCaseByte(CharSequence cs) {
		cs = cs + "a" + 'b';
		return cs;
	}
	
	public Number cornerCaseNumber(Number n) {
		n = (Integer)n + 27;
		return n;
	}
	*/
	
    public double addMaxInt(int i) {
        double d = Integer.MAX_VALUE;
        d = ((double)i + Integer.MAX_VALUE) + d;
        return d;
    }
    
    /*
     * DOUBLE
     */
    
	public double testA(double d) {
		d = (double) d + 1;
		return d;
	}

	public double testB(double d) {
		d = 7 + 4 - 3;
		return d;
	}

	public double testC(double d) {
		d = d + 3;
		return d;
	}

	public double testD(double d) {
		d = d - 3;
		return d;
	}

	public double testE(double d) {
		d = d + 4 - 3;
		return d;
	}

	public double testF(double d) {
		d = d + 4 + 3;
		return d;
	}

	public double testG(double d) {
		d = 3 + d + 4;
		return d;
	}

	public double testH(double d) {
		d = 3 + d - 4;
		return d;
	}

	public double testI(double d) {
		d = int_a + d - 4;
		return d;
	}

	public double testJ(double d) {
		d = int_a + int_b;
		return d;
	}

	public double testK(double d) {
		d = int_a + d;
		return d;
	}

	public double testL(double d) {
		d = d * int_a;
		return d;
	}

	public double testM(double d) {
		d = 4 * d;
		return d;
	}

	public double testN(double d) {
		d = (3 + 4) * d;
		return d;
	}

	public double testO(double d) {
		d = int_a * int_b / int_c * int_q * 3 - d;
		return d;
	}

	public double testP(double d) {
		d = d / 4;
		return d;
	}

	public double testQ(double d) {
		d = 1 + 2 + 3 + 4 + d;
		return d;
	}

	public double testR(double d) {
		d = 1 + 2 + d + 4 + d;
		return d;
	}

	public double testS(double d) {
		d = 1 * 2 + 4 * d;
		return d;
	}

	public double testT(double d) {
		d = d * 4 + d;
		return d;
	}

	public double testU(double d) {
		d = 1 - d + d + 4 + d;
		return d;
	}

	public double testV(double d) {
		d = d * (2 + 4 + d);
		return d;
	}
	
	/*
	 * FLOAT
	 */
	
	public float testA(float f) {
		f = (float) f + 1;
		return f;
	}

	public float testB(float f) {
		f = 7 + 4 - 3;
		return f;
	}

	public float testC(float f) {
		f = f + 3;
		return f;
	}

	public float testD(float f) {
		f = f - 3;
		return f;
	}

	public float testE(float f) {
		f = f + 4 - 3;
		return f;
	}

	public float testF(float f) {
		f = f + 4 + 3;
		return f;
	}

	public float testG(float f) {
		f = 3 + f + 4;
		return f;
	}

	public float testH(float f) {
		f = 3 + f - 4;
		return f;
	}

	public float testI(float f) {
		f = int_a + f - 4;
		return f;
	}

	public float testJ(float f) {
		f = int_a + int_b;
		return f;
	}

	public float testK(float f) {
		f = int_a + f;
		return f;
	}

	public float testL(float f) {
		f = f * int_a;
		return f;
	}

	public float testM(float f) {
		f = 4 * f;
		return f;
	}

	public float testN(float f) {
		f = (3 + 4) * f;
		return f;
	}

	public float testO(float f) {
		f = int_a * int_b / int_c * int_q * 3 - f;
		return f;
	}

	public float testP(float f) {
		f = f / 4;
		return f;
	}

	public float testQ(float f) {
		f = 1 + 2 + 3 + 4 + f;
		return f;
	}

	public float testR(float f) {
		f = 1 + 2 + f + 4 + f;
		return f;
	}

	public float testS(float f) {
		f = 1 * 2 + 4 * f;
		return f;
	}

	public float testT(float f) {
		f = f * 4 + f;
		return f;
	}

	public float testU(float f) {
		f = 1 - f + f + 4 + f;
		return f;
	}

	public float testV(float f) {
		f = f * (2 + 4 + f);
		return f;
	}
    
	/*
	 * LONG
	 */
	
	public long testA(long l) {
		l = (long) l + 1;
		return l;
	}

	public long testB(long l) {
		l = 7 + 4 - 3;
		return l;
	}

	public long testC(long l) {
		l = l + 3;
		return l;
	}

	public long testD(long l) {
		l = l - 3;
		return l;
	}

	public long testE(long l) {
		l = l + 4 - 3;
		return l;
	}

	public long testF(long l) {
		l = l + 4 + 3;
		return l;
	}

	public long testG(long l) {
		l = 3 + l + 4;
		return l;
	}

	public long testH(long l) {
		l = 3 + l - 4;
		return l;
	}

	public long testI(long l) {
		l = int_a + l - 4;
		return l;
	}

	public long testJ(long l) {
		l = int_a + int_b;
		return l;
	}

	public long testK(long l) {
		l = int_a + l;
		return l;
	}

	public long testL(long l) {
		l = l * int_a;
		return l;
	}

	public long testM(long l) {
		l = 4 * l;
		return l;
	}

	public long testN(long l) {
		l = (3 + 4) * l;
		return l;
	}

	public long testO(long l) {
		l = int_a * int_b / int_c * int_q * 3 - l;
		return l;
	}

	public long testP(long l) {
		l = l / 4;
		return l;
	}

	public long testQ(long l) {
		l = 1 + 2 + 3 + 4 + l;
		return l;
	}

	public long testR(long l) {
		l = 1 + 2 + l + 4 + l;
		return l;
	}

	public long testS(long l) {
		l = 1 * 2 + 4 * l;
		return l;
	}

	public long testT(long l) {
		l = l * 4 + l;
		return l;
	}

	public long testU(long l) {
		l = 1 - l + l + 4 + l;
		return l;
	}

	public long testV(long l) {
		l = l * (2 + 4 + l);
		return l;
	}
	
	/*
	 * SHORT
	 */
	
	public short testA(short s) {
		s = (short) ((short) s + 1);
		return s;
	}

	public short testB(short s) {
		s = 7 + 4 - 3;
		return s;
	}

	public short testC(short s) {
		s = (short) (s + 3);
		return s;
	}

	public short testD(short s) {
		s = (short) (s - 3);
		return s;
	}

	public short testE(short s) {
		s = (short) (s + 4 - 3);
		return s;
	}

	public short testF(short s) {
		s = (short) (s + 4 + 3);
		return s;
	}

	public short testG(short s) {
		s = (short) (3 + s + 4);
		return s;
	}

	public short testH(short s) {
		s = (short) (3 + s - 4);
		return s;
	}

	public short testI(short s) {
		s = (short) (int_a + s - 4);
		return s;
	}

	public short testJ(short s) {
		s = (short) (int_a + int_b);
		return s;
	}

	public short testK(short s) {
		s = (short) (int_a + s);
		return s;
	}

	public short testL(short s) {
		s = (short) (s * int_a);
		return s;
	}

	public short testM(short s) {
		s = (short) (4 * s);
		return s;
	}

	public short testN(short s) {
		s = (short) ((3 + 4) * s);
		return s;
	}

	public short testO(short s) {
		s = (short) (int_a * int_b / int_c * int_q * 3 - s);
		return s;
	}

	public short testP(short s) {
		s = (short) (s / 4);
		return s;
	}

	public short testQ(short s) {
		s = (short) (1 + 2 + 3 + 4 + s);
		return s;
	}

	public short testR(short s) {
		s = (short) (1 + 2 + s + 4 + s);
		return s;
	}

	public short testS(short s) {
		s = (short) (1 * 2 + 4 * s);
		return s;
	}

	public short testT(short s) {
		s = (short) (s * 4 + s);
		return s;
	}

	public short testU(short s) {
		s = (short) (1 - s + s + 4 + s);
		return s;
	}

	public short testV(short s) {
		s = (short) (s * (2 + 4 + s));
		return s;
	}
	
	/*
	 * BYTE
	 */

	public byte testA(byte b) {
		b = (byte) ((byte) b + 1);
		return b;
	}

	public byte testB(byte b) {
		b = 7 + 4 - 3;
		return b;
	}

	public byte testC(byte b) {
		b = (byte) (b + 3);
		return b;
	}

	public byte testD(byte b) {
		b = (byte) (b - 3);
		return b;
	}

	public byte testE(byte b) {
		b = (byte) (b + 4 - 3);
		return b;
	}

	public byte testF(byte b) {
		b = (byte) (b + 4 + 3);
		return b;
	}

	public byte testG(byte b) {
		b = (byte) (3 + b + 4);
		return b;
	}

	public byte testH(byte b) {
		b = (byte) (3 + b - 4);
		return b;
	}

	public byte testI(byte b) {
		b = (byte) (int_a + b - 4);
		return b;
	}

	public byte testJ(byte b) {
		b = (byte) (int_a + int_b);
		return b;
	}

	public byte testK(byte b) {
		b = (byte) (int_a + b);
		return b;
	}

	public byte testL(byte b) {
		b = (byte) (b * int_a);
		return b;
	}

	public byte testM(byte b) {
		b = (byte) (4 * b);
		return b;
	}

	public byte testN(byte b) {
		b = (byte) ((3 + 4) * b);
		return b;
	}

	public byte testO(byte b) {
		b = (byte) (int_a * int_b / int_c * int_q * 3 - b);
		return b;
	}

	public byte testP(byte b) {
		b = (byte) (b / 4);
		return b;
	}

	public byte testQ(byte b) {
		b = (byte) (1 + 2 + 3 + 4 + b);
		return b;
	}

	public byte testR(byte b) {
		b = (byte) (1 + 2 + b + 4 + b);
		return b;
	}

	public byte testS(byte b) {
		b = (byte) (1 * 2 + 4 * b);
		return b;
	}

	public byte testT(byte b) {
		b = (byte) (b * 4 + b);
		return b;
	}

	public byte testU(byte b) {
		b = (byte) (1 - b + b + 4 + b);
		return b;
	}

	public byte testV(byte b) {
		b = (byte) (b * (2 + 4 + b));
		return b;
	}
	
	/*
	 * CHAR
	 */
	
	public char testA(char c) {
		c = (char) ((char) c + 1);
		return c;
	}

	public char testB(char c) {
		c = 7 + 4 - 3;
		return c;
	}

	public char testC(char c) {
		c = (char) (c + 3);
		return c;
	}

	public char testD(char c) {
		c = (char) (c - 3);
		return c;
	}

	public char testE(char c) {
		c = (char) (c + 4 - 3);
		return c;
	}

	public char testF(char c) {
		c = (char) (c + 4 + 3);
		return c;
	}

	public char testG(char c) {
		c = (char) (3 + c + 4);
		return c;
	}

	public char testH(char c) {
		c = (char) (3 + c - 4);
		return c;
	}

	public char testI(char c) {
		c = (char) (int_a + c - 4);
		return c;
	}

	public char testJ(char c) {
		c = (char) (int_a + int_b);
		return c;
	}

	public char testK(char c) {
		c = (char) (int_a + c);
		return c;
	}

	public char testL(char c) {
		c = (char) (c * int_a);
		return c;
	}

	public char testM(char c) {
		c = (char) (4 * c);
		return c;
	}

	public char testN(char c) {
		c = (char) ((3 + 4) * c);
		return c;
	}

	public char testO(char c) {
		c = (char) (int_a * int_b / int_c * int_q * 3 - c);
		return c;
	}

	public char testP(char c) {
		c = (char) (c / 4);
		return c;
	}

	public char testQ(char c) {
		c = (char) (1 + 2 + 3 + 4 + c);
		return c;
	}

	public char testR(char c) {
		c = (char) (1 + 2 + c + 4 + c);
		return c;
	}

	public char testS(char c) {
		c = (char) (1 * 2 + 4 * c);
		return c;
	}

	public char testT(char c) {
		c = (char) (c * 4 + c);
		return c;
	}

	public char testU(char c) {
		c = (char) (1 - c + c + 4 + c);
		return c;
	}

	public char testV(char c) {
		c = (char) (c * (2 + 4 + c));
		return c;
	}

}

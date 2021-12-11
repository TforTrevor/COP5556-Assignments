package edu.ufl.cise.plpfa21.assignment5;

import java.util.List;

public class Runtime {

	public static boolean not(boolean arg)
	{
		return !arg;
	}

	public static int opposite(int arg)
	{
		return -arg;
	}

	public static int plusInt(int a, int b)
	{
		return a + b;
	}

	public static String plusString(String a, String b)
	{
		return a + b;
	}

	public static int minus(int a, int b)
	{
		return a - b;
	}

	public static int times(int a, int b)
	{
		return a * b;
	}

	public static int divide(int a, int b)
	{
		return a / b;
	}

	public static boolean and(boolean a, boolean b)
	{
		return a && b;
	}

	public static boolean or(boolean a, boolean b)
	{
		return a || b;
	}

	public static boolean equalsInt(int a, int b)
	{
		return a == b;
	}

	public static boolean equalsString(String a, String b)
	{
		return a.equals(b);
	}

	public static boolean equalsBool(boolean a, boolean b)
	{
		return a == b;
	}

	public static boolean notEqualsInt(int a, int b)
	{
		return a != b;
	}

	public static boolean notEqualsString(String a, String b)
	{
		return !a.equals(b);
	}

	public static boolean notEqualsBool(boolean a, boolean b)
	{
		return a != b;
	}

	public static boolean lessThanInt(int a, int b)
	{
		return a < b;
	}

	public static boolean lessThanString(String a, String b)
	{
		return b.startsWith(a);
	}

	public static boolean lessThanBool(boolean a, boolean b)
	{
		return !a && b;
	}

	public static boolean greaterThanInt(int a, int b)
	{
		return a > b;
	}

	public static boolean greaterThanString(String a, String b)
	{
		return a.startsWith(b);
	}

	public static boolean greaterThanBool(boolean a, boolean b)
	{
		return a && !b;
	}
}

package edu.ufl.cise.plpfa21.assignment1;

import edu.ufl.cise.plpfa21.assignment2.IPLPParser;
import edu.ufl.cise.plpfa21.assignment2.Parser;

public class CompilerComponentFactory
{
	public static IPLPLexer getLexer(String input)
	{
		return new Lexer(input);
	}

	public static IPLPParser getParser(String input)
	{
		return new Parser(getLexer(input));
	}
}

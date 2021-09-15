package edu.ufl.cise.plpfa21.assignment1;

import java.util.ArrayList;
import java.util.Arrays;

public class Lexer implements IPLPLexer
{
    private ArrayList<String> tokenStrings = new ArrayList<>();
    private ArrayList<Integer> lineIndices = new ArrayList<>();
    private ArrayList<Integer> charIndices = new ArrayList<>();
    private int index;

    public Lexer(String code)
    {
        //String[] strings = code.split("[ \n\r\t]+");
        //this.code = new ArrayList<>(Arrays.asList(strings));
        ArrayList<String> lines = new ArrayList<>(Arrays.asList(code.split("\n")));
        for (String line : lines)
        {
            String tokenString = "";
            int tokenIndex = -1;

            for (int i = 0; i < line.length(); i++)
            {
                char c = line.charAt(i);
                if (c != ' ' && c != '\n' && c != '\r' && c != '\t')
                {
                    if (tokenString.length() > 0 && Character.isDigit(tokenString.charAt(0)) && Character.isLetter(c))
                    {
                        if (tokenString.length() > 0)
                        {
                            tokenStrings.add(tokenString);
                            lineIndices.add(lines.indexOf(line));
                            charIndices.add(tokenIndex);
                        }
                        tokenString = "";
                        tokenIndex = -1;
                    }

                    if (tokenIndex == -1)
                        tokenIndex = i;
                    tokenString += line.charAt(i);
                }
                if (i == line.length() - 1 || c == ' ' || c == '\n' || c == '\r' || c == '\t')
                {
                    if (tokenString.length() > 0)
                    {
                        tokenStrings.add(tokenString);
                        lineIndices.add(lines.indexOf(line));
                        charIndices.add(tokenIndex);
                    }
                    tokenString = "";
                    tokenIndex = -1;
                }
            }
        }
    }

    @Override
    public IPLPToken nextToken() throws LexicalException
    {
        Token token = null;
        if (index < tokenStrings.size())
        {
            String string = tokenStrings.get(index);
            int lineIndex = lineIndices.get(index);
            int charIndex = charIndices.get(index);

            if (Character.isLetter(string.charAt(0)))
            {
                switch (string)
                {
                    case "VAR" -> token = new Token(PLPTokenKinds.Kind.KW_VAR, string, lineIndex, charIndex);
                    default -> token = new Token(PLPTokenKinds.Kind.IDENTIFIER, string, lineIndex, charIndex);
                }
            }
            else if (Character.isDigit(string.charAt(0)))
            {
                try {
                    int test = Integer.parseInt(string);
                    token = new Token(PLPTokenKinds.Kind.INT_LITERAL, string, lineIndex, charIndex);
                } catch (NumberFormatException e)
                {
                    throw new LexicalException("Integer cannot be parsed", lineIndex, charIndex);
                }
            }
            else
            {
                switch(string)
                {
                    case "=" -> token = new Token(PLPTokenKinds.Kind.ASSIGN, string, lineIndex, charIndex);
                    case "==", "===" -> token = new Token(PLPTokenKinds.Kind.EQUALS, string, lineIndex, charIndex);
                    case "!=" -> token = new Token(PLPTokenKinds.Kind.NOT_EQUALS, string, lineIndex, charIndex);
                    default -> {
                        throw new LexicalException("Unknown token: " + string, lineIndex, charIndex);
                    }
                }
            }
            index++;
        }
        else
        {
            token = new Token(PLPTokenKinds.Kind.EOF, "\n", 0, 0);
        }
        return token;
    }
}

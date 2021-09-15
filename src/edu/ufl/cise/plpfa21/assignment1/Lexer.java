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
                    case "VAL" -> token = new Token(PLPTokenKinds.Kind.KW_VAL, string, lineIndex, charIndex);
                    case "FUN" -> token = new Token(PLPTokenKinds.Kind.KW_FUN, string, lineIndex, charIndex);
                    case "DO" -> token = new Token(PLPTokenKinds.Kind.KW_DO, string, lineIndex, charIndex);
                    case "END" -> token = new Token(PLPTokenKinds.Kind.KW_END, string, lineIndex, charIndex);
                    case "LET" -> token = new Token(PLPTokenKinds.Kind.KW_LET, string, lineIndex, charIndex);
                    case "SWITCH" -> token = new Token(PLPTokenKinds.Kind.KW_SWITCH, string, lineIndex, charIndex);
                    case "CASE" -> token = new Token(PLPTokenKinds.Kind.KW_CASE, string, lineIndex, charIndex);
                    case "DEFAULT" -> token = new Token(PLPTokenKinds.Kind.KW_DEFAULT, string, lineIndex, charIndex);
                    case "IF" -> token = new Token(PLPTokenKinds.Kind.KW_IF, string, lineIndex, charIndex);
                    case "WHILE" -> token = new Token(PLPTokenKinds.Kind.KW_WHILE, string, lineIndex, charIndex);
                    case "RETURN" -> token = new Token(PLPTokenKinds.Kind.KW_RETURN, string, lineIndex, charIndex);
                    case "NIL" -> token = new Token(PLPTokenKinds.Kind.KW_NIL, string, lineIndex, charIndex);
                    case "TRUE" -> token = new Token(PLPTokenKinds.Kind.KW_TRUE, string, lineIndex, charIndex);
                    case "FALSE" -> token = new Token(PLPTokenKinds.Kind.KW_FALSE, string, lineIndex, charIndex);
                    case "INT" -> token = new Token(PLPTokenKinds.Kind.KW_INT, string, lineIndex, charIndex);
                    case "STRING" -> token = new Token(PLPTokenKinds.Kind.KW_STRING, string, lineIndex, charIndex);
                    case "BOOLEAN" -> token = new Token(PLPTokenKinds.Kind.KW_BOOLEAN, string, lineIndex, charIndex);
                    case "LIST" -> token = new Token(PLPTokenKinds.Kind.KW_LIST, string, lineIndex, charIndex);
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
            else if (string.charAt(0) == '"' || string.charAt(0) == 39)
            {
                
            }
            else
            {
                switch(string)
                {
                    case "=" -> token = new Token(PLPTokenKinds.Kind.ASSIGN, string, lineIndex, charIndex);
                    case "," -> token = new Token(PLPTokenKinds.Kind.COMMA, string, lineIndex, charIndex);
                    case ";" -> token = new Token(PLPTokenKinds.Kind.SEMI, string, lineIndex, charIndex);
                    case ":" -> token = new Token(PLPTokenKinds.Kind.COLON, string, lineIndex, charIndex);
                    case "(" -> token = new Token(PLPTokenKinds.Kind.LPAREN, string, lineIndex, charIndex);
                    case ")" -> token = new Token(PLPTokenKinds.Kind.RPAREN, string, lineIndex, charIndex);
                    case "[" -> token = new Token(PLPTokenKinds.Kind.LSQUARE, string, lineIndex, charIndex);
                    case "]" -> token = new Token(PLPTokenKinds.Kind.RSQUARE, string, lineIndex, charIndex);
                    case "&&" -> token = new Token(PLPTokenKinds.Kind.AND, string, lineIndex, charIndex);
                    case "||" -> token = new Token(PLPTokenKinds.Kind.OR, string, lineIndex, charIndex);
                    case "<" -> token = new Token(PLPTokenKinds.Kind.LT, string, lineIndex, charIndex);
                    case ">" -> token = new Token(PLPTokenKinds.Kind.GT, string, lineIndex, charIndex);
                    case "==" -> token = new Token(PLPTokenKinds.Kind.EQUALS, string, lineIndex, charIndex);
                    case "!=" -> token = new Token(PLPTokenKinds.Kind.NOT_EQUALS, string, lineIndex, charIndex);
                    case "!" -> token = new Token(PLPTokenKinds.Kind.BANG, string, lineIndex, charIndex);
                    case "+" -> token = new Token(PLPTokenKinds.Kind.PLUS, string, lineIndex, charIndex);
                    case "-" -> token = new Token(PLPTokenKinds.Kind.MINUS, string, lineIndex, charIndex);
                    case "*" -> token = new Token(PLPTokenKinds.Kind.TIMES, string, lineIndex, charIndex);
                    case "/" -> token = new Token(PLPTokenKinds.Kind.DIV, string, lineIndex, charIndex);
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

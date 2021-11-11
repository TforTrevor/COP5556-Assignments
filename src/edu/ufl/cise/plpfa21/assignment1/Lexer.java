package edu.ufl.cise.plpfa21.assignment1;

import java.util.ArrayList;
import edu.ufl.cise.plpfa21.assignment1.PLPTokenKinds.Kind;

public class Lexer implements IPLPLexer
{
    private ArrayList<IPLPToken> tokenList;
    private int tokenPos;
    private enum State { START, HAVE_EQUAL, HAVE_NEQUAL, DIGITS, IDENT_PART, COMMENT, STRING, BOOL_OP }

    public Lexer (String input)
    {
        this.tokenPos = 0;
        this.tokenList = new ArrayList<>();
        try {
            this.tokenList = readInput(input);
        } catch (LexicalException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<IPLPToken> readInput (String input) throws LexicalException
    {
        ArrayList<IPLPToken> tokenList = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();

        Kind kind;
        State state = State.START;

        char[] inputChars = input.toCharArray();
        char previousChar = 0;
        char nextChar = 0;

        int currentLine = 1;
        int currentPos = 0;
        int firstLineOfString = 0;
        int index = 0;

        while (index < inputChars.length)
        {
            char c = inputChars[index];

            if (index > 0)
            {
                previousChar = input.charAt(index - 1);
            }
            if (index < inputChars.length - 1)
            {
                nextChar = input.charAt(index + 1);
            }

            switch (state)
            {
                case START -> {
                    switch (c)
                    {
                        case ' ', '\t' -> {
                            index++;
                            currentPos++;
                        }
                        case '\n', '\r' -> {
                            index++;
                            currentPos = 0;
                            currentLine++;
                        }
                        case '0' -> {
                            tokenList.add(new Token(Kind.INT_LITERAL, "0", currentLine, currentPos));
                            index++;
                            currentPos++;
                        }
                        case '+', ',', ';', ':', '(', ')', '[', ']', '<', '>', '-' -> {
                            String text = String.valueOf(c);
                            kind = findKind(text, currentLine, currentPos);
                            tokenList.add(new Token(kind, text, currentLine, currentPos));
                            index++;
                            currentPos++;
                        }
                        case '*' -> {
                            tokenList.add(new Token(Kind.TIMES, "*", currentLine, currentPos));
                            index++;
                            currentPos++;
                        }
                        case '=' -> {
                            if (nextChar != '=')
                            {
                                tokenList.add(new Token(Kind.ASSIGN, "=", currentLine, currentPos));
                            }
                            else
                            {
                                state = State.HAVE_EQUAL;
                            }
                            index++;
                            currentPos++;
                        }
                        case '!' -> {
                            if (nextChar != '=')
                            {
                                tokenList.add(new Token(Kind.BANG, "!", currentLine, currentPos));
                            }
                            else
                            {
                                state = State.HAVE_NEQUAL;
                            }
                            index++;
                            currentPos++;
                        }
                        case '/' -> {
                            if (nextChar == '*')
                            {
                                state = State.COMMENT;
                            }
                            else
                            {
                                tokenList.add(new Token(Kind.DIV, "/", currentLine, currentPos));
                            }
                            index++;
                            currentPos++;
                        }
                        case '&', '|' -> {
                            stringBuilder.append(c);
                            state = State.BOOL_OP;
                            index++;
                            currentPos++;
                        }
                        case '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                            stringBuilder.append(c);
                            state = State.DIGITS;
                            index++;
                            currentPos++;
                        }
                        case '\"', '\'' -> {
                            firstLineOfString = currentLine;
                            stringBuilder.append(c);
                            state = State.STRING;
                            index++;
                            currentPos++;
                        }
                        default -> {
                            if (Character.isJavaIdentifierStart(c))
                            {
                                index++;
                                stringBuilder.append(c);
                                state = State.IDENT_PART;
                                currentPos++;
                            }
                            else
                            {
                                if (c != 0)
                                {
                                    tokenList.add(new Token(Kind.ERROR, "", currentLine, currentPos));
                                    index++;
                                    currentPos++;
                                }
                            }
                        }
                    }
                }
                case STRING -> {
                    if (c != stringBuilder.charAt(0))
                    {
                        index++;
                        if (c == '\n' || c == '\r')
                        {
                            currentLine++;
                            currentPos = 0;
                        }
                        stringBuilder.append(c);
                        currentPos++;
                    }
                    else
                    {
                        stringBuilder.append(c);
                        index++;
                        currentPos++;
                        state = State.START;
                        String text = stringBuilder.toString();

                        kind = findKind(text, currentLine, currentPos);
                        tokenList.add(new Token(kind, text, firstLineOfString, currentPos-text.length()));
                        stringBuilder.setLength(0);
                    }
                }
                case BOOL_OP -> {
                    if (c == previousChar)
                    {
                        stringBuilder.append(c);
                        String text = stringBuilder.toString();
                        kind = findKind(text, currentLine, currentPos);
                        tokenList.add(new Token(kind, text, currentLine, currentPos - 1));
                        stringBuilder.setLength(0);
                        state = State.START;
                        currentPos++;
                    }
                    else
                    {
                        tokenList.add(new Token(Kind.ERROR, "", currentLine, currentPos));
                    }
                    index++;
                }
                case COMMENT -> {
                    if (c == '/' && previousChar == '*')
                    {
                        state = State.START;
                    }
                    index++;
                    currentPos++;
                }
                case HAVE_EQUAL -> {
                    if (c == '=')
                    {
                        tokenList.add(new Token(Kind.EQUALS, "==", currentLine, currentPos - 1));
                        currentPos++;
                    }
                    else
                    {
                        tokenList.add(new Token(Kind.ERROR, "", currentLine, currentPos));
                    }
                    index++;
                    state = State.START;
                }
                case HAVE_NEQUAL -> {
                    if(c == '=')
                    {
                        tokenList.add(new Token(Kind.NOT_EQUALS, "!=", currentLine, currentPos - 1));
                        currentPos++;
                    }
                    else
                    {
                        tokenList.add(new Token(Kind.ERROR, "", currentLine, currentPos));
                    }
                    index++;
                    state = State.START;
                }
                case DIGITS -> {
                    if (Character.isDigit(c))
                    {
                        index++;
                        currentPos++;
                        stringBuilder.append(c);
                    }
                    else
                    {
                        String text = stringBuilder.toString();
                        tokenList.add(new Token(Kind.INT_LITERAL, text, currentLine, currentPos-text.length()));
                        stringBuilder.setLength(0);
                        state = State.START;
                    }
                }
                case IDENT_PART -> {
                    if (Character.isDigit(c) || Character.isLetter(c) || c == '_' || c == '$')
                    {
                        index++;
                        currentPos++;
                        stringBuilder.append(c);
                    }
                    else
                    {
                        String text = stringBuilder.toString();
                        kind = findKind(text, currentLine, currentPos);
                        tokenList.add(new Token(kind, text, currentLine, currentPos-text.length()));
                        stringBuilder.setLength(0);
                        state = State.START;
                    }
                }
            }
        }
        return tokenList;
    }
    private Kind findKind (String kindString, int line, int pos)
    {
        char c = kindString.charAt(0);
        Kind kind = Kind.ERROR;
        if (Character.isLetter(c) || c == '$' || c == '_')
        {
            switch(kindString)
            {
                case "FUN" -> kind = Kind.KW_FUN;
                case "DO" ->  kind = Kind.KW_DO;
                case "END" -> kind = Kind.KW_END;
                case "LET" -> kind = Kind.KW_LET;
                case "SWITCH" -> kind = Kind.KW_SWITCH;
                case "CASE" -> kind = Kind.KW_CASE;
                case "DEFAULT" -> kind = Kind.KW_DEFAULT;
                case "IF" -> kind = Kind.KW_IF;
                case "ELSE" -> kind = Kind.KW_ELSE;
                case "WHILE" -> kind = Kind.KW_WHILE;
                case "RETURN" -> kind = Kind.KW_RETURN;
                case "LIST" -> kind = Kind.KW_LIST;
                case "VAR" -> kind = Kind.KW_VAR;
                case "VAL" -> kind = Kind.KW_VAL;
                case "NIL" -> kind = Kind.KW_NIL;
                case "TRUE" -> kind = Kind.KW_TRUE;
                case "FALSE" -> kind = Kind.KW_FALSE;
                case "INT" -> kind = Kind.KW_INT;
                case "STRING" -> kind = Kind.KW_STRING;
                case "FLOAT" -> kind = Kind.KW_FLOAT;
                case "BOOLEAN" -> kind = Kind.KW_BOOLEAN;
                default -> kind = Kind.IDENTIFIER;
            }
        }
        else if (Character.isDigit(c))
        {
            kind = Kind.INT_LITERAL;
        }
        else if (c == '\'' || c == '\"')
        {
            kind = Kind.STRING_LITERAL;
        }
        else
        {
            switch (kindString)
            {
                case"(" -> kind = Kind.LPAREN;
                case":" -> kind = Kind.COLON;
                case"," -> kind = Kind.COMMA;
                case ")" -> kind = Kind.RPAREN;
                case "=" -> kind = Kind.ASSIGN;
                case ";" -> kind = Kind.SEMI;
                case "&&" -> kind = Kind.AND;
                case "||" -> kind = Kind.OR;
                case "<" -> kind = Kind.LT;
                case ">" -> kind = Kind.GT;
                case "==" -> kind = Kind.EQUALS;
                case "!=" -> kind = Kind.NOT_EQUALS;
                case "+" -> kind = Kind.PLUS;
                case "-" -> kind = Kind.MINUS;
                case "*" -> kind = Kind.TIMES;
                case "/" -> kind = Kind.DIV;
                case "!" -> kind = Kind.BANG;
                case "[" -> kind = Kind.LSQUARE;
                case "]" -> kind = Kind.RSQUARE;
            }
        }
        return kind;
    }

    public IPLPToken nextToken() throws LexicalException
    {
        IPLPToken token = new Token(Kind.EOF, "", 0, 0);

        if (tokenPos < tokenList.size())
        {
            token = tokenList.get(tokenPos);
            this.tokenPos++;
        }

        if (token.getKind() == Kind.ERROR)
        {
            throw new LexicalException("Illegal character detected", token.getLine(), token.getCharPositionInLine());
        }
        else if (token.getKind() == Kind.INT_LITERAL)
        {
            try {
                Integer.parseInt(token.getText());
                tokenList.set(tokenPos - 1, new Token(token.getKind(), token.getText(), token.getLine(), token.getCharPositionInLine()));
                token = tokenList.get(tokenPos - 1);
            } catch (NumberFormatException e) {
                throw new LexicalException("Integer out of range", token.getLine(), token.getCharPositionInLine());
            }
        }
        return token;
    }
}

package edu.ufl.cise.plpfa21.assignment2;

import edu.ufl.cise.plpfa21.assignment1.IPLPLexer;
import edu.ufl.cise.plpfa21.assignment1.IPLPToken;
import edu.ufl.cise.plpfa21.assignment1.LexicalException;
import edu.ufl.cise.plpfa21.assignment1.PLPTokenKinds;
import edu.ufl.cise.plpfa21.assignment3.ast.IASTNode;

public class Parser implements IPLPParser
{
    private IPLPLexer lexer;
    private IPLPToken token;

    public Parser(IPLPLexer lexer)
    {
        this.lexer = lexer;
        consume();
    }

    @Override
    public IASTNode parse() throws Exception
    {
        program();
    }

    private void consume()
    {
        try
        {
            token = lexer.nextToken();
        } catch (LexicalException e)
        {
            e.printStackTrace();
        }
    }

    private void match(PLPTokenKinds.Kind kind) throws SyntaxException
    {
        if (token.getKind() == kind)
        {
            consume();
        }
        else
        {
            throw new SyntaxException("Test", token.getLine(), token.getCharPositionInLine());
        }
    }

    private void program() throws SyntaxException
    {
        boolean next = true;
        while (next)
        {
            next = declaration();
        }
    }

    private boolean declaration() throws SyntaxException
    {
        switch (token.getKind())
        {
            case KW_FUN -> {
                function();
                return true;
            }
            case KW_VAR -> {
                match(PLPTokenKinds.Kind.KW_VAR);
                nameDef();
                if (token.getKind() == PLPTokenKinds.Kind.ASSIGN)
                {
                    match(PLPTokenKinds.Kind.ASSIGN);
                    expression();
                }
                match(PLPTokenKinds.Kind.SEMI);
                return true;
            }
            case KW_VAL -> {
                match(PLPTokenKinds.Kind.KW_VAL);
                nameDef();
                match(PLPTokenKinds.Kind.ASSIGN);
                expression();
                match(PLPTokenKinds.Kind.SEMI);
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private void function() throws SyntaxException
    {
        match(PLPTokenKinds.Kind.KW_FUN);
        match(PLPTokenKinds.Kind.IDENTIFIER);
        match(PLPTokenKinds.Kind.LPAREN);

        if (token.getKind() == PLPTokenKinds.Kind.IDENTIFIER)
        {
            nameDef();
            while (token.getKind() == PLPTokenKinds.Kind.COMMA)
            {
                match(PLPTokenKinds.Kind.COMMA);
                nameDef();
            }
        }

        match(PLPTokenKinds.Kind.RPAREN);

        if (token.getKind() == PLPTokenKinds.Kind.COLON)
        {
            match(PLPTokenKinds.Kind.COLON);
            type();
        }

        match(PLPTokenKinds.Kind.KW_DO);
        block();
        match(PLPTokenKinds.Kind.KW_END);
    }

    private void nameDef() throws SyntaxException
    {
        match(PLPTokenKinds.Kind.IDENTIFIER);
        if (token.getKind() == PLPTokenKinds.Kind.COLON)
        {
            match(PLPTokenKinds.Kind.COLON);
            type();
        }
    }

    private void type() throws SyntaxException
    {
        switch (token.getKind())
        {
            case KW_INT -> match(PLPTokenKinds.Kind.KW_INT);
            case KW_STRING -> match(PLPTokenKinds.Kind.KW_STRING);
            case KW_BOOLEAN -> match(PLPTokenKinds.Kind.KW_BOOLEAN);
            case KW_LIST -> {
                match(PLPTokenKinds.Kind.KW_LIST);
                match(PLPTokenKinds.Kind.LSQUARE);
                if (token.getKind() != PLPTokenKinds.Kind.RSQUARE)
                {
                    type();
                }
                match(PLPTokenKinds.Kind.RSQUARE);
            }
        }
    }

    private void block() throws SyntaxException
    {
        while (true)
        {
            statement();
        }
    }

    private void statement() throws SyntaxException
    {
        switch (token.getKind())
        {
            case KW_LET -> {
                match(PLPTokenKinds.Kind.KW_LET);
                nameDef();
                if (token.getKind() == PLPTokenKinds.Kind.ASSIGN)
                {
                    expression();
                }
                match(PLPTokenKinds.Kind.SEMI);
            }
            case KW_SWITCH -> {
                match(PLPTokenKinds.Kind.KW_SWITCH);
                expression();
                while (token.getKind() == PLPTokenKinds.Kind.KW_CASE)
                {
                    match(PLPTokenKinds.Kind.KW_CASE);
                    expression();
                    match(PLPTokenKinds.Kind.SEMI);
                    block();
                }
                match(PLPTokenKinds.Kind.KW_DEFAULT);
                block();
                match(PLPTokenKinds.Kind.KW_END);
            }
            case KW_IF -> {
                match(PLPTokenKinds.Kind.KW_IF);
                expression();
                match(PLPTokenKinds.Kind.KW_DO);
                block();
                match(PLPTokenKinds.Kind.KW_END);
            }
            case KW_WHILE -> {
                match(PLPTokenKinds.Kind.KW_WHILE);
                expression();
                match(PLPTokenKinds.Kind.KW_DO);
                block();
                match(PLPTokenKinds.Kind.KW_END);
            }
            case KW_RETURN -> {
                match(PLPTokenKinds.Kind.KW_RETURN);
                expression();
                match(PLPTokenKinds.Kind.SEMI);
            }
            default -> {
                expression();
                if (token.getKind() == PLPTokenKinds.Kind.ASSIGN)
                {
                    expression();
                }
                match(PLPTokenKinds.Kind.SEMI);
            }
        }
    }

    private void expression() throws SyntaxException
    {
        logicalExpression();
    }

    private void logicalExpression() throws SyntaxException
    {
        comparisonExpression();
        while (token.getKind() == PLPTokenKinds.Kind.AND || token.getKind() == PLPTokenKinds.Kind.OR)
        {
            switch (token.getKind())
            {
                case AND -> match(PLPTokenKinds.Kind.AND);
                case OR -> match(PLPTokenKinds.Kind.OR);
            }
            comparisonExpression();
        }
    }

    private void comparisonExpression() throws SyntaxException
    {
        additiveExpression();
        while (token.getKind() == PLPTokenKinds.Kind.LT || token.getKind() == PLPTokenKinds.Kind.GT ||
                token.getKind() == PLPTokenKinds.Kind.EQUALS || token.getKind() == PLPTokenKinds.Kind.NOT_EQUALS)
        {
            switch (token.getKind())
            {
                case LT -> match(PLPTokenKinds.Kind.LT);
                case GT -> match(PLPTokenKinds.Kind.GT);
                case EQUALS -> match(PLPTokenKinds.Kind.EQUALS);
                case NOT_EQUALS -> match(PLPTokenKinds.Kind.NOT_EQUALS);
            }
            additiveExpression();
        }
    }

    private void additiveExpression() throws SyntaxException
    {
        multiplicativeExpression();
        while (token.getKind() == PLPTokenKinds.Kind.PLUS || token.getKind() == PLPTokenKinds.Kind.MINUS)
        {
            switch (token.getKind())
            {
                case PLUS -> match(PLPTokenKinds.Kind.PLUS);
                case MINUS -> match(PLPTokenKinds.Kind.MINUS);
            }
            multiplicativeExpression();
        }
    }

    private void multiplicativeExpression() throws SyntaxException
    {
        unaryExpression();
        while (token.getKind() == PLPTokenKinds.Kind.TIMES || token.getKind() == PLPTokenKinds.Kind.DIV)
        {
            switch (token.getKind())
            {
                case TIMES -> match(PLPTokenKinds.Kind.TIMES);
                case DIV -> match(PLPTokenKinds.Kind.DIV);
            }
            unaryExpression();
        }
    }

    private void unaryExpression() throws SyntaxException
    {
        if (token.getKind() == PLPTokenKinds.Kind.BANG || token.getKind() == PLPTokenKinds.Kind.MINUS)
        {
            switch (token.getKind())
            {
                case BANG -> match(PLPTokenKinds.Kind.BANG);
                case MINUS -> match(PLPTokenKinds.Kind.MINUS);
            }
        }
        primaryExpression();
    }

    private void primaryExpression() throws SyntaxException
    {
        switch (token.getKind())
        {
            case KW_NIL -> match(PLPTokenKinds.Kind.KW_NIL);
            case KW_TRUE -> match(PLPTokenKinds.Kind.KW_TRUE);
            case KW_FALSE -> match(PLPTokenKinds.Kind.KW_FALSE);
            case INT_LITERAL -> match(PLPTokenKinds.Kind.INT_LITERAL);
            case STRING_LITERAL -> match(PLPTokenKinds.Kind.STRING_LITERAL);
            case LPAREN -> {
                match(PLPTokenKinds.Kind.LPAREN);
                expression();
                match(PLPTokenKinds.Kind.RPAREN);
            }
            case IDENTIFIER -> {
                match(PLPTokenKinds.Kind.IDENTIFIER);

                if (token.getKind() == PLPTokenKinds.Kind.LPAREN)
                {
                    match(PLPTokenKinds.Kind.LPAREN);
                    try
                    {
                        expression();
                        while (token.getKind() == PLPTokenKinds.Kind.COMMA)
                        {
                            match(PLPTokenKinds.Kind.COMMA);
                            expression();
                        }
                    } catch (SyntaxException e)
                    {
                        e.printStackTrace();
                    }
                    match(PLPTokenKinds.Kind.RPAREN);
                }
                else if (token.getKind() == PLPTokenKinds.Kind.LSQUARE)
                {
                    match(PLPTokenKinds.Kind.LSQUARE);
                    expression();
                    match(PLPTokenKinds.Kind.RSQUARE);
                }
            }
        }
    }

//
//    private void factor() throws SyntaxException
//    {
//        switch (token.getKind())
//        {
//            case INT_LITERAL -> match(PLPTokenKinds.Kind.INT_LITERAL);
//            case LPAREN -> {
//                match(PLPTokenKinds.Kind.LPAREN);
//                expr();
//                match(PLPTokenKinds.Kind.RPAREN);
//            }
//        }
//    }
//
//    private void term() throws SyntaxException
//    {
//        factor();
//        while (token.getKind() == PLPTokenKinds.Kind.TIMES || token.getKind() == PLPTokenKinds.Kind.DIV)
//        {
//            switch (token.getKind())
//            {
//                case TIMES -> match(PLPTokenKinds.Kind.TIMES);
//                case DIV -> match(PLPTokenKinds.Kind.DIV);
//            }
//            factor();
//        }
//    }
//
//    private void expr() throws SyntaxException
//    {
//        term();
//        while (token.getKind() == PLPTokenKinds.Kind.PLUS || token.getKind() == PLPTokenKinds.Kind.MINUS)
//        {
//            switch (token.getKind())
//            {
//                case PLUS -> match(PLPTokenKinds.Kind.PLUS);
//                case MINUS -> match(PLPTokenKinds.Kind.MINUS);
//            }
//            term();
//        }
//    }
}

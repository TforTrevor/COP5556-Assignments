package edu.ufl.cise.plpfa21.assignment2;

import edu.ufl.cise.plpfa21.assignment1.*;
import edu.ufl.cise.plpfa21.assignment3.ast.*;
import edu.ufl.cise.plpfa21.assignment3.astimpl.*;

import java.lang.reflect.Array;
import java.util.ArrayList;

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
        return program();
    }

    private IPLPToken consume()
    {
        try
        {
            IPLPToken returnToken = token;
            token = lexer.nextToken();
            return returnToken;
        } catch (LexicalException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private IPLPToken match(PLPTokenKinds.Kind kind) throws SyntaxException
    {
        if (token.getKind() == kind)
        {
            return consume();
        }
        else
        {
            throw new SyntaxException("Token kind: " + token.getKind().name() + " Match kind: " + kind.name(), token.getLine(), token.getCharPositionInLine());
        }
    }

    private IProgram program() throws SyntaxException
    {
        ArrayList<IDeclaration> declarations = new ArrayList<>();

        IDeclaration next = null;
        do
        {
            next = declaration();
            if (next != null)
            {
                declarations.add(next);
            }
        } while (next != null);

        return new Program__(token.getLine(), token.getCharPositionInLine(), token.getText(), declarations);
    }

    private IDeclaration declaration() throws SyntaxException
    {
        switch (token.getKind())
        {
            case KW_FUN -> {
                return function();
            }
            case KW_VAR -> {
                IPLPToken varToken = match(PLPTokenKinds.Kind.KW_VAR);
                INameDef varDef = nameDef();
                IExpression expression = null;
                if (token.getKind() == PLPTokenKinds.Kind.ASSIGN)
                {
                    match(PLPTokenKinds.Kind.ASSIGN);
                    expression = expression();
                }
                match(PLPTokenKinds.Kind.SEMI);

                return new MutableGlobal__(varToken.getLine(), varToken.getCharPositionInLine(), varToken.getText(), varDef, expression);
            }
            case KW_VAL -> {
                IPLPToken valToken = match(PLPTokenKinds.Kind.KW_VAL);
                INameDef nameDef = nameDef();
                match(PLPTokenKinds.Kind.ASSIGN);
                IExpression expression = expression();
                match(PLPTokenKinds.Kind.SEMI);
                return new ImmutableGlobal__(valToken.getLine(), valToken.getCharPositionInLine(), valToken.getText(), nameDef, expression);
            }
        }
        return null;
    }

    private IFunctionDeclaration function() throws SyntaxException
    {
        IPLPToken functionToken = match(PLPTokenKinds.Kind.KW_FUN);
        IPLPToken identifierToken = match(PLPTokenKinds.Kind.IDENTIFIER);
        IIdentifier identifier = new Identifier__(identifierToken.getLine(), identifierToken.getCharPositionInLine(), identifierToken.getText(), identifierToken.getText());
        match(PLPTokenKinds.Kind.LPAREN);

        ArrayList<INameDef> arguments = new ArrayList<>();
        if (token.getKind() == PLPTokenKinds.Kind.IDENTIFIER)
        {
            arguments.add(nameDef());
            while (token.getKind() == PLPTokenKinds.Kind.COMMA)
            {
                match(PLPTokenKinds.Kind.COMMA);
                arguments.add(nameDef());
            }
        }

        match(PLPTokenKinds.Kind.RPAREN);

        IType resultType = null;
        if (token.getKind() == PLPTokenKinds.Kind.COLON)
        {
            match(PLPTokenKinds.Kind.COLON);
            resultType = type();
        }

        match(PLPTokenKinds.Kind.KW_DO);
        IBlock block = block();
        match(PLPTokenKinds.Kind.KW_END);

        return new FunctionDeclaration___(functionToken.getLine(), functionToken.getCharPositionInLine(), functionToken.getText(), identifier, arguments, resultType, block);
    }

    private INameDef nameDef() throws SyntaxException
    {
        IPLPToken identifierToken = match(PLPTokenKinds.Kind.IDENTIFIER);
        IIdentifier identifier = new Identifier__(identifierToken.getLine(), identifierToken.getCharPositionInLine(), identifierToken.getText(), identifierToken.getText());

        IType type = null;
        if (token.getKind() == PLPTokenKinds.Kind.COLON)
        {
            match(PLPTokenKinds.Kind.COLON);
            type = type();
        }

        return new NameDef__(identifierToken.getLine(), identifierToken.getCharPositionInLine(), identifierToken.getText(), identifier, type);
    }

    private IType type() throws SyntaxException
    {
        switch (token.getKind())
        {
            case KW_INT -> {
                IPLPToken token = match(PLPTokenKinds.Kind.KW_INT);
                return new PrimitiveType__(token.getLine(), token.getCharPositionInLine(), token.getText(), IType.TypeKind.INT);
            }
            case KW_STRING -> {
                IPLPToken token = match(PLPTokenKinds.Kind.KW_STRING);
                return new PrimitiveType__(token.getLine(), token.getCharPositionInLine(), token.getText(), IType.TypeKind.STRING);
            }
            case KW_BOOLEAN -> {
                IPLPToken token = match(PLPTokenKinds.Kind.KW_BOOLEAN);
                return new PrimitiveType__(token.getLine(), token.getCharPositionInLine(), token.getText(), IType.TypeKind.BOOLEAN);
            }
            case KW_LIST -> {
                IPLPToken listToken = match(PLPTokenKinds.Kind.KW_LIST);
                match(PLPTokenKinds.Kind.LSQUARE);
                IType type = null;
                if (token.getKind() != PLPTokenKinds.Kind.RSQUARE)
                {
                    type = type();
                }
                match(PLPTokenKinds.Kind.RSQUARE);
                return new ListType__(listToken.getLine(), listToken.getCharPositionInLine(), listToken.getText(), type);
            }
        }
        return null;
    }

    private IBlock block() throws SyntaxException
    {
        IPLPToken blockToken = this.token;
        ArrayList<IStatement> statements = new ArrayList<>();
        while (token.getKind() != PLPTokenKinds.Kind.KW_END && token.getKind() != PLPTokenKinds.Kind.KW_DEFAULT && token.getKind() != PLPTokenKinds.Kind.KW_CASE)
        {
            statements.add(statement());
        }
        return new Block__(blockToken.getLine(), blockToken.getCharPositionInLine(), blockToken.getText(), statements);
    }

    private IStatement statement() throws SyntaxException
    {
        switch (token.getKind())
        {
            case KW_LET -> {
                IPLPToken token = match(PLPTokenKinds.Kind.KW_LET);
                INameDef localDef = nameDef();
                IExpression expression = null;
                if (this.token.getKind() == PLPTokenKinds.Kind.ASSIGN)
                {
                    match(PLPTokenKinds.Kind.ASSIGN);
                    expression = expression();
                }
                match(PLPTokenKinds.Kind.KW_DO);
                IBlock block = block();
                match(PLPTokenKinds.Kind.KW_END);
                //match(PLPTokenKinds.Kind.SEMI);

                return new LetStatement__(token.getLine(), token.getCharPositionInLine(), token.getText(), block, expression, localDef);
            }
            case KW_SWITCH -> {
                IPLPToken switchToken = match(PLPTokenKinds.Kind.KW_SWITCH);
                IExpression switchExpression = expression();

                ArrayList<IExpression> branchExpressions = new ArrayList<>();
                ArrayList<IBlock> blocks = new ArrayList<>();
                while (token.getKind() == PLPTokenKinds.Kind.KW_CASE)
                {
                    match(PLPTokenKinds.Kind.KW_CASE);
                    branchExpressions.add(expression());
                    match(PLPTokenKinds.Kind.COLON);
                    blocks.add(block());
                }
                match(PLPTokenKinds.Kind.KW_DEFAULT);
                IBlock defaultBlock = block();
                match(PLPTokenKinds.Kind.KW_END);

                return new SwitchStatement__(switchToken.getLine(), switchToken.getCharPositionInLine(), switchToken.getText(),
                        switchExpression, branchExpressions, blocks, defaultBlock);
            }
            case KW_IF -> {
                IPLPToken ifToken = match(PLPTokenKinds.Kind.KW_IF);
                IExpression guardExpression = expression();
                match(PLPTokenKinds.Kind.KW_DO);
                IBlock ifBlock = block();
                match(PLPTokenKinds.Kind.KW_END);

                return new IfStatement__(ifToken.getLine(), ifToken.getCharPositionInLine(), ifToken.getText(), guardExpression, ifBlock);
            }
            case KW_WHILE -> {
                IPLPToken whileToken = match(PLPTokenKinds.Kind.KW_WHILE);
                IExpression guardExpression = expression();
                match(PLPTokenKinds.Kind.KW_DO);
                IBlock block = block();
                match(PLPTokenKinds.Kind.KW_END);

                return new WhileStatement__(whileToken.getLine(), whileToken.getCharPositionInLine(), whileToken.getText(), guardExpression, block);
            }
            case KW_RETURN -> {
                IPLPToken returnToken = match(PLPTokenKinds.Kind.KW_RETURN);
                IExpression returnExpression = expression();
                match(PLPTokenKinds.Kind.SEMI);

                return new ReturnStatement__(returnToken.getLine(), returnToken.getCharPositionInLine(), returnToken.getText(), returnExpression);
            }
            default -> {
                IPLPToken token = this.token;
                IExpression left = expression();
                IExpression right = null;
                if (this.token.getKind() == PLPTokenKinds.Kind.ASSIGN)
                {
                    match(PLPTokenKinds.Kind.ASSIGN);
                    right = expression();
                }
                match(PLPTokenKinds.Kind.SEMI);
                return new AssignmentStatement__(token.getLine(), token.getCharPositionInLine(), token.getText(), left, right);
            }
        }
    }

    private IExpression expression() throws SyntaxException
    {
        return logicalExpression();
    }

    private IExpression logicalExpression() throws SyntaxException
    {
        IExpression left;
        IExpression right;
        left = comparisonExpression();
        while (token.getKind() == PLPTokenKinds.Kind.AND || token.getKind() == PLPTokenKinds.Kind.OR)
        {
            IPLPToken token = this.token;
            PLPTokenKinds.Kind kind = null;
            switch (token.getKind())
            {
                case AND -> {
                    match(PLPTokenKinds.Kind.AND);
                    kind = PLPTokenKinds.Kind.AND;
                }
                case OR -> {
                    match(PLPTokenKinds.Kind.OR);
                    kind = PLPTokenKinds.Kind.OR;
                }
            }
            right = comparisonExpression();
            left = new BinaryExpression__(token.getLine(), token.getCharPositionInLine(), token.getText(), left, right, kind);
        }
        return left;
    }

    private IExpression comparisonExpression() throws SyntaxException
    {
        IExpression left;
        IExpression right;
        left = additiveExpression();
        while (token.getKind() == PLPTokenKinds.Kind.LT || token.getKind() == PLPTokenKinds.Kind.GT ||
                token.getKind() == PLPTokenKinds.Kind.EQUALS || token.getKind() == PLPTokenKinds.Kind.NOT_EQUALS)
        {
            IPLPToken token = this.token;
            PLPTokenKinds.Kind kind = null;
            switch (token.getKind())
            {
                case LT -> {
                    match(PLPTokenKinds.Kind.LT);
                    kind = PLPTokenKinds.Kind.LT;
                }
                case GT -> {
                    match(PLPTokenKinds.Kind.GT);
                    kind = PLPTokenKinds.Kind.GT;
                }
                case EQUALS -> {
                    match(PLPTokenKinds.Kind.EQUALS);
                    kind = PLPTokenKinds.Kind.EQUALS;
                }
                case NOT_EQUALS -> {
                    match(PLPTokenKinds.Kind.NOT_EQUALS);
                    kind = PLPTokenKinds.Kind.NOT_EQUALS;
                }
            }
            right = additiveExpression();
            left = new BinaryExpression__(token.getLine(), token.getCharPositionInLine(), token.getText(), left, right, kind);
        }
        return left;
    }

    private IExpression additiveExpression() throws SyntaxException
    {
        IExpression left;
        IExpression right;
        left = multiplicativeExpression();
        while (token.getKind() == PLPTokenKinds.Kind.PLUS || token.getKind() == PLPTokenKinds.Kind.MINUS)
        {
            IPLPToken token = this.token;
            PLPTokenKinds.Kind kind = null;
            switch (token.getKind())
            {
                case PLUS -> {
                    match(PLPTokenKinds.Kind.PLUS);
                    kind = PLPTokenKinds.Kind.PLUS;
                }
                case MINUS -> {
                    match(PLPTokenKinds.Kind.MINUS);
                    kind = PLPTokenKinds.Kind.MINUS;
                }
            }
            right = multiplicativeExpression();
            left = new BinaryExpression__(token.getLine(), token.getCharPositionInLine(), token.getText(), left, right, kind);
        }
        return left;
    }

    private IExpression multiplicativeExpression() throws SyntaxException
    {
        IExpression left;
        IExpression right;
        left = unaryExpression();
        while (token.getKind() == PLPTokenKinds.Kind.TIMES || token.getKind() == PLPTokenKinds.Kind.DIV)
        {
            IPLPToken token = this.token;
            PLPTokenKinds.Kind kind = null;
            switch (token.getKind())
            {
                case TIMES -> {
                    match(PLPTokenKinds.Kind.TIMES);
                    kind = PLPTokenKinds.Kind.TIMES;
                }
                case DIV -> {
                    match(PLPTokenKinds.Kind.DIV);
                    kind = PLPTokenKinds.Kind.DIV;
                }
            }
            right = unaryExpression();
            left = new BinaryExpression__(token.getLine(), token.getCharPositionInLine(), token.getText(), left, right, kind);
        }
        return left;
    }

    private IExpression unaryExpression() throws SyntaxException
    {
        if (token.getKind() == PLPTokenKinds.Kind.BANG || token.getKind() == PLPTokenKinds.Kind.MINUS)
        {
            switch (token.getKind())
            {
                case BANG -> {
                    IPLPToken token = match(PLPTokenKinds.Kind.BANG);
                    return new UnaryExpression__(token.getLine(), token.getCharPositionInLine(), token.getText(), primaryExpression(), PLPTokenKinds.Kind.BANG);
                }
                case MINUS -> {
                    IPLPToken token = match(PLPTokenKinds.Kind.MINUS);
                    return new UnaryExpression__(token.getLine(), token.getCharPositionInLine(), token.getText(), primaryExpression(), PLPTokenKinds.Kind.MINUS);
                }
            }
        }
        return primaryExpression();
    }

    private IExpression primaryExpression() throws SyntaxException
    {
        switch (token.getKind())
        {
            case KW_NIL -> {
                IPLPToken token = match(PLPTokenKinds.Kind.KW_NIL);
                return new NilConstantExpression__(token.getLine(), token.getCharPositionInLine(), token.getText());
            }
            case KW_TRUE -> {
                IPLPToken token = match(PLPTokenKinds.Kind.KW_TRUE);
                return new BooleanLiteralExpression__(token.getLine(), token.getCharPositionInLine(), token.getText(), true);
            }
            case KW_FALSE -> {
                IPLPToken token = match(PLPTokenKinds.Kind.KW_FALSE);
                return new BooleanLiteralExpression__(token.getLine(), token.getCharPositionInLine(), token.getText(), false);
            }
            case INT_LITERAL -> {
                IPLPToken token = match(PLPTokenKinds.Kind.INT_LITERAL);
                return new IntLiteralExpression__(token.getLine(), token.getCharPositionInLine(), token.getText(), Integer.parseInt(token.getText()));
            }
            case STRING_LITERAL -> {
                IPLPToken token = match(PLPTokenKinds.Kind.STRING_LITERAL);
                return new StringLiteralExpression__(token.getLine(), token.getCharPositionInLine(), token.getText(), token.getStringValue());
            }
            case LPAREN -> {
                match(PLPTokenKinds.Kind.LPAREN);
                IExpression expression = expression();
                match(PLPTokenKinds.Kind.RPAREN);
                return expression;
            }
            case IDENTIFIER -> {
                IPLPToken identifierToken = match(PLPTokenKinds.Kind.IDENTIFIER);
                IIdentifier identifier = new Identifier__(identifierToken.getLine(), identifierToken.getCharPositionInLine(), identifierToken.getText(), identifierToken.getText());

                if (token.getKind() == PLPTokenKinds.Kind.LPAREN)
                {
                    match(PLPTokenKinds.Kind.LPAREN);
                    ArrayList<IExpression> arguments = new ArrayList<>();
                    try
                    {
                        arguments.add(expression());
                        while (token.getKind() == PLPTokenKinds.Kind.COMMA)
                        {
                            match(PLPTokenKinds.Kind.COMMA);
                            arguments.add(expression());
                        }
                    } catch (SyntaxException e)
                    {
                        e.printStackTrace();
                    }
                    match(PLPTokenKinds.Kind.RPAREN);
                    return new FunctionCallExpression__(token.getLine(), token.getCharPositionInLine(), token.getText(), identifier, arguments);
                }
                else if (token.getKind() == PLPTokenKinds.Kind.LSQUARE)
                {
                    match(PLPTokenKinds.Kind.LSQUARE);
                    IExpression expression = expression();
                    match(PLPTokenKinds.Kind.RSQUARE);
                    return expression;
                }

                return new IdentExpression__(identifierToken.getLine(), identifierToken.getCharPositionInLine(), identifierToken.getText(), identifier);
            }
        }
        return null;
    }
}

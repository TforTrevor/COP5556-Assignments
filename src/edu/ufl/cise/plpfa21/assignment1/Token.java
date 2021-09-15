package edu.ufl.cise.plpfa21.assignment1;

public class Token implements IPLPToken
{
    private final Kind kind;
    private final String text;
    private final int lineIndex;
    private final int charIndex;

    public Token(Kind kind, String text, int lineIndex, int charIndex)
    {
        this.kind = kind;
        this.text = text;
        this.lineIndex = lineIndex;
        this.charIndex = charIndex;
    }

    @Override
    public Kind getKind()
    {
        return kind;
    }

    @Override
    public String getText()
    {
        return text;
    }

    @Override
    public int getLine()
    {
        return lineIndex + 1;
    }

    @Override
    public int getCharPositionInLine()
    {
        return charIndex;
    }

    @Override
    public String getStringValue()
    {
        return text.substring(1, text.length() - 1);
    }

    @Override
    public int getIntValue()
    {
        return Integer.parseInt(text);
    }
}

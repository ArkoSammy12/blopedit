package xd.arkosammy.blopedit.properties;

public class EmptyFileLine extends FileLine {
    protected EmptyFileLine(String originalLine) {
        super(originalLine);
    }

    @Override
    public String getString() {
        return this.originalLine;
    }
}

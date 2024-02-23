package xd.arkosammy.blopedit.properties;

public class EmptyFileLine extends FileLine {
    protected EmptyFileLine() {
        super("");
    }

    @Override
    public String getString() {
        return this.originalLine;
    }
}

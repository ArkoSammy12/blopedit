package xd.arkosammy.blopedit.properties;

public class CommentFileLine extends FileLine {
    protected CommentFileLine(String originalLine) {
        super(originalLine);
    }

    @Override
    public String getString() {
        return this.originalLine;
    }
}

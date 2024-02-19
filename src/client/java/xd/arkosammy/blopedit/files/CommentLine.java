package xd.arkosammy.blopedit.files;

public class CommentLine implements FileLine {

    private final String line;

    CommentLine(String line){
        this.line = line;
    }

    @Override
    public String getString() {
        return line;
    }
}

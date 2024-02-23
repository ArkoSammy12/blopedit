package xd.arkosammy.blopedit.properties;

public abstract class FileLine {

    protected String originalLine;

    protected FileLine(String originalLine){
        this.originalLine = originalLine;
    }

    public static FileLine newFileLine(String line){
        if(line.trim().isEmpty()){
            return new EmptyFileLine();
        } else if (line.trim().charAt(0) == '#') {
            return new CommentFileLine(line);
        } else {
            return new PropertyFileLine(line);
        }
    }


    /**
     * Returns a string which reflects all changes made to this file line.
     */
    public abstract String getString();

}

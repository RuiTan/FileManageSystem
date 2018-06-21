package sample;

import javafx.scene.Parent;

import java.util.Vector;

public class FileContent {

    public static final int DEFAULTCAPACITY = 50;

    static class Content{
        String filepath;
        String filename;
        String content;
        public Content(){
            this.filepath = "";
            this.filename = "";
            this.content = "";
        }
        public Content(String filepath, String filename, String content) {
            this.filepath = filepath;
            this.filename = filename;
            this.content = content;
        }
    }

    String GetContent(String filepath, String filename){
        for (Content c : content){
            if (c.filename.equals(filename) && c.filepath.equals(filepath)){
                return c.content;
            }
        }
        return "";
    }

    Content GetContentInstant(String filepath, String filename){
        for (Content c : content){
            if (c.filepath.equals(filepath) && c.filename.equals(filename)){
                return c;
            }
        }
        return null;
    }

    private Vector<Content> content;

    public FileContent() {
        content = new Vector<>();
    }

    public FileContent(Vector<Content> content) {
        this.content = content;
    }

    public Vector<Content> getContent() {
        return content;
    }

    public void setContent(Vector<Content> content) {
        this.content = content;
    }


    public void addContent(String data, int pos){
        content.get(pos).content = data;
    }
}

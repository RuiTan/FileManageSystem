package sample;

import java.io.*;
import java.util.Scanner;

public class FileSystem {
    static final Integer WRONG = -1;
    static final Integer FILE = 0;
    static final Integer FOLDER = 1;

    static final String NAME = "#NAME";
    static final String PATH = "#PATH";
    static final String CONTENT = "#CONTENT";
    static final String CONTENTEND = "#CONTENT_END";

    static final String ROOT = "2018-06-21 21:46:15";
    static final String CATALOGFILE = "./catalog.txt";
    static final String CONTENTFILE = "./content.txt";

    FileAttribute root;
    FileContent contents;

    public FileSystem() {
        root = new FileAttribute("root", "/", true, ROOT);
        contents = new FileContent();
    }

    public void GetCatalog() throws IOException {
        File file = new File(CATALOGFILE);
        System.getProperty(CATALOGFILE);
        Scanner scanner = new Scanner(new FileInputStream(file));
        while (scanner.hasNextLine()){
            String line = scanner.nextLine();
            GetFileFromString(line);
        }
        GetContent();
    }

    private void GetContent() throws FileNotFoundException {
        File file = new File(CONTENTFILE);
        Scanner scanner = new Scanner(new FileInputStream(file));
        while (scanner.hasNextLine()){
            String name = "";
            String path = "";
            StringBuilder content = new StringBuilder();
            if (scanner.nextLine().equals(NAME)){
                name = scanner.nextLine();
            }
            if (scanner.nextLine().equals(PATH)){
                path = scanner.nextLine();
            }
            if (scanner.nextLine().equals(CONTENT)){
                String line = scanner.nextLine();
                while (!line.equals(CONTENTEND)){
                    content.append(line);
                    line = scanner.nextLine();
                }
            }
            contents.getContent().add(new FileContent.Content(path, name, content.toString()));
        }
    }

    void PrintCatalog() throws IOException {
        FileWriter fileWriter = new FileWriter(CATALOGFILE);
        FileAttribute file = root;
        RecursivePrint(fileWriter, file);
        fileWriter.flush();
        fileWriter.close();
        PrintContent();
    }

    private void RecursivePrint(FileWriter fw, FileAttribute file) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(file.name).append(",")
                .append(file.path).append(",")
                .append(file.type).append(",")
                .append(file.size).append(",")
                .append(file.pos).append(",")
                .append(file.initialTime).append(",")
                .append(file.latestChangeTime).append(",")
                .append(file.password).append("\n");
        if (!file.name.equals("root")){
            fw.write(stringBuilder.toString());
        }
        if (file.type){
            for (FileAttribute f : file.subFiles){
                RecursivePrint(fw, f);
            }
        }
    }

    private void PrintContent() throws IOException {
        FileWriter fileWriter = new FileWriter(CONTENTFILE);
        for (FileContent.Content content : contents.getContent()){
            fileWriter.write(NAME + "\n");
            fileWriter.write(content.filename + "\n");
            fileWriter.write(PATH + "\n");
            fileWriter.write(content.filepath + "\n");
            fileWriter.write(CONTENT + "\n");
            fileWriter.write(content.content + "\n");
            fileWriter.write(CONTENTEND + "\n");
        }
        fileWriter.flush();
        fileWriter.close();
    }

    private void GetFileFromString(String line){
        String[] attributes = line.split(",");
        String name = attributes[0];
        String path = attributes[1];
        Boolean type = attributes[2].equals("true");
        Double size = Double.parseDouble(attributes[3]);
        Integer pos = Integer.parseInt(attributes[4]);
        String initialTime = attributes[5];
        String latestChangeTime = attributes[6];
        FileAttribute fileAttribute;
        if (attributes.length == 7){
            fileAttribute = new FileAttribute(name, path, type, size, pos, initialTime, latestChangeTime, " ");
        }else {
            fileAttribute = new FileAttribute(name, path, type, size, pos, initialTime, latestChangeTime, attributes[7]);
        }
        AddFileToPath(fileAttribute);
    }

    public void DeleteFileOrFolder(FileAttribute parent, FileAttribute thisItem){
        for (FileAttribute f : parent.subFiles){
            if (f.name.equals(thisItem.name)){
                parent.subFiles.remove(f);
                return;
            }
        }
        DeleteContent(thisItem);
    }

    public void DeleteContent(FileAttribute fileAttribute){
        if (fileAttribute.type){
            for (FileAttribute f : fileAttribute.subFiles){
                DeleteContent(f);
            }
        }else {
            contents.getContent().remove(contents.GetContentInstant(fileAttribute.path, fileAttribute.name));
        }
    }

    private void AddFileToPath(FileAttribute file){
        String[] paths = file.path.split("/");
        FileAttribute parent = root;
        for (String p : paths){
            if (!p.equals("") && !p.equals("root")){
                assert parent != null;
                parent = SearchFile(p, parent);
            }
        }
        // 此处添加文件内容重复了
//        if (!file.type){
//            RemoveUnusedSpace();
//            AddFileContent(file.name, file.path);
//        }
        assert parent != null;
        parent.subFiles.add(file);
    }

    private FileAttribute SearchFile(String name, FileAttribute fileAttribute){
        for (int i = 0; i < fileAttribute.subFiles.size(); i++){
            if (fileAttribute.subFiles.get(i).name.equals(name)){
                return fileAttribute.subFiles.get(i);
            }
        }
        return null;
    }

    public FileAttribute FindPath(String[] paths){
        FileAttribute parent = root;
        for (int i = 2; i < paths.length; i++){
            parent = parent.FindFromSubfiles(paths[i]);
            if (parent == null){
                return null;
            }
        }
        return parent;
    }

    public Integer AddFileAttribute(FileAttribute fileAttribute){
        FileAttribute parent = FindPath(fileAttribute.path.split("/"));
        if (parent != null){
            if (parent.FindFromSubfiles(fileAttribute.name) == null){
                if (!fileAttribute.type){
//                    RemoveUnusedSpace();
                    AddFileContent(fileAttribute.name, fileAttribute.path);
                }
                parent.subFiles.add(fileAttribute);
                parent.size += fileAttribute.size;
                return fileAttribute.type?FOLDER:FILE;
            }else {
                return WRONG;
            }
        }else {
            return WRONG;
        }
    }

    public void AddFileContent(String name, String path){
        FileContent.Content content = new FileContent.Content();
        content.filepath = path;
        content.filename = name;
        contents.getContent().add(content);
    }

    public boolean FindFileFromPath(String[] paths, String name){
        FileAttribute parent = FindPath(paths);
        if (parent == null)
            return false;
        return parent.FindFromSubfiles(name) != null;
    }


//    public void RemoveUnusedSpace(){
//        for (FileContent.Content content : contents.getContent()) {
//            if (!FindFileFromPath(content.filepath.split("/"), content.filename)){
//                contents.getContent().remove(content);
//            }
//        }
//    }

    public Integer CalcFileAttributeSize(FileAttribute fileAttribute){
        if (!fileAttribute.type){
            return  (contents.GetContent(fileAttribute.path, fileAttribute.name).length())*Controller.BYTE;
        }else {
            Integer size = 0;
            for (FileAttribute f : fileAttribute.subFiles){
                size += CalcFileAttributeSize(f);
            }
            return size;
        }
    }
}

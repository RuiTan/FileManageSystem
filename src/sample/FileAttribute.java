package sample;

import java.util.Vector;

public class FileAttribute {
    String name;
    // 路径
    String path;
    // false为文件，true为文件夹
    boolean type;
    // 文件大小
    Double size;
    // 文件物理地址，对应FileContent中的内容
    Integer pos;
    // 创建时间
    String initialTime;
    // 最近修改时间
    String latestChangeTime;
    // 密码
    String password;
    // 子目录
    Vector<FileAttribute> subFiles;

    public FileAttribute FindFromSubfiles(String name){
        if (subFiles.size() == 0){
            return null;
        }
        for (FileAttribute file : subFiles){
            if (file.name.equals(name)){
                return file;
            }
        }
        return null;
    }

    public FileAttribute(String name, String path, boolean type, String initialTime) {
        this.name = name;
        this.path = path;
        this.type = type;
        this.size = 0.0;
        this.pos = -1;
        this.initialTime = initialTime;
        this.latestChangeTime = initialTime;
        this.password = " ";
        if (type){
            subFiles = new Vector<>();
        }
    }

    public FileAttribute(String name, String path, boolean type, Double size, Integer pos, String initialTime, String latestChangeTime, String password) {
        this.name = name;
        this.path = path;
        this.type = type;
        this.size = size;
        this.pos = pos;
        this.initialTime = initialTime;
        this.latestChangeTime = latestChangeTime;
        this.password = password.equals("") ? " " : password;
        if (type){
            subFiles = new Vector<>();
        }
    }

}

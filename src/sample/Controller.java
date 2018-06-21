package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class Controller {

    private static Integer File = 1;
    private static Integer Folder = 1;


    private static final Integer ROOT = 0;
    private static final Integer FOLDER = 1;
    private static final Integer FILE = 2;

    public static final Integer BYTE = 2;

    private Node getIcon(Integer type){
        if (type.equals(ROOT)){
            return new ImageView(
                    new Image(String.valueOf(getClass().getResource("/image/root.png")))
            );
        }else if (type.equals(FOLDER)){
            return new ImageView(
                    new Image(getClass().getResourceAsStream("/image/folder.png"))
            );
        }else {
            return new ImageView(
                    new Image(getClass().getResourceAsStream("/image/document.png"))
            );
        }
    }

    @FXML
    public TreeView<String> treeView;

    @FXML
    public TableView<Property_Value> tableView;

    @FXML
    public TableColumn propertyColumn;

    @FXML
    public TableColumn valueColumn;

    private ObservableList<Property_Value> data = FXCollections.observableArrayList();

    public class Property_Value{
        String property;
        String value;

        public String getProperty() {
            return property;
        }

        public void setProperty(String property) {
            this.property = property;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public Property_Value() {
        }

        public Property_Value(String property, String value) {

            this.property = property;
            this.value = value;
        }
    }

    @FXML
    public Button saveAndClose;

    @FXML
    public TextArea filetext;

    @FXML
    public Label fileOpened;

    @FXML
    public Label fileOpenedName;

    @FXML
    public Button initSystem;

    @FXML
    private void InitialSystem() {
        initSystem.setVisible(false);
        treeView.setEditable(true);
        treeView.setCellFactory(param -> new TextFieldTreeCellImpl());
        TreeItem<String> rootItem = new TreeItem<>(fileSystem.root.name, getIcon(ROOT));
        for (FileAttribute fileAttribute : fileSystem.root.subFiles){
            addChild(rootItem, fileAttribute);
        }
        treeView.setRoot(rootItem);
        // 初始化"文件/文件夹属性"表格视图
        propertyColumn.setCellValueFactory(new PropertyValueFactory<>("property"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        tableView.setItems(data);
        // 初始化打开文件界面
        saveAndClose.setVisible(false);
        filetext.setVisible(false);
        fileOpened.setVisible(false);
        fileOpenedName.setVisible(false);
    }

    private static FileSystem fileSystem;

    public static void createFileSystem(){
        try {
            fileSystem = new FileSystem();
            fileSystem.GetCatalog();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addChild(TreeItem<String> parent, FileAttribute fileAttribute) {
        parent.setExpanded(true);
        if (fileAttribute.type){
            TreeItem<String> child = new TreeItem<>(fileAttribute.name, getIcon(FOLDER));
            parent.getChildren().add(child);
            for (FileAttribute file : fileAttribute.subFiles){
                addChild(child, file);
            }
        }else {
            TreeItem<String> child = new TreeItem<>(fileAttribute.name, getIcon(FILE));
            parent.getChildren().add(child);
        }
    }

    private final class TextFieldTreeCellImpl extends TreeCell<String>{
        private TextField textField;
        private ContextMenu addFolderMenu = new ContextMenu();
        private ContextMenu addFileMenu = new ContextMenu();

        private void createTextField(){
            FileAttribute parent = fileSystem.FindPath(GetFilePathFromTreeItem(getTreeItem().getParent()).split("/"));
            textField = new TextField(getString());
            textField.setOnKeyReleased(event -> {
                if (event.getCode() == KeyCode.ENTER){
                    // 添加重名检测,且不能命名任何文件或文件夹为root
                    String text = textField.getText();
                    if (text.equals("root")){
                        cancelEdit();
                    }else {
                        if (parent.FindFromSubfiles(text) == null){
                            ChangeNameAndPath(parent.FindFromSubfiles(getTreeItem().getValue()), text);
//                            FileAttribute fileAttribute = fileSystem.FindPath(GetFilePathFromTreeItem(getTreeItem()).split("/"));
                            String path = GetFilePathFromTreeItem(getTreeItem().getParent());
                            fileSystem.contents.GetContentInstant(path, getTreeItem().getValue()).filename = text;
                            commitEdit(text);
                        }else {
                            cancelEdit();
                        }
                    }
                }else if (event.getCode() == KeyCode.ESCAPE){
                    cancelEdit();
                }
            });
        }

        private void ChangeNameAndPath(FileAttribute fileAttribute, String name){
            fileAttribute.name = name;
            String newPath = fileAttribute.path + name + "/";
            if (fileAttribute.type){
                for (FileAttribute f : fileAttribute.subFiles){
                    ChangePath(f, newPath);
                }
            }
        }

        private void ChangePath(FileAttribute fileAttribute, String path){
            fileAttribute.path = path;
            if (fileAttribute.type){
                String newPath = path + fileAttribute.name + "/";
                for (FileAttribute f : fileAttribute.subFiles){
                    ChangePath(f, newPath);
                }
            }
        }

        private String getString() { return getItem() == null ? "" : getItem(); }

        @Override
        public void startEdit(){
            super.startEdit();

            if (textField == null){
                createTextField();
            }
            setText(null);
            setGraphic(textField);
            textField.selectAll();
        }

        @Override
        public void cancelEdit(){
            super.cancelEdit();

            setText(getItem());
            setGraphic(getTreeItem().getGraphic());
        }

        @Override
        public void updateItem(String item, boolean empty){
            super.updateItem(item, empty);
            if (empty){
                setText(null);
                setGraphic(null);
            }else {
                if (isEditing()){
                    if (textField != null){
                        textField.setText(getString());
                    }
                    setText(null);
                    setGraphic(textField);

                }else {
                    TreeItem item1 = getTreeItem();
                    String item1Name= (String) item1.getValue();
                    Node item1Graph = item1.getGraphic();
                    setText(item1Name);
                    setGraphic(item1Graph);
                    FileAttribute fileAttribute = fileSystem.FindPath(GetFilePathFromTreeItem(getTreeItem()).split("/"));
                    try {
                        fileSystem.PrintCatalog();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (fileAttribute != null){
                        if (fileAttribute.type){
                            setContextMenu(addFolderMenu);
                        }else {
                            setContextMenu(addFileMenu);
                        }
                    }
                }
            }
        }

        private String GetFilePathFromTreeItem(TreeItem item){
            StringBuilder path = new StringBuilder().append("/");
            ArrayList<String> paths = new ArrayList<>();
            while (item != null){
                paths.add((String) item.getValue());
                item = item.getParent();
            }
            for (int i = paths.size() - 1; i >= 0; i--){
                path.append(paths.get(i)).append("/");
            }
            return path.toString();
        }

        public TextFieldTreeCellImpl(){
            // 新建文件
            MenuItem addFile = new MenuItem("新建文件");
            addFolderMenu.getItems().add(addFile);
            addFile.setOnAction(event -> {
                String name = "新建文件" + File++;
                FileAttribute fileAttribute = fileSystem.FindPath(GetFilePathFromTreeItem(getTreeItem()).split("/"));
                while (fileAttribute.FindFromSubfiles(name) != null){
                    name = "新建文件" + File++;
                }
                String path = GetFilePathFromTreeItem(getTreeItem());
                fileSystem.AddFileAttribute(new FileAttribute(name, path, false ,0.0, -1, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), ""));
                TreeItem<String> newFile = new TreeItem<>(name,getIcon(FILE));
                getTreeItem().getChildren().add(newFile);
            });

            // 新建文件夹
            MenuItem addFolder = new MenuItem("新建文件夹");
            addFolderMenu.getItems().add(addFolder);
            addFolder.setOnAction(event -> {
                String name = "新建文件夹" + Folder++;
                FileAttribute fileAttribute = fileSystem.FindPath(GetFilePathFromTreeItem(getTreeItem()).split("/"));
                while (fileAttribute.FindFromSubfiles(name) != null){
                    name = "新建文件夹" + Folder++;
                }
                String path = GetFilePathFromTreeItem(getTreeItem());
                fileSystem.AddFileAttribute(new FileAttribute(name, path, true, 0.0, -1, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), ""));
                TreeItem<String> newFolder = new TreeItem<>(name, getIcon(FOLDER));
                getTreeItem().getChildren().add(newFolder);
            });

            // 文件夹属性
            MenuItem viewFolderProperty = new MenuItem("属性");
            addFolderMenu.getItems().add(viewFolderProperty);
            viewFolderProperty.setOnAction(event -> {
                if (!data.isEmpty()){
                    data.remove(0, data.size());
                }
                ArrayList<Property_Value> pv = new ArrayList<>();
                FileAttribute fileAttribute = fileSystem.FindPath(GetFilePathFromTreeItem(getTreeItem()).split("/"));
                pv.add(new Property_Value("文件夹名", fileAttribute.name));
                pv.add(new Property_Value("路径", fileAttribute.path+fileAttribute.name));
                pv.add(new Property_Value("文件夹大小", String.valueOf(fileSystem.CalcFileAttributeSize(fileAttribute)) + "B"));
                pv.add(new Property_Value("创建日期", fileAttribute.initialTime));
                pv.add(new Property_Value("修改日期", fileAttribute.latestChangeTime));
                pv.add(new Property_Value("密码", fileAttribute.password));
                data.addAll(pv);
            });

            // 删除文件夹
            MenuItem deleteFolder = new MenuItem("删除文件夹 ");
            addFolderMenu.getItems().add(deleteFolder);
            deleteFolder.setOnAction(event -> {
                // 不能对root文件夹进行删除
                if (!getTreeItem().getValue().equals("root")){
                    // 从文件系统中删除该文件夹
                    FileAttribute parent = fileSystem.FindPath(GetFilePathFromTreeItem(getTreeItem().getParent()).split("/"));
                    FileAttribute thisItem = fileSystem.FindPath(GetFilePathFromTreeItem(getTreeItem()).split("/"));
                    fileSystem.DeleteFileOrFolder(parent, thisItem);
                    // 从树视图中删除
                    getTreeItem().getParent().getChildren().remove(getTreeItem());
                }
            });

            // 打开文件
            MenuItem openFile = new MenuItem("打开");
            addFileMenu.getItems().add(openFile);
            openFile.setOnAction(event -> {
                tableView.setVisible(false);
                filetext.setVisible(true);
                saveAndClose.setVisible(true);
                fileOpened.setVisible(true);
                fileOpenedName.setVisible(true);
                fileOpenedName.setText(getTreeItem().getValue());
                FileAttribute fileAttribute = fileSystem.FindPath(GetFilePathFromTreeItem(getTreeItem()).split("/"));
                FileContent.Content content = fileSystem.contents.GetContentInstant(fileAttribute.path, fileAttribute.name);
                filetext.setText(content.content);
                // 创建保存按钮监听事件
                saveAndClose.setOnAction(event1 -> {
                    content.content = filetext.getText();
                    tableView.setVisible(true);
                    filetext.setVisible(false);
                    saveAndClose.setVisible(false);
                    fileOpenedName.setVisible(false);
                    fileOpened.setVisible(false);
                    // 更改当前文件的最新操作时间以及其递归更改其上层文件夹直至到达root
                    fileAttribute.latestChangeTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                    TreeItem parent = getTreeItem().getParent();
                    while (parent != null){
                        fileSystem.FindPath(GetFilePathFromTreeItem(getTreeItem().getParent()).split("/")).latestChangeTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                        parent = parent.getParent();
                    }
                });
            });

            // 文件属性
            MenuItem viewFileProperty = new MenuItem("属性");
            addFileMenu.getItems().add(viewFileProperty);
            viewFileProperty.setOnAction(event -> {
                if (!data.isEmpty()){
                    data.remove(0, data.size());
                }
                ArrayList<Property_Value> pv = new ArrayList<>();
                FileAttribute fileAttribute = fileSystem.FindPath(GetFilePathFromTreeItem(getTreeItem()).split("/"));
                pv.add(new Property_Value("文件名", fileAttribute.name));
                pv.add(new Property_Value("路径", fileAttribute.path+fileAttribute.name));
                pv.add(new Property_Value("文件大小", String.valueOf(fileSystem.CalcFileAttributeSize(fileAttribute)) + "B"));
                pv.add(new Property_Value("创建日期", fileAttribute.initialTime));
                pv.add(new Property_Value("修改日期", fileAttribute.latestChangeTime));
                pv.add(new Property_Value("密码", fileAttribute.password));
                data.addAll(pv);
            });

            // 删除文件
            MenuItem deleteFile = new MenuItem("删除文件");
            addFileMenu.getItems().add(deleteFile);
            deleteFile.setOnAction(event -> {
                // 从文件系统中删除该文件
                FileAttribute parent = fileSystem.FindPath(GetFilePathFromTreeItem(getTreeItem().getParent()).split("/"));
                FileAttribute thisItem = fileSystem.FindPath(GetFilePathFromTreeItem(getTreeItem()).split("/"));
                fileSystem.DeleteFileOrFolder(parent, thisItem);
                // 从树视图中删除
                getTreeItem().getParent().getChildren().remove(getTreeItem());
                if (!data.isEmpty()){
                    data.remove(0, data.size());
                }
            });


//            viewProperty.setOnAction();

        }
    }

}

package sample;

import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ResourceBundle;


//    ObservableList <RuleModel> PossibleErrorsobservableList;
//    ObservableList <RuleModel> BestPracticesobservableList;
//    ObservableList <RuleModel> VariablesobservableList;
//    ObservableList <RuleModel> StylisticsobservableList;


public class Controller implements Initializable {

    private Stage primary_stage;
    public JFXTreeTableView jfxtable;
    public JFXCheckBox Default;
    public Label defaultlab,errorlabel,warninglabel,offlabel;
    public JFXTextField search;

    //CheckedData on the page
    JSONObject recordedData;

    //FinalData throughout the page
    JSONObject finalData;


    //Imported Object
    JSONObject importObject;
    public static final String[] name={"Possible Javascript Errors","Best Javascript Practices","Javascript Variables","Stylistic Javascript","Angular js"};
    public static final String [] path={ "possibleerrors.json","BestPractices.json","variables.json","stylistic.json","angular.json"};
    //Parent Checkboxes
    public JFXCheckBox error,warning,off;


    int pageNumber=0;
    //Bottom Buttons
    public JFXButton next,previous,skip,finish,importRules;

    ArrayList<ObservableList<RuleModel>> lists=new ArrayList<>();
    //Tree root
    private RecursiveTreeItem<RuleModel> root;
    private JFXTreeTableColumn ruleMode;
    private Callback<TreeTableColumn.CellDataFeatures<RuleModel, Boolean>, ObservableValue<Boolean>> checkboxfactory;
    private Callback<TreeTableColumn, TreeTableCell> checkboxvaluefactory;
    private Callback<TreeTableColumn, TreeTableCell> textboxfactory;


    public void setPrimary_stage(Stage primary_stage) {
        this.primary_stage = primary_stage;
    }

    public void initializeJSONObjects(){
        recordedData=new JSONObject();
        finalData=new JSONObject();

        checkboxfactory=new Callback<TreeTableColumn.CellDataFeatures<RuleModel,Boolean>, ObservableValue<Boolean>>() {
            @Override
            public ObservableValue call(TreeTableColumn.CellDataFeatures <RuleModel,Boolean> param) {
                return new SimpleBooleanProperty(param.getValue().getValue() != null);
            }
        };

        checkboxvaluefactory=new Callback<TreeTableColumn, TreeTableCell>() {
            int i=-1;
            @Override
            public TreeTableCell call(TreeTableColumn param) {
                if (i>=root.getChildren().size()){
                    i=-1;
                }
                return new CheckBoxesCell(i++);
            }
        };

        textboxfactory=new Callback<TreeTableColumn, TreeTableCell>() {
            int i=0;
            @Override
            public TreeTableCell call(TreeTableColumn param) {
                if (i>=root.getChildren().size()){
                    i=0;
                }
                return new TextFieldCell(i++);
            }
        };
    }

    public void useDefault(){
        ObservableList <RuleModel> observableList= lists.get(pageNumber);
        for (int i=0;i<observableList.size();i++){
            if (observableList.get(i).getRecommended()){
                recordedData.put(observableList.get(i).getName(),"error");
            }else{
                recordedData.put(observableList.get(i).getName(),"None");
            }
        }
    }

    public void processImport(){
        Iterator <?> keys=importObject.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            finalData.put(key, importObject.opt(key));
        }
    }

    public void recordData(){
        ObservableList <RuleModel> observableList= lists.get(pageNumber);
        for (int i=0;i<observableList.size();i++){
            if (finalData.opt(observableList.get(i).getName())==null) {
                if (pageNumber<=3) {
                    recordedData.put(observableList.get(i).getName(), "None");
                }else {
                    recordedData.put(observableList.get(i).getName(), "0");
                }
            }else{
                recordedData.put(observableList.get(i).getName(), finalData.optString(observableList.get(i).getName()));
            }
        }
    }




    public JSONArray getJSONArray(String filename){
        String line="";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(Main.rootPath+filename)))) {

            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                line+=sCurrentLine;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return new JSONArray(line);
    }


    public void getObservableList(){

        for (int i=0;i<path.length;i++) {
            ObservableList <RuleModel> observableList=FXCollections.observableArrayList();
            JSONArray rules = getJSONArray(path[i]);
            for (int j = 0; j < rules.length(); j++) {
                RuleModel rule = new RuleModel(rules.getJSONObject(j));
                observableList.add(rule);
            }
            lists.add(observableList);
        }
//
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeJSONObjects();

        if (Main.filename!=null){
            if (Main.getfileRuleObject(Main.filename).opt("rules")!=null) {
                importObject = Main.getfileRuleObject(Main.filename).getJSONObject("rules");
//                System.out.println(importObject.toString(4));
                processImport();
            }
        }

        getObservableList();
        recordData();
        search.setPromptText("Search Rules for "+name[pageNumber]);
//        System.out.println("Size is "+PossibleErrorsobservableList.size());
//        System.out.println(PossibleErrorsobservableList.get(0).getName());
        root= new RecursiveTreeItem<>(lists.get(pageNumber), RecursiveTreeObject::getChildren);

        JFXTreeTableColumn <RuleModel,String> ruleName=new JFXTreeTableColumn<>("Rule Name");
        ruleName.setMinWidth(250);

        ruleName.setCellValueFactory((TreeTableColumn.CellDataFeatures<RuleModel, String> param) ->{
            if(ruleName.validateValue(param)) return param.getValue().getValue().name;
            else return ruleName.getComputedValue(param);
        });

        next.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (pageNumber<=3) {
                    search.setText("");
                    pageNumber++;
                    search.setPromptText("Search Rules for "+name[pageNumber]);
                    if (pageNumber>=1){
                        previous.setVisible(true);
                    }else {
                        previous.setVisible(false);
                    }
                    if (pageNumber>3){
                        next.setVisible(false);
                        ruleMode.setCellFactory(new Callback<TreeTableColumn, TreeTableCell>() {
                            int i=0;
                            @Override
                            public TreeTableCell call(TreeTableColumn param) {
                                if (i>=root.getChildren().size()){
                                    i=0;
                                }
                                return new TextFieldCell(i++);
                            }
                        });
                        errorlabel.setVisible(false);
                        warninglabel.setVisible(false);
                        offlabel.setVisible(false);
                        error.setVisible(false);
                        warning.setVisible(false);
                        off.setVisible(false);
                    }

                    saveData();
                    setAllParentCheckBoxFalse();
                    recordedData=new JSONObject();
                    recordData();
                    root = new RecursiveTreeItem<>(lists.get(pageNumber), RecursiveTreeObject::getChildren);
                    jfxtable.setRoot(root);

                }
            }
        });

        search.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                ObservableList <RuleModel> parentList=lists.get(pageNumber);
                ObservableList <RuleModel> childList=FXCollections.observableArrayList();
                for (RuleModel child :parentList){
                    if (child.getName().toLowerCase().contains(newValue.toLowerCase())){
                        childList.add(child);
                    }
                }
                root = new RecursiveTreeItem<>(childList, RecursiveTreeObject::getChildren);

                jfxtable.setRoot(null);
                jfxtable.setRoot(root);
            }
        });

        skip.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (pageNumber<=3) {
                    search.setText("");
                    pageNumber++;
                    search.setPromptText("Search Rules for "+name[pageNumber]);
                    if (pageNumber>=1){
                        previous.setVisible(true);
                    }else {
                        previous.setVisible(false);
                    }
                    if (pageNumber>3){
                        next.setVisible(false);
                        ruleMode.setCellFactory(new Callback<TreeTableColumn, TreeTableCell>() {
                            int i=0;
                            @Override
                            public TreeTableCell call(TreeTableColumn param) {
                                if (i>=root.getChildren().size()){
                                    i=0;
                                }
                                return new TextFieldCell(i++);
                            }
                        });

                        error.setVisible(false);
                        warning.setVisible(false);
                        off.setVisible(false);
                        errorlabel.setVisible(false);
                        warninglabel.setVisible(false);
                        offlabel.setVisible(false);
                    }

                    setAllParentCheckBoxFalse();
                    recordedData=new JSONObject();
                    recordData();

                    root = new RecursiveTreeItem<>(lists.get(pageNumber), RecursiveTreeObject::getChildren);
                    jfxtable.setRoot(root);

                }
            }
        });
        previous.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                if (pageNumber>=1) {
                    search.setText("");
                    pageNumber--;
                    search.setPromptText("Search Rules for "+name[pageNumber]);
                    if (pageNumber>=1){
                        previous.setVisible(true);
                    }else {
                        previous.setVisible(false);
                    }
                    if (pageNumber==3){
                        System.out.println("Got called");
                        next.setVisible(true);
                        ruleMode.setCellFactory(new Callback<TreeTableColumn, TreeTableCell>() {
                            int i=0;
                            @Override
                            public TreeTableCell call(TreeTableColumn param) {
                                if (i>=root.getChildren().size()){
                                    i=0;
                                }
                                return new CheckBoxesCell(i++);
                            }
                        });

                        error.setVisible(true);
                        warning.setVisible(true);
                        off.setVisible(true);
                        errorlabel.setVisible(true);
                        warninglabel.setVisible(true);
                        offlabel.setVisible(true);
                    }

                    saveData();
                    setAllParentCheckBoxFalse();
                    recordedData=new JSONObject();
                    recordData();
                    root = new RecursiveTreeItem<>(lists.get(pageNumber), RecursiveTreeObject::getChildren);
                    jfxtable.setRoot(root);
                }
            }
        });

        finish.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                saveData();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        savefile(Main.getParentJSONObject().put("rules",finalData).toString(4));
                    }
                }).run();

            }
        });

        importRules.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        File file=openfile();
                        importObject = Main.getfileRuleObject(file.toString()).getJSONObject("rules");
                        processImport();
                        recordData();
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                jfxtable.setRoot(null);
                                jfxtable.setRoot(root);
                            }
                        });
                    }
                }).run();
            }
        });

        Default.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (pageNumber<=3) {
                    if (Default.isSelected()) {
                        error.setSelected(false);
                        warning.setSelected(false);
                        off.setSelected(false);
                        useDefault();
                    } else {
                        setAllRule("None");
                    }

                }else{
                    if (Default.isSelected()){
                        useAngularDefault();
                    }else {
                        setAllRule("0");
                    }
                }
                jfxtable.setRoot(null);
                jfxtable.setRoot(root);
            }
        });

        error.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                warning.setSelected(false);
                off.setSelected(false);
                Default.setSelected(false);
                jfxtable.refresh();
                if (error.isSelected()){
                    setAllRule("error");
                }else{
                    setAllRule("None");
                }
            }
        });

        warning.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                error.setSelected(false);
                off.setSelected(false);
                Default.setSelected(false);
                jfxtable.refresh();
                if (warning.isSelected()) {
                    setAllRule("warn");
                }else {
                    setAllRule("None");
                }
            }
        });

        off.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                error.setSelected(false);
                warning.setSelected(false);
                Default.setSelected(false);

                jfxtable.refresh();
                if (off.isSelected()) {
                    setAllRule("off");
                }else {
                    setAllRule("None");
                }
            }
        });


        jfxtable.setRowFactory(new Callback<TreeTableView, TreeTableRow>() {
            @Override
            public TreeTableRow call(TreeTableView param) {
                JFXTreeTableRow <RuleModel> row=new JFXTreeTableRow();
                row.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                            RuleModel data=row.getItem();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Desktop desktop = Desktop.getDesktop();
                                    try {
                                        desktop.browse(URI.create(data.getLink()));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                        }
                    }
                });
                return row;
            }
        });

        JFXTreeTableColumn <RuleModel,String> Description=new JFXTreeTableColumn<>("Description");
        Description.setMinWidth(500);
        Description.setCellValueFactory((TreeTableColumn.CellDataFeatures<RuleModel, String> param) ->{
            if(Description.validateValue(param)) return param.getValue().getValue().description;
            else return Description.getComputedValue(param);
        });

        ruleMode=new JFXTreeTableColumn<>("Rule Mode");
        ruleMode.setMinWidth(250);

        ruleMode.setCellValueFactory(checkboxfactory);


        ruleMode.setCellFactory(checkboxvaluefactory);

        jfxtable.setRoot(null);
        jfxtable.setShowRoot(false);
        jfxtable.setEditable(false);
        jfxtable.getColumns().setAll(ruleName,Description,ruleMode);
        jfxtable.setRoot(root);
    }

    private void useAngularDefault() {
        ObservableList <RuleModel> observableList= lists.get(pageNumber);
        for (int i=0;i<observableList.size();i++){
                recordedData.put(observableList.get(i).getName(),observableList.get(i).getValue());
        }
    }

    private File openfile(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON file","*.json"));
        return fileChooser.showOpenDialog(primary_stage);
    }
    private void savefile(String content) {
        FileChooser fileChooser=new FileChooser();
        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");
        fileChooser.setTitle("Save Rule File");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showSaveDialog(primary_stage);
        if (file!=null){
            try {
            FileWriter fileWriter= null;
            fileWriter = new FileWriter(file);
            fileWriter.write(content);
            fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void saveData(){
        Iterator <?> keys=recordedData.keys();
        while (keys.hasNext()){
            String key= (String) keys.next();
            if (!recordedData.opt(key).equals("None")){
                finalData.put(key,recordedData.opt(key));
            }else{
                finalData.remove(key);
            }
        }
    }

    public void setAllParentCheckBoxFalse(){
        error.setSelected(false);
        warning.setSelected(false);
        off.setSelected(false);
        Default.setSelected(false);
    }

    public void setAllRule(String value){
        ObservableList <RuleModel> observableList= lists.get(pageNumber);
        for (int i=0;i<observableList.size();i++){
                recordedData.put(observableList.get(i).getName(), value);
        }
    }

    private class CheckBoxesCell extends TreeTableCell {


        JFXCheckBox error=new JFXCheckBox();
        JFXCheckBox warning=new JFXCheckBox();
        JFXCheckBox off=new JFXCheckBox();
        AnchorPane pane=new AnchorPane(error,warning,off);

        CheckBoxesCell(int i){
            if (i==-1){
                i=0;
            }
            String name=lists.get(pageNumber).get(i).getName();

            if (Controller.this.error.isSelected()) {
                error.setSelected(true);
            }else {
                error.setSelected(false);
            }
            if (Controller.this.warning.isSelected()){
                warning.setSelected(true);
            }else {
                warning.setSelected(false);
            }

            if (Controller.this.off.isSelected()){
                off.setSelected(true);
            }else{
                off.setSelected(false);
            }

            if (!(Controller.this.error.isSelected() || Controller.this.warning.isSelected() || Controller.this.off.isSelected())) {
                if (recordedData.optString(name).equals("error")) {
//                    System.out.println("error got called");
                    error.setSelected(true);
                    warning.setSelected(false);
                    off.setSelected(false);
                }
                else if (recordedData.optString(name).equals("warn")) {
//                    System.out.println("warn got called");
                    warning.setSelected(true);
                    error.setSelected(false);
                    off.setSelected(false);
                }
                else if (recordedData.optString(name).equals("off")) {
//                    System.out.println("off got called");
                    off.setSelected(true);
                    error.setSelected(false);
                    warning.setSelected(false);
                }
            }

            error.setLayoutX(14);

            error.setStyle("-fx-alignment: center ;");
            error.setUnCheckedColor(Paint.valueOf("#cf2d2d"));
            error.setCheckedColor(Paint.valueOf("#cf2d2d"));
            warning.setLayoutX(63);

            warning.setStyle("-fx-alignment: center ;");
            warning.setUnCheckedColor(Paint.valueOf("#bdd015"));
            warning.setCheckedColor(Paint.valueOf("#bdd015"));
            off.setLayoutX(109);

            off.setStyle("-fx-alignment: center ;");
            pane.setStyle("-fx-alignment: top-center;");

            error.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    warning.setSelected(false);
                    off.setSelected(false);
                    if (error.isSelected()){
//                        System.out.println("Error is selected");
                        recordedData.put(((RuleModel) ((TreeTableRow) getParent()).getItem()).getName(),"error");
                    }else{
//                        System.out.println("Error is unselected");
                        recordedData.put(((RuleModel) ((TreeTableRow) getParent()).getItem()).getName(),"None");
                    }
                    setAllParentCheckBoxFalse();
                }
            });

            warning.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    error.setSelected(false);
                    off.setSelected(false);
                    if (warning.isSelected()) {
                        recordedData.put(((RuleModel) ((TreeTableRow) getParent()).getItem()).getName(), "warn");
                    }else{
                        recordedData.put(((RuleModel) ((TreeTableRow) getParent()).getItem()).getName(), "None");
                    }
                    setAllParentCheckBoxFalse();
                }
            });

            off.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    error.setSelected(false);
                    warning.setSelected(false);
                    if (off.isSelected()) {
                        recordedData.put(((RuleModel) ((TreeTableRow) getParent()).getItem()).getName(), "off");
                    }else{
                        recordedData.put(((RuleModel) ((TreeTableRow) getParent()).getItem()).getName(), "None");
                    }
                    setAllParentCheckBoxFalse();
                }
            });
        }


        @Override
        protected void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);

            if (!empty){

                if (!(Controller.this.error.isSelected() || Controller.this.warning.isSelected() || Controller.this.off.isSelected())) {
                    if (getParent() != null) {
                        try {
                            String name = ((RuleModel) ((TreeTableRow) getParent()).getItem()).getName();
                            if (recordedData.optString(name).equals("error")) {
                                error.setSelected(true);
                                warning.setSelected(false);
                                off.setSelected(false);
                            } else if (recordedData.optString(name).equals("warn")) {
                                warning.setSelected(true);
                                error.setSelected(false);
                                off.setSelected(false);
                            } else if (recordedData.optString(name).equals("off")) {
                                off.setSelected(true);
                                error.setSelected(false);
                                warning.setSelected(false);
                            } else {
                                off.setSelected(false);
                                error.setSelected(false);
                                warning.setSelected(false);
                            }
                        }catch (NullPointerException e){
//                            e.printStackTrace();
                        }
                    }
                }
                setGraphic(pane);
                }else{
                setGraphic(null);
            }
        }
    }

    private class TextFieldCell extends TreeTableCell {

        JFXTextField field=new JFXTextField();

        TextFieldCell(int i){
            RuleModel model=lists.get(pageNumber).get(i);
//            System.out.println("value is "+value);
            field.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (getParent()!=null) {
                        String name = ((RuleModel) ((TreeTableRow) getParent()).getItem()).getName();
                        recordedData.put(name, newValue);
                    }
                }
            });
            field.setText(recordedData.getString(model.getName()));
            field.setFocusColor(Color.SLATEGREY);
            field.setStyle("-fx-font-size: 12;-fx-font-family: Droid Sans;-fx-alignment: center;-fx-text-alignment: center;");
        }

        @Override
        protected void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);
            if (!empty){
                setGraphic(field);
                if (getParent() != null) {
                    try {
                        String name = ((RuleModel) ((TreeTableRow) getParent()).getItem()).getName();
                        field.setText(recordedData.getString(name));
                    }catch (Exception e){

                    }
                }

            }else {
                setGraphic(null);
            }
        }
    }

}

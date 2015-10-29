package GUI;

import Server.ClientWorker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import model.doctor;

import java.io.*;
import java.net.Socket;
/**
 * Created by User on 27.10.2015.
 */
public class DoctorOverviewController {

    private TableView<doctor> doctorTable;
    private StackPane stackpane;
    private TableColumn IDCol;
    private TableColumn nameCol;
    private TableColumn surnameCol;
    private TableColumn occCol;
    private TableColumn ageCol;

    //link to MainApp
    private MainApp mainApp = null;
    private Socket server = null;
    private BufferedReader in = null;
    private PrintWriter out = null;

    public DoctorOverviewController() {
        initialize();
    }

    private void initialize() {
        this.stackpane = new StackPane();
        this.IDCol = new TableColumn();
        this.IDCol.setText("ID");
        this.IDCol.setCellValueFactory(new PropertyValueFactory("ID"));
        this.nameCol = new TableColumn();
        this.nameCol.setText("Name");
        this.nameCol.setCellValueFactory(new PropertyValueFactory("name"));
        this.occCol = new TableColumn();
        this.occCol.setText("Occupation");
        this.occCol.setCellValueFactory(new PropertyValueFactory("occupation"));
        this.surnameCol = new TableColumn();
        this.surnameCol.setText("Surname");
        this.surnameCol.setCellValueFactory(new PropertyValueFactory("surname"));
        this.ageCol = new TableColumn();
        this.ageCol.setText("Age");
        this.ageCol.setCellValueFactory(new PropertyValueFactory("age"));
        //Our Table with data
        this.doctorTable = new TableView();
        this.doctorTable.getColumns().addAll(IDCol, nameCol, surnameCol, occCol, ageCol);
        //TableColumns change their size when you change size of the window
        doctorTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        //delete selected doctor
        Button delete = new Button("Delete");
        delete.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                int selectedIndex = doctorTable.getSelectionModel().getSelectedIndex();
                //if doctor was selected
                if (selectedIndex != -1) {
                    try {
                        //id of selected doctor
                        int selectedID = mainApp.getDoctors().get(selectedIndex).getID();
                        //delete doctor from server
                        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(server
                                .getOutputStream())), true);
                        out.println("delElement" + "_" + String.valueOf(selectedID));
                    }
                    catch (IOException e){
                        System.out.println("Connection error!");
                        mainApp.App_exit();
                    }
                }
            }
        });

        //edit selected doctor
        Button edit = new Button("Edit");
        edit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                int selectedIndex = doctorTable.getSelectionModel().getSelectedIndex();
                //if doctor was sected
                if (selectedIndex != -1) {
                    //selected doctor and his id
                    doctor tmpDoc = mainApp.getDoctors().get(selectedIndex);
                    int selectedID = tmpDoc.getID();
                    boolean okClicked = mainApp.showPersonEditDialog(tmpDoc);
                    //if botton "Ok" was clicked
                    if (okClicked) {
                        try{
                            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(server
                                    .getOutputStream())), true);
                            out.println("edit" + "_" + ClientWorker.decodeToString(tmpDoc));
                        }catch(IOException IO){
                            System.out.println("Connection error!");
                            mainApp.App_exit();
                        }
                    }
                }
            }
        });

        //add new doctor
        Button add = new Button("New");
        add.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //object for adding
                doctor tmpDoc = new doctor();
                boolean okClicked = mainApp.showPersonEditDialog(tmpDoc);
                //if botton "Ok" was clicked
                if (okClicked) {
                    try {
                        //add doctor on server
                        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(server
                                .getOutputStream())), true);
                        out.println("add" + "_" + ClientWorker.decodeToString(tmpDoc));
                    } catch (IOException e) {
                        System.out.println("Connection error!");
                        mainApp.App_exit();
                    }
                }
            }
        });

        HBox hbox = new HBox();
        hbox.setSpacing(5);
        hbox.getChildren().addAll(add, edit, delete);
        VBox vbox = new VBox();
        vbox.getChildren().addAll(doctorTable, hbox);
        vbox.setSpacing(10);
        stackpane.getChildren().addAll(vbox);
    }


    //get link to mainApp and some its components
    public void setMainAppComponents(MainApp mainApp, Socket server) {
        this.server = server;
        this.mainApp = mainApp;
        //fill our table
        doctorTable.setItems(mainApp.getDoctors());
        //show window with data
        mainApp.getPrimaryStage().setScene(new Scene(stackpane));
        mainApp.getPrimaryStage().show();
    }

}

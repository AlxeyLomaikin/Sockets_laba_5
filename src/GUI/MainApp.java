package GUI;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.doctor;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by User on 27.10.2015.
 */
public class MainApp extends Application {
    private BufferedReader in = null;
    private PrintWriter out = null;
    private Socket server = null;
    private Stage primaryStage;
    private ObservableList<doctor> doctors = FXCollections.observableArrayList();

    //check data's updates
    private SimpleBooleanProperty WasDataChanged = new SimpleBooleanProperty();
    public SimpleBooleanProperty getWasDataChanged() {
        return this.WasDataChanged;
    }

    //Add/Edit/Delete Errors on server
    private SimpleStringProperty Error = new SimpleStringProperty(null);
    private ChangeListener<String> listener =  listener = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            if (!(newValue).equals(oldValue)) {
                if (!newValue.equals("-"))
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<String> error = new ArrayList<String>();
                        error.add(Error.getValue());
                        new ErrorMessageStage(error, primaryStage);
                    }
                });
            }
        }
    };
    public SimpleStringProperty getError(){
        return this.Error;
    }

    public ObservableList<doctor> getDoctors() {
        return this.doctors;
    }
    public Stage getPrimaryStage() {
        return this.primaryStage;
    }

    void App_exit (){
        if (server!=null){
            try{
                in.close();
                out.close();
                server.close();
            }catch (IOException e){}
        }
        System.exit(0);
    }

    private boolean ConnectToServer() {
        boolean success = false;
        try {
            server = new Socket(InetAddress.getByName(null), 1099);
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(server
                    .getOutputStream())), true);
            in = new BufferedReader(new InputStreamReader(server.getInputStream()));
            out.println("getAll");
            String answer = in.readLine();
            //server is down
            if (answer!=null) {
                //Correct packets:
                //getAllAnswer_0
                // getAllAnswer_sizeOfData_id_name_surname_occupation_age_id...
                String[] spl = answer.split("_");
                if (spl[0].equals("getAllAnswer") && spl.length > 1) {
                    int sizeOfData = Integer.parseInt(spl[1]);
                    int idxOfLastStr = 1 + sizeOfData * 5;
                    //checking the correctness of packet
                    if (spl.length == (idxOfLastStr + 1)) {
                        for (int i = 0; i < sizeOfData; i++) {
                            int ID = Integer.parseInt(spl[2 + i * 5]);
                            String Name = spl[3 + i * 5];
                            String Surname = spl[4 + i * 5];
                            String occupation = spl[5 + i * 5];
                            int age = Integer.parseInt(spl[6 + i * 5]);
                            doctor doc = new doctor(Name, Surname, occupation, age);
                            doc.setID(ID);
                            this.doctors.add(doc);
                        }
                        success = true;
                    }
                    else success = false;
                }
                else success = false;
            }
            else success = false;
        }catch (IOException | NumberFormatException e){
            success = false;
        }
        return success;
    }

    boolean isIDexist (int ID){
        for (doctor doc:doctors) {
            if (doc.getID()==ID)
                return true;
        }
        return false;
    }
    int GetIndexByID (int ID)
    {
        int Index = -1;
        for (int m = 0; m<this.doctors.size(); m++)
            if(this.doctors.get(m).getID() == ID) {
                Index = m;
                break;
            }
        return Index;
    }


    @Override
    public void start(Stage primaryStage) {
        if (ConnectToServer()) {
            //start thread to communicate with server
            Error.addListener(listener);
            new ClientThread(server, this).start();
            this.primaryStage = primaryStage;
            this.primaryStage.setTitle("Doctors List");
            //client is disconnecting
            this.primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    try {
                        in.close();
                        out.close();
                        server.close();
                    } catch (IOException e) {
                    }
                }
            });
            showDoctortable();
        }
        else {
            System.out.println("Connection error!");
            App_exit();
        }
    }

    public void showDoctortable() {
        /*give controller mainApp link
         and socket to communicate with server*/
        DoctorOverviewController controller = new DoctorOverviewController();
        controller.setMainAppComponents(this, this.server);
    }

    //create window to enter data
    //return true, if data was entered correctly and
    // button "OK" was clicked
    public boolean showPersonEditDialog(doctor tmpDoctor) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Edit/Add Doctor");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(primaryStage);
        //set object to add/edit and created window in controller
        PersonEditDialogController controller = new PersonEditDialogController(dialogStage, tmpDoctor, this);
        return controller.isOkClicked();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

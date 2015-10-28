package GUI;

import model.EventBase;
import model.doctor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by User on 28.10.2015.
 */
public class ClientThread extends Thread{
    final Socket fromServer;
    final MainApp mainApp;
    private BufferedReader in;

    public ClientThread (Socket fromServer, MainApp mainApp){
        this.fromServer = fromServer;
        this.mainApp = mainApp;
    }

    void showErrorMessage (String message){
        ArrayList<String> error = new ArrayList<String>();
        error.add(message);
        new ErrorMessageStage(error, mainApp.getPrimaryStage());
    }

    //mask: "UpdateDelData_id"
    private void handleUPD_DELPacket (String[] spl){
        try{
            if (spl.length==2){
                int id = Integer.parseInt(spl[1]);
                mainApp.getDoctors().remove(mainApp.GetIndexByID(id));
                mainApp.getWasDataChanged().set(true);
            }
        }catch(NumberFormatException | ArrayIndexOutOfBoundsException e){}
    }

    //mask:  "UpdateAddData_id_name_surname_occupation_age"
    private void handleUPD_ADDPacket (String[] spl){
        try{
            if (spl.length==6){
                int id = Integer.parseInt(spl[1]);
                int age = Integer.parseInt(spl[5]);
                doctor doc = new doctor(spl[2], spl[3], spl[4], age);
                doc.setID(id);
                mainApp.getDoctors().add(doc);
                mainApp.getWasDataChanged().set(true);
            }
        }catch(NumberFormatException n){}
    }

    //mask:  "UpdateEditData_id_name_surname_occupation_age"
    private void handleUPD_EditPacket (String[] spl){
        try{
            if (spl.length==6){
                int id = Integer.parseInt(spl[1]);
                int age = Integer.parseInt(spl[5]);
                doctor doc = new doctor(spl[2], spl[3], spl[4], age);
                doc.setID(id);
                mainApp.getDoctors().set(mainApp.GetIndexByID(id),doc);
                mainApp.getWasDataChanged().set(true);
            }
        }catch(NumberFormatException n){}
    }

    private void handleIncomingMessage (String message) {
        if (message != null) {
            String[] spl = message.split("_");
            switch (spl[0]) {
                case EventBase.UPD_ADD:
                    handleUPD_ADDPacket(spl);
                    break;
                case EventBase.UPD_DELETE :
                    handleUPD_DELPacket(spl);
                    break;
                case EventBase.UPD_EDIT:
                    handleUPD_EditPacket(spl);
                    break;
                case EventBase.ADD_FAIL:
                    showErrorMessage("Add Failed");
                    break;
                case EventBase.DEL_FAIL:
                    showErrorMessage("Delete Failed");
                    break;
                case EventBase.EDIT_FAIL:
                    showErrorMessage("Edit Failed");
                    break;
                default:
                    showErrorMessage("Incorrect Request of Packet from Client");
                    break;
            }
        }
    }

    @Override
    public void run() {
        try{
            while (true) {
                in = new BufferedReader(new InputStreamReader(fromServer.getInputStream()));
                String message = in.readLine();
                //server is down
                if (message == null) break;
                else
                    handleIncomingMessage(message);
            }
        }catch (IOException e){}
        finally{
            //server is down
            System.out.println("Connection error!");
            mainApp.App_exit();
        }
    }
}

package GUI;

import model.EventBase;
import model.doctor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by User on 28.10.2015.
 */
public class ClientThread extends Thread{
    final Socket fromServer;
    final MainApp mainApp;
    private ObjectInputStream in;

    public ClientThread (Socket fromServer, MainApp mainApp){
        this.fromServer = fromServer;
        this.mainApp = mainApp;
    }

    //mask: "UpdateDelData_id"
    private void handleUPD_DELPacket (){
        try{
            int id = (int)in.readObject();
            mainApp.getDoctors().remove(mainApp.GetIndexByID(id));
            mainApp.getWasDataChanged().set(true);
        } catch(IOException | ClassNotFoundException | ArrayIndexOutOfBoundsException e){}
    }

    //mask:  "UpdateAddData_id_name_surname_occupation_age"
    private void handleUPD_ADDPacket (){
        try{
            doctor doc = (doctor)in.readObject();
            mainApp.getDoctors().add(doc);
            mainApp.getWasDataChanged().set(true);
        }catch(IOException | ClassNotFoundException e){}
    }

    //mask:  "UpdateEditData_id_name_surname_occupation_age"
    private void handleUPD_EditPacket (){
        try{
            doctor doc = (doctor)in.readObject();
            mainApp.getDoctors().set(mainApp.GetIndexByID(doc.getID()),doc);
            mainApp.getWasDataChanged().set(true);
        }catch(IOException | ClassNotFoundException e){}
    }

    private void handleIncomingMessage (String message) {
        if (message != null) {
            mainApp.getError().set("-");
            switch (message) {
                case EventBase.UPD_ADD:
                    handleUPD_ADDPacket();
                    break;
                case EventBase.UPD_DELETE :
                    handleUPD_DELPacket();
                    break;
                case EventBase.UPD_EDIT:
                    handleUPD_EditPacket();
                    break;
                case EventBase.ADD_FAIL:
                    mainApp.getError().set("Add Failed");
                    break;
                case EventBase.DEL_FAIL:
                    mainApp.getError().set("Delete Failed");
                    break;
                case EventBase.EDIT_FAIL:
                    mainApp.getError().set("Edit Failed");
                    break;
                default:
                    mainApp.getError().set("Incorrect Request of Packet from Client");
                    break;
            }
        }
    }

    @Override
    public void run() {
        try{
            while (true) {
                in = new ObjectInputStream((fromServer.getInputStream()));
                String message = (String)in.readObject();
                //server is down
                if (message == null) break;
                else
                    handleIncomingMessage(message);
            }
        }catch (IOException | ClassNotFoundException e){}
        finally{
            //server is down
            System.out.println("Connection error!");
            mainApp.App_exit();
        }
    }
}

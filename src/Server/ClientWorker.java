package Server;

import model.EventBase;
import model.doctor;

import java.io.*;
import java.net.Socket;

public class ClientWorker extends Thread{
    private final ServerImpl servImpl;
    private final Socket client;
    private BufferedReader in;
    private PrintWriter out;

    public ClientWorker (Socket client, ServerImpl servImpl) {
        this.servImpl = servImpl;
        this.client = client;
    }

    private void SendMesToClient (Socket client, String message)throws IOException{
        //Auto clean buffer in out
        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client
                .getOutputStream())), true);
        out.println(message);
    }

    public static String decodeToString(doctor doc) {
        try{
            String decoded;
            decoded = String.valueOf(doc.getID()).concat("_");
            decoded = decoded.concat(doc.getName()).concat("_");
            decoded = decoded.concat(doc.getSurname()).concat("_");
            decoded = decoded.concat(doc.getOccupation()).concat("_");
            decoded = decoded.concat(String.valueOf(doc.getAge()));
            return decoded;
        }catch (NullPointerException np){
            return null;
        }
    }

    //mask : "add_id_name_surname_occupation_age"
    //example of correct packet: "add_0_Alex_Fisher_staff_31"
    private void handleAddPacket (String[] mesSplit) throws IOException{
        if (mesSplit.length < 6)
            SendMesToClient(this.client, "IncorrectAddPacket");
        else
            try {
                int age = Integer.parseInt(mesSplit[5]);
                doctor doc = new doctor(mesSplit[2], mesSplit[3], mesSplit[4], age);
                synchronized (servImpl) {
                    int AddedID = servImpl.add(doc);
                    if (AddedID != -1)
                        for (Socket client:servImpl.getClients())
                            SendMesToClient(client, "UpdateAddData_" + decodeToString(doc));
                    else SendMesToClient(this.client, "addFail");
                }
            } catch (NumberFormatException e) {
                SendMesToClient(this.client, "IncorrectAddPacket");
            }
    }

    //mask : "delElement_id"
    //example of correct packet: "delElement_12"
    private void handleDelPacket (String[] mesSplit)throws IOException{
        if (mesSplit.length == 2) {
            try {
                int ID = Integer.parseInt(mesSplit[1]);
                synchronized (servImpl) {
                    if (servImpl.delElement(ID))
                        for (Socket client:servImpl.getClients())
                            SendMesToClient(client, "UpdateDelData_" + String.valueOf(ID));
                    else SendMesToClient(this.client, "DelFail");
                }
            } catch (NumberFormatException e) {
                SendMesToClient(this.client, "IncorrectDelPacket");
            }
        } else
            SendMesToClient(this.client, "IncorrectDelPacket");
    }

    //mask: "getAll"
    //example packet: "getAll"
    private void handleGetAllPacket (String[] mesSplit)throws IOException{
        if (mesSplit.length == 1) {
            //answer packet: getAllAnswer_11_Alex_Fisher_oculist_33_12_Alex_Drug_oculist_35...
            synchronized (servImpl) {
                String getAllAnswer = "getAllAnswer_".concat(String.valueOf(servImpl.getData().size())).concat("_");
                for (doctor doc : servImpl.getData()) {
                    getAllAnswer = getAllAnswer.concat(decodeToString(doc)).concat("_");
                }
                SendMesToClient(this.client, getAllAnswer);
            }
        } else
            SendMesToClient(this.client, "IncorrectRequest");
    }

    //mask: edit_id_name_surname_occupation_age
    //example of correct packet: edit_21_Alex_Fisher_staff_31
    private void handleEditPacket (String[] mesSplit) throws IOException{
        if (mesSplit.length == 6) {
            try {
                int ID = Integer.parseInt(mesSplit[1]);
                int age = Integer.parseInt(mesSplit[5]);
                doctor doc = new doctor(mesSplit[2], mesSplit[3], mesSplit[4], age);
                doc.setID(ID);
                synchronized (servImpl) {
                    if (servImpl.edit(ID, doc))
                        //update data on all clients
                        for(Socket client:servImpl.getClients())
                            SendMesToClient(client, "UpdateEditData_" + decodeToString(doc));
                    else SendMesToClient(this.client, "EditFail");
                }
            } catch (NumberFormatException e) {
                SendMesToClient(this.client, "IncorrectEditPacket");
            }
        } else
            SendMesToClient(this.client, "IncorrectEditPacket");
    }

    public void handleIncomingMessage (String message) throws IOException{
        if (message != null) {
            String[] mesSplit = message.split("_");
            switch (mesSplit[0]) {
                case EventBase.ADD:
                    this.handleAddPacket(mesSplit);
                    break;
                case EventBase.DELETE:
                    handleDelPacket(mesSplit);
                    break;
                case EventBase.EDIT:
                    this.handleEditPacket(mesSplit);
                    break;
                case EventBase.GET_ALL:
                    this.handleGetAllPacket(mesSplit);
                    break;
                default:
                    SendMesToClient(this.client, "IncorrectRequest");
                    break;
            }
        }
    }

    public void run(){
        try {
            while (true) {
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String message = in.readLine();
                //client is down
                if (message == null) break;
                else
                    this.handleIncomingMessage(message);
            }
        }
        catch(IOException IO){
        }
        finally{
            try{
                synchronized(servImpl.getClients()){
                    servImpl.getClients().remove(client);
                }
                String clientAdr = client.getInetAddress()+":" +client.getPort();
                System.out.println("Client" + clientAdr +" was disconnected");
                in.close();
                out.close();
                client.close();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }
}
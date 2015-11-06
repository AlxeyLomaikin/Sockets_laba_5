package Server;

import model.EventBase;
import model.doctor;

import java.io.*;
import java.net.Socket;

public class ClientWorker extends Thread{
    private ServerImpl servImpl;
    private Socket client;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public ClientWorker (Socket client, ServerImpl servImpl) {
        this.servImpl = servImpl;
        this.client = client;
    }

    private void SendMesToClient (Socket client, String message)throws IOException{
        out = new ObjectOutputStream(client
                .getOutputStream());
        out.writeObject(message);
    }

    private void handleAddPacket () throws IOException{
        try {
            doctor doc = (doctor)in.readObject();
            synchronized (servImpl) {
                int AddedID = servImpl.add(doc);
                if (AddedID != -1)
                    for (Socket client:servImpl.getClients()) {
                        SendMesToClient(client, "UpdateAddData" );
                        out.writeObject(doc);
                    }
                else SendMesToClient(this.client, "addFail");
            }
        } catch (ClassNotFoundException e) {
            SendMesToClient(this.client, "IncorrectAddPacket");
        }
    }

    private void handleDelPacket ()throws IOException{
            try {
                int ID = (int)in.readObject();
                synchronized (servImpl) {
                    if (servImpl.delElement(ID))
                        for (Socket client:servImpl.getClients()) {
                            SendMesToClient(client, "UpdateDelData");
                            out.writeObject(ID);
                        }
                    else SendMesToClient(this.client, "DelFail");
                }
            } catch (ClassNotFoundException e) {
                SendMesToClient(this.client, "IncorrectDelPacket");
            }
    }

    private void handleGetAllPacket ()throws IOException{
        synchronized (servImpl) {
            SendMesToClient(this.client, "getAllAnswer");
            out.writeObject(servImpl.getData());
            }
        }

    private void handleEditPacket () throws IOException{
            try {
                doctor doc = (doctor)in.readObject();
                synchronized (servImpl) {
                    if (servImpl.edit(doc.getID(), doc))
                        //update data on all clients
                        for(Socket client:servImpl.getClients()) {
                            SendMesToClient(client, "UpdateEditData");
                            out.writeObject(doc);
                        }
                    else SendMesToClient(this.client, "EditFail");
                }
            } catch (ClassNotFoundException e) {
                SendMesToClient(this.client, "IncorrectEditPacket");
            }
    }

    public void handleIncomingMessage (String message) throws IOException{
        if (message != null) {
            switch (message) {
                case EventBase.ADD:
                    this.handleAddPacket();
                    break;
                case EventBase.DELETE:
                    handleDelPacket();
                    break;
                case EventBase.EDIT:
                    this.handleEditPacket();
                    break;
                case EventBase.GET_ALL:
                    this.handleGetAllPacket();
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
                in = new ObjectInputStream(client.getInputStream());
                String message = (String)in.readObject();
                //client is down
                if (message == null) break;
                else
                    this.handleIncomingMessage(message);
            }
        }
        catch(IOException | ClassNotFoundException e){
        }
        finally{
            try{
                synchronized(servImpl.getClients()) {
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
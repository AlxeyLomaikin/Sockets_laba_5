package Server;

import java.net.ServerSocket;
import java.net.Socket;


/**
 * Created by User on 26.10.2015.
 */
public class Server {
    private static final int port = 1099;

    public static void main(String[] args) throws Exception{
        //class of server where data and
        //list of clients are stored
        final ServerImpl serv = new ServerImpl();

        Socket client = null;

        ServerSocket ss = new ServerSocket(port);
        System.out.println ("Server is running");
        try {
            //start a thread to communicate with client
            while (true){
                client = ss.accept();
                String clientAdr = client.getInetAddress() +":"+client.getPort();
                System.out.println("New client: " + clientAdr);
                new ClientWorker(client, serv).start();
                synchronized (serv.getClients()){
                    serv.getClients().add(client);
                }
            }
        }finally{
            ss.close();
            for (Socket socket:serv.getClients())
                socket.close();
        }
    }
}
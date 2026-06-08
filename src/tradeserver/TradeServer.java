/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tradeserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


public class TradeServer {
    
    private ServerSocket ssocket;
    private int port;
    private ArrayList<ConnectedTradeClient> clients;
    
    public ServerSocket getSsocket() {
        return ssocket;
    }

    public void setSsocket(ServerSocket ssocket) {
        this.ssocket = ssocket;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
    
    public void acceptClients() {
        Socket client = null;
        Thread thr;
        while (true) {
            try {
                System.out.println("Waiting for new clients..");
                client = this.ssocket.accept();
            } catch (IOException ex) {
                Logger.getLogger(TradeServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (client != null) {
                
                ConnectedTradeClient clnt = new ConnectedTradeClient(client, clients);
                
                clients.add(clnt);
               
                thr = new Thread(clnt);
                
                thr.start();
            } else {
                break;
            }
        }
    }
    
    public TradeServer(int port) {
        this.clients = new ArrayList<>();
        try {
            this.port = port;
            this.ssocket = new ServerSocket(port);
        } catch (IOException ex) {
            Logger.getLogger(TradeServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        TradeServer server = new TradeServer(6001);

        System.out.println("Server pokrenut, slusam na portu 6001");

        
        server.acceptClients();

    }

   
    
}

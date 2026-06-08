/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tradeserver;


import java.io.BufferedReader;
import java.io.IOException;
//import java.io.InputStream;
import java.io.InputStreamReader;
//import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
//import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Random;

public class ConnectedTradeClient implements Runnable {

    //atributi koji se koriste za komunikaciju sa klijentom
    private Socket socket;
    private String userName;
    private BufferedReader br;
    private PrintWriter pw;
    private ArrayList<ConnectedTradeClient> allClients;
    
    // slicice
    private boolean[] duplikati = new boolean[99];
    private boolean[] trazeni = new boolean[99];
    
      //getters and setters
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public ConnectedTradeClient(Socket socket, ArrayList<ConnectedTradeClient> allClients) {
        this.socket = socket;
        this.allClients = allClients;

        try {
            this.br = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), "UTF-8"));
            this.pw = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream()), true);
            this.userName = "";
        } catch (IOException ex) {
            Logger.getLogger(ConnectedTradeClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

 
      @Override
    public void run() {
        try {
            // registracija
            String registracija = br.readLine();
            if (registracija != null && registracija.startsWith("REG|")) {
                String[] parts = registracija.split("\\|");
                this.userName = parts[1];
                
                // proba
                String testDuplikati = "4,5,7,89,5,3";
                String testTrazeni = "22,4,60,23,66";
                
                String response = "REG_OK|" + testDuplikati + "|" + testTrazeni;
                pw.println(response);
                
                System.out.println("Registrovan korisnik: " + userName);
                System.out.println("  Duplikati: " + testDuplikati);
                System.out.println("  Trazeni: " + testTrazeni);
            } else {
                System.out.println("Nije primljena validna registracija");
                return;
            }
            
            // komande
            String komanda;
            while ((komanda = br.readLine()) != null) {
                if (komanda.startsWith("GET_EXCHANGES")) {
                    // za sad vraca da nema razmjene
                    pw.println("EXCHANGES|NEMA");
                    System.out.println("Korisnik " + userName + " je trazio razmjene - za sad nema");
                }
                else {
                    System.out.println("Nepoznata komanda od " + userName + ": " + komanda);
                }
            }
            
        } catch (IOException e) {
            System.out.println("Korisnik " + userName + " se odjavio");
        } finally {
            ukloniKorisnika();
        }
    }

    private void ukloniKorisnika() { //google java remove this user method
        allClients.remove(this);
        System.out.println("Korisnik " + userName + " je uklonjen");
        try {
            socket.close();
        } catch (IOException ex) {}
    }
}
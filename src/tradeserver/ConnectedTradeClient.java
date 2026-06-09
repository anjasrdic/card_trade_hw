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
    private boolean[] duplikati = new boolean[100];
    private boolean[] trazeni = new boolean[100];
    
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
                obradiRegistraciju(registracija);
            } else {
                System.out.println("Nije primljena validna registracija");
                return;
            }
            
            // cekaj komandu 
            String komanda;
            while ((komanda = br.readLine()) != null) {
                
                if (komanda.startsWith("GET_EXCHANGES")) {
                   String razmene = nadjiMoguceRazmene();
                    pw.println(razmene);
                    System.out.println("Poslate razmene za " + userName);
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
    
    private void obradiRegistraciju(String line) {
        // REG|userName od klijenta
        String[] parts = line.split("\\|");
        this.userName = parts[1];
        
        
        //doraditi da budu i brojevi random ali wanted<dupes
        generisiNasumicne(20, 10);  
        
        //saljemo potvru klijetu
        String response = "REG_OK|" + duplikatiToString() + "|" + trazeniToString();
        pw.println(response);
        
        System.out.println("Ime: " + userName);
        System.out.println("  Duplikati: " + duplikatiToString());
        System.out.println("  Trazeni: " + trazeniToString());
    }
    
    private void generisiNasumicne(int brojDuplikata, int brojTrazenih) {
        Random rand = new Random();
        duplikati = new boolean[100]; 
        trazeni = new boolean[100];
        
        int dodatoD = 0;
        while (dodatoD < brojDuplikata) {
            int broj = rand.nextInt(99) + 1;  
            if (!duplikati[broj]) {
                duplikati[broj] = true;
                dodatoD++;
            }
        }
        
        int dodatoT = 0;
        while (dodatoT < brojTrazenih) {
            int broj = rand.nextInt(99) + 1;
            if (!duplikati[broj] && !trazeni[broj]) {
                trazeni[broj] = true;
                dodatoT++;
            }
        }
    }
    
    private String duplikatiToString() {
    String result = "";
    for (int i = 1; i <= 99; i++) {
        if (duplikati[i]) {
            if (!result.isEmpty()) result += ",";
            result += i; 
        }
    }
    return result;
}
    
    private String trazeniToString() {
    String result = "";
    for (int i = 1; i <= 99; i++) {
        if (trazeni[i]) {
            if (!result.isEmpty()) result += ",";
            result += i;  
        }
    }
    return result;
}
    
     private String nadjiMoguceRazmene() {
    String result = "EXCHANGES|";
    boolean trade = false;
    

    for (int i = 0; i < allClients.size(); i++) {
        
        ConnectedTradeClient trenutni = allClients.get(i);
        
        // ne gleda sebe
        if (trenutni.userName.equals(this.userName)) {
            continue;
        }
        
        String jaDajem = "";
        String jaDobijam = "";
        
        for (int s = 1; s <= 99; s++) {
            
            // transmit 
            if (this.duplikati[s] && trenutni.trazeni[s]) {
                if (jaDajem.equals("")) {
                    jaDajem = "" + s;
                } else {
                    jaDajem = jaDajem + "," + s;
                }
            }
            
            // recieve
            if (this.trazeni[s] && trenutni.duplikati[s]) {
                if (jaDobijam.equals("")) {
                    jaDobijam = "" + s;
                } else {
                    jaDobijam = jaDobijam + "," + s;
                }
            }
        }
        
        // tr i rec
        if (!jaDajem.equals("") && !jaDobijam.equals("")) {
            if (trade) {
                result = result + ";";
            }
            result = result + trenutni.userName + "|" + jaDajem + "|" + jaDobijam;
            trade = true;
        }
    }
    
    if (!trade) {
        result = result + "NEMA";
    }
    
    return result;
}

    private void ukloniKorisnika() { //google java remove this user method
        allClients.remove(this);
        System.out.println("Korisnik " + userName + " je uklonjen");
        try {
            socket.close();
        } catch (IOException ex) {}
    }
}
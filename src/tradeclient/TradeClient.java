package tradeclient;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class TradeClient extends JFrame {
    
    private JTextField txtUsername;
    private JButton btnLogin;
    private JButton btnExchanges;
    private JTextArea txtResult;
    private HashMap<String, Checkbox> duplicates; 
    private HashMap<String, Checkbox> wanted;
    
     // konekcija
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    
    public TradeClient() {
        setTitle("Menjač sličica");
        setSize(1000, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        duplicates = new HashMap<>();
        wanted = new HashMap<>();
        
        setLayout(new BorderLayout());
        
        // prijava up
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(new JLabel("Korisničko ime:"));
        txtUsername = new JTextField(14);
        topPanel.add(txtUsername);
        
        btnLogin = new JButton("Prijavi se");
        topPanel.add(btnLogin);
        add(topPanel, BorderLayout.NORTH);
        
        // cb - split levo i desno
        JPanel panelDuplicates = new JPanel(new GridLayout(10, 10, 5, 5));
        panelDuplicates.setBorder(BorderFactory.createTitledBorder("SLIČICE KOJE IMAM"));
        
        JPanel panelWanted = new JPanel(new GridLayout(10, 10, 5, 5));
        panelWanted.setBorder(BorderFactory.createTitledBorder("SLIČICE KOJE MI TREBAJU"));
        
        // dupes i wanted cbs
        int num = 1;
        while (num < 100) {
            Checkbox cb = new Checkbox(num + "");
            duplicates.put("Btn" + num, cb);
            panelDuplicates.add(cb);
            num++;
        }
        
        num = 1;
        while (num < 100) {
            Checkbox cb = new Checkbox(num + "");
            wanted.put("Btn" + num, cb);
            panelWanted.add(cb);
            num++;
        }
        
        //dodavanje panela sa cb u scroll
        JScrollPane scrollDup = new JScrollPane(panelDuplicates);
        JScrollPane scrollWant = new JScrollPane(panelWanted);
        scrollDup.setPreferredSize(new Dimension(480, 500));
        scrollWant.setPreferredSize(new Dimension(480, 500));
        
        add(scrollDup, BorderLayout.WEST);
        add(scrollWant, BorderLayout.EAST);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        btnExchanges = new JButton("Moguće razmene");
        btnExchanges.setEnabled(false);  // onemoguceno dok se ne prijavi
        bottomPanel.add(btnExchanges, BorderLayout.NORTH);
        
        txtResult = new JTextArea(8, 50);
        txtResult.setEditable(false);
        bottomPanel.add(new JScrollPane(txtResult), BorderLayout.CENTER);
        
        add(bottomPanel, BorderLayout.SOUTH);

        btnLogin.addActionListener(e -> logIn());
        btnExchanges.addActionListener(e -> traziRazmene());
        
   
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                zatvoriSocket();
            }
        });
    }
    
     // prijava na server
    private void logIn() {
        username = txtUsername.getText().trim();
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Unesite korisničko ime");
            return;
        }
        
        try {
            // povezivanje na server
            socket = new Socket("localhost", 6001);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // slanje registracije
            out.println("REG|" + username);
            
            // cekanje odgovora
            String response = in.readLine();
            if (response != null && response.startsWith("REG_OK|")) {
                String[] parts = response.split("\\|");
                String duplikatiStr = parts[1];
                String trazeniStr = parts[2];
                
                // resetuj sve checkboxove
                for (int i = 1; i <= 99; i++) {
                    duplicates.get("Btn" + i).setState(false);
                    wanted.get("Btn" + i).setState(false);
                }
                
                // oznaci duplikate
                if (!duplikatiStr.isEmpty()) {
                    String[] brojevi = duplikatiStr.split(",");
                    for (String s : brojevi) {
                        int broj = Integer.parseInt(s);
                        duplicates.get("Btn" + broj).setState(true);
                    }
                }
                
                // oznaci trazene
                if (!trazeniStr.isEmpty()) {
                    String[] brojevi = trazeniStr.split(",");
                    for (String s : brojevi) {
                        int broj = Integer.parseInt(s);
                        wanted.get("Btn" + broj).setState(true);
                    }
                }
                
                txtResult.append("Prijavljeni ste kao: " + username + "\n");
                txtResult.append("Server vam je dodelio sličice!\n");
                txtResult.append("Duplikata: " + duplikatiStr + "\n");
                txtResult.append("Traženih: " + trazeniStr + "\n\n");
                
                btnLogin.setEnabled(false);
                txtUsername.setEnabled(false);
                btnExchanges.setEnabled(true);
            }
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Greška pri povezivanju: " + e.getMessage());
        }
    }
    
    // trazenje mogucih razmena
    private void traziRazmene() {
        
        try {
            out.println("GET_EXCHANGES|" + username);
            String response = in.readLine();
            
            if (response != null && response.startsWith("EXCHANGES|")) {
                String result = response.substring("EXCHANGES|".length());
                
                if (result.equals("NEMA")) {
                    txtResult.append("Nema mogućih razmena.\n");
                } else {
                    txtResult.append("\nMoguće ramene.\n");
                    String[] exchanges = result.split(";");
                    
                    for (String exchange : exchanges) {
                        String[] parts = exchange.split("\\|");
                        String otherUser = parts[0];
                        String iGive = parts[1];
                        String iGet = parts[2];
                        
                        txtResult.append("\n Sa korisnikom: " + otherUser + "\n");
                        txtResult.append("   TI DAJEŠ: " + iGive + "\n");
                        txtResult.append("   TI DOBIJAŠ: " + iGet + "\n");
                    }
                }
            }
            
        } catch (IOException e) {
            txtResult.append("Greška: " + e.getMessage() + "\n");
        }
    }
    
    private void zatvoriSocket() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TradeClient().setVisible(true));
    }
}
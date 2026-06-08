package tradeclient;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class TradeClient extends JFrame {
    
    private JTextField txtUsername;
    private JButton btnLogin;
    private JButton btnExchanges;
    private JTextArea txtResult;
    private HashMap<String, Checkbox> duplicates; 
    private HashMap<String, Checkbox> wanted;      
    
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
        
        // dupes i wanted
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
        
        JScrollPane scrollDup = new JScrollPane(panelDuplicates);
        JScrollPane scrollWant = new JScrollPane(panelWanted);
        scrollDup.setPreferredSize(new Dimension(480, 500));
        scrollWant.setPreferredSize(new Dimension(480, 500));
        
        add(scrollDup, BorderLayout.WEST);
        add(scrollWant, BorderLayout.EAST);
        
        // 
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        btnExchanges = new JButton("Moguće razmene");
        bottomPanel.add(btnExchanges, BorderLayout.NORTH);
        
        txtResult = new JTextArea(8, 50);
        txtResult.setEditable(false);
        bottomPanel.add(new JScrollPane(txtResult), BorderLayout.CENTER);
        
        add(bottomPanel, BorderLayout.SOUTH);
        
        btnLogin.addActionListener(e -> 
            JOptionPane.showMessageDialog(this, "Konekcija ka serveru u razvoju"));
        
        btnExchanges.addActionListener(e -> 
            txtResult.append("Trazim moguce razmene... (jos nije povezano sa serverom)\n"));
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TradeClient().setVisible(true));
    }
}
package bilağları;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.*;
import javax.swing.border.*;

public class YarismaciClientGUI extends JFrame {
    private JLabel questionLabel;
    private JButton[] optionButtons;
    private JButton jokerSButton, jokerYButton;
    private JLabel feedbackLabel;
    private JLabel timerLabel;
    private Timer countdownTimer;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean seyirciJokerKullanildi = false;
    private boolean yariJokerKullanildi = false;
    
    private ImageIcon resizeIcon(String path, int width, int height) {
        try {
            ImageIcon icon = new ImageIcon(path);
            Image img = icon.getImage();
            Image resized = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(resized);
        } catch (Exception e) {
            System.err.println("İkon yüklenemedi: " + path);
            return null;
        }
    }

    
    private ImageIcon seyirciIcon = resizeIcon("resimler/seyirci.png", 37, 37);
    private ImageIcon yariyariyaIcon = resizeIcon("resimler/yariyariya2.jpg", 37, 37);
    private ImageIcon usedIcon = resizeIcon("resimler/cross.png", 37, 37);


    private List<String> jokerLines = new ArrayList<>();

    public YarismaciClientGUI() {
        setTitle("Kim Milyoner Olmak İster - Yarışmacı");
        setSize(650, 820);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // ✅ Arka plan
        setContentPane(new JLabel(new ImageIcon("resimler/sddefault.jpg")));
        setLayout(null);

        // ✅ Soru
        questionLabel = new JLabel("Soru yükleniyor...", SwingConstants.CENTER);
        questionLabel.setFont(new Font("Arial Black", Font.BOLD, 18));
        questionLabel.setForeground(new Color(255, 215, 0));
        questionLabel.setBounds(50, 150, 500, 60);
        add(questionLabel);

        // ✅ Zamanlayıcı etiketi
        timerLabel = new JLabel("Süre: 30", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        timerLabel.setForeground(Color.YELLOW);
        timerLabel.setBounds(550, 220, 80, 40);
        timerLabel.setOpaque(true);
        timerLabel.setBackground(new Color(0, 51, 102));
        timerLabel.setBorder(new LineBorder(Color.YELLOW, 2, true)); // Yuvarlak çerçeve
        add(timerLabel);

        // ✅ Seçenek butonları
        optionButtons = new JButton[4];
        String[] labels = {"A", "B", "C", "D"};
        int y = 440;
        for (int i = 0; i < 4; i++) {
            final String choice = labels[i];
            optionButtons[i] = new JButton(labels[i] + ") ");
            optionButtons[i].setFont(new Font("Arial", Font.BOLD, 16));
            optionButtons[i].setBounds(i % 2 == 0 ? 50 : 310, y, 240, 50);
            optionButtons[i].setBackground(new Color(0, 51, 102));
            optionButtons[i].setForeground(Color.WHITE);
            optionButtons[i].setFocusPainted(false);
            optionButtons[i].setBorder(BorderFactory.createLineBorder(Color.YELLOW, 2));
            optionButtons[i].addActionListener(e -> sendAnswer(choice));
            add(optionButtons[i]);
            if (i % 2 == 1) y += 70;
        }

        // ✅ Joker butonları
        jokerSButton = new OvalButton(seyirciIcon);
        jokerSButton.setBounds(560, 270, 60, 60);
        jokerSButton.setToolTipText("Seyirciye Sorma");


        jokerSButton.addActionListener(e -> {
            if (!seyirciJokerKullanildi) {
                sendAnswer("S");
                jokerSButton.setEnabled(false);
                jokerSButton.setIcon(usedIcon);

                seyirciJokerKullanildi = true;
            }
        });
        add(jokerSButton);

        jokerYButton = new OvalButton(yariyariyaIcon);
        jokerYButton.setBounds(560, 340, 60, 60);
        jokerYButton.setToolTipText("Yarı Yarıya");

        jokerYButton.addActionListener(e -> {
            if (!yariJokerKullanildi) {
                sendAnswer("Y");
                jokerYButton.setEnabled(false);
                jokerYButton.setIcon(usedIcon);


                yariJokerKullanildi = true;
            }
        });
        add(jokerYButton);

        // ✅ Geri bildirim etiketi
        feedbackLabel = new JLabel(" ", SwingConstants.CENTER);
        feedbackLabel.setFont(new Font("Arial", Font.BOLD, 16));
        feedbackLabel.setForeground(Color.YELLOW);
        feedbackLabel.setBounds(50, 570, 500, 30);
        add(feedbackLabel);

        setVisible(true);
        connectToServer();
    }

    private void connectToServer() {
        try {
            socket = new Socket("127.0.0.1", 4337);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            new Thread(this::receiveData).start();
        } catch (IOException e) {
            showError("Sunucuya bağlanılamadı.");
        }
    }

    private void receiveData() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                final String currentLine = line;

                if (currentLine.contains("Soru:")) {
                    jokerLines.clear();
                    String questionText = currentLine;
                    String[] options = new String[4];
                    for (int i = 0; i < 4; i++) options[i] = in.readLine();
                    in.readLine(); // Jokerler satırı
                    in.readLine(); // Cevap girin satırı

                    SwingUtilities.invokeLater(() -> {
                        questionLabel.setText(questionText);
                        for (int i = 0; i < 4; i++) {
                            optionButtons[i].setText(options[i]);
                            optionButtons[i].setEnabled(true);
                        }
                        new Timer(2000, e -> feedbackLabel.setText(" ")).start();
                        jokerSButton.setEnabled(true);
                        jokerYButton.setEnabled(true);

                        if (countdownTimer != null) countdownTimer.stop();
                        final int[] timeLeft = {30};
                        timerLabel.setText("Süre: " + timeLeft[0]);

                        countdownTimer = new Timer(1000, evt -> {
                            timeLeft[0]--;
                            timerLabel.setText("Süre: " + timeLeft[0]);
                            if (timeLeft[0] == 0) {
                                countdownTimer.stop();
                                feedbackLabel.setText("⏰ Süre doldu! Yarışma sona erdi.");
                                setButtonsEnabled(false);
                                out.println("TIMEOUT");
                                new Timer(2000, e -> System.exit(0)).start();
                            }
                        });
                        countdownTimer.start();
                    });

                }else if (currentLine.startsWith("Doğru cevap!")) {
                    SwingUtilities.invokeLater(() -> {feedbackLabel.setText("✅ Doğru cevap!");
                    });
                }

 else if (Arrays.asList(
                        "Linç Yükleniyor",
                        "Önemli olan katılmaktı",
                        "İki birden büyüktür",
                        "Buralara kolay gelmedik",
                        "Sen bu işi biliyorsun",
                        "Harikasın"
                ).contains(currentLine)) {
                    SwingUtilities.invokeLater(() -> {
                        feedbackLabel.setText("<html><center>" + feedbackLabel.getText() + "<br>" + currentLine + "</center></html>");
                    });
                }
else if (currentLine.startsWith("Yanlış cevap!")) {
                    SwingUtilities.invokeLater(() -> {
                        if (countdownTimer != null) countdownTimer.stop();
                        feedbackLabel.setText("❌ " + currentLine);
                        setButtonsEnabled(false);
                    });

                } else if (currentLine.startsWith("Tebrikler!")) {
                    SwingUtilities.invokeLater(() -> {
                        if (countdownTimer != null) countdownTimer.stop();
                        feedbackLabel.setText("🎉 " + currentLine);
                        setButtonsEnabled(false);
                    });

                } else if (currentLine.equals("END")) {
                    List<String> copy = new ArrayList<>(jokerLines);
                    jokerLines.clear();
                    SwingUtilities.invokeLater(() -> showAudiencePollDialog(copy));
                } else if (currentLine.contains("%") || currentLine.matches("^[A-D]\\).*")) {
                    jokerLines.add(currentLine);
                } else {
                    SwingUtilities.invokeLater(() -> feedbackLabel.setText(currentLine));
                }
            }
        } catch (IOException e) {
            showError("Sunucudan bağlantı kesildi.");
        }
    }

    private void sendAnswer(String choice) {
        if (countdownTimer != null) countdownTimer.stop();
        out.println(choice);
    }

    private void setButtonsEnabled(boolean enabled) {
        for (JButton btn : optionButtons) {
            btn.setEnabled(enabled);
        }
        jokerSButton.setEnabled(!seyirciJokerKullanildi);
        jokerSButton.setIcon(seyirciJokerKullanildi ? usedIcon : seyirciIcon);


        jokerYButton.setEnabled(!yariJokerKullanildi);
        jokerYButton.setIcon(yariJokerKullanildi ? usedIcon : yariyariyaIcon);

    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Hata", JOptionPane.ERROR_MESSAGE);
    }

    private void showAudiencePollDialog(List<String> pollResults) {
        StringBuilder message = new StringBuilder("<html>");
        for (String line : pollResults) {
            message.append(line).append("<br>");
        }
        message.append("</html>");
        JOptionPane.showMessageDialog(this, message.toString(), "Seyirciye Sorma", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(YarismaciClientGUI::new);
    }
}




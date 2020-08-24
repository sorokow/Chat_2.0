package com.company.client.service;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientService extends JFrame {

    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private static JTextArea textArea;
    private static JTextField textMassage = new JTextField();
    private boolean click = false;
    private boolean authClient = false;

    public ClientService() {

        try {

            socket = new Socket("localhost", 5115);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            Thread t1 = new Thread(() -> {
                try {
                    clientWindow();
                    while (true) {
                        String strMsg = dis.readUTF();
                        if (strMsg.startsWith("/authOk")) {
                            authClient = true;
                            break;
                        }
                        authClient = true;
                        textArea.append(strMsg + "\n");
                    }
                    while (true) {
                        String strMsg = dis.readUTF();
                        if (strMsg.equals("/exit")) {
                            break;
                        }
                        textArea.append(strMsg + "\n");
                    }
                } catch (IOException e){
                    authClient = false;
                    textArea.append("Вы отключены от чата!");
                    JOptionPane.showMessageDialog(null, "Ошибка отправки сообщения");
                }
            });
            t1.setDaemon(true);
            t1.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void clientWindow() {
        setTitle("Клиент");
        setBounds(400, 150, 700, 500);


        JPanel panel1 = new JPanel();
        add(panel1, BorderLayout.CENTER);
        panel1.setBackground(Color.gray);
        panel1.setLayout(new BorderLayout());

        JPanel panel2 = new JPanel();
        add(panel2, BorderLayout.SOUTH);
        panel2.setBackground(Color.darkGray);
        panel2.setPreferredSize(new Dimension(1, 40));
        panel2.setLayout(new BorderLayout());

        JButton btn = new JButton("Отправить");
        panel2.add(btn, BorderLayout.EAST);

        textArea = new JTextArea();
        JScrollPane textAreaScroll = new JScrollPane(textArea);
        panel1.add(textAreaScroll, BorderLayout.CENTER);
        textArea.setEditable(false);

        textMassage = new JTextField("");
        panel2.add(textMassage, BorderLayout.CENTER);

        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(!click){
                    Thread t2 = new Thread(() -> {
                        long startTime = System.currentTimeMillis();
                        long endTime = 0;
                        while(!authClient) {
                            endTime = System.currentTimeMillis();
                            if ((endTime - startTime) >= 120000) {
                                setVisible(false);
                                JOptionPane.showMessageDialog(null, "Вы слишком долго вводили данные!");
                                break;
                            }
                        }
                    });
                    t2.setDaemon(true);
                    t2.start();

                }
                click = true;
                sendMessage();
            }
        });


        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }


    private void sendMessage() {
        if (!textMassage.getText().trim().isEmpty()) {
            try {
                dos.writeUTF(textMassage.getText());
                textMassage.setText("");
                textMassage.grabFocus();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Ошибка отправки сообщения");
            }
        }
    }



}

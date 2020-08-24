package com.company.server.handler;


import com.company.client.service.ClientService;
import com.company.server.inter.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {

    private Server server;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;

    private String nick;

    public String getNick() {
        return nick;
    }

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
            this.nick = "";
            new Thread(() -> {
                try {
                    if (socket.isConnected()) {
                        authentication();
                        readMessage();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    closeConnection();
                }
            }).start();
        } catch (IOException e) {
            throw new RuntimeException("Проблемы при создании обработчика клиента");
        }
    }

    private void authentication() throws IOException {
        while (true) {
            if (socket.isConnected()) {
                String str = dis.readUTF();
                if (str.startsWith("/auth")) {
                    String[] dataArray = str.split("\\s");
                    String nick = server.getAuthService().getNick(dataArray[1], dataArray[2]);
                    if (nick != null) {
                        if (!server.isNickBusy(nick)) {
                            sendMsg("/authOk " + nick);
                            this.nick = nick;
                            server.broadcastMsg(this.nick + " Join to chat");
                            server.subscribe(this);
                            return;
                        } else {
                            sendMsg("You are logged in");
                        }
                    } else {
                        sendMsg("Incorrect password or login");
                    }
                }
            }
        }
    }

    public void sendMsg(String msg) {
        try {
            dos.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readMessage() throws IOException {
        while (true) {
            String clientStr = dis.readUTF();
            System.out.println("from " + this.nick + ": " + clientStr);
            if (clientStr.equals("/exit")) {
                sendMsg("/exit");
                return;
            }

            if (clientStr.startsWith("/w ")) {
                String[] tokens = clientStr.split("\\s");
                String nick = tokens[1];
                String msg = clientStr.substring(4 + nick.length());
                server.personalMsg(nick, msg);
            }else {
                server.broadcastMsg(this.nick + ": " + clientStr);
            }
        }
    }

    private void closeConnection() {
        server.unsubscribe(this);
        server.broadcastMsg(this.nick + ": out from chat");

        try {
            dis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

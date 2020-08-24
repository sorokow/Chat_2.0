package com.company.server.service;

import com.company.server.handler.ClientHandler;
import com.company.server.inter.AuthService;
import com.company.server.inter.Server;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class ServerImpl implements Server {

    private List<ClientHandler> clients;
    private AuthService authService;

    public ServerImpl() {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            authService = new AuthServiceImpl();
            authService.start();
            clients = new LinkedList<>();
            while (true) {
                System.out.println("Wait join clients");
                Socket socket = serverSocket.accept();
                System.out.println("Client join");
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            System.out.println("Problem in server");
        } finally {
            if (authService != null) {
                authService.stop();
            }
        }
    }

    @Override
    public synchronized boolean isNickBusy(String nick) {
        for (ClientHandler c : clients) {
            if (c.getNick() != null && c.getNick().equals(nick)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized void broadcastMsg(String msg) {
        for (ClientHandler c : clients) {
            c.sendMsg(msg);
        }
    }

    @Override
    public synchronized void subscribe(ClientHandler client) {
        clients.add(client);
        broadcastClientList();
    }

    @Override
    public synchronized void unsubscribe(ClientHandler client) {
        clients.remove(client);
        broadcastClientList();
    }

    @Override
    public AuthService getAuthService() {
        return authService;
    }

    public synchronized void personalMsg(String nickInterlocutor, String msgInterlocutor) {
        for (ClientHandler c : clients) {
            if (c.getNick().equals(nickInterlocutor)) {
                c.sendMsg(msgInterlocutor);
            }
        }
    }

    @Override
    public synchronized void broadcastClientList() {
        StringBuilder builder = new StringBuilder("/clients");
        for (ClientHandler c : clients) {
            builder.append(c.getNick() + " ");
        }
        broadcastMsg(builder.toString());
    }
}

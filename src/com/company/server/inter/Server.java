package com.company.server.inter;


import com.company.server.handler.ClientHandler;

public interface Server {
    int PORT = 5115;

    boolean isNickBusy(String nick);

    void broadcastMsg(String msg);

    void subscribe(ClientHandler client);

    void unsubscribe(ClientHandler client);

    AuthService getAuthService();

    void personalMsg(String nickInterlocutor, String msgInterlocutor);

    void broadcastClientList();
}

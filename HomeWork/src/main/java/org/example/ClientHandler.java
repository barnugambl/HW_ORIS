package org.example;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClientHandler implements Runnable{

    public static List<ClientHandler> clientHandlers = new ArrayList<>();

    private Socket socket;

    private BufferedWriter bufferedWriter;

    private BufferedReader bufferedReader;

    private String clientUsername;

    private String clientId;


    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUsername = bufferedReader.readLine();
            this.clientId = UUID.randomUUID().toString();
            clientHandlers.add(this);
            broadcastMessage("SERVER " + clientUsername + " подключился");

        } catch (Exception e) {
            closeEverything(socket,bufferedReader,bufferedWriter);
        }
    }

    @Override
    public void run() {
        String messageFromClient;

        while (socket.isConnected()){
            try {
                messageFromClient = bufferedReader.readLine();
                broadcastMessage(messageFromClient);
            } catch (IOException e) {
                closeEverything(socket,bufferedReader,bufferedWriter);
                break;
            }
        }
    }


    public void broadcastMessage(String messageToSend){
        for (ClientHandler clientHandlers : clientHandlers){
            try {
                if(!clientHandlers.clientId.equals(this.clientId)){
                    clientHandlers.bufferedWriter.write(messageToSend);
                    clientHandlers.bufferedWriter.newLine();
                    clientHandlers.bufferedWriter.flush();
                }
//                if(clientHandlers != this) {
//                    clientHandlers.bufferedWriter.write(messageToSend);
//                    clientHandlers.bufferedWriter.newLine();
//                    clientHandlers.bufferedWriter.flush();
//                }

            } catch (IOException e) {
                closeEverything(socket,bufferedReader,bufferedWriter);
            }
        }
    }

    public void removeClientHandler(){
        clientHandlers.remove(this);
        broadcastMessage("SERVER: " + clientUsername + " вышел");
    }


    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        try {
            if(bufferedReader != null){
                bufferedReader.close();
            }
            if(bufferedWriter != null){
                bufferedWriter.close();
            }
            if(socket != null){
                socket.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
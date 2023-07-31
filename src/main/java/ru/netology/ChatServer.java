package ru.netology;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatServer {
    private List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private int port;
    private boolean isRunning = true;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    public ChatServer(int port) {
        this.port = port;
    }

    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Сервер запущен на порту " + port);

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Новое подключение: " + clientSocket);
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                clientHandler.start();
            }

            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcastMessage(String message) {
        String timestampedMessage = "[" + dateFormat.format(new Date()) + "] " + message;
        System.out.println(timestampedMessage);
        try {
            FileWriter fileWriter = new FileWriter("file.log", true);
            fileWriter.write(timestampedMessage + "\n");
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public void stop() {
        isRunning = false;
    }

    private class ClientHandler extends Thread {
        private Socket clientSocket;
        private BufferedReader reader;
        private PrintWriter writer;
        private String userName;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                writer = new PrintWriter(clientSocket.getOutputStream(), true);

                userName = reader.readLine();
                broadcastMessage(userName + " присоединился(-лась) к чату.");

                String inputLine;
                while ((inputLine = reader.readLine()) != null) {
                    if (inputLine.equalsIgnoreCase("/exit")) {
                        writer.println("exit");
                        break;
                    }
                    broadcastMessage(userName + ": " + inputLine);
                }

                clientSocket.close();
                clients.remove(this);
                broadcastMessage(userName + " покинул(-а) чат.");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendMessage(String message) {
            writer.println(message);
        }
    }

    public static void main(String[] args) {
        int port = readPortFromSettings();

        ChatServer server = new ChatServer(port);
        server.start();
    }

    private static int readPortFromSettings() {
        try {
            BufferedReader br = new BufferedReader(new FileReader("settings.txt"));
            String portString = br.readLine();
            br.close();
            return Integer.parseInt(portString);
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            return -1;
        }
    }
}

package ru.netology;

import java.io.*;
import java.net.Socket;

public class ChatClient {
    private String serverAddress;
    private int serverPort;
    private String userName;

    public ChatClient(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public void start() {
        try {
            Socket socket = new Socket(serverAddress, serverPort);
            System.out.println("Подключено к серверу: " + socket);

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("Введите ваше имя:");
            userName = reader.readLine();

            writer.println(userName);

            Thread inputThread = new Thread(() -> {
                try {
                    String serverResponse;
                    while ((serverResponse = serverReader.readLine()) != null) {
                        System.out.println(serverResponse);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            inputThread.start();

            String userInput;
            while ((userInput = reader.readLine()) != null) {
                writer.println(userInput);

                if (userInput.equalsIgnoreCase("/exit")) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }

            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String serverAddress = "localhost";
        int port = readPortFromSettings();

        if (args.length > 0) {
            serverAddress = args[0];
        }

        ChatClient client = new ChatClient(serverAddress, port);
        client.start();
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

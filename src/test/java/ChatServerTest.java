import org.junit.jupiter.api.*;
import ru.netology.ChatServer;

import java.io.*;
import java.net.Socket;

public class ChatServerTest {
    private static final int TEST_PORT = 12346;
    private static ChatServer server;
    private static Thread serverThread;

    @BeforeAll
    public static void setUpServer() {
        server = new ChatServer(TEST_PORT);
        serverThread = new Thread(() -> server.start());
        serverThread.start();
    }

    @AfterAll
    public static void tearDownServer() {
        server.stop();
        try {
            serverThread.join(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testClientConnection() {
        try {
            Socket clientSocket = new Socket("localhost", TEST_PORT);
            Assertions.assertTrue(clientSocket.isConnected());
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
            Assertions.fail("Failed to connect the client");
        }
    }

    @Test
    public void testBroadcastMessage() {
        String message = "Hello, server!";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            Socket clientSocket = new Socket("localhost", TEST_PORT);

            Thread clientReadThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                    String response = reader.readLine();
                    outputStream.write(response.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            clientReadThread.start();

            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
            writer.println(message);

            clientReadThread.join(2000);

            String response = new String(outputStream.toByteArray());
            Assertions.assertTrue(response.contains(message));
            clientSocket.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Assertions.fail("Ошибка отправки/получения сообщения");
        }
    }
}


import org.junit.jupiter.api.*;
import ru.netology.ChatServer;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class ChatClientTest {
    private static final int TEST_PORT = 12346;
    private static ChatServer server;
    private static Thread serverThread;
    private static Socket clientSocket;

    @BeforeAll
    public static void setUpServerAndClient() {
        server = new ChatServer(TEST_PORT);
        serverThread = new Thread(() -> server.start());
        serverThread.start();

        try {
            TimeUnit.MILLISECONDS.sleep(500);
            clientSocket = new Socket("localhost", TEST_PORT);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Assertions.fail("Failed to set up server and client");
        }
    }

    @AfterAll
    public static void tearDownServerAndClient() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        server.stop();
        try {
            serverThread.join(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSendMessage() {
        String message = "Hello, server!";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            Thread serverReadThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                    String response = reader.readLine();
                    outputStream.write(response.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            serverReadThread.start();

            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
            writer.println(message);

            serverReadThread.join(2000);

            String response = new String(outputStream.toByteArray());
            Assertions.assertTrue(response.contains(message));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Assertions.fail("Ошибка отправки/получения сообщения");
        }
    }
}


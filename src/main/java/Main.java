import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Main {
    public static void main(String[] args) {
        int port = 9092;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream())) {
            OutputStream outputStream = clientSocket.getOutputStream();

            while (true) {
                handleRequest(inputStream, outputStream);
            }
        } catch (IOException e) {
            System.out.println("IOException while handling client: " + e.getMessage());
        }
    }

    private static void handleRequest(DataInputStream inputStream, OutputStream outputStream) throws IOException {
        int messageSize = inputStream.readInt();
        byte[] apiKey = inputStream.readNBytes(2);
        short apiVersion = inputStream.readShort();
        byte[] correlationId = inputStream.readNBytes(4);
        byte[] remainingBytes = new byte[messageSize - 8];

        inputStream.readFully(remainingBytes);

        if (apiVersion < 0 || apiVersion > 4) {
            sendErrorResponse(outputStream, correlationId);
        } else {
            sendAPIVersionsResponse(outputStream, correlationId);
        }
    }

    private static void sendAPIVersionsResponse(OutputStream outputStream, byte[] correlationId) throws IOException {
        ByteArrayOutputStream responseHeader = new ByteArrayOutputStream();
        responseHeader.write(correlationId);

        responseHeader.write(new byte[] { 0, 0 }); // NO error
        responseHeader.write(3);

        // Entry for APIVersions (API key 18)
        responseHeader.write(new byte[] { 0, 18 }); // apiKey
        responseHeader.write(new byte[] { 0, 0 }); // min version
        responseHeader.write(new byte[] { 0, 4 }); // max version
        responseHeader.write(0);

        // Entry for DescribeTopicPartitions (API key 75)
        responseHeader.write(new byte[] { 0, 75 }); // apiKey
        responseHeader.write(new byte[] { 0, 0 }); // min version
        responseHeader.write(new byte[] { 0, 0 }); // max version
        responseHeader.write(0);

        responseHeader.write(new byte[] { 0, 0, 0, 0 });
        responseHeader.write(0);

        byte[] messageLength = ByteBuffer.allocate(4).putInt(responseHeader.size()).array();
        byte[] response = responseHeader.toByteArray();

        outputStream.write(messageLength);
        outputStream.write(response);
        outputStream.flush();
    }

    private static void sendErrorResponse(OutputStream outputStream, byte[] correlationId) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(correlationId);
        bos.write(new byte[] { 0, 35 });

        byte[] messageLength = ByteBuffer.allocate(4).putInt(bos.size()).array();
        byte[] response = bos.toByteArray();
        outputStream.write(messageLength);
        outputStream.write(response);
        outputStream.flush();
    }
}

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        ServerSocket serverSocket;
        Socket clientSocket = null;
        int port = 9092;

        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            clientSocket = serverSocket.accept(); // Wait for connection from client.

            DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
            OutputStream outputStream = clientSocket.getOutputStream();

            while (true) {
                handleRequest(inputStream, outputStream);
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            }
        }
    }

    private static void handleRequest(DataInputStream inputStream, OutputStream outputStream) throws IOException {
        int messageSize = inputStream.readInt();
        byte[] apiKey = inputStream.readNBytes(2);
        short apiVersion = inputStream.readShort();
        byte[] correlationId = inputStream.readNBytes(4);
        byte[] remainingBytes = new byte[messageSize - 8];

        inputStream.readFully(remainingBytes);

        ByteArrayOutputStream responseHeader = new ByteArrayOutputStream();
        responseHeader.write(correlationId);

        responseHeader.write(getErrorCode(apiVersion));
        responseHeader.write(2);
        responseHeader.write(apiKey);
        responseHeader.write(new byte[] { 0, 0 }); // min version
        responseHeader.write(new byte[] { 0, 4 }); // max version
        responseHeader.write(0); // tagged fields
        responseHeader.write(new byte[] { 0, 0, 0, 0 }); // throttle time
        responseHeader.write(0); // tagged fields

        int responseSize = responseHeader.toByteArray().length;
        byte[] messageLength = ByteBuffer.allocate(4).putInt(responseSize).array();
        byte[] response = responseHeader.toByteArray();

        System.out.println(Arrays.toString(messageLength));
        System.out.println(Arrays.toString(response));

        outputStream.write(messageLength);
        outputStream.write(response);
        outputStream.flush();
    }

    private static byte[] getErrorCode(short apiVersion) {
        return (apiVersion < 0 || apiVersion > 4) ? new byte[] { 0, 35 } : new byte[] { 0, 0 };
    }
}

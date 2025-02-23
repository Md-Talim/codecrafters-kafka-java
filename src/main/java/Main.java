import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Main {
    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        int port = 9092;
        try {
            serverSocket = new ServerSocket(port);
            // Since the tester restarts your program quite often, setting
            // SO_REUSEADDR ensures that we don't run into 'Address already in use'
            // errors
            serverSocket.setReuseAddress(true);
            // Wait for connection from client.
            clientSocket = serverSocket.accept();

            InputStream inputStream = clientSocket.getInputStream();
            OutputStream outputStream = clientSocket.getOutputStream();

            byte[] requestBuffer = new byte[12];
            int bytesRead = inputStream.read(requestBuffer);

            ByteBuffer requestHeader = ByteBuffer.wrap(requestBuffer);
            int messageSize = requestHeader.getInt();
            short apiKey = requestHeader.getShort(); // The API key for the request (2 bytes)
            short apiVersion = requestHeader.getShort(); // The version of the API for the request (2 bytes)
            int correlationId = requestHeader.getInt(); // A unique identifier for the request (4 bytes)
            short errorCode = 35;

            // Construct the response
            ByteBuffer response = ByteBuffer.allocate(10);
            response.putInt(messageSize);
            response.putInt(correlationId);
            response.putShort(errorCode);

            // Send the response
            outputStream.write(response.array());
            outputStream.flush();
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
}

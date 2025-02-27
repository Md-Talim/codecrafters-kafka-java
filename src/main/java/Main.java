import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        int port = 9092;

        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            clientSocket = serverSocket.accept(); // Wait for connection from client.

            InputStream inputStream = clientSocket.getInputStream();
            OutputStream outputStream = clientSocket.getOutputStream();

            inputStream.readNBytes(4); // message_size
            inputStream.readNBytes(2); // api key
            byte[] apiVersionBytes = inputStream.readNBytes(2);
            short apiVersion = ByteBuffer.wrap(apiVersionBytes).getShort();
            byte[] correlationId = inputStream.readNBytes(4);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(correlationId);

            if (apiVersion < 0 || apiVersion > 4) {
                bos.write(new byte[] { 0, 35 }); // error_code = 35
            } else {
                bos.write(new byte[] { 0, 0 }); // error_code = 0
                bos.write(2);
                bos.write(new byte[] { 0, 18 }); // apiKey
                bos.write(new byte[] { 0, 3 }); // min version
                bos.write(new byte[] { 0, 4 }); // max version
                bos.write(0); // tagged fields
                bos.write(new byte[] { 0, 0, 0, 0 }); // throttle time
                bos.write(0); // tagged fields
            }

            int size = bos.size();
            byte[] sizeBytes = ByteBuffer.allocate(4).putInt(size).array();
            byte[] response = bos.toByteArray();

            System.out.println(Arrays.toString(sizeBytes));
            System.out.println(Arrays.toString(response));

            outputStream.write(sizeBytes);
            outputStream.write(response);
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

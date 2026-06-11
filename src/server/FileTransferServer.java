package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FileTransferServer implements Runnable {
    private final int PORT;

    public FileTransferServer(int port) {
        this.PORT = port;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("File Transfer Server running on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new FileTransferHandler(socket)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

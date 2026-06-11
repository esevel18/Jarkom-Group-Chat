package server;

import java.net.Socket;
import java.io.*;
import java.nio.file.*;

public class FileTransferHandler implements Runnable {

    private final Socket socket;

    public FileTransferHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        try {
            DataInputStream dInputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream dOutputStream = new DataOutputStream(socket.getOutputStream());

            System.out.println("File transfer connection from: " + socket.getInetAddress());

            // Read metadata
            String action = dInputStream.readUTF();
            String fileType = dInputStream.readUTF();
            String fileName = dInputStream.readUTF();

            if (action.equalsIgnoreCase("UPLOAD")) {
                long fileSize = dInputStream.readLong();

                // Create upload directory
                File dir = new File("uploads/", fileType);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File targetFile = new File(dir, fileName);
                try (FileOutputStream fOutputStream = new FileOutputStream(targetFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    long totalRead = 0;
                    while (totalRead < fileSize && (bytesRead = dInputStream.read(buffer)) != -1) {
                        fOutputStream.write(buffer, 0, bytesRead);
                        totalRead += bytesRead;
                    }
                }
            } else if (action.equalsIgnoreCase("DOWNLOAD")) {
                File targetFile = new File("uploads/" + fileType + "/" + fileName);
                if (targetFile.exists()) {
                    dOutputStream.writeBoolean(true);
                    dOutputStream.writeLong(targetFile.length());
                    try (FileInputStream fInputStream = new FileInputStream(targetFile)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = fInputStream.read(buffer)) != -1) {
                            dOutputStream.write(buffer, 0, bytesRead);
                        }
                        dOutputStream.flush();
                    }

                } else {
                    dOutputStream.writeBoolean(false);
                }
            } else {
                System.out.println("Unkown action: " + action);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
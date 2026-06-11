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

        try (
            DataInputStream in =
                new DataInputStream(socket.getInputStream())
        ) {

            System.out.println(
                "File transfer connection from: "
                + socket.getInetAddress()
            );

            // Read metadata
            String fileType = in.readUTF();
            String fileName = in.readUTF();
            long fileSize = in.readLong();

            System.out.println(
                "Receiving " + fileName +
                " (" + fileSize + " bytes)"
            );

            // Create upload directory
            Path uploadDir =
                Paths.get("uploads", fileType);

            Files.createDirectories(uploadDir);

            // Target file
            Path targetFile =
                uploadDir.resolve(fileName);

            // Save file
            try (
                OutputStream fileOut =
                    Files.newOutputStream(targetFile)
            ) {

                byte[] buffer = new byte[8192];
                long remaining = fileSize;

                while (remaining > 0) {

                    int read = in.read(
                        buffer,
                        0,
                        (int) Math.min(
                            buffer.length,
                            remaining
                        )
                    );

                    if (read == -1) {
                        break;
                    }

                    fileOut.write(buffer, 0, read);
                    remaining -= read;
                }
            }

            System.out.println(
                "Saved file: " + targetFile
            );

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
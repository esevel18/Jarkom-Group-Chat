package client;

import java.io.*;
import java.net.Socket;

import server.NetworkConfig;

public class FileTransferClient {

    private String host;

    public FileTransferClient(String host) {
        this.host = host;
    }

    public void uploadFile(File file, String fileType) throws IOException {
        try (
                Socket socket = new Socket(host, NetworkConfig.FILE_PORT);

                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                FileInputStream fileIn = new FileInputStream(file)) {

            out.writeUTF("UPLOAD");
            out.writeUTF(fileType);
            out.writeUTF(file.getName());
            out.writeLong(file.length());

            byte[] buffer = new byte[8192];

            int bytesRead;

            while ((bytesRead = fileIn.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }

            out.flush();
            System.out.println("Uploaded: " + file.getName());
        }
    }

    public void uploadImage(File imageFile) throws IOException {
        uploadFile(imageFile, "image");
    }

    public void uploadVideo(File videoFile) throws IOException {
        uploadFile(videoFile, "video");
    }

    public static boolean downloadFile(String host, String fileType, String fileName, File destinationFile)
            throws IOException {
        try (
                Socket socket = new Socket(host, NetworkConfig.FILE_PORT);

                DataOutputStream dOutputStream = new DataOutputStream(socket.getOutputStream());

                DataInputStream dInputStream = new DataInputStream(socket.getInputStream())) {

            dOutputStream.writeUTF("DOWNLOAD");
            dOutputStream.writeUTF(fileType);
            dOutputStream.writeUTF(fileName);
            dOutputStream.flush();

            boolean fileExists = dInputStream.readBoolean();
            if (!fileExists) {
                System.err.println("Server reported file does not exist: " + fileName);
                return false;
            }

            long fileSize = dInputStream.readLong();

            // Check if the folder exist in client side
            if (destinationFile.getParentFile() != null) {
                destinationFile.getParentFile().mkdirs();
            }

            // Write incoming file to disk
            try (FileOutputStream fOutputStream = new FileOutputStream(destinationFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalRead = 0;

                while (totalRead < fileSize && (bytesRead = dInputStream.read(buffer)) != -1) {
                    fOutputStream.write(buffer, 0, bytesRead);
                    totalRead += bytesRead;
                }
            }
            return true;
        } catch (IOException e) {
            System.err.println("Gagal mengunduh file: " + e.getMessage());
            return false;
        }

    }
}
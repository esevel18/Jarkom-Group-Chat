package server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
	// private static final int PORT = 1234;
	private ServerSocket serverSocket;

	// simpan client yang terhubung ke server
	public static final Map<String, ClientHandler> connectedClients = new ConcurrentHashMap<>();
	// simpan juga group chat apa yang ada di server
	public static final Map<String, Room> activeRooms = new ConcurrentHashMap<>();

	public ChatServer(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}

	public void startServer() {
		try {
			while (!serverSocket.isClosed()) {
				// accept client connection while the server is not closed
				Socket clientSocket = serverSocket.accept();
				System.out.println("A new Client has connected!");
				ClientHandler clientHandler = new ClientHandler(clientSocket);

				Thread clientThread = new Thread(clientHandler);
				clientThread.start();
			}
		} catch (IOException e) {
			closeServerSocket();
		}
	}

	private void closeServerSocket() {
		try {
			if (serverSocket != null) {
				serverSocket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {
			Thread fileServerThread = new Thread(new FileTransferServer(NetworkConfig.FILE_PORT));
			fileServerThread.start();

			ServerSocket serverSocket = new ServerSocket(NetworkConfig.CHAT_PORT);
			ChatServer server = new ChatServer(serverSocket);
			System.out.println("Chat Server running on port " + NetworkConfig.CHAT_PORT);
			server.startServer();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}

package client;

import java.io.*;
import java.net.*;

import controller.ChatListener;

public class ChatClient {
	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	private String userName;
	private ChatListener listener;
	
	public ChatClient(String address, int port, ChatListener listener) throws IOException {
		this.listener = listener;
		this.socket = new Socket(address, port);
		this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.out = new PrintWriter(socket.getOutputStream(), true);
		
		new Thread(() -> {
			try {
				String response;
				while((response = in.readLine()) != null) {
					processServerResponse(response);
				}
			} catch(IOException e) {
				listener.onInfoReceived("Koneksi terputus");
			}
		}).start();
	}
	
	private void processServerResponse(String response) {
		String[] parts = response.split("\\#", 3);
	    if (parts.length == 0) return;
	    
	    String action = parts[0];

	    switch (action) {
	        case "msg":
	            if (parts.length >= 3) listener.onMessageReceived(parts[1], parts[2]);
	            break;
	        case "info":
	            if (parts.length >= 2) listener.onInfoReceived(parts[1]);
	            break;
	        case "roomlist":
	            String[] rooms = parts.length > 1 ? parts[1].split(",") : new String[0];
	            listener.onRoomListUpdated(rooms);
	            break;
	        case "joined":
	            if (parts.length >= 2) {
	                // array members dikosongkan karena sekarang ditangani "memberlist"
	                listener.onRoomJoined(parts[1], new String[0]); 
	            }
	            break;
	        case "memberlist": // Menerima data daftar member baru dari server
	            String[] members = parts.length > 1 ? parts[1].split(",") : new String[0];
	            listener.onMemberListUpdated(members);
	            break;
	        case "kicked":
	        case "roomclosed":
	            if (parts.length >= 2) listener.onKickedOrClosed(parts[1]);
	            break;
	        case "error":
	            if (parts.length >= 2) listener.onInfoReceived("Error: " + parts[1]);
	    }
	}
	
	public void login(String userName) {
		this.userName = userName;
		out.println("login#" + userName);
	}
	
	public void createRoom(String roomName) { out.println("create#" + roomName); }
    public void listRooms() { out.println("list#"); }
    public void joinRoom(String roomName) { out.println("join#" + roomName); }
    public void sendMessage(String message) { out.println("msg#" + message); }
    public void leaveRoom() { out.println("leave#"); }
    public void closeRoom() { out.println("close#"); }
    public void kickUser(String targetUsername) { out.println("kick#" + targetUsername); }
	
	public String getUserName() {
		return userName;
	}
	
}

package server;

import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable{
	
	private Socket clientSocket;
	private BufferedReader in;
	private PrintWriter out;
	private String userName;
	private Room currRoom;
	
	public ClientHandler(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}
	
	// client thread 1 client = 1 thread (agar tidak blocking program)
	@Override
	public void run() {
		try {
			// read the message from client TCP
			in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
			out = new PrintWriter(this.clientSocket.getOutputStream(), true);
			
			String message;
			while((message = in.readLine()) != null) {
				processMessage(message);
			}
			
		} catch(IOException e) {
			System.out.println("Sambungan dari " + this.userName + " telah terputus");
		} finally {
			disconnectFromServer();
		}
		
	}
	
	public void sendMessage(String message) {
		if(out != null)
			out.println(message);
	}
	
	private void processMessage(String message) {
		String[] split = message.split("\\#", 2);
		String action = split[0];
		String text = split.length > 1 ? split[1] : "";
		
		switch(action) {
			case "login" -> {
				this.userName = text;
				ChatServer.connectedClients.put(this.userName, this);
				sendMessage(message);
			}
			
			case "create" -> {
				if(!ChatServer.activeRooms.containsKey(text)) {
			        Room newRoom = new Room(text, this.userName);
			        ChatServer.activeRooms.put(text, newRoom);
			        this.currRoom = newRoom;         
			        newRoom.addMember(this);         

			        sendMessage("joined#" + text);
			        broadcastMemberList(newRoom);  // Update member ke UI
			        
			        broadcastGlobalRoomList();
			        
			        System.out.println("Room '" + text + "' berhasil dibuat oleh " + this.userName);
			    } else {
			        sendMessage("error#Nama group sudah ada");
			    }
			}
			
			case "list" -> {
				StringBuilder list = new StringBuilder("roomlist#");
                for (Room r : ChatServer.activeRooms.values()) {
                    list.append(r.getRoomName()).append("(").append(r.getMembers().size()).append("),");
                }
                sendMessage(list.toString());
			}
			
			case "join" -> {
				joinRoom(text);
			}
			
			case "leave" -> {
				leaveCurrentRoom();
			}
			
			case "msg" -> {
				if(currRoom != null)
					currRoom.broadcastMessage("msg#" + this.userName + "#" + text, this);
			}
			
			case "kick" -> {
				if (currRoom != null && ChatServer.activeRooms.containsKey(currRoom.getRoomName()) && currRoom.getOwner().equals(this.userName)) {
			        ClientHandler target = ChatServer.connectedClients.get(text);
			        if (target != null && currRoom.getMembers().contains(target)) {
			            target.sendMessage("kicked#Anda telah dikeluarkan dari ruang.");
			            target.leaveCurrentRoom(); // leaveCurrentRoom akan memanggil broadcastMemberList secara otomatis
			        }
			    } else {
			        sendMessage("error#Hanya pemilik ruang (Owner) yang bisa melakukan kick.");
			    }
			}
			
			case "close" -> {
				if (currRoom != null && ChatServer.activeRooms.containsKey(currRoom.getRoomName()) && currRoom.getOwner().equals(this.userName)) {
			        
			        // 1. Simpan nama room di variabel sementara sebelum currRoom di-null-kan
			        String deletedRoomName = currRoom.getRoomName();
			        
			        // 2. Beri tahu semua member di dalam room (termasuk Owner) bahwa room ditutup.
			        // broadcast ke semua member 
			        for (ClientHandler member : currRoom.getMembers()) {
			            member.sendMessage("roomclosed#Ruang telah ditutup oleh pemilik.");
			            member.currRoom = null;
			        }
			        
			        // 3. Hapus dari daftar room aktif 
			        ChatServer.activeRooms.remove(deletedRoomName);
			        
			        broadcastGlobalRoomList();
			        
			    } else {
			        sendMessage("error#Anda bukan pemilik group ini");
			    }
			}
	            
		}
	}
	
	private void joinRoom(String roomName) {
		Room room = ChatServer.activeRooms.get(roomName);
		if(room != null) {
			
	        // Cek apakah user ada di dalam room yang sama
	        if (currRoom != null && currRoom.getRoomName().equals(roomName)) {
	            sendMessage("error#Anda sudah bergabung di dalam ruangan ini");
	            return; // Hentikan proses agar tidak bergabung dua kali
	        }
	        
	        // Jika user sedang di room lain, keluarkan dulu dari room lama
	        if (currRoom != null) {
	            leaveCurrentRoom();
	        }
			
			currRoom = room;
			currRoom.addMember(this);
			
			StringBuilder memberList = new StringBuilder();
			for(ClientHandler clientHandler : currRoom.getMembers()) {
				memberList.append(clientHandler.getUserName()).append(",");
			}
			
			sendMessage("joined#" + roomName + "#" + memberList);
			currRoom.broadcastMessage("info#" + this.userName + " bergabung ke group", this);
			broadcastMemberList(currRoom);
			
			broadcastGlobalRoomList();
		} else {
			sendMessage("error#Ruangan tidak ditemukan");
		}
	}
	
	private void leaveCurrentRoom() {
		if(currRoom != null) {
			Room tempRoom = currRoom;
	        currRoom.removeMember(this);
	        tempRoom.broadcastMessage("info#" + this.userName + " telah keluar dari group", this);
	        sendMessage("info#Anda keluar dari group");
	        broadcastMemberList(tempRoom); 
	        currRoom = null;
	        
	        broadcastGlobalRoomList();
		}
	}
	
	private void disconnectFromServer() {
		try {
			if(clientSocket != null) 
				clientSocket.close();
			
			if(in != null) 
				in.close();
			
			if(out != null) 
				out.close();
			
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private void broadcastMemberList(Room room) {
	    if (room != null) {
	        StringBuilder memberList = new StringBuilder("memberlist#");
	        for (ClientHandler client : room.getMembers()) {
	        	String memberName = client.getUserName();
	        	if(memberName.equals(room.getOwner())) 
	        		memberName += " (Owner)";
	            memberList.append(memberName).append(",");
	        }
	        String msg = memberList.toString();

	        for (ClientHandler client : room.getMembers()) {
	            client.sendMessage(msg);
	        }
	    }
	}
	
	private void broadcastGlobalRoomList() {
	    StringBuilder list = new StringBuilder("roomlist#");
	    for (Room r : ChatServer.activeRooms.values()) {
	        // Kirim format: NamaRoom(JumlahMember),
	        list.append(r.getRoomName()).append("(").append(r.getMembers().size()).append("),");
	    }
	    String roomListStr = list.toString();

	    // Broadcast daftar ruangan terbaru ke semua client yang online di server
	    for (ClientHandler client : ChatServer.connectedClients.values()) {
	        client.sendMessage(roomListStr);
	    }
	}
	
	public String getUserName() { return userName; }
}

package server;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Room {
	private String roomName;
	private String owner;
	private List<ClientHandler> clientHandlers = new CopyOnWriteArrayList<ClientHandler>();

	public Room(String roomName, String owner) {
		this.roomName = roomName;
		this.owner = owner;
	}

	// method
	public synchronized void addMember(ClientHandler member) {
		if (!this.clientHandlers.contains(member)) {
			this.clientHandlers.add(member);
		}
	}

	public synchronized void removeMember(ClientHandler member) {
		this.clientHandlers.remove(member);
	}

	public synchronized void broadcastMessage(String message, ClientHandler sender) {
		for (ClientHandler clientHandler : clientHandlers) {
			if (!clientHandler.equals(sender))
				clientHandler.sendMessage(message);
		}
	}

	/**
	 * Broacast Image & Video
	 * 
	 * @author Pearce Nathaniel N.
	 * @param Image || Video
	 */

	public synchronized void broadcastImage(String message, ClientHandler sender) {
		for (ClientHandler clientHandler : clientHandlers) {
			if (!clientHandler.equals(sender))
				clientHandler.sendMessage(message);
		}
	}

	public synchronized void broadcastVideo(String message, ClientHandler sender) {
		for (ClientHandler clientHandler : clientHandlers) {
			if (!clientHandler.equals(sender))
				clientHandler.sendMessage(message);
		}
	} 
	
	// getter
	public String getRoomName() {
		return this.roomName;
	}

	public String getOwner() {
		return this.owner;
	}

	public List<ClientHandler> getMembers() {
		return this.clientHandlers;
	}
}

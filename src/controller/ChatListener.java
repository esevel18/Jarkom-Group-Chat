package controller;

public interface ChatListener{
	void onMessageReceived(String sender, String message);
    void onInfoReceived(String infoMessage);
    void onRoomListUpdated(String[] rooms);
    void onRoomJoined(String roomName, String[] members);
    void onKickedOrClosed(String reason);
    void onMemberListUpdated(String[] members);

    // For Image or Video
    void onImageReceived(String sender, String filename);
    void onVideoReceived(String sender, String filename);
}

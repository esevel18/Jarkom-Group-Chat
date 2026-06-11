package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import java.io.File;

import client.ChatClient;

public class MainController implements ChatListener {

    @FXML
    private Label myUsernameLabel;
    @FXML
    private TextField roomInput;
    @FXML
    private VBox roomListContainer;
    @FXML
    private Label currentRoomTitle;
    @FXML
    private VBox messageContainer;
    @FXML
    private TextField messageInput;
    @FXML
    private VBox memberListContainer;
    @FXML
    private Button deleteGroupBtn;

    private ChatClient chatClient;
    private String userName;
    private String currentRoom = "";

    // Set client saat login dilakuakan
    public void setClient(ChatClient client, String userName) {
        this.chatClient = client;
        this.userName = userName;
        this.myUsernameLabel.setText(userName);

        // Meminta daftar ruang saat pertama kali masuk
        chatClient.listRooms();
    }

    @FXML
    void handleDeleteRoom() {
        if (!currentRoom.isEmpty()) {
            chatClient.closeRoom();
        }
    }

    @FXML
    void handleSendMessage() {
        String msg = messageInput.getText().trim();
        if (!msg.isEmpty() && !currentRoom.isEmpty()) {
            chatClient.sendMessage(msg);

            addMessageBubble(msg, "Saya", true);
            messageInput.clear();
        }
    }

    /**
     * Handle Sen Image or Video
     * 
     * @author Pearce Nathaniel N.
     */
    @FXML
    void handleSendImage() {

    }

    @FXML
    void handleSendVideo() {

    }

    @FXML
    void handleCreateOrJoinRoom() {
        String roomName = roomInput.getText().trim();
        if (!roomName.isEmpty()) {
            chatClient.createRoom(roomName); // Server akan otomatis join jika ruang baru, atau join jika sudah ada
            roomInput.clear();
        }
    }

    @Override
    public void onMessageReceived(String sender, String message) {
        Platform.runLater(() -> {
            boolean isMe = sender.equals(userName);
            if (!isMe) {
                addMessageBubble(message, sender, false);
            }
        });
    }

    @Override
    public void onInfoReceived(String infoMessage) {
        Platform.runLater(() -> {
            Label infoLabel = new Label("--- " + infoMessage + " ---");
            infoLabel.setStyle("-fx-text-fill: #949ba4; -fx-font-size: 11; -fx-padding: 5;");
            HBox hbox = new HBox(infoLabel);
            hbox.setAlignment(Pos.CENTER);
            messageContainer.getChildren().add(hbox);
        });
    }

    @Override
    public void onRoomListUpdated(String[] rooms) {
        Platform.runLater(() -> {
            roomListContainer.getChildren().clear();
            for (String room : rooms) {
                if (room.isEmpty())
                    continue;

                String roomNameOnly = room.substring(0, room.indexOf("("));

                Label roomLabel = new Label(room);
                roomLabel.setMaxWidth(Double.MAX_VALUE);

                if (roomNameOnly.equals(currentRoom)) {
                    roomLabel.setStyle(
                            "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-cursor: hand; -fx-background-color: #404249; -fx-background-radius: 5;");
                } else {
                    roomLabel.setStyle(
                            "-fx-text-fill: #949ba4; -fx-font-weight: bold; -fx-padding: 10; -fx-cursor: hand;");

                    roomLabel.setOnMouseEntered(e -> roomLabel.setStyle(
                            "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-cursor: hand; -fx-background-color: #35373c; -fx-background-radius: 5;"));
                    roomLabel.setOnMouseExited(e -> roomLabel.setStyle(
                            "-fx-text-fill: #949ba4; -fx-font-weight: bold; -fx-padding: 10; -fx-cursor: hand;"));
                }

                roomLabel.setOnMouseClicked(event -> {
                    chatClient.joinRoom(roomNameOnly);
                });

                roomListContainer.getChildren().add(roomLabel);
            }
        });
    }

    @Override
    public void onRoomJoined(String roomName, String[] members) {
        Platform.runLater(() -> {
            currentRoom = roomName;
            currentRoomTitle.setText(roomName);
            messageContainer.getChildren().clear();

            if (memberListContainer != null) {
                memberListContainer.getChildren().clear();
            }

            chatClient.listRooms();
        });
    }

    @Override
    public void onKickedOrClosed(String reason) {
        Platform.runLater(() -> {
            currentRoom = "";
            currentRoomTitle.setText("Silakan Pilih Ruang");
            messageContainer.getChildren().clear();
            if (memberListContainer != null)
                memberListContainer.getChildren().clear();
            deleteGroupBtn.setVisible(false);
            onInfoReceived(reason);
            chatClient.listRooms();
        });
    }

    @Override
    public void onMemberListUpdated(String[] members) {
        Platform.runLater(() -> {
            if (memberListContainer == null)
                return;
            memberListContainer.getChildren().clear();

            boolean amIOwner = false;
            for (String member : members) {
                if (member.equals(userName + " (Owner)")) {
                    amIOwner = true;
                    break;
                }
            }

            // tampilkan delete group kalo owner
            if (deleteGroupBtn != null) {
                deleteGroupBtn.setVisible(amIOwner);
            }

            // 3. Tampilkan seluruh member ke panel lobi kanan
            for (String member : members) {
                if (member.isEmpty())
                    continue;

                HBox memberBox = new HBox(10);
                memberBox.setAlignment(Pos.CENTER_LEFT);

                Label memberLabel = new Label(member);
                memberLabel.setStyle("-fx-text-fill: white;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                memberBox.getChildren().addAll(memberLabel, spacer);

                // bisa kick kalo
                // owner resmi (amIOwner == true)
                // target user tersebut bukan diri sendiri
                if (amIOwner && !member.equals(userName + " (Owner)")) {
                    Button kickBtn = new Button("Kick");

                    kickBtn.setStyle(
                            "-fx-background-color: #da373c; -fx-text-fill: white; -fx-font-size: 10px; -fx-cursor: hand; -fx-background-radius: 3; -fx-padding: 3 8; -fx-font-weight: bold;");

                    // Hapus string " (Owner)"
                    String targetUser = member.replace(" (Owner)", "");
                    kickBtn.setOnAction(e -> chatClient.kickUser(targetUser));

                    memberBox.getChildren().add(kickBtn);
                }

                memberListContainer.getChildren().add(memberBox);
            }
        });
    }

    private void addMessageBubble(String message, String senderName, boolean isMe) {
        VBox bubble = new VBox();
        bubble.setMaxWidth(400);

        Label text = new Label(message);
        text.setWrapText(true);
        text.setStyle("-fx-text-fill: " + (isMe ? "white" : "#dbdee1") + ";");

        Label senderLabel = new Label(senderName);
        senderLabel.setStyle("-fx-text-fill: #949ba4; -fx-font-size: 10;");

        bubble.getChildren().addAll(senderLabel, text);

        HBox wrapper = new HBox(bubble);
        if (isMe) {
            bubble.setStyle("-fx-background-color: #5865f2; -fx-background-radius: 10 0 10 10; -fx-padding: 10;");
            wrapper.setAlignment(Pos.CENTER_RIGHT);
        } else {
            bubble.setStyle("-fx-background-color: #2b2d31; -fx-background-radius: 0 10 10 10; -fx-padding: 10;");
            wrapper.setAlignment(Pos.CENTER_LEFT);
        }

        messageContainer.getChildren().add(wrapper);
    }

    /**
     * Implement on Image or Video Received
     * 
     * @param sender
     * @param fileName
     */

    @Override
    public void onImageReceived(String sender, String filename) {
        Platform.runLater(() -> {
            boolean isMe = sender.equals(userName);
            if (!isMe) {
                addImageBubble(filename, sender, isMe);
            }
        });
    }

    @Override
    public void onVideoReceived(String sender, String filename) {
        Platform.runLater(() -> {
            boolean isMe = sender.equals(userName);
            if (!isMe) {
                addVideoBubble(filename, sender, isMe);

            }
        });
    }

    /**
     * Chat Bubble Image or Video
     * 
     * @author Pearce Nathaniel N.
     */

    private void addImageBubble(String fileName, String senderName, boolean isMe) {
        addMessageBubble("[IMAGE] " + fileName, senderName, isMe);
    };

    private void addVideoBubble(String fileName, String senderName, boolean isMe) {
        addMessageBubble("[VIDEO] " + fileName, senderName, isMe);
    }
}

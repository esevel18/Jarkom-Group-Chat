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
import java.io.IOException;

// Render Image & Video
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

// Generic File Transfer
import java.awt.Desktop;

import client.ChatClient;
import client.ChatHistoryLogger;
import client.FileTransferClient;

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
    // IP komputer for LAN test
    private String serverIp = "localhost";

    // Set client saat login dilakuakan
    public void setClient(ChatClient client, String userName, String serverIp) {
        this.chatClient = client;
        this.userName = userName;
        this.serverIp = serverIp;
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
            ChatHistoryLogger.logMessage(userName, currentRoom, msg);

            addMessageBubble(msg, "Saya", true);
            messageInput.clear();
        }
    }

    /**
     * Handle Send Image or Video
     * 
     * @author Pearce Nathaniel N.
     */
    @FXML
    void handleSendImage() {
        if (currentRoom.isEmpty()) {
            return;
        }

        FileChooser chooser = new FileChooser();

        chooser.setTitle("Select Image");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Image Files",
                "*.png",
                "*.jpg",
                "*.jpeg",
                "*.gif"));

        File selectedFile = chooser.showOpenDialog(messageContainer.getScene().getWindow());

        if (selectedFile == null) {
            return;
        }

        try {
            FileTransferClient transferClient = new FileTransferClient("localhost");
            transferClient.uploadImage(selectedFile);

            chatClient.sendImage(selectedFile.getName());
            addImageBubble(selectedFile.getName(), "Saya", true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleSendVideo() {
        if (currentRoom.isEmpty()) {
            return;
        }

        FileChooser chooser = new FileChooser();

        chooser.setTitle("Select Video");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Video Files",
                "*.mp4",
                "*.mov",
                "*.avi",
                "*.mkv"));

        File selectedFile = chooser.showOpenDialog(messageContainer.getScene().getWindow());

        if (selectedFile == null) {
            return;
        }

        try {
            FileTransferClient transferClient = new FileTransferClient(serverIp);
            transferClient.uploadVideo(selectedFile);

            chatClient.sendVideo(selectedFile.getName());
            addVideoBubble(selectedFile.getName(), "Saya", true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleSendFile() {
        if (currentRoom.isEmpty())
            return;

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Any File");
        // No extension filters = allows all files (*.*)

        File selectedFile = chooser.showOpenDialog(messageContainer.getScene().getWindow());
        if (selectedFile == null)
            return;

        try {
            FileTransferClient transferClient = new FileTransferClient(serverIp);
            // We pass the file to the general upload mechanism
            transferClient.uploadFile(selectedFile, "file");

            // Send the file announcement over the chat control port
            chatClient.sendFile(selectedFile.getName());

            // Render local bubble instantly from disk
            addGenericFileBubble(selectedFile, selectedFile.getName(), "Saya", true, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    public void onFileReceived(String sender, String filename) {
        Platform.runLater(() -> {
            if (!sender.equals(userName)) {
                File localFile = new File("client_cache/file/ " + filename);
                addGenericFileBubble(localFile, filename, sender, false, true);
            }
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
        // addMessageBubble("[IMAGE] " + fileName, senderName, isMe);
        File localFile = new File("client_cache/image/" + fileName);

        new Thread(() -> {
            boolean success = true;
            try {
                success = FileTransferClient.downloadFile(serverIp, "image", fileName, localFile);
            } catch (Exception e) {
                System.err.println("Gagal mengunduh gambar: " + e.getMessage());
                success = false;
            }

            final boolean downloadSuccess = success;

            Platform.runLater(() -> {
                VBox bubble = new VBox();
                bubble.setMaxWidth(400);

                Label senderLabel = new Label(senderName);
                senderLabel.setStyle("-fx-text-fill: #949ba4; -fx-font-size: 10;");
                bubble.getChildren().add(senderLabel);

                if (downloadSuccess && localFile.exists()) {
                    Image image = new Image(localFile.toURI().toString());
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(250);
                    imageView.setPreserveRatio(true);
                    imageView.setStyle("-fx-background-radius: 10;");
                    bubble.getChildren().add(imageView);
                } else {
                    Label errorLabel = new Label("[Gagal mengunduh gambar]");
                    errorLabel.setStyle("-fx-text-fill: #ed4245;");
                    bubble.getChildren().add(errorLabel);
                }

                styleAndAppendBubble(bubble, isMe);
            });
        }).start();
    }

    private void addVideoBubble(String fileName, String senderName, boolean isMe) {
        // addMessageBubble("[VIDEO] " + fileName, senderName, isMe);
        File localFile = new File("client_cache/video/" + fileName);

        new Thread(() -> {
            boolean success = true;
            try {
                success = FileTransferClient.downloadFile(serverIp, "video", fileName, localFile);
            } catch (Exception e) {
                System.err.println("Gagal mengunduh video: " + e.getMessage());
                success = false;
            }

            final boolean downloadSuccess = success;

            Platform.runLater(() -> {
                VBox bubble = new VBox();
                bubble.setMaxWidth(400);

                Label senderLabel = new Label(senderName);
                senderLabel.setStyle("-fx-text-fill: #949ba4; -fx-font-size: 10;");
                bubble.getChildren().add(senderLabel);

                if (downloadSuccess && localFile.exists()) {
                    Media media = new Media(localFile.toURI().toString());
                    MediaPlayer mediaPlayer = new MediaPlayer(media);
                    MediaView mediaView = new MediaView(mediaPlayer);
                    mediaView.setFitWidth(250);
                    mediaView.setPreserveRatio(true);

                    Button playBtn = new Button("▶ Play");
                    playBtn.setStyle(
                            "-fx-background-color: #23a55a; -fx-text-fill: white; -fx-font-size: 10; -fx-cursor: hand;");

                    mediaPlayer.setOnEndOfMedia(() -> {
                        mediaPlayer.seek(javafx.util.Duration.ZERO);
                        mediaPlayer.pause();
                        playBtn.setText("▶ Play");
                    });
                    playBtn.setOnAction(e -> {
                        MediaPlayer.Status status = mediaPlayer.getStatus();
                        if (status == MediaPlayer.Status.PLAYING) {
                            mediaPlayer.pause();
                            playBtn.setText("▶ Play");
                        } else {
                            mediaPlayer.play();
                            playBtn.setText("⏸ Pause");
                        }
                    });

                    HBox controls = new HBox(playBtn);
                    controls.setAlignment(Pos.CENTER);
                    controls.setStyle("-fx-padding: 5 0 0 0;");

                    bubble.getChildren().addAll(mediaView, controls);
                } else {
                    Label errorLabel = new Label("[Gagal mengunduh video]");
                    errorLabel.setStyle("-fx-text-fill: #ed4245;");
                    bubble.getChildren().add(errorLabel);
                }

                styleAndAppendBubble(bubble, isMe);
            });
        }).start();
    }

    // DRY Helper for Bubble Styling
    private void styleAndAppendBubble(VBox bubble, boolean isMe) {
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

    private void addGenericFileBubble(File targetFile, String fileName, String senderName, boolean isMe,
            boolean requiresDownload) {
        new Thread(() -> {
            boolean success = true;
            if (requiresDownload && !targetFile.exists()) {
                try {
                    // Requests the server to pull from uploads/file/
                    success = FileTransferClient.downloadFile(serverIp, "file", fileName, targetFile);
                } catch (Exception e) {
                    System.err.println("Gagal mengunduh file: " + e.getMessage());
                    success = false;
                }
            }

            final boolean downloadSuccess = success;

            Platform.runLater(() -> {
                VBox bubble = new VBox(5);
                bubble.setMaxWidth(300);

                Label senderLabel = new Label(senderName);
                senderLabel.setStyle("-fx-text-fill: #949ba4; -fx-font-size: 10;");
                bubble.getChildren().add(senderLabel);

                if (downloadSuccess && targetFile.exists()) {
                    // Build a file box container
                    HBox fileCard = new HBox(10);
                    fileCard.setAlignment(Pos.CENTER_LEFT);
                    fileCard.setStyle("-fx-background-color: #1e1f22; -fx-padding: 10; -fx-background-radius: 5;");

                    // File Icon Label
                    Label fileIcon = new Label("📄");
                    fileIcon.setStyle("-fx-font-size: 20;");

                    // File Details
                    VBox fileDetails = new VBox(2);
                    Label nameLabel = new Label(fileName);
                    nameLabel.setStyle("-fx-text-fill: #00a8fc; -fx-font-weight: bold; -fx-underline: true;");

                    // Display file size in KB smoothly
                    long fileSizeKB = targetFile.length() / 1024;
                    Label sizeLabel = new Label(fileSizeKB + " KB");
                    sizeLabel.setStyle("-fx-text-fill: #949ba4; -fx-font-size: 10;");

                    fileDetails.getChildren().addAll(nameLabel, sizeLabel);
                    fileCard.getChildren().addAll(fileIcon, fileDetails);

                    // Click Action: Open the file using the OS default application!
                    fileCard.setCursor(javafx.scene.Cursor.HAND);
                    fileCard.setOnMouseClicked(e -> {
                        try {
                            if (Desktop.isDesktopSupported()) {
                                Desktop.getDesktop().open(targetFile);
                            }
                        } catch (IOException ex) {
                            System.err.println("Tidak dapat membuka file: " + ex.getMessage());
                        }
                    });

                    bubble.getChildren().add(fileCard);
                } else {
                    Label errorLabel = new Label("[Gagal memuat dokumen]");
                    errorLabel.setStyle("-fx-text-fill: #ed4245;");
                    bubble.getChildren().add(errorLabel);
                }

                styleAndAppendBubble(bubble, isMe);
            });
        }).start();
    }
}

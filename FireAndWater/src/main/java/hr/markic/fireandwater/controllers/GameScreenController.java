package hr.markic.fireandwater.controllers;

import hr.markic.fireandwater.GameApplication;
import hr.markic.fireandwater.chat.ChatClient;
import hr.markic.fireandwater.model.Player;
import hr.markic.fireandwater.model.PlayerType;
import hr.markic.fireandwater.multicast.ClientThread;
import hr.markic.fireandwater.multicast.ServerThread;
import hr.markic.fireandwater.utils.InfoUtil;
import hr.markic.fireandwater.utils.SceneUtil;
import hr.markic.fireandwater.utils.SerializationFileUtil;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static hr.markic.fireandwater.GameApplication.mainStage;

public class GameScreenController implements Initializable {

    public static GameScreenController getInstance() {
        if (INSTANCE == null) {
            throw new RuntimeException("Controller not created");
        }
        return INSTANCE;
    }

    public String EndText = "";

    public boolean isMultiplayer() {
        return multiplayer;
    }

    public void setMultiplayer(boolean multiplayer) {
        this.multiplayer = multiplayer;
    }

    private boolean multiplayer;

    private boolean replay;

    private boolean isReplay() {
        return replay;
    }

    private double MAX_JUMP_BOY;
    private double MAX_JUMP_GIRL;

    private static GameScreenController INSTANCE;

    @FXML
    private Group floorGroup;

    @FXML
    private Group diamondGroup;

    @FXML
    private ImageView ivFire;

    @FXML
    private ImageView ivWater;

    @FXML
    private ImageView ivSlime;

    @FXML
    private ImageView ivEndFloor;

    @FXML
    private ImageView ivBoy;

    @FXML
    private ImageView ivGirl;

    @FXML
    private ImageView ivFirstRedDiamond;

    @FXML
    private ImageView ivFirstBlueDiamond;

    @FXML
    private ImageView ivSecondRedDiamond;

    @FXML
    private ImageView ivSecondBlueDiamond;

    @FXML
    private Label lblRedDiamonds;

    @FXML
    private Label lblBlueDiamonds;

    @FXML
    private TextField tfMessage;

    @FXML
    private TextArea taChat;

    @FXML
    private Button btnSend;

    @FXML
    private AnchorPane anchorPane;

    private final Map<String, Image> boyImages = new HashMap<>();

    private final Map<String, Image> girlImages = new HashMap<>();

    private final Map<String, Image> mapImages = new HashMap<>();

    private final List<Double> coordinatesXOfPlayer = new ArrayList<>();
    private final List<Double> coordinatesYOfPlayer = new ArrayList<>();

    private final List<Player> playersStates = new ArrayList<>();

    private Player boyPlayer;

    private Player girlPlayer;

    private String nameOfPlayer;

    private boolean pause;

    private ClientThread clientThread;
    private ServerThread serverThread;
    private ChatClient chatClient;

    private Document xmlDocument;

    private Element rootElement;




    private void openAlert() {
        Alert alert = InfoUtil.showAlert(
                Alert.AlertType.CONFIRMATION,
                "Options",
                "",
                "",
                new ButtonType("Resume", ButtonBar.ButtonData.OK_DONE),
                new ButtonType("Save game", ButtonBar.ButtonData.OK_DONE),
                new ButtonType("Home Screen", ButtonBar.ButtonData.OK_DONE));

        pause = true;

        alert.showAndWait().ifPresent(response -> {
            if (response.getText().equals("Home Screen")) {
                if (clientThread != null) {
                    clientThread.stop();
                }
                if (serverThread != null) {
                    serverThread.stop();
                }
                if (isMultiplayer() == false){
                    saveReplayToXml();
                }

                SceneUtil.loadNewScene("homeScreen.fxml");
                mainStage.getScene().getStylesheets().add(this.getClass().getResource("/toggleButtons.css").toExternalForm());
            } else if (response.getText().equals("Save game")) {

                prepareForFile(boyPlayer, ivBoy);
                prepareForFile(girlPlayer, ivGirl);
                SerializationFileUtil.writeToFile(Arrays.asList(boyPlayer, girlPlayer));
            }
        });

        pause = false;
    }

    private void prepareForFile(Player player, ImageView ivPlayer) {
        player.setPositionX(ivPlayer.getX());
        player.setPositionY(ivPlayer.getY());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        INSTANCE = this;
        loadPlayers();
        loadImages();
        setMapAnimations();
        setLoopingThread();
        enableChatProperties(false);
        if (isReplay() == false && isMultiplayer() == false){
            initXmlDocument();
        }
    }

    private void initXmlDocument() {
        try {

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

            DocumentBuilder documentBuilder =  documentBuilderFactory.newDocumentBuilder();

            xmlDocument = documentBuilder.newDocument();
            rootElement = xmlDocument.createElement("Replay");
            xmlDocument.appendChild(rootElement);

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    private void initChatConnection() {
        chatClient = new ChatClient();
        chatClient.setDaemon(true);
        chatClient.start();
    }

    private void initChat() {
        if (serverThread.getServerSocket().getLocalPort() == ServerThread.DEFAULT_PORT) {
            nameOfPlayer = "Boy";
        } else {
            nameOfPlayer = "Girl";
        }
    }

    public void sendMessage() {

        initChat();

        if (!tfMessage.getText().isEmpty()) {
            forwardMessage();
        }
        anchorPane.requestFocus();
    }

    private void forwardMessage() {

        tfMessage.setDisable(true);
        btnSend.setDisable(true);

        taChat.setText(taChat.getText() + nameOfPlayer + ": " + tfMessage.getText() + "\n");

        new Thread(() -> {
            try {
                chatClient.sendMessage(tfMessage.getText());
            } catch (IOException e) {
                System.exit(0);
            }
        }).start();

    }

    public void getAnswer(String answer) {

        String name = nameOfPlayer.equals("Boy") ? "Girl" : "Boy";

        taChat.setText(taChat.getText() + name + ": " + answer + "\n");
        tfMessage.setText("");

        tfMessage.setDisable(false);
        btnSend.setDisable(false);

    }

    private void initClientThread() {
        clientThread = new ClientThread(this);
        clientThread.setDaemon(true);
        clientThread.start();
    }

    private void initServerThread() {
        //See how to fix this
        serverThread = new ServerThread();
        serverThread.setDaemon(true);
        serverThread.start();
    }

    public void loadGame() {

        if (isMultiplayer()) {
            initClientThread();
            initServerThread();
            initChatConnection();
            enableChatProperties(true);

        }

        if (SerializationFileUtil.isFileExists() && isMultiplayer() == false) {
            for (Object object : SerializationFileUtil.readFromFile()) {
                Player player = (Player) object;
                deserializePlayer(player);
                diamondGroup.getChildren().removeIf(node -> player.getDiamonds().contains(node.getId()));
            }
            setMaxJump();
        }
    }

    private void enableChatProperties(Boolean enable) {

        if (enable) {
            anchorPane.getChildren().add(taChat);
            anchorPane.getChildren().add(btnSend);
            anchorPane.getChildren().add(tfMessage);
        } else {
            anchorPane.getChildren().remove(taChat);
            anchorPane.getChildren().remove(btnSend);
            anchorPane.getChildren().remove(tfMessage);
        }

    }

    private void deserializePlayer(Player player) {
        if (player.getType() == PlayerType.BOY) {
            boyPlayer = player;
            ivBoy.setX(player.getPositionX());
            ivBoy.setY(player.getPositionY());
            lblRedDiamonds.setText(String.valueOf(player.getDiamonds().size()));

        } else {
            girlPlayer = player;
            ivGirl.setX(player.getPositionX());
            ivGirl.setY(player.getPositionY());
            lblBlueDiamonds.setText(String.valueOf(player.getDiamonds().size()));
        }
    }

    private void setLoopingThread() {
        Thread loopingThread = new Thread(() -> {
            boolean shutdown = false;
            while (!shutdown) {

                if (!pause) {
                    Platform.runLater(() -> {
                        gameGravity(ivBoy, boyPlayer);
                        gameGravity(ivGirl, girlPlayer);
                    });
                }
                checkDiamonds();
                if (isGameOver() && replay == false) {
                    Platform.runLater(() -> {

                        EndText = "You lost :(";
                        saveReplayToXml();

                        SceneUtil.loadNewScene("endScreen.fxml");
                        mainStage.getScene().getStylesheets().add(this.getClass().getResource("/toggleButtons.css").toExternalForm());
                    });
                    shutdown = true;
                }
                if (isGameFinished() && replay == false) {

                    saveReplayToXml();

                    Platform.runLater(() -> {
                        EndText = "You win :) \nCollected \nDiamonds: " + (
                                Integer.parseInt(lblBlueDiamonds.getText())
                                        + Integer.parseInt(lblRedDiamonds.getText()));
                        SceneUtil.loadNewScene("endScreen.fxml");
                        mainStage.getScene().getStylesheets().add(this.getClass().getResource("/toggleButtons.css").toExternalForm());
                    });
                    shutdown = true;
                }
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        loopingThread.setDaemon(true);
        loopingThread.start();
    }

    private void saveReplayToXml() {
        try {

            Transformer transformer
                    = TransformerFactory.newInstance().newTransformer();

            Source xmlSource = new DOMSource(xmlDocument);
            Result xmlResult = new StreamResult(new File("replay.xml"));

            transformer.transform(xmlSource, xmlResult);

        } catch (Exception ex) {
            Logger.getLogger(GameScreenController.class.getName()).log(
                    Level.SEVERE, null, ex);
        }

    }

    public void readReplayFromXml() {

        replay = true;
        try {
            File replayFile = new File("replay.xml");

            DocumentBuilder parser =
                    DocumentBuilderFactory.newInstance().newDocumentBuilder();

            Document xmlDocument = parser.parse(replayFile);

            NodeList nodeList = xmlDocument.getElementsByTagName("Player");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node playerNode = nodeList.item(i);
                Player newPlayer = new Player();

                if (playerNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element playerElement = (Element) playerNode;

                    String nameOfPlayer = playerElement
                            .getElementsByTagName("Name")
                            .item(0)
                            .getTextContent();

                    newPlayer.setName(nameOfPlayer);

                    String typeOfPlayer = playerElement
                            .getElementsByTagName("Type")
                            .item(0)
                            .getTextContent();

                    newPlayer.setType(PlayerType.valueOf(typeOfPlayer));

                    String positionXOfivPlayer = playerElement
                            .getElementsByTagName("PositionX")
                            .item(0)
                            .getTextContent();


                    String positionYOfivPlayer = playerElement
                            .getElementsByTagName("PositionY")
                            .item(0)
                            .getTextContent();


                    int numberOfDiamonds = playerElement.getElementsByTagName("Diamonds").item(0).getChildNodes().getLength();

                    for (int j = 0; j < numberOfDiamonds; j++) {
                        String diamond = playerElement
                                .getElementsByTagName("Diamonds")
                                .item(0)
                                .getChildNodes()
                                .item(j)
                                .getTextContent();
                        newPlayer.getDiamonds().add(diamond);
                    }
                    saveGameState(newPlayer, Double.parseDouble(positionXOfivPlayer), Double.parseDouble(positionYOfivPlayer));
                }
            }

            Timeline replayTimeline = new Timeline();


            int speedCoefficient = playersStates.size() > 10 ? 50 : 150;
            System.out.println("Koeficijent brzine je: " + speedCoefficient);
            System.out.println("Velicina zapisa je: " + playersStates.size());

            for (int i = 0; i < playersStates.size(); i++) {

                final int index = i;

                replayTimeline.getKeyFrames().add(new KeyFrame(Duration.millis((i + 1) * speedCoefficient), (ActionEvent event) -> {

                    if (playersStates.get(index).getType() == PlayerType.BOY) {

                        ivBoy.setX(coordinatesXOfPlayer.get(index));
                        ivBoy.setY(coordinatesYOfPlayer.get(index));

                    } else {
                        ivGirl.setX(coordinatesXOfPlayer.get(index));
                        ivGirl.setY(coordinatesYOfPlayer.get(index));
                    }
                }));
            }
            replayTimeline.setAutoReverse(false);
            replayTimeline.play();
            replayTimeline.setOnFinished(actionEvent -> {
                SceneUtil.loadNewScene("homeScreen.fxml");
                GameApplication.mainStage.getScene().getStylesheets().add(this.getClass().getResource("/toggleButtons.css").toExternalForm());
            });

            System.out.println("Ocitavanje replaya uspjesno!");
        } catch (Exception ex) {
            System.out.println("Došlo je do pogreške kod ocitavanja replaya!");
            Logger.getLogger(GameScreenController.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
    }

    private void checkDiamonds() {
        removeDiamonds(girlPlayer, ivGirl, ivFirstBlueDiamond, ivSecondBlueDiamond);
        removeDiamonds(boyPlayer, ivBoy, ivFirstRedDiamond, ivSecondRedDiamond);
    }

    private void removeDiamonds(Player player, ImageView playerPicture, ImageView ivFirstDiamond, ImageView ivSecondDiamond) {
        removeDiamond(player, playerPicture, ivFirstDiamond);
        removeDiamond(player, playerPicture, ivSecondDiamond);
    }

    private void removeDiamond(Player player, ImageView playerPicture, ImageView ivDiamond) {
        if (playerPicture.getBoundsInParent().intersects(ivDiamond.getBoundsInParent())
                && diamondGroup.getChildren().contains(ivDiamond)) {
            Platform.runLater(() -> {
                diamondGroup.getChildren().remove(ivDiamond);
                player.getDiamonds().add(ivDiamond.getId());
                if (player.getType().equals(PlayerType.BOY)) {
                    int numberOfCollectedDiamonds = Integer.parseInt(lblRedDiamonds.getText());
                    numberOfCollectedDiamonds++;
                    lblRedDiamonds.setText(Integer.toString(numberOfCollectedDiamonds));
                } else {
                    int numberOfCollectedDiamonds = Integer.parseInt(lblBlueDiamonds.getText());
                    numberOfCollectedDiamonds++;
                    lblBlueDiamonds.setText(Integer.toString(numberOfCollectedDiamonds));
                }
            });

        }
    }

    private void gameGravity(ImageView playerPicture, Player player) {
        if (!isPlayerOnFloor(playerPicture)) {

            if (replay == false) {
                playerPicture.setY(playerPicture.getY() + 6);
                if (player.getType() == PlayerType.BOY) {

                    saveGameState(player, ivBoy.getX(), ivBoy.getY());
                } else {
                    saveGameState(player, ivGirl.getX(), ivGirl.getY());
                }
            }
        }
    }

    private boolean isGameFinished() {
        boolean gameFinished = ivBoy.getBoundsInParent().intersects(ivEndFloor.getBoundsInParent());
        gameFinished |= ivGirl.getBoundsInParent().intersects(ivEndFloor.getBoundsInParent());
        return gameFinished;
    }

    private void setMapAnimations() {
        Timeline timeline = new Timeline();

        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(150), "left", (ActionEvent event) -> {
            ivWater.setImage(mapImages.get("waterLeft"));
            ivFire.setImage(mapImages.get("fireLeft"));
            ivSlime.setImage(mapImages.get("slimeLeft"));
        }));

        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(300), "neutral", (ActionEvent event) -> {
            ivWater.setImage(mapImages.get("waterNeutral"));
            ivFire.setImage(mapImages.get("fireNeutral"));
            ivSlime.setImage(mapImages.get("slimeNeutral"));
        }));

        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(450), "right", (ActionEvent event) -> {
            ivWater.setImage(mapImages.get("waterRight"));
            ivFire.setImage(mapImages.get("fireRight"));
            ivSlime.setImage(mapImages.get("slimeRight"));
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void loadPlayers() {

        boyPlayer = new Player(PlayerType.BOY, "Fire");
        girlPlayer = new Player(PlayerType.GIRL, "Water");

        setMaxJump();
    }

    private void setMaxJump() {
        MAX_JUMP_BOY = ivBoy.getY() - 50;
        MAX_JUMP_GIRL = ivGirl.getY() - 50;
    }

    private boolean isGameOver() {
       return ivBoy.getBoundsInParent().intersects(ivWater.getBoundsInParent())
                || ivBoy.getBoundsInParent().intersects(ivSlime.getBoundsInParent())
                || ivGirl.getBoundsInParent().intersects(ivFire.getBoundsInParent())
                || ivGirl.getBoundsInParent().intersects(ivSlime.getBoundsInParent());
    }

    public void sceneListeners(KeyEvent keyEvent) {

        if (multiplayer) {
            if (serverThread.getServerSocket().getLocalPort() == ServerThread.DEFAULT_PORT) {
                updateData(boyPlayer, ivBoy);
                listenersForBoy(keyEvent);
            } else {
                updateData(girlPlayer, ivGirl);
                listenersForGirl(keyEvent);

            }
        } else {
            if (keyEvent.getCode() == KeyCode.ESCAPE)
                openAlert();

            listenersForBoy(keyEvent);
            listenersForGirl(keyEvent);
        }

    }

    private void listenersForGirl(KeyEvent keyEvent) {

        //Girl
        if (keyEvent.getCode() == KeyCode.D) {
            playerMoving(6, girlPlayer, ivGirl, "girlGoingRight", "girlStraightRight");

            saveGameState(girlPlayer, ivGirl.getX(), ivGirl.getY());

        } else if (keyEvent.getCode() == KeyCode.A) {
            playerMoving(-6, girlPlayer, ivGirl, "girlGoingLeft", "girlStraightLeft");

            saveGameState(girlPlayer, ivGirl.getX(), ivGirl.getY());

        } else if (keyEvent.getCode() == KeyCode.W) {
            if (ivGirl.getY() > MAX_JUMP_GIRL) {
                playerJumping(ivGirl, girlPlayer);

                saveGameState(girlPlayer, ivGirl.getX(), ivGirl.getY());
            }
        }
    }

    private void listenersForBoy(KeyEvent keyEvent) {

        //Boy
        if (keyEvent.getCode() == KeyCode.RIGHT) {
            playerMoving(6, boyPlayer, ivBoy, "boyGoingRight", "boyStraightRight");

            saveGameState(boyPlayer, ivBoy.getX(), ivBoy.getY());

        } else if (keyEvent.getCode() == KeyCode.LEFT) {
            playerMoving(-6, boyPlayer, ivBoy, "boyGoingLeft", "boyStraightLeft");

            saveGameState(boyPlayer, ivBoy.getX(), ivBoy.getY());
        } else if (keyEvent.getCode() == KeyCode.UP) {
            if (ivBoy.getY() > MAX_JUMP_BOY) {
                playerJumping(ivBoy, boyPlayer);

                saveGameState(boyPlayer, ivBoy.getX(), ivBoy.getY());
            }
        }
    }

    private void updateData(Player player, ImageView ivPlayer) {

        player.setPositionX(ivPlayer.getX());
        player.setPositionY(ivPlayer.getY());

        serverThread.trigger(player);
    }

    private void playerJumping(ImageView playerPicture, Player player) {
        //Jumping
        TranslateTransition animation =
                new TranslateTransition(Duration.millis(300), playerPicture);
        animation.setFromY(0);
        animation.setToY(-6);
        animation.play();
        animation.setOnFinished(actionEvent -> {

            playerPicture.setY(playerPicture.getY() - 6);
            player.setPositionY(playerPicture.getY());
        });
    }

    public void playerMoving(int shift, Player player, ImageView playerPicture, String nameOfImageGoing, String nameOfImageStraight) {


        boolean canPlayerMove = shift > 0 ? playerPicture.getX() + 86 < 1280 : playerPicture.getX() - 6 > 0;

        if (!(playerPicture.getY() <= 238) || !canPlayerMove)
            return;

        //Player walking
        playerPicture.setX(playerPicture.getX() + shift);


        if (player.getType() == PlayerType.BOY) {
            setKeyFramesForWalking(playerPicture, nameOfImageGoing, nameOfImageStraight, boyImages);
        } else {
            setKeyFramesForWalking(playerPicture, nameOfImageGoing, nameOfImageStraight, girlImages);
        }
        setAnimationForWalking(playerPicture, shift);


    }

    private void setKeyFramesForWalking(ImageView playerPicture, String nameOfImageGoing, String nameOfImageStraight, Map<String, Image> images) {

        Timeline playerTimeline = new Timeline();

        playerTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(100), nameOfImageGoing, (ActionEvent event) ->
                playerPicture.setImage(images.get(nameOfImageGoing))));

        playerTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(200), nameOfImageStraight, (ActionEvent event) ->
                playerPicture.setImage(images.get(nameOfImageStraight))));
        playerTimeline.play();
    }

    private void setAnimationForWalking(ImageView playerPicture, int shift) {
        TranslateTransition animation =
                new TranslateTransition(Duration.millis(300), playerPicture);
        animation.setCycleCount(1);
        animation.setAutoReverse(false);
        animation.setFromX(0);
        animation.setToX(shift);
        animation.play();
    }

    private boolean isPlayerOnFloor(ImageView player) {
        return player.getBoundsInParent().intersects(floorGroup.getBoundsInParent());
    }

    private void loadImages() {
        //Images for boy
        boyImages.put("boyGoingLeft", new Image(this.getClass().getResource("/assets/boy/boyGoingLeft.png").toExternalForm()));
        boyImages.put("boyGoingRight", new Image(this.getClass().getResource("/assets/boy/boyGoingRight.png").toExternalForm()));
        boyImages.put("boyStraightLeft", new Image(this.getClass().getResource("/assets/boy/boyStraightLeft.png").toExternalForm()));
        boyImages.put("boyStraightRight", new Image(this.getClass().getResource("/assets/boy/boyStraightRight.png").toExternalForm()));


        //Images for girl

        girlImages.put("girlGoingLeft", new Image(this.getClass().getResource("/assets/girl/girlGoingLeft.png").toExternalForm()));
        girlImages.put("girlGoingRight", new Image(this.getClass().getResource("/assets/girl/girlGoingRight.png").toExternalForm()));
        girlImages.put("girlStraightLeft", new Image(this.getClass().getResource("/assets/girl/girlStraightLeft.png").toExternalForm()));
        girlImages.put("girlStraightRight", new Image(this.getClass().getResource("/assets/girl/girlStraightRight.png").toExternalForm()));

        //Images for map
        mapImages.put("blueDiamond", new Image(this.getClass().getResource("/assets/map/blueDiamond.png").toExternalForm()));
        mapImages.put("redDiamond", new Image(this.getClass().getResource("/assets/map/redDiamond.png").toExternalForm()));

        mapImages.put("fireLeft", new Image(this.getClass().getResource("/assets/map/fireLeft.png").toExternalForm()));
        mapImages.put("fireNeutral", new Image(this.getClass().getResource("/assets/map/fireNeutral.png").toExternalForm()));
        mapImages.put("fireRight", new Image(this.getClass().getResource("/assets/map/fireRight.png").toExternalForm()));

        mapImages.put("slimeLeft", new Image(this.getClass().getResource("/assets/map/slimeLeft.png").toExternalForm()));
        mapImages.put("slimeNeutral", new Image(this.getClass().getResource("/assets/map/slimeNeutral.png").toExternalForm()));
        mapImages.put("slimeRight", new Image(this.getClass().getResource("/assets/map/slimeRight.png").toExternalForm()));

        mapImages.put("waterLeft", new Image(this.getClass().getResource("/assets/map/waterLeft.png").toExternalForm()));
        mapImages.put("waterNeutral", new Image(this.getClass().getResource("/assets/map/waterNeutral.png").toExternalForm()));
        mapImages.put("waterRight", new Image(this.getClass().getResource("/assets/map/waterRight.png").toExternalForm()));
    }

    public void setPlayer(Player player) {
        deserializePlayer(player);
    }

    private void saveGameState(Player player, double positionX, double positionY) {
        // TODO: 19/01/2022 Namjestiti da gleda x i y os, a ne cijeli item

        if (replay == false){
            savePositionToXml(player);
        }else{
            playersStates.add(player);
            coordinatesXOfPlayer.add(positionX);
            coordinatesYOfPlayer.add(positionY);
        }


    }

    private void savePositionToXml(Player player) {

        Element playerElement
                = xmlDocument.createElement("Player");
        rootElement.appendChild(playerElement);

        Element nameElement
                = xmlDocument.createElement("Name");
        Node nameTextNode = xmlDocument.createTextNode(player.getName());
        nameElement.appendChild(nameTextNode);
        playerElement.appendChild(nameElement);

        Element typeElement
                = xmlDocument.createElement("Type");
        Node typeTextNode = xmlDocument.createTextNode(player.getType().toString());
        typeElement.appendChild(typeTextNode);
        playerElement.appendChild(typeElement);

        Element positionXElement
                = xmlDocument.createElement("PositionX");
        Node positionXTextNode;

        if (player.getType() == PlayerType.BOY) {
            positionXTextNode = xmlDocument.createTextNode(String.valueOf(ivBoy.getX()));
        } else {
            positionXTextNode = xmlDocument.createTextNode(String.valueOf(ivGirl.getX()));
        }
        positionXElement.appendChild(positionXTextNode);
        playerElement.appendChild(positionXElement);

        Element positionYElement
                = xmlDocument.createElement("PositionY");
        Node positionYTextNode;

        if (player.getType() == PlayerType.BOY) {

            positionYTextNode = xmlDocument.createTextNode(String.valueOf(ivBoy.getY()));
        } else {

            positionYTextNode = xmlDocument.createTextNode(String.valueOf(ivGirl.getY()));
        }

        positionYElement.appendChild(positionYTextNode);
        playerElement.appendChild(positionYElement);


        Element listOfDiamondsElement
                = xmlDocument.createElement("Diamonds");
        playerElement.appendChild(listOfDiamondsElement);

        for (String diamond : player.getDiamonds()) {

            Element diamondElement
                    = xmlDocument.createElement("Diamond");
            Node diamondTextNode = xmlDocument.createTextNode(diamond);
            diamondElement.appendChild(diamondTextNode);
            listOfDiamondsElement.appendChild(diamondElement);
        }

    }
}

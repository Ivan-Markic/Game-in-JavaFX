package hr.markic.fireandwater.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

public class Player implements Externalizable {

    private static final long serialVersionUID = 1L;

    private PlayerType type;
    private String name;
    private List<String> diamonds = new ArrayList<>();
    private double positionX;
    private double positionY;


    public void setPositionX(double positionX) {
        this.positionX = positionX;
    }

    public void setPositionY(double positionY) {
        this.positionY = positionY;
    }

    public String getName() {
        return name;
    }

    public PlayerType getType() {
        return type;
    }

    public List<String> getDiamonds() {
        return diamonds;
    }

    public double getPositionX() {
        return positionX;
    }

    public double getPositionY() {
        return positionY;
    }

    public Player() {

    }

    public Player(PlayerType type, String name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public String toString() {
        return "Player: " + name + ", " + type;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(type);
        out.writeUTF(name);
        out.writeObject(diamonds);
        out.writeDouble(positionX);
        out.writeDouble(positionY);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        type = (PlayerType) in.readObject();
        name = in.readUTF();
        diamonds = (List<String>) in.readObject();
        positionX = in.readDouble();
        positionY = in.readDouble();
    }
}

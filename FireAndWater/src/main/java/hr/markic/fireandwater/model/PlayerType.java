package hr.markic.fireandwater.model;

public enum PlayerType {

    BOY("Boy"), GIRL("Girl");

    private final String name;

    PlayerType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

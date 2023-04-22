package net.justugh.dodgeball.game;

public class GameLineData {

    private final Integer maxZ;
    private final MovementDirection direction;

    public GameLineData(Integer maxZ, MovementDirection direction) {
        this.maxZ = maxZ;
        this.direction = direction;
    }

    public Integer getMaxZ() {
        return maxZ;
    }

    public MovementDirection getDirection() {
        return direction;
    }

    public enum MovementDirection {
        POSITIVE,
        NEGATIVE;
    }

}

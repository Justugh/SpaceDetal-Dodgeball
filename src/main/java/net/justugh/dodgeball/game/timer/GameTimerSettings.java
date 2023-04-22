package net.justugh.dodgeball.game.timer;

public class GameTimerSettings {

    private int lobbyTime = 10;
    private int gameTime = 600;
    private int endTime = 10;

    private boolean paused = true;

    public int getLobbyTime() {
        return lobbyTime;
    }

    public void setLobbyTime(int lobbyTime) {
        this.lobbyTime = lobbyTime;
    }

    public int getGameTime() {
        return gameTime;
    }

    public void setGameTime(int gameTime) {
        this.gameTime = gameTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

}

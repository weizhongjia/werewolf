package com.msh.room.dto.room.state;

/**
 * Created by zhangruiqian on 2017/5/14.
 */
public class HunterState {
    private boolean alive;
    private boolean shotAvailable;

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public boolean isShotAvailable() {
        return shotAvailable;
    }

    public void setShotAvailable(boolean shotAvailable) {
        this.shotAvailable = shotAvailable;
    }
}

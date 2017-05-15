package com.msh.room.dto.room.state;

/**
 * Created by zhangruiqian on 2017/5/14.
 */
public class WitchState {
    private boolean alive;
    private boolean poisonAvailable;
    private boolean antidoteAvailable;
    private boolean saveBySelf;

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public boolean isPoisonAvailable() {
        return poisonAvailable;
    }

    public void setPoisonAvailable(boolean poisonAvailable) {
        this.poisonAvailable = poisonAvailable;
    }

    public boolean isAntidoteAvailable() {
        return antidoteAvailable;
    }

    public void setAntidoteAvailable(boolean antidoteAvailable) {
        this.antidoteAvailable = antidoteAvailable;
    }

    public boolean isSaveBySelf() {
        return saveBySelf;
    }

    public void setSaveBySelf(boolean saveBySelf) {
        this.saveBySelf = saveBySelf;
    }
}

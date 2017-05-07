package com.msh.room.dto.event;

import com.msh.room.model.role.Roles;

import java.util.Map;

/**
 * Created by zhangruiqian on 2017/5/3.
 */
public class JudgeEvent {
    private String roomCode;
    private JudgeEventType eventType;
    private Map<Roles, Integer> gameConfig;

    //默认构造方法
    public JudgeEvent() {
    }

    public JudgeEvent(String roomCode, JudgeEventType eventType) {
        this.roomCode = roomCode;
        this.eventType = eventType;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public JudgeEventType getEventType() {
        return eventType;
    }

    public void setEventType(JudgeEventType eventType) {
        this.eventType = eventType;
    }

    public Map<Roles, Integer> getGameConfig() {
        return gameConfig;
    }

    public void setGameConfig(Map<Roles, Integer> gameConfig) {
        this.gameConfig = gameConfig;
    }
}

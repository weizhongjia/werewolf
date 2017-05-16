package com.msh.room.dto.response;

import com.msh.room.dto.event.JudgeEventType;
import com.msh.room.dto.room.record.NightRecord;
import com.msh.room.dto.room.seat.PlayerSeatInfo;
import com.msh.room.dto.room.RoomStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangruiqian on 2017/5/3.
 */
public class JudgeDisplayInfo {
    private String roomCode;
    private RoomStatus status;
    private List<PlayerSeatInfo> playerSeatInfoList;
    private List<JudgeEventType> acceptableEventTypes;
    //当夜结果
    private NightRecord nightRecord;

    public JudgeDisplayInfo() {
    }

    public JudgeDisplayInfo(String roomCode) {
        this.roomCode = roomCode;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public List<PlayerSeatInfo> getPlayerSeatInfoList() {
        return playerSeatInfoList;
    }

    public void setPlayerSeatInfoList(List<PlayerSeatInfo> playerSeatInfoList) {
        this.playerSeatInfoList = playerSeatInfoList;
    }

    public void addPlayerSeatInfo(PlayerSeatInfo seatInfo) {
        if (this.playerSeatInfoList != null) {
            playerSeatInfoList = new ArrayList<>();
        }
        playerSeatInfoList.add(seatInfo);
    }

    public List<JudgeEventType> getAcceptableEventTypes() {
        return acceptableEventTypes;
    }

    public void setAcceptableEventTypes(List<JudgeEventType> acceptableEventTypes) {
        this.acceptableEventTypes = acceptableEventTypes;
    }

    public void addAcceptableEventType(JudgeEventType eventType) {
        if (acceptableEventTypes == null) {
            acceptableEventTypes = new ArrayList<>();
        }
        acceptableEventTypes.add(eventType);
    }

    public NightRecord getNightRecord() {
        return nightRecord;
    }

    public void setNightRecord(NightRecord nightRecord) {
        this.nightRecord = nightRecord;
    }
}

package com.msh.room.dto.room;

import com.msh.room.dto.room.record.NightRecord;
import com.msh.room.dto.room.seat.PlayerSeatInfo;
import com.msh.room.dto.room.state.HunterState;
import com.msh.room.dto.room.state.MoronState;
import com.msh.room.dto.room.state.WitchState;
import com.msh.room.exception.RoomBusinessException;
import com.msh.room.model.role.Roles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangruiqian on 2017/5/3.
 */
public class RoomStateData {
    private String roomCode;
    private RoomStatus status;
    private List<PlayerSeatInfo> playerSeatInfo;
    private Map<Roles, Integer> gameConfig;

    private List<NightRecord> nightRecordList;

    private WitchState witchState;
    private MoronState moronState;
    private HunterState hunterState;


    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    public List<PlayerSeatInfo> getPlayerSeatInfo() {
        return playerSeatInfo;
    }

    public void setPlayerSeatInfo(List<PlayerSeatInfo> playerSeatInfo) {
        this.playerSeatInfo = playerSeatInfo;
    }

    public void addPlaySeatInfo(PlayerSeatInfo seatInfo) {
        if (playerSeatInfo == null) {
            playerSeatInfo = new ArrayList<>();
        }
        playerSeatInfo.add(seatInfo);
    }

    public PlayerSeatInfo getPlaySeatInfoBySeatNumber(int number) {
        if (number < 1 || number > getPlayerSeatInfo().size()) {
            throw new RoomBusinessException("座位号非法，无法获取玩家信息");
        }
        return getPlayerSeatInfo().get(number - 1);
    }

    public Map<Roles, Integer> getGameConfig() {
        return gameConfig;
    }

    public void setGameConfig(Map<Roles, Integer> gameConfig) {
        this.gameConfig = gameConfig;
    }

    public WitchState getWitchState() {
        return witchState;
    }

    public void setWitchState(WitchState witchState) {
        this.witchState = witchState;
    }

    public MoronState getMoronState() {
        return moronState;
    }

    public void setMoronState(MoronState moronState) {
        this.moronState = moronState;
    }

    public HunterState getHunterState() {
        return hunterState;
    }

    public void setHunterState(HunterState hunterState) {
        this.hunterState = hunterState;
    }

    public List<NightRecord> getNightRecordList() {
        return nightRecordList;
    }

    public void setNightRecordList(List<NightRecord> nightRecordList) {
        this.nightRecordList = nightRecordList;
    }

    public void addNightRecord(NightRecord record) {
        if (this.nightRecordList == null) {
            this.nightRecordList = new ArrayList<>();
        }
        this.nightRecordList.add(record);
    }

    public NightRecord getLastNightRecord() {
        int size = this.nightRecordList.size();
        return this.nightRecordList.get(size - 1);
    }

    //没有则为0 死亡则为-1
    public int getAliveSeatByRole(Roles role) {
        for (PlayerSeatInfo seatInfo : playerSeatInfo) {
            if (seatInfo.getRole().equals(role)) {
                if (seatInfo.isAlive())
                    return seatInfo.getSeatNumber();
                else {
                    return -1;
                }
            }
        }
        return 0;
    }
}

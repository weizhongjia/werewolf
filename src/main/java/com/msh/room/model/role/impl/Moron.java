package com.msh.room.model.role.impl;

import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.dto.room.RoomStatus;
import com.msh.room.dto.room.result.GameResult;
import com.msh.room.dto.room.seat.PlayerSeatInfo;
import com.msh.room.dto.room.state.MoronState;

/**
 * Created by zhangruiqian on 2017/5/7.
 */
public class Moron extends AssignedPlayer {
    public Moron(RoomStateData roomState, int number) {
        super(roomState, number);
    }

    @Override
    public RoomStateData voted() {
        MoronState moronState = roomState.getMoronState();
        if (!moronState.isBeanVoted()) {
            moronState.setLastRoomStatus(roomState.getStatus());
            moronState.setBeanVoted(true);
            //进入白痴时间
            roomState.setStatus(RoomStatus.MORON_TIME);
        } else {
            PlayerSeatInfo seatInfo = roomState.getPlaySeatInfoBySeatNumber(number);
            seatInfo.setAlive(false);
            gameEndingCalculate();
        }
        return roomState;
    }

    @Override
    public void calculateScore() {
        //TODO 白痴结算
        if(GameResult.VILLAGERS_WIN.equals(roomState.getGameResult())){
            PlayerSeatInfo seatInfo = roomState.getPlaySeatInfoBySeatNumber(this.number);
            seatInfo.setFinalScore(5);
        }
    }

    //决定翻牌
    public void moronShow() {
        //处理警长移交情况
        resolveSheriffDie();
    }

    //放弃翻牌
    public void giveUp(){
        PlayerSeatInfo seatInfo = roomState.getPlaySeatInfoBySeatNumber(number);
        seatInfo.setAlive(false);
        if (!gameEndingCalculate()) {
            //处理警长死亡情况
            resolveSheriffDie();
        }
    }


    @Override
    public PlayerDisplayInfo displayInfo() {
        PlayerDisplayInfo displayInfo = new PlayerDisplayInfo();
        resolveCommonDisplayInfo(displayInfo);
        return displayInfo;
    }
}

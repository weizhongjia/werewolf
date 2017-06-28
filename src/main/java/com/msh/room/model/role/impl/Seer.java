package com.msh.room.model.role.impl;

import com.msh.room.dto.event.PlayerEventType;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.dto.room.RoomStatus;
import com.msh.room.dto.room.record.DaytimeRecord;
import com.msh.room.dto.room.record.NightRecord;
import com.msh.room.dto.room.result.GameResult;
import com.msh.room.dto.room.seat.PlayerSeatInfo;
import com.msh.room.exception.RoomBusinessException;
import com.msh.room.model.role.Roles;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by zhangruiqian on 2017/5/7.
 */
public class Seer extends AssignedPlayer {
    private boolean alive;

    public Seer(RoomStateData roomState, int number) {
        super(roomState, number);
        this.alive = roomState.getPlaySeatInfoBySeatNumber(number).isAlive();
    }

    @Override
    public void calculateScore() {
        int initialScore = 5;
        if (GameResult.VILLAGERS_WIN.equals(roomState.getGameResult())) {
            PlayerSeatInfo seatInfo = roomState.getPlaySeatInfoBySeatNumber(this.number);
            int finalScore = calculateFinalScore(initialScore);
            if (finalScore > 19) {
                finalScore = 19;
            }
            seatInfo.setFinalScore(finalScore);
        }
    }

    private int calculateFinalScore(int initialScore) {
        int finalScore = initialScore;
        //所有狼人
        List<PlayerSeatInfo> werewolf = roomState.getPlayersByRoles(Roles.WEREWOLVES);
        List<Integer> wolfNumbers = werewolf.stream().map(PlayerSeatInfo::getSeatNumber).collect(Collectors.toList());

        //第一天死亡
        DaytimeRecord firstDaytime = roomState.getDaytimeRecordList().get(0);
        if (wolfNumbers.contains(firstDaytime.getDiedNumber())) {
            finalScore += 3;
        }

        /**
         * 活一轮计算
         */
        //白天晚上永远一样，因为只能天亮了才能结算Over。
        int nightSize = roomState.getNightRecordList().size();
        int dayTimeSize = roomState.getDaytimeRecordList().size();
        //先晚上再白天，晚上数量>=白天数量，因此数晚上;
        for (int i = 0; i < nightSize; i++) {
            NightRecord nightRecord = roomState.getNightRecordList().get(i);
            //验到狼人
            if (wolfNumbers.contains(nightRecord.getSeerVerify())) {
                //第二晚
                if (i == 1)
                    finalScore += 3;
                //第三晚
                if (i == 2)
                    finalScore += 2;
                //第四晚
                if (i > 2)
                    finalScore += 1;
            }


            //当天晚上死
            if (nightRecord.getDiedNumber().contains(number) || i > (dayTimeSize - 1)) {
                break;
            }
            DaytimeRecord daytimeRecord = roomState.getDaytimeRecordList().get(i);
            Integer seatNumber = Integer.valueOf(number);
            //当天白天死
            if (seatNumber.equals(daytimeRecord.getDiedNumber()) || seatNumber.equals(daytimeRecord.getHunterShoot())) {
                break;
            }

            //第1、2轮
            if (i == 0 || i == 1)
                finalScore += 3;
            //第3轮以后
            if (i > 2)
                finalScore += 2;
        }
        return finalScore;
    }

    @Override
    public PlayerDisplayInfo displayInfo() {
        PlayerDisplayInfo displayInfo = new PlayerDisplayInfo();
        resolveCommonDisplayInfo(displayInfo);

        //预言家要看到曾经验到的人
        if (roomState.getNightRecordList() != null
                && !RoomStatus.GAME_OVER.equals(roomState.getStatus())) {
            roomState.getNightRecordList().stream().forEach(
                    nightRecord -> {
                        Integer seerVerify = nightRecord.getSeerVerify();
                        if (seerVerify != null) {
                            if (Roles.WEREWOLVES.equals(roomState.getPlaySeatInfoBySeatNumber(seerVerify).getRole())) {
                                displayInfo.getPlayerSeatInfoList().get(seerVerify - 1).setRole(Roles.WEREWOLVES);
                            } else {
                                displayInfo.getPlayerSeatInfoList().get(seerVerify - 1).setRole(Roles.VILLAGER);
                            }
                        }
                    }
            );
        }
        if (isVerifyEnable()) {
            displayInfo.addAcceptableEventType(PlayerEventType.SEER_VERIFY);
        }
        return displayInfo;
    }

    public boolean isVerifyEnable() {
        if (alive) {
            if (RoomStatus.NIGHT.equals(roomState.getStatus()) && roomState.getLastNightRecord().getSeerVerify() == null) {
                return true;
            }
        }
        return false;
    }

    public void verify(Integer seerVerifyNumber) {
        if (isVerifyEnable()) {
            NightRecord lastNightRecord = roomState.getLastNightRecord();
            lastNightRecord.setSeerVerify(seerVerifyNumber);

            PlayerSeatInfo seatInfo = roomState.getPlaySeatInfoBySeatNumber(seerVerifyNumber);
            lastNightRecord.setSeerVerifyResult(Roles.WEREWOLVES.equals(seatInfo.getRole()));
        } else throw new RoomBusinessException("目前预言家无法验人");
    }

    public void fakeVerify() {
        NightRecord lastNightRecord = roomState.getLastNightRecord();
        lastNightRecord.setSeerVerify(0);
        lastNightRecord.setSeerVerifyResult(false);
    }
}


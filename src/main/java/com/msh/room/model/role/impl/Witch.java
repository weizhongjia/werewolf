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
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by zhangruiqian on 2017/5/7.
 */
public class Witch extends AssignedPlayer {
    private boolean alive;

    public Witch(RoomStateData roomState, int number) {
        super(roomState, number);
        alive = roomState.getPlaySeatInfoBySeatNumber(number).isAlive();
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
        /**
         * 活一轮计算
         */
        //貌似白天晚上永远一样，因为只能天亮了才能结算Over。
        int nightSize = roomState.getNightRecordList().size();
        int dayTimeSize = roomState.getDaytimeRecordList().size();
        //先晚上再白天，晚上数量>=白天数量，因此数晚上;
        for (int i = 0; i < nightSize; i++) {
            NightRecord nightRecord = roomState.getNightRecordList().get(i);
            //救了人
            if (0 < nightRecord.getWitchSaved()) {
                finalScore++;
            }
            //毒了狼
            if (wolfNumbers.contains(nightRecord.getWitchPoisoned())) {
                finalScore += 4;
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
            finalScore += 1;
        }
        /**
         * 投票狼人计算
         */
        for (int i = 0; i < dayTimeSize; i++) {
            DaytimeRecord daytimeRecord = roomState.getDaytimeRecordList().get(i);
            if (daytimeRecord != null) {
                //优先计算PK投票
                if (daytimeRecord.getPkVotingRecord().size() > 0) {
                    int size = daytimeRecord.getPkVotingRecord().size();
                    Map<Integer, List<Integer>> votingRecord = daytimeRecord.getPkVotingRecord().get(size - 1);
                    //每个狼人
                    for (PlayerSeatInfo info : werewolf) {
                        List<Integer> votingCount = votingRecord.get(info.getSeatNumber());
                        if (votingCount != null && votingCount.contains(number)) {
                            finalScore += 2;
                        }
                    }
                } else {
                    Map<Integer, List<Integer>> votingRecord = daytimeRecord.getVotingRecord();
                    //每个狼人
                    for (PlayerSeatInfo info : werewolf) {
                        List<Integer> votingCount = votingRecord.get(info.getSeatNumber());
                        if (votingCount != null && votingCount.contains(number)) {
                            finalScore += 2;
                        }
                    }
                }
            }
        }
        return finalScore;
    }

    @Override
    public PlayerDisplayInfo displayInfo() {
        PlayerDisplayInfo displayInfo = new PlayerDisplayInfo();
        resolveCommonDisplayInfo(displayInfo);
        if (alive) {
            if (RoomStatus.NIGHT.equals(roomState.getStatus())) {
                NightRecord nightRecord = roomState.getLastNightRecord();
                PlayerEventType eventType = getWitchNightEvent(nightRecord);
                if (eventType != null) {
                    displayInfo.addAcceptableEventType(eventType);
                }
            }
        }
        return displayInfo;
    }

    public PlayerEventType getWitchNightEvent(NightRecord nightRecord) {
        Integer wolfKilledSeat = nightRecord.getWolfKilledSeat();

        //女巫用药询问逻辑,此处逻辑仅限不能同时用两种药的情况.逻辑处理不太好，需要再做封装
        if (wolfKilledSeat == null) {
            return null;
        }
        //女巫本轮未询问用解药==null，女巫解药可用，狼人没有杀女巫: WITCH_SAVE
        if (nightRecord.getWitchSaved() == null && roomState.getWitchState().isAntidoteAvailable() && !wolfKilledSeat.equals(number)) {
            return PlayerEventType.WITCH_SAVE;
        }
        //女巫本轮未询问用解药==null，(女巫解药不可用 或者 狼杀了女巫) FAKE_WITCH_SAVE
        else if (nightRecord.getWitchSaved() == null && (!roomState.getWitchState().isAntidoteAvailable() || wolfKilledSeat.equals(number))) {
            return PlayerEventType.FAKE_WITCH_SAVE;
        }
        //女巫本轮已询问用解药,但未用==0，但未询问用毒药==null，女巫毒药可用 WITCH_POISON
        else if (nightRecord.getWitchSaved() == 0 && nightRecord.getWitchPoisoned() == null
                && roomState.getWitchState().isPoisonAvailable()) {
            return PlayerEventType.WITCH_POISON;
        }
        //女巫本轮已询问用解药,但未用==0，但未询问用毒药==null，女巫毒药不可用 FAKE_WITCH_POISON
        else if (nightRecord.getWitchSaved() == 0 && nightRecord.getWitchPoisoned() == null
                && !roomState.getWitchState().isPoisonAvailable()) {
            return PlayerEventType.FAKE_WITCH_POISON;
        }
        //女巫本轮已询问用解药，但已用!=0，但未询问是否用毒药 FAKE_WITCH_POISON
        else if (nightRecord.getWitchSaved() != 0 && nightRecord.getWitchPoisoned() == null) {
            return PlayerEventType.FAKE_WITCH_POISON;
        }
        return null;
    }

    public void save(boolean witchSave) {
        if (roomState.getWitchState().isAntidoteAvailable()) {
            Integer killedSeat = roomState.getLastNightRecord().getWolfKilledSeat();
            if (killedSeat.equals(this.number)) {
                throw new RoomBusinessException("女巫无法自救");
            }
            //空刀时相当于没救
            if (witchSave && killedSeat != 0) {
                roomState.getLastNightRecord().setWitchSaved(killedSeat);
                roomState.getWitchState().setAntidoteAvailable(false);
            } else {
                roomState.getLastNightRecord().setWitchSaved(0);
            }
        } else {
            throw new RoomBusinessException("女巫无法使用解药");
        }
    }


    public void poison(Integer witchPoisonNumber) {
        if (roomState.getWitchState().isPoisonAvailable()) {
            roomState.getLastNightRecord().setWitchPoisoned(witchPoisonNumber);
            if (witchPoisonNumber != 0) {
                roomState.getWitchState().setPoisonAvailable(false);
            }
        } else {
            throw new RoomBusinessException("女巫无法使用毒药");
        }
    }

    public void fakePoison() {
        roomState.getLastNightRecord().setWitchPoisoned(0);
    }

    public void fakeSave() {
        roomState.getLastNightRecord().setWitchSaved(0);
    }
}

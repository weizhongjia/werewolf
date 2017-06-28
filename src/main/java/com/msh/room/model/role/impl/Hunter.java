package com.msh.room.model.role.impl;

import com.msh.room.dto.event.PlayerEventType;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.dto.room.RoomStatus;
import com.msh.room.dto.room.record.DaytimeRecord;
import com.msh.room.dto.room.result.GameResult;
import com.msh.room.dto.room.seat.PlayerSeatInfo;
import com.msh.room.dto.room.state.HunterState;
import com.msh.room.exception.RoomBusinessException;
import com.msh.room.model.role.CommonPlayer;
import com.msh.room.model.role.PlayerRoleFactory;
import com.msh.room.model.role.Roles;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by zhangruiqian on 2017/5/7.
 */
public class Hunter extends AssignedPlayer {
    public Hunter(RoomStateData roomState, int number) {
        super(roomState, number);
    }

    @Override
    public RoomStateData killed() {
        PlayerSeatInfo seatInfo = roomState.getPlaySeatInfoBySeatNumber(number);
        seatInfo.setAlive(false);
        gameEndingCalculate();
        //游戏没结束
        if (roomState.getGameResult() == null) {
            //判断是否被毒
            if (roomState.getLastNightRecord().getWitchPoisoned() != number) {
                HunterState hunterState = new HunterState();
                //当前房间状态缓存
                hunterState.setNextStatus(roomState.getStatus());
                roomState.setHunterState(hunterState);
                roomState.setStatus(RoomStatus.HUNTER_SHOOT);
                //如果是警长死亡,会先要求移交警徽
                resolveSheriffDie();
            }
        }
        return roomState;
    }

    @Override
    public void calculateScore() {
        int initialScore = 5;
        if (GameResult.VILLAGERS_WIN.equals(roomState.getGameResult())) {
            PlayerSeatInfo seatInfo = roomState.getPlaySeatInfoBySeatNumber(this.number);
            int finalScore = calculateFinalScore(initialScore);
            if (finalScore > 14) {
                finalScore = 14;
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
        //白天晚上永远一样，因为只能天亮了才能结算Over。
        int nightSize = roomState.getNightRecordList().size();
        int dayTimeSize = roomState.getDaytimeRecordList().size();
        //先晚上再白天，晚上数量>=白天数量，因此数晚上;
        for (int i = 0; i < nightSize; i++) {
            //猎人优先判断白天带人情况
            DaytimeRecord daytimeRecord = roomState.getDaytimeRecordList().get(i);
            Integer hunterShoot = daytimeRecord.getHunterShoot();
            if (hunterShoot != null && wolfNumbers.contains(hunterShoot)) {
                finalScore += 6;
            }
            //当天晚上死
            if (roomState.getNightRecordList().get(i).getDiedNumber().contains(number) || i > (dayTimeSize - 1)) {
                break;
            }
            //当天白天死
            if (Integer.valueOf(number).equals(daytimeRecord.getDiedNumber())) {
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
                    //每个狼人
                    for (PlayerSeatInfo info : werewolf) {
                        List<Integer> votingCount = daytimeRecord.lastPKRecord().get(info.getSeatNumber());
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
    public RoomStateData voted() {
        PlayerSeatInfo seatInfo = roomState.getPlaySeatInfoBySeatNumber(number);
        seatInfo.setAlive(false);
        gameEndingCalculate();
        //游戏没结束
        if (roomState.getGameResult() == null) {
            HunterState hunterState = new HunterState();
            //当前房间状态缓存
            hunterState.setNextStatus(roomState.getStatus());
            roomState.setHunterState(hunterState);
            roomState.setStatus(RoomStatus.HUNTER_SHOOT);
            //如果是警长死亡,会先要求移交警徽
            resolveSheriffDie();
        }
        return roomState;
    }

    public RoomStateData shoot(int number) {
        if (number > 0) {
            if (!roomState.getPlaySeatInfoBySeatNumber(number).isAlive()) {
                throw new RoomBusinessException("该玩家已死亡，无法再开枪");
            }
            CommonPlayer commonPlayer = PlayerRoleFactory.createPlayerInstance(roomState, number);
            commonPlayer.killed();
        }
        roomState.setStatus(roomState.getHunterState().getNextStatus());
        roomState.getHunterState().setShootNumber(number);
        //记录白天信息
        roomState.getLastDaytimeRecord().setHunterShoot(number);
        return roomState;
    }


    @Override
    public PlayerDisplayInfo displayInfo() {
        PlayerDisplayInfo displayInfo = new PlayerDisplayInfo();
        resolveCommonDisplayInfo(displayInfo);
        if (RoomStatus.HUNTER_SHOOT.equals(roomState.getStatus())) {
            displayInfo.addAcceptableEventType(PlayerEventType.HUNTER_SHOOT);
        }
        return displayInfo;
    }
}

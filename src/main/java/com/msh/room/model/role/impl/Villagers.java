package com.msh.room.model.role.impl;

import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.dto.room.record.DaytimeRecord;
import com.msh.room.dto.room.result.GameResult;
import com.msh.room.dto.room.seat.PlayerSeatInfo;
import com.msh.room.model.role.Roles;

import java.util.List;
import java.util.Map;

/**
 * Created by zhangruiqian on 2017/5/7.
 */
public class Villagers extends AssignedPlayer {
    public Villagers(RoomStateData roomState, int number) {
        super(roomState, number);
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
        //首夜被杀
        if (roomState.getNightRecordList().get(0).getDiedNumber().contains(number)) {
            return initialScore + 4;
        }
        /**
         * 村民活一轮计算
         */
        //貌似白天晚上永远一样，因为只能天亮了才能结算Over。
        int nightSize = roomState.getNightRecordList().size();
        int dayTimeSize = roomState.getDaytimeRecordList().size();
        //先晚上再白天，晚上数量>=白天数量，因此数晚上;
        for (int i = 0; i < nightSize; i++) {
            //当天晚上死
            if (roomState.getNightRecordList().get(i).getDiedNumber().contains(number) || i > (dayTimeSize - 1)) {
                break;
            }
            DaytimeRecord daytimeRecord = roomState.getDaytimeRecordList().get(i);
            Integer seatNumber = Integer.valueOf(number);
            //当天白天死
            if(seatNumber.equals(daytimeRecord.getDiedNumber()) ||seatNumber.equals(daytimeRecord.getHunterShoot())){
                break;
            }
            finalScore += 1;
        }
        /**
         * 投票狼人计算
         */
        List<PlayerSeatInfo> werewolf = roomState.getPlayersByRoles(Roles.WEREWOLVES);
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
        return displayInfo;
    }

}

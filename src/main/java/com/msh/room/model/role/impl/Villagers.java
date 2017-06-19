package com.msh.room.model.role.impl;

import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.dto.room.result.GameResult;
import com.msh.room.dto.room.seat.PlayerSeatInfo;

/**
 * Created by zhangruiqian on 2017/5/7.
 */
public class Villagers extends AssignedPlayer {
    public Villagers(RoomStateData roomState, int number) {
        super(roomState, number);
    }

    @Override
    public void calculateScore() {
        int initialScore = 6;
        if (GameResult.VILLAGERS_WIN.equals(roomState.getGameResult())) {
            PlayerSeatInfo seatInfo = roomState.getPlaySeatInfoBySeatNumber(this.number);
            int finalScore = calculateFinalScore(initialScore);
            seatInfo.setFinalScore(finalScore);
        }
    }

    private int calculateFinalScore(int initialScore) {
        int finalScore = initialScore;
        //首夜被杀
        if (roomState.getNightRecordList().get(0).getDiedNumber().equals(number)) {
            return initialScore + 4;
        }
        //TODO 貌似白天晚上永远一样，因为只能天亮了才能结算Over。
        int nightSize = roomState.getNightRecordList().size();
        int dayTimeSize = roomState.getDaytimeRecordList().size();
        //先晚上再白天，晚上数量>=白天数量，因此数晚上;
        for (int i = 0; i < nightSize; i++) {
            //当天晚上没死
            if (!roomState.getNightRecordList().get(i).getDiedNumber().contains(number)
                    && i <= (dayTimeSize - 1)) {
                //当天白天也没死
                if (!roomState.getDaytimeRecordList().get(i).getDiedNumber().equals(number)) {
                    finalScore += 1;
                }
            }
        }
        for (int i =0; i < dayTimeSize; i++) {
            //TODO 投票加分
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

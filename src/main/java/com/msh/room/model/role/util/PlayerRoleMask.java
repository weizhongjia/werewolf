package com.msh.room.model.role.util;

import com.msh.room.dto.room.seat.PlayerSeatInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangruiqian on 2017/5/8.
 */
public class PlayerRoleMask {
    public static List<PlayerSeatInfo> maskPlayerRole(List<PlayerSeatInfo> playerSeatInfoList, List<Integer> unmaskNumber) {
        List<PlayerSeatInfo> maskedPlayerInfoList = new ArrayList<>();
        playerSeatInfoList.stream().forEach(seatInfo -> {
            PlayerSeatInfo playerSeatInfo = new PlayerSeatInfo(seatInfo.getSeatNumber(), false);
            playerSeatInfo.setSeatAvailable(seatInfo.isSeatAvailable());
            playerSeatInfo.setAlive(seatInfo.isAlive());
            playerSeatInfo.setSeatNumber(seatInfo.getSeatNumber());
            playerSeatInfo.setUserID(seatInfo.getUserID());
            playerSeatInfo.setRole(null);
            playerSeatInfo.setFinalScore(seatInfo.getFinalScore());
            if (unmaskNumber.contains(seatInfo.getSeatNumber())) {
                playerSeatInfo.setRole(seatInfo.getRole());
            }
            maskedPlayerInfoList.add(playerSeatInfo);
        });
        return maskedPlayerInfoList;
    }
}

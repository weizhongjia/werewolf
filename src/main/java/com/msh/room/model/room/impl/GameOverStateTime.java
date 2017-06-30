package com.msh.room.model.room.impl;

import com.msh.room.dto.event.JudgeEvent;
import com.msh.room.dto.event.JudgeEventType;
import com.msh.room.dto.event.PlayerEvent;
import com.msh.room.dto.response.JudgeDisplayInfo;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.dto.room.result.GameResult;
import com.msh.room.dto.room.seat.PlayerSeatInfo;
import com.msh.room.model.role.Roles;
import io.swagger.models.auth.In;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangruiqian on 2017/5/25.
 */
public class GameOverStateTime extends AbstractStateRoom {
    public GameOverStateTime(RoomStateData data) {
        super(data);
    }

    @Override
    public RoomStateData resolveJudgeEvent(JudgeEvent event) {
        filterJudgeEventType(event);
        switch (event.getEventType()) {
            case RESTART_GAME:
                resolveRestartGameEvent(event);
                break;
            case DISBAND_GAME:
                resolveDisbandGameEvent(event);
                break;
        }
        return roomState;
    }

    @Override
    public JudgeDisplayInfo displayJudgeInfo() {
        JudgeDisplayInfo displayInfo = judgeCommonDisplayInfo();
        displayInfo.setResultMap(calculateResult());
        displayInfo.addAcceptableEventType(JudgeEventType.RESTART_GAME);
        displayInfo.addAcceptableEventType(JudgeEventType.DISBAND_GAME);
        return displayInfo;
    }

    private Map<String, List<Integer>> calculateResult() {
        Map<String, List<Integer>> resultMap = new HashMap<>();
        List<Integer> winner = new ArrayList<>();
        List<Integer> looser = new ArrayList<>();
        roomState.getPlayerSeatInfo().parallelStream().forEach(info -> {
            if (Roles.WEREWOLVES.equals(info.getRole())) {
                if (GameResult.WEREWOLVES_WIN.equals(roomState.getGameResult())) {
                    winner.add(info.getSeatNumber());
                } else {
                    looser.add(info.getSeatNumber());
                }
            } else {
                if (GameResult.VILLAGERS_WIN.equals(roomState.getGameResult())) {
                    winner.add(info.getSeatNumber());
                } else {
                    looser.add(info.getSeatNumber());
                }
            }
        });
        resultMap.put("winner", winner);
        resultMap.put("looser", looser);
        return resultMap;
    }

    @Override
    public RoomStateData resolvePlayerEvent(PlayerEvent event) {
        return null;
    }

    @Override
    public PlayerDisplayInfo displayPlayerInfo(int seatNumber) {
        PlayerDisplayInfo displayInfo = playerCommonDisplayInfo(seatNumber);
        //游戏结束，无需隐藏
        displayInfo.setPlayerSeatInfoList(roomState.getPlayerSeatInfo());
        PlayerSeatInfo info = displayInfo.getPlayerInfo();
        if (Roles.WEREWOLVES.equals(info.getRole())) {
            if (GameResult.WEREWOLVES_WIN.equals(roomState.getGameResult())) {
                displayInfo.setGameResult("winner");
            } else {
                displayInfo.setGameResult("looser");
            }
        } else {
            if (GameResult.VILLAGERS_WIN.equals(roomState.getGameResult())) {
                displayInfo.setGameResult("winner");
            } else {
                displayInfo.setGameResult("looser");
            }
        }
        return displayInfo;
    }
}

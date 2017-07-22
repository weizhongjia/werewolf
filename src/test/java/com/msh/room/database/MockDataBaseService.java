package com.msh.room.database;

import com.msh.room.dto.event.JudgeEvent;
import com.msh.room.dto.event.PlayerEvent;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.service.DataBaseService;

/**
 * Created by zhangruiqian on 2017/7/13.
 */
public class MockDataBaseService extends DataBaseService{
    @Override
    public void saveGameInfo(RoomStateData roomState) {

    }

    @Override
    public void resolveRaceId(RoomStateData roomState) {

    }

    @Override
    public void saveJudgeEvent(String gameID, JudgeEvent event, int version) {

    }
    @Override
    public void savePlayerEvent(String gameID, PlayerEvent event, int version) {

    }
    @Override
    public void savePlayerScore(String userID, String gameID, String raceID, int finalScore) {
    }
}

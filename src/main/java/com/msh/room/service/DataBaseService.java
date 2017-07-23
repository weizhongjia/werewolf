package com.msh.room.service;

import com.google.gson.Gson;
import com.msh.common.mapper.*;
import com.msh.common.model.*;
import com.msh.room.dto.event.JudgeEvent;
import com.msh.room.dto.event.PlayerEvent;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.exception.RoomBusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by zhangruiqian on 2017/7/13.
 */
@Component
public class DataBaseService {
    @Autowired
    private GameMapper gameMapper;
    @Autowired
    private RoomMapper roomMapper;
    @Autowired
    private RaceMapper raceMapper;
    @Autowired
    private EventMapper eventMapper;
    @Autowired
    private UserScoreDetailMapper userScoreDetailMapper;

    public void saveGameInfo(RoomStateData roomState) {
        Game game = new Game();
        game.setRace(Long.valueOf(roomState.getRaceID()));
        game.setRoom(Long.valueOf(roomState.getRoomCode()));
        String playerList = new Gson().toJson(roomState.getPlayerSeatInfo());
        game.setPlayerList(playerList);
        gameMapper.insert(game);
        roomState.setGameID(String.valueOf(game.getId()));
    }

    /**
     * 查询获取当前房间号的比赛
     */
    public void resolveRaceId(RoomStateData roomState) {
        Room queryRoom = new Room();
        queryRoom.setId(Integer.parseInt(roomState.getRoomCode()));
        Room room = roomMapper.selectByPrimaryKey(queryRoom);
        if (room == null) {
            throw new RoomBusinessException("没有这个房间，请联系运营人员创建房间");
        }
        long clubID = room.getClub();
        Race raceParameter = new Race();
        raceParameter.setClub(clubID);
        Race race = raceMapper.selectOne(raceParameter);
        if (race != null) {
            roomState.setRaceID(String.valueOf(race.getId()));
        }
    }

    public void saveJudgeEvent(String gameID, JudgeEvent event, int version) {
        Event eventModel = new Event();
        eventModel.setVersion(version);
        eventModel.setGame(Long.valueOf(gameID));
        eventModel.setEventData(new Gson().toJson(event));
        eventMapper.insert(eventModel);
    }

    public void savePlayerEvent(String gameID, PlayerEvent event, int version) {
        Event eventModel = new Event();
        eventModel.setVersion(version);
        eventModel.setGame(Long.valueOf(gameID));
        eventModel.setEventData(new Gson().toJson(event));
        eventMapper.insert(eventModel);
    }

    public void savePlayerScore(String userID, String gameID, String raceID, int finalScore) {
        UserScoreDetail scoreDetail = new UserScoreDetail();
        scoreDetail.setGame(Long.valueOf(gameID));
        scoreDetail.setRace(Long.valueOf(raceID));
        scoreDetail.setUser(Long.valueOf(userID));
        scoreDetail.setFinalScore(finalScore);
        userScoreDetailMapper.insert(scoreDetail);
    }
}

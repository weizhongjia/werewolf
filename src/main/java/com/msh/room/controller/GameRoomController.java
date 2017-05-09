package com.msh.room.controller;

import com.msh.room.dto.event.JudgeEvent;
import com.msh.room.dto.event.PlayerEvent;
import com.msh.room.dto.response.JudgeDisplayInfo;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.model.room.Room;
import com.msh.room.model.room.RoomManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by zhangruiqian on 2017/5/9.
 */
@RestController
public class GameRoomController {
    @Autowired
    private RoomManager roomManager;

    @RequestMapping(value = "room/{roomCode}/judge/event", method = RequestMethod.PUT)
    public JudgeDisplayInfo pushJudgeEvent(@PathVariable("roomCode") String roomCode, @RequestBody JudgeEvent event) {
        Room room = roomManager.loadRoom(roomCode);
        return room.resolveJudgeEvent(event);
    }

    @RequestMapping(value = "room/{roomCode}/player/{number}/event", method = RequestMethod.PUT)
    public PlayerDisplayInfo pushPlayerEvent(@PathVariable("roomCode") String roomCode,
                                             @PathVariable("number") String number,
                                             @RequestBody PlayerEvent event) {
        Room room = roomManager.loadRoom(roomCode);
        //此处需获取用户名
        //event.setUserID(userID);
        event.setSeatNumber(Integer.valueOf(number));
        return room.resolvePlayerEvent(event);
    }


    @RequestMapping(value = "room/{roomCode}/judge/info", method = RequestMethod.GET)
    public JudgeDisplayInfo getJudgeDisplayInfo(@PathVariable("roomCode") String roomCode) {
        Room room = roomManager.loadRoom(roomCode);
        return room.getJudgeDisplayResult();
    }


    @RequestMapping(value = "room/{roomCode}/player/{number}/info", method = RequestMethod.GET)
    public PlayerDisplayInfo getPlayerEvent(@PathVariable("roomCode") String roomCode,
                                             @PathVariable("number") String number) {
        Room room = roomManager.loadRoom(roomCode);
        //应该添加userID参数，判断座位号与userID是否一致
        return room.getPlayerDisplayResult(Integer.valueOf(number));
    }

}

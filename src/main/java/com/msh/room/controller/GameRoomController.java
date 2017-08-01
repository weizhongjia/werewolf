package com.msh.room.controller;

import com.msh.room.dto.event.JudgeEvent;
import com.msh.room.dto.event.PlayerEvent;
import com.msh.room.dto.response.JudgeDisplayInfo;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.service.WereWolfRoomService;
import com.msh.security.WerewolfAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Created by zhangruiqian on 2017/5/9.
 */
@RestController
@CrossOrigin
@RequestMapping(value = "werewolf")
public class GameRoomController {
    @Autowired
    private WereWolfRoomService wereWolfRoomService;


    @RequestMapping(value = "room/{roomCode}/judge/event", method = RequestMethod.PUT)
    public JudgeDisplayInfo pushJudgeEvent(@PathVariable("roomCode") String roomCode, @RequestBody JudgeEvent event) {
        return wereWolfRoomService.resolveJudgeEvent(event, roomCode);
    }

    @RequestMapping(value = "room/{roomCode}/player/{number}/event", method = RequestMethod.PUT)
    public PlayerDisplayInfo pushPlayerEvent(@PathVariable("roomCode") String roomCode,
                                             @PathVariable("number") String number,
                                             @RequestBody PlayerEvent event) {
        WerewolfAuthenticationToken token = (WerewolfAuthenticationToken)SecurityContextHolder.getContext().getAuthentication();
        event.setUserID(token.getOpenId());
        event.setSeatNumber(Integer.valueOf(number));
        return wereWolfRoomService.resolvePlayerEvent(event, roomCode);
    }


    @RequestMapping(value = "room/{roomCode}/judge/info", method = RequestMethod.GET)
    public JudgeDisplayInfo getJudgeDisplayInfo(@PathVariable("roomCode") String roomCode) {
        return wereWolfRoomService.getJudgeDisplayResult(roomCode);
    }


    @RequestMapping(value = "room/{roomCode}/player/{number}/info", method = RequestMethod.GET)
    public PlayerDisplayInfo getPlayerEvent(@PathVariable("roomCode") String roomCode,
                                            @PathVariable("number") String number) {
        //应该添加userID参数，判断座位号与userID是否一致
        return wereWolfRoomService.getPlayerDisplayResult(roomCode, Integer.valueOf(number));
    }

}

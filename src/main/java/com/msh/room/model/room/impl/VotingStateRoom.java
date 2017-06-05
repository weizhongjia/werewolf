package com.msh.room.model.room.impl;

import com.msh.room.dto.event.JudgeEvent;
import com.msh.room.dto.event.JudgeEventType;
import com.msh.room.dto.event.PlayerEvent;
import com.msh.room.dto.response.JudgeDisplayInfo;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.dto.room.RoomStatus;
import com.msh.room.dto.room.record.DaytimeRecord;
import com.msh.room.exception.RoomBusinessException;
import com.msh.room.model.role.CommonPlayer;
import com.msh.room.model.role.PlayerRoleFactory;

import java.util.ArrayList;
import java.util.List;

import static com.msh.room.dto.event.PlayerEventType.DAYTIME_VOTE;

/**
 * Created by zhangruiqian on 2017/5/25.
 */
public class VotingStateRoom extends AbstractStateRoom {
    public VotingStateRoom(RoomStateData data) {
        super(data);
    }

    @Override
    public RoomStateData resolveJudgeEvent(JudgeEvent event) {
        filterJudgeEventType(event);
        switch (event.getEventType()) {
            case NIGHT_COMING:
                resolveNightComing();
                break;
            case GAME_ENDING:
                resolveGameEnding(event);
                break;
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
        //投票完成,有结果
        if (roomState.getLastDaytimeRecord().getDiedNumber() != null) {
            displayInfo.addAcceptableEventType(JudgeEventType.NIGHT_COMING);
        }
        //投票阶段可以结束游戏
        if (roomState.getGameResult() != null) {
            displayInfo.setAcceptableEventTypes(new ArrayList<>());
            displayInfo.addAcceptableEventType(JudgeEventType.GAME_ENDING);
        }
        displayInfo.addAcceptableEventType(JudgeEventType.RESTART_GAME);
        displayInfo.addAcceptableEventType(JudgeEventType.DISBAND_GAME);
        return displayInfo;
    }

    @Override
    public RoomStateData resolvePlayerEvent(PlayerEvent event) {
        filterPlayerEventType(event);
        switch (event.getEventType()) {
            case DAYTIME_VOTE:
                votePlayer(event);
                break;
        }
        return roomState;
    }

    protected RoomStateData votePlayer(PlayerEvent event) {
        Integer voteNumber = event.getDaytimeVoteNumber();
        if (!roomState.getPlaySeatInfoBySeatNumber(event.getSeatNumber()).isAlive()) {
            throw new RoomBusinessException("您已死亡，无法投票");
        }
        if (voteNumber != 0 && !roomState.getPlaySeatInfoBySeatNumber(voteNumber).isAlive()) {
            throw new RoomBusinessException("该玩家已死亡，无法投票");
        }
        DaytimeRecord lastDaytimeRecord = roomState.getLastDaytimeRecord();
        if (lastDaytimeRecord.isDaytimeVoted(event.getSeatNumber())) {
            throw new RoomBusinessException("您已经投票，请勿重复投票");
        }
        lastDaytimeRecord.addVote(event.getSeatNumber(), voteNumber);

        //投票结束
        if (lastDaytimeRecord.isDaytimeVoteComplete(roomState.getAliveCount())) {
            return daytimeVoteResult();
        }
        return roomState;
    }

    protected RoomStateData daytimeVoteResult() {
        DaytimeRecord daytimeRecord = roomState.getLastDaytimeRecord();
        Integer sheriff = 0;
        if (roomState.isSheriff()) {
            sheriff = roomState.getSheriffRecord().getSheriff();
        }
        List<Integer> voteResult = daytimeRecord.resolveVoteResult(sheriff);
        //如果有平票
        if (voteResult.size() > 1) {
            daytimeRecord.addNewPk();
            for (Integer number : voteResult) {
                daytimeRecord.addPkNumber(number);
                roomState.setStatus(RoomStatus.PK);
            }
            return roomState;
        } else {
            if (voteResult.size() == 0) {
                //全部弃票,直接无人死亡
                daytimeRecord.setDiedNumber(0);
                return roomState;
            }
            Integer number = voteResult.get(0);
            daytimeRecord.setDiedNumber(number);
            CommonPlayer player = PlayerRoleFactory.createPlayerInstance(roomState, number);
            return player.voted();
        }
    }

    @Override
    public PlayerDisplayInfo displayPlayerInfo(int seatNumber) {
        PlayerDisplayInfo displayInfo = playerCommonDisplayInfo(seatNumber);
        if (!roomState.getLastDaytimeRecord().isDaytimeVoted(seatNumber)
                && this.roomState.getPlaySeatInfoBySeatNumber(seatNumber).isAlive()) {
            displayInfo.addAcceptableEventType(DAYTIME_VOTE);
        } else if (roomState.getLastDaytimeRecord().getDiedNumber() != null) {
            //如果已经投票死人，说明投票有结果了.公布白天投票信息
            displayInfo.setDaytimeRecord(roomState.getLastDaytimeRecord());
        }


        return displayInfo;
    }

}

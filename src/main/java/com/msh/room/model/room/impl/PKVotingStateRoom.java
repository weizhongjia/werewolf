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
import com.msh.room.model.room.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.msh.room.dto.event.PlayerEventType.PK_VOTE;

/**
 * Created by zhangruiqian on 2017/5/25.
 */
public class PKVotingStateRoom extends AbstractStateRoom {
    public PKVotingStateRoom(RoomStateData data) {
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
        if (roomState.getLastDaytimeRecord().getDiedNumber() != null) {
            displayInfo.addAcceptableEventType(JudgeEventType.NIGHT_COMING);
        }
        //PK投票阶段可以结束游戏
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
            case PK_VOTE:
                pkVotePlayer(event);
                break;
        }
        return roomState;
    }

    protected RoomStateData pkVotePlayer(PlayerEvent event) {
        Integer voteNumber = event.getPkVoteNumber();
        if (!roomState.getPlaySeatInfoBySeatNumber(event.getSeatNumber()).isAlive()) {
            throw new RoomBusinessException("您已死亡，无法投票");
        }
        DaytimeRecord lastDaytimeRecord = roomState.getLastDaytimeRecord();
        if (lastDaytimeRecord.isPKVoted(event.getSeatNumber())) {
            throw new RoomBusinessException("您已经投票，请勿重复投票");
        }
        Map<Integer, List<Integer>> lastPKRecord = lastDaytimeRecord.lastPKRecord();
        lastDaytimeRecord.addPKVote(event.getSeatNumber(), voteNumber);
        if (lastDaytimeRecord.isPKVoteComplete(roomState.getAliveCount() - lastPKRecord.size())) {
            return pkVoteResult();
        }
        return roomState;
    }

    private RoomStateData pkVoteResult() {
        DaytimeRecord daytimeRecord = roomState.getLastDaytimeRecord();
        List<Integer> voteResult = daytimeRecord.resolvePKVoteResult();
        //如果有平票
        if (voteResult.size() > 1) {
            //没到两轮
            if (daytimeRecord.getPkVotingRecord().size() < 2) {
                daytimeRecord.addNewPk();
                for (Integer number : voteResult) {
                    daytimeRecord.addPkNumber(number);
                    roomState.setStatus(RoomStatus.PK);
                }
            } else {
                //两轮pk平票 无人死亡
                daytimeRecord.setDiedNumber(0);
            }
            return roomState;
        } else {
            //没可能所有人弃票
            if (voteResult.size() == 0) {
                throw new RoomBusinessException("状态不对，pk阶段无法弃票");
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
        Map<Integer, List<Integer>> pkRecord = roomState.getLastDaytimeRecord().lastPKRecord();
        if (!pkRecord.containsKey(seatNumber)
                && this.roomState.getPlaySeatInfoBySeatNumber(seatNumber).isAlive()
                && !roomState.getLastDaytimeRecord().isPKVoted(seatNumber)) {
            displayInfo.addAcceptableEventType(PK_VOTE);
        }
        if (roomState.getLastDaytimeRecord().getDiedNumber() != null) {
            //如果已经投票死人(无论是否无人死亡)，说明投票有结果了.公布白天投票信息
            displayInfo.setDaytimeRecord(roomState.getLastDaytimeRecord());
        }
        return displayInfo;
    }
}

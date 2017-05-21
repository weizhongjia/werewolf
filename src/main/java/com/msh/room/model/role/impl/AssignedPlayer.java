package com.msh.room.model.role.impl;

import com.msh.room.dto.event.PlayerEvent;
import com.msh.room.dto.event.PlayerEventType;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.dto.room.RoomStatus;
import com.msh.room.dto.room.record.DaytimeRecord;
import com.msh.room.dto.room.record.NightRecord;
import com.msh.room.dto.room.result.GameResult;
import com.msh.room.dto.room.seat.PlayerSeatInfo;
import com.msh.room.exception.RoomBusinessException;
import com.msh.room.model.role.CommonPlayer;
import com.msh.room.model.role.PlayerRoleFactory;
import com.msh.room.model.role.Roles;
import com.msh.room.model.role.util.PlayerRoleMask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by zhangruiqian on 2017/5/18.
 */
public abstract class AssignedPlayer extends CommonPlayer {
    public AssignedPlayer(RoomStateData roomState, int number) {
        super(roomState, number);
    }

    @Override
    public RoomStateData resolveEvent(PlayerEvent event) {
        if (PlayerEventType.DAYTIME_VOTE.equals(event.getEventType())) {
            return votePlayer(event);
        }
        return roomState;
    }

    protected RoomStateData votePlayer(PlayerEvent event) {
        Integer voteNumber = event.getDaytimeVoteNumber();
        if (!roomState.getPlaySeatInfoBySeatNumber(this.number).isAlive()) {
            throw new RoomBusinessException("您已死亡，无法投票");
        }
        if (!roomState.getPlaySeatInfoBySeatNumber(voteNumber).isAlive()) {
            throw new RoomBusinessException("该玩家已死亡，无法投票");
        }
        DaytimeRecord lastDaytimeRecord = roomState.getLastDaytimeRecord();
        if (lastDaytimeRecord.isDaytimeVoted(this.number)) {
            throw new RoomBusinessException("您已经投票，请勿重复投票");
        }
        lastDaytimeRecord.addVote(this.number, voteNumber);

        //投票结束
        if (lastDaytimeRecord.isDaytimeVoteComplete(roomState.getAliveCount())) {
            return daytimeVoteResult(lastDaytimeRecord);
        }
        return roomState;
    }

    protected RoomStateData daytimeVoteResult(DaytimeRecord lastDaytimeRecord) {
        DaytimeRecord daytimeRecord = roomState.getLastDaytimeRecord();
        List<Integer> voteResult = lastDaytimeRecord.getVoteResult();
        //如果有平票
        if (voteResult.size() > 1) {
            for (Integer number : voteResult) {
                daytimeRecord.addNewPk();
                daytimeRecord.addPkNumber(number);
                roomState.setStatus(RoomStatus.PK);
            }
            return roomState;
        } else {
            Integer number = voteResult.get(0);
            daytimeRecord.setDiedNumber(number);
            CommonPlayer player = PlayerRoleFactory.createPlayerInstance(roomState, number);
            return player.voted();
        }
    }

    protected void gameEndingCalculate() {
        long wolfCount = roomState.getPlayerSeatInfo().parallelStream()
                .filter(playerSeatInfo -> Roles.WEREWOLVES.equals(playerSeatInfo.getRole()))
                .filter(playerSeatInfo -> playerSeatInfo.isAlive()).count();
        long villagerCount = roomState.getPlayerSeatInfo().parallelStream()
                .filter(playerSeatInfo -> Roles.VILLAGER.equals(playerSeatInfo.getRole()))
                .filter(playerSeatInfo -> playerSeatInfo.isAlive()).count();
        long unCommonCount = roomState.getPlayerSeatInfo().parallelStream()
                .filter(playerSeatInfo -> (!Roles.WEREWOLVES.equals(playerSeatInfo.getRole()) &&
                        !Roles.VILLAGER.equals(playerSeatInfo.getRole())))
                .filter(playerSeatInfo -> playerSeatInfo.isAlive()).count();
        if (wolfCount == 0) {
            roomState.setGameResult(GameResult.VILLAGERS_WIN);
        }
        if (villagerCount == 0 || unCommonCount == 0) {
            roomState.setGameResult(GameResult.WEREWOLVES_WIN);
        }
    }


    @Override
    public RoomStateData voted() {
        PlayerSeatInfo seatInfo = roomState.getPlaySeatInfoBySeatNumber(number);
        seatInfo.setAlive(false);
        gameEndingCalculate();
        return roomState;
    }

    @Override
    public RoomStateData killed() {
        PlayerSeatInfo seatInfo = roomState.getPlaySeatInfoBySeatNumber(number);
        seatInfo.setAlive(false);
        gameEndingCalculate();
        return roomState;
    }

    public void resolveCommonDisplayInfo(PlayerDisplayInfo displayInfo) {
        //注入昨夜信息
        NightRecord lastNightRecord = this.roomState.getLastNightRecord();
        displayInfo.setNightRecord(lastNightRecord);
        //除自己以外的玩家均覆盖身份
        displayInfo.setPlayerInfo(roomState.getPlayerSeatInfo().get(number - 1));
        List<PlayerSeatInfo> playerSeatInfos = PlayerRoleMask.maskPlayerRole(roomState.getPlayerSeatInfo(), Arrays.asList(number));
        //游戏结束则不需要隐藏
        if(RoomStatus.GAME_OVER.equals(roomState.getStatus())){
            playerSeatInfos = roomState.getPlayerSeatInfo();
        }
        displayInfo.setPlayerSeatInfoList(playerSeatInfos);
        displayInfo.setAcceptableEventTypeList(new ArrayList<>());
        if (this.roomState.getPlaySeatInfoBySeatNumber(number).isAlive()) {
            if (RoomStatus.VOTING.equals(roomState.getStatus())) {
                if (!roomState.getLastDaytimeRecord().isDaytimeVoted(number)) {
                    displayInfo.addAcceptableEventType(PlayerEventType.DAYTIME_VOTE);
                } else if (roomState.getLastDaytimeRecord().getDiedNumber() != null) {
                    //如果已经投票死人，说明投票有结果了.公布白天投票信息
                    displayInfo.setDaytimeRecord(roomState.getLastDaytimeRecord());
                }
            }
            //TODO PK环节
        }
    }
}

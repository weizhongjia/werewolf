package com.msh.room.model.role.impl;

import com.msh.room.dto.event.PlayerEvent;
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
import java.util.Map;

import static com.msh.room.dto.event.PlayerEventType.DAYTIME_VOTE;
import static com.msh.room.dto.event.PlayerEventType.PK_VOTE;

/**
 * Created by zhangruiqian on 2017/5/18.
 */
public abstract class AssignedPlayer extends CommonPlayer {
    public AssignedPlayer(RoomStateData roomState, int number) {
        super(roomState, number);
    }

    @Override
    public RoomStateData resolveEvent(PlayerEvent event) {
        PlayerDisplayInfo displayInfo = this.displayInfo();
        if (!displayInfo.getAcceptableEventTypeList().contains(event.getEventType())) {
            throw new RoomBusinessException("非法的事件类型");
        }
        switch (event.getEventType()) {
            case DAYTIME_VOTE:
                return votePlayer(event);
            case PK_VOTE:
                return pkVotePlayer(event);
            case HUNTER_SHOOT:
                return hunterShoot(event);
            default:
                return roomState;
        }
    }

    private RoomStateData hunterShoot(PlayerEvent event) {
        if (!Roles.HUNTER.equals(roomState.getPlaySeatInfoBySeatNumber(number).getRole())) {
            throw new RoomBusinessException("你不是猎人无法开枪");
        }
        //重新构造自己(再次证明这里不应该由Player对象来处理逻辑)
        Hunter hunter = (Hunter) PlayerRoleFactory.createPlayerInstance(roomState, number);
        return hunter.shoot(event.getShootNumber());
    }

    protected RoomStateData pkVotePlayer(PlayerEvent event) {
        Integer voteNumber = event.getPkVoteNumber();
        if (!roomState.getPlaySeatInfoBySeatNumber(this.number).isAlive()) {
            throw new RoomBusinessException("您已死亡，无法投票");
        }
        DaytimeRecord lastDaytimeRecord = roomState.getLastDaytimeRecord();
        if (lastDaytimeRecord.isPKVoted(this.number)) {
            throw new RoomBusinessException("您已经投票，请勿重复投票");
        }
        Map<Integer, List<Integer>> lastPKRecord = lastDaytimeRecord.lastPKRecord();
        lastDaytimeRecord.addPKVote(this.number, voteNumber);
        if (lastDaytimeRecord.isPKVoteComplete(roomState.getAliveCount() - lastPKRecord.size())) {
            return pkVoteResult(lastDaytimeRecord);
        }
        return roomState;
    }

    private RoomStateData pkVoteResult(DaytimeRecord lastDaytimeRecord) {
        DaytimeRecord daytimeRecord = roomState.getLastDaytimeRecord();
        List<Integer> voteResult = lastDaytimeRecord.resolvePKVoteResult();
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
            Integer number = voteResult.get(0);
            daytimeRecord.setDiedNumber(number);
            CommonPlayer player = PlayerRoleFactory.createPlayerInstance(roomState, number);
            return player.voted();
        }
    }

    protected RoomStateData votePlayer(PlayerEvent event) {
        Integer voteNumber = event.getDaytimeVoteNumber();
        if (!roomState.getPlaySeatInfoBySeatNumber(this.number).isAlive()) {
            throw new RoomBusinessException("您已死亡，无法投票");
        }
        if (voteNumber != 0 && !roomState.getPlaySeatInfoBySeatNumber(voteNumber).isAlive()) {
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
        List<Integer> voteResult = lastDaytimeRecord.resolveVoteResult();
        //如果有平票
        if (voteResult.size() > 1) {
            daytimeRecord.addNewPk();
            for (Integer number : voteResult) {
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
        displayInfo.setPlayerSeatInfoList(playerSeatInfos);

        displayInfo.setAcceptableEventTypeList(new ArrayList<>());
        if (RoomStatus.VOTING.equals(roomState.getStatus())) {
            if (!roomState.getLastDaytimeRecord().isDaytimeVoted(number)
                    && this.roomState.getPlaySeatInfoBySeatNumber(number).isAlive()) {
                displayInfo.addAcceptableEventType(DAYTIME_VOTE);
            } else if (roomState.getLastDaytimeRecord().getDiedNumber() != null) {
                //如果已经投票死人，说明投票有结果了.公布白天投票信息
                displayInfo.setDaytimeRecord(roomState.getLastDaytimeRecord());
            }
        }
        if (RoomStatus.PK.equals(roomState.getStatus())) {
            //PK环节说明投票结束(无论投票还是pk投票)，也公布白天信息
            displayInfo.setDaytimeRecord(roomState.getLastDaytimeRecord());
        }
        if (RoomStatus.PK_VOTING.equals(roomState.getStatus())) {
            Map<Integer, List<Integer>> pkRecord = roomState.getLastDaytimeRecord().lastPKRecord();
            if (!pkRecord.containsKey(number)
                    && this.roomState.getPlaySeatInfoBySeatNumber(number).isAlive() && !roomState.getLastDaytimeRecord().isPKVoted(number)) {
                displayInfo.addAcceptableEventType(PK_VOTE);
            }
            if (roomState.getLastDaytimeRecord().getDiedNumber() != null) {
                //如果已经投票死人(无论是否无人死亡)，说明投票有结果了.公布白天投票信息
                displayInfo.setDaytimeRecord(roomState.getLastDaytimeRecord());
            }
        }
        if (RoomStatus.HUNTER_SHOOT.equals(roomState.getStatus())) {
            //进入猎人时间后，可以放开白天信息。猎人投票死亡需要公布票型，猎人夜晚死亡白天为空信息
            displayInfo.setDaytimeRecord(roomState.getLastDaytimeRecord());
        }


        //游戏结束则不需要隐藏
        if (RoomStatus.GAME_OVER.equals(roomState.getStatus())) {
            displayInfo.setPlayerSeatInfoList(roomState.getPlayerSeatInfo());
        }
    }
}

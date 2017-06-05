package com.msh.room.model.role.impl;

import com.msh.room.dto.event.PlayerEvent;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.dto.room.RoomStatus;
import com.msh.room.dto.room.record.DaytimeRecord;
import com.msh.room.dto.room.record.NightRecord;
import com.msh.room.dto.room.record.SheriffRecord;
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
//                return votePlayer(event);
            case PK_VOTE:
//                return pkVotePlayer(event);
            case HUNTER_SHOOT:
//                return hunterShoot(event);
            default:
                return roomState;
        }
    }


    protected boolean gameEndingCalculate() {
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
            return true;
        }
        if (villagerCount == 0 || unCommonCount == 0) {
            roomState.setGameResult(GameResult.WEREWOLVES_WIN);
            return true;
        }
        return false;
    }

    /**
     * 玩家被投票死亡
     *
     * @return
     */
    @Override
    public RoomStateData voted() {
        PlayerSeatInfo seatInfo = roomState.getPlaySeatInfoBySeatNumber(number);
        seatInfo.setAlive(false);
        if (!gameEndingCalculate()) {
            //处理警长死亡情况
            resolveSheriffDie();
        }
        return roomState;
    }

    protected void resolveSheriffDie() {
        if (roomState.isSheriff() && roomState.getSheriffRecord().getSheriff() == this.number) {
            //暂存当前状态
            roomState.getSheriffRecord().setAfterSwitchSheriff(roomState.getStatus());
            roomState.setStatus(RoomStatus.SHERIFF_SWITCH_TIME);
        }
    }

    /**
     * 玩家被杀死亡
     *
     * @return
     */
    @Override
    public RoomStateData killed() {
        PlayerSeatInfo seatInfo = roomState.getPlaySeatInfoBySeatNumber(number);
        seatInfo.setAlive(false);
        if (!gameEndingCalculate()) {
            //处理警长死亡情况
            resolveSheriffDie();
        }
        return roomState;
    }

    public void resolveCommonDisplayInfo(PlayerDisplayInfo displayInfo) {
        //注入昨夜信息
        displayInfo.setNightRecord(roomState.getLastNightRecord());
        //则直接展示警长信息，投票阶段单独处理隐藏。
        displayInfo.setSheriffRecord(roomState.getSheriffRecord());
        //除自己以外的玩家均覆盖身份
        displayInfo.setPlayerInfo(roomState.getPlayerSeatInfo().get(number - 1));
        List<PlayerSeatInfo> playerSeatInfos = PlayerRoleMask.maskPlayerRole(roomState.getPlayerSeatInfo(), Arrays.asList(number));
        displayInfo.setPlayerSeatInfoList(playerSeatInfos);
        displayInfo.setAcceptableEventTypeList(new ArrayList<>());
    }
}

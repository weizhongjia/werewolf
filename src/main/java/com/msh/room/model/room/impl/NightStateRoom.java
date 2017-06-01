package com.msh.room.model.room.impl;

import com.msh.room.dto.event.JudgeEvent;
import com.msh.room.dto.event.JudgeEventType;
import com.msh.room.dto.event.PlayerEvent;
import com.msh.room.dto.event.PlayerEventType;
import com.msh.room.dto.response.JudgeDisplayInfo;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.dto.room.RoomStatus;
import com.msh.room.dto.room.record.DaytimeRecord;
import com.msh.room.dto.room.record.NightRecord;
import com.msh.room.dto.room.record.SheriffRecord;
import com.msh.room.exception.RoomBusinessException;
import com.msh.room.model.role.CommonPlayer;
import com.msh.room.model.role.PlayerRoleFactory;
import com.msh.room.model.role.Roles;
import com.msh.room.model.role.impl.Seer;
import com.msh.room.model.role.impl.Witch;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangruiqian on 2017/5/25.
 */
public class NightStateRoom extends AbstractStateRoom {
    public NightStateRoom(RoomStateData data) {
        super(data);
    }

    @Override
    public RoomStateData resolveJudgeEvent(JudgeEvent event) {
        filterJudgeEventType(event);
        switch (event.getEventType()) {
            case WOLF_KILL:
                resolveWolfKill(event);
                break;
            case SEER_VERIFY:
                resolveSeerVerify(event);
                break;
            case FAKE_SEER_VERIFY:
                resolveFakeSeerVerify(event);
                break;
            case WITCH_SAVE:
                resolveWitchSave(event);
                break;
            case FAKE_WITCH_SAVE:
                resolveFakeWitchSave(event);
                break;
            case WITCH_POISON:
                resolveWitchPoison(event);
                break;
            case FAKE_WITCH_POISON:
                resolveFakeWitchPoison(event);
                break;
            case DAYTIME_COMING:
                resolveDaytimeComing(event);
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

    /**
     * 天亮了事件
     *
     * @param event
     */
    private void resolveDaytimeComing(JudgeEvent event) {
        //判断是否需要竞选警长
        RoomStatus roomStatus = sheriffCompetitionStatus();
        roomState.setStatus(roomStatus);
        //如果不需要竞选
        if (RoomStatus.DAYTIME.equals(roomStatus)) {
            roomState.addDaytimeRecord(new DaytimeRecord());
            //直接公布夜晚信息
            calculateNightInfo();
        }
    }

    public void calculateNightInfo() {
        NightRecord lastNightRecord = roomState.getLastNightRecord();
        List<Integer> dieList = new ArrayList();
        Integer wolfKilledSeat = lastNightRecord.getWolfKilledSeat();
        Integer witchSaved = lastNightRecord.getWitchSaved();
        if (wolfKilledSeat != 0 && wolfKilledSeat != witchSaved) {
            dieList.add(wolfKilledSeat);
        }
        if (lastNightRecord.getWitchPoisoned() != 0) {
            dieList.add(lastNightRecord.getWitchPoisoned());
        }
        lastNightRecord.setDiedNumber(dieList);
        for (Integer number : dieList) {
            CommonPlayer commonPlayer = PlayerRoleFactory.createPlayerInstance(roomState, number);
            commonPlayer.killed();
        }
    }

    /**
     * 判断是否需要上警
     *
     * @return
     */
    private RoomStatus sheriffCompetitionStatus() {
        if (roomState.isSheriff()) {
            return sheriffInfoResolve();
        } else {
            return RoomStatus.DAYTIME;
        }
    }

    private RoomStatus sheriffInfoResolve() {
        SheriffRecord sheriffRecord = roomState.getSheriffRecord();
        //还没有任何竞选信息,开始竞选
        if (sheriffRecord == null) {
            roomState.setSheriffRecord(new SheriffRecord());
            return RoomStatus.SHERIFF_REGISTER;
        } else {
            //已有结果,直接进入白天
            if (sheriffRecord.getSheriff() != null) {
                return RoomStatus.DAYTIME;
            } else {
                //有竞选信息，但没有结果。说明狼人自爆，且还没有流失警徽。继续竞选
                //TODO PK阶段自爆与竞选阶段自爆
                //没有考虑PK阶段自爆的逻辑，此处仅考虑狼人在上警阶段才能自爆
                return RoomStatus.SHERIFF_RUNNING;
            }
        }
    }


    private void resolveFakeWitchSave(JudgeEvent event) {
        Witch witch = getWitch();
        witch.fakeSave();
    }

    private void resolveFakeWitchPoison(JudgeEvent event) {
        Witch witch = getWitch();
        witch.fakePoison();
    }

    private void resolveWitchPoison(JudgeEvent event) {
        Witch witch = getWitch();
        witch.poison(event.getWitchPoisonNumber());
    }

    private void resolveWitchSave(JudgeEvent event) {
        Witch witch = getWitch();
        witch.save(event.isWitchSave());
    }

    private Witch getWitch() {
        int witchNumber = roomState.getFirstSeatByRole(Roles.WITCH);
        if (witchNumber == 0) {
            throw new RoomBusinessException("女巫不存在，无法救人");
        }
        return (Witch) PlayerRoleFactory.createPlayerInstance(roomState, witchNumber);
    }

    private void resolveSeerVerify(JudgeEvent event) {
        Integer seerVerifyNumber = event.getSeerVerifyNumber();
        int seerNumber = roomState.getAliveSeatByRole(Roles.SEER);
        if (seerNumber > 0) {
            Seer seer = (Seer) PlayerRoleFactory.createPlayerInstance(roomState, seerNumber);
            seer.verify(seerVerifyNumber);
        } else {
            throw new RoomBusinessException("预言家已死或没有预言家，无法验人");
        }
    }

    private void resolveFakeSeerVerify(JudgeEvent event) {
        int seerNumber = roomState.getFirstSeatByRole(Roles.SEER);
        Seer seer = (Seer) PlayerRoleFactory.createPlayerInstance(roomState, seerNumber);
        seer.fakeVerify();
    }

    private void resolveWolfKill(JudgeEvent event) {
        Integer wolfKillNumber = event.getWolfKillNumber();
        if (wolfKillNumber == 0 || roomState.getPlaySeatInfoBySeatNumber(wolfKillNumber).isAlive()) {
            roomState.getLastNightRecord().setWolfKilledSeat(wolfKillNumber);
        } else {
            throw new RoomBusinessException("无法杀掉一个死人");
        }

    }

    @Override
    public JudgeDisplayInfo displayJudgeInfo() {
        JudgeDisplayInfo displayInfo = judgeCommonDisplayInfo();
        /**
         * 夜晚法官需要的动作
         * TODO 逻辑不应该放在此处. 应该是拿到各个角色的acceptableEventType，然后来判断法官这边下一步应该的事件. 角色按GameConfig中顺序来逐个检查
         * TODO 后续前端将夜晚事件做成一体，允许法官整体一次提交。不需要这样做
         */
        NightRecord nightRecord = displayInfo.getNightRecord();
        if (nightRecord.getWolfKilledSeat() == null) {
            //狼刀
            displayInfo.addAcceptableEventType(JudgeEventType.WOLF_KILL);
        } else {
            if (nightRecord.getSeerVerify() == null) {
                //预言家
                int seerSeat = roomState.getAliveSeatByRole(Roles.SEER);
                //预言家已死则假装验人环节
                JudgeEventType type = (seerSeat > 0) ? JudgeEventType.SEER_VERIFY : JudgeEventType.FAKE_SEER_VERIFY;
                displayInfo.addAcceptableEventType(type);
            } else {
                int witchNumber = roomState.getAliveSeatByRole(Roles.WITCH);
                //女巫已死
                if (witchNumber < 0) {
                    if (nightRecord.getWitchSaved() == null)
                        displayInfo.addAcceptableEventType(JudgeEventType.FAKE_WITCH_SAVE);
                    else if (nightRecord.getWitchPoisoned() == null)
                        displayInfo.addAcceptableEventType(JudgeEventType.FAKE_WITCH_POISON);
                }
                Witch witch = (Witch) PlayerRoleFactory.createPlayerInstance(roomState, witchNumber);
                //根据女巫的状态判断
                PlayerDisplayInfo witchDisplayInfo = witch.displayInfo();
                List<PlayerEventType> acceptableEventTypeList = witchDisplayInfo.getAcceptableEventTypeList();
                //女巫有可能已经闭眼
                if (acceptableEventTypeList.size() > 0) {
                    JudgeEventType judgeEventType = JudgeEventType.valueOf(acceptableEventTypeList.get(0).name());
                    displayInfo.addAcceptableEventType(judgeEventType);
                }

            }
        }
        if (nightRecord.getSeerVerify() != null && nightRecord.getWolfKilledSeat() != null
                && nightRecord.getWitchSaved() != null && nightRecord.getWitchPoisoned() != null) {
            displayInfo.addAcceptableEventType(JudgeEventType.DAYTIME_COMING);
        }
        displayInfo.addAcceptableEventType(JudgeEventType.RESTART_GAME);
        displayInfo.addAcceptableEventType(JudgeEventType.DISBAND_GAME);
        return displayInfo;
    }

    @Override
    public RoomStateData resolvePlayerEvent(PlayerEvent event) {
        filterPlayerEventType(event);
        return roomState;
    }

    @Override
    public PlayerDisplayInfo displayPlayerInfo(int seatNumber) {
        return playerCommonDisplayInfo(seatNumber);
    }
}

package com.msh.room.model.role;

import com.msh.room.dto.event.JudgeEvent;
import com.msh.room.dto.event.JudgeEventType;
import com.msh.room.dto.event.PlayerEventType;
import com.msh.room.dto.response.JudgeDisplayInfo;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.dto.room.RoomStatus;
import com.msh.room.dto.room.record.DaytimeRecord;
import com.msh.room.dto.room.record.NightRecord;
import com.msh.room.dto.room.seat.PlayerSeatInfo;
import com.msh.room.dto.room.state.HunterState;
import com.msh.room.dto.room.state.MoronState;
import com.msh.room.dto.room.state.WitchState;
import com.msh.room.exception.RoomBusinessException;
import com.msh.room.model.role.impl.Seer;
import com.msh.room.model.role.impl.Witch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by zhangruiqian on 2017/5/5.
 */
public class Judge {
    private RoomStateData roomState;

    public Judge(RoomStateData roomState) {
        this.roomState = roomState;
    }

    //TODO roomStatus要用状态机，不然逻辑太乱.以后重构
    public RoomStateData resolveEvent(JudgeEvent event) throws RoomBusinessException {
        JudgeDisplayInfo judgeDisplayInfo = displayInfo();
        if (!judgeDisplayInfo.getAcceptableEventTypes().contains(event.getEventType())) {
            throw new RoomBusinessException("此时无法接受该事件类型:" + event.getEventType());
        }
        switch (event.getEventType()) {
            case CREATE_ROOM:
                resolveCreateRoomEvent(event);
                break;
            case COMPLETE_CREATE:
                resolveCompleteCreateEvent(event);
                break;
            case NIGHT_COMING:
                resolveNightComing();
                break;
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
            case DAYTIME_VOTING:
                resolveDaytimeVoting(event);
                break;
            case DAYTIME_PK_VOTING:
                resolvePKVoting(event);
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

    private void resolvePKVoting(JudgeEvent event) {
        if (this.roomState.getLastDaytimeRecord().getPkVotingRecord().size() < 2) {
            this.roomState.setStatus(RoomStatus.PK_VOTING);
        }
    }

    private void resolveGameEnding(JudgeEvent event) {
        this.roomState.setStatus(RoomStatus.GAME_OVER);
    }

    /**
     * 开始投票事件
     *
     * @param event
     */
    private void resolveDaytimeVoting(JudgeEvent event) {
        //开始投票
        this.roomState.setStatus(RoomStatus.VOTING);
    }

    /**
     * 天亮了事件
     *
     * @param event
     */
    private void resolveDaytimeComing(JudgeEvent event) {
        roomState.addDaytimeRecord(new DaytimeRecord());
        //判断是否需要竞选警长
        RoomStatus roomStatus = sheriffCompetitionStatus();
        //如果不需要竞选
        if (RoomStatus.DAYTIME.equals(roomStatus)) {
            //直接公布夜晚信息
            calculateNightInfo();
            //游戏结束逻辑
        }
        roomState.setStatus(roomStatus);
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
        return RoomStatus.DAYTIME;
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

    private void resolveNightComing() {
        roomState.addNightRecord(new NightRecord());
        roomState.setStatus(RoomStatus.NIGHT);
    }

    private void resolveDisbandGameEvent(JudgeEvent event) {
        //房间清空
        roomState = new RoomStateData();
        roomState.setStatus(RoomStatus.VACANCY);
        roomState.setRoomCode(event.getRoomCode());
    }

    private void resolveRestartGameEvent(JudgeEvent event) {
        //重置所有座位
        roomState.getPlayerSeatInfo().forEach(seatInfo -> {
            if (!seatInfo.isSeatAvailable()) {
                seatInfo.setRole(Roles.UNASSIGN);
                seatInfo.setAlive(true);
            }
        });
        roomState.setStatus(RoomStatus.CRATING);
    }

    private void resolveCompleteCreateEvent(JudgeEvent event) {
        if (RoomStatus.CRATING.equals(roomState.getStatus()) || allPlayersReady()) {
            assignRoleCard();
            initRoleState();
            roomState.setStatus(RoomStatus.CRATED);
        } else {
            throw new RoomBusinessException("房间目前状态还无法开始游戏");
        }
    }

    private void initRoleState() {
        if (roomState.getGameConfig().get(Roles.WITCH) == 1) {
            WitchState witchState = new WitchState();
            witchState.setAlive(true);
            witchState.setPoisonAvailable(true);
            witchState.setAntidoteAvailable(true);
            witchState.setSaveBySelf(false);
            roomState.setWitchState(witchState);
        }
        if (roomState.getGameConfig().get(Roles.HUNTER) == 1) {
            HunterState hunterState = new HunterState();
            hunterState.setAlive(true);
            hunterState.setShotAvailable(false);
            roomState.setHunterState(hunterState);
        }
        if (roomState.getGameConfig().get(Roles.MORON) == 1) {
            MoronState moronState = new MoronState();
            moronState.setBeanVoted(false);
            roomState.setMoronState(moronState);
        }
    }

    private void assignRoleCard() {
        Map<Roles, Integer> gameConfig = roomState.getGameConfig();
        List<Roles> cardList = new ArrayList<>();
        gameConfig.keySet().forEach(role -> {
            Integer count = gameConfig.get(role);
            for (int i = 0; i < count; i++) {
                cardList.add(role);
            }
        });

        if (cardList.size() != roomState.getPlayerSeatInfo().size()) {
            throw new RoomBusinessException("游戏配置与游戏人数不一致");
        }
        Random random = new Random();
        roomState.getPlayerSeatInfo().stream().forEach(seatInfo -> {
            int cardNum = 0;
            if (cardList.size() > 1) {
                cardNum = random.nextInt(cardList.size() - 1);
            }
            seatInfo.setRole(cardList.get(cardNum));
            cardList.remove(cardNum);
        });
    }

    private boolean allPlayersReady() {
        long count = roomState.getPlayerSeatInfo().stream().filter(seatInfo -> seatInfo.isSeatAvailable()).count();
        if (count == 0) {
            return true;
        }
        return false;
    }

    private void resolveCreateRoomEvent(JudgeEvent event) throws RoomBusinessException {
        if (roomState.getStatus().equals(RoomStatus.VACANCY)) {
            roomState.setStatus(RoomStatus.CRATING);
            roomState.setGameConfig(event.getGameConfig());
            //初始化座位
            roomState.setPlayerSeatInfo(initSeatInfo(event.getGameConfig()));
        } else {
            throw new RoomBusinessException("房间非空闲，无法创建游戏");
        }
    }

    private List<PlayerSeatInfo> initSeatInfo(Map<Roles, Integer> gameConfig) {
        List playerInfoList = new ArrayList();
        int seatNumber = 1;
        for (Roles role : gameConfig.keySet()) {
            Integer num = gameConfig.get(role);
            for (int i = 0; i < num; i++) {
                PlayerSeatInfo seatInfo = new PlayerSeatInfo(seatNumber, true);
                seatInfo.setAlive(false);
                seatInfo.setRole(Roles.NONE);
                playerInfoList.add(seatInfo);
                seatNumber++;
            }
        }
        return playerInfoList;
    }


    public JudgeDisplayInfo displayInfo() {
        JudgeDisplayInfo displayInfo = new JudgeDisplayInfo(roomState.getRoomCode());
        displayInfo.setStatus(roomState.getStatus());
        displayInfo.setPlayerSeatInfoList(roomState.getPlayerSeatInfo());
        return acceptableEventCalculate(displayInfo);
    }

    private JudgeDisplayInfo acceptableEventCalculate(JudgeDisplayInfo displayInfo) {
        //夜晚信息拿到无论是否为null
        NightRecord nightRecord = roomState.getLastNightRecord();
        displayInfo.setNightRecord(nightRecord);

        if (RoomStatus.VACANCY.equals(roomState.getStatus())) {
            displayInfo.addAcceptableEventType(JudgeEventType.CREATE_ROOM);
        }
        if (RoomStatus.CRATING.equals(roomState.getStatus())) {
            if (allPlayersReady()) {
                displayInfo.addAcceptableEventType(JudgeEventType.COMPLETE_CREATE);
            }
        }
        if (RoomStatus.CRATED.equals(roomState.getStatus())) {
            displayInfo.addAcceptableEventType(JudgeEventType.NIGHT_COMING);
        }
        if (RoomStatus.NIGHT.equals(roomState.getStatus())) {
            /**
             * 夜晚法官需要的动作
             * TODO 逻辑不应该放在此处. 应该是拿到各个角色的acceptableEventType，然后来判断法官这边下一步应该的事件. 角色按GameConfig中顺序来逐个检查
             * TODO 后续前端将夜晚事件做成一体，允许法官整体一次提交。不需要这样做
             */

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
        }
        displayInfo.setDaytimeRecord(roomState.getLastDaytimeRecord());
        //白天发言时间
        if (RoomStatus.DAYTIME.equals(roomState.getStatus())) {
            displayInfo.addAcceptableEventType(JudgeEventType.DAYTIME_VOTING);
        }
        if (RoomStatus.VOTING.equals(roomState.getStatus())) {
            //投票完成,有结果
            if (roomState.getLastDaytimeRecord().getDiedNumber() != null) {
                displayInfo.addAcceptableEventType(JudgeEventType.NIGHT_COMING);
            }
        }
        if (RoomStatus.PK.equals(roomState.getStatus())) {
            displayInfo.addAcceptableEventType(JudgeEventType.DAYTIME_PK_VOTING);
        }

        //游戏可以结束，所有仅留游戏结束事件
        if (roomState.getGameResult() != null) {
            displayInfo.setAcceptableEventTypes(new ArrayList<>());
            displayInfo.addAcceptableEventType(JudgeEventType.GAME_ENDING);
        }
        displayInfo.addAcceptableEventType(JudgeEventType.RESTART_GAME);
        displayInfo.addAcceptableEventType(JudgeEventType.DISBAND_GAME);

        return displayInfo;
    }
}

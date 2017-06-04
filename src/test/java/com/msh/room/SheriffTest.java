package com.msh.room;

import com.msh.room.cache.RoomStateDataRepository;
import com.msh.room.dto.event.JudgeEvent;
import com.msh.room.dto.event.JudgeEventType;
import com.msh.room.dto.event.PlayerEvent;
import com.msh.room.dto.event.PlayerEventType;
import com.msh.room.dto.response.JudgeDisplayInfo;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.dto.room.RoomStatus;
import com.msh.room.model.role.Roles;
import com.msh.room.model.room.Room;
import com.msh.room.model.room.RoomStateFactory;
import com.msh.room.service.RoomService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by zhangruiqian on 2017/6/1.
 */
public class SheriffTest {
    private RoomStateDataRepository repository;
    private String roomCode = "abc";
    private RoomService service = new RoomService();

    @Before
    public void setup() {
        repository = new RoomStateDataRepository();
        //房间空闲
        RoomStateData data = new RoomStateData();
        data.setRoomCode(roomCode);
        data.setStatus(RoomStatus.VACANCY);
        repository.putRoomStateData(roomCode, data);
        service.setDataRepository(repository);
        service.setRoomFactory(new RoomStateFactory());
        //create
        createRoom();
        //joinAll
        joinAll();
    }

    private void joinAll() {
        for (int i = 1; i <= 12; i++) {
            String userId = "Richard_" + i;
            PlayerEvent event = new PlayerEvent(PlayerEventType.JOIN_ROOM, i, userId);
            service.resolvePlayerEvent(event, roomCode);
        }
        JudgeEvent completeEvent = new JudgeEvent(roomCode, JudgeEventType.COMPLETE_CREATE);
        service.resolveJudgeEvent(completeEvent, roomCode);
    }

    private void createRoom() {
        JudgeEvent createRoomEvent = new JudgeEvent(roomCode, JudgeEventType.CREATE_ROOM);
        Map<Roles, Integer> gameConfig = new HashMap<>();
        gameConfig.put(Roles.VILLAGER, 4);
        gameConfig.put(Roles.WEREWOLVES, 4);
        gameConfig.put(Roles.WITCH, 1);
        gameConfig.put(Roles.HUNTER, 1);
        gameConfig.put(Roles.SEER, 1);
        gameConfig.put(Roles.MORON, 1);
        createRoomEvent.setGameConfig(gameConfig);
        /**
         * 创建时指定本局是否上警
         */
        createRoomEvent.setSheriffSwitch(true);
        service.resolveJudgeEvent(createRoomEvent, roomCode);
    }

    private void simpleKillVillagerNight() {
        JudgeEvent nightComingEvent = new JudgeEvent(roomCode, JudgeEventType.NIGHT_COMING);
        service.resolveJudgeEvent(nightComingEvent, roomCode);
        JudgeEvent wolfKillEvent = new JudgeEvent(roomCode, JudgeEventType.WOLF_KILL);
        //杀个民
        int seat = repository.loadRoomStateData(roomCode).getAliveSeatByRole(Roles.VILLAGER);
        wolfKillEvent.setWolfKillNumber(seat);
        service.resolveJudgeEvent(wolfKillEvent, roomCode);
        JudgeEvent seerVerifyEvent = new JudgeEvent(roomCode, JudgeEventType.SEER_VERIFY);
        //验个狼
        int wolfSeat = repository.loadRoomStateData(roomCode).getAliveSeatByRole(Roles.WEREWOLVES);
        seerVerifyEvent.setSeerVerifyNumber(wolfSeat);
        service.resolveJudgeEvent(seerVerifyEvent, roomCode);
        //没救
        JudgeEvent witchSaveEvent = new JudgeEvent(roomCode, JudgeEventType.WITCH_SAVE);
        witchSaveEvent.setWitchSave(false);
        service.resolveJudgeEvent(witchSaveEvent, roomCode);
        JudgeEvent witchPoisonEvent = new JudgeEvent(roomCode, JudgeEventType.WITCH_POISON);
        //也没毒
        witchPoisonEvent.setWitchPoisonNumber(0);
        service.resolveJudgeEvent(witchPoisonEvent, roomCode);
        return;
    }

    @Test
    public void testDaytimeWithSheriff() {
        simpleKillVillagerNight();
        JudgeEvent dayTimeEvent = new JudgeEvent(roomCode, JudgeEventType.DAYTIME_COMING);
        JudgeDisplayInfo judgeDisplayInfo = service.resolveJudgeEvent(dayTimeEvent, roomCode);
        //警上注册阶段
        assertEquals(RoomStatus.SHERIFF_REGISTER, judgeDisplayInfo.getStatus());
        assertNotNull(judgeDisplayInfo.getSheriffRecord());
        //法官可以发起开始警上发言
        assertEquals(Arrays.asList(JudgeEventType.SHERIFF_RUNNING, JudgeEventType.RESTART_GAME, JudgeEventType.DISBAND_GAME),
                judgeDisplayInfo.getAcceptableEventTypes());
        //每个人都有上警选项
        for (int i = 1; i < 13; i++) {
            PlayerDisplayInfo playerDisplayInfo = service.getPlayerDisplayResult(roomCode, i);
            assertEquals(Arrays.asList(PlayerEventType.SHERIFF_REGISTER), playerDisplayInfo.getAcceptableEventTypeList());
        }

        //前六个上警
        for (int i = 1; i < 7; i++) {
            PlayerDisplayInfo info = service.getPlayerDisplayResult(roomCode, i);
            //每个玩家可以使用上警事件进行自己的上警
            PlayerEvent playerEvent
                    = new PlayerEvent(PlayerEventType.SHERIFF_REGISTER, i, info.getPlayerInfo().getUserID());
            PlayerDisplayInfo result = service.resolvePlayerEvent(playerEvent, roomCode);
            assertTrue(result.getSheriffRecord().getSheriffRegisterList().contains(i));
            assertTrue(result.getSheriffRecord().getVotingRecord().keySet().contains(i));
        }
        for (int i = 7; i < 13; i++) {
            PlayerDisplayInfo info = service.getPlayerDisplayResult(roomCode, i);
        }
        //法官也可以用SHERIFF_RUNNING事件来指定上警人员,使用sheriffApplyList字段即可
        JudgeEvent sheriffRunning = new JudgeEvent(roomCode, JudgeEventType.SHERIFF_RUNNING);
        JudgeDisplayInfo judgeDisplayResult = service.resolveJudgeEvent(sheriffRunning, roomCode);
        //此时为上警发言阶段
        assertEquals(RoomStatus.SHERIFF_RUNNING, judgeDisplayResult.getStatus());
        //法官可以发起上警投票
        assertEquals(Arrays.asList(JudgeEventType.SHERIFF_VOTEING, JudgeEventType.RESTART_GAME, JudgeEventType.DISBAND_GAME),
                judgeDisplayResult.getAcceptableEventTypes());
    }

    /**
     * 该方法测试仅一人上警的情况
     */
    @Test
    public void testSheriffVoteWithOnlyOneRunning() {
        simpleKillVillagerNight();
        JudgeEvent dayTimeEvent = new JudgeEvent(roomCode, JudgeEventType.DAYTIME_COMING);
        service.resolveJudgeEvent(dayTimeEvent, roomCode);
        //一号上警
        PlayerDisplayInfo info = service.getPlayerDisplayResult(roomCode, 1);
        PlayerEvent playerEvent
                = new PlayerEvent(PlayerEventType.SHERIFF_REGISTER, 1, info.getPlayerInfo().getUserID());
        service.resolvePlayerEvent(playerEvent, roomCode);
        JudgeEvent sheriffRunning = new JudgeEvent(roomCode, JudgeEventType.SHERIFF_RUNNING);
        service.resolveJudgeEvent(sheriffRunning, roomCode);

        JudgeEvent sheriffVoting = new JudgeEvent(roomCode, JudgeEventType.SHERIFF_VOTEING);
        JudgeDisplayInfo judgeDisplayResult = service.resolveJudgeEvent(sheriffVoting, roomCode);
        assertEquals(RoomStatus.DAYTIME, judgeDisplayResult.getStatus());
        assertEquals(Integer.valueOf(1), judgeDisplayResult.getSheriffRecord().getSheriff());

        for (int i = 1; i < 13; i++) {
            PlayerDisplayInfo playerDisplayInfo = service.getPlayerDisplayResult(roomCode, i);
            assertEquals(Integer.valueOf(1), playerDisplayInfo.getSheriffRecord().getSheriff());
        }
    }

    /**
     * 测试发言阶段退选的情况
     */
    @Test
    public void testSheriffUnRegister() {
        simpleKillVillagerNight();
        JudgeEvent dayTimeEvent = new JudgeEvent(roomCode, JudgeEventType.DAYTIME_COMING);
        service.resolveJudgeEvent(dayTimeEvent, roomCode);
        //一号上警
        PlayerDisplayInfo info = service.getPlayerDisplayResult(roomCode, 1);
        PlayerEvent playerEvent
                = new PlayerEvent(PlayerEventType.SHERIFF_REGISTER, 1, info.getPlayerInfo().getUserID());
        service.resolvePlayerEvent(playerEvent, roomCode);
        JudgeEvent sheriffRunning = new JudgeEvent(roomCode, JudgeEventType.SHERIFF_RUNNING);
        service.resolveJudgeEvent(sheriffRunning, roomCode);
        //该玩家可以退选
        PlayerDisplayInfo playerDisplayInfo1 = service.getPlayerDisplayResult(roomCode, 1);
        assertEquals(Arrays.asList(PlayerEventType.SHERIFF_UNREGISTER),
                playerDisplayInfo1.getAcceptableEventTypeList());
        //直接退选
        PlayerEvent unregister = new PlayerEvent(PlayerEventType.SHERIFF_UNREGISTER, 1,
                playerDisplayInfo1.getPlayerInfo().getUserID());
        PlayerDisplayInfo playerResult = service.resolvePlayerEvent(unregister, roomCode);
        //竞选列表不在包含该玩家
        assertFalse(playerResult.getSheriffRecord().getVotingRecord().containsKey(1));
        //此时无人竞选，仍可以发起投票。会直接无人当选
        JudgeEvent sheriffVoting = new JudgeEvent(roomCode, JudgeEventType.SHERIFF_VOTEING);
        JudgeDisplayInfo judgeDisplayResult = service.resolveJudgeEvent(sheriffVoting, roomCode);
        assertEquals(RoomStatus.DAYTIME, judgeDisplayResult.getStatus());
        //无人当选
        assertEquals(Integer.valueOf(0), judgeDisplayResult.getSheriffRecord().getSheriff());

        for (int i = 1; i < 13; i++) {
            PlayerDisplayInfo playerDisplayInfo = service.getPlayerDisplayResult(roomCode, i);
            //无人当选
            assertEquals(Integer.valueOf(0), playerDisplayInfo.getSheriffRecord().getSheriff());
        }
    }

    @Test
    public void testSheriffVote() {
        simpleKillVillagerNight();
        JudgeEvent dayTimeEvent = new JudgeEvent(roomCode, JudgeEventType.DAYTIME_COMING);
        service.resolveJudgeEvent(dayTimeEvent, roomCode);
        //一号上警
        PlayerDisplayInfo info1 = service.getPlayerDisplayResult(roomCode, 1);
        PlayerEvent registerEvent
                = new PlayerEvent(PlayerEventType.SHERIFF_REGISTER, 1, info1.getPlayerInfo().getUserID());
        service.resolvePlayerEvent(registerEvent, roomCode);
        //二号上警
        PlayerDisplayInfo info2 = service.getPlayerDisplayResult(roomCode, 2);
        PlayerEvent registerEvent2
                = new PlayerEvent(PlayerEventType.SHERIFF_REGISTER, 2, info2.getPlayerInfo().getUserID());
        service.resolvePlayerEvent(registerEvent2, roomCode);

        JudgeEvent sheriffRunning = new JudgeEvent(roomCode, JudgeEventType.SHERIFF_RUNNING);
        service.resolveJudgeEvent(sheriffRunning, roomCode);

        JudgeEvent sheriffVoting = new JudgeEvent(roomCode, JudgeEventType.SHERIFF_VOTEING);
        JudgeDisplayInfo judgeDisplayResult = service.resolveJudgeEvent(sheriffVoting, roomCode);
        assertEquals(RoomStatus.SHERIFF_VOTING, judgeDisplayResult.getStatus());
        for (int i = 1; i < 13; i++) {
            PlayerDisplayInfo playerDisplayInfo = service.getPlayerDisplayResult(roomCode, i);
            if (i == 1 || i == 2) {
                //1、2号不能投票
                assertFalse(playerDisplayInfo.getAcceptableEventTypeList().contains(PlayerEventType.SHERIFF_VOTE));
            } else {
                assertTrue(playerDisplayInfo.getAcceptableEventTypeList().contains(PlayerEventType.SHERIFF_VOTE));
                //可以投票的人目前看不到票型,但能看到目前竞选的人(key)
                Map<Integer, List<Integer>> votingRecord = playerDisplayInfo.getSheriffRecord().getVotingRecord();
                votingRecord.values().forEach(Assert::assertNull);

                PlayerEvent voteEvent = new PlayerEvent(PlayerEventType.SHERIFF_VOTE, i, "Richard_" + i);
                Random random = new Random();
                //随机投一个
                voteEvent.setSheriffVoteNumber(random.nextInt(3));
                PlayerDisplayInfo displayInfo = service.resolvePlayerEvent(voteEvent, roomCode);
                assertFalse(displayInfo.getAcceptableEventTypeList().contains(PlayerEventType.SHERIFF_VOTE));
            }
        }
        JudgeDisplayInfo judgeInfo = service.getJudgeDisplayResult(roomCode);
        //输出票型
        Map<Integer, List<Integer>> votingRecord = judgeInfo.getSheriffRecord().getVotingRecord();
        for (Integer key : votingRecord.keySet()) {
            System.out.print(key + "[");
            votingRecord.get(key).parallelStream().forEach(integer -> System.out.print(integer + " "));
            System.out.println("]");
        }
        //输出胜出者
        List<Integer> voteResult = repository.loadRoomStateData(roomCode).getSheriffRecord().resolveVoteResult();
        voteResult.forEach(i -> System.out.println("[" + i + "]"));
        //三种情况
        if (voteResult.size() > 1) {
            //多人胜出,进入PK阶段
            assertEquals(RoomStatus.SHERIFF_PK, judgeInfo.getStatus());
            for (Integer number : voteResult) {
                //PK列表中包含胜出者
                assertTrue(judgeInfo.getSheriffRecord().getPkVotingRecord().get(0).containsKey(number));
            }
        } else if (voteResult.size() == 1) {
            //单人胜出，任命为警长。进入天亮发言阶段
            assertEquals(RoomStatus.DAYTIME, judgeInfo.getStatus());
            assertEquals(voteResult.get(0), judgeInfo.getSheriffRecord().getSheriff());
        } else {
            //无人胜出（所有人弃票）,没有警长。进入天亮发言阶段
            assertEquals(RoomStatus.DAYTIME, judgeInfo.getStatus());
            assertEquals(Integer.valueOf(0), judgeInfo.getSheriffRecord().getSheriff());
        }

    }

    /**
     * 测试PK投票
     */
    @Test
    public void testSheriffPKVote() {
        simpleKillVillagerNight();
        JudgeEvent dayTimeEvent = new JudgeEvent(roomCode, JudgeEventType.DAYTIME_COMING);
        service.resolveJudgeEvent(dayTimeEvent, roomCode);
        //一号上警
        PlayerDisplayInfo info1 = service.getPlayerDisplayResult(roomCode, 1);
        PlayerEvent registerEvent
                = new PlayerEvent(PlayerEventType.SHERIFF_REGISTER, 1, info1.getPlayerInfo().getUserID());
        service.resolvePlayerEvent(registerEvent, roomCode);
        //二号上警
        PlayerDisplayInfo info2 = service.getPlayerDisplayResult(roomCode, 2);
        PlayerEvent registerEvent2
                = new PlayerEvent(PlayerEventType.SHERIFF_REGISTER, 2, info2.getPlayerInfo().getUserID());
        service.resolvePlayerEvent(registerEvent2, roomCode);

        //三号上警
        PlayerDisplayInfo info3 = service.getPlayerDisplayResult(roomCode, 3);
        PlayerEvent registerEvent3
                = new PlayerEvent(PlayerEventType.SHERIFF_REGISTER, 3, info3.getPlayerInfo().getUserID());
        service.resolvePlayerEvent(registerEvent3, roomCode);

        JudgeEvent sheriffRunning = new JudgeEvent(roomCode, JudgeEventType.SHERIFF_RUNNING);
        service.resolveJudgeEvent(sheriffRunning, roomCode);

        JudgeEvent sheriffVoting = new JudgeEvent(roomCode, JudgeEventType.SHERIFF_VOTEING);
        service.resolveJudgeEvent(sheriffVoting, roomCode);
        for (int i = 1; i < 13; i++) {
            if (i == 1 || i == 2 || i == 3) {
                //1、2、3号不投票
            } else {
                PlayerEvent voteEvent = new PlayerEvent(PlayerEventType.SHERIFF_VOTE, i, "Richard_" + i);
                //奇数偶数分开投一个
                voteEvent.setSheriffVoteNumber(i % 2 + 1);
                if (i == 4) {
                    //四号弃票,会出现平票
                    voteEvent.setSheriffVoteNumber(0);
                }
                PlayerDisplayInfo displayInfo = service.resolvePlayerEvent(voteEvent, roomCode);
                assertFalse(displayInfo.getAcceptableEventTypeList().contains(PlayerEventType.SHERIFF_VOTE));
            }
        }
        JudgeDisplayInfo judgeInfo = service.getJudgeDisplayResult(roomCode);
        //输出票型
        Map<Integer, List<Integer>> votingRecord = judgeInfo.getSheriffRecord().getVotingRecord();
        for (Integer key : votingRecord.keySet()) {
            System.out.print(key + "[");
            votingRecord.get(key).parallelStream().forEach(integer -> System.out.print(integer + " "));
            System.out.println("]");
        }
        List<Integer> voteResult = repository.loadRoomStateData(roomCode).getSheriffRecord().resolveVoteResult();
        voteResult.forEach(i -> System.out.println("[" + i + "]"));
        //当前为PK发言阶段
        assertEquals(RoomStatus.SHERIFF_PK, judgeInfo.getStatus());
        assertTrue(judgeInfo.getAcceptableEventTypes().contains(JudgeEventType.SHERIFF_PK_VOTEING));
        //法官发起进入PK投票
        JudgeEvent pkVotingEvent = new JudgeEvent(roomCode, JudgeEventType.SHERIFF_PK_VOTEING);
        JudgeDisplayInfo judgeDisplayInfo = service.resolveJudgeEvent(pkVotingEvent, roomCode);
        assertEquals(RoomStatus.SHERIFF_PK_VOTING, judgeDisplayInfo.getStatus());
        assertTrue(judgeDisplayInfo.getSheriffRecord().lastPKVotingRecord().keySet().contains(1));
        assertTrue(judgeDisplayInfo.getSheriffRecord().lastPKVotingRecord().keySet().contains(2));
        for (int i = 1; i < 13; i++) {
            PlayerDisplayInfo displayResult = service.getPlayerDisplayResult(roomCode, i);
            if (i != 1 && i != 2) {
                //非1、2号玩家可以投票，包括3号玩家
                assertTrue(displayResult.getAcceptableEventTypeList().contains(PlayerEventType.SHERIFF_PK_VOTE));
                //看不到票型
                displayResult.getSheriffRecord().lastPKVotingRecord().values().forEach(value -> assertNull(value));
                PlayerEvent pkVoteEvent = new PlayerEvent(PlayerEventType.SHERIFF_PK_VOTE, i, displayResult.getPlayerInfo().getUserID());
                //奇数偶数投票不一样,会再次平票
                pkVoteEvent.setSheriffPKVoteNumber(i % 2 + 1);
                //投票
                PlayerDisplayInfo playerDisplayInfo = service.resolvePlayerEvent(pkVoteEvent, roomCode);
                //投票后即没有了投票选项
                assertFalse(playerDisplayInfo.getAcceptableEventTypeList().contains(PlayerEventType.SHERIFF_PK_VOTE));
                //可以看到票型了
                playerDisplayInfo.getSheriffRecord().lastPKVotingRecord().values().forEach(value -> assertNotNull(value));
            } else {
                //1、2号PK玩家可以看到票型，且不能投票
                assertFalse(displayResult.getAcceptableEventTypeList().contains(PlayerEventType.SHERIFF_PK_VOTE));
                displayResult.getSheriffRecord().lastPKVotingRecord().values().forEach(value -> assertNotNull(value));
            }
        }
        //此时再次进入了PK发言阶段，法官可以再次发起PK投票
        assertEquals(RoomStatus.SHERIFF_PK, repository.loadRoomStateData(roomCode).getStatus());
        service.resolveJudgeEvent(pkVotingEvent, roomCode);

        for (int i = 1; i < 13; i++) {
            PlayerDisplayInfo displayResult = service.getPlayerDisplayResult(roomCode, i);
            if (i != 1 && i != 2) {
                assertTrue(displayResult.getAcceptableEventTypeList().contains(PlayerEventType.SHERIFF_PK_VOTE));
                displayResult.getSheriffRecord().lastPKVotingRecord().values().forEach(value -> assertNull(value));
                PlayerEvent pkVoteEvent = new PlayerEvent(PlayerEventType.SHERIFF_PK_VOTE, i, displayResult.getPlayerInfo().getUserID());
                //奇数偶数投票不一样,再次平票(修改此处，可以验证不平票时情况)
                pkVoteEvent.setSheriffPKVoteNumber(i % 2 + 1);
//                pkVoteEvent.setSheriffPKVoteNumber(2);
                PlayerDisplayInfo playerDisplayInfo = service.resolvePlayerEvent(pkVoteEvent, roomCode);
                assertFalse(playerDisplayInfo.getAcceptableEventTypeList().contains(PlayerEventType.SHERIFF_PK_VOTE));
                playerDisplayInfo.getSheriffRecord().lastPKVotingRecord().values().forEach(value -> assertNotNull(value));
            }
        }
        //直接接入白天
        JudgeDisplayInfo judgeResult = service.getJudgeDisplayResult(roomCode);
        assertEquals(RoomStatus.DAYTIME, judgeResult.getStatus());
        //目前无人当选(根据前面修改票型，对应修改此处断言)
        assertEquals(Integer.valueOf(0), judgeResult.getSheriffRecord().getSheriff());
//        assertEquals(Integer.valueOf(2), judgeResult.getSheriffRecord().getSheriff());
    }
}

package com.msh.common.model;

import javax.persistence.*;

public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "room_id")
    private Integer roomId;

    private Integer state;

    private Integer size;

    private Integer judge;

    @Column(name = "wereworf_victory")
    private Integer wereworfVictory;

    private Integer roundnum;

    private Integer insettime;

    private Integer starttime;

    private Integer endtime;

    /**
     * @return id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return room_id
     */
    public Integer getRoomId() {
        return roomId;
    }

    /**
     * @param roomId
     */
    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    /**
     * @return state
     */
    public Integer getState() {
        return state;
    }

    /**
     * @param state
     */
    public void setState(Integer state) {
        this.state = state;
    }

    /**
     * @return size
     */
    public Integer getSize() {
        return size;
    }

    /**
     * @param size
     */
    public void setSize(Integer size) {
        this.size = size;
    }

    /**
     * @return judge
     */
    public Integer getJudge() {
        return judge;
    }

    /**
     * @param judge
     */
    public void setJudge(Integer judge) {
        this.judge = judge;
    }

    /**
     * @return wereworf_victory
     */
    public Integer getWereworfVictory() {
        return wereworfVictory;
    }

    /**
     * @param wereworfVictory
     */
    public void setWereworfVictory(Integer wereworfVictory) {
        this.wereworfVictory = wereworfVictory;
    }

    /**
     * @return roundnum
     */
    public Integer getRoundnum() {
        return roundnum;
    }

    /**
     * @param roundnum
     */
    public void setRoundnum(Integer roundnum) {
        this.roundnum = roundnum;
    }

    /**
     * @return insettime
     */
    public Integer getInsettime() {
        return insettime;
    }

    /**
     * @param insettime
     */
    public void setInsettime(Integer insettime) {
        this.insettime = insettime;
    }

    /**
     * @return starttime
     */
    public Integer getStarttime() {
        return starttime;
    }

    /**
     * @param starttime
     */
    public void setStarttime(Integer starttime) {
        this.starttime = starttime;
    }

    /**
     * @return endtime
     */
    public Integer getEndtime() {
        return endtime;
    }

    /**
     * @param endtime
     */
    public void setEndtime(Integer endtime) {
        this.endtime = endtime;
    }
}
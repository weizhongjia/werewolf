package com.msh.common.model;

import javax.persistence.*;

public class Round {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "room_id")
    private Integer roomId;

    private Integer roundno;

    @Column(name = "player_number")
    private Integer playerNumber;

    @Column(name = "player_state")
    private Integer playerState;

    private Integer inserttime;

    private Integer overtime;

    private Integer state;

    private Integer sheriffuid;

    private Integer sheriffpid;

    private Integer wolfkilluid;

    private Integer wolfkillpid;

    private Integer prophetuid;

    private Integer prophetpid;

    private Integer witchpoisonuid;

    private Integer witchpoisonpid;

    private Integer witchsaveduid;

    private Integer witchsavedpid;

    private Integer voteuid;

    private Integer votepid;

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
     * @return roundno
     */
    public Integer getRoundno() {
        return roundno;
    }

    /**
     * @param roundno
     */
    public void setRoundno(Integer roundno) {
        this.roundno = roundno;
    }

    /**
     * @return player_number
     */
    public Integer getPlayerNumber() {
        return playerNumber;
    }

    /**
     * @param playerNumber
     */
    public void setPlayerNumber(Integer playerNumber) {
        this.playerNumber = playerNumber;
    }

    /**
     * @return player_state
     */
    public Integer getPlayerState() {
        return playerState;
    }

    /**
     * @param playerState
     */
    public void setPlayerState(Integer playerState) {
        this.playerState = playerState;
    }

    /**
     * @return inserttime
     */
    public Integer getInserttime() {
        return inserttime;
    }

    /**
     * @param inserttime
     */
    public void setInserttime(Integer inserttime) {
        this.inserttime = inserttime;
    }

    /**
     * @return overtime
     */
    public Integer getOvertime() {
        return overtime;
    }

    /**
     * @param overtime
     */
    public void setOvertime(Integer overtime) {
        this.overtime = overtime;
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
     * @return sheriffuid
     */
    public Integer getSheriffuid() {
        return sheriffuid;
    }

    /**
     * @param sheriffuid
     */
    public void setSheriffuid(Integer sheriffuid) {
        this.sheriffuid = sheriffuid;
    }

    /**
     * @return sheriffpid
     */
    public Integer getSheriffpid() {
        return sheriffpid;
    }

    /**
     * @param sheriffpid
     */
    public void setSheriffpid(Integer sheriffpid) {
        this.sheriffpid = sheriffpid;
    }

    /**
     * @return wolfkilluid
     */
    public Integer getWolfkilluid() {
        return wolfkilluid;
    }

    /**
     * @param wolfkilluid
     */
    public void setWolfkilluid(Integer wolfkilluid) {
        this.wolfkilluid = wolfkilluid;
    }

    /**
     * @return wolfkillpid
     */
    public Integer getWolfkillpid() {
        return wolfkillpid;
    }

    /**
     * @param wolfkillpid
     */
    public void setWolfkillpid(Integer wolfkillpid) {
        this.wolfkillpid = wolfkillpid;
    }

    /**
     * @return prophetuid
     */
    public Integer getProphetuid() {
        return prophetuid;
    }

    /**
     * @param prophetuid
     */
    public void setProphetuid(Integer prophetuid) {
        this.prophetuid = prophetuid;
    }

    /**
     * @return prophetpid
     */
    public Integer getProphetpid() {
        return prophetpid;
    }

    /**
     * @param prophetpid
     */
    public void setProphetpid(Integer prophetpid) {
        this.prophetpid = prophetpid;
    }

    /**
     * @return witchpoisonuid
     */
    public Integer getWitchpoisonuid() {
        return witchpoisonuid;
    }

    /**
     * @param witchpoisonuid
     */
    public void setWitchpoisonuid(Integer witchpoisonuid) {
        this.witchpoisonuid = witchpoisonuid;
    }

    /**
     * @return witchpoisonpid
     */
    public Integer getWitchpoisonpid() {
        return witchpoisonpid;
    }

    /**
     * @param witchpoisonpid
     */
    public void setWitchpoisonpid(Integer witchpoisonpid) {
        this.witchpoisonpid = witchpoisonpid;
    }

    /**
     * @return witchsaveduid
     */
    public Integer getWitchsaveduid() {
        return witchsaveduid;
    }

    /**
     * @param witchsaveduid
     */
    public void setWitchsaveduid(Integer witchsaveduid) {
        this.witchsaveduid = witchsaveduid;
    }

    /**
     * @return witchsavedpid
     */
    public Integer getWitchsavedpid() {
        return witchsavedpid;
    }

    /**
     * @param witchsavedpid
     */
    public void setWitchsavedpid(Integer witchsavedpid) {
        this.witchsavedpid = witchsavedpid;
    }

    /**
     * @return voteuid
     */
    public Integer getVoteuid() {
        return voteuid;
    }

    /**
     * @param voteuid
     */
    public void setVoteuid(Integer voteuid) {
        this.voteuid = voteuid;
    }

    /**
     * @return votepid
     */
    public Integer getVotepid() {
        return votepid;
    }

    /**
     * @param votepid
     */
    public void setVotepid(Integer votepid) {
        this.votepid = votepid;
    }
}
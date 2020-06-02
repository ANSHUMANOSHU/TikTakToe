package com.media.tiktaktoe.entity;

public class Player {
    public String id;
    public String name;
    public Stats stats;
    public Status status;

    public Player() {
        stats = new Stats();
        status = new Status();
    }

    public Player(String id, String name, Stats stats, Status status) {
        this.id = id;
        this.name = name;
        this.stats = stats;
        this.status = status;
    }
}

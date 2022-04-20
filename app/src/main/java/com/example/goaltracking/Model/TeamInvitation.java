package com.example.goaltracking.Model;

public class TeamInvitation {
    String teamId;
    String from, to, teamName;

    public TeamInvitation() {
    }

    public TeamInvitation(String from, String to, String teamId, String teamName) {
        this.from = from;
        this.to = to;
        this.teamId = teamId;
        this.teamName = teamName;
    }

    public TeamInvitation(String from, String to, String teamName) {
        this.from = from;
        this.to = to;
        this.teamName = teamName;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }
}

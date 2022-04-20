package com.example.goaltracking.Model;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class User implements Serializable {
    String id, displayName, emailAddress, belongsToTeam, fcmToken;
    Boolean isTeamCreator;
    List<Goal> goals;

    public User() {

    }

    public User(String displayName, String emailAddress, String id, String belongsToTeam, Boolean isTeamCreator) {
        this.displayName = displayName;
        this.emailAddress = emailAddress;
        this.id = id;
        this.belongsToTeam = belongsToTeam;
        this.isTeamCreator = isTeamCreator;
    }

    public User(String displayName, String emailAddress, String belongsToTeam) {
        this.displayName = displayName;
        this.emailAddress = emailAddress;
        this.belongsToTeam = belongsToTeam;
    }

    public User(String displayName, String emailAddress) {
        this.displayName = displayName;
        this.emailAddress = emailAddress;
    }

    public List<Goal> getGoalsList() {
        return goals;
    }

    public void setGoalsList(List<Goal> goalsList) {
        this.goals = new ArrayList<>();
        this.goals.addAll(goalsList);
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public Boolean getIsTeamCreator() {
        return isTeamCreator;
    }

    public void setIsTeamCreator(Boolean isTeamCreator) {
        this.isTeamCreator = isTeamCreator;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBelongsToTeam() {
        return belongsToTeam;
    }

    public void setBelongsToTeam(String belongsToTeam) {
        this.belongsToTeam = belongsToTeam;
    }

    @Override
    public String toString() {
        return displayName + " " + emailAddress;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public int calculateLeaderboardPoints(String dateFromStr, String dateToStr){
        int sum = 0;
        for (int i = 0; i < goals.stream().count(); i++) {
            Goal g = goals.get(i);
            if (!dateFromStr.equals("") && !dateToStr.equals("")) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
                Date dateCreated, dateFrom, dateTo = null;
                try {
                    dateCreated = sdf.parse(g.getDateCreated());
                    dateFrom = sdf.parse(dateFromStr);
                    dateTo = sdf.parse(dateToStr);

                    if (dateCreated.after(dateFrom) && dateCreated.before(dateTo)) {
                        sum += g.calculatePoints();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else {
                sum += g.calculatePoints();
            }
        }
        return sum;
    }
}

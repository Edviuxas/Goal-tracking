package com.example.goaltracking.Model;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Goal implements Serializable {
    @com.google.firebase.database.Exclude
    String goalId;
    int value;
    int difficulty;
    List<Goal> okrGoals;
    List<String> sharedWith;
    String goal, idCreatedBy, dueDate, dateCreated, dateDone, notificationPeriod, notificationLastSent;
    Boolean isImportant, isDone, showProgressToEveryone;

    public Goal() {
        this.dateDone = null;
        this.notificationLastSent = "n/a";
        this.isDone = false;
    }

    public Goal(int value, String goal) {
        this.value = value;
        this.goal = goal;
        this.dateDone = null;
        this.notificationLastSent = "n/a";
        this.isDone = false;
    }

    public Goal(String goal, int value, String id, Boolean isImportant, String dueDate, String dateCreated, Boolean showProgressToEveryone, String notificationPeriod) {
        this.goal = goal;
        this.value = value;
        this.idCreatedBy = id;
        this.isImportant = isImportant;
        this.dueDate = dueDate;
        this.dateCreated = dateCreated;
        this.showProgressToEveryone = showProgressToEveryone;
        this.dateDone = null;
        this.notificationPeriod = notificationPeriod;
        this.notificationLastSent = "n/a";
        this.isDone = false;
    }

    public String getNotificationLastSent() {
        return notificationLastSent;
    }

    public void setNotificationLastSent(String notificationLastSent) {
        this.notificationLastSent = notificationLastSent;
    }

    public String getNotificationPeriod() {
        return notificationPeriod;
    }

    public void setNotificationPeriod(String notificationPeriod) {
        this.notificationPeriod = notificationPeriod;
    }

    public String getDateDone() {
        return dateDone;
    }

    public void setDateDone(String dateDone) {
        this.dateDone = dateDone;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public List<String> getSharedWith() {
        return sharedWith;
    }

    public void setSharedWith(List<String> sharedWith) {
        this.sharedWith = null;
        if (sharedWith != null) {
            this.sharedWith = new ArrayList<>();
            this.sharedWith.addAll(sharedWith);
        }
    }

    public Boolean getDone() {
        return isDone;
    }

    public void setDone(Boolean done) {
        isDone = done;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public List<Goal> getOkrGoals() {
        return okrGoals;
    }

    public void setOkrGoals(List<Goal> okrGoals) {
        this.okrGoals = new ArrayList<>();
        this.okrGoals.addAll(okrGoals);
    }

    public Boolean getShowProgressToEveryone() {
        return showProgressToEveryone;
    }

    public void setShowProgressToEveryone(Boolean showProgressToEveryone) {
        this.showProgressToEveryone = showProgressToEveryone;
    }

    public String getIdCreatedBy() {
        return idCreatedBy;
    }

    public void setIdCreatedBy(String idCreatedBy) {
        this.idCreatedBy = idCreatedBy;
    }

    @com.google.firebase.database.Exclude
    public String getGoalId() {
        return goalId;
    }

    @com.google.firebase.database.Exclude
    public void setGoalId(String goalId) {
        this.goalId = goalId;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public void setEmailCreatedBy(String idCreatedBy) {
        this.idCreatedBy = idCreatedBy;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public Boolean getIsImportant() {
        return isImportant;
    }

    public void setIsImportant(Boolean important) {
        isImportant = important;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    @Override
    public String toString() {
        return "Goal{" +
                "goalId='" + goalId + '\'' +
                ", goal='" + goal + '\'' +
                ", idCreatedBy='" + idCreatedBy + '\'' +
                '}';
    }

    @com.google.firebase.database.Exclude
    @RequiresApi(api = Build.VERSION_CODES.N)
    public Boolean isInProgress() {
        if (okrGoals != null) {
            for (int i = 0; i < okrGoals.stream().count(); i++) {
                if (okrGoals.get(i).getDone()) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }

    @com.google.firebase.database.Exclude
    @RequiresApi(api = Build.VERSION_CODES.N)
    public Boolean isOkrGoalsDone() {
        if (okrGoals != null) {
            for (int i = 0; i < okrGoals.stream().count(); i++) {
                if (!okrGoals.get(i).getDone())
                    return false;
            }
            return true;
        } else {
            return false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @com.google.firebase.database.Exclude
    public int calculatePoints(Date date) {
        int sum = 0;

        Date currentDate = date;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
//        Date currentDate = null;
//        try {
//            currentDate = sdf.parse(sdf.format(new Date()));
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }

        for (int i = 0; i < okrGoals.stream().count(); i++) {
            Date dateDone = null;
            Date goalDueDate = null;
            try {
                String dateDoneString = okrGoals.get(i).getDateDone();
                goalDueDate = sdf.parse(okrGoals.get(i).getDueDate());
                if (dateDoneString != null) {
                    dateDone = sdf.parse(dateDoneString);
                    long timeDiff = dateDone.getTime() - goalDueDate.getTime();
                    int daysDiff = (int) (timeDiff / (1000 * 60 * 60* 24));

                    if (daysDiff > 0) {
                        sum -= 2 * daysDiff;
                        // goal was done later than due date
                    } else if (daysDiff < 0) {
                        sum += Math.abs(daysDiff) * okrGoals.get(i).getDifficulty();
                        // goal was done earlier than due date
                    }
                } else {
                    long diff = currentDate.getTime() - goalDueDate.getTime();
                    int days = (int) (diff / (1000 * 60 * 60* 24));
                    if (days > 0) {
                        sum -= 2 * days;
                    }
                    // goal has not been completed yet
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        /*
        TODO: taškų skaičiavimas:
        Jei užduotis atliekama anksčiau laiko, taškai = sudėtingumas * dienos iki due date
        Jei užduotis atliekama tą pačią dieną, kaip deadline - taškai = sudėtingumas / 2
        Jei vėluojama atlikti, minusas turėtų augti vis labiau, kuo labiau vėluojama
        */
        return sum;
    }
}

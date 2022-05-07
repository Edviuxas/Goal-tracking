package com.example.goaltracking;

import com.example.goaltracking.Model.Goal;
import com.example.goaltracking.Model.User;

import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.*;

public class UserUnitTest {
    @Test
    public void userPointsTimeRangeShouldReturnNegative45Points() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        Date currentDate = null;
        try {
            currentDate = sdf.parse(sdf.format(new Date("2022/05/07")));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        User user = new User();
        Goal goal1 = new Goal();
        Goal goal2 = new Goal();
        goal1.setDateCreated("2022/04/15");
        goal2.setDateCreated("2022/04/15");
        Goal subGoal1 = new Goal();
        Goal subGoal2 = new Goal();
        Goal subGoal3 = new Goal();
        Goal subGoal4 = new Goal();
        subGoal1.setDifficulty(1);
        subGoal2.setDifficulty(2);
        subGoal3.setDifficulty(3);
        subGoal4.setDifficulty(1);
        subGoal1.setDone(true);
        subGoal2.setDone(true);
        subGoal3.setDone(true);
        subGoal4.setDone(false);
        subGoal1.setDueDate("2022/04/21");
        subGoal2.setDueDate("2022/04/21");
        subGoal3.setDueDate("2022/04/21");
        subGoal4.setDueDate("2022/04/15");
        subGoal1.setDateDone("2022/04/20");
        subGoal2.setDateDone("2022/04/21");
        subGoal3.setDateDone("2022/04/22");
        List<Goal> subGoals1 = new ArrayList<>();
        List<Goal> subGoals2 = new ArrayList<>();
        List<Goal> allGoals = new ArrayList<>();
        subGoals1.add(subGoal1);
        subGoals1.add(subGoal2);
        subGoals2.add(subGoal3);
        subGoals2.add(subGoal4);
        goal1.setOkrGoals(subGoals1);
        goal2.setOkrGoals(subGoals2);
        allGoals.add(goal1);
        allGoals.add(goal2);
        user.setGoalsList(allGoals);
        assertEquals(-45, user.calculateLeaderboardPoints("2022/04/01", "2022/04/30", currentDate));
    }

    @Test
    public void userPointsAllGoalsShouldReturnNegative45Points() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        Date currentDate = null;
        try {
            currentDate = sdf.parse(sdf.format(new Date("2022/05/07")));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        User user = new User();
        Goal goal1 = new Goal();
        Goal goal2 = new Goal();
        goal1.setDateCreated("2022/04/15");
        goal2.setDateCreated("2022/04/15");
        Goal subGoal1 = new Goal();
        Goal subGoal2 = new Goal();
        Goal subGoal3 = new Goal();
        Goal subGoal4 = new Goal();
        subGoal1.setDifficulty(1);
        subGoal2.setDifficulty(2);
        subGoal3.setDifficulty(3);
        subGoal4.setDifficulty(1);
        subGoal1.setDone(true);
        subGoal2.setDone(true);
        subGoal3.setDone(true);
        subGoal4.setDone(false);
        subGoal1.setDueDate("2022/04/21");
        subGoal2.setDueDate("2022/04/21");
        subGoal3.setDueDate("2022/04/21");
        subGoal4.setDueDate("2022/04/15");
        subGoal1.setDateDone("2022/04/20");
        subGoal2.setDateDone("2022/04/21");
        subGoal3.setDateDone("2022/04/22");
        List<Goal> subGoals1 = new ArrayList<>();
        List<Goal> subGoals2 = new ArrayList<>();
        List<Goal> allGoals = new ArrayList<>();
        subGoals1.add(subGoal1);
        subGoals1.add(subGoal2);
        subGoals2.add(subGoal3);
        subGoals2.add(subGoal4);
        goal1.setOkrGoals(subGoals1);
        goal2.setOkrGoals(subGoals2);
        allGoals.add(goal1);
        allGoals.add(goal2);
        user.setGoalsList(allGoals);
        assertEquals(-45, user.calculateLeaderboardPoints("", "", currentDate));
    }

    @Test
    public void isEligibleToViewOkrProgressShouldReturnTrue() {
        Goal goal = new Goal();
        Goal subGoal1 = new Goal();
        subGoal1.setDifficulty(1);
        subGoal1.setDone(true);
        subGoal1.setDueDate("2022/04/21");
        List<Goal> subGoals = new ArrayList<>();
        subGoals.add(subGoal1);
        goal.setOkrGoals(subGoals);
        goal.setShowProgressToEveryone(true);
        goal.setIdCreatedBy("dkfjasdkfjhsdf");
        goal.setGoalId("123");
        List<Goal> goalsList = new ArrayList<>();
        goalsList.add(goal);
        User user = new User();
        user.setId("qqqq");
        user.setGoalsList(goalsList);
        assertEquals(true, user.isEligibleToViewOkrProgress("123"));
    }

    @Test
    public void isEligibleToViewOkrProgressShouldReturnFalse() {
        Goal goal = new Goal();
        Goal subGoal1 = new Goal();
        subGoal1.setDifficulty(1);
        subGoal1.setDone(true);
        subGoal1.setDueDate("2022/04/21");
        List<Goal> subGoals = new ArrayList<>();
        subGoals.add(subGoal1);
        goal.setOkrGoals(subGoals);
        goal.setShowProgressToEveryone(false);
        goal.setIdCreatedBy("dkfjasdkfjhsdf");
        goal.setGoalId("123");
        goal.setNotificationPeriod("None");
        goal.setValue(3);
        goal.setGoal("Clean the room");
        goal.setIsImportant(true);
        List<Goal> goalsList = new ArrayList<>();
        goalsList.add(goal);
        User user = new User();
        user.setId("qqqq");
        user.setGoalsList(goalsList);
        user.setIsTeamCreator(false);
        user.setDisplayName("Edvinas Sutkus");
        user.setEmailAddress("edvinas.sutkus@stud.vilniustech.lt");
        user.setBelongsToTeam("");
        assertEquals(false, user.isEligibleToViewOkrProgress("123"));
    }
}

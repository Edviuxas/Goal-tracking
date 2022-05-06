package com.example.goaltracking;

import com.example.goaltracking.Model.Goal;
import com.example.goaltracking.Model.User;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

public class UserUnitTest {
    @Test
    public void userPointsTimeRangeNegative3Points() {
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
        assertEquals(-37, user.calculateLeaderboardPoints("2022/04/01", "2022/04/30"));
    }

    @Test
    public void userPointsAllGoalsNegative3Points() {
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
        assertEquals(-37, user.calculateLeaderboardPoints("", ""));
    }
}

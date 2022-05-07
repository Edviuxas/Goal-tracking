package com.example.goaltracking;

import com.example.goaltracking.Model.Goal;

import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.*;

public class GoalUnitTest {
    @Test
    public void goalIsInProgressShouldReturnTrue() {
        Goal g = new Goal();
        Goal subGoal = new Goal();
        subGoal.setDone(true);
        List<Goal> subGoals = new ArrayList<>();
        subGoals.add(subGoal);
        g.setOkrGoals(subGoals);
        assertEquals(true, g.isInProgress());
    }

    @Test
    public void goalNotInProgressShouldReturnFalse() {
        Goal g = new Goal();
        Goal subGoal1 = new Goal();
        subGoal1.setDone(false);
        Goal subGoal2 = new Goal();
        subGoal2.setDone(false);
        List<Goal> subGoals = new ArrayList<>();
        subGoals.add(subGoal1);
        subGoals.add(subGoal2);
        g.setOkrGoals(subGoals);
        assertEquals(false, g.isInProgress());
    }

    @Test
    public void goalNoSubGoalsShouldReturnFalse() {
        Goal g = new Goal();
        assertEquals(false, g.isInProgress());
    }

    @Test
    public void goalOkrsDoneShouldReturnTrue() {
        Goal g = new Goal();
        Goal subGoal1 = new Goal();
        subGoal1.setDone(true);
        Goal subGoal2 = new Goal();
        subGoal2.setDone(true);
        List<Goal> subGoals = new ArrayList<>();
        subGoals.add(subGoal1);
        subGoals.add(subGoal2);
        g.setOkrGoals(subGoals);
        assertEquals(true, g.isOkrGoalsDone());
    }

    @Test
    public void goalOkrsNotDoneShouldReturnFalse() {
        Goal g = new Goal();
        Goal subGoal1 = new Goal();
        subGoal1.setDone(true);
        Goal subGoal2 = new Goal();
        subGoal2.setDone(false);
        List<Goal> subGoals = new ArrayList<>();
        subGoals.add(subGoal1);
        subGoals.add(subGoal2);
        g.setOkrGoals(subGoals);
        assertEquals(false, g.isOkrGoalsDone());
    }

    @Test
    public void goalNoOkrsShouldReturnFalse() {
        Goal g = new Goal();
        assertEquals(false, g.isOkrGoalsDone());
    }

    @Test
    public void goalPointsAllEarlyShouldReturn3Points() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        Date currentDate = null;
        try {
            currentDate = sdf.parse(sdf.format(new Date("2022/05/06")));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Goal g = new Goal();
        Goal subGoal1 = new Goal();
        Goal subGoal2 = new Goal();
        subGoal1.setDifficulty(1);
        subGoal2.setDifficulty(2);
        subGoal1.setDone(true);
        subGoal2.setDone(true);
        subGoal1.setDueDate("2022/04/21");
        subGoal2.setDueDate("2022/04/21");
        subGoal1.setDateDone("2022/04/20");
        subGoal2.setDateDone("2022/04/20");
        List<Goal> subGoals = new ArrayList<>();
        subGoals.add(subGoal1);
        subGoals.add(subGoal2);
        g.setOkrGoals(subGoals);
        assertEquals(3, g.calculatePoints(currentDate));
    }

    @Test
    public void goalPointsMixedShouldReturnNegative43() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        Date currentDate = null;
        try {
            currentDate = sdf.parse(sdf.format(new Date("2022/05/06")));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Goal g = new Goal();
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
        List<Goal> subGoals = new ArrayList<>();
        subGoals.add(subGoal1);
        subGoals.add(subGoal2);
        subGoals.add(subGoal3);
        subGoals.add(subGoal4);
        g.setOkrGoals(subGoals);
        assertEquals(-43, g.calculatePoints(currentDate));
    }
}

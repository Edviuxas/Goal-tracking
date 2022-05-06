package com.example.goaltracking;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.ListView;

import com.example.goaltracking.Adapters.OkrProgressAdapter;
import com.example.goaltracking.Model.Goal;
import com.example.goaltracking.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import io.sentry.ITransaction;
import io.sentry.Sentry;

public class OkrProgressActivity extends AppCompatActivity {

    FirebaseDatabase database = FirebaseDatabase.getInstance("https://goal-tracking-ccad5-default-rtdb.europe-west1.firebasedatabase.app/");
    DatabaseReference usersRef = database.getReference("Users");
    OkrProgressAdapter progressAdapter;
    String goalId;
    int okrId;
    List<User> allUsers = new ArrayList<>();
    List<User> usersThatHaveGoal = new ArrayList<>();
    ListView listViewOkrProgress;
    ITransaction transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_okr_progress);
        transaction = Sentry.startTransaction("dispayOkrProgress", "task");

        listViewOkrProgress = findViewById(R.id.listViewOkrProgress);

        Intent intent = getIntent();
        goalId = intent.getStringExtra("goalId");
        okrId = intent.getIntExtra("okrId", 0);

        ValueEventListener usersListener = new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    allUsers.clear();
                    usersThatHaveGoal.clear();
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        User user = singleSnapshot.getValue(User.class);
                        user.setId(singleSnapshot.getKey());

                        List<Goal> userGoals = new ArrayList<>();
                        DataSnapshot goalsSnapshot = singleSnapshot.child("Goals");
                        for (DataSnapshot ds : goalsSnapshot.getChildren()) {
                            Goal g = ds.getValue(Goal.class);
                            g.setGoalId(ds.getKey());
                            userGoals.add(g);
                        }
                        user.setGoalsList(userGoals);

                        allUsers.add(user);
                    }
                }

                for (int i = 0; i < allUsers.stream().count(); i++) {
                    User user = allUsers.get(i);
                    if (user.getGoalsList().stream().anyMatch(o -> goalId.equals(o.getGoalId()))) {
                        usersThatHaveGoal.add(user);
                    }
                }
                progressAdapter.notifyDataSetChanged();
                transaction.finish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        progressAdapter = new OkrProgressAdapter(usersThatHaveGoal, this, goalId, okrId);
        listViewOkrProgress.setAdapter(progressAdapter);

        usersRef.addValueEventListener(usersListener);
    }
}
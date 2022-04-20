package com.example.goaltracking;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.goaltracking.Adapters.OkrProgressAdapter;
import com.example.goaltracking.Model.Goal;
import com.example.goaltracking.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ViewGoalActivity extends AppCompatActivity {

    FirebaseDatabase database = FirebaseDatabase.getInstance("https://goal-tracking-ccad5-default-rtdb.europe-west1.firebasedatabase.app/");
    DatabaseReference usersRef = database.getReference("Users");
    Goal clickedGoal;
    User currentUser;
    TableLayout table;
    float scale;
    List<User> allUsers = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_goal);
        scale = getResources().getDisplayMetrics().density;

        clickedGoal = (Goal) getIntent().getSerializableExtra("clickedGoal");
        currentUser = (User) getIntent().getSerializableExtra("currentUser");
        table = findViewById(R.id.tableViewOKR);

//        ValueEventListener usersListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
//                        User user = singleSnapshot.getValue(User.class);
//                        user.setId(singleSnapshot.getKey());
//
//                        List<Goal> userGoals = new ArrayList<>();
//                        DataSnapshot goalsSnapshot = singleSnapshot.child("Goals");
//                        for (DataSnapshot ds : goalsSnapshot.getChildren()) {
//                            Goal g = ds.getValue(Goal.class);
//                            g.setGoalId(ds.getKey());
//                            userGoals.add(g);
//                        }
//                        user.setGoalsList(userGoals);
////                        user.calculateLeaderboardPoints();
//
//                        allUsers.add(user);
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                // Getting Post failed, log a message
////                Toast.makeText(AllGoalsActivity.this, databaseError.toException().toString(), Toast.LENGTH_SHORT).show();
//            }
//        };
//
//        usersRef.addValueEventListener(usersListener);
        populateTableWithOkrGoals(clickedGoal);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void populateTableWithOkrGoals(Goal goal) {
        List<Goal> okrGoals = goal.getOkrGoals();
        for (int i = 0; i < okrGoals.stream().count(); i++) {
            int trHeight = (int) (70 * scale + 0.5f);
            TableRow tableRow = new TableRow(this);
            tableRow.setTag(goal.getGoalId());
            tableRow.setId(i);
            
            tableRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TableRow clickedRow = (TableRow) view;
                    String goalId = clickedRow.getTag().toString();
                    int okrId = clickedRow.getId();

                    Boolean isEligibleToViewProgress = false;
                    List<Goal> goalsList = currentUser.getGoalsList();
                    for (int i = 0; i < goalsList.stream().count(); i++) {
                        Goal singleGoal = goalsList.get(i);
                        if (singleGoal.getGoalId().equals(goalId)) {
                            if (singleGoal.getIdCreatedBy().equals(currentUser.getId()) || singleGoal.getShowProgressToEveryone())
                                isEligibleToViewProgress = true;
                        }
                    }

                    if (isEligibleToViewProgress) {
                        Intent intent = new Intent(getApplicationContext(), OkrProgressActivity.class);
                        intent.putExtra("goalId", goalId);
                        intent.putExtra("okrId", okrId);

                        startActivity(intent);
                    }
//                    Bundle args = new Bundle();
//                    args.putSerializable("allUsers", (Serializable) allUsers);
//                    Toast.makeText(ViewGoalActivity.this, goalId, Toast.LENGTH_SHORT).show();
                }
            });
            
            TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, trHeight, 1f);
            tableRow.setLayoutParams(rowParams);

            CheckBox checkBox = new CheckBox(this);
            TableRow.LayoutParams params1 = new TableRow.LayoutParams(0, trHeight, 0.13f);
            checkBox.setTag(okrGoals.get(i));
            checkBox.setId(i);
            checkBox.setGravity(Gravity.CENTER_VERTICAL);
            checkBox.setLayoutParams(params1);

            TextView goalText = new TextView(this);
            goalText.setText(okrGoals.get(i).getGoal());
            goalText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
            TableRow.LayoutParams params2 = new TableRow.LayoutParams(0, trHeight, 0.4f);
            goalText.setGravity(Gravity.CENTER_VERTICAL);
            goalText.setLayoutParams(params2);

            TextView valueText = new TextView(this);
            valueText.setText(Integer.toString(okrGoals.get(i).getValue()));
            valueText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
            TableRow.LayoutParams params3 = new TableRow.LayoutParams(0, trHeight, 0.2f);
            valueText.setGravity(Gravity.CENTER_VERTICAL);
            valueText.setLayoutParams(params3);

            TextView dueDateText = new TextView(this);
            dueDateText.setText(okrGoals.get(i).getDueDate());
            dueDateText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
            TableRow.LayoutParams params4 = new TableRow.LayoutParams(0, trHeight, 0.27f);
            dueDateText.setGravity(Gravity.CENTER_VERTICAL);
            dueDateText.setLayoutParams(params4);

            if (okrGoals.get(i).getDone()) {
                checkBox.setEnabled(false);
                checkBox.setChecked(true);
                goalText.setPaintFlags(goalText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                valueText.setPaintFlags(valueText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                dueDateText.setPaintFlags(dueDateText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (checkBox.isChecked()) {
                        CheckBox box = (CheckBox) compoundButton;
                        int rowId = box.getId();
                        Goal checkedOkrGoal = (Goal) box.getTag();

                        usersRef.child(currentUser.getId()).child("Goals").child(clickedGoal.getGoalId()).child("okrGoals").child(Integer.toString(rowId)).child("done").setValue(true);
                        String currentDate = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(new Date());
                        usersRef.child(currentUser.getId()).child("Goals").child(clickedGoal.getGoalId()).child("okrGoals").child(Integer.toString(rowId)).child("dateDone").setValue(currentDate);

                        TableRow doneRow = (TableRow) table.getChildAt(rowId);
                        for (int i = 0; i < doneRow.getChildCount(); i++) {
                            View v = doneRow.getChildAt(i);
                            if (v instanceof CheckBox) {
                                v.setEnabled(false);
                            }
                            else if (v instanceof TextView) {
                                TextView text = (TextView) v;
                                text.setPaintFlags(text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                            }
                        }

                    }
                }
            });

            tableRow.addView(checkBox);
            tableRow.addView(goalText);
            tableRow.addView(valueText);
            tableRow.addView(dueDateText);

            table.addView(tableRow);
        }
    }
}
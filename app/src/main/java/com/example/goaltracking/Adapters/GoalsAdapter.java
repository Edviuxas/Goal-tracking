package com.example.goaltracking.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.example.goaltracking.Model.Goal;
import com.example.goaltracking.Model.TeamInvitation;
import com.example.goaltracking.Model.User;
import com.example.goaltracking.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class GoalsAdapter extends BaseAdapter implements ListAdapter {

    private List<Goal> goalsList;
    private Context context;
    private User currentUser;
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://goal-tracking-ccad5-default-rtdb.europe-west1.firebasedatabase.app/");
    DatabaseReference usersRef = database.getReference("Users");
    Boolean allUsersFinishedOkrGoals;

    public GoalsAdapter(List<Goal> goalsList, User currentUser, Context context) {
        this.goalsList = goalsList;
        this.currentUser = currentUser;
        this.context = context;
    }

    @Override
    public int getCount() {
        return goalsList.size();
    }

    @Override
    public Object getItem(int i) {
        return goalsList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.goals_adapter, null);
        }

        Goal goal = goalsList.get(position);
        TextView textGoal = v.findViewById(R.id.textGoalText);
        TextView dueDate = v.findViewById(R.id.textDueDate);
        ImageView exclamationImage = v.findViewById(R.id.imageExclamation);
        ImageView sharedImage = v.findViewById(R.id.imageShared);
        textGoal.setText(goal.getGoal());
        CheckBox checkBoxDone = v.findViewById(R.id.checkBoxDone);
        ConstraintLayout constraintLayout = v.findViewById(R.id.constraintLayout);

        checkBoxDone.setChecked(false);

        if (!dueDate.equals("")) {
            dueDate.setText(goal.getDueDate());
        }

        if (goal.getIsImportant())
            exclamationImage.setVisibility(View.VISIBLE);
        else
            exclamationImage.setVisibility(View.INVISIBLE);

        if (!goal.getIdCreatedBy().equals(currentUser.getId())) {
            sharedImage.setVisibility(View.VISIBLE);
            checkBoxDone.setVisibility(View.INVISIBLE);
        }
        else {
            sharedImage.setVisibility(View.INVISIBLE);
            checkBoxDone.setVisibility(View.VISIBLE);
        }

        checkBoxDone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (checkBoxDone.isChecked()) {
                    Goal checkedGoal = (Goal) getItem(position);

                    allUsersFinishedOkrGoals = true;
                    List<String> sharedWith = checkedGoal.getSharedWith();
                    if (sharedWith != null) {
                        for (int i = 0; i < sharedWith.stream().count(); i++) {
                            Query goalsQuery = usersRef.child(sharedWith.get(i)).child("Goals").orderByKey().equalTo(goal.getGoalId());
                            goalsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        for (DataSnapshot singleSnapshot: snapshot.getChildren()) {
                                            Goal goal = singleSnapshot.getValue(Goal.class);
                                            List<Goal> okrGoals = goal.getOkrGoals();
                                            for (int j = 0; j < okrGoals.stream().count(); j++) {
                                                if (!okrGoals.get(j).getDone())
                                                    allUsersFinishedOkrGoals = false;
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }
                    if (allUsersFinishedOkrGoals) {
                        for (int i = 0; i < sharedWith.stream().count(); i++) {
                            usersRef.child(sharedWith.get(i)).child("Goals").child(checkedGoal.getGoalId()).child("done").setValue(true);
                        }
                        usersRef.child(currentUser.getId()).child("Goals").child(checkedGoal.getGoalId()).child("done").setValue(true);
                        goalsList.remove(checkedGoal);
                    } else {
                        Toast.makeText(context, "Not all users have finished this goal", Toast.LENGTH_SHORT).show();
                    }
                }
                notifyDataSetChanged();
            }
        });

        if (((Goal)getItem(position)).isOkrGoalsDone()) {
            v.setBackgroundColor(Color.rgb(159, 245, 179));
        }

        return v;
    }
}

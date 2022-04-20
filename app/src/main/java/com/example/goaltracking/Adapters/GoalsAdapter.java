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
    DatabaseReference goalsRef = database.getReference("Goals");
    DatabaseReference usersRef = database.getReference("Users");

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

//        ConstraintSet constraintSet = new ConstraintSet();
//        constraintSet.clone(constraintLayout);
        if (!goal.getIdCreatedBy().equals(currentUser.getId())) {
            sharedImage.setVisibility(View.VISIBLE);
            checkBoxDone.setVisibility(View.INVISIBLE);
        }
        else {
            sharedImage.setVisibility(View.INVISIBLE);
            checkBoxDone.setVisibility(View.VISIBLE);
        }
//        constraintSet.connect(R.id.textGoalText, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 8);
//        constraintSet.applyTo(constraintLayout);

        checkBoxDone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (checkBoxDone.isChecked()) {
//                    removeGoalFromDatabase(position); //TODO: prieš ištrinant reiktų parodyti confirmation dialog https://stackoverflow.com/questions/5127407/how-to-implement-a-confirmation-yes-no-dialogpreference
//                    Goal checkedGoal = (Goal) getItem(position);
//                    Toast.makeText(context, checkedGoal.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (((Goal)getItem(position)).isOkrGoalsDone()) {
            v.setBackgroundColor(Color.rgb(159, 245, 179));
        }

        return v;
    }

    private void removeGoalFromDatabase(int index) {
        Goal goal = goalsList.get(index);
        Query goalsQuery = usersRef.child(currentUser.getId()).child("Goals").orderByKey().equalTo(goal.getGoalId());
        goalsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot singleSnapshot: snapshot.getChildren()) {
                        singleSnapshot.getRef().removeValue();
                    }
                }
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}

package com.example.goaltracking;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.goaltracking.Adapters.GoalsAdapter;
import com.example.goaltracking.Model.Goal;
import com.example.goaltracking.Model.TeamInvitation;
import com.example.goaltracking.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import io.sentry.ISpan;
import io.sentry.ITransaction;
import io.sentry.Sentry;

public class GoalsFragment extends Fragment {

    FirebaseDatabase database = FirebaseDatabase.getInstance("https://goal-tracking-ccad5-default-rtdb.europe-west1.firebasedatabase.app/");
    DatabaseReference usersRef = database.getReference("Users");
    ArrayList<Goal> currentUserGoals = new ArrayList<>();
    ListView listViewGoals;
    GoalsAdapter adapter;
    Goal clickedGoal;
    User currentUser;
    ITransaction transaction;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        transaction = Sentry.startTransaction("getGoals", "task");

        Bundle args = getArguments();
        currentUser = (User) args.getSerializable("currentUser");

        ValueEventListener goalsListener = new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentUserGoals.clear();
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                    Goal goal = singleSnapshot.getValue(Goal.class);
                    goal.setGoalId(singleSnapshot.getKey());
                    if (!goal.getDone())
                        currentUserGoals.add(goal);
                }
                currentUser.setGoalsList(currentUserGoals);
                adapter.notifyDataSetChanged();
                transaction.finish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        usersRef.child(currentUser.getId()).child("Goals").addValueEventListener(goalsListener);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onResume() {
        super.onResume();
        colorListViewItemIfGoalDone();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void colorListViewItemIfGoalDone() {
        for (int i = 0; i < currentUserGoals.stream().count(); i++) {
            Goal g = currentUserGoals.get(i);
            if (g.isOkrGoalsDone()) {
                listViewGoals.getChildAt(i).setBackgroundColor(Color.rgb(159, 245, 179));
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        clickedGoal = (Goal) adapter.getItem(info.position);
        menu.add("Edit goal");
        menu.add("Delete goal");
        List<String> sharedWith = clickedGoal.getSharedWith();
        if (clickedGoal.isInProgress() || sharedWith != null || !clickedGoal.getIdCreatedBy().equals(currentUser.getId())) {
            menu.getItem(0).setEnabled(false);
        }
        if (!clickedGoal.getIdCreatedBy().equals(currentUser.getId())) {
            menu.getItem(1).setEnabled(false);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getTitle().equals("Edit goal")) {
            Intent intent = new Intent(getContext(), AddNewGoalActivity.class);
            intent.putExtra("currentUser", currentUser);
            intent.putExtra("editingMode", true);
            intent.putExtra("editingGoal", clickedGoal);
            startActivity(intent);
        } else if (item.getTitle().equals("Delete goal")) {
            removeGoalFromDatabase(clickedGoal);
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void removeGoalFromDatabase(Goal goalToRemove) {
        List<String> sharedWith = new ArrayList<>();
        sharedWith.add(currentUser.getId());
        if (goalToRemove.getSharedWith() != null) {
            for (int i = 0; i < goalToRemove.getSharedWith().stream().count(); i++) {
                sharedWith.add(goalToRemove.getSharedWith().get(i));
            }
        }
        for (int i = 0; i < sharedWith.stream().count(); i++) {
            Query goalsQuery = usersRef.child(sharedWith.get(i)).child("Goals").orderByKey().equalTo(goalToRemove.getGoalId());
            goalsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot singleSnapshot: snapshot.getChildren()) {
                            singleSnapshot.getRef().removeValue();
//                        }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_goals, container, false);

        listViewGoals = (ListView) view.findViewById(R.id.listViewGoals);
        adapter = new GoalsAdapter(currentUserGoals, currentUser, getContext());
        listViewGoals.setAdapter(adapter);
        registerForContextMenu(listViewGoals);

        listViewGoals.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Goal clickedGoal = (Goal) adapterView.getAdapter().getItem(i);
                Intent intent = new Intent(getContext(), ViewGoalActivity.class);
                intent.putExtra("clickedGoal", clickedGoal);
                intent.putExtra("currentUser", currentUser);
                startActivity(intent);
            }
        });

        return view;
    }
}
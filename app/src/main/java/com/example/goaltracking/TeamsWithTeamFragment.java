package com.example.goaltracking;

import android.app.DatePickerDialog;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.goaltracking.Adapters.InvitationsAdapter;
import com.example.goaltracking.Adapters.LeaderboardAdapter;
import com.example.goaltracking.Model.Goal;
import com.example.goaltracking.Model.Team;
import com.example.goaltracking.Model.TeamInvitation;
import com.example.goaltracking.Model.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TeamsWithTeamFragment extends Fragment {

    FirebaseDatabase database = FirebaseDatabase.getInstance("https://goal-tracking-ccad5-default-rtdb.europe-west1.firebasedatabase.app/");
    DatabaseReference usersRef = database.getReference("Users");
    DatabaseReference pendingTeamRequestsRef = database.getReference("PendingTeamRequests");
    User currentUser;
//    List<Goal> currentUserGoals = new ArrayList<>();
    List<Goal> teamGoals = new ArrayList<>();
    List<User> usersEligibleForInvitation = new ArrayList<>();
    List<User> allUsers = new ArrayList<>();
    List<User> usersFromOneTeam = new ArrayList<>();
    View.OnClickListener dateListener;
    ArrayList<TeamInvitation> allInvitations = new ArrayList<>();
    AutoCompleteTextView autoCompleteTextView;
    InvitationsAdapter invitationsAdapter;
    Button btnSendInvite;
    EditText dateFrom, dateTo;
    final Calendar myCalendar = Calendar.getInstance();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.FRANCE);
    View clickedEditTextDate;
    LeaderboardAdapter leaderboardAdapter;
    ListView listViewLeaderboard;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        currentUser = (User) args.getSerializable("currentUser");

        ValueEventListener usersListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    usersEligibleForInvitation.clear();
                    allUsers.clear();
                    usersFromOneTeam.clear();
                    // Get Post object and use the values to update the UI
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
//                        user.calculateLeaderboardPoints();

                        allUsers.add(user);
                        if (!belongsToTeam(user) && !isCurrentUser(user))
                            addUserIfNotAlreadyInvited(user);

                        if (user.getBelongsToTeam().equals(currentUser.getBelongsToTeam()))
                            usersFromOneTeam.add(user);

//                        if (user.getId().equals(currentUser.getId())) {
//                            DataSnapshot goalsSnapshot = singleSnapshot.child("Goals");
//                            for (DataSnapshot ds : goalsSnapshot.getChildren()) {
//                                Goal g = ds.getValue(Goal.class);
//                                currentUserGoals.add(g);
//                                Toast.makeText(getContext(), g.getGoal(), Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                        for (int i = 0; i < goalsSnapshot.getChildrenCount(); i++) {
//
//                        }
                    }
                    Collections.sort(allUsers, (User user1, User user2) -> user2.calculateLeaderboardPoints("", "") - user1.calculateLeaderboardPoints("", ""));
                    leaderboardAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
//                Toast.makeText(AllGoalsActivity.this, databaseError.toException().toString(), Toast.LENGTH_SHORT).show();
            }
        };

        ValueEventListener pendingTeamRequestsListener = new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    usersEligibleForInvitation.clear();
                    usersEligibleForInvitation.addAll(allUsers);
                    usersEligibleForInvitation.removeIf(u -> u.getId().equals(currentUser.getId()));
//                    usersEligibleForInvitation.clear();
                    // Get Post object and use the values to update the UI
//                    usersEligibleForInvitation.addAll(allUsers);
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        TeamInvitation invitation = singleSnapshot.getValue(TeamInvitation.class);

                        for (User user : allUsers) {
                            if (invitation.getTo().equals(user.getEmailAddress()) || belongsToTeam(user) || isCurrentUser(user)) {
                                usersEligibleForInvitation.remove(user);
                            }
                        }
                    }
                }
                invitationsAdapter.notifyDataSetChanged();
                invitationsAdapter.getFilter().filter(autoCompleteTextView.getText(), null);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
//                Toast.makeText(AllGoalsActivity.this, databaseError.toException().toString(), Toast.LENGTH_SHORT).show();
            }
        };

        usersRef.addValueEventListener(usersListener);
        pendingTeamRequestsRef.addValueEventListener(pendingTeamRequestsListener);
    }

    private void addUserIfNotAlreadyInvited(User user) {
        Query pendingTeamRequestsQuery = pendingTeamRequestsRef.orderByChild("to").equalTo(user.getEmailAddress());
        pendingTeamRequestsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean foundInvitation = false;
                if (snapshot.exists()) {
                    for (DataSnapshot singleSnapshot: snapshot.getChildren()) {
                        TeamInvitation inv = singleSnapshot.getValue(TeamInvitation.class);
                        if (inv.getTeamId().equals(currentUser.getBelongsToTeam())) {
                            foundInvitation = true;
                        }
                    }
                }
                if (!foundInvitation && !usersEligibleForInvitation.contains(user))
                    usersEligibleForInvitation.add(user);
                invitationsAdapter.notifyDataSetChanged();
                invitationsAdapter.getFilter().filter(autoCompleteTextView.getText(), null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private boolean isCurrentUser(User user) {
        return user.getEmailAddress().equals(currentUser.getEmailAddress());
    }

    private boolean belongsToTeam(User user) {
        return !user.getBelongsToTeam().equals("null");
    }

    private void updateDateEditText(EditText date) {
        date.setText(dateFormat.format(myCalendar.getTime()));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_teams_with_team, container, false);

        dateFrom = view.findViewById(R.id.editTextFromDate);
        dateTo = view.findViewById(R.id.editTextToDate);

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH,month);
                myCalendar.set(Calendar.DAY_OF_MONTH,day);
                updateDateEditText((EditText) clickedEditTextDate);
                if (!dateFrom.getText().toString().equals("") && !dateTo.getText().toString().equals("")) {
                    String dateFromStr = dateFrom.getText().toString();
                    String dateToStr = dateTo.getText().toString();
                    Collections.sort(allUsers, (User user1, User user2) -> user2.calculateLeaderboardPoints(dateFromStr, dateToStr) - user1.calculateLeaderboardPoints(dateFromStr, dateToStr));
                    leaderboardAdapter = new LeaderboardAdapter(usersFromOneTeam, view.getContext(), currentUser, dateFromStr, dateToStr);
                    listViewLeaderboard.setAdapter(leaderboardAdapter);
                }
//                textDate.setText(myCalendar.getTime().toString());
            }
        };

        dateListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickedEditTextDate = view;
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),myCalendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        };

//        TextView textLeaderboard = view.findViewById(R.id.textViewLeaderboard);
//        textLeaderboard.setTooltipText("This is how points are calculated:\n" +
//                                        "If you finish a task before due date,\n" +
//                                        "you receive (difficulty * days before due date) points\n" +
//                                        "If you finish on due date, you receive 0 points\n" +
//                                        "If you are late, you get negative (2 * days past due date) points");
//
//        textLeaderboard.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                textLeaderboard.performLongClick();
//            }
//        });

        listViewLeaderboard = view.findViewById(R.id.listViewLeaderboard);
        autoCompleteTextView = view.findViewById(R.id.autoComplete);
        dateFrom.setOnClickListener(dateListener);
        dateTo.setOnClickListener(dateListener);
        autoCompleteTextView.setThreshold(1);
        invitationsAdapter = new InvitationsAdapter(view.getContext(), R.layout.fragment_teams_with_team, R.id.textViewEmailAddress, usersEligibleForInvitation);
        autoCompleteTextView.setAdapter(invitationsAdapter);
        leaderboardAdapter = new LeaderboardAdapter(usersFromOneTeam, view.getContext(), currentUser, "", "");
        listViewLeaderboard.setAdapter(leaderboardAdapter);

        btnSendInvite = view.findViewById(R.id.btnSendInvitation);
        btnSendInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AutoCompleteTextView textEmailAddress = getView().findViewById(R.id.autoComplete);
                String toEmailAddress = String.valueOf(textEmailAddress.getText()); //TODO: .split("\\s+")[2] naudojamas laikinai, gali crashint. reikia padaryt kaip nors protingiau
                TeamInvitation teamInvitation = new TeamInvitation(currentUser.getEmailAddress(), toEmailAddress, currentUser.getBelongsToTeam(), "Random team name"); // TODO: padaryti, kad rodytų normalų komandos pavadinimą

                String key = pendingTeamRequestsRef.push().getKey();
                pendingTeamRequestsRef.child(key).setValue(teamInvitation);

                textEmailAddress.setText("");
            }
        });

        return view;
    }
}
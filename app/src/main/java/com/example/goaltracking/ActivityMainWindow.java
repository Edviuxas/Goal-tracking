package com.example.goaltracking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.goaltracking.Model.Team;
import com.example.goaltracking.Model.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import io.sentry.ISpan;
import io.sentry.Sentry;

public class ActivityMainWindow extends AppCompatActivity {

    FloatingActionButton fab;
    private final static int RC_ADD_GOAL = 124;
    User currentUser;
    String currentBottomAppBarTab;
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://goal-tracking-ccad5-default-rtdb.europe-west1.firebasedatabase.app/");
    DatabaseReference teamsRef = database.getReference("Teams");
    DatabaseReference usersRef = database.getReference("Users");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_window);

        currentUser = (User) getIntent().getSerializableExtra("currentUser");
        currentBottomAppBarTab = "Goals";

        fab = findViewById(R.id.fab);
        findViewById(R.id.bottomNavigationView).setBackgroundColor(Color.parseColor("#00000000"));

        Bundle args = new Bundle();
        args.putSerializable("currentUser", currentUser);
        Fragment fragment = new GoalsFragment();
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction().replace(R.id.placeholder, fragment).commit();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment = null;

            switch (item.getItemId()) {
                case R.id.team:
                    currentBottomAppBarTab = "Team";
                    if (!currentUser.getBelongsToTeam().equals("null")) {
                        fab.setEnabled(false);
                        selectedFragment = new TeamsWithTeamFragment();
                    } else {
                        selectedFragment = new TeamsNoTeamFragment();
                    }
                    break;
                case R.id.goals:
                    currentBottomAppBarTab = "Goals";
                    selectedFragment = new GoalsFragment();
                    fab.setEnabled(true);
                    break;
            }

            Bundle args = new Bundle();
            args.putSerializable("currentUser", currentUser);
            selectedFragment.setArguments(args);
            getSupportFragmentManager().beginTransaction().replace(R.id.placeholder, selectedFragment).commit();
            return true;
        }
    };

    public void onBtnAddClick(View view) {
        if (currentBottomAppBarTab == "Goals") {
            Intent intent = null;
            intent = new Intent(getApplicationContext(), AddNewGoalActivity.class);
            intent.putExtra("currentUser", currentUser);
            intent.putExtra("editingMode", false);

            startActivityForResult(intent, RC_ADD_GOAL);
        } else {
            showAlertDialog();
        }
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter team name");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String key = teamsRef.push().getKey();
                teamsRef.child(key).child("teamName").setValue(input.getText().toString());
                Team team = new Team(input.getText().toString(), key);
                currentUser.setBelongsToTeam(team.getId());
                currentUser.setIsTeamCreator(true);

                usersRef.child(currentUser.getId()).child("belongsToTeam").setValue(team.getId());
                usersRef.child(currentUser.getId()).child("isTeamCreator").setValue(true);

                Bundle args = new Bundle();
                args.putSerializable("currentUser", currentUser);
                Fragment fragment = new TeamsWithTeamFragment();
                fragment.setArguments(args);
                getSupportFragmentManager().beginTransaction().replace(R.id.placeholder, fragment).commit();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
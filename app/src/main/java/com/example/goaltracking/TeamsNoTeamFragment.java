package com.example.goaltracking;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.goaltracking.Adapters.CustomTeamsInvitationsAdapter;
import com.example.goaltracking.Model.TeamInvitation;
import com.example.goaltracking.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class TeamsNoTeamFragment extends Fragment {

    User currentUser = null;
    ArrayList<TeamInvitation> allInvitations = new ArrayList<>();
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://goal-tracking-ccad5-default-rtdb.europe-west1.firebasedatabase.app/");
    DatabaseReference invitationsRef = database.getReference("PendingTeamRequests");
    CustomTeamsInvitationsAdapter adapter;
    ListView listView;

    public TeamsNoTeamFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        currentUser = (User) args.getSerializable("currentUser");

        ValueEventListener invitationsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                allInvitations.clear();
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    if (dataSnapshot.exists()) {
                        TeamInvitation invitation = singleSnapshot.getValue(TeamInvitation.class);
                        if (invitation.getTo().equals(currentUser.getEmailAddress())) {
                            allInvitations.add(invitation);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
//                populateInvitationsListView();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
//                Toast.makeText(AllGoalsActivity.this, databaseError.toException().toString(), Toast.LENGTH_SHORT).show();
            }
        };
        invitationsRef.addValueEventListener(invitationsListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_teams_no_team, container, false);

        adapter = new CustomTeamsInvitationsAdapter(allInvitations, currentUser, getContext());
        listView = (ListView) view.findViewById(R.id.listViewTeamsInvitations);
        listView.setAdapter(adapter);

        return view;
    }
}
package com.example.goaltracking.Adapters;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.example.goaltracking.ActivityMainWindow;
import com.example.goaltracking.Model.TeamInvitation;
import com.example.goaltracking.Model.User;
import com.example.goaltracking.R;
import com.example.goaltracking.TeamsWithTeamFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CustomTeamsInvitationsAdapter extends BaseAdapter implements ListAdapter {

    FirebaseDatabase database = FirebaseDatabase.getInstance("https://goal-tracking-ccad5-default-rtdb.europe-west1.firebasedatabase.app/");
    DatabaseReference invitationsRef = database.getReference("PendingTeamRequests");
    DatabaseReference usersRef = database.getReference("Users");
    private User currentUser;
    private ArrayList<TeamInvitation> invitationsList = new ArrayList<>();
    private Context context;

    public CustomTeamsInvitationsAdapter(ArrayList<TeamInvitation> invitations, User currentUser, Context context) {
        this.invitationsList = invitations;
        this.currentUser = currentUser;
        this.context = context;
    }

    @Override
    public int getCount() {
        return invitationsList.size();
    }

    @Override
    public Object getItem(int i) {
        return invitationsList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = view;
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.custom_team_invitation_listview_layout, null);
        }

        //Handle TextView and display string from your list
        TextView textTeamName = (TextView) v.findViewById(R.id.textViewTeamName);
        TextView textInviterEmailAddress = (TextView) v.findViewById(R.id.textViewInviterEmailAddress);
        textTeamName.setText(invitationsList.get(i).getTeamName());
        textInviterEmailAddress.setText(invitationsList.get(i).getFrom());

        //Handle buttons and add onClickListeners
        FloatingActionButton btnAccept = v.findViewById(R.id.btnAccept);
        FloatingActionButton btnDecline = v.findViewById(R.id.btnDecline);

        btnAccept.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                TeamInvitation acceptedInvitation = invitationsList.get(i);
                currentUser.setBelongsToTeam(acceptedInvitation.getTeamId());

                usersRef.child(currentUser.getId()).child("belongsToTeam").setValue(acceptedInvitation.getTeamId());

                removeInvitationFromDatabase(i);

                TeamsWithTeamFragment fragment = new TeamsWithTeamFragment();
                Bundle args = new Bundle();
                args.putSerializable("currentUser", currentUser);
                fragment.setArguments(args);
                ((ActivityMainWindow) context).getSupportFragmentManager().beginTransaction().replace(R.id.placeholder, fragment).commit();
            }
        });
        btnDecline.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                removeInvitationFromDatabase(i);
            }
        });

        return v;
    }

    private void removeInvitationFromDatabase(int index) {
        TeamInvitation teamInvitation = invitationsList.get(index);
        Query invitationsQuery = invitationsRef.orderByChild("teamId").equalTo(teamInvitation.getTeamId());
        invitationsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot singleSnapshot: snapshot.getChildren()) {
                        TeamInvitation inv = singleSnapshot.getValue(TeamInvitation.class);
                        if (inv.getTo().equals(currentUser.getEmailAddress())) {
                            singleSnapshot.getRef().removeValue();
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

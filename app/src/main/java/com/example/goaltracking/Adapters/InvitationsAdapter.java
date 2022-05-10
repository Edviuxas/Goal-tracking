package com.example.goaltracking.Adapters;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.goaltracking.Model.TeamInvitation;
import com.example.goaltracking.Model.User;
import com.example.goaltracking.R;
import com.example.goaltracking.TeamsWithTeamFragment;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

public class InvitationsAdapter extends ArrayAdapter<User> implements Filterable {

    private List<User> usersList;
    private List<User> filteredUsersList;
    private Context context;

    public InvitationsAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List<User> objects) {
        super(context, resource, textViewResourceId, objects);
        this.context = context;
        usersList = objects;
    }

    @Override
    public int getCount() {
        return filteredUsersList.size();
    }

    @Override
    public User getItem(int position) {
        return filteredUsersList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = view;
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.invitations_adapter_row, viewGroup, false);

            TextView textEmailAddress = (TextView) v.findViewById(R.id.textViewEmailAddress);
            User user = getItem(i);
            if (user != null) {
                textEmailAddress.setText(usersList.get(i).getEmailAddress());
            }
        }

        return v;
    }

    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            filteredUsersList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredUsersList.addAll(usersList);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (User user: usersList) {
                    if (user.getEmailAddress().toLowerCase().contains(filterPattern))
                        filteredUsersList.add(user);
                }
            }

            results.values = filteredUsersList;
            results.count = filteredUsersList.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredUsersList = (List<User>) results.values;
            notifyDataSetChanged();
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return ((User) resultValue).getEmailAddress();
        }
    };

    @Override
    public Filter getFilter() {
        return filter;
    }
}

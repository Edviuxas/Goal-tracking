package com.example.goaltracking.Adapters;

import android.content.Context;
import android.os.Build;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.example.goaltracking.Model.User;
import com.example.goaltracking.R;

import java.util.Collections;
import java.util.List;

public class LeaderboardAdapter extends BaseAdapter implements ListAdapter {
    private List<User> data;
    User currentUser;
    Context context;
    String dateFrom, dateTo;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public LeaderboardAdapter(List<User> data, Context context, User currentUser, String dateFrom, String dateTo) {
        this.data = data;
        this.context = context;
        this.currentUser = currentUser;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public int getCount() {
        return (int) data.stream().count();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = view;
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.leaderboard_adapter, null);
        }

        TextView textUserLeaderboard = v.findViewById(R.id.textViewUserLeaderboard);
        TextView textPointsLeaderboard = v.findViewById(R.id.textViewPointsLeaderboard);

        textUserLeaderboard.setText(data.get(i).getEmailAddress());
        textPointsLeaderboard.setText(String.valueOf(data.get(i).calculateLeaderboardPoints(dateFrom, dateTo)) + " pts");
        return v;
    }
}

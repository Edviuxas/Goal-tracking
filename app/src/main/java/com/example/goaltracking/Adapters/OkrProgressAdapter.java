package com.example.goaltracking.Adapters;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.example.goaltracking.Model.Goal;
import com.example.goaltracking.Model.User;
import com.example.goaltracking.R;

import java.util.Collections;
import java.util.List;

public class OkrProgressAdapter extends BaseAdapter implements ListAdapter {
    Context context;
    private List<User> users;
    String goalId;
    int okrId;

    public OkrProgressAdapter(List<User> users, Context context, String goalId, int okrId) {
        this.users = users;
        this.context = context;
        this.goalId = goalId;
        this.okrId = okrId;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public int getCount() {
        return (int) users.stream().count();
//        int count = 0;
//        for (int i = 0; i < allUsers.stream().count(); i++) {
//            User user = allUsers.get(i);
//            List<Goal> goals = user.getGoalsList();
//            for (int j = 0; j < goals.stream().count(); j++) {
//                Goal singleGoal = goals.get(j);
//                if (singleGoal.getGoalId().equals(goalId))
//                    count++;
//            }
//        }
//        return count;
    }

    @Override
    public Object getItem(int i) {
        return users.get(i);
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
            v = inflater.inflate(R.layout.okr_progress_adapter, null);
        }

        TextView textUser = v.findViewById(R.id.textViewOkrProgressUser);
        ImageView imageProgress = v.findViewById(R.id.imageOkrProgress);

        textUser.setText(users.get(i).getEmailAddress());
        List<Goal> allGoals = users.get(i).getGoalsList();
        for (int j = 0; j < allGoals.stream().count(); j++) {
            Goal singleGoal = allGoals.get(j);
            if (singleGoal.getGoalId().equals(goalId)) {
                if (!singleGoal.getOkrGoals().get(okrId).getDone())
                    imageProgress.setImageResource(R.drawable.ic_decline);
            }
        }
//        User user = allUsers.get(i);
//        List<Goal> userGoals = user.getGoalsList();
//        if (userGoals.stream().anyMatch(o -> goalId.equals(o.getGoalId()))) {
//            textUser.setText(user.getEmailAddress());
//            Boolean isOkrDone = userGoals.get(okrId).getDone();
//        }

        return v;
    }
}

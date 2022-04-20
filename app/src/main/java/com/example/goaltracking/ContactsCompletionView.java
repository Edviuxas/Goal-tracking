package com.example.goaltracking;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.goaltracking.Model.User;
import com.tokenautocomplete.TokenCompleteTextView;

public class ContactsCompletionView extends TokenCompleteTextView<User> {
    public ContactsCompletionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View getViewForObject(User user) {
        LayoutInflater l = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        TextView view = (TextView) l.inflate(R.layout.contact_token, (ViewGroup) getParent(), false);
        view.setText(user.getEmailAddress());

        return view;
    }

    @Override
    public boolean shouldIgnoreToken(User token) {
        return getObjects().contains(token);
    }

    @Override
    protected User defaultObject(String completionText) {
        int index = completionText.indexOf('@');
        if (index == -1) {
            return new User(completionText, completionText.replace(" ", "") + "@gmail.com");
        } else {
            return new User(completionText.substring(0, index), completionText);
        }
    }
}

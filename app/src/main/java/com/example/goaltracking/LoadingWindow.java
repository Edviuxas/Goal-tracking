package com.example.goaltracking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.goaltracking.Model.Goal;
import com.example.goaltracking.Model.User;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

import io.sentry.ITransaction;
import io.sentry.Sentry;

public class LoadingWindow extends AppCompatActivity {

    FirebaseDatabase database = FirebaseDatabase.getInstance("https://goal-tracking-ccad5-default-rtdb.europe-west1.firebasedatabase.app/");
    DatabaseReference usersRef = database.getReference("Users");

    private final static int RC_SIGN_IN = 123;
    private FirebaseAuth mAuth;
    private User currentUser;
    String fcmToken;
    ITransaction transaction;

    @Override
    protected void onStart() {
        super.onStart();
        Sentry.captureMessage("testing SDK setup");
        transaction = Sentry.startTransaction("authentication", "task");

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("TAG", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        fcmToken = task.getResult();

                        Log.d("token", fcmToken);
                    }
                });

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            Query query = usersRef.orderByKey().equalTo(uid);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        currentUser = snapshot.child(uid).getValue(User.class);
                        currentUser.setId(uid);

                        List<Goal> userGoals = new ArrayList<>();
                        DataSnapshot goalsSnapshot = snapshot.child(uid).child("Goals");
                        for (DataSnapshot ds : goalsSnapshot.getChildren()) {
                            Goal g = ds.getValue(Goal.class);
                            g.setGoalId(ds.getKey());
                            userGoals.add(g);
                        }
                        currentUser.setGoalsList(userGoals);
                        usersRef.child(uid).child("fcmToken").setValue(fcmToken);

                        loadMainWindow();
                    } else {
                        Toast.makeText(LoadingWindow.this, "There was an error logging in", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            loadLoginWindow();
        }
    }

    private void loadLoginWindow() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("currentUser", currentUser);
        startActivity(intent);
    }

    private void loadMainWindow() {
        Intent intent = new Intent(getApplicationContext(), ActivityMainWindow.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        intent.putExtra("currentUser", currentUser);
        startActivity(intent);
        transaction.finish();
        this.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_window);

        mAuth = FirebaseAuth.getInstance();
    }
}
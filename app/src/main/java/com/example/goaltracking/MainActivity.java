package com.example.goaltracking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.goaltracking.Model.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    FirebaseDatabase database = FirebaseDatabase.getInstance("https://goal-tracking-ccad5-default-rtdb.europe-west1.firebasedatabase.app/");
    DatabaseReference usersRef = database.getReference("Users");

    private GoogleSignInClient mGoogleSignInClient;
    private final static int RC_SIGN_IN = 123;
    private FirebaseAuth mAuth;
    private User currentUser;
    String fcmToken;

//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        FirebaseMessaging.getInstance().getToken()
//                .addOnCompleteListener(new OnCompleteListener<String>() {
//                    @Override
//                    public void onComplete(@NonNull Task<String> task) {
//                        if (!task.isSuccessful()) {
//                            Log.w("TAG", "Fetching FCM registration token failed", task.getException());
//                            return;
//                        }
//
//                        // Get new FCM registration token
//                        fcmToken = task.getResult();
//
//                        // Log and toast
////                        String msg = getString(R.string.msg_token_fmt, token);
//                        Log.d("token", fcmToken);
////                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//        FirebaseUser user = mAuth.getCurrentUser();
//        if (user != null) {
//            String uid = user.getUid();
//            Query query = usersRef.orderByKey().equalTo(uid);
//            query.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    if (snapshot.exists()) {
//                        currentUser = snapshot.child(uid).getValue(User.class);
//                        currentUser.setId(uid);
//
//                        List<Goal> userGoals = new ArrayList<>();
//                        DataSnapshot goalsSnapshot = snapshot.child(uid).child("Goals");
//                        for (DataSnapshot ds : goalsSnapshot.getChildren()) {
//                            Goal g = ds.getValue(Goal.class);
//                            g.setGoalId(ds.getKey());
//                            userGoals.add(g);
//                        }
//                        currentUser.setGoalsList(userGoals);
//                        usersRef.child(uid).child("fcmToken").setValue(fcmToken);
//
////                        Toast.makeText(this, user.getDisplayName(), Toast.LENGTH_SHORT).show();
//                        loadMainWindow();
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//
//                }
//            });
//        }
//    }
//
    private void loadMainWindow() {
        Intent intent = new Intent(getApplicationContext(), ActivityMainWindow.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        intent.putExtra("currentUser", currentUser);
        startActivity(intent);
        this.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        currentUser = (User) intent.getSerializableExtra("currentUser");

        mAuth = FirebaseAuth.getInstance();

        createGoogleLoginRequest();
    }

    private void createGoogleLoginRequest() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    public void btnGoogleLoginClick(View view) {
        signIn();
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN); // TODO: reiktų pakeisti kažkuo, kad nebūtų deprecated (registerForActivityResult)
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                }
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();

                            Toast.makeText(MainActivity.this, user.getDisplayName(), Toast.LENGTH_SHORT).show();

                            currentUser = new User(user.getDisplayName(), user.getEmail(), user.getUid(), null, false);
                            Query usersQuery = usersRef.orderByChild("emailAddress").equalTo(currentUser.getEmailAddress());
                            boolean result = false;
                            usersQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (!snapshot.exists()) {
                                        String uid = user.getUid();
                                        usersRef.child(uid).child("belongsToTeam").setValue("null");
                                        usersRef.child(uid).child("emailAddress").setValue(user.getEmail());
                                        usersRef.child(uid).child("displayName").setValue(user.getDisplayName());
                                        usersRef.child(uid).child("isTeamCreator").setValue(false);
                                        usersRef.child(uid).child("fcmToken").setValue(fcmToken);

                                        addFcmTokenToDB(uid);
                                    } else {
                                        //TODO: padaryti, kad pasiimtu is db jau esanti user, nes dabar jei jau yra user su tokiu emailu, niekas nevyksta
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            loadMainWindow();
                        } else {
                            Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void addFcmTokenToDB(String uid) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("TAG", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        fcmToken = task.getResult();
                        usersRef.child(uid).child("fcmToken").setValue(fcmToken);
                    }
                });
    }
}
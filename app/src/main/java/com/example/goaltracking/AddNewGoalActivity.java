package com.example.goaltracking;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ActionProvider;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.goaltracking.Model.Goal;
import com.example.goaltracking.Model.User;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tokenautocomplete.TokenCompleteTextView;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddNewGoalActivity extends AppCompatActivity implements TokenCompleteTextView.TokenListener { //

    private final static int RC_ADD_GOAL = 124;
    EditText textGoal;
    EditText textDate;
    EditText textValue;
    CheckBox checkShowProgressToEveryone;
    final Calendar myCalendar = Calendar.getInstance();
    CheckBox checkIsImportant;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.FRANCE);
    List<User> usersEligibleForInvitation = new ArrayList<>();
    User currentUser;
    boolean editingMode;
    Goal editingGoal;
    ArrayAdapter<User> usersAdapter;
    TableLayout okrTable;
    Button btnAddGoal;
    float scale;
    MaterialButtonToggleGroup materialButtonToggleGroup;
    int selectedRowId;
//    ToggleSwitch toggleSwitch;
    ContactsCompletionView contactsCompletionView;
    Spinner spinnerNotificationPeriod;

//    GoogleSignInAccount googleAccount;
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://goal-tracking-ccad5-default-rtdb.europe-west1.firebasedatabase.app/");
    DatabaseReference goalsRef = database.getReference("Goals");
    DatabaseReference usersRef = database.getReference("Users");
    DatabaseReference responsibleUsersRef = database.getReference("ResponsibleUsers");

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_goal);
        scale = getResources().getDisplayMetrics().density;
        okrTable = findViewById(R.id.tableOKR);
        contactsCompletionView = findViewById(R.id.boxInvitedPeople);
        materialButtonToggleGroup = findViewById(R.id.toggleButtonsGroup);
        checkShowProgressToEveryone = findViewById(R.id.checkShowProgressToEveryone);
        spinnerNotificationPeriod = findViewById(R.id.spinnerNotificationPeriod);
        materialButtonToggleGroup.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (checkedId == R.id.btnIndividual) {
                    contactsCompletionView.setVisibility(View.INVISIBLE);
                    checkShowProgressToEveryone.setVisibility(View.INVISIBLE);
                } else {
                    contactsCompletionView.setVisibility(View.VISIBLE);
                    checkShowProgressToEveryone.setVisibility(View.VISIBLE);
                }
            }
        });

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        currentUser = (User) extras.getSerializable("currentUser");
        editingMode = extras.getBoolean("editingMode", false);

        if (extras.containsKey("editingGoal")) {
            editingGoal = (Goal) extras.getSerializable("editingGoal");
        }

        if (!currentUser.getIsTeamCreator())
            disableMaterialButtonGroup();

        textGoal = findViewById(R.id.editTextGoal);
        textValue = findViewById(R.id.editTextValue);
        textDate = findViewById(R.id.editTextOkrDueDate);
        btnAddGoal = findViewById(R.id.btnAddGoal);

        usersAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, usersEligibleForInvitation);
        contactsCompletionView.setAdapter(usersAdapter);
        contactsCompletionView.setTokenListener(this);

        ValueEventListener usersListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    usersEligibleForInvitation.clear();
                    // Get Post object and use the values to update the UI
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        String userId = singleSnapshot.getKey();
                        User user = singleSnapshot.getValue(User.class);
                        user.setId(userId);
                        if (!user.getEmailAddress().equals(currentUser.getEmailAddress()) && user.getBelongsToTeam().equals(currentUser.getBelongsToTeam())) {
                            usersEligibleForInvitation.add(user);
//                            invitationsAdapter.getFilter().filter(autoCompleteResponsibleUsers.getText(), null);
                        }
                    }
                    usersAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
//                Toast.makeText(AllGoalsActivity.this, databaseError.toException().toString(), Toast.LENGTH_SHORT).show();
            }
        };

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() { // TODO: PADARYTI, KAD TAI BUTU ISSAUGOMA DB
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH,month);
                myCalendar.set(Calendar.DAY_OF_MONTH,day);
                updateDateEditText(textDate);
//                textDate.setText(myCalendar.getTime().toString());
            }
        };
        textDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(AddNewGoalActivity.this,date,myCalendar.get(Calendar.YEAR),myCalendar.get(Calendar.MONTH),myCalendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                datePickerDialog.show();
            }
        });

        checkIsImportant = findViewById(R.id.checkIsImportant);
//        googleAccount = GoogleSignIn.getLastSignedInAccount(this);
        usersRef.addValueEventListener(usersListener);

        if (editingMode) {
            disableMaterialButtonGroup();
            textGoal.setText(editingGoal.getGoal());
            textValue.setText(Integer.toString(editingGoal.getValue()));
            textDate.setText(editingGoal.getDueDate());
            btnAddGoal.setText("Save");
            if (editingGoal.getIsImportant())
                checkIsImportant.setChecked(true);
            for (int i = 0; i < editingGoal.getOkrGoals().stream().count(); i++) {
                Goal okrGoal = editingGoal.getOkrGoals().get(i);
                addTableRow(okrGoal.getGoal(), Integer.toString(okrGoal.getValue()), okrGoal.getDueDate(), String.valueOf(okrGoal.getDifficulty()));
            }
        }
//        Toast.makeText(this, googleAccount.getGivenName() + " " + googleAccount.getFamilyName(), Toast.LENGTH_SHORT).show();
    }

    private void disableMaterialButtonGroup() {
        for (int i = 0; i < materialButtonToggleGroup.getChildCount(); i++) {
            materialButtonToggleGroup.getChildAt(i).setEnabled(false);
        }
    }

    private void updateDateEditText(EditText date) {
        date.setText(dateFormat.format(myCalendar.getTime()));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void btnAddGoalClick(View view) {
        if (isInformationFilled()) {
            Intent intent=new Intent();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
            String currentDateTime = sdf.format(new Date());
            String notificationPeriod = spinnerNotificationPeriod.getSelectedItem().toString();
            Goal goal = new Goal(textGoal.getText().toString(), Integer.parseInt(textValue.getText().toString()), currentUser.getId(), checkIsImportant.isChecked(), textDate.getText().toString(), currentDateTime, checkShowProgressToEveryone.isChecked(), notificationPeriod);

            List<User> invitedUsers = null;
            List<String> goalSharedWithIds = new ArrayList<>();
            if (materialButtonToggleGroup.getCheckedButtonId() != R.id.btnIndividual) {
                invitedUsers = contactsCompletionView.getObjects();
                for (int i = 0; i < invitedUsers.stream().count(); i++) {
                    goalSharedWithIds.add(invitedUsers.get(i).getId());
                }
            }

            List<Goal> okrGoalsList = new ArrayList<>();
            for (int i = 0; i < okrTable.getChildCount(); i++) {
                View v = okrTable.getChildAt(i);
                if (v instanceof TableRow) {
                    TableRow row = (TableRow) v;
                    Goal subGoal = new Goal();
                    subGoal.setGoal(((TextView) row.getChildAt(0)).getText().toString());
                    subGoal.setValue(Integer.parseInt(((TextView) row.getChildAt(1)).getText().toString()));
                    subGoal.setDueDate(((TextView) row.getChildAt(2)).getText().toString());
                    subGoal.setDifficulty(Integer.parseInt(((TextView) row.getChildAt(3)).getText().toString()));
                    subGoal.setDone(false);
                    okrGoalsList.add(subGoal);
                }
            }
            goal.setOkrGoals(okrGoalsList);

            if (!editingMode) {
                goal.setSharedWith(goalSharedWithIds);
                String goalKey = usersRef.child(currentUser.getId()).child("Goals").push().getKey();
                goal.setGoalId(goalKey);
                usersRef.child(currentUser.getId()).child("Goals").child(goalKey).setValue(goal);

                goal.setSharedWith(null);
                for (int i = 0; i < goalSharedWithIds.stream().count(); i++) {
                    String id = goalSharedWithIds.get(i);
                    usersRef.child(id).child("Goals").child(goalKey).setValue(goal);
                }
            } else {
                goal.setSharedWith(null);
                usersRef.child(currentUser.getId()).child("Goals").child(editingGoal.getGoalId()).setValue(goal);
            }

            intent.putExtra("goalInfo",  goal.toString());
            setResult(RC_ADD_GOAL,intent);
            finish();
        } else {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
        }
    }

    private Boolean isInformationFilled() {
        if (!textGoal.getText().toString().equals("") && !textValue.getText().toString().equals("") && !textDate.getText().toString().equals("") && okrTable.getChildCount() != 0)
            return true;
        return false;
    }

    private void addTableRow(String goalName, String goalValue, String goalDueDate, String goalDifficulty) {
        int trHeight = (int) (70 * scale + 0.5f);
        TableRow tableRow = new TableRow(this);
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, trHeight, 1f);
        tableRow.setLayoutParams(rowParams);

        TextView goalText = new TextView(this);
        goalText.setText(goalName);
        goalText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
        TableRow.LayoutParams params1 = new TableRow.LayoutParams(0, trHeight, 0.4f);
        goalText.setGravity(Gravity.CENTER_VERTICAL);
//        goalText.setBackgroundResource(R.drawable.border);
        goalText.setLayoutParams(params1);

        TextView valueText = new TextView(this);
        valueText.setText(goalValue);
        valueText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
        TableRow.LayoutParams params2 = new TableRow.LayoutParams(0, trHeight, 0.2f);
        valueText.setGravity(Gravity.CENTER_VERTICAL);
//        valueText.setBackgroundResource(R.drawable.border);
        valueText.setLayoutParams(params2);

        TextView dueDateText = new TextView(this);
        dueDateText.setText(goalDueDate);
        dueDateText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
        TableRow.LayoutParams params3 = new TableRow.LayoutParams(0, trHeight, 0.27f);
        dueDateText.setGravity(Gravity.CENTER_VERTICAL);
//        dueDateText.setBackgroundResource(R.drawable.border);
        dueDateText.setLayoutParams(params3);

        TableRow.LayoutParams params4 = new TableRow.LayoutParams(0, trHeight - ((int) (20 * scale + 0.5f)), 0.13f);
        TextView difficultyText = new TextView(this);
        difficultyText.setText(String.valueOf(goalDifficulty));
        difficultyText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
        difficultyText.setGravity(Gravity.CENTER);
        difficultyText.setLayoutParams(params4);
//        Button addBtn = new Button(this);
//        addBtn.setId(okrTable.getChildCount());
//        addBtn.setTag(textGoal);
//        addBtn.setText("X");
//        addBtn.setBackgroundResource(R.drawable.border);
//        addBtn.setLayoutParams(params4);

//        addBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Button clickedButton = (Button) view;
//                int rowId = clickedButton.getId();
//                okrTable.removeViewAt(rowId);
//
//                decreaseAllButtonIds(rowId);
//                Toast.makeText(AddNewGoalActivity.this, ((Button)view).getTag().toString(), Toast.LENGTH_SHORT).show();
//            }
//        });

        tableRow.addView(goalText);
        tableRow.addView(valueText);
        tableRow.addView(dueDateText);
//        tableRow.addView(addBtn);
        tableRow.addView(difficultyText);

        tableRow.setId(okrTable.getChildCount());
        okrTable.addView(tableRow);
        registerForContextMenu(tableRow);

//        editTextGoal.setText("");
//        editTextValue.setText("");
//        editTextGoal.requestFocus();
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(findViewById(R.id.tableOKR).getWindowToken(), 0);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        selectedRowId = v.getId();
        menu.add("Delete");
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getTitle().equals("Delete")) {
            okrTable.removeViewAt(selectedRowId);
            for (int i = 0; i < okrTable.getChildCount(); i++) {
                if (i >= selectedRowId) {
                    int currentId = okrTable.getChildAt(i).getId();
                    okrTable.getChildAt(i).setId(currentId - 1);
                }
            }
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onTokenAdded(Object object) {
        String[] splittedToken = object.toString().split("\\s+");
        String email = splittedToken[splittedToken.length-1];
        if (usersEligibleForInvitation.stream().noneMatch(obj -> obj.getEmailAddress().equals(email))) {
            contactsCompletionView.removeObjectAsync((User) object);
            Toast.makeText(this, "User does not exist", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onTokenRemoved(Object token) {

    }

    @Override
    public void onTokenIgnored(Object token) {
        View view = findViewById(R.id.boxInvitedPeople);
        Toast.makeText(this, "User has already been added", Toast.LENGTH_SHORT).show();
    }

    public void btnAddOkrClick(View view) {
        final Dialog dialog = new Dialog(AddNewGoalActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.add_okr_goal_dialog);

        final EditText okrGoalName = dialog.findViewById(R.id.editTextOKRGoalName);
        final EditText okrGoalValue = dialog.findViewById(R.id.editTextOKRGoalValue);
        final EditText okrGoalDueDate = dialog.findViewById(R.id.editTextOkrDueDate);
        final Spinner okrDifficulty = dialog.findViewById(R.id.spinner2);
        Button btnAdd = dialog.findViewById(R.id.btnAddOkr);

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() { // TODO: PADARYTI, KAD TAI BUTU ISSAUGOMA DB
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH,month);
                myCalendar.set(Calendar.DAY_OF_MONTH,day);
                updateDateEditText(okrGoalDueDate);
//                textDate.setText(myCalendar.getTime().toString());
            }
        };
        okrGoalDueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(AddNewGoalActivity.this,date,myCalendar.get(Calendar.YEAR),myCalendar.get(Calendar.MONTH),myCalendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                datePickerDialog.show();
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String okrGoal, okrVal, okrDueDate, okrDiff;
                okrGoal = okrGoalName.getText().toString();
                okrVal = okrGoalValue.getText().toString();
                okrDueDate = okrGoalDueDate.getText().toString();
                okrDiff = String.valueOf(okrDifficulty.getSelectedItem().toString().split("\\s+")[1].charAt(1));
                if (okrGoal != "" && okrVal != "" && okrDueDate != "" && okrDiff != "") {
                    addTableRow(okrGoal, okrVal, okrDueDate, okrDiff);
                    dialog.dismiss();
                } else {
                    Toast.makeText(AddNewGoalActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();
    }
}
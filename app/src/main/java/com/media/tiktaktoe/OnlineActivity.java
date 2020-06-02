package com.media.tiktaktoe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Objects;

public class OnlineActivity extends AppCompatActivity {

    private TextView one, two, three, four, five, six, seven, eight, nine, player_1_Score, player_2_Score, status, player_1_Name, player_2_Name;

    private static final String OPPONENT = "opponent";
    private String PLAYER_1_PATTERN = "";
    private String PLAYER_2_PATTERN = "";
    public boolean PLAYER_1 = true, GAME_OVER = false, online = false;
    private String CLICKED_BUTTONS = "";
    private String lastMove = "";
    private String opponent = "";
    private boolean turn = false;
    private int wins = 0, losses = 0;

    private DatabaseReference referenceReceive, referenceSet, referencewin, referenceloss;
    private ProgressDialog dialog;

    private ArrayList<View> views;
    private static final String ALLBUTTONS = "123456789";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Objects.requireNonNull(getSupportActionBar()).isShowing()) {
            getSupportActionBar().setTitle("Tic Tac Toe");
        }

        readyDialog();

        Intent intent = getIntent();
        turn = intent.getBooleanExtra("Receiver", false);
        if (turn)
            dialog.show();

        views = new ArrayList<>();

        views.add(one = findViewById(R.id.one));
        views.add(two = findViewById(R.id.two));
        views.add(three = findViewById(R.id.three));
        views.add(four = findViewById(R.id.four));
        views.add(five = findViewById(R.id.five));
        views.add(six = findViewById(R.id.six));
        views.add(seven = findViewById(R.id.seven));
        views.add(eight = findViewById(R.id.eight));
        views.add(nine = findViewById(R.id.nine));
        status = findViewById(R.id.status);

        player_1_Name = findViewById(R.id.player1Name);
        player_1_Name.setSelected(true);
        player_2_Name = findViewById(R.id.player2Name);
        player_2_Name.setSelected(true);

        player_1_Score = findViewById(R.id.player1Score);
        player_2_Score = findViewById(R.id.player2Score);

        opponent = intent.getStringExtra(OPPONENT);
        player_1_Name.setText(opponent);
        player_2_Name.setText(getSharedPreferences(LauncherActivity.USER_DETAILS, MODE_PRIVATE)
                .getString(LauncherActivity.USER_ID, ""));

        attachListener();

    }

    private void readyDialog() {
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("Please wait for Opponent...");
    }

    ValueEventListener valueEventListenerLoss = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            losses = Integer.parseInt(Objects.requireNonNull(dataSnapshot.getValue()).toString());
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
    ValueEventListener valueEventListenerWin = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            wins = Integer.parseInt(Objects.requireNonNull(dataSnapshot.getValue()).toString());
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
    ValueEventListener valueEventListenerReceiver = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            String move = Objects.requireNonNull(dataSnapshot.getValue()).toString();
            if (!move.equals(lastMove)) {
                online = true;
                PLAYER_1 = false;
                if (dialog != null && dialog.isShowing())
                    dialog.dismiss();

                if (ALLBUTTONS.contains(move)) {
                    Clicked(views.get(Integer.parseInt(move) - 1));
                } else {
                    switch (move) {
                        case "requestRefresh":
                            manageAlertDialogToRefresh("requestRefresh");
                            break;
                        case "confirmRefresh":
                            manageAlertDialogToRefresh("confirmRefresh");
                            break;
                        case "requestEnd":
                            manageAlertDialogToRefresh("requestEnd");
                            break;
                        case "confirmEnd":
                            manageAlertDialogToRefresh("confirmEnd");
                            break;
                    }
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        referenceSet.setValue("confirmEnd");
        referenceReceive.setValue("");
        detachListeners();
    }

    private void detachListeners() {
        referenceReceive.setValue("");
        referenceloss.removeEventListener(valueEventListenerLoss);
        referencewin.removeEventListener(valueEventListenerWin);
        referenceReceive.removeEventListener(valueEventListenerReceiver);
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        referenceSet.setValue("confirmEnd");
        referenceReceive.setValue("");
        detachListeners();
    }

    @Override
    public void onBackPressed() {
        referenceSet.setValue("confirmEnd");
        referenceReceive.setValue("");
        detachListeners();
        finish();
    }

    private void attachListener() {
        try {
            // R E F E R E N C E S
            referenceloss = FirebaseDatabase.getInstance().getReference(LauncherActivity.NODE_OF_USERS)
                    .child(getSharedPreferences(LauncherActivity.USER_DETAILS, MODE_PRIVATE)
                            .getString(LauncherActivity.USER_ID, "")).child("stats").child("losses");

            referencewin = FirebaseDatabase.getInstance().getReference(LauncherActivity.NODE_OF_USERS)
                    .child(getSharedPreferences(LauncherActivity.USER_DETAILS, MODE_PRIVATE)
                            .getString(LauncherActivity.USER_ID, "")).child("stats").child("wins");

            referenceReceive = FirebaseDatabase.getInstance().getReference(LauncherActivity.NODE_OF_USERS)
                    .child(getSharedPreferences(LauncherActivity.USER_DETAILS, MODE_PRIVATE)
                            .getString(LauncherActivity.USER_ID, "")).child("status").child("oppositionid");

            referenceSet = FirebaseDatabase.getInstance().getReference(LauncherActivity.NODE_OF_USERS)
                    .child(opponent).child("status").child("oppositionid");

            // L I S T E N E R S

            referenceloss.addValueEventListener(valueEventListenerLoss);

            referencewin.addValueEventListener(valueEventListenerWin);

            referenceReceive.addValueEventListener(valueEventListenerReceiver);

        } catch (Exception ignored) {
        }
    }

    private void manageAlertDialogToRefresh(final String msg) {
        try {
            switch (msg) {
                case "requestRefresh":
                    openAlert(msg.replace("request", ""));
                    break;
                case "confirmRefresh":
                    refreshTextViews();
                    referenceReceive.setValue("");
                    referencewin.setValue(wins + Integer.parseInt(player_1_Score.getText().toString()) + "");
                    referenceloss.setValue(losses + Integer.parseInt(player_2_Score.getText().toString()) + "");
                    break;
                case "requestEnd":
                    openAlert(msg.replace("request", ""));
                    break;
                case "confirmEnd":
                    referencewin.setValue(wins + Integer.parseInt(player_1_Score.getText().toString()) + "");
                    referenceloss.setValue(losses + Integer.parseInt(player_2_Score.getText().toString()) + "");
                    referenceReceive.setValue("");
                    finish();
                    break;
                default:
                    break;
            }
        } catch (Exception ignored) {
        }

    }

    private void openAlert(final String msg) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Confirm");
            builder.setMessage("Opponent want to " + msg + " the game. Do you ?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (msg) {
                        case "Refresh":
                            refreshTextViews();
                            referenceReceive.setValue("");
                            referenceSet.setValue("confirmRefresh");
                            dialog.dismiss();
                            break;
                        case "End":
                            referenceReceive.setValue("");
                            referenceSet.setValue("confirmEnd");
                            referenceloss.setValue(losses + Integer.parseInt(player_2_Score.getText().toString()) + "");
                            referencewin.setValue(wins + Integer.parseInt(player_1_Score.getText().toString()) + "");
                            dialog.dismiss();
                            finish();
                            break;
                    }

                }
            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    referenceReceive.setValue("");
                    dialog.dismiss();
                }
            });
            builder.create().show();
        } catch (Exception ignored) {
        }
    }


    private void manageDialog() {
        try {
            if (dialog.isShowing()) {
                dialog.dismiss();
            } else if (!dialog.isShowing()) {
                dialog.show();
            }
        } catch (Exception ignored) {
        }
    }

    private boolean win_con(String PATTERN) {
        return (PATTERN.contains("1") && PATTERN.contains("2") && PATTERN.contains("3"))
                || (PATTERN.contains("4") && PATTERN.contains("5") && PATTERN.contains("6"))
                || (PATTERN.contains("7") && PATTERN.contains("8") && PATTERN.contains("9"))
                || (PATTERN.contains("1") && PATTERN.contains("4") && PATTERN.contains("7"))
                || (PATTERN.contains("2") && PATTERN.contains("5") && PATTERN.contains("8"))
                || (PATTERN.contains("3") && PATTERN.contains("6") && PATTERN.contains("9"))
                || (PATTERN.contains("1") && PATTERN.contains("5") && PATTERN.contains("9"))
                || (PATTERN.contains("3") && PATTERN.contains("5") && PATTERN.contains("7"));
    }

    @SuppressLint("SetTextI18n")
    private void announceWinner(int i) {
        dialog.setCancelable(true);
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        switch (i) {
            case 2:
                player_1_Score.setText(Integer.parseInt(player_1_Score.getText().toString()) + 1 + "");
                status.setText(player_1_Name.getText().toString() + " won ");
                break;
            case 1:
                player_2_Score.setText(Integer.parseInt(player_2_Score.getText().toString()) + 1 + "");
                status.setText(player_2_Name.getText().toString() + " won ");
                break;
            case 0:
                status.setText(" Match Tied ");
                break;
        }

       GAME_OVER = true;
    }

    @SuppressLint("SetTextI18n")
    private void refreshTextViews() {
        one.setText("");
        two.setText("");
        three.setText("");
        four.setText("");
        five.setText("");
        six.setText("");
        seven.setText("");
        eight.setText("");
        nine.setText("");

        PLAYER_1_PATTERN = "";
        PLAYER_2_PATTERN = "";
        CLICKED_BUTTONS = "";
        PLAYER_1 = true;
        status.setText("Result");
        GAME_OVER = false;
        online = false;
        lastMove = "";
        dialog.setCancelable(false);
        turn = !turn;
        if (turn)
            dialog.show();

    }

    public void Clicked(View view) {
        try {
            if (!GAME_OVER) {
                String tag = view.getTag().toString();
                if (!CLICKED_BUTTONS.contains(tag)) {
                    CLICKED_BUTTONS += tag;
                    lastMove = tag;
                    if (PLAYER_1) {
                        referenceSet.setValue(tag);
                        PLAYER_1_PATTERN += PLAYER_1_PATTERN + tag;
                        ((TextView) view).setText("X");
                        PLAYER_1 = false;
                        // C H E C K   W I N   C O N D I T I O N
                        if (win_con(PLAYER_1_PATTERN))
                            announceWinner(1);
                    } else {
                        referenceSet.setValue(tag);
                        PLAYER_2_PATTERN += PLAYER_2_PATTERN + tag;
                        ((TextView) view).setText("O");
                        PLAYER_1 = true;
                        // C H E C K   W I N   C O N D I T I O N
                        if (win_con(PLAYER_2_PATTERN))
                            announceWinner(2);
                    }
                    if (CLICKED_BUTTONS.length() == 9)
                        announceWinner(0);

                    if (!online) {
                        manageDialog();
                    } else
                        online = false;
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                referenceSet.setValue("requestRefresh");
                break;
            case R.id.end:
                referenceSet.setValue("requestEnd");
                break;
        }
        return true;
    }
}

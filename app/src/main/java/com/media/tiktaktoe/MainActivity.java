package com.media.tiktaktoe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    public String PLAYER_1_PATTERN = "";
    public String PLAYER_2_PATTERN = "";
    public boolean PLAYER_1 = true, GAME_OVER = false;

    private TextView one, two, three, four, five, six, seven, eight, nine, player_1_Score,
            player_2_Score, status, player_1_Name, player_2_Name;

    private String CLICKED_BUTTONS = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Objects.requireNonNull(getSupportActionBar()).isShowing()) {
            getSupportActionBar().setTitle("");
        }

        one = findViewById(R.id.one);
        two = findViewById(R.id.two);
        three = findViewById(R.id.three);
        four = findViewById(R.id.four);
        five = findViewById(R.id.five);
        six = findViewById(R.id.six);
        seven = findViewById(R.id.seven);
        eight = findViewById(R.id.eight);
        nine = findViewById(R.id.nine);

        player_1_Name = findViewById(R.id.player1Name);
        player_2_Name = findViewById(R.id.player2Name);

        player_1_Score = findViewById(R.id.player1Score);
        player_2_Score = findViewById(R.id.player2Score);

        status = findViewById(R.id.status);

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
        switch (i) {
            case 1:
                player_1_Score.setText(Integer.parseInt(player_1_Score.getText().toString()) + 1 + "");
                status.setText(player_1_Name.getText().toString().substring(0, player_1_Name.getText().toString().length() - 1)
                        + " won ");
                break;
            case 2:
                player_2_Score.setText(Integer.parseInt(player_2_Score.getText().toString()) + 1 + "");
                status.setText(player_2_Name.getText().toString().substring(0, player_2_Name.getText().toString().length() - 1)
                        + " won ");
                break;
            case 0:
                status.setText(" Match Tied ");
                break;
        }
        refreshGameStatus();
    }


    private void refreshGameStatus() {
        PLAYER_1 = true;
        GAME_OVER = true;
        PLAYER_1_PATTERN = "";
        PLAYER_2_PATTERN = "";
        CLICKED_BUTTONS = "";
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

        status.setText("Result");
        GAME_OVER = false;

    }


    public void Clicked(View view) {
        try {
            if (!GAME_OVER) {
                String tag = view.getTag().toString();
                if (!CLICKED_BUTTONS.contains(tag)) {
                    CLICKED_BUTTONS += tag;
                    if (PLAYER_1) {
                        PLAYER_1_PATTERN += PLAYER_1_PATTERN + tag;
                        ((TextView)view).setText("X");
                        PLAYER_1 = false;
                        // C H E C K   W I N   C O N D I T I O N
                        if (win_con(PLAYER_1_PATTERN))
                            announceWinner(1);
                    } else {
                        PLAYER_2_PATTERN += PLAYER_2_PATTERN + tag;
                        ((TextView)view).setText("O");
                        PLAYER_1 = true;
                        // C H E C K   W I N   C O N D I T I O N
                        if (win_con(PLAYER_2_PATTERN))
                            announceWinner(2);
                    }
                    if (CLICKED_BUTTONS.length() == 9)
                        announceWinner(0);
                }
            }
        } catch (Exception e) {
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
                refreshTextViews();
                break;
            case R.id.end:
                onBackPressed();
                break;
            case R.id.goOnline:
                startActivity(new Intent(this, LauncherActivity.class));
                break;
        }
        return true;
    }

}

package com.media.tiktaktoe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.media.tiktaktoe.entity.Player;
import com.media.tiktaktoe.entity.Stats;
import com.media.tiktaktoe.entity.Status;
import com.media.tiktaktoe.helper.RequestHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class LauncherActivity extends AppCompatActivity {

    private TextView noUser;
    private RecyclerView recyclerView;
    private PlayersAdapter adapter;
    private ArrayList<Player> players;
    private DatabaseReference reference, referenceRequestor;
    private FirebaseAuth auth;
    public static final String REJECTED = "REJECTED";
    public static final String OFFLINE = "offline";
    public static final String ONLINE = "online";
    public static final String NODE_OF_USERS = "users";
    public static final String USER_DETAILS = "myDetails";
    public static final String USER_ID = "user_id";
    public static final String STATUS_NODE = "status";
    public static final String CURRENT_STATUS = "currentstatus";
    public static final String OPPOSITION_ID = "oppositionid";
    public static final String STATUS = "status";
    private ArrayList<Player> temp = new ArrayList<>();
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        if (Objects.requireNonNull(getSupportActionBar()).isShowing()) {
            getSupportActionBar().setTitle("Players Online");
        }

        dialog = new Dialog(this);

        noUser = findViewById(R.id.noplayers);
        recyclerView = findViewById(R.id.playersRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PlayersAdapter(this);
        recyclerView.setAdapter(adapter);
        players = new ArrayList<>();

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            createUser();
        } else {
            fetchAllPlayers();
            attachListener();
        }
    }

    private void attachListener() {
        try {

            referenceRequestor = FirebaseDatabase.getInstance().getReference(NODE_OF_USERS)
                    .child(getSharedPreferences(USER_DETAILS, MODE_PRIVATE).getString(USER_ID, ""))
                    .child(STATUS).child(OPPOSITION_ID);

            referenceRequestor.addValueEventListener(new ValueEventListener() {
                @SuppressLint("ShowToast")
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        if (dataSnapshot != null) {
                            if (dataSnapshot.getValue().toString().equals(REJECTED)) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(LauncherActivity.this);
                                builder.setMessage("Sorry your request has been rejected");
                                builder.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        referenceRequestor.setValue("");
                                        dialog.dismiss();
                                    }
                                });
                                builder.create().show();
                            } else if (dataSnapshot.getValue().toString().contains("(ACCEPTED)")) {
                                String oppenent = dataSnapshot.getValue().toString().substring(10);
                                referenceRequestor.setValue("");
                                Toast.makeText(LauncherActivity.this, "confirmation done", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LauncherActivity.this, OnlineActivity.class)
                                        .putExtra("opponent", oppenent).putExtra("Receiver", true));

                            } else if (dataSnapshot.getValue().toString().contains("(REQUEST)")) {
                                RequestHelper.openRecieverDialog(LauncherActivity.this, dataSnapshot.getValue().toString());
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } catch (Exception ignored) {
        }
    }

    private void createUser() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText editText = new EditText(this);
        builder.setView(editText);
        builder.setTitle("Enter Name");
        builder.setCancelable(false);
        builder.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = editText.getText().toString();

                if (!name.isEmpty()) {
                    if (name.contains(" "))
                        name = name.replace(" ", "");

                    String password = name.trim() + new Date().getTime();
                    // A D D   E N T R Y   T O   R e a l t i m e   D B
                    Player player = new Player(password, name
                            , new Stats("0", "0")
                            , new Status(ONLINE, ""));
                    saveUser(player, password + "@gmail.com", password);
                    dialog.dismiss();
                } else
                    Toast.makeText(LauncherActivity.this, "Name can't be empty", Toast.LENGTH_SHORT).show();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        builder.create().show();
    }

    private void saveUser(final Player player, final String name, final String password) {
        try {
            final ProgressDialog dialog = new ProgressDialog(this);
            dialog.setMessage("Saving information...");
            dialog.show();
            // C R E A T E   U S E R
            auth.createUserWithEmailAndPassword(name, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            auth.signInWithEmailAndPassword(name, password)
                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {

                                            reference = FirebaseDatabase.getInstance().getReference(NODE_OF_USERS).child(password);
                                            reference.setValue(player);

                                            // A D D   T O   S H A R E D     P R E F E R E N C E
                                            SharedPreferences sharedPreferences = getSharedPreferences(USER_DETAILS, MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putString(USER_ID, password);
                                            editor.apply();
                                            fetchAllPlayers();
                                            dialog.dismiss();
                                            attachListener();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("Error1", "onFailure: " + e.getMessage());
                                    Toast.makeText(LauncherActivity.this, "1." + e, Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("Error2", "onFailure: " + e.getMessage());
                    Toast.makeText(LauncherActivity.this, "2." + e, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.launcher_menu, menu);

        MenuItem item = menu.findItem(R.id.search_bar);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.equals("")) {
                    adapter.setPlayers(players);
                    return true;
                }
                temp.clear();
                for (Player p : players) {
                    if (p.id.contains(newText))
                        temp.add(p);
                }
                adapter.setPlayers(temp);
                return true;
            }
        });


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.changeName:
                showChangeNameDialog();
                break;

            case R.id.mystats:
                openStatsDialog();
                break;

            case R.id.about:
                openAboutDialog();
                break;
            case R.id.playOffline:
                finish();
                break;

        }
        return true;
    }

    @SuppressLint("SetTextI18n")
    private void openAboutDialog() {
        try {
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_layout, null, false);
            TextView heading = view.findViewById(R.id.heading);
            TextView value = view.findViewById(R.id.value);

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            heading.setText(R.string.developersheading);
            value.setText(R.string.developers);
            value.setGravity(Gravity.NO_GRAVITY);
            value.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            dialog.setContentView(view);
            dialog.show();
        } catch (Exception ignored) {
        }
    }

    private void openStatsDialog() {
        try {
            final View view = LayoutInflater.from(this).inflate(R.layout.dialog_layout, null, false);
            TextView heading = view.findViewById(R.id.heading);
            final TextView value = view.findViewById(R.id.value);

            final Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            heading.setText(R.string.won_to_loose);
            dialog.setContentView(view);
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    dialog.dismiss();
                }
            });

            reference = FirebaseDatabase.getInstance().getReference(NODE_OF_USERS)
                    .child(getSharedPreferences(USER_DETAILS, MODE_PRIVATE).getString(USER_ID, "")).child("stats");
            reference.addValueEventListener(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Stats stats = dataSnapshot.getValue(Stats.class);
                    if (stats != null) {
                        value.setText(stats.wins + " / " + stats.losses);
                    }
                    dialog.setContentView(view);
                    dialog.show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(LauncherActivity.this, "Some Error", Toast.LENGTH_SHORT).show();
                }
            });

            dialog.show();

        } catch (Exception ignored) {
        }
    }

    private void showChangeNameDialog() {
        try {
            if (auth.getCurrentUser() != null) {
                String userID = getSharedPreferences(USER_DETAILS, MODE_PRIVATE).getString(USER_ID, "");
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Enter new name :");
                final EditText editText = new EditText(this);
                builder.setView(editText);
                builder.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        String newName = editText.getText().toString();
                        if (newName != null) {
                            if (newName.contains(" "))
                                newName = newName.replace(" ", "");
                            final String password = newName.trim() + new Date().getTime();

                            auth.getCurrentUser().delete();
                            reference = FirebaseDatabase.getInstance().getReference(NODE_OF_USERS)
                                    .child(getSharedPreferences(USER_DETAILS, MODE_PRIVATE).getString(USER_ID, ""));
                            final String finalNewName = newName;
                            reference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Player player = dataSnapshot.getValue(Player.class);
                                    if (player != null) {
                                        String ref = player.id;
                                        player.id = password;
                                        player.name = finalNewName;
                                        saveUser(player, password + "@gmail.com", password);
                                        FirebaseDatabase.getInstance().getReference(NODE_OF_USERS).child(ref).removeValue();
                                    }
                                    dialog.dismiss();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });


                        } else
                            Toast.makeText(LauncherActivity.this, "Field can't be empty", Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        } catch (Exception ignored) {
        }
    }


    private void fetchAllPlayers() {
        try {
            final ProgressDialog dialog = new ProgressDialog(this);
            dialog.setTitle("Loading Players...");
            dialog.setMessage("Checking Internet...");
            dialog.setCancelable(false);
            dialog.show();

            reference = FirebaseDatabase.getInstance().getReference(NODE_OF_USERS);
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    players.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot != null) {
                            Player player = snapshot.getValue(Player.class);
                            if (player != null && player.status.currentstatus.equals(ONLINE)
                                    && !player.id.equals(getSharedPreferences(USER_DETAILS, MODE_PRIVATE)
                                    .getString(USER_ID, ""))) {
                                players.add(player);
                            }
                        }
                    }
                    if (players.isEmpty()) noUser.setVisibility(View.VISIBLE);
                    else noUser.setVisibility(View.GONE);

                    adapter.setPlayers(players);
                    dialog.dismiss();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(LauncherActivity.this, "Some Error", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
        }catch (Exception ignored){}
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (auth.getCurrentUser() != null)
            setStatus(ONLINE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (auth.getCurrentUser() != null)
            setStatus(ONLINE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (auth.getCurrentUser() != null)
            setStatus(ONLINE);
    }

    @Override
    public void finish() {
        super.finish();
        if (auth.getCurrentUser() != null)
            refreshDataStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (auth.getCurrentUser() != null)
            refreshDataStatus();
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (auth.getCurrentUser() != null) {
            refreshDataStatus();
        }
    }

    private void setStatus(String status) {
        reference = FirebaseDatabase.getInstance().getReference(NODE_OF_USERS)
                .child(getSharedPreferences(USER_DETAILS, MODE_PRIVATE).getString(USER_ID, ""))
                .child(STATUS_NODE).child(CURRENT_STATUS);
        reference.setValue(status);
    }

    private void refreshDataStatus(){
        referenceRequestor.setValue("");
        setStatus(OFFLINE);
    }
}

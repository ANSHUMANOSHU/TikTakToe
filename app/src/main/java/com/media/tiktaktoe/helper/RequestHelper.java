package com.media.tiktaktoe.helper;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.media.tiktaktoe.LauncherActivity;
import com.media.tiktaktoe.OnlineActivity;

public class RequestHelper {
    static DatabaseReference reference;
    public static final String REJECTED = "REJECTED";


    public static void openRequestDialog(final Context context, String name, final String id) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm");
        builder.setMessage("Do you want to play with " + name);
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                reference = FirebaseDatabase.getInstance().getReference(LauncherActivity.NODE_OF_USERS).child(id)
                        .child("status").child("oppositionid");
                reference.setValue("(REQUEST)"+context.getSharedPreferences(LauncherActivity.USER_DETAILS, Context.MODE_PRIVATE)
                        .getString(LauncherActivity.USER_ID, ""));
                dialog.dismiss();
                Toast.makeText(context, "Request Sent", Toast.LENGTH_SHORT).show();
            }
        });
        builder.create().show();
    }

    public static void openRecieverDialog(final Context context, final String requestor) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm");
        builder.setMessage("You got a request from  " + requestor.substring(9));
        builder.setNegativeButton("Reject", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                reference = FirebaseDatabase.getInstance().getReference(LauncherActivity.NODE_OF_USERS)
                        .child(requestor.replace("(REQUEST)",""))
                        .child("status").child("oppositionid");
                reference.setValue(REJECTED);

                reference = FirebaseDatabase.getInstance().getReference(LauncherActivity.NODE_OF_USERS)
                        .child(context.getSharedPreferences(LauncherActivity.USER_DETAILS,Context.MODE_PRIVATE)
                                .getString(LauncherActivity.USER_ID,"")).child("status").child("oppositionid");
                reference.setValue("");
                dialog.dismiss();
            }
        }).setPositiveButton("Accept", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                reference = FirebaseDatabase.getInstance().getReference(LauncherActivity.NODE_OF_USERS)
                        .child(requestor.substring(9))
                        .child("status").child("oppositionid");
                reference.setValue("(ACCEPTED)"+context.getSharedPreferences(LauncherActivity.USER_DETAILS, Context.MODE_PRIVATE)
                        .getString(LauncherActivity.USER_ID, ""));
                dialog.dismiss();
                // TODO  open game activity

                Intent intent = new Intent(context,OnlineActivity.class);
                intent.putExtra("opponent",requestor.substring(9));
                intent.putExtra("Receiver",false);
                reference = FirebaseDatabase.getInstance().getReference(LauncherActivity.NODE_OF_USERS)
                        .child(context.getSharedPreferences(LauncherActivity.USER_DETAILS,Context.MODE_PRIVATE)
                                .getString(LauncherActivity.USER_ID,"")).child("status").child("oppositionid");
                reference.setValue("");
                context.startActivity(intent);
            }
        });
        builder.create().show();

    }


}

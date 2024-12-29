package com.example.bookapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {

    //firebase auth
    private FirebaseAuth auth;

    //progress dialog
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        //init firebase auth
        auth = FirebaseAuth.getInstance();

        //check if user is already logged in


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Delay for 2 seconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkUser();
            }
        }, 2000);
    }

    private void checkUser() {
        //init progress dialog
        pd = new ProgressDialog(this);
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) {
            //user not logged in
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        } else {
            //user is logged in
            // get user id
            String uid = firebaseUser.getUid();
            // get user info
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // get user type
                    String userType = "" + snapshot.child("userType").getValue();
                    if (userType.equals("user")) {
                        // user is user
                        pd.dismiss();
                        // start user dashboard
                        startActivity(new Intent(SplashActivity.this, DashboardUserActivity.class));
                        finish();
                    } else if (userType.equals("admin")) {
                        // user is admin
                        pd.dismiss();
                        // start admin dashboard
                        startActivity(new Intent(SplashActivity.this, DashboardAdminActivity.class));
                        finish();
                    } else {
                        // user is neither user nor admin
                        pd.dismiss();
                        // show error message
                        Toast.makeText(SplashActivity.this, "Invalid user type", Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    //error
                    pd.dismiss();
                    Toast.makeText(SplashActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }
}
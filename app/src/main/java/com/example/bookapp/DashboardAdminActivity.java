package com.example.bookapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bookapp.databinding.ActivityDashboardAdminBinding;
import com.google.firebase.auth.FirebaseAuth;

public class DashboardAdminActivity extends AppCompatActivity {

    //firebase auth
    private FirebaseAuth auth;

    //view binding
    private ActivityDashboardAdminBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard_admin);
        binding = ActivityDashboardAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase auth
        auth = FirebaseAuth.getInstance();
        checkUser();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //handle logoutBtn click, logout user
        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //logout user
                auth.signOut();
                checkUser();
            }
        });

    }

    private void checkUser() {
        //check if user is already logged in
        if (auth.getCurrentUser() == null) {
            //user not logged in, start login activity
            startActivity(new Intent(DashboardAdminActivity.this, LoginActivity.class));
            finish();
        }
        else {
            String email = auth.getCurrentUser().getEmail();
            binding.subtitleTv.setText(email);
        }
    }
}
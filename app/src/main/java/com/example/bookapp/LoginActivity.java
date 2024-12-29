package com.example.bookapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bookapp.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    //view binding
    private ActivityLoginBinding binding;

    //firebase auth
    private FirebaseAuth auth;

    //progress dialog
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());

        //init firebase auth
        auth = FirebaseAuth.getInstance();

        // init progress dialog
        pd = new ProgressDialog(this);
        pd.setMessage("Registering user...");
        pd.setCanceledOnTouchOutside(false);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setContentView(binding.getRoot());

        // handle click, go to register activity
        binding.noAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // handle click
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // handle click
                validateData();            }
        });
    }
    private String email, password;

    private void validateData() {
        // get data
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString().trim();

        // validate data
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            // invalid email format
            binding.emailEt.setError("Invalid email format");
        }
        else if(TextUtils.isEmpty(password)){
            // no password entered
            binding.passwordEt.setError("Enter password");
        }
        else{
            // data is valid, login user
            loginUser(email, password);
        }

    }

    private void loginUser(String email, String password) {
        // login user
        pd.show();
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //check the user if it is admin or user
                        checkUser();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private void checkUser() {
        pd.setMessage("Checking user...");
        // get current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // get user id
        String uid = user.getUid();
        // get user info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // get user type
                String userType = ""+snapshot.child("userType").getValue();
                if(userType.equals("user")){
                    // user is user
                    pd.dismiss();
                    // start user dashboard
                    startActivity(new Intent(LoginActivity.this, DashboardUserActivity.class));
                    finish();
                }
                else if(userType.equals("admin")){
                    // user is admin
                    pd.dismiss();
                    // start admin dashboard
                    startActivity(new Intent(LoginActivity.this, DashboardAdminActivity.class));
                    finish();
                }else {
                    // user is neither user nor admin
                    pd.dismiss();
                    // show error message
                    Toast.makeText(LoginActivity.this, "Invalid user type", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
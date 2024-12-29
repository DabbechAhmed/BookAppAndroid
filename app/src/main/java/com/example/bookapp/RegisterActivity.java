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

import com.example.bookapp.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    // view binding
    private ActivityRegisterBinding binding;

    // firebase auth
    private FirebaseAuth auth;

    // progress dialog
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_register);

        // init firebase auth
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
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // handle back button click
                onBackPressed();
            }
        });
        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // handle register button click
                validateData();
            }
        });
    }
    String name, email, password, confirmPassword;
        private void validateData() {
            // get data
            name = binding.nameEt.getText().toString().trim();
             email = binding.emailEt.getText().toString().trim();
             password = binding.passwordEt.getText().toString().trim();
             confirmPassword = binding.passwordconfEt.getText().toString().trim();

            // validate data
            if (TextUtils.isEmpty(name)) {
                // no name entered
                Toast.makeText(this, "Enter name", Toast.LENGTH_SHORT).show();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                // invalid email format
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(password)) {
                // password is empty
                Toast.makeText(this, "Enter password", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(confirmPassword)) {
                // password doesn't match
                Toast.makeText(this, "Password doesn't match", Toast.LENGTH_SHORT).show();
            } else {
                // data is correct, continue to register user
                registerUser();
            }
        }

    private void registerUser() {
        //show progress dialog
        pd.setMessage("Registering user...");
        pd.show();

        // create user
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        // user registered
                       updateUserInfo();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // failed to register
                        pd.dismiss();
                        Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUserInfo() {
        // setup user data
        pd.setMessage("Updating user info...");
        long timestamp = System.currentTimeMillis();

        //get current user uid, since is registered
        String uid = auth.getUid();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("name", name);
        hashMap.put("email", email);
        hashMap.put("profileImage", "");
        hashMap.put("userType", "user"); // user type: user or admin
        hashMap.put("timestamp", timestamp);

        // save to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // user info updated
                        pd.dismiss();
                        Toast.makeText(RegisterActivity.this, "Registered...\n" + auth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, DashboardUserActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // failed to update user info
                        pd.dismiss();
                        Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

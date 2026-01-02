package com.example.splitwise;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.splitwise.databinding.ActivityRegisterBinding;

/**
 * Handles new user registration with username availability check
 */
public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private final String BASE_URL = "https://splitwise-backend-1-eg2d.onrender.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Registration handler
        binding.btnSubmit.setOnClickListener(v -> {
            String u = binding.etRegUsername.getText().toString().trim();
            String p = binding.etRegPassword.getText().toString().trim();
            if (!u.isEmpty() && !p.isEmpty()) {
                String hashedpass = HashUtils.hashPassword(p);
                processRegistration(u, hashedpass);
            }
        });

        // Navigate to login
        binding.btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    /**
     * Checks if username exists, creates user account and personal table
     */
    private void processRegistration(String u, String p) {
        new Thread(() -> {
            try {
                if (Exists.exist(BASE_URL + "/GetRowData?table=Credentials_Splitwise&username=" + u)) {
                    runOnUiThread(() -> Toast.makeText(this, "User already exists", Toast.LENGTH_SHORT).show());
                } else {
                    ApiCaller.ApiCaller1(BASE_URL + "/InsertData?table=Credentials_Splitwise&params=(username,password)&info=('" + u + "','" + p + "')");
                    ApiCaller.ApiCaller1(BASE_URL + "/CreateTable?table=PF_" + u + "&columns=id%20INT%20AUTO_INCREMENT%20PRIMARY%20KEY,GroupID%20VARCHAR(100),GroupName%20VARCHAR(100)");
                    runOnUiThread(() -> {
                        Intent intent = new Intent(this, GroupActivity.class);
                        intent.putExtra("KEY_USERNAME", u);
                        startActivity(intent);
                        finish();
                    });
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }
}
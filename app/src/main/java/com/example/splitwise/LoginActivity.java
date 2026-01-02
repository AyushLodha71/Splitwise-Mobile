package com.example.splitwise;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.splitwise.databinding.ActivityLoginBinding;

/**
 * Handles user authentication and login
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private final String BASE_URL = "https://splitwise-backend-1-eg2d.onrender.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Login button handler
        binding.btnSubmit.setOnClickListener(v -> {
            String u = binding.etLoginUsername.getText().toString().trim();
            String p = binding.etLoginPassword.getText().toString().trim();
            if (!u.isEmpty() && !p.isEmpty()) {
                String hashedpass = HashUtils.hashPassword(p);
                attemptLogin(u, hashedpass);
            }
        });

        // Navigate to registration
        binding.btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });
    }

    /**
     * Validates credentials against backend and navigates to GroupActivity on success
     */
    private void attemptLogin(String u, String p) {
        new Thread(() -> {
            try {
                String url = BASE_URL + "/GetRowData?table=Credentials_Splitwise&username=" + u + "&password=" + p;
                if (Exists.exist(url)) {
                    runOnUiThread(() -> {
                        Intent intent = new Intent(this, GroupActivity.class);
                        intent.putExtra("KEY_USERNAME", u);
                        startActivity(intent);
                        finish();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }
}
package com.example.splitwise;

import android.content.Intent;
import android.os.Bundle;
import android.view.View; // This is the equivalent of ActionEvent
import androidx.appcompat.app.AppCompatActivity;
import com.example.splitwise.databinding.ActivityWelcomeBinding;

// 1. Implement View.OnClickListener (This is like implementing ActionListener)
public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityWelcomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 2. Register the buttons to "this" listener
        binding.btnLogin.setOnClickListener(this);
        binding.btnRegister.setOnClickListener(this);
    }

    // 3. This is exactly like your actionPerformed(ActionEvent event)
    @Override
    public void onClick(View v) {
        // v.getId() is like event.getActionCommand()
        int id = v.getId();

        if (id == R.id.btnLogin) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.btnRegister) {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
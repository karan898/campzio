package com.abs.campzio.app.auth.otp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.abs.campzio.app.R;
import com.abs.campzio.app.auth.register.Register;
import com.abs.campzio.app.auth.signin.Login;
import com.abs.campzio.app.models.User;
import com.abs.campzio.app.repository.DataRepository;
import com.abs.campzio.app.utils.UIUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AccountActivation extends AppCompatActivity {

    private TextInputLayout enrollmentLayout, emailLayout;
    private TextInputEditText editTextEnrollment, editTextEmail;
    private Button verifyBtn;
    private ProgressBar progressBar;
    private TextView loginTv;

    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_activation);

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        enrollmentLayout = findViewById(R.id.activationEnrollmentLayout);
        emailLayout = findViewById(R.id.activationEmailLayout);
        editTextEnrollment = findViewById(R.id.activationEnrollment);
        editTextEmail = findViewById(R.id.activationEmail);
        verifyBtn = findViewById(R.id.activateButton);
        progressBar = findViewById(R.id.activationProgressBar);
        loginTv = findViewById(R.id.activationLoginTv);

        loginTv.setOnClickListener(v -> {
            startActivity(new Intent(AccountActivation.this, Login.class));
            finish();
        });

        verifyBtn.setOnClickListener(v -> verifyAccount());
    }

    private void verifyAccount() {
        String enrollment = editTextEnrollment.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();

        if (enrollment.isEmpty()) {
            enrollmentLayout.setError("Enter enrollment number");
            return;
        }
        if (!UIUtils.isValidEmail(email)) {
            emailLayout.setError("Enter registered email");
            return;
        }

        UIUtils.toggleProgress(progressBar, verifyBtn, true);

        // Query the user by enrollment (the key in your DB)
        userRef.child(enrollment).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String dbEmail = snapshot.child("email").getValue(String.class);
                    if (email.equalsIgnoreCase(dbEmail)) {
                        // Success! Proceed to register (set password)
                        Intent intent = new Intent(AccountActivation.this, Register.class);
                        intent.putExtra("enrollment", enrollment);
                        intent.putExtra("email", email);
                        intent.putExtra("name", snapshot.child("name").getValue(String.class));
                        intent.putExtra("phone", snapshot.child("id").getValue(String.class));
                        startActivity(intent);
                    } else {
                        UIUtils.toggleProgress(progressBar, verifyBtn, false);
                        Toast.makeText(AccountActivation.this, "Email does not match our records", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    UIUtils.toggleProgress(progressBar, verifyBtn, false);
                    Toast.makeText(AccountActivation.this, "Enrollment number not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                UIUtils.toggleProgress(progressBar, verifyBtn, false);
                Toast.makeText(AccountActivation.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

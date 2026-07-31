package com.abs.campzio.app.auth.signin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.abs.campzio.app.auth.session.MainActivity;
import com.abs.campzio.app.auth.otp.AccountActivation;
import com.abs.campzio.app.auth.session.SessionManager;
import com.abs.campzio.app.models.User;
import com.abs.campzio.app.repository.DataRepository;
import com.abs.campzio.app.utils.UIUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;
import java.util.Objects;

import com.abs.campzio.app.admin.AdminMainActivity;
import com.abs.campzio.app.R;
import com.abs.campzio.app.student.UserMainActivity;
import com.abs.campzio.app.auth.otp.PhoneVerification;

public class Login extends AppCompatActivity {

    TextInputLayout phoneLayout, emailLayout, enrollmentLayout, passwordLayout;
    TextInputEditText editTextPhone, editTextEmail, editTextEnrollment, editTextPassword;
    ProgressBar loginLayoutProgressBar;
    LinearLayout loginLayout;
    Button loginUserBtn;
    ProgressBar loginUserProgressBar;

    TextView signUpTv;
    private final static String TAG = "LoginActivity";
    String email, password;
    
    SessionManager sessionManager;
    DataRepository repository;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(getApplicationContext());
        repository = DataRepository.getInstance();

        loginLayoutProgressBar=findViewById(R.id.loginLayoutProgressBar);
        loginUserProgressBar=findViewById(R.id.loginUserProgressBar);
        loginLayout=findViewById(R.id.loginLayout);

        emailLayout=findViewById(R.id.loginEmailLayout);
        passwordLayout=findViewById(R.id.loginPasswordLayout);

        editTextEmail=findViewById(R.id.loginEmail);
        editTextPassword=findViewById(R.id.loginPassword);

        signUpTv=findViewById(R.id.signUpTv);
        signUpTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, AccountActivation.class);
                startActivity(intent);
                finish();
            }
        });

        loginUserBtn=findViewById(R.id.loginBtn);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
            if(ContextCompat.checkSelfPermission(Login.this, Manifest.permission.POST_NOTIFICATIONS)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(Login.this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
        loginUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateData()){
                    UIUtils.toggleProgress(loginUserProgressBar, loginUserBtn, true);
                    
                    repository.getUserByEmail(email, new DataRepository.RepositoryCallback<User>() {
                        @Override
                        public void onSuccess(User user) {
                            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    sessionManager.setLoggedIn(true);
                                    sessionManager.saveUser(user);
                                    
                                    Toast.makeText(Login.this, "Login successful", Toast.LENGTH_SHORT).show();
                                    
                                    Intent intent;
                                    if ("admin".equals(user.getRole())) {
                                        intent = new Intent(Login.this, AdminMainActivity.class);
                                    } else {
                                        intent = new Intent(Login.this, UserMainActivity.class);
                                    }
                                    
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    UIUtils.toggleProgress(loginUserProgressBar, loginUserBtn, false);
                                    handleAuthError(task.getException());
                                }
                            });
                        }

                        @Override
                        public void onError(String message) {
                            UIUtils.toggleProgress(loginUserProgressBar, loginUserBtn, false);
                            Toast.makeText(Login.this, message, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        editTextEmail.addTextChangedListener(emailTextWatcher);
        editTextPassword.addTextChangedListener(passwordTextWatcher);
    }

    private void handleAuthError(Exception e) {
        if (e instanceof FirebaseAuthInvalidUserException) {
            Toast.makeText(Login.this, "User not registered", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Login.this, PhoneVerification.class);
            startActivity(intent);
            finish();
        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
            Toast.makeText(Login.this, "Email or Password doesn't match", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(Login.this, e != null ? e.getMessage() : "Authentication failed", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateData() {
        return validateEmail() && validatePassword();
    }



    private TextWatcher emailTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            emailLayout.setError(null);
            emailLayout.setErrorEnabled(false);

        }
    };

    private TextWatcher passwordTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            passwordLayout.setErrorEnabled(true);
        }

        @Override
        public void afterTextChanged(Editable editable) {
            passwordLayout.setError(null);
            passwordLayout.setErrorEnabled(false);

        }
    };

    private boolean validateEmail() {
        email= Objects.requireNonNull(editTextEmail.getText()).toString().toLowerCase(Locale.ROOT).trim();
        if (email.isEmpty()){
            emailLayout.setError("Email should not be empty");
            loginUserProgressBar.setVisibility(View.GONE);
            loginUserBtn.setVisibility(View.VISIBLE);
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Enter a valid email address");
            loginUserProgressBar.setVisibility(View.GONE);
            loginUserBtn.setVisibility(View.VISIBLE);
            return false;
        }else {
            emailLayout.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        password= Objects.requireNonNull(editTextPassword.getText()).toString().trim();

        if (password.isEmpty()){
            passwordLayout.setError("Password should not be empty");
            loginUserProgressBar.setVisibility(View.GONE);
            loginUserBtn.setVisibility(View.VISIBLE);
            return false;
        }else if (password.length()<8){
            passwordLayout.setError("Password should be at least 8 characters long");
            loginUserProgressBar.setVisibility(View.GONE);
            loginUserBtn.setVisibility(View.VISIBLE);
            return false;
        }else {
            passwordLayout.setError(null);
            return true;
        }
    }
}

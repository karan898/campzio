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
import com.abs.campzio.app.auth.session.SessionManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.Locale;
import java.util.Objects;

import com.abs.campzio.app.admin.AdminMainActivity;
import com.abs.campzio.app.R;
import com.abs.campzio.app.student.UserMainActivity;
import com.abs.campzio.app.auth.otp.PhoneVerification;

public class Login extends AppCompatActivity {

    TextInputLayout phoneLayout, emailLayout, enrollmentLayout, passwordLayout;
    TextInputEditText editTextPhone, editTextEmail, editTextEnrollment, editTextPassword;
    TelephonyManager telephonyManager;
    ProgressBar loginLayoutProgressBar;
    LinearLayout loginLayout;
    Button loginUserBtn;
    ProgressBar loginUserProgressBar;

    TextView signUpTv;
    private final static String TAG = "LoginActivity";
    String country, phone, email, enrollment, password, role;
    String phoneNoWithoutCountryCode;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference userRef = database.getReference().child("Users");

    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String IS_FIRST_LAUNCH = "IsFirstLaunch";
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SessionManager sessionManager = new SessionManager(getApplicationContext());

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
                Intent intent = new Intent(Login.this, PhoneVerification.class);
                startActivity(intent);
                finish();
            }
        });
        telephonyManager = (TelephonyManager) this.getSystemService(Login.this.TELEPHONY_SERVICE);

        loginUserBtn=findViewById(R.id.loginBtn);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
            if(ContextCompat.checkSelfPermission(Login.this, Manifest.permission.POST_NOTIFICATIONS)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(Login.this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
        loginUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUserProgressBar.setVisibility(View.VISIBLE);
                loginUserBtn.setVisibility(View.GONE);
                if(validateData()){
                    //login
                    Query query = userRef.orderByChild("email").equalTo(email);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // Extract the user key (phone number) from the snapshot
                                DataSnapshot userSnapshot = dataSnapshot.getChildren().iterator().next();
                                String phoneKey = userSnapshot.getKey();

                                if (phoneKey == null) {
                                    loginUserProgressBar.setVisibility(View.GONE);
                                    loginUserBtn.setVisibility(View.VISIBLE);
                                    Toast.makeText(Login.this, "Error: User record is invalid", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        sessionManager.setLoggedIn(true);

                                        // Fetch role directly from the identified user node
                                        userRef.child(phoneKey).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                role = snapshot.child("role").getValue(String.class);
                                                if (role != null) {
                                                    if (role.equals("admin")) {
                                                        sessionManager.setAccountRoleAdmin(true);
                                                        Toast.makeText(Login.this, "Login successful", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(Login.this, AdminMainActivity.class);
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        startActivity(intent);
                                                        finish();
                                                    } else if (role.equals("user")) {
                                                        sessionManager.setAccountRoleUser(true);
                                                        Toast.makeText(Login.this, "Login successful", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(Login.this, UserMainActivity.class);
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        startActivity(intent);
                                                        finish();
                                                    } else {
                                                        loginUserProgressBar.setVisibility(View.GONE);
                                                        loginUserBtn.setVisibility(View.VISIBLE);
                                                        mAuth.signOut();
                                                        sessionManager.setLoggedIn(false);
                                                        Toast.makeText(Login.this, "The user role is undefined", Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                loginUserProgressBar.setVisibility(View.GONE);
                                                loginUserBtn.setVisibility(View.VISIBLE);
                                                mAuth.signOut();
                                                sessionManager.setLoggedIn(false);
                                                Toast.makeText(Login.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else {
                                        loginUserProgressBar.setVisibility(View.GONE);
                                        loginUserBtn.setVisibility(View.VISIBLE);
                                        try {
                                            throw Objects.requireNonNull(task.getException());
                                        } catch (FirebaseAuthInvalidUserException e) {
                                            Toast.makeText(Login.this, "User not registered", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(Login.this, PhoneVerification.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            finish();
                                        } catch (FirebaseAuthInvalidCredentialsException e) {
                                            Toast.makeText(Login.this, "Email or Password doesn't match", Toast.LENGTH_SHORT).show();
                                        } catch (Exception e) {
                                            Log.e(TAG, e.getMessage());
                                            Toast.makeText(Login.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }).addOnFailureListener(e -> {
                                    loginUserProgressBar.setVisibility(View.GONE);
                                    loginUserBtn.setVisibility(View.VISIBLE);
                                    Toast.makeText(Login.this, "Authentication failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                            } else {
                                loginUserProgressBar.setVisibility(View.GONE);
                                loginUserBtn.setVisibility(View.VISIBLE);
                                Toast.makeText(Login.this, "Record not found", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            loginUserProgressBar.setVisibility(View.GONE);
                            loginUserBtn.setVisibility(View.VISIBLE);
                            Toast.makeText(Login.this, "Database error", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    loginUserProgressBar.setVisibility(View.GONE);
                    loginUserBtn.setVisibility(View.VISIBLE);
                    Toast.makeText(Login.this, "something went wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });

        editTextEmail.addTextChangedListener(emailTextWatcher);

        editTextPassword.addTextChangedListener(passwordTextWatcher);
    }


    private boolean validateData() {
        if(validateEmail() && validatePassword())
            return true;
        else return false;

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

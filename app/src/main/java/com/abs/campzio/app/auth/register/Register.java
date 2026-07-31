package com.abs.campzio.app.auth.register;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.se.omapi.Session;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.abs.campzio.app.auth.session.SessionManager;
import com.abs.campzio.app.models.User;
import com.abs.campzio.app.repository.DataRepository;
import com.abs.campzio.app.utils.UIUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;

import com.abs.campzio.app.admin.AdminMainActivity;
import com.abs.campzio.app.R;
import com.abs.campzio.app.student.UserMainActivity;
import com.abs.campzio.app.auth.signin.Login;

public class Register extends AppCompatActivity {
    TextInputLayout countryLayout, phoneLayout, passwordLayout, confirmPasswordLayout, emailLayout, enrollmentLayout;
    TextInputEditText editTextName,editTextCountry, editTextPhone, editTextEmail, editTextEnrollment, editTextPassword, editTextConfirmPassword;
    Button registerUserBtn;

    TextView enrollmentTv, emailTv;

    String phone, email,name, enrollment,password,confirmPassword,role;
    private final static String TAG = "Register Activity";

    ProgressBar registerProgressBar;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    SessionManager sessionManager;
    DataRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        sessionManager = new SessionManager(getApplicationContext());
        repository = DataRepository.getInstance();
        
        registerProgressBar=findViewById(R.id.registerProgressBar);
        enrollmentTv=findViewById(R.id.enrollmentTv);
        emailTv=findViewById(R.id.emailTv);

        passwordLayout=findViewById(R.id.registerPasswordLayout);
        confirmPasswordLayout=findViewById(R.id.registerConfirmPasswordLayout);
        editTextPassword=findViewById(R.id.registerPassword);
        editTextConfirmPassword=findViewById(R.id.registerConfirmPassword);

        editTextPassword.addTextChangedListener(passwordTextWatcher);
        editTextConfirmPassword.addTextChangedListener(confirmPasswordTextWatcher);

        name=getIntent().getStringExtra("name");
        phone = getIntent().getStringExtra("phone");
        email=getIntent().getStringExtra("email");
        enrollment=getIntent().getStringExtra("enrollment");

        enrollmentTv.setText(enrollment);
        emailTv.setText(email);

        registerUserBtn=findViewById(R.id.registerBtn);

        registerUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateData()){
                    UIUtils.toggleProgress(registerProgressBar, registerUserBtn, true);
                    
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                if (firebaseUser != null) {
                                    // Update profile
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(name)
                                            .build();
                                    firebaseUser.updateProfile(profileUpdates);
                                    
                                    // Linking phone credential is often complex and might need re-auth, 
                                    // but for now let's just fetch the user from our DB and set the session.
                                    repository.getUserByPhone(phone, new DataRepository.RepositoryCallback<User>() {
                                        @Override
                                        public void onSuccess(User user) {
                                            sessionManager.setLoggedIn(true);
                                            sessionManager.saveUser(user);
                                            
                                            if (!firebaseUser.isEmailVerified()) {
                                                firebaseUser.sendEmailVerification();
                                            }
                                            
                                            Toast.makeText(Register.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                            
                                            Intent intent;
                                            if ("admin".equals(user.getRole())) {
                                                intent = new Intent(Register.this, AdminMainActivity.class);
                                            } else {
                                                intent = new Intent(Register.this, UserMainActivity.class);
                                            }
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            finish();
                                        }

                                        @Override
                                        public void onError(String message) {
                                            UIUtils.toggleProgress(registerProgressBar, registerUserBtn, false);
                                            Toast.makeText(Register.this, "Error fetching user data: " + message, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            } else {
                                UIUtils.toggleProgress(registerProgressBar, registerUserBtn, false);
                                handleRegistrationError(task.getException());
                            }
                        }
                    });
                }
            }
        });
    }

    private void handleRegistrationError(Exception e) {
        if (e instanceof FirebaseAuthUserCollisionException) {
            Toast.makeText(Register.this, "User already registered", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Register.this, Login.class));
            finish();
        } else {
            Toast.makeText(Register.this, e != null ? e.getMessage() : "Registration failed", Toast.LENGTH_SHORT).show();
        }
    }




    private boolean validateData() {
        if(validatePassword())
            return true;
        else return false;
    }


    /*private TextWatcher countryTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            countryLayout.setError(null);
            countryLayout.setErrorEnabled(false);

        }
    };

    private TextWatcher phoneTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            phoneLayout.setError(null);
            phoneLayout.setErrorEnabled(false);
            phoneLayout.setCounterEnabled(true);

        }
    };

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

    private TextWatcher enrollmentTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            enrollmentLayout.setError(null);
            enrollmentLayout.setErrorEnabled(false);
            enrollmentLayout.setCounterEnabled(true);
        }
    };*/

    private TextWatcher passwordTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            confirmPasswordLayout.setError(null);
            confirmPasswordLayout.setErrorEnabled(false);
        }

        @Override
        public void afterTextChanged(Editable editable) {
            passwordLayout.setError(null);
            passwordLayout.setErrorEnabled(false);

        }
    };

    private TextWatcher confirmPasswordTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            confirmPasswordLayout.setHelperText(null);
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            passwordLayout.setError(null);
            passwordLayout.setErrorEnabled(false);
        }

        @Override
        public void afterTextChanged(Editable editable) {
            confirmPasswordLayout.setError(null);
            confirmPasswordLayout.setErrorEnabled(false);

        }
    };


    /*private boolean validateCountry() {
        otpCountryCode= Objects.requireNonNull(editTextCountry.getText()).toString().trim();

        if (otpCountryCode.isEmpty()){
            countryLayout.setError("Country code should not be empty");
            return false;
        } else {
            countryLayout.setError(null);
            return true;
        }
    }
    private boolean validatePhone() {
        phone= editTextPhone.getText().toString().trim();

        if (phone.isEmpty()){
            phoneLayout.setError("Phone no. should not be empty");
            return false;
        } else if (phone.length() != 10) {
            phoneLayout.setError("Phone no. must be 10 digits long");
            return false;
        } else {
            phoneLayout.setError(null);
            return true;
        }
    }

    private boolean validateEmail() {
        email= editTextEmail.getText().toString().trim();
        if (email.isEmpty()){
            emailLayout.setError("Email should not be empty");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Enter a valid email address");
            return false;
        }else {
            emailLayout.setError(null);
            return true;
        }
    }

    private boolean validateEnrollment() {
        enrollment= editTextEnrollment.getText().toString().trim();

        if (enrollment.isEmpty()){
            enrollmentLayout.setError("Enrollment no. should not be empty");
            return false;
        } else if (enrollment.length()<10 || enrollment.length()>10) {
            enrollmentLayout.setError("Enrollment no. must be 10 digits long");
            return false;
        } else {
            enrollmentLayout.setError(null);
            return true;
        }
    }*/

    private boolean validatePassword() {
        password= editTextPassword.getText().toString().trim();
        confirmPassword= editTextConfirmPassword.getText().toString().trim();

        if (password.isEmpty()){
           passwordLayout.setError("Password should not be empty");
            registerProgressBar.setVisibility(View.GONE);
            registerUserBtn.setVisibility(View.VISIBLE);
           return false;
        }else if (password.length()<8){
            passwordLayout.setError("Password should be at least 8 characters long");
            registerProgressBar.setVisibility(View.GONE);
            registerUserBtn.setVisibility(View.VISIBLE);
            return false;
        }else if (!password.equals(confirmPassword) && !confirmPassword.equals(password)){
            passwordLayout.setError("Password doesn't match");
            confirmPasswordLayout.setError("Password doesn't match");
            registerProgressBar.setVisibility(View.GONE);
            registerUserBtn.setVisibility(View.VISIBLE);
            return false;
        }else {
            confirmPasswordLayout.setHelperText("Password matched :)");
            passwordLayout.setError(null);
            confirmPasswordLayout.setError(null);
            return true;
        }
    }
}

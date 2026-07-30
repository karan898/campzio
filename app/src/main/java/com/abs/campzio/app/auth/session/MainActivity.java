package com.abs.campzio.app.auth.session;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowInsetsAnimationController;
import android.widget.Toast;

import com.abs.campzio.app.R;
import com.abs.campzio.app.admin.AdminMainActivity;
import com.abs.campzio.app.auth.signin.Login;
import com.abs.campzio.app.student.UserMainActivity;
import com.google.firebase.auth.FirebaseAuth;
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

public class MainActivity extends AppCompatActivity {

    String role, phoneNoWithoutCountryCode;
    private final static String TAG = "MainActivity";
    TelephonyManager telephonyManager;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference userRef = database.getReference().child("Users");
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen=SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SessionManager sessionManager=new SessionManager(this);
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (sessionManager.isLoggedIn() && currentUser != null) {
            // User is logged in, proceed with the app's main functionality
            if (sessionManager.isAccountAdmin()){
                Intent intent = new Intent(MainActivity.this, AdminMainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else if(sessionManager.isAccountUser()){
                Intent intent = new Intent(MainActivity.this, UserMainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                // Logged in but no role? Go to login
                sessionManager.clearSession();
                redirectToLogin();
            }
        } else {
            // User is not logged in or session is stale
            sessionManager.clearSession();
            redirectToLogin();
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(MainActivity.this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}

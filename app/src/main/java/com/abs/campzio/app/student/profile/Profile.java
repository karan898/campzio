package com.abs.campzio.app.student.profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.abs.campzio.app.auth.session.SessionManager;
import com.abs.campzio.app.models.User;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;

import com.abs.campzio.app.R;
import com.abs.campzio.app.auth.signin.Login;

public class Profile extends Fragment {

    private MaterialCardView materialCardView, materialCardViewShimmer;
    private TextView userName, userEnrollment, userEmail, userPhone, userCourse, userSemester, userDot;
    private Button logoutUser;
    private MaterialCardView enrollmentID;
    private ImageView dotProfile;

    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_profile, container, false);
        
        sessionManager = new SessionManager(getContext());
        
        materialCardView = view.findViewById(R.id.materialCardView);
        materialCardViewShimmer = view.findViewById(R.id.materialCardViewShimmer);
        dotProfile = view.findViewById(R.id.dotProfile);
        userDot = view.findViewById(R.id.dotText);
        enrollmentID = view.findViewById(R.id.enrollmentID);
        userName = view.findViewById(R.id.userName);
        userEnrollment = view.findViewById(R.id.userEnrollment);
        userEmail = view.findViewById(R.id.userEmail);
        userPhone = view.findViewById(R.id.userPhone);
        userCourse = view.findViewById(R.id.userCourse);
        userSemester = view.findViewById(R.id.userSemester);
        logoutUser = view.findViewById(R.id.userLogout);

        loadProfileData();

        logoutUser.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(getContext())
                    .setIcon(R.drawable.ic_signout)
                    .setTitle("Logout")
                    .setMessage("Are you sure want to log out?")
                    .setCancelable(true)
                    .setPositiveButton("OK", (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();
                        sessionManager.clearSession();
                        Toast.makeText(getContext(), "You have logged out successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getContext(), Login.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        if (getActivity() != null) getActivity().finish();
                    })
                    .setNegativeButton("CANCEL", (dialog, which) -> dialog.cancel())
                    .show();
        });

        return view;
    }

    private void loadProfileData() {
        User user = sessionManager.getUser();
        if (user != null) {
            materialCardViewShimmer.setVisibility(View.GONE);
            materialCardView.setVisibility(View.VISIBLE);
            
            userName.setText(user.getName());
            userEmail.setText(user.getEmail());
            userPhone.setText(user.getId()); // 'id' field in User class stores the phone number

            if ("admin".equals(user.getRole())) {
                dotProfile.setVisibility(View.VISIBLE);
                userSemester.setVisibility(View.VISIBLE);
                userSemester.setText("Admin");
                userCourse.setVisibility(View.GONE);
                userDot.setVisibility(View.GONE);
                enrollmentID.setVisibility(View.GONE);
            } else {
                userCourse.setVisibility(View.VISIBLE);
                userDot.setVisibility(View.VISIBLE);
                userSemester.setVisibility(View.VISIBLE);
                enrollmentID.setVisibility(View.VISIBLE);
                userEnrollment.setText(user.getEnrollment());
                userCourse.setText(user.getCourse());
                userSemester.setText(user.getSemester());
            }
        } else {
            Toast.makeText(getContext(), "Session error. Please login again.", Toast.LENGTH_SHORT).show();
        }
    }
}

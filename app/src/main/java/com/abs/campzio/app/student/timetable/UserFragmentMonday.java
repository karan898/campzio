package com.abs.campzio.app.student.timetable;

import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.abs.campzio.app.R;
import com.abs.campzio.app.admin.timetable.Lecture;
import com.abs.campzio.app.admin.timetable.LectureAdapter;
import com.abs.campzio.app.auth.session.SessionManager;
import com.abs.campzio.app.models.User;
import com.abs.campzio.app.repository.DataRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class UserFragmentMonday extends Fragment {
    NestedScrollView lecture;
    LinearLayout lectureShimmer;
    private final static String TAG = "MondayFragment";
    View view;
    private RecyclerView mondayLecture;
    private List<UserLecture> lectureList;
    private UserLectureAdapter userLectureAdapter;

    SessionManager sessionManager;
    DataRepository repository;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser=mAuth.getCurrentUser();

    public UserFragmentMonday() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_monday_user,container, false);

        mondayLecture=view.findViewById(R.id.user_mondayRecyclerView);
        lectureShimmer=view.findViewById(R.id.lectureShimmerMonday);
        lecture=view.findViewById(R.id.lectureMonday);
        
        sessionManager = new SessionManager(getContext());
        repository = DataRepository.getInstance();
        
        fetchMondayLecture();

        return view;
    }

    private void fetchMondayLecture() {
        User user = sessionManager.getUser();
        if (user == null || currentUser == null) {
            Toast.makeText(getContext(), "Session error. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        repository.getTimetable("Monday", user.getCourse(), user.getSemester(), user.getSection(), new DataRepository.RepositoryCallback<List<UserLecture>>() {
            @Override
            public void onSuccess(List<UserLecture> result) {
                lectureList = result;
                if (lectureList.isEmpty()) {
                    Toast.makeText(getContext(), "No classes scheduled for Monday", Toast.LENGTH_SHORT).show();
                } else {
                    mondayLecture.setHasFixedSize(true);
                    mondayLecture.setLayoutManager(new LinearLayoutManager(getContext()));
                    userLectureAdapter = new UserLectureAdapter(getContext(), lectureList);
                    mondayLecture.setAdapter(userLectureAdapter);
                    lectureShimmer.setVisibility(View.GONE);
                    lecture.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(getContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}


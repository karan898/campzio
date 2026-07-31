package com.abs.campzio.app.repository;

import androidx.annotation.NonNull;
import com.abs.campzio.app.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DataRepository {
    private static DataRepository instance;
    private final DatabaseReference usersRef;
    private final DatabaseReference timetableRef;

    private DataRepository() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        usersRef = db.getReference("Users");
        timetableRef = db.getReference("Timetable");
    }

    public static synchronized DataRepository getInstance() {
        if (instance == null) {
            instance = new DataRepository();
        }
        return instance;
    }

    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    public void getUserByEmail(String email, RepositoryCallback<User> callback) {
        usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    DataSnapshot userSnapshot = snapshot.getChildren().iterator().next();
                    User user = userSnapshot.getValue(User.class);
                    if (user != null) {
                        callback.onSuccess(user);
                    } else {
                        callback.onError("User data is invalid");
                    }
                } else {
                    callback.onError("User not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public void getUserByPhone(String phone, RepositoryCallback<User> callback) {
        usersRef.orderByChild("id").equalTo(phone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    DataSnapshot userSnapshot = snapshot.getChildren().iterator().next();
                    User user = userSnapshot.getValue(User.class);
                    if (user != null) {
                        callback.onSuccess(user);
                    } else {
                        callback.onError("User data is invalid");
                    }
                } else {
                    callback.onError("User not found in records");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public void getTimetable(String day, String course, String semester, String section, RepositoryCallback<java.util.List<com.abs.campzio.app.student.timetable.UserLecture>> callback) {
        timetableRef.child(day).child(course).child(semester).child(section).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                java.util.List<com.abs.campzio.app.student.timetable.UserLecture> list = new java.util.ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    com.abs.campzio.app.student.timetable.UserLecture data = child.getValue(com.abs.campzio.app.student.timetable.UserLecture.class);
                    if (data != null) list.add(data);
                }
                callback.onSuccess(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }
}

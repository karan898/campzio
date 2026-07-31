package com.abs.campzio.app.auth.session;

import android.content.Context;
import android.content.SharedPreferences;
import com.abs.campzio.app.models.User;
import com.google.gson.Gson;

public class SessionManager {
    private static final String PREF_NAME = "campzioLoginSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_DATA = "userData";
    private static final String ACCOUNT_ROLE_ADMIN = "isAccountAdmin";
    private static final String ACCOUNT_ROLE_USER = "isAccountUser";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;
    private final Gson gson;

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
        gson = new Gson();
    }

    public void setLoggedIn(boolean isLoggedIn) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void saveUser(User user) {
        String userJson = gson.toJson(user);
        editor.putString(KEY_USER_DATA, userJson);
        
        // Keep backward compatibility for role checks if needed
        if ("admin".equals(user.getRole())) {
            setAccountRoleAdmin(true);
            setAccountRoleUser(false);
        } else {
            setAccountRoleAdmin(false);
            setAccountRoleUser(true);
        }
        editor.apply();
    }

    public User getUser() {
        String userJson = pref.getString(KEY_USER_DATA, null);
        if (userJson == null) return null;
        return gson.fromJson(userJson, User.class);
    }

    public void setAccountRoleAdmin(boolean isAccountAdmin) {
        editor.putBoolean(ACCOUNT_ROLE_ADMIN, isAccountAdmin);
        editor.apply();
    }

    public boolean isAccountAdmin() {
        return pref.getBoolean(ACCOUNT_ROLE_ADMIN, false);
    }

    public void setAccountRoleUser(boolean isAccountUser) {
        editor.putBoolean(ACCOUNT_ROLE_USER, isAccountUser);
        editor.apply();
    }

    public boolean isAccountUser() {
        return pref.getBoolean(ACCOUNT_ROLE_USER, false);
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}

package com.alexlim.touristspotfinder.util;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import android.widget.Toast;

import com.alexlim.touristspotfinder.R;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.Arrays;
import java.util.List;

public class FireAuthUiManager {
    private static final String TAG = FireAuthUiManager.class.getSimpleName();

    public static final int RC_SIGN_IN = 123;

    public static void startSignIn(Activity activity) {

        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build()
        );

        activity.startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setTheme(R.style.AppTheme)
                        .build(),
                RC_SIGN_IN);
    }

    public static void startSignOut(final FragmentActivity fragmentActivity) {
        AuthUI.getInstance()
                .signOut(fragmentActivity)
                .addOnCompleteListener(task -> Toast.makeText(fragmentActivity,
                        "You have been signed out.",
                        Toast.LENGTH_SHORT)
                        .show());
    }
}

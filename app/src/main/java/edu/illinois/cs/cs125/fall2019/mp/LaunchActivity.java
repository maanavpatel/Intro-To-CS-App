package edu.illinois.cs.cs125.fall2019.mp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;
/**
 * Represents the game launch screen, where the user signs in.
 */
public class LaunchActivity extends AppCompatActivity {
    /** Integer for sign-in request. */
    private static final int SIGNIN_REQ = 200;

    /** Main Activity. */
    private Intent main;

    /**
     * Called by the Android system when the activity is created. Performs initial setup.
     * @param savedInstanceState saved state from the last terminated instance (unused)
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        main = new Intent(this, MainActivity.class);

        Button button = findViewById(R.id.goLogin);
        System.out.println("button:" + button);

        button.setOnClickListener(v -> {
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build());
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    SIGNIN_REQ);
        });
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            System.out.println("Starting Main Activity: Already Signed in");
            startActivity(main);
            finish();
        } else {
            System.out.println("Starting sign in activity");
            // Choose authentication providers
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build());

            // Create and launch sign-in intent
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    SIGNIN_REQ);
        }


    }


    /**
     * Invoked by the Android system when a request launched by startActivityForResult completes.
     * @param requestCode the request code passed by to startActivityForResult
     * @param resultCode a value indicating how the request finished (e.g. completed or canceled)
     * @param data an Intent containing results (e.g. as a URI or in extras)
     */
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGNIN_REQ) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                // ...
                System.out.println("Starting Main Activity: Sign in complete");
                startActivity(main);
                finish();
            }
        }
    }
}

package com.example.yalatour.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.yalatour.Classes.User;
import com.example.yalatour.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;


import java.util.ArrayList;
import java.util.List;

public class LoginPage extends AppCompatActivity {
    private EditText email, password;
    private Button Login;
    private TextView gotoRegister, invalidCredentialsMessage;
    private ToggleButton ShowPass;
    private int savedCursorPosition = 0;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;

    @Override
    protected void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = fAuth.getCurrentUser();
        if (currentUser != null) {
            // User is signed in, navigate to homepage
            startActivity(new Intent(LoginPage.this, HomePage.class));
            finish(); // Optional, depending on your navigation flow
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        // Initialize UI elements
        email = findViewById(R.id.user_email);
        password = findViewById(R.id.user_password);
        Login = findViewById(R.id.Login);
        gotoRegister = findViewById(R.id.signupRedirectText);
        invalidCredentialsMessage = findViewById(R.id.invalidCredentialsMessage);
        ShowPass = findViewById(R.id.toggleButton);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //checking if the password and email are not empty
                if (checkField(email) && checkField(password)) {
                    // Checking the credentials are valid using firebase authentication
                    fAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            // Retrieve the current user
                            FirebaseUser currentUser = fAuth.getCurrentUser();
                            if (currentUser != null) {
                                String userId = currentUser.getUid();

                                // Get FCM token
                                FirebaseMessaging.getInstance().getToken()
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful() && task.getResult() != null) {
                                                String fcmToken = task.getResult();

                                                // Update user information in Firestore with the new FCM token
                                                DocumentReference userRef = fStore.collection("Users").document(userId);
                                                userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                        if (documentSnapshot.exists()) {
                                                            User user = documentSnapshot.toObject(User.class);
                                                            List<String> fcmTokens = user.getFcmToken();
                                                            if (fcmTokens == null) {
                                                                fcmTokens = new ArrayList<>();
                                                            }
                                                            if (!fcmTokens.contains(fcmToken)) {
                                                                fcmTokens.add(fcmToken);
                                                                userRef.update("fcmToken", fcmTokens)
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                // Redirect to home page
                                                                                Intent intent = new Intent(LoginPage.this, HomePage.class);
                                                                                startActivity(intent);
                                                                                finish();
                                                                            }
                                                                        })
                                                                        .addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception e) {
                                                                                Toast.makeText(LoginPage.this, "Failed to update FCM token: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        });
                                                            } else {
                                                                // Redirect to home page
                                                                Intent intent = new Intent(LoginPage.this, HomePage.class);
                                                                startActivity(intent);
                                                                finish();
                                                            }
                                                        }
                                                    }
                                                });
                                            } else {
                                                // Handle error
                                                Toast.makeText(LoginPage.this, "Failed to get FCM token", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // If the email or password are wrong the user will get the following text
                            invalidCredentialsMessage.setText("Email or Password is Invalid");
                        }
                    });
                }
            }
        });

        // Toggle button to show/hide password
        ShowPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                savedCursorPosition = password.getSelectionStart();

                if (isChecked) {
                    password.setTransformationMethod(null); // Show password
                } else {
                    password.setTransformationMethod(new PasswordTransformationMethod()); // Hide password
                }
                password.setSelection(savedCursorPosition);
            }
        });

        // Redirect to Signup page
        gotoRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginPage.this, SignupPage.class);
                startActivity(intent);
            }
        });
    }

    //a method to check if the field is empty
    public boolean checkField(EditText textField) {
        if (textField.getText().toString().isEmpty()) {
            textField.setError("Could not be empty");
            return false;
        }
        return true;
    }
}
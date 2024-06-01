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
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SignupPage extends AppCompatActivity {
    private EditText email, password, username;
    private Button Signup;
    private TextView gotoLogin, passwordRequirement;
    private ToggleButton ShowPass;
    List<String> fcmTokenList;
    private int savedCursorPosition = 0;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_page);

        // Initialize UI elements
        email = findViewById(R.id.user_email);
        username = findViewById(R.id.user_name);
        password = findViewById(R.id.user_passsword);
        Signup = findViewById(R.id.Signup);
        gotoLogin = findViewById(R.id.loginRedirectText);
        passwordRequirement = findViewById(R.id.passwordRequirement);
        ShowPass = findViewById(R.id.toggleButton);
        // Initialize Firebase components
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        fcmTokenList=new ArrayList<>();

        // Signup button click listener
        Signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if fields are not empty
                if (checkField(username) && checkField(email) && checkField(password)) {
                    // Check if password meets minimum length requirement
                    if (password.getText().toString().length() < 6) {
                        passwordRequirement.setText("Password should be at least 6 characters");
                        return;
                    } else {
                        passwordRequirement.setText("");
                    }

                    // Check if the email already exists
                    fAuth.fetchSignInMethodsForEmail(email.getText().toString())
                            .addOnSuccessListener(new OnSuccessListener<SignInMethodQueryResult>() {
                                @Override
                                public void onSuccess(SignInMethodQueryResult signInMethodQueryResult) {
                                    if (signInMethodQueryResult.getSignInMethods().size() > 0) {
                                        // Email already exists
                                        Toast.makeText(SignupPage.this, "An account already exists with this email", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // Email doesn't exist, create user account
                                        createUserAccount();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Failed to check email existence
                                    Toast.makeText(SignupPage.this, "Failed to check email existence", Toast.LENGTH_SHORT).show();
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

        // Redirect to login page
        gotoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignupPage.this, LoginPage.class);
                startActivity(intent);
            }
        });
    }

    // Method to create user account
    private void createUserAccount() {
        fAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                // User account created successfully
                FirebaseUser firebaseUser = fAuth.getCurrentUser();
                String userId = firebaseUser.getUid();

                // Get FCM token
                FirebaseMessaging.getInstance().getToken()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null) {
                                String fcmToken = task.getResult();
                                fcmTokenList.add(fcmToken);

                                // Create User object with FCM token
                                User user = new User(username.getText().toString(), email.getText().toString(), true, fcmTokenList);

                                // Store user information in Firestore
                                DocumentReference userRef = fStore.collection("Users").document(userId);
                                userRef.set(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // Account created successfully
                                                Toast.makeText(SignupPage.this, "Account Created", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(SignupPage.this, HomePage.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(SignupPage.this, "Failed to create user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                // Handle error
                                Toast.makeText(SignupPage.this, "Failed to get FCM token", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Failed to create user account
                if (e instanceof FirebaseAuthUserCollisionException) {
                    Toast.makeText(SignupPage.this, "An account already exists with this email", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SignupPage.this, "Failed to Create Account: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Method to check if the field is empty
    public boolean checkField(EditText textField) {
        boolean valid = true;
        if (textField.getText().toString().isEmpty()) {
            textField.setError("Could not be empty");
            valid = false;
        }
        return valid;
    }
}

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

import com.example.yalatour.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupPage extends AppCompatActivity {
    private  EditText email, password, username;
    private Button Signup;
    private boolean valid = true;
    private TextView gotoLogin, passwordRequirement;
    private ToggleButton ShowPass;
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

        // Signup button click listener
        Signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if fields are not empty
                checkField(username);
                checkField(email);
                // Check if password meets minimum length requirement
                if (checkField(password)) {
                    if (password.getText().toString().length() < 6) {
                        passwordRequirement.setText("Password should be at least 6 characters");
                        return;
                    } else {
                        passwordRequirement.setText("");
                    }
                } else {
                    return;
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
                FirebaseUser user = fAuth.getCurrentUser();
                Toast.makeText(SignupPage.this, "Account Created", Toast.LENGTH_SHORT).show();
                // Store user information in Firestore
                DocumentReference df = fStore.collection("Users").document(user.getUid());
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("Username", username.getText().toString());
                userInfo.put("Email", email.getText().toString());
                userInfo.put("isUser", 1);
                df.set(userInfo);
                // Redirect to home page
                Intent intent = new Intent(SignupPage.this, HomePage.class);
                startActivity(intent);
                finish();
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
        if (textField.getText().toString().isEmpty()) {
            textField.setError("Could not be empty");
            valid = false;
        } else {
            valid = true;
        }

        return valid;
    }
}

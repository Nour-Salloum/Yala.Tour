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
import android.widget.ToggleButton;

import com.example.yalatour.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginPage extends AppCompatActivity {
    private EditText email, password;
    private Button Login;
    private TextView gotoRegister, invalidCredentialsMessage;
    private boolean valid = true;
    private ToggleButton ShowPass;
    private int savedCursorPosition = 0;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        // Initialize UI elements
        email = findViewById(R.id.user_email);
        password = findViewById(R.id.user_passsword);
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
                checkField(email);
                checkField(password);
                if (valid) {
                    // Checking the credentials are valid using firebase authentication
                    fAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            // Redirect to home page
                            Intent intent = new Intent(LoginPage.this, HomePage.class);
                            startActivity(intent);
                            finish();
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
            valid = false;
        } else {
            valid = true;
        }

        return valid;
    }
}

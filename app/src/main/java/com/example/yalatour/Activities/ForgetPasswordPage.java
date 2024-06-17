package com.example.yalatour.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.app.AlertDialog;
import android.content.DialogInterface;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.yalatour.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class ForgetPasswordPage extends AppCompatActivity {

    EditText Email;
    Button ResetPasswordButton;
    FirebaseAuth auth;
    TextView CheckEmail;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forget_password_page);

        // Initialize UI elements
        Email = findViewById(R.id.forget_user_email);
        ResetPasswordButton = findViewById(R.id.ResetPassword);
        CheckEmail = findViewById(R.id.CheckEmail); // Make sure this ID is correct in your XML layout
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ResetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = Email.getText().toString().trim();
                if (TextUtils.isEmpty(userEmail) || !Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
                    CheckEmail.setText("Please Enter a Valid Email");
                    return;
                } else {
                    checkEmailExists(userEmail);
                }
            }
        });
    }

    private void checkEmailExists(final String email) {
        db.collection("Users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            // Email exists, proceed with password reset
                            sendPasswordResetEmail(email);
                        } else {
                            // Email does not exist, show dialog
                            showEmailNotFoundDialog();
                        }
                    }
                });
    }

    private void sendPasswordResetEmail(String email) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    CheckEmail.setText("A reset link has been sent to your email.");
                } else {
                    CheckEmail.setText("Failed to send reset email. Please try again.");
                }
            }
        });
    }

    private void showEmailNotFoundDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("We did not find your account for this email.")
                .setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked Try Again button
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Create New Account", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(ForgetPasswordPage.this, SignupPage.class);
                        startActivity(intent);
                        finish();
                    }
                });
        // Create the AlertDialog object and return it
        builder.create().show();
    }
}

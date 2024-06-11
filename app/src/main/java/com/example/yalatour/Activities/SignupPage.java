package com.example.yalatour.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignupPage extends AppCompatActivity {
    private EditText email, password, username;
    private CircleImageView ProfileImage;
    private Button Signup;
    private TextView gotoLogin, passwordRequirement;
    private ToggleButton ShowPass;
    private String currentUserId;
    private Uri profileImageUri; // New field for storing the image URI
    List<String> fcmTokenList;
    private int savedCursorPosition = 0;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    final static int Gallery_Pick = 1;
    private StorageReference UserProfileImageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_page);

        // Initialize UI elements
        email = findViewById(R.id.user_email);
        username = findViewById(R.id.user_name);
        password = findViewById(R.id.user_password);
        ProfileImage = findViewById(R.id.ProfileImage);

        Signup = findViewById(R.id.Signup);
        gotoLogin = findViewById(R.id.loginRedirectText);
        passwordRequirement = findViewById(R.id.passwordRequirement);
        ShowPass = findViewById(R.id.toggleButton);

        // Initialize Firebase components
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        fcmTokenList = new ArrayList<>();
        UserProfileImageReference = FirebaseStorage.getInstance().getReference();

        // Signup button click listener
        Signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkField(username) && checkField(email) && checkField(password)) {
                    if (password.getText().toString().length() < 6) {
                        passwordRequirement.setText("Password should be at least 6 characters");
                        return;
                    } else {
                        passwordRequirement.setText("");
                    }

                    fAuth.fetchSignInMethodsForEmail(email.getText().toString())
                            .addOnSuccessListener(new OnSuccessListener<SignInMethodQueryResult>() {
                                @Override
                                public void onSuccess(SignInMethodQueryResult signInMethodQueryResult) {
                                    if (signInMethodQueryResult.getSignInMethods().size() > 0) {
                                        Toast.makeText(SignupPage.this, "An account already exists with this email", Toast.LENGTH_SHORT).show();
                                    } else {
                                        createUserAccount();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(SignupPage.this, "Failed to check email existence", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });

        ShowPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                savedCursorPosition = password.getSelectionStart();

                if (isChecked) {
                    password.setTransformationMethod(null);
                } else {
                    password.setTransformationMethod(new PasswordTransformationMethod());
                }
                password.setSelection(savedCursorPosition);
            }
        });

        gotoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignupPage.this, LoginPage.class);
                startActivity(intent);
            }
        });
    }

    private void createUserAccount() {
        fAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                FirebaseUser firebaseUser = fAuth.getCurrentUser();
                currentUserId = firebaseUser.getUid();

                FirebaseMessaging.getInstance().getToken()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null) {
                                String fcmToken = task.getResult();
                                fcmTokenList.add(fcmToken);

                                if (profileImageUri != null) {
                                    uploadProfileImage();
                                } else {
                                    saveUserToFirestore(null);
                                }
                            } else {
                                Toast.makeText(SignupPage.this, "Failed to get FCM token", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof FirebaseAuthUserCollisionException) {
                    Toast.makeText(SignupPage.this, "An account already exists with this email", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SignupPage.this, "Failed to Create Account: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void uploadProfileImage() {
        StorageReference filepath = UserProfileImageReference.child("profile_images").child(currentUserId + ".jpg");
        filepath.putFile(profileImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            saveUserToFirestore(uri.toString());
                        }
                    });
                } else {
                    String message = task.getException().getMessage();
                    Toast.makeText(SignupPage.this, "Error occurred: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveUserToFirestore(String profileImageUrl) {
        User user = new User(username.getText().toString(), email.getText().toString(), true, fcmTokenList, profileImageUrl);

        DocumentReference userRef = fStore.collection("Users").document(currentUserId);
        userRef.set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null) {
            profileImageUri = data.getData();
            ProfileImage.setImageURI(profileImageUri);
        }
    }

    public boolean checkField(EditText textField) {
        boolean valid = true;
        if (textField.getText().toString().isEmpty()) {
            textField.setError("Could not be empty");
            valid = false;
        }
        return valid;
    }
}
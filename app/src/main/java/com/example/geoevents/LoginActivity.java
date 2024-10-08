package com.example.geoevents;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.geoevents.database.FirebaseDatabaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Struct;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText usernameOrEmailField, passwordField;
    private TextView registerLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        usernameOrEmailField = findViewById(R.id.usernameOrEmailField);
        passwordField = findViewById(R.id.passwordField);

        registerLink = findViewById(R.id.registerLink);
        registerLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

    }


    public void loginUser(View view) {
        String usernameOrEmail = ((EditText) findViewById(R.id.usernameOrEmailField)).getText().toString();
        String password = passwordField.getText().toString();

        if (usernameOrEmail.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        if (usernameOrEmail.contains("@")) {
            signWithEmail(usernameOrEmail, password);
        } else {
            DatabaseReference database = FirebaseDatabaseHelper.getInstance().getReference("user");
            database.orderByChild("username").equalTo(usernameOrEmail)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                    String email = userSnapshot.child("email").getValue(String.class);
                                    signWithEmail(email, password);
                                    break;
                                }
                            } else {
                                Toast.makeText(LoginActivity.this, "Пользователь не найден", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(LoginActivity.this, "Ошибка при поиске пользователя", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void signWithEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Вход выполнен", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Ошибка входа", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}





//    public void registerUser(View view) {
//        String email = emailField.getText().toString();
//        String password = passwordField.getText().toString();
//        String confirmPassword = confirmPasswordField.getText().toString();
//        String username = usernameField.getText().toString();
//
//        if (!password.equals(confirmPassword)) {
//            Toast.makeText(LoginActivity.this, "Пароли не совпадают", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
//            Toast.makeText(LoginActivity.this, "Заполните все поля", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        mAuth.createUserWithEmailAndPassword(email, password)
//                .addOnCompleteListener(this, task -> {
//                    if (task.isSuccessful()) {
//                        FirebaseUser user = mAuth.getCurrentUser();
//
//                        if(user != null) {
//                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
//                                    .setDisplayName(username).build();
//
//                            user.updateProfile(profileUpdates)
//                                    .addOnCompleteListener(updateTask -> {
//                                        if (updateTask.isSuccessful()) {
//                                            Toast.makeText(LoginActivity.this, "Регистрация успешна", Toast.LENGTH_SHORT).show();
//                                            finish();
//                                        }
//                                    });
//                        }
//                    } else {
//                        Toast.makeText(LoginActivity.this, "Ошибка при регистрации", Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }
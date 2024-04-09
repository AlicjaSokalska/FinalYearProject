package com.example.testsample;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailEditText, passwordEditText;
    TextView textView;
    ProgressBar progressBar;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        textView = findViewById(R.id.registerNow);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Register.class);
                startActivity(intent);
                finish();
            }
        });

        Button loginButton = findViewById(R.id.btn_login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });
    }

    private void loginUser() {
        progressBar.setVisibility(View.VISIBLE);
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();


        if (TextUtils.isEmpty(email)) {
            Toast.makeText(Login.this, "Enter email", Toast.LENGTH_SHORT).show();
            return;
        }

        if
        (TextUtils.isEmpty(password)) {
            Toast.makeText(Login.this, "Enter password", Toast.LENGTH_SHORT).show();
            return;
        }


        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Login successful
                            Toast.makeText(Login.this, "Login successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Login.this, SelectMode.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // Login failed
                            String errorMessage = task.getException().getMessage();
                            Toast.makeText(Login.this, "Login failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}

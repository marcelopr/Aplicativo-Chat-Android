package com.celosoft.fastchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout mLoginEmail, mLoginPassword;
    private Button mLoginBtn;
    private Toolbar mToolbar;
    private ProgressDialog mLoginProgress;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        mLoginEmail = (TextInputLayout)findViewById(R.id.login_email);
        mLoginPassword = (TextInputLayout)findViewById(R.id.login_password);
        mLoginBtn = (Button)findViewById(R.id.login_btn);

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mToolbar = (Toolbar)findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Entrar");

        mLoginProgress = new ProgressDialog(this);

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mLoginEmail.getEditText().getText().toString();
                String password = mLoginPassword.getEditText().getText().toString();

                if( !TextUtils.isEmpty(email) || !TextUtils.isEmpty(password) ){

                    mLoginProgress.setMessage("Entrando...");
                    mLoginProgress.setCanceledOnTouchOutside(false);
                    mLoginProgress.show();
                    loginUser(email, password);

                }else{
                    Toast.makeText(LoginActivity.this, "Preencha os dois campos!", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void loginUser(String email, String password) {

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){
                    mLoginProgress.dismiss();

                    String deviceToken = FirebaseInstanceId.getInstance().getToken();
                    String currentUserId = mAuth.getCurrentUser().getUid();
                    mUserDatabase.child(currentUserId).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |  Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            finish();

                        }
                    });

                }else{

                    String erroExcecao = "";

                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e) {
                        erroExcecao = "Email inválido!";
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        erroExcecao = "Senha incorreta!";
                    } catch (Exception e) {
                        erroExcecao = "Erro ao realizar login!";
                        e.printStackTrace();
                    }

                    mLoginProgress.hide();
                    Toast.makeText(LoginActivity.this, erroExcecao, Toast.LENGTH_LONG).show();
                }

            }
        });
    }
}

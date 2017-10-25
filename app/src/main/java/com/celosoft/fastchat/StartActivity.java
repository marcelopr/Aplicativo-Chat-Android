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
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class StartActivity extends AppCompatActivity {

    private Button mRegBtn;
    private TextInputLayout mLoginEmail, mLoginPassword;
    private ImageButton mLoginBtn;
    private Toolbar mToolbar;
    private ProgressDialog mLoginProgress;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mRegBtn = (Button)findViewById(R.id.start_reg_btn);
        mAuth = FirebaseAuth.getInstance();

        mLoginEmail = (TextInputLayout)findViewById(R.id.login_email);
        mLoginPassword = (TextInputLayout)findViewById(R.id.login_password);
        mLoginBtn = (ImageButton)findViewById(R.id.login_btn);

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
                    Toast.makeText(StartActivity.this, "Preencha os dois campos!", Toast.LENGTH_LONG).show();
                }
            }
        });

        mRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent reg_intent = new Intent(StartActivity.this, RegisterActivity.class);
                startActivity(reg_intent);
            }
        });

    }

    private void loginUser(String email, String password) {

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){
                    mLoginProgress.dismiss();
                    Intent mainIntent = new Intent(StartActivity.this, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |  Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                    finish();
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
                    Toast.makeText(StartActivity.this, erroExcecao, Toast.LENGTH_LONG).show();
                }

            }
        });
    }
}

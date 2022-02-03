package com.example.evaluacion_final;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import android.os.Bundle;

public class LoginadminActivity extends AppCompatActivity {

    private EditText numero,codigo;
    private Button enviarnumero,enviarcodigo;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String VerificacionID;
    private PhoneAuthProvider.ForceResendingToken resendingToken;
    private FirebaseAuth auth;
    private ProgressDialog dialog;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginadmin);

        numero=(EditText)findViewById(R.id.numeroadmin);
        codigo=(EditText)findViewById(R.id.codigoadmin);
        enviarnumero=(Button)findViewById(R.id.enviarnumeroadmin);
        enviarcodigo=(Button)findViewById(R.id.enviarcodigoadmin);

        auth= FirebaseAuth.getInstance();
        dialog = new ProgressDialog(this);

        enviarnumero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNumber=numero.getText().toString();
                if(TextUtils.isEmpty(phoneNumber)){
                    Toast.makeText(LoginadminActivity.this, "Ingresa tu numero primero....", Toast.LENGTH_SHORT).show();
                }else{
                    dialog.setTitle("Validando numero");
                    dialog.setMessage("Por favor espere mientras validamos su numero");
                    dialog.show();
                    dialog.setCanceledOnTouchOutside(true);

                    PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                            .setPhoneNumber("+51"+phoneNumber)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(LoginadminActivity.this)
                            .setCallbacks(callbacks)
                            .build();
                    PhoneAuthProvider.verifyPhoneNumber(options);
                }
            }
        });
        enviarcodigo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numero.setVisibility(View.GONE);
                enviarnumero.setVisibility(View.GONE);
                String VerificacionCode = codigo.getText().toString();
                if(TextUtils.isEmpty(VerificacionCode)){
                    Toast.makeText(LoginadminActivity.this, "Ingresa el codigo resibido", Toast.LENGTH_SHORT).show();
                }else{
                    dialog.setTitle("Verificando");
                    dialog.setMessage("Espere por favor...");
                    dialog.show();
                    dialog.setCanceledOnTouchOutside(true);
                    PhoneAuthCredential credencial = PhoneAuthProvider.getCredential(VerificacionID, VerificacionCode);
                    IngresadoConExito(credencial);
                }
            }
        });
        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                IngresadoConExito(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                dialog.dismiss();
                Toast.makeText(LoginadminActivity.this, "Fallo el inicio Causas: \n1. Numero Invalido\n2. Sin Conexión a Internet", Toast.LENGTH_SHORT).show();
                numero.setVisibility(View.VISIBLE);
                enviarnumero.setVisibility(View.VISIBLE);
                codigo.setVisibility(View.GONE);
                enviarcodigo.setVisibility(View.GONE);
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                VerificacionID = s;
                resendingToken = token;
                dialog.dismiss();
                Toast.makeText(LoginadminActivity.this, "Codigo Enviado Satisfactoriamente", Toast.LENGTH_SHORT).show();
                numero.setVisibility(View.GONE);
                enviarnumero.setVisibility(View.GONE);
                codigo.setVisibility(View.VISIBLE);
                enviarcodigo.setVisibility(View.VISIBLE);
            }
        };
    }

    private void IngresadoConExito(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    dialog.dismiss();
                    Toast.makeText(LoginadminActivity.this, "Ingresado Con Exito", Toast.LENGTH_SHORT).show();
                    EnviaralaPrincipal();
                }else{
                    String err = task.getException().toString();
                    Toast.makeText(LoginadminActivity.this, "Error: "+err, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if(firebaseUser != null){
            EnviaralaPrincipal();
        }
    }

    private void EnviaralaPrincipal() {
        Intent intent = new Intent(LoginadminActivity.this, PrincipalActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("papel","administrador");
        intent.putExtra("phone",phoneNumber);
        startActivity(intent);
        finish();
    }
}
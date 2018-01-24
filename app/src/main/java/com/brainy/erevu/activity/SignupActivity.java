package com.brainy.erevu.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.brainy.erevu.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity {

    private EditText inputEmail, inputUsername, inputPassword,inputRePassword, inputName;
    private Button btnSignIn, btnSignUp, btnResetPassword;
    private DatabaseReference mDatabaseUsers;
    private ProgressBar progressBar;
    private Menu menu;
    private FirebaseAuth auth;
    private ProgressDialog mprogress;
    private StorageReference mStorage;
    private ImageView backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_activitry);

        Window window = SignupActivity.this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor( SignupActivity.this,R.color.colorPrimaryDark));

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        auth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        mprogress = new ProgressDialog(this);
        btnSignUp = (Button) findViewById(R.id.btn_signup);
        btnSignIn = (Button) findViewById(R.id.btn_login);
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initSave();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cardonClick = new Intent(SignupActivity.this, SigninActivity.class);
                cardonClick.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(cardonClick);
            }
        });
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        inputRePassword = (EditText) findViewById(R.id.re_password);
        inputUsername = (EditText) findViewById(R.id.username);
        inputName = (EditText) findViewById(R.id.name);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnResetPassword = (Button) findViewById(R.id.btn_reset_password);
        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cardonClick = new Intent(SignupActivity.this, ForgotPassActivity.class);
                cardonClick.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(cardonClick);
            }
        });

        backBtn = (ImageView) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignupActivity.this.finish();
            }
        });

    }

    private void initSave() {


        final String email = inputEmail.getText().toString().trim();
        final String password = inputPassword.getText().toString().trim();
        final String re_password = inputRePassword.getText().toString().trim();
        final String name = inputName.getText().toString().trim();
        final String username = inputUsername.getText().toString().trim();

        Date date = new Date();
        final String stringDate = DateFormat.getDateInstance().format(date);

        //final String user_id = auth.getCurrentUser().getUid();

        // if you don't care why it fails and only want to know if valid or not

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);

            if (TextUtils.isEmpty(name)) {
                inputName.setError("Enter name!");
                //Toast.makeText(getApplicationContext(), "Enter name!", Toast.LENGTH_SHORT).show();

            }

                else if (name.length() < 3 || name.length() >15 ){
                    System.out.println("Name too short or too long");
                   inputName.setError("Name too short or too long");
                }
        else if (TextUtils.isEmpty(username)) {
            inputUsername.setError("Enter username!");
            //Toast.makeText(getApplicationContext(), "Enter username!", Toast.LENGTH_SHORT).show();

        }

        else if (username.length() < 3 || username.length() >15 ){
            System.out.println("Username too short or too long");
            inputUsername.setError("Username too short or too long");
        }
            else if (TextUtils.isEmpty(email)) {
                inputEmail.setError("Enter email address!");
                //Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();


            }
            else if (!matcher.matches()) {
                     inputEmail.setError("Invalid email Address!");
                    // Toast.makeText(getApplicationContext(), "Invalid email Address!", Toast.LENGTH_SHORT).show();
                 }

            else if (TextUtils.isEmpty(password)) {
                inputPassword.setError("Enter password!");
                //Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();

            }

            else if (password.length() < 6 || password.length() > 15 ){
                System.out.println("pass too short or too long");
                inputPassword.setError("Password too short or too long!");
                //Toast.makeText(getApplicationContext(), "Password too short or too long!", Toast.LENGTH_SHORT).show();
            }

            else if (!password.matches(".*\\d.*")){
                System.out.println("no digits found");
                inputPassword.setError("No digits found in password! e.g [0 - 9] ");
                //Toast.makeText(getApplicationContext(), "No digits found in password! e.g [0 - 9] ", Toast.LENGTH_SHORT).show();
            }

            else if (!password.matches(".*[a-z].*")) {
                System.out.println("no lowercase letters found");
                inputPassword.setError("No lowercase letters found in password! e.g [a - z] ");
                //Toast.makeText(getApplicationContext(), "No lowercase letters found in password! e.g [a - z] ", Toast.LENGTH_SHORT).show();
            }
            else if (!password.matches(".*[!@#$%^&*+=?-].*")) {
                System.out.println("no special chars found");
                inputPassword.setError("No special chars found in password! e.g [! @ # $ % ^ & * + = ? -] ");
                //Toast.makeText(getApplicationContext(), "No special chars found in password! e.g [! @ # $ % ^ & * + = ? -] ", Toast.LENGTH_SHORT).show();

            }
            else if (TextUtils.isEmpty(re_password)) {
                inputRePassword.setError("Confirm password!");
                //Toast.makeText(getApplicationContext(), "Confirm password!", Toast.LENGTH_SHORT).show();

            } else if (!password.equals(re_password)) {
                inputRePassword.setError("Your password and confirmation password do not match!");
                //Toast.makeText(getApplicationContext(), "Your password and confirmation password do not match!", Toast.LENGTH_SHORT).show();

            }
            else {

                progressBar.setVisibility(View.VISIBLE);
                //create user
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Toast.makeText(SignupActivity.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.GONE);
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Toast.makeText(SignupActivity.this, "Authentication failed." + task.getException(),
                                            Toast.LENGTH_LONG).show();
                                } else {

                                    mprogress.setMessage("Creating account, please wait...");
                                    mprogress.show();

                                    final DatabaseReference newPost = mDatabaseUsers;
                                    String deviceToken = FirebaseInstanceId.getInstance().getToken();

                                    newPost.child(auth.getCurrentUser().getUid()).child("date").setValue(stringDate);
                                    newPost.child(auth.getCurrentUser().getUid()).child("uid").setValue(auth.getCurrentUser().getUid());
                                    newPost.child(auth.getCurrentUser().getUid()).child("name").setValue(name);
                                    newPost.child(auth.getCurrentUser().getUid()).child("username").setValue("@"+username);
                                    newPost.child(auth.getCurrentUser().getUid()).child("user_image").setValue("");
                                    newPost.child(auth.getCurrentUser().getUid()).child("joined_date").setValue(stringDate);
                                    newPost.child(auth.getCurrentUser().getUid()).child("uid").setValue(auth.getCurrentUser().getUid());
                                    newPost.child(auth.getCurrentUser().getUid()).child("user_email").setValue(email);
                                    newPost.child(auth.getCurrentUser().getUid()).child("user_password").setValue(password);
                                    newPost.child(auth.getCurrentUser().getUid()).child("sign_in_type").setValue("manual_sign_In");
                                    newPost.child(auth.getCurrentUser().getUid()).child("reputation").setValue("Beginner");
                                    newPost.child(auth.getCurrentUser().getUid()).child("points_earned").setValue(10);
                                    newPost.child(auth.getCurrentUser().getUid()).child("device_token").setValue(deviceToken);

                                    Toast.makeText(SignupActivity.this, "Welcome " + name + " your Account was created successfully!",
                                            Toast.LENGTH_LONG).show();

                                    Intent cardonClick = new Intent(SignupActivity.this, MainActivity.class);
                                    cardonClick.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(cardonClick);


                                }
                            }
                        });

            }


           /* if (containsPartOf(password,name)) {
                System.out.println("pass contains substring of username");
                Toast.makeText(getApplicationContext(), "No special chars found in password! e.g [!@#$%^&*+=?-] ", Toast.LENGTH_SHORT).show();
            }
            if (containsPartOf(password,email)) {
                System.out.println("pass contains substring of email");
            }*/

        }

        private static boolean containsPartOf(String pass, String username) {
            int requiredMin = 3;
            for(int i=0;(i+requiredMin)<username.length();i++){
                if(pass.contains(username.substring(i,i+requiredMin))){
                    return true;
                }
            }
            return false;
        }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (item.getItemId()) {

            case android.R.id.home:
                this.finish();
                return true;
            default:

        }

        return super.onOptionsItemSelected(item);
    }


}

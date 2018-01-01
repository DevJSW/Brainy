package com.brainy.erevu.activity;

import android.content.Intent;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.brainy.erevu.R;
import com.google.firebase.auth.FirebaseAuth;

public class AdminpassActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private LinearLayout linSignout, linPostAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adminpass);

        auth = FirebaseAuth.getInstance();
        Window window = AdminpassActivity.this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor( AdminpassActivity.this,R.color.colorPrimaryDark));

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        linPostAd = (LinearLayout) findViewById(R.id.liny_postad);
        linPostAd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cardonClick = new Intent(AdminpassActivity.this, AdminPostAdActivity.class);
                cardonClick.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(cardonClick);
            }
        });

        linSignout = (LinearLayout) findViewById(R.id.liny_signout);
        linSignout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
                /*finish();
                Toast.makeText(SettingsActivity.this, "You have successfully Logged out!",Toast.LENGTH_LONG).show();*/
            }
        });

        linPostAd = (LinearLayout) findViewById(R.id.liny_postad);

    }

    //sign out method
    public void signOut() {

        auth.signOut();
        this.recreate();
        Toast.makeText(AdminpassActivity.this, "You have successfully Logged out!",Toast.LENGTH_LONG).show();

        Intent cardonClick = new Intent(AdminpassActivity.this, MainActivity.class);
        cardonClick.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(cardonClick);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (item.getItemId()) {

            case android.R.id.home:
                this.finish();
                return true;
            default:
                if (id == R.id.action_logout) {

                    auth.signOut();
                    this.recreate();
                    Toast.makeText(AdminpassActivity.this, "You have successfully Logged out!",Toast.LENGTH_LONG).show();
                }
        }
        return super.onOptionsItemSelected(item);
    }
}

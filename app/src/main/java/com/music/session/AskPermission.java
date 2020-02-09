package com.music.session;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class AskPermission extends AppCompatActivity {

    String TAG = "AskPermission";
    boolean isFirst = true;
    private final AtomicLong counter = new AtomicLong();
    private static final int READ_STORAGE_PERMISSION_CODE = 101;
    private static final int WRITE_STORAGE_PERMISSION_CODE = 101;
    private TextView permissionText;
    private Button startMainActivityButton;
        String[] permissions= new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE};
        public static final int MULTIPLE_PERMISSIONS = 10; // code you want.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        if (checkPermissions()) {
            Log.v(TAG, "imri permission grant");
            startMainActivity();
        }
        //  permissions  granted.
    }

    private  boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p:permissions) {
            result = ContextCompat.checkSelfPermission(this,p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),MULTIPLE_PERMISSIONS );
            return false;
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissionsList[], int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS:{
                if (grantResults.length > 0) {
                    String permissionsDenied = "";
                    for (String per : permissionsList) {
                        if(grantResults[0] == PackageManager.PERMISSION_DENIED){
                            permissionsDenied += "\n" + per;

                        }

                    }
                    // Show permissionsDenied
                    //updateViews();
                }
                return;
            }
        }
    }

    void checkPermissions2() {
        checkPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                READ_STORAGE_PERMISSION_CODE);

    }

    public void startMainActivity() {
        Intent i = new Intent(AskPermission.this,
                MainActivity.class);
        startActivity(i);
    }

    public void checkPermission(String permission, int requestCode)
    {
        Log.i(TAG, "Permission requested " + permission);
        if (ContextCompat.checkSelfPermission(AskPermission.this, permission)
                == PackageManager.PERMISSION_DENIED) {

            setContentView(R.layout.loading_first_screen);
            permissionText = findViewById(R.id.permission_text);
            startMainActivityButton = findViewById(R.id.start_main_activity_button);
            isFirst = false;

            // Requesting the permission
            ActivityCompat.requestPermissions(AskPermission.this,
                    new String[]{permission},
                    requestCode);
        }
        else {
            startMainActivity();
        }
    }

    //@Override
    public void onRequestPermissionsResult2(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);
        if (requestCode == WRITE_STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(AskPermission.this,
                        "Write Storage Permission Granted",
                        Toast.LENGTH_SHORT)
                        .show();
                permissionText.setVisibility(View.INVISIBLE);
                startMainActivityButton.setVisibility(View.INVISIBLE);
                startMainActivity();
            }
            else {
                Toast.makeText(AskPermission.this,
                        "Write Storage Permission Denied",
                        Toast.LENGTH_SHORT)
                        .show();
                permissionText.setText(R.string.no_permission_text);
                startMainActivityButton.setText(R.string.re_request);
                startMainActivityButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checkPermissions();
                    }
                });

            }
        }
    }
}

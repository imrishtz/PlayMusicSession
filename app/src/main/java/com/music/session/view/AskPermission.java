package com.music.session.view;

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

import com.music.session.R;

import java.util.Objects;

public class AskPermission extends AppCompatActivity {

    String TAG = "AskPermission";
    boolean isFirst = true;
    private static final int READ_STORAGE_PERMISSION_CODE = 101;
    private TextView permissionText;
    private Button reRequestPermissionsButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().hide();

        checkPermissions();
    }

    void checkPermissions() {
        checkPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                READ_STORAGE_PERMISSION_CODE);

    }

    public void startMainActivity() {
        Intent i = new Intent(AskPermission.this,
                MainActivity.class);
        startActivity(i);
    }

    public void checkPermission(String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(AskPermission.this, permission)
                == PackageManager.PERMISSION_DENIED) {
            setContentView(R.layout.loading_first_screen);
            permissionText = findViewById(R.id.permission_text);
            reRequestPermissionsButton = findViewById(R.id.re_request_permissions);
            isFirst = false;

            // Requesting the permission
            ActivityCompat.requestPermissions(AskPermission.this,
                    new String[]{permission},
                    requestCode);
        } else {
            startMainActivity();
        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);
        if (requestCode == READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionText.setVisibility(View.INVISIBLE);
                reRequestPermissionsButton.setVisibility(View.INVISIBLE);
                startMainActivity();
            }
            else {
                Toast.makeText(AskPermission.this,
                        "Read Storage Permission Denied",
                        Toast.LENGTH_SHORT)
                        .show();
                permissionText.setText(R.string.no_permission_text);
                reRequestPermissionsButton.setText(R.string.re_request);
                reRequestPermissionsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checkPermissions();
                    }
                });

            }
        } else {
            Log.i(TAG, "onRequestPermissionsResult: requestCode" + requestCode);
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}

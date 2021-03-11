package ir.shahabazimi.instagrampicker.gallery;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

import ir.shahabazimi.instagrampicker.classes.BackgroundActivity;
import ir.shahabazimi.instagrampicker.classes.MySharedPreference;
import ir.shahabazimi.instagrampicker.R;

public class SelectActivity extends AppCompatActivity {

    private final int CAMERA_PERMISSION_REQ = 236;
    private final int STORAGE_PERMISSION_REQ = 326;
    private ArrayList<String> selectedUrls=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);
        Toolbar toolbar = findViewById(R.id.select_toolbar);
        setSupportActionBar(toolbar);
        BackgroundActivity.getInstance().setActivity(this);
        BottomNavigationView bnv = findViewById(R.id.select_bnv);
        try {
            selectedUrls.addAll(getIntent().getStringArrayListExtra("SELECTED_IMG"));
        }catch (Exception e){}
        bnv.setOnNavigationItemSelectedListener(mi -> {

            int itemId = mi.getItemId();
            if (itemId == R.id.bnv_gallery) {
                requestStorage();
            } else if (itemId == R.id.bnv_camera) {
                requestCamera();
            }


            return true;
        });
        bnv.setOnNavigationItemReselectedListener(mi -> {
        });

        requestStorage();


        bnv.setOnNavigationItemReselectedListener(mi -> {
            if (mi.getItemId() == R.id.bnv_camera && checkPermission() != PackageManager.PERMISSION_GRANTED)
                requestCamera();
            else if (mi.getItemId() == R.id.bnv_gallery && checkStoragePermission() != PackageManager.PERMISSION_GRANTED)
                requestStorage();
        });
    }


    private void openCamera() {
        getSupportFragmentManager().beginTransaction().replace(R.id.select_container, new CameraFragment())
                .commit();

    }

    private void openGallery() {

        getSupportFragmentManager().beginTransaction().replace(R.id.select_container,GalleryFragment.getInstance(selectedUrls))
                .commit();
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private int checkPermission() {
        return ActivityCompat.checkSelfPermission(SelectActivity.this, Manifest.permission.CAMERA);
    }

    private int checkStoragePermission() {
        return ActivityCompat.checkSelfPermission(SelectActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(SelectActivity.this,
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_REQ);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(SelectActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                STORAGE_PERMISSION_REQ);
    }

    private void showExplanation() {
        AlertDialog.Builder builder;
        AlertDialog alertDialog;
        builder = new AlertDialog.Builder(SelectActivity.this);
        builder.setTitle(getString(R.string.camera_permission_title));
        builder.setMessage(getString(R.string.camera_permission_message));
        builder.setPositiveButton(getString(R.string.camera_permission_positive), (DialogInterface dialog, int which) ->
                requestPermission()
        );
        builder.setNegativeButton(getString(R.string.camera_permission_negative), (DialogInterface dialog, int which) ->
                dialog.dismiss()
        );

        alertDialog = builder.create();
        alertDialog.show();
    }

    private void showStorageExplanation() {
        AlertDialog.Builder builder;
        AlertDialog alertDialog;
        builder = new AlertDialog.Builder(SelectActivity.this);
        builder.setTitle(getString(R.string.storage_permission_title));
        builder.setMessage(getString(R.string.storage_permission_message));
        builder.setPositiveButton(getString(R.string.storage_permission_positive), (DialogInterface dialog, int which) ->
                requestStoragePermission()
        );
        builder.setNegativeButton(getString(R.string.storage_permission_negative), (DialogInterface dialog, int which) ->
                dialog.dismiss()
        );

        alertDialog = builder.create();
        alertDialog.show();
    }

    private void requestCamera() {
        if (checkPermission() != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(SelectActivity.this,
                    Manifest.permission.CAMERA)) {
                showExplanation();
            } else if (!MySharedPreference.getInstance(SelectActivity.this).getCameraPermission()) {
                requestPermission();
                MySharedPreference.getInstance(SelectActivity.this).setCameraPermission();
            } else {
                showToast(getString(R.string.camera_permission_deny));
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        } else {
            openCamera();
        }

    }

    private void requestStorage() {
        if (checkStoragePermission() != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(SelectActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showStorageExplanation();
            } else if (!MySharedPreference.getInstance(SelectActivity.this).getStoragePermission()) {
                requestStoragePermission();
                MySharedPreference.getInstance(SelectActivity.this).setStoragePermission();
            } else {
                showToast(getString(R.string.storage_permission_deny));
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        } else {
            openGallery();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQ) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            }
        } else if (requestCode == STORAGE_PERMISSION_REQ) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                onBackPressed();
            }
        }
    }

}

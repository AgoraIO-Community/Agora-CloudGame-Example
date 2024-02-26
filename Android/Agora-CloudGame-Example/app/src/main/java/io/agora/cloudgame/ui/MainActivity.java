package io.agora.cloudgame.ui;

import android.Manifest;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.github.dfqin.grantor.PermissionListener;
import com.github.dfqin.grantor.PermissionsUtil;

import io.agora.cloudgame.example.BuildConfig;
import io.agora.cloudgame.example.R;
import me.add1.iris.PageDelegate;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        setContentView(R.layout.activity_main);

        FragmentTransaction transaction;
        transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content,
                PageDelegate.DelegateFragment.newInstance(new MainTabsDelegate()),
                PageDelegate.TAG_MAIN);
        transaction.addToBackStack(null);
        transaction.commitAllowingStateLoss();

        TextView versionText = findViewById(R.id.version);
        versionText.setText(String.format("%s", BuildConfig.VERSION_NAME));
    }

    @Override
    protected void onResume() {
        super.onResume();
        processPermission();
    }

    private void processPermission() {
        String[] perms = {Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA};
        if (PermissionsUtil.hasPermission(this.getApplicationContext(),
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA)) {

        } else {
            PermissionsUtil.requestPermission(this, new PermissionListener() {
                @Override
                public void permissionGranted(@NonNull String[] permissions) {
                }

                @Override
                public void permissionDenied(@NonNull String[] permissions) {

                }
            }, perms);
        }
    }
}
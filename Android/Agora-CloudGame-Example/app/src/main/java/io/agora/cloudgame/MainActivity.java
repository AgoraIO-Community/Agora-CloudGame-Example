package io.agora.cloudgame;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

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
        versionText.setText(String.format("v%s", BuildConfig.VERSION_NAME));
    }
}
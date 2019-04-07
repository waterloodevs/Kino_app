package com.earnwithkino.kino;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView nav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nav = findViewById(R.id.navigationView);
        nav.setOnNavigationItemSelectedListener(navListener);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    Fragment selectedFragment = null;

                    switch (menuItem.getItemId()){
                        case R.id.navigation_store:
                            selectedFragment = new StoreActivity();
                            break;
                        case R.id.navigation_wallet:
                            selectedFragment = new WalletActivity();
                            break;
                        case R.id.navigation_settings:
                            selectedFragment = new SettingsActivity();
                            break;
                    }

                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .addToBackStack(null)
                            .commit();

                    return true;
                }
            };

}

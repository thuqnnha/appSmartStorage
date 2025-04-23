package com.example.appsmartstorage;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;

public class AdminActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Ánh xạ view
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigationView);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Lấy role từ Intent
        int role = getIntent().getIntExtra("role", -1);

        if (role == 2) {
            // Ẩn menu "Quản lý tài khoản" nếu là user thường
            Menu menu = navigationView.getMenu();
            MenuItem accountUserItem = menu.findItem(R.id.nav_AccountUser);
            if (accountUserItem != null) {
                accountUserItem.setVisible(false);
            }
            getSupportActionBar().setTitle("User");
        }
        else {
            getSupportActionBar().setTitle("Admin");
        }

        // Setup Toggle để mở menu khi nhấn icon
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Mặc định mở StoringFragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frameContainer, new StoringActivity())
                    .commit();
            navigationView.setCheckedItem(R.id.nav_storing);
        }

        // Xử lý sự kiện click vào menu
        navigationView.setNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_storing) {
                selectedFragment = new StoringActivity();
            } else if (id == R.id.nav_AccountUser) {
                selectedFragment = new AccountUserActivity();
            } else if (id == R.id.nav_Selling) {
                selectedFragment = new SellingActivity();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frameContainer, selectedFragment)
                        .commit();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}

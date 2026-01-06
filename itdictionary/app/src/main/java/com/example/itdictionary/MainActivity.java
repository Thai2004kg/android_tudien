package com.example.itdictionary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils; // THÊM IMPORT NÀY
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer_layout;
    private NavigationView nav_view;
    private Toolbar toolbar;

    public static final String PREFS_NAME = "AppSettings";
    public static final String KEY_FONT_SIZE = "FONT_SIZE_SCALE";
    public static final String KEY_THEME = "THEME_MODE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        drawer_layout = findViewById(R.id.drawer_layout);
        nav_view = findViewById(R.id.nav_view);
        setSupportActionBar(toolbar);
        nav_view.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer_layout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer_layout.addDrawerListener(toggle);
        toggle.syncState();

        setupThemeSwitch();

        if (savedInstanceState == null) {
            String fragmentToLoad = getIntent().getStringExtra("FRAGMENT_TO_LOAD");
            if (TextUtils.equals(fragmentToLoad, "FAVORITES")) {
                loadFragment(new FavoritesFragment());
                nav_view.setCheckedItem(R.id.nav_favorites);
            } else {
                loadFragment(new SearchFragment());
                nav_view.setCheckedItem(R.id.nav_search);
            }
        }
    }


    private void setupThemeSwitch() {
        MenuItem themeItem = nav_view.getMenu().findItem(R.id.nav_theme);
        SwitchCompat themeSwitch = (SwitchCompat) themeItem.getActionView();

        if (themeSwitch != null) {
            int currentTheme = AppCompatDelegate.getDefaultNightMode();
            themeSwitch.setChecked(currentTheme == AppCompatDelegate.MODE_NIGHT_YES);

            themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int newTheme;
                if (isChecked) {
                    newTheme = AppCompatDelegate.MODE_NIGHT_YES;
                } else {
                    newTheme = AppCompatDelegate.MODE_NIGHT_NO;
                }

                AppCompatDelegate.setDefaultNightMode(newTheme);

                SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                editor.putInt(KEY_THEME, newTheme);
                editor.apply();
            });
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int itemId = item.getItemId();

        if (itemId == R.id.nav_home) {
            finish();
            return true;

        } else if (itemId == R.id.nav_search) {
            loadFragment(new SearchFragment());
        } else if (itemId == R.id.nav_favorites) {
            loadFragment(new FavoritesFragment());
        } else if (itemId == R.id.nav_translate) {
            Intent intent = new Intent(this, TranslateActivity.class);
            startActivity(intent);

        } else if (itemId == R.id.nav_font_size) {
            showFontSizeDialog();
            return true;

        } else if (itemId == R.id.nav_theme) {
            return false;
        }

        drawer_layout.closeDrawer(GravityCompat.START);
        return true;
    }


    private void showFontSizeDialog() {
        final String[] options = {"100% (Mặc định)", "125%", "150%", "200%"};
        final float[] sizeScales = {1.0f, 1.25f, 1.5f, 2.0f};

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        float currentScale = prefs.getFloat(KEY_FONT_SIZE, 1.0f);

        int checkedItem = 0;
        for (int i = 0; i < sizeScales.length; i++) {
            if (currentScale == sizeScales[i]) {
                checkedItem = i;
                break;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn cỡ chữ nội dung");
        builder.setSingleChoiceItems(options, checkedItem, (dialog, which) -> {
            float selectedScale = sizeScales[which];

            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
            editor.putFloat(KEY_FONT_SIZE, selectedScale);
            editor.apply();

            Toast.makeText(this, "Đã đổi cỡ chữ thành " + options[which], Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }


    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
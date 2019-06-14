package com.kcirqueapps.chatapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kcirqueapps.chatapp.R;
import com.kcirqueapps.chatapp.network.api.Api;
import com.kcirqueapps.chatapp.network.api.ApiClient;
import com.kcirqueapps.chatapp.network.model.HttpResponse;
import com.kcirqueapps.chatapp.network.model.User;
import com.kcirqueapps.chatapp.utils.PrefUtils;

import java.util.List;

import es.dmoral.toasty.Toasty;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_USER = "com.kcirqueapps.chatapp.activity.EXTRA_USER";
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navController = Navigation.findNavController(this, R.id.navigation_container);
        final BottomNavigationView navigationView = findViewById(R.id.bottom_nav);
        NavigationUI.setupWithNavController(navigationView, navController);
        whiteNotificationBar(navigationView);
        Api api = ApiClient.getInstance().getApi();
        User currentUser = new PrefUtils(this).getUser();
        if (currentUser != null) {
            api.pendingRequest(currentUser.getId())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableSingleObserver<HttpResponse<List<User>>>() {
                        @Override
                        public void onSuccess(HttpResponse<List<User>> listHttpResponse) {
                            if (!listHttpResponse.isError() && listHttpResponse.getResponse().size() > 0) {
                                navigationView.showBadge(R.id.nav_friend);
                                BadgeDrawable badgeDrawable = navigationView.getBadge(R.id.nav_friend);
                                badgeDrawable.setNumber(listHttpResponse.getResponse().size());
                            }
                        }

                        @Override
                        public void onError(Throwable e) {

                        }
                    });
        }
    }

    @Override
    public boolean onNavigateUp() {
        return NavigationUI.navigateUp(navController, (DrawerLayout) null);
    }

    private void whiteNotificationBar(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
            getWindow().setStatusBarColor(Color.WHITE);
        }
    }
}

package com.kcirqueapps.chatapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.kcirqueapps.chatapp.R;
import com.kcirqueapps.chatapp.network.api.Api;
import com.kcirqueapps.chatapp.network.api.ApiClient;
import com.kcirqueapps.chatapp.network.model.HttpResponse;
import com.kcirqueapps.chatapp.network.model.User;
import com.kcirqueapps.chatapp.utils.PrefUtils;


import dmax.dialog.SpotsDialog;
import es.dmoral.toasty.Toasty;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {
    private static final String TAG = "LoginActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private CompositeDisposable disposable = new CompositeDisposable();
    private TextInputLayout emailLayout, passwordLayout;
    private TextInputEditText emailEditText, passwordEditText;
    private Button loginBtn;
    private TextView noAccountTextView;
    private Api api;
    private AlertDialog alertDialog;
    private PrefUtils prefUtils;

    private FusedLocationProviderClient fusedLocationProviderClient;

    private double lat;
    private double lng;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        api = ApiClient.getInstance().getApi();
        prefUtils = new PrefUtils(this);
        loginBtn.setOnClickListener(this);
        noAccountTextView.setOnClickListener(this);
        whiteNotificationBar(noAccountTextView);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (checkPermission()) {
            getLastLocation();
        } else {
            requestPermission();
        }

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                token = instanceIdResult.getToken();
                Log.e(TAG, "onSuccess: " + token);
            }
        });

        passwordEditText.addTextChangedListener(this);
        emailEditText.addTextChangedListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLastLocation();
        }

    }

    private boolean checkPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    private void getLastLocation() {
        if (checkPermission()) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        lat = location.getLatitude();
                        lng = location.getLongitude();
                        Log.e("Login ", "onComplete: " + location);
                    }
                }
            });
        }
    }


    private void initView() {
        emailLayout = findViewById(R.id.email_layout);
        passwordLayout = findViewById(R.id.password_layout);
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        loginBtn = findViewById(R.id.login_btn);
        noAccountTextView = findViewById(R.id.no_account_text_view);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_btn:
                loginUser();
                break;
            case R.id.no_account_text_view:
                gotoRegisterActivity();
                break;
        }
    }

    private void loginUser() {
        Log.e(TAG, "Token: " + token);
        Log.e(TAG, "LatLng: " + lat + lng);

        if (TextUtils.isEmpty(emailEditText.getText())) {
            emailLayout.setError("Enter email");
            return;
        }
        if (TextUtils.isEmpty(passwordEditText.getText())) {
            passwordLayout.setError("Enter password");
            return;
        }
        showDialog();
        api.login(
                emailEditText.getText().toString(),
                passwordEditText.getText().toString(),
                lat,
                lng,
                token
        ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<HttpResponse<User>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onSuccess(HttpResponse<User> userHttpResponse) {
                        dismissDialog();
                        if (userHttpResponse.isError()) {
                            Toasty.error(LoginActivity.this, userHttpResponse.getMessage(), Toasty.LENGTH_SHORT).show();
                        } else {
                            Toasty.success(LoginActivity.this, userHttpResponse.getMessage(), Toasty.LENGTH_SHORT).show();
                            prefUtils.putUser(userHttpResponse.getResponse());
                            gotoMainActivity();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissDialog();
                        Toasty.error(LoginActivity.this, e.getLocalizedMessage(), Toasty.LENGTH_SHORT).show();
                    }
                });
    }

    private void showDialog() {
        alertDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setCancelable(false)
                .setMessage("Loading....")
                .build();
        alertDialog.show();
    }

    private void dismissDialog() {
        if (alertDialog != null)
            alertDialog.dismiss();
    }

    private void gotoRegisterActivity() {
        Intent registerIntent = new Intent(this, RegisterActivity.class);
        //registerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(registerIntent);
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
    }

    private void gotoMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
    }

    private void whiteNotificationBar(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
            getWindow().setStatusBarColor(Color.WHITE);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        emailLayout.setError(null);
        passwordLayout.setError(null);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}

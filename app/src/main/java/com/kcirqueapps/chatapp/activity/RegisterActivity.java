package com.kcirqueapps.chatapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.kcirqueapps.chatapp.R;
import com.kcirqueapps.chatapp.network.api.Api;
import com.kcirqueapps.chatapp.network.api.ApiClient;
import com.kcirqueapps.chatapp.network.model.HttpResponse;
import com.kcirqueapps.chatapp.network.model.User;

import java.util.Calendar;
import java.util.TimeZone;

import dmax.dialog.SpotsDialog;
import es.dmoral.toasty.Toasty;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener, TextWatcher {
    private CompositeDisposable disposable = new CompositeDisposable();
    private TextInputLayout firstNameLayout, lastNameLayout, emailLayout, passwordLayout;
    private TextInputEditText firstNameEditText, lastNameEditText, emailEditText, passwordEditText, mobileEditText;
    private RadioGroup genderRadioGroup;
    private TextView birthdayTextView, alreadyAccountTextView;
    private Button registerBtn;

    private Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
    private int year = calendar.get(Calendar.YEAR);
    private int month = calendar.get(Calendar.MONTH);
    private int day = calendar.get(Calendar.DAY_OF_MONTH);
    private String birthday;
    private String gender = "Male";

    private Api api;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initView();
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        api = ApiClient.getInstance().getApi();

        registerBtn.setOnClickListener(this);
        birthdayTextView.setOnClickListener(this);
        alreadyAccountTextView.setOnClickListener(this);
        genderRadioGroup.setOnCheckedChangeListener(this);
        whiteNotificationBar(alreadyAccountTextView);

        firstNameEditText.addTextChangedListener(this);
        lastNameEditText.addTextChangedListener(this);
        emailEditText.addTextChangedListener(this);
        passwordEditText.addTextChangedListener(this);
        mobileEditText.addTextChangedListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.already_account_text_view:
                gotoLoginActivity();
                break;
            case R.id.birthday_text_view:
                showBirthdayDialog();
                break;
            case R.id.registration_btn:
                registerUser();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void gotoLoginActivity() {
        Intent registerIntent = new Intent(this, LoginActivity.class);
        registerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(registerIntent);
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
    }

    private void registerUser() {
        if (TextUtils.isEmpty(firstNameEditText.getText())) {
            firstNameLayout.setError("Enter first name");
            return;
        }
        if (TextUtils.isEmpty(lastNameEditText.getText())) {
            lastNameLayout.setError("Enter last name");
            return;
        }
        if (TextUtils.isEmpty(emailEditText.getText())) {
            emailLayout.setError("Enter email");
            return;
        }
        if (TextUtils.isEmpty(passwordEditText.getText())) {
            passwordLayout.setError("Enter password");
            return;
        }
        if (birthday == null) {
            Toast.makeText(this, "Select birthday", Toast.LENGTH_SHORT).show();
            return;
        }
        showDialog();

        api.registerUser(
                firstNameEditText.getText().toString(),
                lastNameEditText.getText().toString(),
                emailEditText.getText().toString(),
                passwordEditText.getText().toString(),
                birthday,
                gender,
                mobileEditText.getText().toString()
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
                            Toasty.error(RegisterActivity.this, userHttpResponse.getMessage(), Toasty.LENGTH_SHORT).show();
                        } else {
                            Toasty.success(RegisterActivity.this, userHttpResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            gotoLoginActivity();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        dismissDialog();
                        Toasty.error(RegisterActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
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

    private void showBirthdayDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                if (month < 10 && dayOfMonth < 10) {
                    birthday = year + "-0" + (month + 1) + "-0" + dayOfMonth;
                } else if (month < 10) {
                    birthday = year + "-0" + (month + 1) + "-" + dayOfMonth;
                } else if (dayOfMonth < 10) {
                    birthday = year + "-" + (month + 1) + "-0" + dayOfMonth;
                } else {
                    birthday = year + "-" + (month + 1) + "-" + dayOfMonth;
                }
                birthdayTextView.setText(birthday);
            }
        }, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        RadioButton radioButton = findViewById(checkedId);
        gender = radioButton.getText().toString();
    }

    private void initView() {
        firstNameLayout = findViewById(R.id.first_name_layout);
        lastNameLayout = findViewById(R.id.last_name_layout);
        emailLayout = findViewById(R.id.email_layout);
        passwordLayout = findViewById(R.id.password_layout);
        firstNameEditText = findViewById(R.id.first_name_edit_text);
        lastNameEditText = findViewById(R.id.last_name_edit_text);
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        mobileEditText = findViewById(R.id.mobile_edit_text);
        genderRadioGroup = findViewById(R.id.gender_radio_group);
        birthdayTextView = findViewById(R.id.birthday_text_view);
        alreadyAccountTextView = findViewById(R.id.already_account_text_view);
        registerBtn = findViewById(R.id.registration_btn);
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
        firstNameLayout.setError(null);
        lastNameLayout.setError(null);
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


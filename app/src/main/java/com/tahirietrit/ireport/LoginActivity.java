package com.tahirietrit.ireport;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import objects.UserToken;
import requests.RequestCallBack;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import utils.AppPreferences;
import utils.Utilitys;

/**
 * Created by macb on 10/05/16.
 */
public class LoginActivity extends Activity{
    private CallbackManager callbackManager;
    private LoginButton loginButton;
    RequestCallBack reqCall;
    Retrofit retrofit;
    private Tracker mTracker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        setContentView(R.layout.login_layout);
        AppPreferences.init(getApplicationContext());
        if(AppPreferences.getAccessToken()!=null){
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("email");

        callbackManager = CallbackManager.Factory.create();

        retrofit = new Retrofit.Builder()
                .baseUrl(Utilitys.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        reqCall = retrofit.create(RequestCallBack.class);

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Call<UserToken> authCall = reqCall.userToken(loginResult.getAccessToken().getToken(), "", "Android", "");
                authCall.enqueue(new Callback<UserToken>() {
                    @Override
                    public void onResponse(Call<UserToken> call, Response<UserToken> response) {
                        if (response.body() != null) {
                            System.out.println(response.body().getToken());
                            AppPreferences.setAccessToken(response.body().getToken());

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<UserToken> call, Throwable t) {

                    }
                });

            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException exception) {
            }
        });
        IReport application = (IReport) getApplication();
        mTracker = application.getDefaultTracker();

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName("Login Activity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }
}

package com.ws1617.iosl.pubcrawl20;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by Icke-Hier on 08.02.2017.
 */
public class SignInActivity extends AppCompatActivity implements
  GoogleApiClient.OnConnectionFailedListener,
  View.OnClickListener {

  private static final String TAG = "SignInActivity";
  private static final int RC_SIGN_IN = 9008;

  private TextView mTextAuthCode;
  private GoogleApiClient mGoogleApiClient;
  private ProgressDialog mProgressDialog;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_sign_in);

    // Button listeners
    findViewById(R.id.sign_in_button).setOnClickListener(this);
    findViewById(R.id.continue_button).setOnClickListener(this);
    findViewById(R.id.authText);

    // [START configure_signin]
    // Configure sign-in to request the user's ID, email address, and basic
    // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
    String serverClientId = getString(R.string.server_client_id);
    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
      .requestScopes(new Scope(Scopes.DRIVE_APPFOLDER))
      .requestServerAuthCode(serverClientId, false)
      .build();
    // [END configure_signin]

    // [START build_client]
    // Build a GoogleApiClient with access to the Google Sign-In API and the
    // options specified by gso.
    mGoogleApiClient = new GoogleApiClient.Builder(this)
      .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
      .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
      .build();
    // [END build_client]

    mTextAuthCode = (TextView) findViewById(R.id.authText);
    // [START customize_button]
    // Set the dimensions of the sign-in button.
    SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
    signInButton.setSize(SignInButton.SIZE_STANDARD);
    // [END customize_button]
  }

  @Override
  public void onStart() {
    super.onStart();

    OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
    if (opr.isDone()) {
      // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
      // and the GoogleSignInResult will be available instantly.
      Log.d(TAG, "Got cached sign-in");
      GoogleSignInResult result = opr.get();
      handleSignInResult(result);
    } else {
      // If the user has not previously signed in on this device or the sign-in has expired,
      // this asynchronous branch will attempt to sign in the user silently.  Cross-device
      // single sign-on will occur in this branch.
      showProgressDialog();
      opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
        @Override
        public void onResult(GoogleSignInResult googleSignInResult) {
          hideProgressDialog();
          handleSignInResult(googleSignInResult);
        }
      });
    }
  }

  // [START onActivityResult]
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
    if (requestCode == RC_SIGN_IN) {
      if (resultCode == RESULT_OK) {
        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
        handleSignInResult(result);
      } else {
        Log.d(TAG, "Result from on activity result is not ok " + resultCode);
      }
    }
  }
  // [END onActivityResult]

  // [START handleSignInResult]
  private void handleSignInResult(GoogleSignInResult result) {
    Log.d(TAG, "handleSignInResult:" + result.isSuccess());
    if (result.isSuccess()) {
      // Signed in successfully, show authenticated UI.
      GoogleSignInAccount acct = result.getSignInAccount();

      String authCode = acct.getServerAuthCode();
      mTextAuthCode.setText("Auth Code: " + authCode);
      getToken(authCode);
    } else {
      // Signed out, show unauthenticated UI.
      Log.d(TAG, "Signed out - Should be here!");
    }
  }
  // [END handleSignInResult]


  // [START signIn]
  private void signIn() {
    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
    startActivityForResult(signInIntent, RC_SIGN_IN);

  }
  // [END signIn]

  @Override
  public void onConnectionFailed(ConnectionResult connectionResult) {
    // An unresolvable error has occurred and Google APIs (including Sign-In) will not
    // be available.
    Log.d(TAG, "onConnectionFailed:" + connectionResult);
  }

  private void showProgressDialog() {
    if (mProgressDialog == null) {
      mProgressDialog = new ProgressDialog(this);
      mProgressDialog.setMessage(getString(R.string.loading));
      mProgressDialog.setIndeterminate(true);
    }

    mProgressDialog.show();
  }

  private void hideProgressDialog() {
    if (mProgressDialog != null && mProgressDialog.isShowing()) {
      mProgressDialog.hide();
    }
  }

  @Override
  protected void onDestroy() {
    try {
      if (mProgressDialog != null && mProgressDialog.isShowing()) {
        mProgressDialog.dismiss();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    super.onDestroy();
  }

  private void showApp() {
    Intent intent = new Intent(this, MainActivity.class);
    this.startActivity(intent);
  }

  private void getToken(final String authCode) {
    RequestQueue queue = Volley.newRequestQueue(this);
    String url = "https://www.googleapis.com/oauth2/v4/token";
    StringRequest postRequest = new StringRequest(Request.Method.POST, url,
      new Response.Listener<String>()
      {
        @Override
        public void onResponse(String response) {
          // response
          Log.d("Response", response);
          try {
            JSONObject jsonObject = new JSONObject(response);
            App.setToken(jsonObject.getString("access_token"));
          } catch (JSONException e) {
            e.printStackTrace();
          }

        }
      },
      new Response.ErrorListener()
      {
        @Override
        public void onErrorResponse(VolleyError error) {
          // error
          Log.d("Error.Response", error.getMessage());
        }
      }
    ) {
      @Override
      protected Map<String, String> getParams()
      {
        Map<String, String>  params = new HashMap<String, String>();
        params.put("code", authCode);
        params.put("client_id", "649804390923-7mov7q7g42kbod1do8ikvhtgdu0m58ai.apps.googleusercontent.com");
        params.put("client_secret",  "3zRUO4fOIBwLlIU8VntClGB6");
        params.put("redirect_uri",  "https://developers.google.com/oauthplayground");
        params.put("grant_type",  "authorization_code");
        return params;
      }
    };
    queue.add(postRequest);
  }

  @Override
  protected void onPause() {
    super.onPause();

    if(mProgressDialog != null) {
      mProgressDialog.dismiss();
      mProgressDialog = null;
    }
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.sign_in_button:
        signIn();
        break;
      case R.id.continue_button:
        showApp();
        break;
    }
  }
}
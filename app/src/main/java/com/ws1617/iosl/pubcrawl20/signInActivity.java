package com.ws1617.iosl.pubcrawl20;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
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
import com.ws1617.iosl.pubcrawl20.Database.DatabaseHelper;
import com.ws1617.iosl.pubcrawl20.Database.RequestQueueHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import static com.ws1617.iosl.pubcrawl20.Database.JsonParser.EMBEDDED;
import static com.ws1617.iosl.pubcrawl20.Database.JsonParser.PERSONS;


/**
 * Created by Icke-Hier on 08.02.2017.
 */
public class SignInActivity extends AppCompatActivity implements
  GoogleApiClient.OnConnectionFailedListener,
  View.OnClickListener {

  private static final String TAG = "SignInActivity";
  private static final int RC_SIGN_IN = 9008;

  private GoogleApiClient mGoogleApiClient;
  private ProgressDialog mProgressDialog;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_sign_in);

    // Button listeners
    findViewById(R.id.sign_in_button).setOnClickListener(this);

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
      getToken(acct);

    } else {
      // Signed out, show unauthenticated UI.
      Log.d(TAG, "Signed out - Shouldnt be here!");
    }
  }
  // [END handleSignInResult]

  private void getCrawler(final GoogleSignInAccount acc) {
    SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_user), Context.MODE_PRIVATE);
    int id = sharedPref.getInt(getString(R.string.user_id), -1);
    if(id == -1) {
      getCrawlerID(acc);
    }
    showApp();
  }

  private void createCrawler(GoogleSignInAccount acc) {
    try {
      RequestQueueHelper requestQueue = new RequestQueueHelper(this);
      String url = DatabaseHelper.getServerUrl(this)+ "/crawlers/";
      JSONObject jsonBody = new JSONObject();
      jsonBody.put("userName", acc.getDisplayName());
      jsonBody.put("profile", acc.getEmail());
      jsonBody.put("Authorization", "Bearer " + App.getToken());
      final String requestBody = jsonBody.toString();

      StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
          Log.i("VOLLEY", response);
        }
      }, new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
          Log.e("VOLLEY", error.toString());
        }
      }) {
        @Override
        public String getBodyContentType() {
          return "application/json; charset=utf-8";
        }

        @Override
        public byte[] getBody() throws AuthFailureError {
          try {
            return requestBody == null ? null : requestBody.getBytes("utf-8");
          } catch (UnsupportedEncodingException uee) {
            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
            return null;
          }
        }

        @Override
        protected Response<String> parseNetworkResponse(NetworkResponse response) {
          String responseString = "";
          if (response != null) {
            responseString = String.valueOf(response.statusCode);
            // can get more details such as response.headers
          }
          return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
        }
      };

      requestQueue.add(stringRequest);
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  private void getCrawlerID(final GoogleSignInAccount acc) {
    final RequestQueueHelper requestQueue = new RequestQueueHelper(this);
    String url = DatabaseHelper.getServerUrl(this)+ "/crawlers/";
    JsonObjectRequest personrequest = new JsonObjectRequest(Request.Method.GET, url, null,
      new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
          try {
            JSONArray crawlersJSON = response.getJSONObject(EMBEDDED).getJSONArray(PERSONS);
            for (int i = 0; i < crawlersJSON.length(); i++) {
              JSONObject jsonCrawler = crawlersJSON.getJSONObject(i);
              if(jsonCrawler.getString("profile").equals(acc.getEmail())) {
                setCrawler(jsonCrawler);
              }
            }
            createCrawler(acc);
          } catch (Exception e) {
            Log.e(TAG, "eventsRequest error: " + e.getLocalizedMessage());
          }
          requestQueue.gotResponse();
        }
      },
      new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
          Log.e(TAG, "eventsRequest error: " + error.getLocalizedMessage());
          requestQueue.gotResponse();
        }
      })
    {
      @Override
      public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> params = new HashMap<String, String>();
        params.put("Authorization", "Bearer " + App.getToken());
        return params;
      }
    };
    requestQueue.add(personrequest);
  }

  private void setCrawler(JSONObject json) {
    Log.i(TAG, json.toString());
    SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_user), Context.MODE_PRIVATE);
    try {
      JSONObject linksJSON = json.getJSONObject("_links");
      JSONObject selfJSON = linksJSON.getJSONObject("self");
      String link = selfJSON.getString("href");
      Log.i(TAG, link);
      sharedPref.edit().putInt(getString(R.string.user_id), getIDfromURL(link)).apply();
    } catch (JSONException e) {
      e.printStackTrace();
      return;
    }
  }

  /**
   * Method to get id from a link provide by the REST API.
   * Reads string from end to starts till first non number charackter
   * @param url
   * @return Last numbers in url
   */
  private int getIDfromURL(String url) {
    StringBuilder sb = new StringBuilder();
    for (int i = url.length() - 1; i >= 0; i --) {
      char c = url.charAt(i);
      if (Character.isDigit(c)) {
        sb.insert(0, c);
      } else {
        break;
      }
    }
    int result;
    try {
      result = Integer.parseInt(sb.toString());
    }
    catch (Exception e)
    {
      result = -1;
    }
    return result;
  }


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

  private void getToken(final GoogleSignInAccount acct) {
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
            getCrawler(acct);
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
        params.put("code", acct.getServerAuthCode());
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
    if(v.getId() == R.id.sign_in_button)
        signIn();
  }
}
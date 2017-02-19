package com.ws1617.iosl.pubcrawl20.Utilites;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

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
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.ws1617.iosl.pubcrawl20.App;
import com.ws1617.iosl.pubcrawl20.DataModels.Person;
import com.ws1617.iosl.pubcrawl20.Database.DatabaseException;
import com.ws1617.iosl.pubcrawl20.Database.DatabaseHelper;
import com.ws1617.iosl.pubcrawl20.Database.JsonParser;
import com.ws1617.iosl.pubcrawl20.Database.PersonDbHelper;
import com.ws1617.iosl.pubcrawl20.Database.RequestQueueHelper;
import com.ws1617.iosl.pubcrawl20.R;
import com.ws1617.iosl.pubcrawl20.StartActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import static com.ws1617.iosl.pubcrawl20.Database.DatabaseHelper.resetPersonsDatabase;
import static com.ws1617.iosl.pubcrawl20.Database.JsonParser.EMBEDDED;
import static com.ws1617.iosl.pubcrawl20.Database.JsonParser.PERSONS;
import static com.ws1617.iosl.pubcrawl20.Database.JsonParser.PERSON_NAME;
import static com.ws1617.iosl.pubcrawl20.Database.JsonParser.PERSON_PROFILE;

/**
 * Created by Icke-Hier on 14.02.2017.
 */

public class SignInHelper {

  private static final String TAG = "SignInHelper";
  private final StartActivity activity;

  public SignInHelper(StartActivity act) {
    activity = act;
  }

  public void getToken(final GoogleSignInAccount acct) {
    RequestQueue queue = Volley.newRequestQueue(activity);
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

  private void setCrawlerID(final GoogleSignInAccount acc) {
    resetPersonsDatabase(activity);
    final RequestQueueHelper requestQueue = new RequestQueueHelper(activity);
    String url = DatabaseHelper.getServerUrl(activity)+ "/crawlers/";
    JsonObjectRequest personRequest = new JsonObjectRequest(Request.Method.GET, url, null,
      new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
          Log.i(TAG, "reset Person database to add crawler " +acc.getId());
          handleResponse(response, acc, true);
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
    requestQueue.add(personRequest);
  }

  private void handleResponse(JSONObject json, GoogleSignInAccount acc, boolean newAccount) {
    try {
      JSONArray crawlersJSON = json.getJSONObject(EMBEDDED).getJSONArray(PERSONS);
      for (int i = 0; i < crawlersJSON.length(); i++) {
        JSONObject jsonCrawler = crawlersJSON.getJSONObject(i);
        if(jsonCrawler.getString(PERSON_PROFILE).equals(acc.getId()) && setCrawlerInApp(jsonCrawler)) {
          activity.showApp();
          return;
        }
      }
      //Crawler unknown: Create new one
      if(newAccount) createCrawler(acc);
      else {
        Toast.makeText(activity, R.string.error_creating_acc, Toast.LENGTH_LONG).show();
        Log.d(TAG, "Error creating account!");
        activity.signOut();
      }
    } catch (Exception e) {
      Log.e(TAG, "eventsRequest error: " + e.getLocalizedMessage());
    }
  }

  private void createCrawler(final GoogleSignInAccount acc) {
    try {
      RequestQueueHelper requestQueue = new RequestQueueHelper(activity);
      String url = DatabaseHelper.getServerUrl(activity)+ "/crawlers/";
      JSONObject jsonBody = new JSONObject();
      jsonBody.put(PERSON_NAME, acc.getDisplayName());
      jsonBody.put(PERSON_PROFILE, acc.getId());
      final String requestBody = jsonBody.toString();

      JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
          resetPersonsDatabase(activity);
          handleResponse(response, acc, false);
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
        public byte[] getBody()  {
          try {
            return requestBody == null ? null : requestBody.getBytes("utf-8");
          } catch (UnsupportedEncodingException uee) {
            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
            return null;
          }
        }

        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
          Map<String, String> params = new HashMap<String, String>();
          params.put("Authorization", "Bearer " + App.getToken());
          return params;
        }
      };

      requestQueue.add(stringRequest);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    /*try {
      final RequestQueueHelper requestQueue = new RequestQueueHelper(activity);
      String url = DatabaseHelper.getServerUrl(activity) + "/crawlers/";
      JsonObjectRequest createRequest = new JsonObjectRequest(Request.Method.POST, url, null,
        new Response.Listener<JSONObject>() {
          @Override
          public void onResponse(JSONObject response) {
            handleResponse(response, acc, true);
            requestQueue.gotResponse();
          }
        },
        new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            Log.e(TAG, "eventsRequest error: " + error.getLocalizedMessage());
            requestQueue.gotResponse();
          }
        }) {
        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
          Map<String, String> params = new HashMap<String, String>();
          params.put("Authorization", "Bearer " + App.getToken());
          return params;
        }

        @Override
        protected Map<String, String> getParams()
        {
          Map<String, String>  params = new HashMap<String, String>();
          params.put(PERSON_NAME, acc.getDisplayName());
          params.put(PERSON_PROFILE, acc.getId());
          return params;
        }
      };
      requestQueue.add(createRequest);
    } catch (Exception e) {
      Log.e(TAG, "eventsRequest error: " + e.getLocalizedMessage());
    }*/
  }

  public void deleteCrawler(final int id) {
    try {
      final RequestQueueHelper requestQueue = new RequestQueueHelper(activity);
      String url = DatabaseHelper.getServerUrl(activity) + "/crawlers/" +id;
      JsonObjectRequest createRequest = new JsonObjectRequest(Request.Method.DELETE, url, null,
        new Response.Listener<JSONObject>() {
          @Override
          public void onResponse(JSONObject response) {
            resetPersonsDatabase(activity);
            Log.i(TAG, "deleted crawler: " +id);
          }
        },
        new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            Log.e(TAG, "error while deleting crawler: " +id+ " error: " +error.getLocalizedMessage());
            requestQueue.gotResponse();
          }
        }) {
        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
          Map<String, String> params = new HashMap<String, String>();
          params.put("Authorization", "Bearer " + App.getToken());
          return params;
        }
      };
      requestQueue.add(createRequest);
    } catch (Exception e) {
      Log.e(TAG, "eventsRequest error: " + e.getLocalizedMessage());
    }
  }

  private void getCrawler(final GoogleSignInAccount acc) {
    SharedPreferences sharedPref = activity.getSharedPreferences(activity.getString(R.string.preference_user), Context.MODE_PRIVATE);
    long id = sharedPref.getLong(activity.getString(R.string.user_id), -1);
    if(id == -1) {
      setCrawlerID(acc);
    }
    checkUser(acc, id);
  }

	/**
   * Method to check if user in shared prefs is correct
   * @param acc
   * @param id
   */
  private void checkUser(GoogleSignInAccount acc, long id) {
    Person person = null;
    try {
      person = new PersonDbHelper().getPerson(id);
    } catch (DatabaseException e) {
      Log.d(TAG, "Person: id " + id + " not in database, needed to create");
    }
    if(person==null) {
      setCrawlerID(acc);
      return;
    }
    activity.showApp();
  }

  private boolean setCrawlerInApp(JSONObject json) {
    Log.i(TAG, json.toString());
    SharedPreferences sharedPref = activity.getSharedPreferences(activity.getString(R.string.preference_user), Context.MODE_PRIVATE);
    try {
      JSONObject linksJSON = json.getJSONObject("_links");
      JSONObject selfJSON = linksJSON.getJSONObject("self");
      String link = selfJSON.getString("href");
      Log.i(TAG, link);
      sharedPref.edit().putLong(activity.getString(R.string.user_id), getIDfromURL(link)).apply();
    } catch (JSONException e) {
      e.printStackTrace();
      return false;
    }
    return true;
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
}

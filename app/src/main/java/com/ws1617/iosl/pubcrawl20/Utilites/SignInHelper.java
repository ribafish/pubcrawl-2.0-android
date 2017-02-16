package com.ws1617.iosl.pubcrawl20.Utilites;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

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

import static com.ws1617.iosl.pubcrawl20.Database.JsonParser.EMBEDDED;
import static com.ws1617.iosl.pubcrawl20.Database.JsonParser.PERSONS;

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
    final RequestQueueHelper requestQueue = new RequestQueueHelper(activity);
    String url = DatabaseHelper.getServerUrl(activity)+ "/crawlers/";
    JsonObjectRequest personrequest = new JsonObjectRequest(Request.Method.GET, url, null,
      new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
          try {
            JSONArray crawlersJSON = response.getJSONObject(EMBEDDED).getJSONArray(PERSONS);
            boolean knownUser = false;
            for (int i = 0; i < crawlersJSON.length(); i++) {
              JSONObject jsonCrawler = crawlersJSON.getJSONObject(i);
              if(jsonCrawler.getString("profile").equals(acc.getEmail())) {
                knownUser = setCrawler(jsonCrawler);
                if(knownUser) break;
              }
            }
            if(!knownUser) createCrawler(acc);
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

  private void createCrawler(final GoogleSignInAccount acc) {
    try {
      RequestQueueHelper requestQueue = new RequestQueueHelper(activity);
      String url = DatabaseHelper.getServerUrl(activity)+ "/crawlers/";
      JSONObject jsonBody = new JSONObject();
      jsonBody.put("userName", acc.getDisplayName());
      jsonBody.put("profile", acc.getEmail());
      final String requestBody = jsonBody.toString();

      StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
          setCrawlerID(acc);
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
        public Map<String, String> getHeaders() throws AuthFailureError {
          Map<String, String> params = new HashMap<String, String>();
          params.put("Authorization", "Bearer " + App.getToken());
          return params;
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

  private void getCrawler(final GoogleSignInAccount acc) {
    SharedPreferences sharedPref = activity.getSharedPreferences(activity.getString(R.string.preference_user), Context.MODE_PRIVATE);
    int id = sharedPref.getInt(activity.getString(R.string.user_id), -1);
    if(id == -1) {
      setCrawlerID(acc);
    }
    checkUser(acc, id);
    activity.showApp();
  }

	/**
   * Method to check if user in shared prefs is correct
   * @param acc
   * @param id
   */
  private void checkUser(GoogleSignInAccount acc, int id) {
    Person person = null;
    try {
      person = new PersonDbHelper().getPerson(id);
    } catch (DatabaseException e) {
      Log.d(TAG, "Person: id " + id + " not in database, needed to create");
    }
    if(person!=null)  return;
    setCrawlerID(acc);
  }

  private boolean setCrawler(JSONObject json) {
    Log.i(TAG, json.toString());
    SharedPreferences sharedPref = activity.getSharedPreferences(activity.getString(R.string.preference_user), Context.MODE_PRIVATE);
    try {
      JSONObject linksJSON = json.getJSONObject("_links");
      JSONObject selfJSON = linksJSON.getJSONObject("self");
      String link = selfJSON.getString("href");
      Log.i(TAG, link);
      sharedPref.edit().putInt(activity.getString(R.string.user_id), getIDfromURL(link)).apply();
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

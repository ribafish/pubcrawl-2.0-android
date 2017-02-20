package com.ws1617.iosl.pubcrawl20.Utilites;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.ws1617.iosl.pubcrawl20.App;
import com.ws1617.iosl.pubcrawl20.Database.DatabaseHelper;
import com.ws1617.iosl.pubcrawl20.Database.RequestQueueHelper;
import com.ws1617.iosl.pubcrawl20.R;

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
 * AccountManager implements...
 * <p>
 * Created by phahne on 20.02.2017.
 */

public class AccountManager {

	private static final String TAG = "AccountManager";
	private final IAccManager manager;
	private final GoogleSignInAccount acc;
	private final Context context;

	public interface IAccManager {

		Context getContext();

		void loginFinished();

		void loginFailed();

		void loginError();
	}

	public AccountManager(IAccManager m, GoogleSignInAccount a) {
		manager  = m;
		context = manager.getContext();
		acc = a;
	}

	public void startLogin() {
		RequestQueue queue = Volley.newRequestQueue(context);
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
						knownUserOnServer(false);
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
				params.put("code", acc.getServerAuthCode());
				params.put("client_id", "649804390923-7mov7q7g42kbod1do8ikvhtgdu0m58ai.apps.googleusercontent.com");
				params.put("client_secret",  "3zRUO4fOIBwLlIU8VntClGB6");
				params.put("redirect_uri",  "https://developers.google.com/oauthplayground");
				params.put("grant_type",  "authorization_code");
				return params;
			}
		};
		queue.add(postRequest);
	}

	private void knownUserOnServer(final boolean newCrawler) {
		final RequestQueueHelper requestQueue = new RequestQueueHelper(context);
		String url = DatabaseHelper.getServerUrl(context)+ "/crawlers/";
		JsonObjectRequest personRequest = new JsonObjectRequest(Request.Method.GET, url, null,
			new Response.Listener<JSONObject>() {
				@Override
				public void onResponse(JSONObject response) {
					handleResponse(response, acc, newCrawler);
				}
			},
			new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					Log.e(TAG, "knownUserOnServer requests failed: " + error.getLocalizedMessage());
					manager.loginError();
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

	/**
	 * Method to handle Crawler REST calls
	 * @param json
	 * @param acc
	 * @param newCrawler true if crawler is newly create -> login failed if user isnt in list
	 */
	private void handleResponse(JSONObject json, GoogleSignInAccount acc, boolean newCrawler) {
		try {
			JSONArray crawlersJSON = json.getJSONObject(EMBEDDED).getJSONArray(PERSONS);
			for (int i = 0; i < crawlersJSON.length(); i++) {
				JSONObject jsonCrawler = crawlersJSON.getJSONObject(i);
				if(jsonCrawler.getString(PERSON_PROFILE).equals(acc.getId())) {
					if(setCrawlerInApp(jsonCrawler)) {
						if(newCrawler) resetPersonsDatabase(context);
						manager.loginFinished();
					}
					else {
						manager.loginFailed();
						Log.d(TAG, "Error on handle Response! Failed to get ID from JSON");
					}
					return;
				}
			}
			//Crawler unknown: Create new one
			if(newCrawler) manager.loginFailed();
			else createNewCrawler();
		} catch (Exception e) {
			Log.e(TAG, "eventsRequest error: " + e.getLocalizedMessage());
		}
	}

	private boolean setCrawlerInApp(JSONObject json) {
		Log.i(TAG, json.toString());
		SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_user), Context.MODE_PRIVATE);
		try {
			JSONObject linksJSON = json.getJSONObject("_links");
			JSONObject selfJSON = linksJSON.getJSONObject("self");
			String link = selfJSON.getString("href");
			Log.i(TAG, link);
			sharedPref.edit().putLong(context.getString(R.string.user_id), getIDfromURL(link)).apply();
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}


	private void createNewCrawler() {
		RequestQueueHelper requestQueue = new RequestQueueHelper(context);
		String url = DatabaseHelper.getServerUrl(context)+ "/crawlers/";
		final String requestBody;
		try {
			JSONObject jsonBody = new JSONObject();
			jsonBody.put(PERSON_NAME, acc.getDisplayName());
			jsonBody.put(PERSON_PROFILE, acc.getId());
			requestBody = jsonBody.toString();
		}
		catch (JSONException j) {
			j.printStackTrace();
			manager.loginFailed();
			return;
		}
		CustomRequest jsonObjectRequest = new CustomRequest(Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject response) {
					handleResponse(response, acc, true);
				}
			},
			new Response.ErrorListener()
			{
				@Override
				public void onErrorResponse(VolleyError error)
				{
					Log.e(TAG, error.toString());
					Log.d(TAG, "Create crawler respones not well formed");
					knownUserOnServer(true);
				}
			})
		{
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
		requestQueue.add(jsonObjectRequest);
	}

	public void deleteCrawler(final int id) {
		try {
			final RequestQueueHelper requestQueue = new RequestQueueHelper(context);
			String url = DatabaseHelper.getServerUrl(context) + "/crawlers/" +id;
			JsonObjectRequest createRequest = new JsonObjectRequest(Request.Method.DELETE, url, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						resetPersonsDatabase(context);
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

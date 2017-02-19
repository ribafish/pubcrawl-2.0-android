package com.ws1617.iosl.pubcrawl20.Database;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ws1617.iosl.pubcrawl20.App;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Gasper Kojek on 19. 02. 2017.
 * Github: https://github.com/ribafish/
 */

public class SecureJsonObjectRequest extends JsonObjectRequest {

    public SecureJsonObjectRequest(int method, String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
    }

    public SecureJsonObjectRequest(String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(url, jsonRequest, listener, errorListener);
    }


    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> params = new HashMap<String, String>();
        params.put("Authorization", "Bearer " + App.getToken());
        return params;
    }


}
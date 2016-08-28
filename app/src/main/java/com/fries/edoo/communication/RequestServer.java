package com.fries.edoo.communication;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.fries.edoo.app.AppController;
import com.fries.edoo.helper.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tmq on 12/07/2016.
 */
public class RequestServer {
    private static final String TAG = "RequestServer";
    private JsonObjectRequest request;
    private int method;
    private String url;
    private JSONObject jsonReq;
    private Context mContext;

    private SessionManager session;

    // Request: Not upload data (JSONObject)
    public RequestServer(Context context, int method, String url) {
        session = new SessionManager(context);
        this.mContext = context;
        this.method = method;
        this.url = url;
        this.jsonReq = new JSONObject();

        initListener();
    }

    // Request: Upload data (JSONObject)
    public RequestServer(Context context, int method, String url, JSONObject jsonReq) {
        session = new SessionManager(context);
        this.mContext = context;
        this.method = method;
        this.url = url;
        this.jsonReq = jsonReq;

        initListener();
    }

    private void initListener() {
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (mListener != null){
                        mListener.onReceive(false, response, response.getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        Response.ErrorListener error = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    byte[] data = error.networkResponse.data;
                    JSONObject jsonError = new JSONObject(new String(data));
                    if (mListener != null){
                        mListener.onReceive(true, jsonError, jsonError.getString("message"));
                    }
                } catch (Exception e) {
                    if (mListener != null){
                        try {
                            mListener.onReceive(true, null, "");
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        };

        final boolean isLoggin = session.isLoggedIn();
        final String token = session.getTokenLogin();

        request = new JsonObjectRequest(method, url, jsonReq, listener, error) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                if (isLoggin) {
                    Map<String, String> params = new HashMap<String, String>();
                    // Send token to server -> finish a session
                    params.put("Authorization", token);
                    return params;
                }

                return super.getHeaders();
            }
        };
    }

    public boolean sendRequest(String tag) {
        if (isOnline()){
            AppController.getInstance().addToRequestQueue(request, tag);
            return true;
        } else {
            Toast.makeText(mContext, "Vui lòng kiểm tra kết nối internet!", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


    // ---------------------------------------------------------------------------------------------
    private ServerListener mListener;

    public void setListener(ServerListener listener) {
        mListener = listener;
    }

    public interface ServerListener {
        void onReceive(boolean error, JSONObject response, String message) throws JSONException;
    }

}

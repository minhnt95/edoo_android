package com.fries.edoo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.fries.edoo.app.AppConfig;
import com.fries.edoo.communication.RequestServer;
import com.fries.edoo.helper.SQLiteHandler;
import com.fries.edoo.helper.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends Activity {
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private static final int REQUEST_CODE_REGISTER = 1234;
    private Button btnLogin;
    private Button btnLinkToRegister;
    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);

        //lay du lieu tu intent do vao edittext
//        Intent mIntent = getIntent();
//        inputEmail.setText(mIntent.getStringExtra(SQLiteHandler.KEY_EMAIL));
//        inputPassword.setText(mIntent.getStringExtra("password"));

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Session manager
        session = new SessionManager(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        // Login button Click Event
        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                // Check for empty data in the form
                if (!email.isEmpty() && !password.isEmpty()) {
                    // login user
                    checkLogin(email, password);
                } else {
                    // Prompt user to enter credentials
                    Toast.makeText(getApplicationContext(),
                            "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_LONG)
                            .show();
                }
            }

        });

        // Link to Register Screen
        btnLinkToRegister.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        RegisterActivity.class);
//                startActivity(i);
                startActivityForResult(i, REQUEST_CODE_REGISTER);
            }
        });

        btnLinkToRegister.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_REGISTER) {
            if (resultCode == RESULT_CANCELED) {
                return;
            } else if (resultCode == RESULT_OK) {
                inputEmail.setText(data.getStringExtra(SQLiteHandler.KEY_EMAIL));
                inputPassword.setText(data.getStringExtra("password"));
            }
        }
    }

    /**
     * function to verify login details in mysql db
     */
    private void checkLogin(final String email, final String password) {
        pDialog.setMessage("Đăng nhập ...");
        showDialog();

        JSONObject objRequest = new JSONObject();
        try {
            objRequest.put("email", email);
            objRequest.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestServer requestServer = new RequestServer(getApplicationContext(), Method.POST, AppConfig.URL_LOGIN, objRequest);
        requestServer.setListener(new RequestServer.ServerListener() {
            @Override
            public void onReceive(boolean error, JSONObject response, String message) throws JSONException {
                hideDialog();
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                if (!error) {
//                    Toast.makeText(getApplicationContext(), "Đăng nhập thành công!", Toast.LENGTH_LONG).show();

                    // user successfully logged in
                    // Create login session
                    session.setLogin(true);
                    String token = response.getJSONObject("data").getString("token");
                    session.setTokenLogin(token);
                    Toast.makeText(getApplicationContext(), "Token: " + token, Toast.LENGTH_SHORT).show();

/*
                    // Now store the user in SQLite
                    String uid = jObj.getString("uid");

                    JSONObject user = jObj.getJSONObject("user");
                    String ava = user.getString("avatar");
                    String email = user.getString("email");
                    String lop = user.getString("lop");
                    String mssv = user.getString("mssv");
                    String type = user.getString("type");
                    String name = user.getString("name");

                    String created_at = "";

                    Log.i(TAG, "login: " + name);
                    Log.i(TAG, "ava: " + ava);
                    Log.i(TAG, "login: " + email);
                    Log.i(TAG, "login: " + lop);
                    Log.i(TAG, "login: " + mssv);
                    Log.i(TAG, "login: " + type);
*/
                    // Inserting row in users table
//                                db.addUser(name, email, uid, created_at, lop, mssv, type, ava);

                    // Temporary data
                    db.addUser("Trần Minh Quý", email, "", "", "K58CLC", "13020355", "student", "http://downloadicons.net/sites/default/files/female-college-students-'-icon-14067.png");


                    // Launch main activity
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
        requestServer.sendRequest("req_log_in");
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}

package com.exsample.empit_sample01;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String ACCESS_TYPE_LOGIN = "1";
    public static final String ACCESS_TYPE_NEW_USER = "2";
    public static final String ACCESS_TYPE_DELETE_USER = "3";
    public static final String ACCESS_TYPE_PROFILE = "4";

    private static String DEFAULT_URL = "172.20.10.9:8080";
    public static String SERVER_URL = DEFAULT_URL;

    private EditText editMailAddr;
    private EditText editPassword;
    private static EditText editFullName;
    private static EditText editAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        editMailAddr = findViewById(R.id.editMailAddress);
        editMailAddr.setText("");
        editPassword = findViewById(R.id.editPassword);
        editPassword.setText("");
        editFullName = findViewById(R.id.editFullName);
        editFullName.setText("");
        editAddress = findViewById(R.id.editAddress);
        editAddress.setText("");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabLogin);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Connecting2Server connecting2Server = new Connecting2Server(MainActivity.this);
                connecting2Server.setOnCallBack(new Connecting2Server.CallBackTask() {
                    @Override
                    public void CallBack(Boolean result) {

                    }
                });
                String user = editMailAddr.getText().toString();
                String pass = editPassword.getText().toString();
                connecting2Server.execute(ACCESS_TYPE_LOGIN, user, pass);

//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Get text from inputstream.
     * @param is InputStream
     * @return Text
     * @throws IOException Exception
     */
    private static String inputStreamToString(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        StringBuilder result = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            result.append(line);
        }

        if (result.length() == 0) {
            return null;
        } else {
            return result.toString();
        }
    }


    private static int AccessServer(Context context, String accessType,String id, String mailAddress, String password) {
        try {
            HttpURLConnection con = null;
            URL url = null;

            if (String.valueOf(accessType).equals(ACCESS_TYPE_LOGIN)) {
                String url_sv = "http://" + SERVER_URL + "/account?mail_address=" + mailAddress + "&password=" + password;

                // URLの作成
                url = new URL(url_sv);
                // 接続用HttpURLConnectionオブジェクト作成
                con = (HttpURLConnection) url.openConnection();
                // リクエストメソッドの設定
                con.setRequestMethod("GET");
                // リダイレクトを自動で許可しない設定
                con.setInstanceFollowRedirects(false);
                // URL接続からデータを読み取る場合はtrue
                con.setDoInput(true);
                // URL接続にデータを書き込む場合はtrue
                con.setDoOutput(false);

                // 接続
                con.connect();
                int responsCode = con.getResponseCode();
                if (responsCode == 200) {
                    JSONArray jsonArray;
                    InputStream in = con.getInputStream();
                    String jsonText = inputStreamToString(in);
                    jsonArray = new JSONArray(jsonText);
                    JSONObject json = jsonArray.getJSONObject(0);

                    Log.i(TAG, "json data:" + json.toString());

                    if (jsonText.length() > 0) {
                        String rtn = json.getString("id");
                        return Integer.valueOf(rtn);
                    }
                } else {
                    return -1;
                }
            } else if (String.valueOf(accessType).equals(ACCESS_TYPE_PROFILE)) {
                String url_sv = "http://" + SERVER_URL + "/profile?account_id=" + id;

                // URLの作成
                url = new URL(url_sv);
                // 接続用HttpURLConnectionオブジェクト作成
                con = (HttpURLConnection) url.openConnection();
                // リクエストメソッドの設定
                con.setRequestMethod("GET");
                // リダイレクトを自動で許可しない設定
                con.setInstanceFollowRedirects(false);
                // URL接続からデータを読み取る場合はtrue
                con.setDoInput(true);
                // URL接続にデータを書き込む場合はtrue
                con.setDoOutput(false);

                // 接続
                con.connect();
                int responsCode = con.getResponseCode();
                if (responsCode == 200) {
                    JSONArray jsonArray;
                    InputStream in = con.getInputStream();
                    String jsonText = inputStreamToString(in);
                    jsonArray = new JSONArray(jsonText);
                    JSONObject json = jsonArray.getJSONObject(0);

                    Log.i(TAG, "json data:" + json.toString());

                    if (jsonText.length() > 0) {
                        String name = json.getString("name");
                        String address = json.getString("address");
                        editFullName.setText(name);
                        editAddress.setText(address);
                    }
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "AccessServer() exception:" + e.getMessage());
            return -1;
        }
        return -1;
    }

    /**
     * サーバへの問い合わせ非同期クラス
     */
    private static class Connecting2Server extends AsyncTask <String, Integer, Boolean> {
        private Context mContext;
        private ProgressDialog mProgressDialog;
        private Boolean isShowProgress;
        private CallBackTask callbacktask;
        private int id = 0;

        /**
         * コンストラクタ
         * @param context
         */
        public Connecting2Server(Context context) {
            mContext = context;
        }

        /**
         * getIsShowProgress
         * プログレスが表示中かどうかを返却
         * @return
         */
        public Boolean getIsShowProgress() {
            return isShowProgress;
        }

        /**
         * onPreExecute
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isShowProgress = true;
            showDialog();
        }

        /**
         * doInBackground
         * @param params
         * @return
         */
        @Override
        protected Boolean doInBackground(String... params) {
            id = AccessServer(mContext, params[0], "", params[1], params[2]);
            AccessServer(mContext, ACCESS_TYPE_PROFILE, String.valueOf(id), "", "");
            return null;
        }

        /**
         * onPostExecute
         * @param result
         */
        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            dismissDialog();
            isShowProgress = false;
            callbacktask.CallBack(result);

            if (id > 0) {
                Toast.makeText(mContext, "認証成功", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(mContext, "エラー", Toast.LENGTH_LONG).show();
            }
        }

        /**
         * showDialog
         * ダイアログ表示
         */
        public void showDialog() {
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setMessage(mContext.getResources().getString(R.string.connect_Server));
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.show();
        }

        /**
         * dismissDialog
         */
        public void dismissDialog() {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }

        /**
         *
         * @param callBackTask
         */
        public void setOnCallBack(CallBackTask callBackTask) {
            callbacktask = callBackTask;
        }

        /**
         * コールバック用のstaticなclass
         */
        public static class CallBackTask {
            public void CallBack(Boolean result) {
            }
        }
    }
}

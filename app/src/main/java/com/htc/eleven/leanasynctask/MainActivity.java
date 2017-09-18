package com.htc.eleven.leanasynctask;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    private TextView tv;
    private ProgressBar bar;
    private Handler handler;

    private final String TAG = "eleven-AsyncTask";
    private boolean DEBUG = true;

    public static final int HANDLER_SET_MAX = 0;
    public static final int HANDLER_UPDATE_VALUE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bar = (ProgressBar) findViewById(R.id.progress_bar);
        bar.setMin(0);

        tv = (TextView) findViewById(R.id.tv);

        findViewById(R.id.readContent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readWebContent("https://www.sina.com.cn");
            }
        });

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Bundle bundle = msg.getData();

                if(DEBUG)
                    Log.i(TAG, "got message, msg.what ==> " + msg.what + ": " + bundle.getInt("data"));
                switch (msg.what) {
                    case HANDLER_SET_MAX:
                        bar.setMax(bundle.getInt("data"));
                        bar.setVisibility(ProgressBar.VISIBLE);
                        break;
                    case HANDLER_UPDATE_VALUE:
                        bar.setProgress(bundle.getInt("data"));
                        break;
                }
            }
        };
    }
    private void readWebContent(final String s) {
        new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... strings) {
                try {
                    URL url = new URL(strings[0]);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestProperty("Accept-Encoding", "identity");
//                    connection.connect();

                    int size = connection.getContentLength();
//                    int size = 136218;

                    Log.i(TAG, "Total length: " + size);

                    Bundle data = new Bundle();
                    data.putInt("data", size);
                    Message msg = new Message();
                    msg.what = MainActivity.HANDLER_SET_MAX;
                    msg.setData(data);
                    handler.sendMessage(msg);

                    InputStreamReader inputstreamreader = new InputStreamReader(connection.getInputStream());
                    BufferedReader br = new BufferedReader(inputstreamreader);

                    String line;
                    StringBuilder builder = new StringBuilder();

                    while ((line = br.readLine()) != null) {
                        builder.append(line);
                        publishProgress(builder.toString().length());

//                        Thread.sleep(1000);

                    }
                    br.close();
                    inputstreamreader.close();

                    return builder.toString();
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                tv.setText(s);

                if(DEBUG)
                    Log.i(TAG, "Total read: " + s.length());
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Toast.makeText(MainActivity.this, "开始读取!", Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);

                Log.i(TAG, "update +++");
                Bundle data = new Bundle();
                data.putInt("data", values[0].intValue());

                Message msg = new Message();
                msg.what = MainActivity.HANDLER_UPDATE_VALUE;
                msg.setData(data);
                handler.sendMessage(msg);
            }
        }.execute(s);
    }
}

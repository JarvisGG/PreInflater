package jarvis.com.app;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.AsyncLayoutInflater;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.jarvis.android.preinflater.App$R2InflaterMapper;

import jarvis.com.preinflater.AsyncWrapperLayoutInflater;
import jarvis.com.preinflater.PreInflaterManager;
import jarvis.com.preinflater.annotation.PreInflater;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.preinflater).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreInflaterManager.$.init(MainActivity.this);
            }
        });

        findViewById(R.id.test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test1();
                test2();
            }
        });
    }


    private void test1() {
        long startTime = System.currentTimeMillis();
        AsyncWrapperLayoutInflater.getInstance(this).inflater(R.layout.activity_first);
        Log.e("preInflater", " ------------------> " + (System.currentTimeMillis() - startTime));
    }

    private void test2() {
        long startTime = System.currentTimeMillis();
        LayoutInflater.from(this).inflate(R.layout.activity_first, null, false);
        Log.e("preInflater", " ------------------> " + (System.currentTimeMillis() - startTime));
    }
}

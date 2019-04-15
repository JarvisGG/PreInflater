package jarvis.com.app;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.AsyncLayoutInflater;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import jarvis.com.preinflater.annotation.PreInflater;

/**
 * @author yyf @ Zhihu Inc.
 * @since 04-02-2019
 */
@PreInflater(layout = R2.layout.activity_first, scheduler = "main")
public class FristActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new AsyncLayoutInflater(this).inflate(jarvis.com.app.R.layout.activity_first, new FrameLayout(this), new AsyncLayoutInflater.OnInflateFinishedListener() {
            @Override
            public void onInflateFinished(@NonNull View view, int i, @Nullable ViewGroup viewGroup) {
                setContentView(view);
            }
        });
    }
}

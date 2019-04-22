package jarvis.com.testmodule2;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import jarvis.com.preinflater.annotation.PreInflater;

/**
 * @author yyf @ Zhihu Inc.
 * @since 04-16-2019
 */
@PreInflater(layout = R2.layout.activity_module_2_frist, scheduler = "main")
public class Module2FristActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_2_frist);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
    }

}

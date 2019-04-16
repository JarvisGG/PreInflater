package jarvis.com.testmodule2;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import jarvis.com.preinflater.annotation.PreInflater;

/**
 * @author yyf @ Zhihu Inc.
 * @since 04-16-2019
 */
@PreInflater(layout = R2.layout.activity_module_2_frist, scheduler = "main")
public class Module2FristActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_2_frist);
    }
}

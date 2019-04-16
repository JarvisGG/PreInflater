package jarvis.com.testmodule3;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import jarvis.com.preinflater.annotation.PreInflater;

/**
 * @author yyf @ Zhihu Inc.
 * @since 04-16-2019
 */
@PreInflater(layout = R2.layout.activity_module_3_frist, scheduler = "io")
public class Module3FristActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_3_frist);
    }
}

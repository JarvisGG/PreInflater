package jarvis.com.app;

import android.app.Application;

import jarvis.com.preinflater.PreInflaterManager;

/**
 * @author yyf @ Zhihu Inc.
 * @since 04-18-2019
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PreInflaterManager.$.init(this);
    }
}

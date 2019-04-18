package jarvis.com.preinflater;

import android.content.Context;
import android.content.ContextWrapper;
import android.support.annotation.LayoutRes;
import android.support.v4.view.AsyncLayoutInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import java.util.HashMap;

/**
 * @author yyf @ Zhihu Inc.
 * @since 04-02-2019
 */
public class AsyncWrapperLayoutInflater {

    private static AsyncWrapperLayoutInflater sInstance;

    private AsyncLayoutInflater mAsyncInflater;

    private LayoutInflater mInflater;

    private final HashMap<Integer, View> mViewPool = new HashMap<>();


    public static AsyncWrapperLayoutInflater getInstance(Context context) {
        if (sInstance == null) {
            synchronized (AsyncWrapperLayoutInflater.class) {
                if (sInstance == null) {
                    sInstance = new AsyncWrapperLayoutInflater(context);
                }
            }
        }
        return sInstance;
    }

    private AsyncWrapperLayoutInflater(Context context) {
        mAsyncInflater = new AsyncLayoutInflater(context);
        mInflater = LayoutInflater.from(context);
    }

    public void preInflater(@LayoutRes int layoutRes) {
        mAsyncInflater.inflate(layoutRes, null, (view, layoutId, viewGroup) -> mViewPool.put(layoutId, view));
    }

    public View inflater(@LayoutRes int layoutRes) {

        View view = mViewPool.get(layoutRes);
        if (view != null) {
            return view;
        }

        view = mInflater.inflate(layoutRes, null, false);
        mViewPool.put(layoutRes, view);

        return view;
    }

    public Context wrapContext(Context newBase) {
        Log.e("asm-execute -> ", " newBase !! ");
        return new PreInflaterWrapper(newBase);
    }

    private static class PreInflaterWrapper extends ContextWrapper {

        public PreInflaterWrapper(Context base) {
            super(base);
        }
    }
}

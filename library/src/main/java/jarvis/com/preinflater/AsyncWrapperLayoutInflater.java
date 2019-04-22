package jarvis.com.preinflater;

import android.content.Context;
import android.content.ContextWrapper;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.view.AsyncLayoutInflater;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * @author yyf @ Zhihu Inc.
 * @since 04-02-2019
 */
public class AsyncWrapperLayoutInflater {

    private static final String TAG = "AsyncWrapperLayoutInflater";

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
            mViewPool.remove(layoutRes);
            return view;
        }

        view = mInflater.inflate(layoutRes, null, false);

        return view;
    }

    public Context wrapContext(Context newBase) {
        Log.e("asm-execute -> ", " newBase !! ");
        return new PreInflaterWrapper(newBase);
    }

    private class PreInflaterWrapper extends ContextWrapper {

        private WrapperLayoutInflater inflater;

        public PreInflaterWrapper(Context base) {
            super(base);
        }

        @Override
        public Object getSystemService(String name) {
            if (LAYOUT_INFLATER_SERVICE.equals(name)) {
                if (inflater == null) {
                    inflater = new WrapperLayoutInflater((LayoutInflater) super.getSystemService(name), this);
                }
                return inflater;
            }
            return super.getSystemService(name);
        }
    }

    private class WrapperLayoutInflater extends LayoutInflater {

        private LayoutInflater original;

        WrapperLayoutInflater(LayoutInflater original, Context newContext) {
            super(original, newContext);
            this.original = original;
        }

        @Override
        public LayoutInflater cloneInContext(Context newContext) {
            return new WrapperLayoutInflater(original.cloneInContext(newContext), newContext);
        }

        @Override
        public void setFactory(Factory factory) {
            super.setFactory(factory);
            original.setFactory(factory);
        }

        @Override
        public void setFactory2(Factory2 factory) {
            super.setFactory2(factory);
            original.setFactory2(factory);

            if ((getContext() instanceof Factory2)) {
                forceSetPrivateFactory(original, (Factory2) getContext());
            }
        }

        @Override
        public View inflate(int resource, @Nullable ViewGroup root, boolean attachToRoot) {


            View view = mViewPool.get(resource);
            if (view != null) {
                Log.e("asm-resource -> ", resource + " !! ");
                if (root != null && attachToRoot) {
                    root.addView(view);
                }
                mViewPool.remove(resource);
                return view;
            }

            view = mInflater.inflate(resource, root, attachToRoot);

            return view;
        }
    }


    private static Field sPrivateFactoryField;
    private static boolean sCheckedPrivateFactoryField;

    private static void forceSetPrivateFactory(LayoutInflater inflater, LayoutInflater.Factory2 factory) {
        if (!sCheckedPrivateFactoryField) {
            try {
                sPrivateFactoryField = LayoutInflater.class.getDeclaredField("mPrivateFactory");
                sPrivateFactoryField.setAccessible(true);
            } catch (NoSuchFieldException e) {
            }
            sCheckedPrivateFactoryField = true;
        }
        if (sPrivateFactoryField != null) {
            try {
                LayoutInflater.Factory2 origin = (LayoutInflater.Factory2) sPrivateFactoryField.get(inflater);
                if (origin == null) {
                    sPrivateFactoryField.set(inflater, factory);
                } else {
                    sPrivateFactoryField.set(inflater, new LayoutInflater.Factory2() {

                        @Override
                        public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
                            View view = factory.onCreateView(parent, name, context, attrs);
                            if (view != null) {
                                return view;
                            }
                            return origin.onCreateView(parent, name, context, attrs);
                        }

                        @Override
                        public View onCreateView(String name, Context context, AttributeSet attrs) {
                            View view = factory.onCreateView(name, context, attrs);
                            if (view != null) {
                                return view;
                            }
                            return origin.onCreateView(name, context, attrs);
                        }
                    });
                }

            } catch (IllegalAccessException e) {
            }
        }
    }
}

package jarvis.com.preinflater;

import android.content.Context;
import android.support.annotation.IdRes;
import android.util.Log;
import android.util.Pair;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;
import java8.util.stream.StreamSupport;

import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * @author yyf @ Zhihu Inc.
 * @since 03-30-2019
 */
public enum PreInflaterManager {
    /**
     * PreInflaterManager 处理预加载
     */
    $;

    public static Pair<String, Scheduler> IOParams = new Pair<>("io", Schedulers.io());
    public static Pair<String, Scheduler> MAINParams = new Pair<>("main", AndroidSchedulers.mainThread());

    public List<Integer> sPreInflateInfoOnIo = new ArrayList<>();
    public List<Integer> sPreInflateInfoOnMain = new ArrayList<>();

    public static class PreInflateInfo {
        public @IdRes int layout;
        public String scheduler;

        public PreInflateInfo(@IdRes int layout, String scheduler) {
            this.layout = layout;
            this.scheduler = scheduler;
        }
    }

    public void init(Context context) {

        _collectPreInflater();
        // 开始预加载
        executePreInflater(context);
    }

    /**
     * asm 织入代码
     */
    public void _collectPreInflater() {

    }

    /**
     * 开始预加载
     * @param context
     */
    public void executePreInflater(Context context) {

        AsyncWrapperLayoutInflater inflater = AsyncWrapperLayoutInflater.getInstance(context);

        Completable.fromRunnable(() -> StreamSupport
                .stream(sPreInflateInfoOnMain)
                .distinct()
                .forEach(res -> inflater.preInflater(res)))
                .subscribeOn(MAINParams.second)
                .subscribe();

        Completable.fromRunnable(() -> StreamSupport
                .stream(sPreInflateInfoOnIo)
                .distinct()
                .forEach(inflater::preInflater))
                .subscribeOn(IOParams.second)
                .subscribe();
    }

    public void addPreInflateInfo(PreInflateInfo info) {
        if (IOParams.first.equals(info.scheduler)) {
            sPreInflateInfoOnIo.add(info.layout);
        } else if (MAINParams.first.equals(info.scheduler)) {
            sPreInflateInfoOnMain.add(info.layout);
        }
    }
}

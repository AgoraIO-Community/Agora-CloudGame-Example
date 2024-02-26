package io.agora.cloudgame.utils;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import io.agora.cloudgame.example.R;
import me.add1.iris.PageDelegate;
import me.add1.iris.utilities.ThreadUtils;


public class NavigationUtils {

    public static void enterNewFragment(@NonNull FragmentManager manager,
                                        @NonNull PageDelegate.DelegateFragment to, int viewId) {
        if (manager == null) {
            return;
        }
        ThreadUtils.postOnUiThread(() -> {
            FragmentTransaction ft = manager.beginTransaction();
            ft.setCustomAnimations(R.anim.fragment_slide_left_enter,
                    R.anim.fragment_slide_left_exit,
                    R.anim.fragment_slide_right_enter,
                    R.anim.fragment_slide_right_exit);
            ft.replace(viewId, to, PageDelegate.TAG_MAIN);
            ft.addToBackStack(null);
            ft.commit();
        });
    }

    public static void enterNewFragment(@NonNull FragmentManager manager,
                                        @NonNull Fragment to, int viewId) {
        ThreadUtils.postOnUiThread(() -> {
            FragmentTransaction ft = manager.beginTransaction();
            ft.setCustomAnimations(R.anim.fade_in_center, 0, 0,
                    R.anim.fade_out_center);
            ft.replace(viewId, to, PageDelegate.TAG_MAIN);
            ft.addToBackStack(null);
            ft.commit();
        });
    }

    public static void enterNewFragmentPost(@NonNull FragmentManager manager,
                                            @NonNull Fragment to) {
        ThreadUtils.postOnUiThread(() -> {
            FragmentTransaction ft = manager.beginTransaction();
            ft.setCustomAnimations(R.anim.dialog_enter, 0, 0,
                    R.anim.dialog_exit);
            ft.replace(android.R.id.content, to, PageDelegate.TAG_MAIN);
            ft.addToBackStack(null);
            ft.commit();
        });
    }

}

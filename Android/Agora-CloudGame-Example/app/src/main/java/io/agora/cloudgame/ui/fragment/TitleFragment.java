package io.agora.cloudgame.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;

import java.util.Objects;

import io.agora.cloudgame.example.databinding.FragmentTitleBinding;
import io.agora.cloudgame.utils.ViewUtils;
import me.add1.iris.PageDelegate;

public class TitleFragment extends PageDelegate.DelegateFragment {

    private PageDelegate mDelegate;

    public static TitleFragment newInstance(@NonNull PageDelegate delegate) {
        TitleFragment fragment = new TitleFragment(delegate);
        delegate.mHost = new PageDelegate.Host() {
            @Override
            public Context getContext() {
                return fragment.getContext();
            }

            @Override
            public void close() {
                assert getFragmentManager() != null;
                if (getFragmentManager().findFragmentByTag(PageDelegate.TAG_MAIN) == fragment) {
                    Objects.requireNonNull(fragment.getActivity()).getOnBackPressedDispatcher().onBackPressed();
                }
            }

            @Override
            public void setTitle(String title) {
                if (fragment.mBinding != null) {
                    fragment.mBinding.toolbar.setTitle(title);
                }
            }

            @Nullable
            @Override
            public Lifecycle getLifecycle() {
                return fragment.getLifecycle();
            }

            @Override
            public boolean isStateSaved() {
                return fragment.isStateSaved();
            }

            @Nullable
            @Override
            public FragmentManager getFragmentManager() {
                return fragment.getFragmentManager();
            }

            @Override
            public FragmentManager getChildFragmentManager() {
                return fragment.getChildFragmentManager();
            }

            @Nullable
            @Override
            public FragmentManager getRootFragmentManager() {
                return fragment.getActivity().getSupportFragmentManager();
            }

            @Override
            public View getView() {
                return fragment.getView();
            }

            @Override
            public void startActivityForResult(@NonNull Intent intent, int requestCode) {
                fragment.startActivityForResult(intent, requestCode);
            }

            @Override
            public void startActivity(@NonNull Intent intent) {
                fragment.startActivity(intent);
            }
        };
        return fragment;
    }

    public TitleFragment() {
        super();
    }

    protected TitleFragment(@NonNull PageDelegate delegate) {
        super(delegate);
        mDelegate = delegate;
    }

    protected FragmentTitleBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = FragmentTitleBinding.inflate(inflater, container, false);
        View view = super.onCreateView(inflater, mBinding.delegateContent, savedInstanceState);
        mBinding.delegateContent.addView(view);
        if (getActivity() instanceof AppCompatActivity) {

            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.setSupportActionBar(mBinding.toolbar);
            ActionBar actionBar = activity.getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);

            mBinding.toolbar.setNavigationOnClickListener(v -> {
                if (mDelegate.onBackPressed()) {
                    return;
                }
                activity.onBackPressed();
            });
        }

        mBinding.toolbar.setTitle(mDelegate.getTitle(getContext()));
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mBinding.toolbarView.getLayoutParams();
        layoutParams.height = ViewUtils.getStatusBarHeight(getContext());
        mBinding.toolbarView.setLayoutParams(layoutParams);
    }

    @Override
    public void onDestroyView() {
        mBinding.unbind();
        mBinding = null;
        super.onDestroyView();
    }
}

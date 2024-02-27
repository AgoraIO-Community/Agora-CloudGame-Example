package me.add1.iris;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;

public abstract class PageDelegate {
    public static final String TAG_MAIN = "main";

    public interface Host {
        Context getContext();

        void close();

        void setTitle(String title);

        boolean isStateSaved();

        @Nullable
        FragmentManager getChildFragmentManager();

        @Nullable
        FragmentManager getFragmentManager();

        @Nullable
        FragmentManager getRootFragmentManager();

        @Nullable
        View getView();

        @Nullable
        Lifecycle getLifecycle();

        void startActivityForResult(@NonNull Intent intent, int requestCode);

        void startActivity(@NonNull Intent intent);

    }

    private boolean mIsAlive;
    private boolean mActive = false;
    public Host mHost;

    protected void onCreate(@Nullable Bundle savedInstanceState) {

    }

    protected void onDestroy() {
    }

    protected void onStart() {
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    }

    public void startActivityForResult(@NonNull Intent intent, int requestCode) {

        if (mHost == null) {
            if (Check.ON) Check.shouldNeverHappen();
            return;
        }
        mHost.startActivityForResult(intent, requestCode);
    }

    public void startActivity(@NonNull Intent intent) {
        if (mHost == null) {
            if (Check.ON) Check.shouldNeverHappen();
            return;
        }
        mHost.startActivity(intent);
    }

    @Nullable
    public AppCompatActivity getActivity() {
        if (getContext() != null) {
            return (AppCompatActivity) getContext();
        }
        return null;
    }

    protected abstract View onCreateView(LayoutInflater inflater, ViewGroup container,
                                         Bundle savedInstanceState);

    @CallSuper
    protected void onDestroyView() {
        mIsAlive = false;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

    }

    public boolean hasOptionsMenu() {
        return false;
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return false;
    }

    @CallSuper
    protected void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mIsAlive = true;
    }

    @CallSuper
    protected void onSaveInstanceState(@NonNull Bundle outState) {
    }

    @Nullable
    public Context getContext() {
        if (mHost == null) return null;
        return mHost.getContext();
    }

    @Nullable
    public Lifecycle getLifecycle() {
        if (mHost == null) return null;
        return mHost.getLifecycle();
    }

    @Nullable
    public FragmentManager getChildFragmentManager() {
        if (mHost == null) return null;
        return mHost.getChildFragmentManager();
    }

    @Nullable
    public FragmentManager getFragmentManager() {
        if (mHost == null) return null;
        return mHost.getFragmentManager();
    }

    @Nullable
    public FragmentManager getRootFragmentManager() {
        if (mHost == null) return null;
        return mHost.getRootFragmentManager();
    }

    @Nullable
    public View getView() {
        if (mHost == null) return null;
        return mHost.getView();
    }

    @CallSuper
    protected void onActive() {
        mActive = true;
    }

    @CallSuper
    protected void onInactive() {
        mActive = false;
    }

    public boolean isActive() {
        return mActive;
    }

    public boolean handleBack(boolean backButton) {
        return false;
    }

    public boolean isAlive() {
        if (mHost == null) return false;
        return mHost.getContext() == null || mHost.isStateSaved() ? false : mIsAlive;
    }

    public boolean onBackPressed() {
        return false;
    }

    @Nullable
    public String getTitle(@NonNull Context context) {
        return null;
    }

    @Nullable
    public void updateTitle(@NonNull String title) {
        if (mHost != null) mHost.setTitle(title);
    }

    @Nullable
    public String getString(@StringRes int res) {
        if (mHost == null || mHost.getContext() == null) return null;
        return mHost.getContext().getString(res);
    }

    public void showToast(@StringRes int res) {
        if (getContext() == null) {
            return;
        }
        Toast.makeText(getContext(), res, Toast.LENGTH_SHORT).show();
    }

    public void showToast(@NonNull String message) {
        if (getContext() == null) {
            return;
        }
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    public void showToastLong(@NonNull String message) {
        if (getContext() == null) {
            return;
        }
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    public void finish() {
        if (mHost == null) return;
        mHost.close();
    }

    protected String getOrigin() {
        return null;
    }

    @Nullable
    Host getHost() {
        return mHost;
    }

    @SuppressLint("ValidFragment")
    public static class DelegateFragment extends Fragment {

        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            mDelegate.onActivityResult(requestCode, resultCode, data);
            super.onActivityResult(requestCode, resultCode, data);
        }


        @NonNull
        protected PageDelegate mDelegate;

        public static DelegateFragment newInstance(PageDelegate delegate) {
            DelegateFragment fragment = new DelegateFragment(delegate);
            delegate.mHost = new Host() {
                @Override
                public Context getContext() {
                    return fragment.getContext();
                }

                @Nullable
                @Override
                public Lifecycle getLifecycle() {
                    return fragment.getLifecycle();
                }

                @Override
                public void close() {
                    if (getFragmentManager().findFragmentByTag(TAG_MAIN) == fragment) {
                        fragment.getActivity().getOnBackPressedDispatcher().onBackPressed();
                    }
                }

                @Override
                public void setTitle(String title) {

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

        public DelegateFragment() {
            super();
        }

        @SuppressLint("ValidFragment")
        protected DelegateFragment(PageDelegate delegate) {
            super();
            mDelegate = delegate;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mDelegate.onCreate(savedInstanceState);
        }

        @Override
        public void onSaveInstanceState(@NonNull Bundle outState) {
            super.onSaveInstanceState(outState);
            mDelegate.onSaveInstanceState(outState);
        }

        @Override
        public void onDestroy() {
            mDelegate.onDestroy();
            super.onDestroy();
        }

        @Override
        public void onDestroyView() {
            mDelegate.onDestroyView();
            super.onDestroyView();
        }

        @Override
        public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
            mDelegate.onCreateOptionsMenu(menu, inflater);
            super.onCreateOptionsMenu(menu, inflater);
        }

        @Override
        public void onPrepareOptionsMenu(@NonNull Menu menu) {
            super.onPrepareOptionsMenu(menu);
        }

        @Override
        public boolean onOptionsItemSelected(@NonNull MenuItem item) {
            if (mDelegate.onOptionsItemSelected(item)) return true;
            return super.onOptionsItemSelected(item);
        }

        public String getOrigin() {
            return mDelegate.getOrigin();
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            return mDelegate.onCreateView(inflater, container, savedInstanceState);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            setHasOptionsMenu(true);
            mDelegate.onViewCreated(view, savedInstanceState);
        }

        public boolean onBackPressed() {
            return mDelegate.onBackPressed();
        }

        @Override
        public void onResume() {
            super.onResume();
            mDelegate.onActive();
        }

        @Override
        public void onStart() {
            super.onStart();
            mDelegate.onStart();
        }

        @Override
        public void onPause() {
            mDelegate.onInactive();
            super.onPause();
        }
    }
}

package io.agora.cloudgame.delegate;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.agora.cloudgame.KeyCenter;
import io.agora.cloudgame.example.R;
import io.agora.cloudgame.example.databinding.DelegateGameGoBinding;
import io.agora.cloudgame.network.model.GameEntity;
import io.agora.cloudgame.utilities.NavigationUtils;
import io.agora.cloudgame.widget.ViewJudge;
import me.add1.iris.PageDelegate;

public class GameGoDelegate extends PageDelegate {

    protected DelegateGameGoBinding mBinding;

    @Nullable
    @Override
    public String getTitle(@NonNull Context context) {
        return mEntity.name;
    }

    private final GameEntity mEntity;

    private boolean isLiveSelect = true;

    public GameGoDelegate(GameEntity entity) {
        mEntity = entity;
    }

    @Override
    protected View onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState) {
        mBinding = DelegateGameGoBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (null != getActivity()) {
            getActivity().findViewById(R.id.version).setVisibility(View.GONE);
        }
        mBinding.newText.setText(mEntity.name);

        mBinding.channelText.setFilters(new InputFilter[]{new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                Pattern p = Pattern.compile("[0-9a-zA-Z]+");
                Matcher m = p.matcher(source.toString());
                if (!m.matches()) {
                    return "";
                }
                return null;
            }
        }});

        mBinding.liveView.setImageDrawable(Objects.requireNonNull(getContext()).getResources().getDrawable(isLiveSelect ? R.drawable.switch_open : R.drawable.switch_close, null));

        mBinding.setHandler(v -> {
            switch (v.getId()) {
                case R.id.live_view:
                    isLiveSelect = !isLiveSelect;
                    mBinding.liveView.setImageDrawable(Objects.requireNonNull(getContext()).getResources().getDrawable(isLiveSelect ? R.drawable.switch_open : R.drawable.switch_close, null));
                    break;
                case R.id.join_view:
                    mEntity.roomId = mBinding.channelText.getText().toString();
                    if (TextUtils.isEmpty(mEntity.roomId)) {
                        showToast("请输入频道名称");
                        return;
                    }
                    mEntity.uid = KeyCenter.getLiveUid();
                    ViewJudge.INSTANCE.hideKeyboard(Objects.requireNonNull(getActivity()));
                    //NavigationUtils.enterNewFragment(Objects.requireNonNull(getRootFragmentManager()), PageDelegate.DelegateFragment.newInstance(new GameDetailsDelegate(mEntity, isLiveSelect)), R.id.content);
                    NavigationUtils.enterNewFragment(Objects.requireNonNull(getRootFragmentManager()), PageDelegate.DelegateFragment.newInstance(new GameDetailsWebViewDelegate(mEntity, isLiveSelect)), R.id.content);
                    break;
                default:
                    break;
            }
        });
    }
}

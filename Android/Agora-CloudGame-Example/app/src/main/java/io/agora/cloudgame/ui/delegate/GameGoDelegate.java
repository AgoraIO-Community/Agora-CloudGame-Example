package io.agora.cloudgame.ui.delegate;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.agora.cloudgame.context.GameDataContext;
import io.agora.cloudgame.example.BuildConfig;
import io.agora.cloudgame.example.R;
import io.agora.cloudgame.example.databinding.DelegateGameGoBinding;
import io.agora.cloudgame.ui.widget.ViewJudge;
import io.agora.cloudgame.utils.NavigationUtils;
import me.add1.iris.PageDelegate;

public class GameGoDelegate extends PageDelegate {

    protected DelegateGameGoBinding mBinding;

    @Nullable
    @Override
    public String getTitle(@NonNull Context context) {
        return GameDataContext.getInstance().getGameEntity().name;
    }

    private boolean isLiveSelect = true;
    private boolean isNativeRtc = true;

    public GameGoDelegate() {
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
        mBinding.gameName.setEnabled(false);
        mBinding.gameName.setText(GameDataContext.getInstance().getGameEntity().name);

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

        mBinding.rtcTypeLayout.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                isNativeRtc = checkedId == R.id.native_rtc_radio;
            }
        });
        mBinding.setHandler(v -> {
            switch (v.getId()) {
                case R.id.live_view:
                    isLiveSelect = !isLiveSelect;
                    mBinding.liveView.setImageDrawable(Objects.requireNonNull(getContext()).getResources().getDrawable(isLiveSelect ? R.drawable.switch_open : R.drawable.switch_close, null));
                    break;

                case R.id.join_view:
                    if (TextUtils.isEmpty(mBinding.channelText.getText().toString())) {
                        showToast("请输入频道名称");
                        return;
                    } else if (TextUtils.isEmpty(mBinding.uidView.getText().toString())) {
                        showToast("请输入uid");
                        return;
                    }
                    GameDataContext.getInstance().setChannelName(mBinding.channelText.getText().toString());
                    int uid = 0;
                    try {
                        uid = Integer.parseInt(mBinding.uidView.getText().toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (0 != uid) {
                        if (isLiveSelect) {
                            GameDataContext.getInstance().setBroadcastUid(uid);
                        } else {
                            GameDataContext.getInstance().setAudienceUid(uid);
                        }
                    }
                    GameDataContext.getInstance().setRoomId(BuildConfig.APP_ID + "_" + mBinding.channelText.getText().toString());

                    ViewJudge.INSTANCE.hideKeyboard(Objects.requireNonNull(getActivity()));
                    if (isNativeRtc) {
                        NavigationUtils.enterNewFragment(Objects.requireNonNull(getRootFragmentManager()), PageDelegate.DelegateFragment.newInstance(new GameDetailsFrameLayoutDelegate(isLiveSelect)), R.id.content);
                    } else {
                        NavigationUtils.enterNewFragment(Objects.requireNonNull(getRootFragmentManager()), PageDelegate.DelegateFragment.newInstance(new GameDetailsWebViewDelegate(isLiveSelect)), R.id.content);
                    }
                    break;
                default:
                    break;
            }
        });
    }
}

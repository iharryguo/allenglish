package com.lws.allenglish.controller.activities;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.lws.allenglish.Constants;
import com.lws.allenglish.R;
import com.lws.allenglish.adapter.WordDetailsAdapter;
import com.lws.allenglish.base.BaseActivity;
import com.lws.allenglish.base.BaseApplication;
import com.lws.allenglish.bean.BaseWord;
import com.lws.allenglish.bean.DetailedWord;
import com.lws.allenglish.model.DetailedWordModel;
import com.lws.allenglish.model.OnDetailedWordListener;
import com.lws.allenglish.model.impl.DetailedWordModelImpl;
import com.lws.allenglish.util.PlayAudio;
import com.lws.allenglish.util.StringUtils;
import com.lws.allenglish.util.common.NetWorkUtils;
import com.lws.allenglish.util.common.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WordDetailsActivity extends BaseActivity {
    @BindView(R.id.back)
    ImageView mBack;
    @BindView(R.id.search)
    ImageView mSearch;
    @BindView(R.id.voa_english)
    TextView mWord;
    @BindView(R.id.collection)
    ImageView mCollection;
    @BindView(R.id.ph_en)
    TextView mPhen;
    @BindView(R.id.ph_am)
    TextView mPham;
    @BindView(R.id.phen_horn)
    ImageView mPhenHorn;
    @BindView(R.id.pham_horn)
    ImageView mPhamHorn;
    @BindView(R.id.word_details)
    RecyclerView mRecyclerView;

    private AnimationDrawable mPhenHornAnimation;
    private AnimationDrawable mPhamHornAnimation;

    private PlayAudio mPlayAudio;
    private WordDetailsAdapter mAdapter;
    private BaseWord mBaseWord;
    private DetailedWord mDetailedWord = new DetailedWord();
    private DetailedWordModel detailedWordModel;
    private List<AnimationDrawable> mSentenceHornAnimations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_details);
        ButterKnife.bind(this);
        mPlayAudio = new PlayAudio();
        detailedWordModel = new DetailedWordModelImpl(new CustomOnDetailedWordListener(), mContext);
        dealIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        dealIntent(intent);
    }

    private void dealIntent(Intent intent) {
        BaseWord baseWord = null;
        if (intent != null) {
            String action = intent.getAction();
            String type = intent.getType();
            if (Intent.ACTION_SEND.equals(action) && type != null) {
                if ("text/plain".equals(type)) {
                    String text = intent.getStringExtra(Intent.EXTRA_TEXT);
                    if (!TextUtils.isEmpty(text)) {
                        baseWord = new BaseWord();
                        baseWord.word = text;
                    }
                }
            }
            if (baseWord == null)
            {
                Object temp = intent.getSerializableExtra(Constants.BASE_INFO);
                if (temp instanceof BaseWord)
                    baseWord = (BaseWord) temp;
            }
        }
        if (baseWord != null) {
            mBaseWord = baseWord;
            mDetailedWord.baseWord = mBaseWord;
            detailedWordModel.setCollectedWord(mBaseWord.word);
            init();
            if (!NetWorkUtils.getNetworkTypeName(BaseApplication.getInstance()).equals(NetWorkUtils.NETWORK_TYPE_DISCONNECT)) {
                detailedWordModel.getDetailedWord(0, mBaseWord.word);
            }
        }
    }

    private void init() {
        initView();
        initRecyclerView();
    }

    private void initRecyclerView() {
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new WordDetailsAdapter(mDetailedWord, mContext, mPlayAudio, mSentenceHornAnimations);
        mRecyclerView.setAdapter(mAdapter); //  TODO: 还有嵌套recyclerview卡顿的问题待解决
        mRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (mDetailedWord.nativeADDataRef != null){
                    mAdapter.exposureAd(mDetailedWord.nativeADDataRef);
                }
            }
        });
    }

    private void initView() {
        mWord.setText(mBaseWord.word);
        updatePronounce();
        mWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterPopup(v);
            }
        });
        mCollection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mCollection.isSelected()) {
                    mCollection.setSelected(true);
                    detailedWordModel.saveCollectedWord(mBaseWord);
                } else {
                    mCollection.setSelected(false);
                    detailedWordModel.cancelCollectedWord(mBaseWord.word);
                }
            }
        });
        mPhenHorn.setBackgroundResource(R.drawable.animation_horn);
        mPhenHornAnimation = (AnimationDrawable) mPhenHorn.getBackground();
        mSentenceHornAnimations.add(mPhenHornAnimation);
        mPhenHorn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAdapter.stopAnimation();
                mPhenHornAnimation.start();
                mPlayAudio.play(BaseApplication.getInstance(), mWord.getText().toString(), 1);
            }
        });
        mPhamHorn.setBackgroundResource(R.drawable.animation_horn);
        mPhamHornAnimation = (AnimationDrawable) mPhamHorn.getBackground();
        mSentenceHornAnimations.add(mPhamHornAnimation);
        mPhamHorn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAdapter.stopAnimation();
                mPhamHornAnimation.start();
                mPlayAudio.play(BaseApplication.getInstance(), mWord.getText().toString(), 2);
            }
        });
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, SearchWordActivity.class);
                startActivity(intent);
            }
        });
    }

    private void showFilterPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        // Inflate the menu from xml
        popup.getMenuInflater().inflate(R.menu.popup_filter, popup.getMenu());
        // Setup menu item selection
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.copy:
                        StringUtils.copyToClipboard(mContext, mWord.getText().toString());
                        return true;
//                    case R.id.open_bookmark:
//                        Intent intent = new Intent(mContext, BookmarkActivity.class);
//                        startActivity(intent);
//                        return true;
                    default:
                        return false;
                }
            }
        });
        // Handle dismissal with: popup.setOnDismissListener(...);
        // Show the menu
        popup.show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.stopAnimation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPlayAudio.killMediaPlayer();
        mAdapter.stopAnimation();
    }

    private void updatePronounce() {
        String pron = "英 ";
        if (!TextUtils.isEmpty(mBaseWord.ph_en))
            pron += mBaseWord.ph_en;
        mPhen.setText(pron);

        pron = "美 ";
        if (!TextUtils.isEmpty(mBaseWord.ph_am))
            pron += mBaseWord.ph_am;
        mPham.setText(pron);
    }

    private class CustomOnDetailedWordListener implements OnDetailedWordListener {

        @Override
        public void onSuccess(DetailedWord detailedWord) {
            mDetailedWord.baesInfo = detailedWord.baesInfo;
            mDetailedWord.sentence = detailedWord.sentence;
            mDetailedWord.phrase = detailedWord.phrase;
            mDetailedWord.ee_mean = detailedWord.ee_mean;
            mDetailedWord.iflyNativeAd = detailedWord.iflyNativeAd;
            mDetailedWord.nativeADDataRef = detailedWord.nativeADDataRef;
            mAdapter.notifyDataSetChanged();

            // 用网络数据替代从数据库查询到的离线数据
            updateOfflineData(detailedWord);
        }

        private void updateOfflineData(DetailedWord detailedWord) {
            if (detailedWord.baesInfo != null) {
                DetailedWord.BaesInfoEntity info = detailedWord.baesInfo;
                if (info.symbols != null && info.symbols.size() > 0) {
                    DetailedWord.BaesInfoEntity.SymbolsEntity entity = info.symbols.get(0);
                    if (entity.parts != null) {
                        StringBuilder meansB = new StringBuilder();
                        for (int i = 0; i < entity.parts.size(); i++) {
                            DetailedWord.BaesInfoEntity.SymbolsEntity.PartsEntity parts = entity.parts.get(i);
                            meansB.append(parts.part);
                            StringBuilder meanB = new StringBuilder();
                            for (int j = 0; j < parts.means.size(); j++) {
                                meanB.append(parts.means.get(j));
                                if (j < parts.means.size() - 1)
                                    meanB.append(", ");
                            }
                            meanB.append(";");
                            if (i < entity.parts.size() - 1)
                                meanB.append("\n");
                            meansB.append(meanB.toString());
                        }
                        if (meansB.length() > 0)
                            mBaseWord.means = meansB.toString();
                    }
                    if (!TextUtils.isEmpty(entity.ph_am))
                        mBaseWord.ph_am = entity.ph_am;
                    if (!TextUtils.isEmpty(entity.ph_en))
                        mBaseWord.ph_en = entity.ph_en;
                    updatePronounce();
                }
            }
        }

        @Override
        public void onError() {
            ToastUtils.show(mContext, R.string.bad_internet);
        }

        @Override
        public void onIsCollectedWord(boolean isCollected) {
            if (isCollected) {
                mCollection.setSelected(true);
            }
        }
    }
}

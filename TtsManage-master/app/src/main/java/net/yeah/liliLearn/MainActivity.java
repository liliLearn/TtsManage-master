package net.yeah.liliLearn;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.sinovoice.hcicloudsdk.player.TTSCommonPlayer;
import com.sinovoice.hcicloudsdk.player.TTSPlayerListener;

import net.yeah.liliLearn.utils.TtsPlayUtil;
import net.yeah.liliLearn.utils.TtsUtil;

public class MainActivity extends AppCompatActivity implements TTSPlayerListener {
    private TtsPlayUtil mTtsPlayUtil;
    private TtsUtil mInitTts;
    private boolean isInitPlayer;
    private EditText mInputMsgEdit;
    private Button mPlayerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initTts();
    }
    private void initView(){
        mInputMsgEdit= (EditText) findViewById(R.id.input_msg_edit);
        mPlayerButton= (Button) findViewById(R.id.player_btn);
        mPlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                synth(mInputMsgEdit.getText().toString());
            }
        });
    }
    private void initTts(){
        // 灵云语音工具类
        mInitTts = new TtsUtil(this);
        // 语音合成能力工具类
        mTtsPlayUtil = new TtsPlayUtil(this);
        // 初始化语音合成
        isInitPlayer = mTtsPlayUtil.initPlayer(this);
    }
    @Override
    public void onPlayerEventStateChange(TTSCommonPlayer.PlayerEvent playerEvent) {

    }

    @Override
    public void onPlayerEventProgressChange(TTSCommonPlayer.PlayerEvent playerEvent, int i, int i1) {

    }

    @Override
    public void onPlayerEventPlayerError(TTSCommonPlayer.PlayerEvent playerEvent, int i) {

    }

    public void synth(String msg) {
        if (!isInitPlayer) {
            Toast.makeText(this, "语音播报初始化失败", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(msg)) {
            Toast.makeText(this, "语音播报合成内容为空", Toast.LENGTH_SHORT).show();
            return;
        }
        mTtsPlayUtil.synth(msg);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTtsPlayUtil != null) {
            mTtsPlayUtil.release();
        }
        if (null != mInitTts) {
            mInitTts.hciRelease();
        }
    }
}

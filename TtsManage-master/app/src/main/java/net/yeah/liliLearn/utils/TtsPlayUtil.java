package net.yeah.liliLearn.utils;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.sinovoice.hcicloudsdk.android.tts.player.TTSPlayer;
import com.sinovoice.hcicloudsdk.common.asr.AsrInitParam;
import com.sinovoice.hcicloudsdk.common.hwr.HwrInitParam;
import com.sinovoice.hcicloudsdk.common.tts.TtsConfig;
import com.sinovoice.hcicloudsdk.common.tts.TtsInitParam;
import com.sinovoice.hcicloudsdk.player.TTSCommonPlayer;
import com.sinovoice.hcicloudsdk.player.TTSPlayerListener;

/**
 * Created by kqw on 2016/8/12.
 * 初始化语音合成能力
 */
public class TtsPlayUtil {

    private static final String TAG = "HciUtil";
    private Activity mActivity;
    private TTSPlayer mTtsPlayer;

    public TtsPlayUtil(Activity activity) {
        mActivity = activity;
    }

    /**
     * 初始化播放器
     */
    public boolean initPlayer(TTSPlayerListener ttsPlayerListener) {
        // 构造Tts初始化的帮助类的实例
        TtsInitParam ttsInitParam = new TtsInitParam();
        // 获取App应用中的lib的路径
        String dataPath = mActivity.getBaseContext().getFilesDir().getAbsolutePath().replace("files", "lib");
        Log.e("path",dataPath);
        ttsInitParam.addParam(TtsInitParam.PARAM_KEY_DATA_PATH, dataPath);
        // 此处演示初始化的能力为tts.cloud.xiaokun, 用户可以根据自己可用的能力进行设置, 另外,此处可以传入多个能力值,并用;隔开
        ttsInitParam.addParam(AsrInitParam.PARAM_KEY_INIT_CAP_KEYS, ConfigUtil.CAP_KEY_TTS_LOCAL);
        // 如果使用本地能力，需要设置本地音库文件路径

        // 使用lib下的资源文件,需要添加android_so的标记
        ttsInitParam.addParam(HwrInitParam.PARAM_KEY_FILE_FLAG, HwrInitParam.VALUE_OF_PARAM_FILE_FLAG_ANDROID_SO);

        mTtsPlayer = new TTSPlayer();
        // 配置TTS初始化参数
        mTtsPlayer.init(ttsInitParam.getStringConfig(), ttsPlayerListener);

        return mTtsPlayer.getPlayerState() == TTSPlayer.PLAYER_STATE_IDLE;
    }


    // 云端合成,不启用编码传输(默认encode=none)
    public void synth(String text) {
        // 配置播放器的属性。包括：音频格式，音库文件，语音风格，语速等等。详情见文档。
        TtsConfig ttsConfig = new TtsConfig();
        // 音频格式
        ttsConfig.addParam(TtsConfig.BasicConfig.PARAM_KEY_AUDIO_FORMAT, "pcm16k16bit");

        // 指定语音合成的能力(云端合成,发言人是XiaoKun)
        ttsConfig.addParam(TtsConfig.SessionConfig.PARAM_KEY_CAP_KEY, ConfigUtil.CAP_KEY_TTS_LOCAL);
        //
        ttsConfig.addParam(TtsConfig.BasicConfig.PARAM_KEY_SPEED, "5");
        // property为私有云能力必选参数，公有云传此参数无效
//        ttsConfig.addParam("property", "cn_xiaokun_common");

        if (mTtsPlayer.getPlayerState() == TTSCommonPlayer.PLAYER_STATE_PLAYING || mTtsPlayer.getPlayerState() == TTSCommonPlayer.PLAYER_STATE_PAUSE) {
            mTtsPlayer.stop();
        }

        if (mTtsPlayer.getPlayerState() == TTSCommonPlayer.PLAYER_STATE_IDLE) {
            mTtsPlayer.play(text, ttsConfig.getStringConfig());
        } else {
            Toast.makeText(mActivity, "播放器内部状态错误", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 释放
     */
    public void release() {
        if (null != mTtsPlayer) {
            mTtsPlayer.release();
        }
    }

}

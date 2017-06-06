package net.yeah.liliLearn.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.sinovoice.hcicloudsdk.api.HciCloudSys;
import com.sinovoice.hcicloudsdk.common.AuthExpireTime;
import com.sinovoice.hcicloudsdk.common.HciErrorCode;
import com.sinovoice.hcicloudsdk.common.InitParam;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TtsUtil {
    private final String TAG = this.getClass().getSimpleName();
    private  InitParam initParam;
    private final boolean isSaveLog=false;

    public TtsUtil(Context context) {
        setInitParam(context);
        checkStatus(context);
    }

    public boolean checkStatus(Context context) {
        // 初始化
        int errCode = HciCloudSys.hciInit(initParam.getStringConfig(), context);
        if (errCode != HciErrorCode.HCI_ERR_NONE && errCode != HciErrorCode.HCI_ERR_SYS_ALREADY_INIT) {
            Toast.makeText(context, "hciInit error: " + HciCloudSys.hciGetErrorInfo(errCode), Toast.LENGTH_SHORT).show();
            return false;
        }

        // 获取授权/更新授权文件 :
        errCode = checkAuthAndUpdateAuth();
        if (errCode != HciErrorCode.HCI_ERR_NONE) {
            // 由于系统已经初始化成功,在结束前需要调用方法hciRelease()进行系统的反初始化
            Toast.makeText(context, "CheckAuthAndUpdateAuth error: " + HciCloudSys.hciGetErrorInfo(errCode), Toast.LENGTH_SHORT).show();
            HciCloudSys.hciRelease();
            return false;
        }

        if (isSaveLog) {
            saveLog(context.getPackageName(), initParam);
        }
        return true;
    }

    /**
     * 加载初始化信息
     *
     * @return 系统初始化参数
     */
    private void setInitParam(Context context) {
        String authDirPath = context.getFilesDir().getAbsolutePath();
        // 前置条件：无
        initParam= new InitParam();
        // 授权文件所在路径，此项必填
        initParam.addParam(InitParam.AuthParam.PARAM_KEY_AUTH_PATH, authDirPath);
        // 是否自动访问云授权,详见 获取授权/更新授权文件处注释
        initParam.addParam(InitParam.AuthParam.PARAM_KEY_AUTO_CLOUD_AUTH, "no");
        // 灵云云服务的接口地址，此项必填
        initParam.addParam(InitParam.AuthParam.PARAM_KEY_CLOUD_URL, ConfigUtil.CLOUD_URL);
        // 开发者Key，此项必填，由捷通华声提供
        initParam.addParam(InitParam.AuthParam.PARAM_KEY_DEVELOPER_KEY, ConfigUtil.DEVELOPER_KEY);
        // 应用Key，此项必填，由捷通华声提供
        initParam.addParam(InitParam.AuthParam.PARAM_KEY_APP_KEY, ConfigUtil.APP_KEY);
    }

    public void saveLog(String packageName, InitParam initparam) {
        // 配置日志参数
        String sdcardState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(sdcardState)) {
            String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            String logPath = sdPath + File.separator + "sinovoice" + File.separator + packageName + File.separator + "log" + File.separator;
            // 日志文件地址
            File fileDir = new File(logPath);
            if (!fileDir.exists()) {
                fileDir.mkdirs();
            }
            // 日志的路径，可选，如果不传或者为空则不生成日志
            initparam.addParam(InitParam.LogParam.PARAM_KEY_LOG_FILE_PATH, logPath);
            // 日志数目，默认保留多少个日志文件，超过则覆盖最旧的日志
            initparam.addParam(InitParam.LogParam.PARAM_KEY_LOG_FILE_COUNT, "5");
            // 日志大小，默认一个日志文件写多大，单位为K
            initparam.addParam(InitParam.LogParam.PARAM_KEY_LOG_FILE_SIZE, "1024");
            // 日志等级，0=无，1=错误，2=警告，3=信息，4=细节，5=调试，SDK将输出小于等于logLevel的日志信息
            initparam.addParam(InitParam.LogParam.PARAM_KEY_LOG_LEVEL, "5");
        }
    }

    private int checkAuthAndUpdateAuth() {
        // 获取系统授权到期时间
        int initResult;
        AuthExpireTime objExpireTime = new AuthExpireTime();
        initResult = HciCloudSys.hciGetAuthExpireTime(objExpireTime);
        if (initResult == HciErrorCode.HCI_ERR_NONE) {
            // 显示授权日期,如用户不需要关注该值,此处代码可忽略
            Date date = new Date(objExpireTime.getExpireTime() * 1000);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            Log.i(TAG, "expire time: " + sdf.format(date));
            if (objExpireTime.getExpireTime() * 1000 > System.currentTimeMillis()) {
                // 已经成功获取了授权,并且距离授权到期有充足的时间(>7天)
                Log.i(TAG, "checkAuth success");
                return initResult;
            }
        }
        // 获取过期时间失败或者已经过期
        initResult = HciCloudSys.hciCheckAuth();
        if (initResult == HciErrorCode.HCI_ERR_NONE) {
            Log.i(TAG, "checkAuth success");
            return initResult;
        } else {
            Log.e(TAG, "checkAuth failed: " + initResult);
            return initResult;
        }
    }

    /**
     * 释放
     */
    public void hciRelease(){
        HciCloudSys.hciRelease();
    }
}

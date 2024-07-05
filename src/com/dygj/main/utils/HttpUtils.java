package com.dygj.main.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtils {

    /**
     * 以post或get方式调用对方接口方法
     *
     * @param token
     * @param pathUrl
     * @param data
     * @param requstMethod
     * @param fakeIp
     * @return
     */
    public static String doPostOrGet(String token, String pathUrl, String data, String requstMethod, boolean fakeIp) {
        OutputStreamWriter out = null;
        BufferedReader br = null;
        String result = "";
        try {
            URL url = new URL(pathUrl);
            //打开和url之间的连接
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //请求方式
            conn.setRequestMethod(requstMethod);

            // 是否需要伪造IP
            if (fakeIp) {
                // 生成随机IP
                String randomIp = RandomIpUtils.getRandomIp();
                System.out.println("伪造IP：" + randomIp);
                // 设置伪造的IP地址
                conn.setRequestProperty("X-Forwarded-For", randomIp);
            }

            //设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            if (token != null && !"".equals(token)) {
                conn.setRequestProperty("Authorization", token);
            }
            //DoOutput设置是否向httpUrlConnection输出，DoInput设置是否从httpUrlConnection读入，此外发送post请求必须设置这两个
            conn.setDoOutput(true);
            conn.setDoInput(true);
            /**
             * 下面的三句代码，就是调用第三方http接口
             */
            //获取URLConnection对象对应的输出流
            out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
            //发送请求参数即数据
            out.write(data);
            //flush输出流的缓冲
            out.flush();
            /**
             * 下面的代码相当于，获取调用第三方http接口后返回的结果
             */
            //获取URLConnection对象对应的输入流
            InputStream is = conn.getInputStream();
            //构造一个字符流缓存
            br = new BufferedReader(new InputStreamReader(is));
            String str = "";
            while ((str = br.readLine()) != null) {
                result += str;
            }
            //关闭流
            is.close();
            //断开连接，disconnect是在底层tcp socket链接空闲时才切断，如果正在被其他线程使用就不切断。
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 获取URL输入流信息
     *
     * @param token
     * @param pathUrl
     * @param requstMethod
     * @param fakeIp
     * @return
     */
    public static InputStream getUrlInputStream(String token, String pathUrl, String requstMethod, boolean fakeIp) {
        try {
            URL url = new URL(pathUrl);
            //打开和url之间的连接
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //请求方式
            conn.setRequestMethod(requstMethod);

            // 是否需要伪造IP
            if (fakeIp) {
                // 生成随机IP
                String randomIp = RandomIpUtils.getRandomIp();
                System.out.println("伪造IP：" + randomIp);
                // 设置伪造的IP地址
                conn.setRequestProperty("X-Forwarded-For", randomIp);
            }

            //设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            if (token != null && !"".equals(token)) {
                conn.setRequestProperty("Authorization", token);
            }
            //DoOutput设置是否向httpUrlConnection输出，DoInput设置是否从httpUrlConnection读入，此外发送post请求必须设置这两个
            conn.setDoOutput(true);
            conn.setDoInput(true);
            //获取URLConnection对象对应的输入流
            return conn.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}

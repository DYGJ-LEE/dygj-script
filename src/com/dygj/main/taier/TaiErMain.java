package com.dygj.main.taier;

import com.dygj.entity.ResultVo;
import com.dygj.entity.SongVo;
import com.google.gson.Gson;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


/**
 * 客户：太二酸菜鱼视频
 * 客户id：10777
 * 需求：将所有终端名称中开头4位数字店号清掉
 *
 * @Auther:Lipf
 */
public class TaiErMain {

    static String token = "";

    public static void main(String[] args) {
        // TODO 1.获取终端列表
        List<SongVo> list = getUpdateTermList(1000, "10777", "0");

        // TODO 2.修改终端名称
//        batchUpdateTermName(list);


    }

    /**
     * 获取需要修改终端名列表
     *
     * @return
     */
    public static List<SongVo> getUpdateTermList(int pageSize, String userId, String isshow) {
        String param = "pageNo=1&pageSize=" + pageSize + "&code=&groupid=&offlinedays=&isshowdevinfo=0&name=&onlinestatus=&terminalid=&version=&productcode=&playonline=&isshow="+isshow+"&finduserid=" + userId + "&languagetype=0";
        String res = doPostOrGet("https://b6.hottermusic.com:33344/api/userterminal/list", param, "POST");
        System.out.println("getTermList：" + res);

        Gson gson = new Gson();
        ResultVo resultVo = gson.fromJson(res, ResultVo.class);
        List<SongVo> list = resultVo.data;
        List<SongVo> resultList = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            String name = list.get(i).name;
            String newName = name.replaceAll("[0-9]", "");
            // 防止终端名中出现数字
//            if(newName.length()){
//
//            }
            if (name.equals(newName)) {
                continue;
            }
            System.out.println("原终端名：" + name + "\t" + "新终端名：" + newName);

            list.get(i).name = newName;
            resultList.add(list.get(i));
        }
        System.out.println("\ngetUpdateTermList：" + gson.toJson(resultList));
        return resultList;
    }

    /**
     * 批量更新终端名
     *
     * @param list
     */
    public static void batchUpdateTermName(List<SongVo> list) {
        /**
         * id: 141985
         * name: 太二1
         * languagetype: 0
         */
        for (int i = 0; i < list.size(); i++) {
            // 添加所有文件到此文件夹
            String param = "id=" + URLEncoder.encode(list.get(i).id)
                    + "&name=" + URLEncoder.encode(list.get(i).name)
                    + "&languagetype=" + URLEncoder.encode("0");
            String result = doPostOrGet("https://b6.hottermusic.com:33344/api/userterminal/update", param, "POST");
            System.out.println("batchUpdateTermName：" + result);
        }
    }


    /**
     * 以post或get方式调用对方接口方法，
     *
     * @param pathUrl
     */
    public static String doPostOrGet(String pathUrl, String data, String requstMethod) {
        OutputStreamWriter out = null;
        BufferedReader br = null;
        String result = "";
        try {
            URL url = new URL(pathUrl);
            //打开和url之间的连接
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //请求方式
            conn.setRequestMethod(requstMethod);
            //设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            conn.setRequestProperty("Authorization", token);
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
}
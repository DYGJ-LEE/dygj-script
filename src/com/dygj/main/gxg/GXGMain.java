package com.dygj.main.gxg;

import com.dygj.entity.*;
import com.google.gson.Gson;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


/**
 * 客户：GXG
 * 客户id：10432
 * 需求：将系统中终端名称与表格名称模糊匹配，匹配的名称以表格为准同步到系统【暂缓】
 *
 * @Auther:Lipf
 */
public class GXGMain {

    static String token = "";

    public static void main(String[] args) {
        // TODO 0.读取表格数据
        List<String> tableNames = new ArrayList<>();
        String path = "C:/Users/Administrator/Desktop/example.txt";
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);

                tableNames.add(line.replaceAll("\t","").replaceAll(" ","").trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(new Gson().toJson(tableNames));



        // TODO 1.获取系统终端列表
        List<SongVo> list = getUpdateTermList(1000, "3434","10432","0", tableNames);

        // TODO 2.修改终端名称
//        batchUpdateTermName(list);


    }

    /**
     * 获取需要修改名称的终端列表
     *
     * @return
     */
    public static List<SongVo> getUpdateTermList(int pageSize, String groupid, String userId, String isshow, List<String> tableNames) {
        String param = "pageNo=1&pageSize=" + pageSize + "&code=&groupid="+groupid+"&offlinedays=&isshowdevinfo=0&name=&onlinestatus=&terminalid=&version=&productcode=&playonline=&isshow="+isshow+"&finduserid=" + userId + "&languagetype=0";
        String res = doPostOrGet("https://b6.hottermusic.com:33344/api/userterminal/list", param, "POST");
        System.out.println("getTermList：" + res);

        Gson gson = new Gson();
        ResultVo resultVo = gson.fromJson(res, ResultVo.class);
        List<SongVo> list = resultVo.data;
        List<SongVo> resultList = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            String name = list.get(i).name;
            // TODO 特殊终端名匹配处理
            if (name.contains(">")) {
                name = name.substring(name.indexOf(">")+1);
            }
            if (name.contains("-")) {
                name = name.substring(name.indexOf("-")+1);
            }
            /*根据名称关键字做处理
            例如：
                表格中（武汉市汉阳区方圆荟奥莱GXG ）系统中（武汉方圆荟奥特莱斯店）
                 福建安溪万达广场茂                       安溪万达GXG
                 溧阳上河城茂                            溧阳上河城茂店
                                               X010218-湖州首创奥特莱斯
                 宁波鄞州万达银泰百货GXG                   鄞州万达银泰商场
             */
            name = name.replaceAll("店","").replaceAll("GXG","");

//            String newName = null;
//            String finalName = name;
//            List<String> names = tableNames.stream().filter(e -> e.contains(finalName)).toList();
//            if(names == null || names.size() == 0) {
//                continue;
//            }
//            newName = names.get(0);
            // 数字无法参与”模糊匹配新的终端名“方法
            if (name.replaceAll("[0-9]","").length() != name.length()) {
                continue;
            }
            // 模糊匹配新的终端名
            String newName = SearchTermName(tableNames, name);


            if (name.equals(newName) || newName == null) {
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
     * 搜索匹配终端名称
     * @param tableNames
     * @param name
     * @return
     */
    public static String SearchTermName(List<String> tableNames, String name) {
        if (name.length() == 1) {
            return null;
        }
        String finalName = name;
        List<String> names = tableNames.stream().filter(e -> e.contains(finalName)).toList();
        if(names == null || names.size() == 0) {
            // TODO 名称两边每次缩减一个字，来提取匹配中间关键词；例如：北京朝阳大悦城购物广场 => 京朝阳大悦城购物广 => 朝阳大悦城购物
            name = name.substring(1, name.length() - 1);

            // 递归模糊匹配
            return SearchTermName(tableNames, name);
        }
        return names.get(0);
    }
    /**
     * 批量更新终端名
     *
     * @param list
     */
    public static void batchUpdateTermName(List<SongVo> list) {
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
package com.dygj.main.uniqlo;

import com.dygj.entity.*;
import com.google.gson.Gson;

import java.io.*;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 快速创建播放素材程序
 *
 * @Auther:Lipf
 */
public class Main {

    static String token = "";
    static String userId = "";
    static String songlistid = ""; // 文件夹ID

    public static void main(String[] args) throws Exception {

        /**
         * 使用步骤：
         *      前提：更新token、songlistid、userId
         *
         *      0.除了《1.获取歌单模板》其他全部方法注释掉
         *      1.打开《2.更新模板歌单中文件名称》 方法【执行后并注释掉】
         *      2.更新《手动写入 - 1.文件夹名称》《手动写入 - 2.文件夹名称对应序号》
         *      3.打开《3.批量创建素材文件夹》《4.构建文件夹中完整数据》《5.歌单批量添加文件》 方法【执行后并注释掉】
         *      4.结束 -> 检查生成后歌单数据是否正确
         */

        // TODO 1.获取歌单模板
        List<SongVo> list = getMetaData();

        // TODO 2.更新模板歌单中文件名称
//        batchUpdateFileName(list);

        // TODO <<<<<<<<<<<<<      手动写入 - 1.文件夹名称     >>>>>>>>>>>>>>>>
        String[] folderNames = {
                "7.12~7.18——全社",
                "7.12~7.18——388",
                "7.12~7.18——88",
                "7.12~7.18——888",
                "7.12~7.18——4家包装服务店铺",
                "7.12~7.18——725、295",
                "7.12~7.18——37成都IY双楠闭店"
        };
        // TODO 3.批量创建素材文件夹
//        List<SongVo> folderRes = batchAddFolder(folderNames);
        // TODO <<<<<<<<<<<<<      手动写入 - 2.文件夹名称对应序号     >>>>>>>>>>>>>>>>
        Map<String, String> templateMap = new HashMap<>();
        templateMap.put(folderNames[0], "01、03、04、05、06、07、01、03、08、09、10、11、12、01、03、13、14、21、22".trim());
        templateMap.put(folderNames[1], "02、03、04、05、06、07、02、03、08、09、10、11、12、02、03、13、14、15、16、19、09、21、22".trim());
        templateMap.put(folderNames[2], "02、03、04、05、06、07、02、03、08、09、10、11、12、02、03、13、14、17、18、19、09、21、22".trim());
        templateMap.put(folderNames[3], "01、03、04、05、06、07、01、03、08、09、10、11、12、01、03、13、14、19、21、22".trim());
        templateMap.put(folderNames[4], "02、03、04、05、06、07、02、03、08、09、10、11、12、02、03、13、14、21、22".trim());
        templateMap.put(folderNames[5], "01、03、04、05、06、07、01、03、08、09、10、11、12、01、03、13、14、21、22".trim());
        templateMap.put(folderNames[6], "01、03、04、05、06、07、01、03、08、09、10、11、12、01、03、13、14、20、21、22".trim());
        // TODO 4.构建文件夹中完整数据
//        Map<String, List<SongVo>> fileMap = buildFileMap(templateMap, list);
        // TODO 5.歌单批量添加文件
//        batchAddFile(folderRes, fileMap);

    }

    /**
     * 构建文件夹中完整数据
     * @param templateMap
     * @param list
     * @return
     * @throws IllegalAccessException
     */
    public static Map<String, List<SongVo>> buildFileMap(Map<String, String> templateMap, List<SongVo> list) throws IllegalAccessException {
        Map<String, List<SongVo>> fileMap = new HashMap<>();
        List<SongVo> fileList;
        // 缓存接口返回文件详情
        Map<String, SongVo> sessionsMap = new HashMap<>();

        for (Map.Entry<String, String> map : templateMap.entrySet()) {
            fileList = new ArrayList<>();
            String folderName = map.getKey();
            String template = map.getValue();
            String[] arr = template.split("、");
            for(int i = 0; i < arr.length; i++) {
                int no = Integer.parseInt(arr[i]);
                for(SongVo file : list) {
                    // 240517-1+24516-1+新店背景乐.mp3
                    String fileName = file.name;
                    String count = fileName.substring(fileName.indexOf("-") + 1, fileName.indexOf("+"));
                    if(no == Integer.parseInt(count)) {
                        // 根据文件名匹配素材
                        SongVo result = sessionsMap.get(fileName) == null ? findFilesByName(fileName) : sessionsMap.get(fileName);
                        if(result != null) {
                            fileList.add(result);
                            // 缓存数据
                            sessionsMap.put(fileName, result);
                        }
                    }
                }
            }

            fileMap.put(folderName, fileList);
        }

        Gson gson = new Gson();
        System.out.println("buildFileMap：" + gson.toJson(fileMap));
        return fileMap;
    }

    /**
     * 获取歌单模板
     * @return
     */
    public static List<SongVo> getMetaData() {
        String param = "songname=&isshow=0&pageNo=1&pageSize=100&songlistid="+songlistid+"&languagetype=0";
        String res = doPostOrGet("https://b2.hottermusic.com:33340/api/songlist/song", param, "POST");
//        String res = "{   \"data\" : [      {         \"album\" : \"未知\",         \"bpm\" : \"0\",         \"copyright\" : \"\",         \"duration\" : \"0\",         \"filesize\" : \"0.45M\",         \"id\" : 984973,         \"name\" : \"11.jpg\",         \"singer\" : \"未知\",         \"songinsonglistid\" : 4124162,         \"songstylename\" : \"其他\",         \"songtypename\" : \"歌曲\",         \"thumbnailurl\" : \"\",         \"type\" : 3,         \"url\" : \"http://z.hottermusic.com/resources/ckDjKtaZQr25zkA7Q5jpRGrwhQbd6cdJ.jpg\",         \"userid\" : \"11926\"      },      {         \"album\" : \"未知\",         \"bpm\" : \"0\",         \"copyright\" : \"\",         \"duration\" : \"0\",         \"filesize\" : \"0.46M\",         \"id\" : 984974,         \"name\" : \"2.jpg\",         \"singer\" : \"未知\",         \"songinsonglistid\" : 4124163,         \"songstylename\" : \"其他\",         \"songtypename\" : \"歌曲\",         \"thumbnailurl\" : \"\",         \"type\" : 3,         \"url\" : \"http://z.hottermusic.com/resources/K7jtkSDfmKGBMxErbbdXt3wHe7YGnMMj.jpg\",         \"userid\" : \"11926\"      },      {         \"album\" : \"未知\",         \"bpm\" : \"0\",         \"copyright\" : \"\",         \"duration\" : \"0\",         \"filesize\" : \"0.36M\",         \"id\" : 984975,         \"name\" : \"3.jpg\",         \"singer\" : \"未知\",         \"songinsonglistid\" : 4124164,         \"songstylename\" : \"其他\",         \"songtypename\" : \"歌曲\",         \"thumbnailurl\" : \"\",         \"type\" : 3,         \"url\" : \"http://z.hottermusic.com/resources/PDXF3STyByXfexnCBYeZmWmHFWRWexjC.jpg\",         \"userid\" : \"11926\"      },      {         \"album\" : \"未知\",         \"bpm\" : \"0\",         \"copyright\" : \"\",         \"duration\" : \"0\",         \"filesize\" : \"0.14M\",         \"id\" : 984976,         \"name\" : \"4.jpg\",         \"singer\" : \"未知\",         \"songinsonglistid\" : 4124165,         \"songstylename\" : \"其他\",         \"songtypename\" : \"歌曲\",         \"thumbnailurl\" : \"\",         \"type\" : 3,         \"url\" : \"http://z.hottermusic.com/resources/ZMSpNQe7rSjJaB3siwBrMthXC75tsBbk.jpg\",         \"userid\" : \"11926\"      },      {         \"album\" : \"未知\",         \"bpm\" : \"0\",         \"copyright\" : \"\",         \"duration\" : \"0\",         \"filesize\" : \"0.30M\",         \"id\" : 984977,         \"name\" : \"5.jpg\",         \"singer\" : \"未知\",         \"songinsonglistid\" : 4124166,         \"songstylename\" : \"其他\",         \"songtypename\" : \"歌曲\",         \"thumbnailurl\" : \"\",         \"type\" : 3,         \"url\" : \"http://z.hottermusic.com/resources/8FemeBMF5mYdzpcwtiwkkyaJfNTzrxFW.jpg\",         \"userid\" : \"11926\"      }   ],   \"message\" : \"success\",   \"recordtotal\" : 5,   \"resultcode\" : \"1\"}\n";
        System.out.println("getMetaData-API："+res);

        Gson gson = new Gson();
        ResultVo resultVo = gson.fromJson(res, ResultVo.class);
        List<SongVo> list = resultVo.data;
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
        String dateStr = sdf.format(new Date());

        for (int i = 0; i < list.size(); i++) {
            String name = list.get(i).name;
            System.out.print(name + "\t");

            list.get(i).songid = list.get(i).id;
            list.get(i).languagetype = "0";
            if(!list.get(i).name.startsWith(dateStr+"-"+(i+1))) {
                list.get(i).name = dateStr + "-" + (i+1) + "+" + name;
            }
        }
        System.out.println("\nMyMetaData：" + gson.toJson(list));
        return list;
    }

    /**
     * 批量添加文件
     * @param folderRes
     * @param fileMap
     */
    public static void batchAddFile(List<SongVo> folderRes, Map<String, List<SongVo>> fileMap) {
        /**
         * songlistid: 69955
         * songids: 1010985 1010972;1010984
         * languagetype: 0
         */
        // 遍历文件夹
        for(int i = 0; i < folderRes.size(); i++) {
            List<SongVo> files = fileMap.get(folderRes.get(i).name);
            if(files == null || files.size() == 0) {
                System.err.println("batchAddFile Error：" + folderRes.get(i).name);
                continue;
            }
            String songlistid = folderRes.get(i).id;
            String languagetype = "0";
            List<String> songids = files.stream().map(e -> e.id).collect(Collectors.toList());
            String songidsStr = String.join(";", songids);
            // 添加所有文件到此文件夹
            String param = "songlistid="+URLEncoder.encode(songlistid)
                    +"&songids="+URLEncoder.encode(songidsStr)
                    +"&languagetype="+URLEncoder.encode(languagetype);
            String result = doPostOrGet("https://b2.hottermusic.com:33340/api/songlist/addsong", param, "POST");
            System.out.println("batchAddFileRes："+result);
//            if(!result.equals("success")) {
//                System.err.println("Add File Error:" + folderRes.get(i).name + "=>" + file.name);
//            }
        }
    }

    /**
     * 根据文件名匹配素材
     * @param fileName 240517-1+24516-1+新店背景乐.mp3
     * @return
     */
    public static SongVo findFilesByName(String fileName) throws IllegalAccessException {
        SongListVo listVo = new SongListVo();
        listVo.name = fileName;
        listVo.planselmaterial = "0";
        listVo.finduserid = userId;
        listVo.songtypeid = "1";
        listVo.pageNo = "1";
        listVo.pageSize = "10";
        listVo.languagetype = "0";

        /**
         * songid:
         * name: 24516-5+de67725371-06.mp3
         * singer:
         * album:
         * planselmaterial: 0
         * finduserid: 11926
         * songtypeid: 1
         * type:
         * pageNo: 1
         * pageSize: 10
         * languagetype: 0
         */
        String param = "";
        Class cla = listVo.getClass();
        Field[] declaredFields = cla.getDeclaredFields();
        for (Field field : declaredFields) {
            Object obj = field.get(listVo);
            param += field.getName() +"="+ URLEncoder.encode(obj==null ? "" : obj.toString())+"&";
        }
        param = param.substring(0, param.length()-1);
        System.out.println("\n"+param);
        String result = doPostOrGet("https://b2.hottermusic.com:33340/api/song/list", param, "POST");
        System.out.println("findFileByName-API："+result);
        Gson gson = new Gson();
        ResultVo resultVo = gson.fromJson(result, ResultVo.class);
        List<SongVo> list = resultVo.data;
        if (list == null || list.size() == 0) {
            System.err.println("findFilesByName ERROR:" + fileName);
            return null;
        }
        if (list.size() > 1) {
            // 多个则根据名称+后缀完整匹配,根据创建时间倒序拍
            list = list.stream().filter(e -> e.name.equals(fileName)).sorted((o1, o2) -> o2.createdate.compareTo(o1.createdate)).toList();
        }
        System.out.println("findFilesByName：" + gson.toJson(list));
        // 获取最新时间文件
        return list.get(0);
    }

    /**
     * 批量创建素材文件夹
     * @param folderNames
     * @return
     * @throws IllegalAccessException
     */
    public static List<SongVo> batchAddFolder(String[] folderNames) throws IllegalAccessException {
        /**
         * name: CopyFile
         * findsonglistid:
         * songlisttypeid: 1
         * songstyleid: 7
         * description:
         * coverurl:
         * copyright: 2
         * songlistid:
         * id:
         * userid: 11926
         * languagetype: 0
         */
        // 构建新增文件夹参数
        List<SongAdd> list = new ArrayList<>();
        for(String folderName : folderNames) {
            SongAdd songAdd = new SongAdd();
            songAdd.name = folderName;
            songAdd.songstyleid = "7";
            songAdd.songlisttypeid = "1";
            songAdd.copyright = "2";
            songAdd.userid = userId;
            songAdd.languagetype = "0";
            list.add(songAdd);
        }

        Gson gson = new Gson();
        List<SongVo> results = new ArrayList<>();
        for(int i = 0; i < list.size(); i++) {
            String param = "";
            Class cla = list.get(i).getClass();
            Field[] declaredFields = cla.getDeclaredFields();
            for (Field field : declaredFields) {
                Object obj = field.get(list.get(i));
                param += field.getName() +"="+ URLEncoder.encode(obj==null ? "" : obj.toString())+"&";
            }
            param = param.substring(0, param.length()-1);
            System.out.println("\n"+param);
            String result = doPostOrGet("https://b2.hottermusic.com:33340/api/songlist/add", param, "POST");
            System.out.println("batchAddFolderRes："+result);

            FolderResultVo resultVo = gson.fromJson(result, FolderResultVo.class);
            results.add(resultVo.data);
        }
        return results;
    }

    /**
     * 批量修改文件名称
     */
    public static void batchUpdateFileName(List<SongVo> list) throws IllegalAccessException {
        for(int i = 0; i < list.size(); i++) {
            String updateParam = "";
            Class cla = list.get(i).getClass();
            Field[] declaredFields = cla.getDeclaredFields();
            for (Field field : declaredFields) {
                Object obj = field.get(list.get(i));
                updateParam += field.getName() +"="+ URLEncoder.encode(obj==null ? "" : obj.toString())+"&";
            }
            updateParam = updateParam.substring(0, updateParam.length()-1);
            System.out.println("\n"+updateParam);
            String updateRes = doPostOrGet("https://b2.hottermusic.com:33340/api/song/update", updateParam, "POST");
            System.out.println("updateRes："+updateRes);
        }
    }


    /**
     * 以post或get方式调用对方接口方法，
     * @param pathUrl
     */
    public static String doPostOrGet(String pathUrl, String data, String requstMethod){
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
            while ((str = br.readLine()) != null){
                result += str;
            }
            //关闭流
            is.close();
            //断开连接，disconnect是在底层tcp socket链接空闲时才切断，如果正在被其他线程使用就不切断。
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if (out != null){
                    out.close();
                }
                if (br != null){
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
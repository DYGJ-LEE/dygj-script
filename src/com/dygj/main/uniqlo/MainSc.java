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
 * @Auther: Lipf
 */
public class MainSc {

    static String token = "";
    static String songlistid = ""; // 文件夹ID
    static String userId = "";

    public static void main(String[] args) throws Exception {

        System.out.println("\n**************** 快速生成素材 ****************\n");

        System.out.print("请输入token(Authorization)：");
        Scanner scanner = new Scanner(System.in);
        String tokenInput = scanner.next().trim();

        System.out.print("\n请输入userid：");
        String userIdInput = scanner.next().trim();

        System.out.print("\n请输入songlistid：");
        String songlistidInput = scanner.next().trim();

        System.out.print("\n请仔细确认以上输入无误(y/n)：");
        String isCommit = scanner.next().trim();
        if(!isCommit.equals("y")) {
            System.out.print("\n输入任何键退出");
            scanner.next();
            return;
        }

        token = tokenInput;
        songlistid = songlistidInput;
        userId = userIdInput;
        if(token == null || "".equals(token) || songlistid == null || "".equals(songlistid) || userId == null || "".equals(userId) ) {
            System.err.println("error：token、songlistid、userid数据不完整！");
            System.out.print("\n输入任何键退出");
            scanner.next();
            return;
        }

        // TODO 1.获取歌单模板
        List<SongVo> list = getMetaData();

        System.out.print("\n---------------- 二选一 ----------------\n1.更新模板歌单内文件名称\n2.根据模板批量生成文件\n");
        System.out.print("请选择(1、2）：");
        String type = scanner.next().trim();
        if("1".equals(type)) {
            // TODO 2.更新模板歌单中文件名称
            batchUpdateFileName(list);
            System.out.println("\n---------------- 文件名称已成功更新~ ----------------");
        } else if("2".equals(type)) {
            System.out.print("\n输入文件夹名称(多个以中文，分割)【例：name1，name2，name3】\n");
            System.out.print("请输入：");
            String folderNamesInput = scanner.next().trim();

            System.out.print("\n按照文件夹名称顺序输入对应文件序号(多个以中文；分割)【例：01、02；05、06；01、05、06】\n");
            System.out.print("请输入：");
            String fileNumsInput = scanner.next().trim();
            if(folderNamesInput == null || "".equals(folderNamesInput) || fileNumsInput == null || "".equals(fileNumsInput)) {
                System.err.println("error：文件夹名称/对应文件序号输入有误！");
                System.out.print("\n输入任何键退出");
                scanner.next();
                return;
            }

            String[] folderNames = folderNamesInput.split("，");
            String[] fileNums = fileNumsInput.split("；");

            if(folderNames.length != fileNums.length) {
                System.err.println("error：文件夹数量与分配文件数量不一致！");
                System.out.print("\n输入任何键退出");
                scanner.next();
                return;
            }
            Map<String, String> templateMap = new HashMap<>();
            for (int i = 0; i < folderNames.length; i++) {
                templateMap.put(folderNames[i], fileNums[i].trim());
            }

            // TODO 3.批量创建素材文件夹
            List<SongVo> folderRes = batchAddFolder(folderNames);
            // TODO 4.构建文件夹中完整数据
            Map<String, List<SongVo>> fileMap = buildFileMap(templateMap, list);
            // TODO 5.歌单批量添加文件
            batchAddFile(folderRes, fileMap);

            System.out.println("\n---------------- 文件已成功生成完毕，可自行检查数据完整性~ ----------------");
        } else {
            System.err.println("error：输入内容有误！");
        }

        System.out.print("\n输入任何键退出");
        scanner.next();
    }


    /**
     * 构建文件夹中完整数据
     * @param templateMap
     * @param list
     * @return
     * @throws IllegalAccessException
     */
    public static Map<String, List<SongVo>> buildFileMap(Map<String, String> templateMap, List<SongVo> list) throws Exception {
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
            if(!list.get(i).name.startsWith(dateStr)) {
                list.get(i).name = dateStr + "-" +  (i+1) + "+" + name;
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
    public static void batchAddFile(List<SongVo> folderRes, Map<String, List<SongVo>> fileMap) throws Exception {
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
            String param = "songlistid="+URLEncoder.encode(songlistid, "UTF-8")
                    +"&songids="+URLEncoder.encode(songidsStr, "UTF-8")
                    +"&languagetype="+URLEncoder.encode(languagetype, "UTF-8");
            String result = doPostOrGet("https://b2.hottermusic.com:33340/api/songlist/addsong", param, "POST");
            System.out.println("batchAddFileRes："+result);
        }
    }

    /**
     * 根据文件名匹配素材
     * @param fileName 240517-1+24516-1+新店背景乐.mp3
     * @return
     */
    public static SongVo findFilesByName(String fileName) throws Exception {
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
            param += field.getName() +"="+ URLEncoder.encode((obj==null ? "" : obj.toString()), "UTF-8")+"&";
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
            // 多个则根据名称+后缀完整匹配
            list = list.stream().filter(e -> e.name.equals(fileName)).toList();
//            for(SongVo songVo : list) {
//                if (songVo.name.split("[.]")[0].equals("")) {
//                }
//            }
            // 获取最新时间文件
            list.sort((o1, o2) -> o2.createdate.compareTo(o1.createdate));
        }
        System.out.println("findFilesByName：" + gson.toJson(list));

        return list.get(0);
    }

    /**
     * 批量创建素材文件夹
     * @param folderNames
     * @return
     * @throws IllegalAccessException
     */
    public static List<SongVo> batchAddFolder(String[] folderNames) throws IllegalAccessException, UnsupportedEncodingException {
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
                param += field.getName() +"="+ URLEncoder.encode((obj==null ? "" : obj.toString()), "UTF-8")+"&";
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
    public static void batchUpdateFileName(List<SongVo> list) throws Exception {
        for(int i = 0; i < list.size(); i++) {
            String updateParam = "";
            Class cla = list.get(i).getClass();
            Field[] declaredFields = cla.getDeclaredFields();
            for (Field field : declaredFields) {
                Object obj = field.get(list.get(i));
                updateParam += field.getName() +"="+ URLEncoder.encode((obj==null ? "" : obj.toString()), "UTF-8")+"&";
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
            br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String str = "";
            while ((str = br.readLine()) != null){
                result += str;
//                result += new String(str.getBytes(), "UTF-8");
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
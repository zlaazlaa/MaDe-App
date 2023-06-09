package com.tech.stationsearch.util;

import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {
    /**
    *@params [context]
    *@description 读取json文件
    *@return String
    **/
    public static String readJsonStr(Context context){
        BufferedReader bufferedReader = null;
        InputStream inputStream = null;
        Reader reader = null;
        int i=0;
        try {
            //得到资源中的asset数据流
            inputStream = context.getResources().getAssets().open("city.json");
            reader = new InputStreamReader(inputStream);// 字符流
            StringBuffer sb = new StringBuffer();
            bufferedReader = new BufferedReader(reader);
            String word;
            while ((word = bufferedReader.readLine()) != null) {
                sb.append(word);
            }
            return sb.toString();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}

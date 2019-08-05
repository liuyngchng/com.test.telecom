package com.demo.file.util;

/**
 * Created by richard on 05/08/2019.
 */
public class DbUtil {

    /**
     * 获取文件所储存的机器IP.
     * @param fileId
     * @return
     */
    public static String getHost(String fileId) {
        return "192.168.0.1";
    }

    /**
     * 保存文件元数据.
     * @param fileId
     * @param hostIP
     * @return
     */
    public static boolean saveMetadata(String fileId, String hostIP) {

        return false;
    }
}

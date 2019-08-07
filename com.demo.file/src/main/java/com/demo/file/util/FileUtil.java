package com.demo.file.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by richard on 06/08/2019.
 */
public class FileUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);

    private static final String namePattern = "_vim_highlight";

    private static final String location = "/tmp/";

    public static final int BUF_SIZE = 1024 * 80;



    public static void merge(String outFileName, String inFileDir) {
        File fileDir  = new File(inFileDir);
        File[] fs = fileDir.listFiles();
        LOGGER.info("all {} files", fs.length);
        File file  = new File(outFileName);
        if (file.exists()) {
            LOGGER.info("file {} existed", outFileName);
            return;
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            LOGGER.error("error", e);
            return;
        }
        int count = mergeFiles(outFileName, fs, namePattern);
        LOGGER.info("{} files merged", count);

    }

    public static int mergeFiles(String outFile, File[] files, String namePattern) {
        int count = 0;
        FileChannel outChannel = null;
        try {
            outChannel = new FileOutputStream(outFile).getChannel();
            for(File f : files){
                if (!f.getName().contains(namePattern)) {
                    LOGGER.info("file {} excluded", f.getName());
                    continue;
                }
                FileChannel fc = new FileInputStream(f).getChannel();
                ByteBuffer bb = ByteBuffer.allocate(BUF_SIZE);
                while(fc.read(bb) != -1){
                    bb.flip();
                    outChannel.write(bb);
                    bb.clear();
                }
                fc.close();
                count ++;
//                LOGGER.info("{} file {} merged", count++, f.getName());
            }
        } catch (IOException ioe) {
            LOGGER.error("error", ioe);
        } finally{
            try {
                if (outChannel != null) {
                    outChannel.close();
                }
            } catch (IOException ignore) {

            }
        }
        return count;
    }

    public static void main(String[] args) {
        merge(location + namePattern + ".webarchive" , location);
        LOGGER.info("merge finished.");
    }


}

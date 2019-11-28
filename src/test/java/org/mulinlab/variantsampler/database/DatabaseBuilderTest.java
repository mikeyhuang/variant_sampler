package org.mulinlab.variantsampler.database;

import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.samtools.util.BlockCompressedOutputStream;
import htsjdk.samtools.util.Log;
import htsjdk.tribble.util.LittleEndianOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.mulinlab.variantsampler.utils.RunFactory;
import org.mulinlab.varnote.utils.LoggingUtils;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;


public class DatabaseBuilderTest {

    @Test
    public void testBuildDatabase() throws Exception {
        LoggingUtils.setLoggingLevel(Log.LogLevel.ERROR);
        RunFactory.buildDatabase("src/main/resources/db.ini", DatabaseBuilder.Population.EUR, 4);

//        try {
//            LittleEndianOutputStream out = new LittleEndianOutputStream(new BlockCompressedOutputStream(new File("/Users/hdd/Desktop/vanno/random/hg19/EUR" + ".gz")));
//
//            byte[] buf = new byte[1024 * 128];
//            for (int i = 1; i <= 22; i++) {
//
//                BlockCompressedInputStream in = null;
//
//                in = new BlockCompressedInputStream(new File("/Users/hdd/Desktop/vanno/random/hg19/EUR" + i + ".gz"));
//                int n = 0;
//                while((n = in.read(buf)) != -1) {
//                    out.write(buf, 0, n);
//                }
//                in.close();
//
//            }
//
//            out.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

}
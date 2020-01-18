package org.mulinlab.variantsampler.query;

import jdk.nashorn.internal.ir.VarNode;
import org.junit.Test;
import org.mulinlab.variantsampler.utils.GP;
import org.mulinlab.variantsampler.utils.QueryParam;
import org.mulinlab.variantsampler.utils.enumset.GCDeviation;
import org.mulinlab.variantsampler.utils.node.DBNode;
import org.mulinlab.varnote.operations.decode.TABLocCodec;
import org.mulinlab.varnote.operations.readers.db.VannoMixReader;
import org.mulinlab.varnote.operations.readers.db.VannoReader;
import org.mulinlab.varnote.utils.format.Format;

import java.io.IOException;
import java.util.List;

public class QueryTest {

    @Test
    public void doQuery() {
        try {
            Format format = Format.newTAB();
            format.sequenceColumn = 1;
            format.startPositionColumn = 2;
            format.endPositionColumn = 2;
//            format.refPositionColumn = 4;
//            format.altPositionColumn = 5;

            QueryParam queryParam = QueryParam.defaultQueryParam(format);

            queryParam.setGcIdx(2, GCDeviation.D5);
            queryParam.setTissueIdx(2);
            queryParam.setSamplerNumber(10000);
            queryParam.setAnnoNumber(1);

            queryParam.setExcludeInput(true);
            queryParam.setVariantTypeSpecific(true);
            queryParam.setCrossChr(false);
            Query query = new Query("/Users/hdd/Desktop/vanno/random/query.sort.chr.pos.txt", "/Users/hdd/Desktop/vanno/random/hg19/EUR.gz", queryParam);
            query.doQuery();
            query.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void queryMAF() throws IOException {
        TABLocCodec locCodec = GP.getDefaultDecode(true);
        VannoReader reader = new VannoMixReader("/Users/hdd/Desktop/vanno/random/hg19/EUR.gz");
        reader.query("9:5453459-5453460");

        List<String> list = reader.getResults();
        DBNode dbNode = new DBNode(locCodec.decode(list.get(0)));
        dbNode.decodeOthers();
        System.out.println();
    }


    @Test
    public void runCMD() throws IOException, InterruptedException {
        String cmd = "/usr/local/bin/python /Users/hdd/Desktop/vanno/random/hg19/draw_distribution.py -i /Users/hdd/Downloads/up/tell_workspace/vsampler/vsampler_202001142022328922";

        Process process = Runtime.getRuntime().exec(cmd);
        int exitValue = process.waitFor();

        if (0 == exitValue) {
            System.out.println("1111");
        } else {
            System.out.println("1111");
        }
    }
}
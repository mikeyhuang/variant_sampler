package org.mulinlab.snpsnap.index;


import nl.harmjanwestra.utilities.legacy.genetica.containers.Pair;
import nl.harmjanwestra.utilities.legacy.genetica.math.stats.Correlation;
import org.junit.Test;
import org.mulinlab.snpsnap.utils.Node;
import org.mulinlab.varnote.filters.iterator.NoFilterIterator;
import org.mulinlab.varnote.utils.enumset.FileType;
import org.mulinlab.varnote.utils.gz.MyBlockCompressedOutputStream;
import org.mulinlab.varnote.utils.gz.MyEndianOutputStream;
import java.io.IOException;

public class IndexWriterTest {

    public final static String TAB = "\t";
    final String filePath = "/Users/hdd/Desktop/vanno/random/match_database.sort.noncds_new.sort.txt.gz";

    @Test
    public void writeIndex() {
        IndexWriter indexWriter = new IndexWriter(filePath);
        indexWriter.makeIndex();
    }

    @Test
    public void readIndex() {
        Index index = new Index(filePath);
    }

    public void editFile() {
        try {
            final NoFilterIterator reader = new NoFilterIterator(filePath, FileType.BGZ);
            final MyEndianOutputStream out = new MyEndianOutputStream(new MyBlockCompressedOutputStream(filePath.replace(".gz", "_new.gz")));

            Node node = null;
            while (reader.hasNext()) {
                node = new Node(reader.next().split(TAB));
                out.writeBytes(String.format("%s\t%d\t%s\t%d\t%f\t%d\n",
                        node.getChr(), node.getBp(), node.getRsid(), (int)(node.getMaf()*100), node.getMaf(), node.getDistance()));
            }

            reader.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
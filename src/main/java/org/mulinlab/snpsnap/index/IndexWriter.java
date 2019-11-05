package org.mulinlab.snpsnap.index;

import htsjdk.samtools.util.StringUtil;
import javafx.util.Pair;
import org.mulinlab.snpsnap.utils.Node;
import org.mulinlab.varnote.filters.iterator.NoFilterIterator;
import org.mulinlab.varnote.utils.VannoUtils;
import org.mulinlab.varnote.utils.enumset.FileType;
import org.mulinlab.varnote.utils.gz.MyBlockCompressedOutputStream;
import org.mulinlab.varnote.utils.gz.MyEndianOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class IndexWriter {
    public final static String INDEX_EXT = ".idx";
    public final static String TAB = "\t";
    public final static int MAX_DIS_COUNT = 2000;

    private final String filePath;
    private Map<String, List<MAF>> mafMap;
    private List<MAF> mafList;

    public IndexWriter(final String filePath) {
        this.filePath = filePath;
        this.mafMap = new HashMap<>();
        this.mafList = new ArrayList<>();
    }

    public void makeIndex() {
        VannoUtils.checkValidBGZ(filePath);

        try {
            NoFilterIterator reader = new NoFilterIterator(filePath, FileType.BGZ);

            String seqName = "";
            Node node = null;
            int maf, lasMaf = 0, count = 0;

            long filePointer = reader.getPosition();
            while (reader.hasNext()) {
                node = new Node(reader.next().split(TAB));

                maf = node.getMaf() ;
                if(!node.getChr().equals(seqName)) {
                    if(mafList.size() > 0) {
                        this.mafMap.put(seqName, mafList);
                        this.mafList = new ArrayList<>();
                    }

                    mafList.add(new MAF(maf, filePointer));
                    lasMaf = maf;
                } else if(maf > lasMaf) {
                    mafList.add(new MAF(maf, filePointer));
                    lasMaf = maf;
                }

                if(count == MAX_DIS_COUNT) {
                    mafList.get(mafList.size() - 1).addDistance(new Pair<>(node.getDistance(), filePointer));
                    count = 0;
                }
                seqName = node.getChr();
                filePointer = reader.getPosition();
                count++;
            }

            this.mafMap.put(seqName, mafList);
            writeIndex();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    public void writeIndex() throws IOException {
        final MyEndianOutputStream index = new MyEndianOutputStream(new MyBlockCompressedOutputStream(filePath + INDEX_EXT));

        index.writeInt(mafMap.keySet().size());
        for (String chr: mafMap.keySet()) {
            index.writeInt(chr.length());
            index.write(StringUtil.stringToBytes(chr));

            mafList = mafMap.get(chr);
            index.writeInt(mafList.size());
            for (MAF maf: mafList) {
                index.writeInt(maf.getMaf());
                index.writeLong(maf.getAddress());

                index.writeInt(maf.getDistances().size());
                for (Pair<Integer, Long> dis: maf.getDistances()) {
                    index.writeInt(dis.getKey());
                    index.writeLong(dis.getValue());
                }
            }
        }

        index.close();
    }



}

package org.mulinlab.snpsnap.index;

import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.samtools.util.StringUtil;
import javafx.util.Pair;
import org.mulinlab.snpsnap.utils.Node;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.filters.iterator.NoFilterIterator;
import org.mulinlab.varnote.utils.VannoUtils;
import org.mulinlab.varnote.utils.enumset.FileType;
import org.mulinlab.varnote.utils.gz.MyBlockCompressedOutputStream;
import org.mulinlab.varnote.utils.gz.MyEndianOutputStream;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class Index {

    private final String indexPath;
    private Map<String, List<MAF>> mafMap;
    private Map<String, Long> chrAddress;
    protected byte[] longbuf = new byte[8];

    public Index(final String filePath) {
        this.indexPath = filePath + IndexWriter.INDEX_EXT;
        this.mafMap = new HashMap<>();
        this.chrAddress = new HashMap<>();

        try {
            readIndex();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readIndex() throws IOException {
        BlockCompressedInputStream in = new BlockCompressedInputStream(new File(this.indexPath));
        final int size = GlobalParameter.readInt(in);

        int length, disSize;
        byte[] buf;
        String chr;
        List<MAF> mafList;
        MAF maf;

        for (int i = 0; i < size; i++) {
            length = GlobalParameter.readInt(in);
            buf = new byte[length];
            in.read(buf);
            chr = new String(buf);

            mafList = new ArrayList<>();
            length = GlobalParameter.readInt(in);
            for (int j = 0; j < length; j++) {
                maf = new MAF(GlobalParameter.readInt(in), GlobalParameter.readLong(in, longbuf));

                disSize = GlobalParameter.readInt(in);
                for (int k = 0; k < disSize; k++) {
                    maf.addDistance(new Pair<>(GlobalParameter.readInt(in), GlobalParameter.readLong(in, longbuf)));
                }
                mafList.add(maf);
            }

            chrAddress.put(chr, mafList.get(0).getAddress());
            mafMap.put(chr, mafList);

        }
        in.close();
    }

    public List<MAF> getMafList(final String chr) {
        return mafMap.get(chr);
    }
}

package org.mulinlab.variantsampler.query;

import htsjdk.samtools.seekablestream.SeekableStreamFactory;
import htsjdk.samtools.util.BlockCompressedInputStream;
import org.mulinlab.variantsampler.database.RoadmapAnnotation;
import org.mulinlab.variantsampler.index.Index;
import org.mulinlab.variantsampler.index.IndexMaf;
import org.mulinlab.variantsampler.utils.GP;
import org.mulinlab.variantsampler.utils.MafAddress;
import org.mulinlab.variantsampler.utils.node.Node;
import org.mulinlab.variantsampler.utils.QueryParam;
import org.mulinlab.varnote.constants.GlobalParameter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class MafReader {
    private byte[] longbuf = new byte[8];

    private final BlockCompressedInputStream mafin;
    private final Index index;

    public MafReader(final String dbPath) throws IOException {
        this.mafin = new BlockCompressedInputStream(SeekableStreamFactory.getInstance().getBufferedStream(
                SeekableStreamFactory.getInstance().getStreamFor(IndexMaf.getMafPath(dbPath))));
        this.index = new Index(IndexMaf.getMafIdxPath(dbPath));
    }

    public void close() throws IOException {
        this.mafin.close();
    }

    public List<Node> loadMAF(final String chr, final int maf, final QueryParam queryParam) throws IOException {
        if(maf < 0 || maf > 50) return null;

        MafAddress address = index.getMafAddress(chr, maf);

        mafin.seek(address.getMafAddress());
        int size = GlobalParameter.readInt(mafin);
        List<Node> nodes = new ArrayList<>();

        Node node;
        for (int i = 0; i < size; i++) {
            node = new Node();
            node.setMaf(maf);
            node.setDtct(GlobalParameter.readInt(mafin));
            node.setMafOrg(GP.readFloat(mafin));
            node.setAddress(GlobalParameter.readLong(mafin, longbuf));
            nodes.add(node);
        }

        if(queryParam.getGeneDisIndex() != GP.NO_GENE_DIS) {
            mafin.seek(address.getGeneDistanceAddress()[queryParam.getGeneDisIndex()]);
            for (int i = 0; i < nodes.size(); i++) {
                nodes.get(i).setGeneDis(GlobalParameter.readShort(mafin));
            }
        }

        if(queryParam.getGeneLDIndex() != GP.NO_GENE_LD) {
            mafin.seek(address.getGeneLDAddress()[queryParam.getGeneLDIndex()]);
            for (int i = 0; i < nodes.size(); i++) {
                nodes.get(i).setGeneLD(GlobalParameter.readShort(mafin));
            }
        }

        if(queryParam.getLdIndex() != GP.NO_LD_BUDDIES) {
            mafin.seek(address.getLdBuddiesAddress()[queryParam.getLdIndex()]);
            for (int i = 0; i < nodes.size(); i++) {
                nodes.get(i).setLdBuddies(GlobalParameter.readShort(mafin));
            }
        }

        if(queryParam.hasCellMarker()) {
            mafin.seek(address.getRoadmapAnno()[queryParam.getMarkerIndex()]);
            for (int i = 0; i < nodes.size(); i++) {
                nodes.get(i).setRoadmap(RoadmapAnnotation.getValOfIndex(GlobalParameter.readLong(mafin, longbuf), GlobalParameter.readLong(mafin, longbuf), queryParam.getCellIdx()));
            }
        }

        return nodes;
    }
}

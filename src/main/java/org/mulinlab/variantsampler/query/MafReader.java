package org.mulinlab.variantsampler.query;

import htsjdk.samtools.seekablestream.SeekableStreamFactory;
import htsjdk.samtools.util.BlockCompressedInputStream;
import org.mulinlab.variantsampler.database.RoadmapAnnotation;
import org.mulinlab.variantsampler.database.TissueAnnotation;
import org.mulinlab.variantsampler.index.Index;
import org.mulinlab.variantsampler.index.IndexMaf;
import org.mulinlab.variantsampler.utils.GP;
import org.mulinlab.variantsampler.utils.MafAddress;
import org.mulinlab.variantsampler.utils.node.Node;
import org.mulinlab.variantsampler.utils.QueryParam;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.node.LocFeature;

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

    public List<Node> loadMAF(final String chr, final int maf, final QueryParam queryParam, final InputStack inputStack) throws IOException {
        if(maf < 0 || maf > 50) return null;

        MafAddress address = index.getMafAddress(chr, maf);

        mafin.seek(address.getMafAddress());
        int size = GlobalParameter.readInt(mafin);
        List<Node> nodes = new ArrayList<>();

        Node node;
        int beg, idx;
        byte[] b;
        String refalt;
        LocFeature locFeature;

        for (int i = 0; i < size; i++) {
            node = new Node();
            node.setMaf(maf);

            beg = GlobalParameter.readInt(mafin);
            locFeature = new LocFeature(beg, beg, chr);

            b = new byte[GlobalParameter.readInt(mafin)];
            mafin.read(b);
            refalt = new String(b);
            idx = refalt.indexOf(",");
            locFeature.ref = refalt.substring(0, idx);
            locFeature.alt = refalt.substring(idx+1);
            locFeature.end = beg + locFeature.ref.length();

            node.setLocFeature(locFeature);
            node.setDtct(GlobalParameter.readInt(mafin));
            node.setMafOrg(GP.readFloat(mafin));

            if(queryParam.isExcludeInput() && inputStack.isNodeInQuery(node)) {
                node.setInQuery(true);
            }
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

        int len;
        if(queryParam.hasCellMarker()) {
            mafin.seek(address.getRoadmapAnnoAddress()[queryParam.getMarkerIndex()]);
            for (int i = 0; i < nodes.size(); i++) {
                len = GlobalParameter.read(mafin);
                if(len == 0) {
                    nodes.get(i).setRoadmap(false);
                } else {
                    long[] val = new long[len];
                    for (int j = 0; j < len; j++) {
                        val[j] = GlobalParameter.readLong(mafin, longbuf);
                    }
                    nodes.get(i).setRoadmap(RoadmapAnnotation.getValOfIndex(val, queryParam.getCellIdx()));
                }
            }
        }

        if(queryParam.isVariantTypeMatch()) {
            mafin.seek(address.getCatAddress());
            for (int i = 0; i < nodes.size(); i++) {
                nodes.get(i).setCat(GlobalParameter.read(mafin));
            }
        }

        if(queryParam.getTissueIdx() != GP.NO_TISSUE) {
            mafin.seek(address.getTissueAddress());

            for (int i = 0; i < nodes.size(); i++) {
                len = GlobalParameter.read(mafin);
                if(len == 0) {
                    nodes.get(i).setTissue(false);
                } else {
                    nodes.get(i).setTissue(TissueAnnotation.getValOfIndex(new long[]{GlobalParameter.readLong(mafin, longbuf)}, queryParam.getTissueIdx()));
                }
            }
        }

        if(queryParam.getGcRange() != null) {
            mafin.seek(address.getGcAddress()[queryParam.getGcIdx()]);
            for (int i = 0; i < nodes.size(); i++) {
                nodes.get(i).setGc(GlobalParameter.read(mafin));
            }
        }

        return nodes;
    }
}

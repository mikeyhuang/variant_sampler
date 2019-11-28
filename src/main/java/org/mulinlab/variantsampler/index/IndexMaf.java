package org.mulinlab.variantsampler.index;

import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.tribble.util.LittleEndianInputStream;
import org.mulinlab.variantsampler.utils.GP;
import org.mulinlab.variantsampler.utils.node.DBNode;
import org.mulinlab.variantsampler.utils.MafAddress;
import org.mulinlab.variantsampler.utils.Pair;
import org.mulinlab.variantsampler.utils.sort.DTCTSort;
import org.mulinlab.variantsampler.utils.sort.MAFSort;
import org.mulinlab.varnote.exceptions.InvalidArgumentException;
import org.mulinlab.varnote.operations.readers.db.VannoMixReader;
import org.mulinlab.varnote.operations.readers.db.VannoReader;
import org.mulinlab.varnote.utils.database.index.TbiIndex;
import org.mulinlab.varnote.utils.gz.MyBlockCompressedOutputStream;
import org.mulinlab.varnote.utils.gz.MyEndianOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

public final class IndexMaf {
    public final static String MAF = ".maf";
    public final static String INDEX_EXT = ".maf.idx";

    private static byte[] longbuf = new byte[8];

    private final VannoReader srcReader;
    private final TbiIndex index;
    private final BlockCompressedInputStream in;
    private final MyEndianOutputStream out;
    private final MyEndianOutputStream outIdx;

    private List<DBNode> mafList;
    private Map<Integer, MafAddress> mafAddrsMap;
    private Map<Integer, Map<Integer, MafAddress>> chrMap;

    public static String getMafPath(final String filePath) {
        return filePath.replace(".gz", MAF);
    }

    public static String getMafIdxPath(final String filePath) {
        return filePath.replace(".gz", INDEX_EXT);
    }

    public IndexMaf(final String filePath) throws IOException {
        this.srcReader = new VannoMixReader(filePath);
        this.index = new TbiIndex(filePath + ".tbi");
        in = new BlockCompressedInputStream(new File(filePath));
        out = new MyEndianOutputStream(new MyBlockCompressedOutputStream(getMafPath(filePath)));
        outIdx = new MyEndianOutputStream(new MyBlockCompressedOutputStream(getMafIdxPath(filePath)));

        chrMap = new HashMap<>();
        for (int i=GP.FROM_CHROM; i<=GP.TO_CHROM; i++) {

            mafAddrsMap = new HashMap<>();
            System.out.println("chr=" + i);
            for (int j = 0; j <= GP.MAX_MAF; j++) {
                System.out.println("maf=" + j);
                getMafList(String.valueOf(i), j);
                if (mafList.size() > 0) {
                    writeIndexForMaf(j);
                }
            }
            chrMap.put(i, mafAddrsMap);
        }

        writeEnd();

        in.close();
        srcReader.close();
        out.close();
        outIdx.close();
    }

    private void writeEnd() throws IOException {
        Map<Integer, MafAddress> mafAddrsMap;

        outIdx.writeInt(chrMap.keySet().size());
        for (Integer chr:chrMap.keySet()) {
            mafAddrsMap = chrMap.get(chr);
            outIdx.writeInt(chr);

            outIdx.writeInt(mafAddrsMap.keySet().size());
            for (MafAddress address: mafAddrsMap.values()) {
                outIdx.writeInt(address.getMaf());
                outIdx.writeLong(address.getMafAddress());

                outIdx.writeInt(address.getGeneDistanceAddress().length);
                for (int j = 0; j < address.getGeneDistanceAddress().length; j++) {
                    outIdx.writeLong(address.getGeneDistanceAddress()[j]);
                }

                outIdx.writeInt(address.getGeneLDAddress().length);
                for (int j = 0; j < address.getGeneLDAddress().length; j++) {
                    outIdx.writeLong(address.getGeneLDAddress()[j]);
                }

                outIdx.writeInt(address.getLdBuddiesAddress().length);
                for (int j = 0; j < address.getLdBuddiesAddress().length; j++) {
                    outIdx.writeLong(address.getLdBuddiesAddress()[j]);
                }

                outIdx.writeInt(address.getRoadmapAnno().length);
                for (int j = 0; j < address.getRoadmapAnno().length; j++) {
                    outIdx.writeLong(address.getRoadmapAnno()[j]);
                }
            }
        }
    }

    private void writeIndexForMaf(final int maf) throws IOException {
        MafAddress mafAddress = new MafAddress(maf);
        mafAddress.setMafAddress(out.getOut().getFilePointer());

        out.writeInt(mafList.size());
        for (DBNode dbNode: mafList) {
            out.writeInt(dbNode.getDtct());
            out.writeFloat((float) dbNode.getMafOrg());
            out.writeLong(dbNode.getAddress());
        }

        for (int i=0; i<GP.GENE_DIS_SIZE; i++) {
            mafAddress.setGeneDistanceAddress(out.getOut().getFilePointer(), i);
            for (DBNode dbNode: mafList) {
                try {
                    if(dbNode.getGeneDensity()[i] >= GP.MAX_SHORT_UNSIGNED) throw new InvalidArgumentException("out of boundary " + dbNode.getLocFeature());
                    out.writeShort(dbNode.getGeneDensity()[i]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        for (int i=0; i<GP.GENE_LD_SIZE; i++) {
            mafAddress.setGeneLDAddress(out.getOut().getFilePointer(), i);

            for (DBNode dbNode: mafList) {
                if(dbNode.getGeneInLD()[i] >= GP.MAX_SHORT_UNSIGNED) throw new InvalidArgumentException("out of boundary " + dbNode.getLocFeature());
                out.writeShort(dbNode.getGeneInLD()[i]);
            }
        }

        for (int i=0; i<GP.LD_BUDDIES_SIZE; i++) {
            mafAddress.setLdBuddiesAddress(out.getOut().getFilePointer(), i);

            for (DBNode dbNode: mafList) {
                if(dbNode.getLdBuddies()[i] >= GP.MAX_SHORT_UNSIGNED) throw new InvalidArgumentException("out of boundary " + dbNode.getLocFeature());
                out.writeShort(dbNode.getLdBuddies()[i]);
            }
        }

        Pair<Long, Long> cellMark;
        for (int i=0; i<GP.ROADMAP_SIZE; i++) {
            mafAddress.setRoadmapAnno(out.getOut().getFilePointer(), i);
            for (DBNode dbNode: mafList) {
                cellMark = dbNode.getCellMarks()[i];
                out.writeLong(cellMark.getKey());
                out.writeLong(cellMark.getValue());
            }
        }

        mafAddrsMap.put(maf, mafAddress);
    }

    private void getMafList(final String chr, final int maf) throws IOException {
        in.seek(index.getMinOffForChr(index.chr2tid(chr)));
        mafList = new ArrayList<>();

        String line;
        DBNode dbNode;
        long filePointer = in.getFilePointer();
        while ((line = in.readLine()) != null) {
            dbNode = new DBNode(line.split("\t"));

            if(!dbNode.getLocFeature().chr.equals(chr)) break;
            if(dbNode.getMaf() == maf) {
                dbNode.setAddress(filePointer);
                dbNode.decodeOthers();
                mafList.add(dbNode);
            }
            filePointer = in.getFilePointer();
        }

        Collections.sort(mafList, new MAFSort());
    }
}

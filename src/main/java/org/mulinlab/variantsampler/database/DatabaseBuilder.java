package org.mulinlab.variantsampler.database;

import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.samtools.util.Log;
import htsjdk.samtools.util.StringUtil;
import htsjdk.variant.variantcontext.VariantContext;
import org.apache.commons.lang3.StringUtils;
import org.mulinlab.variantsampler.utils.*;
import org.mulinlab.variantsampler.utils.node.DBNode;
import org.mulinlab.varnote.config.param.DBParam;
import org.mulinlab.varnote.exceptions.InvalidArgumentException;
import org.mulinlab.varnote.utils.LoggingUtils;
import org.mulinlab.varnote.utils.database.DatabaseFactory;
import org.mulinlab.varnote.utils.database.TbiDatabase;
import org.mulinlab.varnote.utils.database.index.TbiIndex;
import org.mulinlab.varnote.utils.enumset.IndexType;
import org.mulinlab.varnote.utils.gz.MyBlockCompressedOutputStream;
import org.mulinlab.varnote.utils.gz.MyEndianOutputStream;
import org.mulinlab.varnote.utils.node.LocFeature;
import java.io.File;
import java.io.IOException;

public final class DatabaseBuilder {
    public final static String TAB = GP.TAB;

    public enum Population {EUR, EAS, AFR};
    public static final String AF = "_AF";

    private final String mafStr;

    private final DBSource dbSource;
    private final LDComputer ldComputer;
    private final DistanceComputer distanceComputer;
    private final RoadmapAnnotation roadmapAnno;

    private final BlockCompressedInputStream in;
    private final TbiIndex index;
    private final TbiDatabase database;

    private MyEndianOutputStream out;

    public DatabaseBuilder(final String srcFile, final Population popus) throws Exception {
        LoggingUtils.setLoggingLevel(Log.LogLevel.ERROR);

        this.dbSource = new DBSource(srcFile);
        this.mafStr = popus.toString() + AF;
        if(popus == Population.EUR) {
            ldComputer = new LDComputer(dbSource.getDatabasePath(DBSource.EUR_LD_DB));
        } else if(popus == Population.EAS) {
            ldComputer = new LDComputer(dbSource.getDatabasePath(DBSource.EAS_LD_DB));
        } else if(popus == Population.AFR) {
            ldComputer = new LDComputer(dbSource.getDatabasePath(DBSource.AFR_LD_DB));
        } else {
            throw new InvalidArgumentException(String.format("%s is not supported.", popus));
        }

        final String path1000G = dbSource.getDatabasePath(DBSource.DB1000G);
        in = new BlockCompressedInputStream(new File(path1000G));

        DBParam dbParam = new DBParam(path1000G);
        dbParam.setIndexType(IndexType.TBI);
        database = (TbiDatabase)DatabaseFactory.readDatabase(dbParam);
        database.setVCFLocCodec(false, database.getVcfParser().getCodec());

        index = (TbiIndex)database.getIndex();

        distanceComputer = new DistanceComputer(dbSource.getDatabasePath(DBSource.GENCODE_GENE));
        roadmapAnno = new RoadmapAnnotation(dbSource.getDatabasePath(DBSource.ROADMAP));
    }

    public String getMergeResult() {
        return dbSource.getDatabasePath(DBSource.OUT_DIR) + mafStr.replace(AF, "") ;
    }

    public void buildDatabase(final String chr) throws IOException {

        long count = 0;
        out = new MyEndianOutputStream(new MyBlockCompressedOutputStream(getMergeResult() + chr + ".gz"));

        in.seek(index.getMinOffForChr(index.chr2tid(chr)));

        String line;
        LocFeature locFeature;
        VariantContext ctx;

        while ((line = in.readLine()) != null) {
            if(line.charAt(0) != '#') {
                locFeature = database.decode(line);
                if(!locFeature.chr.equals(chr)) break;

                ctx = locFeature.variantContext;
                final String[] mafs = ctx.getAttributeAsString(mafStr, ".").replace('[', ' ').replace(']', ' ').trim().split(",");
                double mafval = 9;
                for (String maf:mafs) {
                    if(Double.parseDouble(maf) < mafval) {
                        mafval = Double.parseDouble(maf);
                    }
                }

                mafval = mafval > 0.5 ? 1 - mafval : mafval;
                if(mafval > GP.MAF_FILTER) {
                    printNode(processLocus(locFeature, mafval));
                    count++;
                    if(count % 10000 == 0) System.out.println(count);
//                    if(count == 200) break;
                }
            }
        }

        close();
    }

    public void close() throws IOException {
        in.close();
        out.close();
        roadmapAnno.close();
        distanceComputer.close();
        ldComputer.close();
    }

    public void printNode(final DBNode dbNode) throws IOException {
        final LocFeature loc = dbNode.getLocFeature();

        out.writeBytes(String.format("%s\t%d\t%s\t%s\t%f\t%d\t%s\t%s\t%s\t", loc.chr, loc.variantContext.getStart(), loc.variantContext.getReference().getBaseString(),
                StringUtils.join(loc.variantContext.getAlternateAlleles(), ","), dbNode.getMaf(), dbNode.getDtct(),
                StringUtils.join(dbNode.getGeneDensity(), TAB), StringUtils.join(dbNode.getGeneInLD(), TAB), StringUtils.join(dbNode.getLdBuddies(), TAB)));
        for (Pair<Long, Long> cellType: dbNode.getCellMarks()) {
//            System.out.println(cellType.getKey() + ", " + cellType.getValue());
            out.writeBytes(String.valueOf(cellType.getKey()) + "\t");
            out.writeBytes(String.valueOf(cellType.getValue()) + "\t");
        }

        out.writeBytes(loc.variantContext.getID() + '\n');
    }

    public DBNode processLocus(final LocFeature locFeature, final double mafval) throws IOException {
        final VariantContext ctx = locFeature.variantContext;
        final String chr = locFeature.chr;
        final int pos = ctx.getStart();


        Pair<Integer[], Pair<Integer, Integer>[]> ldResult = ldComputer.compute(chr, pos, ctx.getReference().getBaseString() + "," + StringUtil.join(",", ctx.getAlternateAlleles()));

        final int dtct = distanceComputer.computeDTCT(chr, pos);
        final Integer[] geneDensity = distanceComputer.computeGeneInDistance(chr, pos);
        final Integer[] geneInLD = distanceComputer.computeGeneInLD(chr, ldResult.getValue());
        final Pair<Long, Long>[] cellMarks = roadmapAnno.query(chr, pos);
        return new DBNode(locFeature, mafval, dtct, geneDensity, geneInLD, ldResult.getKey(), cellMarks);
    }

}

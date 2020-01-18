package org.mulinlab.variantsampler.database.temp;


import htsjdk.samtools.util.Log;
import htsjdk.variant.variantcontext.VariantContext;
import org.apache.commons.lang3.StringUtils;
import org.mulinlab.variantsampler.database.DistanceComputer;
import org.mulinlab.variantsampler.database.LDComputer;
import org.mulinlab.variantsampler.utils.DBSource;
import org.mulinlab.variantsampler.utils.GP;
import org.mulinlab.variantsampler.utils.Pair;
import org.mulinlab.varnote.filters.iterator.LineFilterIterator;
import org.mulinlab.varnote.operations.readers.query.VCFFileReader;
import org.mulinlab.varnote.utils.LoggingUtils;
import org.mulinlab.varnote.utils.gz.MyBlockCompressedOutputStream;
import org.mulinlab.varnote.utils.gz.MyEndianOutputStream;
import org.mulinlab.varnote.utils.node.LocFeature;
import java.io.IOException;

public final class LDBuilder {
//    public final static String TAB = GP.TAB;
//
//    public enum Population {EUR, EAS, AFR};
//    public static final String AF = "_AF";
//
//    private final String mafStr;
//    private final DBSource dbSource;
//    private final LDComputer ldComputer;
//    private final DistanceComputer distanceComputer;
//
//    private MyEndianOutputStream out;
//
//    public LDBuilder(final String srcFile, final Population popus) throws Exception {
//        LoggingUtils.setLoggingLevel(Log.LogLevel.ERROR);
//
//        this.dbSource = new DBSource(srcFile);
//        this.mafStr = popus.toString() + AF;
//        this.ldComputer = new LDComputer(dbSource.getVal(DBSource.BITFILE));
//        this.distanceComputer = new DistanceComputer(dbSource.getVal(DBSource.GENCODE_GENE));
//        out = new MyEndianOutputStream(new MyBlockCompressedOutputStream(dbSource.getVal(DBSource.OUT_DIR)  + "ld.gz"));
//    }
//
//    public void buildDatabase() throws IOException {
//        long count = 0;
//
//        final long startTime = System.currentTimeMillis();
//        VCFFileReader reader = new VCFFileReader(dbSource.getVal(DBSource.DB1000G));
//        reader.setDecodeLoc(false);
//        LineFilterIterator lineFilterIterator = reader.getFilterIterator();
//
//        LocFeature locFeature;
//        VariantContext ctx;
//        while (lineFilterIterator.hasNext()) {
//            locFeature = lineFilterIterator.next();
//            if(locFeature != null) {
//                ctx = locFeature.variantContext;
//                try {
//                    double mafval = ctx.getAttributeAsDouble(mafStr, -1);
//
//                    if(mafval != -1) {
//                        mafval = mafval > 0.5 ? 1 - mafval : mafval;
//                        processLocus(new LocFeature(locFeature.beg, locFeature.end, locFeature.chr, locFeature.ref, locFeature.alt), mafval);
//
//                        count++;
//                        if(count % 10000 == 0) {
//                            long seconds = (System.currentTimeMillis() - startTime) / 1000L;
//                            System.out.println(count + ", " + seconds); //
//                        }
//                    } else {
//                        System.out.println(locFeature + "\t" + mafval);
//                    }
//                } catch (Exception e) {
//                    System.out.println("Exception:" + locFeature.toString());
//                }
//            }
//        }
//        close();
//    }
//
//    public void close() throws IOException {
//        out.close();
//        distanceComputer.close();
//        ldComputer.close();
//    }
//
//    public void processLocus(final LocFeature locFeature, final double mafval) throws IOException {
//        String chr = locFeature.chr;
//        int pos = locFeature.beg + 1;
//
//        Pair<Integer[], Pair<Integer, Integer>[]> ldResult = ldComputer.compute(chr, pos, locFeature.ref, locFeature.alt);
//        if(ldResult != null) {
//
//            int dtct = distanceComputer.computeDTCT(chr, pos);
//            final Integer[] geneDensity = distanceComputer.computeGeneInDistance(chr, pos);
//            final Integer[] geneInLD = distanceComputer.computeGeneInLD(chr, ldResult.getValue());
//
//            out.writeBytes(String.format("%s\t%d\t%s\t%s\t%f\t%d\t%s\t%s\t%s\n", chr, pos, locFeature.ref,
//                    locFeature.alt, mafval, dtct,
//                    StringUtils.join(geneDensity, TAB), StringUtils.join(geneInLD, TAB), StringUtils.join(ldResult.getKey(), TAB)));
//        }
//    }
}

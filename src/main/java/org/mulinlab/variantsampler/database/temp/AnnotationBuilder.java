package org.mulinlab.variantsampler.database.temp;

import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.mulinlab.variantsampler.database.RoadmapAnnotation;
import org.mulinlab.variantsampler.database.TissueAnnotation;
import org.mulinlab.variantsampler.utils.DBSource;
import org.mulinlab.variantsampler.utils.GP;
import org.mulinlab.varnote.filters.iterator.LineFilterIterator;
import org.mulinlab.varnote.operations.readers.query.VCFFileReader;
import org.mulinlab.varnote.utils.JannovarUtils;
import org.mulinlab.varnote.utils.enumset.GenomeAssembly;
import org.mulinlab.varnote.utils.gz.MyBlockCompressedOutputStream;
import org.mulinlab.varnote.utils.gz.MyEndianOutputStream;
import org.mulinlab.varnote.utils.jannovar.VariantAnnotation;
import org.mulinlab.varnote.utils.node.LocFeature;

import java.io.File;
import java.io.IOException;
import java.util.BitSet;
import java.util.Map;


public final class AnnotationBuilder {
//    public final static String TAB = GP.TAB;
//
//    public enum Population {EUR, EAS, AFR};
//
//    private final DBSource dbSource;
//    private RoadmapAnnotation roadmapAnno;
//    private JannovarUtils jannovarUtils;
//    private Map<String, Map<String, String>> variantAnnoMap;
//    private TissueAnnotation tissueAnno;
//
//    private final Gson gson = new Gson();
//    private MyEndianOutputStream out;
//
//    public AnnotationBuilder(final String srcFile, final Population popus) throws Exception {
//        this.dbSource = new DBSource(srcFile);
//
//        getVariantEffectResource();
//        roadmapAnno = new RoadmapAnnotation(dbSource.getVal(DBSource.ROADMAP));
//
//        out = new MyEndianOutputStream(new MyBlockCompressedOutputStream(dbSource.getVal(DBSource.OUT_DIR)  + "anno.gz"));
//    }
//
//    public void getVariantEffectResource() throws IOException {
//        if(variantAnnoMap == null) {
//            variantAnnoMap = gson.fromJson(FileUtils.readFileToString(new File(dbSource.getVal(DBSource.VF_PATH)), "utf-8"), Map.class);
//        }
//    }
//
//    public void getTissueAnno() throws IOException {
//        if(tissueAnno == null) {
//            final String tissuePath = dbSource.getVal(DBSource.TISSUE_PATH);
//            String[] tissueList = FileUtils.readFileToString(new File(dbSource.getVal(DBSource.TISSUE_LIST)), "utf-8").split("\n");
//
//            tissueAnno = new TissueAnnotation(tissueList, tissuePath);
//        }
//    }
//
//    public void loadJannovar() {
//        if(this.jannovarUtils == null) {
//            this.jannovarUtils = new JannovarUtils(dbSource.getVal(DBSource.SERPATH));
//            this.jannovarUtils.setGenome(GenomeAssembly.valueOf(dbSource.getVal(DBSource.GENOME)));
//            this.jannovarUtils.setJannovarData();
//        }
//    }
//
//    public void buildDatabase(final String chr) throws IOException {
//        long count = 0;
//
//        VCFFileReader reader = new VCFFileReader(dbSource.getVal(DBSource.DB1000G));
//        reader.setDecodeLoc(true);
//        LineFilterIterator lineFilterIterator = reader.getFilterIterator();
//
//        LocFeature locFeature;
//        loopA: while (lineFilterIterator.hasNext()) {
//            locFeature = lineFilterIterator.next();
//            if(locFeature != null) {
//                processLocus(new LocFeature(locFeature.beg, locFeature.end, locFeature.chr, locFeature.ref, locFeature.alt));
//
//                count++;
//                if(count % 10000 == 0) {
//                    System.out.println(count);
//                }
//            }
//        }
//        close();
//    }
//
//    public void close() throws IOException {
//        out.close();
//        roadmapAnno.close();
//        tissueAnno.close();
//    }
//
//    public void processLocus(final LocFeature locFeature) throws IOException {
//        final String chr = locFeature.chr;
//        final int pos = locFeature.beg + 1;
//
//        getTissueAnno();
//        loadJannovar();
//
//        VariantAnnotation annotation = jannovarUtils.annotate(chr, pos, locFeature.ref, locFeature.alt);
//        int category = -1;
//        if(annotation != null) {
//            Map<String, String> map = variantAnnoMap.get(annotation.getVariantEffect().toString());
//            category = (int)(Double.parseDouble(map.get("category")));
//        }
//
//        final long[][] cellMarks = roadmapAnno.query(chr, pos);
//
//        BitSet bitSet = tissueAnno.getAnno(chr,pos, locFeature.ref, locFeature.alt);
//        out.writeBytes(String.format("%s\t%d\t%s\t%s\t", chr, pos, locFeature.ref, locFeature.alt));
//        for (long[] cellType: cellMarks) {
//            out.writeBytes(cellType + "\t");
//        }
//        out.writeBytes(category + "\t");
//        out.writeBytes(new String(bitSet.toByteArray()) + "\n");
//    }
}

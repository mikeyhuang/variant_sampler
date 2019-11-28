package org.mulinlab.variantsampler.database;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import htsjdk.samtools.util.StringUtil;
import nl.harmjanwestra.utilities.legacy.genetica.containers.Pair;
import nl.harmjanwestra.utilities.vcf.VCFVariant;
import org.mulinlab.variantsampler.utils.GP;
import org.mulinlab.varnote.operations.readers.db.VannoMixReader;
import org.mulinlab.varnote.operations.readers.db.VannoReader;
import org.mulinlab.varnote.utils.node.LocFeature;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.lang.Double.isNaN;

public final class LDComputer {

    private int distance = GP.LD_DISTANCE;
    private int LAST_END = 2;
    private Integer[] ldBuddies;
    private org.mulinlab.variantsampler.utils.Pair<Integer, Integer>[] posRange;


    private final VannoReader eurReader;
    private Map<String, Variant> variantsMap;
    private String lastChr = "";

    public LDComputer(final String gtDatabase) throws IOException {
        this.eurReader = new VannoMixReader(gtDatabase);
        ldBuddies = new Integer[GP.LD_BUDDIES_SIZE];
        posRange = new org.mulinlab.variantsampler.utils.Pair[GP.GENE_LD_SIZE];

        for (int i = 0; i < ldBuddies.length; i++) {
            ldBuddies[i] = new Integer(0);
        }

        for (int i = 0; i < posRange.length; i++) {
            posRange[i] = new org.mulinlab.variantsampler.utils.Pair(-1, -1);
        }
    }

    public void close() {
        eurReader.close();
    }

    public org.mulinlab.variantsampler.utils.Pair<Integer[], org.mulinlab.variantsampler.utils.Pair<Integer, Integer>[]> compute(final String chr, final int pos, final String refalt) throws IOException {
        if(!chr.equals(lastChr)) {
            System.out.println(chr);
            this.variantsMap = new HashMap<>();
            LAST_END = 2;
        } else {
            removeUseless(pos);
        }

        lastChr = chr;
        loadSNP(chr, pos);
        resetLDBuddies();

        final String queryID = getID(chr, pos, refalt);
        if(variantsMap.get(queryID) != null) {
            final Variant querySNPG = variantsMap.get(queryID);

            double corr, rsq;
            for (String key: variantsMap.keySet()) {
                if(variantsMap.get(key) != null && !key.equals(queryID)) {
                    corr = computeCorrelation(querySNPG.getMatrix(), variantsMap.get(key).getMatrix());

                    if(!isNaN(corr)) {
                        rsq = corr * corr;
                        addLDBuddies(rsq, variantsMap.get(key));
                    }
                }
            }
        } else {
            System.out.println("Not found: " + queryID);
        }
        return new org.mulinlab.variantsampler.utils.Pair<Integer[], org.mulinlab.variantsampler.utils.Pair<Integer, Integer>[]>(ldBuddies, posRange);
    }

    private double computeCorrelation(final double[] querySNPG, final double[] dbGenotypes) {
        Pair<double[], double[]> filtered = stripmissing(querySNPG, dbGenotypes);
        return correlate(filtered.getLeft(), filtered.getRight());
    }

    public double correlate(double[] x, double[] y) {
        if (x.length != y.length) {
            System.out.println("Warning two arrays of non identical length are put in for correlation.");
            System.out.println("Returning NaN");
            return 0.0D / 0.0;
        } else {
            Pair<Double, Double> tmpMean = mean(x, y);
            double meanX = (Double)tmpMean.getLeft();
            double meanY = (Double)tmpMean.getRight();
            double varX = 0.0D;
            double varY = 0.0D;
            double covarianceInterim = 0.0D;

            for(int a = 0; a < x.length; ++a) {
                double varXT = x[a] - meanX;
                double varYT = y[a] - meanY;
                covarianceInterim += varXT * varYT;
                varX += varXT * varXT;
                varY += varYT * varYT;
            }

            varY /= (double)(y.length - 1);
            varX /= (double)(x.length - 1);
            double denominator = Math.sqrt(varX * varY);
            double covariance = covarianceInterim / (double)(x.length - 1);
            double correlation = covariance / denominator;
            return correlation;
        }
    }

    public Pair<Double, Double> mean(double[] v, double[] w) {
        double sumV = 0.0D;
        double sumW = 0.0D;

        for(int k = 0; k < v.length; ++k) {
            sumV += v[k];
            sumW += w[k];
        }

        return new Pair(sumV / (double)v.length, sumW / (double)v.length);
    }

    private void addLDBuddies(final double rsq, final Variant variant) {
        int index = (int)(rsq * 10) - 1;
        if(index >= 0) {
            if(index > 8) index = 8;

            for (int i = 0; i <= index; i++) {
                ldBuddies[i]++;

                if(variant.getPos() < posRange[i].getKey() || posRange[i].getKey() == -1) {
                    posRange[i].setKey(variant.getPos());
                }
                if(variant.getPos() > posRange[i].getValue()) {
                    posRange[i].setValue(variant.getPos());
                }
            }
        }
    }

    private void resetLDBuddies() {
        for (int i = 0; i < ldBuddies.length; i++) {
            ldBuddies[i] = 0;
        }

        for (int i = 0; i < posRange.length; i++) {
            posRange[i].setKey(-1);
            posRange[i].setValue(-1);
        }
    }

    private String getID(final VCFVariant variant) {
        return getID(variant.getChr(), variant.getPos(), StringUtil.join(",", variant.getAlleles() ));
    }

    private String getID(final String chr, final int pos, final String refalt) {
        return String.format("%s_%d_%s", chr, pos, refalt);
    }

    private void removeUseless(final int pos) {
        Iterator<Map.Entry<String, Variant>> iter = variantsMap.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry<String,Variant> entry = iter.next();
            if(entry.getValue().getPos() < (pos - distance)) {
                iter.remove();
            }
        }
    }

    private void loadSNP(final String chr, final int pos) throws IOException {
        final int begin = (pos - distance) > LAST_END ? (pos - distance) : (LAST_END - 1);
        final int end = pos + distance;

        eurReader.query(new LocFeature(begin, end, chr));
        List<String> results = eurReader.getResults();

        VCFVariant variant;
        for (String s:results) {
            variant = new VCFVariant(s, VCFVariant.PARSE.ALL);
            variantsMap.put(getID(variant), new Variant(variant.getChr(), variant.getPos(), convertToDouble(variant)));
        }

        LAST_END = end;
    }

    private Pair<double[], double[]> stripmissing(double[] a, double[] b) {
        boolean[] missing = new boolean[a.length];
        int nrmissing = 0;
        for (int i = 0; i < a.length; i++) {
            if (a[i] == -1 || b[i] == -1) {
                missing[i] = true;
                nrmissing++;
            }
        }
        if (nrmissing == 0) {
            return new Pair<>(a, b);
        } else {
            double[] tmpa = new double[a.length - nrmissing];
            double[] tmpb = new double[a.length - nrmissing];
            int ctr = 0;
            for (int i = 0; i < a.length; i++)
                if (!missing[i]) {
                    tmpa[ctr] = a[i];
                    tmpb[ctr] = b[i];
                    ctr++;
                }

            return new Pair<>(tmpa, tmpb);
        }
    }

    private double[] convertToDouble(final VCFVariant vcfVariant) {
        DoubleMatrix2D alleles = vcfVariant.getGenotypeAllelesAsMatrix2D();
        double[] output = new double[alleles.rows()];
        for (int i = 0; i < alleles.rows(); i++) {
            if (alleles.getQuick(i, 0) == -1) {
                output[i] = -1;
            } else {
                output[i] = (alleles.getQuick(i, 0) + alleles.getQuick(i, 1));
            }
        }

        return output;
    }

    final class Variant {
        final private String chr;
        final private Integer pos;
        final double[] matrix;

        public Variant(String chr, Integer pos, double[] matrix) {
            this.chr = chr;
            this.pos = pos;
            this.matrix = matrix;
        }

        public String getChr() {
            return chr;
        }

        public Integer getPos() {
            return pos;
        }

        public double[] getMatrix() {
            return matrix;
        }
    }
}

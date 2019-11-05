package org.mulinlab.snpsnap.database;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import nl.harmjanwestra.utilities.legacy.genetica.containers.Pair;
import nl.harmjanwestra.utilities.legacy.genetica.math.stats.Correlation;
import nl.harmjanwestra.utilities.vcf.VCFVariant;
import org.mulinlab.varnote.operations.readers.db.VannoMixReader;
import org.mulinlab.varnote.operations.readers.db.VannoReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Double.isNaN;

public final class LDComputer {
    private static final String EUR_DB = "/Users/hdd/Desktop/vanno/random/hg19/1kg.phase3.v5.shapeit2.eur.hg19.all.vcf.gz";
    private static final int distance = (int)(0.1*1000*1000);
    private static int LAST_END = 1;
    private static int[] ldBuddies = new int[]{0,0,0,0,0,0,0,0,0,0};
    private static org.mulinlab.snpsnap.utils.Pair<Integer, Integer>[] posRange = new org.mulinlab.snpsnap.utils.Pair[10];


    private final VannoReader eurReader;
    private Map<String, VCFVariant> variantsMap;
    private final String lastChr = "";

    public LDComputer() throws IOException {
        this.eurReader = new VannoMixReader(EUR_DB);
        for (int i = 0; i < posRange.length; i++) {
            posRange[i] = new org.mulinlab.snpsnap.utils.Pair(0, 0);
        }
    }

    public void compute(final String chr, final int pos, final String rsid) throws IOException {
        if(!chr.equals(lastChr)) {
            this.variantsMap = new HashMap<>();
            LAST_END = 1;
        } else {
            removeUseless(pos);
        }

        loadSNP(chr, pos);
        resetLDBuddies();

        final String queryID = getID(chr, pos, rsid);
        final double[] querySNPG = convertToDouble(variantsMap.get(queryID));

        double corr, rsq;
        System.out.println(queryID);
        for (String key: variantsMap.keySet()) {
            if(variantsMap.get(key) != null && !key.equals(queryID)) {
                corr = computeCorrelation(querySNPG, variantsMap.get(key));

                if(!isNaN(corr)) {
                    rsq = corr * corr;
                    addLDBuddies(rsq, variantsMap.get(key));
                }
            }
        }
        for (int i = ldBuddies.length - 1; i >=0 ; i--) {
            System.out.print(ldBuddies[i] + "\t");
            if(i > 1) ldBuddies[i-1] = ldBuddies[i-1] + ldBuddies[i];
        }
        System.out.println("");
    }

    private double computeCorrelation(final double[] querySNPG, final VCFVariant dbVariant) {
        double[] genotypes = convertToDouble(dbVariant);

        Pair<double[], double[]> filtered = stripmissing(querySNPG, genotypes);
        return Correlation.correlate(filtered.getLeft(), filtered.getRight());
    }

    private void addLDBuddies(final double rsq, final VCFVariant variant) {
        int index = (int)(rsq * 10);
        if(index > 9) index = 9;

        for (int i = 0; i <= index; i++) {
            ldBuddies[index]++;

            if(variant.getPos() < posRange[index].getKey()) {
                posRange[index].setKey(variant.getPos());
            }
            if(variant.getPos() > posRange[index].getValue()) {
                posRange[index].setValue(variant.getPos());
            }
        }
    }

    private void resetLDBuddies() {
        for (int i = 0; i < ldBuddies.length; i++) {
            ldBuddies[i] = 0;
        }

        for (int i = 0; i < posRange.length; i++) {
            posRange[i].setKey(0);
            posRange[i].setValue(0);
        }
    }

    private String getID(final VCFVariant variant) {
        return getID(variant.getChr(), variant.getPos(), variant.getId());
    }

    private String getID(final String chr, final int pos, final String rsid) {
        return String.format("%s_%d_%s", chr, pos, rsid);
    }

    private void removeUseless(final int pos) {
        for (String key: variantsMap.keySet()) {
            if(variantsMap.get(key).getPos() < (pos - distance)) {
                variantsMap.remove(key);
            }
        }
    }

    private void loadSNP(final String chr, final int pos) throws IOException {
        VCFVariant variant;

        final int begin = (pos - distance) > LAST_END ? (pos - distance) : (LAST_END - 1);
        final int end = pos + distance;

        eurReader.query(String.format("%s:%d-%d", chr, begin, end));
        List<String> results = eurReader.getResults();

        for (String s:results) {
            variant = new VCFVariant(s, VCFVariant.PARSE.ALL);
            variantsMap.put(getID(variant), variant);
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
}

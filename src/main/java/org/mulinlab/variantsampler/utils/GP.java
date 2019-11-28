package org.mulinlab.variantsampler.utils;

import org.mulinlab.variantsampler.utils.enumset.*;
import org.mulinlab.varnote.constants.GlobalParameter;

import java.io.IOException;
import java.io.InputStream;

public final class GP {
    public final static String PRO_NAME = "VariantSampler";
    public final static String PRO_CMD = "java -jar /path/to/VariantSampler.jar" ;
    public final static boolean DEFAULT_LOG = true;

    public final static String TAB = "\t";
    public static final boolean COLOR_STATUS;

    static {
        COLOR_STATUS = true;
    }

    public final static int HELP_SIMILARITY_FLOOR = 7;
    public final static int MINIMUM_SUBSTRING_LENGTH = 5;

    public static final int MAX_SHORT_UNSIGNED = 65535;

    public static final int FROM_CHROM = 1;
    public static final int TO_CHROM = 22;
    public static final int MAX_MAF = 50;

    public static final int GENE_DIS_SIZE = 10;
    public static final int GENE_LD_SIZE = 9;
    public static final int LD_BUDDIES_SIZE = 9;
    public static final int ROADMAP_SIZE = 6;

    public static final int GENE_DIS_START = 6;
    public static final int GENE_LD_START = 16;
    public static final int LD_BUDDIES_START = 25;
    public static final int ROADMAP_START = 34;

    public static final int RSID_IDX = 46;
    public static final int CHR_IDX = 0;
    public static final int BP_IDX = 1;
    public static final int REF_IDX = 2;
    public static final int ALT_IDX = 3;
    public static final int MAF_IDX = 4;
    public static final int DIS_IDX = 5;

    public static final int BITSET_SIZE = 64;
    public static final int ROADMAP_LEFT = 64;
    public static final int ROADMAP_RIGHE = 63;

    public static final double MAF_FILTER = 0.01;

    public static final int LD_DISTANCE = (int)(0.1*1000*1000);
    public static final int RANGE_STEP = 5000;

    public static final int NO_MARKER = -1;
    public static final int NO_CELL_TYPE = -1;
    public static final int NO_GENE_DIS = -1;
    public static final int NO_GENE_LD = -1;
    public static final int NO_LD_BUDDIES = -1;

    public static final GeneInDis DEFAULT_GENE_DIS = GeneInDis.KB500;
    public static final LD DEFAULT_GENE_LD = LD.GT5;
    public static final LD DEFAULT_LD_BUDDIES = LD.GT5;


    public static final MAFRange DEFAULT_MAF_RANGE = MAFRange.R5;
    public static final int DEFAULT_DIS_RANGE = 5000;
    public static final int DEFAULT_GENE_DIS_RANGE = 5;
    public static final int DEFAULT_GENE_LD_RANGE = 5;
    public static final int DEFAULT_LD_BUDDIES_RANGE = 50;

    public static final String DEFAULT_OUT_SUFFIX = ".VariantSampler.txt";

    public static float readFloat(InputStream is) throws IOException {
        return Float.intBitsToFloat(GlobalParameter.readInt(is));
    }
}

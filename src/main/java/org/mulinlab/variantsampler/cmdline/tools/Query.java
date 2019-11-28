package org.mulinlab.variantsampler.cmdline.tools;

import org.broadinstitute.barclay.argparser.Argument;
import org.broadinstitute.barclay.argparser.CommandLineProgramProperties;
import org.mulinlab.variantsampler.cmdline.CMDProgram;
import org.mulinlab.variantsampler.cmdline.programGroup.QueryProgramGroup;
import org.mulinlab.variantsampler.utils.GP;
import org.mulinlab.variantsampler.utils.QueryParam;
import org.mulinlab.variantsampler.utils.enumset.*;
import org.mulinlab.varnote.constants.GlobalParameter;
import java.io.File;
import java.io.IOException;

@CommandLineProgramProperties(
        summary = Query.USAGE_SUMMARY + Query.USAGE_DETAILS,
        oneLineSummary = Query.USAGE_SUMMARY,
        programGroup = QueryProgramGroup.class)
public final class Query extends CMDProgram {

    static final String USAGE_SUMMARY = "Query";
    static final String USAGE_DETAILS =
            "\n\nUsage example:" +
                    "\n" +
                    "java -jar " + GlobalParameter.PRO_NAME + ".jar Query -Q input.sort.txt -D EUR.gz \n" ;

    @Argument(fullName = "Database", shortName = "D", doc = "The database file.", optional = false)
    private File databaseFile = null;

    @Argument(fullName = "Query", shortName = "Q", doc = "The query file.", optional = false)
    private File queryFile = null;

    @Argument(fullName = "Output", shortName = "O", doc = "The output file.", optional = true)
    private File outFile = null;

    @Argument(fullName = "GDistance", shortName = "GP", doc = "Gene density of physical distance.", optional = true)
    private GeneInDis geneInDis = GP.DEFAULT_GENE_DIS;

    @Argument(fullName = "GeneInLD", shortName = "GLD", doc = "Gene density of LD SNPs.", optional = true)
    private LD geneInLD = GP.DEFAULT_GENE_LD;

    @Argument(fullName = "LDBuddies", shortName = "LDB", doc = "LD buddies .", optional = true)
    private LD ldBuddies = GP.DEFAULT_LD_BUDDIES;

    @Argument(fullName = "MAFRange", shortName = "MR", doc = "MAF Range.", optional = false)
    private MAFRange mafRange = GP.DEFAULT_MAF_RANGE;

    @Argument(fullName = "DisRange", shortName = "DR", doc = "Range of distance to closest transcription start site.", optional = true)
    private int disRange = GP.DEFAULT_DIS_RANGE;

    @Argument(fullName = "GeneInDisRange", shortName = "GDR", doc = "Range of gene in distance.", optional = true)
    private int geneInDisRange = GP.DEFAULT_GENE_DIS_RANGE;

    @Argument(fullName = "GeneInLDRange", shortName = "GLR", doc = "Range of gene in ld.", optional = true)
    private int geneInLDRange = GP.DEFAULT_GENE_LD_RANGE;

    @Argument(fullName = "ldBuddiesRange", shortName = "LR", doc = "Range of ld buddies.", optional = true)
    private int ldBuddiesRange = GP.DEFAULT_LD_BUDDIES_RANGE;

    @Argument(fullName = "CellType", shortName = "CT", doc = "Roadmap cell type.", optional = true)
    private CellType cellType = null;

    @Argument(fullName = "Marker", shortName = "M", doc = "Roadmap cell type marker.", optional = true)
    private Marker marker = null;

    @Override
    protected int doWork() {
        try {
            QueryParam queryParam = new QueryParam(GeneInDis.getIdx(geneInDis), LD.getIdx(geneInLD), LD.getIdx(ldBuddies), Marker.getIdx(marker), CellType.getIdx(cellType),
                    mafRange, disRange, geneInDisRange, geneInLDRange, ldBuddiesRange);

            org.mulinlab.variantsampler.query.Query query = new org.mulinlab.variantsampler.query.Query(queryFile.getAbsolutePath(), databaseFile.getAbsolutePath(), queryParam, outFile == null ? null : outFile.getAbsolutePath());
            query.doQuery();
            query.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }
}

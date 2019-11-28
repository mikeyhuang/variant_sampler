package org.mulinlab.variantsampler.cmdline.programGroup;

import org.broadinstitute.barclay.argparser.CommandLineProgramGroup;

public class QueryProgramGroup implements CommandLineProgramGroup {

    @Override
    public String getName() { return "Query"; }

    @Override
    public String getDescription() { return "Query related."; }
}
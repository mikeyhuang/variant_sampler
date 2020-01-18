package org.mulinlab.variantsampler.cmdline;

import org.broadinstitute.barclay.argparser.Argument;
import org.mulinlab.varnote.cmdline.collection.TagArgument;
import org.mulinlab.varnote.cmdline.constant.Arguments;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.format.Format;

public final class InputFileArgumentCollection {
    private static final long serialVersionUID = 1L;

    @Argument(
            fullName = Arguments.INTERSECT_INPUT_LONG, shortName = Arguments.INTERSECT_INPUT_SHORT,
            doc = "Path of query file (support plain text or compressed files, including gzip and block gzip).\n\n" +
                    "Possible Tags: {vcf, bed, tab} \n\n" +
                    "Possible attributes for \"tab\" tag: {c, b, e, ref, alt, ci}\n" +
                    "c: column of sequence name (1-based)\n" +
                    "b: column of start chromosomal position (1-based)\n" +
                    "e: column of end chromosomal position (1-based)\n" +
                    "ref: column of reference allele\n" +
                    "alt: column of alternative allele\n" +
                    "0: specify the position in the data file is 0-based rather than 1-based\n" +
                    "ci: comment indicator\n"
    )
    protected TagArgument queryFile = null;

    @Argument(shortName = Arguments.FORMAT_HEADER_SHORT, fullName = Arguments.FORMAT_HEADER_LONG, optional = true,
            doc = "Indicate whether the first line of input is header line.")
    public Boolean hasHeader = GlobalParameter.DEFAULT_HAS_HEADER;

    public String getQueryFilePath() {
        return queryFile.getArgValue();
    }

    public Format getFormat(final String queryFilePath, final boolean isQuery) {
        Format format = queryFile.getFormat();

        if(format == null) format = Format.defaultFormat(queryFilePath, isQuery);
        format = queryFile.setFormat(format);

        if(hasHeader) format.setHasHeaderInFile(true);

        return format;
    }
}

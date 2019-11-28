package org.mulinlab.variantsampler.utils;

import org.apache.commons.cli.MissingArgumentException;
import org.ini4j.InvalidFileFormatException;
import org.mulinlab.varnote.filters.iterator.NoFilterIterator;
import org.mulinlab.varnote.utils.enumset.FileType;

import java.util.HashMap;
import java.util.Map;

public final class DBSource {
    static final char COMMAND = '#';
    static final char SECTION_BEGIN = '[';
    static final char SECTION_END = ']';

    public static final String ROADMAP = "roadmap";
    public static final String DB1000G = "1000G";
    public static final String GENCODE_GENE = "gene_file";
    public static final String EUR_LD_DB = "1000G_eur";
    public static final String EAS_LD_DB = "1000G_eas";
    public static final String AFR_LD_DB = "1000G_afr";
    public static final String OUT_DIR = "output_dir";


    private final String[] required = new String[]{ROADMAP, DB1000G, GENCODE_GENE, EUR_LD_DB, EAS_LD_DB, AFR_LD_DB, OUT_DIR};
    private final Map<String, String> srcMap;

    public DBSource(final String srcFile) throws MissingArgumentException {
        srcMap = new HashMap<>();
        for (String src: required) {
            srcMap.put(src, "");
        }

        NoFilterIterator reader = new NoFilterIterator(srcFile, FileType.TXT);

        String line;
        while(reader.hasNext()) {
            line = reader.next();
            if ((line.charAt(0) != SECTION_BEGIN) && line.charAt(0) != COMMAND) {
                parseLine(line);
            }
        }

        for (String src: required) {
            if(srcMap.get(src).equals("")) {
                throw new MissingArgumentException(String.format("%s database is missing.", src));
            }
        }
        reader.close();
    }

    private void parseLine(final String line) {
        String name = null, value = null;

        int idx = line.indexOf('=');
        if(idx != -1 ) {
            name = line.substring(0, idx).trim();
            value = line.substring(idx+1).trim();

            if(srcMap.get(name) != null) {
                srcMap.put(name, value);
            }
        }
    }

    public String getDatabasePath(final String dbName) {
        return srcMap.get(dbName);
    }
}

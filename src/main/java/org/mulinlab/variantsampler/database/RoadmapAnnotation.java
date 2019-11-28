package org.mulinlab.variantsampler.database;

import htsjdk.tribble.util.ParsingUtils;
import org.mulinlab.variantsampler.utils.GP;
import org.mulinlab.variantsampler.utils.Pair;
import org.mulinlab.variantsampler.utils.enumset.CellType;
import org.mulinlab.variantsampler.utils.enumset.Marker;
import org.mulinlab.varnote.operations.readers.db.VannoMixReader;
import org.mulinlab.varnote.operations.readers.db.VannoReader;
import org.mulinlab.varnote.utils.database.Database;
import org.mulinlab.varnote.utils.node.LocFeature;
import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RoadmapAnnotation {
    private final VannoReader roadReader;
    private final Database db;

    private Map<String, Integer> cellTypes;
    private final Map<String, Integer[]> markTypes;

    private final String[] token = new String[13];
    private final String[] parts = new String[2];

    public RoadmapAnnotation(final String roadmapFile) throws IOException {
        this.roadReader = new VannoMixReader(roadmapFile);
        this.db = this.roadReader.getDb();

        this.markTypes = new HashMap<>();
        this.cellTypes = new HashMap<>();

        int idx = 0;
        for (CellType cell: CellType.values()) {
            cellTypes.put(cell.toString(), idx++);
        }

        for (Marker mark: Marker.values()) {
            this.markTypes.put(mark.toString(), newCells());
        }
    }

    public Integer[] newCells() {
        Integer[] cells = new Integer[cellTypes.size()];
        for (int i = 0; i < cells.length; i++) {
            cells[i] = 0;
        }
        return cells;
    }

    public void clear() {
        for (Integer[] cells:markTypes.values()) {
            for (int i = 0; i < cells.length; i++) {
                cells[i] = 0;
            }
        }
    }

    public void addCellMark(final String cell, final String mark) {
        Integer[] cells = markTypes.get(mark);
        if(cells != null) {
            cells[cellTypes.get(cell)]++;
        }
    }

    public static BitSet convert(long value) {
        BitSet bits = new BitSet();
        int index = 0;
        while (value != 0L) {
            if (value % 2L != 0) {
                bits.set(index);
            }
            ++index;
            value = value >>> 1;
        }
        return bits;
    }

    public static int getValOfIndex(final long left, final long right, final int idx) {
        return RoadmapAnnotation.getValOfIndex(new Pair<>(RoadmapAnnotation.convert(left), RoadmapAnnotation.convert(right)), idx) ? 1 : 0;
    }

    public static boolean getValOfIndex(final Pair<BitSet, BitSet> bitSet, final int idx) {
        if(idx < GP.ROADMAP_LEFT) {
            return bitSet.getKey().get(idx);
        } else {
            return bitSet.getValue().get(idx - GP.ROADMAP_LEFT);
        }
    }

    public static long convert(final BitSet bits) {
        long value = 0L;
        for (int i = 0; i < bits.length(); ++i) {
            value += bits.get(i) ? (1L << i) : 0L;
        }
        return value;
    }

    public Pair<Long, Long> compressOne(final Integer[] cells) {
        BitSet left = new BitSet(GP.BITSET_SIZE), right = new BitSet(GP.BITSET_SIZE);

        for (int i = 0; i < GP.ROADMAP_LEFT; i++) {
            if(cells[i] == 1) left.set(i);
        }
        for (int i = 0; i < GP.ROADMAP_RIGHE; i++) {
            if(cells[i+GP.ROADMAP_LEFT] == 1) right.set(i);
        }

        return new Pair<>(convert(left), convert(right));
    }

    public Pair<Long, Long>[] compress() {
        Pair<Long, Long>[] results = new Pair[markTypes.keySet().size()];

        int index = 0;
        for (String mark:markTypes.keySet()) {
            results[index++] = compressOne(markTypes.get(mark));
        }
        return results;
    }

    public void count() {
        int count = 0;
        Integer[] cells;

        for (String mark:markTypes.keySet()) {
            cells = markTypes.get(mark);
            count = 0;
            for (int i = 0; i < cells.length; i++) {
                if(cells[i] == 1) {
                    count++;
                }
            }
        }
    }

    public void close() {
        roadReader.close();
    }

    public Pair<Long, Long>[] query(final String chr, final int pos) throws IOException {
        clear();

        roadReader.query(new LocFeature(pos-1, pos, chr));
        List<String> results = roadReader.getResults();

        int idx = -1;
        for (String r:results) {
            ParsingUtils.split(r, token, '\t', true);
            ParsingUtils.split(token[10], parts, '-', true);

            addCellMark(parts[0], parts[1]);
        }
        return compress();
    }
}

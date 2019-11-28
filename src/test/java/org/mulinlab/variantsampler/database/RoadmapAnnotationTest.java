package org.mulinlab.variantsampler.database;

import org.apache.commons.cli.MissingArgumentException;
import org.mulinlab.variantsampler.utils.DBSource;
import org.mulinlab.variantsampler.utils.Pair;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.BitSet;

public class RoadmapAnnotationTest {




    @Test
    public void testQuery() throws Exception {
        DBSource dbSource = new DBSource("src/main/resources/db.ini");

        RoadmapAnnotation roadmapAnnotation = new RoadmapAnnotation(dbSource.getDatabasePath(DBSource.ROADMAP));
        Pair<Long, Long>[] r = roadmapAnnotation.query("9", 5453460);

        for (Pair<Long, Long> p: r) {
            System.out.println(p.getKey() + ", " + p.getValue());
        }
        roadmapAnnotation.close();

//        BitSet left = RoadmapAnnotation.convert(-7479878638778852224l);
//        for (int i = 0; i < 64; i++)
//        {
//            System.out.print((left.get(i) ? 1 : 0) + ",") ;
//        }

//        System.out.println(StringUtils.join(cells, ","));

//        for (int i = 0; i < 63; i++)
//        {
//            System.out.print((right.get(i) ? 1 : 0 )+ ",") ;
//        }
//        System.out.println();
//        System.out.println( left + ", " + right);
//        System.out.println( a + ", " + b );
//        System.out.println( convert(a) + ", " + convert(b));
//        System.out.println();
    }
}
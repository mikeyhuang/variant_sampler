package org.mulinlab.variantsampler.query;

import htsjdk.samtools.seekablestream.SeekableStreamFactory;
import htsjdk.samtools.util.BlockCompressedInputStream;
import org.mulinlab.variantsampler.index.Index;
import org.mulinlab.variantsampler.utils.node.Node;

import java.io.*;
import java.util.*;

public final class Query_bak {
    private static final String DBPath = "/Users/hdd/Desktop/vanno/random/match_database.sort.noncds_new.sort.txt.gz";
    private static final int[] mafRange = new int[]{-10, 10};
    private static final int[] disRange = new int[]{-5000, 5000};
    private static final Random r = new Random();

    private static final int LESS = 1;
    private static final int IN = 2;
    private static final int GREATER = 3;
    private static final int NO_MAF = -1;

    private final String input;
    private final BufferedWriter output;

    private final Index index;
    private BlockCompressedInputStream dbFile;
    private Map<Integer, List<AddressNode>> linkMap;
    private Map<Integer, List<AddressNode>> tempMap;
//    private MAF lastMAF = null;


    public Query_bak(final String input) throws IOException {
        this.input = input;
        this.output = new BufferedWriter(new FileWriter(new File(input + ".out")));

        this.dbFile = new BlockCompressedInputStream(SeekableStreamFactory.getInstance().getBufferedStream(
                SeekableStreamFactory.getInstance().getStreamFor(DBPath)));
        this.index = new Index(DBPath);
        this.linkMap = new HashMap<>();
        this.tempMap = new HashMap<>();
    }

    public void close() throws IOException {
        this.dbFile.close();
        this.output.close();
    }

    public void doQuery() throws IOException {
//        NoFilterIterator reader = new NoFilterIterator(input, FileType.TXT);
//
//        List<Node> querys = new ArrayList<>();
//        Node node = null;
//        String seqName = "";
//        int currMAF = NO_MAF;
//
//        while (reader.hasNext()) {
////            node = new Node(reader.next().split(IndexWriter.TAB));
//
//            if(!node.getChr().equals(seqName)) {
//                if(querys.size() > 0) {
//                    find(querys);
//                    querys = new ArrayList<>();
//                }
//
//                this.linkMap = new HashMap<>();
//                this.tempMap = new HashMap<>();
//                lastMAF = null;
//                currMAF = NO_MAF;
//                seqName = node.getChr();
//            }
//
//            if(node.getMaf() != currMAF) {
//                if(querys.size() > 0) {
//                    find(querys);
//                    querys = new ArrayList<>();
//                }
//
//                loadMAF(node.getChr(), node.getMaf());
//                currMAF = node.getMaf();
//            }
//
//            querys.add(node);
//        }
//
//        reader.close();
//        close();
    }

    public void find(final List<Node> querys) throws IOException{
//        Map<Integer, Integer> index = new HashMap<>();
//
//        List<Integer> keyList = null;
//        List<AddressNode> temp;
//        boolean flag;
//        int disFlag;
//
//        for (Node q: querys) {
//            removeAddrssNodes(q.getDistance());
//            index = readAddrssNodes(q.getDistance(), index);
//
//            keyList = new ArrayList<>();
//            for (Integer key: tempMap.keySet()) {
//                temp = tempMap.get(key);
//                flag = false;
//                if(temp != null && temp.size() > 0) {
//                    for (AddressNode node: temp) {
//                        disFlag = isInRangeDistance(q.getDistance(), node.distance);
//                        if(disFlag == IN) {
//                            flag = true;
//                            break;
//                        } else if(disFlag == GREATER) {
//                            flag = false;
//                            break;
//                        }
//                    }
//                }
//                if(flag) keyList.add(key);
//            }
//            output.write(String.format("%s\t%d\t%s\t%s\t%d\t%s\n", q.getChr(), q.getBp(), q.getRsid(), q.getMafs(), q.getDistance(), randomSelect(keyList)));
//        }
    }

    public String randomSelect(List<Integer> keyList) throws IOException {
        if(keyList.size() > 0)  {
            final int index = r.nextInt(keyList.size());
            List<AddressNode> temp = tempMap.get(keyList.get(index));
            final AddressNode node = temp.get(r.nextInt(temp.size()));
            dbFile.seek(node.address);

            return dbFile.readLine();
        } else {
            return "No data";
        }
    }

    public void removeAddrssNodes(final int distance) {
//        int flag;
//        List<AddressNode> temp;
//
//        for (Integer maf: tempMap.keySet()) {
//            temp = tempMap.get(maf);
//            for (int i = 0; i < temp.size(); i++) {
//                flag = isInRangeDistance(distance, temp.get(i).distance);
//                if(flag == LESS) {
//                    temp.remove(i);
//                    i--;
//                } else {
//                    break;
//                }
//            }
//        }
    }

    public Map<Integer, Integer> readAddrssNodes(final int distance, final Map<Integer, Integer> indexMap) {
        List<AddressNode> temp, dbNodes;
        AddressNode dbNode;
        int flag;
        int index;

//        for (Integer maf: linkMap.keySet()) {
//            dbNodes = linkMap.get(maf);
//            if(dbNodes != null && dbNodes.size() > 0) {
//                if(tempMap.get(maf) != null) {
//                    temp = tempMap.get(maf);
//                } else {
//                    temp = new LinkedList<>();
//                }
//
//                index = indexMap.get(maf) == null ? 0 : indexMap.get(maf);
//                if(index >= 0) {
//                    loop1: for (int i = index; i < dbNodes.size(); i++) {
//
//                        dbNode = dbNodes.get(i);
//                        flag = isInRangeDistance(distance, dbNode.distance);
//                        if(flag == IN) {
//                            temp.add(dbNode);
//                        } else if(flag == GREATER) {
//                            indexMap.put(maf, i - 1);
//                            break loop1;
//                        }
//
//                    }
//                    tempMap.put(maf, temp);
//                }
//
//            }
//        }

        return indexMap;
    }




    public void loadDB(final long address, final int maf) throws IOException {
//        if(linkMap.get(maf) != null) return;
//
//        dbFile.seek(address);
//
//        String line;
//        Node dbnode = null;
//        List<AddressNode> addrList;
//        long filepointer = address;
//
//        while ((line = dbFile.readLine()) != null) {
//            dbnode = new Node(line.split(IndexWriter.TAB));
//            if(dbnode.getMaf() != maf) break;
//
//            if(linkMap.get(maf) == null) {
//                addrList = new ArrayList<>();
//            } else {
//                addrList = linkMap.get(maf);
//            }
//
//            addrList.add(new AddressNode(dbnode.getDistance(), filepointer));
//            linkMap.put(maf, addrList);
//            filepointer = dbFile.getFilePointer();
//        }
    }



    class AddressNode {
        private int distance;
        private long address;

        public AddressNode(final int distance, final long address) {
            this.distance = distance;
            this.address = address;
        }
    }
}

package com.mg.search;

import java.io.*;
import java.util.Arrays;
import java.util.BitSet;

public class FileSearch {

    static String FILEPATH="src/com/mg/search/nums.dat";
    static File file=new File(FILEPATH);
    static final int TWO_ON_FIFTEENTH=(int) Math.pow(2, 15);
    static final int TWO_ON_SIXTEENTH=(int) Math.pow(2, 16);

    public static void main (String[]args) {

        try {
            System.out.println(bitSearchForNonPresentInt(file));
            System.out.println(bucketCount(file));

        } catch (IOException e) {
            System.out.println("Couldn't reach nums.dat" + e.getMessage());
            e.printStackTrace();
        }
    }

    /*
    Létrehozunk egy BitSet-et, amely leképezi (majdnem) az összes lehetséges int számot.
    Miközben egyszer végiglapozzuk a file-t,
    minden beolvasott int esetén a hozzá tartozó megfelelő indexszel ellátott bit értékét igazra állítja.
    Ezután megkeressük az első hamis értékű bitet és visszaadjuk a hozzá tartozó intet.
    A file-t egyszer kell végiglapozni O(n) I/O komplexitású.
    A BitSet-nek (2^32) bitre van szüksége, ami 0,5 GB memóriaigényt jelent.
     */
    static int bitSearchForNonPresentInt(File file) throws IOException {
        BitSet nonNegIntegers=new BitSet(Integer.MAX_VALUE);
        BitSet negIntegers=new BitSet(Integer.MAX_VALUE);
        boolean EOF=false;
        try (FileInputStream input=new FileInputStream(file);
             DataInputStream din=new DataInputStream(input))
        {
            while (!EOF) {
                try {
                    int num=din.readInt();
                    if (num == Integer.MAX_VALUE || num == Integer.MIN_VALUE || num == Integer.MIN_VALUE + 1)
                        continue; // nem létező indexet hívna.
                    if (num >= 0) {
                        nonNegIntegers.flip(num);
                    } else {
                        negIntegers.flip(-1 * num - 1);
                    }
                } catch (EOFException e) {
                    EOF=true;
                }
            }
        }

        for (int i=0; i < Integer.MAX_VALUE; i++) {
            if (!nonNegIntegers.get(i)) return i;
            if (!negIntegers.get(i)) return -1 * (i + 1);
        }

        return Integer.MAX_VALUE; // A legnagyobb int jelenléte nem vizsgált, de 4000000000 int esetében nem érjük el ezt a returnt.
    }

    /*
    Létrehozunk egy int tömböt, amely számon tartja,
    hogy hány inttel nem találkoztunk a tartományon belül.
    Miközben egyszer végiglapozzuk a file-t,
    minden beolvasott int esetén a neki megfelelő kalapban levő int értékét eggyel csökkentjük.
    Ezután megkeressük az első nem nulla értékű intet az int tömbünkből
    és a hozzá tartozó tartományt vizsgáljuk a bitSetLookUp metódussal.
    A file-t egyszer kell végiglapozni ebben a metódusban,
    és egyszer a bitSetLookUp metódusban, O(2n) I/O komplexitású.
    A bucketCount és a bitSetLookUp metódusnak is egy 2^16 nagyságrendű tömbre van szüksége.
    Előbbinek intben, utóbbinak bitben. Az össz memóriaigény ~0,5 MB.
     */
    static int bucketCount(File file) throws IOException {
        int[] buckets=new int[TWO_ON_SIXTEENTH];
        Arrays.fill(buckets, TWO_ON_SIXTEENTH);
        boolean EOF=false;
        try (FileInputStream input=new FileInputStream(file);
             DataInputStream din=new DataInputStream(input))
        {
            while (!EOF) {
                try {
                    int num=din.readInt();
                    if (num < 0) buckets[(num - Integer.MIN_VALUE) / TWO_ON_SIXTEENTH]--;
                    else buckets[(num) / TWO_ON_SIXTEENTH + TWO_ON_FIFTEENTH]--;
                } catch (EOFException e) {
                    EOF=true;
                }
            }
        }
        for (int i=0; i < buckets.length; i++) {
            if (buckets[i] > 0) return bitSetLookUp(file, i);
        }
        return 0; // 4000000000 int esetében nem érjük el ezt a pontot.
    }

    /*
    Elvben ugyanaz, mint a bitSearchForNonPresentInt,
    de itt a teljes lehetséges integerek helyett csak egy olyan szűkített tartományt vizsgálunk,
    amelyen belül bizonyosan találunk nem jelenlevő intet.
    A
     */
    static int bitSetLookUp(File file, int bucketNumber) throws IOException {
        BitSet IntsOfBucket=new BitSet(TWO_ON_SIXTEENTH);
        int lowLimit;
        int highLimit;
        if (bucketNumber < TWO_ON_FIFTEENTH) {
            lowLimit=bucketNumber * TWO_ON_SIXTEENTH + Integer.MIN_VALUE;
        } else {
            lowLimit=(bucketNumber - TWO_ON_FIFTEENTH) * TWO_ON_SIXTEENTH;
        }
        highLimit=lowLimit + TWO_ON_SIXTEENTH - 1; // inclusive
        boolean EOF=false;
        try (FileInputStream input=new FileInputStream(file);
             DataInputStream din=new DataInputStream(input))
        {
            while (!EOF) {
                try {
                    int num=din.readInt();
                    if (num >= lowLimit && num <= highLimit) {
                        IntsOfBucket.flip(num - lowLimit);
                    }
                } catch (EOFException e) {
                    EOF=true;
                }
                for (int i=0; i < TWO_ON_SIXTEENTH; i++) {
                    if (!IntsOfBucket.get(i)) return i;
                }
            }
        }
        return Integer.MAX_VALUE; // A legnagyobb int jelenléte nem vizsgált, de 4000000000 int esetében nem érjük el ezt a returnt.
    }
}

package ga;

import com.google.common.collect.Lists;
import common.CommonUtil;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

class GeneAlgorithmTest {

    @Test
    void doubleToInt(){
        System.out.println(Math.ceil(0.1));
        System.out.println(Math.ceil(-0.1));
        System.out.println((int) -0.1d);
    }

    @Test
    void selectedTable(){
        int populationSize = 1;
        int tableSize = populationSize*(populationSize+1)/2;
        int[] selectTable = new int[tableSize];
        for (int i = 0, p = 0; i < populationSize; i++) {
            for (int j = 0; j < i + 1; j++) {
                selectTable[p++] = i;
            }
        }
        System.out.println(Arrays.toString(selectTable));
    }

    @Test
    void sorted(){
        Chromosome c1 = Chromosome.builder().score(-1.5d).build();
        Chromosome c2 = Chromosome.builder().score(-1.1d).build();
        Chromosome c3 = Chromosome.builder().score(-3.5d).build();
        Chromosome c4 = Chromosome.builder().score(-2.5d).build();
        ArrayList<Chromosome> origin = Lists.newArrayList(c1, c2, c3, c4);
        System.out.println(origin);
        origin.sort(CommonUtil::chromoComparator);
        System.out.println(origin);
    }

    @Test
    void crossover(){
        int genesLength = 20;
        int[] init = new int[genesLength];
        for (int i = 0; i < genesLength; i++) {
            init[i] = i;
        }
        int[] parent1 = CommonUtil.shuffleIntArray(init);
        int[] parent2 = CommonUtil.shuffleIntArray(init);

        //闭区间
        int[] twoPoints = CommonUtil.randomTwoPoints(genesLength);

        int[] offspring1 = new int[genesLength];
        int[] offspring2 = new int[genesLength];


        int[] exchangeTable = init.clone();
        for (int i = twoPoints[0]; i < twoPoints[1]; i++) {
            int a = exchangeTable[parent1[i]];
            int b = exchangeTable[parent2[i]];
            exchangeTable[a] = b;
            exchangeTable[b] = a;
        }

        for (int i = 0; i < twoPoints[0]; i++) {
            offspring1[i] = exchangeTable[parent1[i]];
            offspring2[i] = exchangeTable[parent2[i]];
        }
        for (int i = twoPoints[0]; i <= twoPoints[1]; i++) {
            offspring1[i] = parent2[i];
            offspring2[i] = parent1[i];
        }
        for (int i = twoPoints[1]+1; i < genesLength; i++) {
            offspring1[i] = exchangeTable[parent1[i]];
            offspring2[i] = exchangeTable[parent2[i]];
        }

        System.out.println(Arrays.toString(init));
        System.out.println(Arrays.toString(parent1));
        System.out.println(Arrays.toString(parent2));
        System.out.printf("point1=%s, point2=%s%n",twoPoints[0],twoPoints[1]);
        System.out.println(Arrays.toString(offspring1));
        System.out.println(Arrays.toString(offspring2));
    }
}
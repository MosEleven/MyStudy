package common;

import ga.Chromosome;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

@UtilityClass
public class CommonUtil {

    public int[] shuffleIntArray(int[] nums){
        int len = nums.length;
        int[] shuffled = nums.clone();
        Random random = new Random();
        for (int i = len; i > 0; i--) {
            int change = random.nextInt(i);
            swapIntArray(shuffled,change,i-1);
        }
        return shuffled;
    }

    //从nums中任选n个数
    public int[] randomSelect(int[] nums, int n){
        int len = nums.length;
        int[] selected = nums.clone();
        Random random = new Random();
        for (int i = 0; i < n; i++) {
            int r = random.nextInt(len-i);
            swapIntArray(selected,i,i+r);
        }
        return Arrays.copyOf(selected,n);
    }

    public void swapIntArray(int[] nums, int a, int b){
        int temp = nums[a];
        nums[a] = nums[b];
        nums[b] = temp;
    }

    public int chromoComparator(Chromosome a, Chromosome b){
        double d = a.getScore() - b.getScore();
        if (d > 0) return 1;
        else if (d < 0) return -1;
        return 0;
    }
    /*public int chromoComparator(Chromosome a, Chromosome b){
        return a.getScore().compareTo(b.getScore());
    }*/

    public int[] randomTwoPoints(int n){
        if (n<2) throw new IllegalArgumentException("random two point should accept a param greater than 1");
        Random random = new Random();
        int a = random.nextInt(n);
        int b = random.nextInt(n);
        while (b==a) b = random.nextInt(n);
        int[] res = new int[2];
        if (a<b){
            res[0] = a;res[1] = b;
        }else {
            res[0] = b;res[1] = a;
        }
        return res;
    }

    public <T> List<List<T>> splitList(List<T> list, Predicate<T> predicate){
        List<List<T>> result = new ArrayList<>();
        List<T> l = new ArrayList<>();
        for (T t : list) {
            if (predicate.test(t)){
                l.add(t);
            }else {
                result.add(l);
                l = new ArrayList<>();
            }
        }
        if (!l.isEmpty()){
            result.add(l);
        }
        return result;
    }
}

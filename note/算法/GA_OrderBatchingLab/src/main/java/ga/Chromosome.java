package ga;

import lombok.Builder;
import lombok.Data;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@Data
public class Chromosome {

    //适应度数
    private double score;

    private int rank;

    private int[] genes;

    public Chromosome cloneGenes() {
        return Chromosome.builder().genes(genes.clone()).build();
    }

    public <T> List<T> getListFromGenes(List<T> dateList){
        return Arrays.stream(genes).mapToObj(dateList::get).collect(Collectors.toList());
    }
}

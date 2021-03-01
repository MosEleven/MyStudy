package ga;

import lombok.Data;

import java.util.List;

@Data
public class Population {

    //种群大小（即染色体数量）
    private int size;

    //种群
    private List<Chromosome> chromosomes;
}

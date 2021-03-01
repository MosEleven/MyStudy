package ga;

import java.util.List;

public interface GaCalculate<T> {

    double calFitness(List<T> dataList);

    boolean checkData(List<T> dataList);

    void debug(List<T> dataList);
}

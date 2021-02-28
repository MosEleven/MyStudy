package entity;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Data
public class Batch {

    //优先级
    private int priority;

    //波次号（第几次分批）
    private int waveNo;

    //类型【0：单品】【1：多品】
    private int type;

    //各分区服务时间最大值
    private BigDecimal tService;

    //各分区平均服务时间
    private double tAverageService;

    //todo 该批次预计完成时间，开始时间
    private BigDecimal startTime;

    private List<Order> orderList;

    //当前批次拣选明细集合
    private List<Area> areaList;
}

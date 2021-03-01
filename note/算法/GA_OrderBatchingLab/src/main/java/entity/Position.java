package entity;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Position {

    //分区（共4个）
    private char area;

    //巷道（0到2）
    private int tunnel;

    //货格（从1到20）
    private int shelf;
}

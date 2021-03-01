package service;

import entity.Position;

import java.math.BigDecimal;

public class BaseInfo {

    //区域数量
    public static final int areaNum = 4;

    //巷道数量
    public static final int tunnelNum = 3;

    //每个巷道货位数量
    public static final int shelfNum = 20;

    //仓库内总sku种类
    public static final int totalSkuCategories = areaNum*tunnelNum*shelfNum;

    //起始区域名
    public static final char startArea = 'A';

    //每个区域分区时最大拣选数量
    public static final int threshold = 10;

    //巷道间的宽度
    public static final double tunnelWidth = 3.6d;

    //巷道长度
    public static final double tunnelLength = 11.0d;

    //每个货位长度
    public static final double shelfLength = 1.1d;

    //行走速度
    public static final double vTravel = 1.0d;

    //基础拣选时间（秒）
    public static final double vBasePick = 12.0d;

    //每件拣选所需时间（秒/个）
    public static final double vAdditionPick = 1.0d;

    //打包时间（秒）
    public static final double vBasePack = 15.0d;

    //找出每件货的时间（秒/件）
    public static final double vAdditionPack = 2.0d;

    //货位管理
    public static final Position[] positions;

    public static final int carNum = 4;

    public static final int carShift = 4;

    public static final BigDecimal[][] departTime;

    static {
        positions = new Position[totalSkuCategories];
        for (int i = 0; i < areaNum; i++) {
            char area = (char) ('A'+i);
            for (int tunnel = 0; tunnel < tunnelNum; tunnel++) {
                for (int shelf = 0; shelf < shelfNum; shelf++) {
                    int n = i*tunnelNum*shelfNum + tunnel*shelfNum + shelf;
                    positions[n] = Position.builder().area(area).tunnel(tunnel).shelf(shelf+1).build();
                }
            }
        }

        departTime = new BigDecimal[carNum][carShift];
        departTime[0][0] = BigDecimal.valueOf(7200);
        departTime[0][1] = BigDecimal.valueOf(14400);
        departTime[1][0] = BigDecimal.valueOf(5400);
        departTime[1][1] = BigDecimal.valueOf(12600);
        departTime[1][2] = BigDecimal.valueOf(19800);
        departTime[2][0] = BigDecimal.valueOf(6840);
        departTime[2][1] = BigDecimal.valueOf(10400);
        departTime[2][2] = BigDecimal.valueOf(14040);
        departTime[2][3] = BigDecimal.valueOf(17640);
        departTime[3][0] = BigDecimal.valueOf(7200);
        departTime[3][1] = BigDecimal.valueOf(10800);
        departTime[3][2] = BigDecimal.valueOf(14400);
    }
}

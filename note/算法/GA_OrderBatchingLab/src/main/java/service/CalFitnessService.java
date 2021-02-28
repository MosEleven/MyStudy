package service;

import entity.*;
import ga.GaCalculate;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

import static service.BaseInfo.*;


public class CalFitnessService implements GaCalculate<Order> {

    private final DecimalFormat df = new DecimalFormat("####.00");

    @Override
    public boolean checkData(List<Order> dataList) {
        List<Batch> batches = batching(dataList);
        return batches.size() > 1;
    }

    @Override
    public double calFitness(List<Order> orderList){

        List<Batch> batches = batching(orderList);
        calTService(batches);
        calTDelay(batches);

        BigDecimal tTotalService = batches.stream().map(Batch::getTService).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal tTotalDelay = orderList.stream().map(Order::getTDelay).reduce(BigDecimal.ZERO, BigDecimal::add);

        double power = tTotalService.add(tTotalDelay).doubleValue();

        //计算适应度
        double base = 0d;
        for (Batch batch : batches) {
            double average = batch.getTAverageService();
            base += batch.getAreaList().stream().mapToDouble(a->Math.abs(average-a.getTService())).reduce(0d,Double::sum);
        }

        double score = Math.log(base) * power;
        if (Double.MAX_VALUE <= score){
            System.out.printf("base:%s,power:%s%n",base,power);
            throw new IllegalArgumentException("double溢出,值为："+score);
        }
        return -score;
    }

    @Override
    public void debug(List<Order> orderList) {
        List<Batch> batches = batching(orderList);
        calTService(batches);
        calTDelay(batches);

        BigDecimal tTotalService = batches.stream().map(Batch::getTService).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal tTotalDelay = orderList.stream().map(Order::getTDelay).reduce(BigDecimal.ZERO, BigDecimal::add);

        double power = tTotalService.add(tTotalDelay).doubleValue();

        //计算适应度
        double base = 0d;
        for (Batch batch : batches) {
            double average = batch.getTAverageService();
            base += batch.getAreaList().stream().mapToDouble(a->Math.abs(average-a.getTService())).reduce(0d,Double::sum);
        }

        System.out.printf("拣选时间：%s，延迟时间：%s，差异时间：%s%n",df.format(tTotalService),df.format(tTotalDelay),df.format(base));
    }

    //todo 优化一下使用aop打印分批结果
    public List<Batch> batching(List<Order> orderList){
        List<Batch> batches = new ArrayList<>();
        List<Order> list = new ArrayList<>();
        int[] areas = new int[areaNum];
        for (Order order : orderList) {
            boolean exceed = false;
            for (OrderDetail detail : order.getDetailList()) {
                int area = detail.getPosition().getArea() - startArea;
                areas[area]++;
                if (areas[area] > threshold){
                    exceed = true;
                    break;
                }
            }
            if (exceed){
                batches.add(Batch.builder().orderList(list).build());
                list = new ArrayList<>();
                for (int i = 0; i < areaNum; i++) {
                    areas[i] = 0;
                }
            }
            list.add(order);
        }
        if (!list.isEmpty()) batches.add(Batch.builder().orderList(list).build());
        return batches;
    }

    public void calTService(List<Batch> batches){
        for (Batch batch : batches) {
            //先分区
            Map<Character, Area> areaMap = divideArea(batch);
            batch.setAreaList(new ArrayList<>(areaMap.values()));

            //计算每个区的服务时间时间
            batch.getAreaList().forEach(this::calAreaTService);

            //计算这个批次的服务时间和平均时间
            double tTotalService = 0.0d;
            double tMaxService = 0.0d;
            for (Area area : batch.getAreaList()) {
                tTotalService = tTotalService + area.getTService();
                if (area.getTService() > tMaxService){
                    tMaxService = area.getTService();
                }
            }
            batch.setTService(new BigDecimal(tMaxService));
            batch.setTAverageService(tTotalService / batch.getAreaList().size());
        }
    }

    public void calTDelay(List<Batch> batches){
        BigDecimal tQueue = BigDecimal.ZERO;
        for (Batch batch : batches) {
            tQueue = tQueue.add(batch.getTService());
            for (Order order : batch.getOrderList()) {
                calOrderTPackage(order);
                order.setFinalTime(TimeSystem.pickFinishTime.add(tQueue).add(order.getTPackage()));
            }
        }
    }

    private Map<Character, Area> divideArea(Batch batch){
        Map<Character, Area> map = new HashMap<>(10);
        for (int i = 0; i < areaNum; i++) {
            char areaName = (char) ('A'+i);
            Area area = new Area();
            area.setDetailList(new ArrayList<>());
            area.setPickedTunnelSet(new HashSet<>());
            map.put(areaName,area);
        }
        for (Order order : batch.getOrderList()) {
            for (OrderDetail detail : order.getDetailList()) {
                Position position = detail.getPosition();
                Area area = map.get(position.getArea());
                area.getDetailList().add(detail);
                int tunnel = position.getTunnel();
                if (tunnel >= area.getMaxTunnelNo()){
                    int shelf = position.getShelf();
                    if (shelf > area.getFarthestShelf()){
                        area.setFarthestShelf(shelf);
                    }
                    area.setMaxTunnelNo(tunnel);
                }
                area.getPickedTunnelSet().add(tunnel);
            }
        }
        return map;
    }

    private void calAreaTService(Area area){
        List<OrderDetail> detailList = area.getDetailList();
        if (!detailList.isEmpty()){
            calAreaTTravel(area);
            calAreaTPick(area);
            area.setTService(area.getTTravel() + area.getTPick());
        }
    }

    private void calAreaTTravel(Area area){
        int pickedTunnelNum = area.getPickedTunnelNum();
        double distance;
        if (pickedTunnelNum%2 == 0){
            distance = tunnelWidth *area.getMaxTunnelNo()*2 + pickedTunnelNum*tunnelLength;
        }else {
            distance = tunnelWidth *area.getMaxTunnelNo()*2 + (pickedTunnelNum - 1)*tunnelLength + 2*shelfLength*area.getFarthestShelf();
        }
        area.setTTravel(distance / vTravel);
    }
    private void calAreaTPick(Area area){
        double tPick = 0d;
        for (OrderDetail detail : area.getDetailList()) {
            tPick += vBasePick + detail.getPickNum()*vAdditionPick;
        }
        area.setTPick(tPick);
    }

    private void calOrderTPackage(Order order){
        double tPackage = vBasePack;
        if (order.getSkuNum() > 1){
            tPackage += order.getSkuNum() * vAdditionPack;
        }
        order.setTPackage(new BigDecimal(tPackage));
    }
}

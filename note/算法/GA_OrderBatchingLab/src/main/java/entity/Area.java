package entity;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class Area {

    private double tService;
    private double tTravel;
    private double tPick;

    private Set<Integer> pickedTunnelSet;
    private int pickedTunnelNum;
    private int maxTunnelNo;
    private int farthestShelf;

    private List<OrderDetail> detailList;

    public int getPickedTunnelNum(){
        return pickedTunnelSet.size();
    }
}

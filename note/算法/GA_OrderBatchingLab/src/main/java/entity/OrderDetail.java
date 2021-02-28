package entity;


import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class OrderDetail {

    private int sku;

    private int pickNum;

    private Position position;
}

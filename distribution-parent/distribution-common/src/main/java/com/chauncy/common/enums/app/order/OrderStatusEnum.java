package com.chauncy.common.enums.app.order;

import com.chauncy.common.enums.BaseEnum;
import lombok.Getter;

import java.util.Objects;

/**
 * 支付单状态枚举类
 * @Author zhangrt
 * @Date 2019/7/22 22:50
 **/
@Getter
public enum OrderStatusEnum implements BaseEnum {


    NEED_PAY(0,"未支付"),
    NEED_SEND_GOODS(1,"待发货"),
    NEED_RECEIVE_GOODS(2,"待收货"),
    NEED_EVALUATE(3,"待评价"),
    ALREADY_FINISH(4,"已完成"),
    NEED_USE(5,"待使用"),
    ALREADY_USE(6,"已使用"),
    ALREADY_CANCEL(7,"已取消");

    private Integer id;

    private String name;

    OrderStatusEnum(Integer id, String name) {
        this.id = id;
        this.name = name;
    }


    //通过name获取结果
    public static OrderStatusEnum getOrderStatusEnum(Integer id) {
        for (OrderStatusEnum orderStatusEnum : OrderStatusEnum.values()) {
            if (orderStatusEnum.getId().equals(id))
                return orderStatusEnum;
        }
        return null;
    }


    @Override
    public boolean isExist(Object field) {
        return Objects.nonNull(getOrderStatusEnum(Integer.valueOf(field.toString())));
    }
}

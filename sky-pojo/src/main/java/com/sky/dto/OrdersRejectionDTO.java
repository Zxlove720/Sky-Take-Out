package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrdersRejectionDTO implements Serializable {

    // 订单id
    private Long id;

    //订单拒绝原因
    private String rejectionReason;

}

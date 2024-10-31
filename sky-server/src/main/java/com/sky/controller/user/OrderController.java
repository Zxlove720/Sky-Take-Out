package com.sky.controller.user;


import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


// 在一切类似电商系统的程序中，用户是通过下单的方式通知商家，用户已经购买了某商品，需要商家备货和发货，用户下单之后会产生
// 订单相关数据，订单数据必须包含以下信息：
// 1.购买了哪些商品    2.每一个商品的数量是多少   3.订单总金额是多少  4.下单的用户是谁   5.收货的地址是哪   6.用户的手机号

// 在本系统中，用户将菜品或套餐加入购物车之后，可以通过结算按钮，跳转到订单确认页面，然后进行支付操作
// 点餐业务逻辑：1.购物车 ———— 2.提交订单 ———— 3.支付订单 ———— 4.下单成功

// 完成订单提交业务需要两个表，一个是order订单表；另一个是order_detail订单明细表。用户提交订单时，需要在order表中插入一条记录；需要在
// order_detail表中插入一条或多条记录，代表这次订单的明细

@RestController("userOrderController")
@RequestMapping("/user/order")
@Slf4j
@Api(tags = "用户端-订单接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 提交订单
     *
     * @param ordersSubmitDTO
     * @return
     */
    @PostMapping("/submit")
    @ApiOperation("用户下单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        log.info("用户下单：{}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }
}

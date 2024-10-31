package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 地址簿：是消费者用户的地址信息，当用户成功登录后才可以维护自己的地址信息，同一个用户可以有多个地址信息，但是只能有一个默认地址
// 地址簿设计：
// 新增地址 查询登录用户保存的所有地址 查询登录用户的默认地址 根据地址id修改地址 根据地址id删除地址 根据地址id查询地址 设置默认地址

@RestController
@RequestMapping("/user/addressBook")
@Api(tags = "客户端地址簿接口")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 查询当前登录用户的所有地址信息
     *
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("查询当前登录用户的所有地址信息")
    public Result<List<AddressBook>> list() {
        // 前端不会为这个请求携带任何参数，这个请求只需要用户id，但是每次请求都会携带一个token，可以从中获得用户id
        // 创建AddressBook对象，并封装userId，方便在Service中使用
        AddressBook addressBook = new AddressBook();
        addressBook.setUserId(BaseContext.getCurrentId());
        List<AddressBook> list = addressBookService.list(addressBook);
        return Result.success(list);
    }

    /**
     * 在地址簿中新增地址
     *
     * @param addressBook
     * @return
     */
    @PostMapping
    @ApiOperation("新增地址")
    public Result save(@RequestBody AddressBook addressBook) {
        // 前端请求会携带json数据，只需要将AddressBook对象给Service处理即可
        addressBookService.save(addressBook);
        return Result.success();
    }

}

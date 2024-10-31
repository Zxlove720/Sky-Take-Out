package com.sky.mapper;

import com.sky.entity.AddressBook;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AddressMapper {

    /**
     * 查询当前登录用户的所有地址
     *
     * @param addressBook
     * @return
     */
    List<AddressBook> list(AddressBook addressBook);
}

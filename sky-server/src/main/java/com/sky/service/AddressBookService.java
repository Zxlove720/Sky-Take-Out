package com.sky.service;

import com.sky.entity.AddressBook;

import java.util.List;

public interface AddressBookService {

    /**
     * 查询当前登录用户的所有地址
     *
     * @param addressBook
     * @return
     */
    List<AddressBook> list(AddressBook addressBook);

}

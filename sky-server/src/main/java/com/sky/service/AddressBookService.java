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

    /**
     * 在地址簿中新增地址
     *
     * @param addressBook
     */
    void save(AddressBook addressBook);

    /**
     * 根据地址id查询地址
     *
     * @param id
     * @return
     */
    AddressBook getById(Long id);
}

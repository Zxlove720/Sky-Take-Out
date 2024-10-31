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

    /**
     * 根据地址id修改地址
     *
     * @param addressBook
     */
    void update(AddressBook addressBook);

    /**
     * 将一个地址设置为默认地址
     *
     * @param addressBook
     */
    void setDefault(AddressBook addressBook);

    /**
     * 根据地址id删除地址
     *
     * @param id
     */
    void deleteById(Long id);

    /**
     * 查询默认地址
     *
     * @return
     */
    AddressBook getDefault();
}

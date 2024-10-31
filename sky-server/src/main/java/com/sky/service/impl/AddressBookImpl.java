package com.sky.service.impl;

import com.sky.entity.AddressBook;
import com.sky.mapper.AddressMapper;
import com.sky.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Slf4j
public class AddressBookImpl implements AddressBookService {

    @Autowired
    private AddressMapper addressMapper;

    /**
     * 查询当前登录用户的所有地址
     *
     * @param addressBook
     * @return
     */
    @Override
    public List<AddressBook> list(AddressBook addressBook) {
        return addressMapper.list(addressBook);
    }
}

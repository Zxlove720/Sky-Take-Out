package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Slf4j
public class AddressBookImpl implements AddressBookService {

    @Autowired
    private AddressBookMapper addressBookMapper;

    /**
     * 查询当前登录用户的所有地址
     *
     * @param addressBook
     * @return
     */
    @Override
    public List<AddressBook> list(AddressBook addressBook) {
        log.info("查询当前登录用户的所有地址：{}", addressBook);
        return addressBookMapper.list(addressBook);
    }

    /**
     * 在地址簿中新增地址
     *
     * @param addressBook
     */
    @Override
    public void save(AddressBook addressBook) {
        log.info("在地址簿中新增地址：{}", addressBook);
        // 封装用户id
        Long userId = BaseContext.getCurrentId();
        addressBook.setUserId(userId);
        // 是否是默认地址（新地址默认不是默认地址）
        addressBook.setIsDefault(0);
        addressBookMapper.insert(addressBook);
    }

    /**
     * 根据地址id查询地址
     *
     * @param id
     * @return
     */
    @Override
    public AddressBook getById(Long id) {
        log.info("根据地址id查询地址：{}", id);
        return addressBookMapper.getById(id);
    }

    /**
     * 根据地址id修改地址
     *
     * @param addressBook
     */
    @Override
    public void update(AddressBook addressBook) {
        log.info("根据地址id修改地址：{}", addressBook);
        addressBookMapper.update(addressBook);
    }

    /**
     * 将一个地址设置为默认地址
     *
     * @param addressBook
     */
    @Override
    public void setDefault(AddressBook addressBook) {
        log.info("设置默认地址：{}", addressBook);
        // 为了避免bug，需要将当前用户的所有地址修改为非默认地址
        addressBook.setIsDefault(0);
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBookMapper.updateIsDefaultByUserId(addressBook);

        // 将当前地址改为默认地址
        addressBook.setIsDefault(1);
        addressBookMapper.update(addressBook);
    }

    /**
     * 根据地址id删除地址
     *
     * @param id
     */
    @Override
    public void deleteById(Long id) {
        log.info("删除地址：{}", id);
        addressBookMapper.deleteById(id);
    }

    /**
     * 查询默认地址
     *
     * @return
     */
    @Override
    public AddressBook getDefault() {
        log.info("查询默认地址");
        AddressBook addressBook = new AddressBook();
        // 封装用户id和当前地址状态
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBook.setIsDefault(1);
        // 这个AddressBook对象不是用户传递的，而是相当于一个该用户的默认地址的模板，Mapper需要按照这个模板查询用户的默认地址
        List<AddressBook> list = addressBookMapper.list(addressBook);
        if (list == null || list.size() != 1) {
            // 因为每个用户只会有一个默认地址，所以说若正确的查询，list中只会存在一个元素所以说
            // 此时list中没有内容，或者list的长度不为1；说明该用户不存在默认地址，直接返回null
            return null;
        }
        // 直接取出list中的第一个元素就是需要查询的用户的默认地址
        return list.get(0);
    }
}

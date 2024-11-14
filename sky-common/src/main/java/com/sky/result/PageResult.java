package com.sky.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 封装分页查询结果，分页查询的统一返回结果中封装的数据都是PageResult
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult implements Serializable {
    //分页查询总记录数
    private long total;

    //当前页数据封装的集合
    private List records;

}

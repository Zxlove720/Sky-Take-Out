package com.sky.service;

import com.sky.vo.BusinessDataVO;

import java.time.LocalDateTime;

public interface WorkSpacerService {

    /**
     * 根据时间区间统计营业数据
     *
     * @param begin
     * @param end
     * @return
     */
    BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end);
}
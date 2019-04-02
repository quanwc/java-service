package com.quanwc.javase.thread.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.quanwc.javase.thread.bean.HKAndCCS;

/**
 * 港股、中概股jpa
 * @author quanwenchao
 * @date 2019/3/21 16:30:47
 */
public interface HKAndCCSRepository extends JpaRepository<HKAndCCS, Long> {
}

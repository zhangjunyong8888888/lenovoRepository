package com.lenovo.repository.data.dao;

import com.lenovo.repository.data.pojo.ErrorList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ErrorListDao extends JpaRepository<ErrorList,String> {

}

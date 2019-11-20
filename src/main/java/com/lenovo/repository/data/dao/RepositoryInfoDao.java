package com.lenovo.repository.data.dao;

import com.lenovo.repository.data.pojo.RepositoryInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepositoryInfoDao extends JpaRepository<RepositoryInfo,String> {

}

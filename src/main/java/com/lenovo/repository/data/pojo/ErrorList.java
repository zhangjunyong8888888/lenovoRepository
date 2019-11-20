package com.lenovo.repository.data.pojo;


import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "error_list")
public class ErrorList {

    @Id
    @Column(length = 32)
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @GeneratedValue(generator = "uuid")
    private String id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id")
    private RepositoryInfo repositoryInfo;
    @Column(length = 6,name = "error_code")
    private Integer errorCode;
}

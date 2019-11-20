package com.lenovo.repository.data.service;

import com.lenovo.repository.data.dao.RepositoryInfoDao;
import com.lenovo.repository.data.enums.ErrorCodeEnum;
import com.lenovo.repository.data.pojo.ErrorList;
import com.lenovo.repository.data.pojo.RepositoryInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SaveDataPipelineService implements Pipeline {

    private static final Logger logger = LoggerFactory.getLogger(SaveDataPipelineService.class);

    @Autowired
    private RepositoryInfoDao repositoryInfoDao;

    @Override
    public void process(ResultItems resultItems, Task task) {
        Boolean isError = resultItems.get("isError");
        RepositoryInfo repositoryInfo = resultItems.get("data");
        List<Integer> errorList = resultItems.get("errorList");
        String url = resultItems.getRequest().getUrl();
        try {
            repositoryInfo.setIsError(isError);
            repositoryInfo.setUrl(url);
            List<ErrorList>  errorLists=getErrorList(repositoryInfo,errorList);
            repositoryInfo.setErrorLists(errorLists);
            repositoryInfoDao.save(repositoryInfo);
        } catch (Exception e) {
            logger.error("插入数据库时发生异常");
            e.printStackTrace();
            try {
                repositoryInfo = new RepositoryInfo();
                repositoryInfo.setIsError(Boolean.TRUE);
                repositoryInfo.setUrl(url);
                errorList.add(ErrorCodeEnum.INSERT_ERROR.getCode());
                List<ErrorList>  errorLists=getErrorList(repositoryInfo,errorList);
                repositoryInfo.setErrorLists(errorLists);
                repositoryInfoDao.save(repositoryInfo);
            } catch (Exception ex) {
                logger.error("二次插入发生异常-{}",url);
                ex.printStackTrace();
            }
        }
    }

    private List<ErrorList> getErrorList(RepositoryInfo repositoryInfo, List<Integer> errorList) {
        return  errorList.stream().map(i->{
            ErrorList error = new ErrorList();
            error.setErrorCode(i);
            error.setRepositoryInfo(repositoryInfo);
            return error;
        }).collect(Collectors.toList());
    }
}

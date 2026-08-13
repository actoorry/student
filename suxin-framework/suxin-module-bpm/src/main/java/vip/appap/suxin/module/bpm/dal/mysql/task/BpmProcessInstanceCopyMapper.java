package vip.appap.suxin.module.bpm.dal.mysql.task;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCopyPageReqVO;
import vip.appap.suxin.module.bpm.dal.dataobject.task.BpmProcessInstanceCopyDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BpmProcessInstanceCopyMapper extends BaseMapperX<BpmProcessInstanceCopyDO> {

    default PageResult<BpmProcessInstanceCopyDO> selectPage(Long loginUserId, BpmProcessInstanceCopyPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<BpmProcessInstanceCopyDO>()
                .eqIfPresent(BpmProcessInstanceCopyDO::getUserId, loginUserId)
                .likeIfPresent(BpmProcessInstanceCopyDO::getProcessInstanceName, reqVO.getProcessInstanceName())
                .betweenIfPresent(BpmProcessInstanceCopyDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(BpmProcessInstanceCopyDO::getId));
    }

    default void deleteByProcessInstanceId(String processInstanceId) {
        delete(BpmProcessInstanceCopyDO::getProcessInstanceId, processInstanceId);
    }

}

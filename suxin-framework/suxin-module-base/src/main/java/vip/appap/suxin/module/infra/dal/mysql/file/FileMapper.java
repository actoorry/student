package vip.appap.suxin.module.infra.dal.mysql.file;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.infra.controller.admin.file.vo.file.FilePageReqVO;
import vip.appap.suxin.module.infra.dal.dataobject.file.FileDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 文件操作 Mapper
 *
 * @author 书心软件
 */
@Mapper
public interface FileMapper extends BaseMapperX<FileDO> {

    default PageResult<FileDO> selectPage(FilePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<FileDO>()
                .likeIfPresent(FileDO::getPath, reqVO.getPath())
                .likeIfPresent(FileDO::getType, reqVO.getType())
                .betweenIfPresent(FileDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(FileDO::getId));
    }

    default List<FileDO> selectListByBiz(String bizType, Long bizId) {
        return selectList(new LambdaQueryWrapperX<FileDO>()
                .eq(FileDO::getBizType, bizType)
                .eq(FileDO::getBizId, bizId)
                .orderByAsc(FileDO::getId));
    }

    default List<FileDO> selectListByBizTypeAndBizIds(String bizType, Collection<Long> bizIds) {
        if (bizIds == null || bizIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<FileDO>()
                .eq(FileDO::getBizType, bizType)
                .in(FileDO::getBizId, bizIds)
                .orderByAsc(FileDO::getBizId)
                .orderByAsc(FileDO::getId));
    }

    default int updateBizByUrls(String bizType, Long bizId, Collection<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return 0;
        }
        return update(new LambdaUpdateWrapper<FileDO>()
                .in(FileDO::getUrl, urls)
                .and(wrapper -> wrapper.isNull(FileDO::getBizType).or().eq(FileDO::getBizType, ""))
                .set(FileDO::getBizType, bizType)
                .set(FileDO::getBizId, bizId));
    }

    default FileDO selectByUrl(String url) {
        return selectOne(new LambdaQueryWrapperX<FileDO>()
                .eq(FileDO::getUrl, url));
    }

    default int updateAuditStatusByTraceId(String traceId, Integer auditStatus, String auditReason,
                                            java.time.LocalDateTime auditTime) {
        return update(new LambdaUpdateWrapper<FileDO>()
                .eq(FileDO::getAuditTraceId, traceId)
                .set(FileDO::getAuditStatus, auditStatus)
                .set(FileDO::getAuditReason, auditReason)
                .set(FileDO::getAuditTime, auditTime));
    }

}

package vip.appap.suxin.module.crm.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.MPJLambdaWrapperX;
import vip.appap.suxin.module.crm.controller.admin.vo.CrmCluePageReqVO;
import vip.appap.suxin.module.crm.dal.dataobject.CrmClueDO;
import vip.appap.suxin.module.crm.enums.CrmBizTypeEnum;
import vip.appap.suxin.module.crm.enums.CrmSceneTypeEnum;
import vip.appap.suxin.module.crm.util.CrmPermissionUtils;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 线索 Mapper
 *
 * @author Wanwan
 */
@Mapper
public interface CrmClueMapper extends BaseMapperX<CrmClueDO> {

    default PageResult<CrmClueDO> selectPage(CrmCluePageReqVO pageReqVO, Long userId) {
        MPJLambdaWrapperX<CrmClueDO> query = new MPJLambdaWrapperX<>();
        // 拼接数据权限的查询条件
        CrmPermissionUtils.appendPermissionCondition(query, CrmBizTypeEnum.CRM_CLUE.getType(),
                CrmClueDO::getId, userId, pageReqVO.getSceneType());
        // JOIN partner 表用于 mobile 字段的过滤
        boolean needJoin = pageReqVO.getMobile() != null;
        if (needJoin) {
            query.leftJoin(PartnerDO.class, PartnerDO::getId, CrmClueDO::getId);
        }
        // 拼接自身的查询条件
        query.selectAll(CrmClueDO.class)
                .likeIfPresent(CrmClueDO::getName, pageReqVO.getName())
                .eqIfPresent(CrmClueDO::getTransformStatus, pageReqVO.getTransformStatus())
                .likeIfPresent(CrmClueDO::getTelephone, pageReqVO.getTelephone())
                .likeIfPresent(PartnerDO::getMobile, pageReqVO.getMobile())
                .eqIfPresent(CrmClueDO::getLevel, pageReqVO.getLevel())
                .eqIfPresent(CrmClueDO::getSource, pageReqVO.getSource())
                .eqIfPresent(CrmClueDO::getFollowUpStatus, pageReqVO.getFollowUpStatus())
                .betweenIfPresent(CrmClueDO::getCreateTime, pageReqVO.getCreateTime())
                .orderByDesc(CrmClueDO::getId);
        return selectJoinPage(pageReqVO, CrmClueDO.class, query);
    }

    default Long selectCountByFollow(Long userId) {
        MPJLambdaWrapperX<CrmClueDO> query = new MPJLambdaWrapperX<>();
        // 我负责的 + 非公海
        CrmPermissionUtils.appendPermissionCondition(query, CrmBizTypeEnum.CRM_CLUE.getType(),
                CrmClueDO::getId, userId, CrmSceneTypeEnum.OWNER.getType());
        // 未跟进 + 未转化
        query.eq(CrmClueDO::getFollowUpStatus, false)
                .eq(CrmClueDO::getTransformStatus, false);
        return selectCount(query);
    }

}

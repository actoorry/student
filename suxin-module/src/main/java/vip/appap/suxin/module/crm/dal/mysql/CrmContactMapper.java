package vip.appap.suxin.module.crm.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.framework.mybatis.core.query.MPJLambdaWrapperX;
import vip.appap.suxin.module.crm.controller.admin.vo.CrmContactPageReqVO;
import vip.appap.suxin.module.crm.dal.dataobject.CrmContactDO;
import vip.appap.suxin.module.crm.enums.CrmBizTypeEnum;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import vip.appap.suxin.module.crm.util.CrmPermissionUtils;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * CRM 联系人 Mapper
 *
 * @author 书心软件
 */
@Mapper
public interface CrmContactMapper extends BaseMapperX<CrmContactDO> {

    default int updateOwnerUserIdByCustomerId(Long customerId, Long ownerUserId) {
        return update(new LambdaUpdateWrapper<CrmContactDO>()
                .eq(CrmContactDO::getCustomerId, customerId)
                .set(CrmContactDO::getOwnerUserId, ownerUserId));
    }

    default PageResult<CrmContactDO> selectPageByCustomerId(CrmContactPageReqVO pageVO) {
        MPJLambdaWrapperX<CrmContactDO> query = new MPJLambdaWrapperX<>();
        // mobile/email 仍在 partner 表
        boolean needJoin = pageVO.getMobile() != null || pageVO.getEmail() != null;
        if (needJoin) {
            query.leftJoin(PartnerDO.class, PartnerDO::getId, CrmContactDO::getId);
        }
        query.selectAll(CrmContactDO.class)
                .eq(CrmContactDO::getCustomerId, pageVO.getCustomerId())
                .likeIfPresent(CrmContactDO::getName, pageVO.getName())
                .eqIfPresent(PartnerDO::getMobile, pageVO.getMobile())
                .eqIfPresent(CrmContactDO::getTelephone, pageVO.getTelephone())
                .eqIfPresent(PartnerDO::getEmail, pageVO.getEmail())
                .likeIfPresent(CrmContactDO::getQq, pageVO.getQq() != null ? String.valueOf(pageVO.getQq()) : null)
                .eqIfPresent(CrmContactDO::getWechat, pageVO.getWechat())
                .orderByDesc(CrmContactDO::getId);
        return selectJoinPage(pageVO, CrmContactDO.class, query);
    }

    default PageResult<CrmContactDO> selectPageByBusinessId(CrmContactPageReqVO pageVO, Collection<Long> ids) {
        MPJLambdaWrapperX<CrmContactDO> query = new MPJLambdaWrapperX<>();
        boolean needJoin = pageVO.getMobile() != null || pageVO.getEmail() != null;
        if (needJoin) {
            query.leftJoin(PartnerDO.class, PartnerDO::getId, CrmContactDO::getId);
        }
        query.selectAll(CrmContactDO.class)
                .in(CrmContactDO::getId, ids)
                .likeIfPresent(CrmContactDO::getName, pageVO.getName())
                .eqIfPresent(PartnerDO::getMobile, pageVO.getMobile())
                .eqIfPresent(CrmContactDO::getTelephone, pageVO.getTelephone())
                .eqIfPresent(PartnerDO::getEmail, pageVO.getEmail())
                .likeIfPresent(CrmContactDO::getQq, pageVO.getQq() != null ? String.valueOf(pageVO.getQq()) : null)
                .eqIfPresent(CrmContactDO::getWechat, pageVO.getWechat())
                .orderByDesc(CrmContactDO::getId);
        return selectJoinPage(pageVO, CrmContactDO.class, query);
    }

    default PageResult<CrmContactDO> selectPage(CrmContactPageReqVO pageReqVO, Long userId) {
        MPJLambdaWrapperX<CrmContactDO> query = new MPJLambdaWrapperX<>();
        // 拼接数据权限的查询条件
        CrmPermissionUtils.appendPermissionCondition(query, CrmBizTypeEnum.CRM_CONTACT.getType(),
                CrmContactDO::getId, userId, pageReqVO.getSceneType());
        // JOIN partner 表用于 mobile/email 字段的过滤
        boolean needJoin = pageReqVO.getMobile() != null || pageReqVO.getEmail() != null;
        if (needJoin) {
            query.leftJoin(PartnerDO.class, PartnerDO::getId, CrmContactDO::getId);
        }
        // 拼接自身的查询条件
        query.selectAll(CrmContactDO.class)
                .likeIfPresent(CrmContactDO::getName, pageReqVO.getName())
                .eqIfPresent(PartnerDO::getMobile, pageReqVO.getMobile())
                .eqIfPresent(CrmContactDO::getTelephone, pageReqVO.getTelephone())
                .eqIfPresent(PartnerDO::getEmail, pageReqVO.getEmail())
                .likeIfPresent(CrmContactDO::getQq, pageReqVO.getQq() != null ? String.valueOf(pageReqVO.getQq()) : null)
                .eqIfPresent(CrmContactDO::getWechat, pageReqVO.getWechat())
                .orderByDesc(CrmContactDO::getId);
        return selectJoinPage(pageReqVO, CrmContactDO.class, query);
    }

    default List<CrmContactDO> selectListByCustomerId(Long customerId) {
        return selectList(CrmContactDO::getCustomerId, customerId);
    }

    default List<CrmContactDO> selectListByCustomerIdOwnerUserId(Long customerId, Long ownerUserId) {
        return selectList(CrmContactDO::getCustomerId, customerId,
                CrmContactDO::getOwnerUserId, ownerUserId);
    }

}

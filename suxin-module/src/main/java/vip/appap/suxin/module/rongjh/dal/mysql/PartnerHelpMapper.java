package vip.appap.suxin.module.rongjh.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.rongjh.controller.admin.vo.HelpPageReqVO;
import vip.appap.suxin.module.rongjh.dal.dataobject.PartnerHelpDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

@Mapper
public interface PartnerHelpMapper extends BaseMapperX<PartnerHelpDO> {

    default PageResult<PartnerHelpDO> selectPage(HelpPageReqVO reqVO, Collection<Long> partnerIds) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PartnerHelpDO>()
                .likeIfPresent(PartnerHelpDO::getName, reqVO.getName())
                .likeIfPresent(PartnerHelpDO::getPhone, reqVO.getPhone())
                .eqIfPresent(PartnerHelpDO::getStatus, reqVO.getStatus())
                .inIfPresent(PartnerHelpDO::getPartnerId, partnerIds)
                .betweenIfPresent(PartnerHelpDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(PartnerHelpDO::getCreateTime)
                .orderByDesc(PartnerHelpDO::getId));
    }

    default List<PartnerHelpDO> selectByPartnerId(Long partnerId) {
        return selectList(new LambdaQueryWrapper<PartnerHelpDO>()
                .eq(PartnerHelpDO::getPartnerId, partnerId)
                .orderByDesc(PartnerHelpDO::getId));
    }

    default BigDecimal sumApprovedActualAmount() {
        return selectList(new LambdaQueryWrapper<PartnerHelpDO>()
                        .eq(PartnerHelpDO::getStatus, 1)
                        .isNotNull(PartnerHelpDO::getActualAmount))
                .stream()
                .map(PartnerHelpDO::getActualAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}

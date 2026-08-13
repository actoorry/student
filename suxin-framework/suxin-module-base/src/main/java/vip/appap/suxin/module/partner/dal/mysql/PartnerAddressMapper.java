package vip.appap.suxin.module.partner.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerAddressPageReqVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerAddressDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 会员地址 Mapper
 *
 * @author 书心软件
 */
@Mapper
public interface PartnerAddressMapper extends BaseMapperX<PartnerAddressDO> {

    default PageResult<PartnerAddressDO> selectPage(PartnerAddressPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PartnerAddressDO>()
                .eqIfPresent(PartnerAddressDO::getUserId, reqVO.getUserId())
                .eqIfPresent(PartnerAddressDO::getType, reqVO.getType())
                .likeIfPresent(PartnerAddressDO::getName, reqVO.getName())
                .likeIfPresent(PartnerAddressDO::getMobile, reqVO.getMobile())
                .betweenIfPresent(PartnerAddressDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(PartnerAddressDO::getId));
    }

    default List<PartnerAddressDO> selectListByType(Integer type) {
        return selectList(new LambdaQueryWrapperX<PartnerAddressDO>()
                .eq(PartnerAddressDO::getType, type)
                .orderByDesc(PartnerAddressDO::getDefaulted)
                .orderByDesc(PartnerAddressDO::getId));
    }

}

package vip.appap.suxin.module.rongjh.dal.mysql;

import cn.hutool.core.util.StrUtil;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.rongjh.controller.admin.vo.WarriorPageReqVO;
import vip.appap.suxin.module.rongjh.dal.dataobject.PartnerWarriorDO;
import vip.appap.suxin.module.rongjh.enums.WarriorTypeEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Mapper
public interface PartnerWarriorMapper extends BaseMapperX<PartnerWarriorDO> {

    List<String> SANSHU_TYPES = Arrays.stream(WarriorTypeEnum.values())
            .map(WarriorTypeEnum::getType)
            .filter(type -> !WarriorTypeEnum.SELF.getType().equals(type))
            .collect(Collectors.toList());

    default PageResult<PartnerWarriorDO> selectPage(WarriorPageReqVO reqVO, Collection<Long> partnerIds) {
        LambdaQueryWrapperX<PartnerWarriorDO> wrapper = new LambdaQueryWrapperX<PartnerWarriorDO>()
                .likeIfPresent(PartnerWarriorDO::getName, reqVO.getName())
                .eqIfPresent(PartnerWarriorDO::getStatus, reqVO.getStatus())
                .eqIfPresent(PartnerWarriorDO::getStateId, reqVO.getStateId())
                .inIfPresent(PartnerWarriorDO::getId, partnerIds)
                .betweenIfPresent(PartnerWarriorDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(PartnerWarriorDO::getCreateTime)
                .orderByDesc(PartnerWarriorDO::getId);
        applyTypeFilter(wrapper, reqVO);
        return selectPage(reqVO, wrapper);
    }

    default void applyTypeFilter(LambdaQueryWrapperX<PartnerWarriorDO> wrapper, WarriorPageReqVO reqVO) {
        if (StrUtil.isNotBlank(reqVO.getType())) {
            wrapper.eq(PartnerWarriorDO::getType, reqVO.getType());
            return;
        }
        if ("SANSHU".equalsIgnoreCase(reqVO.getTypeGroup())) {
            wrapper.in(PartnerWarriorDO::getType, SANSHU_TYPES);
            return;
        }
        if ("SELF".equalsIgnoreCase(reqVO.getTypeGroup())) {
            wrapper.eq(PartnerWarriorDO::getType, WarriorTypeEnum.SELF.getType());
        }
    }

    default Long selectCountByStatus(Integer status) {
        return selectCount(new LambdaQueryWrapper<PartnerWarriorDO>()
                .eq(PartnerWarriorDO::getStatus, status));
    }

    default Long selectCountByStateIdAndStatus(Integer stateId, Integer status) {
        return selectCount(new LambdaQueryWrapper<PartnerWarriorDO>()
                .eq(PartnerWarriorDO::getStateId, stateId)
                .eq(PartnerWarriorDO::getStatus, status));
    }

}

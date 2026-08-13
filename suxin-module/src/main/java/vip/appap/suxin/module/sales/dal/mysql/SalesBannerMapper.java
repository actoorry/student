package vip.appap.suxin.module.sales.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBannerPageReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBannerDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Banner Mapper
 *
 * @author xia
 */
@Mapper
public interface SalesBannerMapper extends BaseMapperX<SalesBannerDO> {

    default PageResult<SalesBannerDO> selectPage(SalesBannerPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SalesBannerDO>()
                .likeIfPresent(SalesBannerDO::getTitle, reqVO.getTitle())
                .eqIfPresent(SalesBannerDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(SalesBannerDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SalesBannerDO::getSort));
    }

    default void updateBrowseCount(Long id) {
        update(null, new LambdaUpdateWrapper<SalesBannerDO>()
                .eq(SalesBannerDO::getId, id)
                .setSql("browse_count = browse_count + 1"));
    }

    default List<SalesBannerDO> selectBannerListByPosition(Integer position) {
        return selectList(new LambdaQueryWrapperX<SalesBannerDO>().eq(SalesBannerDO::getPosition, position));
    }

}

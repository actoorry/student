package vip.appap.suxin.module.sales.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDiyPagePageReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDiyPageDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 装修页面 Mapper
 *
 * @author owen
 */
@Mapper
public interface SalesDiyPageMapper extends BaseMapperX<SalesDiyPageDO> {

    default PageResult<SalesDiyPageDO> selectPage(SalesDiyPagePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SalesDiyPageDO>()
                .likeIfPresent(SalesDiyPageDO::getName, reqVO.getName())
                .betweenIfPresent(SalesDiyPageDO::getCreateTime, reqVO.getCreateTime())
                // 模板下面的页面，在模板中管理
                .isNull(SalesDiyPageDO::getTemplateId)
                .orderByDesc(SalesDiyPageDO::getId));
    }

    default List<SalesDiyPageDO> selectListByTemplateId(Long templateId) {
        return selectList(SalesDiyPageDO::getTemplateId, templateId);
    }

    default SalesDiyPageDO selectByNameAndTemplateIdIsNull(String name) {
        return selectOne(new LambdaQueryWrapperX<SalesDiyPageDO>()
                .eq(SalesDiyPageDO::getName, name)
                .isNull(SalesDiyPageDO::getTemplateId));
    }

}

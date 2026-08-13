package vip.appap.suxin.module.sales.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDiyTemplatePageReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDiyTemplateDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 装修模板 Mapper
 *
 * @author owen
 */
@Mapper
public interface SalesDiyTemplateMapper extends BaseMapperX<SalesDiyTemplateDO> {

    default PageResult<SalesDiyTemplateDO> selectPage(SalesDiyTemplatePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SalesDiyTemplateDO>()
                .likeIfPresent(SalesDiyTemplateDO::getName, reqVO.getName())
                .eqIfPresent(SalesDiyTemplateDO::getUsed, reqVO.getUsed())
                .betweenIfPresent(SalesDiyTemplateDO::getUsedTime, reqVO.getUsedTime())
                .betweenIfPresent(SalesDiyTemplateDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SalesDiyTemplateDO::getUsed) // 排序规则1：已使用的排到最前面
                .orderByDesc(SalesDiyTemplateDO::getId)); // 排序规则2：新创建的排到前面
    }

    default SalesDiyTemplateDO selectByUsed(boolean used) {
        return selectOne(SalesDiyTemplateDO::getUsed, used);
    }

    /**
     * 查询指定使用状态的模板，用于婚恋装修草稿与保留版本管理。
     */
    default List<SalesDiyTemplateDO> selectListByUsed(boolean used) {
        return selectList(new LambdaQueryWrapperX<SalesDiyTemplateDO>()
                .eq(SalesDiyTemplateDO::getUsed, used)
                .orderByAsc(SalesDiyTemplateDO::getId));
    }

    /**
     * 锁定当前租户的全部模板，保证发布和回滚时只有一个生效模板。
     */
    default List<SalesDiyTemplateDO> selectListForUpdate() {
        return selectList(new LambdaQueryWrapperX<SalesDiyTemplateDO>()
                .orderByAsc(SalesDiyTemplateDO::getId)
                .last("FOR UPDATE"));
    }

    /**
     * 使用父模板更新时间作为乐观锁令牌。
     *
     * @return 受影响行数，0 表示令牌已过期
     */
    default int touchUpdateTimeIfMatch(Long id, LocalDateTime updateTime) {
        return update(null, new LambdaUpdateWrapper<SalesDiyTemplateDO>()
                .eq(SalesDiyTemplateDO::getId, id)
                .eq(SalesDiyTemplateDO::getUpdateTime, updateTime)
                .setSql("update_time = NOW()"));
    }

    default SalesDiyTemplateDO selectByName(String name) {
        return selectOne(SalesDiyTemplateDO::getName, name);
    }

}

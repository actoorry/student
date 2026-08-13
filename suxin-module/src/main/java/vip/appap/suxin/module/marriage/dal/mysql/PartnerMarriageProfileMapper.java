package vip.appap.suxin.module.marriage.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.MPJLambdaWrapperX;
import vip.appap.suxin.module.marriage.controller.app.vo.AppPartnerMarriageProfilePageReqVO;
import vip.appap.suxin.module.marriage.dal.dataobject.PartnerMarriageProfileDO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PartnerMarriageProfileMapper extends BaseMapperX<PartnerMarriageProfileDO> {

    /**
     * 推荐会员分页查询（联表 partner 表，按人物瀑布流组件规则过滤）
     *
     * @param reqVO         分页参数
     * @param loginUserId   当前登录用户ID（可为null）
     * @param loginUserSex  当前登录用户性别（可为null，null时不过滤性别）
     * @return 分页结果
     */
    default PageResult<PartnerMarriageProfileDO> selectRecommendPage(AppPartnerMarriageProfilePageReqVO reqVO,
                                                                      Long loginUserId,
                                                                      Integer loginUserSex) {
        MPJLambdaWrapperX<PartnerMarriageProfileDO> query = new MPJLambdaWrapperX<>();
        // 联表 partner 查询（同租户有效关联由 tenant_id 拦截器和硬规则共同保证）
        query.innerJoin(PartnerDO.class, PartnerDO::getId, PartnerMarriageProfileDO::getId);
        // 只查询 partner_marriage 表的字段
        query.selectAll(PartnerMarriageProfileDO.class);

        // ===== 不可关闭的硬规则 =====
        // 婚恋档案有效：profile_status = 0，推荐标记为 true
        query.eq(PartnerMarriageProfileDO::getProfileStatus, 0);
        query.eq(PartnerMarriageProfileDO::getRecommendFlag, Boolean.TRUE);
        // 人物有效：未删除、状态正常、头像非空
        query.eq(PartnerDO::getDeleted, Boolean.FALSE);
        query.eq(PartnerDO::getStatus, 0);
        query.isNotNull(PartnerDO::getAvatar);
        query.ne(PartnerDO::getAvatar, "");
        // 排除自己
        query.neIfPresent(PartnerMarriageProfileDO::getId, loginUserId);

        // ===== 人物信息卡瀑布流组件规则 =====
        if (Boolean.TRUE.equals(reqVO.getRealVerifiedOnly())) {
            query.eq(PartnerMarriageProfileDO::getRealVerified, 1);
        }
        if (Boolean.TRUE.equals(reqVO.getBackgroundImageRequired())) {
            query.isNotNull(PartnerMarriageProfileDO::getBackgroundImage);
            query.ne(PartnerMarriageProfileDO::getBackgroundImage, "");
        }
        if (Boolean.TRUE.equals(reqVO.getOppositeSexOnly()) && loginUserSex != null) {
            query.ne(PartnerDO::getSex, loginUserSex);
        }

        // 固定排序
        query.orderByAsc(PartnerMarriageProfileDO::getRecommendSort);
        query.orderByDesc(PartnerMarriageProfileDO::getId);
        return selectPage(reqVO, query);
    }

}

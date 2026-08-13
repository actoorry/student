package vip.appap.suxin.module.product.service;

import vip.appap.suxin.module.product.controller.admin.vo.ProductDisplayConfigListRespVO;
import vip.appap.suxin.module.product.controller.admin.vo.ProductDisplayConfigUpdateReqVO;

import java.util.List;

public interface ProductDisplayConfigService {

    String SCENE_MEMBER_PAGE = "MEMBER_PAGE";

    String SCENE_EMOTION_COURSE_PAGE = "EMOTION_COURSE_PAGE";

    String SCENE_OFFLINE_ACTIVITY_PAGE = "OFFLINE_ACTIVITY_PAGE";

    /**
     * 获得启用展示场景下有效的商品销售分类编号。
     * 该配置只决定页面展示范围，不承担商品类型、配送方式或履约分支判断。
     *
     * @param sceneCode 展示场景编码
     * @return 当前租户下启用且存在的分类编号
     */
    List<Long> getEnabledCategoryIdsBySceneCode(String sceneCode);

    /**
     * 获得当前租户下所有固定展示场景的配置概览。
     *
     * @return 展示场景配置列表
     */
    List<ProductDisplayConfigListRespVO> getDisplayConfigList();

    /**
     * 更新或创建当前租户下指定展示场景的配置。
     *
     * @param updateReqVO 更新请求
     * @return 更新后的配置概览
     */
    ProductDisplayConfigListRespVO updateDisplayConfig(ProductDisplayConfigUpdateReqVO updateReqVO);

}

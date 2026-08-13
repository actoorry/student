package vip.appap.suxin.module.product.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.product.api.dto.ProductCommentCreateReqDTO;
import vip.appap.suxin.module.product.controller.admin.vo.ProductCommentCreateReqVO;
import vip.appap.suxin.module.product.controller.admin.vo.ProductCommentPageReqVO;
import vip.appap.suxin.module.product.controller.admin.vo.ProductCommentReplyReqVO;
import vip.appap.suxin.module.product.controller.admin.vo.ProductCommentUpdateVisibleReqVO;
import vip.appap.suxin.module.product.controller.app.vo.AppCommentPageReqVO;
import vip.appap.suxin.module.product.dal.dataobject.ProductCommentDO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 商品评论 Service 接口
 *
 * @author wangzhs
 */
@Service
@Validated
public interface ProductCommentService {

    /**
     * 创建商品评论
     * 后台管理员创建评论使用
     *
     * @param createReqVO 商品评价创建 Request VO 对象
     */
    void createComment(ProductCommentCreateReqVO createReqVO);

    /**
     * 创建评论
     * 创建商品评论 APP 端创建商品评论使用
     *
     * @param createReqDTO 创建请求 dto
     * @return 返回评论 id
     */
    Long createComment(ProductCommentCreateReqDTO createReqDTO);

    /**
     * 修改评论是否可见
     *
     * @param updateReqVO 修改评论可见
     */
    void updateCommentVisible(ProductCommentUpdateVisibleReqVO updateReqVO);

    /**
     * 商家回复
     *
     * @param replyVO     商家回复
     * @param userId 管理后台商家登陆人 ID
     */
    void replyComment(ProductCommentReplyReqVO replyVO, Long userId);

    /**
     * 【管理员】获得商品评价分页
     *
     * @param pageReqVO 分页查询
     * @return 商品评价分页
     */
    PageResult<ProductCommentDO> getCommentPage(ProductCommentPageReqVO pageReqVO);

    /**
     * 【会员】获得商品评价分页
     *
     * @param pageVO  分页查询
     * @param visible 是否可见
     * @return 商品评价分页
     */
    PageResult<ProductCommentDO> getCommentPage(AppCommentPageReqVO pageVO, Boolean visible);

}

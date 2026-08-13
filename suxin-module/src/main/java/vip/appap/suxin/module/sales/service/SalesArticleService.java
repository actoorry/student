package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesArticleCreateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesArticlePageReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesArticleUpdateReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesArticlePageReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesArticleDO;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 文章 Service 接口
 *
 * @author HUIHUI
 */
public interface SalesArticleService {

    /**
     * 创建文章
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createArticle(@Valid SalesArticleCreateReqVO createReqVO);

    /**
     * 更新文章
     *
     * @param updateReqVO 更新信息
     */
    void updateArticle(@Valid SalesArticleUpdateReqVO updateReqVO);

    /**
     * 删除文章
     *
     * @param id 编号
     */
    void deleteArticle(Long id);

    /**
     * 获得文章
     *
     * @param id 编号
     * @return 文章
     */
    SalesArticleDO getArticle(Long id);

    /**
     * 基于标题，获得文章
     *
     * 如果有重名的文章，获取最后发布的
     *
     * @param title 标题
     * @return 文章
     */
    SalesArticleDO getLastArticleByTitle(String title);

    /**
     * 获得文章分页
     *
     * @param pageReqVO 分页查询
     * @return 文章分页
     */
    PageResult<SalesArticleDO> getArticlePage(SalesArticlePageReqVO pageReqVO);

    /**
     * 获得文章列表
     *
     * @param recommendHot    是否热门
     * @param recommendBanner 是否轮播图
     * @return 文章列表
     */
    List<SalesArticleDO> getArticleCategoryListByRecommend(Boolean recommendHot, Boolean recommendBanner);

    /**
     * 获得文章分页
     *
     * @param pageReqVO 分页查询
     * @return 文章分页
     */
    PageResult<SalesArticleDO> getArticlePage(AppSalesArticlePageReqVO pageReqVO);

    /**
     * 获得指定分类的文章数量
     *
     * @param categoryId 文章分类编号
     * @return 文章数量
     */
    Long getArticleCountByCategoryId(Long categoryId);

    /**
     * 增加文章浏览量
     *
     * @param id 文章编号
     */
    void addArticleBrowseCount(Long id);

}

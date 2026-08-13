package vip.appap.suxin.module.sales.service;

import cn.hutool.core.collection.CollUtil;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesArticleCreateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesArticlePageReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesArticleUpdateReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesArticlePageReqVO;
import vip.appap.suxin.module.sales.convert.SalesArticleConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesArticleCategoryDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesArticleDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesArticleMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.List;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.sales.enums.SalesErrorCodeConstants.ARTICLE_CATEGORY_NOT_EXISTS;
import static vip.appap.suxin.module.sales.enums.SalesErrorCodeConstants.ARTICLE_NOT_EXISTS;

/**
 * 文章管理 Service 实现类
 *
 * @author HUIHUI
 */
@Service
@Validated
public class SalesArticleServiceImpl implements SalesArticleService {

    @Resource
    private SalesArticleMapper articleMapper;

    @Resource
    private SalesArticleCategoryService articleCategoryService;

    @Override
    public Long createArticle(SalesArticleCreateReqVO createReqVO) {
        // 校验分类存在
        validateArticleCategoryExists(createReqVO.getCategoryId());

        // 插入
        SalesArticleDO article = SalesArticleConvert.INSTANCE.convert(createReqVO);
        article.setBrowseCount(0); // 初始浏览量
        articleMapper.insert(article);
        // 返回
        return article.getId();
    }

    @Override
    public void updateArticle(SalesArticleUpdateReqVO updateReqVO) {
        // 校验存在
        validateArticleExists(updateReqVO.getId());
        // 校验分类存在
        validateArticleCategoryExists(updateReqVO.getCategoryId());

        // 更新
        SalesArticleDO updateObj = SalesArticleConvert.INSTANCE.convert(updateReqVO);
        articleMapper.updateById(updateObj);
    }

    @Override
    public void deleteArticle(Long id) {
        // 校验存在
        validateArticleExists(id);
        // 删除
        articleMapper.deleteById(id);
    }

    private void validateArticleExists(Long id) {
        if (articleMapper.selectById(id) == null) {
            throw exception(ARTICLE_NOT_EXISTS);
        }
    }

    private void validateArticleCategoryExists(Long categoryId) {
        SalesArticleCategoryDO articleCategory = articleCategoryService.getArticleCategory(categoryId);
        if (articleCategory == null) {
            throw exception(ARTICLE_CATEGORY_NOT_EXISTS);
        }
    }

    @Override
    public SalesArticleDO getArticle(Long id) {
        return articleMapper.selectById(id);
    }

    @Override
    public SalesArticleDO getLastArticleByTitle(String title) {
        List<SalesArticleDO> articles = articleMapper.selectListByTitle(title);
        return CollUtil.getLast(articles);
    }

    @Override
    public PageResult<SalesArticleDO> getArticlePage(SalesArticlePageReqVO pageReqVO) {
        return articleMapper.selectPage(pageReqVO);
    }

    @Override
    public List<SalesArticleDO> getArticleCategoryListByRecommend(Boolean recommendHot, Boolean recommendBanner) {
        return articleMapper.selectList(recommendHot, recommendBanner);
    }

    @Override
    public PageResult<SalesArticleDO> getArticlePage(AppSalesArticlePageReqVO pageReqVO) {
        return articleMapper.selectPage(pageReqVO);
    }

    @Override
    public Long getArticleCountByCategoryId(Long categoryId) {
        return articleMapper.selectCount(SalesArticleDO::getCategoryId, categoryId);
    }

    @Override
    public void addArticleBrowseCount(Long id) {
        // 校验文章是否存在
        validateArticleExists(id);
        // 增加浏览次数
        articleMapper.updateBrowseCount(id);
    }

}

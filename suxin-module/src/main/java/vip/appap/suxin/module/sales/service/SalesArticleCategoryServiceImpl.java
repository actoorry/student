package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesArticleCategoryCreateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesArticleCategoryPageReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesArticleCategoryUpdateReqVO;
import vip.appap.suxin.module.sales.convert.SalesArticleCategoryConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesArticleCategoryDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesArticleCategoryMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.List;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.sales.enums.SalesErrorCodeConstants.ARTICLE_CATEGORY_DELETE_FAIL_HAVE_ARTICLES;
import static vip.appap.suxin.module.sales.enums.SalesErrorCodeConstants.ARTICLE_CATEGORY_NOT_EXISTS;

/**
 * 文章分类 Service 实现类
 *
 * @author HUIHUI
 */
@Service
@Validated
public class SalesArticleCategoryServiceImpl implements SalesArticleCategoryService {

    @Resource
    private SalesArticleCategoryMapper articleCategoryMapper;

    @Resource
    @Lazy // 延迟加载，解决循环依赖问题
    private SalesArticleService articleService;

    @Override
    public Long createArticleCategory(SalesArticleCategoryCreateReqVO createReqVO) {
        // 插入
        SalesArticleCategoryDO category = SalesArticleCategoryConvert.INSTANCE.convert(createReqVO);
        articleCategoryMapper.insert(category);
        // 返回
        return category.getId();
    }

    @Override
    public void updateArticleCategory(SalesArticleCategoryUpdateReqVO updateReqVO) {
        // 校验存在
        validateArticleCategoryExists(updateReqVO.getId());
        // 更新
        SalesArticleCategoryDO updateObj = SalesArticleCategoryConvert.INSTANCE.convert(updateReqVO);
        articleCategoryMapper.updateById(updateObj);
    }

    @Override
    public void deleteArticleCategory(Long id) {
        // 校验存在
        validateArticleCategoryExists(id);
        // 校验是不是存在关联文章
        Long count = articleService.getArticleCountByCategoryId(id);
        if (count > 0) {
            throw exception(ARTICLE_CATEGORY_DELETE_FAIL_HAVE_ARTICLES);
        }

        // 删除
        articleCategoryMapper.deleteById(id);
    }

    private void validateArticleCategoryExists(Long id) {
        if (articleCategoryMapper.selectById(id) == null) {
            throw exception(ARTICLE_CATEGORY_NOT_EXISTS);
        }
    }

    @Override
    public SalesArticleCategoryDO getArticleCategory(Long id) {
        return articleCategoryMapper.selectById(id);
    }

    @Override
    public PageResult<SalesArticleCategoryDO> getArticleCategoryPage(SalesArticleCategoryPageReqVO pageReqVO) {
        return articleCategoryMapper.selectPage(pageReqVO);
    }

    @Override
    public List<SalesArticleCategoryDO> getArticleCategoryListByStatus(Integer status) {
        return articleCategoryMapper.selectListByStatus(status);
    }

}

package vip.appap.suxin.module.product.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.util.collection.CollectionUtils;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.product.controller.admin.vo.ProductCategoryListReqVO;
import vip.appap.suxin.module.product.controller.admin.vo.ProductCategorySaveReqVO;
import vip.appap.suxin.module.product.dal.dataobject.ProductCategoryDO;
import vip.appap.suxin.module.product.dal.mysql.ProductCategoryMapper;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.product.dal.dataobject.ProductCategoryDO.CATEGORY_LEVEL;
import static vip.appap.suxin.module.product.dal.dataobject.ProductCategoryDO.PARENT_ID_NULL;
import static vip.appap.suxin.module.product.enums.ProductCategoryDimensionConstants.*;
import static vip.appap.suxin.module.product.enums.ErrorCodeConstants.*;

/**
 * 商品分类 Service 实现类
 *
 * @author 书心软件
 */
@Service
@Validated
public class ProductCategoryServiceImpl implements ProductCategoryService {

    @Resource
    private ProductCategoryMapper productCategoryMapper;
    @Resource
    @Lazy // 循环依赖，避免报错
    private ProductSpuService productSpuService;

    @Override
    public Long createCategory(ProductCategorySaveReqVO createReqVO) {
        if (isVirtualRoot(createReqVO.getId())) {
            throw exception(CATEGORY_VIRTUAL_ROOT_IMMUTABLE);
        }
        validateParentProductCategory(createReqVO.getParentId(), null);

        // 插入
        ProductCategoryDO category = BeanUtils.toBean(createReqVO, ProductCategoryDO.class);
        productCategoryMapper.insert(category);
        // 返回
        return category.getId();
    }

    @Override
    public void updateCategory(ProductCategorySaveReqVO updateReqVO) {
        if (isVirtualRoot(updateReqVO.getId())) {
            throw exception(CATEGORY_VIRTUAL_ROOT_IMMUTABLE);
        }
        // 校验分类是否存在
        validateProductCategoryExists(updateReqVO.getId());
        // 校验父分类存在
        validateParentProductCategory(updateReqVO.getParentId(), updateReqVO.getId());

        // 更新
        ProductCategoryDO updateObj = BeanUtils.toBean(updateReqVO, ProductCategoryDO.class);
        productCategoryMapper.updateById(updateObj);
    }

    @Override
    public void deleteCategory(Long id) {
        if (isVirtualRoot(id)) {
            throw exception(CATEGORY_VIRTUAL_ROOT_IMMUTABLE);
        }
        // 校验分类是否存在
        validateProductCategoryExists(id);
        // 校验是否还有子分类
        if (productCategoryMapper.selectCountByParentId(id) > 0) {
            throw exception(CATEGORY_EXISTS_CHILDREN);
        }
        // 校验分类是否绑定了 SPU
        Long spuCount = productSpuService.getSpuCountByCategoryId(id);
        if (spuCount > 0) {
            throw exception(CATEGORY_HAVE_BIND_SPU);
        }
        // 删除
        productCategoryMapper.deleteById(id);
    }

    private void validateParentProductCategory(Long parentId, Long categoryId) {
        if (Objects.equals(parentId, PARENT_ID_NULL)) {
            throw exception(CATEGORY_ROOT_NOT_ALLOWED);
        }
        if (isVirtualRoot(parentId)) {
            return;
        }
        ProductCategoryDO parent = productCategoryMapper.selectById(parentId);
        if (parent == null) {
            throw exception(CATEGORY_PARENT_NOT_EXISTS);
        }
        if (categoryId != null && (Objects.equals(categoryId, parentId)
                || isAncestor(categoryId, parentId))) {
            throw exception(CATEGORY_PARENT_CYCLE);
        }
    }

    /** 判断 prospectiveParent 的祖先链是否包含 categoryId。 */
    private boolean isAncestor(Long categoryId, Long prospectiveParentId) {
        Set<Long> visited = new HashSet<>();
        Long currentId = prospectiveParentId;
        while (true) {
            if (isVirtualRoot(currentId)) {
                return false;
            }
            if (!visited.add(currentId)) {
                throw exception(CATEGORY_ANCESTRY_INVALID);
            }
            if (Objects.equals(currentId, categoryId)) {
                return true;
            }
            ProductCategoryDO current = productCategoryMapper.selectById(currentId);
            if (current == null || Objects.equals(current.getParentId(), PARENT_ID_NULL)) {
                throw exception(CATEGORY_ANCESTRY_INVALID);
            }
            currentId = current.getParentId();
        }
    }

    private void validateProductCategoryExists(Long id) {
        ProductCategoryDO category = productCategoryMapper.selectById(id);
        if (category == null) {
            throw exception(CATEGORY_NOT_EXISTS);
        }
    }

    @Override
    public void validateCategoryList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 获得商品分类信息
        List<ProductCategoryDO> list = productCategoryMapper.selectByIds(ids);
        Map<Long, ProductCategoryDO> categoryMap = CollectionUtils.convertMap(list, ProductCategoryDO::getId);
        // 校验
        ids.forEach(id -> {
            ProductCategoryDO category = categoryMap.get(id);
            validateCategoryInDimension(id, SALES_ROOT_ID);
            // 商品分类层级校验，必须使用第二级的商品分类
            if (getCategoryLevel(id) < CATEGORY_LEVEL) {
                throw exception(SPU_SAVE_FAIL_CATEGORY_LEVEL_ERROR);
            }
        });
    }

    @Override
    public ProductCategoryDO getCategory(Long id) {
        if (isVirtualRoot(id)) {
            return null;
        }
        return productCategoryMapper.selectById(id);
    }

    @Override
    public void validateCategory(Long id) {
        if (isVirtualRoot(id)) {
            throw exception(CATEGORY_VIRTUAL_ROOT_IMMUTABLE);
        }
        ProductCategoryDO category = productCategoryMapper.selectById(id);
        if (category == null) {
            throw exception(CATEGORY_NOT_EXISTS);
        }
        if (Objects.equals(category.getStatus(), CommonStatusEnum.DISABLE.getStatus())) {
            throw exception(CATEGORY_DISABLED, category.getName());
        }
    }

    @Override
    public Integer getCategoryLevel(Long id) {
        int level = 1;
        Set<Long> visited = new HashSet<>();
        while (true) {
            if (isVirtualRoot(id)) {
                return level;
            }
            if (Objects.equals(id, PARENT_ID_NULL) || !visited.add(id)) {
                throw exception(CATEGORY_ANCESTRY_INVALID);
            }
            ProductCategoryDO category = productCategoryMapper.selectById(id);
            if (category == null || Objects.equals(category.getParentId(), PARENT_ID_NULL)) {
                throw exception(CATEGORY_ANCESTRY_INVALID);
            }
            level++;
            id = category.getParentId();
        }
    }

    @Override
    public void validateCategoryInDimension(Long id, Long virtualRootId) {
        if (!isVirtualRoot(virtualRootId) || isVirtualRoot(id)) {
            throw exception(CATEGORY_DIMENSION_INVALID);
        }
        ProductCategoryDO category = productCategoryMapper.selectById(id);
        if (category == null) {
            throw exception(CATEGORY_NOT_EXISTS);
        }
        if (!CommonStatusEnum.ENABLE.getStatus().equals(category.getStatus())) {
            throw exception(CATEGORY_DISABLED, category.getName());
        }
        if (!Objects.equals(resolveVirtualRoot(id), virtualRootId)) {
            throw exception(CATEGORY_DIMENSION_INVALID);
        }
    }

    private Long resolveVirtualRoot(Long id) {
        Set<Long> visited = new HashSet<>();
        Long currentId = id;
        while (true) {
            if (isVirtualRoot(currentId)) {
                return currentId;
            }
            if (Objects.equals(currentId, PARENT_ID_NULL) || !visited.add(currentId)) {
                throw exception(CATEGORY_ANCESTRY_INVALID);
            }
            ProductCategoryDO category = productCategoryMapper.selectById(currentId);
            if (category == null) {
                throw exception(CATEGORY_ANCESTRY_INVALID);
            }
            currentId = category.getParentId();
        }
    }

    @Override
    public List<ProductCategoryDO> getCategoryList(ProductCategoryListReqVO listReqVO) {
        List<ProductCategoryDO> allList = productCategoryMapper.selectList(new ProductCategoryListReqVO());
        Map<Long, ProductCategoryDO> categoryMap = CollectionUtils.convertMap(allList, ProductCategoryDO::getId);
        LinkedHashSet<Long> scopes = new LinkedHashSet<>();
        if (listReqVO.getParentId() != null) {
            scopes.add(listReqVO.getParentId());
        }
        if (CollUtil.isNotEmpty(listReqVO.getParentIds())) {
            scopes.addAll(listReqVO.getParentIds());
        }
        if (scopes.isEmpty()) {
            scopes.add(SALES_ROOT_ID);
            scopes.add(WAREHOUSE_ROOT_ID);
        }
        for (Long scope : scopes) {
            if (!isVirtualRoot(scope) && !categoryMap.containsKey(scope)) {
                throw exception(CATEGORY_PARENT_NOT_EXISTS);
            }
        }
        Map<Long, List<ProductCategoryDO>> childrenByParentId = new HashMap<>();
        allList.forEach(category -> childrenByParentId
                .computeIfAbsent(category.getParentId(), ignored -> new ArrayList<>()).add(category));
        LinkedHashMap<Long, ProductCategoryDO> scoped = new LinkedHashMap<>();
        Set<Long> visited = new HashSet<>();
        scopes.forEach(scope -> collectChildren(childrenByParentId, scope, scoped, visited));
        List<ProductCategoryDO> result = new ArrayList<>();
        scoped.values().forEach(category -> {
            if (listReqVO.getStatus() == null || Objects.equals(category.getStatus(), listReqVO.getStatus())) {
                result.add(category);
            }
        });
        if (StrUtil.isBlank(listReqVO.getName())) {
            return result;
        }
        // 管理端名称过滤保留匹配节点的祖先，避免树组件因父节点缺失而隐藏匹配项。
        Set<Long> visibleIds = new HashSet<>();
        result.stream().filter(category -> StrUtil.containsIgnoreCase(category.getName(), listReqVO.getName()))
                .forEach(category -> collectAncestors(category.getId(), scoped, visibleIds));
        List<ProductCategoryDO> visibleCategories = new ArrayList<>();
        result.forEach(category -> {
            if (visibleIds.contains(category.getId())) {
                visibleCategories.add(category);
            }
        });
        return visibleCategories;
    }

    /**
     * 递归收集 parentId 下的所有子分类
     */
    private void collectChildren(Map<Long, List<ProductCategoryDO>> childrenByParentId, Long parentId,
                                 Map<Long, ProductCategoryDO> result, Set<Long> visited) {
        for (ProductCategoryDO item : childrenByParentId.getOrDefault(parentId, List.of())) {
            if (!visited.add(item.getId())) {
                continue;
            }
            result.put(item.getId(), item);
            collectChildren(childrenByParentId, item.getId(), result, visited);
        }
    }

    private void collectAncestors(Long categoryId, Map<Long, ProductCategoryDO> scoped, Set<Long> visibleIds) {
        Long currentId = categoryId;
        Set<Long> visited = new HashSet<>();
        while (currentId != null && visited.add(currentId)) {
            ProductCategoryDO category = scoped.get(currentId);
            if (category == null) {
                return;
            }
            visibleIds.add(currentId);
            currentId = category.getParentId();
        }
    }

    @Override
    public List<ProductCategoryDO> getEnableCategoryList() {
        return productCategoryMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
    }

    @Override
    public List<ProductCategoryDO> getEnableCategoryList(List<Long> ids) {
        return productCategoryMapper.selectListByIdAndStatus(ids, CommonStatusEnum.ENABLE.getStatus());
    }

}

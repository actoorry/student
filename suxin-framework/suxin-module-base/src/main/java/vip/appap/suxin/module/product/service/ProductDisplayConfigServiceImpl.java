package vip.appap.suxin.module.product.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.module.product.controller.admin.vo.ProductDisplayConfigListRespVO;
import vip.appap.suxin.module.product.controller.admin.vo.ProductDisplayConfigUpdateReqVO;
import vip.appap.suxin.module.product.dal.dataobject.ProductCategoryDO;
import vip.appap.suxin.module.product.dal.dataobject.ProductDisplayConfigDO;
import vip.appap.suxin.module.product.dal.mysql.ProductDisplayConfigMapper;
import vip.appap.suxin.module.product.enums.ProductDisplaySceneEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertMap;
import static vip.appap.suxin.module.product.enums.ErrorCodeConstants.*;

@Service
@Validated
@Slf4j
public class ProductDisplayConfigServiceImpl implements ProductDisplayConfigService {

    @Resource
    private ProductDisplayConfigMapper productDisplayConfigMapper;
    @Resource
    private vip.appap.suxin.module.product.dal.mysql.ProductCategoryMapper productCategoryMapper;
    @Resource
    private ProductCategoryService productCategoryService;

    @Override
    public List<Long> getEnabledCategoryIdsBySceneCode(String sceneCode) {
        ProductDisplayConfigDO config = productDisplayConfigMapper.selectEnabledBySceneCode(sceneCode,
                CommonStatusEnum.ENABLE.getStatus());
        if (config == null || StrUtil.isBlank(config.getCategoryIds())) {
            return List.of();
        }
        List<Long> parsedCategoryIds = parseCategoryIds(sceneCode, config.getCategoryIds());
        if (CollUtil.isEmpty(parsedCategoryIds)) {
            return List.of();
        }
        List<ProductCategoryDO> categories = productCategoryMapper.selectListByIdAndStatus(parsedCategoryIds,
                CommonStatusEnum.ENABLE.getStatus());
        if (CollUtil.isEmpty(categories)) {
            return List.of();
        }
        Map<Long, ProductCategoryDO> categoryMap = convertMap(categories, ProductCategoryDO::getId);
        return parsedCategoryIds.stream().filter(categoryMap::containsKey).toList();
    }

    @Override
    public List<ProductDisplayConfigListRespVO> getDisplayConfigList() {
        List<ProductDisplaySceneEnum> scenes = ProductDisplaySceneEnum.all();
        List<String> sceneCodes = scenes.stream().map(ProductDisplaySceneEnum::getCode).toList();

        List<ProductDisplayConfigDO> configs = productDisplayConfigMapper.selectListBySceneCodes(sceneCodes);
        Map<String, ProductDisplayConfigDO> configMap = convertMap(configs, ProductDisplayConfigDO::getSceneCode);

        // 解析所有配置行中存储的分类编号，用于批量查询分类信息
        Set<Long> allCategoryIds = new LinkedHashSet<>();
        for (ProductDisplayConfigDO config : configs) {
            allCategoryIds.addAll(parseCategoryIds(config.getSceneCode(), config.getCategoryIds()));
        }

        Map<Long, ProductCategoryDO> categoryMap;
        if (CollUtil.isEmpty(allCategoryIds)) {
            categoryMap = Collections.emptyMap();
        } else {
            List<ProductCategoryDO> categories = productCategoryMapper.selectByIds(allCategoryIds);
            categoryMap = convertMap(categories, ProductCategoryDO::getId);
        }

        List<ProductDisplayConfigListRespVO> result = new ArrayList<>(scenes.size());
        for (ProductDisplaySceneEnum scene : scenes) {
            ProductDisplayConfigDO config = configMap.get(scene.getCode());
            result.add(buildDisplayConfigResp(scene, config, categoryMap));
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductDisplayConfigListRespVO updateDisplayConfig(ProductDisplayConfigUpdateReqVO updateReqVO) {
        // 1. 校验场景是否支持
        ProductDisplaySceneEnum scene = ProductDisplaySceneEnum.fromCode(updateReqVO.getSceneCode());
        if (scene == null) {
            throw exception(DISPLAY_CONFIG_SCENE_NOT_SUPPORTED);
        }

        // 2. 校验分类有效性
        ProductCategoryDO category = validateAndGetSalesCategory(updateReqVO.getCategoryId());

        // 3. 查询现有配置
        List<ProductDisplayConfigDO> existingConfigs = productDisplayConfigMapper.selectListBySceneCodes(
                Collections.singleton(scene.getCode()));
        ProductDisplayConfigDO existingConfig = CollUtil.isEmpty(existingConfigs) ? null : existingConfigs.get(0);

        LocalDateTime now = LocalDateTime.now();
        if (existingConfig != null) {
            // 3.1 更新现有配置：校验更新时间是否一致
            if (updateReqVO.getUpdateTime() == null
                    || !updateReqVO.getUpdateTime().equals(existingConfig.getUpdateTime())) {
                throw exception(DISPLAY_CONFIG_STALE_UPDATE);
            }
            existingConfig.setSceneName(scene.getLabel());
            existingConfig.setCategoryIds(String.valueOf(category.getId()));
            existingConfig.setStatus(updateReqVO.getStatus());
            existingConfig.setSort(updateReqVO.getSort());
            existingConfig.setRemark(updateReqVO.getRemark());
            existingConfig.setUpdateTime(now);
            productDisplayConfigMapper.updateById(existingConfig);
        } else {
            // 3.2 新增配置
            ProductDisplayConfigDO newConfig = new ProductDisplayConfigDO();
            newConfig.setSceneCode(scene.getCode());
            newConfig.setSceneName(scene.getLabel());
            newConfig.setCategoryIds(String.valueOf(category.getId()));
            newConfig.setStatus(updateReqVO.getStatus());
            newConfig.setSort(updateReqVO.getSort());
            newConfig.setRemark(updateReqVO.getRemark());
            newConfig.setUpdateTime(now);
            try {
                productDisplayConfigMapper.insert(newConfig);
            } catch (org.apache.ibatis.exceptions.PersistenceException ex) {
                // 唯一索引冲突（tenant_id, scene_code, deleted）：并发创建
                if (ex.getMessage() != null && ex.getMessage().contains("Duplicate entry")) {
                    throw exception(DISPLAY_CONFIG_CONCURRENT_CREATE);
                }
                throw ex;
            }
            existingConfig = newConfig;
        }

        Map<Long, ProductCategoryDO> categoryMap = convertMap(Collections.singletonList(category), ProductCategoryDO::getId);
        return buildDisplayConfigResp(scene, existingConfig, categoryMap);
    }

    /**
     * 校验并返回有效的销售分类
     */
    private ProductCategoryDO validateAndGetSalesCategory(Long categoryId) {
        try {
            productCategoryService.validateCategoryInDimension(categoryId,
                    vip.appap.suxin.module.product.enums.ProductCategoryDimensionConstants.SALES_ROOT_ID);
        } catch (RuntimeException ex) {
            throw exception(DISPLAY_CONFIG_CATEGORY_INVALID);
        }
        ProductCategoryDO category = productCategoryMapper.selectById(categoryId);
        if (category == null) {
            throw exception(DISPLAY_CONFIG_CATEGORY_INVALID);
        }
        return category;
    }

    private ProductDisplayConfigListRespVO buildDisplayConfigResp(ProductDisplaySceneEnum scene,
                                                                  ProductDisplayConfigDO config,
                                                                  Map<Long, ProductCategoryDO> categoryMap) {
        ProductDisplayConfigListRespVO resp = new ProductDisplayConfigListRespVO();
        resp.setSceneCode(scene.getCode());
        resp.setSceneName(scene.getLabel());
        resp.setStatus(CommonStatusEnum.DISABLE.getStatus());
        resp.setSort(scene.ordinal() * 10);
        resp.setConfigState("UNCONFIGURED");
        resp.setStoredCategoryIds(List.of());

        if (config != null) {
            resp.setStatus(config.getStatus());
            resp.setSort(config.getSort());
            resp.setRemark(config.getRemark());
            resp.setUpdateTime(config.getUpdateTime());

            List<Long> storedIds = parseCategoryIds(scene.getCode(), config.getCategoryIds());
            resp.setStoredCategoryIds(storedIds);

            if (CollUtil.isEmpty(storedIds)) {
                resp.setConfigState("UNCONFIGURED");
            } else if (storedIds.size() > 1) {
                resp.setConfigState("MULTIPLE_CATEGORIES");
                // 展示第一个存储的 id 作为当前有效 id（但不保证有效）
                Long firstId = storedIds.get(0);
                ProductCategoryDO category = categoryMap.get(firstId);
                resp.setCategoryId(firstId);
                resp.setCategoryName(category != null ? category.getName() : null);
            } else {
                Long categoryId = storedIds.get(0);
                ProductCategoryDO category = categoryMap.get(categoryId);
                resp.setCategoryId(categoryId);
                if (category == null) {
                    resp.setConfigState("INVALID_CATEGORY");
                } else if (!CommonStatusEnum.ENABLE.getStatus().equals(category.getStatus())) {
                    resp.setConfigState("INVALID_CATEGORY");
                    resp.setCategoryName(category.getName());
                } else if (Objects.equals(config.getStatus(), CommonStatusEnum.ENABLE.getStatus())) {
                    resp.setConfigState("ACTIVE");
                    resp.setCategoryName(category.getName());
                } else {
                    resp.setConfigState("DISABLED");
                    resp.setCategoryName(category.getName());
                }
            }
        } else {
            resp.setUpdateTime(null);
        }
        return resp;
    }

    private List<Long> parseCategoryIds(String sceneCode, String categoryIds) {
        if (StrUtil.isBlank(categoryIds)) {
            return List.of();
        }
        LinkedHashSet<Long> result = new LinkedHashSet<>();
        for (String categoryId : categoryIds.split(",")) {
            String value = categoryId.trim();
            if (value.isEmpty()) {
                continue;
            }
            try {
                Long id = Long.parseLong(value);
                if (id > 0) {
                    result.add(id);
                } else {
                    log.warn("[parseCategoryIds][sceneCode({}) invalid category id({})]", sceneCode, value);
                }
            } catch (NumberFormatException ex) {
                log.warn("[parseCategoryIds][sceneCode({}) invalid category id({})]", sceneCode, value);
            }
        }
        return List.copyOf(result);
    }

}

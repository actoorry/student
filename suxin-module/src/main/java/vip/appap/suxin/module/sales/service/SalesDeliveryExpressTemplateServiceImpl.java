package vip.appap.suxin.module.sales.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.collection.CollectionUtils;
import vip.appap.suxin.framework.tenant.core.context.TenantContextHolder;
import vip.appap.suxin.module.sales.controller.admin.vo.*;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDeliveryExpressTemplateChargeDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDeliveryExpressTemplateDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDeliveryExpressTemplateFreeDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesDeliveryExpressTemplateChargeMapper;
import vip.appap.suxin.module.sales.dal.mysql.SalesDeliveryExpressTemplateFreeMapper;
import vip.appap.suxin.module.sales.dal.mysql.SalesDeliveryExpressTemplateMapper;
import vip.appap.suxin.module.sales.dal.mysql.SalesDeliveryReferenceMapper;
import vip.appap.suxin.module.sales.service.bo.SalesDeliveryExpressTemplateRespBO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.*;
import static vip.appap.suxin.module.sales.convert.SalesDeliveryExpressTemplateConvert.INSTANCE;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.*;

/**
 * 快递运费模板 Service 实现类
 *
 * @author jason
 */
@Service
@Validated
public class SalesDeliveryExpressTemplateServiceImpl implements SalesDeliveryExpressTemplateService {

    @Resource
    private SalesDeliveryExpressTemplateMapper expressTemplateMapper;
    @Resource
    private SalesDeliveryExpressTemplateChargeMapper expressTemplateChargeMapper;
    @Resource
    private SalesDeliveryExpressTemplateFreeMapper expressTemplateFreeMapper;
    @Resource
    private SalesDeliveryReferenceMapper deliveryReferenceMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDeliveryExpressTemplate(SalesDeliveryExpressTemplateCreateReqVO createReqVO) {
        // 校验模板名是否唯一
        validateTemplateNameUnique(createReqVO.getName(), null);
        // 校验计费/包邮规则区域互不重复
        validateAreaNotDuplicate(createReqVO.getCharges(), createReqVO.getFrees());

        // 插入
        SalesDeliveryExpressTemplateDO template = INSTANCE.convert(createReqVO);
        expressTemplateMapper.insert(template);
        // 插入运费模板计费表
        if (CollUtil.isNotEmpty(createReqVO.getCharges())) {
            expressTemplateChargeMapper.insertBatch(
                    INSTANCE.convertTemplateChargeList(template.getId(), createReqVO.getChargeMode(), createReqVO.getCharges())
            );
        }
        // 插入运费模板包邮表
        if (CollUtil.isNotEmpty(createReqVO.getFrees())) {
            expressTemplateFreeMapper.insertBatch(
                    INSTANCE.convertTemplateFreeList(template.getId(), createReqVO.getFrees())
            );
        }
        return template.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDeliveryExpressTemplate(SalesDeliveryExpressTemplateUpdateReqVO updateReqVO) {
        // 校验存在
        validateDeliveryExpressTemplateExists(updateReqVO.getId());
        // 校验模板名是否唯一
        validateTemplateNameUnique(updateReqVO.getName(), updateReqVO.getId());
        // 校验计费/包邮规则区域互不重复
        validateAreaNotDuplicate(updateReqVO.getCharges(), updateReqVO.getFrees());

        // 更新运费从表
        updateExpressTemplateCharge(updateReqVO.getId(), updateReqVO.getChargeMode(), updateReqVO.getCharges());
        // 更新包邮从表
        updateExpressTemplateFree(updateReqVO.getId(), updateReqVO.getFrees());
        // 更新模板主表
        SalesDeliveryExpressTemplateDO updateObj = INSTANCE.convert(updateReqVO);
        expressTemplateMapper.updateById(updateObj);
    }

    private void updateExpressTemplateFree(Long templateId, List<SalesDeliveryExpressTemplateFreeBaseVO> frees) {
        // 第一步，对比新老数据，获得添加、修改、删除的列表
        List<SalesDeliveryExpressTemplateFreeDO> oldList = expressTemplateFreeMapper.selectListByTemplateId(templateId);
        List<SalesDeliveryExpressTemplateFreeDO> newList = INSTANCE.convertTemplateFreeList(templateId, frees);
        List<List<SalesDeliveryExpressTemplateFreeDO>> diffList = CollectionUtils.diffList(oldList, newList,
                (oldVal, newVal) -> ObjectUtil.equal(oldVal.getId(), newVal.getId()));

        // 第二步，批量添加、修改、删除
        if (CollUtil.isNotEmpty(diffList.get(0))) {
            expressTemplateFreeMapper.insertBatch(diffList.get(0));
        }
        if (CollUtil.isNotEmpty(diffList.get(1))) {
            expressTemplateFreeMapper.updateBatch(diffList.get(1));
        }
        if (CollUtil.isNotEmpty(diffList.get(2))) {
            expressTemplateFreeMapper.deleteByIds(convertList(diffList.get(2), SalesDeliveryExpressTemplateFreeDO::getId));
        }
    }

    private void updateExpressTemplateCharge(Long templateId, Integer chargeMode, List<SalesDeliveryExpressTemplateChargeBaseVO> charges) {
        // 第一步，对比新老数据，获得添加、修改、删除的列表
        List<SalesDeliveryExpressTemplateChargeDO> oldList = expressTemplateChargeMapper.selectListByTemplateId(templateId);
        List<SalesDeliveryExpressTemplateChargeDO> newList = INSTANCE.convertTemplateChargeList(templateId, chargeMode, charges);
        List<List<SalesDeliveryExpressTemplateChargeDO>> diffList = diffList(oldList, newList, (oldVal, newVal) -> {
            boolean same = ObjectUtil.equal(oldVal.getId(), newVal.getId());
            if (same) {
                newVal.setChargeMode(chargeMode); // 更新下收费模式
            }
            return same;
        });

        // 第二步，批量添加、修改、删除
        if (CollUtil.isNotEmpty(diffList.get(0))) {
            expressTemplateChargeMapper.insertBatch(diffList.get(0));
        }
        if (CollUtil.isNotEmpty(diffList.get(1))) {
            expressTemplateChargeMapper.updateBatch(diffList.get(1));
        }
        if (CollUtil.isNotEmpty(diffList.get(2))) {
            expressTemplateChargeMapper.deleteByIds(convertList(diffList.get(2), SalesDeliveryExpressTemplateChargeDO::getId));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDeliveryExpressTemplate(Long id) {
        // 校验存在
        validateDeliveryExpressTemplateExists(id);
        // 校验商品引用：仍被商品使用时拒绝删除，引导先换绑或移除快递配送
        if (deliveryReferenceMapper.selectCountProductByTemplateId(id, TenantContextHolder.getTenantId()) > 0) {
            throw exception(EXPRESS_TEMPLATE_REFERENCED_BY_SPU);
        }

        // 删除主表
        expressTemplateMapper.deleteById(id);
        // 删除运费从表
        expressTemplateChargeMapper.deleteByTemplateId(id);
        // 删除包邮从表
        expressTemplateFreeMapper.deleteByTemplateId(id);
    }

    /**
     * 校验计费规则区域互不重复、包邮规则区域互不重复
     *
     * @param charges 计费规则
     * @param frees   包邮规则
     */
    private void validateAreaNotDuplicate(List<SalesDeliveryExpressTemplateChargeBaseVO> charges,
                                          List<SalesDeliveryExpressTemplateFreeBaseVO> frees) {
        if (CollUtil.isNotEmpty(charges)) {
            Set<String> chargeAreaKeys = new HashSet<>();
            for (SalesDeliveryExpressTemplateChargeBaseVO charge : charges) {
                if (CollUtil.isEmpty(charge.getAreaIds())) {
                    continue; // 非空校验由 @NotEmpty 负责
                }
                String key = charge.getAreaIds().stream().sorted().map(String::valueOf).collect(Collectors.joining(","));
                if (!chargeAreaKeys.add(key)) {
                    throw exception(EXPRESS_TEMPLATE_CHARGE_AREA_DUPLICATE);
                }
            }
        }
        if (CollUtil.isNotEmpty(frees)) {
            Set<String> freeAreaKeys = new HashSet<>();
            for (SalesDeliveryExpressTemplateFreeBaseVO free : frees) {
                if (CollUtil.isEmpty(free.getAreaIds())) {
                    continue; // 非空校验由 @NotEmpty 负责
                }
                String key = free.getAreaIds().stream().sorted().map(String::valueOf).collect(Collectors.joining(","));
                if (!freeAreaKeys.add(key)) {
                    throw exception(EXPRESS_TEMPLATE_FREE_AREA_DUPLICATE);
                }
            }
        }
    }

    /**
     * 校验运费模板名是否唯一
     *
     * @param name 模板名称
     * @param id   运费模板编号,可以为 null
     */
    private void validateTemplateNameUnique(String name, Long id) {
        SalesDeliveryExpressTemplateDO template = expressTemplateMapper.selectByName(name);
        if (template == null) {
            return;
        }
        // 如果 id 为空
        if (id == null) {
            throw exception(EXPRESS_TEMPLATE_NAME_DUPLICATE);
        }
        if (!template.getId().equals(id)) {
            throw exception(EXPRESS_TEMPLATE_NAME_DUPLICATE);
        }
    }

    private void validateDeliveryExpressTemplateExists(Long id) {
        if (expressTemplateMapper.selectById(id) == null) {
            throw exception(EXPRESS_TEMPLATE_NOT_EXISTS);
        }
    }

    @Override
    public SalesDeliveryExpressTemplateDetailRespVO getDeliveryExpressTemplate(Long id) {
        List<SalesDeliveryExpressTemplateChargeDO> chargeList = expressTemplateChargeMapper.selectListByTemplateId(id);
        List<SalesDeliveryExpressTemplateFreeDO> freeList = expressTemplateFreeMapper.selectListByTemplateId(id);
        SalesDeliveryExpressTemplateDO template = expressTemplateMapper.selectById(id);
        return INSTANCE.convert(template, chargeList, freeList);
    }

    @Override
    public List<SalesDeliveryExpressTemplateDO> getDeliveryExpressTemplateList(Collection<Long> ids) {
        return expressTemplateMapper.selectByIds(ids);
    }

    @Override
    public List<SalesDeliveryExpressTemplateDO> getDeliveryExpressTemplateList() {
        return expressTemplateMapper.selectList();
    }

    @Override
    public PageResult<SalesDeliveryExpressTemplateDO> getDeliveryExpressTemplatePage(SalesDeliveryExpressTemplatePageReqVO pageReqVO) {
        return expressTemplateMapper.selectPage(pageReqVO);
    }

    @Override
    public SalesDeliveryExpressTemplateDO validateDeliveryExpressTemplate(Long templateId) {
        SalesDeliveryExpressTemplateDO template = expressTemplateMapper.selectById(templateId);
        if (template == null) {
            throw exception(EXPRESS_TEMPLATE_NOT_EXISTS);
        }
        return template;
    }

    @Override
    public SalesDeliveryExpressTemplateDO validateDeliveryExpressTemplateComputable(Long templateId) {
        SalesDeliveryExpressTemplateDO template = validateDeliveryExpressTemplate(templateId);
        // 至少有一条计费规则才算可计算模板
        if (CollUtil.isEmpty(expressTemplateChargeMapper.selectListByTemplateId(templateId))) {
            throw exception(EXPRESS_TEMPLATE_NOT_COMPUTABLE);
        }
        return template;
    }

    @Override
    public Map<Long, SalesDeliveryExpressTemplateRespBO> getExpressTemplateMapByIdsAndArea(Collection<Long> ids, Integer areaId) {
        Assert.notNull(areaId, "区域编号 {} 不能为空", areaId);
        // 查询 template 数组
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        List<SalesDeliveryExpressTemplateDO> templateList = expressTemplateMapper.selectByIds(ids);
        // 查询 templateCharge 数组
        List<SalesDeliveryExpressTemplateChargeDO> chargeList = expressTemplateChargeMapper.selectByTemplateIds(ids);
        // 查询 templateFree 数组
        List<SalesDeliveryExpressTemplateFreeDO> freeList = expressTemplateFreeMapper.selectListByTemplateIds(ids);

        // 组合运费模板配置 RespBO
        return INSTANCE.convertMap(areaId, templateList, chargeList, freeList);
    }

}

package vip.appap.suxin.module.sales.service;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDiyTemplateCreateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDiyTemplatePageReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDiyTemplatePropertyUpdateRequestVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDiyTemplateUpdateReqVO;
import vip.appap.suxin.module.sales.convert.SalesDiyPageConvert;
import vip.appap.suxin.module.sales.convert.SalesDiyTemplateConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDiyTemplateDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDiyPageDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesDiyPageMapper;
import vip.appap.suxin.module.sales.dal.mysql.SalesDiyTemplateMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.sales.enums.SalesErrorCodeConstants.*;

/**
 * 装修模板 Service 实现类
 *
 * @author owen
 */
@Service
@Validated
public class SalesDiyTemplateServiceImpl implements SalesDiyTemplateService {

    @Resource
    private SalesDiyTemplateMapper diyTemplateMapper;

    @Resource
    private SalesDiyPageService diyPageService;
    @Resource
    private SalesDiyPageMapper diyPageMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long createDiyTemplate(SalesDiyTemplateCreateReqVO createReqVO) {
        // 校验名称唯一
        validateNameUnique(null, createReqVO.getName());
        // 插入
        SalesDiyTemplateDO diyTemplate = SalesDiyTemplateConvert.INSTANCE.convert(createReqVO);
        diyTemplate.setProperty("{}");
        diyTemplateMapper.insert(diyTemplate);
        // 创建默认页面
        createDefaultPage(diyTemplate);
        // 返回
        return diyTemplate.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDiyTemplateWithDefaultPages(SalesDiyTemplateCreateReqVO createReqVO,
                                                  List<String> defaultPageNames) {
        validateNameUnique(null, createReqVO.getName());
        SalesDiyTemplateDO diyTemplate = SalesDiyTemplateConvert.INSTANCE.convert(createReqVO);
        diyTemplate.setProperty("{}");
        diyTemplateMapper.insert(diyTemplate);
        for (String pageName : defaultPageNames) {
            SalesDiyPageDO diyPage = SalesDiyPageConvert.INSTANCE.convert(
                    SalesDiyPageConvert.INSTANCE.convertCreateVo(diyTemplate.getId(), pageName,
                            String.format("模板【%s】自动创建", diyTemplate.getName())));
            diyPage.setProperty("{}");
            diyPageMapper.insert(diyPage);
        }
        return diyTemplate.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long copyDiyTemplate(Long sourceTemplateId, String targetName) {
        SalesDiyTemplateDO source = validateDiyTemplateExists(sourceTemplateId);
        validateNameUnique(null, targetName);
        SalesDiyTemplateDO target = SalesDiyTemplateDO.builder()
                .name(targetName)
                .remark(source.getRemark())
                .previewPicUrls(source.getPreviewPicUrls())
                .property(source.getProperty())
                .used(false)
                .usedTime(null)
                .build();
        diyTemplateMapper.insert(target);
        for (SalesDiyPageDO sourcePage : diyPageMapper.selectListByTemplateId(sourceTemplateId)) {
            SalesDiyPageDO targetPage = SalesDiyPageDO.builder()
                    .templateId(target.getId())
                    .name(sourcePage.getName())
                    .remark(sourcePage.getRemark())
                    .previewPicUrls(sourcePage.getPreviewPicUrls())
                    .property(sourcePage.getProperty())
                    .build();
            diyPageMapper.insert(targetPage);
        }
        return target.getId();
    }

    /**
     * 创建模板下面的默认页面
     * 默认创建两个页面：首页、我的
     *
     * @param diyTemplate 模板对象
     */
    private void createDefaultPage(SalesDiyTemplateDO diyTemplate) {
        String remark = String.format("模板【%s】自动创建", diyTemplate.getName());
        diyPageService.createDiyPage(SalesDiyPageConvert.INSTANCE.convertCreateVo(diyTemplate.getId(), "首页", remark));
        diyPageService.createDiyPage(SalesDiyPageConvert.INSTANCE.convertCreateVo(diyTemplate.getId(), "我的", remark));
    }

    @Override
    public void updateDiyTemplate(SalesDiyTemplateUpdateReqVO updateReqVO) {
        // 校验存在
        validateDiyTemplateExists(updateReqVO.getId());
        // 校验名称唯一
        validateNameUnique(updateReqVO.getId(), updateReqVO.getName());
        // 更新
        SalesDiyTemplateDO updateObj = SalesDiyTemplateConvert.INSTANCE.convert(updateReqVO);
        diyTemplateMapper.updateById(updateObj);
    }

    void validateNameUnique(Long id, String name) {
        if (StrUtil.isBlank(name)) {
            return;
        }
        SalesDiyTemplateDO template = diyTemplateMapper.selectByName(name);
        if (template == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的模板
        if (id == null) {
            throw exception(DIY_TEMPLATE_NAME_USED, name);
        }
        if (!template.getId().equals(id)) {
            throw exception(DIY_TEMPLATE_NAME_USED, name);
        }
    }

    @Override
    public void deleteDiyTemplate(Long id) {
        // 校验存在
        SalesDiyTemplateDO diyTemplateDO = validateDiyTemplateExists(id);
        // 校验使用中
        if (BooleanUtil.isTrue(diyTemplateDO.getUsed())) {
            throw exception(DIY_TEMPLATE_USED_CANNOT_DELETE);
        }
        // 删除
        diyTemplateMapper.deleteById(id);
    }

    private SalesDiyTemplateDO validateDiyTemplateExists(Long id) {
        SalesDiyTemplateDO diyTemplateDO = diyTemplateMapper.selectById(id);
        if (diyTemplateDO == null) {
            throw exception(DIY_TEMPLATE_NOT_EXISTS);
        }
        return diyTemplateDO;
    }

    @Override
    public SalesDiyTemplateDO getDiyTemplate(Long id) {
        return diyTemplateMapper.selectById(id);
    }

    @Override
    public PageResult<SalesDiyTemplateDO> getDiyTemplatePage(SalesDiyTemplatePageReqVO pageReqVO) {
        return diyTemplateMapper.selectPage(pageReqVO);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void useDiyTemplate(Long id) {
        // 校验存在
        validateDiyTemplateExists(id);
        // TODO @疯狂：要不已使用的情况，抛个业务异常？
        // 已使用的更新为未使用
        SalesDiyTemplateDO used = diyTemplateMapper.selectByUsed(true);
        if (used != null) {
            // 如果 id 相同，说明未发生变化
            if (used.getId().equals(id)) {
                return;
            }
            this.updateTemplateUsed(used.getId(), false, null);
        }
        // 更新为已使用
        this.updateTemplateUsed(id, true, LocalDateTime.now());
    }

    /**
     * 更新模板是否使用
     *
     * @param id       模板编号
     * @param used     是否使用
     * @param usedTime 使用时间
     */
    private void updateTemplateUsed(Long id, Boolean used, LocalDateTime usedTime) {
        SalesDiyTemplateDO updateObj = new SalesDiyTemplateDO().setId(id)
                .setUsed(used).setUsedTime(usedTime);
        diyTemplateMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDiyTemplateProperty(SalesDiyTemplatePropertyUpdateRequestVO updateReqVO) {
        // 校验存在
        validateDiyTemplateExists(updateReqVO.getId());
        // 更新模板属性
        SalesDiyTemplateDO updateObj = SalesDiyTemplateConvert.INSTANCE.convert(updateReqVO);
        diyTemplateMapper.updateById(updateObj);
    }

    @Override
    public SalesDiyTemplateDO getUsedDiyTemplate() {
        return diyTemplateMapper.selectByUsed(true);
    }

}

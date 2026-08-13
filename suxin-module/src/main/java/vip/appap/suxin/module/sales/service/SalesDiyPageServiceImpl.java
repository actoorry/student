package vip.appap.suxin.module.sales.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDiyPageCreateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDiyPagePageReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDiyPagePropertyUpdateRequestVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDiyPageUpdateReqVO;
import vip.appap.suxin.module.sales.convert.SalesDiyPageConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDiyPageDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesDiyPageMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.sales.enums.SalesErrorCodeConstants.DIY_PAGE_NAME_USED;
import static vip.appap.suxin.module.sales.enums.SalesErrorCodeConstants.DIY_PAGE_NOT_EXISTS;

/**
 * 装修页面 Service 实现类
 *
 * @author owen
 */
@Service
@Validated
public class SalesDiyPageServiceImpl implements SalesDiyPageService {

    @Resource
    private SalesDiyPageMapper diyPageMapper;

    @Override
    public Long createDiyPage(SalesDiyPageCreateReqVO createReqVO) {
        // 校验名称唯一
        validateNameUnique(null, createReqVO.getTemplateId(), createReqVO.getName());
        // 插入
        SalesDiyPageDO diyPage = SalesDiyPageConvert.INSTANCE.convert(createReqVO);
        diyPage.setProperty("{}");
        diyPageMapper.insert(diyPage);
        return diyPage.getId();
    }

    @Override
    public void updateDiyPage(SalesDiyPageUpdateReqVO updateReqVO) {
        // 校验存在
        validateDiyPageExists(updateReqVO.getId());
        // 校验名称唯一
        validateNameUnique(updateReqVO.getId(), updateReqVO.getTemplateId(), updateReqVO.getName());
        // 更新
        SalesDiyPageDO updateObj = SalesDiyPageConvert.INSTANCE.convert(updateReqVO);
        diyPageMapper.updateById(updateObj);
    }

    /**
     * 校验 Page 页面，在一个 template 模版下的名字是唯一的
     *
     * @param id Page 编号
     * @param templateId 模版编号
     * @param name Page 名字
     */
    void validateNameUnique(Long id, Long templateId, String name) {
        if (StrUtil.isBlank(name)) {
            return;
        }
        if (templateId != null) {
            SalesDiyPageDO sameNamePage = diyPageMapper.selectListByTemplateId(templateId).stream()
                    .filter(page -> name.equals(page.getName()))
                    .findFirst().orElse(null);
            if (sameNamePage != null && !sameNamePage.getId().equals(id)) {
                throw exception(DIY_PAGE_NAME_USED, name);
            }
            return;
        }
        SalesDiyPageDO page = diyPageMapper.selectByNameAndTemplateIdIsNull(name);
        if (page == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的页面
        if (id == null) {
            throw exception(DIY_PAGE_NAME_USED, name);
        }
        if (!page.getId().equals(id)) {
            throw exception(DIY_PAGE_NAME_USED, name);
        }
    }

    @Override
    public void deleteDiyPage(Long id) {
        // 校验存在
        validateDiyPageExists(id);
        // 删除
        diyPageMapper.deleteById(id);
    }

    private SalesDiyPageDO validateDiyPageExists(Long id) {
        SalesDiyPageDO diyPage = diyPageMapper.selectById(id);
        if (diyPage == null) {
            throw exception(DIY_PAGE_NOT_EXISTS);
        }
        return diyPage;
    }

    @Override
    public SalesDiyPageDO getDiyPage(Long id) {
        return diyPageMapper.selectById(id);
    }

    @Override
    public List<SalesDiyPageDO> getDiyPageList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return ListUtil.empty();
        }
        return diyPageMapper.selectByIds(ids);
    }

    @Override
    public PageResult<SalesDiyPageDO> getDiyPagePage(SalesDiyPagePageReqVO pageReqVO) {
        return diyPageMapper.selectPage(pageReqVO);
    }

    @Override
    public List<SalesDiyPageDO> getDiyPageByTemplateId(Long templateId) {
        return diyPageMapper.selectListByTemplateId(templateId);
    }

    @Override
    public void updateDiyPageProperty(SalesDiyPagePropertyUpdateRequestVO updateReqVO) {
        // 校验存在
        validateDiyPageExists(updateReqVO.getId());
        // 更新
        SalesDiyPageDO updateObj = SalesDiyPageConvert.INSTANCE.convert(updateReqVO);
        diyPageMapper.updateById(updateObj);
    }

}

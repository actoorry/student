package vip.appap.suxin.module.sales.convert;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.collection.CollectionUtils;
import vip.appap.suxin.module.sales.controller.admin.vo.*;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDeliveryExpressTemplateChargeDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDeliveryExpressTemplateDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDeliveryExpressTemplateFreeDO;
import vip.appap.suxin.module.sales.service.bo.SalesDeliveryExpressTemplateRespBO;
import com.google.common.collect.Maps;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.findFirst;

@Mapper
public interface SalesDeliveryExpressTemplateConvert {

    SalesDeliveryExpressTemplateConvert INSTANCE = Mappers.getMapper(SalesDeliveryExpressTemplateConvert.class);

    // ========== Template ==========

    SalesDeliveryExpressTemplateDO convert(SalesDeliveryExpressTemplateCreateReqVO bean);

    SalesDeliveryExpressTemplateDO convert(SalesDeliveryExpressTemplateUpdateReqVO bean);

    SalesDeliveryExpressTemplateRespVO convert(SalesDeliveryExpressTemplateDO bean);

    SalesDeliveryExpressTemplateDetailRespVO convert2(SalesDeliveryExpressTemplateDO bean);

    List<SalesDeliveryExpressTemplateRespVO> convertList(List<SalesDeliveryExpressTemplateDO> list);

    List<SalesDeliveryExpressTemplateSimpleRespVO> convertList1(List<SalesDeliveryExpressTemplateDO> list);

    PageResult<SalesDeliveryExpressTemplateRespVO> convertPage(PageResult<SalesDeliveryExpressTemplateDO> page);

    default SalesDeliveryExpressTemplateDetailRespVO convert(SalesDeliveryExpressTemplateDO bean,
                                                        List<SalesDeliveryExpressTemplateChargeDO> chargeList,
                                                        List<SalesDeliveryExpressTemplateFreeDO> freeList) {
        SalesDeliveryExpressTemplateDetailRespVO respVO = convert2(bean);
        respVO.setCharges(convertTemplateChargeList(chargeList));
        respVO.setFrees(convertTemplateFreeList(freeList));
        return respVO;
    }

    // ========== Template Charge ==========

    SalesDeliveryExpressTemplateChargeDO convertTemplateCharge(Long templateId, Integer chargeMode, SalesDeliveryExpressTemplateChargeBaseVO vo);

    SalesDeliveryExpressTemplateRespBO.Charge convertTemplateCharge(SalesDeliveryExpressTemplateChargeDO bean);

    default List<SalesDeliveryExpressTemplateChargeDO> convertTemplateChargeList(Long templateId, Integer chargeMode, List<SalesDeliveryExpressTemplateChargeBaseVO> list) {
        return CollectionUtils.convertList(list, vo -> convertTemplateCharge(templateId, chargeMode, vo));
    }

    // ========== Template Free ==========

    SalesDeliveryExpressTemplateFreeDO convertTemplateFree(Long templateId, SalesDeliveryExpressTemplateFreeBaseVO vo);

    SalesDeliveryExpressTemplateRespBO.Free convertTemplateFree(SalesDeliveryExpressTemplateFreeDO bean);

    List<SalesDeliveryExpressTemplateChargeBaseVO> convertTemplateChargeList(List<SalesDeliveryExpressTemplateChargeDO> list);

    List<SalesDeliveryExpressTemplateFreeBaseVO> convertTemplateFreeList(List<SalesDeliveryExpressTemplateFreeDO> list);

    default List<SalesDeliveryExpressTemplateFreeDO> convertTemplateFreeList(Long templateId, List<SalesDeliveryExpressTemplateFreeBaseVO> list) {
        return CollectionUtils.convertList(list, vo -> convertTemplateFree(templateId, vo));
    }

    default Map<Long, SalesDeliveryExpressTemplateRespBO> convertMap(Integer areaId, List<SalesDeliveryExpressTemplateDO> templateList,
                                                                List<SalesDeliveryExpressTemplateChargeDO> chargeList,
                                                                List<SalesDeliveryExpressTemplateFreeDO> freeList) {
        Map<Long, List<SalesDeliveryExpressTemplateChargeDO>> templateIdChargeMap = convertMultiMap(chargeList,
                SalesDeliveryExpressTemplateChargeDO::getTemplateId);
        Map<Long, List<SalesDeliveryExpressTemplateFreeDO>> templateIdFreeMap = convertMultiMap(freeList,
                SalesDeliveryExpressTemplateFreeDO::getTemplateId);
        // 组合运费模板配置 RespBO
        Map<Long, SalesDeliveryExpressTemplateRespBO> result = Maps.newHashMapWithExpectedSize(templateList.size());
        templateList.forEach(template -> {
            SalesDeliveryExpressTemplateRespBO bo = new SalesDeliveryExpressTemplateRespBO()
                    .setChargeMode(template.getChargeMode())
                    .setCharge(convertTemplateCharge(findFirst(templateIdChargeMap.get(template.getId()), charge -> charge.getAreaIds().contains(areaId))))
                    .setFree(convertTemplateFree(findFirst(templateIdFreeMap.get(template.getId()), free -> free.getAreaIds().contains(areaId))));
            if (bo.getCharge() != null || bo.getFree() != null) {
                result.put(template.getId(), bo);
            }
        });
        return result;
    }

}

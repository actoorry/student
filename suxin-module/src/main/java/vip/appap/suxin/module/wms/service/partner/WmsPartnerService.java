package vip.appap.suxin.module.wms.service.partner;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import vip.appap.suxin.module.partner.api.PartnerApi;
import vip.appap.suxin.module.partner.api.dto.PartnerRespDTO;

import java.util.*;
import java.util.stream.Collectors;

/**
 * WMS 客商服务（通过 PartnerApi 跨模块调用）
 *
 * 禁止直接引入 partner 内部 Service 或 DO，统一通过 PartnerApi 对外接口调用。
 * 禁止修改 PartnerRespDTO 内部属性。
 */
@Service
@Validated
public class WmsPartnerService {

    @Resource
    private PartnerApi partnerApi;

    /**
     * 获取供应商列表（用于下拉选择）
     */
    public List<PartnerRespDTO> getSupplierList() {
        // 通过昵称模糊查询获取所有合作伙伴，再过滤出供应商
        return partnerApi.getPartnerListByNickname("")
                .stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsSupplier()))
                .collect(Collectors.toList());
    }

    /**
     * 获取客户列表（用于下拉选择）
     */
    public List<PartnerRespDTO> getCustomerList() {
        return partnerApi.getPartnerListByNickname("")
                .stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsCustomer()))
                .collect(Collectors.toList());
    }

    /**
     * 获取公司列表（用于仓库负责人下拉选择，仅 isCompany=true）
     */
    public List<PartnerRespDTO> getCompanyList() {
        return partnerApi.getPartnerListByNickname("")
                .stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsCompany()))
                .collect(Collectors.toList());
    }

    /**
     * 根据 ID 获取客商信息
     */
    public PartnerRespDTO getPartner(Long id) {
        if (id == null) return null;
        return partnerApi.getPartner(id);
    }

    /**
     * 批量获取客商名称 Map（用于列表展示）
     *
     * @param ids 客商 ID 集合
     * @return Map<id, nickname>
     */
    public Map<Long, String> getPartnerNameMap(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyMap();
        List<PartnerRespDTO> list = partnerApi.getPartnerList(ids);
        return list.stream()
                .filter(p -> p != null)
                .collect(Collectors.toMap(PartnerRespDTO::getId, PartnerRespDTO::getNickname, (a, b) -> a));
    }

    /**
     * 获取单个客商名称
     */
    public String getPartnerName(Long id) {
        if (id == null) return null;
        PartnerRespDTO partner = partnerApi.getPartner(id);
        return partner != null ? partner.getNickname() : null;
    }

}
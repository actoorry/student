package vip.appap.suxin.module.partner.api.dto;

import lombok.Data;

@Data
public class PartnerAddressRespDTO {

    private Long id;
    private Long userId;
    private Integer type;
    private String name;
    private String mobile;
    private Integer areaId;
    private String detailAddress;
}

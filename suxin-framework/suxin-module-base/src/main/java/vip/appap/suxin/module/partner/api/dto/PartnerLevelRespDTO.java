package vip.appap.suxin.module.partner.api.dto;

import lombok.Data;

@Data
public class PartnerLevelRespDTO {

    private Long id;
    private String name;
    private Integer status;
    private Integer discountPercent;
}

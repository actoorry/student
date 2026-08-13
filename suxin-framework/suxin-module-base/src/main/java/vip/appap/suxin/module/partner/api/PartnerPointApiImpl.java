package vip.appap.suxin.module.partner.api;

import vip.appap.suxin.module.partner.service.PartnerPointRecordService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class PartnerPointApiImpl implements PartnerPointApi {

    @Resource
    private PartnerPointRecordService partnerPointRecordService;

    @Override
    public void addPoint(Long userId, Integer point, Integer bizType, String bizId) {
        partnerPointRecordService.createPointRecord(userId, point, bizType, bizId);
    }

    @Override
    public void reducePoint(Long userId, Integer point, Integer bizType, String bizId) {
        partnerPointRecordService.createPointRecord(userId, -point, bizType, bizId);
    }

}

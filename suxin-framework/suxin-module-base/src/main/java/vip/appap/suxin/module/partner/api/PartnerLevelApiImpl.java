package vip.appap.suxin.module.partner.api;

import vip.appap.suxin.module.partner.api.dto.PartnerLevelRespDTO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerLevelDO;
import vip.appap.suxin.module.partner.service.PartnerExperienceRecordService;
import vip.appap.suxin.module.partner.service.PartnerLevelService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import vip.appap.suxin.module.partner.service.PartnerService;

@Service
public class PartnerLevelApiImpl implements PartnerLevelApi {

    @Resource
    private PartnerLevelService partnerLevelService;
    @Resource
    private PartnerService partnerService;
    @Resource
    private PartnerExperienceRecordService partnerExperienceRecordService;

    @Override
    public PartnerLevelRespDTO getMemberLevel(Long levelId) {
        PartnerLevelDO level = partnerLevelService.getLevel(levelId);
        if (level == null) {
            return null;
        }
        PartnerLevelRespDTO respDTO = new PartnerLevelRespDTO();
        respDTO.setId(level.getId());
        respDTO.setName(level.getName());
        respDTO.setStatus(level.getStatus());
        respDTO.setDiscountPercent(level.getDiscountPercent());
        return respDTO;
    }

    @Override
    public void addExperience(Long userId, Integer price, Integer bizType, String bizId) {
        createExperienceRecord(userId, price, bizType, bizId);
    }

    @Override
    public void reduceExperience(Long userId, Integer price, Integer bizType, String bizId) {
        createExperienceRecord(userId, -price, bizType, bizId);
    }

    private void createExperienceRecord(Long userId, Integer experience, Integer bizType, String bizId) {
        PartnerDO partner = partnerService.getPartner(userId);
        if (partner == null) {
            return;
        }
        int currentExperience = partner.getExperience() != null ? partner.getExperience() : 0;
        int totalExperience = currentExperience + experience;
        partnerExperienceRecordService.createExperienceRecord(userId, experience, totalExperience, bizType, bizId);
    }

}

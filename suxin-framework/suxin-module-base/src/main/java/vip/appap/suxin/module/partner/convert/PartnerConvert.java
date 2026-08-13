package vip.appap.suxin.module.partner.convert;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.api.dto.PartnerRespDTO;
import vip.appap.suxin.module.partner.controller.admin.vo.*;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerGroupDO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerLevelDO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerTagDO;
import vip.appap.suxin.module.system.dal.dataobject.AdminUserDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertMap;

@Mapper
public interface PartnerConvert {

    PartnerConvert INSTANCE = Mappers.getMapper(PartnerConvert.class);

    PartnerRespDTO convertDTO(PartnerDO bean);

    List<PartnerRespDTO> convertDTOList(List<PartnerDO> list);

    List<PartnerRespVO> convertList(List<PartnerDO> list);

    PartnerDO convert(PartnerCreateReqVO bean);

    PartnerDO convert(PartnerUpdateReqVO bean);

    PageResult<PartnerRespVO> convertPage(PageResult<PartnerDO> page);

    PartnerRespVO convertRespVO(PartnerDO bean);

    default PartnerMemberRespVO convertMember(PartnerDO partner, AdminUserDO user) {
        if (partner == null) {
            return null;
        }
        PartnerMemberRespVO respVO = new PartnerMemberRespVO();
        respVO.setId(partner.getId());
        respVO.setMobile(partner.getMobile());
        respVO.setNickname(partner.getNickname());
        respVO.setAvatar(partner.getAvatar());
        respVO.setName(partner.getNickname());
        respVO.setSex(partner.getSex());
        respVO.setBirthday(partner.getBirthday());
        respVO.setAreaId(partner.getAreaId() != null ? partner.getAreaId().intValue() : null);
        respVO.setRemark(partner.getRemark());
        respVO.setCreateTime(partner.getCreateTime());
        respVO.setPoint(partner.getPoint());
        respVO.setCustomerLevel(partner.getCustomerLevel());
        respVO.setExperience(partner.getExperience());
        respVO.setGroupId(partner.getGroupId());
        respVO.setTagIds(partner.getTagIds());
        respVO.setRegisterIp(partner.getRegisterIp());
        respVO.setRegisterTerminal(partner.getRegisterTerminal());
        if (user != null) {
            respVO.setUsername(user.getUsername());
            respVO.setLoginIp(user.getLoginIp());
            respVO.setLoginDate(user.getLoginDate());
        }
        return respVO;
    }

    default PageResult<PartnerMemberRespVO> convertMemberPage(PageResult<PartnerDO> pageResult,
                                                              Map<Long, AdminUserDO> userMap,
                                                              List<PartnerTagDO> tags,
                                                              List<PartnerLevelDO> levels,
                                                              List<PartnerGroupDO> groups) {
        Map<Long, String> tagMap = convertMap(tags, PartnerTagDO::getId, PartnerTagDO::getName);
        Map<Long, String> levelMap = convertMap(levels, PartnerLevelDO::getId, PartnerLevelDO::getName);
        Map<Long, String> groupMap = convertMap(groups, PartnerGroupDO::getId, PartnerGroupDO::getName);

        List<PartnerMemberRespVO> list = pageResult.getList().stream().map(partner -> {
            AdminUserDO user = userMap.get(partner.getId());
            PartnerMemberRespVO respVO = convertMember(partner, user);
            respVO.setLevelName(levelMap.get(partner.getCustomerLevel()));
            respVO.setGroupName(groupMap.get(partner.getGroupId()));
            if (partner.getTagIds() != null && !partner.getTagIds().isEmpty()) {
                String[] tagIdArr = partner.getTagIds().split(",");
                List<String> tagNames = new ArrayList<>();
                for (String tagId : tagIdArr) {
                    String tagName = tagMap.get(Long.parseLong(tagId.trim()));
                    if (tagName != null) {
                        tagNames.add(tagName);
                    }
                }
                respVO.setTagNames(tagNames);
            }
            return respVO;
        }).collect(java.util.stream.Collectors.toList());

        return new PageResult<>(list, pageResult.getTotal());
    }

}

package vip.appap.suxin.module.rongjh.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.framework.ip.core.utils.AreaUtils;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import vip.appap.suxin.module.partner.dal.mysql.PartnerMapper;
import vip.appap.suxin.module.rongjh.controller.admin.vo.WarriorPageReqVO;
import vip.appap.suxin.module.rongjh.controller.admin.vo.WarriorRespVO;
import vip.appap.suxin.module.rongjh.controller.app.vo.AppWarriorStatusRespVO;
import vip.appap.suxin.module.rongjh.controller.app.vo.AppWarriorSubmitReqVO;
import vip.appap.suxin.module.rongjh.dal.dataobject.PartnerWarriorDO;
import vip.appap.suxin.module.rongjh.dal.mysql.PartnerWarriorMapper;
import vip.appap.suxin.module.rongjh.enums.WarriorStatusEnum;
import vip.appap.suxin.module.rongjh.enums.WarriorTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.rongjh.enums.ErrorCodeConstants.*;

@Service
@Validated
public class WarriorServiceImpl implements WarriorService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private PartnerWarriorMapper warriorMapper;
    @Resource
    private PartnerMapper partnerMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitWarrior(Long partnerId, AppWarriorSubmitReqVO reqVO) {
        PartnerWarriorDO exist = warriorMapper.selectById(partnerId);
        PartnerWarriorDO warrior = buildWarriorDO(partnerId, reqVO);
        if (exist != null && WarriorStatusEnum.APPROVED.getStatus().equals(exist.getStatus())) {
            warrior.setStatus(WarriorStatusEnum.APPROVED.getStatus());
            warrior.setRemark(exist.getRemark());
            warrior.setCreateTime(exist.getCreateTime());
        } else {
            warrior.setStatus(WarriorStatusEnum.PENDING.getStatus());
            warrior.setRemark(null);
            warrior.setCreateTime(exist != null ? exist.getCreateTime() : LocalDateTime.now());
        }
        if (exist != null) {
            warriorMapper.updateById(warrior);
        } else {
            warriorMapper.insert(warrior);
        }
        syncPartnerProfile(partnerId, reqVO);
    }

    @Override
    public PartnerWarriorDO getWarrior(Long partnerId) {
        return warriorMapper.selectById(partnerId);
    }

    @Override
    public AppWarriorStatusRespVO getWarriorStatus(Long partnerId, Integer filterStateId) {
        PartnerWarriorDO warrior = warriorMapper.selectById(partnerId);
        AppWarriorStatusRespVO resp = new AppWarriorStatusRespVO();
        resp.setTotalCount(warriorMapper.selectCountByStatus(WarriorStatusEnum.APPROVED.getStatus()));
        if (filterStateId != null && filterStateId > 0) {
            resp.setMemberCount(warriorMapper.selectCountByStateIdAndStatus(filterStateId, WarriorStatusEnum.APPROVED.getStatus()));
        } else {
            resp.setMemberCount(resp.getTotalCount());
        }
        if (warrior == null) {
            resp.setHasApplication(false);
            resp.setIsMember(false);
            return resp;
        }
        resp.setHasApplication(true);
        resp.setIsMember(WarriorStatusEnum.APPROVED.getStatus().equals(warrior.getStatus()));
        resp.setName(warrior.getName());
        resp.setPhone(warrior.getPhone());
        resp.setStateId(warrior.getStateId());
        resp.setStateName(resolveStateName(warrior));
        resp.setCity(warrior.getCity());
        resp.setServiceUnit(warrior.getServiceUnit());
        resp.setServiceYear(warrior.getServiceYear());
        resp.setDescription(warrior.getDescription());
        fillStatusDisplay(resp, warrior);
        if (warrior.getCreateTime() != null) {
            resp.setCreateDate(warrior.getCreateTime().format(DATE_TIME_FORMATTER));
        }
        if (WarriorStatusEnum.APPROVED.getStatus().equals(warrior.getStatus()) && warrior.getUpdateTime() != null) {
            resp.setApproveDate(warrior.getUpdateTime().format(DATE_TIME_FORMATTER));
        }
        return resp;
    }

    @Override
    public PageResult<WarriorRespVO> getWarriorPage(WarriorPageReqVO reqVO) {
        List<Long> partnerIds = resolvePartnerIdsByMobile(reqVO.getMobile());
        if (partnerIds != null && partnerIds.isEmpty()) {
            return PageResult.empty();
        }
        PageResult<PartnerWarriorDO> page = warriorMapper.selectPage(reqVO, partnerIds);
        if (CollUtil.isEmpty(page.getList())) {
            return PageResult.empty(page.getTotal());
        }
        List<Long> ids = page.getList().stream().map(PartnerWarriorDO::getId).toList();
        Map<Long, PartnerDO> partnerMap = partnerMapper.selectByIds(ids).stream()
                .collect(Collectors.toMap(PartnerDO::getId, item -> item, (a, b) -> a));
        List<WarriorRespVO> list = page.getList().stream()
                .map(item -> convert(item, partnerMap.get(item.getId()), false))
                .toList();
        return new PageResult<>(list, page.getTotal());
    }

    @Override
    public WarriorRespVO getWarriorDetail(Long id) {
        PartnerWarriorDO warrior = validateExists(id);
        PartnerDO partner = partnerMapper.selectById(id);
        return convert(warrior, partner, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveWarrior(Long id, String remark) {
        PartnerWarriorDO warrior = validateExists(id);
        if (!WarriorStatusEnum.PENDING.getStatus().equals(warrior.getStatus())) {
            throw exception(WARRIOR_STATUS_AUDIT_FAIL);
        }
        PartnerWarriorDO update = new PartnerWarriorDO();
        update.setId(id);
        update.setStatus(WarriorStatusEnum.APPROVED.getStatus());
        update.setRemark(remark);
        update.setUpdateTime(LocalDateTime.now());
        warriorMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectWarrior(Long id, String remark) {
        PartnerWarriorDO warrior = validateExists(id);
        if (!WarriorStatusEnum.PENDING.getStatus().equals(warrior.getStatus())) {
            throw exception(WARRIOR_STATUS_REJECT_FAIL);
        }
        PartnerWarriorDO update = new PartnerWarriorDO();
        update.setId(id);
        update.setStatus(WarriorStatusEnum.REJECTED.getStatus());
        update.setRemark(remark);
        update.setUpdateTime(LocalDateTime.now());
        warriorMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void blacklistWarrior(Long id, String remark) {
        PartnerWarriorDO warrior = validateExists(id);
        if (!WarriorStatusEnum.APPROVED.getStatus().equals(warrior.getStatus())) {
            throw exception(WARRIOR_STATUS_BLACKLIST_FAIL);
        }
        PartnerWarriorDO update = new PartnerWarriorDO();
        update.setId(id);
        update.setStatus(WarriorStatusEnum.REJECTED.getStatus());
        update.setRemark(remark);
        update.setUpdateTime(LocalDateTime.now());
        warriorMapper.updateById(update);
    }

    private PartnerWarriorDO validateExists(Long id) {
        PartnerWarriorDO warrior = warriorMapper.selectById(id);
        if (warrior == null) {
            throw exception(WARRIOR_NOT_EXISTS);
        }
        return warrior;
    }

    private List<Long> resolvePartnerIdsByMobile(String mobile) {
        if (StrUtil.isBlank(mobile)) {
            return null;
        }
        PartnerDO partner = partnerMapper.selectByMobile(mobile.trim());
        return partner == null ? Collections.emptyList() : List.of(partner.getId());
    }

    private PartnerWarriorDO buildWarriorDO(Long partnerId, AppWarriorSubmitReqVO reqVO) {
        String province = StrUtil.blankToDefault(reqVO.getProvince(), reqVO.getStateName());
        return PartnerWarriorDO.builder()
                .id(partnerId)
                .type(StrUtil.blankToDefault(reqVO.getType(), WarriorTypeEnum.SELF.getType()))
                .name(reqVO.getName())
                .idCard(reqVO.getIdCard())
                .phone(reqVO.getPhone())
                .province(province)
                .stateId(reqVO.getStateId())
                .city(reqVO.getCity())
                .militaryBranch(reqVO.getMilitaryBranch())
                .serviceYears(reqVO.getServiceYears())
                .serviceUnit(reqVO.getServiceUnit())
                .serviceYear(reqVO.getServiceYear())
                .description(reqVO.getDescription())
                .certificateImg(reqVO.getCertificateImg())
                .updateTime(LocalDateTime.now())
                .build();
    }

    private void syncPartnerProfile(Long partnerId, AppWarriorSubmitReqVO reqVO) {
        PartnerDO update = new PartnerDO();
        update.setId(partnerId);
        boolean changed = false;
        if (StrUtil.isNotBlank(reqVO.getName())) {
            update.setName(reqVO.getName());
            changed = true;
        }
        if (StrUtil.isNotBlank(reqVO.getIdCard())) {
            update.setIdCard(reqVO.getIdCard());
            changed = true;
        }
        if (StrUtil.isNotBlank(reqVO.getPhone())) {
            update.setMobile(reqVO.getPhone());
            changed = true;
        }
        if (changed) {
            partnerMapper.updateById(update);
        }
    }

    private void fillStatusDisplay(AppWarriorStatusRespVO resp, PartnerWarriorDO warrior) {
        if (WarriorStatusEnum.APPROVED.getStatus().equals(warrior.getStatus())) {
            resp.setState("approved");
            resp.setStateDisplay("已通过");
        } else if (WarriorStatusEnum.REJECTED.getStatus().equals(warrior.getStatus())) {
            resp.setState("rejected");
            resp.setStateDisplay(StrUtil.blankToDefault(warrior.getRemark(), "已驳回"));
        } else {
            resp.setState("pending");
            resp.setStateDisplay("审核中");
        }
    }

    private String resolveStateName(PartnerWarriorDO warrior) {
        if (StrUtil.isNotBlank(warrior.getProvince())) {
            return warrior.getProvince();
        }
        if (warrior.getStateId() != null) {
            var area = AreaUtils.getArea(warrior.getStateId());
            if (area != null) {
                return area.getName();
            }
        }
        return null;
    }

    private WarriorRespVO convert(PartnerWarriorDO warrior, PartnerDO partner, boolean showFullIdCard) {
        WarriorRespVO resp = BeanUtils.toBean(warrior, WarriorRespVO.class);
        if (partner != null) {
            resp.setNickname(partner.getNickname());
            resp.setMobile(partner.getMobile());
            if (StrUtil.isBlank(resp.getIdCard())) {
                resp.setIdCard(partner.getIdCard());
            }
        }
        resp.setTypeName(WarriorTypeEnum.getName(warrior.getType()));
        resp.setStatusName(WarriorStatusEnum.getName(warrior.getStatus()));
        resp.setIdCard(maskIdCard(resp.getIdCard(), showFullIdCard));
        return resp;
    }

    private String maskIdCard(String idCard, boolean showFull) {
        if (StrUtil.isBlank(idCard) || showFull) {
            return idCard;
        }
        if (idCard.length() <= 8) {
            return idCard;
        }
        return StrUtil.hide(idCard, 3, idCard.length() - 4);
    }

}

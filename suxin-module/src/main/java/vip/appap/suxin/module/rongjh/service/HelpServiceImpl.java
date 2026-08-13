package vip.appap.suxin.module.rongjh.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import vip.appap.suxin.module.partner.dal.mysql.PartnerMapper;
import vip.appap.suxin.module.rongjh.controller.admin.vo.HelpPageReqVO;
import vip.appap.suxin.module.rongjh.controller.admin.vo.HelpRespVO;
import vip.appap.suxin.module.rongjh.controller.app.vo.AppHelpSubmitReqVO;
import vip.appap.suxin.module.rongjh.controller.app.vo.AppHelpTotalAmountRespVO;
import vip.appap.suxin.module.rongjh.dal.dataobject.PartnerHelpDO;
import vip.appap.suxin.module.rongjh.dal.mysql.PartnerHelpMapper;
import vip.appap.suxin.module.rongjh.enums.HelpStatusEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.rongjh.enums.ErrorCodeConstants.*;

@Service
@Validated
public class HelpServiceImpl implements HelpService {

    @Resource
    private PartnerHelpMapper helpMapper;
    @Resource
    private PartnerMapper partnerMapper;
    @Resource
    private LoveValueService loveValueService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createHelp(Long partnerId, AppHelpSubmitReqVO reqVO) {
        String reason = StrUtil.blankToDefault(reqVO.getReason(), reqVO.getDescription());
        if (StrUtil.isBlank(reason)) {
            throw exception(HELP_REASON_REQUIRED);
        }
        PartnerHelpDO help = PartnerHelpDO.builder()
                .partnerId(partnerId)
                .name(reqVO.getName())
                .phone(StrUtil.trim(reqVO.getPhone()))
                .idCard(StrUtil.blankToDefault(reqVO.getIdCard(), ""))
                .reason(buildReason(reason, reqVO))
                .applyAmount(reqVO.getApplyAmount())
                .materials(buildMaterials(reqVO))
                .status(HelpStatusEnum.PENDING.getStatus())
                .build();
        helpMapper.insert(help);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelHelp(Long partnerId, Long id) {
        PartnerHelpDO help = helpMapper.selectById(id);
        if (help == null || !Objects.equals(help.getPartnerId(), partnerId)) {
            throw exception(HELP_NOT_EXISTS);
        }
        if (!HelpStatusEnum.PENDING.getStatus().equals(help.getStatus())) {
            throw exception(HELP_CANNOT_CANCEL);
        }
        PartnerHelpDO update = new PartnerHelpDO();
        update.setId(id);
        update.setStatus(HelpStatusEnum.CANCELLED.getStatus());
        helpMapper.updateById(update);
    }

    @Override
    public List<PartnerHelpDO> getHelpList(Long partnerId) {
        return helpMapper.selectByPartnerId(partnerId);
    }

    @Override
    public PartnerHelpDO getHelp(Long partnerId, Long id) {
        PartnerHelpDO help = helpMapper.selectById(id);
        if (help == null || !Objects.equals(help.getPartnerId(), partnerId)) {
            throw exception(HELP_NOT_EXISTS);
        }
        return help;
    }

    @Override
    public AppHelpTotalAmountRespVO getTotalAmount(Long userId) {
        BigDecimal amount = helpMapper.sumApprovedActualAmount();
        BigDecimal safeAmount = amount != null ? amount : BigDecimal.ZERO;
        BigDecimal userLoveValue = loveValueService.getUserLoveValue(userId);
        AppHelpTotalAmountRespVO resp = new AppHelpTotalAmountRespVO();
        resp.setContributionAmount(userLoveValue);
        resp.setTotalAmount(safeAmount);
        resp.setDeadline(LocalDate.now().toString());
        return resp;
    }

    @Override
    public PageResult<HelpRespVO> getHelpPage(HelpPageReqVO pageReqVO) {
        List<Long> partnerIds = resolvePartnerIdsByMobile(pageReqVO.getMobile());
        if (partnerIds != null && partnerIds.isEmpty()) {
            return PageResult.empty();
        }
        PageResult<PartnerHelpDO> page = helpMapper.selectPage(pageReqVO, partnerIds);
        if (CollUtil.isEmpty(page.getList())) {
            return PageResult.empty(page.getTotal());
        }
        List<Long> ids = page.getList().stream().map(PartnerHelpDO::getPartnerId).distinct().toList();
        Map<Long, PartnerDO> partnerMap = partnerMapper.selectByIds(ids).stream()
                .collect(Collectors.toMap(PartnerDO::getId, item -> item, (a, b) -> a));
        List<HelpRespVO> list = page.getList().stream()
                .map(item -> convert(item, partnerMap.get(item.getPartnerId())))
                .toList();
        return new PageResult<>(list, page.getTotal());
    }

    @Override
    public HelpRespVO getHelpDetail(Long id) {
        PartnerHelpDO help = validateHelpExists(id);
        PartnerDO partner = partnerMapper.selectById(help.getPartnerId());
        return convert(help, partner);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveHelp(Long id, BigDecimal actualAmount, String remark) {
        PartnerHelpDO help = validateHelpExists(id);
        if (!HelpStatusEnum.PENDING.getStatus().equals(help.getStatus())) {
            throw exception(HELP_STATUS_AUDIT_FAIL);
        }
        if (actualAmount == null || actualAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw exception(HELP_ACTUAL_AMOUNT_REQUIRED);
        }
        PartnerHelpDO update = new PartnerHelpDO();
        update.setId(id);
        update.setStatus(HelpStatusEnum.APPROVED.getStatus());
        update.setActualAmount(actualAmount);
        update.setRemark(remark);
        update.setUpdateTime(LocalDateTime.now());
        helpMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectHelp(Long id, String remark) {
        PartnerHelpDO help = validateHelpExists(id);
        if (!HelpStatusEnum.PENDING.getStatus().equals(help.getStatus())) {
            throw exception(HELP_STATUS_REJECT_FAIL);
        }
        PartnerHelpDO update = new PartnerHelpDO();
        update.setId(id);
        update.setStatus(HelpStatusEnum.REJECTED.getStatus());
        update.setRemark(remark);
        update.setUpdateTime(LocalDateTime.now());
        helpMapper.updateById(update);
    }

    private PartnerHelpDO validateHelpExists(Long id) {
        PartnerHelpDO help = helpMapper.selectById(id);
        if (help == null) {
            throw exception(HELP_NOT_EXISTS);
        }
        return help;
    }

    private List<Long> resolvePartnerIdsByMobile(String mobile) {
        if (StrUtil.isBlank(mobile)) {
            return null;
        }
        PartnerDO partner = partnerMapper.selectByMobile(mobile.trim());
        return partner == null ? Collections.emptyList() : List.of(partner.getId());
    }

    private HelpRespVO convert(PartnerHelpDO help, PartnerDO partner) {
        HelpRespVO resp = BeanUtils.toBean(help, HelpRespVO.class);
        if (partner != null) {
            resp.setNickname(partner.getNickname());
            resp.setMobile(partner.getMobile());
        }
        resp.setStatusName(HelpStatusEnum.getName(help.getStatus()));
        resp.setMaterialUrls(parseMaterialUrls(help.getMaterials()));
        return resp;
    }

    private List<String> parseMaterialUrls(String materials) {
        if (StrUtil.isBlank(materials)) {
            return Collections.emptyList();
        }
        try {
            JSONArray array = JSONUtil.parseArray(materials);
            List<String> urls = new ArrayList<>();
            for (Object item : array) {
                if (item instanceof Map<?, ?> mapItem) {
                    appendMaterialUrl(urls, mapItem);
                } else if (item instanceof JSONObject jsonObject) {
                    appendMaterialUrl(urls, jsonObject);
                }
            }
            return urls;
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    private void appendMaterialUrl(List<String> urls, Map<?, ?> mapItem) {
        Object url = mapItem.get("url");
        if (url == null) {
            url = mapItem.get("datas");
        }
        if (url != null && StrUtil.isNotBlank(String.valueOf(url))) {
            urls.add(String.valueOf(url));
        }
    }

    private void appendMaterialUrl(List<String> urls, JSONObject jsonObject) {
        String url = jsonObject.getStr("url");
        if (StrUtil.isBlank(url)) {
            url = jsonObject.getStr("datas");
        }
        if (StrUtil.isNotBlank(url)) {
            urls.add(url);
        }
    }

    private String buildReason(String reason, AppHelpSubmitReqVO reqVO) {
        StringBuilder sb = new StringBuilder(StrUtil.blankToDefault(reason, ""));
        if (reqVO.getProvinceId() != null) {
            if (!sb.isEmpty()) {
                sb.append('；');
            }
            sb.append("省份编号:").append(reqVO.getProvinceId());
        }
        if (StrUtil.isNotBlank(reqVO.getCity())) {
            if (!sb.isEmpty()) {
                sb.append('；');
            }
            sb.append("地区:").append(reqVO.getCity());
        }
        return sb.toString();
    }

    private String buildMaterials(AppHelpSubmitReqVO reqVO) {
        if (reqVO.getAttachments() == null || reqVO.getAttachments().isEmpty()) {
            return null;
        }
        return JSONUtil.toJsonStr(reqVO.getAttachments());
    }

}

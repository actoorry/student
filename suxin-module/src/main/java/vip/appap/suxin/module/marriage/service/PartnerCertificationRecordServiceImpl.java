package vip.appap.suxin.module.marriage.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.json.JsonUtils;
import vip.appap.suxin.module.marriage.dal.dataobject.PartnerMarriageProfileDO;
import vip.appap.suxin.module.marriage.dal.mysql.PartnerMarriageProfileMapper;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerCertificationRecordPageReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerCertificationRecordWithPartnerRespVO;
import vip.appap.suxin.module.partner.controller.app.vo.*;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerCertificationRecordDO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import vip.appap.suxin.module.partner.dal.mysql.PartnerCertificationRecordMapper;
import vip.appap.suxin.module.partner.dal.mysql.PartnerMapper;
import vip.appap.suxin.module.partner.service.ChinaDataPayCertificationClient;
import vip.appap.suxin.module.partner.service.PartnerCertificationCallResult;
import vip.appap.suxin.module.partner.service.PartnerCertificationRecordService;
import vip.appap.suxin.module.partner.service.PartnerIdCardValidator;
import vip.appap.suxin.module.system.enums.SexEnum;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.partner.enums.ErrorCodeConstants.PARTNER_NOT_EXISTS;
import static vip.appap.suxin.module.partner.enums.PartnerCertificationConstants.*;

@Service
@Validated
@Slf4j
public class PartnerCertificationRecordServiceImpl implements PartnerCertificationRecordService {

    @Resource
    private PartnerMapper partnerMapper;

    @Resource
    private PartnerCertificationRecordMapper partnerCertificationRecordMapper;

    @Resource
    private ChinaDataPayCertificationClient chinaDataPayCertificationClient;

    @Resource
    private PartnerMarriageProfileMapper partnerMarriageProfileMapper;

    @Value("${suxin.rate-limit.marriage.month-count:1}")
    private Integer marriageMonthCount;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PartnerNameCheckRespVO nameCheck(Long partnerId, PartnerNameCheckReqVO reqVO) {
        PartnerDO partner = getPartner(partnerId);
        if (StrUtil.isBlank(partner.getMobile())) {
            return buildNameCheckResp(false, "Please bind mobile first", false);
        }

        String name = StrUtil.trim(reqVO.getName());
        String idCard = normalizeIdCard(reqVO.getIdCard());
        String mobile = StrUtil.trim(partner.getMobile());
        if (!isValidRealName(name)) {
            return buildNameCheckResp(false, "真实姓名必须为 1-20 个中文字符", false);
        }
        String idCardError = PartnerIdCardValidator.validate(idCard);
        if (StrUtil.isNotBlank(idCardError)) {
            return buildNameCheckResp(false, idCardError, false);
        }

        String requestKey = buildRequestKey(CERT_TYPE_REAL_NAME, partnerId, name, idCard, mobile);
        PartnerCertificationRecordDO historyRecord = partnerCertificationRecordMapper.selectLatestByRequestKeyAndStates(
                requestKey, List.of(REAL_NAME_STATE_PASS, REAL_NAME_STATE_NOT_PASS));
        if (historyRecord != null) {
            if (REAL_NAME_STATE_PASS.equals(historyRecord.getState())) {
                syncRealNamePassResult(partner, name, idCard);
            }
            return buildNameCheckResp(REAL_NAME_STATE_PASS.equals(historyRecord.getState()),
                    buildRealNameReason(historyRecord.getState(), historyRecord.getMessage()), true);
        }

        PartnerCertificationCallResult callResult;
        try {
            callResult = chinaDataPayCertificationClient.realNameCheck(name, idCard, mobile);
        } catch (Exception e) {
            log.error("[nameCheck][partnerId({}) real-name provider failed: {}]", partnerId,
                    e.getClass().getSimpleName());
            String errorMessage = "实名认证服务暂不可用";
            callResult = PartnerCertificationCallResult.builder()
                    .code("CLIENT_EXCEPTION")
                    .message(errorMessage)
                    .state(REAL_NAME_STATE_EXCEPTION)
                    .responseBody(JsonUtils.toJsonString(Map.of("message", errorMessage)))
                    .build();
        }

        PartnerCertificationRecordDO record = PartnerCertificationRecordDO.builder()
                .partnerId(partnerId)
                .certType(CERT_TYPE_REAL_NAME)
                .requestKey(requestKey)
                .providerCode(PROVIDER_CHINA_DATA_PAY)
                .providerSeqNo(callResult.getProviderSeqNo())
                .code(callResult.getCode())
                .message(truncate(callResult.getMessage(), 255))
                .state(StrUtil.blankToDefault(callResult.getState(), REAL_NAME_STATE_EXCEPTION))
                .charged(chinaDataPayCertificationClient.isRealNameCharged(callResult))
                .requestParams(buildRealNameRequestParams(name, idCard, mobile))
                .responseBody(callResult.getResponseBody())
                .build();
        partnerCertificationRecordMapper.insert(record);

        if (REAL_NAME_STATE_PASS.equals(record.getState())) {
            syncRealNamePassResult(partner, name, idCard);
        }
        return buildNameCheckResp(REAL_NAME_STATE_PASS.equals(record.getState()),
                buildRealNameReason(record.getState(), record.getMessage()), false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PartnerMarriageCheckRespVO marriageCheck(Long partnerId, PartnerMarriageCheckReqVO reqVO) {
        PartnerDO partner = getPartner(partnerId);

        // 从实名认证信息中获取姓名和身份证号
        String name = StrUtil.trim(partner.getName());
        String idCard = normalizeIdCard(partner.getIdCard());

        // 检查是否已完成实名认证
        if (StrUtil.isBlank(name) || StrUtil.isBlank(idCard)) {
            return buildMarriageCheckResp(false, null, null, "请先完成实名认证", false);
        }

        String idCardError = PartnerIdCardValidator.validate(idCard);
        if (StrUtil.isNotBlank(idCardError)) {
            return buildMarriageCheckResp(false, null, null, idCardError, false);
        }
        if (!isRealNameVerified(partnerId)) {
            return buildMarriageCheckResp(false, null, null, "请先完成实名认证", false);
        }

        String requestKey = buildRequestKey(CERT_TYPE_REAL_MARRIAGE, partnerId, name, idCard, null);
        PartnerCertificationRecordDO historyRecord = partnerCertificationRecordMapper.selectLatestByRequestKeyAndStates(
                requestKey, List.of(MARRIAGE_STATE_MARRIED, MARRIAGE_STATE_UNMARRIED, MARRIAGE_STATE_DIVORCED));
        if (historyRecord != null) {
            recordMarriageHistoryReuse(partnerId, requestKey, name, idCard, historyRecord);
            syncMarriageProfileStatus(partnerId, historyRecord.getState());
            return buildMarriageCheckResp(true, historyRecord.getState(), buildMarriageStateLabel(historyRecord.getState()),
                    buildMarriageReason(historyRecord.getState()), true);
        }

        PartnerCertificationCallResult callResult;
        try {
            callResult = chinaDataPayCertificationClient.marriageCheck(name, idCard);
        } catch (Exception e) {
            log.error("[marriageCheck][partnerId({}) marriage provider failed: {}]", partnerId,
                    e.getClass().getSimpleName());
            String errorMessage = "婚姻认证服务暂不可用";
            callResult = PartnerCertificationCallResult.builder()
                    .code("CLIENT_EXCEPTION")
                    .message(errorMessage)
                    .responseBody(JsonUtils.toJsonString(Map.of("message", errorMessage)))
                    .build();
        }

        String state = truncate(callResult.getState(), 32);
        PartnerCertificationRecordDO record = PartnerCertificationRecordDO.builder()
                .partnerId(partnerId)
                .certType(CERT_TYPE_REAL_MARRIAGE)
                .requestKey(requestKey)
                .providerCode(PROVIDER_CHINA_DATA_PAY)
                .providerSeqNo(callResult.getProviderSeqNo())
                .code(callResult.getCode())
                .message(truncate(callResult.getMessage(), 255))
                .state(state)
                .charged(chinaDataPayCertificationClient.isMarriageCharged(callResult))
                .requestParams(buildMarriageRequestParams(name, idCard))
                .responseBody(callResult.getResponseBody())
                .build();
        partnerCertificationRecordMapper.insert(record);

        if (isMarriageSuccessState(state)) {
            syncMarriageProfileStatus(partnerId, state);
            return buildMarriageCheckResp(true, state, buildMarriageStateLabel(state), buildMarriageReason(state), false);
        }
        return buildMarriageCheckResp(false, state, null,
                StrUtil.blankToDefault(record.getMessage(), "婚姻认证结果不可用"), false);
    }

    @Override
    public PartnerMarriageCheckStatusRespVO getMarriageCheckStatus(Long partnerId) {
        getPartner(partnerId);
        PartnerCertificationRecordDO latestRecord = partnerCertificationRecordMapper
                .selectLatestByPartnerIdAndCertType(partnerId, CERT_TYPE_REAL_MARRIAGE);

        LocalDate currentDate = LocalDate.now();
        LocalDate nextAvailableDate = currentDate.withDayOfMonth(1).plusMonths(1);
        LocalDateTime monthStart = currentDate.withDayOfMonth(1).atStartOfDay();
        long currentMonthCount = partnerCertificationRecordMapper.selectCountByPartnerIdAndCertTypeAndCreateTime(
                partnerId, CERT_TYPE_REAL_MARRIAGE, monthStart, nextAvailableDate.atStartOfDay());
        int maxCount = marriageMonthCount != null && marriageMonthCount > 0 ? marriageMonthCount : 1;
        boolean limitReached = currentMonthCount >= maxCount;

        PartnerMarriageCheckStatusRespVO respVO = new PartnerMarriageCheckStatusRespVO();
        respVO.setChecked(latestRecord != null);
        respVO.setSuccess(latestRecord != null && isMarriageSuccessState(latestRecord.getState()));
        if (latestRecord != null) {
            respVO.setState(latestRecord.getState());
            respVO.setStateLabel(buildMarriageStateLabel(latestRecord.getState()));
            respVO.setReason(isMarriageSuccessState(latestRecord.getState())
                    ? buildMarriageReason(latestRecord.getState())
                    : StrUtil.blankToDefault(latestRecord.getMessage(), "婚姻认证结果不可用"));
        }
        respVO.setLimitReached(limitReached);
        if (limitReached) {
            respVO.setNextAvailableDate(nextAvailableDate);
            respVO.setLimitMessage("本月婚姻认证次数已达上限，请于"
                    + nextAvailableDate.format(DateTimeFormatter.ofPattern("yyyy年M月d日")) + "后再试");
        }
        return respVO;
    }

    @Override
    public PartnerCertificationRecordDO getPartnerCertificationRecord(Long id) {
        return partnerCertificationRecordMapper.selectById(id);
    }

    @Override
    public PageResult<PartnerCertificationRecordDO> getPartnerCertificationRecordPage(PartnerCertificationRecordPageReqVO pageReqVO) {
        return partnerCertificationRecordMapper.selectPage(pageReqVO);
    }

    @Override
    public PageResult<PartnerCertificationRecordWithPartnerRespVO> getPartnerCertificationRecordWithPartnerPage(PartnerCertificationRecordPageReqVO pageReqVO) {
        Page<?> page = new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize());
        IPage<PartnerCertificationRecordWithPartnerRespVO> pageResult = partnerCertificationRecordMapper.selectPageWithPartner(page, pageReqVO);
        // 填充 stateLabel
        pageResult.getRecords().forEach(vo -> vo.setStateLabel(buildStateLabel(vo.getCertType(), vo.getState())));
        return new PageResult<>(pageResult.getRecords(), pageResult.getTotal());
    }

    @Override
    public Integer getRealNameVerifiedStatus(Long partnerId) {
        PartnerCertificationRecordDO record = partnerCertificationRecordMapper.selectLatestByPartnerIdAndCertTypeAndStates(
                partnerId, CERT_TYPE_REAL_NAME, List.of(REAL_NAME_STATE_PASS));
        return record == null ? 0 : 1;
    }

    @Override
    public Integer getMarriageVerifiedStatus(Long partnerId) {
        PartnerCertificationRecordDO record = partnerCertificationRecordMapper.selectLatestByPartnerIdAndCertTypeAndStates(
                partnerId, CERT_TYPE_REAL_MARRIAGE, List.of(MARRIAGE_STATE_MARRIED, MARRIAGE_STATE_UNMARRIED, MARRIAGE_STATE_DIVORCED));
        return record == null ? 0 : 1;
    }

    @Override
    public Set<Long> getMarriageVerifiedPartnerIds(Collection<Long> partnerIds) {
        if (CollUtil.isEmpty(partnerIds)) {
            return Collections.emptySet();
        }
        return partnerCertificationRecordMapper.selectMarriageVerifiedPartnerIds(partnerIds, CERT_TYPE_REAL_MARRIAGE,
                List.of(MARRIAGE_STATE_MARRIED, MARRIAGE_STATE_UNMARRIED, MARRIAGE_STATE_DIVORCED));
    }

    private PartnerDO getPartner(Long partnerId) {
        PartnerDO partner = partnerMapper.selectById(partnerId);
        if (partner == null) {
            throw exception(PARTNER_NOT_EXISTS);
        }
        return partner;
    }

    private String buildRequestKey(String certType, Long partnerId, String name, String idCard, String mobile) {
        return DigestUtil.sha256Hex(certType + "|" + partnerId + "|" + name + "|" + idCard + "|" + StrUtil.blankToDefault(mobile, ""));
    }

    private String buildRealNameRequestParams(String name, String idCard, String mobile) {
        return JsonUtils.toJsonString(Map.of(
                "name", name,
                "idcard", idCard,
                "mobile", mobile
        ));
    }

    private String buildMarriageRequestParams(String name, String idCard) {
        return JsonUtils.toJsonString(Map.of(
                "name", name,
                "certNum", idCard
        ));
    }

    private void recordMarriageHistoryReuse(Long partnerId, String requestKey, String name, String idCard,
                                            PartnerCertificationRecordDO historyRecord) {
        partnerCertificationRecordMapper.insert(PartnerCertificationRecordDO.builder()
                .partnerId(partnerId)
                .certType(CERT_TYPE_REAL_MARRIAGE)
                .requestKey(requestKey)
                .providerCode(historyRecord.getProviderCode())
                .code("REUSED")
                .message(buildMarriageReason(historyRecord.getState()))
                .state(historyRecord.getState())
                .charged(false)
                .requestParams(buildMarriageRequestParams(name, idCard))
                .responseBody(historyRecord.getResponseBody())
                .build());
    }

    private void syncRealNamePassResult(PartnerDO partner, String name, String idCard) {
        updatePartnerRealNameInfo(partner, name, idCard);
        updatePartnerMarriageRealVerified(partner.getId());
    }

    private void updatePartnerRealNameInfo(PartnerDO partner, String name, String idCard) {
        PartnerDO updateObj = PartnerDO.builder()
                .id(partner.getId())
                .name(name)
                .idCard(idCard)
                .sex(parseSex(idCard))
                .birthday(parseBirthday(idCard))
                .build();
        partnerMapper.updateById(updateObj);
    }

    private void updatePartnerMarriageRealVerified(Long partnerId) {
        PartnerMarriageProfileDO profile = partnerMarriageProfileMapper.selectById(partnerId);
        if (profile == null) {
            partnerMarriageProfileMapper.insert(PartnerMarriageProfileDO.builder()
                    .id(partnerId)
                    .realVerified(1)
                    .build());
            return;
        }
        if (!Integer.valueOf(1).equals(profile.getRealVerified())) {
            partnerMarriageProfileMapper.updateById(PartnerMarriageProfileDO.builder()
                    .id(partnerId)
                    .realVerified(1)
                    .build());
        }
    }

    private boolean isRealNameVerified(Long partnerId) {
        PartnerMarriageProfileDO profile = partnerMarriageProfileMapper.selectById(partnerId);
        return profile != null && Integer.valueOf(1).equals(profile.getRealVerified());
    }

    private void syncMarriageProfileStatus(Long partnerId, String state) {
        Integer maritalStatus = mapMaritalStatus(state);
        if (maritalStatus == null) {
            return;
        }
        PartnerMarriageProfileDO profile = partnerMarriageProfileMapper.selectById(partnerId);
        if (profile == null) {
            partnerMarriageProfileMapper.insert(PartnerMarriageProfileDO.builder()
                    .id(partnerId)
                    .maritalStatus(maritalStatus)
                    .build());
            return;
        }
        if (!maritalStatus.equals(profile.getMaritalStatus())) {
            partnerMarriageProfileMapper.updateById(PartnerMarriageProfileDO.builder()
                    .id(partnerId)
                    .maritalStatus(maritalStatus)
                    .build());
        }
    }

    private Integer mapMaritalStatus(String state) {
        if (MARRIAGE_STATE_MARRIED.equals(state)) {
            return 1;
        }
        if (MARRIAGE_STATE_UNMARRIED.equals(state)) {
            return 2;
        }
        if (MARRIAGE_STATE_DIVORCED.equals(state)) {
            return 3;
        }
        return null;
    }

    private Integer parseSex(String idCard) {
        if (StrUtil.length(idCard) != 18) {
            return null;
        }
        char genderChar = idCard.charAt(16);
        return (genderChar - '0') % 2 == 1 ? SexEnum.MALE.getSex() : SexEnum.FEMALE.getSex();
    }

    private LocalDateTime parseBirthday(String idCard) {
        if (StrUtil.length(idCard) != 18) {
            return null;
        }
        try {
            LocalDate birthday = LocalDate.parse(idCard.substring(6, 14), DateTimeFormatter.ofPattern("yyyyMMdd"));
            return birthday.atStartOfDay();
        } catch (Exception ignored) {
            return null;
        }
    }

    private PartnerNameCheckRespVO buildNameCheckResp(boolean passed, String reason, boolean reused) {
        PartnerNameCheckRespVO respVO = new PartnerNameCheckRespVO();
        respVO.setPassed(passed);
        respVO.setReason(reason);
        respVO.setReused(reused);
        return respVO;
    }

    private PartnerMarriageCheckRespVO buildMarriageCheckResp(boolean success, String state, String stateLabel,
                                                              String reason, boolean reused) {
        PartnerMarriageCheckRespVO respVO = new PartnerMarriageCheckRespVO();
        respVO.setSuccess(success);
        respVO.setState(state);
        respVO.setStateLabel(stateLabel);
        respVO.setReason(reason);
        respVO.setReused(reused);
        return respVO;
    }

    private String buildStateLabel(String certType, String state) {
        if (CERT_TYPE_REAL_NAME.equals(certType)) {
            return buildRealNameStateLabel(state);
        }
        if (CERT_TYPE_REAL_MARRIAGE.equals(certType)) {
            return buildMarriageStateLabel(state);
        }
        return state;
    }

    private String buildRealNameStateLabel(String state) {
        if (REAL_NAME_STATE_PASS.equals(state)) {
            return "通过";
        }
        if (REAL_NAME_STATE_NOT_PASS.equals(state)) {
            return "不通过";
        }
        if (REAL_NAME_STATE_EXCEPTION.equals(state)) {
            return "异常";
        }
        return state;
    }

    private String buildRealNameReason(String state, String message) {
        if (REAL_NAME_STATE_PASS.equals(state)) {
            return "Verification passed";
        }
        if (REAL_NAME_STATE_NOT_PASS.equals(state)) {
            return "Verification failed";
        }
        return StrUtil.blankToDefault(message, "Verification result unavailable");
    }

    private boolean isMarriageSuccessState(String state) {
        return MARRIAGE_STATE_MARRIED.equals(state)
                || MARRIAGE_STATE_UNMARRIED.equals(state)
                || MARRIAGE_STATE_DIVORCED.equals(state);
    }

    private String buildMarriageStateLabel(String state) {
        if (MARRIAGE_STATE_MARRIED.equals(state)) {
            return "结婚";
        }
        if (MARRIAGE_STATE_UNMARRIED.equals(state)) {
            return "未婚";
        }
        if (MARRIAGE_STATE_DIVORCED.equals(state)) {
            return "离婚";
        }
        return "";
    }

    private String buildMarriageReason(String state) {
        String stateLabel = buildMarriageStateLabel(state);
        return StrUtil.isBlank(stateLabel) ? "婚姻认证完成" : "婚姻认证结果：" + stateLabel;
    }

    private String normalizeIdCard(String idCard) {
        return StrUtil.blankToDefault(StrUtil.trim(idCard), "").toUpperCase();
    }

    private boolean isValidRealName(String name) {
        return StrUtil.isNotBlank(name) && name.codePointCount(0, name.length()) <= 20
                && name.matches("^[\\u4e00-\\u9fa5]+$");
    }

    private String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }

}

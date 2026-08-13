package vip.appap.suxin.module.sales.controller.admin.vo;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SalesConfigSaveReqVOTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @Test
    void validate_brokerageDisabled_allowsEmptyBrokerageSettings() {
        SalesConfigSaveReqVO reqVO = validConfig();
        reqVO.setBrokerageEnabled(false);
        reqVO.setBrokerageEnabledCondition(null);
        reqVO.setBrokerageBindMode(null);
        reqVO.setBrokerageFirstPercent(null);
        reqVO.setBrokerageSecondPercent(null);
        reqVO.setBrokerageWithdrawMinPrice(null);
        reqVO.setBrokerageWithdrawFeePercent(null);
        reqVO.setBrokerageFrozenDays(null);
        reqVO.setBrokerageWithdrawTypes(null);

        Set<ConstraintViolation<SalesConfigSaveReqVO>> violations = validator.validate(reqVO);

        assertTrue(violations.isEmpty(), () -> "关闭分销不应要求填写分销配置: " + violations);
    }

    private static SalesConfigSaveReqVO validConfig() {
        SalesConfigSaveReqVO reqVO = new SalesConfigSaveReqVO();
        reqVO.setAfterSaleRefundReasons(List.of("退款"));
        reqVO.setAfterSaleReturnReasons(List.of("退货"));
        reqVO.setDeliveryExpressFreeEnabled(false);
        reqVO.setDeliveryExpressFreePrice(0);
        reqVO.setDeliveryPickUpEnabled(false);
        reqVO.setBrokerageEnabled(false);
        return reqVO;
    }
}

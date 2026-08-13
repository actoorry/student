package vip.appap.suxin.module.partner.controller.app.vo;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PartnerMarriageCheckReqVOTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    void validate_whenIdCardBlank_allowsServerSideRealNameInfo() {
        PartnerMarriageCheckReqVO reqVO = new PartnerMarriageCheckReqVO();
        reqVO.setName("\u5f20\u4e09");
        reqVO.setIdCard("");

        Set<ConstraintViolation<PartnerMarriageCheckReqVO>> violations = validator.validate(reqVO);

        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_whenNameBlank_rejectsRequest() {
        PartnerMarriageCheckReqVO reqVO = new PartnerMarriageCheckReqVO();

        Set<ConstraintViolation<PartnerMarriageCheckReqVO>> violations = validator.validate(reqVO);

        assertEquals(1, violations.stream()
                .filter(violation -> "name".equals(violation.getPropertyPath().toString()))
                .count());
    }

}

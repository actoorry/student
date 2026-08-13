package vip.appap.suxin.module.marriage.convert;

import vip.appap.suxin.module.marriage.controller.app.vo.AppPartnerMarriageProfileRespVO;
import vip.appap.suxin.module.marriage.dal.dataobject.PartnerMarriageProfileDO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

@Mapper
public interface PartnerMarriageProfileConvert {

    PartnerMarriageProfileConvert INSTANCE = Mappers.getMapper(PartnerMarriageProfileConvert.class);

    @Mappings({
            @Mapping(source = "bean.id", target = "id"),
            @Mapping(source = "partner.nickname", target = "name"),
            @Mapping(source = "partner.sex", target = "sex"),
            @Mapping(source = "bean.heightCm", target = "height"),
            @Mapping(source = "bean.weightKg", target = "weight"),
            @Mapping(source = "bean.jobTitle", target = "job"),
            @Mapping(source = "partner.avatar", target = "avatarImage"),
            @Mapping(source = "bean.bio", target = "bio"),
            @Mapping(source = "bean.realVerified", target = "realVerified"),
            @Mapping(target = "age", expression = "java(calculateAge(partner.getBirthday()))"),
            @Mapping(target = "city", ignore = true),
            @Mapping(target = "education", ignore = true),
            @Mapping(target = "income", ignore = true),
            @Mapping(target = "maritalStatus", ignore = true),
            @Mapping(target = "houseStatus", ignore = true),
            @Mapping(target = "carStatus", ignore = true),
            @Mapping(target = "maskedIdCard", ignore = true),
            @Mapping(target = "verifiedLabel", ignore = true),
            @Mapping(target = "onlineLabel", ignore = true),
            @Mapping(target = "viewerLabel", ignore = true),
            @Mapping(target = "tags", ignore = true),
            @Mapping(target = "interestTags", ignore = true),
            @Mapping(target = "albumImages", ignore = true),
            @Mapping(target = "mainImage", ignore = true),
            @Mapping(target = "latestMoment", ignore = true),
            @Mapping(target = "memberActive", ignore = true)
    })
    AppPartnerMarriageProfileRespVO convert(PartnerMarriageProfileDO bean, PartnerDO partner);

    default Integer calculateAge(LocalDateTime birthday) {
        if (birthday == null) {
            return null;
        }
        return Period.between(birthday.toLocalDate(), LocalDate.now()).getYears();
    }

}

package vip.appap.suxin.module.hr.integration.zhiye;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 读取智业 SOAP XML 模板并组装请求报文（对齐旧 OA FtXmlUtil）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ZhiyeXmlBuilder {

    private static final String TEMPLATE_BODY = "zhiye/providerInfoRegister.xml";
    private static final String TEMPLATE_HEAD = "zhiye/head.xml";
    private static final String TEMPLATE_SOAP = "zhiye/webserviceParams.xml";

    private final ZhiyeHisProperties properties;

    public String buildRequest(String methodName, ZhiyeSyncContext context) {
        String body = readTemplate(TEMPLATE_BODY);
        if (Boolean.TRUE.equals(properties.getOaCompatible())) {
            body = replaceOaPlaceholders(body, context);
        } else {
            body = replaceExtendedPlaceholders(body, context);
        }

        String head = readTemplate(TEMPLATE_HEAD);
        head = head.replace("@serverName@", methodName);
        head = head.replace("@certificate@", StrUtil.nullToEmpty(properties.getCertificate()));

        String soap = readTemplate(TEMPLATE_SOAP);
        soap = soap.replace("@msgHeader@", collapseWhitespace(optionalStripProlog(head)));
        soap = soap.replace("@msgBody@", collapseWhitespace(optionalStripProlog(body)));
        return soap;
    }

    /**
     * 旧 OA {@code FTapiServiceImpl.providerInfoRegisterIn} 仅替换的字段
     */
    private String replaceOaPlaceholders(String content, ZhiyeSyncContext ctx) {
        return content
                .replace("@name@", safe(ctx.getName()))
                .replace("@healthcareProvider@", safe(ctx.getHealthcareProvider()))
                .replace("@idCard@", safe(ctx.getIdCard()))
                .replace("@genderCode@", safe(ctx.getGenderCode()))
                .replace("@genderDisplayName@", safe(ctx.getGenderDisplayName()))
                .replace("@birthDate@", safe(ctx.getBirthDate()))
                .replace("@technologyCodeSystemName@", safe(ctx.getTechnologyCodeSystemName()))
                .replace("@technologyDisplayName@", safe(ctx.getTechnologyDisplayName()))
                .replace("@departmentName@", safe(ctx.getDepartmentName()));
    }

    private String replaceExtendedPlaceholders(String content, ZhiyeSyncContext ctx) {
        return content
                .replace("@messageId@", safe(ctx.getMessageId()))
                .replace("@messageCreateTime@", safe(ctx.getMessageCreateTime()))
                .replace("@healthcareProvider@", safe(ctx.getHealthcareProvider()))
                .replace("@technologyCodeSystemName@", safe(ctx.getTechnologyCodeSystemName()))
                .replace("@technologyDisplayName@", safe(ctx.getTechnologyDisplayName()))
                .replace("@idCard@", safe(ctx.getIdCard()))
                .replace("@name@", safe(ctx.getName()))
                .replace("@nameCode@", safe(ctx.getNameCode()))
                .replace("@genderCode@", safe(ctx.getGenderCode()))
                .replace("@genderDisplayName@", safe(ctx.getGenderDisplayName()))
                .replace("@birthDate@", safe(ctx.getBirthDate()))
                .replace("@originCode@", safe(ctx.getOriginCode()))
                .replace("@branchCode@", safe(ctx.getBranchCode()))
                .replace("@departmentCode@", safe(ctx.getDepartmentCode()))
                .replace("@departmentName@", safe(ctx.getDepartmentName()))
                .replace("@birthPlace@", safe(ctx.getBirthPlace()))
                .replace("@applyId@", safe(ctx.getApplyId()))
                .replace("@applyName@", safe(ctx.getApplyName()));
    }

    private String optionalStripProlog(String text) {
        if (!Boolean.TRUE.equals(properties.getStripXmlProlog()) || StrUtil.isBlank(text)) {
            return text;
        }
        return text.replaceFirst("^\\s*<\\?xml[^?]*\\?>\\s*", "").trim();
    }

    private static String safe(String value) {
        return StrUtil.nullToEmpty(value);
    }

    private static String collapseWhitespace(String text) {
        return text.replaceAll("\\s+", " ");
    }

    private String readTemplate(String classpathPath) {
        try {
            ClassPathResource resource = new ClassPathResource(classpathPath);
            byte[] bytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("[readTemplate][读取模板失败 path={}]", classpathPath, e);
            throw new IllegalStateException("智业 XML 模板读取失败: " + classpathPath);
        }
    }

}

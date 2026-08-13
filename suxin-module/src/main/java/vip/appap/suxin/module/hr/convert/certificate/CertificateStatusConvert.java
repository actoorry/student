package vip.appap.suxin.module.hr.convert.certificate;

import cn.idev.excel.converters.Converter;
import cn.idev.excel.enums.CellDataTypeEnum;
import cn.idev.excel.metadata.GlobalConfiguration;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.metadata.property.ExcelContentProperty;

import java.util.Map;

/**
 * 证书状态 Excel 转换器
 *
 * 证书 status 由 SQL 动态计算（非字典），导出时把英文枚举转为中文，
 * 便于督查台账 Excel 直接交付卫健委。仅在 Excel 导出时生效，
 * 不影响 JSON 响应中的英文 status（前端 el-tag 依赖英文值做样式判断）。
 *
 * @author admin
 */
public class CertificateStatusConvert implements Converter<Object> {

    private static final Map<String, String> LABELS = Map.of(
            "valid", "有效",
            "expiring", "30天内到期",
            "expiring_60", "60天内到期",
            "expiring_90", "90天内到期",
            "expired", "已过期",
            "assessment_overdue", "考核逾期",
            "assessment_due_60", "考核将到期"
    );

    @Override
    public Class<?> supportJavaTypeKey() {
        return String.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public WriteCellData<String> convertToExcelData(Object object, ExcelContentProperty contentProperty,
                                                    GlobalConfiguration globalConfiguration) {
        if (object == null) {
            return new WriteCellData<>("");
        }
        String value = String.valueOf(object);
        return new WriteCellData<>(LABELS.getOrDefault(value, value));
    }

}

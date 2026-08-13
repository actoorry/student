package vip.appap.suxin.module.hr.convert.appointment;

import cn.idev.excel.converters.Converter;
import cn.idev.excel.enums.CellDataTypeEnum;
import cn.idev.excel.metadata.GlobalConfiguration;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.metadata.property.ExcelContentProperty;

import java.util.Map;

/**
 * 聘任状态 Excel 转换器（JSON 仍用英文枚举）
 */
public class AppointmentStatusConvert implements Converter<Object> {

    private static final Map<String, String> LABELS = Map.of(
            "active", "在聘",
            "expired", "已到期",
            "renewed", "已续聘",
            "terminated", "已解聘"
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
        return new WriteCellData<>(LABELS.getOrDefault(String.valueOf(object), String.valueOf(object)));
    }

}

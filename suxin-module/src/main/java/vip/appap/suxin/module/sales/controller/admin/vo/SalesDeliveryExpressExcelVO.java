package vip.appap.suxin.module.sales.controller.admin.vo;

import vip.appap.suxin.framework.excel.core.annotations.DictFormat;
import vip.appap.suxin.framework.excel.core.convert.DictConvert;
import vip.appap.suxin.module.system.enums.DictTypeConstants;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 快递公司 Excel VO
 */
@Data
public class SalesDeliveryExpressExcelVO {

    @ExcelProperty("编号")
    private Long id;

    @ExcelProperty("快递公司编码")
    private String code;

    @ExcelProperty("快递公司名称")
    private String name;

    @ExcelProperty("快递公司 logo")
    private String logo;

    @ExcelProperty("排序")
    private Integer sort;

    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.COMMON_STATUS)
    private Integer status;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}

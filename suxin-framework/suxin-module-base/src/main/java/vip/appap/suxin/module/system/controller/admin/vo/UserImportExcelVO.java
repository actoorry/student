package vip.appap.suxin.module.system.controller.admin.vo;

import cn.idev.excel.annotation.ExcelProperty;
import vip.appap.suxin.framework.excel.core.annotations.DictFormat;
import vip.appap.suxin.framework.excel.core.convert.DictConvert;
import vip.appap.suxin.module.system.enums.DictTypeConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserImportExcelVO {

    @ExcelProperty("登录名称")
    private String username;

    @ExcelProperty("部门编号")
    private Long deptId;

    @ExcelProperty(value = "账号状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.COMMON_STATUS)
    private Integer status;

}

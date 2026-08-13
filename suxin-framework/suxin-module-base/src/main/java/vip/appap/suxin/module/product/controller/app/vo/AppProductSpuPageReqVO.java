package vip.appap.suxin.module.product.controller.app.vo;

import cn.hutool.core.util.StrUtil;
import vip.appap.suxin.framework.common.pojo.PageParam;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Schema(description = "用户 App - 商品 SPU 分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppProductSpuPageReqVO extends PageParam {

    public static final String SORT_FIELD_PRICE = "price";
    public static final String SORT_FIELD_SALES_COUNT = "salesCount";
    public static final String SORT_FIELD_CREATE_TIME = "createTime";

    @Schema(description = "商品 SPU 编号数组", example = "1,3,5")
    private List<Long> ids;

    @Schema(description = "销售分类ID", example = "1")
    private Long categorySales;

    @Schema(description = "分类编号数组", example = "1,2,3")
    private List<Long> categoryIds;

    @Schema(description = "关键字", example = "好看")
    private String keyword;

    @Schema(description = "城市/区域编号（同城特产）", example = "340100")
    private Integer cityId;

    @Schema(description = "城市/区域编号（前端别名 areaId）", example = "340100")
    private Integer areaId;

    @Schema(description = "省份编号（全国特产，展开为省内全部市级编号查询）", example = "120000")
    private Integer provinceId;

    @Schema(description = "是否仅查询全国特产（仅返回已绑定地区 cityId 的上架商品）", example = "true")
    private Boolean specialtyOnly;

    @Schema(description = "是否仅查询军创区商品（仅返回 is_military=1 且上架的商品）", example = "true")
    private Boolean isMilitary;

    @Schema(description = "排序字段", example = "price") // 参见 AppProductSpuPageReqVO.SORT_FIELD_XXX 常量
    private String sortField;

    @Schema(description = "排序方式", example = "true")
    private Boolean sortAsc;

    @AssertTrue(message = "排序字段不合法")
    @JsonIgnore
    public boolean isSortFieldValid() {
        if (StrUtil.isEmpty(sortField)) {
            return true;
        }
        return StrUtil.equalsAny(sortField, SORT_FIELD_PRICE, SORT_FIELD_SALES_COUNT, SORT_FIELD_CREATE_TIME);
    }

}

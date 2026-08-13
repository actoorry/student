package vip.appap.suxin.module.product.controller.admin.vo;

import vip.appap.suxin.framework.common.validation.InEnum;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import vip.appap.suxin.module.product.enums.SalesDeliveryTypeEnum;

import java.util.List;

@Schema(description = "管理后台 - 商品 SPU 新增/更新 Request VO")
@Data
public class ProductSpuSaveReqVO {

    @Schema(description = "商品编号", example = "1")
    private Long id;

    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "清凉小短袖")
    @NotEmpty(message = "商品名称不能为空")
    private String name;

    @Schema(description = "关键字", example = "清凉丝滑不出汗")
    private String keyword;

    @Schema(description = "商品简介",  example = "清凉小短袖简介")
    private String introduction;

    @Schema(description = "商品详情",  example = "清凉小短袖详情")
    private String description;

    @Schema(description = "销售分类ID",  example = "1")
    private Long categorySales;

    @Schema(description = "仓储分类ID", example = "2")
    private Long categoryStore;

    @Schema(description = "商品品牌编号", example = "1")
    private Long brandId;

    @Schema(description = "商品封面图", example = "https://www.appap.vip/xx.png")
    private String picUrl;

    @Schema(description = "商品轮播图", example = "[https://www.appap.vip/xx.png, https://www.appap.vip/xxx.png]")
    private List<String> sliderPicUrls;

    @Schema(description = "排序字段", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "商品排序字段不能为空")
    private Integer sort;

    // ========== ERP 控制字段 ==========

    @Schema(description = "产品类型: 1-实体产品, 2-服务产品, 3-组合产品, 4-会员产品", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "产品类型不能为空")
    private Integer type;

    @Schema(description = "是否微信小程序虚拟商品；创建后不可修改", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "请选择是否微信小程序虚拟商品")
    private Boolean isWechatMiniappVirtualGoods;

    @Schema(description = "计价单位", example = "102")
    private Long unitId;

    @Schema(description = "是否可销售", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "是否可销售不能为空")
    private Boolean isSale;

    @Schema(description = "是否可采购", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    @NotNull(message = "是否可采购不能为空")
    private Boolean isPurchase;

    @Schema(description = "是否生产可用", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    @NotNull(message = "是否生产可用管理不能为空")
    private Boolean isMes;

    @Schema(description = "是否军创区商品；缺失值按 false 处理", example = "false")
    private Boolean isMilitary;

    // ========== SKU 相关字段 =========

    @Schema(description = "规格类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "商品规格类型不能为空")
    private Boolean specType;

    // ========== 物流相关字段 =========

    @Schema(description = "配送方式数组", example = "1")
    @NotEmpty(message = "配送方式不能为空")
    @InEnum(value = SalesDeliveryTypeEnum.class, message = "配送方式不正确")
    private List<Integer> deliveryTypes;

    @Schema(description = "物流配置模板编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "111")
    private Long deliveryTemplateId;

    // ========== 营销相关字段 =========

    @Schema(description = "赠送积分", requiredMode = Schema.RequiredMode.REQUIRED, example = "111")
    private Integer giveIntegral;

    @Schema(description = "分销类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "商品分销类型不能为空")
    private Boolean subCommissionType;

    // ========== 统计相关字段 =========

    @Schema(description = "虚拟销量", example = "66")
    private Integer virtualSalesCount;

    @Schema(description = "商品销量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1999")
    private Integer salesCount;

    @Schema(description = "浏览量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1999")
    private Integer browseCount;

    // ========== SKU 相关字段 =========

    @Schema(description = "SKU 数组")
    @Valid
    private List<ProductSkuSaveReqVO> skus;

    @Schema(description = "归属城市编号（同城特产）", example = "340100")
    private Integer cityId;

}

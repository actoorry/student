package vip.appap.suxin.module.system.controller.app;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.util.servlet.ServletUtils;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.framework.ip.core.Area;
import vip.appap.suxin.framework.ip.core.enums.AreaTypeEnum;
import vip.appap.suxin.framework.ip.core.utils.AreaUtils;
import vip.appap.suxin.framework.ip.core.utils.IPUtils;
import vip.appap.suxin.module.system.controller.app.vo.AppAreaLocationRespVO;
import vip.appap.suxin.module.system.controller.app.vo.AppAreaNodeRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 地区")
@RestController
@RequestMapping("/system/area")
@Validated
public class AppAreaController {

    @GetMapping("/tree")
    @Operation(summary = "获得地区树")
    @PermitAll
    public CommonResult<List<AppAreaNodeRespVO>> getAreaTree() {
        Area area = AreaUtils.getArea(Area.ID_CHINA);
        Assert.notNull(area, "获取不到中国");
        return success(BeanUtils.toBean(area.getChildren(), AppAreaNodeRespVO.class));
    }

    @GetMapping("/get-by-ip")
    @Operation(summary = "根据 IP 解析城市（同城特产定位）")
    @Parameter(name = "ip", description = "IP，不传则取当前请求 IP")
    @PermitAll
    public CommonResult<AppAreaLocationRespVO> getAreaByIp(
            @RequestParam(value = "ip", required = false) String ip) {
        String clientIp = StrUtil.blankToDefault(ip, ServletUtils.getClientIP());
        Area area = IPUtils.getArea(clientIp);
        return success(buildCityLocation(area));
    }

    private AppAreaLocationRespVO buildCityLocation(Area area) {
        if (area == null) {
            return null;
        }
        Integer cityId = AreaUtils.getParentIdByType(area.getId(), AreaTypeEnum.CITY);
        if (cityId == null && AreaTypeEnum.CITY.getType().equals(area.getType())) {
            cityId = area.getId();
        }
        if (cityId == null) {
            return null;
        }
        Area city = AreaUtils.getArea(cityId);
        if (city == null) {
            return null;
        }
        Integer provinceId = AreaUtils.getParentIdByType(cityId, AreaTypeEnum.PROVINCE);
        Area province = provinceId != null ? AreaUtils.getArea(provinceId) : null;

        AppAreaLocationRespVO resp = new AppAreaLocationRespVO();
        resp.setId(city.getId());
        resp.setName(city.getName());
        resp.setAddress(AreaUtils.format(city.getId(), " "));
        if (province != null) {
            resp.setProvinceId(province.getId());
            resp.setProvinceName(province.getName());
        }
        return resp;
    }

}

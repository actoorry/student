package vip.appap.suxin.module.campus.controller.admin;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.campus.controller.admin.vo.TeacherPageReqVO;
import vip.appap.suxin.module.campus.controller.admin.vo.TeacherRespVO;
import vip.appap.suxin.module.campus.controller.admin.vo.TeacherSaveReqVO;
import vip.appap.suxin.module.campus.dal.dataobject.TeacherDO;
import vip.appap.suxin.module.campus.service.TeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 校园教师")
@RestController
@RequestMapping("/campus/teacher")
@Validated
public class TeacherController {

    @Resource
    private TeacherService teacherService;

    @PostMapping("/create")
    @Operation(summary = "创建教师")
    @PreAuthorize("@ss.hasPermission('campus:teacher:create')")
    public CommonResult<Long> createTeacher(@Valid @RequestBody TeacherSaveReqVO createReqVO) {
        return success(teacherService.createTeacher(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新教师")
    @PreAuthorize("@ss.hasPermission('campus:teacher:update')")
    public CommonResult<Boolean> updateTeacher(@Valid @RequestBody TeacherSaveReqVO updateReqVO) {
        teacherService.updateTeacher(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除教师")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('campus:teacher:delete')")
    public CommonResult<Boolean> deleteTeacher(@RequestParam("id") Long id) {
        teacherService.deleteTeacher(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得教师")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('campus:teacher:query')")
    public CommonResult<TeacherRespVO> getTeacher(@RequestParam("id") Long id) {
        TeacherDO teacher = teacherService.getTeacher(id);
        return success(BeanUtils.toBean(teacher, TeacherRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得教师分页")
    @PreAuthorize("@ss.hasPermission('campus:teacher:query')")
    public CommonResult<PageResult<TeacherRespVO>> getTeacherPage(@Valid TeacherPageReqVO pageReqVO) {
        PageResult<TeacherDO> pageResult = teacherService.getTeacherPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, TeacherRespVO.class));
    }

}

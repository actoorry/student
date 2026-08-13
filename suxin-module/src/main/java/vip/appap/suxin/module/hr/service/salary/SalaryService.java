package vip.appap.suxin.module.hr.service.salary;

import java.util.*;
import jakarta.validation.*;
import vip.appap.suxin.module.hr.controller.admin.salary.vo.*;
import vip.appap.suxin.framework.common.pojo.PageResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 薪资 Service 接口
 *
 * @author admin
 */
public interface SalaryService {

    /**
     * 创建薪资
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSalary(@Valid SalarySaveReqVO createReqVO);

    /**
     * 更新薪资
     *
     * @param updateReqVO 更新信息
     */
    void updateSalary(@Valid SalarySaveReqVO updateReqVO);

    /**
     * 删除薪资
     *
     * @param id 编号
     */
    void deleteSalary(Long id);

    /**
    * 批量删除薪资
    *
    * @param ids 编号
    */
    void deleteSalaryListByIds(List<Long> ids);

    /**
     * 获得薪资
     *
     * @param id 编号
     * @return 薪资
     */
    SalaryRespVO getSalary(Long id);

    /**
     * 获得薪资分页
     *
     * @param pageReqVO 分页查询
     * @return 薪资分页
     */
    PageResult<SalaryRespVO> getSalaryPage(SalaryPageReqVO pageReqVO);

    /**
     * 薪资导入预检：从文件名解析年月、校验表头、统计数据行、查同年月同工号重复
     *
     * @param file 上传的 Excel 文件
     * @return 预检结果
     */
    SalaryImportCheckRespVO checkSalaryImport(MultipartFile file) throws IOException;

    /**
     * 薪资导入：confirmOverwrite=false 且存在重复时抛 SALARY_IMPORT_DUPLICATE_NEED_CONFIRM；
     * 否则按工号关联员工档案，批量 upsert（不存在则新增，存在则覆盖更新）
     *
     * @param file            上传的 Excel 文件
     * @param confirmOverwrite 是否确认覆盖已存在的重复记录
     * @return 导入结果
     */
    SalaryImportRespVO importSalary(MultipartFile file, boolean confirmOverwrite) throws IOException;

}

package vip.appap.suxin.module.hr.service.salary;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.framework.dict.core.DictFrameworkUtils;
import vip.appap.suxin.framework.excel.core.util.ExcelUtils;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.hr.controller.admin.salary.vo.*;
import vip.appap.suxin.module.hr.dal.dataobject.employee.EmployeeDO;
import vip.appap.suxin.module.hr.dal.dataobject.salary.SalaryDO;
import vip.appap.suxin.module.hr.dal.mysql.employee.EmployeeMapper;
import vip.appap.suxin.module.hr.dal.mysql.salary.SalaryMapper;
import vip.appap.suxin.module.system.controller.admin.vo.DeptListReqVO;
import vip.appap.suxin.module.system.dal.dataobject.DeptDO;
import vip.appap.suxin.module.system.service.DeptService;
import jakarta.annotation.Resource;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.hr.enums.ErrorCodeConstants.*;

/**
 * 薪资 Service 实现类
 *
 * 说明：薪资是纯财务数据，只写入 hr_salary 表，不双写 partner 表。
 * 查询时通过 LEFT JOIN partner 获取员工姓名。
 *
 * @author admin
 */
@Service
@Validated
public class SalaryServiceImpl implements SalaryService {

    /**
     * 文件名年月正则：匹配「2025年12月」「2025年12月份」等
     */
    private static final Pattern YEAR_MONTH_PATTERN = Pattern.compile("(\\d{4})年(\\d{1,2})月");

    /** 人员类别字典（与前端 HR_PERSONNEL_CATEGORY 一致） */
    private static final String DICT_TYPE_PERSONNEL_CATEGORY = "hr_personnel_category";

    /**
     * 35 个薪酬列的表头 → SalaryDO setter 映射（顺序无关，按表头名称匹配）
     */
    private static final Map<String, BiConsumer<SalaryDO, BigDecimal>> SALARY_FIELD_SETTERS = new LinkedHashMap<>();

    /**
     * 必需表头：人员编号 + 35 个薪酬列（姓名/部门/人员类别可选，缺失时回退员工档案）
     */
    private static final List<String> REQUIRED_HEADERS = new ArrayList<>();

    static {
        SALARY_FIELD_SETTERS.put("岗位工资", SalaryDO::setBasicSalary);
        SALARY_FIELD_SETTERS.put("薪级工资", SalaryDO::setSalaryGrade);
        SALARY_FIELD_SETTERS.put("单位职补", SalaryDO::setUnitAllowance);
        SALARY_FIELD_SETTERS.put("独生子女", SalaryDO::setOnlyChildAllowance);
        SALARY_FIELD_SETTERS.put("回民补贴", SalaryDO::setHuiEthnicAllowance);
        SALARY_FIELD_SETTERS.put("岗位津贴", SalaryDO::setPostAllowance);
        SALARY_FIELD_SETTERS.put("计生兼职", SalaryDO::setFamilyPlanningAllowance);
        SALARY_FIELD_SETTERS.put("福利费", SalaryDO::setWelfareFee);
        SALARY_FIELD_SETTERS.put("公务交通补贴", SalaryDO::setOfficialTransportAllowance);
        SALARY_FIELD_SETTERS.put("反聘费", SalaryDO::setRehireFee);
        SALARY_FIELD_SETTERS.put("补发工资", SalaryDO::setBackPay);
        SALARY_FIELD_SETTERS.put("其他工资", SalaryDO::setOtherWage);
        SALARY_FIELD_SETTERS.put("提租补贴", SalaryDO::setRentAllowance);
        SALARY_FIELD_SETTERS.put("基础性绩效", SalaryDO::setBasicPerformance);
        SALARY_FIELD_SETTERS.put("绩效工资", SalaryDO::setPerformanceSalary);
        SALARY_FIELD_SETTERS.put("应发合计", SalaryDO::setGrossSalaryTotal);
        SALARY_FIELD_SETTERS.put("社保基金", SalaryDO::setSocialSecurity);
        SALARY_FIELD_SETTERS.put("医保金", SalaryDO::setMedicalInsurance);
        SALARY_FIELD_SETTERS.put("职业年金", SalaryDO::setOccupationalAnnuity);
        SALARY_FIELD_SETTERS.put("房租费用", SalaryDO::setRentFee);
        SALARY_FIELD_SETTERS.put("病事假", SalaryDO::setSickLeaveDeduction);
        SALARY_FIELD_SETTERS.put("代扣所得税", SalaryDO::setIncomeTax);
        SALARY_FIELD_SETTERS.put("其他扣款", SalaryDO::setOtherDeduction);
        SALARY_FIELD_SETTERS.put("失业金", SalaryDO::setUnemploymentInsurance);
        SALARY_FIELD_SETTERS.put("住房公积金", SalaryDO::setHousingFund);
        SALARY_FIELD_SETTERS.put("工会经费", SalaryDO::setUnionFee);
        SALARY_FIELD_SETTERS.put("扣款合计", SalaryDO::setTotalDeduction);
        SALARY_FIELD_SETTERS.put("实发合计", SalaryDO::setNetSalaryTotal);
        SALARY_FIELD_SETTERS.put("所得基数", SalaryDO::setTaxBase);
        SALARY_FIELD_SETTERS.put("子女教育", SalaryDO::setChildEducation);
        SALARY_FIELD_SETTERS.put("继续教育", SalaryDO::setContinuingEducation);
        SALARY_FIELD_SETTERS.put("住房贷款利息", SalaryDO::setHousingLoanInterest);
        SALARY_FIELD_SETTERS.put("住房租金", SalaryDO::setHousingRent);
        SALARY_FIELD_SETTERS.put("老人赡养费", SalaryDO::setElderlySupport);
        SALARY_FIELD_SETTERS.put("其他合法扣除", SalaryDO::setOtherLegalDeduction);

        REQUIRED_HEADERS.add("人员编号");
        REQUIRED_HEADERS.addAll(SALARY_FIELD_SETTERS.keySet());
    }

    @Resource
    private SalaryMapper salaryMapper;

    @Resource
    private EmployeeMapper employeeMapper;

    @Resource
    private DeptService deptService;

    @Override
    public Long createSalary(SalarySaveReqVO createReqVO) {
        SalaryDO salary = BeanUtils.toBean(createReqVO, SalaryDO.class);
        salaryMapper.insert(salary);
        return salary.getId();
    }

    @Override
    public void updateSalary(SalarySaveReqVO updateReqVO) {
        validateSalaryExists(updateReqVO.getId());
        SalaryDO updateObj = BeanUtils.toBean(updateReqVO, SalaryDO.class);
        salaryMapper.updateById(updateObj);
    }

    @Override
    public void deleteSalary(Long id) {
        validateSalaryExists(id);
        salaryMapper.deleteById(id);
    }

    @Override
    public void deleteSalaryListByIds(List<Long> ids) {
        salaryMapper.deleteByIds(ids);
    }

    private void validateSalaryExists(Long id) {
        if (salaryMapper.selectById(id) == null) {
            throw exception(SALARY_NOT_EXISTS);
        }
    }

    @Override
    public SalaryRespVO getSalary(Long id) {
        SalaryRespVO salary = salaryMapper.selectByIdJoin(id);
        if (salary == null) {
            throw exception(SALARY_NOT_EXISTS);
        }
        return salary;
    }

    @Override
    public PageResult<SalaryRespVO> getSalaryPage(SalaryPageReqVO pageReqVO) {
        return salaryMapper.selectPage(pageReqVO);
    }

    // ==================== Excel 导入 ====================

    @Override
    public SalaryImportCheckRespVO checkSalaryImport(MultipartFile file) throws IOException {
        // 1. 文件名解析年月
        int[] ym = parseYearMonth(file.getOriginalFilename());
        byte[] bytes = file.getBytes();
        // 2. POI 读第 1 行表头 + FastExcel 读数据行（与项目 ExcelUtils 一致）
        Set<String> headerNames = readExcelHeaderNames(bytes);
        List<SalaryImportExcelVO> rows = readSalaryImportRows(bytes);
        // 3. 组装预检结果
        SalaryImportCheckRespVO resp = new SalaryImportCheckRespVO();
        resp.setYear(ym[0]);
        resp.setMonth(ym[1]);
        resp.setTotalRows(rows.size());
        // 4. 表头校验
        List<String> missing = findMissingHeaders(headerNames);
        resp.setHeaderValid(missing.isEmpty());
        resp.setMissingHeaders(missing);
        if (!missing.isEmpty() || rows.isEmpty()) {
            resp.setDuplicateCount(0);
            resp.setDuplicateEmployeeNos(Collections.emptyList());
            return resp;
        }
        // 5. 收集工号，查同年月同工号重复
        Set<String> employeeNos = collectEmployeeNos(rows);
        List<SalaryDO> existList = queryExistSalaries(ym[0], ym[1], employeeNos);
        resp.setDuplicateCount(existList.size());
        resp.setDuplicateEmployeeNos(existList.stream()
                .map(SalaryDO::getEmployeeNo)
                .filter(Objects::nonNull)
                .limit(10)
                .collect(Collectors.toList()));
        return resp;
    }

    @Override
    public SalaryImportRespVO importSalary(MultipartFile file, boolean confirmOverwrite) throws IOException {
        // 1. 文件名解析年月
        int[] ym = parseYearMonth(file.getOriginalFilename());
        byte[] bytes = file.getBytes();
        // 2. 读表头 + 数据
        Set<String> headerNames = readExcelHeaderNames(bytes);
        List<SalaryImportExcelVO> rows = readSalaryImportRows(bytes);
        // 3. 表头校验（导入阶段严格抛错）
        List<String> missing = findMissingHeaders(headerNames);
        if (!missing.isEmpty()) {
            throw exception(SALARY_IMPORT_HEADER_INVALID, String.join("、", missing));
        }
        if (rows.isEmpty()) {
            throw exception(SALARY_IMPORT_LIST_IS_EMPTY);
        }
        // 4. 收集工号
        Set<String> employeeNos = collectEmployeeNos(rows);
        // 5. 查员工档案（按工号）
        Map<String, EmployeeDO> empMap = queryEmployeesByNo(employeeNos);
        // 6. 查已有薪资（同年月同工号）
        Map<String, SalaryDO> existMap = queryExistSalaries(ym[0], ym[1], employeeNos).stream()
                .collect(Collectors.toMap(SalaryDO::getEmployeeNo, s -> s, (a, b) -> a));
        // 7. 重复检测：未确认覆盖且有重复 -> 抛错由前端弹窗确认
        if (!existMap.isEmpty() && !confirmOverwrite) {
            throw exception(SALARY_IMPORT_DUPLICATE_NEED_CONFIRM, existMap.size());
        }
        // 8. 逐行 upsert，单行失败收集不中断
        DeptLookupCache deptCache = buildDeptLookupCache();
        Map<String, String> failureRows = new LinkedHashMap<>();
        int insertCount = 0;
        int updateCount = 0;
        int rowIndex = 1; // 数据从第 2 行开始（第 1 行表头）
        for (SalaryImportExcelVO row : rows) {
            rowIndex++;
            String employeeNo = normalizeEmployeeNo(row.getEmployeeNo());
            String rowKey = StrUtil.isNotBlank(employeeNo) ? employeeNo : "第" + rowIndex + "行";
            if (StrUtil.isBlank(employeeNo)) {
                failureRows.put(rowKey, "人员编号为空");
                continue;
            }
            EmployeeDO emp = empMap.get(employeeNo);
            if (emp == null) {
                failureRows.put(rowKey, "工号在员工档案中不存在");
                continue;
            }
            try {
                SalaryDO salary = buildSalaryDO(row, ym[0], ym[1], emp, existMap.get(employeeNo), deptCache);
                if (salary.getId() != null) {
                    salaryMapper.updateById(salary);
                    updateCount++;
                } else {
                    salaryMapper.insert(salary);
                    insertCount++;
                }
            } catch (Exception e) {
                failureRows.put(rowKey, "保存失败：" + e.getMessage());
            }
        }
        return SalaryImportRespVO.builder()
                .year(ym[0])
                .month(ym[1])
                .insertCount(insertCount)
                .updateCount(updateCount)
                .failureCount(failureRows.size())
                .failureRows(failureRows)
                .build();
    }

    /**
     * 解析文件名中的年月，如「2025年12月份工资数据(1).xls」→ [2025, 12]
     */
    private int[] parseYearMonth(String filename) {
        if (StrUtil.isBlank(filename)) {
            throw exception(SALARY_IMPORT_FILENAME_INVALID);
        }
        Matcher m = YEAR_MONTH_PATTERN.matcher(filename);
        if (!m.find()) {
            throw exception(SALARY_IMPORT_FILENAME_INVALID);
        }
        int year = Integer.parseInt(m.group(1));
        int month = Integer.parseInt(m.group(2));
        if (month < 1 || month > 12) {
            throw exception(SALARY_IMPORT_FILENAME_INVALID);
        }
        return new int[]{year, month};
    }

    /**
     * 找出缺失的必需表头
     */
    private List<String> findMissingHeaders(Set<String> actualHeaders) {
        return REQUIRED_HEADERS.stream()
                .filter(h -> !actualHeaders.contains(h))
                .collect(Collectors.toList());
    }

    /**
     * 收集数据行中所有非空工号
     */
    private Set<String> collectEmployeeNos(List<SalaryImportExcelVO> rows) {
        Set<String> employeeNos = new LinkedHashSet<>();
        for (SalaryImportExcelVO row : rows) {
            String no = normalizeEmployeeNo(row.getEmployeeNo());
            if (StrUtil.isNotBlank(no)) {
                employeeNos.add(no);
            }
        }
        return employeeNos;
    }

    /**
     * 按工号批量查员工档案
     */
    private Map<String, EmployeeDO> queryEmployeesByNo(Set<String> employeeNos) {
        if (CollUtil.isEmpty(employeeNos)) {
            return Collections.emptyMap();
        }
        List<EmployeeDO> employees = employeeMapper.selectList(new LambdaQueryWrapperX<EmployeeDO>()
                .inIfPresent(EmployeeDO::getEmployeeNo, employeeNos));
        return employees.stream()
                .collect(Collectors.toMap(EmployeeDO::getEmployeeNo, e -> e, (a, b) -> a));
    }

    /**
     * 查同年月同工号的已有薪资记录
     */
    private List<SalaryDO> queryExistSalaries(int year, int month, Set<String> employeeNos) {
        if (CollUtil.isEmpty(employeeNos)) {
            return Collections.emptyList();
        }
        return salaryMapper.selectByYearMonthAndEmployeeNos(year, month, employeeNos);
    }

    /**
     * 将一行 Excel 数据组装为 SalaryDO（复用已有记录则覆盖更新）
     */
    private SalaryDO buildSalaryDO(SalaryImportExcelVO row, int year, int month,
                                   EmployeeDO emp, SalaryDO exist, DeptLookupCache deptCache) {
        SalaryDO salary;
        if (exist != null) {
            salary = exist;
            copyNonNullAmounts(row, salary);
        } else {
            salary = BeanUtils.toBean(row, SalaryDO.class);
        }
        salary.setPartnerId(emp.getPartnerId());
        salary.setEmployeeNo(emp.getEmployeeNo());
        salary.setYear(year);
        salary.setMonth(month);
        salary.setDept(resolveDept(row.getDepartment(), emp, deptCache));
        salary.setPersonnelCategory(resolvePersonnelCategory(row.getPersonnelCategory(), emp));
        return salary;
    }

    /**
     * 部门：Excel 有值则先对 system_dept（ID 或部门名称），对不上则回退员工档案；无值直接用档案
     */
    private Long resolveDept(String excelValue, EmployeeDO emp, DeptLookupCache deptCache) {
        if (StrUtil.isBlank(excelValue)) {
            return emp.getDept();
        }
        String input = excelValue.trim();
        // 1. 已是部门 ID（如 295）
        Long deptId = parseDeptId(input);
        if (deptId != null && deptCache.containsId(deptId)) {
            return deptId;
        }
        // 2. 按部门名称匹配（如「人事科」）
        Long deptIdByName = deptCache.getIdByName(input);
        if (deptIdByName != null) {
            return deptIdByName;
        }
        // 3. 客户填写的名称/编码对不上系统部门，回退员工档案
        return emp.getDept();
    }

    private Long parseDeptId(String input) {
        String normalized = normalizeNumericString(input);
        if (!StrUtil.isNumeric(normalized)) {
            return null;
        }
        try {
            return Long.parseLong(normalized);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private DeptLookupCache buildDeptLookupCache() {
        return new DeptLookupCache(deptService.getDeptList(new DeptListReqVO()));
    }

    /**
     * 导入时部门查找缓存（仅启用状态的 system_dept）
     */
    private static final class DeptLookupCache {

        private final Map<Long, DeptDO> idMap;
        private final Map<String, Long> nameToIdMap;

        private DeptLookupCache(List<DeptDO> depts) {
            idMap = new HashMap<>();
            nameToIdMap = new HashMap<>();
            if (CollUtil.isEmpty(depts)) {
                return;
            }
            for (DeptDO dept : depts) {
                if (!CommonStatusEnum.ENABLE.getStatus().equals(dept.getStatus())) {
                    continue;
                }
                idMap.put(dept.getId(), dept);
                nameToIdMap.putIfAbsent(dept.getName(), dept.getId());
            }
        }

        private boolean containsId(Long id) {
            return idMap.containsKey(id);
        }

        private Long getIdByName(String name) {
            return nameToIdMap.get(name);
        }
    }

    /**
     * 人员类别：Excel 有值则先对字典（编码或中文 label），对不上则回退员工档案；无值直接用档案
     */
    private String resolvePersonnelCategory(String excelValue, EmployeeDO emp) {
        if (StrUtil.isBlank(excelValue)) {
            return emp.getPersonnelCategory();
        }
        String input = excelValue.trim();
        // 1. 已是字典 value（如 30、07）
        if (DictFrameworkUtils.parseDictDataLabel(DICT_TYPE_PERSONNEL_CATEGORY, input) != null) {
            return input;
        }
        // 2. 按字典 label 反查（如「医生」→ 30）
        String valueByLabel = DictFrameworkUtils.parseDictDataValue(DICT_TYPE_PERSONNEL_CATEGORY, input);
        if (StrUtil.isNotBlank(valueByLabel)) {
            return valueByLabel;
        }
        // 3. 客户自定义中文（如「管理人员」）对不上字典，回退员工档案
        return emp.getPersonnelCategory();
    }

    /**
     * 覆盖更新时仅写入 Excel 中非空的薪酬字段
     */
    private void copyNonNullAmounts(SalaryImportExcelVO row, SalaryDO salary) {
        SALARY_FIELD_SETTERS.forEach((header, setter) -> {
            BigDecimal val = getAmountByHeader(row, header);
            if (val != null) {
                setter.accept(salary, val);
            }
        });
    }

    private BigDecimal getAmountByHeader(SalaryImportExcelVO row, String header) {
        return switch (header) {
            case "岗位工资" -> row.getBasicSalary();
            case "薪级工资" -> row.getSalaryGrade();
            case "单位职补" -> row.getUnitAllowance();
            case "独生子女" -> row.getOnlyChildAllowance();
            case "回民补贴" -> row.getHuiEthnicAllowance();
            case "岗位津贴" -> row.getPostAllowance();
            case "计生兼职" -> row.getFamilyPlanningAllowance();
            case "福利费" -> row.getWelfareFee();
            case "公务交通补贴" -> row.getOfficialTransportAllowance();
            case "反聘费" -> row.getRehireFee();
            case "补发工资" -> row.getBackPay();
            case "其他工资" -> row.getOtherWage();
            case "提租补贴" -> row.getRentAllowance();
            case "基础性绩效" -> row.getBasicPerformance();
            case "绩效工资" -> row.getPerformanceSalary();
            case "应发合计" -> row.getGrossSalaryTotal();
            case "社保基金" -> row.getSocialSecurity();
            case "医保金" -> row.getMedicalInsurance();
            case "职业年金" -> row.getOccupationalAnnuity();
            case "房租费用" -> row.getRentFee();
            case "病事假" -> row.getSickLeaveDeduction();
            case "代扣所得税" -> row.getIncomeTax();
            case "其他扣款" -> row.getOtherDeduction();
            case "失业金" -> row.getUnemploymentInsurance();
            case "住房公积金" -> row.getHousingFund();
            case "工会经费" -> row.getUnionFee();
            case "扣款合计" -> row.getTotalDeduction();
            case "实发合计" -> row.getNetSalaryTotal();
            case "所得基数" -> row.getTaxBase();
            case "子女教育" -> row.getChildEducation();
            case "继续教育" -> row.getContinuingEducation();
            case "住房贷款利息" -> row.getHousingLoanInterest();
            case "住房租金" -> row.getHousingRent();
            case "老人赡养费" -> row.getElderlySupport();
            case "其他合法扣除" -> row.getOtherLegalDeduction();
            default -> null;
        };
    }

    /**
     * 工号规范化：去除首尾空格，Excel 数字格可能读成 9999.0
     */
    private String normalizeEmployeeNo(String employeeNo) {
        if (StrUtil.isBlank(employeeNo)) {
            return employeeNo;
        }
        return normalizeNumericString(employeeNo.trim());
    }

    /** Excel 数字单元格常带 .0，统一去掉小数部分 */
    private String normalizeNumericString(String value) {
        if (StrUtil.isBlank(value)) {
            return value;
        }
        String normalized = value.trim();
        if (normalized.matches("\\d+\\.0+")) {
            normalized = normalized.substring(0, normalized.indexOf('.'));
        }
        return normalized;
    }

    /**
     * POI 读取第 1 行表头（用于校验必需列是否存在）
     */
    private Set<String> readExcelHeaderNames(byte[] bytes) throws IOException {
        Set<String> headers = new LinkedHashSet<>();
        try (ByteArrayInputStream is = new ByteArrayInputStream(bytes);
             Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row row = sheet.getRow(0);
            if (row == null) {
                return headers;
            }
            DataFormatter formatter = new DataFormatter();
            for (Cell cell : row) {
                String val = formatter.formatCellValue(cell);
                if (StrUtil.isNotBlank(val)) {
                    headers.add(val.trim());
                }
            }
        }
        return headers;
    }

    /**
     * 使用项目标准 ExcelUtils 按 @ExcelProperty 表头名称读取数据行
     */
    private List<SalaryImportExcelVO> readSalaryImportRows(byte[] bytes) throws IOException {
        return ExcelUtils.read(new ByteArrayMultipartFile(bytes), SalaryImportExcelVO.class);
    }

    /**
     * 内存 MultipartFile，便于同一文件字节流多次读取
     */
    private static class ByteArrayMultipartFile implements MultipartFile {
        private final byte[] content;

        ByteArrayMultipartFile(byte[] content) {
            this.content = content;
        }

        @Override
        public String getName() {
            return "file";
        }

        @Override
        public String getOriginalFilename() {
            return "salary-import.xls";
        }

        @Override
        public String getContentType() {
            return "application/vnd.ms-excel";
        }

        @Override
        public boolean isEmpty() {
            return content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() {
            return content;
        }

        @Override
        public java.io.InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
            throw new UnsupportedOperationException();
        }
    }

}

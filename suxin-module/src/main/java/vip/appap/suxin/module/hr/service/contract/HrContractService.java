package vip.appap.suxin.module.hr.service.contract;

import jakarta.validation.*;
import vip.appap.suxin.module.hr.controller.admin.contract.vo.*;
import vip.appap.suxin.framework.common.pojo.PageResult;

/**
 * 劳动合同 Service 接口
 *
 * @author admin
 */
public interface HrContractService {

    /**
     * 创建劳动合同
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createContract(@Valid HrContractSaveReqVO createReqVO);

    /**
     * 更新劳动合同
     *
     * @param updateReqVO 更新信息
     */
    void updateContract(@Valid HrContractSaveReqVO updateReqVO);

    /**
     * 删除劳动合同
     *
     * @param id 编号
     */
    void deleteContract(Long id);

    /**
     * 批量删除劳动合同
     *
     * @param ids 编号
     */
    void deleteContractListByIds(java.util.List<Long> ids);

    /**
     * 获得劳动合同
     *
     * @param id 编号
     * @return 劳动合同
     */
    HrContractRespVO getContract(Long id);

    /**
     * 获得劳动合同分页
     *
     * @param pageReqVO 分页查询
     * @return 劳动合同分页
     */
    PageResult<HrContractRespVO> getContractPage(HrContractPageReqVO pageReqVO);

    /**
     * 预览合同编号（新增表单打开时展示，对齐 OA 自动编号宏控件）
     *
     * @return 合同编号
     */
    String previewContractNo();

}

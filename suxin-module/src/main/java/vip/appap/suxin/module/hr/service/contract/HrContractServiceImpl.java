package vip.appap.suxin.module.hr.service.contract;

import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import vip.appap.suxin.module.hr.controller.admin.contract.vo.*;
import vip.appap.suxin.module.hr.dal.dataobject.contract.HrContractDO;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;

import vip.appap.suxin.module.hr.dal.mysql.contract.HrContractMapper;
import vip.appap.suxin.module.hr.dal.redis.no.HrContractNoRedisDAO;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.hr.enums.ErrorCodeConstants.*;

/**
 * 劳动合同 Service 实现类
 *
 * 说明：劳动合同只写入 hr_contract 表，不双写 partner 表。
 * 查询时通过 LEFT JOIN partner 获取员工姓名。
 *
 * @author admin
 */
@Service
@Validated
public class HrContractServiceImpl implements HrContractService {

    @Resource
    private HrContractMapper contractMapper;
    @Resource
    private HrContractNoRedisDAO contractNoRedisDAO;

    @Override
    public String previewContractNo() {
        return contractNoRedisDAO.preview();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createContract(HrContractSaveReqVO createReqVO) {
        HrContractDO contract = BeanUtils.toBean(createReqVO, HrContractDO.class);
        contract.setContractNo(generateContractNo());
        contractMapper.insert(contract);
        return contract.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateContract(HrContractSaveReqVO updateReqVO) {
        HrContractDO existing = contractMapper.selectById(updateReqVO.getId());
        if (existing == null) {
            throw exception(CONTRACT_NOT_EXISTS);
        }
        HrContractDO updateObj = BeanUtils.toBean(updateReqVO, HrContractDO.class);
        updateObj.setContractNo(existing.getContractNo());
        contractMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteContract(Long id) {
        validateContractExists(id);
        contractMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteContractListByIds(List<Long> ids) {
        contractMapper.deleteByIds(ids);
    }

    private void validateContractExists(Long id) {
        if (contractMapper.selectById(id) == null) {
            throw exception(CONTRACT_NOT_EXISTS);
        }
    }

    @Override
    public HrContractRespVO getContract(Long id) {
        HrContractRespVO contract = contractMapper.selectByIdJoin(id);
        if (contract == null) {
            throw exception(CONTRACT_NOT_EXISTS);
        }
        return contract;
    }

    @Override
    public PageResult<HrContractRespVO> getContractPage(HrContractPageReqVO pageReqVO) {
        return contractMapper.selectPage(pageReqVO);
    }

    /**
     * 生成合同编号，对齐 OA flow_sequence#2：yyyyMMdd + 当日递增序号
     */
    private String generateContractNo() {
        String no = contractNoRedisDAO.generate();
        if (contractMapper.selectByContractNo(no) == null) {
            return no;
        }
        // 极端并发：若 DB 已存在则继续递增直到唯一
        for (int i = 0; i < 30; i++) {
            no = contractNoRedisDAO.generate();
            if (contractMapper.selectByContractNo(no) == null) {
                return no;
            }
        }
        return no;
    }

}

package vip.appap.suxin.module.accountant.api;

import vip.appap.suxin.module.accountant.api.dto.PayTransferCreateReqDTO;
import vip.appap.suxin.module.accountant.api.dto.PayTransferCreateRespDTO;
import vip.appap.suxin.module.accountant.api.dto.PayTransferRespDTO;
import jakarta.validation.Valid;

/**
 * 转账单 API 接口
 *
 * @author jason
 */
public interface PayTransferApi {

    /**
     * 创建转账单
     *
     * @param reqDTO 创建请求
     * @return 创建结果
     */
    PayTransferCreateRespDTO createTransfer(@Valid PayTransferCreateReqDTO reqDTO);

    /**
     * 获得转账单
     *
     * @param id 转账单编号
     * @return 转账单
     */
    PayTransferRespDTO getTransfer(Long id);

}


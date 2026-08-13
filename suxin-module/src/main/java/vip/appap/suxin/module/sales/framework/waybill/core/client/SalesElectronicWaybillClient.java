package vip.appap.suxin.module.sales.framework.waybill.core.client;

import vip.appap.suxin.module.sales.dal.dataobject.SalesElectronicWaybillAccountDO;
import vip.appap.suxin.module.sales.framework.waybill.core.client.dto.SalesElectronicWaybillCancelReqDTO;
import vip.appap.suxin.module.sales.framework.waybill.core.client.dto.SalesElectronicWaybillCancelRespDTO;
import vip.appap.suxin.module.sales.framework.waybill.core.client.dto.SalesElectronicWaybillOrderReqDTO;
import vip.appap.suxin.module.sales.framework.waybill.core.client.dto.SalesElectronicWaybillOrderRespDTO;
import vip.appap.suxin.module.sales.framework.waybill.core.client.dto.SalesElectronicWaybillReprintReqDTO;

/**
 * 快递100电子面单客户端端口
 * <p>
 * 独立于既有轨迹查询 {@code ExpressClient}，仅服务电子面单下单/复打/取消。
 *
 * @author 书心软件
 */
public interface SalesElectronicWaybillClient {

    /**
     * 电子面单下单
     *
     * @param account 电子面单账户（含平台授权与承运商账户凭据）
     * @param reqDTO  下单请求
     * @return 下单结果；业务失败抛出 ServiceException
     */
    SalesElectronicWaybillOrderRespDTO order(SalesElectronicWaybillAccountDO account, SalesElectronicWaybillOrderReqDTO reqDTO);

    /**
     * 电子面单复打（不创建新单号）
     *
     * @param account 电子面单账户
     * @param reqDTO  复打请求
     * @return 复打结果；业务失败抛出 ServiceException
     */
    SalesElectronicWaybillOrderRespDTO reprint(SalesElectronicWaybillAccountDO account, SalesElectronicWaybillReprintReqDTO reqDTO);

    /**
     * 电子面单取消
     *
     * @param account 电子面单账户
     * @param reqDTO  取消请求
     * @return 取消响应
     */
    SalesElectronicWaybillCancelRespDTO cancel(SalesElectronicWaybillAccountDO account, SalesElectronicWaybillCancelReqDTO reqDTO);

}

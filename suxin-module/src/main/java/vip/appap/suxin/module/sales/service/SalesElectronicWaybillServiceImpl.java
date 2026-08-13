package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.module.sales.dal.dataobject.SalesElectronicWaybillDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesElectronicWaybillMapper;
import vip.appap.suxin.module.sales.enums.SalesElectronicWaybillStatusEnum;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 电子面单订单记录 Service 实现类
 *
 * @author 书心软件
 */
@Service
@Validated
public class SalesElectronicWaybillServiceImpl implements SalesElectronicWaybillService {

    @Resource
    private SalesElectronicWaybillMapper waybillMapper;

    @Override
    public SalesElectronicWaybillDO getLatestByOrderId(Long orderId) {
        return waybillMapper.selectLatestByOrderId(orderId);
    }

    @Override
    public SalesElectronicWaybillDO getOldestUnknownByOrderId(Long orderId) {
        return waybillMapper.selectOldestUnknownByOrderId(orderId);
    }

    @Override
    public SalesElectronicWaybillDO getValidByOrderId(Long orderId) {
        return waybillMapper.selectValidByOrderId(orderId);
    }

    @Override
    public List<SalesElectronicWaybillDO> getValidListByOrderIds(Collection<Long> orderIds) {
        return waybillMapper.selectValidListByOrderIds(orderIds);
    }

    @Override
    public void insertWaybill(SalesElectronicWaybillDO waybill) {
        waybillMapper.insert(waybill);
    }

    @Override
    public void markCanceled(Long id, Long cancelUserId, String reason) {
        waybillMapper.updateById(new SalesElectronicWaybillDO()
                .setId(id)
                .setStatus(SalesElectronicWaybillStatusEnum.CANCELED.getStatus())
                .setValidFlag(null) // 释放“一单一有效面单”唯一约束
                .setCancelTime(LocalDateTime.now())
                .setCancelReason(reason)
                .setCancelUserId(cancelUserId));
    }

    @Override
    public void updateLabelUrl(Long id, String labelUrl) {
        waybillMapper.updateById(new SalesElectronicWaybillDO().setId(id).setLabelUrl(labelUrl));
    }

}

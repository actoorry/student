package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.module.sales.convert.SalesOrderLogConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesOrderLogDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesOrderLogMapper;
import vip.appap.suxin.module.sales.service.bo.SalesOrderLogCreateReqBO;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 交易下单日志 Service 实现类
 *
 * @author 陈賝
 * @since 2023/7/6 15:44
 */
@Service
public class SalesOrderLogServiceImpl implements SalesOrderLogService {

    @Resource
    private SalesOrderLogMapper tradeOrderLogMapper;

    @Override
    public void createOrderLog(SalesOrderLogCreateReqBO createReqBO) {
        tradeOrderLogMapper.insert(SalesOrderLogConvert.INSTANCE.convert(createReqBO));
    }

    @Override
    public List<SalesOrderLogDO> getOrderLogListByOrderId(Long orderId) {
        return tradeOrderLogMapper.selectListByOrderId(orderId);
    }

}

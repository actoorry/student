package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.module.sales.convert.SalesAfterSaleLogConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesAfterSaleLogDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesAfterSaleLogMapper;
import vip.appap.suxin.module.sales.service.bo.SalesAfterSaleLogCreateReqBO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 交易售后日志 Service 实现类
 *
 * @author 书心软件
 */
@Service
@Validated
public class SalesAfterSaleLogServiceImpl implements SalesAfterSaleLogService {

    @Resource
    private SalesAfterSaleLogMapper afterSaleLogMapper;

    @Override
    public void createAfterSaleLog(SalesAfterSaleLogCreateReqBO createReqBO) {
        SalesAfterSaleLogDO afterSaleLog = SalesAfterSaleLogConvert.INSTANCE.convert(createReqBO);
        afterSaleLogMapper.insert(afterSaleLog);
    }

    @Override
    public List<SalesAfterSaleLogDO> getAfterSaleLogList(Long afterSaleId) {
        return afterSaleLogMapper.selectListByAfterSaleId(afterSaleId);
    }

}

package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.framework.common.util.collection.CollectionUtils;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesConfigSaveReqVO;
import vip.appap.suxin.module.sales.convert.SalesConfigConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesConfigDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesConfigMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 交易中心配置 Service 实现类
 *
 * @author owen
 */
@Service
@Validated
public class SalesConfigServiceImpl implements SalesConfigService {

    @Resource
    private SalesConfigMapper tradeConfigMapper;

    @Override
    public void saveTradeConfig(SalesConfigSaveReqVO saveReqVO) {
        // 存在，则进行更新
        SalesConfigDO dbConfig = getTradeConfig();
        if (dbConfig != null) {
            tradeConfigMapper.updateById(SalesConfigConvert.INSTANCE.convert(saveReqVO).setId(dbConfig.getId()));
            return;
        }
        // 不存在，则进行插入
        tradeConfigMapper.insert(SalesConfigConvert.INSTANCE.convert(saveReqVO));
    }

    @Override
    public SalesConfigDO getTradeConfig() {
        List<SalesConfigDO> list = tradeConfigMapper.selectList();
        return CollectionUtils.getFirst(list);
    }

}

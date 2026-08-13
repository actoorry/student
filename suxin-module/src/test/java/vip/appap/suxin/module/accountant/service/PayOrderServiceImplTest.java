package vip.appap.suxin.module.accountant.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.accountant.dal.dataobject.PayOrderDO;
import vip.appap.suxin.module.accountant.dal.dataobject.PayOrderExtensionDO;
import vip.appap.suxin.module.accountant.dal.mysql.PayOrderExtensionMapper;
import vip.appap.suxin.module.accountant.dal.mysql.PayOrderMapper;
import vip.appap.suxin.module.accountant.enums.PayChannelEnum;
import vip.appap.suxin.module.accountant.enums.PayOrderStatusEnum;
import vip.appap.suxin.module.accountant.service.PayChannelService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PayOrderServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private PayOrderServiceImpl service;

    @Mock
    private PayOrderMapper orderMapper;
    @Mock
    private PayOrderExtensionMapper orderExtensionMapper;
    @Mock
    private PayChannelService channelService;

    @Test
    void syncOrder_wechatVirtualExtension_skipsGenericPayClient() {
        PayOrderExtensionDO extension = wechatVirtualExtension();
        when(orderExtensionMapper.selectListByStatusAndCreateTimeGe(anyInt(), any(LocalDateTime.class)))
                .thenReturn(List.of(extension));

        int count = service.syncOrder(LocalDateTime.now().minusMinutes(10));

        assertEquals(0, count);
        verifyNoInteractions(channelService);
    }

    @Test
    void syncOrderQuietly_wechatVirtualExtension_skipsGenericPayClient() {
        when(orderExtensionMapper.selectListByOrderIdAndStatus(10L, PayOrderStatusEnum.WAITING.getStatus()))
                .thenReturn(List.of(wechatVirtualExtension()));

        service.syncOrderQuietly(10L);

        verifyNoInteractions(channelService);
    }

    @Test
    void validateOrderActuallyPaid_wechatVirtualExtension_skipsGenericPayClient() {
        when(orderExtensionMapper.selectListByOrderId(10L)).thenReturn(List.of(wechatVirtualExtension()));

        service.validateOrderActuallyPaid(10L);

        verifyNoInteractions(channelService);
    }

    @Test
    void expireOrder_wechatVirtualExtension_doesNotCloseWithoutVirtualQuery() {
        PayOrderDO order = new PayOrderDO().setId(10L).setStatus(PayOrderStatusEnum.WAITING.getStatus());
        when(orderMapper.selectListByStatusAndExpireTimeLt(anyInt(), any(LocalDateTime.class))).thenReturn(List.of(order));
        when(orderExtensionMapper.selectListByOrderId(10L)).thenReturn(List.of(wechatVirtualExtension()));

        int count = service.expireOrder();

        assertEquals(0, count);
        verifyNoInteractions(channelService);
        verify(orderMapper, never()).updateByIdAndStatus(anyLong(), anyInt(), any(PayOrderDO.class));
    }

    private PayOrderExtensionDO wechatVirtualExtension() {
        return new PayOrderExtensionDO()
                .setId(100L)
                .setOrderId(10L)
                .setChannelId(20L)
                .setChannelCode(PayChannelEnum.WX_VIRTUAL_LITE.getCode())
                .setStatus(PayOrderStatusEnum.WAITING.getStatus());
    }

}

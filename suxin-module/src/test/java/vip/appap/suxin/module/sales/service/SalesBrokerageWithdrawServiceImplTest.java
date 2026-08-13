package vip.appap.suxin.module.sales.service;

import jakarta.validation.Validator;
import vip.appap.suxin.framework.common.exception.ServiceException;
import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.accountant.api.PayTransferApi;
import vip.appap.suxin.module.accountant.api.AccountApi;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBrokerageWithdrawCreateReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBrokerageWithdrawDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesConfigDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesBrokerageWithdrawMapper;
import vip.appap.suxin.module.sales.enums.SalesBrokerageRecordBizTypeEnum;
import vip.appap.suxin.module.sales.enums.SalesBrokerageWithdrawStatusEnum;
import vip.appap.suxin.module.sales.enums.SalesBrokerageWithdrawTypeEnum;
import vip.appap.suxin.module.sales.framework.order.config.SalesOrderProperties;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Collections;

import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.BROKERAGE_WITHDRAW_MIN_PRICE;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.BROKERAGE_WITHDRAW_NOT_EXISTS;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.BROKERAGE_WITHDRAW_STATUS_NOT_AUDITING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SalesBrokerageWithdrawServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private SalesBrokerageWithdrawServiceImpl withdrawService;

    @Mock
    private SalesBrokerageWithdrawMapper brokerageWithdrawMapper;
    @Mock
    private SalesBrokerageRecordService brokerageRecordService;
    @Mock
    private SalesConfigService tradeConfigService;
    @Mock
    private PayTransferApi payTransferApi;
    @Mock
    private AccountApi accountApi;
    @Mock
    private Validator validator;
    @Mock
    private SalesOrderProperties tradeOrderProperties;

    // ========== 提现申请（任务 4.5） ==========

    @Test
    void createBrokerageWithdraw_belowMinPrice_throwsMinPrice() {
        // 金额低于租户最低提现门槛：拒绝且不扣减余额
        when(tradeConfigService.getTradeConfig()).thenReturn(salesConfig(1000, 0));
        AppSalesBrokerageWithdrawCreateReqVO reqVO = withdrawReqVO(SalesBrokerageWithdrawTypeEnum.WALLET.getType(), 500);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> withdrawService.createBrokerageWithdraw(100L, reqVO));
        assertEquals(BROKERAGE_WITHDRAW_MIN_PRICE.getCode(), ex.getCode());
        verify(brokerageWithdrawMapper, never()).insert(any(SalesBrokerageWithdrawDO.class));
    }

    @Test
    void createBrokerageWithdraw_valid_createsWithdrawWithFeeAndRecord() {
        // 有效提现：按配置计算手续费并生成提现记录与余额变动
        when(tradeConfigService.getTradeConfig()).thenReturn(salesConfig(100, 10));
        when(validator.validate(any(), any())).thenReturn(Collections.emptySet());
        AppSalesBrokerageWithdrawCreateReqVO reqVO = withdrawReqVO(SalesBrokerageWithdrawTypeEnum.WALLET.getType(), 10000);

        Long id = withdrawService.createBrokerageWithdraw(100L, reqVO);

        ArgumentCaptor<SalesBrokerageWithdrawDO> captor = ArgumentCaptor.forClass(SalesBrokerageWithdrawDO.class);
        verify(brokerageWithdrawMapper).insert(captor.capture());
        SalesBrokerageWithdrawDO withdraw = captor.getValue();
        assertEquals(100L, withdraw.getUserId());
        assertEquals(10000, withdraw.getPrice());
        assertEquals(1000, withdraw.getFeePrice()); // 手续费 10%
        // 余额扣减：提现记录关联
        verify(brokerageRecordService).reduceBrokerage(100L, SalesBrokerageRecordBizTypeEnum.WITHDRAW,
                String.valueOf(id), 10000, SalesBrokerageRecordBizTypeEnum.WITHDRAW.getTitle());
    }

    // ========== 审核（任务 4.5） ==========

    @Test
    void auditBrokerageWithdraw_reject_refundsBalance() {
        // 审核拒绝：按现有记录返还可用佣金
        SalesBrokerageWithdrawDO withdraw = new SalesBrokerageWithdrawDO().setId(1L).setUserId(300L).setPrice(8000)
                .setStatus(SalesBrokerageWithdrawStatusEnum.AUDITING.getStatus());
        when(brokerageWithdrawMapper.selectById(1L)).thenReturn(withdraw);
        when(brokerageWithdrawMapper.updateByIdAndStatus(eq(1L), eq(SalesBrokerageWithdrawStatusEnum.AUDITING.getStatus()), any()))
                .thenReturn(1);

        withdrawService.auditBrokerageWithdraw(1L, SalesBrokerageWithdrawStatusEnum.AUDIT_FAIL, "不符合要求", "127.0.0.1");

        verify(brokerageRecordService).addBrokerage(300L, SalesBrokerageRecordBizTypeEnum.WITHDRAW_REJECT,
                "1", 8000, SalesBrokerageRecordBizTypeEnum.WITHDRAW_REJECT.getTitle());
    }

    @Test
    void auditBrokerageWithdraw_approve_manualTransferMarksSuccess() {
        // 审核通过（非 API 转账，手动打款）：标记提现成功
        SalesBrokerageWithdrawDO withdraw = new SalesBrokerageWithdrawDO().setId(1L).setUserId(300L).setPrice(8000)
                .setType(SalesBrokerageWithdrawTypeEnum.BANK.getType())
                .setStatus(SalesBrokerageWithdrawStatusEnum.AUDITING.getStatus());
        when(brokerageWithdrawMapper.selectById(1L)).thenReturn(withdraw);
        when(brokerageWithdrawMapper.updateByIdAndStatus(eq(1L), eq(SalesBrokerageWithdrawStatusEnum.AUDITING.getStatus()), any()))
                .thenReturn(1);
        when(brokerageWithdrawMapper.updateByIdAndStatus(eq(1L), eq(SalesBrokerageWithdrawStatusEnum.AUDIT_SUCCESS.getStatus()), any()))
                .thenReturn(1);

        withdrawService.auditBrokerageWithdraw(1L, SalesBrokerageWithdrawStatusEnum.AUDIT_SUCCESS, "", "127.0.0.1");

        verify(brokerageRecordService, never()).addBrokerage(any(), any(), any(), any(), any());
    }

    @Test
    void auditBrokerageWithdraw_notAuditing_throwsStatusNotAuditing() {
        // 非审核中状态：拒绝审核
        SalesBrokerageWithdrawDO withdraw = new SalesBrokerageWithdrawDO().setId(1L).setUserId(300L).setPrice(8000)
                .setStatus(SalesBrokerageWithdrawStatusEnum.WITHDRAW_SUCCESS.getStatus());
        when(brokerageWithdrawMapper.selectById(1L)).thenReturn(withdraw);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> withdrawService.auditBrokerageWithdraw(1L, SalesBrokerageWithdrawStatusEnum.AUDIT_FAIL, "", "127.0.0.1"));
        assertEquals(BROKERAGE_WITHDRAW_STATUS_NOT_AUDITING.getCode(), ex.getCode());
    }

    // ========== 转账结果回调（任务 4.5） ==========

    @Test
    void updateBrokerageWithdrawTransferred_repeatedCallback_isIdempotent() {
        // 重复回调：转账单编号相同，直接返回不重复处理
        SalesBrokerageWithdrawDO withdraw = new SalesBrokerageWithdrawDO().setId(1L).setUserId(300L).setPrice(8000)
                .setStatus(SalesBrokerageWithdrawStatusEnum.WITHDRAW_SUCCESS.getStatus()).setPayTransferId(999L);
        when(brokerageWithdrawMapper.selectById(1L)).thenReturn(withdraw);

        withdrawService.updateBrokerageWithdrawTransferred(1L, 999L);

        verify(brokerageWithdrawMapper, never()).updateById(any(SalesBrokerageWithdrawDO.class));
    }

    @Test
    void updateBrokerageWithdrawTransferred_notExists_throwsNotExists() {
        when(brokerageWithdrawMapper.selectById(1L)).thenReturn(null);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> withdrawService.updateBrokerageWithdrawTransferred(1L, 999L));
        assertEquals(BROKERAGE_WITHDRAW_NOT_EXISTS.getCode(), ex.getCode());
    }

    private SalesConfigDO salesConfig(int withdrawMinPrice, int feePercent) {
        SalesConfigDO config = new SalesConfigDO();
        config.setBrokerageWithdrawMinPrice(withdrawMinPrice);
        config.setBrokerageWithdrawFeePercent(feePercent);
        return config;
    }

    private AppSalesBrokerageWithdrawCreateReqVO withdrawReqVO(Integer type, Integer price) {
        AppSalesBrokerageWithdrawCreateReqVO reqVO = new AppSalesBrokerageWithdrawCreateReqVO();
        reqVO.setType(type);
        reqVO.setPrice(price);
        reqVO.setUserAccount("10086");
        reqVO.setUserName("测试");
        return reqVO;
    }

}

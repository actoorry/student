package vip.appap.suxin.module.sales.dal.dataobject;

import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 电子面单订单记录 DO
 *
 * 保存快递100电子面单下单结果：销售订单关联、账户、快递公司、运单号、第三方订单标识、
 * 状态、寄收件快照、打印类型、失败摘要与作废审计。
 *
 * 并发一致性：同一订单仅允许一条有效面单，由唯一索引 (order_id, valid_flag) 保证；
 * 有效面单 valid_flag=1，作废/失败 valid_flag=NULL（MySQL 唯一索引允许多个 NULL）。
 *
 * @author 书心软件
 */
@TableName(value = "sales_electronic_waybill")
@KeySequence("sales_electronic_waybill_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesElectronicWaybillDO extends BaseDO {

    /**
     * 编号，自增
     */
    @TableId
    private Long id;

    /**
     * 销售订单编号
     *
     * 关联 {@link SalesOrderDO#getId()}
     */
    private Long orderId;

    /**
     * 销售订单流水号快照
     *
     * 关联 {@link SalesOrderDO#getNo()}
     */
    private String orderNo;

    /**
     * 电子面单账户编号
     *
     * 关联 {@link SalesElectronicWaybillAccountDO#getId()}
     */
    private Long accountId;

    /**
     * 快递公司编号
     *
     * 关联 {@link SalesDeliveryExpressDO#getId()}
     */
    private Long expressId;

    /**
     * 快递公司编码快照
     */
    private String expressCode;

    /**
     * 快递单号（快递100 下单返回）
     */
    private String waybillNo;

    /**
     * 快递公司订单号（快递100 下单返回）
     */
    private String kdComOrderNum;

    /**
     * 第三方任务 ID（快递100 下单返回，复打 printOld 使用）
     */
    private String taskId;

    /**
     * 第三方订单标识（下单时发送给快递100 的业务订单号，幂等键）
     */
    private String thirdOrderId;

    /**
     * 状态
     *
     * 枚举 {@link vip.appap.suxin.module.sales.enums.SalesElectronicWaybillStatusEnum}
     */
    private Integer status;

    /**
     * 有效性标记：有效面单=1，作废/失败=NULL
     *
     * 与 order_id 组成唯一索引，保证一单一有效面单的并发一致性
     */
    private Integer validFlag;

    /**
     * 打印类型
     *
     * 枚举 {@link vip.appap.suxin.module.sales.enums.SalesElectronicWaybillPrintTypeEnum}
     */
    private String printType;

    /**
     * 面单短链（HTML/IMAGE）
     */
    private String labelUrl;

    /**
     * 寄件人姓名快照
     */
    private String senderName;
    /**
     * 寄件人手机快照
     */
    private String senderMobile;
    /**
     * 寄件人地区编码快照
     */
    private Integer senderAreaId;
    /**
     * 寄件人地区名称快照
     */
    private String senderAreaName;
    /**
     * 寄件人详细地址快照
     */
    private String senderDetailAddress;

    /**
     * 收件人姓名快照
     */
    private String receiverName;
    /**
     * 收件人手机快照
     */
    private String receiverMobile;
    /**
     * 收件人地区编码快照
     */
    private Integer receiverAreaId;
    /**
     * 收件人地区名称快照
     */
    private String receiverAreaName;
    /**
     * 收件人详细地址快照
     */
    private String receiverDetailAddress;

    /**
     * 失败错误码（脱敏）
     */
    private String failCode;
    /**
     * 失败摘要（脱敏）
     */
    private String failMessage;

    /**
     * 作废时间
     */
    private LocalDateTime cancelTime;
    /**
     * 作废原因
     */
    private String cancelReason;
    /**
     * 作废操作人
     */
    private Long cancelUserId;

}

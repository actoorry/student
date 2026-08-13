package vip.appap.suxin.module.system.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.system.controller.admin.vo.NotifyMessageMyPageReqVO;
import vip.appap.suxin.module.system.controller.admin.vo.NotifyMessagePageReqVO;
import vip.appap.suxin.module.system.dal.dataobject.NotifyMessageDO;
import vip.appap.suxin.module.system.dal.dataobject.NotifyTemplateDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 站内信 Service 接口
 *
 * @author xrcoder
 */
public interface NotifyMessageService {

    /**
     * 创建站内信
     *
     * @param userId 用户编号
     * @param userType 用户类型
     * @param template 模版信息
     * @param templateContent 模版内容
     * @param templateParams 模版参数
     * @return 站内信编号
     */
    Long createNotifyMessage(Long userId, Integer userType,
                             NotifyTemplateDO template, String templateContent, Map<String, Object> templateParams);

    /**
     * 获得站内信分页
     *
     * @param pageReqVO 分页查询
     * @return 站内信分页
     */
    PageResult<NotifyMessageDO> getNotifyMessagePage(NotifyMessagePageReqVO pageReqVO);

    /**
     * 获得【我的】站内信分页
     *
     * @param pageReqVO 分页查询
     * @param userId 用户编号
     * @param userType 用户类型
     * @return 站内信分页
     */
    PageResult<NotifyMessageDO> getMyMyNotifyMessagePage(NotifyMessageMyPageReqVO pageReqVO, Long userId, Integer userType);

    /**
     * 获得站内信
     *
     * @param id 编号
     * @return 站内信
     */
    NotifyMessageDO getNotifyMessage(Long id);

    /**
     * 获得【我的】未读站内信列表
     *
     * @param userId   用户编号
     * @param userType 用户类型
     * @param size     数量
     * @return 站内信列表
     */
    List<NotifyMessageDO> getUnreadNotifyMessageList(Long userId, Integer userType, Integer size);

    /**
     * 统计用户未读站内信条数
     *
     * @param userId   用户编号
     * @param userType 用户类型
     * @return 返回未读站内信条数
     */
    Long getUnreadNotifyMessageCount(Long userId, Integer userType);

    /**
     * 标记站内信为已读
     *
     * @param ids    站内信编号集合
     * @param userId 用户编号
     * @param userType 用户类型
     * @return 更新到的条数
     */
    int updateNotifyMessageRead(Collection<Long> ids, Long userId, Integer userType);

    /**
     * 标记所有站内信为已读
     *
     * @param userId   用户编号
     * @param userType 用户类型
     * @return 更新到的条数
     */
    int updateAllNotifyMessageRead(Long userId, Integer userType);

    /**
     * 鏍囪鎸囧畾妯℃澘缂栫爜鐨勭珯鍐呬俊涓哄凡璇?
     *
     * @param userId 鐢ㄦ埛缂栧彿
     * @param userType 鐢ㄦ埛绫诲瀷
     * @param templateCode 妯℃澘缂栫爜
     * @return 鏇存柊鍒扮殑鏉℃暟
     */
    int updateNotifyMessageReadByTemplateCode(Long userId, Integer userType, String templateCode);

    /**
     * 鍒犻櫎銆愭垜鐨勩€戠珯鍐呬俊
     *
     * @param id 绔欏唴淇＄紪鍙?
     * @param userId 鐢ㄦ埛缂栧彿
     * @param userType 鐢ㄦ埛绫诲瀷
     * @return 鍒犻櫎鍒扮殑鏉℃暟
     */
    int deleteMyNotifyMessage(Long id, Long userId, Integer userType);

}

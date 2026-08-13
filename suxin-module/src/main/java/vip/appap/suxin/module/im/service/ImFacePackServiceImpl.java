package vip.appap.suxin.module.im.service;

import cn.hutool.core.collection.CollUtil;
import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.im.controller.admin.vo.ImFacePackPageReqVO;
import vip.appap.suxin.module.im.controller.admin.vo.ImFacePackSaveReqVO;
import vip.appap.suxin.module.im.dal.dataobject.ImFacePackDO;
import vip.appap.suxin.module.im.dal.mysql.ImFacePackMapper;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.im.enums.ErrorCodeConstants.FACE_PACK_HAS_ITEMS;
import static vip.appap.suxin.module.im.enums.ErrorCodeConstants.FACE_PACK_NOT_EXISTS;

/**
 * IM 表情包 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ImFacePackServiceImpl implements ImFacePackService {

    @Resource
    private ImFacePackMapper facePackMapper;

    /**
     * @Lazy 解决与 ImFacePackItemServiceImpl 的循环依赖（item.create / update 校验所属包存在 → 反向调用本类）
     */
    @Resource
    @Lazy
    private ImFacePackItemService facePackItemService;

    // ==================== 用户端 ====================

    @Override
    public List<ImFacePackDO> getEnabledFacePackList() {
        return facePackMapper.selectListByStatusOrderBySort(CommonStatusEnum.ENABLE.getStatus());
    }

    @Override
    public ImFacePackDO validateFacePackExists(Long id) {
        ImFacePackDO pack = facePackMapper.selectById(id);
        if (pack == null) {
            throw exception(FACE_PACK_NOT_EXISTS);
        }
        return pack;
    }

    // ==================== 管理后台 ====================

    @Override
    public PageResult<ImFacePackDO> getFacePackPage(ImFacePackPageReqVO reqVO) {
        return facePackMapper.selectPage(reqVO);
    }

    @Override
    public ImFacePackDO getFacePack(Long id) {
        return facePackMapper.selectById(id);
    }

    @Override
    public Long createFacePack(ImFacePackSaveReqVO reqVO) {
        ImFacePackDO pack = BeanUtils.toBean(reqVO, ImFacePackDO.class);
        facePackMapper.insert(pack);
        return pack.getId();
    }

    @Override
    public void updateFacePack(ImFacePackSaveReqVO reqVO) {
        // 1. 校验存在
        validateFacePackExists(reqVO.getId());

        // 2. 更新
        ImFacePackDO updateObj = BeanUtils.toBean(reqVO, ImFacePackDO.class);
        facePackMapper.updateById(updateObj);
    }

    @Override
    public void deleteFacePack(Long id) {
        // 1.1 校验存在
        validateFacePackExists(id);
        // 1.2 校验表情包下没有表情；防止误删表情包导致历史 face 消息无法回查归属
        if (facePackItemService.getFacePackItemCount(id) > 0) {
            throw exception(FACE_PACK_HAS_ITEMS);
        }

        // 2. 删除
        facePackMapper.deleteById(id);
    }

    @Override
    public void deleteFacePackList(List<Long> ids) {
        // 1. 任一存在表情则拒绝整批删除，避免「只删一半」的中间态
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        if (facePackItemService.getFacePackItemCount(ids) > 0) {
            throw exception(FACE_PACK_HAS_ITEMS);
        }

        // 2. 删除
        facePackMapper.deleteByIds(ids);
    }

}

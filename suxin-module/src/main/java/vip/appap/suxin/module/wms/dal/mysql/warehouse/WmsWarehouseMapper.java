package vip.appap.suxin.module.wms.dal.mysql.warehouse;

import java.util.*;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.module.wms.dal.dataobject.warehouse.WmsWarehouseDO;
import org.apache.ibatis.annotations.Mapper;
import vip.appap.suxin.module.wms.controller.admin.warehouse.vo.*;

/**
 * 仓库位置表（仓库/库区/库位树形结构） Mapper
 *
 * @author admin
 */
@Mapper
public interface WmsWarehouseMapper extends BaseMapperX<WmsWarehouseDO> {

    default PageResult<WmsWarehouseDO> selectPage(WmsWarehousePageReqVO reqVO) {
        // 名称搜索：返回「命中节点 + 其所有下级（整棵子树）」，保证树形结构完整
        if (reqVO.getName() != null && !reqVO.getName().isEmpty()) {
            return selectPageWithNameSubTree(reqVO);
        }
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsWarehouseDO>()
                .eqIfPresent(WmsWarehouseDO::getParentId, reqVO.getParentId())
                .likeIfPresent(WmsWarehouseDO::getName, reqVO.getName())
                .eqIfPresent(WmsWarehouseDO::getCode, reqVO.getCode())
                .eqIfPresent(WmsWarehouseDO::getAreaId, reqVO.getAreaId())
                .eqIfPresent(WmsWarehouseDO::getAddress, reqVO.getAddress())
                .eqIfPresent(WmsWarehouseDO::getPartnerId, reqVO.getPartnerId())
                .eqIfPresent(WmsWarehouseDO::getStatus, reqVO.getStatus())
                .eqIfPresent(WmsWarehouseDO::getSort, reqVO.getSort())
                .eqIfPresent(WmsWarehouseDO::getRemark, reqVO.getRemark())
                .betweenIfPresent(WmsWarehouseDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(WmsWarehouseDO::getId));
    }

    /**
     * 名称搜索专用：先按名称模糊匹配，再把每个命中节点的所有后代一并查出，
     * 合并后返回（此时不再对补全行施加 name 约束），避免树形结构断裂。
     * 由于仓库为 仓库/库区/库位 三级树，这里全量加载后在内存中展开子树，
     * 不做分页截断，确保前端 buildTree 能拿到完整父子关系。
     */
    default PageResult<WmsWarehouseDO> selectPageWithNameSubTree(WmsWarehousePageReqVO reqVO) {
        // 1. 全量加载（名称搜索场景下数据量有限，直接全量便于内存展开）
        List<WmsWarehouseDO> all = selectList(new LambdaQueryWrapperX<WmsWarehouseDO>()
                .eqIfPresent(WmsWarehouseDO::getCode, reqVO.getCode())
                .eqIfPresent(WmsWarehouseDO::getAreaId, reqVO.getAreaId())
                .eqIfPresent(WmsWarehouseDO::getAddress, reqVO.getAddress())
                .eqIfPresent(WmsWarehouseDO::getPartnerId, reqVO.getPartnerId())
                .eqIfPresent(WmsWarehouseDO::getStatus, reqVO.getStatus())
                .eqIfPresent(WmsWarehouseDO::getSort, reqVO.getSort())
                .eqIfPresent(WmsWarehouseDO::getRemark, reqVO.getRemark())
                .betweenIfPresent(WmsWarehouseDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(WmsWarehouseDO::getId));
        if (all.isEmpty()) {
            return new PageResult<>(Collections.emptyList(), 0L);
        }
        // 2. 找出名称命中的节点
        String keyword = reqVO.getName();
        Set<Long> keepIds = new HashSet<>();
        Map<Long, List<WmsWarehouseDO>> childrenMap = new HashMap<>();
        for (WmsWarehouseDO node : all) {
            childrenMap.computeIfAbsent(node.getParentId(), k -> new ArrayList<>()).add(node);
        }
        for (WmsWarehouseDO node : all) {
            if (node.getName() != null && node.getName().contains(keyword)) {
                collectSubTreeIds(node, childrenMap, keepIds);
            }
        }
        // 3. 过滤出需要保留的节点（命中节点 + 其所有下级）
        List<WmsWarehouseDO> result = all.stream()
                .filter(n -> keepIds.contains(n.getId()))
                .sorted(Comparator.comparing(WmsWarehouseDO::getId, Comparator.reverseOrder()))
                .collect(java.util.stream.Collectors.toList());
        return new PageResult<>(result, (long) result.size());
    }

    /** 收集节点自身及其所有后代的 id */
    private void collectSubTreeIds(WmsWarehouseDO node,
                                   Map<Long, List<WmsWarehouseDO>> childrenMap,
                                   Set<Long> keepIds) {
        keepIds.add(node.getId());
        List<WmsWarehouseDO> children = childrenMap.get(node.getId());
        if (children != null) {
            for (WmsWarehouseDO child : children) {
                collectSubTreeIds(child, childrenMap, keepIds);
            }
        }
    }

}
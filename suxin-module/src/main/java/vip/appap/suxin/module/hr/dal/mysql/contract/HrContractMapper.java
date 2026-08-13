package vip.appap.suxin.module.hr.dal.mysql.contract;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.module.hr.controller.admin.contract.vo.HrContractPageReqVO;
import vip.appap.suxin.module.hr.controller.admin.contract.vo.HrContractRespVO;
import vip.appap.suxin.module.hr.dal.dataobject.contract.HrContractDO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 劳动合同 Mapper
 *
 * 说明：劳动合同只写入 hr_contract 表，不双写 partner 表。
 * 查询时通过 LEFT JOIN partner 获取员工姓名。
 *
 * @author admin
 */
@Mapper
public interface HrContractMapper extends BaseMapperX<HrContractDO> {

    String SELECT_JOIN_COLUMNS =
            "SELECT c.id, c.partner_id, c.contract_no, c.party_a, c.dept, " +
            "c.start_time, c.end_time, c.sign_date_a, c.sign_date_b, " +
            "c.contract_file, c.photo, c.status, c.create_time, " +
            "p.name AS name, " +
            "d.name AS deptName " +
            "FROM hr_contract c " +
            "LEFT JOIN partner p ON p.id = c.partner_id AND p.deleted = 0 " +
            "LEFT JOIN system_dept d ON d.id = c.dept AND d.deleted = 0 ";

    String WHERE_JOIN_PAGE =
            "<where>" +
            "c.deleted = 0 " +
            "<if test='reqVO.partnerId != null'>AND c.partner_id = #{reqVO.partnerId}</if>" +
            "<if test='reqVO.name != null and reqVO.name != \"\"'>AND p.name LIKE CONCAT('%', #{reqVO.name}, '%')</if>" +
            "<if test='reqVO.contractNo != null and reqVO.contractNo != \"\"'>AND c.contract_no = #{reqVO.contractNo}</if>" +
            "<if test='reqVO.partyA != null and reqVO.partyA != \"\"'>AND c.party_a = #{reqVO.partyA}</if>" +
            "<if test='reqVO.dept != null and reqVO.dept != \"\"'>AND c.dept = #{reqVO.dept}</if>" +
            "<if test='reqVO.startTime != null and reqVO.startTime.length == 2'>AND c.start_time BETWEEN #{reqVO.startTime[0]} AND #{reqVO.startTime[1]}</if>" +
            "<if test='reqVO.endTime != null and reqVO.endTime.length == 2'>AND c.end_time BETWEEN #{reqVO.endTime[0]} AND #{reqVO.endTime[1]}</if>" +
            "<if test='reqVO.status != null and reqVO.status != \"\"'>AND c.status = #{reqVO.status}</if>" +
            "<if test='reqVO.createTime != null and reqVO.createTime.length == 2'>AND c.create_time BETWEEN #{reqVO.createTime[0]} AND #{reqVO.createTime[1]}</if>" +
            "</where>";

    @Select("<script>" + SELECT_JOIN_COLUMNS + WHERE_JOIN_PAGE + "ORDER BY c.id DESC</script>")
    IPage<HrContractRespVO> selectPageJoin(IPage<HrContractRespVO> page, @Param("reqVO") HrContractPageReqVO reqVO);

    default PageResult<HrContractRespVO> selectPage(HrContractPageReqVO reqVO) {
        IPage<HrContractRespVO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        IPage<HrContractRespVO> result = selectPageJoin(page, reqVO);
        return new PageResult<>(result.getRecords(), result.getTotal());
    }

    @Select(SELECT_JOIN_COLUMNS + "WHERE c.deleted = 0 AND c.id = #{id}")
    HrContractRespVO selectByIdJoin(@Param("id") Long id);

    default HrContractDO selectByContractNo(String contractNo) {
        return selectOne(HrContractDO::getContractNo, contractNo);
    }

}

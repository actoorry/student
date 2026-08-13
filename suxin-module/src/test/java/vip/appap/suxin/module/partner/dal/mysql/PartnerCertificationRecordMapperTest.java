package vip.appap.suxin.module.partner.dal.mysql;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.framework.tenant.core.db.TenantBaseDO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerCertificationRecordDO;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PartnerCertificationRecordMapperTest {

    @BeforeEach
    void initTableInfo() {
        // LambdaQueryWrapperX 的 lambda 列解析需要 MyBatis-Plus TableInfo 缓存；
        // 纯单元测试未启动 MyBatis-Plus，需手动初始化实体表信息。
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                PartnerCertificationRecordDO.class);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void selectMarriageVerifiedPartnerIds_limitsTypeAndStatesAndDeduplicates() {
        PartnerCertificationRecordMapper mapper = mock(PartnerCertificationRecordMapper.class, CALLS_REAL_METHODS);
        doReturn(List.of(101L, 101L, 103L)).when(mapper).selectObjs(any(Wrapper.class));

        Set<Long> result = mapper.selectMarriageVerifiedPartnerIds(
                List.of(101L, 102L, 103L), "REAL_MARRIAGE", List.of("1", "2", "3"));

        assertEquals(Set.of(101L, 103L), result);
        ArgumentCaptor<Wrapper> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(mapper).selectObjs(wrapperCaptor.capture());
        LambdaQueryWrapperX<PartnerCertificationRecordDO> wrapper =
                (LambdaQueryWrapperX<PartnerCertificationRecordDO>) wrapperCaptor.getValue();
        String sqlSegment = wrapper.getSqlSegment();
        assertTrue(sqlSegment.contains("partner_id"));
        assertTrue(sqlSegment.contains("cert_type"));
        assertTrue(sqlSegment.contains("state"));
        assertEquals("partner_id", wrapper.getSqlSelect());

        Collection<Object> parameterValues = wrapper.getParamNameValuePairs().values();
        assertTrue(parameterValues.containsAll(List.of(
                101L, 102L, 103L, "REAL_MARRIAGE", "1", "2", "3")));
    }

    @Test
    void certificationRecordParticipatesInTenantAndLogicalDeleteIsolation() throws NoSuchFieldException {
        assertTrue(TenantBaseDO.class.isAssignableFrom(PartnerCertificationRecordDO.class));
        assertNotNull(BaseDO.class.getDeclaredField("deleted").getAnnotation(TableLogic.class));
    }

}

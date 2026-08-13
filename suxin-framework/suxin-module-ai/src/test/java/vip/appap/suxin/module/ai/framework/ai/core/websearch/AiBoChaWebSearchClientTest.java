package vip.appap.suxin.module.ai.framework.ai.core.websearch;

import vip.appap.suxin.framework.common.util.json.JsonUtils;
import vip.appap.suxin.module.ai.framework.ai.core.webserch.AiWebSearchRequest;
import vip.appap.suxin.module.ai.framework.ai.core.webserch.AiWebSearchResponse;
import vip.appap.suxin.module.ai.framework.ai.core.webserch.bocha.AiBoChaWebSearchClient;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * {@link AiBoChaWebSearchClient} 集成测试类
 *
 * @author 书心软件
 */
public class AiBoChaWebSearchClientTest {

    private final AiBoChaWebSearchClient webSearchClient = new AiBoChaWebSearchClient(
            "sk-40500e52840f4d24b956d0b1d80d9abe");

    @Test
    @Disabled
    public void testSearch() {
        AiWebSearchRequest request = new AiWebSearchRequest()
                .setQuery("阿里巴巴")
                .setCount(3);
        AiWebSearchResponse response = webSearchClient.search(request);
        System.out.println(JsonUtils.toJsonPrettyString(response));
    }

}
package vip.appap.suxin.module.ai.framework.ai.config;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import vip.appap.suxin.module.ai.framework.ai.core.model.AiModelFactory;
import vip.appap.suxin.module.ai.framework.ai.core.model.AiModelFactoryImpl;
import vip.appap.suxin.module.ai.framework.ai.core.model.baichuan.BaiChuanChatModel;
import vip.appap.suxin.module.ai.framework.ai.core.model.doubao.DouBaoChatModel;
import vip.appap.suxin.module.ai.framework.ai.core.model.gemini.GeminiChatModel;
import vip.appap.suxin.module.ai.framework.ai.core.model.grok.GrokChatModel;
import vip.appap.suxin.module.ai.framework.ai.core.model.hunyuan.HunYuanChatModel;
import vip.appap.suxin.module.ai.framework.ai.core.model.midjourney.api.MidjourneyApi;
import vip.appap.suxin.module.ai.framework.ai.core.model.siliconflow.SiliconFlowApiConstants;
import vip.appap.suxin.module.ai.framework.ai.core.model.siliconflow.SiliconFlowChatModel;
import vip.appap.suxin.module.ai.framework.ai.core.model.suno.api.SunoApi;
import vip.appap.suxin.module.ai.framework.ai.core.model.xinghuo.XingHuoChatModel;
import vip.appap.suxin.module.ai.framework.ai.core.webserch.AiWebSearchClient;
import vip.appap.suxin.module.ai.framework.ai.core.webserch.bocha.AiBoChaWebSearchClient;
import vip.appap.suxin.module.ai.tool.method.PersonService;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tokenizer.JTokkitTokenCountEstimator;
import org.springframework.ai.tokenizer.TokenCountEstimator;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.vectorstore.milvus.autoconfigure.MilvusServiceClientProperties;
import org.springframework.ai.vectorstore.milvus.autoconfigure.MilvusVectorStoreProperties;
import org.springframework.ai.vectorstore.qdrant.autoconfigure.QdrantVectorStoreProperties;
import org.springframework.ai.vectorstore.redis.autoconfigure.RedisVectorStoreProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Optional;

/**
 * 书心 AI 自动配置
 *
 * @author fansili
 */
@Configuration
@EnableConfigurationProperties({ SuxinAiProperties.class,
        QdrantVectorStoreProperties.class, // 解析 Qdrant 配置
        RedisVectorStoreProperties.class, // 解析 Redis 配置
        MilvusVectorStoreProperties.class, MilvusServiceClientProperties.class // 解析 Milvus 配置
})
@Slf4j
public class AiAutoConfiguration {

    @Bean
    public AiModelFactory aiModelFactory() {
        return new AiModelFactoryImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public ObservationRegistry observationRegistry() {
        // 特殊：兜底有 ObservationRegistry Bean，避免相关的 ChatModel 创建报错。相关 issue：https://t.zsxq.com/CuPu4
        return ObservationRegistry.NOOP;
    }

    // ========== 各种 AI Client 创建 ==========

    @Bean
    @ConditionalOnProperty(value = "suxin.ai.gemini.enable", havingValue = "true")
    public GeminiChatModel geminiChatModel(SuxinAiProperties suxinAiProperties) {
        SuxinAiProperties.Gemini properties = suxinAiProperties.getGemini();
        return buildGeminiChatClient(properties);
    }

    public GeminiChatModel buildGeminiChatClient(SuxinAiProperties.Gemini properties) {
        if (StrUtil.isEmpty(properties.getModel())) {
            properties.setModel(GeminiChatModel.MODEL_DEFAULT);
        }
        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
                .openAiApi(OpenAiApi.builder()
                        .baseUrl(GeminiChatModel.BASE_URL)
                        .completionsPath(GeminiChatModel.COMPLETE_PATH)
                        .apiKey(properties.getApiKey())
                        .build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(properties.getModel())
                        .temperature(properties.getTemperature())
                        .maxTokens(properties.getMaxTokens())
                        .topP(properties.getTopP())
                        .build())
                .toolCallingManager(getToolCallingManager())
                .build();
        return new GeminiChatModel(openAiChatModel);
    }

    @Bean
    @ConditionalOnProperty(value = "suxin.ai.doubao.enable", havingValue = "true")
    public DouBaoChatModel douBaoChatClient(SuxinAiProperties suxinAiProperties) {
        SuxinAiProperties.DouBao properties = suxinAiProperties.getDoubao();
        return buildDouBaoChatClient(properties);
    }

    public DouBaoChatModel buildDouBaoChatClient(SuxinAiProperties.DouBao properties) {
        if (StrUtil.isEmpty(properties.getModel())) {
            properties.setModel(DouBaoChatModel.MODEL_DEFAULT);
        }
        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
                .openAiApi(OpenAiApi.builder()
                        .baseUrl(DouBaoChatModel.BASE_URL)
                        .completionsPath(DouBaoChatModel.COMPLETE_PATH)
                        .apiKey(properties.getApiKey())
                        .build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(properties.getModel())
                        .temperature(properties.getTemperature())
                        .maxTokens(properties.getMaxTokens())
                        .topP(properties.getTopP())
                        .build())
                .toolCallingManager(getToolCallingManager())
                .build();
        return new DouBaoChatModel(openAiChatModel);
    }

    @Bean
    @ConditionalOnProperty(value = "suxin.ai.siliconflow.enable", havingValue = "true")
    public SiliconFlowChatModel siliconFlowChatClient(SuxinAiProperties suxinAiProperties) {
        SuxinAiProperties.SiliconFlow properties = suxinAiProperties.getSiliconflow();
        return buildSiliconFlowChatClient(properties);
    }

    public SiliconFlowChatModel buildSiliconFlowChatClient(SuxinAiProperties.SiliconFlow properties) {
        if (StrUtil.isEmpty(properties.getModel())) {
            properties.setModel(SiliconFlowApiConstants.MODEL_DEFAULT);
        }
        DeepSeekChatModel openAiChatModel = DeepSeekChatModel.builder()
                .deepSeekApi(DeepSeekApi.builder()
                        .baseUrl(SiliconFlowApiConstants.DEFAULT_BASE_URL)
                        .apiKey(properties.getApiKey())
                        .build())
                .defaultOptions(DeepSeekChatOptions.builder()
                        .model(properties.getModel())
                        .temperature(properties.getTemperature())
                        .maxTokens(properties.getMaxTokens())
                        .topP(properties.getTopP())
                        .build())
                .toolCallingManager(getToolCallingManager())
                .build();
        return new SiliconFlowChatModel(openAiChatModel);
    }

    @Bean
    @ConditionalOnProperty(value = "suxin.ai.hunyuan.enable", havingValue = "true")
    public HunYuanChatModel hunYuanChatClient(SuxinAiProperties suxinAiProperties) {
        SuxinAiProperties.HunYuan properties = suxinAiProperties.getHunyuan();
        return buildHunYuanChatClient(properties);
    }

    public HunYuanChatModel buildHunYuanChatClient(SuxinAiProperties.HunYuan properties) {
        if (StrUtil.isEmpty(properties.getModel())) {
            properties.setModel(HunYuanChatModel.MODEL_DEFAULT);
        }
        // 特殊：由于混元大模型不提供 deepseek，而是通过知识引擎，所以需要区分下 URL
        if (StrUtil.isEmpty(properties.getBaseUrl())) {
            properties.setBaseUrl(
                    StrUtil.startWithIgnoreCase(properties.getModel(), "deepseek") ? HunYuanChatModel.DEEP_SEEK_BASE_URL
                            : HunYuanChatModel.BASE_URL);
        }
        // 创建 DeepSeekChatModel、HunYuanChatModel 对象
        DeepSeekChatModel openAiChatModel = DeepSeekChatModel.builder()
                .deepSeekApi(DeepSeekApi.builder()
                        .baseUrl(properties.getBaseUrl())
                        .completionsPath(HunYuanChatModel.COMPLETE_PATH)
                        .apiKey(properties.getApiKey())
                        .build())
                .defaultOptions(DeepSeekChatOptions.builder()
                        .model(properties.getModel())
                        .temperature(properties.getTemperature())
                        .maxTokens(properties.getMaxTokens())
                        .topP(properties.getTopP())
                        .build())
                .toolCallingManager(getToolCallingManager())
                .build();
        return new HunYuanChatModel(openAiChatModel);
    }

    @Bean
    @ConditionalOnProperty(value = "suxin.ai.xinghuo.enable", havingValue = "true")
    public XingHuoChatModel xingHuoChatClient(SuxinAiProperties suxinAiProperties) {
        SuxinAiProperties.XingHuo properties = suxinAiProperties.getXinghuo();
        return buildXingHuoChatClient(properties);
    }

    public XingHuoChatModel buildXingHuoChatClient(SuxinAiProperties.XingHuo properties) {
        if (StrUtil.isEmpty(properties.getModel())) {
            properties.setModel(XingHuoChatModel.MODEL_DEFAULT);
        }
        OpenAiApi.Builder builder = OpenAiApi.builder()
                .baseUrl(XingHuoChatModel.BASE_URL_V1)
                .apiKey(properties.getAppKey() + ":" + properties.getSecretKey());
        if ("x1".equals(properties.getModel())) {
            builder.baseUrl(XingHuoChatModel.BASE_URL_V2)
                    .completionsPath(XingHuoChatModel.BASE_COMPLETIONS_PATH_V2);
        }
        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
                .openAiApi(builder.build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(properties.getModel())
                        .temperature(properties.getTemperature())
                        .maxTokens(properties.getMaxTokens())
                        .topP(properties.getTopP())
                        .build())
                // TODO @芋艿：星火的 function call 有 bug，会报 ToolResponseMessage must have an id 错误！！！
                .toolCallingManager(getToolCallingManager())
                .build();
        return new XingHuoChatModel(openAiChatModel);
    }

    @Bean
    @ConditionalOnProperty(value = "suxin.ai.baichuan.enable", havingValue = "true")
    public BaiChuanChatModel baiChuanChatClient(SuxinAiProperties suxinAiProperties) {
        SuxinAiProperties.BaiChuan properties = suxinAiProperties.getBaichuan();
        return buildBaiChuanChatClient(properties);
    }

    public BaiChuanChatModel buildBaiChuanChatClient(SuxinAiProperties.BaiChuan properties) {
        if (StrUtil.isEmpty(properties.getModel())) {
            properties.setModel(BaiChuanChatModel.MODEL_DEFAULT);
        }
        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
                .openAiApi(OpenAiApi.builder()
                        .baseUrl(BaiChuanChatModel.BASE_URL)
                        .apiKey(properties.getApiKey())
                        .build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(properties.getModel())
                        .temperature(properties.getTemperature())
                        .maxTokens(properties.getMaxTokens())
                        .topP(properties.getTopP())
                        .build())
                .toolCallingManager(getToolCallingManager())
                .build();
        return new BaiChuanChatModel(openAiChatModel);
    }

    @Bean
    @ConditionalOnProperty(value = "suxin.ai.midjourney.enable", havingValue = "true")
    public MidjourneyApi midjourneyApi(SuxinAiProperties suxinAiProperties) {
        SuxinAiProperties.Midjourney config = suxinAiProperties.getMidjourney();
        return new MidjourneyApi(config.getBaseUrl(), config.getApiKey(), config.getNotifyUrl());
    }

    @Bean
    @ConditionalOnProperty(value = "suxin.ai.suno.enable", havingValue = "true")
    public SunoApi sunoApi(SuxinAiProperties suxinAiProperties) {
        return new SunoApi(suxinAiProperties.getSuno().getBaseUrl());
    }

    public ChatModel buildGrokChatClient(SuxinAiProperties.Grok properties) {
        if (StrUtil.isEmpty(properties.getModel())) {
            properties.setModel(GrokChatModel.MODEL_DEFAULT);
        }
        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
                .openAiApi(OpenAiApi.builder()
                        .baseUrl(Optional.ofNullable(properties.getBaseUrl())
                                .orElse(GrokChatModel.BASE_URL))
                        .completionsPath(GrokChatModel.COMPLETE_PATH)
                        .apiKey(properties.getApiKey())
                        .build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(properties.getModel())
                        .temperature(properties.getTemperature())
                        .maxTokens(properties.getMaxTokens())
                        .topP(properties.getTopP())
                        .build())
                .toolCallingManager(getToolCallingManager())
                .build();
        return new DouBaoChatModel(openAiChatModel);
    }

    // ========== RAG 相关 ==========

    @Bean
    public TokenCountEstimator tokenCountEstimator() {
        return new JTokkitTokenCountEstimator();
    }

    @Bean
    public BatchingStrategy batchingStrategy() {
        return new TokenCountBatchingStrategy();
    }

    private static ToolCallingManager getToolCallingManager() {
        return SpringUtil.getBean(ToolCallingManager.class);
    }

    // ========== Web Search 相关 ==========

    @Bean
    @ConditionalOnProperty(value = "suxin.ai.web-search.enable", havingValue = "true")
    public AiWebSearchClient webSearchClient(SuxinAiProperties suxinAiProperties) {
        return new AiBoChaWebSearchClient(suxinAiProperties.getWebSearch().getApiKey());
    }

    // ========== MCP 相关 ==========

    /**
     * 参考自 <a href="https://docs.spring.io/spring-ai/reference/api/mcp/mcp-client-boot-starter-docs.html">MCP Server Boot Starter</>
     */
    @Bean
    public List<ToolCallback> toolCallbacks(PersonService personService) {
        return List.of(ToolCallbacks.from(personService));
    }

}
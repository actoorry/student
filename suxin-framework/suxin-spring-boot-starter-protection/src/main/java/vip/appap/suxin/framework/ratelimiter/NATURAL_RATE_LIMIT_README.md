# 自然时间段限流使用指南

## 概述

`@NaturalRateLimit` 注解支持按自然时间段（分钟、小时、天、周、月、年）进行限流，适用于认证次数限制等需要按自然时间段统计的场景。

## 核心特性

1. **自然时间段限流**：支持按分钟、小时、天、周、月、年进行限流
2. **外部配置支持**：限流次数可通过 `application.yaml` 动态配置
3. **剩余时间提示**：超限时显示剩余等待时间
4. **用户级别限流**：默认按用户 ID 进行限流

## 使用示例

### 1. 基本用法

```java
@NaturalRateLimit(
    period = NaturalPeriod.DAY,  // 按天限流
    count = 3,                   // 每天最多 3 次
    message = "实名认证次数已达上限"
)
@PostMapping("/verify")
public CommonResult<PartnerNameCheckRespVO> nameCheck(...) {
    // 业务逻辑
}
```

### 2. 使用配置文件

```java
@NaturalRateLimit(
    period = NaturalPeriod.MONTH,
    configKey = "suxin.rate-limit.marriage.month-count",  // 从配置文件读取限流次数
    message = "婚姻认证次数已达上限"
)
@PostMapping("/verify")
public CommonResult<PartnerMarriageCheckRespVO> marriageCheck(...) {
    // 业务逻辑
}
```

### 3. 配置文件示例

```yaml
suxin:
  rate-limit:
    # 实名认证：每天 3 次
    real-name:
      period: DAY
      day-count: 3
    # 婚姻认证：每月 1 次
    marriage:
      period: MONTH
      month-count: 1
    # 其他业务...
    sms-code:
      period: HOUR
      hour-count: 10
```

## 注解参数说明

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `period` | `NaturalPeriod` | `DAY` | 限流时间段类型 |
| `count` | `int` | `10` | 限流次数（默认值） |
| `configKey` | `String` | `""` | 配置文件 key，优先从配置读取 |
| `message` | `String` | `""` | 自定义提示信息 |
| `keyResolver` | `Class<?>` | `UserNaturalRateLimiterKeyResolver` | Key 解析器 |
| `keyArg` | `String` | `""` | Key 参数 |

## NaturalPeriod 枚举值

| 枚举值 | 说明 | 示例 Key |
|--------|------|----------|
| `MINUTE` | 按分钟 | `202606101430` |
| `HOUR` | 按小时 | `2026061014` |
| `DAY` | 按天 | `20260610` |
| `WEEK` | 按周（ISO） | `202624` |
| `MONTH` | 按月 | `202606` |
| `YEAR` | 按年 | `2026` |

## 超限提示示例

当用户请求超限时，会返回类似以下提示：

```
{
  "code": 429,
  "msg": "实名认证次数已达上限，请23小时59分后重试",
  "data": null
}
```

或使用默认提示：

```
{
  "code": 429,
  "msg": "请求过于频繁，请23小时59分后重试",
  "data": null
}
```

## Redis 存储格式

限流数据存储在 Redis 中，Key 格式：

```
rate_limiter:natural:{methodKey}:{periodName}:{periodKey}
```

示例：
```
rate_limiter:natural:a1b2c3d4e5f6:DAY:20260610
```

- `methodKey`：方法签名 + 用户 ID 的 MD5 值
- `periodName`：时间段类型（MINUTE/HOUR/DAY/WEEK/MONTH/YEAR）
- `periodKey`：时间段标识（如 20260610）

## 自定义 Key Resolver

如需自定义限流维度（如按 IP、按设备等），可实现 `NaturalRateLimiterKeyResolver` 接口：

```java
@Component
public class CustomNaturalRateLimiterKeyResolver implements NaturalRateLimiterKeyResolver {
    @Override
    public String resolver(JoinPoint joinPoint, NaturalRateLimit naturalRateLimit) {
        // 自定义 Key 生成逻辑
        return "custom-key";
    }
}
```

然后在注解中指定：

```java
@NaturalRateLimit(
    keyResolver = CustomNaturalRateLimiterKeyResolver.class,
    period = NaturalPeriod.DAY,
    count = 100
)
```

## 注意事项

1. **首次请求**：Redis Key 的 TTL 会在首次请求时自动设置，确保在时间段结束后自动过期
2. **边界情况**：在时间段切换点（如 23:59:59 -> 00:00:00）的请求会正确计数到新的时间段
3. **配置优先级**：`configKey` 配置优先于 `count` 默认值
4. **线程安全**：使用 Redis 原子操作，支持高并发场景

## 与现有 @RateLimiter 的区别

| 特性 | @RateLimiter | @NaturalRateLimit |
|------|--------------|-------------------|
| 时间窗口 | 固定时间窗口（如1秒100次） | 自然时间段（每天、每月） |
| 适用场景 | 瞬时限流（防刷） | 周期限流（配额控制） |
| 外部配置 | ❌ | ✅ |
| 剩余时间提示 | ❌ | ✅ |

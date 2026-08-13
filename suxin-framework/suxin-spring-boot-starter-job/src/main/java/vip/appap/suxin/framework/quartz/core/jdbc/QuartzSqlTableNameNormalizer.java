package vip.appap.suxin.framework.quartz.core.jdbc;

import java.util.List;
import java.util.Locale;

/**
 * Quartz SQL 表名归一化器。
 *
 * Quartz 的表前缀可配置，但内置表名后缀固定为大写。该工具仅将 SQL 标识符位置中的 Quartz 固定表名
 * 归一化为书心数据库现有的小写表名，不处理字符串字面量和其他业务标识符。
 */
final class QuartzSqlTableNameNormalizer {

    private static final List<String> TABLE_NAMES = List.of(
            "PAUSED_TRIGGER_GRPS", "SCHEDULER_STATE", "SIMPLE_TRIGGERS", "SIMPROP_TRIGGERS",
            "BLOB_TRIGGERS", "CRON_TRIGGERS", "FIRED_TRIGGERS", "JOB_DETAILS", "TRIGGERS",
            "CALENDARS", "LOCKS");

    private QuartzSqlTableNameNormalizer() {
    }

    static String normalize(String sql) {
        if (sql == null || sql.isEmpty()) {
            return sql;
        }
        StringBuilder result = new StringBuilder(sql.length());
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        for (int index = 0; index < sql.length();) {
            char current = sql.charAt(index);
            if (current == '\'' && !inDoubleQuote) {
                result.append(current);
                if (inSingleQuote && index + 1 < sql.length() && sql.charAt(index + 1) == '\'') {
                    result.append('\'');
                    index += 2;
                    continue;
                }
                inSingleQuote = !inSingleQuote;
                index++;
                continue;
            }
            if (current == '"' && !inSingleQuote) {
                result.append(current);
                if (inDoubleQuote && index + 1 < sql.length() && sql.charAt(index + 1) == '"') {
                    result.append('"');
                    index += 2;
                    continue;
                }
                inDoubleQuote = !inDoubleQuote;
                index++;
                continue;
            }
            if (!inSingleQuote && !inDoubleQuote) {
                String normalizedTableName = matchQuartzTableName(sql, index);
                if (normalizedTableName != null) {
                    result.append(normalizedTableName);
                    index += normalizedTableName.length();
                    continue;
                }
            }
            result.append(current);
            index++;
        }
        return result.toString();
    }

    private static String matchQuartzTableName(String sql, int start) {
        if (start > 0 && isIdentifierCharacter(sql.charAt(start - 1))) {
            return null;
        }
        for (String tableName : TABLE_NAMES) {
            String candidate = "qrtz_" + tableName;
            int end = start + candidate.length();
            if (end <= sql.length() && sql.regionMatches(true, start, candidate, 0, candidate.length())
                    && (end == sql.length() || !isIdentifierCharacter(sql.charAt(end)))) {
                return candidate.toLowerCase(Locale.ROOT);
            }
        }
        return null;
    }

    private static boolean isIdentifierCharacter(char character) {
        return Character.isLetterOrDigit(character) || character == '_';
    }
}

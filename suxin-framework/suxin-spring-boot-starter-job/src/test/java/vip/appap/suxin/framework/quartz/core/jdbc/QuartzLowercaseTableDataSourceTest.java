package vip.appap.suxin.framework.quartz.core.jdbc;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public class QuartzLowercaseTableDataSourceTest {

    private static final List<String> TABLE_NAMES = List.of(
            "JOB_DETAILS", "TRIGGERS", "SIMPLE_TRIGGERS", "CRON_TRIGGERS", "BLOB_TRIGGERS",
            "FIRED_TRIGGERS", "CALENDARS", "PAUSED_TRIGGER_GRPS", "LOCKS", "SCHEDULER_STATE",
            "SIMPROP_TRIGGERS");

    public static void main(String[] args) throws Exception {
        normalizesEveryQuartzTableName();
        leavesNonQuartzIdentifiersUnchanged();
        rewritesPreparedAndDirectStatementSql();
        System.out.println("Quartz lowercase table SQL tests passed");
    }

    private static void normalizesEveryQuartzTableName() {
        for (String tableName : TABLE_NAMES) {
            String input = "SELECT * FROM qrtz_" + tableName + " WHERE SCHED_NAME = ?";
            String expected = "SELECT * FROM qrtz_" + tableName.toLowerCase() + " WHERE SCHED_NAME = ?";
            assertEquals(expected, QuartzSqlTableNameNormalizer.normalize(input));
        }
    }

    private static void leavesNonQuartzIdentifiersUnchanged() {
        String sql = "SELECT QRTZ_LOCKS_EXTRA FROM business_job WHERE name = 'QRTZ_LOCKS'";
        assertEquals(sql, QuartzSqlTableNameNormalizer.normalize(sql));
    }

    private static void rewritesPreparedAndDirectStatementSql() throws Exception {
        AtomicReference<String> executedSql = new AtomicReference<>();
        DataSource dataSource = new QuartzLowercaseTableDataSource(new RecordingDataSource(executedSql));
        Connection connection = dataSource.getConnection();

        connection.prepareStatement("SELECT * FROM qrtz_LOCKS WHERE LOCK_NAME = ?");
        assertEquals("SELECT * FROM qrtz_locks WHERE LOCK_NAME = ?", executedSql.get());

        connection.prepareCall("SELECT * FROM QRTZ_TRIGGERS WHERE TRIGGER_NAME = ?");
        assertEquals("SELECT * FROM qrtz_triggers WHERE TRIGGER_NAME = ?", executedSql.get());

        connection.createStatement().executeQuery("SELECT * FROM qrtz_JOB_DETAILS");
        assertEquals("SELECT * FROM qrtz_job_details", executedSql.get());

        connection.createStatement().addBatch("DELETE FROM QRTZ_FIRED_TRIGGERS WHERE ENTRY_ID = '1'");
        assertEquals("DELETE FROM qrtz_fired_triggers WHERE ENTRY_ID = '1'", executedSql.get());
    }

    private static void assertEquals(String expected, String actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected <" + expected + "> but was <" + actual + ">");
        }
    }

    private static final class RecordingDataSource implements DataSource {

        private final AtomicReference<String> executedSql;

        private RecordingDataSource(AtomicReference<String> executedSql) {
            this.executedSql = executedSql;
        }

        @Override
        public Connection getConnection() {
            return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(),
                    new Class<?>[]{Connection.class}, (proxy, method, args) -> {
                        if ((method.getName().equals("prepareStatement") || method.getName().equals("prepareCall"))
                                && args != null && args.length > 0 && args[0] instanceof String sql) {
                            executedSql.set(sql);
                            Class<?> statementType = method.getName().equals("prepareCall")
                                    ? java.sql.CallableStatement.class : PreparedStatement.class;
                            return emptyProxy(statementType);
                        }
                        if (method.getName().equals("createStatement")) {
                            return Proxy.newProxyInstance(Statement.class.getClassLoader(), new Class<?>[]{Statement.class},
                                    (statementProxy, statementMethod, statementArgs) -> {
                                        if (statementArgs != null && statementArgs.length > 0
                                                && statementArgs[0] instanceof String sql) {
                                            executedSql.set(sql);
                                        }
                                        return defaultValue(statementMethod.getReturnType());
                                    });
                        }
                        return defaultValue(method.getReturnType());
                    });
        }

        @Override
        public Connection getConnection(String username, String password) {
            return getConnection();
        }

        private static Object emptyProxy(Class<?> type) {
            return Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                    (proxy, method, args) -> defaultValue(method.getReturnType()));
        }

        private static Object defaultValue(Class<?> type) {
            if (!type.isPrimitive()) {
                return null;
            }
            if (type == boolean.class) {
                return false;
            }
            if (type == byte.class) {
                return (byte) 0;
            }
            if (type == short.class) {
                return (short) 0;
            }
            if (type == int.class) {
                return 0;
            }
            if (type == long.class) {
                return 0L;
            }
            if (type == float.class) {
                return 0F;
            }
            if (type == double.class) {
                return 0D;
            }
            if (type == char.class) {
                return '\0';
            }
            return null;
        }

        @Override public PrintWriter getLogWriter() { return null; }
        @Override public void setLogWriter(PrintWriter out) { }
        @Override public void setLoginTimeout(int seconds) { }
        @Override public int getLoginTimeout() { return 0; }
        @Override public Logger getParentLogger() { return Logger.getGlobal(); }
        @Override public <T> T unwrap(Class<T> iface) throws SQLException { throw new SQLException("Not a wrapper"); }
        @Override public boolean isWrapperFor(Class<?> iface) { return false; }
    }
}

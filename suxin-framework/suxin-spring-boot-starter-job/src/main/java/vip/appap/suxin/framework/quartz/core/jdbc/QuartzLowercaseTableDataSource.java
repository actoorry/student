package vip.appap.suxin.framework.quartz.core.jdbc;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.logging.Logger;

/**
 * 仅供 Quartz 使用的 DataSource 包装器，将 Quartz SQL 中的固定表名归一化为小写。
 */
public final class QuartzLowercaseTableDataSource implements DataSource {

    private final DataSource delegate;

    public QuartzLowercaseTableDataSource(DataSource delegate) {
        this.delegate = delegate;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return wrapConnection(delegate.getConnection());
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return wrapConnection(delegate.getConnection(username, password));
    }

    private static Connection wrapConnection(Connection connection) {
        return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class<?>[]{Connection.class},
                (proxy, method, args) -> invokeConnection(connection, method, args));
    }

    private static Object invokeConnection(Connection connection, Method method, Object[] args) throws Throwable {
        Object[] invocationArgs = normalizeFirstSqlArgument(args,
                method.getName().equals("prepareStatement") || method.getName().equals("prepareCall")
                        || method.getName().equals("nativeSQL"));
        Object result = invoke(connection, method, invocationArgs);
        if (method.getName().equals("createStatement") && result instanceof Statement statement) {
            return wrapStatement(statement);
        }
        return result;
    }

    private static Statement wrapStatement(Statement statement) {
        return (Statement) Proxy.newProxyInstance(Statement.class.getClassLoader(), new Class<?>[]{Statement.class},
                (proxy, method, args) -> {
                    boolean acceptsSql = method.getName().startsWith("execute") || method.getName().equals("addBatch");
                    return invoke(statement, method, normalizeFirstSqlArgument(args, acceptsSql));
                });
    }

    private static Object[] normalizeFirstSqlArgument(Object[] args, boolean acceptsSql) {
        if (!acceptsSql || args == null || args.length == 0 || !(args[0] instanceof String sql)) {
            return args;
        }
        Object[] invocationArgs = args.clone();
        invocationArgs[0] = QuartzSqlTableNameNormalizer.normalize(sql);
        return invocationArgs;
    }

    private static Object invoke(Object target, Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException exception) {
            throw exception.getTargetException();
        }
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return delegate.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        delegate.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        delegate.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return delegate.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return delegate.getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return iface.cast(this);
        }
        return delegate.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isInstance(this) || delegate.isWrapperFor(iface);
    }
}

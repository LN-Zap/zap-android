package zapsolutions.zap.connection;

/**
 * Base class meant to be extended for parsing various connection strings like
 * - BTCPay Configuration
 * - LNConnect Configuration
 *
 * @param <T> type of config to be parsed
 */
public abstract class BaseConnectionParser<T> {

    protected int mError = -1;
    protected String mConnectionString;

    private T mConnectionConfig;

    public BaseConnectionParser(String connectionString) {
        mConnectionString = connectionString;
    }

    public T getConnectionConfig() {
        return mConnectionConfig;
    }

    protected void setConnectionConfig(T connectionConfig) {
        mConnectionConfig = connectionConfig;
    }

    public boolean hasError() {
        return mError > -1;
    }

    public int getError() {
        return mError;
    }
}

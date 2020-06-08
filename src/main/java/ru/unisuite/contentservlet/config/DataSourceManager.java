package ru.unisuite.contentservlet.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

class DataSourceManager {
    private static final Logger logger = LoggerFactory.getLogger(DataSourceManager.class);

    private DataSourceManager() {
    }


    static DataSource getDataSource(ContentServletProperties prop) {
        try {
            if (prop.getDatasourceJndiName() != null) {
                return lookup(prop.getDatasourceJndiName());
            } else {
                String datasourceUrl = prop.getDatasourceUrl();
                String datasourceUsername = prop.getDatasourceUsername();
                String datasourcePassword = prop.getDatasourcePassword();
                return createDataSource(datasourceUrl, datasourceUsername, datasourcePassword);
            }
        } catch (Exception e) {
            logger.error("Unable to configure jdbc dataSource", e);
            throw new RuntimeException("Unable to configure jdbc dataSource", e);
        }
    }


    private static DataSource lookup(String jndiName) {
        try {
            return lookupDataSourceInternal(jndiName, false);
        } catch (NamingException e1) {
            try {
                return lookupDataSourceInternal(jndiName, true);
            } catch (NamingException e2) {
                String errorMessage = "Unable to lookup datasource by JNDI name '" + jndiName + '\'';
                throw new RuntimeException(errorMessage, e1);
            }
        }
    }

    /**
     * Tomcat wants data source jndi named prepended with "java:/comp/env/" but WebLogic doesn't. We try both.
     */
    private static DataSource lookupDataSourceInternal(String jndiName, boolean prependWithJavaCompEnv) throws NamingException {
        Context initialContext = null;
        try {
            initialContext = new InitialContext();
            return (DataSource) initialContext.lookup((prependWithJavaCompEnv ? "java:/comp/env/" : "") + jndiName);
        } finally {
            if (initialContext != null) {
                try {
                    initialContext.close();
                } catch (NamingException e) {
                    logger.warn("InitialContext wasn't closed", e);
                }
            }
        }
    }

    private static DataSource createDataSource(String propertyFileName) {
        return createHikariConnectionPool(propertyFileName);
    }

    private static DataSource createDataSource(String url, String username, String password) {
        return createHikariConnectionPool(url, username, password);
    }

    private static DataSource createHikariConnectionPool(String url, String username, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        return new HikariDataSource(config);
    }

    private static DataSource createHikariConnectionPool(String propertyFileName) {
        HikariConfig config = new HikariConfig(propertyFileName);
        return new HikariDataSource(config);
    }
}

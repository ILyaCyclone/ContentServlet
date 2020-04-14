package ru.unisuite.contentservlet.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

class DataSourceLookup {
    private static final Logger logger = LoggerFactory.getLogger(DataSourceLookup.class);

    public DataSource lookup(String jndiName) {
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
    private DataSource lookupDataSourceInternal(String jndiName, boolean prependWithJavaCompEnv) throws NamingException {
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
}

package ru.miit.databasereader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.sql.rowset.serial.SerialBlob;

import org.apache.commons.io.FileUtils;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleResultSet;
import ru.miit.cache.Cache;

public class EmulationDatabaseReader implements DatabaseReader {

	public String getCodeDataEmul(final String webMetaId) throws SQLException, NamingException {
		String sqlQuery = "select wpms_cm_wp.get_ContentURL(cv.id_web_metaterm, null, 4) rStr\n"
				+ "from content_version_wp cv where cv.id_web_metaterm = :p1 ";
		String body = "";

		try (OracleConnection connection = getOracleConnection();
				OraclePreparedStatement preparedStatement = (OraclePreparedStatement) connection
						.prepareStatement(sqlQuery)) {

			setParameterInt(preparedStatement, "p1", String.valueOf(webMetaId));

			try (OracleResultSet resultSet = (OracleResultSet) preparedStatement.executeQuery()) {
				resultSet.next();
				body = resultSet.getString(1);
			}

		}
		return body;
	}

	public void getResTestListDataEmul(PrintWriter out, HttpServletResponse resp) // Нужен ли тут response?
			throws SQLException, NamingException, Exception {
		String sqlQuery = "select wpms_cm_wp.get_ContentURL(t.id_web_metaterm, null, 2) cnt\n"
				+ "from actual_content_version_wp t\n";
		String body = "";

		OracleConnection connection = getOracleConnection();
		try {
			OraclePreparedStatement preparedStatement = (OraclePreparedStatement) connection.prepareStatement(sqlQuery);
			try {

				OracleResultSet resultSet = (OracleResultSet) preparedStatement.executeQuery();
				try {
					while (resultSet.next()) {
						body = resultSet.getString(1); // Возвращается одна строка или несколько?
						if (body != null) {
							out.println("<p>" + body + "</p>");
						}
					}
				} finally {
					resultSet.close();
				}
			} finally {
				preparedStatement.close();
			}
		} finally {
			connection.close();
		}
	}
	
	private static final String DATASOURCE_NAME = "jdbc/ds_basic";

	public Context createContext() throws NamingException {
		Context ic = new InitialContext();
		return ic;
	}

	public OracleConnection getOracleConnection() throws NamingException, SQLException {

		Context context = new InitialContext();
		DataSource dataSource = (DataSource) context.lookup(DATASOURCE_NAME);
		try (OracleConnection connection = (OracleConnection) dataSource.getConnection()) {

			return connection;
		}
	}

	public Blob getBlob() {

		byte[] fileContent = null;
		String filePath = "C:\\Users\\romanov\\Desktop\\cache\\200kb.jpg";
		try {
			fileContent = FileUtils.readFileToByteArray(new File(filePath));
		} catch (IOException e) {
			e.printStackTrace();
		}

		Blob blob = null;
		try {
			blob = new SerialBlob(fileContent);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return blob;
	}

	public static void writeToStreamEmul(final Blob blobData, OutputStream out) {

		int length;
		int bufSize = 4096; // bdata.getBufferSize();
		byte buffer[] = new byte[bufSize];
		try (InputStream img = blobData.getBinaryStream()) {
			while ((length = img.read(buffer, 0, bufSize)) != -1) {
				out.write(buffer, 0, length);
				out.flush();
			}
			try {
				out.flush();
			} catch (Throwable e) {

			}
		} catch (IOException | SQLException e1) {
			e1.printStackTrace();
		}

	}

	public static void setParameterIntEmul(OraclePreparedStatement APs, String APName, String APValue) throws SQLException {
		if (APValue == "") {
			APs.setNullAtName(APName, 0);
		} else {
			int iP;
			try {
				iP = Integer.parseInt(APValue);
			} catch (NumberFormatException e) {
				throw new NumberFormatException("РћС€РёР±РєР° РїСЂРёРІРµРґРµРЅРёСЏ Р·РЅР°С‡РµРЅРёСЏ - '" + APValue
						+ "' РїР°СЂР°РјРµС‚СЂР° - '" + APName + "'  Рє С†РµР»РѕРјСѓ. ");
			}
			APs.setIntAtName(APName, iP);
		}
	}

	public static void setParameterStrEmul(OraclePreparedStatement preparedStatement, String APName, String APValue)
			throws SQLException {
		if (APValue == "") {
			preparedStatement.setNullAtName(APName, 0);
		} else {
			preparedStatement.setStringAtName(APName, APValue);
		}
	}

	@Override
	public OracleConnection getConnection() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void getResTestListData(PrintWriter out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeToStream(Blob blobData, OutputStream out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeToTwoStreams(Blob blobData, OutputStream os, FileOutputStream cacheOs)
			throws OracleDatabaseReaderServletOSException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getCodeData(int webMetaId) throws OracleDatabaseReaderException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void getBinaryDataByMetaId(Map<String, Object> queryParameters, OutputStream osServlet,
			HttpServletResponse response, Cache cache, boolean cacheIsUp, String idInCache)
			throws OracleDatabaseReaderException, OracleDatabaseReaderServletOSException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getBinaryDataByFileVersionId(Map<String, Object> queryParameters, OutputStream osServlet,
			HttpServletResponse response, Cache cache, boolean cacheIsUp, String idInCache)
			throws OracleDatabaseReaderException, OracleDatabaseReaderServletOSException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getBinaryDataByClientId(Map<String, Object> queryParameters, OutputStream osServlet,
			HttpServletResponse response, Cache cache, boolean cacheIsUp, String idInCache)
			throws OracleDatabaseReaderException, OracleDatabaseReaderServletOSException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setParameterInt(OraclePreparedStatement preparedStatement, String filed, Object value)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setParameterStr(OraclePreparedStatement preparedStatement, String filed, Object value)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fetchDataFromResultSet(ResultSet resultSet, OutputStream osServlet, HttpServletResponse response,
			Cache cache, boolean cacheIsUp, String idInCache)
			throws SQLException, OracleDatabaseReaderServletOSException, OracleDatabaseReaderException {
		// TODO Auto-generated method stub
		
	}


}

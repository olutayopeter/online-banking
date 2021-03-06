package com.expertedge.uba.collection.engine.beans;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.derby.client.am.Types;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import com.expertedge.uba.collection.engine.connect24.Connect24Helper;
import com.expertedge.uba.collection.engine.dao.ConfigurationInfoDAO;
import com.expertedge.uba.collection.engine.jpa.ConfigurationInfo;
import com.expertedge.uba.collection.engine.jpa.EntityManagerHelper;
import com.expertedge.uba.collection.engine.jpa.FeeTable;
import com.expertedge.uba.collection.engine.jpa.FeesMnt;
import com.expertedge.uba.collection.engine.jpa.TransactionStatus;
import com.expertedge.uba.collection.engine.util.ChannelTools;
import com.expertedge.uba.collection.engine.util.DSConfig2;
import com.expertedge.uba.collection.engine.util.FacesUtils;
import com.expertedge.uba.collection.engine.util.Nought;
import com.expertedge.uba.collection.engine.util.SystemUUIDGenerator;
import com.expertedge.uba.collection.engine.util.dao.Dao;
import com.expertedge.uba.collection.engine.util.dao.JpaDao;

/**
 *
 * @author <a href="mailto:jniyiooster@gmail.com">Olaniyi Osunsanya</a>
 */
@Scope("session")
@Controller
public class FeesMntUploadDetail {

	private final int START_ROW = 0;
	private FeesMnt feesMnt;
	private File uploadFile;
	private UploadedFile uploadedFile;
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static List<FeesMnt> listFeesMnt;
	private static Map<String, Connection> mainConnectionMap;
	private static Map<String, PreparedStatement> mainStatementMap;

	ConfigurationInfoDAO configurationInfoDao = new ConfigurationInfoDAO();
	ConfigurationInfo configurationInfo;
	List<ConfigurationInfo> configurationInfos;

	public FeesMntUploadDetail() {
		mainConnectionMap = new HashMap<String, Connection>();
		mainStatementMap = new HashMap<String, PreparedStatement>();
	}

	public void uploadFileAction(FileUploadEvent event){
		System.out.println("##### File Saved #####");
        String componentStatus = "";
        UploadedFile inputfile = event.getFile();
        // file has been saved
        componentStatus = "file_saved";
		try {
//			uploadFile = inputfile.getFileName()
			FacesUtils.addInfoMessage(componentStatus);
			throw new RuntimeException("##### File Saved #####");
		} catch (Exception e) {
        	componentStatus = "file_invalid";
			e.printStackTrace();
			FacesUtils.addErrorMessage(event.getComponent().getClientId(FacesContext.getCurrentInstance()), componentStatus, null);
		}
	}
	public void handleFileUpload(FileUploadEvent event) {
        FacesMessage msg = new FacesMessage("Succesful", event.getFile().getFileName() + " is uploaded.");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

	@RequiresPermissions("*:create")
	public String submit(){
		if(configurationInfo == null){
			FacesUtils.addErrorMessage(null,"blank_field",new Object[]{"Product Configuration Code"});
			return null;
		}
		if(uploadedFile == null){
			FacesUtils.addErrorMessage(null,"no_uploaded_file",new Object[]{""});
			return null;
		}
		boolean hasError = false;
       if(uploadedFile != null) {
    		String tmpdir = SystemUtils.JAVA_IO_TMPDIR;
    		uploadFile = new File(tmpdir, uploadedFile.getFileName());
    		FacesUtils.addInfoMessage("fee_mnt_upload_success");
    		System.out.println("##### File Saved " + uploadFile.getPath() + " #####");

    		try {
				// allocate the stream ... only for example
				final InputStream input = uploadedFile.getInputstream();
				final OutputStream output = new FileOutputStream(uploadFile.getPath());
				// get an channel from the stream
				final ReadableByteChannel inputChannel = Channels.newChannel(input);
				final WritableByteChannel outputChannel = Channels.newChannel(output);
				// copy the channels
				ChannelTools.fastChannelCopy(inputChannel, outputChannel);
				// closing the channels
				inputChannel.close();
				outputChannel.close();
			} catch (FileNotFoundException e) {
				hasError = true;
				e.printStackTrace();
			} catch (IOException e) {
				hasError = true;
				e.printStackTrace();
			}

    		processFile(uploadFile);

        }

       if(hasError){
   		FacesUtils.addErrorMessage("fee_mnt_upload_error");
       }

		return null;
	}

	private Workbook getInfoFromExcel(File file){
		Workbook wk = null;
		try {
			wk = WorkbookFactory.create(file);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return wk;
	}

	private void processFile(File file){
    	final int RECORD_ID = 0;
    	final int CHARGE_CATEGORY = 1;
    	final int DR_ACCOUNT      = 2;
    	final int DR_CURRENCY     = 3;
    	final int DR_SOL_ID       = 4;
    	final int CR_ACCOUNT      = 5;
    	final int CR_CURRENCY     = 6;
    	final int CR_SOL_ID       = 7;
    	final int VAT_ACCOUNT     = 8;
    	final int VAT_CURRENCY    = 9;
    	final int VAT_AMOUNT      = 10;
    	final int PURPOSE         = 11;
    	final int TRAN_AMOUNT     = 12;
    	final int TRAN_CURRENCY   = 13;
    	final int TRAN_DATE       = 14;
    	final int TRAN_PARTICULAR = 15;
    	final int VALUE_DATE      = 16;
    	final int COUNTRY_CODE    = 17;
    	final int BATCH_ID 		  = 18;

		Workbook wk = getInfoFromExcel(file);
		if(null == wk){
			return;
		}

		Sheet sh = wk.getSheetAt(0);
		int lineCount = 0;
		int[] actualLenghts = getActualLengths(sh);
		int rLength = actualLenghts[0];
		int cLength = actualLenghts[1];

		Row row = sh.getRow(0);

		listFeesMnt = new ArrayList<FeesMnt>();

		EntityManagerFactory emf = Persistence.createEntityManagerFactory("main", EntityManagerHelper.getMap());
		EntityManager em = emf.createEntityManager();
		Dao feeMntDao = new JpaDao<String, com.expertedge.uba.collection.engine.jpa.FeesMnt>(){};
		feeMntDao.reset(em);

		boolean ignoredEntries = false;
		String cvalue = "";
		Calendar cal = null;
		Date dt = null;
		Double dvalue = 0d;
		long lastFeesMntId = getNextFeesMntId();
		long lastFeeTableId = getNextFeeTableId();
//		String batchNumber = SystemUUIDGenerator.getUUID().concat("-").concat(Thread.currentThread().getName().toUpperCase());

//		beginTransaction(em);
		Connection conn = null;

		conn = getMainConnection();
		PreparedStatement stmt1 = null;
		PreparedStatement stmt2 = null;

		int recordCount = 0;
		for (lineCount = START_ROW + 1; lineCount < rLength; lineCount++) {

			row = sh.getRow(lineCount);
			recordCount++;

			if(null == row)continue;

			FeesMnt feesMnt = new FeesMnt();

			// read each field in the row
			// Don't allow to go beyond the column length calculated above.
			int icolumnLength = row.getLastCellNum();
			if(icolumnLength > cLength){
				icolumnLength = cLength;
			}
			for (int count = 0; count < icolumnLength; ++count) {
				cvalue = null;
				dvalue = 0d;
				switch (count) {
				case RECORD_ID:
					try {
						dvalue = row.getCell(count).getNumericCellValue();
					} catch (Exception e) {
//						e.printStackTrace();
					}
					if(false == Nought.isNought(dvalue)){
						feesMnt.setRecordId(dvalue.longValue());
					}

					break;
				case CHARGE_CATEGORY:
					try {
						cvalue = row.getCell(count).getStringCellValue();
					} catch (Exception e) {
//						e.printStackTrace();
					}
					if(false == Nought.isNought(cvalue)){
						feesMnt.setChargeCategory(cvalue);
					}

					break;
				case DR_ACCOUNT:
					try {
						cvalue = row.getCell(count).getStringCellValue();
					} catch (Exception e) {
//						e.printStackTrace();
					}
					if(false == Nought.isNought(cvalue)){
						feesMnt.setDrAccount(cvalue);
					}

					break;
				case DR_CURRENCY:
					try {
						cvalue = row.getCell(count).getStringCellValue();
					} catch (Exception e) {
//						e.printStackTrace();
					}
					if(false == Nought.isNought(cvalue)){
						feesMnt.setDrCurrency(cvalue);
					}

					break;
				case DR_SOL_ID:
					try {
						cvalue = row.getCell(count).getStringCellValue();
					} catch (Exception e) {
//						e.printStackTrace();
					}
					if(false == Nought.isNought(cvalue)){
						feesMnt.setDrSolId(cvalue);
					}

					break;
				case CR_ACCOUNT:
					try {
						cvalue = row.getCell(count).getStringCellValue();
					} catch (Exception e) {
//						e.printStackTrace();
					}
					if(false == Nought.isNought(cvalue)){
						feesMnt.setCrAccount(cvalue);
					}

					break;
				case CR_CURRENCY:
					try {
						cvalue = row.getCell(count).getStringCellValue();
					} catch (Exception e) {
//						e.printStackTrace();
					}
					if(false == Nought.isNought(cvalue)){
						feesMnt.setCrCurrency(cvalue);
					}

					break;
				case CR_SOL_ID:
					try {
						cvalue = row.getCell(count).getStringCellValue();
					} catch (Exception e) {
//						e.printStackTrace();
					}
					if(false == Nought.isNought(cvalue)){
						feesMnt.setCrSolId(cvalue);
					}

					break;
				case VAT_ACCOUNT:
					try {
						cvalue = row.getCell(count).getStringCellValue();
					} catch (Exception e) {
//						e.printStackTrace();
					}
					if(false == Nought.isNought(cvalue)){
						feesMnt.setVatAccount(cvalue);
					}

					break;
				case VAT_CURRENCY:
					try {
						cvalue = row.getCell(count).getStringCellValue();
					} catch (Exception e) {
//						e.printStackTrace();
					}
					if(false == Nought.isNought(cvalue)){
						feesMnt.setVatCurrency(cvalue);
					}

					break;
				case VAT_AMOUNT:
					try {
						cvalue = "" + row.getCell(count).getNumericCellValue();
						dvalue = Double.parseDouble(cvalue);
					} catch (Exception e) {
//						e.printStackTrace();
					}
					if(false == Nought.isNought(cvalue)){
						feesMnt.setVatAmount(dvalue);
					}

					break;
				case PURPOSE:
					try {
						cvalue = row.getCell(count).getStringCellValue();
					} catch (Exception e) {
//						e.printStackTrace();
					}
					if(false == Nought.isNought(cvalue)){
						feesMnt.setPurpose(cvalue);
					}

					break;
				case TRAN_AMOUNT:
					try {
						cvalue = "" + row.getCell(count).getNumericCellValue();
						dvalue = Double.parseDouble(cvalue);
					} catch (Exception e) {
//						e.printStackTrace();
					}
					if(false == Nought.isNought(cvalue)){
						feesMnt.setTranAmount(dvalue);
					}

					break;
				case TRAN_CURRENCY:
					try {
						cvalue = row.getCell(count).getStringCellValue();
					} catch (Exception e) {
//						e.printStackTrace();
					}
					if(false == Nought.isNought(cvalue)){
						feesMnt.setTranCurrency(cvalue);
					}

					break;
				case TRAN_DATE:
					cal = null;
					dt = null;
					try {
						cvalue = getCellTextValue(sh, lineCount, count);
					} catch (Exception e) {
//						e.printStackTrace();
					}
					if(false == Nought.isNought(cvalue)){
						try {
							dt = dateFormat.parse(cvalue);
							cal = Calendar.getInstance();
							cal.setTime(dt);
						} catch (Exception e) {
							e.printStackTrace();
						}
						feesMnt.setTranDate(cal);
					}

					break;
				case TRAN_PARTICULAR:
					try {
						cvalue = row.getCell(count).getStringCellValue();
					} catch (Exception e) {
//						e.printStackTrace();
					}
					if(false == Nought.isNought(cvalue)){
						feesMnt.setTranParticular(cvalue);
					}

					break;
				case VALUE_DATE:
					cal = null;
					dt = null;
					try {
						cvalue = getCellTextValue(sh, lineCount, count);
					} catch (Exception e) {
//						e.printStackTrace();
					}
					if(false == Nought.isNought(cvalue)){
						try {
							dt = dateFormat.parse(cvalue);
							cal = Calendar.getInstance();
							cal.setTime(dt);
						} catch (Exception e) {
							e.printStackTrace();
						}
						feesMnt.setValueDate(cal);
					}

					break;
				case COUNTRY_CODE:
					try {
						cvalue = row.getCell(count).getStringCellValue();
					} catch (Exception e) {
//						e.printStackTrace();
					}
					if(false == Nought.isNought(cvalue)){
						feesMnt.setCountryCode(cvalue);
					}

					break;
				case BATCH_ID:
					try {
						cvalue = row.getCell(count).getStringCellValue();
					} catch (Exception e) {
//						e.printStackTrace();
					}
					if(false == Nought.isNought(cvalue)){
						feesMnt.setBatchId(cvalue);
					}

					break;
				default:
				}
			}


			// Set the conf code for FeesMnt
			feesMnt.setConfCode(configurationInfo.getConfCode());
			// Set the Posting Status for FeesMnt
			feesMnt.setPostingStatus(TransactionStatus.UNPROCESSED.toString());
			feesMnt.setPostingStatus(TransactionStatus.UNPROCESSED.toString());

			feesMnt.setId(lastFeesMntId);
			lastFeesMntId = lastFeesMntId + 1;

			// Check for duplicate entries
//			String uniqueRecordKey = feesMnt.getRecordId() + "-" + feesMnt.getCountryCode() + "-" + feesMnt.getConfCode();

			Map map = new HashMap();

			map.put("recordId", feesMnt.getRecordId());
			map.put("countryCode", feesMnt.getCountryCode());
			map.put("confCode", feesMnt.getConfCode());

			List prevEntries = feeMntDao.findByProperties(map);

			if(null != prevEntries && prevEntries.isEmpty() == false){
				if(false == ignoredEntries){
					FacesUtils.addErrorMessage("duplicate_entries");
					ignoredEntries = true;
				}
				continue;
			}

//			feeMntDao.save(feesMnt);
//			listFeesMnt.add(feesMnt);

//			if(0 == (recordCount % 1000)){
//				commitTransaction(em);
//				beginTransaction(em);
//			}
			stmt1 = insertIntoFeesMntSQL(feesMnt);
			stmt2 = insertIntoFeeTableSQL(getFeeTable(feesMnt), lastFeeTableId);
			lastFeeTableId = lastFeeTableId + 1;
		}
//		commitTransaction(em);
//		copyToFeeTable(listFeesMnt);
		try {
			int [] updateCounts1 = stmt1.executeBatch();
			int [] updateCounts2 = stmt2.executeBatch();
			conn.commit();
			conn.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void beginTransaction(EntityManager em){
		if(null != em){
			try {
				if(em.getTransaction().isActive() == false)
					em.getTransaction().begin();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	private void commitTransaction(EntityManager em){
		if(null != em){
			try {
				if(em.getTransaction().isActive())
					em.getTransaction().commit();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	private static PreparedStatement insertIntoFeesMntSQL(FeesMnt feesMnt) {

		ResultSet resultset = null;
		PreparedStatement stmt = null;
		Connection conn = null;

		conn = getMainConnection();

		String insertSQL =
				"INSERT INTO FEES_MNT (       "+
				"	ID,                                "+
				"	BATCH_ID,                                "+
				"	CHARGE_CATEGORY,                         "+
				"	DR_ACCOUNT,                              "+
				"	DR_CURRENCY,                             "+
				"	DR_SOL_ID,                               "+
				"	ERR_CODE,                                "+
				"	POSTED_BY,                               "+
				"	POSTED_DATE,                             "+
				"	POSTING_STATUS,                          "+
				"	PURPOSE,                                 "+
				"	RECORD_ID,                               "+
				"	STAN,                                    "+
				"	TRAN_AMOUNT,                             "+
				"	TRAN_CURRENCY,                           "+
				"	TRAN_DATE,                               "+
				"	TRAN_PARTICULAR,                         "+
				"	UPLOADED_BY,                             "+
				"	UPLOADED_DATE,                           "+
				"	VALUE_DATE,                              "+
				"	VAT_AMOUNT,                              "+
				"	COUNTRY_CODE,                            "+
				"	CONF_CODE,                               "+
				"	CR_ACCOUNT,                              "+
				"	CR_CURRENCY,                             "+
				"	CR_SOL_ID,                               "+
				"	VAT_ACCOUNT,                             "+
				"	VAT_CURRENCY                             "+
				"	)                                    "+
				"                                        "+
				"VALUES (                                "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?                                    "+
				"	)                                    ";

		try {
			conn.setAutoCommit(false);
			stmt = getMainStatement (insertSQL);

			try {
				stmt.setLong  (1 , feesMnt.getId            ());
			} catch (Exception e3) {
//				e3.printStackTrace();
			}
			stmt.setString(2 , feesMnt.getBatchId            ());
			stmt.setString(3 , feesMnt.getChargeCategory     ());
			stmt.setString(4 , feesMnt.getDrAccount          ());
			stmt.setString(5 , feesMnt.getDrCurrency         ());
			stmt.setString(6 , feesMnt.getDrSolId        	());
			stmt.setString(7 , feesMnt.getErrCode            ());
			stmt.setString(8 , feesMnt.getPostedBy           ());
			try {
				stmt.setTimestamp(9 , new java.sql.Timestamp(feesMnt.getPostedDate         ().getTime().getTime()));
			} catch (Exception e) {
//				e.printStackTrace();
				stmt.setNull(9, Types.TIMESTAMP);
			}
			stmt.setString(10 , feesMnt.getPostingStatus      ());
			stmt.setString(11, feesMnt.getPurpose            ());
			try {
				stmt.setLong(12, feesMnt.getRecordId           ());
			} catch (Exception e2) {
//				e2.printStackTrace();
				stmt.setNull(12, Types.BIGINT);
			}
			stmt.setString(13, feesMnt.getStan               ());
			try {
				stmt.setDouble(14, feesMnt.getTranAmount         ());
			} catch (Exception e1) {
//				e1.printStackTrace();
				stmt.setNull(14, Types.DOUBLE);
			}
			stmt.setString(15, feesMnt.getTranCurrency       ());
			try {
				stmt.setTimestamp(16, new java.sql.Timestamp(feesMnt.getTranDate           ().getTime().getTime()));
			} catch (Exception e) {
//				e.printStackTrace();
				stmt.setNull(16, Types.TIMESTAMP);
			}
			stmt.setString(17, feesMnt.getTranParticular     ());
			stmt.setString(18, feesMnt.getUploadedBy         ());
			try {
				stmt.setTimestamp(19, new java.sql.Timestamp(feesMnt.getUploadedDate       ().getTime().getTime()));
			} catch (Exception e) {
//				e.printStackTrace();
				stmt.setNull(19, Types.TIMESTAMP);
			}
			try {
				stmt.setTimestamp(20, new java.sql.Timestamp(feesMnt.getValueDate          ().getTime().getTime()));
			} catch (Exception e) {
//				e.printStackTrace();
				stmt.setNull(20, Types.TIMESTAMP);
			}
			try {
				stmt.setDouble(21, feesMnt.getVatAmount          ());
			} catch (Exception e) {
//				e.printStackTrace();
				stmt.setNull(21, Types.DOUBLE);
			}
			stmt.setString(22, feesMnt.getCountryCode        ());
			stmt.setString(23, feesMnt.getConfCode           ());
			stmt.setString(24, feesMnt.getCrAccount          ());
			stmt.setString(25, feesMnt.getCrCurrency         ());
			stmt.setString(26, feesMnt.getCrSolId        	());
			stmt.setString(27, feesMnt.getVatAccount         ());
			stmt.setString(28, feesMnt.getVatCurrency        ());

//			stmt.executeUpdate();
			stmt.addBatch();
//			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally{
			if(null != resultset){
				try {
					resultset.close();
				} catch (SQLException e) {
				}
			}
		}
		return stmt;
	}

	private static PreparedStatement insertIntoFeeTableSQL(FeeTable feeTable, long feeTableId) {

		ResultSet resultset = null;
		PreparedStatement stmt = null;
		Connection conn = null;

		conn = getMainConnection();

		String insertSQL =
				"INSERT INTO FEE_TABLE (       "+
				"	ID,                                            "+
				"	BATCH_ID,                                      "+
				"	CHARGE_CATEGORY,                               "+
				"	CR_ACCOUNT,                                    "+
				"	CR_CURRENCY,                                   "+
				"	CR_SOL_ID,                                     "+
				"	DR_ACCOUNT,                                    "+
				"	DR_CURRENCY,                                   "+
				"	DR_SOL_ID,                                     "+
				"	ERR_CODE,                                      "+
				"	LAST_RESPONSE,                                 "+
				"	NUMBER_OF_TRIES,                               "+
				"	POSTED_BY,                                     "+
				"	POSTED_DATE,                                   "+
				"	POSTING_STATUS,                                "+
				"	PROCESS_DATE,                                  "+
				"	PURPOSE,                                       "+
				"	RECORD_ID,                                     "+
				"	REMARKS,                                       "+
				"	STAN,                                          "+
				"	STATUS_FLG,                                    "+
				"	TRAN_AMOUNT,                                   "+
				"	TRAN_CURRENCY,                                 "+
				"	TRAN_DATE,                                     "+
				"	TRAN_PARTICULAR,                               "+
				"	UPLOADED_BY,                                   "+
				"	UPLOADED_DATE,                                 "+
				"	VALUE_DATE,                                    "+
				"	VAT_AMOUNT,                                    "+
				"	PRODUCT,                                       "+
				"	COUNTRY_CODE,                                  "+
				"	CONF_CODE,                                     "+
				"	VAT_ACCOUNT,                                   "+
				"	VAT_CURRENCY,                                  "+
				"	HAS_SPECIAL_ACCOUNT,                           "+
				"	COUNTRY                             "+
				"	)                                    "+
				"                                        "+
				"VALUES (                                "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?,                                   "+
				"	?                                    "+
				"	)                                    ";

		try {
			conn.setAutoCommit(false);
			stmt = getMainStatement (insertSQL);

			stmt.setLong(1  , feeTableId);
			stmt.setString(2  , feeTable.getBatchId            ());
			stmt.setString(3  , feeTable.getChargeCategory     ());
			stmt.setString(4  , feeTable.getCrAccount          ());
			stmt.setString(5  , feeTable.getCrCurrency         ());
			stmt.setString(6  , feeTable.getCrSolId            ());
			stmt.setString(7  , feeTable.getDrAccount          ());
			stmt.setString(8  , feeTable.getDrCurrency         ());
			stmt.setString(9  , feeTable.getDrSolId            ());
			stmt.setString(10  , feeTable.getErrCode            ());
			stmt.setString(11 , feeTable.getLastResponse       ());
			try {
				stmt.setInt(12 , feeTable.getNumberOfTries      ());
			} catch (Exception e) {
//				e.printStackTrace();
				stmt.setNull(12, Types.INTEGER);
			}
			stmt.setString(13 , feeTable.getPostedBy           ());
			try {
				stmt.setTimestamp(14 , new java.sql.Timestamp(feeTable.getPostedDate         ().getTime().getTime()));
			} catch (Exception e) {
//				e.printStackTrace();
				stmt.setNull(14, Types.TIMESTAMP);
			}
			stmt.setString(15 , feeTable.getPostingStatus      ());
			try {
				stmt.setTimestamp(16 , new java.sql.Timestamp(feeTable.getProcessDate        ().getTime().getTime()));
			} catch (Exception e) {
//				e.printStackTrace();
				stmt.setNull(16, Types.TIMESTAMP);
			}
			stmt.setString(17 , feeTable.getPurpose            ());
			try {
				stmt.setLong(18 , feeTable.getRecordId           ());
			} catch (Exception e2) {
//				e2.printStackTrace();
				stmt.setNull(18, Types.BIGINT);
			}
			stmt.setString(19 , feeTable.getRemarks            ());
			stmt.setString(20 , feeTable.getStan               ());
			stmt.setString(21 , feeTable.getStatusFlg          ());
			try {
				stmt.setDouble(22 , feeTable.getTranAmount         ());
			} catch (Exception e1) {
//				e1.printStackTrace();
				stmt.setNull(22, Types.DOUBLE);
			}
			stmt.setString(23 , feeTable.getTranCurrency       ());
			try {
				stmt.setTimestamp(24 , new java.sql.Timestamp(feeTable.getTranDate           ().getTime().getTime()));
			} catch (Exception e) {
//				e.printStackTrace();
				stmt.setNull(24, Types.TIMESTAMP);
			}
			stmt.setString(25 , feeTable.getTranParticular     ());
			stmt.setString(26 , feeTable.getUploadedBy         ());
			try {
				stmt.setTimestamp(27 , new java.sql.Timestamp(feeTable.getUploadedDate       ().getTime().getTime()));
			} catch (Exception e) {
//				e.printStackTrace();
				stmt.setNull(27, Types.TIMESTAMP);
			}
			try {
				stmt.setTimestamp(28 , new java.sql.Timestamp(feeTable.getValueDate          ().getTime().getTime()));
			} catch (Exception e) {
//				e.printStackTrace();
				stmt.setNull(28, Types.TIMESTAMP);
			}
			try {
				stmt.setDouble(29 , feeTable.getVatAmount          ());
			} catch (Exception e) {
//				e.printStackTrace();
				stmt.setNull(29, Types.DOUBLE);
			}
			try {
				stmt.setLong(30 , feeTable.getProduct            ().getId());
			} catch (Exception e) {
//				e.printStackTrace();
				stmt.setNull(30, Types.BIGINT);
			}
			stmt.setString(31 , feeTable.getCountryCode        ());
			stmt.setString(32 , feeTable.getConfCode           ());
			stmt.setString(33 , feeTable.getVatAccount         ());
			stmt.setString(34 , feeTable.getVatCurrency        ());
			try {
				stmt.setBoolean(35 , feeTable.getHasSpecialAccount  ());
			} catch (Exception e) {
//				e.printStackTrace();
				stmt.setNull(35, Types.BOOLEAN);
			}
			try {
				stmt.setLong(36 , feeTable.getCountry            ().getId());
			} catch (Exception e) {
//				e.printStackTrace();
				stmt.setNull(36, Types.BIGINT);
			}

//			stmt.executeUpdate();
			stmt.addBatch();
//			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally{
			if(null != resultset){
				try {
					resultset.close();
				} catch (SQLException e) {
				}
			}
		}
		return stmt;
	}

	private static Connection getMainConnectionb(){
		Connection conn = null;
		DSConfig2 dsConfig2instance = DSConfig2.getInstance();
		conn = dsConfig2instance.getConventionalConnection(EntityManagerHelper.getMap());
		return conn;
	}
	private static PreparedStatement getMainStatementb(String sql){
		PreparedStatement stmt = null;
		Connection conn = getMainConnection();
		try {
			if(null != conn){
				stmt = conn.prepareStatement(sql);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return stmt;
	}
	private static Connection getMainConnection(){
		Connection conn = null;
		if(null != mainConnectionMap){
			String key = Thread.currentThread().getName();
			conn = mainConnectionMap.get(key);
			if(null == conn){
				DSConfig2 dsConfig2instance = DSConfig2.getInstance();
				conn = dsConfig2instance.getConventionalConnection(EntityManagerHelper.getMap());
				mainConnectionMap.put(key, conn);
			}
		}
		return conn;
	}
	private static PreparedStatement getMainStatement(String sql){
		PreparedStatement field = null;
		if(null != mainStatementMap){
			String key = Thread.currentThread().getName().concat(".").concat(sql);
			field = mainStatementMap.get(key);
			if(null == field){
				Connection conn = getMainConnection();
				try {
					if(null != conn){
						field = conn.prepareStatement(sql);
						mainStatementMap.put(key, field);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return field;
	}

	private void copyToFeeTable(List<FeesMnt> listFeesMnt){
		/*ResultSet resultset = null;
		PreparedStatement stmt = null;
		Connection conn = null;

		conn = getMainConnection();

//		System.out.println("######### SIZE OF FEES_MNT LIST: " + listFeesMnt.size());
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("main", EntityManagerHelper.getMap());
		EntityManager em = emf.createEntityManager();
		Dao feeDao = new JpaDao<String, com.expertedge.uba.collection.engine.jpa.FeeTable>(){};
		feeDao.reset(em);
		Boolean hasSpecialAccount = configurationInfo.getHasSpecialAccount();
		int recordCount = 0;
		beginTransaction(em);
		for (FeesMnt feesMnt : listFeesMnt) {
			recordCount++;

			boolean found = false;
			String findSQL = "SELECT ID FROM FEES_MNT WHERE ID = ?";

			try {
				stmt = getMainStatement (findSQL);
				stmt.setLong(1, feesMnt.getId());
				resultset = stmt.executeQuery();
//				resultset.setFetchSize(1);

				if(resultset.next()){
					found = true;
				}
				else {
					System.out.println("##### FATAL ERROR: Cannot connect to FeesMnt table represented by this FeeTable record. 'ID': " + feeTable.getId());
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
			}

			if(found){
				FeeTable feeTable = new FeeTable();
				feeTable.setRecordId       (resultset.getLong("id"));
				feeTable.setPostedDate     (feesMnt.getPostedDate     ());
				feeTable.setTranDate       (feesMnt.getTranDate       ());
				feeTable.setUploadedDate   (feesMnt.getUploadedDate   ());
				feeTable.setValueDate      (feesMnt.getValueDate      ());
				feeTable.setTranAmount     (feesMnt.getTranAmount     ());
				feeTable.setBatchId        (feesMnt.getBatchId        ());
				feeTable.setChargeCategory (feesMnt.getChargeCategory ());
				feeTable.setDrAccount      (feesMnt.getDrAccount      ());
				feeTable.setDrCurrency     (feesMnt.getDrCurrency     ());
				feeTable.setDrSolId        (feesMnt.getDrSolId        ());
				feeTable.setCrAccount      (feesMnt.getCrAccount      ());
				feeTable.setCrCurrency     (feesMnt.getCrCurrency     ());
				feeTable.setCrSolId        (feesMnt.getCrSolId        ());
				feeTable.setErrCode        (feesMnt.getErrCode        ());
				feeTable.setPostedBy       (feesMnt.getPostedBy       ());
				feeTable.setPostingStatus  (feesMnt.getPostingStatus  ());
				feeTable.setPurpose        (feesMnt.getPurpose        ());
				feeTable.setStan           (Connect24Helper.getStan());
				feeTable.setTranCurrency   (feesMnt.getTranCurrency   ());
				feeTable.setVatAccount     (feesMnt.getVatAccount());
				feeTable.setVatCurrency    (feesMnt.getVatCurrency());
				feeTable.setTranParticular (feesMnt.getTranParticular ());
				feeTable.setUploadedBy     (feesMnt.getUploadedBy     ());
				feeTable.setProduct        (configurationInfo.getProduct());
				feeTable.setCountry        (configurationInfo.getCountry());
				feeTable.setCrAccount      (feesMnt.getCrAccount());
				feeTable.setCrCurrency     (feesMnt.getCrCurrency());
				feeTable.setCrSolId        (feesMnt.getCrSolId());
				feeTable.setCountryCode    (feesMnt.getCountryCode());
				feeTable.setConfCode       (feesMnt.getConfCode());
				feeTable.setRemarks        ("Fresh Record");
				feeTable.setPostingStatus  (TransactionStatus.UNPROCESSED.toString());
				feeTable.setStatusFlg      (TransactionStatus.UNPROCESSED.toString());
				feeTable.setBatchId(feesMnt.getBatchId());
				feeTable.setVatAmount(feesMnt.getVatAmount());
				if(null != hasSpecialAccount && hasSpecialAccount.booleanValue() == true){
					feeTable.setHasSpecialAccount(hasSpecialAccount);
				}
				else {
					feeTable.setHasSpecialAccount(false);
				}
			}
//			feeDao.save(feeTable);
//			if(0 == (recordCount % 1000)){
//				commitTransaction(em);
//				beginTransaction(em);
//			}
		}
		commitTransaction(em);*/
	}

	private static Long getLastFeesMntId(){
		ResultSet resultset = null;
		PreparedStatement stmt = null;
		Connection conn = null;
		Long id = null;

		conn = getMainConnection();

		boolean found = false;
		String findSQL = "SELECT MAX(ID) FROM FEES_MNT";

		try {
			stmt = getMainStatement (findSQL);
			resultset = stmt.executeQuery();

			if(resultset.next()){
				found = true;
			}
			else {
			}

			if(found){
				id = resultset.getLong(1);
			}
		} catch (Exception e1) {
			found = false;
			e1.printStackTrace();
		}

		if(false == found)id = 0L;

		return id;
	}

	private static Long getNextFeesMntId(){
		ResultSet resultset = null;
		PreparedStatement stmt = null;
		Connection conn = null;
		Long id = null;

		conn = getMainConnection();

		boolean found = false;
		String findSQL = "SELECT MAX(ID) FROM FEES_MNT";

		try {
			stmt = getMainStatement (findSQL);
			resultset = stmt.executeQuery();

			if(resultset.next()){
				found = true;
			}
			else {
			}

			if(found){
				id = resultset.getLong(1);
				id = id + 1;
			}
		} catch (Exception e1) {
			found = false;
			e1.printStackTrace();
		}

		if(false == found)id = 1L;

		return id;
	}

	private static Long getNextFeeTableId(){
		ResultSet resultset = null;
		PreparedStatement stmt = null;
		Connection conn = null;
		Long id = null;

		conn = getMainConnection();

		boolean found = false;
		String findSQL = "SELECT MAX(ID) FROM FEE_TABLE";

		try {
			stmt = getMainStatement (findSQL);
			resultset = stmt.executeQuery();

			if(resultset.next()){
				found = true;
			}
			else {
			}

			if(found){
				id = resultset.getLong(1);
				id = id + 1;
			}
		} catch (Exception e1) {
			found = false;
			e1.printStackTrace();
		}

		if(false == found)id = 1L;

		return id;
	}

	private FeeTable getFeeTable(FeesMnt feesMnt){
		Boolean hasSpecialAccount = configurationInfo.getHasSpecialAccount();
		FeeTable feeTable = new FeeTable();
		feeTable.setId             (System.nanoTime());
		feeTable.setRecordId       (feesMnt.getId());
		feeTable.setPostedDate     (feesMnt.getPostedDate     ());
		feeTable.setTranDate       (feesMnt.getTranDate       ());
		feeTable.setUploadedDate   (feesMnt.getUploadedDate   ());
		feeTable.setValueDate      (feesMnt.getValueDate      ());
		feeTable.setTranAmount     (feesMnt.getTranAmount     ());
		feeTable.setBatchId        (feesMnt.getBatchId        ());
		feeTable.setChargeCategory (feesMnt.getChargeCategory ());
		feeTable.setDrAccount      (feesMnt.getDrAccount      ());
		feeTable.setDrCurrency     (feesMnt.getDrCurrency     ());
		feeTable.setDrSolId        (feesMnt.getDrSolId        ());
		feeTable.setCrAccount      (feesMnt.getCrAccount      ());
		feeTable.setCrCurrency     (feesMnt.getCrCurrency     ());
		feeTable.setCrSolId        (feesMnt.getCrSolId        ());
		feeTable.setErrCode        (feesMnt.getErrCode        ());
		feeTable.setPostedBy       (feesMnt.getPostedBy       ());
		feeTable.setPostingStatus  (feesMnt.getPostingStatus  ());
		feeTable.setPurpose        (feesMnt.getPurpose        ());
		feeTable.setStan           (Connect24Helper.getStan());
		feeTable.setTranCurrency   (feesMnt.getTranCurrency   ());
		feeTable.setVatAccount     (feesMnt.getVatAccount());
		feeTable.setVatCurrency    (feesMnt.getVatCurrency());
		feeTable.setTranParticular (feesMnt.getTranParticular ());
		feeTable.setUploadedBy     (feesMnt.getUploadedBy     ());
		feeTable.setProduct        (configurationInfo.getProduct());
		feeTable.setCountry        (configurationInfo.getCountry());
		feeTable.setCrAccount      (feesMnt.getCrAccount());
		feeTable.setCrCurrency     (feesMnt.getCrCurrency());
		feeTable.setCrSolId        (feesMnt.getCrSolId());
		feeTable.setCountryCode    (feesMnt.getCountryCode());
		feeTable.setConfCode       (feesMnt.getConfCode());
		feeTable.setRemarks        ("Fresh Record");
		feeTable.setPostingStatus  (TransactionStatus.UNPROCESSED.toString());
		feeTable.setStatusFlg      (TransactionStatus.UNPROCESSED.toString());
		feeTable.setBatchId(feesMnt.getBatchId());
		feeTable.setVatAmount(feesMnt.getVatAmount());
		if(null != hasSpecialAccount && hasSpecialAccount.booleanValue() == true){
			feeTable.setHasSpecialAccount(hasSpecialAccount);
		}
		else {
			feeTable.setHasSpecialAccount(false);
		}

		return feeTable;
	}

	private int[] getActualLengths(Sheet sh){
		int idx = sh.getLastRowNum() + 1;
		int idy = sh.getRow(START_ROW).getLastCellNum();
		int rowIndex = idx - 1;
		int colIndex = idy - 1;
		final int main_row = START_ROW;
		final int main_col = 0;
		for (; rowIndex >= 0; --rowIndex) {
			String val = null;
			try {
//				val = sh.getRow(rowIndex).getCell(main_col).getStringCellValue();
				val = getCellTextValue(sh, rowIndex, main_col);
			} catch (RuntimeException e) {
				//e.printStackTrace();
				continue;
			}
			if(StringUtils.isBlank(val)){
				continue;
			}
			else{
				break;
			}
		}
		for (; colIndex >= 0; --colIndex) {
			String val = null;
			try {
//				val = sh.getRow(main_row).getCell(colIndex).getStringCellValue();
				val = getCellTextValue(sh, main_row, colIndex);
			} catch (RuntimeException e) {
				//e.printStackTrace();
				continue;
			}
			if(StringUtils.isBlank(val)){
				continue;
			}
			else{
				break;
			}
		}
		int rowLength = rowIndex + 1;
		int columnLength = colIndex + 1;
		return new int[]{rowLength,columnLength};
	}

	private String getCellTextValue(Sheet sh, int row, int col){
		String str = "";
		Cell cell = sh.getRow(row).getCell(col);
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
            	str = "" + cell.getRichStringCellValue().getString();
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                	str = "" + dateFormat.format(cell.getDateCellValue());
                } else {
                	str = "" + cell.getNumericCellValue();
                }
                break;
            case Cell.CELL_TYPE_BOOLEAN:
            	str = "" + cell.getBooleanCellValue() ;
                break;
            case Cell.CELL_TYPE_FORMULA:
            	str = "" + cell.getCellFormula();
                break;
            default:
        }
	    return str;
	}

	public List<ConfigurationInfo> getConfigurationInfos() {
		if(null == configurationInfos){
			configurationInfos = new ArrayList<ConfigurationInfo>();
			ConfigurationInfoDAO configurationInfoDao = new ConfigurationInfoDAO();
			try {
				configurationInfos = configurationInfoDao.findAll();
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
		}
		return configurationInfos;
	}
	public void setConfigurationInfos(List<ConfigurationInfo> configurationInfos) {
		this.configurationInfos = configurationInfos;
	}

	public ConfigurationInfo getConfigurationInfo() {
		return configurationInfo;
	}

	public void setConfigurationInfo(ConfigurationInfo configurationInfo) {
		this.configurationInfo = configurationInfo;
	}
	public UploadedFile getUploadedFile() {
		return uploadedFile;
	}
	public void setUploadedFile(UploadedFile uploadedFile) {
		this.uploadedFile = uploadedFile;
	}

}

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
public class FeesMntUploadDetailCopy {

	private final int START_ROW = 0;
	private FeesMnt feesMnt;
	private File uploadFile;
	private UploadedFile uploadedFile;
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	ConfigurationInfoDAO configurationInfoDao = new ConfigurationInfoDAO();
	ConfigurationInfo configurationInfo;
	List<ConfigurationInfo> configurationInfos;

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

		EntityManagerFactory emf = Persistence.createEntityManagerFactory("main", EntityManagerHelper.getMap());
		EntityManager em = emf.createEntityManager();
		EntityManagerFactory emf2 = Persistence.createEntityManagerFactory("main", EntityManagerHelper.getMap());
		EntityManager em2 = emf2.createEntityManager();
		Dao feeMntDao = new JpaDao<String, com.expertedge.uba.collection.engine.jpa.FeesMnt>(){};
		Dao feeDao = new JpaDao<String, com.expertedge.uba.collection.engine.jpa.FeeTable>(){};
		feeMntDao.reset(em);
		feeDao.reset(em2);

		Boolean hasSpecialAccount = configurationInfo.getHasSpecialAccount();

		boolean ignoredEntries = false;
		String cvalue = "";
		Calendar cal = null;
		Date dt = null;
		Double dvalue = 0d;
//		String batchNumber = SystemUUIDGenerator.getUUID().concat("-").concat(Thread.currentThread().getName().toUpperCase());

//		em2.getTransaction().begin();
		int recordCount = 0;
		for (lineCount = START_ROW + 1; lineCount < rLength; lineCount++) {

			row = sh.getRow(lineCount);
			recordCount++;

			if(null == row)continue;

			beginTransaction(em2);

			FeesMnt feesMnt = new FeesMnt();
			FeeTable feeTable = null;

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
			feeTable = new FeeTable();
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
			feesMnt.setPostingStatus(TransactionStatus.UNPROCESSED.toString());

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

//			em.getTransaction().begin();
			beginTransaction(em);
			feeMntDao.save(feesMnt);
//			em.getTransaction().commit();
			commitTransaction(em);
			// Now set the record id of feeTable to the id of feeMnt
			feeTable.setRecordId       (feesMnt.getId());
			feeDao.save(feeTable);

			if(0 == (recordCount % 1000))
//				em2.getTransaction().commit();
				commitTransaction(em2);

		}
//		em2.getTransaction().commit();
		commitTransaction(em2);
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

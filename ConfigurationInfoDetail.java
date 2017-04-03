package com.expertedge.uba.collection.engine.beans;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.faces.model.*;

import org.primefaces.event.FileUploadEvent;  
import org.primefaces.model.UploadedFile;  

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;

import com.expertedge.uba.collection.engine.dao.*;
import com.expertedge.uba.collection.engine.util.*;
import com.expertedge.uba.collection.engine.jpa.*;
import com.expertedge.uba.collection.engine.jpa.ConfigurationInfo;
import com.expertedge.uba.collection.engine.jpa.Product;
import com.expertedge.uba.collection.engine.jpa.Country;
import com.geniunwit.security.util.UserManagementUtil;
import org.apache.commons.lang.SerializationUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author <a href="mailto:jniyiooster@gmail.com">Olaniyi Osunsanya</a>
 */
@Scope("session")
@Controller
public class ConfigurationInfoDetail {

	private ConfigurationInfo configurationInfo;
	private boolean dirty;



	private Integer firstRowIndex;
	private Integer currentRowIndex;
	private Integer rowCount;
	private Integer rows;

	private List<ConfigurationInfo> model;
	private boolean creating = false;
	private boolean submitted;
	private Date dt;
	private SimpleDateFormat sdateFormat = new SimpleDateFormat("dd/MM/yyyy");

	ProductDAO productDao = new ProductDAO();
	List<Product> products;

	CountryDAO countryDao = new CountryDAO();
	List<Country> countrys;


	private WebAuditLogDAO webAuditLogDAO = new WebAuditLogDAO();
	@Autowired
	private UserManagementUtil userManagerUtil;

	public void init(ConfigurationInfo configurationInfo){
		setDirty(false);
		setConfigurationInfo(configurationInfo);
		model = new ArrayList();
		dt = new Date();
	}
	public ConfigurationInfoDetail() {
		create();
		model = new ArrayList();
	}

	@RequiresPermissions("*:create")
	public String create(){
		ConfigurationInfo configurationInfo = new ConfigurationInfo();
		String uid = SystemUUIDGenerator.getUUID();
		configurationInfo.setId(new Long(System.currentTimeMillis()).longValue());
		setConfigurationInfo(configurationInfo);
		setCreating(true);

		addWebAuditLogEntry("Create", "Button Label: New");

		return null;
	}

	@RequiresPermissions("*:create")
	public String duplicate(){
		ConfigurationInfo configurationInfo = (ConfigurationInfo)getConfigurationInfo().clone();
		configurationInfo.setId(null);
		setConfigurationInfo(configurationInfo);
		setCreating(true);
		FacesUtils.addInfoMessage("duplication_successful");

		addWebAuditLogEntry("Duplicate", new StringBuilder("Button Label: Duplicate").append(". Previous Content: ID = ").append(getConfigurationInfo().getId()).append(". Values: ").append(getConfigurationInfo().toString()).toString());

		return null;
	}

	private void addWebAuditLogEntry(String actionPerformed, String actionDetail){
		if(null != userManagerUtil){
	        String actionSource = "CONFIGURATION_INFO";
	        String uri = "/ConfigurationInfoDetailED.xhtml";
			WebAuditLog webAuditLog = new WebAuditLog();
			String uid = SystemUUIDGenerator.getUUID();
			webAuditLog.setId(new Long(System.currentTimeMillis()).longValue());
			webAuditLog.setUsername(userManagerUtil.getCurrentUserName());
			webAuditLog.setRolename(userManagerUtil.getStringRoleToDisplayForUser());
			String visitedUrl = userManagerUtil.getScheme()+"://"+userManagerUtil.getServerName()+":"+userManagerUtil.getServerPort()+userManagerUtil.getContextPath()+uri;
			webAuditLog.setVisitedUrl(visitedUrl);
			Calendar now = Calendar.getInstance();
			webAuditLog.setTimeOfEvent(now);
			webAuditLog.setIpAddress(userManagerUtil.getRemoteIPAddress());
			webAuditLog.setActionPerformed(actionPerformed);
			webAuditLog.setActionSource(actionSource);
			webAuditLog.setActionDetail(actionDetail);
			EntityManagerHelper.beginTransaction();
			webAuditLogDAO.save(webAuditLog);
			EntityManagerHelper.commit();
		}
	}
	
	public int getFirstPageIndex(int index){
		int firstRow = getFirstRowIndex();;
		int currentRow = index + 1;
		int rowCount = getRowCount();
		int rows = getRows();
		int pages = currentRow / rows;
		int findex = 0;
		if(currentRow == 0)findex = 0;
		else findex = (pages * rows) - 1;
		return findex;
	}

	public String next(){
		List lst = model;
		int rowCount = lst.size();
		int lastIndex = rowCount - 1;
		int index = lst.indexOf(getConfigurationInfo());
		if(rowCount == 0 || index == -1){
			FacesUtils.addErrorMessage("no_row_selection");
			return null;
		}
		if(index == lastIndex){
			FacesUtils.addErrorMessage("end_of_list");
			return null;
		}

		// Now move to record
		index++;
		ConfigurationInfo configurationInfo = (ConfigurationInfo)lst.get(index);
		setConfigurationInfo(configurationInfo);
		setCreating(false);
		
		setCurrentRowIndex(index);
		int findex = getFirstPageIndex(index);
		setFirstRowIndex(findex);
		
		return null;
	}
	public String previous(){
		List lst = model;
		int rowCount = lst.size();
		int lastIndex = rowCount - 1;
		int index = lst.indexOf(getConfigurationInfo());
		if(rowCount == 0 || index == -1){
			FacesUtils.addErrorMessage("no_row_selection");
			return null;
		}
		if(index == 0){
			FacesUtils.addErrorMessage("start_of_list");
			return null;
		}

		// Now move to record
		index--;
		ConfigurationInfo configurationInfo = (ConfigurationInfo)lst.get(index);
		setConfigurationInfo(configurationInfo);
		setCreating(false);
		
		setCurrentRowIndex(index);
		int findex = getFirstPageIndex(index);
		setFirstRowIndex(findex);
		
		return null;
	}
	public String first(){
		List lst = model;
		int rowCount = lst.size();
		int lastIndex = rowCount - 1;
		int index = lst.indexOf(getConfigurationInfo());
		if(rowCount == 0 || index == -1){
			FacesUtils.addErrorMessage("no_row_selection");
			return null;
		}
		if(index == 0){
			FacesUtils.addErrorMessage("start_of_list");
			return null;
		}

		// Now move to record
		index = 0;
		ConfigurationInfo configurationInfo = (ConfigurationInfo)lst.get(index);
		setConfigurationInfo(configurationInfo);
		setCreating(false);
		
		setCurrentRowIndex(index);
		int findex = getFirstPageIndex(index);
		setFirstRowIndex(findex);
		
		return null;
	}
	public String last(){
		List lst = model;
		int rowCount = lst.size();
		int lastIndex = rowCount - 1;
		int index = lst.indexOf(getConfigurationInfo());
		if(rowCount == 0 || index == -1){
			FacesUtils.addErrorMessage("no_row_selection");
			return null;
		}
		if(index == lastIndex){
			FacesUtils.addErrorMessage("end_of_list");
			return null;
		}

		// Now move to record
		index = lastIndex;
		ConfigurationInfo configurationInfo = (ConfigurationInfo)lst.get(index);
		setConfigurationInfo(configurationInfo);
		setCreating(false);
		
		setCurrentRowIndex(index);
		int findex = getFirstPageIndex(index);
		setFirstRowIndex(findex);
		
		return null;
	}
	public String list(){
		ConfigurationInfoList configurationInfoList = null;
		try {
			configurationInfoList = FacesUtils.findBean("configurationInfoList", ConfigurationInfoList.class);
			configurationInfoList.setList(getModel());
			configurationInfoList.setFirstRowIndex(getFirstRowIndex());
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return "/views/ConfigurationInfoListED";
	}

	@RequiresPermissions("*:edit,create")
	public String save(){
		boolean validated = validate();
		if(validated == false)return null;

		ConfigurationInfoDAO dao = new ConfigurationInfoDAO();
		EntityManagerHelper.beginTransaction();
		if(isCreating()){
			dao.save(getConfigurationInfo());
			List lst = getModel();
			lst.add(getConfigurationInfo());
			//setModel(lst);
			setCreating(false);

			addWebAuditLogEntry("Save", new StringBuilder("Button Label: Save").append(". New Content: ID = ").append(getConfigurationInfo().getId()).append(". Values: ").append(getConfigurationInfo().toString()).toString());

		}
		else {
			dao.update(getConfigurationInfo());

			addWebAuditLogEntry("Update", new StringBuilder("Button Label: Save").append(". New Content: ID = ").append(getConfigurationInfo().getId()).append(". Values: ").append(getConfigurationInfo().toString()).toString());

		}
		EntityManagerHelper.commit();
		if(isCreating())setCreating(false);
		FacesUtils.addInfoMessage("save_successful");
		return null;
	}
	
	@RequiresPermissions("*:delete")
	public String delete(){
		ConfigurationInfoDAO dao = new ConfigurationInfoDAO();
		if(isCreating()){
			FacesUtils.addErrorMessage(null,"delete_not_successful",new Object[]{" Nothing to delete."});
			return null;
		}
		else {
			ConfigurationInfo configurationInfo = getConfigurationInfo();
			ConfigurationInfo previousConfigurationInfo = (ConfigurationInfo)getConfigurationInfo().clone();
			List lst = model;
			int rowCount = lst.size();
			int lastIndex = rowCount - 1;
			int index = lst.indexOf(configurationInfo);
			if(rowCount == 0 || index == -1){
				FacesUtils.addErrorMessage("no_row_selection");
				return null;
			}
			EntityManagerHelper.beginTransaction();
			dao.delete(configurationInfo);
			EntityManagerHelper.commit();
			lst.remove(configurationInfo);

			configurationInfo = null;
			try {
				configurationInfo = (ConfigurationInfo)lst.get(index);
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
			if(configurationInfo != null){
				setConfigurationInfo(configurationInfo);
				setCreating(false);
				//setModel(lst);
			}
			else {
				configurationInfo = new ConfigurationInfo();
				String uid = SystemUUIDGenerator.getUUID();
		configurationInfo.setId(new Long(System.currentTimeMillis()).longValue());
				setConfigurationInfo(configurationInfo);
				setCreating(true);
				//setModel(lst);
			}

			addWebAuditLogEntry("Delete", new StringBuilder("Button Label: Delete").append(". Deleted Content: ID = ").append(previousConfigurationInfo.getId()).append(". Values: ").append(previousConfigurationInfo.toString()).toString());

		}
		FacesUtils.addInfoMessage("delete_successful");
		return null;
	}

	private boolean validate(){
		boolean hasErrors = false;
		// Validate blank fields


		String confCode = getConfigurationInfo().getConfCode();
		if(confCode == null || confCode.trim().equals("")){
			FacesUtils.addErrorMessage(null,"blank_field",new Object[]{"ConfCode"});
			hasErrors = true;
		}

		Product product = null;
		try {
			product = getConfigurationInfo().getProduct();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(product == null){
			FacesUtils.addErrorMessage(null,"blank_field",new Object[]{"Product"});
			hasErrors = true;
		}

		String c24Host = getConfigurationInfo().getC24Host();
		if(c24Host == null || c24Host.trim().equals("")){
			FacesUtils.addErrorMessage(null,"blank_field",new Object[]{"C24Host"});
			hasErrors = true;
		}

		String c24Port = getConfigurationInfo().getC24Port();
		if(c24Port == null || c24Port.trim().equals("")){
			FacesUtils.addErrorMessage(null,"blank_field",new Object[]{"C24Port"});
			hasErrors = true;
		}

		String bankID = getConfigurationInfo().getBankID();
		if(bankID == null || bankID.trim().equals("")){
			FacesUtils.addErrorMessage(null,"blank_field",new Object[]{"BankID"});
			hasErrors = true;
		}

		String dccID = getConfigurationInfo().getDccID();
		if(dccID == null || dccID.trim().equals("")){
			FacesUtils.addErrorMessage(null,"blank_field",new Object[]{"DccID"});
			hasErrors = true;
		}

		String fdccID = getConfigurationInfo().getFdccID();
		if(fdccID == null || fdccID.trim().equals("")){
			FacesUtils.addErrorMessage(null,"blank_field",new Object[]{"FdccID"});
			hasErrors = true;
		}

		String databaseType = getConfigurationInfo().getDatabaseType();
		if(databaseType == null || databaseType.trim().equals("")){
			FacesUtils.addErrorMessage(null,"blank_field",new Object[]{"DatabaseType"});
			hasErrors = true;
		}

		String databaseHost = getConfigurationInfo().getDatabaseHost();
		if(databaseHost == null || databaseHost.trim().equals("")){
			FacesUtils.addErrorMessage(null,"blank_field",new Object[]{"DatabaseHost"});
			hasErrors = true;
		}

		String databaseNameOrServiceName = getConfigurationInfo().getDatabaseNameOrServiceName();
		if(databaseNameOrServiceName == null || databaseNameOrServiceName.trim().equals("")){
			FacesUtils.addErrorMessage(null,"blank_field",new Object[]{"DatabaseNameOrServiceName"});
			hasErrors = true;
		}

		String databaseUserName = getConfigurationInfo().getDatabaseUserName();
		if(databaseUserName == null || databaseUserName.trim().equals("")){
			FacesUtils.addErrorMessage(null,"blank_field",new Object[]{"DatabaseUserName"});
			hasErrors = true;
		}

		String databasePassword = getConfigurationInfo().getDatabasePassword();
		if(databasePassword == null || databasePassword.trim().equals("")){
			FacesUtils.addErrorMessage(null,"blank_field",new Object[]{"DatabasePassword"});
			hasErrors = true;
		}

		String finacleDatabaseType = getConfigurationInfo().getFinacleDatabaseType();
		if(finacleDatabaseType == null || finacleDatabaseType.trim().equals("")){
			FacesUtils.addErrorMessage(null,"blank_field",new Object[]{"FinacleDatabaseType"});
			hasErrors = true;
		}

		String finacleDatabaseHost = getConfigurationInfo().getFinacleDatabaseHost();
		if(finacleDatabaseHost == null || finacleDatabaseHost.trim().equals("")){
			FacesUtils.addErrorMessage(null,"blank_field",new Object[]{"FinacleDatabaseHost"});
			hasErrors = true;
		}

		String finacleDatabaseNameOrServiceName = getConfigurationInfo().getFinacleDatabaseNameOrServiceName();
		if(finacleDatabaseNameOrServiceName == null || finacleDatabaseNameOrServiceName.trim().equals("")){
			FacesUtils.addErrorMessage(null,"blank_field",new Object[]{"FinacleDatabaseNameOrServiceName"});
			hasErrors = true;
		}

		String finacleDatabaseUserName = getConfigurationInfo().getFinacleDatabaseUserName();
		if(finacleDatabaseUserName == null || finacleDatabaseUserName.trim().equals("")){
			FacesUtils.addErrorMessage(null,"blank_field",new Object[]{"FinacleDatabaseUserName"});
			hasErrors = true;
		}

		String finacleDatabasePassword = getConfigurationInfo().getFinacleDatabasePassword();
		if(finacleDatabasePassword == null || finacleDatabasePassword.trim().equals("")){
			FacesUtils.addErrorMessage(null,"blank_field",new Object[]{"FinacleDatabasePassword"});
			hasErrors = true;
		}

		String blockCode = getConfigurationInfo().getBlockCode();
		if(blockCode == null || blockCode.trim().equals("")){
			FacesUtils.addErrorMessage(null,"blank_field",new Object[]{"BlockCode"});
			hasErrors = true;
		}




	ConfigurationInfoDAO dao = new ConfigurationInfoDAO();
	Collection col = null;
	Object fieldValue = null;
	boolean isBlankField = false;

	col = null;
	fieldValue = null;
	fieldValue = getConfigurationInfo().getConfCode();
	isBlankField = false;
	if(fieldValue instanceof String){
		isBlankField = (fieldValue == null || "".equals(fieldValue));
	}
	else {
		isBlankField = (fieldValue == null);
	}

	if(isBlankField == false){
		try {
			col = dao.findByConfCode(fieldValue);
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		if(isCreating()){
			// Disallow duplicate ConfigurationInfo confCode
			if(null != col && col.size() > 0){
					FacesUtils.addErrorMessage(null,"duplicate_value",new Object[]{": ConfigurationInfo -> confCode already exists"});
					hasErrors = true;
			}
		}
		else {
			// If record is being modified, ensure that it is the same as that found by the dao
			// otherwise, a duplicate code can exist by assigning code the value in another record
			if(null != col && col.size() > 1){
					FacesUtils.addErrorMessage(null,"duplicate_value",new Object[]{": ConfigurationInfo -> confCode already exists"});
					hasErrors = true;
			}
			else if(null != col && col.size() == 1){
				Object db_pk = getConfigurationInfo().getId();
				Object en_pk = null;
				ConfigurationInfo tconfigurationInfo = (ConfigurationInfo)col.iterator().next();
				en_pk = tconfigurationInfo.getId();
				if(db_pk.equals(en_pk) == false){
					FacesUtils.addErrorMessage(null,"duplicate_value",new Object[]{": ConfigurationInfo -> confCode already exists"});
					hasErrors = true;
				}
			}
		}
	}

	col = null;
	fieldValue = null;
	fieldValue = getConfigurationInfo().getProduct();
	isBlankField = false;
	if(fieldValue instanceof String){
		isBlankField = (fieldValue == null || "".equals(fieldValue));
	}
	else {
		isBlankField = (fieldValue == null);
	}

	if(isBlankField == false){
		try {
			col = dao.findByProduct(fieldValue);
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		if(isCreating()){
			// Disallow duplicate ConfigurationInfo product
			if(null != col && col.size() > 0){
					FacesUtils.addErrorMessage(null,"duplicate_value",new Object[]{": ConfigurationInfo -> product already exists"});
					hasErrors = true;
			}
		}
		else {
			// If record is being modified, ensure that it is the same as that found by the dao
			// otherwise, a duplicate code can exist by assigning code the value in another record
			if(null != col && col.size() > 1){
					FacesUtils.addErrorMessage(null,"duplicate_value",new Object[]{": ConfigurationInfo -> product already exists"});
					hasErrors = true;
			}
			else if(null != col && col.size() == 1){
				Object db_pk = getConfigurationInfo().getId();
				Object en_pk = null;
				ConfigurationInfo tconfigurationInfo = (ConfigurationInfo)col.iterator().next();
				en_pk = tconfigurationInfo.getId();
				if(db_pk.equals(en_pk) == false){
					FacesUtils.addErrorMessage(null,"duplicate_value",new Object[]{": ConfigurationInfo -> product already exists"});
					hasErrors = true;
				}
			}
		}
	}


		return (hasErrors == false);
	}

	public String back(){
		return "back";
	}
	public String print(){
		return null;
	}
	private File generatePdf(String rpath, String fileName, Collection beanCol){
		File outputFile = null;
		try {
			File filepath = new File(rpath);
			outputFile = new File(filepath.getParentFile(),fileName);
			Map parameters = new HashMap();
			JasperReport report = JasperCompileManager.compileReport(rpath);
			JRDataSource ds = new JRBeanCollectionDataSource(beanCol) ;
			JasperPrint jprint = null;
			jprint = JasperFillManager.fillReport(report, parameters, ds);
			JRPdfExporter pdfExporter = new JRPdfExporter();
			pdfExporter.setParameter(JRPdfExporterParameter.JASPER_PRINT, jprint);
			pdfExporter.setParameter(JRPdfExporterParameter.OUTPUT_FILE, outputFile);
			pdfExporter.setParameter(JRPdfExporterParameter.PDF_VERSION, JRPdfExporterParameter.PDF_VERSION_1_3.toString());
			pdfExporter.exportReport();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return outputFile;
	}
	private UIComponent findChildComponent(UIComponent component, String id){
		return recurseChild(component,id);
	}
	private UIComponent recurseChild(UIComponent parent, String id){
		if(parent.getId().equals(id)){
			return parent;
		}

		Iterator itcf = parent.getFacetsAndChildren();
		if(itcf.hasNext() == false){
			return null;
		}

		while (itcf.hasNext()) {
			UIComponent elem = (UIComponent)itcf.next();
			UIComponent retValue = recurseChild(elem,id);
			if(null == retValue){
				continue;
			}
			else return retValue;
		}
		return null;
	}

	public String home(){
		return "/views/Login";
	}
	/**
	 * @return the dirty
	 */
	public boolean isDirty() {
		return dirty;
	}
	/**
	 * @param dirty the dirty to set
	 */
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	/**
	 * @return the ConfigurationInfo
	 */
	public ConfigurationInfo getConfigurationInfo() {
		return configurationInfo;
	}
	/**
	 * @param configurationInfo the ConfigurationInfo to set
	 */
	public void setConfigurationInfo(ConfigurationInfo configurationInfo) {
		this.configurationInfo = configurationInfo;
	}



	/**
	 * @return the creating
	 */
	public boolean isCreating() {
		return creating;
	}
	/**
	 * @param creating the creating to set
	 */
	public void setCreating(boolean creating) {
		this.creating = creating;
	}

	/**
	 * @return the dt
	 */
	public Date getDt() {
		return dt;
	}
	/**
	 * @param dt the dt to set
	 */
	public void setDt(Date dt) {
		this.dt = dt;
	}

	/**
	 * @return the model
	 */
	public List<ConfigurationInfo> getModel() {
		return model;
	}
	/**
	 * @param model the model to set
	 */
	public void setModel(List<ConfigurationInfo> model) {
		this.model = model;
	}
    






	private void getUIValues(){
		
	}

	public Integer getFirstRowIndex() {
		if(Nought.isNought(firstRowIndex)){
			return 0;
		}
		return firstRowIndex;
	}
	public void setFirstRowIndex(Integer firstRowIndex) {
		this.firstRowIndex = firstRowIndex;
	}
	public Integer getRowCount() {
		return rowCount;
	}
	public void setRowCount(Integer rowCount) {
		this.rowCount = rowCount;
	}
	public Integer getCurrentRowIndex() {
		return currentRowIndex;
	}
	public void setCurrentRowIndex(Integer currentRowIndex) {
		this.currentRowIndex = currentRowIndex;
	}
	public Integer getRows() {
		return rows;
	}
	public void setRows(Integer rows) {
		this.rows = rows;
	}


	public List<Product> getProducts() {
		if(null == products){
			products = new ArrayList<Product>();
			ProductDAO productDao = new ProductDAO();
			try { 
				products = productDao.findAll();			
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
		}
		return products;
	}
	public void setProducts(List<Product> products) {
		this.products = products;
	}

	public List<Country> getCountrys() {
		if(null == countrys){
			countrys = new ArrayList<Country>();
			CountryDAO countryDao = new CountryDAO();
			try { 
				countrys = countryDao.findAll();			
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
		}
		return countrys;
	}
	public void setCountrys(List<Country> countrys) {
		this.countrys = countrys;
	}

	
}

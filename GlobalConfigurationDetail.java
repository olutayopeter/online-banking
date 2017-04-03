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
import com.expertedge.uba.collection.engine.jpa.GlobalConfiguration;
import com.geniunwit.security.util.UserManagementUtil;
import org.apache.commons.lang.SerializationUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author <a href="mailto:jniyiooster@gmail.com">Olaniyi Osunsanya</a>
 */
@Scope("session")
@Controller
public class GlobalConfigurationDetail {

	private GlobalConfiguration globalConfiguration;
	private boolean dirty;



	private Integer firstRowIndex;
	private Integer currentRowIndex;
	private Integer rowCount;
	private Integer rows;

	private List<GlobalConfiguration> model;
	private boolean creating = false;
	private boolean submitted;
	private Date dt;
	private SimpleDateFormat sdateFormat = new SimpleDateFormat("dd/MM/yyyy");


	private WebAuditLogDAO webAuditLogDAO = new WebAuditLogDAO();
	@Autowired
	private UserManagementUtil userManagerUtil;

	public void init(GlobalConfiguration globalConfiguration){
		setDirty(false);
		setGlobalConfiguration(globalConfiguration);
		model = new ArrayList();
		dt = new Date();
	}
	public GlobalConfigurationDetail() {
		create();
		model = new ArrayList();
	}

	@RequiresPermissions("*:create")
	public String create(){
		GlobalConfiguration globalConfiguration = new GlobalConfiguration();
		String uid = SystemUUIDGenerator.getUUID();
		globalConfiguration.setId(new Long(System.currentTimeMillis()).longValue());
		setGlobalConfiguration(globalConfiguration);
		setCreating(true);

		addWebAuditLogEntry("Create", "Button Label: New");

		return null;
	}

	@RequiresPermissions("*:create")
	public String duplicate(){
		GlobalConfiguration globalConfiguration = (GlobalConfiguration)getGlobalConfiguration().clone();
		globalConfiguration.setId(null);
		setGlobalConfiguration(globalConfiguration);
		setCreating(true);
		FacesUtils.addInfoMessage("duplication_successful");

		addWebAuditLogEntry("Duplicate", new StringBuilder("Button Label: Duplicate").append(". Previous Content: ID = ").append(getGlobalConfiguration().getId()).append(". Values: ").append(getGlobalConfiguration().toString()).toString());

		return null;
	}

	private void addWebAuditLogEntry(String actionPerformed, String actionDetail){
		if(null != userManagerUtil){
	        String actionSource = "GLOBAL_CONFIGURATION";
	        String uri = "/GlobalConfigurationDetailED.xhtml";
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
		int index = lst.indexOf(getGlobalConfiguration());
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
		GlobalConfiguration globalConfiguration = (GlobalConfiguration)lst.get(index);
		setGlobalConfiguration(globalConfiguration);
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
		int index = lst.indexOf(getGlobalConfiguration());
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
		GlobalConfiguration globalConfiguration = (GlobalConfiguration)lst.get(index);
		setGlobalConfiguration(globalConfiguration);
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
		int index = lst.indexOf(getGlobalConfiguration());
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
		GlobalConfiguration globalConfiguration = (GlobalConfiguration)lst.get(index);
		setGlobalConfiguration(globalConfiguration);
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
		int index = lst.indexOf(getGlobalConfiguration());
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
		GlobalConfiguration globalConfiguration = (GlobalConfiguration)lst.get(index);
		setGlobalConfiguration(globalConfiguration);
		setCreating(false);
		
		setCurrentRowIndex(index);
		int findex = getFirstPageIndex(index);
		setFirstRowIndex(findex);
		
		return null;
	}
	public String list(){
		GlobalConfigurationList globalConfigurationList = null;
		try {
			globalConfigurationList = FacesUtils.findBean("globalConfigurationList", GlobalConfigurationList.class);
			globalConfigurationList.setList(getModel());
			globalConfigurationList.setFirstRowIndex(getFirstRowIndex());
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return "/views/GlobalConfigurationListED";
	}

	@RequiresPermissions("*:edit,create")
	public String save(){
		boolean validated = validate();
		if(validated == false)return null;

		GlobalConfigurationDAO dao = new GlobalConfigurationDAO();
		EntityManagerHelper.beginTransaction();
		if(isCreating()){
			dao.save(getGlobalConfiguration());
			List lst = getModel();
			lst.add(getGlobalConfiguration());
			//setModel(lst);
			setCreating(false);

			addWebAuditLogEntry("Save", new StringBuilder("Button Label: Save").append(". New Content: ID = ").append(getGlobalConfiguration().getId()).append(". Values: ").append(getGlobalConfiguration().toString()).toString());

		}
		else {
			dao.update(getGlobalConfiguration());

			addWebAuditLogEntry("Update", new StringBuilder("Button Label: Save").append(". New Content: ID = ").append(getGlobalConfiguration().getId()).append(". Values: ").append(getGlobalConfiguration().toString()).toString());

		}
		EntityManagerHelper.commit();
		if(isCreating())setCreating(false);
		FacesUtils.addInfoMessage("save_successful");
		return null;
	}
	
	@RequiresPermissions("*:delete")
	public String delete(){
		GlobalConfigurationDAO dao = new GlobalConfigurationDAO();
		if(isCreating()){
			FacesUtils.addErrorMessage(null,"delete_not_successful",new Object[]{" Nothing to delete."});
			return null;
		}
		else {
			GlobalConfiguration globalConfiguration = getGlobalConfiguration();
			GlobalConfiguration previousGlobalConfiguration = (GlobalConfiguration)getGlobalConfiguration().clone();
			List lst = model;
			int rowCount = lst.size();
			int lastIndex = rowCount - 1;
			int index = lst.indexOf(globalConfiguration);
			if(rowCount == 0 || index == -1){
				FacesUtils.addErrorMessage("no_row_selection");
				return null;
			}
			EntityManagerHelper.beginTransaction();
			dao.delete(globalConfiguration);
			EntityManagerHelper.commit();
			lst.remove(globalConfiguration);

			globalConfiguration = null;
			try {
				globalConfiguration = (GlobalConfiguration)lst.get(index);
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
			if(globalConfiguration != null){
				setGlobalConfiguration(globalConfiguration);
				setCreating(false);
				//setModel(lst);
			}
			else {
				globalConfiguration = new GlobalConfiguration();
				String uid = SystemUUIDGenerator.getUUID();
		globalConfiguration.setId(new Long(System.currentTimeMillis()).longValue());
				setGlobalConfiguration(globalConfiguration);
				setCreating(true);
				//setModel(lst);
			}

			addWebAuditLogEntry("Delete", new StringBuilder("Button Label: Delete").append(". Deleted Content: ID = ").append(previousGlobalConfiguration.getId()).append(". Values: ").append(previousGlobalConfiguration.toString()).toString());

		}
		FacesUtils.addInfoMessage("delete_successful");
		return null;
	}

	private boolean validate(){
		boolean hasErrors = false;
		// Validate blank fields


		Integer startHourOfDay = null;
		try {
			startHourOfDay = getGlobalConfiguration().getStartHourOfDay();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(startHourOfDay == null){
			FacesUtils.addErrorMessage(null,"blank_field",new Object[]{"StartHourOfDay"});
			hasErrors = true;
		}

		Integer stopHourOfDay = null;
		try {
			stopHourOfDay = getGlobalConfiguration().getStopHourOfDay();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(stopHourOfDay == null){
			FacesUtils.addErrorMessage(null,"blank_field",new Object[]{"StopHourOfDay"});
			hasErrors = true;
		}

		Integer maxNumberOfTries = null;
		try {
			maxNumberOfTries = getGlobalConfiguration().getMaxNumberOfTries();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(maxNumberOfTries == null){
			FacesUtils.addErrorMessage(null,"blank_field",new Object[]{"MaxNumberOfTries"});
			hasErrors = true;
		}

		Integer c24ConnectionTimeOut = null;
		try {
			c24ConnectionTimeOut = getGlobalConfiguration().getC24ConnectionTimeOut();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(c24ConnectionTimeOut == null){
			FacesUtils.addErrorMessage(null,"blank_field",new Object[]{"C24ConnectionTimeOut"});
			hasErrors = true;
		}

		Integer recordFetchSize = null;
		try {
			recordFetchSize = getGlobalConfiguration().getRecordFetchSize();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(recordFetchSize == null){
			FacesUtils.addErrorMessage(null,"blank_field",new Object[]{"RecordFetchSize"});
			hasErrors = true;
		}

		Integer recordProcessSize = null;
		try {
			recordProcessSize = getGlobalConfiguration().getRecordProcessSize();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(recordProcessSize == null){
			FacesUtils.addErrorMessage(null,"blank_field",new Object[]{"RecordProcessSize"});
			hasErrors = true;
		}

		Integer recordProcessPriority = null;
		try {
			recordProcessPriority = getGlobalConfiguration().getRecordProcessPriority();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(recordProcessPriority == null){
			FacesUtils.addErrorMessage(null,"blank_field",new Object[]{"RecordProcessPriority"});
			hasErrors = true;
		}

		Integer numberOfProcessThreads = null;
		try {
			numberOfProcessThreads = getGlobalConfiguration().getNumberOfProcessThreads();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(numberOfProcessThreads == null){
			FacesUtils.addErrorMessage(null,"blank_field",new Object[]{"NumberOfProcessThreads"});
			hasErrors = true;
		}




	GlobalConfigurationDAO dao = new GlobalConfigurationDAO();
	Collection col = null;
	Object fieldValue = null;
	boolean isBlankField = false;


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
	 * @return the GlobalConfiguration
	 */
	public GlobalConfiguration getGlobalConfiguration() {
		return globalConfiguration;
	}
	/**
	 * @param globalConfiguration the GlobalConfiguration to set
	 */
	public void setGlobalConfiguration(GlobalConfiguration globalConfiguration) {
		this.globalConfiguration = globalConfiguration;
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
	public List<GlobalConfiguration> getModel() {
		return model;
	}
	/**
	 * @param model the model to set
	 */
	public void setModel(List<GlobalConfiguration> model) {
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


	
}

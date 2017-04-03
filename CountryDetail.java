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
import com.expertedge.uba.collection.engine.jpa.Country;
import com.expertedge.uba.collection.engine.jpa.Currency;
import com.geniunwit.security.util.UserManagementUtil;
import org.apache.commons.lang.SerializationUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author <a href="mailto:jniyiooster@gmail.com">Olaniyi Osunsanya</a>
 */
@Scope("session")
@Controller
public class CountryDetail {

	private Country country;
	private boolean dirty;



	private Integer firstRowIndex;
	private Integer currentRowIndex;
	private Integer rowCount;
	private Integer rows;

	private List<Country> model;
	private boolean creating = false;
	private boolean submitted;
	private Date dt;
	private SimpleDateFormat sdateFormat = new SimpleDateFormat("dd/MM/yyyy");

	CurrencyDAO currencyDao = new CurrencyDAO();
	List<Currency> currencys;


	private WebAuditLogDAO webAuditLogDAO = new WebAuditLogDAO();
	@Autowired
	private UserManagementUtil userManagerUtil;

	public void init(Country country){
		setDirty(false);
		setCountry(country);
		model = new ArrayList();
		dt = new Date();
	}
	public CountryDetail() {
		create();
		model = new ArrayList();
	}

	@RequiresPermissions("*:create")
	public String create(){
		Country country = new Country();
		String uid = SystemUUIDGenerator.getUUID();
		country.setId(new Long(System.currentTimeMillis()).longValue());
		setCountry(country);
		setCreating(true);

		addWebAuditLogEntry("Create", "Button Label: New");

		return null;
	}

	@RequiresPermissions("*:create")
	public String duplicate(){
		Country country = (Country)getCountry().clone();
		country.setId(null);
		setCountry(country);
		setCreating(true);
		FacesUtils.addInfoMessage("duplication_successful");

		addWebAuditLogEntry("Duplicate", new StringBuilder("Button Label: Duplicate").append(". Previous Content: ID = ").append(getCountry().getId()).append(". Values: ").append(getCountry().toString()).toString());

		return null;
	}

	private void addWebAuditLogEntry(String actionPerformed, String actionDetail){
		if(null != userManagerUtil){
	        String actionSource = "COUNTRY";
	        String uri = "/CountryDetailED.xhtml";
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
		int index = lst.indexOf(getCountry());
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
		Country country = (Country)lst.get(index);
		setCountry(country);
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
		int index = lst.indexOf(getCountry());
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
		Country country = (Country)lst.get(index);
		setCountry(country);
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
		int index = lst.indexOf(getCountry());
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
		Country country = (Country)lst.get(index);
		setCountry(country);
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
		int index = lst.indexOf(getCountry());
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
		Country country = (Country)lst.get(index);
		setCountry(country);
		setCreating(false);
		
		setCurrentRowIndex(index);
		int findex = getFirstPageIndex(index);
		setFirstRowIndex(findex);
		
		return null;
	}
	public String list(){
		CountryList countryList = null;
		try {
			countryList = FacesUtils.findBean("countryList", CountryList.class);
			countryList.setList(getModel());
			countryList.setFirstRowIndex(getFirstRowIndex());
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return "/views/CountryListED";
	}

	@RequiresPermissions("*:edit,create")
	public String save(){
		boolean validated = validate();
		if(validated == false)return null;

		CountryDAO dao = new CountryDAO();
		EntityManagerHelper.beginTransaction();
		if(isCreating()){
			dao.save(getCountry());
			List lst = getModel();
			lst.add(getCountry());
			//setModel(lst);
			setCreating(false);

			addWebAuditLogEntry("Save", new StringBuilder("Button Label: Save").append(". New Content: ID = ").append(getCountry().getId()).append(". Values: ").append(getCountry().toString()).toString());

		}
		else {
			dao.update(getCountry());

			addWebAuditLogEntry("Update", new StringBuilder("Button Label: Save").append(". New Content: ID = ").append(getCountry().getId()).append(". Values: ").append(getCountry().toString()).toString());

		}
		EntityManagerHelper.commit();
		if(isCreating())setCreating(false);
		FacesUtils.addInfoMessage("save_successful");
		return null;
	}
	
	@RequiresPermissions("*:delete")
	public String delete(){
		CountryDAO dao = new CountryDAO();
		if(isCreating()){
			FacesUtils.addErrorMessage(null,"delete_not_successful",new Object[]{" Nothing to delete."});
			return null;
		}
		else {
			Country country = getCountry();
			Country previousCountry = (Country)getCountry().clone();
			List lst = model;
			int rowCount = lst.size();
			int lastIndex = rowCount - 1;
			int index = lst.indexOf(country);
			if(rowCount == 0 || index == -1){
				FacesUtils.addErrorMessage("no_row_selection");
				return null;
			}
			EntityManagerHelper.beginTransaction();
			dao.delete(country);
			EntityManagerHelper.commit();
			lst.remove(country);

			country = null;
			try {
				country = (Country)lst.get(index);
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
			if(country != null){
				setCountry(country);
				setCreating(false);
				//setModel(lst);
			}
			else {
				country = new Country();
				String uid = SystemUUIDGenerator.getUUID();
		country.setId(new Long(System.currentTimeMillis()).longValue());
				setCountry(country);
				setCreating(true);
				//setModel(lst);
			}

			addWebAuditLogEntry("Delete", new StringBuilder("Button Label: Delete").append(". Deleted Content: ID = ").append(previousCountry.getId()).append(". Values: ").append(previousCountry.toString()).toString());

		}
		FacesUtils.addInfoMessage("delete_successful");
		return null;
	}

	private boolean validate(){
		boolean hasErrors = false;
		// Validate blank fields


		String name = getCountry().getName();
		if(name == null || name.trim().equals("")){
			FacesUtils.addErrorMessage(null,"blank_field",new Object[]{"Name"});
			hasErrors = true;
		}

		String isoCode = getCountry().getIsoCode();
		if(isoCode == null || isoCode.trim().equals("")){
			FacesUtils.addErrorMessage(null,"blank_field",new Object[]{"IsoCode"});
			hasErrors = true;
		}

		Currency currency = null;
		try {
			currency = getCountry().getCurrency();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(currency == null){
			FacesUtils.addErrorMessage(null,"blank_field",new Object[]{"Currency"});
			hasErrors = true;
		}




	CountryDAO dao = new CountryDAO();
	Collection col = null;
	Object fieldValue = null;
	boolean isBlankField = false;

	col = null;
	fieldValue = null;
	fieldValue = getCountry().getName();
	isBlankField = false;
	if(fieldValue instanceof String){
		isBlankField = (fieldValue == null || "".equals(fieldValue));
	}
	else {
		isBlankField = (fieldValue == null);
	}

	if(isBlankField == false){
		try {
			col = dao.findByName(fieldValue);
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		if(isCreating()){
			// Disallow duplicate Country name
			if(null != col && col.size() > 0){
					FacesUtils.addErrorMessage(null,"duplicate_value",new Object[]{": Country -> name already exists"});
					hasErrors = true;
			}
		}
		else {
			// If record is being modified, ensure that it is the same as that found by the dao
			// otherwise, a duplicate code can exist by assigning code the value in another record
			if(null != col && col.size() > 1){
					FacesUtils.addErrorMessage(null,"duplicate_value",new Object[]{": Country -> name already exists"});
					hasErrors = true;
			}
			else if(null != col && col.size() == 1){
				Object db_pk = getCountry().getId();
				Object en_pk = null;
				Country tcountry = (Country)col.iterator().next();
				en_pk = tcountry.getId();
				if(db_pk.equals(en_pk) == false){
					FacesUtils.addErrorMessage(null,"duplicate_value",new Object[]{": Country -> name already exists"});
					hasErrors = true;
				}
			}
		}
	}

	col = null;
	fieldValue = null;
	fieldValue = getCountry().getIsoCode();
	isBlankField = false;
	if(fieldValue instanceof String){
		isBlankField = (fieldValue == null || "".equals(fieldValue));
	}
	else {
		isBlankField = (fieldValue == null);
	}

	if(isBlankField == false){
		try {
			col = dao.findByIsoCode(fieldValue);
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		if(isCreating()){
			// Disallow duplicate Country isoCode
			if(null != col && col.size() > 0){
					FacesUtils.addErrorMessage(null,"duplicate_value",new Object[]{": Country -> isoCode already exists"});
					hasErrors = true;
			}
		}
		else {
			// If record is being modified, ensure that it is the same as that found by the dao
			// otherwise, a duplicate code can exist by assigning code the value in another record
			if(null != col && col.size() > 1){
					FacesUtils.addErrorMessage(null,"duplicate_value",new Object[]{": Country -> isoCode already exists"});
					hasErrors = true;
			}
			else if(null != col && col.size() == 1){
				Object db_pk = getCountry().getId();
				Object en_pk = null;
				Country tcountry = (Country)col.iterator().next();
				en_pk = tcountry.getId();
				if(db_pk.equals(en_pk) == false){
					FacesUtils.addErrorMessage(null,"duplicate_value",new Object[]{": Country -> isoCode already exists"});
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
	 * @return the Country
	 */
	public Country getCountry() {
		return country;
	}
	/**
	 * @param country the Country to set
	 */
	public void setCountry(Country country) {
		this.country = country;
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
	public List<Country> getModel() {
		return model;
	}
	/**
	 * @param model the model to set
	 */
	public void setModel(List<Country> model) {
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


	public List<Currency> getCurrencys() {
		if(null == currencys){
			currencys = new ArrayList<Currency>();
			CurrencyDAO currencyDao = new CurrencyDAO();
			try { 
				currencys = currencyDao.findAll();			
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
		}
		return currencys;
	}
	public void setCurrencys(List<Currency> currencys) {
		this.currencys = currencys;
	}

	
}

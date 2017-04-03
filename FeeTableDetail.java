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
import com.expertedge.uba.collection.engine.jpa.FeeTable;
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
public class FeeTableDetail {

	private FeeTable feeTable;
	private boolean dirty;



	private Integer firstRowIndex;
	private Integer currentRowIndex;
	private Integer rowCount;
	private Integer rows;

	private List<FeeTable> model;
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

	public void init(FeeTable feeTable){
		setDirty(false);
		setFeeTable(feeTable);
		model = new ArrayList();
		dt = new Date();
	}
	public FeeTableDetail() {
		create();
		model = new ArrayList();
	}

	@RequiresPermissions("*:create")
	public String create(){
		FeeTable feeTable = new FeeTable();
		String uid = SystemUUIDGenerator.getUUID();
		feeTable.setId(new Long(System.currentTimeMillis()).longValue());
		setFeeTable(feeTable);
		setCreating(true);

		addWebAuditLogEntry("Create", "Button Label: New");

		return null;
	}

	@RequiresPermissions("*:create")
	public String duplicate(){
		FeeTable feeTable = (FeeTable)getFeeTable().clone();
		feeTable.setId(null);
		setFeeTable(feeTable);
		setCreating(true);
		FacesUtils.addInfoMessage("duplication_successful");

		addWebAuditLogEntry("Duplicate", new StringBuilder("Button Label: Duplicate").append(". Previous Content: ID = ").append(getFeeTable().getId()).append(". Values: ").append(getFeeTable().toString()).toString());

		return null;
	}

	private void addWebAuditLogEntry(String actionPerformed, String actionDetail){
		if(null != userManagerUtil){
	        String actionSource = "FEE_TABLE";
	        String uri = "/FeeTableDetailED.xhtml";
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
		int index = lst.indexOf(getFeeTable());
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
		FeeTable feeTable = (FeeTable)lst.get(index);
		setFeeTable(feeTable);
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
		int index = lst.indexOf(getFeeTable());
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
		FeeTable feeTable = (FeeTable)lst.get(index);
		setFeeTable(feeTable);
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
		int index = lst.indexOf(getFeeTable());
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
		FeeTable feeTable = (FeeTable)lst.get(index);
		setFeeTable(feeTable);
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
		int index = lst.indexOf(getFeeTable());
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
		FeeTable feeTable = (FeeTable)lst.get(index);
		setFeeTable(feeTable);
		setCreating(false);
		
		setCurrentRowIndex(index);
		int findex = getFirstPageIndex(index);
		setFirstRowIndex(findex);
		
		return null;
	}
	public String list(){
		FeeTableList feeTableList = null;
		try {
			feeTableList = FacesUtils.findBean("feeTableList", FeeTableList.class);
			feeTableList.setList(getModel());
			feeTableList.setFirstRowIndex(getFirstRowIndex());
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return "/views/FeeTableListED";
	}

	@RequiresPermissions("*:edit,create")
	public String save(){
		boolean validated = validate();
		if(validated == false)return null;

		FeeTableDAO dao = new FeeTableDAO();
		EntityManagerHelper.beginTransaction();
		if(isCreating()){
			dao.save(getFeeTable());
			List lst = getModel();
			lst.add(getFeeTable());
			//setModel(lst);
			setCreating(false);

			addWebAuditLogEntry("Save", new StringBuilder("Button Label: Save").append(". New Content: ID = ").append(getFeeTable().getId()).append(". Values: ").append(getFeeTable().toString()).toString());

		}
		else {
			dao.update(getFeeTable());

			addWebAuditLogEntry("Update", new StringBuilder("Button Label: Save").append(". New Content: ID = ").append(getFeeTable().getId()).append(". Values: ").append(getFeeTable().toString()).toString());

		}
		EntityManagerHelper.commit();
		if(isCreating())setCreating(false);
		FacesUtils.addInfoMessage("save_successful");
		return null;
	}
	
	@RequiresPermissions("*:delete")
	public String delete(){
		FeeTableDAO dao = new FeeTableDAO();
		if(isCreating()){
			FacesUtils.addErrorMessage(null,"delete_not_successful",new Object[]{" Nothing to delete."});
			return null;
		}
		else {
			FeeTable feeTable = getFeeTable();
			FeeTable previousFeeTable = (FeeTable)getFeeTable().clone();
			List lst = model;
			int rowCount = lst.size();
			int lastIndex = rowCount - 1;
			int index = lst.indexOf(feeTable);
			if(rowCount == 0 || index == -1){
				FacesUtils.addErrorMessage("no_row_selection");
				return null;
			}
			EntityManagerHelper.beginTransaction();
			dao.delete(feeTable);
			EntityManagerHelper.commit();
			lst.remove(feeTable);

			feeTable = null;
			try {
				feeTable = (FeeTable)lst.get(index);
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
			if(feeTable != null){
				setFeeTable(feeTable);
				setCreating(false);
				//setModel(lst);
			}
			else {
				feeTable = new FeeTable();
				String uid = SystemUUIDGenerator.getUUID();
		feeTable.setId(new Long(System.currentTimeMillis()).longValue());
				setFeeTable(feeTable);
				setCreating(true);
				//setModel(lst);
			}

			addWebAuditLogEntry("Delete", new StringBuilder("Button Label: Delete").append(". Deleted Content: ID = ").append(previousFeeTable.getId()).append(". Values: ").append(previousFeeTable.toString()).toString());

		}
		FacesUtils.addInfoMessage("delete_successful");
		return null;
	}

	private boolean validate(){
		boolean hasErrors = false;
		// Validate blank fields


		Product product = null;
		try {
			product = getFeeTable().getProduct();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(product == null){
			FacesUtils.addErrorMessage(null,"blank_field",new Object[]{"Product"});
			hasErrors = true;
		}

		String confCode = getFeeTable().getConfCode();
		if(confCode == null || confCode.trim().equals("")){
			FacesUtils.addErrorMessage(null,"blank_field",new Object[]{"ConfCode"});
			hasErrors = true;
		}




	FeeTableDAO dao = new FeeTableDAO();
	Collection col = null;
	Object fieldValue = null;
	boolean isBlankField = false;

	col = null;
	fieldValue = null;
	fieldValue = getFeeTable().getProduct();
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
			// Disallow duplicate FeeTable product
			if(null != col && col.size() > 0){
					FacesUtils.addErrorMessage(null,"duplicate_value",new Object[]{": FeeTable -> product already exists"});
					hasErrors = true;
			}
		}
		else {
			// If record is being modified, ensure that it is the same as that found by the dao
			// otherwise, a duplicate code can exist by assigning code the value in another record
			if(null != col && col.size() > 1){
					FacesUtils.addErrorMessage(null,"duplicate_value",new Object[]{": FeeTable -> product already exists"});
					hasErrors = true;
			}
			else if(null != col && col.size() == 1){
				Object db_pk = getFeeTable().getId();
				Object en_pk = null;
				FeeTable tfeeTable = (FeeTable)col.iterator().next();
				en_pk = tfeeTable.getId();
				if(db_pk.equals(en_pk) == false){
					FacesUtils.addErrorMessage(null,"duplicate_value",new Object[]{": FeeTable -> product already exists"});
					hasErrors = true;
				}
			}
		}
	}

	col = null;
	fieldValue = null;
	fieldValue = getFeeTable().getConfCode();
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
			// Disallow duplicate FeeTable confCode
			if(null != col && col.size() > 0){
					FacesUtils.addErrorMessage(null,"duplicate_value",new Object[]{": FeeTable -> confCode already exists"});
					hasErrors = true;
			}
		}
		else {
			// If record is being modified, ensure that it is the same as that found by the dao
			// otherwise, a duplicate code can exist by assigning code the value in another record
			if(null != col && col.size() > 1){
					FacesUtils.addErrorMessage(null,"duplicate_value",new Object[]{": FeeTable -> confCode already exists"});
					hasErrors = true;
			}
			else if(null != col && col.size() == 1){
				Object db_pk = getFeeTable().getId();
				Object en_pk = null;
				FeeTable tfeeTable = (FeeTable)col.iterator().next();
				en_pk = tfeeTable.getId();
				if(db_pk.equals(en_pk) == false){
					FacesUtils.addErrorMessage(null,"duplicate_value",new Object[]{": FeeTable -> confCode already exists"});
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
	 * @return the FeeTable
	 */
	public FeeTable getFeeTable() {
		return feeTable;
	}
	/**
	 * @param feeTable the FeeTable to set
	 */
	public void setFeeTable(FeeTable feeTable) {
		this.feeTable = feeTable;
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
	public List<FeeTable> getModel() {
		return model;
	}
	/**
	 * @param model the model to set
	 */
	public void setModel(List<FeeTable> model) {
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

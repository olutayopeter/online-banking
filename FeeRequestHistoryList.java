package com.expertedge.uba.collection.engine.beans;

import java.text.SimpleDateFormat;
import java.util.*;

import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.faces.model.*;

import com.expertedge.uba.collection.engine.jpa.EntityManagerHelper;
import com.expertedge.uba.collection.engine.util.LogHelper;
import org.apache.commons.lang.BooleanUtils;
import com.expertedge.uba.collection.engine.dao.*;
import com.expertedge.uba.collection.engine.util.*;
import com.expertedge.uba.collection.engine.jpa.*;
import com.expertedge.uba.collection.engine.jpa.FeeRequestHistory;

import com.geniunwit.security.util.UserManagementUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;


/**
 *
 * @author <a href="mailto:jniyiooster@gmail.com">Olaniyi Osunsanya</a>
 */
@Scope("session")
@Controller
public class FeeRequestHistoryList {

	private Long searchRecordId;
	private boolean includeSearchRecordId;
	private Calendar searchTranDate;
	private Calendar searchCreationTime;
	private String searchStatusFlag;
	private String searchResponseMesg;
	private String searchSolId;
	private String searchTranType;
	private String searchTranDetails;
	private String searchProductName;
	private String searchCountryName;




	private Integer firstRowIndex;
	private Integer currentRowIndex;
	private Integer rowCount;
	private Integer rows;
	
	private HashMap<String,Object> parameters;
	private HashMap<String,Object> searchFields;
	private List<FeeRequestHistory> model;
	private String sortColumnName;
	private boolean ascending;
	private String oldSort;
	private boolean oldAscending;
	private SimpleDateFormat sdateFormat = new SimpleDateFormat("yyyy-MM-dd");

	private WebAuditLogDAO webAuditLogDAO = new WebAuditLogDAO();
	@Autowired
	private UserManagementUtil userManagerUtil;

	public List getList() {
		if (!oldSort.equals(sortColumnName) ||
	            oldAscending != ascending){
	            sort();
	            oldSort = sortColumnName;
	            oldAscending = ascending;
	        }
		return model;
	}

	public void setList(List<FeeRequestHistory> list) {
		this.model = list;
	}
	public void init(){
		FacesContext fcontext = FacesContext.getCurrentInstance();
		ExternalContext extcontext = fcontext.getExternalContext();
		FeeRequestHistoryDAO dao = new FeeRequestHistoryDAO();
		List feeRequestHistorys = null;
		
		sortColumnName = "countryName";
		ascending = true;
		oldSort = "countryName";
		oldAscending = true;
		model = new ArrayList();
		try {
			feeRequestHistorys = dao.findAll();
			setList(feeRequestHistorys);
			sort();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}
	public FeeRequestHistoryList() {
		init();
	}

	private void addWebAuditLogEntry(String actionPerformed, String actionDetail){
		if(null != userManagerUtil){
	        String actionSource = "FEE_REQUEST_HISTORY";
	        String uri = "/FeeRequestHistoryListED.xhtml";
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

	public String search(){
		StringBuilder builder = new StringBuilder("select model from FeeRequestHistory model where ");
		int initialSize = builder.length();
		Object srcValue = null;
		boolean isDate = false;
		boolean isEnum = false;
		boolean isNumber = false;
		boolean isBoolean = false;
    

        srcValue = getSearchRecordId();
        if(null != srcValue && "".equals(srcValue) == false){
        	isDate = (srcValue instanceof java.util.Date) || (srcValue instanceof java.util.Calendar);
        	isEnum = (srcValue instanceof java.lang.Enum);
        	isNumber = (srcValue instanceof java.lang.Number);
        	isBoolean = (srcValue instanceof java.lang.Boolean);
        	if(isDate){
        		if(srcValue instanceof java.util.Date){
        			srcValue = sdateFormat.format(((java.util.Date)srcValue)).toString();
        		}
        		else{
        			srcValue = sdateFormat.format(((java.util.Calendar)srcValue).getTime()).toString();
        		}
        	}
        	else if(isEnum){
        		if("blank".equalsIgnoreCase(srcValue.toString())){
        			srcValue = null;
        		}
        	}
        	else if(isNumber){

       		
				Number numValue = (java.lang.Number)srcValue;
				
				srcValue = ((java.lang.Number)srcValue).toString();
				boolean isNought = Nought.isNought(numValue);
				if(isNought){
					if(getIncludeSearchRecordId() == false)srcValue = null;
				}
        	}
        	else{
        		srcValue = srcValue.toString();
        	}
			if(null != srcValue){
				if(isDate){
					builder.append("model.recordId = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.recordId = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.recordId = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.recordId = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.recordId) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchTranDate();
        if(null != srcValue && "".equals(srcValue) == false){
        	isDate = (srcValue instanceof java.util.Date) || (srcValue instanceof java.util.Calendar);
        	isEnum = (srcValue instanceof java.lang.Enum);
        	isNumber = (srcValue instanceof java.lang.Number);
        	isBoolean = (srcValue instanceof java.lang.Boolean);
        	if(isDate){
        		if(srcValue instanceof java.util.Date){
        			srcValue = sdateFormat.format(((java.util.Date)srcValue)).toString();
        		}
        		else{
        			srcValue = sdateFormat.format(((java.util.Calendar)srcValue).getTime()).toString();
        		}
        	}
        	else if(isEnum){
        		if("blank".equalsIgnoreCase(srcValue.toString())){
        			srcValue = null;
        		}
        	}
        	else if(isNumber){

       		
				srcValue = ((java.lang.Number)srcValue).toString();
				
        	}
        	else{
        		srcValue = srcValue.toString();
        	}
			if(null != srcValue){
				if(isDate){
					builder.append("model.tranDate = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.tranDate = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.tranDate = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.tranDate = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.tranDate) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchCreationTime();
        if(null != srcValue && "".equals(srcValue) == false){
        	isDate = (srcValue instanceof java.util.Date) || (srcValue instanceof java.util.Calendar);
        	isEnum = (srcValue instanceof java.lang.Enum);
        	isNumber = (srcValue instanceof java.lang.Number);
        	isBoolean = (srcValue instanceof java.lang.Boolean);
        	if(isDate){
        		if(srcValue instanceof java.util.Date){
        			srcValue = sdateFormat.format(((java.util.Date)srcValue)).toString();
        		}
        		else{
        			srcValue = sdateFormat.format(((java.util.Calendar)srcValue).getTime()).toString();
        		}
        	}
        	else if(isEnum){
        		if("blank".equalsIgnoreCase(srcValue.toString())){
        			srcValue = null;
        		}
        	}
        	else if(isNumber){

       		
				srcValue = ((java.lang.Number)srcValue).toString();
				
        	}
        	else{
        		srcValue = srcValue.toString();
        	}
			if(null != srcValue){
				if(isDate){
					builder.append("model.creationTime = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.creationTime = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.creationTime = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.creationTime = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.creationTime) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchStatusFlag();
        if(null != srcValue && "".equals(srcValue) == false){
        	isDate = (srcValue instanceof java.util.Date) || (srcValue instanceof java.util.Calendar);
        	isEnum = (srcValue instanceof java.lang.Enum);
        	isNumber = (srcValue instanceof java.lang.Number);
        	isBoolean = (srcValue instanceof java.lang.Boolean);
        	if(isDate){
        		if(srcValue instanceof java.util.Date){
        			srcValue = sdateFormat.format(((java.util.Date)srcValue)).toString();
        		}
        		else{
        			srcValue = sdateFormat.format(((java.util.Calendar)srcValue).getTime()).toString();
        		}
        	}
        	else if(isEnum){
        		if("blank".equalsIgnoreCase(srcValue.toString())){
        			srcValue = null;
        		}
        	}
        	else if(isNumber){

       		
				srcValue = ((java.lang.Number)srcValue).toString();
				
        	}
        	else{
        		srcValue = srcValue.toString();
        	}
			if(null != srcValue){
				if(isDate){
					builder.append("model.statusFlag = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.statusFlag = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.statusFlag = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.statusFlag = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.statusFlag) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchResponseMesg();
        if(null != srcValue && "".equals(srcValue) == false){
        	isDate = (srcValue instanceof java.util.Date) || (srcValue instanceof java.util.Calendar);
        	isEnum = (srcValue instanceof java.lang.Enum);
        	isNumber = (srcValue instanceof java.lang.Number);
        	isBoolean = (srcValue instanceof java.lang.Boolean);
        	if(isDate){
        		if(srcValue instanceof java.util.Date){
        			srcValue = sdateFormat.format(((java.util.Date)srcValue)).toString();
        		}
        		else{
        			srcValue = sdateFormat.format(((java.util.Calendar)srcValue).getTime()).toString();
        		}
        	}
        	else if(isEnum){
        		if("blank".equalsIgnoreCase(srcValue.toString())){
        			srcValue = null;
        		}
        	}
        	else if(isNumber){

       		
				srcValue = ((java.lang.Number)srcValue).toString();
				
        	}
        	else{
        		srcValue = srcValue.toString();
        	}
			if(null != srcValue){
				if(isDate){
					builder.append("model.responseMesg = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.responseMesg = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.responseMesg = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.responseMesg = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.responseMesg) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchSolId();
        if(null != srcValue && "".equals(srcValue) == false){
        	isDate = (srcValue instanceof java.util.Date) || (srcValue instanceof java.util.Calendar);
        	isEnum = (srcValue instanceof java.lang.Enum);
        	isNumber = (srcValue instanceof java.lang.Number);
        	isBoolean = (srcValue instanceof java.lang.Boolean);
        	if(isDate){
        		if(srcValue instanceof java.util.Date){
        			srcValue = sdateFormat.format(((java.util.Date)srcValue)).toString();
        		}
        		else{
        			srcValue = sdateFormat.format(((java.util.Calendar)srcValue).getTime()).toString();
        		}
        	}
        	else if(isEnum){
        		if("blank".equalsIgnoreCase(srcValue.toString())){
        			srcValue = null;
        		}
        	}
        	else if(isNumber){

       		
				srcValue = ((java.lang.Number)srcValue).toString();
				
        	}
        	else{
        		srcValue = srcValue.toString();
        	}
			if(null != srcValue){
				if(isDate){
					builder.append("model.solId = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.solId = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.solId = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.solId = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.solId) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchTranType();
        if(null != srcValue && "".equals(srcValue) == false){
        	isDate = (srcValue instanceof java.util.Date) || (srcValue instanceof java.util.Calendar);
        	isEnum = (srcValue instanceof java.lang.Enum);
        	isNumber = (srcValue instanceof java.lang.Number);
        	isBoolean = (srcValue instanceof java.lang.Boolean);
        	if(isDate){
        		if(srcValue instanceof java.util.Date){
        			srcValue = sdateFormat.format(((java.util.Date)srcValue)).toString();
        		}
        		else{
        			srcValue = sdateFormat.format(((java.util.Calendar)srcValue).getTime()).toString();
        		}
        	}
        	else if(isEnum){
        		if("blank".equalsIgnoreCase(srcValue.toString())){
        			srcValue = null;
        		}
        	}
        	else if(isNumber){

       		
				srcValue = ((java.lang.Number)srcValue).toString();
				
        	}
        	else{
        		srcValue = srcValue.toString();
        	}
			if(null != srcValue){
				if(isDate){
					builder.append("model.tranType = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.tranType = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.tranType = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.tranType = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.tranType) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchTranDetails();
        if(null != srcValue && "".equals(srcValue) == false){
        	isDate = (srcValue instanceof java.util.Date) || (srcValue instanceof java.util.Calendar);
        	isEnum = (srcValue instanceof java.lang.Enum);
        	isNumber = (srcValue instanceof java.lang.Number);
        	isBoolean = (srcValue instanceof java.lang.Boolean);
        	if(isDate){
        		if(srcValue instanceof java.util.Date){
        			srcValue = sdateFormat.format(((java.util.Date)srcValue)).toString();
        		}
        		else{
        			srcValue = sdateFormat.format(((java.util.Calendar)srcValue).getTime()).toString();
        		}
        	}
        	else if(isEnum){
        		if("blank".equalsIgnoreCase(srcValue.toString())){
        			srcValue = null;
        		}
        	}
        	else if(isNumber){

       		
				srcValue = ((java.lang.Number)srcValue).toString();
				
        	}
        	else{
        		srcValue = srcValue.toString();
        	}
			if(null != srcValue){
				if(isDate){
					builder.append("model.tranDetails = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.tranDetails = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.tranDetails = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.tranDetails = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.tranDetails) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchProductName();
        if(null != srcValue && "".equals(srcValue) == false){
        	isDate = (srcValue instanceof java.util.Date) || (srcValue instanceof java.util.Calendar);
        	isEnum = (srcValue instanceof java.lang.Enum);
        	isNumber = (srcValue instanceof java.lang.Number);
        	isBoolean = (srcValue instanceof java.lang.Boolean);
        	if(isDate){
        		if(srcValue instanceof java.util.Date){
        			srcValue = sdateFormat.format(((java.util.Date)srcValue)).toString();
        		}
        		else{
        			srcValue = sdateFormat.format(((java.util.Calendar)srcValue).getTime()).toString();
        		}
        	}
        	else if(isEnum){
        		if("blank".equalsIgnoreCase(srcValue.toString())){
        			srcValue = null;
        		}
        	}
        	else if(isNumber){

       		
				srcValue = ((java.lang.Number)srcValue).toString();
				
        	}
        	else{
        		srcValue = srcValue.toString();
        	}
			if(null != srcValue){
				if(isDate){
					builder.append("model.productName = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.productName = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.productName = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.productName = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.productName) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchCountryName();
        if(null != srcValue && "".equals(srcValue) == false){
        	isDate = (srcValue instanceof java.util.Date) || (srcValue instanceof java.util.Calendar);
        	isEnum = (srcValue instanceof java.lang.Enum);
        	isNumber = (srcValue instanceof java.lang.Number);
        	isBoolean = (srcValue instanceof java.lang.Boolean);
        	if(isDate){
        		if(srcValue instanceof java.util.Date){
        			srcValue = sdateFormat.format(((java.util.Date)srcValue)).toString();
        		}
        		else{
        			srcValue = sdateFormat.format(((java.util.Calendar)srcValue).getTime()).toString();
        		}
        	}
        	else if(isEnum){
        		if("blank".equalsIgnoreCase(srcValue.toString())){
        			srcValue = null;
        		}
        	}
        	else if(isNumber){

       		
				srcValue = ((java.lang.Number)srcValue).toString();
				
        	}
        	else{
        		srcValue = srcValue.toString();
        	}
			if(null != srcValue){
				if(isDate){
					builder.append("model.countryName = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.countryName = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.countryName = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.countryName = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.countryName) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



		String suffix = " and ";
		String where = "where";

		int finalSize = builder.length();
		if(finalSize > initialSize){
			int suffix_size = suffix.length();
			builder.setLength(builder.length() - suffix_size);
		}
		else {
			String str = builder.toString();
			str = str.replaceFirst(where, "");
			builder.setLength(0);
			builder.append(str);
		}

		String query = builder.toString();
		System.out.println("######## SEARCH QUERY: " + query);
		List lst = null;
		try {
			lst = EntityManagerHelper.getEntityManager().createQuery(query).getResultList();
		} catch (Exception e) {
			e.printStackTrace();
		}
		setList(lst);
		LogHelper.log(builder.toString(), java.util.logging.Level.INFO, null);
		return null;
	}

	@RequiresPermissions("*:create")
	public String create(){
		FeeRequestHistoryDetail feeRequestHistoryDetail = null;
		FeeRequestHistory feeRequestHistory = new FeeRequestHistory();

		String uid = SystemUUIDGenerator.getUUID();
		feeRequestHistory.setId(new Long(System.currentTimeMillis()).longValue());

		try {
			feeRequestHistoryDetail = FacesUtils.findBean("feeRequestHistoryDetail", FeeRequestHistoryDetail.class);
			feeRequestHistoryDetail.setFeeRequestHistory(feeRequestHistory);
			feeRequestHistoryDetail.setCreating(true);
			feeRequestHistoryDetail.setModel(model);
		} catch (RuntimeException e) {
			e.printStackTrace();
		}

		addWebAuditLogEntry("Create", "Button Label: New");

		return "/views/FeeRequestHistoryDetailED";
	}

	public String detail(){
		FacesContext fcontext = FacesContext.getCurrentInstance();
		UIData table = (UIData)findChildComponent(fcontext.getViewRoot(), "entity_dt");
		FeeRequestHistory feeRequestHistory = null;
		
		List lst = getList();
		int rowCount = lst.size();
		int lastIndex = rowCount - 1;
		int rows = 0;
		try {
			feeRequestHistory = (FeeRequestHistory)table.getRowData();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		int currentRowIndex = lst.indexOf(feeRequestHistory);
		
		FeeRequestHistoryDetail feeRequestHistoryDetail = null;
		try {
			feeRequestHistoryDetail = FacesUtils.findBean("feeRequestHistoryDetail", FeeRequestHistoryDetail.class);
			feeRequestHistoryDetail.init(feeRequestHistory);
			feeRequestHistoryDetail.setCreating(false);
			feeRequestHistoryDetail.setModel(model);
			
			Integer firstRowIndex = 0;
			
			firstRowIndex = table.getFirst();
			rows = table.getRows();
			try {
				feeRequestHistoryDetail.setFirstRowIndex(firstRowIndex);
				feeRequestHistoryDetail.setCurrentRowIndex(currentRowIndex);
				feeRequestHistoryDetail.setRowCount(rowCount);
				feeRequestHistoryDetail.setRows(rows);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}

		addWebAuditLogEntry("Read/View", new StringBuilder("Button Label: Detail").append(". Content: ID = ").append(feeRequestHistory.getId()).append(". Values: ").append(feeRequestHistory.toString()).toString());

		LogHelper.log("Detail link clicked: ", java.util.logging.Level.INFO, null);
		return "/views/FeeRequestHistoryDetailED";
	}

	@RequiresPermissions("*:delete")
	public String delete(){
		FeeRequestHistoryDAO dao = new FeeRequestHistoryDAO();
		List lst = getList();
		List lselect = new ArrayList();
		boolean hasSelected = false;
		Iterator it = lst.iterator();
		while (it.hasNext()) {
			FeeRequestHistory feeRequestHistory = (FeeRequestHistory) it.next();
			if(BooleanUtils.isTrue(feeRequestHistory.getSelected())){
				hasSelected = true;
				lselect.add(feeRequestHistory);
			}
		}
		if(hasSelected == false){
			String errorMessage = "no_selection";
			FacesUtils.addErrorMessage(errorMessage);
			return null;
		}

		// Save the initial size
		int beforeSize = lst.size();

		EntityManagerHelper.beginTransaction();
		// Iterate over selected list
		Iterator itselect = lselect.iterator();
		while (itselect.hasNext()) {
			FeeRequestHistory feeRequestHistory = (FeeRequestHistory) itselect.next();
			FeeRequestHistory previousFeeRequestHistory = (FeeRequestHistory)feeRequestHistory.clone();
			if(lst != null && lst.size() > 0){
				feeRequestHistory.setSelected(false);
				lst.remove(feeRequestHistory);
				dao.delete(feeRequestHistory);

				addWebAuditLogEntry("Delete", new StringBuilder("Button Label: Delete").append(". Deleted Content: ID = ").append(previousFeeRequestHistory.getId()).append(". Values: ").append(previousFeeRequestHistory.toString()).toString());

			}
		}
		EntityManagerHelper.commit();

		String errorMessage = "";
		if(lst.size() < beforeSize){
			errorMessage = "object_removed";
		}
		else {
			errorMessage = "no_object_removed";
		}
		FacesUtils.addInfoMessage(null,errorMessage,new Object[]{"FeeRequestHistory"});
		LogHelper.log("Delete button clicked: ", java.util.logging.Level.INFO, null);
		return null;
	}
	private UIComponent findChildComponent(UIComponent component, String id){
		return recurseChild(component,id);
	}
	private UIComponent recurseChild(UIComponent parent, String id){
		if(parent.getId().equals(id)){
			return parent;
		}

		if(parent.getChildCount() <= 0){
			return null;
		}

		List lst = parent.getChildren();
		for (int i = 0; i < lst.size(); i++) {
			UIComponent elem = (UIComponent)lst.get(i);
			UIComponent retValue = recurseChild(elem,id);
			if(null == retValue){
				continue;
			}
			else return retValue;
		}
		return null;
	}
	public String selectPage(){
		int firstRow = 0;
		int lastRow = 0;
		int rowCount = 0;
		int rows = 0;
		FacesContext fcontext = FacesContext.getCurrentInstance();
		UIData table = (UIData)findChildComponent(fcontext.getViewRoot(), "entity_dt");
		firstRow = table.getFirst();
		rowCount = table.getRowCount();
		rows = table.getRows();
		int x = firstRow + rows;
		if(x > rowCount)
			lastRow = rowCount - 1;
		else lastRow = x - 1;
		List list = getList();
		for (int i = firstRow; i <= lastRow; i++) {
			FeeRequestHistory feeRequestHistory = (FeeRequestHistory)list.get(i);
			if(BooleanUtils.isNotTrue(feeRequestHistory.getSelected()))
				feeRequestHistory.setSelected(true);
		}
		LogHelper.log("Select Page button clicked: ", java.util.logging.Level.INFO, null);
		return null;
	}
	public String selectAll(){
		List list = getList();
		for (int i = 0; i < list.size(); i++) {
			FeeRequestHistory feeRequestHistory = (FeeRequestHistory)list.get(i);
			if(BooleanUtils.isNotTrue(feeRequestHistory.getSelected()))
				feeRequestHistory.setSelected(true);
		}
		LogHelper.log("Select All button clicked: ", java.util.logging.Level.INFO, null);
		return null;
	}
	public String deselectPage(){
		int firstRow = 0;
		int lastRow = 0;
		int rowCount = 0;
		int rows = 0;
		FacesContext fcontext = FacesContext.getCurrentInstance();
		UIData table = (UIData)findChildComponent(fcontext.getViewRoot(), "entity_dt");
		firstRow = table.getFirst();
		rowCount = table.getRowCount();
		rows = table.getRows();
		int x = firstRow + rows;
		if(x > rowCount)
			lastRow = rowCount - 1;
		else lastRow = x - 1;
		List list = getList();
		for (int i = firstRow; i <= lastRow; i++) {
			FeeRequestHistory feeRequestHistory = (FeeRequestHistory)list.get(i);
			if(BooleanUtils.isNotFalse(feeRequestHistory.getSelected()))
				feeRequestHistory.setSelected(false);
		}
		LogHelper.log("Deselect Page button clicked: ", java.util.logging.Level.INFO, null);
		return null;
	}
	public String deselectAll(){
		List list = getList();
		for (int i = 0; i < list.size(); i++) {
			FeeRequestHistory feeRequestHistory = (FeeRequestHistory)list.get(i);
			if(BooleanUtils.isNotFalse(feeRequestHistory.getSelected()))
				feeRequestHistory.setSelected(false);
		}
		LogHelper.log("Deselect All button clicked: ", java.util.logging.Level.INFO, null);
		return null;
	}
	public String logout(){
		return null;
	}
	public String home(){
		return "/views/Login";
	}
	public void resetSearch(){
	setSearchRecordId(null);
	setIncludeSearchRecordId(false);
	setSearchTranDate(null);
	setSearchCreationTime(null);
	setSearchStatusFlag(null);
	setSearchResponseMesg(null);
	setSearchSolId(null);
	setSearchTranType(null);
	setSearchTranDetails(null);
	setSearchProductName(null);
	setSearchCountryName(null);

	}
	public String back(){
		return "back";
	}
	public String refresh(){
		EntityManagerHelper.resetEMF();
		init();
		resetSearch();
		return null;
	}
	/* private HtmlDataTable findParentHtmlDataTable(UIComponent component)
	{
		if (component == null)
		{
			return null;
		}
		if (component instanceof HtmlDataTable)
		{
			return (HtmlDataTable) component;
		}
		return findParentHtmlDataTable(component.getParent());
	} */
	/**
	 * @return the sort
	 */
	public String getSortColumnName() {
		return sortColumnName;
	}
	/**
	 * @param sort the sort to set
	 */
	public void setSortColumnName(String sortColumnName) {
		this.sortColumnName = sortColumnName;
	}
	/**
	 * @return the ascending
	 */
	public boolean isAscending() {
		return ascending;
	}
	/**
	 * @param ascending the ascending to set
	 */
	public void setAscending(boolean ascending) {
		this.ascending = ascending;
	}
    protected void sort() {
        Comparator comparator = new Comparator() {
            public int compare(Object o1, Object o2) {
                FeeRequestHistory model1 = (FeeRequestHistory) o1;
                FeeRequestHistory model2 = (FeeRequestHistory) o2;
                if (sortColumnName == null) {
                    return 0;
                }

				else if (sortColumnName.equals("recordId")) {
					int c1 = 0;
                    if(null == model1.getRecordId()){
                        return -1;
                    }
                    if(null == model2.getRecordId()){
                        return 1;
                    }
					try {
						c1 = model1.getRecordId().compareTo(model2.getRecordId());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getRecordId().compareTo(model1.getRecordId());
					} catch (Exception e) {
						c2 = -1;
					}
					return ascending ? c1 : c2;
				}
			
				else if (sortColumnName.equals("tranDate")) {
					int c1 = 0;
                    if(null == model1.getTranDate()){
                        return -1;
                    }
                    if(null == model2.getTranDate()){
                        return 1;
                    }
					try {
						c1 = model1.getTranDate().compareTo(model2.getTranDate());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getTranDate().compareTo(model1.getTranDate());
					} catch (Exception e) {
						c2 = -1;
					}
					return ascending ? c1 : c2;
				}
			
				else if (sortColumnName.equals("creationTime")) {
					int c1 = 0;
                    if(null == model1.getCreationTime()){
                        return -1;
                    }
                    if(null == model2.getCreationTime()){
                        return 1;
                    }
					try {
						c1 = model1.getCreationTime().compareTo(model2.getCreationTime());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getCreationTime().compareTo(model1.getCreationTime());
					} catch (Exception e) {
						c2 = -1;
					}
					return ascending ? c1 : c2;
				}
			
				else if (sortColumnName.equals("statusFlag")) {
					int c1 = 0;
                    if(null == model1.getStatusFlag()){
                        return -1;
                    }
                    if(null == model2.getStatusFlag()){
                        return 1;
                    }
					try {
						c1 = model1.getStatusFlag().compareTo(model2.getStatusFlag());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getStatusFlag().compareTo(model1.getStatusFlag());
					} catch (Exception e) {
						c2 = -1;
					}
					return ascending ? c1 : c2;
				}
			
				else if (sortColumnName.equals("responseMesg")) {
					int c1 = 0;
                    if(null == model1.getResponseMesg()){
                        return -1;
                    }
                    if(null == model2.getResponseMesg()){
                        return 1;
                    }
					try {
						c1 = model1.getResponseMesg().compareTo(model2.getResponseMesg());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getResponseMesg().compareTo(model1.getResponseMesg());
					} catch (Exception e) {
						c2 = -1;
					}
					return ascending ? c1 : c2;
				}
			
				else if (sortColumnName.equals("solId")) {
					int c1 = 0;
                    if(null == model1.getSolId()){
                        return -1;
                    }
                    if(null == model2.getSolId()){
                        return 1;
                    }
					try {
						c1 = model1.getSolId().compareTo(model2.getSolId());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getSolId().compareTo(model1.getSolId());
					} catch (Exception e) {
						c2 = -1;
					}
					return ascending ? c1 : c2;
				}
			
				else if (sortColumnName.equals("tranType")) {
					int c1 = 0;
                    if(null == model1.getTranType()){
                        return -1;
                    }
                    if(null == model2.getTranType()){
                        return 1;
                    }
					try {
						c1 = model1.getTranType().compareTo(model2.getTranType());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getTranType().compareTo(model1.getTranType());
					} catch (Exception e) {
						c2 = -1;
					}
					return ascending ? c1 : c2;
				}
			
				else if (sortColumnName.equals("tranDetails")) {
					int c1 = 0;
                    if(null == model1.getTranDetails()){
                        return -1;
                    }
                    if(null == model2.getTranDetails()){
                        return 1;
                    }
					try {
						c1 = model1.getTranDetails().compareTo(model2.getTranDetails());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getTranDetails().compareTo(model1.getTranDetails());
					} catch (Exception e) {
						c2 = -1;
					}
					return ascending ? c1 : c2;
				}
			
				else if (sortColumnName.equals("productName")) {
					int c1 = 0;
                    if(null == model1.getProductName()){
                        return -1;
                    }
                    if(null == model2.getProductName()){
                        return 1;
                    }
					try {
						c1 = model1.getProductName().compareTo(model2.getProductName());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getProductName().compareTo(model1.getProductName());
					} catch (Exception e) {
						c2 = -1;
					}
					return ascending ? c1 : c2;
				}
			
				else if (sortColumnName.equals("countryName")) {
					int c1 = 0;
                    if(null == model1.getCountryName()){
                        return -1;
                    }
                    if(null == model2.getCountryName()){
                        return 1;
                    }
					try {
						c1 = model1.getCountryName().compareTo(model2.getCountryName());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getCountryName().compareTo(model1.getCountryName());
					} catch (Exception e) {
						c2 = -1;
					}
					return ascending ? c1 : c2;
				}
			
                else return 0;
            }
        };
        List list = getList();
        Collections.sort(list, comparator);
    }

    
    /**
	 * Returns the value of searchRecordId.
	 */
	public Long getSearchRecordId()
	{
		return searchRecordId;
	}

	/**
	 * Sets the value of searchRecordId.
	 * @param searchRecordId The value to assign searchRecordId.
	 */
	public void setSearchRecordId(Long searchRecordId)
	{
		this.searchRecordId = searchRecordId;
	}
	
	/**
	 * Returns the value of includeSearchRecordId.
	 */
	public boolean getIncludeSearchRecordId()
	{
		return includeSearchRecordId;
	}

	/**
	 * Sets the value of includeSearchRecordId.
	 * @param includeSearchRecordId The value to assign includeSearchRecordId.
	 */
	public void setIncludeSearchRecordId(boolean includeSearchRecordId)
	{
		this.includeSearchRecordId = includeSearchRecordId;
	}

    /**
	 * Returns the value of searchTranDate.
	 */
	public Calendar getSearchTranDate()
	{
		return searchTranDate;
	}

	/**
	 * Sets the value of searchTranDate.
	 * @param searchTranDate The value to assign searchTranDate.
	 */
	public void setSearchTranDate(Calendar searchTranDate)
	{
		this.searchTranDate = searchTranDate;
	}
	
    /**
	 * Returns the value of searchCreationTime.
	 */
	public Calendar getSearchCreationTime()
	{
		return searchCreationTime;
	}

	/**
	 * Sets the value of searchCreationTime.
	 * @param searchCreationTime The value to assign searchCreationTime.
	 */
	public void setSearchCreationTime(Calendar searchCreationTime)
	{
		this.searchCreationTime = searchCreationTime;
	}
	
    /**
	 * Returns the value of searchStatusFlag.
	 */
	public String getSearchStatusFlag()
	{
		return searchStatusFlag;
	}

	/**
	 * Sets the value of searchStatusFlag.
	 * @param searchStatusFlag The value to assign searchStatusFlag.
	 */
	public void setSearchStatusFlag(String searchStatusFlag)
	{
		this.searchStatusFlag = searchStatusFlag;
	}
	
    /**
	 * Returns the value of searchResponseMesg.
	 */
	public String getSearchResponseMesg()
	{
		return searchResponseMesg;
	}

	/**
	 * Sets the value of searchResponseMesg.
	 * @param searchResponseMesg The value to assign searchResponseMesg.
	 */
	public void setSearchResponseMesg(String searchResponseMesg)
	{
		this.searchResponseMesg = searchResponseMesg;
	}
	
    /**
	 * Returns the value of searchSolId.
	 */
	public String getSearchSolId()
	{
		return searchSolId;
	}

	/**
	 * Sets the value of searchSolId.
	 * @param searchSolId The value to assign searchSolId.
	 */
	public void setSearchSolId(String searchSolId)
	{
		this.searchSolId = searchSolId;
	}
	
    /**
	 * Returns the value of searchTranType.
	 */
	public String getSearchTranType()
	{
		return searchTranType;
	}

	/**
	 * Sets the value of searchTranType.
	 * @param searchTranType The value to assign searchTranType.
	 */
	public void setSearchTranType(String searchTranType)
	{
		this.searchTranType = searchTranType;
	}
	
    /**
	 * Returns the value of searchTranDetails.
	 */
	public String getSearchTranDetails()
	{
		return searchTranDetails;
	}

	/**
	 * Sets the value of searchTranDetails.
	 * @param searchTranDetails The value to assign searchTranDetails.
	 */
	public void setSearchTranDetails(String searchTranDetails)
	{
		this.searchTranDetails = searchTranDetails;
	}
	
    /**
	 * Returns the value of searchProductName.
	 */
	public String getSearchProductName()
	{
		return searchProductName;
	}

	/**
	 * Sets the value of searchProductName.
	 * @param searchProductName The value to assign searchProductName.
	 */
	public void setSearchProductName(String searchProductName)
	{
		this.searchProductName = searchProductName;
	}
	
    /**
	 * Returns the value of searchCountryName.
	 */
	public String getSearchCountryName()
	{
		return searchCountryName;
	}

	/**
	 * Sets the value of searchCountryName.
	 * @param searchCountryName The value to assign searchCountryName.
	 */
	public void setSearchCountryName(String searchCountryName)
	{
		this.searchCountryName = searchCountryName;
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

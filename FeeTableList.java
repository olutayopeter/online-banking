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
import com.expertedge.uba.collection.engine.jpa.FeeTable;

import com.geniunwit.security.util.UserManagementUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;


/**
 *
 * @author <a href="mailto:jniyiooster@gmail.com">Olaniyi Osunsanya</a>
 */
@Scope("session")
@Controller
public class FeeTableList {

	private Long searchRecordId;
	private boolean includeSearchRecordId;
	private String searchProduct;
	private String searchDrAccount;
	private String searchDrCurrency;
	private String searchDrSolId;
	private String searchCrAccount;
	private String searchCrCurrency;
	private String searchCrSolId;
	private Double searchTranAmount;
	private boolean includeSearchTranAmount;
	private String searchTranCurrency;
	private String searchVatAccount;
	private String searchVatCurrency;
	private Double searchVatAmount;
	private boolean includeSearchVatAmount;
	private String searchTranParticular;
	private String searchCountryCode;
	private Calendar searchTranDate;
	private Calendar searchValueDate;
	private String searchPurpose;
	private String searchChargeCategory;
	private String searchBatchId;
	private String searchUploadedBy;
	private Calendar searchUploadedDate;
	private String searchPostingStatus;
	private String searchPostedBy;
	private Calendar searchPostedDate;
	private String searchErrCode;
	private String searchStan;
	private String searchStatusFlg;
	private Calendar searchProcessDate;
	private Integer searchNumberOfTries;
	private boolean includeSearchNumberOfTries;
	private String searchLastResponse;
	private String searchRemarks;
	private String searchConfCode;
	private String searchHasSpecialAccount;
	private String searchCountry;




	private Integer firstRowIndex;
	private Integer currentRowIndex;
	private Integer rowCount;
	private Integer rows;
	
	private HashMap<String,Object> parameters;
	private HashMap<String,Object> searchFields;
	private List<FeeTable> model;
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

	public void setList(List<FeeTable> list) {
		this.model = list;
	}
	public void init(){
		FacesContext fcontext = FacesContext.getCurrentInstance();
		ExternalContext extcontext = fcontext.getExternalContext();
		FeeTableDAO dao = new FeeTableDAO();
		List feeTables = null;
		
		sortColumnName = "country";
		ascending = true;
		oldSort = "country";
		oldAscending = true;
		model = new ArrayList();
		try {
			feeTables = dao.findAll();
			setList(feeTables);
			sort();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}
	public FeeTableList() {
		init();
	}

	private void addWebAuditLogEntry(String actionPerformed, String actionDetail){
		if(null != userManagerUtil){
	        String actionSource = "FEE_TABLE";
	        String uri = "/FeeTableListED.xhtml";
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
		StringBuilder builder = new StringBuilder("select model from FeeTable model where ");
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


        srcValue = getSearchProduct();
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
        		srcValue = srcValue.toString().trim();
        	}
			if(null != srcValue){
				if(isDate){
					builder.append("model.product.name = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.product.name = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.product.name = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.product.name = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.product.name) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchDrAccount();
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
					builder.append("model.drAccount = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.drAccount = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.drAccount = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.drAccount = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.drAccount) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchDrCurrency();
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
					builder.append("model.drCurrency = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.drCurrency = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.drCurrency = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.drCurrency = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.drCurrency) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchDrSolId();
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
					builder.append("model.drSolId = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.drSolId = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.drSolId = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.drSolId = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.drSolId) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchCrAccount();
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
					builder.append("model.crAccount = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.crAccount = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.crAccount = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.crAccount = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.crAccount) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchCrCurrency();
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
					builder.append("model.crCurrency = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.crCurrency = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.crCurrency = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.crCurrency = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.crCurrency) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchCrSolId();
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
					builder.append("model.crSolId = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.crSolId = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.crSolId = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.crSolId = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.crSolId) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchTranAmount();
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
					if(getIncludeSearchTranAmount() == false)srcValue = null;
				}
        	}
        	else{
        		srcValue = srcValue.toString();
        	}
			if(null != srcValue){
				if(isDate){
					builder.append("model.tranAmount = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.tranAmount = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.tranAmount = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.tranAmount = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.tranAmount) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchTranCurrency();
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
					builder.append("model.tranCurrency = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.tranCurrency = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.tranCurrency = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.tranCurrency = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.tranCurrency) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchVatAccount();
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
					builder.append("model.vatAccount = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.vatAccount = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.vatAccount = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.vatAccount = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.vatAccount) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchVatCurrency();
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
					builder.append("model.vatCurrency = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.vatCurrency = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.vatCurrency = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.vatCurrency = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.vatCurrency) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchVatAmount();
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
					if(getIncludeSearchVatAmount() == false)srcValue = null;
				}
        	}
        	else{
        		srcValue = srcValue.toString();
        	}
			if(null != srcValue){
				if(isDate){
					builder.append("model.vatAmount = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.vatAmount = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.vatAmount = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.vatAmount = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.vatAmount) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchTranParticular();
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
					builder.append("model.tranParticular = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.tranParticular = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.tranParticular = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.tranParticular = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.tranParticular) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchCountryCode();
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
					builder.append("model.countryCode = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.countryCode = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.countryCode = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.countryCode = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.countryCode) like lower('%");
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



        srcValue = getSearchValueDate();
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
					builder.append("model.valueDate = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.valueDate = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.valueDate = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.valueDate = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.valueDate) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchPurpose();
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
					builder.append("model.purpose = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.purpose = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.purpose = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.purpose = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.purpose) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchChargeCategory();
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
					builder.append("model.chargeCategory = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.chargeCategory = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.chargeCategory = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.chargeCategory = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.chargeCategory) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchBatchId();
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
					builder.append("model.batchId = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.batchId = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.batchId = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.batchId = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.batchId) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchUploadedBy();
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
					builder.append("model.uploadedBy = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.uploadedBy = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.uploadedBy = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.uploadedBy = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.uploadedBy) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchUploadedDate();
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
					builder.append("model.uploadedDate = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.uploadedDate = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.uploadedDate = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.uploadedDate = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.uploadedDate) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchPostingStatus();
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
					builder.append("model.postingStatus = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.postingStatus = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.postingStatus = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.postingStatus = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.postingStatus) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchPostedBy();
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
					builder.append("model.postedBy = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.postedBy = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.postedBy = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.postedBy = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.postedBy) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchPostedDate();
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
					builder.append("model.postedDate = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.postedDate = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.postedDate = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.postedDate = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.postedDate) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchErrCode();
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
					builder.append("model.errCode = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.errCode = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.errCode = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.errCode = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.errCode) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchStan();
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
					builder.append("model.stan = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.stan = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.stan = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.stan = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.stan) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchStatusFlg();
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
					builder.append("model.statusFlg = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.statusFlg = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.statusFlg = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.statusFlg = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.statusFlg) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchProcessDate();
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
					builder.append("model.processDate = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.processDate = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.processDate = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.processDate = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.processDate) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchNumberOfTries();
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
					if(getIncludeSearchNumberOfTries() == false)srcValue = null;
				}
        	}
        	else{
        		srcValue = srcValue.toString();
        	}
			if(null != srcValue){
				if(isDate){
					builder.append("model.numberOfTries = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.numberOfTries = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.numberOfTries = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.numberOfTries = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.numberOfTries) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchLastResponse();
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
					builder.append("model.lastResponse = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.lastResponse = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.lastResponse = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.lastResponse = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.lastResponse) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchRemarks();
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
					builder.append("model.remarks = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.remarks = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.remarks = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.remarks = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.remarks) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchConfCode();
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
					builder.append("model.confCode = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.confCode = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.confCode = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.confCode = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.confCode) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchHasSpecialAccount();
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
					builder.append("model.hasSpecialAccount = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.hasSpecialAccount = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.hasSpecialAccount = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.hasSpecialAccount = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.hasSpecialAccount) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }


        srcValue = getSearchCountry();
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
        		srcValue = srcValue.toString().trim();
        	}
			if(null != srcValue){
				if(isDate){
					builder.append("model.country.name = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.country.name = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.country.name = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.country.name = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.country.name) like lower('%");
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
		FeeTableDetail feeTableDetail = null;
		FeeTable feeTable = new FeeTable();

		String uid = SystemUUIDGenerator.getUUID();
		feeTable.setId(new Long(System.currentTimeMillis()).longValue());

		try {
			feeTableDetail = FacesUtils.findBean("feeTableDetail", FeeTableDetail.class);
			feeTableDetail.setFeeTable(feeTable);
			feeTableDetail.setCreating(true);
			feeTableDetail.setModel(model);
		} catch (RuntimeException e) {
			e.printStackTrace();
		}

		addWebAuditLogEntry("Create", "Button Label: New");

		return "/views/FeeTableDetailED";
	}

	public String detail(){
		FacesContext fcontext = FacesContext.getCurrentInstance();
		UIData table = (UIData)findChildComponent(fcontext.getViewRoot(), "entity_dt");
		FeeTable feeTable = null;
		
		List lst = getList();
		int rowCount = lst.size();
		int lastIndex = rowCount - 1;
		int rows = 0;
		try {
			feeTable = (FeeTable)table.getRowData();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		int currentRowIndex = lst.indexOf(feeTable);
		
		FeeTableDetail feeTableDetail = null;
		try {
			feeTableDetail = FacesUtils.findBean("feeTableDetail", FeeTableDetail.class);
			feeTableDetail.init(feeTable);
			feeTableDetail.setCreating(false);
			feeTableDetail.setModel(model);
			
			Integer firstRowIndex = 0;
			
			firstRowIndex = table.getFirst();
			rows = table.getRows();
			try {
				feeTableDetail.setFirstRowIndex(firstRowIndex);
				feeTableDetail.setCurrentRowIndex(currentRowIndex);
				feeTableDetail.setRowCount(rowCount);
				feeTableDetail.setRows(rows);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}

		addWebAuditLogEntry("Read/View", new StringBuilder("Button Label: Detail").append(". Content: ID = ").append(feeTable.getId()).append(". Values: ").append(feeTable.toString()).toString());

		LogHelper.log("Detail link clicked: ", java.util.logging.Level.INFO, null);
		return "/views/FeeTableDetailED";
	}

	@RequiresPermissions("*:delete")
	public String delete(){
		FeeTableDAO dao = new FeeTableDAO();
		List lst = getList();
		List lselect = new ArrayList();
		boolean hasSelected = false;
		Iterator it = lst.iterator();
		while (it.hasNext()) {
			FeeTable feeTable = (FeeTable) it.next();
			if(BooleanUtils.isTrue(feeTable.getSelected())){
				hasSelected = true;
				lselect.add(feeTable);
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
			FeeTable feeTable = (FeeTable) itselect.next();
			FeeTable previousFeeTable = (FeeTable)feeTable.clone();
			if(lst != null && lst.size() > 0){
				feeTable.setSelected(false);
				lst.remove(feeTable);
				dao.delete(feeTable);

				addWebAuditLogEntry("Delete", new StringBuilder("Button Label: Delete").append(". Deleted Content: ID = ").append(previousFeeTable.getId()).append(". Values: ").append(previousFeeTable.toString()).toString());

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
		FacesUtils.addInfoMessage(null,errorMessage,new Object[]{"FeeTable"});
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
			FeeTable feeTable = (FeeTable)list.get(i);
			if(BooleanUtils.isNotTrue(feeTable.getSelected()))
				feeTable.setSelected(true);
		}
		LogHelper.log("Select Page button clicked: ", java.util.logging.Level.INFO, null);
		return null;
	}
	public String selectAll(){
		List list = getList();
		for (int i = 0; i < list.size(); i++) {
			FeeTable feeTable = (FeeTable)list.get(i);
			if(BooleanUtils.isNotTrue(feeTable.getSelected()))
				feeTable.setSelected(true);
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
			FeeTable feeTable = (FeeTable)list.get(i);
			if(BooleanUtils.isNotFalse(feeTable.getSelected()))
				feeTable.setSelected(false);
		}
		LogHelper.log("Deselect Page button clicked: ", java.util.logging.Level.INFO, null);
		return null;
	}
	public String deselectAll(){
		List list = getList();
		for (int i = 0; i < list.size(); i++) {
			FeeTable feeTable = (FeeTable)list.get(i);
			if(BooleanUtils.isNotFalse(feeTable.getSelected()))
				feeTable.setSelected(false);
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
	setSearchProduct(null);
	setSearchDrAccount(null);
	setSearchDrCurrency(null);
	setSearchDrSolId(null);
	setSearchCrAccount(null);
	setSearchCrCurrency(null);
	setSearchCrSolId(null);
	setSearchTranAmount(null);
	setIncludeSearchTranAmount(false);
	setSearchTranCurrency(null);
	setSearchVatAccount(null);
	setSearchVatCurrency(null);
	setSearchVatAmount(null);
	setIncludeSearchVatAmount(false);
	setSearchTranParticular(null);
	setSearchCountryCode(null);
	setSearchTranDate(null);
	setSearchValueDate(null);
	setSearchPurpose(null);
	setSearchChargeCategory(null);
	setSearchBatchId(null);
	setSearchUploadedBy(null);
	setSearchUploadedDate(null);
	setSearchPostingStatus(null);
	setSearchPostedBy(null);
	setSearchPostedDate(null);
	setSearchErrCode(null);
	setSearchStan(null);
	setSearchStatusFlg(null);
	setSearchProcessDate(null);
	setSearchNumberOfTries(null);
	setIncludeSearchNumberOfTries(false);
	setSearchLastResponse(null);
	setSearchRemarks(null);
	setSearchConfCode(null);
	setSearchHasSpecialAccount(null);
	setSearchCountry(null);

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
                FeeTable model1 = (FeeTable) o1;
                FeeTable model2 = (FeeTable) o2;
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
			
                    else if (sortColumnName.equals("product")) {
                        int c1 = 0;
                        if(null == model1.getProduct()){
                            return -1;
                        }
                        if(null == model2.getProduct()){
                            return 1;
                        }
                        try {
                            c1 = model1.getProduct().getName().compareToIgnoreCase(model2.getProduct().getName());
                        } catch (Exception e) {
                            c1 = -1;
                        }
                        int c2 = 0;
                        try {
                            c2 = model2.getProduct().getName().compareToIgnoreCase(model1.getProduct().getName());
                        } catch (Exception e) {
                            c2 = -1;
                        }
                        return ascending ? c1 : c2;
                    }
                
				else if (sortColumnName.equals("drAccount")) {
					int c1 = 0;
                    if(null == model1.getDrAccount()){
                        return -1;
                    }
                    if(null == model2.getDrAccount()){
                        return 1;
                    }
					try {
						c1 = model1.getDrAccount().compareTo(model2.getDrAccount());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getDrAccount().compareTo(model1.getDrAccount());
					} catch (Exception e) {
						c2 = -1;
					}
					return ascending ? c1 : c2;
				}
			
				else if (sortColumnName.equals("drCurrency")) {
					int c1 = 0;
                    if(null == model1.getDrCurrency()){
                        return -1;
                    }
                    if(null == model2.getDrCurrency()){
                        return 1;
                    }
					try {
						c1 = model1.getDrCurrency().compareTo(model2.getDrCurrency());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getDrCurrency().compareTo(model1.getDrCurrency());
					} catch (Exception e) {
						c2 = -1;
					}
					return ascending ? c1 : c2;
				}
			
				else if (sortColumnName.equals("tranAmount")) {
					int c1 = 0;
                    if(null == model1.getTranAmount()){
                        return -1;
                    }
                    if(null == model2.getTranAmount()){
                        return 1;
                    }
					try {
						c1 = model1.getTranAmount().compareTo(model2.getTranAmount());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getTranAmount().compareTo(model1.getTranAmount());
					} catch (Exception e) {
						c2 = -1;
					}
					return ascending ? c1 : c2;
				}
			
				else if (sortColumnName.equals("tranCurrency")) {
					int c1 = 0;
                    if(null == model1.getTranCurrency()){
                        return -1;
                    }
                    if(null == model2.getTranCurrency()){
                        return 1;
                    }
					try {
						c1 = model1.getTranCurrency().compareTo(model2.getTranCurrency());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getTranCurrency().compareTo(model1.getTranCurrency());
					} catch (Exception e) {
						c2 = -1;
					}
					return ascending ? c1 : c2;
				}
			
				else if (sortColumnName.equals("vatAccount")) {
					int c1 = 0;
                    if(null == model1.getVatAccount()){
                        return -1;
                    }
                    if(null == model2.getVatAccount()){
                        return 1;
                    }
					try {
						c1 = model1.getVatAccount().compareTo(model2.getVatAccount());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getVatAccount().compareTo(model1.getVatAccount());
					} catch (Exception e) {
						c2 = -1;
					}
					return ascending ? c1 : c2;
				}
			
				else if (sortColumnName.equals("vatCurrency")) {
					int c1 = 0;
                    if(null == model1.getVatCurrency()){
                        return -1;
                    }
                    if(null == model2.getVatCurrency()){
                        return 1;
                    }
					try {
						c1 = model1.getVatCurrency().compareTo(model2.getVatCurrency());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getVatCurrency().compareTo(model1.getVatCurrency());
					} catch (Exception e) {
						c2 = -1;
					}
					return ascending ? c1 : c2;
				}
			
				else if (sortColumnName.equals("vatAmount")) {
					int c1 = 0;
                    if(null == model1.getVatAmount()){
                        return -1;
                    }
                    if(null == model2.getVatAmount()){
                        return 1;
                    }
					try {
						c1 = model1.getVatAmount().compareTo(model2.getVatAmount());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getVatAmount().compareTo(model1.getVatAmount());
					} catch (Exception e) {
						c2 = -1;
					}
					return ascending ? c1 : c2;
				}
			
				else if (sortColumnName.equals("tranParticular")) {
					int c1 = 0;
                    if(null == model1.getTranParticular()){
                        return -1;
                    }
                    if(null == model2.getTranParticular()){
                        return 1;
                    }
					try {
						c1 = model1.getTranParticular().compareTo(model2.getTranParticular());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getTranParticular().compareTo(model1.getTranParticular());
					} catch (Exception e) {
						c2 = -1;
					}
					return ascending ? c1 : c2;
				}
			
				else if (sortColumnName.equals("countryCode")) {
					int c1 = 0;
                    if(null == model1.getCountryCode()){
                        return -1;
                    }
                    if(null == model2.getCountryCode()){
                        return 1;
                    }
					try {
						c1 = model1.getCountryCode().compareTo(model2.getCountryCode());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getCountryCode().compareTo(model1.getCountryCode());
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
			
				else if (sortColumnName.equals("purpose")) {
					int c1 = 0;
                    if(null == model1.getPurpose()){
                        return -1;
                    }
                    if(null == model2.getPurpose()){
                        return 1;
                    }
					try {
						c1 = model1.getPurpose().compareTo(model2.getPurpose());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getPurpose().compareTo(model1.getPurpose());
					} catch (Exception e) {
						c2 = -1;
					}
					return ascending ? c1 : c2;
				}
			
				else if (sortColumnName.equals("chargeCategory")) {
					int c1 = 0;
                    if(null == model1.getChargeCategory()){
                        return -1;
                    }
                    if(null == model2.getChargeCategory()){
                        return 1;
                    }
					try {
						c1 = model1.getChargeCategory().compareTo(model2.getChargeCategory());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getChargeCategory().compareTo(model1.getChargeCategory());
					} catch (Exception e) {
						c2 = -1;
					}
					return ascending ? c1 : c2;
				}
			
				else if (sortColumnName.equals("statusFlg")) {
					int c1 = 0;
                    if(null == model1.getStatusFlg()){
                        return -1;
                    }
                    if(null == model2.getStatusFlg()){
                        return 1;
                    }
					try {
						c1 = model1.getStatusFlg().compareTo(model2.getStatusFlg());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getStatusFlg().compareTo(model1.getStatusFlg());
					} catch (Exception e) {
						c2 = -1;
					}
					return ascending ? c1 : c2;
				}
			
				else if (sortColumnName.equals("confCode")) {
					int c1 = 0;
                    if(null == model1.getConfCode()){
                        return -1;
                    }
                    if(null == model2.getConfCode()){
                        return 1;
                    }
					try {
						c1 = model1.getConfCode().compareTo(model2.getConfCode());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getConfCode().compareTo(model1.getConfCode());
					} catch (Exception e) {
						c2 = -1;
					}
					return ascending ? c1 : c2;
				}
			
                    else if (sortColumnName.equals("country")) {
                        int c1 = 0;
                        if(null == model1.getCountry()){
                            return -1;
                        }
                        if(null == model2.getCountry()){
                            return 1;
                        }
                        try {
                            c1 = model1.getCountry().getName().compareToIgnoreCase(model2.getCountry().getName());
                        } catch (Exception e) {
                            c1 = -1;
                        }
                        int c2 = 0;
                        try {
                            c2 = model2.getCountry().getName().compareToIgnoreCase(model1.getCountry().getName());
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
	 * Returns the value of searchProduct.
	 */
	public String getSearchProduct()
	{
		return searchProduct;
	}

	/**
	 * Sets the value of searchProduct.
	 * @param searchProduct The value to assign searchProduct.
	 */
	public void setSearchProduct(String searchProduct)
	{
		this.searchProduct = searchProduct;
	}
	
    /**
	 * Returns the value of searchDrAccount.
	 */
	public String getSearchDrAccount()
	{
		return searchDrAccount;
	}

	/**
	 * Sets the value of searchDrAccount.
	 * @param searchDrAccount The value to assign searchDrAccount.
	 */
	public void setSearchDrAccount(String searchDrAccount)
	{
		this.searchDrAccount = searchDrAccount;
	}
	
    /**
	 * Returns the value of searchDrCurrency.
	 */
	public String getSearchDrCurrency()
	{
		return searchDrCurrency;
	}

	/**
	 * Sets the value of searchDrCurrency.
	 * @param searchDrCurrency The value to assign searchDrCurrency.
	 */
	public void setSearchDrCurrency(String searchDrCurrency)
	{
		this.searchDrCurrency = searchDrCurrency;
	}
	
    /**
	 * Returns the value of searchDrSolId.
	 */
	public String getSearchDrSolId()
	{
		return searchDrSolId;
	}

	/**
	 * Sets the value of searchDrSolId.
	 * @param searchDrSolId The value to assign searchDrSolId.
	 */
	public void setSearchDrSolId(String searchDrSolId)
	{
		this.searchDrSolId = searchDrSolId;
	}
	
    /**
	 * Returns the value of searchCrAccount.
	 */
	public String getSearchCrAccount()
	{
		return searchCrAccount;
	}

	/**
	 * Sets the value of searchCrAccount.
	 * @param searchCrAccount The value to assign searchCrAccount.
	 */
	public void setSearchCrAccount(String searchCrAccount)
	{
		this.searchCrAccount = searchCrAccount;
	}
	
    /**
	 * Returns the value of searchCrCurrency.
	 */
	public String getSearchCrCurrency()
	{
		return searchCrCurrency;
	}

	/**
	 * Sets the value of searchCrCurrency.
	 * @param searchCrCurrency The value to assign searchCrCurrency.
	 */
	public void setSearchCrCurrency(String searchCrCurrency)
	{
		this.searchCrCurrency = searchCrCurrency;
	}
	
    /**
	 * Returns the value of searchCrSolId.
	 */
	public String getSearchCrSolId()
	{
		return searchCrSolId;
	}

	/**
	 * Sets the value of searchCrSolId.
	 * @param searchCrSolId The value to assign searchCrSolId.
	 */
	public void setSearchCrSolId(String searchCrSolId)
	{
		this.searchCrSolId = searchCrSolId;
	}
	
    /**
	 * Returns the value of searchTranAmount.
	 */
	public Double getSearchTranAmount()
	{
		return searchTranAmount;
	}

	/**
	 * Sets the value of searchTranAmount.
	 * @param searchTranAmount The value to assign searchTranAmount.
	 */
	public void setSearchTranAmount(Double searchTranAmount)
	{
		this.searchTranAmount = searchTranAmount;
	}
	
	/**
	 * Returns the value of includeSearchTranAmount.
	 */
	public boolean getIncludeSearchTranAmount()
	{
		return includeSearchTranAmount;
	}

	/**
	 * Sets the value of includeSearchTranAmount.
	 * @param includeSearchTranAmount The value to assign includeSearchTranAmount.
	 */
	public void setIncludeSearchTranAmount(boolean includeSearchTranAmount)
	{
		this.includeSearchTranAmount = includeSearchTranAmount;
	}

    /**
	 * Returns the value of searchTranCurrency.
	 */
	public String getSearchTranCurrency()
	{
		return searchTranCurrency;
	}

	/**
	 * Sets the value of searchTranCurrency.
	 * @param searchTranCurrency The value to assign searchTranCurrency.
	 */
	public void setSearchTranCurrency(String searchTranCurrency)
	{
		this.searchTranCurrency = searchTranCurrency;
	}
	
    /**
	 * Returns the value of searchVatAccount.
	 */
	public String getSearchVatAccount()
	{
		return searchVatAccount;
	}

	/**
	 * Sets the value of searchVatAccount.
	 * @param searchVatAccount The value to assign searchVatAccount.
	 */
	public void setSearchVatAccount(String searchVatAccount)
	{
		this.searchVatAccount = searchVatAccount;
	}
	
    /**
	 * Returns the value of searchVatCurrency.
	 */
	public String getSearchVatCurrency()
	{
		return searchVatCurrency;
	}

	/**
	 * Sets the value of searchVatCurrency.
	 * @param searchVatCurrency The value to assign searchVatCurrency.
	 */
	public void setSearchVatCurrency(String searchVatCurrency)
	{
		this.searchVatCurrency = searchVatCurrency;
	}
	
    /**
	 * Returns the value of searchVatAmount.
	 */
	public Double getSearchVatAmount()
	{
		return searchVatAmount;
	}

	/**
	 * Sets the value of searchVatAmount.
	 * @param searchVatAmount The value to assign searchVatAmount.
	 */
	public void setSearchVatAmount(Double searchVatAmount)
	{
		this.searchVatAmount = searchVatAmount;
	}
	
	/**
	 * Returns the value of includeSearchVatAmount.
	 */
	public boolean getIncludeSearchVatAmount()
	{
		return includeSearchVatAmount;
	}

	/**
	 * Sets the value of includeSearchVatAmount.
	 * @param includeSearchVatAmount The value to assign includeSearchVatAmount.
	 */
	public void setIncludeSearchVatAmount(boolean includeSearchVatAmount)
	{
		this.includeSearchVatAmount = includeSearchVatAmount;
	}

    /**
	 * Returns the value of searchTranParticular.
	 */
	public String getSearchTranParticular()
	{
		return searchTranParticular;
	}

	/**
	 * Sets the value of searchTranParticular.
	 * @param searchTranParticular The value to assign searchTranParticular.
	 */
	public void setSearchTranParticular(String searchTranParticular)
	{
		this.searchTranParticular = searchTranParticular;
	}
	
    /**
	 * Returns the value of searchCountryCode.
	 */
	public String getSearchCountryCode()
	{
		return searchCountryCode;
	}

	/**
	 * Sets the value of searchCountryCode.
	 * @param searchCountryCode The value to assign searchCountryCode.
	 */
	public void setSearchCountryCode(String searchCountryCode)
	{
		this.searchCountryCode = searchCountryCode;
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
	 * Returns the value of searchValueDate.
	 */
	public Calendar getSearchValueDate()
	{
		return searchValueDate;
	}

	/**
	 * Sets the value of searchValueDate.
	 * @param searchValueDate The value to assign searchValueDate.
	 */
	public void setSearchValueDate(Calendar searchValueDate)
	{
		this.searchValueDate = searchValueDate;
	}
	
    /**
	 * Returns the value of searchPurpose.
	 */
	public String getSearchPurpose()
	{
		return searchPurpose;
	}

	/**
	 * Sets the value of searchPurpose.
	 * @param searchPurpose The value to assign searchPurpose.
	 */
	public void setSearchPurpose(String searchPurpose)
	{
		this.searchPurpose = searchPurpose;
	}
	
    /**
	 * Returns the value of searchChargeCategory.
	 */
	public String getSearchChargeCategory()
	{
		return searchChargeCategory;
	}

	/**
	 * Sets the value of searchChargeCategory.
	 * @param searchChargeCategory The value to assign searchChargeCategory.
	 */
	public void setSearchChargeCategory(String searchChargeCategory)
	{
		this.searchChargeCategory = searchChargeCategory;
	}
	
    /**
	 * Returns the value of searchBatchId.
	 */
	public String getSearchBatchId()
	{
		return searchBatchId;
	}

	/**
	 * Sets the value of searchBatchId.
	 * @param searchBatchId The value to assign searchBatchId.
	 */
	public void setSearchBatchId(String searchBatchId)
	{
		this.searchBatchId = searchBatchId;
	}
	
    /**
	 * Returns the value of searchUploadedBy.
	 */
	public String getSearchUploadedBy()
	{
		return searchUploadedBy;
	}

	/**
	 * Sets the value of searchUploadedBy.
	 * @param searchUploadedBy The value to assign searchUploadedBy.
	 */
	public void setSearchUploadedBy(String searchUploadedBy)
	{
		this.searchUploadedBy = searchUploadedBy;
	}
	
    /**
	 * Returns the value of searchUploadedDate.
	 */
	public Calendar getSearchUploadedDate()
	{
		return searchUploadedDate;
	}

	/**
	 * Sets the value of searchUploadedDate.
	 * @param searchUploadedDate The value to assign searchUploadedDate.
	 */
	public void setSearchUploadedDate(Calendar searchUploadedDate)
	{
		this.searchUploadedDate = searchUploadedDate;
	}
	
    /**
	 * Returns the value of searchPostingStatus.
	 */
	public String getSearchPostingStatus()
	{
		return searchPostingStatus;
	}

	/**
	 * Sets the value of searchPostingStatus.
	 * @param searchPostingStatus The value to assign searchPostingStatus.
	 */
	public void setSearchPostingStatus(String searchPostingStatus)
	{
		this.searchPostingStatus = searchPostingStatus;
	}
	
    /**
	 * Returns the value of searchPostedBy.
	 */
	public String getSearchPostedBy()
	{
		return searchPostedBy;
	}

	/**
	 * Sets the value of searchPostedBy.
	 * @param searchPostedBy The value to assign searchPostedBy.
	 */
	public void setSearchPostedBy(String searchPostedBy)
	{
		this.searchPostedBy = searchPostedBy;
	}
	
    /**
	 * Returns the value of searchPostedDate.
	 */
	public Calendar getSearchPostedDate()
	{
		return searchPostedDate;
	}

	/**
	 * Sets the value of searchPostedDate.
	 * @param searchPostedDate The value to assign searchPostedDate.
	 */
	public void setSearchPostedDate(Calendar searchPostedDate)
	{
		this.searchPostedDate = searchPostedDate;
	}
	
    /**
	 * Returns the value of searchErrCode.
	 */
	public String getSearchErrCode()
	{
		return searchErrCode;
	}

	/**
	 * Sets the value of searchErrCode.
	 * @param searchErrCode The value to assign searchErrCode.
	 */
	public void setSearchErrCode(String searchErrCode)
	{
		this.searchErrCode = searchErrCode;
	}
	
    /**
	 * Returns the value of searchStan.
	 */
	public String getSearchStan()
	{
		return searchStan;
	}

	/**
	 * Sets the value of searchStan.
	 * @param searchStan The value to assign searchStan.
	 */
	public void setSearchStan(String searchStan)
	{
		this.searchStan = searchStan;
	}
	
    /**
	 * Returns the value of searchStatusFlg.
	 */
	public String getSearchStatusFlg()
	{
		return searchStatusFlg;
	}

	/**
	 * Sets the value of searchStatusFlg.
	 * @param searchStatusFlg The value to assign searchStatusFlg.
	 */
	public void setSearchStatusFlg(String searchStatusFlg)
	{
		this.searchStatusFlg = searchStatusFlg;
	}
	
    /**
	 * Returns the value of searchProcessDate.
	 */
	public Calendar getSearchProcessDate()
	{
		return searchProcessDate;
	}

	/**
	 * Sets the value of searchProcessDate.
	 * @param searchProcessDate The value to assign searchProcessDate.
	 */
	public void setSearchProcessDate(Calendar searchProcessDate)
	{
		this.searchProcessDate = searchProcessDate;
	}
	
    /**
	 * Returns the value of searchNumberOfTries.
	 */
	public Integer getSearchNumberOfTries()
	{
		return searchNumberOfTries;
	}

	/**
	 * Sets the value of searchNumberOfTries.
	 * @param searchNumberOfTries The value to assign searchNumberOfTries.
	 */
	public void setSearchNumberOfTries(Integer searchNumberOfTries)
	{
		this.searchNumberOfTries = searchNumberOfTries;
	}
	
	/**
	 * Returns the value of includeSearchNumberOfTries.
	 */
	public boolean getIncludeSearchNumberOfTries()
	{
		return includeSearchNumberOfTries;
	}

	/**
	 * Sets the value of includeSearchNumberOfTries.
	 * @param includeSearchNumberOfTries The value to assign includeSearchNumberOfTries.
	 */
	public void setIncludeSearchNumberOfTries(boolean includeSearchNumberOfTries)
	{
		this.includeSearchNumberOfTries = includeSearchNumberOfTries;
	}

    /**
	 * Returns the value of searchLastResponse.
	 */
	public String getSearchLastResponse()
	{
		return searchLastResponse;
	}

	/**
	 * Sets the value of searchLastResponse.
	 * @param searchLastResponse The value to assign searchLastResponse.
	 */
	public void setSearchLastResponse(String searchLastResponse)
	{
		this.searchLastResponse = searchLastResponse;
	}
	
    /**
	 * Returns the value of searchRemarks.
	 */
	public String getSearchRemarks()
	{
		return searchRemarks;
	}

	/**
	 * Sets the value of searchRemarks.
	 * @param searchRemarks The value to assign searchRemarks.
	 */
	public void setSearchRemarks(String searchRemarks)
	{
		this.searchRemarks = searchRemarks;
	}
	
    /**
	 * Returns the value of searchConfCode.
	 */
	public String getSearchConfCode()
	{
		return searchConfCode;
	}

	/**
	 * Sets the value of searchConfCode.
	 * @param searchConfCode The value to assign searchConfCode.
	 */
	public void setSearchConfCode(String searchConfCode)
	{
		this.searchConfCode = searchConfCode;
	}
	
    /**
	 * Returns the value of searchHasSpecialAccount.
	 */
	public String getSearchHasSpecialAccount()
	{
		return searchHasSpecialAccount;
	}

	/**
	 * Sets the value of searchHasSpecialAccount.
	 * @param searchHasSpecialAccount The value to assign searchHasSpecialAccount.
	 */
	public void setSearchHasSpecialAccount(String searchHasSpecialAccount)
	{
		this.searchHasSpecialAccount = searchHasSpecialAccount;
	}
	
    /**
	 * Returns the value of searchCountry.
	 */
	public String getSearchCountry()
	{
		return searchCountry;
	}

	/**
	 * Sets the value of searchCountry.
	 * @param searchCountry The value to assign searchCountry.
	 */
	public void setSearchCountry(String searchCountry)
	{
		this.searchCountry = searchCountry;
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

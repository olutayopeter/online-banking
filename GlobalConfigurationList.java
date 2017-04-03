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
import com.expertedge.uba.collection.engine.jpa.GlobalConfiguration;

import com.geniunwit.security.util.UserManagementUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;


/**
 *
 * @author <a href="mailto:jniyiooster@gmail.com">Olaniyi Osunsanya</a>
 */
@Scope("session")
@Controller
public class GlobalConfigurationList {

	private Integer searchStartHourOfDay;
	private boolean includeSearchStartHourOfDay;
	private Integer searchStopHourOfDay;
	private boolean includeSearchStopHourOfDay;
	private Integer searchMaxNumberOfTries;
	private boolean includeSearchMaxNumberOfTries;
	private Integer searchC24ConnectionTimeOut;
	private boolean includeSearchC24ConnectionTimeOut;
	private Integer searchRecordFetchSize;
	private boolean includeSearchRecordFetchSize;
	private Integer searchRecordProcessSize;
	private boolean includeSearchRecordProcessSize;
	private Integer searchRecordFetchPriority;
	private boolean includeSearchRecordFetchPriority;
	private Integer searchRecordProcessPriority;
	private boolean includeSearchRecordProcessPriority;
	private Integer searchNumberOfProcessThreads;
	private boolean includeSearchNumberOfProcessThreads;
	private String searchTurnOnServiceAuditTrail;
	private String searchTurnOnReportGeneration;
	private String searchProcessSpecialAccountsOnly;




	private Integer firstRowIndex;
	private Integer currentRowIndex;
	private Integer rowCount;
	private Integer rows;
	
	private HashMap<String,Object> parameters;
	private HashMap<String,Object> searchFields;
	private List<GlobalConfiguration> model;
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

	public void setList(List<GlobalConfiguration> list) {
		this.model = list;
	}
	public void init(){
		FacesContext fcontext = FacesContext.getCurrentInstance();
		ExternalContext extcontext = fcontext.getExternalContext();
		GlobalConfigurationDAO dao = new GlobalConfigurationDAO();
		List globalConfigurations = null;
		
		sortColumnName = "processSpecialAccountsOnly";
		ascending = true;
		oldSort = "processSpecialAccountsOnly";
		oldAscending = true;
		model = new ArrayList();
		try {
			globalConfigurations = dao.findAll();
			setList(globalConfigurations);
			sort();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}
	public GlobalConfigurationList() {
		init();
	}

	private void addWebAuditLogEntry(String actionPerformed, String actionDetail){
		if(null != userManagerUtil){
	        String actionSource = "GLOBAL_CONFIGURATION";
	        String uri = "/GlobalConfigurationListED.xhtml";
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
		StringBuilder builder = new StringBuilder("select model from GlobalConfiguration model where ");
		int initialSize = builder.length();
		Object srcValue = null;
		boolean isDate = false;
		boolean isEnum = false;
		boolean isNumber = false;
		boolean isBoolean = false;
    

        srcValue = getSearchStartHourOfDay();
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
					if(getIncludeSearchStartHourOfDay() == false)srcValue = null;
				}
        	}
        	else{
        		srcValue = srcValue.toString();
        	}
			if(null != srcValue){
				if(isDate){
					builder.append("model.startHourOfDay = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.startHourOfDay = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.startHourOfDay = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.startHourOfDay = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.startHourOfDay) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchStopHourOfDay();
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
					if(getIncludeSearchStopHourOfDay() == false)srcValue = null;
				}
        	}
        	else{
        		srcValue = srcValue.toString();
        	}
			if(null != srcValue){
				if(isDate){
					builder.append("model.stopHourOfDay = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.stopHourOfDay = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.stopHourOfDay = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.stopHourOfDay = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.stopHourOfDay) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchMaxNumberOfTries();
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
					if(getIncludeSearchMaxNumberOfTries() == false)srcValue = null;
				}
        	}
        	else{
        		srcValue = srcValue.toString();
        	}
			if(null != srcValue){
				if(isDate){
					builder.append("model.maxNumberOfTries = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.maxNumberOfTries = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.maxNumberOfTries = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.maxNumberOfTries = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.maxNumberOfTries) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchC24ConnectionTimeOut();
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
					if(getIncludeSearchC24ConnectionTimeOut() == false)srcValue = null;
				}
        	}
        	else{
        		srcValue = srcValue.toString();
        	}
			if(null != srcValue){
				if(isDate){
					builder.append("model.c24ConnectionTimeOut = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.c24ConnectionTimeOut = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.c24ConnectionTimeOut = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.c24ConnectionTimeOut = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.c24ConnectionTimeOut) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchRecordFetchSize();
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
					if(getIncludeSearchRecordFetchSize() == false)srcValue = null;
				}
        	}
        	else{
        		srcValue = srcValue.toString();
        	}
			if(null != srcValue){
				if(isDate){
					builder.append("model.recordFetchSize = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.recordFetchSize = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.recordFetchSize = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.recordFetchSize = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.recordFetchSize) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchRecordProcessSize();
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
					if(getIncludeSearchRecordProcessSize() == false)srcValue = null;
				}
        	}
        	else{
        		srcValue = srcValue.toString();
        	}
			if(null != srcValue){
				if(isDate){
					builder.append("model.recordProcessSize = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.recordProcessSize = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.recordProcessSize = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.recordProcessSize = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.recordProcessSize) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchRecordFetchPriority();
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
					if(getIncludeSearchRecordFetchPriority() == false)srcValue = null;
				}
        	}
        	else{
        		srcValue = srcValue.toString();
        	}
			if(null != srcValue){
				if(isDate){
					builder.append("model.recordFetchPriority = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.recordFetchPriority = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.recordFetchPriority = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.recordFetchPriority = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.recordFetchPriority) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchRecordProcessPriority();
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
					if(getIncludeSearchRecordProcessPriority() == false)srcValue = null;
				}
        	}
        	else{
        		srcValue = srcValue.toString();
        	}
			if(null != srcValue){
				if(isDate){
					builder.append("model.recordProcessPriority = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.recordProcessPriority = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.recordProcessPriority = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.recordProcessPriority = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.recordProcessPriority) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchNumberOfProcessThreads();
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
					if(getIncludeSearchNumberOfProcessThreads() == false)srcValue = null;
				}
        	}
        	else{
        		srcValue = srcValue.toString();
        	}
			if(null != srcValue){
				if(isDate){
					builder.append("model.numberOfProcessThreads = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.numberOfProcessThreads = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.numberOfProcessThreads = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.numberOfProcessThreads = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.numberOfProcessThreads) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchTurnOnServiceAuditTrail();
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
					builder.append("model.turnOnServiceAuditTrail = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.turnOnServiceAuditTrail = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.turnOnServiceAuditTrail = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.turnOnServiceAuditTrail = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.turnOnServiceAuditTrail) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchTurnOnReportGeneration();
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
					builder.append("model.turnOnReportGeneration = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.turnOnReportGeneration = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.turnOnReportGeneration = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.turnOnReportGeneration = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.turnOnReportGeneration) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchProcessSpecialAccountsOnly();
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
					builder.append("model.processSpecialAccountsOnly = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.processSpecialAccountsOnly = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.processSpecialAccountsOnly = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.processSpecialAccountsOnly = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.processSpecialAccountsOnly) like lower('%");
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
		GlobalConfigurationDetail globalConfigurationDetail = null;
		GlobalConfiguration globalConfiguration = new GlobalConfiguration();

		String uid = SystemUUIDGenerator.getUUID();
		globalConfiguration.setId(new Long(System.currentTimeMillis()).longValue());

		try {
			globalConfigurationDetail = FacesUtils.findBean("globalConfigurationDetail", GlobalConfigurationDetail.class);
			globalConfigurationDetail.setGlobalConfiguration(globalConfiguration);
			globalConfigurationDetail.setCreating(true);
			globalConfigurationDetail.setModel(model);
		} catch (RuntimeException e) {
			e.printStackTrace();
		}

		addWebAuditLogEntry("Create", "Button Label: New");

		return "/views/GlobalConfigurationDetailED";
	}

	public String detail(){
		FacesContext fcontext = FacesContext.getCurrentInstance();
		UIData table = (UIData)findChildComponent(fcontext.getViewRoot(), "entity_dt");
		GlobalConfiguration globalConfiguration = null;
		
		List lst = getList();
		int rowCount = lst.size();
		int lastIndex = rowCount - 1;
		int rows = 0;
		try {
			globalConfiguration = (GlobalConfiguration)table.getRowData();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		int currentRowIndex = lst.indexOf(globalConfiguration);
		
		GlobalConfigurationDetail globalConfigurationDetail = null;
		try {
			globalConfigurationDetail = FacesUtils.findBean("globalConfigurationDetail", GlobalConfigurationDetail.class);
			globalConfigurationDetail.init(globalConfiguration);
			globalConfigurationDetail.setCreating(false);
			globalConfigurationDetail.setModel(model);
			
			Integer firstRowIndex = 0;
			
			firstRowIndex = table.getFirst();
			rows = table.getRows();
			try {
				globalConfigurationDetail.setFirstRowIndex(firstRowIndex);
				globalConfigurationDetail.setCurrentRowIndex(currentRowIndex);
				globalConfigurationDetail.setRowCount(rowCount);
				globalConfigurationDetail.setRows(rows);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}

		addWebAuditLogEntry("Read/View", new StringBuilder("Button Label: Detail").append(". Content: ID = ").append(globalConfiguration.getId()).append(". Values: ").append(globalConfiguration.toString()).toString());

		LogHelper.log("Detail link clicked: ", java.util.logging.Level.INFO, null);
		return "/views/GlobalConfigurationDetailED";
	}

	@RequiresPermissions("*:delete")
	public String delete(){
		GlobalConfigurationDAO dao = new GlobalConfigurationDAO();
		List lst = getList();
		List lselect = new ArrayList();
		boolean hasSelected = false;
		Iterator it = lst.iterator();
		while (it.hasNext()) {
			GlobalConfiguration globalConfiguration = (GlobalConfiguration) it.next();
			if(BooleanUtils.isTrue(globalConfiguration.getSelected())){
				hasSelected = true;
				lselect.add(globalConfiguration);
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
			GlobalConfiguration globalConfiguration = (GlobalConfiguration) itselect.next();
			GlobalConfiguration previousGlobalConfiguration = (GlobalConfiguration)globalConfiguration.clone();
			if(lst != null && lst.size() > 0){
				globalConfiguration.setSelected(false);
				lst.remove(globalConfiguration);
				dao.delete(globalConfiguration);

				addWebAuditLogEntry("Delete", new StringBuilder("Button Label: Delete").append(". Deleted Content: ID = ").append(previousGlobalConfiguration.getId()).append(". Values: ").append(previousGlobalConfiguration.toString()).toString());

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
		FacesUtils.addInfoMessage(null,errorMessage,new Object[]{"GlobalConfiguration"});
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
			GlobalConfiguration globalConfiguration = (GlobalConfiguration)list.get(i);
			if(BooleanUtils.isNotTrue(globalConfiguration.getSelected()))
				globalConfiguration.setSelected(true);
		}
		LogHelper.log("Select Page button clicked: ", java.util.logging.Level.INFO, null);
		return null;
	}
	public String selectAll(){
		List list = getList();
		for (int i = 0; i < list.size(); i++) {
			GlobalConfiguration globalConfiguration = (GlobalConfiguration)list.get(i);
			if(BooleanUtils.isNotTrue(globalConfiguration.getSelected()))
				globalConfiguration.setSelected(true);
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
			GlobalConfiguration globalConfiguration = (GlobalConfiguration)list.get(i);
			if(BooleanUtils.isNotFalse(globalConfiguration.getSelected()))
				globalConfiguration.setSelected(false);
		}
		LogHelper.log("Deselect Page button clicked: ", java.util.logging.Level.INFO, null);
		return null;
	}
	public String deselectAll(){
		List list = getList();
		for (int i = 0; i < list.size(); i++) {
			GlobalConfiguration globalConfiguration = (GlobalConfiguration)list.get(i);
			if(BooleanUtils.isNotFalse(globalConfiguration.getSelected()))
				globalConfiguration.setSelected(false);
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
	setSearchStartHourOfDay(null);
	setIncludeSearchStartHourOfDay(false);
	setSearchStopHourOfDay(null);
	setIncludeSearchStopHourOfDay(false);
	setSearchMaxNumberOfTries(null);
	setIncludeSearchMaxNumberOfTries(false);
	setSearchC24ConnectionTimeOut(null);
	setIncludeSearchC24ConnectionTimeOut(false);
	setSearchRecordFetchSize(null);
	setIncludeSearchRecordFetchSize(false);
	setSearchRecordProcessSize(null);
	setIncludeSearchRecordProcessSize(false);
	setSearchRecordFetchPriority(null);
	setIncludeSearchRecordFetchPriority(false);
	setSearchRecordProcessPriority(null);
	setIncludeSearchRecordProcessPriority(false);
	setSearchNumberOfProcessThreads(null);
	setIncludeSearchNumberOfProcessThreads(false);
	setSearchTurnOnServiceAuditTrail(null);
	setSearchTurnOnReportGeneration(null);
	setSearchProcessSpecialAccountsOnly(null);

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
                GlobalConfiguration model1 = (GlobalConfiguration) o1;
                GlobalConfiguration model2 = (GlobalConfiguration) o2;
                if (sortColumnName == null) {
                    return 0;
                }

				else if (sortColumnName.equals("startHourOfDay")) {
					int c1 = 0;
                    if(null == model1.getStartHourOfDay()){
                        return -1;
                    }
                    if(null == model2.getStartHourOfDay()){
                        return 1;
                    }
					try {
						c1 = model1.getStartHourOfDay().compareTo(model2.getStartHourOfDay());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getStartHourOfDay().compareTo(model1.getStartHourOfDay());
					} catch (Exception e) {
						c2 = -1;
					}
					return ascending ? c1 : c2;
				}
			
				else if (sortColumnName.equals("stopHourOfDay")) {
					int c1 = 0;
                    if(null == model1.getStopHourOfDay()){
                        return -1;
                    }
                    if(null == model2.getStopHourOfDay()){
                        return 1;
                    }
					try {
						c1 = model1.getStopHourOfDay().compareTo(model2.getStopHourOfDay());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getStopHourOfDay().compareTo(model1.getStopHourOfDay());
					} catch (Exception e) {
						c2 = -1;
					}
					return ascending ? c1 : c2;
				}
			
				else if (sortColumnName.equals("maxNumberOfTries")) {
					int c1 = 0;
                    if(null == model1.getMaxNumberOfTries()){
                        return -1;
                    }
                    if(null == model2.getMaxNumberOfTries()){
                        return 1;
                    }
					try {
						c1 = model1.getMaxNumberOfTries().compareTo(model2.getMaxNumberOfTries());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getMaxNumberOfTries().compareTo(model1.getMaxNumberOfTries());
					} catch (Exception e) {
						c2 = -1;
					}
					return ascending ? c1 : c2;
				}
			
				else if (sortColumnName.equals("c24ConnectionTimeOut")) {
					int c1 = 0;
                    if(null == model1.getC24ConnectionTimeOut()){
                        return -1;
                    }
                    if(null == model2.getC24ConnectionTimeOut()){
                        return 1;
                    }
					try {
						c1 = model1.getC24ConnectionTimeOut().compareTo(model2.getC24ConnectionTimeOut());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getC24ConnectionTimeOut().compareTo(model1.getC24ConnectionTimeOut());
					} catch (Exception e) {
						c2 = -1;
					}
					return ascending ? c1 : c2;
				}
			
				else if (sortColumnName.equals("recordFetchSize")) {
					int c1 = 0;
                    if(null == model1.getRecordFetchSize()){
                        return -1;
                    }
                    if(null == model2.getRecordFetchSize()){
                        return 1;
                    }
					try {
						c1 = model1.getRecordFetchSize().compareTo(model2.getRecordFetchSize());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getRecordFetchSize().compareTo(model1.getRecordFetchSize());
					} catch (Exception e) {
						c2 = -1;
					}
					return ascending ? c1 : c2;
				}
			
				else if (sortColumnName.equals("recordProcessSize")) {
					int c1 = 0;
                    if(null == model1.getRecordProcessSize()){
                        return -1;
                    }
                    if(null == model2.getRecordProcessSize()){
                        return 1;
                    }
					try {
						c1 = model1.getRecordProcessSize().compareTo(model2.getRecordProcessSize());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getRecordProcessSize().compareTo(model1.getRecordProcessSize());
					} catch (Exception e) {
						c2 = -1;
					}
					return ascending ? c1 : c2;
				}
			
				else if (sortColumnName.equals("recordFetchPriority")) {
					int c1 = 0;
                    if(null == model1.getRecordFetchPriority()){
                        return -1;
                    }
                    if(null == model2.getRecordFetchPriority()){
                        return 1;
                    }
					try {
						c1 = model1.getRecordFetchPriority().compareTo(model2.getRecordFetchPriority());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getRecordFetchPriority().compareTo(model1.getRecordFetchPriority());
					} catch (Exception e) {
						c2 = -1;
					}
					return ascending ? c1 : c2;
				}
			
				else if (sortColumnName.equals("recordProcessPriority")) {
					int c1 = 0;
                    if(null == model1.getRecordProcessPriority()){
                        return -1;
                    }
                    if(null == model2.getRecordProcessPriority()){
                        return 1;
                    }
					try {
						c1 = model1.getRecordProcessPriority().compareTo(model2.getRecordProcessPriority());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getRecordProcessPriority().compareTo(model1.getRecordProcessPriority());
					} catch (Exception e) {
						c2 = -1;
					}
					return ascending ? c1 : c2;
				}
			
				else if (sortColumnName.equals("numberOfProcessThreads")) {
					int c1 = 0;
                    if(null == model1.getNumberOfProcessThreads()){
                        return -1;
                    }
                    if(null == model2.getNumberOfProcessThreads()){
                        return 1;
                    }
					try {
						c1 = model1.getNumberOfProcessThreads().compareTo(model2.getNumberOfProcessThreads());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getNumberOfProcessThreads().compareTo(model1.getNumberOfProcessThreads());
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
	 * Returns the value of searchStartHourOfDay.
	 */
	public Integer getSearchStartHourOfDay()
	{
		return searchStartHourOfDay;
	}

	/**
	 * Sets the value of searchStartHourOfDay.
	 * @param searchStartHourOfDay The value to assign searchStartHourOfDay.
	 */
	public void setSearchStartHourOfDay(Integer searchStartHourOfDay)
	{
		this.searchStartHourOfDay = searchStartHourOfDay;
	}
	
	/**
	 * Returns the value of includeSearchStartHourOfDay.
	 */
	public boolean getIncludeSearchStartHourOfDay()
	{
		return includeSearchStartHourOfDay;
	}

	/**
	 * Sets the value of includeSearchStartHourOfDay.
	 * @param includeSearchStartHourOfDay The value to assign includeSearchStartHourOfDay.
	 */
	public void setIncludeSearchStartHourOfDay(boolean includeSearchStartHourOfDay)
	{
		this.includeSearchStartHourOfDay = includeSearchStartHourOfDay;
	}

    /**
	 * Returns the value of searchStopHourOfDay.
	 */
	public Integer getSearchStopHourOfDay()
	{
		return searchStopHourOfDay;
	}

	/**
	 * Sets the value of searchStopHourOfDay.
	 * @param searchStopHourOfDay The value to assign searchStopHourOfDay.
	 */
	public void setSearchStopHourOfDay(Integer searchStopHourOfDay)
	{
		this.searchStopHourOfDay = searchStopHourOfDay;
	}
	
	/**
	 * Returns the value of includeSearchStopHourOfDay.
	 */
	public boolean getIncludeSearchStopHourOfDay()
	{
		return includeSearchStopHourOfDay;
	}

	/**
	 * Sets the value of includeSearchStopHourOfDay.
	 * @param includeSearchStopHourOfDay The value to assign includeSearchStopHourOfDay.
	 */
	public void setIncludeSearchStopHourOfDay(boolean includeSearchStopHourOfDay)
	{
		this.includeSearchStopHourOfDay = includeSearchStopHourOfDay;
	}

    /**
	 * Returns the value of searchMaxNumberOfTries.
	 */
	public Integer getSearchMaxNumberOfTries()
	{
		return searchMaxNumberOfTries;
	}

	/**
	 * Sets the value of searchMaxNumberOfTries.
	 * @param searchMaxNumberOfTries The value to assign searchMaxNumberOfTries.
	 */
	public void setSearchMaxNumberOfTries(Integer searchMaxNumberOfTries)
	{
		this.searchMaxNumberOfTries = searchMaxNumberOfTries;
	}
	
	/**
	 * Returns the value of includeSearchMaxNumberOfTries.
	 */
	public boolean getIncludeSearchMaxNumberOfTries()
	{
		return includeSearchMaxNumberOfTries;
	}

	/**
	 * Sets the value of includeSearchMaxNumberOfTries.
	 * @param includeSearchMaxNumberOfTries The value to assign includeSearchMaxNumberOfTries.
	 */
	public void setIncludeSearchMaxNumberOfTries(boolean includeSearchMaxNumberOfTries)
	{
		this.includeSearchMaxNumberOfTries = includeSearchMaxNumberOfTries;
	}

    /**
	 * Returns the value of searchC24ConnectionTimeOut.
	 */
	public Integer getSearchC24ConnectionTimeOut()
	{
		return searchC24ConnectionTimeOut;
	}

	/**
	 * Sets the value of searchC24ConnectionTimeOut.
	 * @param searchC24ConnectionTimeOut The value to assign searchC24ConnectionTimeOut.
	 */
	public void setSearchC24ConnectionTimeOut(Integer searchC24ConnectionTimeOut)
	{
		this.searchC24ConnectionTimeOut = searchC24ConnectionTimeOut;
	}
	
	/**
	 * Returns the value of includeSearchC24ConnectionTimeOut.
	 */
	public boolean getIncludeSearchC24ConnectionTimeOut()
	{
		return includeSearchC24ConnectionTimeOut;
	}

	/**
	 * Sets the value of includeSearchC24ConnectionTimeOut.
	 * @param includeSearchC24ConnectionTimeOut The value to assign includeSearchC24ConnectionTimeOut.
	 */
	public void setIncludeSearchC24ConnectionTimeOut(boolean includeSearchC24ConnectionTimeOut)
	{
		this.includeSearchC24ConnectionTimeOut = includeSearchC24ConnectionTimeOut;
	}

    /**
	 * Returns the value of searchRecordFetchSize.
	 */
	public Integer getSearchRecordFetchSize()
	{
		return searchRecordFetchSize;
	}

	/**
	 * Sets the value of searchRecordFetchSize.
	 * @param searchRecordFetchSize The value to assign searchRecordFetchSize.
	 */
	public void setSearchRecordFetchSize(Integer searchRecordFetchSize)
	{
		this.searchRecordFetchSize = searchRecordFetchSize;
	}
	
	/**
	 * Returns the value of includeSearchRecordFetchSize.
	 */
	public boolean getIncludeSearchRecordFetchSize()
	{
		return includeSearchRecordFetchSize;
	}

	/**
	 * Sets the value of includeSearchRecordFetchSize.
	 * @param includeSearchRecordFetchSize The value to assign includeSearchRecordFetchSize.
	 */
	public void setIncludeSearchRecordFetchSize(boolean includeSearchRecordFetchSize)
	{
		this.includeSearchRecordFetchSize = includeSearchRecordFetchSize;
	}

    /**
	 * Returns the value of searchRecordProcessSize.
	 */
	public Integer getSearchRecordProcessSize()
	{
		return searchRecordProcessSize;
	}

	/**
	 * Sets the value of searchRecordProcessSize.
	 * @param searchRecordProcessSize The value to assign searchRecordProcessSize.
	 */
	public void setSearchRecordProcessSize(Integer searchRecordProcessSize)
	{
		this.searchRecordProcessSize = searchRecordProcessSize;
	}
	
	/**
	 * Returns the value of includeSearchRecordProcessSize.
	 */
	public boolean getIncludeSearchRecordProcessSize()
	{
		return includeSearchRecordProcessSize;
	}

	/**
	 * Sets the value of includeSearchRecordProcessSize.
	 * @param includeSearchRecordProcessSize The value to assign includeSearchRecordProcessSize.
	 */
	public void setIncludeSearchRecordProcessSize(boolean includeSearchRecordProcessSize)
	{
		this.includeSearchRecordProcessSize = includeSearchRecordProcessSize;
	}

    /**
	 * Returns the value of searchRecordFetchPriority.
	 */
	public Integer getSearchRecordFetchPriority()
	{
		return searchRecordFetchPriority;
	}

	/**
	 * Sets the value of searchRecordFetchPriority.
	 * @param searchRecordFetchPriority The value to assign searchRecordFetchPriority.
	 */
	public void setSearchRecordFetchPriority(Integer searchRecordFetchPriority)
	{
		this.searchRecordFetchPriority = searchRecordFetchPriority;
	}
	
	/**
	 * Returns the value of includeSearchRecordFetchPriority.
	 */
	public boolean getIncludeSearchRecordFetchPriority()
	{
		return includeSearchRecordFetchPriority;
	}

	/**
	 * Sets the value of includeSearchRecordFetchPriority.
	 * @param includeSearchRecordFetchPriority The value to assign includeSearchRecordFetchPriority.
	 */
	public void setIncludeSearchRecordFetchPriority(boolean includeSearchRecordFetchPriority)
	{
		this.includeSearchRecordFetchPriority = includeSearchRecordFetchPriority;
	}

    /**
	 * Returns the value of searchRecordProcessPriority.
	 */
	public Integer getSearchRecordProcessPriority()
	{
		return searchRecordProcessPriority;
	}

	/**
	 * Sets the value of searchRecordProcessPriority.
	 * @param searchRecordProcessPriority The value to assign searchRecordProcessPriority.
	 */
	public void setSearchRecordProcessPriority(Integer searchRecordProcessPriority)
	{
		this.searchRecordProcessPriority = searchRecordProcessPriority;
	}
	
	/**
	 * Returns the value of includeSearchRecordProcessPriority.
	 */
	public boolean getIncludeSearchRecordProcessPriority()
	{
		return includeSearchRecordProcessPriority;
	}

	/**
	 * Sets the value of includeSearchRecordProcessPriority.
	 * @param includeSearchRecordProcessPriority The value to assign includeSearchRecordProcessPriority.
	 */
	public void setIncludeSearchRecordProcessPriority(boolean includeSearchRecordProcessPriority)
	{
		this.includeSearchRecordProcessPriority = includeSearchRecordProcessPriority;
	}

    /**
	 * Returns the value of searchNumberOfProcessThreads.
	 */
	public Integer getSearchNumberOfProcessThreads()
	{
		return searchNumberOfProcessThreads;
	}

	/**
	 * Sets the value of searchNumberOfProcessThreads.
	 * @param searchNumberOfProcessThreads The value to assign searchNumberOfProcessThreads.
	 */
	public void setSearchNumberOfProcessThreads(Integer searchNumberOfProcessThreads)
	{
		this.searchNumberOfProcessThreads = searchNumberOfProcessThreads;
	}
	
	/**
	 * Returns the value of includeSearchNumberOfProcessThreads.
	 */
	public boolean getIncludeSearchNumberOfProcessThreads()
	{
		return includeSearchNumberOfProcessThreads;
	}

	/**
	 * Sets the value of includeSearchNumberOfProcessThreads.
	 * @param includeSearchNumberOfProcessThreads The value to assign includeSearchNumberOfProcessThreads.
	 */
	public void setIncludeSearchNumberOfProcessThreads(boolean includeSearchNumberOfProcessThreads)
	{
		this.includeSearchNumberOfProcessThreads = includeSearchNumberOfProcessThreads;
	}

    /**
	 * Returns the value of searchTurnOnServiceAuditTrail.
	 */
	public String getSearchTurnOnServiceAuditTrail()
	{
		return searchTurnOnServiceAuditTrail;
	}

	/**
	 * Sets the value of searchTurnOnServiceAuditTrail.
	 * @param searchTurnOnServiceAuditTrail The value to assign searchTurnOnServiceAuditTrail.
	 */
	public void setSearchTurnOnServiceAuditTrail(String searchTurnOnServiceAuditTrail)
	{
		this.searchTurnOnServiceAuditTrail = searchTurnOnServiceAuditTrail;
	}
	
    /**
	 * Returns the value of searchTurnOnReportGeneration.
	 */
	public String getSearchTurnOnReportGeneration()
	{
		return searchTurnOnReportGeneration;
	}

	/**
	 * Sets the value of searchTurnOnReportGeneration.
	 * @param searchTurnOnReportGeneration The value to assign searchTurnOnReportGeneration.
	 */
	public void setSearchTurnOnReportGeneration(String searchTurnOnReportGeneration)
	{
		this.searchTurnOnReportGeneration = searchTurnOnReportGeneration;
	}
	
    /**
	 * Returns the value of searchProcessSpecialAccountsOnly.
	 */
	public String getSearchProcessSpecialAccountsOnly()
	{
		return searchProcessSpecialAccountsOnly;
	}

	/**
	 * Sets the value of searchProcessSpecialAccountsOnly.
	 * @param searchProcessSpecialAccountsOnly The value to assign searchProcessSpecialAccountsOnly.
	 */
	public void setSearchProcessSpecialAccountsOnly(String searchProcessSpecialAccountsOnly)
	{
		this.searchProcessSpecialAccountsOnly = searchProcessSpecialAccountsOnly;
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

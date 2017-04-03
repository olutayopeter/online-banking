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
import com.expertedge.uba.collection.engine.jpa.Branch;

import com.geniunwit.security.util.UserManagementUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;


/**
 *
 * @author <a href="mailto:jniyiooster@gmail.com">Olaniyi Osunsanya</a>
 */
@Scope("session")
@Controller
public class BranchList {

	private String searchName;
	private String searchCountry;
	private String searchHeadOffice;
	private String searchState;
	private String searchCity;
	private String searchAddress;
	private String searchEmail;
	private String searchWebsite;
	private String searchPhone;
	private String searchContactPersonName;
	private String searchContactPersonEmail;
	private String searchContactPersonPhone;




	private Integer firstRowIndex;
	private Integer currentRowIndex;
	private Integer rowCount;
	private Integer rows;
	
	private HashMap<String,Object> parameters;
	private HashMap<String,Object> searchFields;
	private List<Branch> model;
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

	public void setList(List<Branch> list) {
		this.model = list;
	}
	public void init(){
		FacesContext fcontext = FacesContext.getCurrentInstance();
		ExternalContext extcontext = fcontext.getExternalContext();
		BranchDAO dao = new BranchDAO();
		List branchs = null;
		
		sortColumnName = "contactPersonPhone";
		ascending = true;
		oldSort = "contactPersonPhone";
		oldAscending = true;
		model = new ArrayList();
		try {
			branchs = dao.findAll();
			setList(branchs);
			sort();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}
	public BranchList() {
		init();
	}

	private void addWebAuditLogEntry(String actionPerformed, String actionDetail){
		if(null != userManagerUtil){
	        String actionSource = "BRANCH";
	        String uri = "/BranchListED.xhtml";
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
		StringBuilder builder = new StringBuilder("select model from Branch model where ");
		int initialSize = builder.length();
		Object srcValue = null;
		boolean isDate = false;
		boolean isEnum = false;
		boolean isNumber = false;
		boolean isBoolean = false;
    

        srcValue = getSearchName();
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
					builder.append("model.name = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.name = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.name = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.name = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.name) like lower('%");
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


        srcValue = getSearchHeadOffice();
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
					builder.append("model.headOffice.name = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.headOffice.name = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.headOffice.name = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.headOffice.name = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.headOffice.name) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchState();
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
					builder.append("model.state = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.state = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.state = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.state = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.state) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchCity();
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
					builder.append("model.city = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.city = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.city = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.city = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.city) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchAddress();
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
					builder.append("model.address = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.address = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.address = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.address = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.address) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchEmail();
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
					builder.append("model.email = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.email = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.email = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.email = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.email) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchWebsite();
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
					builder.append("model.website = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.website = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.website = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.website = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.website) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchPhone();
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
					builder.append("model.phone = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.phone = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.phone = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.phone = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.phone) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchContactPersonName();
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
					builder.append("model.contactPersonName = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.contactPersonName = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.contactPersonName = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.contactPersonName = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.contactPersonName) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchContactPersonEmail();
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
					builder.append("model.contactPersonEmail = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.contactPersonEmail = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.contactPersonEmail = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.contactPersonEmail = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.contactPersonEmail) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchContactPersonPhone();
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
					builder.append("model.contactPersonPhone = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.contactPersonPhone = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.contactPersonPhone = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.contactPersonPhone = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.contactPersonPhone) like lower('%");
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
		BranchDetail branchDetail = null;
		Branch branch = new Branch();

		String uid = SystemUUIDGenerator.getUUID();
		branch.setId(new Long(System.currentTimeMillis()).longValue());

		try {
			branchDetail = FacesUtils.findBean("branchDetail", BranchDetail.class);
			branchDetail.setBranch(branch);
			branchDetail.setCreating(true);
			branchDetail.setModel(model);
		} catch (RuntimeException e) {
			e.printStackTrace();
		}

		addWebAuditLogEntry("Create", "Button Label: New");

		return "/views/BranchDetailED";
	}

	public String detail(){
		FacesContext fcontext = FacesContext.getCurrentInstance();
		UIData table = (UIData)findChildComponent(fcontext.getViewRoot(), "entity_dt");
		Branch branch = null;
		
		List lst = getList();
		int rowCount = lst.size();
		int lastIndex = rowCount - 1;
		int rows = 0;
		try {
			branch = (Branch)table.getRowData();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		int currentRowIndex = lst.indexOf(branch);
		
		BranchDetail branchDetail = null;
		try {
			branchDetail = FacesUtils.findBean("branchDetail", BranchDetail.class);
			branchDetail.init(branch);
			branchDetail.setCreating(false);
			branchDetail.setModel(model);
			
			Integer firstRowIndex = 0;
			
			firstRowIndex = table.getFirst();
			rows = table.getRows();
			try {
				branchDetail.setFirstRowIndex(firstRowIndex);
				branchDetail.setCurrentRowIndex(currentRowIndex);
				branchDetail.setRowCount(rowCount);
				branchDetail.setRows(rows);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}

		addWebAuditLogEntry("Read/View", new StringBuilder("Button Label: Detail").append(". Content: ID = ").append(branch.getId()).append(". Values: ").append(branch.toString()).toString());

		LogHelper.log("Detail link clicked: ", java.util.logging.Level.INFO, null);
		return "/views/BranchDetailED";
	}

	@RequiresPermissions("*:delete")
	public String delete(){
		BranchDAO dao = new BranchDAO();
		List lst = getList();
		List lselect = new ArrayList();
		boolean hasSelected = false;
		Iterator it = lst.iterator();
		while (it.hasNext()) {
			Branch branch = (Branch) it.next();
			if(BooleanUtils.isTrue(branch.getSelected())){
				hasSelected = true;
				lselect.add(branch);
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
			Branch branch = (Branch) itselect.next();
			Branch previousBranch = (Branch)branch.clone();
			if(lst != null && lst.size() > 0){
				branch.setSelected(false);
				lst.remove(branch);
				dao.delete(branch);

				addWebAuditLogEntry("Delete", new StringBuilder("Button Label: Delete").append(". Deleted Content: ID = ").append(previousBranch.getId()).append(". Values: ").append(previousBranch.toString()).toString());

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
		FacesUtils.addInfoMessage(null,errorMessage,new Object[]{"Branch"});
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
			Branch branch = (Branch)list.get(i);
			if(BooleanUtils.isNotTrue(branch.getSelected()))
				branch.setSelected(true);
		}
		LogHelper.log("Select Page button clicked: ", java.util.logging.Level.INFO, null);
		return null;
	}
	public String selectAll(){
		List list = getList();
		for (int i = 0; i < list.size(); i++) {
			Branch branch = (Branch)list.get(i);
			if(BooleanUtils.isNotTrue(branch.getSelected()))
				branch.setSelected(true);
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
			Branch branch = (Branch)list.get(i);
			if(BooleanUtils.isNotFalse(branch.getSelected()))
				branch.setSelected(false);
		}
		LogHelper.log("Deselect Page button clicked: ", java.util.logging.Level.INFO, null);
		return null;
	}
	public String deselectAll(){
		List list = getList();
		for (int i = 0; i < list.size(); i++) {
			Branch branch = (Branch)list.get(i);
			if(BooleanUtils.isNotFalse(branch.getSelected()))
				branch.setSelected(false);
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
	setSearchName(null);
	setSearchCountry(null);
	setSearchHeadOffice(null);
	setSearchState(null);
	setSearchCity(null);
	setSearchAddress(null);
	setSearchEmail(null);
	setSearchWebsite(null);
	setSearchPhone(null);
	setSearchContactPersonName(null);
	setSearchContactPersonEmail(null);
	setSearchContactPersonPhone(null);

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
                Branch model1 = (Branch) o1;
                Branch model2 = (Branch) o2;
                if (sortColumnName == null) {
                    return 0;
                }

				else if (sortColumnName.equals("name")) {
					int c1 = 0;
                    if(null == model1.getName()){
                        return -1;
                    }
                    if(null == model2.getName()){
                        return 1;
                    }
					try {
						c1 = model1.getName().compareTo(model2.getName());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getName().compareTo(model1.getName());
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
                
                    else if (sortColumnName.equals("headOffice")) {
                        int c1 = 0;
                        if(null == model1.getHeadOffice()){
                            return -1;
                        }
                        if(null == model2.getHeadOffice()){
                            return 1;
                        }
                        try {
                            c1 = model1.getHeadOffice().getName().compareToIgnoreCase(model2.getHeadOffice().getName());
                        } catch (Exception e) {
                            c1 = -1;
                        }
                        int c2 = 0;
                        try {
                            c2 = model2.getHeadOffice().getName().compareToIgnoreCase(model1.getHeadOffice().getName());
                        } catch (Exception e) {
                            c2 = -1;
                        }
                        return ascending ? c1 : c2;
                    }
                
				else if (sortColumnName.equals("state")) {
					int c1 = 0;
                    if(null == model1.getState()){
                        return -1;
                    }
                    if(null == model2.getState()){
                        return 1;
                    }
					try {
						c1 = model1.getState().compareTo(model2.getState());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getState().compareTo(model1.getState());
					} catch (Exception e) {
						c2 = -1;
					}
					return ascending ? c1 : c2;
				}
			
				else if (sortColumnName.equals("city")) {
					int c1 = 0;
                    if(null == model1.getCity()){
                        return -1;
                    }
                    if(null == model2.getCity()){
                        return 1;
                    }
					try {
						c1 = model1.getCity().compareTo(model2.getCity());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getCity().compareTo(model1.getCity());
					} catch (Exception e) {
						c2 = -1;
					}
					return ascending ? c1 : c2;
				}
			
				else if (sortColumnName.equals("address")) {
					int c1 = 0;
                    if(null == model1.getAddress()){
                        return -1;
                    }
                    if(null == model2.getAddress()){
                        return 1;
                    }
					try {
						c1 = model1.getAddress().compareTo(model2.getAddress());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getAddress().compareTo(model1.getAddress());
					} catch (Exception e) {
						c2 = -1;
					}
					return ascending ? c1 : c2;
				}
			
				else if (sortColumnName.equals("email")) {
					int c1 = 0;
                    if(null == model1.getEmail()){
                        return -1;
                    }
                    if(null == model2.getEmail()){
                        return 1;
                    }
					try {
						c1 = model1.getEmail().compareTo(model2.getEmail());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getEmail().compareTo(model1.getEmail());
					} catch (Exception e) {
						c2 = -1;
					}
					return ascending ? c1 : c2;
				}
			
				else if (sortColumnName.equals("website")) {
					int c1 = 0;
                    if(null == model1.getWebsite()){
                        return -1;
                    }
                    if(null == model2.getWebsite()){
                        return 1;
                    }
					try {
						c1 = model1.getWebsite().compareTo(model2.getWebsite());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getWebsite().compareTo(model1.getWebsite());
					} catch (Exception e) {
						c2 = -1;
					}
					return ascending ? c1 : c2;
				}
			
				else if (sortColumnName.equals("phone")) {
					int c1 = 0;
                    if(null == model1.getPhone()){
                        return -1;
                    }
                    if(null == model2.getPhone()){
                        return 1;
                    }
					try {
						c1 = model1.getPhone().compareTo(model2.getPhone());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getPhone().compareTo(model1.getPhone());
					} catch (Exception e) {
						c2 = -1;
					}
					return ascending ? c1 : c2;
				}
			
				else if (sortColumnName.equals("contactPersonName")) {
					int c1 = 0;
                    if(null == model1.getContactPersonName()){
                        return -1;
                    }
                    if(null == model2.getContactPersonName()){
                        return 1;
                    }
					try {
						c1 = model1.getContactPersonName().compareTo(model2.getContactPersonName());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getContactPersonName().compareTo(model1.getContactPersonName());
					} catch (Exception e) {
						c2 = -1;
					}
					return ascending ? c1 : c2;
				}
			
				else if (sortColumnName.equals("contactPersonEmail")) {
					int c1 = 0;
                    if(null == model1.getContactPersonEmail()){
                        return -1;
                    }
                    if(null == model2.getContactPersonEmail()){
                        return 1;
                    }
					try {
						c1 = model1.getContactPersonEmail().compareTo(model2.getContactPersonEmail());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getContactPersonEmail().compareTo(model1.getContactPersonEmail());
					} catch (Exception e) {
						c2 = -1;
					}
					return ascending ? c1 : c2;
				}
			
				else if (sortColumnName.equals("contactPersonPhone")) {
					int c1 = 0;
                    if(null == model1.getContactPersonPhone()){
                        return -1;
                    }
                    if(null == model2.getContactPersonPhone()){
                        return 1;
                    }
					try {
						c1 = model1.getContactPersonPhone().compareTo(model2.getContactPersonPhone());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getContactPersonPhone().compareTo(model1.getContactPersonPhone());
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
	 * Returns the value of searchName.
	 */
	public String getSearchName()
	{
		return searchName;
	}

	/**
	 * Sets the value of searchName.
	 * @param searchName The value to assign searchName.
	 */
	public void setSearchName(String searchName)
	{
		this.searchName = searchName;
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
	
    /**
	 * Returns the value of searchHeadOffice.
	 */
	public String getSearchHeadOffice()
	{
		return searchHeadOffice;
	}

	/**
	 * Sets the value of searchHeadOffice.
	 * @param searchHeadOffice The value to assign searchHeadOffice.
	 */
	public void setSearchHeadOffice(String searchHeadOffice)
	{
		this.searchHeadOffice = searchHeadOffice;
	}
	
    /**
	 * Returns the value of searchState.
	 */
	public String getSearchState()
	{
		return searchState;
	}

	/**
	 * Sets the value of searchState.
	 * @param searchState The value to assign searchState.
	 */
	public void setSearchState(String searchState)
	{
		this.searchState = searchState;
	}
	
    /**
	 * Returns the value of searchCity.
	 */
	public String getSearchCity()
	{
		return searchCity;
	}

	/**
	 * Sets the value of searchCity.
	 * @param searchCity The value to assign searchCity.
	 */
	public void setSearchCity(String searchCity)
	{
		this.searchCity = searchCity;
	}
	
    /**
	 * Returns the value of searchAddress.
	 */
	public String getSearchAddress()
	{
		return searchAddress;
	}

	/**
	 * Sets the value of searchAddress.
	 * @param searchAddress The value to assign searchAddress.
	 */
	public void setSearchAddress(String searchAddress)
	{
		this.searchAddress = searchAddress;
	}
	
    /**
	 * Returns the value of searchEmail.
	 */
	public String getSearchEmail()
	{
		return searchEmail;
	}

	/**
	 * Sets the value of searchEmail.
	 * @param searchEmail The value to assign searchEmail.
	 */
	public void setSearchEmail(String searchEmail)
	{
		this.searchEmail = searchEmail;
	}
	
    /**
	 * Returns the value of searchWebsite.
	 */
	public String getSearchWebsite()
	{
		return searchWebsite;
	}

	/**
	 * Sets the value of searchWebsite.
	 * @param searchWebsite The value to assign searchWebsite.
	 */
	public void setSearchWebsite(String searchWebsite)
	{
		this.searchWebsite = searchWebsite;
	}
	
    /**
	 * Returns the value of searchPhone.
	 */
	public String getSearchPhone()
	{
		return searchPhone;
	}

	/**
	 * Sets the value of searchPhone.
	 * @param searchPhone The value to assign searchPhone.
	 */
	public void setSearchPhone(String searchPhone)
	{
		this.searchPhone = searchPhone;
	}
	
    /**
	 * Returns the value of searchContactPersonName.
	 */
	public String getSearchContactPersonName()
	{
		return searchContactPersonName;
	}

	/**
	 * Sets the value of searchContactPersonName.
	 * @param searchContactPersonName The value to assign searchContactPersonName.
	 */
	public void setSearchContactPersonName(String searchContactPersonName)
	{
		this.searchContactPersonName = searchContactPersonName;
	}
	
    /**
	 * Returns the value of searchContactPersonEmail.
	 */
	public String getSearchContactPersonEmail()
	{
		return searchContactPersonEmail;
	}

	/**
	 * Sets the value of searchContactPersonEmail.
	 * @param searchContactPersonEmail The value to assign searchContactPersonEmail.
	 */
	public void setSearchContactPersonEmail(String searchContactPersonEmail)
	{
		this.searchContactPersonEmail = searchContactPersonEmail;
	}
	
    /**
	 * Returns the value of searchContactPersonPhone.
	 */
	public String getSearchContactPersonPhone()
	{
		return searchContactPersonPhone;
	}

	/**
	 * Sets the value of searchContactPersonPhone.
	 * @param searchContactPersonPhone The value to assign searchContactPersonPhone.
	 */
	public void setSearchContactPersonPhone(String searchContactPersonPhone)
	{
		this.searchContactPersonPhone = searchContactPersonPhone;
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

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
import com.expertedge.uba.collection.engine.jpa.CountryConfiguration;

import com.geniunwit.security.util.UserManagementUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;


/**
 *
 * @author <a href="mailto:jniyiooster@gmail.com">Olaniyi Osunsanya</a>
 */
@Scope("session")
@Controller
public class CountryConfigurationList {

	private Integer searchStartHourOfDayOffset;
	private boolean includeSearchStartHourOfDayOffset;
	private Integer searchStopHourOfDayOffset;
	private boolean includeSearchStopHourOfDayOffset;
	private String searchCountry;




	private Integer firstRowIndex;
	private Integer currentRowIndex;
	private Integer rowCount;
	private Integer rows;
	
	private HashMap<String,Object> parameters;
	private HashMap<String,Object> searchFields;
	private List<CountryConfiguration> model;
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

	public void setList(List<CountryConfiguration> list) {
		this.model = list;
	}
	public void init(){
		FacesContext fcontext = FacesContext.getCurrentInstance();
		ExternalContext extcontext = fcontext.getExternalContext();
		CountryConfigurationDAO dao = new CountryConfigurationDAO();
		List countryConfigurations = null;
		
		sortColumnName = "country";
		ascending = true;
		oldSort = "country";
		oldAscending = true;
		model = new ArrayList();
		try {
			countryConfigurations = dao.findAll();
			setList(countryConfigurations);
			sort();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}
	public CountryConfigurationList() {
		init();
	}

	private void addWebAuditLogEntry(String actionPerformed, String actionDetail){
		if(null != userManagerUtil){
	        String actionSource = "COUNTRY_CONFIGURATION";
	        String uri = "/CountryConfigurationListED.xhtml";
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
		StringBuilder builder = new StringBuilder("select model from CountryConfiguration model where ");
		int initialSize = builder.length();
		Object srcValue = null;
		boolean isDate = false;
		boolean isEnum = false;
		boolean isNumber = false;
		boolean isBoolean = false;
    

        srcValue = getSearchStartHourOfDayOffset();
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
					if(getIncludeSearchStartHourOfDayOffset() == false)srcValue = null;
				}
        	}
        	else{
        		srcValue = srcValue.toString();
        	}
			if(null != srcValue){
				if(isDate){
					builder.append("model.startHourOfDayOffset = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.startHourOfDayOffset = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.startHourOfDayOffset = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.startHourOfDayOffset = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.startHourOfDayOffset) like lower('%");
					builder.append(srcValue);
					builder.append("%') and ");
				}
			}
        }



        srcValue = getSearchStopHourOfDayOffset();
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
					if(getIncludeSearchStopHourOfDayOffset() == false)srcValue = null;
				}
        	}
        	else{
        		srcValue = srcValue.toString();
        	}
			if(null != srcValue){
				if(isDate){
					builder.append("model.stopHourOfDayOffset = '");
					builder.append(srcValue);
					builder.append("' and ");
				}
				else if(isEnum){
					builder.append("model.stopHourOfDayOffset = ");
					builder.append(srcValue.getClass().getName().concat("." + srcValue.toString()));
					builder.append(" and ");
				}
				else if(isNumber){
					builder.append("model.stopHourOfDayOffset = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else if(isBoolean){
					builder.append("model.stopHourOfDayOffset = ");
					builder.append(srcValue);
					builder.append(" and ");
				}
				else {
					builder.append("lower(model.stopHourOfDayOffset) like lower('%");
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
		CountryConfigurationDetail countryConfigurationDetail = null;
		CountryConfiguration countryConfiguration = new CountryConfiguration();

		String uid = SystemUUIDGenerator.getUUID();
		countryConfiguration.setId(new Long(System.currentTimeMillis()).longValue());

		try {
			countryConfigurationDetail = FacesUtils.findBean("countryConfigurationDetail", CountryConfigurationDetail.class);
			countryConfigurationDetail.setCountryConfiguration(countryConfiguration);
			countryConfigurationDetail.setCreating(true);
			countryConfigurationDetail.setModel(model);
		} catch (RuntimeException e) {
			e.printStackTrace();
		}

		addWebAuditLogEntry("Create", "Button Label: New");

		return "/views/CountryConfigurationDetailED";
	}

	public String detail(){
		FacesContext fcontext = FacesContext.getCurrentInstance();
		UIData table = (UIData)findChildComponent(fcontext.getViewRoot(), "entity_dt");
		CountryConfiguration countryConfiguration = null;
		
		List lst = getList();
		int rowCount = lst.size();
		int lastIndex = rowCount - 1;
		int rows = 0;
		try {
			countryConfiguration = (CountryConfiguration)table.getRowData();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		int currentRowIndex = lst.indexOf(countryConfiguration);
		
		CountryConfigurationDetail countryConfigurationDetail = null;
		try {
			countryConfigurationDetail = FacesUtils.findBean("countryConfigurationDetail", CountryConfigurationDetail.class);
			countryConfigurationDetail.init(countryConfiguration);
			countryConfigurationDetail.setCreating(false);
			countryConfigurationDetail.setModel(model);
			
			Integer firstRowIndex = 0;
			
			firstRowIndex = table.getFirst();
			rows = table.getRows();
			try {
				countryConfigurationDetail.setFirstRowIndex(firstRowIndex);
				countryConfigurationDetail.setCurrentRowIndex(currentRowIndex);
				countryConfigurationDetail.setRowCount(rowCount);
				countryConfigurationDetail.setRows(rows);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}

		addWebAuditLogEntry("Read/View", new StringBuilder("Button Label: Detail").append(". Content: ID = ").append(countryConfiguration.getId()).append(". Values: ").append(countryConfiguration.toString()).toString());

		LogHelper.log("Detail link clicked: ", java.util.logging.Level.INFO, null);
		return "/views/CountryConfigurationDetailED";
	}

	@RequiresPermissions("*:delete")
	public String delete(){
		CountryConfigurationDAO dao = new CountryConfigurationDAO();
		List lst = getList();
		List lselect = new ArrayList();
		boolean hasSelected = false;
		Iterator it = lst.iterator();
		while (it.hasNext()) {
			CountryConfiguration countryConfiguration = (CountryConfiguration) it.next();
			if(BooleanUtils.isTrue(countryConfiguration.getSelected())){
				hasSelected = true;
				lselect.add(countryConfiguration);
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
			CountryConfiguration countryConfiguration = (CountryConfiguration) itselect.next();
			CountryConfiguration previousCountryConfiguration = (CountryConfiguration)countryConfiguration.clone();
			if(lst != null && lst.size() > 0){
				countryConfiguration.setSelected(false);
				lst.remove(countryConfiguration);
				dao.delete(countryConfiguration);

				addWebAuditLogEntry("Delete", new StringBuilder("Button Label: Delete").append(". Deleted Content: ID = ").append(previousCountryConfiguration.getId()).append(". Values: ").append(previousCountryConfiguration.toString()).toString());

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
		FacesUtils.addInfoMessage(null,errorMessage,new Object[]{"CountryConfiguration"});
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
			CountryConfiguration countryConfiguration = (CountryConfiguration)list.get(i);
			if(BooleanUtils.isNotTrue(countryConfiguration.getSelected()))
				countryConfiguration.setSelected(true);
		}
		LogHelper.log("Select Page button clicked: ", java.util.logging.Level.INFO, null);
		return null;
	}
	public String selectAll(){
		List list = getList();
		for (int i = 0; i < list.size(); i++) {
			CountryConfiguration countryConfiguration = (CountryConfiguration)list.get(i);
			if(BooleanUtils.isNotTrue(countryConfiguration.getSelected()))
				countryConfiguration.setSelected(true);
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
			CountryConfiguration countryConfiguration = (CountryConfiguration)list.get(i);
			if(BooleanUtils.isNotFalse(countryConfiguration.getSelected()))
				countryConfiguration.setSelected(false);
		}
		LogHelper.log("Deselect Page button clicked: ", java.util.logging.Level.INFO, null);
		return null;
	}
	public String deselectAll(){
		List list = getList();
		for (int i = 0; i < list.size(); i++) {
			CountryConfiguration countryConfiguration = (CountryConfiguration)list.get(i);
			if(BooleanUtils.isNotFalse(countryConfiguration.getSelected()))
				countryConfiguration.setSelected(false);
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
	setSearchStartHourOfDayOffset(null);
	setIncludeSearchStartHourOfDayOffset(false);
	setSearchStopHourOfDayOffset(null);
	setIncludeSearchStopHourOfDayOffset(false);
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
                CountryConfiguration model1 = (CountryConfiguration) o1;
                CountryConfiguration model2 = (CountryConfiguration) o2;
                if (sortColumnName == null) {
                    return 0;
                }

				else if (sortColumnName.equals("startHourOfDayOffset")) {
					int c1 = 0;
                    if(null == model1.getStartHourOfDayOffset()){
                        return -1;
                    }
                    if(null == model2.getStartHourOfDayOffset()){
                        return 1;
                    }
					try {
						c1 = model1.getStartHourOfDayOffset().compareTo(model2.getStartHourOfDayOffset());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getStartHourOfDayOffset().compareTo(model1.getStartHourOfDayOffset());
					} catch (Exception e) {
						c2 = -1;
					}
					return ascending ? c1 : c2;
				}
			
				else if (sortColumnName.equals("stopHourOfDayOffset")) {
					int c1 = 0;
                    if(null == model1.getStopHourOfDayOffset()){
                        return -1;
                    }
                    if(null == model2.getStopHourOfDayOffset()){
                        return 1;
                    }
					try {
						c1 = model1.getStopHourOfDayOffset().compareTo(model2.getStopHourOfDayOffset());
					} catch (Exception e) {
						c1 = -1;
					}
					int c2 = 0;
					try {
						c2 = model2.getStopHourOfDayOffset().compareTo(model1.getStopHourOfDayOffset());
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
	 * Returns the value of searchStartHourOfDayOffset.
	 */
	public Integer getSearchStartHourOfDayOffset()
	{
		return searchStartHourOfDayOffset;
	}

	/**
	 * Sets the value of searchStartHourOfDayOffset.
	 * @param searchStartHourOfDayOffset The value to assign searchStartHourOfDayOffset.
	 */
	public void setSearchStartHourOfDayOffset(Integer searchStartHourOfDayOffset)
	{
		this.searchStartHourOfDayOffset = searchStartHourOfDayOffset;
	}
	
	/**
	 * Returns the value of includeSearchStartHourOfDayOffset.
	 */
	public boolean getIncludeSearchStartHourOfDayOffset()
	{
		return includeSearchStartHourOfDayOffset;
	}

	/**
	 * Sets the value of includeSearchStartHourOfDayOffset.
	 * @param includeSearchStartHourOfDayOffset The value to assign includeSearchStartHourOfDayOffset.
	 */
	public void setIncludeSearchStartHourOfDayOffset(boolean includeSearchStartHourOfDayOffset)
	{
		this.includeSearchStartHourOfDayOffset = includeSearchStartHourOfDayOffset;
	}

    /**
	 * Returns the value of searchStopHourOfDayOffset.
	 */
	public Integer getSearchStopHourOfDayOffset()
	{
		return searchStopHourOfDayOffset;
	}

	/**
	 * Sets the value of searchStopHourOfDayOffset.
	 * @param searchStopHourOfDayOffset The value to assign searchStopHourOfDayOffset.
	 */
	public void setSearchStopHourOfDayOffset(Integer searchStopHourOfDayOffset)
	{
		this.searchStopHourOfDayOffset = searchStopHourOfDayOffset;
	}
	
	/**
	 * Returns the value of includeSearchStopHourOfDayOffset.
	 */
	public boolean getIncludeSearchStopHourOfDayOffset()
	{
		return includeSearchStopHourOfDayOffset;
	}

	/**
	 * Sets the value of includeSearchStopHourOfDayOffset.
	 * @param includeSearchStopHourOfDayOffset The value to assign includeSearchStopHourOfDayOffset.
	 */
	public void setIncludeSearchStopHourOfDayOffset(boolean includeSearchStopHourOfDayOffset)
	{
		this.includeSearchStopHourOfDayOffset = includeSearchStopHourOfDayOffset;
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

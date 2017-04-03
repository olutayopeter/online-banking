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
import com.expertedge.uba.collection.engine.jpa.FinacleDtdTable;

import com.geniunwit.security.util.UserManagementUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;


/**
 *
 * @author <a href="mailto:jniyiooster@gmail.com">Olaniyi Osunsanya</a>
 */
@Scope("session")
@Controller
public class FinacleDtdTableList {





	private Integer firstRowIndex;
	private Integer currentRowIndex;
	private Integer rowCount;
	private Integer rows;
	
	private HashMap<String,Object> parameters;
	private HashMap<String,Object> searchFields;
	private List<FinacleDtdTable> model;
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

	public void setList(List<FinacleDtdTable> list) {
		this.model = list;
	}
	public void init(){
		FacesContext fcontext = FacesContext.getCurrentInstance();
		ExternalContext extcontext = fcontext.getExternalContext();
		FinacleDtdTableDAO dao = new FinacleDtdTableDAO();
		List finacleDtdTables = null;
		
		sortColumnName = "bankCode";
		ascending = true;
		oldSort = "bankCode";
		oldAscending = true;
		model = new ArrayList();
		try {
			finacleDtdTables = dao.findAll();
			setList(finacleDtdTables);
			sort();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}
	public FinacleDtdTableList() {
		init();
	}

	private void addWebAuditLogEntry(String actionPerformed, String actionDetail){
		if(null != userManagerUtil){
	        String actionSource = "FINACLE_DTD_TABLE";
	        String uri = "/FinacleDtdTableListED.xhtml";
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
		StringBuilder builder = new StringBuilder("select model from FinacleDtdTable model where ");
		int initialSize = builder.length();
		Object srcValue = null;
		boolean isDate = false;
		boolean isEnum = false;
		boolean isNumber = false;
		boolean isBoolean = false;
    

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
		FinacleDtdTableDetail finacleDtdTableDetail = null;
		FinacleDtdTable finacleDtdTable = new FinacleDtdTable();

		String uid = SystemUUIDGenerator.getUUID();
		finacleDtdTable.setId(uid);

		try {
			finacleDtdTableDetail = FacesUtils.findBean("finacleDtdTableDetail", FinacleDtdTableDetail.class);
			finacleDtdTableDetail.setFinacleDtdTable(finacleDtdTable);
			finacleDtdTableDetail.setCreating(true);
			finacleDtdTableDetail.setModel(model);
		} catch (RuntimeException e) {
			e.printStackTrace();
		}

		addWebAuditLogEntry("Create", "Button Label: New");

		return "/views/FinacleDtdTableDetailED";
	}

	public String detail(){
		FacesContext fcontext = FacesContext.getCurrentInstance();
		UIData table = (UIData)findChildComponent(fcontext.getViewRoot(), "entity_dt");
		FinacleDtdTable finacleDtdTable = null;
		
		List lst = getList();
		int rowCount = lst.size();
		int lastIndex = rowCount - 1;
		int rows = 0;
		try {
			finacleDtdTable = (FinacleDtdTable)table.getRowData();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		int currentRowIndex = lst.indexOf(finacleDtdTable);
		
		FinacleDtdTableDetail finacleDtdTableDetail = null;
		try {
			finacleDtdTableDetail = FacesUtils.findBean("finacleDtdTableDetail", FinacleDtdTableDetail.class);
			finacleDtdTableDetail.init(finacleDtdTable);
			finacleDtdTableDetail.setCreating(false);
			finacleDtdTableDetail.setModel(model);
			
			Integer firstRowIndex = 0;
			
			firstRowIndex = table.getFirst();
			rows = table.getRows();
			try {
				finacleDtdTableDetail.setFirstRowIndex(firstRowIndex);
				finacleDtdTableDetail.setCurrentRowIndex(currentRowIndex);
				finacleDtdTableDetail.setRowCount(rowCount);
				finacleDtdTableDetail.setRows(rows);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}

		addWebAuditLogEntry("Read/View", new StringBuilder("Button Label: Detail").append(". Content: ID = ").append(finacleDtdTable.getId()).append(". Values: ").append(finacleDtdTable.toString()).toString());

		LogHelper.log("Detail link clicked: ", java.util.logging.Level.INFO, null);
		return "/views/FinacleDtdTableDetailED";
	}

	@RequiresPermissions("*:delete")
	public String delete(){
		FinacleDtdTableDAO dao = new FinacleDtdTableDAO();
		List lst = getList();
		List lselect = new ArrayList();
		boolean hasSelected = false;
		Iterator it = lst.iterator();
		while (it.hasNext()) {
			FinacleDtdTable finacleDtdTable = (FinacleDtdTable) it.next();
			if(BooleanUtils.isTrue(finacleDtdTable.getSelected())){
				hasSelected = true;
				lselect.add(finacleDtdTable);
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
			FinacleDtdTable finacleDtdTable = (FinacleDtdTable) itselect.next();
			FinacleDtdTable previousFinacleDtdTable = (FinacleDtdTable)finacleDtdTable.clone();
			if(lst != null && lst.size() > 0){
				finacleDtdTable.setSelected(false);
				lst.remove(finacleDtdTable);
				dao.delete(finacleDtdTable);

				addWebAuditLogEntry("Delete", new StringBuilder("Button Label: Delete").append(". Deleted Content: ID = ").append(previousFinacleDtdTable.getId()).append(". Values: ").append(previousFinacleDtdTable.toString()).toString());

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
		FacesUtils.addInfoMessage(null,errorMessage,new Object[]{"FinacleDtdTable"});
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
			FinacleDtdTable finacleDtdTable = (FinacleDtdTable)list.get(i);
			if(BooleanUtils.isNotTrue(finacleDtdTable.getSelected()))
				finacleDtdTable.setSelected(true);
		}
		LogHelper.log("Select Page button clicked: ", java.util.logging.Level.INFO, null);
		return null;
	}
	public String selectAll(){
		List list = getList();
		for (int i = 0; i < list.size(); i++) {
			FinacleDtdTable finacleDtdTable = (FinacleDtdTable)list.get(i);
			if(BooleanUtils.isNotTrue(finacleDtdTable.getSelected()))
				finacleDtdTable.setSelected(true);
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
			FinacleDtdTable finacleDtdTable = (FinacleDtdTable)list.get(i);
			if(BooleanUtils.isNotFalse(finacleDtdTable.getSelected()))
				finacleDtdTable.setSelected(false);
		}
		LogHelper.log("Deselect Page button clicked: ", java.util.logging.Level.INFO, null);
		return null;
	}
	public String deselectAll(){
		List list = getList();
		for (int i = 0; i < list.size(); i++) {
			FinacleDtdTable finacleDtdTable = (FinacleDtdTable)list.get(i);
			if(BooleanUtils.isNotFalse(finacleDtdTable.getSelected()))
				finacleDtdTable.setSelected(false);
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

	}
	public String back(){
		return "back";
	}
	public String refresh(){
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
                FinacleDtdTable model1 = (FinacleDtdTable) o1;
                FinacleDtdTable model2 = (FinacleDtdTable) o2;
                if (sortColumnName == null) {
                    return 0;
                }

                else return 0;
            }
        };
        List list = getList();
        Collections.sort(list, comparator);
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

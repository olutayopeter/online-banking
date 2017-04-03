package com.expertedge.uba.collection.engine.beans;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
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
import com.expertedge.uba.collection.engine.jpa.Currency;
import com.geniunwit.security.crypto.AESencrp;

import org.apache.commons.lang.SerializationUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author <a href="mailto:jniyiooster@gmail.com">Olaniyi Osunsanya</a>
 */
@Scope("session")
@Controller
public class EncryptPassword {
	private String password;
	private String encryptedPassword;
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getEncryptedPassword() {
		return encryptedPassword;
	}
	public void setEncryptedPassword(String encryptedPassword) {
		this.encryptedPassword = encryptedPassword;
	}
	public String encrypt(){
		if(Nought.isNought(password))return null;
		if(validate() == false)return null;

		String temp = null;
		try {
			temp = AESencrp.encrypt(getPassword());
		} catch (Exception e) {
		}
		setEncryptedPassword(temp);
		return null;
	}

	private boolean validate(){
		boolean hasErrors = false;
		// Validate blank fields

		String password = getPassword();
		if(password == null || password.trim().equals("")){
			FacesUtils.addErrorMessage(null,"blank_field",new Object[]{"Name"});
			hasErrors = true;
		}

		return (hasErrors == false);
	}
}

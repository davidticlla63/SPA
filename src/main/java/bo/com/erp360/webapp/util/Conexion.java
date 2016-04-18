package bo.com.erp360.webapp.util;

import java.io.Serializable;

import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.persistence.EntityManager;

public class Conexion implements Serializable {

	/**
	 * 
	 */
	
	
	private static final long serialVersionUID = -2579990640927344648L;
	
	
	@Inject
    private EntityManager em;
	
	@Inject
    private FacesContext facesContext;
	
	public static String datasourse = "java:jboss/datasources/SpaDS";

	public String getDatasourse() {
		
		return this.datasourse;
	}

	public void setDatasourse(String datasourse) {
		this.datasourse = datasourse;
	}

}

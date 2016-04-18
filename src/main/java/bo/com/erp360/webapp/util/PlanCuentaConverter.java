package bo.com.erp360.webapp.util;

import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;

import bo.com.erp360.webapp.data.PlanCuentaRepository;
import bo.com.erp360.webapp.model.PlanCuenta;


@FacesConverter("planCuentaConverter")
public class PlanCuentaConverter implements Converter, Serializable {

	private static final long   serialVersionUID    = 1L;

	@Inject
	private PlanCuentaRepository ejb;

	@Override
	public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String value) throws ConverterException {
		if (value == null || value.length() == 0) {
			return null;
		} else {
			System.out.println("getAsObject: "+value);
			return ejb.findById(Integer.parseInt(value));
		}
	}

	@Override
	public String getAsString(FacesContext facesContext, UIComponent uiComponent, Object value) throws ConverterException {
		System.out.println("getAsString: "+value.getClass());
		if (value == null) {
			return null;
		} else {
			return String.valueOf(((PlanCuenta) value).getId());
		}
	}
}
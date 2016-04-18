package bo.com.erp360.webapp.util;

import java.util.ArrayList;
import java.util.List;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.context.FacesContext;

import bo.com.erp360.webapp.model.Permiso;
import bo.com.erp360.webapp.model.Privilegio;

public class EDPermisoV2 {

	private List<Privilegio> listaPrivilegio = new ArrayList<Privilegio>();
	private List<Permiso> listPermiso = new ArrayList<Permiso>();

	private List<Privilegio> listaPrivilegioAceptados = new ArrayList<Privilegio>();

	//estados

	//seguridad
	private boolean seguridadTodos= false, seguridad= false,usuarioL=false,usuarioE= false,rolesL=false,rolesE= false,permisoL=false,permisoE=false;
	//Parametrizacion
	private boolean parametrizacionTodos = false, parametrizacion =false, empresaL=false,empresaE=false,sucursalL=false,sucursalE=false,servicioL=false,servicioE=false,tipoComprobanteL=false,tipoComprobanteE=false,tipoCambioL=false,tipoCambioE=false,tipoUfvL=false,tipoUfvE=false,planCuentaL=false,planCuentaE=false,centroCostoL=false,centroCostoE=false,clientesL=false,clientesE=false,aperturaCierreGestionL=false,aperturaCierreGestionE=false;
	//proceso
	private boolean procesoTodos = false, proceso = false, cuentasMonetariasL=false,cuentasMonetariasE=false,cuentasNoMonetariasL=false,cuentasNoMonetariasE=false;

	// Constructor
	public EDPermisoV2() {
		super();		
	}

	public void cargarListPrivilegio(){
		resetValues();
		for(Privilegio p: listaPrivilegio){
			switch (p.getPermiso().getName().toString()) {
			//seguridad
			case "Seguridad": listaPrivilegioAceptados.add(p);seguridad = true; break;
			case "Usuario": listaPrivilegioAceptados.add(p);usuarioL = true; usuarioE = p.getEscritura().equals("AC")?true:false ;break;
			case "Roles": listaPrivilegioAceptados.add(p);rolesL = true; rolesE = p.getEscritura().equals("AC")?true:false ;break;
			case "Permiso": listaPrivilegioAceptados.add(p);permisoL = true; permisoE = p.getEscritura().equals("AC")?true:false ;break;
			//paremetrizacion
			case "Parametrizacion": listaPrivilegioAceptados.add(p);parametrizacion = true; break;
			case "Empresa": listaPrivilegioAceptados.add(p);empresaL=true; empresaE = p.getEscritura().equals("AC")?true:false ;break;
			case "Sucursal": listaPrivilegioAceptados.add(p);sucursalL=true; sucursalE = p.getEscritura().equals("AC")?true:false ;break;
			case "Servicio": listaPrivilegioAceptados.add(p);servicioL=true; servicioE = p.getEscritura().equals("AC")?true:false ;break;
			case "Tipo Comprobante": listaPrivilegioAceptados.add(p);tipoComprobanteL=true; tipoComprobanteE=p.getEscritura().equals("AC")?true:false ;break;
			case "Tipo de cambio": listaPrivilegioAceptados.add(p);tipoCambioL=true; tipoCambioE=p.getEscritura().equals("AC")?true:false ;break;
			case "Tipo de UFV": listaPrivilegioAceptados.add(p);tipoUfvL=true; tipoUfvE=p.getEscritura().equals("AC")?true:false ;break;
			case "Plan de cuenta": listaPrivilegioAceptados.add(p);planCuentaL=true; planCuentaE=p.getEscritura().equals("AC")?true:false ;break;
			case "Centro Costo": listaPrivilegioAceptados.add(p);centroCostoL=true; centroCostoE=p.getEscritura().equals("AC")?true:false ;break;
			case "Cliente": listaPrivilegioAceptados.add(p);clientesL=true; clientesE=p.getEscritura().equals("AC")?true:false ;break;
			case "Apertura y cierre de gestion": listaPrivilegioAceptados.add(p);aperturaCierreGestionL=true; aperturaCierreGestionE=p.getEscritura().equals("AC")?true:false ;break;
			//proceso
			case "Proceso": proceso=true;  ;break;
			case "Cuentas monetarias": listaPrivilegioAceptados.add(p);cuentasMonetariasL=true; cuentasMonetariasE=p.getEscritura().equals("AC")?true:false ;break;
			case "Cuentas no monetarias": listaPrivilegioAceptados.add(p);cuentasNoMonetariasL=true; cuentasNoMonetariasE=p.getEscritura().equals("AC")?true:false ;break;
			//formulario

			//reporte
			default:
				break;
			}
		}
		verificarAgrupacionSeguridad();
		verificarAgrupacionParametrizacion();
		verificarAgrupacionProceso();
	}

	private Privilegio obtnerPrivilegioListPrivilegioAceptados(String nombrePermiso){
		for(Privilegio p: listaPrivilegioAceptados){
			if(p.getPermiso().getName().equals(nombrePermiso)){
				return p;
			}
		}
		return null;
	}

	private int obtnerIndexListPrivilegioAceptados(String nombrePermiso){
		for(int index = 0 ; index < listaPrivilegioAceptados.size();index++){
			Privilegio p = listaPrivilegioAceptados.get(index);
			if(p.getPermiso().getName().equals(nombrePermiso)){
				return index;
			}
		}
		return 0;
	}

	private Permiso obtenerPermiso(String nombrePermiso){
		for(Permiso p : listPermiso){
			if(p.getName().equals(nombrePermiso)){
				return p;
			}
		}
		return null;
	}

	private boolean yaExistePrivilegio(String nombrePermiso){
		for(Privilegio p : listaPrivilegioAceptados){
			if(p.getPermiso().getName().equals(nombrePermiso)){
				return true;
			}
		}
		return false;
	}

	private void agregarPrivilegio(String nombrePermiso,boolean lectura,boolean escritura){
		Privilegio privilegio = new Privilegio();
		if(! yaExistePrivilegio(nombrePermiso)){
			privilegio.setPermiso(obtenerPermiso(nombrePermiso));
			privilegio.setLectura(lectura?"AC":"IN");
			privilegio.setEscritura(escritura?"AC":"IN");
			listaPrivilegioAceptados.add(privilegio);
			return;
		}
		privilegio = obtnerPrivilegioListPrivilegioAceptados(nombrePermiso);
		privilegio.setLectura(lectura?"AC":"IN");
		privilegio.setEscritura(escritura?"AC":"IN");
		listaPrivilegioAceptados.set(obtnerIndexListPrivilegioAceptados(nombrePermiso), privilegio);
	}

	private void eliminarPrivilegio(String nombrePrivilegio){
		if( yaExistePrivilegio(nombrePrivilegio)){
			listaPrivilegioAceptados.remove(obtnerIndexListPrivilegioAceptados(nombrePrivilegio));
		}
	}

	public void resetValues(){
		listaPrivilegioAceptados = new ArrayList<Privilegio>();
		//seguridad
		seguridadTodos=false;
		seguridad=false;
		usuarioL=false;
		usuarioE=false;
		rolesL=false;
		rolesE=false;
		permisoL=false;
		permisoE=false;
		//parametrizacion
		parametrizacionTodos = false;
		parametrizacion =false;
		empresaL=false;
		empresaE=false;
		sucursalL=false;
		sucursalE=false;
		servicioL=false;
		servicioE=false;
		tipoComprobanteL=false;
		tipoComprobanteE=false;
		tipoCambioL=false;
		tipoCambioE=false;
		tipoUfvL=false;
		tipoUfvE=false;
		planCuentaL=false;
		planCuentaE=false;
		centroCostoL=false;
		centroCostoE=false;
		clientesL=false;
		clientesE=false;
		aperturaCierreGestionL=false;
		aperturaCierreGestionE=false;
		//proceso
		procesoTodos = false;
		proceso = false;
		cuentasMonetariasL = false;
		cuentasMonetariasE = false;
		cuentasNoMonetariasL = false;
		cuentasNoMonetariasL = false;
	}

	//Seguridad

	private void verificarAgrupacionSeguridad(){
		if(seguridad && usuarioL && usuarioE && rolesL && rolesE && permisoL && permisoE ){
			seguridadTodos = true;
		}
	}

	public void cargarTrueAgrupacionSeguridad(){
		seguridad = true;
		usuarioL = true;
		usuarioE = true;
		rolesL = true;
		rolesE = true;
		permisoL = true;
		permisoE = true;
		
		agregarPrivilegio(getMessage("group.seguridad"), true, true);
		agregarPrivilegio(getMessage("usuario"), true, true);
		agregarPrivilegio(getMessage("roles"), true, true);
		agregarPrivilegio(getMessage("permiso"), true, true);
	}

	public void cargarFalseAgrupacionSeguridad(){
		seguridad = false;
		usuarioL = false;
		usuarioE = false;
		rolesL = false;
		rolesE = false;
		permisoL = false;
		permisoE = false;
		eliminarPrivilegio(getMessage("group.seguridad"));
		eliminarPrivilegio(getMessage("usuario"));
		eliminarPrivilegio(getMessage("roles"));
		eliminarPrivilegio(getMessage("permiso"));

	}

	//Parametrizacion
	private void verificarAgrupacionParametrizacion(){
		if(parametrizacion && empresaL && empresaE && sucursalL && 	sucursalE && servicioL && 
				servicioE &&  tipoComprobanteL && tipoComprobanteE && tipoCambioL && tipoCambioE && 
				tipoUfvL && tipoUfvE && planCuentaL && planCuentaE && centroCostoL && centroCostoE && 
				clientesL && clientesE && aperturaCierreGestionL && aperturaCierreGestionE   ){
			parametrizacionTodos = true;
		}
	}

	public void cargarTrueAgrupacionParametrizacion(){
		parametrizacion =true;
		empresaL=true;
		empresaE=true;
		sucursalL=true;
		sucursalE=true;
		servicioL=true;
		servicioE=true;
		tipoComprobanteL=true;
		tipoComprobanteE=true;
		tipoCambioL=true;
		tipoCambioE=true;
		tipoUfvL=true;
		tipoUfvE=true;
		planCuentaL=true;
		planCuentaE=true;
		centroCostoL=true;
		centroCostoE=true;
		clientesL=true;
		clientesE=true;
		aperturaCierreGestionL=true;
		aperturaCierreGestionE=true;
		agregarPrivilegio(getMessage("group.parametrizacion"), true, true);
		agregarPrivilegio(getMessage("empresa"), true, true);
		agregarPrivilegio(getMessage("sucursal"), true, true);
		agregarPrivilegio(getMessage("servicio"), true, true);
		agregarPrivilegio(getMessage("tipo.de.comprobante"), true, true);
		agregarPrivilegio(getMessage("tipo.de.cambio"), true, true);
		agregarPrivilegio(getMessage("tipo.de.ufv"), true, true);
		agregarPrivilegio(getMessage("plan.de.cuenta"), true, true);
		agregarPrivilegio(getMessage("centro.costo"), true, true);
		agregarPrivilegio(getMessage("cliente"), true, true);
		agregarPrivilegio(getMessage("apertura.y.cierre.de.gestion"), true, true);
	}

	public void cargarFalseAgrupacionParametrizacion(){
		parametrizacion =false;
		empresaL=false;
		empresaE=false;
		sucursalL=false;
		sucursalE=false;
		servicioL=false;
		servicioE=false;
		tipoComprobanteL=false;
		tipoComprobanteE=false;
		tipoCambioL=false;
		tipoCambioE=false;
		tipoUfvL=false;
		tipoUfvE=false;
		planCuentaL=false;
		planCuentaE=false;
		centroCostoL=false;
		centroCostoE=false;
		clientesL=false;
		clientesE=false;
		aperturaCierreGestionL=false;
		aperturaCierreGestionE=false;
		eliminarPrivilegio(getMessage("group.parametrizacion"));
		eliminarPrivilegio(getMessage("empresa"));
		eliminarPrivilegio(getMessage("sucursal"));
		eliminarPrivilegio(getMessage("servicio"));
		eliminarPrivilegio(getMessage("tipo.de.comprobante"));
		eliminarPrivilegio(getMessage("tipo.de.cambio"));
		eliminarPrivilegio(getMessage("tipo.de.ufv"));
		eliminarPrivilegio(getMessage("plan.de.cuenta"));
		eliminarPrivilegio(getMessage("centro.costo"));
		eliminarPrivilegio(getMessage("cliente"));
		eliminarPrivilegio(getMessage("apertura.y.cierre.de.gestion"));
	}

	//Proceso
	private void verificarAgrupacionProceso(){
		if(proceso && cuentasMonetariasL && cuentasMonetariasE && cuentasNoMonetariasL && cuentasNoMonetariasE){
			procesoTodos = true;
		}
	}

	public void cargarTrueAgrupacionProceso(){
		proceso = true;
		cuentasMonetariasL = true;
		cuentasMonetariasE = true;
		cuentasNoMonetariasL = true;
		cuentasNoMonetariasL = true;
		agregarPrivilegio(getMessage("group.proceso"), true, true);
		agregarPrivilegio(getMessage("cuentas.monetarias"), true, true);
		agregarPrivilegio(getMessage("cuentas.no.monetarias"), true, true);
	}

	public void cargarFalseAgrupacionProceso(){
		proceso = false;
		cuentasMonetariasL = false;
		cuentasMonetariasE = false;
		cuentasNoMonetariasL = false;
		cuentasNoMonetariasL = false;
		eliminarPrivilegio(getMessage("group.proceso"));
		eliminarPrivilegio(getMessage("cuentas.monetarias"));
		eliminarPrivilegio(getMessage("cuentas.no.monetarias"));
	}

	//Formulario

	
	
	public String getMessage(String key) {
		return (String)getExpression("privilege['"+key+"']");
	}

	private Object getExpression(String expression) {
		FacesContext ctx = getCurrentContext();
		ExpressionFactory factory = ctx.getApplication().getExpressionFactory();
		ValueExpression ex = factory.createValueExpression(ctx.getELContext(), "#{"+expression+"}", Object.class);
		return ex.getValue(ctx.getELContext());
	}
	
	protected FacesContext getCurrentContext() {
		return FacesContext.getCurrentInstance();
	}

	// ------- get and set -------
	
	public boolean isSeguridadTodos() {
		return seguridadTodos;
	}

	public void setSeguridadTodos(boolean seguridadTodos) {
		this.seguridadTodos = seguridadTodos;
		if(seguridadTodos){
			cargarTrueAgrupacionSeguridad();
			return;
		}
		cargarFalseAgrupacionSeguridad();
	}

	public boolean isSeguridad() {
		return seguridad;
	}

	public void setSeguridad(boolean seguridad) {
		this.seguridad = seguridad;
	}

	public boolean isUsuarioL() {
		return usuarioL;
	}

	public void setUsuarioL(boolean usuarioL) {
		this.usuarioL = usuarioL;
		if(!usuarioL){
			usuarioE = false;
			eliminarPrivilegio(getMessage("usuario"));
			return;
		}
		agregarPrivilegio(getMessage("usuario"), usuarioL, usuarioE);
	}

	public boolean isUsuarioE() {
		return usuarioE;
	}

	public void setUsuarioE(boolean usuarioE) {
		this.usuarioE = usuarioE;
		agregarPrivilegio(getMessage("usuario"), usuarioL, usuarioE);
	}

	public boolean isRolesL() {
		return rolesL;
	}

	public void setRolesL(boolean rolesL) {
		this.rolesL = rolesL;
		if(!rolesL){
			rolesE = false;
			eliminarPrivilegio(getMessage("roles"));
			return;
		}
		agregarPrivilegio(getMessage("roles"), rolesL, rolesE);
	}

	public boolean isRolesE() {
		return rolesE;
	}

	public void setRolesE(boolean rolesE) {
		this.rolesE = rolesE;
		agregarPrivilegio(getMessage("roles"), rolesL, rolesE);
	}

	public boolean isPermisoL() {
		return permisoL;
	}

	public void setPermisoL(boolean permisoL) {
		this.permisoL = permisoL;
		if(!permisoL){
			permisoE = false;
			eliminarPrivilegio(getMessage("permiso"));
			return;
		}
		agregarPrivilegio(getMessage("permiso"), permisoL, permisoE);
	}

	public boolean isPermisoE() {
		return permisoE;
	}

	public void setPermisoE(boolean permisoE) {
		this.permisoE = permisoE;
		agregarPrivilegio(getMessage("permiso"), permisoL, permisoE);
	}

	public List<Privilegio> getListaPrivilegio() {
		return listaPrivilegio;
	}

	public void setListaPrivilegio(List<Privilegio> listaPrivilegio) {
		this.listaPrivilegio = listaPrivilegio;
	}

	public boolean isParametrizacionTodos() {
		return parametrizacionTodos;
	}

	public void setParametrizacionTodos(boolean parametrizacionTodos) {
		this.parametrizacionTodos = parametrizacionTodos;
		if(parametrizacionTodos){
			cargarTrueAgrupacionParametrizacion();
			return;
		}
		cargarFalseAgrupacionParametrizacion();
	}

	public boolean isParametrizacion() {
		return parametrizacion;
	}

	public void setParametrizacion(boolean parametrizacion) {
		this.parametrizacion = parametrizacion;
	}

	public boolean isEmpresaL() {
		return empresaL;
	}

	public void setEmpresaL(boolean empresaL) {
		this.empresaL = empresaL;
		if(!empresaL){
			empresaE = false;
			eliminarPrivilegio(getMessage("empresa"));
			return;
		}
		agregarPrivilegio(getMessage("empresa"), empresaL, empresaE);
	}

	public boolean isEmpresaE() {
		return empresaE;
	}

	public void setEmpresaE(boolean empresaE) {
		this.empresaE = empresaE;
		agregarPrivilegio(getMessage("empresa"), empresaL, empresaE);
	}

	public boolean isSucursalL() {
		return sucursalL;
	}

	public void setSucursalL(boolean sucursalL) {
		this.sucursalL = sucursalL;
		if(!sucursalL){
			sucursalE = false;
			eliminarPrivilegio(getMessage("sucursal"));
			return;
		}
		agregarPrivilegio(getMessage("sucursal"), sucursalL, sucursalE);
	}

	public boolean isSucursalE() {
		return sucursalE;
	}

	public void setSucursalE(boolean sucursalE) {
		this.sucursalE = sucursalL;
		agregarPrivilegio(getMessage("sucursal"), sucursalL, sucursalE);
	}

	public boolean isServicioL() {
		return servicioL;
	}

	public void setServicioL(boolean servicioL) {
		this.servicioL = servicioL;
		if(!servicioL){
			servicioE = false;
			eliminarPrivilegio(getMessage("servicio"));
			return;
		}
		agregarPrivilegio(getMessage("servicio"), servicioL, servicioE);
	}

	public boolean isServicioE() {
		return servicioE;
	}

	public void setServicioE(boolean servicioE) {
		this.servicioE = servicioE;
		agregarPrivilegio(getMessage("servicio"), servicioL, servicioE);
	}

	public boolean isTipoComprobanteL() {
		return tipoComprobanteL;
	}

	public void setTipoComprobanteL(boolean tipoComprobanteL) {
		this.tipoComprobanteL = tipoComprobanteL;
		if(!tipoComprobanteL){
			tipoComprobanteE = false;
			eliminarPrivilegio(getMessage("tipo.de.comprobante"));
			return;
		}
		agregarPrivilegio(getMessage("tipo.de.comprobante"), tipoComprobanteL, tipoComprobanteE);
	}

	public boolean isTipoComprobanteE() {
		return tipoComprobanteE;
	}

	public void setTipoComprobanteE(boolean tipoComprobanteE) {
		this.tipoComprobanteE = tipoComprobanteE;
		agregarPrivilegio(getMessage("tipo.de.comprobante"), tipoComprobanteL, tipoComprobanteE);
	}

	public boolean isTipoCambioL() {
		return tipoCambioL;
	}

	public void setTipoCambioL(boolean tipoCambioL) {
		this.tipoCambioL = tipoCambioL;
		if(!tipoCambioL){
			tipoCambioE = false;
			eliminarPrivilegio(getMessage("tipo.de.cambio"));
			return;
		}
		agregarPrivilegio(getMessage("tipo.de.cambio"), tipoCambioL, tipoCambioE);
	}

	public boolean isTipoCambioE() {
		return tipoCambioE;
	}

	public void setTipoCambioE(boolean tipoCambioE) {
		this.tipoCambioE = tipoCambioE;
		agregarPrivilegio(getMessage("tipo.de.cambio"), tipoCambioL, tipoCambioE);
	}

	public boolean isTipoUfvL() {
		return tipoUfvL;
	}

	public void setTipoUfvL(boolean tipoUfvL) {
		this.tipoUfvL = tipoUfvL;
		if(!tipoUfvL){
			tipoUfvE = false;
			eliminarPrivilegio(getMessage("tipo.de.ufv"));
			return;
		}
		agregarPrivilegio(getMessage("tipo.de.ufv"), tipoUfvL, tipoUfvE);
	}

	public boolean isTipoUfvE() {
		return tipoUfvE;
	}

	public void setTipoUfvE(boolean tipoUfvE) {
		this.tipoUfvE = tipoUfvE;
		agregarPrivilegio(getMessage("tipo.de.ufv"), tipoUfvL, tipoUfvE);
	}

	public boolean isPlanCuentaL() {
		return planCuentaL;
	}

	public void setPlanCuentaL(boolean planCuentaL) {
		this.planCuentaL = planCuentaL;
		if(!planCuentaL){
			planCuentaE = false;
			eliminarPrivilegio(getMessage("plan.de.cuenta"));
			return;
		}
		agregarPrivilegio(getMessage("plan.de.cuenta"), planCuentaL, planCuentaE);
	}

	public boolean isPlanCuentaE() {
		return planCuentaE;
	}

	public void setPlanCuentaE(boolean planCuentaE) {
		this.planCuentaE = planCuentaE;
		agregarPrivilegio(getMessage("plan.de.cuenta"), planCuentaL, planCuentaE);
	}

	public boolean isCentroCostoL() {
		return centroCostoL;
	}

	public void setCentroCostoL(boolean centroCostoL) {
		this.centroCostoL = centroCostoL;
		if(!centroCostoL){
			centroCostoE = false;
			eliminarPrivilegio(getMessage("centro.costo"));
			return;
		}
		agregarPrivilegio(getMessage("centro.costo"), centroCostoL, centroCostoE);
	}

	public boolean isCentroCostoE() {
		return centroCostoE;
	}

	public void setCentroCostoE(boolean centroCostoE) {
		this.centroCostoE = centroCostoE;
		agregarPrivilegio(getMessage("centro.costo"), centroCostoL, centroCostoE);
	}

	public boolean isClientesL() {
		return clientesL;
	}

	public void setClientesL(boolean clientesL) {
		this.clientesL = clientesL;
		if(!clientesL){
			clientesE = false;
			eliminarPrivilegio(getMessage("cliente"));
			return;
		}
		agregarPrivilegio(getMessage("cliente"), clientesL, clientesE);
	}

	public boolean isClientesE() {
		return clientesE;
	}

	public void setClientesE(boolean clientesE) {
		this.clientesE = clientesE;
		agregarPrivilegio(getMessage("cliente"), clientesL, clientesE);
	}

	public boolean isAperturaCierreGestionL() {
		return aperturaCierreGestionL;
	}

	public void setAperturaCierreGestionL(boolean aperturaCierreGestionL) {
		this.aperturaCierreGestionL = aperturaCierreGestionL;
		if(!aperturaCierreGestionL){
			aperturaCierreGestionE = false;
			eliminarPrivilegio(getMessage("apertura.y.cierre.de.gestion"));
			return;
		}
		agregarPrivilegio(getMessage("apertura.y.cierre.de.gestion"), aperturaCierreGestionL, aperturaCierreGestionE);
	}

	public boolean isAperturaCierreGestionE() {
		return aperturaCierreGestionE;
	}

	public void setAperturaCierreGestionE(boolean aperturaCierreGestionE) {
		this.aperturaCierreGestionE = aperturaCierreGestionE;
		agregarPrivilegio(getMessage("apertura.y.cierre.de.gestion"), aperturaCierreGestionL, aperturaCierreGestionE);
	}

	public boolean isProcesoTodos() {
		return procesoTodos;
	}

	public void setProcesoTodos(boolean procesoTodos) {
		this.procesoTodos = procesoTodos;
		if(procesoTodos){
			cargarTrueAgrupacionProceso();
			return;
		}
		cargarFalseAgrupacionProceso();
	}

	public boolean isProceso() {
		return proceso;
	}

	public void setProceso(boolean proceso) {
		this.proceso = proceso;
	}

	public boolean isCuentasMonetariasL() {
		return cuentasMonetariasL;
	}

	public void setCuentasMonetariasL(boolean cuentasMonetariasL) {
		this.cuentasMonetariasL = cuentasMonetariasL;
		if(!cuentasMonetariasL){
			cuentasMonetariasE = false;
			eliminarPrivilegio(getMessage("cuentas.monetarias"));
			return;
		}
		agregarPrivilegio(getMessage("cuentas.monetarias"), cuentasMonetariasL, cuentasMonetariasE);
	}

	public boolean isCuentasNoMonetariasL() {
		return cuentasNoMonetariasL;
	}

	public void setCuentasNoMonetariasL(boolean cuentasNoMonetariasL) {
		this.cuentasNoMonetariasL = cuentasNoMonetariasL;
		if(!cuentasNoMonetariasL){
			cuentasNoMonetariasE = false;
			eliminarPrivilegio(getMessage("cuentas.no.monetarias"));
			return;
		}
		agregarPrivilegio(getMessage("cuentas.no.monetarias"), cuentasNoMonetariasL, cuentasNoMonetariasE);
	}

	public boolean isCuentasNoMonetariasE() {
		return cuentasNoMonetariasE;
	}

	public void setCuentasNoMonetariasE(boolean cuentasNoMonetariasE) {
		this.cuentasNoMonetariasE = cuentasNoMonetariasE;
		agregarPrivilegio(getMessage("cuentas.no.monetarias"), cuentasNoMonetariasL, cuentasNoMonetariasE);
	}

	public boolean isCuentasMonetariasE() {
		return cuentasMonetariasE;
	}

	public void setCuentasMonetariasE(boolean cuentasMonetariasE) {
		this.cuentasMonetariasE = cuentasMonetariasE;
		agregarPrivilegio(getMessage("cuentas.monetarias"), cuentasMonetariasL, cuentasMonetariasE);
	}

	public List<Permiso> getListPermiso() {
		return listPermiso;
	}

	public void setListPermiso(List<Permiso> listPermiso) {
		this.listPermiso = listPermiso;
	}

	public List<Privilegio> getListaPrivilegioAceptados(){
		return listaPrivilegioAceptados;
	}
}

package bo.com.erp360.webapp.util;

import java.util.ArrayList;
import java.util.List;

import bo.com.erp360.webapp.model.Privilegio;

public class EDPermisoAux {

	//parametros
	private List<Privilegio> listaPrivilegio = new ArrayList<Privilegio>();

	//seguridad
	private boolean   seguridadAll			= false;
	private boolean   seguridad				= false;
	private Boolean[] usuario				= {false,false,false};
	private Boolean[] roles					= {false,false,false};
	private Boolean[] permiso				= {false,false,false};

	//parametrizacion
	private boolean   parametrizacionAll	= false;
	private boolean   parametrizacion		= false;
	private Boolean[] empresa				= {false,false,false};
	private Boolean[] sucursal				= {false,false,false};
	private Boolean[] servicio				= {false,false,false};
	private Boolean[] tipoComprobante		= {false,false,false};
	private Boolean[] tipoCambio			= {false,false,false};
	private Boolean[] tipoMonedaUFV			= {false,false,false};
	private Boolean[] planCuenta			= {false,false,false};
	private Boolean[] centroCosto			= {false,false,false};
	private Boolean[] cliente				= {false,false,false};
	private Boolean[] aperturaCierreGestion	= {false,false,false};

	//proceso
	private boolean   procesoAll			= false;
	private boolean   proceso				= false;
	private Boolean[] cuentasMonetarias		= {false,false,false};
	private Boolean[] cuentasNoMonetarias	= {false,false,false};

	//formulario
	private boolean   formularioAll			= false;
	private boolean	  formulario			= false;
	private Boolean[] comprobante			= {false,false,false};
	private Boolean[] compra				= {false,false,false};
	private Boolean[] venta					= {false,false,false};
	private Boolean[] cotizacion			= {false,false,false};
	private Boolean[] activoFijo			= {false,false,false};

	//reporte
	private boolean   reporteAll			= false;
	private boolean   reporte				= false;
	//libros
	private boolean   librosAll				= false;
	private boolean   libros				= false;
	private Boolean[] libroDiario			= {false,false,false};
	//estado financiero
	private boolean   estadoFinancieroAll	= false;
	private boolean   estadoFinanciero		= false;
	//cuadro activo fijo
	private boolean   cuadroActivoFijoAll	= false;
	private boolean   cuadroActivoFijo		= false;




	// Constructor
	public EDPermisoAux() {
		super();
	}

	////////////////////////////////////////////////////////////////////////////////

	private List<String[]> mListPrivilegio = new ArrayList<String[]>();
	private boolean mPrivilegio;
	private boolean mLectura;
	private boolean mEscritura;

	public void mCargarPrivilegio(){
		for(Privilegio p: listaPrivilegio){
			String[] a = {p.getPermiso().getName(),p.getLectura(),p.getEscritura()};
			mListPrivilegio.add(a);
		}		
	}

	public void mResetValuesPrivilegio(){
		mListPrivilegio = new ArrayList<String[]>();
	}

	public void mExistPrivilegio(String privilegio){
		for(String[] s: mListPrivilegio ){
			if(s[0].equals(privilegio)){
				mPrivilegio = true;
				if(s[1].equals("AC")){ mLectura = true; } else{ mLectura = false; }
				if(s[2].equals("AC")){ mEscritura = true; } else{ mEscritura = true; }
			}
		}
	}
	////////////////////////////////////////////////////////////////////////////////

	public void resetValuePrivilegio(){
		//parametros
		listaPrivilegio = new ArrayList<Privilegio>();

		//seguridad
		seguridadAll			= false;
		seguridad				= false;
		usuario[0]				= false;
		usuario[1]				= false;
		usuario[2]				= false;
		roles[0]					= false;
		roles[1]					= false;
		roles[2]					= false;
		permiso[0]				= false;
		permiso[1]				= false;
		permiso[2]				= false;

		//parametrizacion
		parametrizacionAll	= false;
		parametrizacion		= false;
		empresa[0]				= false;
		empresa[1]				= false;
		empresa[2]				= false;
		sucursal[0]				= false;
		sucursal[1]				= false;
		sucursal[2]				= false;
		servicio[0]				= false;
		servicio[1]				= false;
		servicio[2]				= false;
		tipoComprobante[0]		= false;
		tipoComprobante[1]		= false;
		tipoComprobante[2]		= false;
		tipoCambio[0]			= false;
		tipoCambio[1]			= false;
		tipoCambio[2]			= false;
		tipoMonedaUFV[0]			= false;
		tipoMonedaUFV[1]			= false;
		tipoMonedaUFV[2]			= false;
		planCuenta[0]			= false;
		planCuenta[1]			= false;
		planCuenta[2]			= false;
		centroCosto[0]			= false;
		centroCosto[1]			= false;
		centroCosto[2]			= false;
		cliente[0]				= false;
		cliente[1]				= false;
		cliente[2]				= false;
		aperturaCierreGestion[0]	= false;
		aperturaCierreGestion[1]	= false;
		aperturaCierreGestion[2]	= false;

		//proceso
		procesoAll			= false;
		proceso				= false;
		cuentasMonetarias[0]		= false;
		cuentasMonetarias[1]		= false;
		cuentasMonetarias[2]		= false;
		cuentasNoMonetarias[0]	= false;
		cuentasNoMonetarias[1]	= false;
		cuentasNoMonetarias[2]	= false;

		//formulario
		formularioAll			= false;
		formulario			= false;
		comprobante[0]			= false;
		comprobante[1]			= false;
		comprobante[2]			= false;
		compra[0]				= false;
		compra[1]				= false;
		compra[2]				= false;
		venta[0]					= false;
		venta[1]					= false;
		venta[2]					= false;
		cotizacion[0]			= false;
		cotizacion[1]			= false;
		cotizacion[2]			= false;
		activoFijo[0]			= false;
		activoFijo[1]			= false;
		activoFijo[2]			= false;

		//reporte
		reporteAll			= false;
		reporte				= false;
		//libros
		librosAll				= false;
		libros				= false;
		libroDiario[0]			= false;
		libroDiario[1]			= false;
		libroDiario[2]			= false;
		//estado financiero
		estadoFinancieroAll	= false;
		estadoFinanciero		= false;
		//cuadro activo fijo
		cuadroActivoFijoAll	= false;
		cuadroActivoFijo		= false;
	}

	// GET and SET
	public boolean isSeguridadAll() {
		return seguridadAll;
	}

	public void setSeguridadAll(boolean seguridadAll) {
		this.seguridadAll = seguridadAll;
	}

	public boolean isSeguridad() {
		return seguridad;
	}

	public void setSeguridad(boolean seguridad) {
		this.seguridad = seguridad;
	}

	public Boolean[] getUsuario() {
		return usuario;
	}

	public void setUsuario(Boolean[] usuario) {
		this.usuario = usuario;
	}

	public Boolean[] getRoles() {
		return roles;
	}

	public void setRoles(Boolean[] roles) {
		this.roles = roles;
	}

	public Boolean[] getPermiso() {
		return permiso;
	}

	public void setPermiso(Boolean[] permiso) {
		this.permiso = permiso;
	}

	public boolean isParametrizacionAll() {
		return parametrizacionAll;
	}

	public void setParametrizacionAll(boolean parametrizacionAll) {
		this.parametrizacionAll = parametrizacionAll;
	}

	public boolean isParametrizacion() {
		return parametrizacion;
	}

	public void setParametrizacion(boolean parametrizacion) {
		this.parametrizacion = parametrizacion;
	}

	public Boolean[] getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Boolean[] empresa) {
		this.empresa = empresa;
	}

	public Boolean[] getSucursal() {
		return sucursal;
	}

	public void setSucursal(Boolean[] sucursal) {
		this.sucursal = sucursal;
	}

	public Boolean[] getServicio() {
		return servicio;
	}

	public void setServicio(Boolean[] servicio) {
		this.servicio = servicio;
	}

	public Boolean[] getTipoCambio() {
		return tipoCambio;
	}

	public void setTipoCambio(Boolean[] tipoCambio) {
		this.tipoCambio = tipoCambio;
	}

	public Boolean[] getTipoComprobante() {
		return tipoComprobante;
	}

	public void setTipoComprobante(Boolean[] tipoComprobante) {
		this.tipoComprobante = tipoComprobante;
	}

	public Boolean[] getTipoMonedaUFV() {
		return tipoMonedaUFV;
	}

	public void setTipoMonedaUFV(Boolean[] tipoMonedaUFV) {
		this.tipoMonedaUFV = tipoMonedaUFV;
	}


	public Boolean[] getPlanCuenta() {
		return planCuenta;
	}


	public void setPlanCuenta(Boolean[] planCuenta) {
		this.planCuenta = planCuenta;
	}


	public Boolean[] getCentroCosto() {
		return centroCosto;
	}


	public void setCentroCosto(Boolean[] centroCosto) {
		this.centroCosto = centroCosto;
	}


	public Boolean[] getCliente() {
		return cliente;
	}


	public void setCliente(Boolean[] cliente) {
		this.cliente = cliente;
	}


	public Boolean[] getAperturaCierreGestion() {
		return aperturaCierreGestion;
	}


	public void setAperturaCierreGestion(Boolean[] aperturaCierreGestion) {
		this.aperturaCierreGestion = aperturaCierreGestion;
	}


	public boolean isProcesoAll() {
		return procesoAll;
	}


	public void setProcesoAll(boolean procesoAll) {
		this.procesoAll = procesoAll;
	}


	public boolean isProceso() {
		return proceso;
	}


	public void setProceso(boolean proceso) {
		this.proceso = proceso;
	}


	public Boolean[] getCuentasMonetarias() {
		return cuentasMonetarias;
	}


	public void setCuentasMonetarias(Boolean[] cuentasMonetarias) {
		this.cuentasMonetarias = cuentasMonetarias;
	}


	public Boolean[] getCuentasNoMonetarias() {
		return cuentasNoMonetarias;
	}


	public void setCuentasNoMonetarias(Boolean[] cuentasNoMonetarias) {
		this.cuentasNoMonetarias = cuentasNoMonetarias;
	}


	public boolean isFormularioAll() {
		return formularioAll;
	}


	public void setFormularioAll(boolean formularioAll) {
		this.formularioAll = formularioAll;
	}


	public boolean isFormulario() {
		return formulario;
	}


	public void setFormulario(boolean formulario) {
		this.formulario = formulario;
	}


	public Boolean[] getComprobante() {
		return comprobante;
	}


	public void setComprobante(Boolean[] comprobante) {
		this.comprobante = comprobante;
	}


	public Boolean[] getCompra() {
		return compra;
	}


	public void setCompra(Boolean[] compra) {
		this.compra = compra;
	}


	public Boolean[] getVenta() {
		return venta;
	}


	public void setVenta(Boolean[] venta) {
		this.venta = venta;
	}


	public Boolean[] getCotizacion() {
		return cotizacion;
	}


	public void setCotizacion(Boolean[] cotizacion) {
		this.cotizacion = cotizacion;
	}


	public Boolean[] getActivoFijo() {
		return activoFijo;
	}


	public void setActivoFijo(Boolean[] activoFijo) {
		this.activoFijo = activoFijo;
	}


	public boolean isReporteAll() {
		return reporteAll;
	}


	public void setReporteAll(boolean reporteAll) {
		this.reporteAll = reporteAll;
	}


	public boolean isReporte() {
		return reporte;
	}


	public void setReporte(boolean reporte) {
		this.reporte = reporte;
	}


	public boolean isLibrosAll() {
		return librosAll;
	}


	public void setLibrosAll(boolean librosAll) {
		this.librosAll = librosAll;
	}


	public boolean isLibros() {
		return libros;
	}


	public void setLibros(boolean libros) {
		this.libros = libros;
	}


	public Boolean[] getLibroDiario() {
		return libroDiario;
	}


	public void setLibroDiario(Boolean[] libroDiario) {
		this.libroDiario = libroDiario;
	}


	public boolean isEstadoFinancieroAll() {
		return estadoFinancieroAll;
	}


	public void setEstadoFinancieroAll(boolean estadoFinancieroAll) {
		this.estadoFinancieroAll = estadoFinancieroAll;
	}


	public boolean isEstadoFinanciero() {
		return estadoFinanciero;
	}


	public void setEstadoFinanciero(boolean estadoFinanciero) {
		this.estadoFinanciero = estadoFinanciero;
	}


	public boolean isCuadroActivoFijoAll() {
		return cuadroActivoFijoAll;
	}


	public void setCuadroActivoFijoAll(boolean cuadroActivoFijoAll) {
		this.cuadroActivoFijoAll = cuadroActivoFijoAll;
	}


	public boolean isCuadroActivoFijo() {
		return cuadroActivoFijo;
	}


	public void setCuadroActivoFijo(boolean cuadroActivoFijo) {
		this.cuadroActivoFijo = cuadroActivoFijo;
	}

	public List<Privilegio> getListaPrivilegio() {
		return listaPrivilegio;
	}

	public void setListaPrivilegio(List<Privilegio> listaPrivilegio) {
		this.listaPrivilegio = listaPrivilegio;
	}

	public List<String[]> getmListPrivilegio() {
		return mListPrivilegio;
	}

	public void setmListPrivilegio(List<String[]> mListPrivilegio) {
		this.mListPrivilegio = mListPrivilegio;
	}	

	public boolean ismPrivilegio() {
		return mPrivilegio;
	}

	public void setmPrivilegio(boolean mPrivilegio) {
		this.mPrivilegio = mPrivilegio;
	}

	public boolean ismLectura() {
		return mLectura;
	}

	public void setmLectura(boolean mLectura) {
		this.mLectura = mLectura;
	}

	public boolean ismEscritura() {
		return mEscritura;
	}

	public void setmEscritura(boolean mEscritura) {
		this.mEscritura = mEscritura;
	}

}

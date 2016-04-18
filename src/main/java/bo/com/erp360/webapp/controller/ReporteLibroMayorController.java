package bo.com.erp360.webapp.controller;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.event.Event;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.primefaces.component.datatable.DataTable;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.data.PageEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.richfaces.cdi.push.Push;

import bo.com.erp360.webapp.data.MayorRepository;
import bo.com.erp360.webapp.data.MonedaRepository;
import bo.com.erp360.webapp.data.PlanCuentaRepository;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.Mayor;
import bo.com.erp360.webapp.model.MonedaEmpresa;
import bo.com.erp360.webapp.model.PlanCuenta;
import bo.com.erp360.webapp.util.FacesUtil;
import bo.com.erp360.webapp.util.Fechas;
import bo.com.erp360.webapp.util.SessionMain;

@Named(value = "reporteLibroMayorController")
@ConversationScoped
public class ReporteLibroMayorController implements Serializable {

	private static final long serialVersionUID = -1811510000189343775L;

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	@Inject
	Conversation conversation;

	@Inject
	private MayorRepository mayorRepository;

	@Inject
	private MonedaRepository monedaRepository;

	@Inject
	private PlanCuentaRepository planCuentaRepository;

	private Logger log = Logger.getLogger(this.getClass());

	//login
	private String nombreUsuario;
	private @Inject SessionMain sessionMain; //variable del login
	private Empresa empresaLogin;
	private Gestion gestionLogin;

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventSucursal;

	private String nombreMonedaEmpresa;
	private String urlLibroMayor;
	private Date fechaInicial;
	private Date fechaFinal;
	private Date fechaActual;
	private String tipoConsulta ;
	private String seleccionTipoCuenta;
	private String nombreMes;
	private String nombreSelectFiltro;
	private String cuenta;
	private String nombreCuenta;

	private PlanCuenta busquedaCuentaInicial;
	private PlanCuenta busquedaCuentaFinal;

	private MonedaEmpresa selectedMonedaEmpresa;
	private String textoAutoCompleteCuentaInicial;
	private String textoAutoCompleteCuentaFinal;

	private List<MonedaEmpresa> listMonedaEmpresa;
	private List<PlanCuenta> listCuentasAuxiliares = new ArrayList<PlanCuenta>();
	private List<PlanCuenta> listPlanCuenta = new  ArrayList<PlanCuenta>();
	private String[] arrayMes = {"ENERO","FEBRERO","MARZO","ABRIL","MAYO","JUNIO","JULIO","AGOSTO","SEPTIEMBRE","OCTUBRE","NOVIEMBRE","DICIEMBRE"};
	private String[] arrayFiltro = {"FECHA","MES"};

	//component Lazydatamodel Primefaces
	private int first; 
	private int pageSize;
	private int lengthList;

	private List<Mayor> listMayor = new ArrayList<Mayor>();
	private double saldoAnterior = 0;

	private LazyDataModel<Mayor> listReport;
	private PlanCuenta planCuenta;//plan de cuenta mostrada actualmente
	/*
	 * 1.- Traer el total de registro que tiene el filtro
	 *     - El filtro se lo hizo por periodo o por fecha
	 * 2.- obtener el id registro de la primer cuenta
	 * 3.- obtener el ultimo registro de la cuenta
	 * 4.- Capturar el evento de paginacion y con ese poder actualizar el outputext de la cuenta
	 */

	//estados
	private boolean seleccionado = false;

	@PostConstruct
	public void initNewReporte() {

		log.info(" init new initNewReporte");
		beginConversation();
		nombreUsuario = sessionMain.getUsuarioLoggin().getLogin();
		empresaLogin = sessionMain.getEmpresaLoggin();
		gestionLogin = sessionMain.getGestionLoggin();

		listMonedaEmpresa = monedaRepository.findMonedaEmpresaAllByEmpresa(empresaLogin);
		nombreMonedaEmpresa = listMonedaEmpresa.get(0).getMoneda().getNombre();
		selectedMonedaEmpresa = listMonedaEmpresa.get(0);
		nombreSelectFiltro = arrayFiltro[1];
		cuenta = "";
		loadValuesDefault();
	}

	private void loadValuesDefault(){
		seleccionTipoCuenta = "todas";
		seleccionado = false;
		nombreMes = arrayMes[0];
		tipoConsulta = "periodo";
		fechaInicial = new Date();
		fechaFinal = new Date();
		fechaActual = new Date();

		listCuentasAuxiliares = planCuentaRepository.findAllAuxiliarByEmpresa(empresaLogin,gestionLogin);
		if(listCuentasAuxiliares.size()>0){
			busquedaCuentaInicial = listCuentasAuxiliares.get(0);
			textoAutoCompleteCuentaInicial = busquedaCuentaInicial.getDescripcion();

			busquedaCuentaFinal = listCuentasAuxiliares.get(listCuentasAuxiliares.size() -1);
			textoAutoCompleteCuentaFinal = busquedaCuentaFinal.getDescripcion();
		}

		loadFechasByNumeroMes();
	}

	public void test(PageEvent event){
		int page = event.getPage();
		//AsientoContable a = listReport.g
		log.info("test page="+page);
		// cuenta = "test | fist="+first+" | pageSize="+pageSize+" | lengthList="+lengthList;

		FacesUtil.updateComponent("formQuery:outputLabelCuenta");
	}

	@SuppressWarnings("unchecked")
	public void procesarReporte(boolean primeraVez){
		if(primeraVez){
			listPlanCuenta = mayorRepository.findByFecha(fechaInicial, fechaFinal,empresaLogin,gestionLogin);

			if(listPlanCuenta.size()>0){
				planCuenta = listPlanCuenta.get(0);
				cargarSaldoAnterior();
			}
		}
		if(listPlanCuenta.size()>0){
			lengthList = countTotalRecord();
			setListReport(new LazyDataModel() {
				@Override
				public List<Mayor> load(int first, int pageSize, String sortField,
						SortOrder sortOrder, Map filters) {
					setFirst(first) ;
					setPageSize(pageSize );
					return obtenerListMayor(first,pageSize);
				}
			});
			listReport.setRowCount(lengthList);
			listReport.setPageSize(pageSize);
		}
	}

	private int countTotalRecord(){
		return mayorRepository.countTotalRecordByFechas(fechaInicial, fechaFinal, empresaLogin).intValue();
	}

	private List<Mayor> obtenerListMayor(int first, int pageSize){
		listMayor =   mayorRepository.findByFechaAndPlanCuenta(first,pageSize,fechaInicial, fechaFinal, planCuenta);
		return listMayor;
	}

	private void cargarSaldoAnterior(){
		saldoAnterior = mayorRepository.findSaldoAnterior(Fechas.restarDiasFecha(fechaInicial, 1), planCuenta);
	}

	public Integer obtnerNumeroDeMes(String mes){
		for(Integer i=0; i < arrayMes.length;i++){
			if(arrayMes[i].toString().equals(mes)){
				return i +1;
			}
		}
		return -1;
	}

	public void beginConversation() {
		if (conversation.isTransient()) {
			log.info("beginning conversation : " + this.conversation);
			conversation.begin();
			log.info("---> Init Conversation");
		}
	}

	public void endConversation() {
		if (!conversation.isTransient()) {
			conversation.end();
		}
	}

	public void resetearFitrosTabla(String id) {
		DataTable table = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(id);
		table.setSelection(null);
		table.reset();
	}

	public void onRowSelect(SelectEvent event) {
		seleccionado = true ;		
	}

	// ------------------------ reporte

	public String loadURL(){
		try{
			HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();  
			String urlPath = request.getRequestURL().toString();
			urlPath = urlPath.substring(0, urlPath.length() - request.getRequestURI().length()) + request.getContextPath() + "/";
			String urlPDFreporte =urlPath+"ReporteLibroMayor?pIdGestion="+gestionLogin.getId()+"&pIdEmpresa="+empresaLogin.getId()+"&pIdPlanCuenta="+planCuenta.getId()+"&pFechaInicio="+Fechas.deDateAString(fechaInicial)+"&pFechaFin="+Fechas.deDateAString(fechaFinal)+"&pUsuario="+nombreUsuario;
			log.info("getURL() -> "+urlPDFreporte);
			return urlPDFreporte;
		}catch(Exception e){
			log.error("getURL error: "+e.getMessage());
			return "error";
		}
	}

	public void actualizarForm(){
		seleccionado = false;
		setUrlLibroMayor(loadURL());
		log.info("cargando......");
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("PF('dlgVistaPreviaLibroMayor').show();");
	}

	public List<PlanCuenta> completeTextCuenta(String query) {
		String upperQuery = query.toUpperCase();
		List<PlanCuenta> results = new ArrayList<PlanCuenta>();
		for(PlanCuenta i : listCuentasAuxiliares) {
			if(i.getDescripcion().toUpperCase().startsWith(upperQuery)){
				results.add(i);
			}
		}         
		return results;
	}


	public void onItemSelectCuentaInicial(SelectEvent event) {
		String nombre =  event.getObject().toString();
		for(PlanCuenta s : listCuentasAuxiliares){
			if(s.getDescripcion().equals(nombre)){
				busquedaCuentaInicial = s;

			}
		}
	}

	public void onItemSelectCuentaFinal(SelectEvent event) {
		String nombre =  event.getObject().toString();
		for(PlanCuenta s : listCuentasAuxiliares){
			if(s.getDescripcion().equals(nombre)){
				busquedaCuentaFinal = s;
			}
		}
	}

	public void loadFechasByNumeroMes(){
		Date fecha = new Date();
		int month = Integer.parseInt(new SimpleDateFormat("MM").format(fecha));
		nombreMes = arrayMes[month - 1];
		int year=gestionLogin.getGestion();
		int day=1;
		Calendar fecha1 = new GregorianCalendar(year, month-1, day);
		this.fechaInicial = fecha1.getTime();
		int day2=Fechas.getUltimoDiaMes(year,month);
		Calendar fecha2 = new GregorianCalendar(year, month-1, day2);
		this.fechaFinal = fecha2.getTime();
	}

	public void loadFechasByPeriodo(String mes){
		int month = obtnerNumeroDeMes(mes);
		int year=gestionLogin.getGestion();
		int day=1;
		Calendar fecha1 = new GregorianCalendar(year, month-1, day);
		this.fechaInicial = fecha1.getTime();
		int day2=Fechas.getUltimoDiaMes(year,month);
		Calendar fecha2 = new GregorianCalendar(year, month-1, day2);
		this.fechaFinal = fecha2.getTime();
	}

	private PlanCuenta obtenerPlanCuentaByLocal(String nombre){
		for(PlanCuenta pc: listCuentasAuxiliares){
			if(pc.getDescripcion().equals(nombre)){
				return pc;
			}
		}
		return null;
	}

	public void cargarCuentasDefault(){
		if(listCuentasAuxiliares.size()>0){
			busquedaCuentaInicial = listCuentasAuxiliares.get(0);
			textoAutoCompleteCuentaInicial = busquedaCuentaInicial.getDescripcion();

			busquedaCuentaFinal = listCuentasAuxiliares.get(listCuentasAuxiliares.size() -1);
			textoAutoCompleteCuentaFinal = busquedaCuentaFinal.getDescripcion();
		}
	}

	// ----- get and set -----

	public boolean isSeleccionado() {
		return seleccionado;
	}

	public void setSeleccionado(boolean seleccionado) {
		this.seleccionado = seleccionado;
	}

	public String getNombreUsuario() {
		return nombreUsuario;
	}

	public void setNombreUsuario(String nombreUsuario) {
		this.nombreUsuario = nombreUsuario;
	}

	public String getUrlLibroMayor() {
		return urlLibroMayor;
	}

	public void setUrlLibroMayor(String urlLibroMayor) {
		this.urlLibroMayor = urlLibroMayor;
	}

	public Date getFechaInicial() {
		return fechaInicial;
	}

	public void setFechaInicial(Date fechaInicial) {
		this.fechaInicial = fechaInicial;
	}

	public Date getFechaFinal() {
		return fechaFinal;
	}

	public void setFechaFinal(Date fechaFinal) {
		this.fechaFinal = fechaFinal;
	}

	public int getFirst() {
		return first;
	}

	public void setFirst(int first) {
		this.first = first;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getLengthList() {
		return lengthList;
	}

	public void setLengthList(int lengthList) {
		this.lengthList = lengthList;
	}

	public LazyDataModel<Mayor> getListReport() {
		return listReport;
	}

	public void setListReport(LazyDataModel<Mayor> listReport) {
		this.listReport = listReport;
	}

	public String getTipoConsulta() {
		return tipoConsulta;
	}

	public void setTipoConsulta(String tipoConsulta) {
		this.tipoConsulta = tipoConsulta;
	}

	public String getNombreMes() {
		return nombreMes;
	}

	public void setNombreMes(String nombreMes) {
		this.nombreMes = nombreMes;
		loadFechasByPeriodo(nombreMes);
	}

	public String[] getArrayMes() {
		return arrayMes;
	}

	public void setArrayMes(String[] arrayMes) {
		this.arrayMes = arrayMes;
	}

	public String[] getArrayFiltro() {
		return arrayFiltro;
	}

	public void setArrayFiltro(String[] arrayFiltro) {
		this.arrayFiltro = arrayFiltro;
	}

	public String getNombreMonedaEmpresa() {
		return nombreMonedaEmpresa;
	}

	public void setNombreMonedaEmpresa(String nombreMonedaEmpresa) {
		this.nombreMonedaEmpresa = nombreMonedaEmpresa;
	}

	public List<MonedaEmpresa> getListMonedaEmpresa() {
		return listMonedaEmpresa;
	}

	public void setListMonedaEmpresa(List<MonedaEmpresa> listMonedaEmpresa) {
		this.listMonedaEmpresa = listMonedaEmpresa;
	}

	public MonedaEmpresa getSelectedMonedaEmpresa() {
		return selectedMonedaEmpresa;
	}

	public void setSelectedMonedaEmpresa(MonedaEmpresa selectedMonedaEmpresa) {
		this.selectedMonedaEmpresa = selectedMonedaEmpresa;
	}

	public String getNombreSelectFiltro() {
		return nombreSelectFiltro;
	}

	public void setNombreSelectFiltro(String nombreSelectFiltro) {
		this.nombreSelectFiltro = nombreSelectFiltro;
	}

	public Date getFechaActual() {
		return fechaActual;
	}

	public void setFechaActual(Date fechaActual) {
		this.fechaActual = fechaActual;
	}

	public String getCuenta() {
		return cuenta;
	}

	public void setCuenta(String cuenta) {
		this.cuenta = cuenta;
	}

	public PlanCuenta getPlanCuenta() {
		return planCuenta;
	}

	public void setPlanCuenta(PlanCuenta planCuenta) {
		this.planCuenta = planCuenta;
	}

	public String getNombreCuenta() {
		return nombreCuenta;
	}

	public void setNombreCuenta(String nombreCuenta) {
		log.info("setNombreCuenta : "+nombreCuenta);
		this.nombreCuenta = nombreCuenta;
		planCuenta = obtenerPlanCuentaByLocal(nombreCuenta);
		procesarReporte(false);
		cargarSaldoAnterior();
	}

	public List<PlanCuenta> getListPlanCuenta() {
		return listPlanCuenta;
	}

	public void setListPlanCuenta(List<PlanCuenta> listPlanCuenta) {
		this.listPlanCuenta = listPlanCuenta;
	}

	public double getSaldoAnterior() {
		return saldoAnterior;
	}

	public void setSaldoAnterior(double saldoAnterior) {
		this.saldoAnterior = saldoAnterior;
	}

	public List<Mayor> getListMayor() {
		return listMayor;
	}

	public void setListMayor(List<Mayor> listMayor) {
		this.listMayor = listMayor;
	}

	public String getTextoAutoCompleteCuentaInicial() {
		return textoAutoCompleteCuentaInicial;
	}

	public void setTextoAutoCompleteCuentaInicial(
			String textoAutoCompleteCuentaInicial) {
		this.textoAutoCompleteCuentaInicial = textoAutoCompleteCuentaInicial;
	}

	public String getTextoAutoCompleteCuentaFinal() {
		return textoAutoCompleteCuentaFinal;
	}

	public void setTextoAutoCompleteCuentaFinal(
			String textoAutoCompleteCuentaFinal) {
		this.textoAutoCompleteCuentaFinal = textoAutoCompleteCuentaFinal;
	}

	public PlanCuenta getBusquedaCuentaInicial() {
		return busquedaCuentaInicial;
	}

	public void setBusquedaCuentaInicial(PlanCuenta busquedaCuentaInicial) {
		this.busquedaCuentaInicial = busquedaCuentaInicial;
	}

	public PlanCuenta getBusquedaCuentaFinal() {
		return busquedaCuentaFinal;
	}

	public void setBusquedaCuentaFinal(PlanCuenta busquedaCuentaFinal) {
		this.busquedaCuentaFinal = busquedaCuentaFinal;
	}

	public String getSeleccionTipoCuenta() {
		return seleccionTipoCuenta;
	}

	public void setSeleccionTipoCuenta(String seleccionTipoCuenta) {
		this.seleccionTipoCuenta = seleccionTipoCuenta;
	}
}

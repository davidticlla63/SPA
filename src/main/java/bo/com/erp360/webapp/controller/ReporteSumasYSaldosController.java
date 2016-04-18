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
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.richfaces.cdi.push.Push;

import bo.com.erp360.webapp.data.MayorRepository;
import bo.com.erp360.webapp.data.MonedaRepository;
import bo.com.erp360.webapp.model.CentroCosto;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.Mayor;
import bo.com.erp360.webapp.model.MonedaEmpresa;
import bo.com.erp360.webapp.model.PlanCuenta;
import bo.com.erp360.webapp.util.EDSumasSaldos;
import bo.com.erp360.webapp.util.Fechas;
import bo.com.erp360.webapp.util.SessionMain;

@Named(value = "reporteSumasYSaldosController")
@ConversationScoped
public class ReporteSumasYSaldosController implements Serializable {

	private static final long serialVersionUID = 6480489020208379295L;

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	@Inject
	Conversation conversation;

	@Inject
	private MonedaRepository monedaRepository;

	@Inject
	private MayorRepository mayorRepository;

	private Logger log = Logger.getLogger(this.getClass());

	//login
	private @Inject SessionMain sessionMain; //variable del login
	private Empresa empresaLogin;
	private Gestion gestionLogin;
	private MonedaEmpresa selectedMonedaEmpresa;

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventSucursal;

	private String nombreTipoComprobante;
	private String nombreMonedaEmpresa;
	private String nombreUsuario;
	private String nombreMes;
	private String nombreSelectFiltro;
	private String urlSumasSaldos;

	private double totalDebeNacional;
	private double totalHaberNacional;

	private double totalDebeExtrajero;
	private double totalHaberExtrajero;

	private double totalDeudorNacional;
	private double totalAcreedorNacional;

	private double totalDeuddorExtranjero;
	private double totalAcreedorExtranjero;

	private List<MonedaEmpresa> listMonedaEmpresa;
	private List<CentroCosto> listCentroCosto = new ArrayList<CentroCosto>();
	private String[] arrayMes = {"ENERO","FEBRERO","MARZO","ABRIL","MAYO","JUNIO","JULIO","AGOSTO","SEPTIEMBRE","OCTUBRE","NOVIEMBRE","DICIEMBRE"};
	private String[] arrayFiltro = {"FECHA","MES"};

	private Date fechaInicial;
	private Date fechaFinal;
	private Date fechaActual;

	//estados
	private boolean seleccionado = false;
	private String tipoConsulta ;

	//component Lazydatamodel Primefaces
	private int first; 
	private int pageSize;
	private int lengthList;

	private LazyDataModel<EDSumasSaldos> listReportes;

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

		loadValuesDefault();		
	}

	@SuppressWarnings("unchecked")
	public void procesarReporte2(){
		lengthList = countTotalRecord();
		setListReportes(new LazyDataModel() {
			@Override
			public List<EDSumasSaldos> load(int first, int pageSize, String sortField,
					SortOrder sortOrder, Map filters) {
				setFirst(first) ;
				setPageSize(pageSize );
				return calcularSumasYSaldos(first,pageSize);
			}
		});
		listReportes.setRowCount(lengthList);
		listReportes.setPageSize(pageSize);
	}

	private List<EDSumasSaldos> listSumasSaldos = new ArrayList<EDSumasSaldos>();

	private List<EDSumasSaldos> calcularSumasYSaldos(int first, int pageSize){
		List<Mayor> listMayor = new ArrayList<Mayor>();
		//		if( tipoConsulta.equals("periodo") ){//periodo
		//			//listAsientoContable = mayorRepository.findByFechas(first,pageSize,obtnerNumeroDeMes(nombreMes), empresaLogin, gestionLogin);
		//		}else{//fecha
		listMayor = mayorRepository.findByFechas(first,pageSize,fechaInicial, fechaFinal, empresaLogin);
		//}
		log.info("listMayor.size() : "+listMayor.size());
		log.info("lengthList : "+lengthList);
		for(Mayor ma: listMayor){
			double debito = ma.getDebitoNacional();
			double credito = ma.getCreditoNacional();
			cargarMontoCuenta(ma.getPlanCuenta(), debito, credito, debito>credito?debito-credito:0, credito>debito?credito - debito:0);
		}
		calcularTotales();
		return listSumasSaldos;
	}

	private void cargarMontoCuenta(PlanCuenta pc, double debe, double haber,double deudor, double acreedor){
		verificarSumasSaldos(pc);
		for(EDSumasSaldos ss: listSumasSaldos){
			if(ss.getPlanCuenta().equals(pc)){
				ss.setDebe(ss.getDebe()+debe);
				ss.setHaber(ss.getHaber()+haber);
				ss.setDeudor(ss.getDeudor()+deudor);
				ss.setAcreedor(ss.getAcreedor()+acreedor);
			}
		}
	}

	private void verificarSumasSaldos(PlanCuenta pc){
		if( ! existe(pc)){
			EDSumasSaldos edSumasSaldos = new EDSumasSaldos();
			edSumasSaldos.setId(pc.getId());
			edSumasSaldos.setPlanCuenta(pc);
			edSumasSaldos.setDebe(0);
			edSumasSaldos.setHaber(0);
			edSumasSaldos.setDeudor(0);
			edSumasSaldos.setAcreedor(0);
			listSumasSaldos.add(edSumasSaldos);
		}
	}

	private boolean existe(PlanCuenta pc){
		for(EDSumasSaldos ed: listSumasSaldos){
			if(ed.getId() == pc.getId()){
				return true;
			}
		}
		return false;
	}

	private void calcularTotales(){
		totalDeudorNacional = 0;
		totalAcreedorNacional = 0;
		totalDebeNacional = 0;
		totalHaberNacional = 0;
		for(EDSumasSaldos ed : listSumasSaldos){
			totalDeudorNacional = totalDeudorNacional + ed.getDeudor();
			totalAcreedorNacional = totalAcreedorNacional + ed.getAcreedor();
			totalDebeNacional = totalDebeNacional + ed.getDebe();
			totalHaberNacional = totalHaberNacional + ed.getHaber();
		}
	}

	private int countTotalRecord(){
		int count = 0;
		count = mayorRepository.countTotalRecordByFechas(fechaInicial, fechaFinal, empresaLogin).intValue();
		return count;
	}

	private void loadValuesDefault(){
		totalDeudorNacional = 0;
		totalAcreedorNacional = 0;

		totalDebeNacional = 0;
		totalHaberNacional = 0;

		totalDebeExtrajero = 0;
		totalHaberExtrajero = 0;

		lengthList = 0;
		seleccionado = false;
		nombreMes = arrayMes[0];
		tipoConsulta = "periodo";
		fechaInicial = new Date();
		fechaFinal = new Date();
		fechaActual = new Date();
		loadFechasByNumeroMes();
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

	public void procesar(){

	}

	// --------- reporte --------

	public String loadURL(){
		try{
			HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();  
			String urlPath = request.getRequestURL().toString();
			urlPath = urlPath.substring(0, urlPath.length() - request.getRequestURI().length()) + request.getContextPath() + "/";
			String urlPDFreporte =urlPath+"ReporteSumasSaldos?pIdGestion="+gestionLogin.getId()+"&pIdEmpresa="+empresaLogin.getId()+"&pFechaInicio="+Fechas.deDateAString(fechaInicial)+"&pFechaFin="+Fechas.deDateAString(fechaFinal)+"&pUsuario="+nombreUsuario;
			log.info("getURL() -> "+urlPDFreporte);
			return urlPDFreporte;
		}catch(Exception e){
			log.error("getURL error: "+e.getMessage());
			return "error";
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

	public void actualizarForm(){
		seleccionado = false;
		urlSumasSaldos = loadURL();
		log.info("cargando......");
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("PF('dlgVistaPreviaSumasSaldos').show();");
	}

	public Integer obtnerNumeroDeMes(String mes){
		for(Integer i=0; i < arrayMes.length;i++){
			if(arrayMes[i].toString().equals(mes)){
				return i +1;
			}
		}
		return -1;
	}

	public void cargarMonedaEmpresaByLocal(String nombre){
		for(MonedaEmpresa me : listMonedaEmpresa){
			if(me.getMoneda().equals(nombre)){
				selectedMonedaEmpresa = me ;
				return;
			}
		}
	}
	// --------------------- get and set---------------------

	public String getNombreTipoComprobante() {
		return nombreTipoComprobante;
	}

	public void setNombreTipoComprobante(String nombreTipoComprobante) {
		this.nombreTipoComprobante = nombreTipoComprobante;
	}

	public List<CentroCosto> getListCentroCosto() {
		return listCentroCosto;
	}

	public void setListCentroCosto(List<CentroCosto> listCentroCosto) {
		this.listCentroCosto = listCentroCosto;
	}

	public String[] getArrayMes() {
		return arrayMes;
	}

	public void setArrayMes(String[] arrayMes) {
		this.arrayMes = arrayMes;
	}

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

	public String getNombreMes() {
		return nombreMes;
	}

	public void setNombreMes(String nombreMes) {
		this.nombreMes = nombreMes;
		loadFechasByPeriodo(nombreMes);
	}

	public List<MonedaEmpresa> getListMonedaEmpresa() {
		return listMonedaEmpresa;
	}

	public void setListMonedaEmpresa(List<MonedaEmpresa> listMonedaEmpresa) {
		this.listMonedaEmpresa = listMonedaEmpresa;
	}

	public String getNombreMonedaEmpresa() {
		return nombreMonedaEmpresa;
	}

	public void setNombreMonedaEmpresa(String nombreMonedaEmpresa) {
		log.info("setNombreMonedaEmpresa : "+nombreMonedaEmpresa);
		this.nombreMonedaEmpresa = nombreMonedaEmpresa;
		cargarMonedaEmpresaByLocal(nombreMonedaEmpresa);
	}

	public String getNombreSelectFiltro() {
		return nombreSelectFiltro;
	}

	public void setNombreSelectFiltro(String nombreSelectFiltro) {
		this.nombreSelectFiltro = nombreSelectFiltro;
	}

	public String[] getArrayFiltro() {
		return arrayFiltro;
	}

	public void setArrayFiltro(String[] arrayFiltro) {
		this.arrayFiltro = arrayFiltro;
	}

	public String getUrlSumasSaldos() {
		return urlSumasSaldos;
	}

	public void setUrlSumasSaldos(String urlSumasSaldos) {
		this.urlSumasSaldos = urlSumasSaldos;
	}

	public String getTipoConsulta() {
		return tipoConsulta;
	}

	public void setTipoConsulta(String tipoConsulta) {
		this.tipoConsulta = tipoConsulta;
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

	public Date getFechaActual() {
		return fechaActual;
	}

	public void setFechaActual(Date fechaActual) {
		this.fechaActual = fechaActual;
	}

	public double getTotalDebeNacional() {
		return totalDebeNacional;
	}

	public void setTotalDebeNacional(double totalDebeNacional) {
		this.totalDebeNacional = totalDebeNacional;
	}

	public double getTotalHaberNacional() {
		return totalHaberNacional;
	}

	public void setTotalHaberNacional(double totalHaberNacional) {
		this.totalHaberNacional = totalHaberNacional;
	}

	public double getTotalDebeExtrajero() {
		return totalDebeExtrajero;
	}

	public void setTotalDebeExtrajero(double totalDebeExtrajero) {
		this.totalDebeExtrajero = totalDebeExtrajero;
	}

	public double getTotalHaberExtrajero() {
		return totalHaberExtrajero;
	}

	public void setTotalHaberExtrajero(double totalHaberExtrajero) {
		this.totalHaberExtrajero = totalHaberExtrajero;
	}

	public void setSelectMonedaEmpresa(MonedaEmpresa selectedMonedaEmpresa){
		this.selectedMonedaEmpresa = selectedMonedaEmpresa;
	}

	public MonedaEmpresa getSelectMonedaEmpresa(){
		return selectedMonedaEmpresa ;
	}

	public double getTotalDeudorNacional() {
		return totalDeudorNacional;
	}

	public void setTotalDeudorNacional(double totalDeuddorNacional) {
		this.totalDeudorNacional = totalDeuddorNacional;
	}

	public double getTotalAcreedorNacional() {
		return totalAcreedorNacional;
	}

	public void setTotalAcreedorNacional(double totalAcreedorNacional) {
		this.totalAcreedorNacional = totalAcreedorNacional;
	}

	public double getTotalAcreedorExtranjero() {
		return totalAcreedorExtranjero;
	}

	public void setTotalAcreedorExtranjero(double totalAcreedorExtranjero) {
		this.totalAcreedorExtranjero = totalAcreedorExtranjero;
	}

	public double getTotalDeuddorExtranjero() {
		return totalDeuddorExtranjero;
	}

	public void setTotalDeuddorExtranjero(double totalDeuddorExtranjero) {
		this.totalDeuddorExtranjero = totalDeuddorExtranjero;
	}

	public LazyDataModel<EDSumasSaldos> getListReportes() {
		return listReportes;
	}

	public void setListReportes(LazyDataModel<EDSumasSaldos> listReportes) {
		this.listReportes = listReportes;
	}

	public List<EDSumasSaldos> getListSumasSaldos() {
		return listSumasSaldos;
	}

	public void setListSumasSaldos(List<EDSumasSaldos> listSumasSaldos) {
		this.listSumasSaldos = listSumasSaldos;
	}
}

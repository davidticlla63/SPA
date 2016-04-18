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

import bo.com.erp360.webapp.data.AsientoContableRepository;
import bo.com.erp360.webapp.data.MonedaRepository;
import bo.com.erp360.webapp.data.TipoComprobanteRepository;
import bo.com.erp360.webapp.model.AsientoContable;
import bo.com.erp360.webapp.model.CentroCosto;
import bo.com.erp360.webapp.model.Comprobante;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.MonedaEmpresa;
import bo.com.erp360.webapp.model.TipoComprobante;
import bo.com.erp360.webapp.util.Fechas;
import bo.com.erp360.webapp.util.SessionMain;

@Named(value = "reporteLibroDiarioController")
@ConversationScoped
public class ReporteLibroDiarioController implements Serializable {

	private static final long serialVersionUID = 3454548849713815003L;

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	@Inject
	Conversation conversation;

	@Inject
	private MonedaRepository monedaRepository;

	@Inject
	private TipoComprobanteRepository tipoComprobanteRepository;

	@Inject
	private AsientoContableRepository asientoContableRepository;

	Logger log = Logger.getLogger(ReporteLibroDiarioController.class);

	//login
	private String nombreUsuario;
	private @Inject SessionMain sessionMain; //variable del login
	private Empresa empresaLogin;
	private Gestion gestionLogin;
	
	private MonedaEmpresa selectedMonedaEmpresa;

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventSucursal;

	private String nombreTipoComprobante;
	private String nombreMonedaEmpresa;
	private String nombreMes;
	private String nombreSelectFiltro;
	private String urlLibroDiario;

	private double totalDebeNacional;
	private double totalHaberNacional;

	private double totalDebeExtrajero;
	private double totalHaberExtrajero;

	private List<Comprobante> listComprobante= new ArrayList<Comprobante>();
	private List<TipoComprobante> listTipoComprobante;
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

	private LazyDataModel<AsientoContable> listReport;


	@PostConstruct
	public void initNewComprobante() {

		System.out.println(" init new initNewComprobante");
		beginConversation();
		nombreUsuario = sessionMain.getUsuarioLoggin().getLogin();
		empresaLogin = sessionMain.getEmpresaLoggin();
		gestionLogin = sessionMain.getGestionLoggin();

		listTipoComprobante = tipoComprobanteRepository.findAllByEmpresa(empresaLogin);
		listMonedaEmpresa = monedaRepository.findMonedaEmpresaAllByEmpresa(empresaLogin);
		nombreMonedaEmpresa = listMonedaEmpresa.get(0).getMoneda().getNombre();
		selectedMonedaEmpresa = listMonedaEmpresa.get(0);
		nombreSelectFiltro = arrayFiltro[1];

		loadValuesDefault();		
	}
	
	@SuppressWarnings("unchecked")
	public void procesarReporte(){
		lengthList = countTotalRecord();
		setListReport(new LazyDataModel() {
			@Override
			public List<AsientoContable> load(int first, int pageSize, String sortField,
					SortOrder sortOrder, Map filters) {
				setFirst(first) ;
				setPageSize(pageSize );
				return obtenerListAsientos(first,pageSize);
			}
		});
		listReport.setRowCount(lengthList);
		listReport.setPageSize(pageSize);
	}

	private List<AsientoContable> listAsientoContable = new ArrayList<AsientoContable>();

	private List<AsientoContable> obtenerListAsientos(int first, int pageSize){

		if( tipoConsulta.equals("periodo") ){//periodo
			listAsientoContable = asientoContableRepository.findByPeridodo(first,pageSize,obtnerNumeroDeMes(nombreMes), empresaLogin, gestionLogin);
		}else{//fecha
			listAsientoContable = asientoContableRepository.findByFechas(first,pageSize,fechaInicial, fechaFinal, empresaLogin);
		}
		calcularTotales();
		return listAsientoContable;
	}

	private void calcularTotales(){
		log.info("calcularTotales() listAsientoContable.size = "+listAsientoContable.size());
		totalDebeNacional = 0;
		totalHaberNacional = 0;
		totalDebeExtrajero = 0;
		totalHaberExtrajero = 0;
		for(AsientoContable ac : listAsientoContable ){
			totalDebeNacional = totalDebeNacional + ac.getDebeNacional();
			totalHaberNacional = totalHaberNacional + ac.getHaberNacional();
			totalDebeExtrajero = totalDebeExtrajero + ac.getDebeExtranjero();
			totalHaberExtrajero = totalHaberExtrajero+ ac.getHaberExtranjero();
		}
	}

	private int countTotalRecord(){
		//if( tipoConsulta.equals("periodo") ){//periodo
		//	return asientoContableRepository.countTotalRecordByPeridodo(obtnerNumeroDeMes(nombreMes),empresaLogin,gestionLogin).intValue();
		//}else{//fecha
			return asientoContableRepository.countTotalRecordByFechas(fechaInicial, fechaFinal, empresaLogin).intValue();
		//}
	}

	private void loadValuesDefault(){
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
			System.out.println("beginning conversation : " + this.conversation);
			conversation.begin();
			System.out.println("---> Init Conversation");
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

	// reporte

	public String loadURL(){
		try{
			HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();  
			String urlPath = request.getRequestURL().toString();
			urlPath = urlPath.substring(0, urlPath.length() - request.getRequestURI().length()) + request.getContextPath() + "/";
			String urlPDFreporte = urlPath+"ReporteLibroDiario?pFechaInicio="+Fechas.deDateAString(fechaInicial)+"&pFechaFin="+Fechas.deDateAString(fechaFinal)+"&pIdGestion="+gestionLogin.getId()+"&pIdEmpresa="+empresaLogin.getId()+"&pIdMonedaEmpresa="+selectedMonedaEmpresa.getId()+"&pUsuario="+nombreUsuario;
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

		log.info("mes = "+mes);
		log.info("year = "+year);
		log.info("month = "+month);
		log.info("day1 = "+day);
		log.info("day2 = "+day2);
		log.info("fechaInicial = "+fechaInicial);
		log.info("fechaFinal = "+fechaFinal);
		log.info("------- fecha actual ------- "+Fechas.getFechaActual());
	}

	public void actualizarForm(){
		seleccionado = false;
		urlLibroDiario = loadURL();
		log.info("cargando......");
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("PF('dlgVistaPreviaLibroDiario').show();");
		//resetearFitrosTabla("formTableComprobante:dataTableComprobante");
		//loadValuesDefaul();
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
	
	// ------------------------------ get and set -----------------------------


	public String getNombreTipoComprobante() {
		return nombreTipoComprobante;
	}

	public void setNombreTipoComprobante(String nombreTipoComprobante) {
		this.nombreTipoComprobante = nombreTipoComprobante;
	}

	public List<TipoComprobante> getListTipoComprobante() {
		return listTipoComprobante;
	}

	public void setListTipoComprobante(List<TipoComprobante> listTipoComprobante) {
		this.listTipoComprobante = listTipoComprobante;
	}

	public List<CentroCosto> getListCentroCosto() {
		return listCentroCosto;
	}

	public void setListCentroCosto(List<CentroCosto> listCentroCosto) {
		this.listCentroCosto = listCentroCosto;
	}

	public List<Comprobante> getListComprobante() {
		return listComprobante;
	}

	public void setListComprobante(List<Comprobante> listComprobante) {
		this.listComprobante = listComprobante;
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

	public String getUrlLibroDiario() {
		return urlLibroDiario;
	}

	public void setUrlLibroDiario(String urlLibroDiario) {
		this.urlLibroDiario = urlLibroDiario;
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

	public LazyDataModel<AsientoContable> getListReport() {
		return listReport;
	}

	public void setListReport(LazyDataModel<AsientoContable> listReport) {
		this.listReport = listReport;
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

}

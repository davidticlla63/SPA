package bo.com.erp360.webapp.controller;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

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
import org.richfaces.cdi.push.Push;

import bo.com.erp360.webapp.data.MayorRepository;
import bo.com.erp360.webapp.data.MonedaRepository;
import bo.com.erp360.webapp.data.PlanCuentaRepository;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.Mayor;
import bo.com.erp360.webapp.model.MonedaEmpresa;
import bo.com.erp360.webapp.model.PlanCuenta;
import bo.com.erp360.webapp.model.Usuario;
import bo.com.erp360.webapp.util.EDBalanceGeneral;
import bo.com.erp360.webapp.util.Fechas;
import bo.com.erp360.webapp.util.SessionMain;

@Named(value = "reporteBalanceGeneralController")
@ConversationScoped
public class ReporteBalanceGeneralController implements Serializable {

	private static final long serialVersionUID = -7819149623543804669L;

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	@Inject
	Conversation conversation;

	@Inject
	private MonedaRepository monedaRepository;

	@Inject
	private PlanCuentaRepository planCuentaRepository;

	@Inject
	private MayorRepository mayorRepository;

	private Logger log = Logger.getLogger(this.getClass());

	//login
	private String nombreUsuario;
	private @Inject SessionMain sessionMain; //variable del login
	private Empresa empresaLogin;
	private Gestion gestionLogin;
	private Usuario usuarioSession;
	private MonedaEmpresa selectedMonedaEmpresa;

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventSucursal;

	private String nombreTipoComprobante;
	private String nombreMonedaEmpresa;
	private String nombreMes;
	private String nombreSelectFiltro;
	private String urlBalanceGeneral;

	private double totalDebeNacional;
	private double totalHaberNacional;

	private double totalDebeExtrajero;
	private double totalHaberExtrajero;

	private List<EDBalanceGeneral> listEDBalanceGeneral = new ArrayList<>();
	private List<MonedaEmpresa> listMonedaEmpresa;
	private List<PlanCuenta> listPlanCuenta = new ArrayList<PlanCuenta>();
	private String[] arrayMes = {"ENERO","FEBRERO","MARZO","ABRIL","MAYO","JUNIO","JULIO","AGOSTO","SEPTIEMBRE","OCTUBRE","NOVIEMBRE","DICIEMBRE"};
	private String[] arrayFiltro = {"FECHA","MES"};

	private Date fechaInicial;
	private Date fechaFinal;
	private Date fechaActual;

	//estados
	private boolean seleccionado = false;
	private String tipoConsulta ;

	@PostConstruct
	public void initNewBalanceGeneral() {

		log.info(" init new initNewBalanceGeneral");
		beginConversation();
		nombreUsuario = sessionMain.getUsuarioLoggin().getLogin();
		empresaLogin = sessionMain.getEmpresaLoggin();
		gestionLogin = sessionMain.getGestionLoggin();
		usuarioSession = sessionMain.getUsuarioLoggin();

		listMonedaEmpresa = monedaRepository.findMonedaEmpresaAllByEmpresa(empresaLogin);
		nombreMonedaEmpresa = listMonedaEmpresa.get(0).getMoneda().getNombre();
		selectedMonedaEmpresa = listMonedaEmpresa.get(0);
		nombreSelectFiltro = arrayFiltro[1];

		loadValuesDefault();		
	}

	public void procesarReporte(){
		listPlanCuenta = planCuentaRepository.findAllActivoByEmpresa(empresaLogin);
		for(PlanCuenta pc : listPlanCuenta){
			String tipo = String.valueOf(pc.getTipoCuenta()==null?"0":pc.getTipoCuenta());
			if(tipo.equals("ACTIVO") || tipo.equals("PASIVO")  || tipo.equals("PATRIMONIO")  ){
				List<Mayor> listMayor = mayorRepository.findByFechaAndPlanCuenta( fechaInicial, fechaFinal, pc);
				EDBalanceGeneral ed = new EDBalanceGeneral();
				ed.setPlanCuenta(pc);
				double totalDebeNacional = 0;
				double totalHaberNacional = 0;
				double totalDebeExtranjero = 0;
				double totalHaberExtranjero = 0;
				for(Mayor m: listMayor){
					totalDebeNacional = totalDebeNacional+ m.getDebitoNacional();
					totalHaberNacional = totalHaberNacional + m.getCreditoNacional();

					totalDebeExtranjero = totalDebeExtranjero+ m.getDebitoExtranjero();
					totalHaberExtranjero = totalHaberExtranjero + m.getCreditoExtranjero();
				}
				double totalMayorNacional = totalDebeNacional>totalHaberNacional?totalDebeNacional-totalHaberNacional:totalHaberNacional - totalDebeNacional;
				double totalMayorExtranjero = totalDebeExtranjero>totalHaberNacional?totalDebeNacional-totalHaberNacional:totalHaberNacional - totalDebeNacional;

				ed.setTotalNacional( (totalMayorNacional<0)?totalMayorNacional*(-1):totalMayorNacional);
				ed.setTotalExtranjero( (totalMayorExtranjero<0)?totalMayorExtranjero*(-1):totalMayorExtranjero);
				listEDBalanceGeneral.add(ed);
			}
		}
	}

	private void loadValuesDefault(){
		totalDebeNacional = 0;
		totalHaberNacional = 0;

		totalDebeExtrajero = 0;
		totalHaberExtrajero = 0;

		//lengthList = 0;
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
			String urlPDFreporte = urlPath+"ReporteBalanceGeneral?pFechaInicio="+Fechas.deDateAString(fechaInicial)+"&pFechaFin="+Fechas.deDateAString(fechaFinal)+"&pIdGestion="+gestionLogin.getId()+"&pIdEmpresa="+empresaLogin.getId()+"&pUsuario="+nombreUsuario;
			log.info("getURL() -> "+urlPDFreporte);
			return urlPDFreporte;
		}catch(Exception e){
			System.out.println("getURL error: "+e.getMessage());
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
		urlBalanceGeneral = loadURL();
		log.info("cargando......");
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("PF('dlgVistaPreviaBalanceGeneral').show();");
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
	// get and set

	public Usuario getUsuario() {
		return usuarioSession;
	}

	public void setUsuario(Usuario usuarioSession) {
		this.usuarioSession = usuarioSession;
	}

	public String getNombreTipoComprobante() {
		return nombreTipoComprobante;
	}

	public void setNombreTipoComprobante(String nombreTipoComprobante) {
		this.nombreTipoComprobante = nombreTipoComprobante;
	}

	public List<PlanCuenta> getListPlanCuenta() {
		return listPlanCuenta;
	}

	public void setListPlanCuenta(List<PlanCuenta> listPlanCuenta) {
		this.listPlanCuenta = listPlanCuenta;
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

	public String getUrlBalanceGeneral() {
		return urlBalanceGeneral;
	}

	public void setUrlBalanceGeneral(String urlBalanceGeneral) {
		this.urlBalanceGeneral = urlBalanceGeneral;
	}

	public String getTipoConsulta() {
		return tipoConsulta;
	}

	public void setTipoConsulta(String tipoConsulta) {
		this.tipoConsulta = tipoConsulta;
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

	public List<EDBalanceGeneral> getListEDBalanceGeneral() {
		return listEDBalanceGeneral;
	}

	public void setListEDBalanceGeneral(List<EDBalanceGeneral> listEDBalanceGeneral) {
		this.listEDBalanceGeneral = listEDBalanceGeneral;
	}
}


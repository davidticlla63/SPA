package bo.com.erp360.webapp.controller;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.event.Event;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.component.datatable.DataTable;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.PieChartModel;
import org.richfaces.cdi.push.Push;

import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.util.SessionMain;

@Named(value = "dashboardController")
@ConversationScoped
public class DashboardController implements Serializable {

	private static final long serialVersionUID = -356224018463030806L;

	public static final String PUSH_CDI_TOPIC = "pushCdi";
	
	@Inject
	Conversation conversation;

	Logger log = Logger.getLogger(DashboardController.class);

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventSucursal;

	//estados
	private boolean crear = true;
	private boolean registrar = false;
	private boolean modificar = false;

	private String tituloPanel = "Registrar Proveedor";
	private String nombreEstado="ACTIVO";


	//login
	private @Inject SessionMain sessionMain; //variable del login
	private String nombreUsuario;	
	private Empresa empresaLogin;
	private Gestion gestionLogin;
	
	private PieChartModel pieModel;
	private Map<String,Integer> agents;
	private BarChartModel animatedModel2;


	@PostConstruct
	public void initNewDashboard() {
		log.info(" init new initNewDashboard");
		beginConversation();		
		nombreUsuario = sessionMain.getUsuarioLoggin().getLogin();
		empresaLogin = sessionMain.getEmpresaLoggin();
		gestionLogin = sessionMain.getGestionLoggin();
		loadValuesDefaul();
		createAnimatedModels();
	}

	private void loadValuesDefaul(){
		// tituloPanel
		tituloPanel = "Dashboard";
		
		pieModel = new PieChartModel();
        pieModel.setData(cargarMapAgents());
        pieModel.setTitle("ESTADO DE LAS CUENTAS");
        pieModel.setShowDataLabels(true);
        pieModel.setLegendPosition("w");
	}
	
	private void createAnimatedModels() {
		animatedModel2 = initBarModel();
        animatedModel2.setTitle("ESTADOS");
        animatedModel2.setAnimate(true);
        animatedModel2.setLegendPosition("ne");
        Axis yAxis = animatedModel2.getAxis(AxisType.Y);
        yAxis.setMin(0);
        yAxis.setMax(200);
	}

	private BarChartModel initBarModel() {
        BarChartModel model = new BarChartModel();
 
        ChartSeries boys = new ChartSeries();
        boys.setLabel("VENTAS NETAS");
        boys.set("2004", 120);
        boys.set("2005", 100);
        boys.set("2006", 44);
        boys.set("2007", 150);
        boys.set("2008", 25);
 
        ChartSeries girls = new ChartSeries();
        girls.setLabel("VENTAS GENERALES");
        girls.set("2004", 52);
        girls.set("2005", 60);
        girls.set("2006", 110);
        girls.set("2007", 135);
        girls.set("2008", 120);
 
        model.addSeries(boys);
        model.addSeries(girls);
         
        return model;
    }
	private Map cargarMapAgents(){
		agents = new LinkedHashMap<String, Integer>();
		agents.put("PASIVO", 2);
        agents.put("ACTIVO", 30);
        agents.put("INGRESO", 51);
        agents.put("EGRESO", 9);
        agents.put("OTROS", 8);
        return agents;
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

	public void cambiarAspecto(){
		crear = false;
		registrar = true;
		modificar = false;
	}

	public void actualizarFormReg(){
		crear = true;
		registrar = false;
		modificar = false;
	}

	//  ---- get and set -----
	public String getTituloPanel() {
		return tituloPanel;
	}

	public void setTituloPanel(String tituloPanel) {
		this.tituloPanel = tituloPanel;
	}

	public boolean isModificar() {
		return modificar;
	}

	public void setModificar(boolean modificar) {
		this.modificar = modificar;
	}

	public String getTest(){
		return "test";
	}

	public Empresa getEmpresaLogin() {
		return empresaLogin;
	}

	public void setEmpresaLogin(Empresa empresaLogin) {
		this.empresaLogin = empresaLogin;
	}

	public String getNombreEstado() {
		return nombreEstado;
	}

	public void setNombreEstado(String nombreEstado) {
		this.nombreEstado = nombreEstado;
	}

	public boolean isCrear() {
		return crear;
	}

	public void setCrear(boolean crear) {
		this.crear = crear;
	}

	public boolean isRegistrar() {
		return registrar;
	}

	public void setRegistrar(boolean registrar) {
		this.registrar = registrar;
	}

	public String getNombreUsuario() {
		return nombreUsuario;
	}

	public void setNombreUsuario(String nombreUsuario) {
		this.nombreUsuario = nombreUsuario;
	}

	public PieChartModel getPieModel() {
		return pieModel;
	}

	public void setPieModel(PieChartModel pieModel) {
		this.pieModel = pieModel;
	}

	public BarChartModel getAnimatedModel2() {
		return animatedModel2;
	}

	public void setAnimatedModel2(BarChartModel animatedModel2) {
		this.animatedModel2 = animatedModel2;
	}

}

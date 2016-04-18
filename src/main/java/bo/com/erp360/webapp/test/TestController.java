package bo.com.erp360.webapp.test;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.richfaces.cdi.push.Push;

import bo.com.erp360.webapp.model.Test;

@Named(value = "testController")
@SuppressWarnings("serial")
@ConversationScoped
public class TestController implements Serializable {

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	Conversation conversation;

	@Inject
	private TestRepository testRepository2; 

	@Inject
	private TestRegistration testRegistration; 

	Logger log = Logger.getLogger(TestController.class);

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventSucursal;

	private int inicio; 
	private int tamanhoPagina;
	private int tamanhoLista;

	private LazyDataModel<Test> listar;
	private Test newTest;

	@SuppressWarnings("unchecked")
	@PostConstruct
	public void initNewTest() {
		log.info("init initNewTest()");
		beginConversation();
		tamanhoLista = testRepository2.countTotalRecord().intValue();
		listar = new LazyDataModel() {
			@Override
			public List<Test> load(int first, int pageSize, String sortField,
					SortOrder sortOrder, Map filters) {
				setInicio(first);
				setTamanhoPagina(pageSize);
				return testRepository2.findAllBySize(getInicio(),getTamanhoPagina());
			}
		};
		listar.setRowCount(tamanhoLista);
        listar.setPageSize(getTamanhoPagina());
		//testRegister();

	}

	private void testRegister(){
		Test t = new Test();
		t.setId(0);
		t.setDetail("detail");
		t.setNombre("detail");
		t.setState("AC");
		t.setFechaRegistro(new Date());
		testRegistration.create(t);
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

	//----------------------------------------------------------------------

	public Test getNewTest() {
		return newTest;
	}

	public int getInicio() {
		return inicio;
	}

	public void setInicio(int inicio) {
		this.inicio = inicio;
	}

	public int getTamanhoPagina() {
		return tamanhoPagina;
	}

	public void setTamanhoPagina(int tamanhoPagina) {
		this.tamanhoPagina = tamanhoPagina;
	}

	public int getTamanhoLista() {
		return tamanhoLista;
	}

	public void setTamanhoLista(int tamanhoLista) {
		this.tamanhoLista = tamanhoLista;
	}

	public LazyDataModel<Test> getListar() {
		return listar;
	}

	public void setListar(LazyDataModel<Test>  listar) {
		this.listar = listar;
	}

}

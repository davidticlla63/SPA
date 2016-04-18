package bo.com.erp360.webapp.util;

import bo.com.erp360.webapp.model.Cliente;
import bo.com.erp360.webapp.model.security.Accion;
import bo.com.erp360.webapp.model.security.Pagina;

public class EDAccion {
	
	private Accion accion;
	private Pagina pagina;
	
	public EDAccion(){
		
	}

	public Accion getAccion() {
		return accion;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj==null){
			return false;
		}else{
			if(!(obj instanceof EDAccion)){
				return false;
			}else{
				if( (( (EDAccion)obj).getAccion().getId() == this.accion.getId()) && (( (EDAccion)obj).getPagina().getId() ==this.getPagina().getId())){
					return true;
				}else{
					return false;
				}
			}
		}
	}

	public void setAccion(Accion accion) {
		this.accion = accion;
	}

	public Pagina getPagina() {
		return pagina;
	}

	public void setPagina(Pagina pagina) {
		this.pagina = pagina;
	}
	
	
	

}

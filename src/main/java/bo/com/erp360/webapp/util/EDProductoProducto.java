package bo.com.erp360.webapp.util;

import java.util.ArrayList;
import java.util.List;

import com.ibm.icu.impl.duration.impl.DataRecord.EDecimalHandling;

import bo.com.erp360.webapp.model.Producto;
import bo.com.erp360.webapp.model.TipoProducto;

public class EDProductoProducto {
	private TipoProducto tipoProducto;
	private List<EDProducto> listEDProducto= new ArrayList<EDProducto>();
	private int pagina=0;


	public TipoProducto getTipoProducto() {
		return tipoProducto;
	}

	public void setTipoProducto(TipoProducto tipoProducto) {
		this.tipoProducto = tipoProducto;
	}

	public List<EDProducto> getListEDProducto() {
		return listEDProducto;
	}

	public void setListEDProducto(List<EDProducto> listProducto) {
		this.listEDProducto = listProducto;
	}

	public int getPagina() {
		return pagina;
	}

	public void setPagina(int pagina) {
		this.pagina = pagina;
	}

	
}

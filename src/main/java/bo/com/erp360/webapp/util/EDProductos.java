package bo.com.erp360.webapp.util;

import java.io.Serializable;

import bo.com.erp360.webapp.model.Producto;

public class EDProductos  implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1596691923521456177L;
	private Producto producto = new Producto();
	private double cantidad = 0;
	private boolean media = false;

	


	public EDProductos(Producto producto, double cantidad, boolean media) {
		super();
		this.producto = producto;
		this.cantidad = cantidad;
		this.media = media;
	}

	public double getCantidad() {
		return cantidad;
	}

	public void setCantidad(double cantidad) {
		this.cantidad = cantidad;
	}

	public boolean isMedia() {
		return media;
	}

	public void setMedia(boolean media) {
		this.media = media;
	}

	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
	}

	

}

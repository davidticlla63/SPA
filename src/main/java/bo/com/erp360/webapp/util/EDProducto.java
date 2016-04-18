package bo.com.erp360.webapp.util;

import bo.com.erp360.webapp.model.Producto;

public class EDProducto {
	private Producto producto = new Producto();
	private double cantidad = 0;
	private boolean media = false;
	private String nombre;
	private int pagina=0;

	

	@Override
	public String toString() {
		return "EDProducto [producto=" + producto.getNombre() + ", cantidad=" + cantidad
				+ ", media=" + media + ", nombre=" + nombre + "]";
	}

	public double getCantidad() {
		return cantidad;
	}

	public void setCantidad(double cantidad) {
		this.cantidad = cantidad;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
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

	public int getPagina() {
		return pagina;
	}

	public void setPagina(int pagina) {
		this.pagina = pagina;
	}

}

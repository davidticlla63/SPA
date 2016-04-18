package bo.com.erp360.webapp.util;

import bo.com.erp360.webapp.model.BienServicio;

public class EDOrdenCompra {

	private Integer id;
	private String bienServicio;
	private double precio;
	private Integer cantidad;
	private double subTotal;
	private BienServicio objectBienServicio;
	private double descuento;
	
	public EDOrdenCompra(){
		super();
		this.id = 0;
		this.bienServicio = "";
		this.precio = 0;
		this.cantidad = 0;
		this.subTotal = 0;
		objectBienServicio = new BienServicio();
		this.descuento = 0; 
	}
	
	public EDOrdenCompra(Integer id,String bienServicio, double precio, Integer cantidad,
			double subTotal,BienServicio objectBienServicio,double descuento) {
		super();
		this.id = id;
		this.bienServicio = bienServicio;
		this.precio = precio;
		this.cantidad = cantidad;
		this.subTotal = subTotal;
		this.objectBienServicio = objectBienServicio;
		this.descuento = descuento;
	}

	public String getBienServicio() {
		return bienServicio;
	}
	
	public void setBienServicio(String bienServicio) {
		this.bienServicio = bienServicio;
	}
	
	public double getPrecio() {
		return precio;
	}
	
	public void setPrecio(double precio) {
		this.precio = precio;
	}
	
	public Integer getCantidad() {
		return cantidad;
	}
	
	public void setCantidad(Integer cantidad) {
		this.cantidad = cantidad;
	}
	
	public double getSubTotal() {
		return subTotal;
	}
	
	public void setSubTotal(double subTotal) {
		this.subTotal = subTotal;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public BienServicio getObjectBienServicio() {
		return objectBienServicio;
	}

	public void setObjectBienServicio(BienServicio objectBienServicio) {
		this.objectBienServicio = objectBienServicio;
	}

	public double getDescuento() {
		return descuento;
	}

	public void setDescuento(double descuento) {
		this.descuento = descuento;
	}
	
}

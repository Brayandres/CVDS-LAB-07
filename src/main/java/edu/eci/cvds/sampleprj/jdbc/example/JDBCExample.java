/*
 * Copyright (C) 2015 hcadavid
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.eci.cvds.sampleprj.jdbc.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mysql.jdbc.PreparedStatement;

/**
 *
 * @author hcadavid
 */
public class JDBCExample {

	public static void main(String args[]) {
		try {
			String url = "jdbc:mysql://desarrollo.is.escuelaing.edu.co:3306/bdprueba";
			String driver = "com.mysql.jdbc.Driver";
			String user = "bdprueba";
			String pwd = "prueba2019";

			Class.forName(driver);
			Connection con = DriverManager.getConnection(url, user, pwd);
			con.setAutoCommit(false);

			System.out.println("Valor total pedido 1:" + valorTotalPedido(con, 1));

			List<String> prodsPedido = nombresProductosPedido(con, 1);

			System.out.println("Productos del pedido 1:");
			System.out.println("-----------------------");
			for (String nomprod : prodsPedido) {
				System.out.println(nomprod);
			}
			System.out.println("-----------------------");

			int suCodigoECI = 2131039;
			registrarNuevoProducto(con, suCodigoECI, "BM", 777);
			con.commit();

			con.close();

		} catch (ClassNotFoundException | SQLException ex) {
			Logger.getLogger(JDBCExample.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

	/**
	 * Agregar un nuevo producto con los par??metros dados
	 * 
	 * @param con la conexi??n JDBC
	 * @param codigo
	 * @param nombre
	 * @param precio
	 * @throws SQLException
	 */
	public static void registrarNuevoProducto(Connection con, int codigo, String nombre, int precio)
	throws SQLException {

		PreparedStatement insertProductos = null;
		String insertar = "INSERT INTO ORD_PRODUCTOS VALUES(?,?,?)";
		insertProductos = (PreparedStatement) con.prepareStatement(insertar);

		insertProductos.setInt(1, codigo);
		insertProductos.setString(2, nombre);
		insertProductos.setInt(3, precio);
		insertProductos.execute();

		con.commit();
	}

	/**
	 * Consultar los nombres de los productos asociados a un pedido
	 * 
	 * @param con          la conexi??n JDBC
	 * @param codigoPedido el c??digo del pedido
	 * @return
	 * @throws SQLException
	 */
	public static List<String> nombresProductosPedido(Connection con, int codigoPedido) throws SQLException {
		List<String> np = new LinkedList<>();

		PreparedStatement updateNames = null;
		String updateString = ""
				+ "SELECT nombre "
				+ "FROM ORD_PRODUCTOS "
				+ "JOIN ORD_DETALLE_PEDIDO  ON ORD_PRODUCTOS.codigo=ORD_DETALLE_PEDIDO.producto_fk "
				+ "WHERE ORD_PRODUCTOS.codigo=?";
		con.setAutoCommit(false);
		updateNames = (PreparedStatement) con.prepareStatement(updateString);
		updateNames.setInt(1, codigoPedido);
		ResultSet rs = updateNames.executeQuery();
		while (rs.next()) {
			np.add(rs.getString(1));
		}
		updateNames.close();
		rs.close();

		return np;
	}

	/**
	 * Calcular el costo total de un pedido
	 * 
	 * @param con
	 * @param codigoPedido c??digo del pedido cuyo total se calcular??
	 * @return el costo total del pedido (suma de: cantidades*precios)
	 * @throws SQLException
	 */
	public static int valorTotalPedido(Connection con, int codigoPedido) throws SQLException {
		PreparedStatement calculoStatement = null;

		String calcule = ""
				+ "SELECT SUM(precio*cantidad) "
				+ "FROM ORD_PEDIDOS JOIN ORD_DETALLE_PEDIDO ON ORD_PEDIDOS.codigo=ORD_DETALLE_PEDIDO.pedido_fk "
				+ "JOIN ORD_PRODUCTOS ON ORD_PRODUCTOS.codigo=producto_fk "
				+ "WHERE ORD_PEDIDOS.codigo=?";
		calculoStatement = (PreparedStatement) con.prepareStatement(calcule);
		calculoStatement.setInt(1, codigoPedido);
		ResultSet rs = calculoStatement.executeQuery();
		int valor = 0;
		while (rs.next()) {
			valor = rs.getInt(1);
		}
		calculoStatement.close();
		rs.close();
		return valor;
	}
}
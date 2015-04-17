package crud.ui

import java.awt.BorderLayout
import java.rmi.Naming
import javax.swing._

import crud.server.RemoteCrud

object CrudApp extends App {
  val serverHostName =
    JOptionPane.showInputDialog(null, "Please enter server host name:", "Connect", JOptionPane.QUESTION_MESSAGE, null, null, "localhost")
  val remoteCrud = Naming.lookup("//"+serverHostName+"/remoteCrud").asInstanceOf[RemoteCrud]

  val orderListPanel = new OrderListPanel(remoteCrud.select())
  val orderAddEditPanel = new OrderAddEditPanel(remoteCrud, orderListPanel.selectedOrder)
  val orderRemovePanel = new OrderRemovePanel(orderListPanel.selectedOrder)
  
  // Connect orderAddPanel and orderRemovePanel to table
  remoteCrud.connectInsert(orderAddEditPanel.nextOrders)
  remoteCrud.connectRemove(orderRemovePanel.removeOrders)

  // Setup application window
  val window = new JFrame("SharedCRUD Orders")
  window.setLayout(new BorderLayout())
  window.add(orderAddEditPanel, BorderLayout.NORTH)
  window.add(orderListPanel, BorderLayout.CENTER)
  window.add(orderRemovePanel, BorderLayout.SOUTH)
  // window configuration
  window.pack()
  window.setLocationRelativeTo(null)
  window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
  window.setVisible(true)
}

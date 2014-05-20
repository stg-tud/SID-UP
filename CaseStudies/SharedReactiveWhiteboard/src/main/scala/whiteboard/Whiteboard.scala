package whiteboard

import whiteboard.ui.panels.{DrawingPanel, ShapeSelectionPanel, StrokeInputPanel}
import javax.swing._
import java.awt.{Dimension, BorderLayout}
import java.awt.event.{WindowEvent, WindowAdapter}
import java.rmi.Naming
import whiteboard.WhiteboardServer.RemoteWhiteboard
import reactive.events.EventStream
import reactive.remote.impl.{RemoteSignalSourceImpl, RemoteEventSourceImpl, RemoteSignalSinkImpl}
import reactive.mutex.{LockSignal, LockEventStream, TransactionLock}


object Whiteboard {
  val strokeInputPanel = new StrokeInputPanel
  val shapeSelectionPanel = new ShapeSelectionPanel
  val drawingPanel = new DrawingPanel(
    shapeSelectionPanel.nextShapeFactory,
    strokeInputPanel.nextStrokeWidth,
    strokeInputPanel.nextColor
  )

  val serverHostName =
    JOptionPane.showInputDialog(null, "Please enter server host name:", "Connect", JOptionPane.QUESTION_MESSAGE)
  val remoteWhiteboard = Naming.lookup("//"+serverHostName+"/remoteWhiteboard").asInstanceOf[RemoteWhiteboard]
  val remoteLock = remoteWhiteboard.lock()

  val newShapesCommands: EventStream[Command] =
    drawingPanel.newShapes.map[Command] { ShapeCommand } merge shapeSelectionPanel.clearCommands

  val lockedShapeCommands = new LockEventStream(newShapesCommands, remoteLock)
  val lockedCurrentShape = new LockSignal(drawingPanel.constructingShape, remoteLock)

  val shapesRemote = new RemoteSignalSinkImpl(remoteWhiteboard.connectShapes(
    new RemoteEventSourceImpl(lockedShapeCommands),
    Some(new RemoteSignalSourceImpl(lockedCurrentShape)))
  )
  drawingPanel.shapes << shapesRemote

  def main(args: Array[String]): Unit = {
    makeWindow("Whiteboard", 1000, 600)(
      drawingPanel.asComponent -> BorderLayout.CENTER,
      strokeInputPanel -> BorderLayout.NORTH,
      shapeSelectionPanel -> BorderLayout.WEST
    ).addWindowListener(new WindowAdapter {
      override def windowClosing(e: WindowEvent) = {
        shapesRemote.disconnect()
      }
    })
  }

  def makeWindow(name: String, width: Int, height: Int)(components: (JComponent, String)*) = {
    val window = new JFrame(name)
    window.setPreferredSize(new Dimension(width, height))
    window.setLayout(new BorderLayout())

    for ((component, direction) <- components) {
      window.add(component, direction)
    }

    window.pack()
    window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
    window.setVisible(true)
    window
  }
}
